package com.example.kasirkita.repository

import com.example.kasirkita.data.SupabaseClientProvider
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val role: String
)

class AuthRepository {

    private val supabase = SupabaseClientProvider.client
    private val supabaseUrl = "https://dljctesyiguinubiwzwq.supabase.co"
    private val supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRsamN0ZXN5aWd1aW51Yml3endxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzMxOTUyODcsImV4cCI6MjA4ODc3MTI4N30.wvD-Ou7E5-eSDO-9TGudJTpKUJA58d7Ld_DzldLqLyg"

    val sessionStatus: Flow<SessionStatus> = supabase.auth.sessionStatus

    suspend fun login(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun logout() {
        supabase.auth.signOut()
    }

    suspend fun isLoggedIn(): Boolean {
        try {
            supabase.auth.awaitInitialization()
        } catch (e: Exception) {}
        return supabase.auth.currentSessionOrNull() != null
    }

    suspend fun awaitAuthInitialization() {
        supabase.auth.awaitInitialization()
    }

    // Fungsi baru: ambil profile user yang sedang login
    suspend fun getUserProfile(): UserProfile? {
        val userId = supabase.auth.currentSessionOrNull()?.user?.id ?: return null
        return try {
            supabase.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserProfile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfileName(name: String) {
        val userId = supabase.auth.currentSessionOrNull()?.user?.id ?: return
        supabase.from("profiles").update(
            com.example.kasirkita.model.ProfileUpdate(name = name)
        ) {
            filter {
                eq("id", userId)
            }
        }
    }

    /**
     * Register kasir oleh owner tanpa mengganggu session owner.
     * Menggunakan Shadow Client dengan session yang tidak disimpan.
     */
    suspend fun registerKasir(email: String, password: String, name: String) {
        // 1. Buat Shadow Client (Tanpa Session Persistence)
        val tempClient = createSupabaseClient(supabaseUrl, supabaseKey) {
            install(Auth)
        }

        try {
            // 2. Lakukan pendaftaran diam-diam
            tempClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                // Metadata ini akan dibaca oleh Database Trigger untuk isi public.profiles
                data = buildJsonObject {
                    put("full_name", name)
                }
            }
        } finally {
            // 3. Hancurkan client sementara
            tempClient.close()
        }
    }
}
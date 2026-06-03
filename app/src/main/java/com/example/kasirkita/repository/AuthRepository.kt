package com.example.kasirkita.repository

import com.example.kasirkita.data.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
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

    val sessionStatus: Flow<SessionStatus> = supabase.auth.sessionStatus

    suspend fun register(email: String, password: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

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
            supabase.postgrest["profiles"]
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

    // Fungsi baru: register kasir oleh owner
    suspend fun registerKasir(email: String, password: String, name: String) {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("name", name)
                put("role", "cashier")
            }
        }
        supabase.auth.signOut()
    }
}
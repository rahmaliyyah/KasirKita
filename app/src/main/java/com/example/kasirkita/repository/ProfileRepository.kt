package com.example.kasirkita.repository

import com.example.kasirkita.data.SupabaseClientProvider
import com.example.kasirkita.model.ProfileInsert
import com.example.kasirkita.model.ProfileUpdate
import com.example.kasirkita.repository.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class ProfileRepository {

    private val supabase = SupabaseClientProvider.client

    /*
     * Ambil semua profil.
     * RLS otomatis memfilter:
     *   - Owner: lihat semua profil
     *   - Cashier: hanya profil miliknya sendiri
     */
    suspend fun getProfiles(): List<UserProfile> {
        return supabase
            .from("profiles")
            .select()
            .decodeList<UserProfile>()
    }

    /*
     * Ambil profil berdasarkan id.
     */
    suspend fun getProfileById(id: String): UserProfile {
        return supabase
            .from("profiles")
            .select { filter { eq("id", id) } }
            .decodeSingle<UserProfile>()
    }

    /*
     * Buat profil baru — hanya owner yang bisa (dijaga RLS).
     * Dipanggil setelah registerKasir berhasil buat akun auth.
     */
    suspend fun createProfile(id: String, name: String, role: String) {
        supabase
            .from("profiles")
            .insert(
                ProfileInsert(
                    id = id,
                    name = name,
                    role = role
                )
            )
    }

    /*
     * Update nama atau role profil — hanya owner yang bisa (dijaga RLS).
     */
    suspend fun updateProfile(id: String, name: String? = null, role: String? = null) {
        supabase
            .from("profiles")
            .update(ProfileUpdate(name = name, role = role)) {
                filter { eq("id", id) }
            }
    }

    /*
     * Ambil ID user yang sedang login.
     */
    fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("User belum login")
    }
}
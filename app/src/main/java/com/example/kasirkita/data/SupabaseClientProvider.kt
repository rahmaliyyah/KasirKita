package com.example.kasirkita.data
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth

object SupabaseClientProvider {

    /*
     * object digunakan agar Supabase client cukup dibuat satu kali.
     * Ini mirip singleton sederhana di Kotlin.
     */
    val client = createSupabaseClient(
        supabaseUrl = "https://dljctesyiguinubiwzwq.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRsamN0ZXN5aWd1aW51Yml3endxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzMxOTUyODcsImV4cCI6MjA4ODc3MTI4N30.wvD-Ou7E5-eSDO-9TGudJTpKUJA58d7Ld_DzldLqLyg"
    ) {
        /*
         * install(Auth) digunakan agar aplikasi bisa memakai fitur autentikasi,
         * seperti login, register, logout, dan membaca session user.
         */
        install(Auth)
    }
}

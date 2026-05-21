package com.si.myfoodordering.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseConfig {
    const val SUPABASE_URL = "https://wlrcoqdetwgjvarrmftl.supabase.co"
    const val SUPABASE_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndscmNvcWRldHdnanZhcnJtZnRsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgwOTI5MjcsImV4cCI6MjA5MzY2ODkyN30.Z2NKOKZSpvw0jluxoQtIGyY1K920kRoVKV-3_Jg0l3A"

    val client by lazy {
        try {
            createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_KEY
            ) {
                install(Auth)
                install(Postgrest)
                install(Storage)
                install(Realtime)
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

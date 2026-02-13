package com.exapps.gemgpt

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

val supabase = createSupabaseClient(
    supabaseUrl = "https://riyndypamffzgeybnnsn.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJpeW5keXBhbWZmemdleWJubnNuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzA5Nzk3MjYsImV4cCI6MjA4NjU1NTcyNn0.lIZfdj3S3vYVFgVJTL0nkjIAzYpL2iHrpzIWCTZsXKw"
) {
    install(Postgrest)
    install(GoTrue)
    install(Storage)
}
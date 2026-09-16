package com.scrap2stack.app.core.network

import io.github.jan.supabase.auth.auth
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds the Supabase JWT token to the Authorization header
 * for Retrofit requests.
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Get the current session token directly from Supabase
        val token = try {
            supabase.auth.currentSessionOrNull()?.accessToken
        } catch (e: Exception) {
            null
        }

        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
            // Supabase backend might also require the API Key (Anon Key) in headers
            addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
        }.build()

        return chain.proceed(request)
    }
}

package com.scrap2stack.app.core.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // We use runBlocking here because Interceptor is a synchronous API.
        // We use .first() to ensure we wait for the first emitted value from DataStore.
        val token = try {
            runBlocking {
                sessionManager.authToken.first()
            }
        } catch (e: Exception) {
            null
        }

        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        return chain.proceed(request)
    }
}

package com.vybesxapp.service.api

import android.content.Context
import com.vybesxapp.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    private val sessionManager: SessionManager = SessionManager(context)
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        sessionManager.getAccessToken()?.let {
            requestBuilder.header("X-App-Version", "1.23")
            requestBuilder.header("X-Platform", "Android")
            requestBuilder.addHeader("Authorization", "bearer $it")
        }
        return chain.proceed(requestBuilder.build())
    }
}
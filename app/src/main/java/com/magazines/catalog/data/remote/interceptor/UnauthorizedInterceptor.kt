package com.magazines.catalog.data.remote.interceptor

import com.magazines.catalog.data.remote.session.UnauthorizedHandler
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UnauthorizedInterceptor @Inject constructor(
    private val unauthorizedHandler: UnauthorizedHandler,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401 && !isAuthEndpoint(request.url.encodedPath)) {
            unauthorizedHandler.handleUnauthorized()
        }

        return response
    }

    private fun isAuthEndpoint(path: String): Boolean {
        return path.contains("/api/auth/login") ||
            path.contains("/api/auth/register")
    }
}

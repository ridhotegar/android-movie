package com.ridhotegar.androidmovie.data.api

import com.ridhotegar.androidmovie.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class TmdbInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .header("Accept", "application/json")
            .build()

        return chain.proceed(newRequest)
    }
}

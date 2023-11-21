package com.example.getiiabotpapp.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class HeadersInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val request = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("X-IBM-Client-Id", "1b3dbe1f98dce857397c16463684b1ea")
            .header("X-IBM-Client-Secret", "fb37dda8700ce6295d247de5729cf6d2")
            .method(original.method, original.body)
            .build()
        return chain.proceed(request)
    }


}
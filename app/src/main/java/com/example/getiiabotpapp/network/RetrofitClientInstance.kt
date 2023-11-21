package com.example.getiiabotpapp.network

import android.app.Application
import com.example.getiiabotpapp.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier


class RetrofitClientInstance(var application: Application) {

    private val TIMEOUT = 30L
    companion object {
        var baseUrl : String = "https://185.180.80.140/porg-iiab/catalog-internal/"
        val baseUat = "https://185.180.80.140/porg-iiab/catalog-internal/"
        val baseDev = "https://185.180.80.141/porg-iiab/catalog-internal/"
    }

    fun provideRetrofit(): Retrofit {
        val httpClient = provideOkHttpClient()

        return Retrofit.Builder()
            .baseUrl(baseUrl/*BuildConfig.BASE_API_URL*/)
            .client(httpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun provideOkHttpClient(): OkHttpClient {
        val httpLoggingInterceptor = provideOkHttpLogging()
        val errorInterceptor = provideErrorInterceptor(application)
        val headersInterceptor = HeadersInterceptor(application)

        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .hostnameVerifier(hostnameVerifier = HostnameVerifier { hostname, sslSession ->
                true
            })
            .addInterceptor(errorInterceptor)
            .addInterceptor(headersInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    fun provideOkHttpLogging(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    fun provideErrorInterceptor(mApplication: Application): Interceptor =
        ErrorInterceptor(mApplication)

    val gson: Gson
        get() {
            GsonBuilder().apply {
                setLenient()
                return create()
            }
        }

    fun <S> createService(serviceClass: Class<S>): S {
        return provideRetrofit().create(serviceClass)
    }
}
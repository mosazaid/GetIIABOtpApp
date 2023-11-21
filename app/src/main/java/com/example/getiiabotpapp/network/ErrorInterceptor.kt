package com.example.getiiabotpapp.network

import android.content.Context
import com.example.getiiabotpapp.R
import com.mosa.data.network.result.RequestErrorHandler
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class ErrorInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request: Request = chain.request()
        val response = chain.proceed(request)
        val builder = response.newBuilder()

        when (response.code) {
            in RequestErrorHandler.HTTP_CODE_CLIENT_START..RequestErrorHandler.HTTP_CODE_CLIENT_END -> {
                builder.message(context.getString(R.string.error_client_message))
            }
            in RequestErrorHandler.HTTP_CODE_SERVER_START..RequestErrorHandler.HTTP_CODE_SERVER_END -> {
                builder.message(context.getString(R.string.error_server_message))
            }
            200 -> {

            }
            /*   400 -> {
                   //Show Bad Request Error Message
               }
               401 -> {
                   //Show UnauthorizedError Message
               }

               403 -> {
                   //Show Forbidden Message
               }

               404 -> {
                   //Show NotFound Message
               }
               500 -> {
                  new Intent(
                  ErrorHandlingActivity.this,
                  ServerIsBrokenActivity.class
                            )
               }*/
            else -> {
                builder.message(context.getString(R.string.error_unexpected_message))
            }
        }
        return builder.build()
    }


}
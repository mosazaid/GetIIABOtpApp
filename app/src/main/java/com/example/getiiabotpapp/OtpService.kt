package com.example.getiiabotpapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface OtpService {

    @GET("user/otp/getOtp")
     fun getOtp(@Query("number") number: String): Call<OtpModel>
}
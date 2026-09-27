package com.example.smartagriculture.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val WEATHER_BASE_URL = "https://api.open-meteo.com/v1/"
    private const val MANDI_BASE_URL = "https://api.data.gov.in/"

    val weatherRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val mandiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(MANDI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

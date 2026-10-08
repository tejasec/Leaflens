package com.example.smartagriculture.network

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = "relativehumidity_2m,precipitation_probability"
    ): WeatherResponse
}

data class WeatherResponse(
    val current_weather: CurrentWeather?,
    val hourly: HourlyWeather? = null
)

data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int
)

data class HourlyWeather(
    val relativehumidity_2m: List<Int>? = null,
    val precipitation_probability: List<Int>? = null
)

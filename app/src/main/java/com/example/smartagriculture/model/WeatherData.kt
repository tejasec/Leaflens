package com.example.smartagriculture.model

data class WeatherData(
    val cityName: String,
    val temperature: Int,
    val condition: String,
    val humidity: Int,
    val windSpeed: Int
)
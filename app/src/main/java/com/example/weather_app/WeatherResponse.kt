package com.example.weather_app

data class WeatherResponse(
    val name: String,
    val main: MainData,
    val weather: List<Weather>
)

data class MainData(
    val temp: Double,
    val feels_like: Double,
    val humidity: Int
)

data class Weather(
    val description: String,
    val icon: String
)

package com.example.weather_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request


@DelicateCoroutinesApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherApp()
        }
    }
}

@DelicateCoroutinesApi
@Composable
fun WeatherApp(){
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        WeatherScreen()
    }
}
@Preview(showBackground = true, showSystemUi = true)
@DelicateCoroutinesApi
@Composable
fun WeatherScreen(){
    var city by remember { mutableStateOf("Gomel") }
    var weatherData by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun loadWeather(cityName: String){
        isLoading = true
        error = null

        GlobalScope.launch(Dispatchers.IO) {
            try{
                val apiKey = Config.WEATHER_API_KEY
                val url = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey&units=metric&lang=ru"

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()

                if(response.isSuccessful){
                    val json = response.body?.string()
                    val data = Gson().fromJson(json, WeatherResponse::class.java)

                    launch(Dispatchers.Main) {
                        weatherData = data
                        isLoading = false
                    }
                }else{
                    launch (Dispatchers.Main){
                        error = "Город не найден"
                        isLoading = false
                    }
                }

            }catch (e: Exception) {
                launch(Dispatchers.Main) {
                    error = "Ошибка: ${e.message}"
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadWeather(city)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Простая погода",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        OutlinedTextField(
            value = city,
            onValueChange = { city = it},
            label = { Text("Введите город") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (city.isNotBlank()) {
                        loadWeather(city)
                    }
                }
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { loadWeather(city)},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Узнать погоду")
        }
        Spacer(modifier = Modifier.height(20.dp))

        if(isLoading){
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Загрузка...")
        }
        error?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFE0E0)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Ошибка",
                        color = Color.Red,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.padding(16.dp))
                    Text(text = it)
                }
            }
        }
        weatherData?.let { data ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)

            ) {
                Column(
                    modifier = Modifier.padding(25.dp),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Text(
                        text = data.name,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${data.main.temp.toInt()}°C",
                        fontSize = 64.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = data.weather.firstOrNull()?.description ?: "",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ){
                        WeatherInfoItem(
                            title = "Ощущается",
                            value = "${data.main.feels_like.toInt()}°C"
                        )

                        WeatherInfoItem(
                            title = "Влажность",
                            value = "${data.main.humidity}%"
                        )
                    }

                    val iconCode = data.weather.firstOrNull()?.icon
                    if(iconCode != null){
                        Spacer(modifier = Modifier.height(16.dp))
                        val iconUrl = "https://openweathermap.org/img/wn/\${iconCode}@2x.png"

                        Text(
                            text = getWeatherEmoji(iconCode),
                            fontSize = 48.sp
                        )
                    }
                }
            }
        }

    }
}
@Composable
fun WeatherInfoItem(title:String,value:String){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getWeatherEmoji(iconCode: String) : String {
    return when {
        iconCode.contains("01d") -> "☀️" // ясно день
        iconCode.contains("01n") -> "🌙" // ясно ночь
        iconCode.contains("02") -> "⛅" // малооблачно
        iconCode.contains("03") -> "☁️" // облачно
        iconCode.contains("04") -> "🌫️" // пасмурно
        iconCode.contains("09") -> "🌧️" // ливень
        iconCode.contains("10") -> "🌦️" // дождь
        iconCode.contains("11") -> "⛈️" // гроза
        iconCode.contains("13") -> "❄️" // снег
        iconCode.contains("50") -> "🌫️" // туман
        else -> "🌡️"
    }
}


//package com.example.android.myapplication_weather


package com.example.android.myapplication_weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            application_weather()
        }
    }
}

@Composable
fun application_weather() {
    var insertDate by remember { mutableStateOf("") }
    var insertYear by remember { mutableStateOf("") }
    var tempMax by remember { mutableStateOf<Double?>(null) }
    var tempMin by remember { mutableStateOf<Double?>(null) }
    var output by remember { mutableStateOf("") }
    var fetchingTemperature by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(Color.LightGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = insertDate,
            onValueChange = { insertDate = it },
            label = { Text("Enter Date (dd/MM)") },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        TextField(
            value = insertYear,
            onValueChange = { insertYear = it },
            label = { Text("Enter Year (yyyy)") },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    fetchingTemperature = true
                    val selectedDate = parseDate(insertDate, insertYear)
                    selectedDate?.let { date ->
                        fetchWeatherData(selectedDate) { max, min ->
                            tempMax = max
                            tempMin = min
                            output = "Max Temp: $tempMax °C\nMin Temp: $tempMin °C"
                            fetchingTemperature = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            if (fetchingTemperature) {
                Text("Fetching Temperature...")
            } else {
                Text("Fetch Weather")
            }
        }

        Text(
            text = output,
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun parseDate(dateInput: String, yearInput: String): Date? {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString = "$dateInput/$yearInput"
    return try {
        dateFormat.parse(dateString)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun fetchWeatherData(selectedDate: Date?, callback: (Double?, Double?) -> Unit) {
    selectedDate ?: return

    val apiKey = "87FZASQKNH5Q8SQQ9V5MSVNSQ"
    val city = "mumbai"
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateString = dateFormat.format(selectedDate)

    val apiUrl = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$city/$dateString?unitGroup=metric&key=$apiKey&contentType=json"

    val response = withContext(Dispatchers.IO) {
        val url = URL(apiUrl)
        val connection = url.openConnection() as HttpURLConnection

        try {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }

            response.toString()
        } finally {
            connection.disconnect()
        }
    }

    try {
        val jsonObject = JSONObject(response)
        val days = jsonObject.getJSONArray("days")
        val todayData = days.getJSONObject(0)
        val tempMax = todayData.getDouble("tempmax")
        val tempMin = todayData.getDouble("tempmin")

        callback(tempMax, tempMin)
    } catch (e: Exception) {
        e.printStackTrace()
        callback(null, null)
    }
}

@Preview(showBackground = true)
@Composable
fun Previewapplication_weather() {
    application_weather()
}

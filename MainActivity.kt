package com.example.android.weatherapp_2
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import android.content.Context
import android.content.pm.PackageManager
import android.Manifest
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat

import androidx.compose.ui.unit.dp


import androidx.room.Entity
import androidx.room.*

import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

import java.io.BufferedReader
import org.json.JSONObject

import java.net.HttpURLConnection
import java.io.InputStreamReader

import java.net.URL

// WeatherData.kt
@Entity(tableName="weather_db")
data class WeatherData(
    @PrimaryKey val date: String,
    val Temperature_maxmerature: Double,
    val minTemperature: Double
)

@Database(entities=[WeatherData::class], version=1)
abstract class WeatherDatabase : RoomDatabase()
{
    abstract fun weatherDao(): WeatherDao
}
@Dao
interface WeatherDao
{
    @Insert
    suspend fun insert(weatherData: WeatherData)

    @Query("SELECT * FROM weather_db WHERE date=:date")
    suspend fun getWeatherData(date: String): WeatherData?
}
class MainActivity : ComponentActivity() {
    private lateinit var db: WeatherDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db=Room.databaseBuilder(
            applicationContext,
            WeatherDatabase::class.java, "weather-database"
        ).build()

        setContent {
            WeatherApp(db)
        }
    }
}
private suspend fun fetching_fr_database(weatherDao: WeatherDao, dateString: Date): WeatherData? {
    val formatofdate=SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDateString=formatofdate.format(dateString)
    return withContext(Dispatchers.IO) {
        weatherDao.getWeatherData(formattedDateString)
    }
}
fun checkedpermisionwithNet(context: Context): Boolean
{
    return ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
}
@Composable
fun WeatherApp(db: WeatherDatabase) {
    var insert_year by remember { mutableStateOf("") }
    var insert_date by remember { mutableStateOf("") }
    var Temperatur_minm by remember { mutableStateOf(0.0) }
    var Temperature_maxm by remember { mutableStateOf(0.0) }
    var output by remember { mutableStateOf("") }
    val context=LocalContext.current
    val coroutineScope=rememberCoroutineScope()
    Column(
        modifier=Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value=insert_date,
            onValueChange={ insert_date=it },
            label={
                Text(
                    "Enter Date(form: dd/MM)",
                    style = TextStyle(color = Color.Gray),
                    fontSize = 16.sp
                )
            },
            modifier=Modifier.fillMaxWidth()
        )

        Spacer(modifier=Modifier.height(16.dp))
        TextField(
            value=insert_year,
            onValueChange={ insert_year=it },
            label={
                Text(
                    text = "Year only in form (yyyy)",
                    style = TextStyle(fontSize = 16.sp, color = Color.Gray)
                )
            },
            modifier=Modifier.fillMaxWidth()
        )

        Spacer(modifier=Modifier.height(16.dp))
        Button(
            onClick={
                coroutineScope.launch {
                    val selectedDate=dateparsing(insert_date, insert_year)
                    selectedDate?.let { date ->
                        if (checkedpermisionwithNet(context)) {
                            fetchWeatherDataAPI(
                                selectedDate,
                                db.weatherDao()
                            ) { Temperature_maxmValue, minTempValue ->
                                Temperature_maxm=Temperature_maxmValue
                                Temperatur_minm= minTempValue
                                output =
                                    "Maximum Temperature is: $Temperature_maxmValue °C\nMinimum Temperature is: $minTempValue °C"

                            }

                        }else{

                            val Information_weather=   fetching_fr_database(db.weatherDao(), selectedDate)
                            withContext(Dispatchers.Main) {
                                if (Information_weather != null)
                                {
                                    Temperature_maxm=Information_weather.Temperature_maxmerature
                                }
                                if (Information_weather != null)
                                {
                                    Temperatur_minm= Information_weather.minTemperature
                                }
                                output =
                                    "Maximum Temperature is: $Temperature_maxm °C\nMinimum Temperature is: $Temperatur_minm°C"
                            }

                        }
                    } ?: run {
                        Toast.makeText(context, "Invalid Date", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier=Modifier.fillMaxWidth()
        ) {
            if(checkedpermisionwithNet(context))
            {
                Text("Fetch Weather Data")
            }
            else
            {
                Text("Fetch Weather DataBase")
            }
        }

        Spacer(modifier=Modifier.height(16.dp))

        Text(
            text="Maximum Temperature is : $Temperature_maxm °C",
            color=Color(0xFFF8E004),
            modifier=Modifier.padding(horizontal=8.dp)
        )

        Text(
            text="Minimum Temperature is : $Temperatur_minm°C",
            color=Color(0xFF45FA03),
            modifier=Modifier.padding(horizontal=8.dp)
        )
    }
}



private fun dateparsing(insert_date: String, insert_year: String): Date? {
    val dateFormat=SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString="$insert_date/$insert_year"
    return try {
        dateFormat.parse(dateString)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun availableFuture_date(date: Date): Boolean {
    val currentDate=Calendar.getInstance().time
    return date.after(currentDate)
}

private suspend fun insertWeatherData(weatherDao: WeatherDao, date: Date, Temperature_maxm: Double, minTemp: Double) {

    val dateFormat=SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateString=dateFormat.format(date)

    val weatherData=WeatherData(dateString, Temperature_maxm, minTemp)
    withContext(Dispatchers.IO) {
        weatherDao.insert(weatherData)
    }
}



private suspend fun getAverageWeatherData(date: Date, weatherDao: WeatherDao): WeatherData? {
    val calendar=Calendar.getInstance()
    calendar.time=date
    val currentYear=calendar.get(Calendar.YEAR)

    var totalTemperature_maxm=0.0
    var totalTemperatur_minm= 0.0
    var count=0

    // Fetching weather data for each of the past 10 years from the database
    for (year in currentYear - 1 downTo currentYear - 10) {
        val pastDate=calendar.apply { set(Calendar.YEAR, year) }.time
        val dateFormat=SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString=dateFormat.format(pastDate)
        val weatherData=fetching_fr_database(weatherDao, date)
        if (weatherData != null) {
            totalTemperatur_minm+= weatherData.minTemperature
            totalTemperature_maxm += weatherData.Temperature_maxmerature

            count++
        }
    }

    // If sufficient data present from the database, we calculate average temperatures
    if (count > 0) {
        val avg_max=totalTemperature_maxm / count
        val avg_min=totalTemperatur_minm/ count
        return WeatherData("Average", avg_max, avg_min)
    } else {
        if (availableFuture_date(date)) {
            return fetchAverageWeatherDataFromAPI(date)
        }
    }

    return null
}

private suspend fun fetchWeatherDataFromAPI(apiUrl: String): String {
    val url=URL(apiUrl)
    val connection=url.openConnection() as HttpURLConnection

    return try {
        val reader=BufferedReader(InputStreamReader(connection.inputStream))
        val response=StringBuilder()
        var line: String?

        while (reader.readLine().also { line=it } != null) {
            response.append(line)
        }

        println("Weather API Response: $response")

        response.toString()
    } finally {
        connection.disconnect()
    }
}

private suspend fun fetchAverageWeatherDataFromAPI(date: Date): WeatherData? {
    val calendar=Calendar.getInstance()
    calendar.time=date

    var totalTemperature_maxm=0.0
    var totalTemperatur_minm= 0.0
    var count=0

    // Fetching weather data for each of the past 10 years from the API
    for (i in 1..10) {
        val pastDate=calendar.apply { add(Calendar.YEAR, -i) }.time
        val apiUrl=getApiUrlForDate(pastDate)
        try {
            val response=fetchWeatherDataFromAPI(apiUrl)
            val jsonObject=JSONObject(response)
            val tempMax=jsonObject.getDouble("tempmax")
            val tempMin=jsonObject.getDouble("tempmin")
            totalTemperature_maxm += tempMax
            totalTemperatur_minm+= tempMin
            count++
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (count > 0) {
        val avg_max=totalTemperature_maxm / count
        val avg_min=totalTemperatur_minm/ count
        return WeatherData("Average", avg_max, avg_min)
    }

    return null
}

private fun getApiUrlForDate(date: Date): String {
    val city="mumbai" // Change this to your desired city
    val apiKey="87FZASQKNH5Q8SQQ9V5MSVNSQ"
    val dateFormat=SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateString=dateFormat.format(date)
    return "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$city/$dateString?unitGroup=metric&key=$apiKey&contentType=json"
}

private suspend fun fetchWeatherDataAPI(selectedDate: Date, weatherDao: WeatherDao, updateoutput: (Temperature_maxm: Double, minTemp: Double) -> Unit) {
    selectedDate ?: return

    val apiKey="87FZASQKNH5Q8SQQ9V5MSVNSQ"
    val city="mumbai" // e.g., "Delhi"
    val dateFormat=SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateString=dateFormat.format(selectedDate)

    val apiUrl="https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/$city/$dateString?unitGroup=metric&key=$apiKey&contentType=json"

    val response=withContext(Dispatchers.IO) {
        val url=URL(apiUrl)
        val connection=url.openConnection() as HttpURLConnection

        try {
            val reader=BufferedReader(InputStreamReader(connection.inputStream))
            val response=StringBuilder()
            var line: String?

            while (reader.readLine().also { line=it } != null) {
                response.append(line)
            }

            println("Weather API Response: $response")

            response.toString()
        } finally {
            connection.disconnect()
        }
    }

    try {
        val jsonObject=JSONObject(response)
        val days=jsonObject.getJSONArray("days")
        val todayData=days.getJSONObject(0)
        val tempMax = todayData.getDouble("tempmax")
        val tempMin = todayData.getDouble("tempmin")

        insertWeatherData(weatherDao, selectedDate, tempMax, tempMin)

        // Updating UI with the fetched weather data
        withContext(Dispatchers.Main) {
            updateoutput(tempMax, tempMin)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWeatherApp() {
    val db = Room.inMemoryDatabaseBuilder(
        androidx.compose.ui.platform.LocalContext.current,
        WeatherDatabase::class.java
    ).build()

    WeatherApp(db)
}
# MC_ass2_MT23059
# WeatherApp

## Overview
WeatherApp is an Android application that allows users to fetch weather data based on a specified date and year. The app fetches weather information either from an API or a local database, depending on the availability of internet connectivity. It provides the maximum and minimum temperature for the given date and year.

## Features
- Users can enter a date and year to fetch weather data.
- Weather data can be fetched either from an API or a local database.
- The app displays the maximum and minimum temperature for the specified date.
- If internet connectivity is available, the app fetches real-time weather data from an API.
- If no internet connectivity is available, the app retrieves weather data from the local database, if available.
- The app provides a user-friendly interface for inputting date and year, fetching weather data, and displaying the results.

## Technical Details
- The app is built using Kotlin and Jetpack Compose.
- It uses Room Database to store and retrieve weather data locally.
- Retrofit is used for network calls to fetch weather data from the API.
- The app handles internet permission checks and runtime permissions.
- It supports backward compatibility and efficient handling of UI updates using Coroutines.

## Usage
1. Launch the app on your Android device.
2. Enter the desired date (in the format dd/MM) and year (in the format yyyy).
3. Click on the "Fetch Weather Data" button to retrieve weather information.
4. The app will display the maximum and minimum temperature for the specified date.



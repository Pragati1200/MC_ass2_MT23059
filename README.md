# Weather App README

## Overview
The Weather App is designed to help users access historical weather data for a specific date and year in a chosen city. It utilizes a free weather API to fetch data in JSON format, parses the JSON response to extract temperature information, and displays the maximum and minimum temperatures for the specified date.

## Features
- Users can enter a date and year to fetch weather data.
- Weather data can be fetched either from an API or a local database.
- The app displays the maximum and minimum temperature for the specified date.
- If internet connectivity is available, the app fetches real-time weather data from an API.
- If no internet connectivity is available, the app retrieves weather data from the local database, if available.
- The app provides a user-friendly interface for inputting date and year, fetching weather data, and displaying the results.

## Functionality

1. **API Integration and Data Retrieval:**
   - The app integrates with a free weather API to fetch historical weather data.
   - It sends a request to the API endpoint with the selected date, year, and city information.
   - Upon receiving the JSON response, it parses the data to extract temperature information.

2. **User Interface (UI):**
   - The UI allows users to input the date and year for which they want to retrieve weather data.
   - It provides text fields for users to enter the date and year.
   - Upon clicking the "Fetch Weather from API" button, the app triggers the data retrieval process.

3. **Data Parsing:**
   - After receiving the JSON response from the API, the app parses it to extract the maximum and minimum temperature values for the specified date.
   - It handles any errors that may occur during the parsing process.

4. **Output:**
   - The app displays the maximum and minimum temperatures in Celsius units for the selected date.
   - If the API request is successful and data is retrieved, the temperatures are shown.
   - If there is an error in the API request or parsing process, appropriate error messages are displayed.

5. **Input Validation:**
   - The app validates user input for the date and year fields to ensure they are in the correct format.
   - If the input format is incorrect, the app displays an error message prompting the user to enter the date and year in the specified format (dd/MM for date and yyyy for year).

## Technical Details
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Networking Library:** Retrofit 2
- **JSON Parsing:** org.json library
- **Asynchronous Operations:** Kotlin Coroutines

## Usage
1. Launch the app on your Android device.
2. Enter the desired date (in the format dd/MM) and year (in the format yyyy).
3. Click on the "Fetch Weather Data" button to retrieve weather information.
4. The app will display the maximum and minimum temperature for the specified date.

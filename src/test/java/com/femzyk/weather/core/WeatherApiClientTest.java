package com.femzyk.weather.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Unit tests for the Open-Meteo response parsing in WeatherApiClient.
 *
 * CS 1103-01, Programming Assignment Unit 8. The samples are trimmed
 * copies of real API responses, so the parsers are verified without any
 * network access: the geocoding entry, the "current" block, and the
 * hourly forecast slicing that builds the short-term strip.
 */
class WeatherApiClientTest {

    /** Trimmed from a real geocoding-api.open-meteo.com response for Lagos. */
    private static final String GEOCODING_ENTRY = """
            {
              "id": 2332459,
              "name": "Lagos",
              "latitude": 6.45407,
              "longitude": 3.39467,
              "country": "Nigeria",
              "admin1": "Lagos",
              "timezone": "Africa/Lagos"
            }""";

    /** Trimmed from a real api.open-meteo.com forecast response. */
    private static final String FORECAST = """
            {
              "current": {
                "time": "2026-10-24T19:15",
                "temperature_2m": 28.2,
                "relative_humidity_2m": 84,
                "apparent_temperature": 33.5,
                "is_day": 0,
                "weather_code": 3,
                "wind_speed_10m": 10.9,
                "wind_direction_10m": 225
              },
              "hourly": {
                "time": ["2026-10-24T18:00", "2026-10-24T19:00", "2026-10-24T20:00",
                         "2026-10-24T21:00", "2026-10-24T22:00", "2026-10-24T23:00",
                         "2026-10-25T00:00", "2026-10-25T01:00", "2026-10-25T02:00",
                         "2026-10-25T03:00"],
                "temperature_2m": [29.1, 28.5, 27.8, 27.2, 26.9, 26.5, 26.1, 25.8, 25.5, 25.2],
                "weather_code": [3, 3, 2, 2, 0, 0, 1, 1, 1, 2]
              }
            }""";

    @Test
    void geocodingEntryParsesAllDisplayFields() {
        JsonObject object = JsonParser.parseString(GEOCODING_ENTRY).getAsJsonObject();
        GeocodingResult result = WeatherApiClient.parseGeocodingResult(object);
        assertEquals("Lagos", result.name());
        assertEquals("Nigeria", result.country());
        assertEquals("Africa/Lagos", result.timezone());
        assertEquals(6.45407, result.latitude(), 0.00001);
        assertEquals(3.39467, result.longitude(), 0.00001);
        assertEquals("Lagos, Nigeria", result.displayName());
    }

    @Test
    void currentBlockParsesValuesAndDayFlag() {
        JsonObject root = JsonParser.parseString(FORECAST).getAsJsonObject();
        CurrentWeather current = WeatherApiClient.parseCurrent(root.getAsJsonObject("current"));
        assertEquals(28.2, current.temperatureC(), 0.0001);
        assertEquals(84, current.humidityPct());
        assertEquals(33.5, current.feelsLikeC(), 0.0001);
        assertEquals(10.9, current.windKmh(), 0.0001);
        assertEquals(225, current.windDirectionDeg());
        assertFalse(current.isDay());
        assertEquals(3, current.weatherCode());
    }

    @Test
    void forecastSlicesTheNextEightHoursAfterNow() {
        JsonObject root = JsonParser.parseString(FORECAST).getAsJsonObject();
        List<ForecastHour> forecast = WeatherApiClient.parseForecast(
                root.getAsJsonObject("hourly"), "2026-10-24T19:15");
        assertEquals(8, forecast.size());
        assertEquals("20:00", forecast.get(0).hourLabel());
        assertEquals(27.8, forecast.get(0).temperatureC(), 0.0001);
        assertEquals("03:00", forecast.get(7).hourLabel());
    }

    @Test
    void forecastSkipsHoursBeforeTheObservation() {
        JsonObject root = JsonParser.parseString(FORECAST).getAsJsonObject();
        List<ForecastHour> forecast = WeatherApiClient.parseForecast(
                root.getAsJsonObject("hourly"), "2026-10-24T18:30");
        assertEquals("19:00", forecast.get(0).hourLabel());
        assertEquals(28.5, forecast.get(0).temperatureC(), 0.0001);
    }

    @Test
    void badCityInputIsRejectedWithAClearMessage() {
        WeatherApiClient client = new WeatherApiClient();
        WeatherApiException error = assertThrows(WeatherApiException.class, () -> client.geocode("   "));
        assertEquals("Please enter a city name first.", error.getMessage());
    }
}

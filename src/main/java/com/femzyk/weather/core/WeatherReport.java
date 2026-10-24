package com.femzyk.weather.core;

import java.time.Instant;
import java.util.List;

/**
 * WeatherReport - everything one search produces.
 *
 * CS 1103-01, Programming Assignment Unit 8. An immutable record that
 * bundles the matched location, the current conditions, the short-term
 * hourly forecast, and the moment the data was fetched (shown in the
 * GUI's status line).
 */
public record WeatherReport(
        GeocodingResult location,
        CurrentWeather current,
        List<ForecastHour> hourlyForecast,
        Instant fetchedAt) {
}

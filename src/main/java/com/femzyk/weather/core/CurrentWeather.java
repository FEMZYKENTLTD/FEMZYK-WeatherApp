package com.femzyk.weather.core;

/**
 * CurrentWeather - the current conditions at a location.
 *
 * CS 1103-01, Programming Assignment Unit 8. An immutable record with the
 * fields the assignment requires the app to display: temperature, humidity,
 * wind speed, and conditions (weather code). Temperatures are always kept
 * in Celsius and wind speeds in km/h inside the core layer; the GUI converts
 * them when the user switches units.
 */
public record CurrentWeather(
        String observedAtIso,
        double temperatureC,
        int humidityPct,
        double feelsLikeC,
        double windKmh,
        int windDirectionDeg,
        boolean isDay,
        int weatherCode) {
}

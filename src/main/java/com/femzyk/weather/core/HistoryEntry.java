package com.femzyk.weather.core;

import java.time.LocalDateTime;

/**
 * HistoryEntry - one row of the search history.
 *
 * CS 1103-01, Programming Assignment Unit 8. An immutable record storing
 * what the user searched, when, and what the weather was at that moment,
 * which is exactly what the assignment's history-tracking requirement
 * asks to show: recent searches with timestamps.
 */
public record HistoryEntry(
        String city,
        String country,
        double temperatureC,
        int weatherCode,
        LocalDateTime searchedAt) {

    /**
     * Short display text for the history list, for example
     * "New York, United States - 16\u00B0C, Clear sky".
     */
    public String summary() {
        String place = country.isBlank() ? city : city + ", " + country;
        return place + " - " + UnitConverter.formatTemperature(temperatureC, false)
                + ", " + WmoWeather.fromCode(weatherCode).description();
    }
}

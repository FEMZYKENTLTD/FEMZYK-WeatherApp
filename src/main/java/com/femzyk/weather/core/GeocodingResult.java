package com.femzyk.weather.core;

/**
 * GeocodingResult - the location the weather API matched for a city name.
 *
 * CS 1103-01, Programming Assignment Unit 8. An immutable record holding
 * everything the forecast request needs (latitude, longitude, timezone)
 * plus the display fields the GUI shows (name, country, region).
 */
public record GeocodingResult(
        String name,
        String country,
        String admin1,
        double latitude,
        double longitude,
        String timezone) {

    /** Builds a single display line, for example "Lagos, Nigeria". */
    public String displayName() {
        StringBuilder text = new StringBuilder(name);
        if (!country.isBlank()) {
            text.append(", ").append(country);
        }
        return text.toString();
    }
}

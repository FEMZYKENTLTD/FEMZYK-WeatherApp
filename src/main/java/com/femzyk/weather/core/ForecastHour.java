package com.femzyk.weather.core;

/**
 * ForecastHour - one hour of the short-term forecast.
 *
 * CS 1103-01, Programming Assignment Unit 8. An immutable record used by
 * the forecast strip in the GUI: the hour label, the expected temperature,
 * and the weather code (mapped to a description and an icon).
 */
public record ForecastHour(
        String timeIso,
        double temperatureC,
        int weatherCode) {

    /** Returns the hour label in "HH:mm" form, for example "20:00". */
    public String hourLabel() {
        return timeIso.length() >= 16 ? timeIso.substring(11, 16) : timeIso;
    }
}

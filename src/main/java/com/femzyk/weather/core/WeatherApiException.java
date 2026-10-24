package com.femzyk.weather.core;

/**
 * WeatherApiException - a checked exception for every failure the weather
 * app can recover from and explain to the user.
 *
 * CS 1103-01, Programming Assignment Unit 8. The message is always written
 * for the user interface: "City not found: ...", "Network problem: ...",
 * "Weather service returned HTTP 500". The GUI shows the message in its
 * error banner instead of crashing, which satisfies the assignment's error
 * handling requirement for invalid locations and failed API requests.
 */
public class WeatherApiException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Creates an exception with a user-readable message. */
    public WeatherApiException(String message) {
        super(message);
    }

    /** Creates an exception with a user-readable message and the cause. */
    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

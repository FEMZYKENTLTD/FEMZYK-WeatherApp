package com.femzyk.weather.core;

/**
 * UnitConverter - conversions between the measurement units the app offers.
 *
 * CS 1103-01, Programming Assignment Unit 8. Implements the assignment's
 * unit-conversion requirement: temperature between Celsius and Fahrenheit,
 * wind speed between km/h and mph. The core layer always stores SI units
 * (Celsius, km/h); the GUI calls these helpers only when rendering, so a
 * unit switch never re-contacts the API. Also maps wind directions in
 * degrees to the 16 compass points.
 */
public final class UnitConverter {

    private static final String[] COMPASS_POINTS = {
        "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
        "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"
    };

    private UnitConverter() {
        // Utility class: no instances.
    }

    /** Converts a temperature from Celsius to Fahrenheit. */
    public static double celsiusToFahrenheit(double celsius) {
        return celsius * 9.0 / 5.0 + 32.0;
    }

    /** Converts a temperature from Fahrenheit to Celsius. */
    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32.0) * 5.0 / 9.0;
    }

    /** Converts a wind speed from km/h to mph. */
    public static double kmhToMph(double kmh) {
        return kmh * 0.621371;
    }

    /** Converts a wind speed from mph to km/h. */
    public static double mphToKmh(double mph) {
        return mph / 0.621371;
    }

    /**
     * Formats a Celsius temperature for display in the chosen unit,
     * for example "28\u00B0C" or "83\u00B0F".
     */
    public static String formatTemperature(double celsius, boolean useFahrenheit) {
        if (useFahrenheit) {
            return String.format("%.0f\u00B0F", celsiusToFahrenheit(celsius));
        }
        return String.format("%.0f\u00B0C", celsius);
    }

    /**
     * Formats a km/h wind speed for display in the chosen unit,
     * for example "11 km/h" or "7 mph".
     */
    public static String formatWind(double kmh, boolean useMph) {
        if (useMph) {
            return String.format("%.0f mph", kmhToMph(kmh));
        }
        return String.format("%.0f km/h", kmh);
    }

    /**
     * Maps a wind direction in degrees (0-360, where 0 is north) to the
     * nearest of the 16 compass points, for example 225 to "SW".
     */
    public static String windCompass(int degrees) {
        int normalized = ((degrees % 360) + 360) % 360;
        int index = (int) Math.round(normalized / 22.5) % 16;
        return COMPASS_POINTS[index];
    }
}

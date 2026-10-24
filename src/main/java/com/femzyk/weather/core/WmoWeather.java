package com.femzyk.weather.core;

/**
 * WmoWeather - maps the WMO weather interpretation codes used by the
 * Open-Meteo API to a human-readable description and an icon key the
 * GUI can draw.
 *
 * CS 1103-01, Programming Assignment Unit 8. Open-Meteo follows the WMO
 * standard: 0 = clear sky, 1-3 = increasing cloud, 45/48 = fog,
 * 51-67 = drizzle and rain, 71-77 = snow, 80-82 = showers,
 * 95/96/99 = thunderstorms. The icon keys ("sun", "sun-cloud", "cloud",
 * "rain", "snow", "storm", "fog") are drawn as vector icons by
 * WeatherIconCanvas; at night the GUI swaps sun icons for moon icons.
 */
public enum WmoWeather {

    CLEAR_SKY(0, "Clear sky", "sun"),
    MAINLY_CLEAR(1, "Mainly clear", "sun-cloud"),
    PARTLY_CLOUDY(2, "Partly cloudy", "sun-cloud"),
    OVERCAST(3, "Overcast", "cloud"),
    FOG(45, "Fog", "fog"),
    RIME_FOG(48, "Depositing rime fog", "fog"),
    LIGHT_DRIZZLE(51, "Light drizzle", "rain"),
    DRIZZLE(53, "Drizzle", "rain"),
    HEAVY_DRIZZLE(55, "Heavy drizzle", "rain"),
    LIGHT_FREEZING_DRIZZLE(56, "Light freezing drizzle", "rain"),
    HEAVY_FREEZING_DRIZZLE(57, "Heavy freezing drizzle", "rain"),
    SLIGHT_RAIN(61, "Slight rain", "rain"),
    RAIN(63, "Rain", "rain"),
    HEAVY_RAIN(65, "Heavy rain", "rain"),
    LIGHT_FREEZING_RAIN(66, "Light freezing rain", "rain"),
    HEAVY_FREEZING_RAIN(67, "Heavy freezing rain", "rain"),
    LIGHT_SNOW(71, "Slight snowfall", "snow"),
    SNOW(73, "Snowfall", "snow"),
    HEAVY_SNOW(75, "Heavy snowfall", "snow"),
    SNOW_GRAINS(77, "Snow grains", "snow"),
    SLIGHT_SHOWERS(80, "Slight rain showers", "rain"),
    SHOWERS(81, "Rain showers", "rain"),
    VIOLENT_SHOWERS(82, "Violent rain showers", "rain"),
    SLIGHT_SNOW_SHOWERS(85, "Slight snow showers", "snow"),
    HEAVY_SNOW_SHOWERS(86, "Heavy snow showers", "snow"),
    THUNDERSTORM(95, "Thunderstorm", "storm"),
    THUNDERSTORM_HAIL(96, "Thunderstorm with slight hail", "storm"),
    THUNDERSTORM_HEAVY_HAIL(99, "Thunderstorm with heavy hail", "storm");

    private final int code;
    private final String description;
    private final String iconKey;

    WmoWeather(int code, String description, String iconKey) {
        this.code = code;
        this.description = description;
        this.iconKey = iconKey;
    }

    /** Returns the WMO code this entry represents. */
    public int code() {
        return code;
    }

    /** Returns the human-readable weather description. */
    public String description() {
        return description;
    }

    /** Returns the icon key the GUI draws for this weather. */
    public String iconKey() {
        return iconKey;
    }

    /**
     * Looks up a weather code. Unknown codes fall back to Overcast so the
     * interface always has something sensible to display.
     */
    public static WmoWeather fromCode(int code) {
        for (WmoWeather weather : values()) {
            if (weather.code == code) {
                return weather;
            }
        }
        return OVERCAST;
    }
}

package com.femzyk.weather.core;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * WeatherApiClient - the integration with the Open-Meteo weather API.
 *
 * CS 1103-01, Programming Assignment Unit 8. Every search is a two-step
 * REST conversation: (1) the geocoding API resolves a city name to
 * coordinates, (2) the forecast API fetches current conditions plus the
 * hourly forecast for those coordinates. Open-Meteo is free for
 * non-commercial use and requires no API key; the app follows its usage
 * guidelines by caching nothing beyond the session and making only two
 * small requests per search.
 *
 * The client uses the modern java.net.http.HttpClient with explicit
 * timeouts, checks every HTTP status, and converts every failure into a
 * WeatherApiException whose message is safe to show in the GUI. The JSON
 * parsing helpers are package-visible so they can be unit-tested without
 * any network access.
 */
public class WeatherApiClient {

    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";

    /** How many forecast hours the short-term strip shows. */
    private static final int FORECAST_HOURS = 8;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * Runs a full search: city name to coordinates to weather report.
     *
     * @param cityName the user's input, for example "Lagos"
     * @return an immutable WeatherReport with current conditions and forecast
     * @throws WeatherApiException for invalid input, unknown cities, and
     *                             network or API failures
     */
    public WeatherReport search(String cityName) throws WeatherApiException {
        GeocodingResult location = geocode(cityName);
        return fetchReport(location);
    }

    /**
     * Resolves a city name to a location with the geocoding API.
     */
    public GeocodingResult geocode(String cityName) throws WeatherApiException {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new WeatherApiException("Please enter a city name first.");
        }
        String url = GEOCODING_URL
                + "?name=" + URLEncoder.encode(cityName.trim(), StandardCharsets.UTF_8)
                + "&count=1&language=en&format=json";
        JsonObject root = getJson(url);
        JsonArray results = root.has("results") ? root.getAsJsonArray("results") : null;
        if (results == null || results.size() == 0) {
            throw new WeatherApiException("City not found: \"" + cityName.trim()
                    + "\". Check the spelling and try again.");
        }
        return parseGeocodingResult(results.get(0).getAsJsonObject());
    }

    /**
     * Fetches current conditions and the hourly forecast for a location.
     */
    public WeatherReport fetchReport(GeocodingResult location) throws WeatherApiException {
        String url = FORECAST_URL
                + "?latitude=" + location.latitude()
                + "&longitude=" + location.longitude()
                + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,"
                + "is_day,weather_code,wind_speed_10m,wind_direction_10m"
                + "&hourly=temperature_2m,weather_code&forecast_days=2&timezone=auto";
        JsonObject root = getJson(url);
        if (root.has("error") && root.get("error").getAsBoolean()) {
            String reason = root.has("reason") ? root.get("reason").getAsString() : "unknown error";
            throw new WeatherApiException("Weather service error: " + reason);
        }
        CurrentWeather current = parseCurrent(root.getAsJsonObject("current"));
        List<ForecastHour> forecast = parseForecast(root.getAsJsonObject("hourly"),
                current.observedAtIso());
        return new WeatherReport(location, current, forecast, Instant.now());
    }

    /**
     * Sends one GET request and parses the body as a JSON object.
     * All network and HTTP problems become WeatherApiException.
     */
    private JsonObject getJson(String url) throws WeatherApiException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException error) {
            throw new WeatherApiException(
                    "Network problem: could not reach the weather service. "
                    + "Check your internet connection.", error);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new WeatherApiException("The request was interrupted.", error);
        }
        if (response.statusCode() != 200) {
            throw new WeatherApiException("Weather service returned HTTP "
                    + response.statusCode() + ". Please try again later.");
        }
        try {
            return JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (RuntimeException error) {
            throw new WeatherApiException("The weather service sent a malformed response.", error);
        }
    }

    /**
     * Parses one "results" entry of the geocoding response.
     */
    static GeocodingResult parseGeocodingResult(JsonObject entry) {
        return new GeocodingResult(
                text(entry, "name"),
                text(entry, "country"),
                text(entry, "admin1"),
                entry.get("latitude").getAsDouble(),
                entry.get("longitude").getAsDouble(),
                text(entry, "timezone"));
    }

    /**
     * Parses the "current" object of the forecast response.
     */
    static CurrentWeather parseCurrent(JsonObject current) {
        return new CurrentWeather(
                text(current, "time"),
                current.get("temperature_2m").getAsDouble(),
                current.get("relative_humidity_2m").getAsInt(),
                current.get("apparent_temperature").getAsDouble(),
                current.get("wind_speed_10m").getAsDouble(),
                current.get("wind_direction_10m").getAsInt(),
                current.get("is_day").getAsInt() == 1,
                current.get("weather_code").getAsInt());
    }

    /**
     * Parses the "hourly" object and returns the next FORECAST_HOURS
     * entries at or after the current observation time, which becomes the
     * short-term forecast strip in the GUI.
     */
    static List<ForecastHour> parseForecast(JsonObject hourly, String currentIso) {
        JsonArray times = hourly.getAsJsonArray("time");
        JsonArray temps = hourly.getAsJsonArray("temperature_2m");
        JsonArray codes = hourly.getAsJsonArray("weather_code");
        List<ForecastHour> forecast = new ArrayList<>();
        int start = 0;
        while (start < times.size() && times.get(start).getAsString().compareTo(currentIso) < 0) {
            start++;
        }
        for (int i = start; i < times.size() && forecast.size() < FORECAST_HOURS; i++) {
            forecast.add(new ForecastHour(
                    times.get(i).getAsString(),
                    temps.get(i).getAsDouble(),
                    codes.get(i).getAsInt()));
        }
        return List.copyOf(forecast);
    }

    /** Reads an optional string member, mapping missing/null to "". */
    private static String text(JsonObject object, String member) {
        if (object.has(member) && !object.get(member).isJsonNull()) {
            return object.get(member).getAsString();
        }
        return "";
    }
}

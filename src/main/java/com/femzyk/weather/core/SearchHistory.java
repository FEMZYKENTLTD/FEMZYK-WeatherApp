package com.femzyk.weather.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * SearchHistory - the user's recent weather searches, persisted to disk.
 *
 * CS 1103-01, Programming Assignment Unit 8. Implements the history
 * tracking requirement: every successful search is stored with a
 * timestamp, the newest entry first, capped at a small maximum. The list
 * survives restarts because it is saved as JSON in the user's home
 * directory (~/.femzyk-weather/history.json) - file I/O in the spirit of
 * Units 4 and 7. Re-searching a city moves its existing entry to the top
 * instead of duplicating it. A corrupted or missing file is treated as an
 * empty history, never a crash.
 */
public class SearchHistory {

    /** Newest entries are kept up to this many. */
    public static final int MAX_ENTRIES = 8;

    private final Path file;

    /** Creates a history backed by the default file in the user's home. */
    public SearchHistory() {
        this(Path.of(System.getProperty("user.home"), ".femzyk-weather", "history.json"));
    }

    /** Creates a history backed by an explicit file (used by the tests). */
    public SearchHistory(Path file) {
        this.file = file;
    }

    /** Returns the stored entries, newest first. */
    public List<HistoryEntry> load() {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            JsonArray array = JsonParser.parseString(Files.readString(file)).getAsJsonArray();
            return fromJson(array);
        } catch (IOException | RuntimeException error) {
            // Missing or unreadable file: start with an empty history.
            return new ArrayList<>();
        }
    }

    /**
     * Adds a search result at the top of the history, removes duplicates
     * of the same city (case-insensitive), trims to the capacity, saves
     * to disk, and returns the updated list.
     */
    public List<HistoryEntry> add(HistoryEntry entry) {
        List<HistoryEntry> entries = load();
        entries.removeIf(existing -> existing.city().equalsIgnoreCase(entry.city()));
        entries.add(0, entry);
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
        save(entries);
        return entries;
    }

    /**
     * Saves the entries as a JSON array, creating the folder if needed.
     * A failed save is silently ignored: losing a history line must never
     * break a weather search.
     */
    public void save(List<HistoryEntry> entries) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, toJson(entries).toString());
        } catch (IOException error) {
            System.out.println("Could not save history: " + error.getMessage());
        }
    }

    /** Serializes entries to JSON (manual, so records stay reflection-free). */
    static JsonArray toJson(List<HistoryEntry> entries) {
        JsonArray array = new JsonArray();
        for (HistoryEntry entry : entries) {
            JsonObject object = new JsonObject();
            object.addProperty("city", entry.city());
            object.addProperty("country", entry.country());
            object.addProperty("temperatureC", entry.temperatureC());
            object.addProperty("weatherCode", entry.weatherCode());
            object.addProperty("searchedAt", entry.searchedAt().toString());
            array.add(object);
        }
        return array;
    }

    /** Parses entries back from JSON produced by {@link #toJson}. */
    static List<HistoryEntry> fromJson(JsonArray array) {
        List<HistoryEntry> entries = new ArrayList<>();
        for (var element : array) {
            JsonObject object = element.getAsJsonObject();
            entries.add(new HistoryEntry(
                    object.get("city").getAsString(),
                    object.has("country") ? object.get("country").getAsString() : "",
                    object.get("temperatureC").getAsDouble(),
                    object.get("weatherCode").getAsInt(),
                    LocalDateTime.parse(object.get("searchedAt").getAsString())));
        }
        return entries;
    }
}

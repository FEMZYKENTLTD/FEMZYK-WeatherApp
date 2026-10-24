package com.femzyk.weather.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for the persistent search history.
 *
 * CS 1103-01, Programming Assignment Unit 8. Verifies the history
 * tracking requirement: round trip through the JSON file, newest-first
 * ordering, case-insensitive de-duplication, the capacity limit, and the
 * safe treatment of a corrupted file.
 */
class SearchHistoryTest {

    @TempDir
    Path tempDir;

    private HistoryEntry entry(String city, double temperatureC, int code, int hour) {
        return new HistoryEntry(city, "Testland", temperatureC, code,
                LocalDateTime.of(2026, 10, 24, hour, 0));
    }

    @Test
    void entriesRoundTripThroughTheFile() {
        Path file = tempDir.resolve("history.json");
        SearchHistory history = new SearchHistory(file);
        history.add(entry("Lagos", 28.2, 3, 10));
        history.add(entry("Tokyo", 18.0, 2, 11));

        List<HistoryEntry> loaded = new SearchHistory(file).load();
        assertEquals(2, loaded.size());
        assertEquals("Tokyo", loaded.get(0).city());
        assertEquals(18.0, loaded.get(0).temperatureC(), 0.0001);
        assertEquals(LocalDateTime.of(2026, 10, 24, 11, 0), loaded.get(0).searchedAt());
    }

    @Test
    void missingFileMeansEmptyHistory() {
        assertTrue(new SearchHistory(tempDir.resolve("absent.json")).load().isEmpty());
    }

    @Test
    void reSearchingMovesTheCityToTheTopWithoutDuplicates() {
        SearchHistory history = new SearchHistory(tempDir.resolve("history.json"));
        history.add(entry("Lagos", 28.2, 3, 10));
        history.add(entry("Tokyo", 18.0, 2, 11));
        history.add(entry("LAGOS", 27.5, 2, 12));

        List<HistoryEntry> entries = history.load();
        assertEquals(2, entries.size());
        // The most recent spelling the user typed wins.
        assertEquals("LAGOS", entries.get(0).city());
        assertEquals(27.5, entries.get(0).temperatureC(), 0.0001);
    }

    @Test
    void historyIsCappedAtTheMaximumSize() {
        SearchHistory history = new SearchHistory(tempDir.resolve("history.json"));
        for (int i = 0; i < SearchHistory.MAX_ENTRIES + 4; i++) {
            history.add(entry("City" + i, 20.0 + i, 1, i % 24));
        }
        List<HistoryEntry> entries = history.load();
        assertEquals(SearchHistory.MAX_ENTRIES, entries.size());
        assertEquals("City" + (SearchHistory.MAX_ENTRIES + 3), entries.get(0).city());
    }

    @Test
    void corruptedFileIsTreatedAsEmptyHistory() throws Exception {
        Path file = tempDir.resolve("history.json");
        Files.writeString(file, "{ this is not valid json ]");
        assertTrue(new SearchHistory(file).load().isEmpty());
    }
}

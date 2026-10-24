package com.femzyk.weather.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the WMO weather-code mapping.
 *
 * CS 1103-01, Programming Assignment Unit 8. Open-Meteo reports codes
 * such as 0 (clear) and 95 (thunderstorm); these tests pin the codes the
 * screenshots show, plus the safe fallback for unknown codes.
 */
class WmoWeatherTest {

    @Test
    void zeroIsClearSkyWithASunIcon() {
        WmoWeather weather = WmoWeather.fromCode(0);
        assertEquals("Clear sky", weather.description());
        assertEquals("sun", weather.iconKey());
    }

    @Test
    void twoIsPartlyCloudy() {
        assertEquals("Partly cloudy", WmoWeather.fromCode(2).description());
        assertEquals("sun-cloud", WmoWeather.fromCode(2).iconKey());
    }

    @Test
    void threeIsOvercastWithACloudIcon() {
        assertEquals("Overcast", WmoWeather.fromCode(3).description());
        assertEquals("cloud", WmoWeather.fromCode(3).iconKey());
    }

    @Test
    void sixtyOneIsRain() {
        assertEquals("Slight rain", WmoWeather.fromCode(61).description());
        assertEquals("rain", WmoWeather.fromCode(61).iconKey());
    }

    @Test
    void ninetyFiveIsAThunderstorm() {
        assertEquals("Thunderstorm", WmoWeather.fromCode(95).description());
        assertEquals("storm", WmoWeather.fromCode(95).iconKey());
    }

    @Test
    void ninetyNineDescribesHeavyHail() {
        assertEquals("Thunderstorm with heavy hail", WmoWeather.fromCode(99).description());
    }

    @Test
    void unknownCodesFallBackSafely() {
        assertNotNull(WmoWeather.fromCode(42));
        assertEquals("cloud", WmoWeather.fromCode(42).iconKey());
    }
}

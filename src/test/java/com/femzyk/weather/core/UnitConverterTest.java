package com.femzyk.weather.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for UnitConverter.
 *
 * CS 1103-01, Programming Assignment Unit 8. Verifies the assignment's
 * unit conversion requirement: Celsius/Fahrenheit, km/h/mph, display
 * formatting, and the 16-point compass mapping.
 */
class UnitConverterTest {

    private static final double DELTA = 0.0001;

    @Test
    void zeroCelsiusIsThirtyTwoFahrenheit() {
        assertEquals(32.0, UnitConverter.celsiusToFahrenheit(0.0), DELTA);
    }

    @Test
    void boilingCelsiusIsTwoTwelveFahrenheit() {
        assertEquals(212.0, UnitConverter.celsiusToFahrenheit(100.0), DELTA);
    }

    @Test
    void bodyTemperatureConverts() {
        assertEquals(98.6, UnitConverter.celsiusToFahrenheit(37.0), 0.01);
    }

    @Test
    void fahrenheitToCelsiusRoundTrip() {
        assertEquals(28.2, UnitConverter.fahrenheitToCelsius(
                UnitConverter.celsiusToFahrenheit(28.2)), DELTA);
    }

    @Test
    void hundredKmhIsAboutSixtyTwoMph() {
        assertEquals(62.1371, UnitConverter.kmhToMph(100.0), DELTA);
    }

    @Test
    void mphRoundTripReturnsToKmh() {
        assertEquals(10.9, UnitConverter.mphToKmh(UnitConverter.kmhToMph(10.9)), DELTA);
    }

    @Test
    void temperatureFormatsInBothUnits() {
        assertEquals("28\u00B0C", UnitConverter.formatTemperature(28.2, false));
        assertEquals("83\u00B0F", UnitConverter.formatTemperature(28.2, true));
    }

    @Test
    void windFormatsInBothUnits() {
        assertEquals("11 km/h", UnitConverter.formatWind(10.9, false));
        assertEquals("7 mph", UnitConverter.formatWind(10.9, true));
    }

    @Test
    void compassPointsCoverTheCardinalDirections() {
        assertEquals("N", UnitConverter.windCompass(0));
        assertEquals("E", UnitConverter.windCompass(90));
        assertEquals("S", UnitConverter.windCompass(180));
        assertEquals("SW", UnitConverter.windCompass(225));
        assertEquals("W", UnitConverter.windCompass(270));
    }

    @Test
    void compassWrapsAroundTheNorthPoint() {
        assertEquals("N", UnitConverter.windCompass(359));
        assertEquals("N", UnitConverter.windCompass(361));
        assertEquals("NNW", UnitConverter.windCompass(348));
    }

    @Test
    void negativeDegreesMapCounterClockwise() {
        // -90 degrees is 90 degrees counter-clockwise from north: west.
        assertEquals("W", UnitConverter.windCompass(-90));
    }
}

package com.femzyk.weather.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.paint.Color;

/**
 * WeatherIconCanvas - draws the weather icons as vector graphics.
 *
 * CS 1103-01, Programming Assignment Unit 8. Implements the icon
 * representation requirement: sun for clear sky, clouds for cloudy
 * weather, rain drops, snow flakes, a lightning bolt, and fog bars. All
 * icons are drawn with JavaFX Canvas primitives, so they scale crisply
 * to any size and never depend on image files or emoji fonts. At night
 * the clear-sky icons become a moon, which supports the dynamic
 * day/night theming of the app.
 */
public class WeatherIconCanvas extends Canvas {

    private static final Color SUN_COLOR = Color.web("#FFC53D");
    private static final Color MOON_COLOR = Color.web("#E9EDF5");
    private static final Color CRATER_COLOR = Color.web("#C2CCE0");
    private static final Color RAIN_COLOR = Color.web("#7CC4FF");
    private static final Color SNOW_COLOR = Color.web("#F5FAFF");
    private static final Color BOLT_COLOR = Color.web("#FFD600");
    private static final Color FOG_COLOR = Color.web("#B7C4CE");

    private WeatherIconCanvas(double size) {
        super(size, size);
    }

    /**
     * Creates an icon for a weather code key. Night switches the sun to a
     * moon and the sun-behind-cloud to a moon-behind-cloud.
     *
     * @param iconKey one of "sun", "sun-cloud", "cloud", "rain", "snow",
     *                "storm", "fog"
     * @param isDay   false renders night versions of sun-based icons
     * @param size    side length in pixels
     */
    public static WeatherIconCanvas create(String iconKey, boolean isDay, double size) {
        WeatherIconCanvas canvas = new WeatherIconCanvas(size);
        canvas.draw(iconKey, isDay);
        return canvas;
    }

    private void draw(String iconKey, boolean isDay) {
        GraphicsContext g = getGraphicsContext2D();
        double s = getWidth();
        switch (iconKey) {
            case "sun" -> {
                if (isDay) {
                    drawSun(g, s, 0.5 * s, 0.5 * s, 1.0);
                } else {
                    drawMoon(g, s);
                }
            }
            case "sun-cloud" -> {
                if (isDay) {
                    drawSun(g, s, 0.32 * s, 0.32 * s, 0.62);
                } else {
                    drawSmallMoon(g, s);
                }
                drawCloud(g, s, Color.web("#F4F7FA"));
            }
            case "cloud" -> drawCloud(g, s, Color.web("#ECEFF1"));
            case "rain" -> {
                drawCloud(g, s, Color.web("#CFD8DC"));
                drawRain(g, s);
            }
            case "snow" -> {
                drawCloud(g, s, Color.web("#ECEFF1"));
                drawSnow(g, s);
            }
            case "storm" -> {
                drawCloud(g, s, Color.web("#9FB0BC"));
                drawBolt(g, s);
            }
            case "fog" -> drawFog(g, s);
            default -> drawCloud(g, s, Color.web("#ECEFF1"));
        }
    }

    /** Draws a sun disc with eight rays around the given centre. */
    private void drawSun(GraphicsContext g, double s, double cx, double cy, double scale) {
        double radius = 0.13 * s * scale;
        double rayInner = radius * 1.5;
        double rayOuter = radius * 2.1;
        g.setStroke(SUN_COLOR);
        g.setLineWidth(s * 0.032);
        g.setLineCap(StrokeLineCap.ROUND);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45.0);
            double dx = Math.cos(angle);
            double dy = Math.sin(angle);
            g.strokeLine(cx + dx * rayInner, cy + dy * rayInner,
                    cx + dx * rayOuter, cy + dy * rayOuter);
        }
        g.setFill(SUN_COLOR);
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
    }

    /** Draws a full moon with a few craters, for clear nights. */
    private void drawMoon(GraphicsContext g, double s) {
        double cx = 0.5 * s;
        double cy = 0.48 * s;
        double radius = 0.17 * s;
        g.setFill(MOON_COLOR);
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        g.setFill(CRATER_COLOR);
        g.fillOval(cx - 0.07 * s, cy - 0.07 * s, 0.07 * s, 0.07 * s);
        g.fillOval(cx + 0.02 * s, cy + 0.03 * s, 0.05 * s, 0.05 * s);
        g.fillOval(cx - 0.02 * s, cy + 0.09 * s, 0.035 * s, 0.035 * s);
    }

    /** Draws a smaller moon for the night variant of "sun-cloud". */
    private void drawSmallMoon(GraphicsContext g, double s) {
        double cx = 0.30 * s;
        double cy = 0.26 * s;
        double radius = 0.10 * s;
        g.setFill(MOON_COLOR);
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        g.setFill(CRATER_COLOR);
        g.fillOval(cx - 0.04 * s, cy - 0.03 * s, 0.04 * s, 0.04 * s);
    }

    /** Draws the puffy cloud used by most weather icons. */
    private void drawCloud(GraphicsContext g, double s, Color color) {
        g.setFill(color);
        g.fillOval(0.16 * s, 0.46 * s, 0.26 * s, 0.26 * s);
        g.fillOval(0.33 * s, 0.32 * s, 0.34 * s, 0.34 * s);
        g.fillOval(0.56 * s, 0.46 * s, 0.28 * s, 0.28 * s);
        g.fillRoundRect(0.16 * s, 0.54 * s, 0.68 * s, 0.18 * s, 0.16 * s, 0.16 * s);
    }

    /** Draws three slanted rain drops under the cloud. */
    private void drawRain(GraphicsContext g, double s) {
        g.setStroke(RAIN_COLOR);
        g.setLineWidth(s * 0.030);
        g.setLineCap(StrokeLineCap.ROUND);
        double[][] drops = {{0.36, 0.50}, {0.52, 0.54}, {0.68, 0.50}};
        for (double[] drop : drops) {
            g.strokeLine(drop[0] * s, drop[1] * s,
                    (drop[0] - 0.05) * s, (drop[1] + 0.16) * s);
        }
    }

    /** Draws three snow flakes under the cloud. */
    private void drawSnow(GraphicsContext g, double s) {
        g.setFill(SNOW_COLOR);
        double[][] flakes = {{0.34, 0.78, 0.035}, {0.51, 0.84, 0.030}, {0.66, 0.77, 0.033}};
        for (double[] flake : flakes) {
            double r = flake[2] * s;
            g.fillOval(flake[0] * s - r, flake[1] * s - r, r * 2, r * 2);
        }
    }

    /** Draws the lightning bolt for thunderstorms. */
    private void drawBolt(GraphicsContext g, double s) {
        double[] xs = {0.52, 0.38, 0.48, 0.44, 0.58, 0.50};
        double[] ys = {0.60, 0.86, 0.86, 1.00, 0.78, 0.78};
        double[] pixelsX = new double[xs.length];
        double[] pixelsY = new double[ys.length];
        for (int i = 0; i < xs.length; i++) {
            pixelsX[i] = xs[i] * s;
            pixelsY[i] = ys[i] * s;
        }
        g.setFill(BOLT_COLOR);
        g.fillPolygon(pixelsX, pixelsY, xs.length);
    }

    /** Draws fog: a small cloud above two soft horizontal bars. */
    private void drawFog(GraphicsContext g, double s) {
        g.setFill(Color.web("#ECEFF1"));
        g.fillOval(0.26 * s, 0.26 * s, 0.30 * s, 0.30 * s);
        g.fillOval(0.42 * s, 0.20 * s, 0.32 * s, 0.32 * s);
        g.fillRoundRect(0.26 * s, 0.36 * s, 0.48 * s, 0.16 * s, 0.14 * s, 0.14 * s);
        g.setStroke(FOG_COLOR);
        g.setLineWidth(s * 0.034);
        g.setLineCap(StrokeLineCap.ROUND);
        g.strokeLine(0.24 * s, 0.66 * s, 0.76 * s, 0.66 * s);
        g.strokeLine(0.34 * s, 0.80 * s, 0.66 * s, 0.80 * s);
    }
}

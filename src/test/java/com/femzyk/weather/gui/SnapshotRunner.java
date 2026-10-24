package com.femzyk.weather.gui;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * SnapshotRunner - automated screenshot harness for the assignment.
 *
 * Extends WeatherApp, drives three real searches through the public
 * performSearch method, and saves the scene as PNG after each one:
 * New York (daytime palette), Tokyo (night palette at the same moment -
 * the dynamic background requirement), and an invalid city (the styled
 * error banner - the error handling requirement). Not part of the
 * shipped application; lives in the test sources.
 */
public class SnapshotRunner extends WeatherApp {

    private static final int WIDTH = 1060;
    private static final int HEIGHT = 740;

    @Override
    public void start(Stage stage) {
        super.start(stage);
        Timeline schedule = new Timeline(
                new KeyFrame(Duration.seconds(1.5),
                        event -> performSearch("New York")),
                new KeyFrame(Duration.seconds(6.5),
                        event -> capture(stage, "/tmp/weather_newyork.png")),
                new KeyFrame(Duration.seconds(7.0),
                        event -> selectUnit(stage, "\u00B0F")),
                new KeyFrame(Duration.seconds(7.2),
                        event -> selectUnit(stage, "mph")),
                new KeyFrame(Duration.seconds(8.2),
                        event -> capture(stage, "/tmp/weather_imperial.png")),
                new KeyFrame(Duration.seconds(8.8),
                        event -> performSearch("Tokyo")),
                new KeyFrame(Duration.seconds(14.0),
                        event -> capture(stage, "/tmp/weather_tokyo.png")),
                new KeyFrame(Duration.seconds(14.6),
                        event -> performSearch("Xyzzyville123")),
                new KeyFrame(Duration.seconds(19.2),
                        event -> capture(stage, "/tmp/weather_error.png")),
                new KeyFrame(Duration.seconds(20.0),
                        event -> Platform.exit()));
        schedule.play();
    }

    /**
     * Switches a unit toggle (for example the Fahrenheit button) by
     * looking it up through the scene graph, so no production code needs
     * test-only entry points. fire() selects the button and runs its
     * action, which re-renders every label in the new unit.
     */
    private void selectUnit(Stage stage, String caption) {
        for (javafx.scene.Node node : stage.getScene().getRoot()
                .lookupAll(".toggle-button")) {
            if (node instanceof javafx.scene.control.ToggleButton toggle
                    && caption.equals(toggle.getText()) && !toggle.isSelected()) {
                toggle.fire();
            }
        }
    }

    /** Renders the scene into a PNG file. */
    private void capture(Stage stage, String path) {
        try {
            Scene scene = stage.getScene();
            int width = (int) scene.getWidth();
            int height = (int) scene.getHeight();
            WritableImage image = new WritableImage(width, height);
            scene.snapshot(image);
            BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            PixelReader reader = image.getPixelReader();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    buffered.setRGB(x, y, reader.getArgb(x, y));
                }
            }
            ImageIO.write(buffered, "png", new File(path));
            System.out.println("Saved " + path + " (" + width + "x" + height + ")");
        } catch (Exception error) {
            System.out.println("Capture failed: " + error);
        }
    }
}

package com.femzyk.weather.gui;

import javafx.application.Application;

/**
 * SnapshotMain - bootstrap for the automated screenshot harness.
 *
 * A plain main class (not an Application subclass) so the runner works
 * from the classpath under Xvfb. Not part of the shipped application.
 */
public final class SnapshotMain {

    private SnapshotMain() {
    }

    /** Launches SnapshotRunner via the JavaFX launcher. */
    public static void main(String[] args) {
        Application.launch(SnapshotRunner.class, args);
    }
}

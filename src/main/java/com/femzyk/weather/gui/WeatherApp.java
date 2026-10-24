package com.femzyk.weather.gui;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import com.femzyk.weather.core.CurrentWeather;
import com.femzyk.weather.core.ForecastHour;
import com.femzyk.weather.core.HistoryEntry;
import com.femzyk.weather.core.SearchHistory;
import com.femzyk.weather.core.UnitConverter;
import com.femzyk.weather.core.WeatherApiClient;
import com.femzyk.weather.core.WeatherApiException;
import com.femzyk.weather.core.WeatherReport;
import com.femzyk.weather.core.WmoWeather;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;

/**
 * WeatherApp - the JavaFX front end of the FEMZYK Weather application.
 *
 * CS 1103-01, Programming Assignment Unit 8. The user types a city, the
 * app resolves it with the Open-Meteo geocoding API and displays current
 * conditions, a short-term hourly forecast, and the recent search
 * history, all in a styled interface whose sky-gradient background
 * follows the searched city's real time of day (the API's is_day flag
 * and local hour drive four palettes: dawn, day, sunset, night).
 *
 * Threading: the API call runs on a background thread via
 * CompletableFuture, and only the finished result touches the UI through
 * Platform.runLater - the window never freezes while the network is
 * busy. Errors (unknown city, network down, HTTP failures) surface in a
 * status banner instead of crashing the app.
 */
public class WeatherApp extends Application {

    private static final double WINDOW_WIDTH = 1060;
    private static final double WINDOW_HEIGHT = 740;

    private final WeatherApiClient apiClient = new WeatherApiClient();
    private final SearchHistory searchHistory = new SearchHistory();

    private final DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern("HH:mm");

    private StackPane root;
    private TextField cityField;
    private Button searchButton;
    private Label statusLabel;
    private Label locationLabel;
    private Label localTimeLabel;
    private Label temperatureLabel;
    private Label descriptionLabel;
    private Label feelsLikeLabel;
    private Label humidityLabel;
    private Label windLabel;
    private HBox forecastBox;
    private ListView<HistoryEntry> historyList;
    private StackPane iconPane;
    private ToggleButton celsiusButton;
    private ToggleButton fahrenheitButton;
    private ToggleButton kmhButton;
    private ToggleButton mphButton;

    private WeatherReport lastReport;
    private List<HistoryEntry> historyEntries = new ArrayList<>();

    /** The four sky palettes used by the dynamic background. */
    private enum ThemePhase {
        DAWN, DAY, SUNSET, NIGHT
    }

    @Override
    public void start(Stage stage) {
        BorderPane layout = buildLayout();
        root = new StackPane(layout);
        applyTheme(ThemePhase.DAY);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        URL stylesheet = getClass().getResource("/css/styles.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }

        stage.setTitle("FEMZYK Weather - Real-time Weather Information");
        stage.setMinWidth(900);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();

        refreshHistoryView();
    }

    // ------------------------------------------------------------------
    // Layout
    // ------------------------------------------------------------------

    /** Builds the whole interface and returns the root layout. */
    private BorderPane buildLayout() {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(18));
        layout.setTop(buildHeader());
        layout.setCenter(buildMainArea());
        layout.setBottom(buildHistoryCard());
        return layout;
    }

    /** Title row plus the input row (city field, search, unit toggles). */
    private VBox buildHeader() {
        Label title = new Label("FEMZYK Weather");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");
        Label subtitle = new Label("real-time weather - powered by the Open-Meteo API");
        subtitle.setStyle("-fx-font-size: 12px; -fx-font-style: italic; -fx-opacity: 0.85;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox titleRow = new HBox(12, title, spacer, subtitle);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        cityField = new TextField();
        cityField.setPromptText("Enter a city, e.g. Lagos, Tokyo, New York");
        cityField.setPrefColumnCount(24);
        HBox.setHgrow(cityField, Priority.ALWAYS);

        searchButton = new Button("Search");
        searchButton.setOnAction(event -> performSearch(null));
        cityField.setOnAction(event -> performSearch(null));

        ToggleGroup temperatureGroup = new ToggleGroup();
        celsiusButton = new ToggleButton("\u00B0C");
        fahrenheitButton = new ToggleButton("\u00B0F");
        celsiusButton.setToggleGroup(temperatureGroup);
        fahrenheitButton.setToggleGroup(temperatureGroup);
        celsiusButton.setSelected(true);
        ToggleGroup windGroup = new ToggleGroup();
        kmhButton = new ToggleButton("km/h");
        mphButton = new ToggleButton("mph");
        kmhButton.setToggleGroup(windGroup);
        mphButton.setToggleGroup(windGroup);
        kmhButton.setSelected(true);
        for (ToggleButton toggle : new ToggleButton[]{celsiusButton, fahrenheitButton, kmhButton, mphButton}) {
            toggle.setOnAction(event -> onUnitsChanged());
        }

        HBox inputRow = new HBox(8, cityField, searchButton,
                unitGroup("Temperature", celsiusButton, fahrenheitButton),
                unitGroup("Wind", kmhButton, mphButton));
        inputRow.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("Type a city and press Search to see live weather.");
        statusLabel.setStyle("-fx-font-size: 13px;");

        VBox header = new VBox(10, titleRow, inputRow, statusLabel);
        header.setPadding(new Insets(0, 0, 6, 0));
        return header;
    }

    /** One labelled pair of unit toggle buttons. */
    private VBox unitGroup(String caption, ToggleButton first, ToggleButton second) {
        Label label = new Label(caption);
        label.setStyle("-fx-font-size: 10px; -fx-opacity: 0.8;");
        HBox buttons = new HBox(4, first, second);
        buttons.setAlignment(Pos.CENTER);
        VBox group = new VBox(2, label, buttons);
        group.setAlignment(Pos.CENTER);
        group.setPadding(new Insets(0, 0, 0, 10));
        return group;
    }

    /** Current conditions card, forecast strip, and their titles. */
    private VBox buildMainArea() {
        iconPane = new StackPane();
        iconPane.setPrefSize(150, 150);
        iconPane.getChildren().add(WeatherIconCanvas.create("sun-cloud", true, 140));

        locationLabel = new Label("Search for a city");
        locationLabel.setStyle("-fx-font-size: 25px; -fx-font-weight: bold;");
        localTimeLabel = new Label("The app shows local time, conditions, forecast, and history.");
        localTimeLabel.setStyle("-fx-font-size: 13px; -fx-font-style: italic; -fx-opacity: 0.85;");
        temperatureLabel = new Label("--");
        temperatureLabel.setStyle("-fx-font-size: 60px; -fx-font-weight: bold;");
        descriptionLabel = new Label("");
        descriptionLabel.setStyle("-fx-font-size: 19px;");
        feelsLikeLabel = new Label("");
        feelsLikeLabel.setStyle("-fx-font-size: 14px; -fx-opacity: 0.9;");

        humidityLabel = new Label("Humidity: --");
        windLabel = new Label("Wind: --");
        humidityLabel.setStyle("-fx-font-size: 15px;");
        windLabel.setStyle("-fx-font-size: 15px;");
        VBox statBox = new VBox(12, humidityLabel, windLabel);
        statBox.setAlignment(Pos.CENTER_LEFT);
        statBox.setPadding(new Insets(8, 0, 0, 0));

        VBox details = new VBox(4, locationLabel, localTimeLabel, temperatureLabel,
                descriptionLabel, feelsLikeLabel);
        details.setAlignment(Pos.CENTER_LEFT);

        HBox currentCard = new HBox(30, iconPane, details, statBox);
        currentCard.setAlignment(Pos.CENTER_LEFT);
        styleCard(currentCard, 16);
        currentCard.setPadding(new Insets(20, 26, 20, 26));

        Label forecastTitle = new Label("Next hours");
        forecastTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        forecastBox = new HBox(10);
        forecastBox.setAlignment(Pos.CENTER_LEFT);

        VBox main = new VBox(14, currentCard, forecastTitle, forecastBox);
        main.setPadding(new Insets(8, 0, 8, 0));
        VBox.setVgrow(main, Priority.NEVER);
        return main;
    }

    /** The history card: recent searches with timestamps, click to re-run. */
    private VBox buildHistoryCard() {
        Label historyTitle = new Label("Recent searches (click a row to search again)");
        historyTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        historyList = new ListView<>();
        historyList.setPrefHeight(150);
        historyList.setPlaceholder(new Label("No searches yet - your history with timestamps will appear here."));
        historyList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(HistoryEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                setText(empty || entry == null ? null : historyText(entry));
            }
        });
        historyList.setOnMouseClicked(event -> {
            HistoryEntry selected = historyList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                performSearch(selected.city());
            }
        });

        VBox card = new VBox(8, historyTitle, historyList);
        styleCard(card, 14);
        card.setPadding(new Insets(14, 18, 14, 18));
        return card;
    }

    /** Gives a container the translucent rounded "glass card" look. */
    private void styleCard(Region region, double radius) {
        region.setBackground(new Background(new BackgroundFill(
                Color.rgb(10, 30, 55, 0.28), new CornerRadii(radius), Insets.EMPTY)));
        region.setBorder(new javafx.scene.layout.Border(new javafx.scene.layout.BorderStroke(
                Color.rgb(255, 255, 255, 0.18),
                javafx.scene.layout.BorderStrokeStyle.SOLID,
                new CornerRadii(radius),
                new javafx.scene.layout.BorderWidths(1))));
    }

    // ------------------------------------------------------------------
    // Searching (background thread + UI updates)
    // ------------------------------------------------------------------

    /**
     * Runs one search. Called by the Search button, the Enter key, and
     * the history list; also used by the screenshot test harness.
     *
     * @param cityName a specific city, or null to use the text field
     */
    public void performSearch(String cityName) {
        String query = cityName != null ? cityName : cityField.getText();
        if (query == null || query.isBlank()) {
            showError("Please enter a city name first.");
            return;
        }
        statusLabel.setTextFill(Color.web("#FFE082"));
        statusLabel.setText("Fetching weather for " + query.trim() + " ...");
        searchButton.setDisable(true);
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return apiClient.search(query);
                    } catch (WeatherApiException error) {
                        throw new CompletionException(error);
                    }
                })
                .orTimeout(30, TimeUnit.SECONDS)
                .whenComplete((report, error) -> Platform.runLater(() -> {
                    searchButton.setDisable(false);
                    if (error != null) {
                        showError(friendlyMessage(error));
                        return;
                    }
                    showReport(report);
                }));
    }

    /** Displays one successful report: labels, icon, forecast, theme. */
    private void showReport(WeatherReport report) {
        lastReport = report;
        CurrentWeather current = report.current();
        WmoWeather weather = WmoWeather.fromCode(current.weatherCode());

        iconPane.getChildren().setAll(
                WeatherIconCanvas.create(weather.iconKey(), current.isDay(), 140));
        renderWeather();
        recordHistory(report);
        setStatus("Updated " + LocalDateTime.now().format(hourFormat)
                + " (your time) - data by Open-Meteo", Color.web("#C8E6C9"));
    }

    /** Renders every number from lastReport using the current units. */
    private void renderWeather() {
        if (lastReport == null) {
            return;
        }
        boolean fahrenheit = fahrenheitButton.isSelected();
        boolean mph = mphButton.isSelected();
        CurrentWeather current = lastReport.current();
        WmoWeather weather = WmoWeather.fromCode(current.weatherCode());

        locationLabel.setText(lastReport.location().displayName());
        localTimeLabel.setText("Local time " + LocalDateTime.parse(current.observedAtIso())
                .format(DateTimeFormatter.ofPattern("HH:mm, EEEE d MMMM"))
                + " - " + lastReport.location().timezone());
        temperatureLabel.setText(UnitConverter.formatTemperature(current.temperatureC(), fahrenheit));
        descriptionLabel.setText(weather.description());
        feelsLikeLabel.setText("Feels like "
                + UnitConverter.formatTemperature(current.feelsLikeC(), fahrenheit));
        humidityLabel.setText("Humidity: " + current.humidityPct() + "%");
        windLabel.setText("Wind: " + UnitConverter.formatWind(current.windKmh(), mph)
                + " from " + UnitConverter.windCompass(current.windDirectionDeg())
                + " (" + current.windDirectionDeg() + "\u00B0)");

        forecastBox.getChildren().clear();
        for (ForecastHour hour : lastReport.hourlyForecast()) {
            forecastBox.getChildren().add(buildForecastCard(hour, fahrenheit));
        }
        applyTheme(phaseOf(current));
        historyList.refresh();
    }

    /** Builds one mini card of the hourly forecast strip. */
    private VBox buildForecastCard(ForecastHour hour, boolean fahrenheit) {
        WmoWeather weather = WmoWeather.fromCode(hour.weatherCode());
        Label time = new Label(hour.hourLabel());
        time.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        WeatherIconCanvas icon = WeatherIconCanvas.create(weather.iconKey(), isDaytimeHour(hour), 42);
        Label temp = new Label(UnitConverter.formatTemperature(hour.temperatureC(), fahrenheit));
        temp.setStyle("-fx-font-size: 13px;");
        VBox card = new VBox(5, time, icon, temp);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setBackground(new Background(new BackgroundFill(
                Color.rgb(10, 30, 55, 0.22), new CornerRadii(12), Insets.EMPTY)));
        return card;
    }

    /** Estimates day/night for a forecast hour by local clock time. */
    private boolean isDaytimeHour(ForecastHour hour) {
        int clockHour = Integer.parseInt(hour.timeIso().substring(11, 13));
        return clockHour >= 7 && clockHour < 18;
    }

    // ------------------------------------------------------------------
    // History, units, status, theme
    // ------------------------------------------------------------------

    /** Stores a successful search in the persistent history. */
    private void recordHistory(WeatherReport report) {
        HistoryEntry entry = new HistoryEntry(
                report.location().name(),
                report.location().country(),
                report.current().temperatureC(),
                report.current().weatherCode(),
                LocalDateTime.now());
        historyEntries = searchHistory.add(entry);
        refreshHistoryView();
    }

    /** Rebuilds the history list rows from the stored entries. */
    private void refreshHistoryView() {
        historyEntries = searchHistory.load();
        historyList.getItems().setAll(historyEntries);
    }

    /** One history row: timestamp + place + weather at that moment. */
    private String historyText(HistoryEntry entry) {
        String stamp = entry.searchedAt().format(DateTimeFormatter.ofPattern("d MMM, HH:mm"));
        boolean fahrenheit = fahrenheitButton.isSelected();
        String temperature = UnitConverter.formatTemperature(entry.temperatureC(), fahrenheit);
        String description = WmoWeather.fromCode(entry.weatherCode()).description();
        String place = entry.country().isBlank()
                ? entry.city() : entry.city() + ", " + entry.country();
        return stamp + "   " + place + "   " + temperature + ", " + description;
    }

    /** Re-renders numbers when a unit toggle changes. */
    private void onUnitsChanged() {
        renderWeather();
    }

    /** Shows a red error banner; the previous data stays on screen. */
    private void showError(String message) {
        setStatus(message, Color.web("#FF8A80"));
    }

    /** Sets the status banner text and colour. */
    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
    }

    /** Unwraps a completion failure into a user-safe message. */
    private String friendlyMessage(Throwable error) {
        Throwable cause = error;
        while (cause != null && !(cause instanceof WeatherApiException)) {
            cause = cause.getCause();
        }
        if (cause instanceof WeatherApiException apiError) {
            return apiError.getMessage();
        }
        return "Unexpected error: " + error.getClass().getSimpleName();
    }

    /**
     * Chooses the sky palette for a location from its real local time:
     * night unless the API says it is day, then dawn before 07:00,
     * sunset from 17:00, and day in between.
     */
    private ThemePhase phaseOf(CurrentWeather current) {
        if (!current.isDay()) {
            return ThemePhase.NIGHT;
        }
        int clockHour = Integer.parseInt(current.observedAtIso().substring(11, 13));
        if (clockHour < 7) {
            return ThemePhase.DAWN;
        }
        if (clockHour >= 17) {
            return ThemePhase.SUNSET;
        }
        return ThemePhase.DAY;
    }

    /** Paints the dynamic sky-gradient background for a phase. */
    private void applyTheme(ThemePhase phase) {
        Stop[] stops = switch (phase) {
            case DAY -> new Stop[]{
                    new Stop(0, Color.web("#1E5FA8")),
                    new Stop(0.6, Color.web("#3E86CC")),
                    new Stop(1, Color.web("#74AEDC"))};
            case DAWN -> new Stop[]{
                    new Stop(0, Color.web("#4A4286")),
                    new Stop(0.55, Color.web("#C96E93")),
                    new Stop(1, Color.web("#F5B26B"))};
            case SUNSET -> new Stop[]{
                    new Stop(0, Color.web("#35427E")),
                    new Stop(0.5, Color.web("#B95B85")),
                    new Stop(1, Color.web("#EE9B60"))};
            case NIGHT -> new Stop[]{
                    new Stop(0, Color.web("#0B1626")),
                    new Stop(0.5, Color.web("#16324F")),
                    new Stop(1, Color.web("#274B63"))};
        };
        root.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops),
                CornerRadii.EMPTY, Insets.EMPTY)));
    }

    /** The JavaFX entry point used by mvn javafx:run. */
    public static void main(String[] args) {
        launch(args);
    }
}

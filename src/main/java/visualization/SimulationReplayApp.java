package visualization;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import simulation.Main;
import simulation.Simulation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JavaFX application that can run the backend simulation and replay the generated CSV logs.
 * Users can launch a random scenario or provide explicit map/object counts directly from the UI.
 */
public class SimulationReplayApp extends Application {

    private static final Path TRUCKS_FILE = Path.of("TrucksCSV.txt");
    private static final Path WAREHOUSES_FILE = Path.of("WarehousesCSV.txt");
    private static final Path SHIPMENTS_FILE = Path.of("ShipmentsCSV.txt");
    private static final File JAVAFX_CONFIG_FILE = new File("javafx-config.txt");

    private Canvas canvas;
    private Label hourLabel;
    private Label speedLabel;
    private Label statusLabel;
    private Slider hourSlider;
    private Slider speedSlider;
    private Button playPauseButton;
    private Button runRandomButton;
    private Button runConfiguredButton;

    private TextField mapXField;
    private TextField mapYField;
    private TextField warehousesField;
    private TextField shipmentsField;
    private TextField trucksField;

    private ReplayData replayData;
    private Timeline timeline;
    private boolean sliderDragInProgress;
    private double playheadHour = 0.0;

    @Override
    public void start(Stage stage) {
        replayData = loadReplayData();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        canvas = new Canvas(1100, 700);
        root.setCenter(canvas);

        hourLabel = new Label("Hour: 0");
        speedLabel = new Label("Speed: 1.0x");
        statusLabel = new Label(replayData.message);

        hourSlider = new Slider(0, Math.max(0, replayData.maxHour), 0);
        hourSlider.setMajorTickUnit(Math.max(1, replayData.maxHour / 10.0));
        hourSlider.setMinorTickCount(0);
        hourSlider.setSnapToTicks(false);
        hourSlider.setPrefWidth(420);

        speedSlider = new Slider(0.25, 5.0, 1.0);
        speedSlider.setPrefWidth(180);

        playPauseButton = new Button("Play");
        playPauseButton.setOnAction(e -> togglePlayback());

        runRandomButton = new Button("Run backend (random)");
        runRandomButton.setOnAction(e -> runBackendWithRandomConfig());

        runConfiguredButton = new Button("Run backend (configured)");
        runConfiguredButton.setOnAction(e -> runBackendWithUserConfig());

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            timeline.stop();
            playPauseButton.setText("Play");
            playheadHour = 0.0;
            hourSlider.setValue(0.0);
            drawInterpolatedFrame(playheadHour);
        });

        Button previousButton = new Button("◀ Prev");
        previousButton.setOnAction(e -> {
            timeline.stop();
            playPauseButton.setText("Play");
            playheadHour = Math.max(0, Math.floor(playheadHour) - 1);
            hourSlider.setValue(playheadHour);
            drawInterpolatedFrame(playheadHour);
        });

        Button nextButton = new Button("Next ▶");
        nextButton.setOnAction(e -> {
            timeline.stop();
            playPauseButton.setText("Play");
            playheadHour = Math.min(replayData.maxHour, Math.ceil(playheadHour) + 1);
            hourSlider.setValue(playheadHour);
            drawInterpolatedFrame(playheadHour);
        });

        initializeConfigFields();
        HBox backendControls = createBackendControlBar();

        HBox controls = new HBox(10,
                playPauseButton,
                previousButton,
                nextButton,
                resetButton,
                new Label("Timeline:"), hourSlider,
                hourLabel,
                new Label("Speed:"), speedSlider, speedLabel);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox controlArea = new VBox(10, backendControls, controls);
        controlArea.setPadding(new Insets(10, 0, 0, 0));

        HBox footer = new HBox(statusLabel);
        footer.setPadding(new Insets(8, 0, 0, 0));

        BorderPane bottom = new BorderPane();
        bottom.setTop(controlArea);
        bottom.setBottom(footer);
        root.setBottom(bottom);

        setupTimeline();
        bindControls();
        drawInterpolatedFrame(0.0);

        Scene scene = new Scene(root, 1420, 900);
        stage.setTitle("Transport Simulation Replay Viewer");
        stage.setScene(scene);
        stage.show();
    }

    /** Initializes text fields with valid defaults and sample values. */
    private void initializeConfigFields() {
        mapXField = new TextField("500");
        mapYField = new TextField("500");
        warehousesField = new TextField("20");
        shipmentsField = new TextField("100");
        trucksField = new TextField("20");

        mapXField.setPrefWidth(70);
        mapYField.setPrefWidth(70);
        warehousesField.setPrefWidth(70);
        shipmentsField.setPrefWidth(70);
        trucksField.setPrefWidth(70);
    }

    /** Creates the UI controls for running backend simulation from JavaFX. */
    private HBox createBackendControlBar() {
        HBox backendControls = new HBox(8,
                runRandomButton,
                new Label("Map X:"), mapXField,
                new Label("Map Y:"), mapYField,
                new Label("Warehouses:"), warehousesField,
                new Label("Shipments:"), shipmentsField,
                new Label("Trucks:"), trucksField,
                runConfiguredButton);
        backendControls.setAlignment(Pos.CENTER_LEFT);
        return backendControls;
    }

    private void bindControls() {
        hourSlider.setOnMousePressed(e -> sliderDragInProgress = true);
        hourSlider.setOnMouseReleased(e -> {
            sliderDragInProgress = false;
            playheadHour = hourSlider.getValue();
            drawInterpolatedFrame(playheadHour);
        });

        hourSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (sliderDragInProgress) {
                playheadHour = newVal.doubleValue();
                drawInterpolatedFrame(playheadHour);
            }
        });

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedLabel.setText(String.format("Speed: %.2fx", newVal.doubleValue()));
        });
    }

    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.millis(40), event -> {
            double deltaHours = (speedSlider.getValue() * 0.06);
            playheadHour = Math.min(replayData.maxHour, playheadHour + deltaHours);
            hourSlider.setValue(playheadHour);
            drawInterpolatedFrame(playheadHour);
            if (playheadHour >= replayData.maxHour) {
                timeline.stop();
                playPauseButton.setText("Play");
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    /** Runs backend simulation with random configuration values. */
    private void runBackendWithRandomConfig() {
        runBackendTask(() -> Main.randomConfiguration(JAVAFX_CONFIG_FILE), "Running backend with random configuration...");
    }

    /**
     * Runs backend simulation with user-specified values entered in the JavaFX inputs.
     * Input constraints are validated by {@link Main#configure(File, int, int, int, int, int)}.
     */
    private void runBackendWithUserConfig() {
        try {
            int mapX = Integer.parseInt(mapXField.getText().trim());
            int mapY = Integer.parseInt(mapYField.getText().trim());
            int warehouses = Integer.parseInt(warehousesField.getText().trim());
            int shipments = Integer.parseInt(shipmentsField.getText().trim());
            int trucks = Integer.parseInt(trucksField.getText().trim());

            runBackendTask(() -> Main.configure(JAVAFX_CONFIG_FILE, mapX, mapY, trucks, warehouses, shipments),
                    "Running backend with configured values...");
        } catch (NumberFormatException ex) {
            statusLabel.setText("Invalid input: all configured values must be whole numbers.");
        } catch (IllegalArgumentException ex) {
            statusLabel.setText("Invalid configured values: " + ex.getMessage());
        }
    }

    /**
     * Executes backend generation + simulation in a background task and refreshes replay once complete.
     */
    private void runBackendTask(Runnable configurationWriter, String runningMessage) {
        timeline.stop();
        playPauseButton.setText("Play");
        setBackendButtonsDisabled(true);
        statusLabel.setText(runningMessage);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                configurationWriter.run();
                Simulation simulation = new Simulation(JAVAFX_CONFIG_FILE);
                simulation.simulate();
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            reloadReplayData();
            statusLabel.setText("Backend run completed. Replay refreshed from newly generated CSV logs.");
            setBackendButtonsDisabled(false);
        });

        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            statusLabel.setText("Backend run failed: " + (ex == null ? "unknown error" : ex.getMessage()));
            setBackendButtonsDisabled(false);
        });

        Thread worker = new Thread(task, "simulation-backend-runner");
        worker.setDaemon(true);
        worker.start();
    }

    /** Enables/disables backend run controls during long-running simulation execution. */
    private void setBackendButtonsDisabled(boolean disabled) {
        runRandomButton.setDisable(disabled);
        runConfiguredButton.setDisable(disabled);
    }

    /** Reloads CSV data generated by backend and resets replay controls to start. */
    private void reloadReplayData() {
        replayData = loadReplayData();
        playheadHour = 0.0;
        hourSlider.setMin(0.0);
        hourSlider.setMax(Math.max(0, replayData.maxHour));
        hourSlider.setValue(0.0);
        hourSlider.setMajorTickUnit(Math.max(1, replayData.maxHour / 10.0));
        Platform.runLater(() -> drawInterpolatedFrame(0.0));
    }

    private void togglePlayback() {
        if (timeline.getStatus() == Animation.Status.RUNNING) {
            timeline.stop();
            playPauseButton.setText("Play");
            return;
        }

        if (playheadHour >= replayData.maxHour) {
            playheadHour = 0.0;
            hourSlider.setValue(0.0);
            drawInterpolatedFrame(0.0);
        }

        timeline.play();
        playPauseButton.setText("Pause");
    }

    private void drawInterpolatedFrame(double hourPosition) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(20, 24, 35));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int baseHour = (int) Math.floor(hourPosition);
        int nextHour = Math.min(replayData.maxHour, baseHour + 1);
        double t = hourPosition - baseHour;

        FrameState baseFrame = replayData.frames.getOrDefault(baseHour, new FrameState());
        FrameState nextFrame = replayData.frames.getOrDefault(nextHour, baseFrame);

        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double leftPad = 60;
        double topPad = 40;
        double mapW = w - (leftPad * 2);
        double mapH = h - (topPad * 2);

        gc.setStroke(Color.rgb(90, 105, 135));
        gc.setLineWidth(2.0);
        gc.strokeRect(leftPad, topPad, mapW, mapH);

        for (WarehouseState warehouse : baseFrame.warehouses.values()) {
            double x = leftPad + (warehouse.posX / replayData.mapX) * mapW;
            double y = topPad + (warehouse.posY / replayData.mapY) * mapH;
            gc.setFill(Color.web("#5ca9ff"));
            gc.fillOval(x - 8, y - 8, 16, 16);
            gc.setFill(Color.WHITE);
            gc.fillText("W" + warehouse.id, x + 10, y - 6);
        }

        for (TruckState currentTruck : baseFrame.trucks.values()) {
            TruckState nextTruck = nextFrame.trucks.getOrDefault(currentTruck.id, currentTruck);
            double lerpX = currentTruck.posX + ((nextTruck.posX - currentTruck.posX) * t);
            double lerpY = currentTruck.posY + ((nextTruck.posY - currentTruck.posY) * t);
            double x = leftPad + (lerpX / replayData.mapX) * mapW;
            double y = topPad + (lerpY / replayData.mapY) * mapH;

            gc.setFill(statusColor(currentTruck.status));
            gc.fillRect(x - 6, y - 6, 12, 12);
            gc.setFill(Color.WHITE);
            gc.fillText("T" + currentTruck.id, x + 8, y - 8);
        }

        int delivered = 0;
        for (ShipmentState shipment : baseFrame.shipments.values()) {
            if ("Delivered".equalsIgnoreCase(shipment.status)) {
                delivered++;
            }
        }

        gc.setFill(Color.WHITE);
        gc.fillText("Warehouses: " + baseFrame.warehouses.size(), 20, 20);
        gc.fillText("Trucks: " + baseFrame.trucks.size(), 160, 20);
        gc.fillText("Shipments delivered: " + delivered + "/" + baseFrame.shipments.size(), 260, 20);

        hourLabel.setText(String.format("Hour: %.2f / %d", hourPosition, replayData.maxHour));
    }

    private Color statusColor(String status) {
        if (status == null) return Color.GRAY;
        String normalized = status.trim().toLowerCase();
        if (normalized.contains("done")) return Color.LIMEGREEN;
        if (normalized.contains("wait")) return Color.GOLD;
        if (normalized.contains("move") || normalized.contains("travel")) return Color.ORANGE;
        return Color.CORNFLOWERBLUE;
    }

    private ReplayData loadReplayData() {
        try {
            List<String> truckLines = Files.readAllLines(TRUCKS_FILE);
            List<String> warehouseLines = Files.readAllLines(WAREHOUSES_FILE);
            List<String> shipmentLines = Files.readAllLines(SHIPMENTS_FILE);

            Map<Integer, FrameState> frames = new HashMap<>();
            Set<Integer> allHours = new HashSet<>();
            double maxX = 1.0;
            double maxY = 1.0;

            for (int i = 1; i < warehouseLines.size(); i++) {
                String[] cols = warehouseLines.get(i).split(",", -1);
                int hour = parseInt(cols, 0);
                int id = parseInt(cols, 1);
                double posX = parseDouble(cols, 2);
                double posY = parseDouble(cols, 3);
                int invSize = parseInt(cols, 7);
                maxX = Math.max(maxX, posX);
                maxY = Math.max(maxY, posY);

                FrameState frame = frames.computeIfAbsent(hour, k -> new FrameState());
                frame.warehouses.put(id, new WarehouseState(id, posX, posY, invSize));
                allHours.add(hour);
            }

            for (int i = 1; i < truckLines.size(); i++) {
                String[] cols = truckLines.get(i).split(",", -1);
                int hour = parseInt(cols, 0);
                int id = parseInt(cols, 1);
                double posX = parseDouble(cols, 2);
                double posY = parseDouble(cols, 3);
                String status = cols.length > 6 ? cols[6] : "";
                maxX = Math.max(maxX, posX);
                maxY = Math.max(maxY, posY);

                FrameState frame = frames.computeIfAbsent(hour, k -> new FrameState());
                frame.trucks.put(id, new TruckState(id, posX, posY, status));
                allHours.add(hour);
            }

            for (int i = 1; i < shipmentLines.size(); i++) {
                String[] cols = shipmentLines.get(i).split(",", -1);
                int hour = parseInt(cols, 0);
                int id = parseInt(cols, 1);
                String status = cols.length > 6 ? cols[6] : "";

                FrameState frame = frames.computeIfAbsent(hour, k -> new FrameState());
                frame.shipments.put(id, new ShipmentState(id, status));
                allHours.add(hour);
            }

            if (allHours.isEmpty()) {
                return ReplayData.empty("No replay rows found in CSV files.");
            }

            int maxHour = Collections.max(allHours);
            for (int hour = 0; hour <= maxHour; hour++) {
                frames.computeIfAbsent(hour, k -> new FrameState());
            }

            return new ReplayData(frames, maxHour, maxX, maxY,
                    "Loaded replay files successfully. Use buttons and sliders to navigate the replay.");
        } catch (IOException ex) {
            return ReplayData.empty("Could not load CSV files. Run simulation first to generate logs.");
        }
    }

    private int parseInt(String[] cols, int idx) {
        if (idx >= cols.length || cols[idx].isBlank()) return 0;
        return Integer.parseInt(cols[idx].trim());
    }

    private double parseDouble(String[] cols, int idx) {
        if (idx >= cols.length || cols[idx].isBlank()) return 0.0;
        return Double.parseDouble(cols[idx].trim());
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static class ReplayData {
        final Map<Integer, FrameState> frames;
        final int maxHour;
        final double mapX;
        final double mapY;
        final String message;

        ReplayData(Map<Integer, FrameState> frames, int maxHour, double mapX, double mapY, String message) {
            this.frames = frames;
            this.maxHour = maxHour;
            this.mapX = mapX;
            this.mapY = mapY;
            this.message = message;
        }

        static ReplayData empty(String message) {
            Map<Integer, FrameState> frames = new HashMap<>();
            frames.put(0, new FrameState());
            return new ReplayData(frames, 0, 1.0, 1.0, message);
        }
    }

    private static class FrameState {
        final Map<Integer, WarehouseState> warehouses = new HashMap<>();
        final Map<Integer, TruckState> trucks = new HashMap<>();
        final Map<Integer, ShipmentState> shipments = new HashMap<>();
    }

    private record WarehouseState(int id, double posX, double posY, int inventorySize) {}
    private record TruckState(int id, double posX, double posY, String status) {}
    private record ShipmentState(int id, String status) {}
}

package visualization;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimulationReplayApp extends Application {

    private static final Path TRUCKS_FILE = Path.of("TrucksCSV.txt");
    private static final Path WAREHOUSES_FILE = Path.of("WarehousesCSV.txt");
    private static final Path SHIPMENTS_FILE = Path.of("ShipmentsCSV.txt");

    private Canvas canvas;
    private Label hourLabel;
    private Label speedLabel;
    private Label statusLabel;
    private Slider hourSlider;
    private Slider speedSlider;
    private Button playPauseButton;

    private ReplayData replayData;
    private Timeline timeline;
    private boolean sliderDragInProgress;

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
        hourSlider.setPrefWidth(550);

        speedSlider = new Slider(0.25, 5.0, 1.0);
        speedSlider.setPrefWidth(220);

        playPauseButton = new Button("Play");
        playPauseButton.setOnAction(e -> togglePlayback());

        HBox controls = new HBox(12, playPauseButton, new Label("Timeline:"), hourSlider,
                hourLabel, new Label("Playback speed:"), speedSlider, speedLabel);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 0, 0));

        HBox footer = new HBox(statusLabel);
        footer.setPadding(new Insets(8, 0, 0, 0));

        BorderPane bottom = new BorderPane();
        bottom.setTop(controls);
        bottom.setBottom(footer);
        root.setBottom(bottom);

        setupTimeline();
        bindControls();
        drawCurrentFrame(0);

        Scene scene = new Scene(root, 1180, 830);
        stage.setTitle("Transport Simulation Replay Viewer");
        stage.setScene(scene);
        stage.show();
    }

    private void bindControls() {
        hourSlider.setOnMousePressed(e -> sliderDragInProgress = true);
        hourSlider.setOnMouseReleased(e -> {
            sliderDragInProgress = false;
            int hour = (int) Math.round(hourSlider.getValue());
            drawCurrentFrame(hour);
        });

        hourSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (sliderDragInProgress) {
                int hour = (int) Math.round(newVal.doubleValue());
                drawCurrentFrame(hour);
            }
        });

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            speedLabel.setText(String.format("Speed: %.2fx", newVal.doubleValue()));
            if (timeline != null) {
                timeline.setRate(newVal.doubleValue());
            }
        });
    }

    private void setupTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.millis(300), event -> {
            int nextHour = Math.min(replayData.maxHour, (int) Math.round(hourSlider.getValue()) + 1);
            hourSlider.setValue(nextHour);
            drawCurrentFrame(nextHour);
            if (nextHour >= replayData.maxHour) {
                timeline.stop();
                playPauseButton.setText("Play");
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setRate(speedSlider.getValue());
    }

    private void togglePlayback() {
        if (timeline.getStatus() == Animation.Status.RUNNING) {
            timeline.stop();
            playPauseButton.setText("Play");
            return;
        }

        if (hourSlider.getValue() >= replayData.maxHour) {
            hourSlider.setValue(0);
            drawCurrentFrame(0);
        }

        timeline.play();
        playPauseButton.setText("Pause");
    }

    private void drawCurrentFrame(int hour) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(20, 24, 35));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        FrameState frame = replayData.frames.get(hour);
        if (frame == null) {
            hourLabel.setText("Hour: " + hour + " (no data)");
            return;
        }

        double w = canvas.getWidth();
        double h = canvas.getHeight();
        double leftPad = 60;
        double topPad = 40;
        double mapW = w - (leftPad * 2);
        double mapH = h - (topPad * 2);

        gc.setStroke(Color.rgb(90, 105, 135));
        gc.setLineWidth(2.0);
        gc.strokeRect(leftPad, topPad, mapW, mapH);

        for (WarehouseState warehouse : frame.warehouses.values()) {
            double x = leftPad + (warehouse.posX / replayData.mapX) * mapW;
            double y = topPad + (warehouse.posY / replayData.mapY) * mapH;
            gc.setFill(Color.web("#5ca9ff"));
            gc.fillOval(x - 8, y - 8, 16, 16);
            gc.setFill(Color.WHITE);
            gc.fillText("W" + warehouse.id, x + 10, y - 6);
            gc.setFill(Color.LIGHTGRAY);
            gc.fillText("Inv:" + warehouse.inventorySize, x + 10, y + 10);
        }

        for (TruckState truck : frame.trucks.values()) {
            double x = leftPad + (truck.posX / replayData.mapX) * mapW;
            double y = topPad + (truck.posY / replayData.mapY) * mapH;
            gc.setFill(statusColor(truck.status));
            gc.fillRect(x - 6, y - 6, 12, 12);
            gc.setFill(Color.WHITE);
            gc.fillText("T" + truck.id, x + 8, y - 8);
        }

        int delivered = 0;
        for (ShipmentState shipment : frame.shipments.values()) {
            if ("Delivered".equalsIgnoreCase(shipment.status)) {
                delivered++;
            }
        }

        gc.setFill(Color.WHITE);
        gc.fillText("Warehouses: " + frame.warehouses.size(), 20, 20);
        gc.fillText("Trucks: " + frame.trucks.size(), 160, 20);
        gc.fillText("Shipments delivered: " + delivered + "/" + frame.shipments.size(), 260, 20);

        hourLabel.setText("Hour: " + hour);
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
                    "Loaded replay files successfully. Use play or drag the timeline slider.");
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

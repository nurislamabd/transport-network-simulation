package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import simulation.Main;
import simulation.Simulation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lightweight HTTP server that serves the web replay UI and runs backend simulations.
 */
public class SimulationWebServer
{
    private static final int DEFAULT_PORT = 8080;
    private static final Path RUNS_DIRECTORY = Path.of("web-simulation-runs");
    private static final int MAX_WEB_TRUCKS = 100;
    private static final int MAX_WEB_WAREHOUSES = 100;
    private static final int MAX_WEB_SHIPMENTS = 500;
    private static final int MAX_WEB_MAP_SIZE = 1000;

    /**
     * Starts the web server.
     *
     * @param args optional first arg or PORT environment variable can set the port
     * @throws IOException if the server cannot start
     */
    public static void main(String[] args) throws IOException
    {
        int port = resolvePort(args);
        Files.createDirectories(RUNS_DIRECTORY);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/simulations", SimulationWebServer::handleSimulationRequest);
        server.createContext("/runs", SimulationWebServer::handleRunFileRequest);
        server.createContext("/", SimulationWebServer::handleStaticRequest);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Transport Simulation website running at http://localhost:" + port + "/");
    }

    private static int resolvePort(String[] args)
    {
        if (args.length > 0) return Integer.parseInt(args[0]);
        String port = System.getenv("PORT");
        if (port != null && !port.isBlank()) return Integer.parseInt(port);
        return DEFAULT_PORT;
    }

    private static void handleSimulationRequest(HttpExchange exchange) throws IOException
    {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed", "text/plain");
            return;
        }

        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            SimulationSettings settings = SimulationSettings.fromJson(requestBody);
            validateSettings(settings);

            String runId = Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8);
            Path runDirectory = RUNS_DIRECTORY.resolve(runId);
            Files.createDirectories(runDirectory);
            File configFile = runDirectory.resolve("config.txt").toFile();

            if (settings.random) {
                Main.randomConfiguration(configFile);
            } else {
                Main.configure(configFile, settings.mapX, settings.mapY, settings.trucks, settings.warehouses, settings.shipments);
            }
            settings = settings.withConfigValues(configFile.toPath());

            Simulation simulation = new Simulation(configFile, runDirectory.toFile());
            simulation.simulate();

            SimulationPayload payload = SimulationPayload.fromRun(runId, settings, simulation, runDirectory);
            sendText(exchange, 200, payload.toJson(), "application/json");
        } catch (IllegalArgumentException e) {
            sendText(exchange, 400, "{\"error\":\"" + jsonEscape(e.getMessage()) + "\"}", "application/json");
        } catch (Exception e) {
            e.printStackTrace();
            sendText(exchange, 500, "{\"error\":\"Simulation failed: " + jsonEscape(e.getMessage()) + "\"}", "application/json");
        }
    }

    private static void validateSettings(SimulationSettings settings)
    {
        if (settings.random) return;
        if (settings.mapX < 1 || settings.mapX > MAX_WEB_MAP_SIZE) throw new IllegalArgumentException("Map X must be between 1 and " + MAX_WEB_MAP_SIZE + ".");
        if (settings.mapY < 1 || settings.mapY > MAX_WEB_MAP_SIZE) throw new IllegalArgumentException("Map Y must be between 1 and " + MAX_WEB_MAP_SIZE + ".");
        if (settings.trucks < 1 || settings.trucks > MAX_WEB_TRUCKS) throw new IllegalArgumentException("Trucks must be between 1 and " + MAX_WEB_TRUCKS + " for web runs.");
        if (settings.warehouses < 2 || settings.warehouses > MAX_WEB_WAREHOUSES) throw new IllegalArgumentException("Warehouses must be between 2 and " + MAX_WEB_WAREHOUSES + " for web runs.");
        if (settings.shipments < 1 || settings.shipments > MAX_WEB_SHIPMENTS) throw new IllegalArgumentException("Shipments must be between 1 and " + MAX_WEB_SHIPMENTS + " for web runs.");
    }

    private static void handleRunFileRequest(HttpExchange exchange) throws IOException
    {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed", "text/plain");
            return;
        }

        URI uri = exchange.getRequestURI();
        String relative = uri.getPath().replaceFirst("^/runs/", "");
        Path target = RUNS_DIRECTORY.resolve(relative).normalize();
        if (!target.startsWith(RUNS_DIRECTORY) || !Files.isRegularFile(target)) {
            sendText(exchange, 404, "Not Found", "text/plain");
            return;
        }

        String contentType = URLConnection.guessContentTypeFromName(target.getFileName().toString());
        if (contentType == null) contentType = "text/plain";
        byte[] bytes = Files.readAllBytes(target);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static void handleStaticRequest(HttpExchange exchange) throws IOException
    {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed", "text/plain");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        if (path.contains("..")) {
            sendText(exchange, 400, "Bad Request", "text/plain");
            return;
        }

        String resourcePath = "/web" + path;
        try (InputStream input = SimulationWebServer.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                sendText(exchange, 404, "Not Found", "text/plain");
                return;
            }
            byte[] bytes = input.readAllBytes();
            String contentType = URLConnection.guessContentTypeFromName(path);
            if (contentType == null) contentType = "application/octet-stream";
            exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        }
    }

    private static void sendText(HttpExchange exchange, int status, String body, String contentType) throws IOException
    {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static final class SimulationSettings
    {
        private final boolean random;
        private final int mapX;
        private final int mapY;
        private final int trucks;
        private final int warehouses;
        private final int shipments;

        private SimulationSettings(boolean random, int mapX, int mapY, int trucks, int warehouses, int shipments)
        {
            this.random = random;
            this.mapX = mapX;
            this.mapY = mapY;
            this.trucks = trucks;
            this.warehouses = warehouses;
            this.shipments = shipments;
        }

        private static SimulationSettings fromJson(String json)
        {
            boolean random = booleanValue(json, "random", false);
            return new SimulationSettings(
                random,
                intValue(json, "mapX", 500),
                intValue(json, "mapY", 500),
                intValue(json, "trucks", 3),
                intValue(json, "warehouses", 4),
                intValue(json, "shipments", 8)
            );
        }

        private SimulationSettings withConfigValues(Path configPath) throws IOException
        {
            List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
            String[] dimensions = lines.get(0).split(",", -1);
            String[] counts = lines.get(1).split(",", -1);
            return new SimulationSettings(
                random,
                (int) Double.parseDouble(dimensions[0]),
                (int) Double.parseDouble(dimensions[1]),
                Integer.parseInt(counts[2]),
                Integer.parseInt(counts[0]),
                Integer.parseInt(counts[1])
            );
        }
    }

    private static final class SimulationPayload
    {
        private final String runId;
        private final int mapX;
        private final int mapY;
        private final int trucks;
        private final int warehouses;
        private final int shipments;
        private final int completedHours;
        private final double truckMs;
        private final double warehouseMs;
        private final double shipmentMs;
        private final List<Map<String, String>> warehouseRows;
        private final List<Map<String, String>> truckRows;
        private final List<Map<String, String>> shipmentRows;

        private SimulationPayload(
            String runId,
            SimulationSettings settings,
            Simulation simulation,
            List<Map<String, String>> warehouseRows,
            List<Map<String, String>> truckRows,
            List<Map<String, String>> shipmentRows
        ) {
            this.runId = runId;
            this.mapX = settings.mapX;
            this.mapY = settings.mapY;
            this.trucks = simulation.trucks;
            this.warehouses = simulation.warehouses;
            this.shipments = simulation.shipments;
            this.completedHours = simulation.getCompletedHours();
            this.truckMs = simulation.getTrucksMs();
            this.warehouseMs = simulation.getWarehousesMs();
            this.shipmentMs = simulation.getShipmentsMs();
            this.warehouseRows = warehouseRows;
            this.truckRows = truckRows;
            this.shipmentRows = shipmentRows;
        }

        private static SimulationPayload fromRun(String runId, SimulationSettings settings, Simulation simulation, Path runDirectory) throws IOException
        {
            return new SimulationPayload(
                runId,
                settings,
                simulation,
                readCsv(runDirectory.resolve("WarehousesCSV.txt")),
                readCsv(runDirectory.resolve("TrucksCSV.txt")),
                readCsv(runDirectory.resolve("ShipmentsCSV.txt"))
            );
        }

        private String toJson()
        {
            return "{"
                + "\"runId\":\"" + jsonEscape(runId) + "\","
                + "\"mapX\":" + mapX + ","
                + "\"mapY\":" + mapY + ","
                + "\"trucks\":" + trucks + ","
                + "\"warehouses\":" + warehouses + ","
                + "\"shipments\":" + shipments + ","
                + "\"completedHours\":" + completedHours + ","
                + "\"runtimeMs\":{\"trucks\":" + truckMs + ",\"warehouses\":" + warehouseMs + ",\"shipments\":" + shipmentMs + "},"
                + "\"csv\":{"
                + "\"trucks\":\"/runs/" + jsonEscape(runId) + "/TrucksCSV.txt\","
                + "\"warehouses\":\"/runs/" + jsonEscape(runId) + "/WarehousesCSV.txt\","
                + "\"shipments\":\"/runs/" + jsonEscape(runId) + "/ShipmentsCSV.txt\"},"
                + "\"warehousesData\":" + rowsToJson(warehouseRows) + ","
                + "\"trucksData\":" + rowsToJson(truckRows) + ","
                + "\"shipmentsData\":" + rowsToJson(shipmentRows)
                + "}";
        }
    }

    private static List<Map<String, String>> readCsv(Path path) throws IOException
    {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Map<String, String>> rows = new ArrayList<>();
        if (lines.isEmpty()) return rows;
        String[] headers = lines.get(0).split(",", -1);
        for (int i = 1; i < lines.size(); i++) {
            String[] values = lines.get(i).split(",", -1);
            Map<String, String> row = new HashMap<>();
            for (int j = 0; j < headers.length && j < values.length; j++) {
                row.put(headers[j], values[j]);
            }
            rows.add(row);
        }
        return rows;
    }

    private static String rowsToJson(List<Map<String, String>> rows)
    {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) builder.append(',');
            builder.append('{');
            int column = 0;
            for (Map.Entry<String, String> entry : rows.get(i).entrySet()) {
                if (column++ > 0) builder.append(',');
                builder.append('\"').append(jsonEscape(entry.getKey())).append("\":");
                builder.append(jsonScalar(entry.getValue()));
            }
            builder.append('}');
        }
        builder.append(']');
        return builder.toString();
    }

    private static String jsonScalar(String value)
    {
        if (value == null || value.equals("null")) return "null";
        if (value.matches("-?\\d+(\\.\\d+)?")) return value;
        return "\"" + jsonEscape(value) + "\"";
    }

    private static int intValue(String json, String key, int fallback)
    {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(-?\\d+)").matcher(json);
        if (matcher.find()) return Integer.parseInt(matcher.group(1));
        return fallback;
    }

    private static boolean booleanValue(String json, String key, boolean fallback)
    {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(true|false)").matcher(json);
        if (matcher.find()) return Boolean.parseBoolean(matcher.group(1));
        return fallback;
    }

    private static String jsonEscape(String value)
    {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}

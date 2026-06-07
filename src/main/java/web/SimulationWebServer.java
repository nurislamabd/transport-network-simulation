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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
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

    // Truck/Warehouse/Shipment log to static BufferedWriters in Simulation, so runs must not
    // overlap. A single-thread executor both serializes runs and keeps them off the HTTP
    // threads, so the POST returns immediately and the client polls for the result.
    private static final ExecutorService SIMULATION_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Map<String, Job> JOBS = new ConcurrentHashMap<>();

    // Compiled once; jsonScalar runs this against every CSV cell, so String.matches (which
    // recompiles the pattern per call) was a major serialization cost.
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

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
        String method = exchange.getRequestMethod();
        if ("POST".equalsIgnoreCase(method)) {
            handleSimulationStart(exchange);
        } else if ("GET".equalsIgnoreCase(method)) {
            handleSimulationStatus(exchange);
        } else {
            sendText(exchange, 405, "Method Not Allowed", "text/plain");
        }
    }

    /**
     * Accepts simulation settings, queues the run on the background executor, and immediately
     * returns the run id so the client can poll for completion.
     */
    private static void handleSimulationStart(HttpExchange exchange) throws IOException
    {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            SimulationSettings parsed = SimulationSettings.fromJson(requestBody);
            validateSettings(parsed);

            String runId = Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8);
            Path runDirectory = RUNS_DIRECTORY.resolve(runId);
            Files.createDirectories(runDirectory);
            File configFile = runDirectory.resolve("config.txt").toFile();

            if (parsed.random) {
                Main.randomConfiguration(configFile);
            } else {
                Main.configure(configFile, parsed.mapX, parsed.mapY, parsed.trucks, parsed.warehouses, parsed.shipments);
            }
            SimulationSettings settings = parsed.withConfigValues(configFile.toPath());

            Job job = new Job();
            JOBS.put(runId, job);
            SIMULATION_EXECUTOR.submit(() -> runSimulationJob(runId, job, settings, configFile, runDirectory));

            sendText(exchange, 202, "{\"runId\":\"" + jsonEscape(runId) + "\",\"status\":\"running\"}", "application/json");
        } catch (IllegalArgumentException e) {
            sendText(exchange, 400, "{\"error\":\"" + jsonEscape(e.getMessage()) + "\"}", "application/json");
        } catch (Exception e) {
            e.printStackTrace();
            sendText(exchange, 500, "{\"error\":\"Could not start simulation: " + jsonEscape(e.getMessage()) + "\"}", "application/json");
        }
    }

    private static void runSimulationJob(String runId, Job job, SimulationSettings settings, File configFile, Path runDirectory)
    {
        try {
            Simulation simulation = new Simulation(configFile, runDirectory.toFile());
            simulation.simulate();
            job.result = SimulationPayload.fromRun(runId, settings, simulation, runDirectory).toJson();
            job.status = "done";
        } catch (Exception e) {
            e.printStackTrace();
            job.error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            job.status = "error";
        }
    }

    /**
     * Polled by the client at /api/simulations/{runId}. Returns the running/error status, or the
     * full payload once the background run finishes.
     */
    private static void handleSimulationStatus(HttpExchange exchange) throws IOException
    {
        String runId = exchange.getRequestURI().getPath().replaceFirst("^/api/simulations/?", "");
        Job job = runId.isEmpty() ? null : JOBS.get(runId);
        if (job == null) {
            sendText(exchange, 404, "{\"error\":\"Unknown run id.\"}", "application/json");
            return;
        }
        if ("done".equals(job.status)) {
            sendText(exchange, 200, job.result, "application/json");
        } else if ("error".equals(job.status)) {
            sendText(exchange, 500, "{\"status\":\"error\",\"error\":\"Simulation failed: " + jsonEscape(job.error) + "\"}", "application/json");
        } else {
            sendText(exchange, 200, "{\"status\":\"running\"}", "application/json");
        }
    }

    /** Mutable holder for the state of one background simulation run. */
    private static final class Job
    {
        private volatile String status = "running";
        private volatile String result;
        private volatile String error;
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
        // Pre-built compact JSON fragments. The replay UI only needs warehouse positions,
        // per-hour truck positions/state, and a per-hour delivered count, so we collapse the
        // full per-hour CSV history (entities x hours rows) down to just those instead of
        // inlining every column of every row.
        private final String warehousesJson;
        private final String trucksByHourJson;
        private final String deliveredByHourJson;

        private SimulationPayload(
            String runId,
            SimulationSettings settings,
            Simulation simulation,
            String warehousesJson,
            String trucksByHourJson,
            String deliveredByHourJson
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
            this.warehousesJson = warehousesJson;
            this.trucksByHourJson = trucksByHourJson;
            this.deliveredByHourJson = deliveredByHourJson;
        }

        private static SimulationPayload fromRun(String runId, SimulationSettings settings, Simulation simulation, Path runDirectory) throws IOException
        {
            return new SimulationPayload(
                runId,
                settings,
                simulation,
                warehousesJson(runDirectory.resolve("WarehousesCSV.txt")),
                trucksByHourJson(runDirectory.resolve("TrucksCSV.txt")),
                deliveredByHourJson(runDirectory.resolve("ShipmentsCSV.txt"))
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
                + "\"warehouseList\":" + warehousesJson + ","
                + "\"trucksByHour\":" + trucksByHourJson + ","
                + "\"deliveredByHour\":" + deliveredByHourJson
                + "}";
        }
    }

    /**
     * Warehouse positions are constant across the run, so we keep a single row per id.
     * CSV columns: Hour,WarehouseID,PosX,PosY,...
     */
    private static String warehousesJson(Path path) throws IOException
    {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        TreeMap<Integer, String> byId = new TreeMap<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] v = lines.get(i).split(",", -1);
            if (v.length < 4) continue;
            int id = Integer.parseInt(v[1]);
            byId.putIfAbsent(id, "{\"id\":" + id + ",\"x\":" + numOrZero(v[2]) + ",\"y\":" + numOrZero(v[3]) + "}");
        }
        return "[" + String.join(",", byId.values()) + "]";
    }

    /**
     * Groups trucks by simulation hour, keeping only the fields the canvas draws.
     * CSV columns: Hour,TruckID,PosX,PosY,LoadSize,Speed,Status,...
     */
    private static String trucksByHourJson(Path path) throws IOException
    {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        java.util.LinkedHashMap<String, StringBuilder> byHour = new java.util.LinkedHashMap<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] v = lines.get(i).split(",", -1);
            if (v.length < 7) continue;
            StringBuilder hour = byHour.computeIfAbsent(v[0], k -> new StringBuilder());
            if (hour.length() > 0) hour.append(',');
            hour.append("{\"id\":").append(v[1])
                .append(",\"x\":").append(numOrZero(v[2]))
                .append(",\"y\":").append(numOrZero(v[3]))
                .append(",\"done\":").append("Done".equals(v[6]))
                .append('}');
        }
        StringBuilder out = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, StringBuilder> e : byHour.entrySet()) {
            if (!first) out.append(',');
            first = false;
            out.append('"').append(jsonEscape(e.getKey())).append("\":[").append(e.getValue()).append(']');
        }
        return out.append('}').toString();
    }

    /**
     * Reduces the shipment history to a delivered count per hour.
     * CSV columns: Hour,ShipmentID,Size,SourceID,DestinationID,CurrentWarehouse,Status
     */
    private static String deliveredByHourJson(Path path) throws IOException
    {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        java.util.LinkedHashMap<String, int[]> counts = new java.util.LinkedHashMap<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] v = lines.get(i).split(",", -1);
            if (v.length < 7) continue;
            int[] count = counts.computeIfAbsent(v[0], k -> new int[1]);
            if ("Delivered".equals(v[6])) count[0]++;
        }
        StringBuilder out = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, int[]> e : counts.entrySet()) {
            if (!first) out.append(',');
            first = false;
            out.append('"').append(jsonEscape(e.getKey())).append("\":").append(e.getValue()[0]);
        }
        return out.append('}').toString();
    }

    private static String numOrZero(String value)
    {
        return (value != null && NUMBER_PATTERN.matcher(value).matches()) ? value : "0";
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

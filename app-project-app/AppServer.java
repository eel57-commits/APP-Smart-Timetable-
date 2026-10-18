import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * AppServer - Plain Java Backend for the Smart Timetable Maker (7 Periods).
 * 
 * Runs a standalone HTTP server using Java's built-in HttpServer.
 * Fully compatible with BlueJ and any standard JDK (8+).
 */
public class AppServer {

    public static final int PORT = 8080;
    private static final long START_TIME = System.currentTimeMillis();
    private static final TimetableManager MANAGER = new TimetableManager();

    public static HttpServer startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoints for Timetable
        server.createContext("/api/status", new StatusHandler());
        server.createContext("/api/timetable", new TimetableHandler());
        server.createContext("/api/timetable/generate", new GenerateHandler());
        server.createContext("/api/timetable/clear", new ClearHandler());
        server.createContext("/api/timetable/slot", new SlotHandler());
        server.createContext("/api/subjects", new SubjectsHandler());

        // Static Web Content Handler
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null); // Default executor
        server.start();
        return server;
    }

    public static void main(String[] args) {
        try {
            startServer(PORT);

            System.out.println("====================================================");
            System.out.println(" 📅 Smart Timetable Maker Backend (7 Periods) Started!");
            System.out.println("====================================================");
            System.out.println(" Open in browser: http://localhost:" + PORT);
            System.out.println(" Java Version:    " + System.getProperty("java.version"));
            System.out.println(" Subjects:        " + MANAGER.getSubjects().size() + " loaded");
            System.out.println(" Scheduled Slots: " + MANAGER.getSlots().size() + " loaded");
            System.out.println(" Press Ctrl+C in terminal or terminate in BlueJ to stop.");
            System.out.println("====================================================");

        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handler for /api/timetable: returns slots and conflict analysis.
     */
    static class TimetableHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<String> conflicts = MANAGER.detectConflicts();
                StringBuilder conflictArray = new StringBuilder("[");
                for (int i = 0; i < conflicts.size(); i++) {
                    conflictArray.append("\"").append(escapeJson(conflicts.get(i))).append("\"");
                    if (i < conflicts.size() - 1) conflictArray.append(",");
                }
                conflictArray.append("]");

                String json = String.format(
                    "{\"slots\":%s,\"conflicts\":%s,\"hasConflicts\":%b,\"totalSlots\":%d}",
                    MANAGER.getSlotsJson(),
                    conflictArray.toString(),
                    !conflicts.isEmpty(),
                    MANAGER.getSlots().size()
                );
                sendResponse(exchange, 200, "application/json", json);
            } else {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
            }
        }
    }

    /**
     * Handler for /api/timetable/generate: triggers constraint-based smart scheduling.
     */
    static class GenerateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    MANAGER.generateSmartTimetable();
                    int total = MANAGER.getSlots().size();
                    String response = String.format(
                        "{\"success\":true,\"message\":\"Smart timetable auto-generated successfully!\",\"totalSlots\":%d}",
                        total
                    );
                    sendResponse(exchange, 200, "application/json", response);
                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "application/json", "{\"error\":\"Generation failed: " + escapeJson(e.getMessage()) + "\"}");
                }
            } else {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
            }
        }
    }

    /**
     * Handler for /api/timetable/clear: clears all slots.
     */
    static class ClearHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                MANAGER.clearSlots();
                sendResponse(exchange, 200, "application/json", "{\"success\":true,\"message\":\"All slots cleared.\"}");
            } else {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
            }
        }
    }

    /**
     * Handler for /api/timetable/slot: add or delete a specific slot.
     */
    static class SlotHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            String method = exchange.getRequestMethod();
            if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                String day = extractField(body, "day");
                String startTime = extractField(body, "startTime");
                String endTime = extractField(body, "endTime");
                String subjectName = extractField(body, "subjectName");
                String subjectCode = extractField(body, "subjectCode");
                String teacher = extractField(body, "teacher");
                String room = extractField(body, "room");
                String color = extractField(body, "color");
                if (color.isEmpty()) color = "#3b82f6";

                String id = "slot-" + System.currentTimeMillis();
                TimetableSlot slot = new TimetableSlot(id, day, startTime, endTime, subjectName, subjectCode, teacher, room, color);
                MANAGER.addSlot(slot);

                sendResponse(exchange, 200, "application/json", "{\"success\":true,\"slot\":" + slot.toJson() + "}");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                String id = getQueryParam(query, "id");
                if (id != null && MANAGER.deleteSlot(id)) {
                    sendResponse(exchange, 200, "application/json", "{\"success\":true}");
                } else {
                    sendResponse(exchange, 404, "application/json", "{\"error\":\"Slot not found\"}");
                }
            } else {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
            }
        }
    }

    /**
     * Handler for /api/subjects: list, add, or delete subjects.
     */
    static class SubjectsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                sendResponse(exchange, 200, "application/json", MANAGER.getSubjectsJson());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readBody(exchange);
                String name = extractField(body, "name");
                String code = extractField(body, "code");
                String teacher = extractField(body, "teacher");
                String room = extractField(body, "room");
                String hoursStr = extractField(body, "weeklyHours");
                String color = extractField(body, "color");
                if (color.isEmpty()) color = "#3b82f6";
                int weeklyHours = 3;
                try { weeklyHours = Integer.parseInt(hoursStr); } catch (Exception ignored) {}

                String id = "sub-" + System.currentTimeMillis();
                Subject subject = new Subject(id, name, code, teacher, room, weeklyHours, color);
                MANAGER.addSubject(subject);

                sendResponse(exchange, 200, "application/json", "{\"success\":true,\"subject\":" + subject.toJson() + "}");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                String query = exchange.getRequestURI().getQuery();
                String id = getQueryParam(query, "id");
                if (id != null && MANAGER.deleteSubject(id)) {
                    sendResponse(exchange, 200, "application/json", "{\"success\":true}");
                } else {
                    sendResponse(exchange, 404, "application/json", "{\"error\":\"Subject not found\"}");
                }
            } else {
                sendResponse(exchange, 405, "application/json", "{\"error\":\"Method Not Allowed\"}");
            }
        }
    }

    /**
     * General status endpoint.
     */
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            long uptimeSeconds = (System.currentTimeMillis() - START_TIME) / 1000;
            String json = String.format(
                "{\"status\":\"ONLINE\",\"appName\":\"Smart Timetable Maker\",\"uptimeSeconds\":%d,\"slots\":%d,\"subjects\":%d}",
                uptimeSeconds,
                MANAGER.getSlots().size(),
                MANAGER.getSubjects().size()
            );
            sendResponse(exchange, 200, "application/json", json);
        }
    }

    /**
     * Static file handler serving web/ directory.
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            if (path.contains("..")) {
                sendResponse(exchange, 400, "text/plain", "Bad Request");
                return;
            }

            Path filePath = Paths.get("web", path);
            if (!Files.exists(filePath)) {
                filePath = Paths.get(".", path);
            }

            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                sendResponse(exchange, 404, "text/plain", "404 Not Found: " + path);
                return;
            }

            String contentType = getContentType(path);
            byte[] bytes = Files.readAllBytes(filePath);

            exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String getContentType(String path) {
            if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".json")) return "application/json";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".svg")) return "image/svg+xml";
            return "text/plain";
        }
    }

    // --- Helper Methods ---

    private static boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String extractField(String json, String field) {
        String pattern = "\"" + field + "\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return "";

        int colon = json.indexOf(":", idx + pattern.length());
        if (colon == -1) return "";

        int valStart = colon + 1;
        while (valStart < json.length() && Character.isWhitespace(json.charAt(valStart))) {
            valStart++;
        }
        if (valStart >= json.length()) return "";

        if (json.charAt(valStart) == '"') {
            valStart++;
            int valEnd = json.indexOf('"', valStart);
            if (valEnd != -1) {
                return json.substring(valStart, valEnd);
            }
        } else {
            int valEnd = valStart;
            while (valEnd < json.length() && json.charAt(valEnd) != ',' && json.charAt(valEnd) != '}') {
                valEnd++;
            }
            return json.substring(valStart, valEnd).trim();
        }
        return "";
    }

    private static String getQueryParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length > 0 && kv[0].equalsIgnoreCase(param)) {
                return kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            }
        }
        return null;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}

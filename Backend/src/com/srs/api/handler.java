package com.srs.api;

import com.sun.net.httpserver.*;
import com.srs.db.DBconnections;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.stream.Collectors;

public class handler implements HttpHandler {
    private DBconnections db;

    public handler(DBconnections db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if (path.equals("/api/get-template") && "GET".equalsIgnoreCase(method)) {
            try {
                String template = Files.readString(Paths.get("src/resources/srs_template.json"));
                sendResponse(exchange, template, 200);
            } catch (Exception e) {
                sendResponse(exchange, "{\"error\":\"Template missing\"}", 500);
            }
        } 
        else if (path.equals("/api/signup") && "POST".equalsIgnoreCase(method)) {
            handleSignup(exchange);
        }
        else if (path.equals("/api/login") && "POST".equalsIgnoreCase(method)) {
            handleLogin(exchange);
        }
        else if (path.equals("/api/save-srs") && "POST".equalsIgnoreCase(method)) {
            String body = readRequestBody(exchange);
            boolean saved = saveToDb(body);
            sendResponse(exchange, saved ? "{\"status\":\"success\"}" : "{\"status\":\"error\"}", saved ? 200 : 500);
        } 
        else {
            sendResponse(exchange, "{\"error\":\"Not Found\"}", 404);
        }
    }

    private void handleSignup(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        String email = getJsonValue(body, "email");
        String password = getJsonValue(body, "password");
        String name = getJsonValue(body, "name"); 

        try (Connection conn = db.getConnection()) {
            String sql = Files.readString(Paths.get("src/sql/signup_user.sql"));
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, password);
                pstmt.executeUpdate();
                sendResponse(exchange, "{\"message\":\"Signup successful! Please login.\"}", 200);
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                sendResponse(exchange, "{\"error\":\"User already exists\"}", 400);
            } else {
                sendResponse(exchange, "{\"error\":\"Database error\"}", 500);
            }
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        String email = getJsonValue(body, "email");
        String password = getJsonValue(body, "password");

        try (Connection conn = db.getConnection()) {
            String sql = Files.readString(Paths.get("src/sql/login_user.sql"));
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String userJson = "{\"message\":\"Welcome back!\", \"user\":{\"email\":\"" + rs.getString("username") + "\"}}";
                    sendResponse(exchange, userJson, 200);
                } else {
                    sendResponse(exchange, "{\"error\":\"Invalid credentials\", \"code\":\"USER_NOT_FOUND\"}", 401);
                }
            }
        } catch (SQLException e) {
            sendResponse(exchange, "{\"error\":\"Database error\"}", 500);
        }
    }

    private String getJsonValue(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return "";
            int valueStart = json.indexOf(":", keyIndex) + 2;
            int valueEnd = json.indexOf("\"", valueStart);
            return json.substring(valueStart, valueEnd);
        } catch (Exception e) { return ""; }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(isr)) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private boolean saveToDb(String data) {
    try {
        String sql = Files.readString(Paths.get("src/sql/insert_srs.sql"));
        try (Connection conn = db.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, 1);   
                pstmt.setString(2, data);
                pstmt.setString(3, "1.0");
                
                pstmt.executeUpdate();
                return true;
        }
    } catch (Exception e) {
        System.err.println("❌ DB Error: " + e.getMessage());
        e.printStackTrace(); 
        return false;
    }
}
}
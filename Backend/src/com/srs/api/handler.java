package com.srs.api;

import com.sun.net.httpserver.*;
import com.srs.db.DBconnections;
import com.lowagie.text.DocumentException; 
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
                sendResponse(exchange, template, 200, "application/json");
            } catch (Exception e) {
                sendResponse(exchange, "{\"error\":\"Template missing\"}", 500, "application/json");
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
            sendResponse(exchange, saved ? "{\"status\":\"success\"}" : "{\"status\":\"error\"}", saved ? 200 : 500, "application/json");
        } 
        else if (path.equals("/api/download-pdf") && "POST".equalsIgnoreCase(method)) {
            handleDownloadPdf(exchange);
        }
        else {
            sendResponse(exchange, "{\"error\":\"Not Found\"}", 404, "application/json");
        }
    }

    private void handleDownloadPdf(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        String projectName = getJsonValue(body, "Project");
        String author = getJsonValue(body, "author");
        
        if (projectName.isEmpty()) projectName = "Unnamed Project";
        if (author.isEmpty()) author = "Anonymous";

        try {
            byte[] pdfBytes = DocumentGenerator.generateIEEEReport(projectName, author, body);
            
            exchange.getResponseHeaders().set("Content-Type", "application/pdf");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=SRS_Report.pdf");
            exchange.sendResponseHeaders(200, pdfBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(pdfBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, "{\"error\":\"PDF Generation Failed: " + e.getMessage() + "\"}", 500, "application/json");
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
                sendResponse(exchange, "{\"message\":\"Signup successful! Please login.\"}", 200, "application/json");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                sendResponse(exchange, "{\"error\":\"User already exists\"}", 400, "application/json");
            } else {
                sendResponse(exchange, "{\"error\":\"Database error\"}", 500, "application/json");
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
                    sendResponse(exchange, userJson, 200, "application/json");
                } else {
                    sendResponse(exchange, "{\"error\":\"Invalid credentials\", \"code\":\"USER_NOT_FOUND\"}", 401, "application/json");
                }
            }
        } catch (SQLException e) {
            sendResponse(exchange, "{\"error\":\"Database error\"}", 500, "application/json");
        }
    }

    private String getJsonValue(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return "";
            int valueStart = json.indexOf(":", keyIndex);
            valueStart = json.indexOf("\"", valueStart) + 1;
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

    private void sendResponse(HttpExchange exchange, String response, int code, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
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
            return false;
        }
    }
}
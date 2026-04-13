package com.srs.api;

import com.sun.net.httpserver.*;
import com.srs.db.DBconnections;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.stream.Collectors;
import com.google.gson.*;

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
        } else if (path.equals("/api/get-srs") && "GET".equalsIgnoreCase(method)) {
            handleGetSrs(exchange);
        } else if (path.equals("/api/signup") && "POST".equalsIgnoreCase(method)) {
            handleSignup(exchange);
        } else if (path.equals("/api/login") && "POST".equalsIgnoreCase(method)) {
            handleLogin(exchange);
        } else if (path.equals("/api/save-srs") && "POST".equalsIgnoreCase(method)) {
            String body = readRequestBody(exchange);
            boolean saved = saveToDb(body);
            sendResponse(exchange, saved ? "{\"status\":\"success\"}" : "{\"status\":\"error\"}", saved ? 200 : 500, "application/json");
        } else if (path.equals("/api/download-pdf") && "POST".equalsIgnoreCase(method)) {
            handleDownloadPdf(exchange);
        } else {
            sendResponse(exchange, "{\"error\":\"Not Found\"}", 404, "application/json");
        }
    }

    private void handleGetSrs(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        int projectId = -1;
        
        if (query != null && !query.isEmpty()) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1 && pair[0].trim().equals("project_id")) {
                    try {
                        projectId = Integer.parseInt(pair[1].trim());
                    } catch (NumberFormatException e) {
                        projectId = -1;
                    }
                }
            }
        }

        if (projectId == -1) {
            sendResponse(exchange, "{\"error\":\"project_id required\"}", 400, "application/json");
            return;
        }

        try (Connection conn = db.getConnection()) {
            String sql = "SELECT content_json FROM srs_docs WHERE project_id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, projectId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    String content = rs.getString("content_json");
                    
                    if (content == null || content.trim().isEmpty()) {
                        content = "{}";
                    }
                    
                    sendResponse(exchange, content, 200, "application/json");
                } else {
                    sendResponse(exchange, "{}", 200, "application/json");
                }
            }
        } catch (Exception e) {
            System.err.println("DB Fetch Error for project " + projectId + ": " + e.getMessage());
            sendResponse(exchange, "{\"error\":\"Database fetch error\"}", 500, "application/json");
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
                sendResponse(exchange, "{\"message\":\"Signup successful!\"}", 200, "application/json");
            }
        } catch (Exception e) {
            sendResponse(exchange, "{\"error\":\"Signup failed\"}", 500, "application/json");
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
                    sendResponse(exchange, "{\"message\":\"Login success\", \"user\":{\"email\":\"" + email + "\"}}", 200, "application/json");
                } else {
                    sendResponse(exchange, "{\"error\":\"Invalid credentials\"}", 401, "application/json");
                }
            }
        } catch (Exception e) {
            sendResponse(exchange, "{\"error\":\"Login error\"}", 500, "application/json");
        }
    }

    private void handleDownloadPdf(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        String projectName = getJsonValue(body, "Project");
        String author = getJsonValue(body, "author");
        if (projectName.isEmpty()) projectName = "Project";
        if (author.isEmpty()) author = "Author";

        try {
            byte[] pdfBytes = DocumentGenerator.generateIEEEReport(projectName, author, body);
            exchange.getResponseHeaders().set("Content-Type", "application/pdf");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=SRS.pdf");
            exchange.sendResponseHeaders(200, pdfBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(pdfBytes);
            }
        } catch (Exception e) {
            sendResponse(exchange, "{\"error\":\"PDF error\"}", 500, "application/json");
        }
    }

    private boolean saveToDb(String body) {
        try {
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            int projectId = json.get("project_id").getAsInt();
            JsonObject contentObj = json.getAsJsonObject("content_json");

            String content = contentObj.toString(); 

            String sql = Files.readString(Paths.get("src/sql/insert_srs.sql"));
            try (Connection conn = db.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, projectId);
                pstmt.setString(2, content);
                pstmt.setString(3, "1.0");

                pstmt.executeUpdate();
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getJsonValue(String json, String key) {
        try {
            String search = "\"" + key + "\"";
            int keyIndex = json.indexOf(search);
            if (keyIndex == -1) return "";
            int valueStart = json.indexOf(":", keyIndex) + 1;
            while (Character.isWhitespace(json.charAt(valueStart))) valueStart++;
            if (json.charAt(valueStart) == '\"') {
                valueStart++;
                return json.substring(valueStart, json.indexOf("\"", valueStart));
            } else {
                int valueEnd = valueStart;
                while (valueEnd < json.length() && (Character.isDigit(json.charAt(valueEnd)) || json.charAt(valueEnd) == '.')) valueEnd++;
                return json.substring(valueStart, valueEnd);
            }
        } catch (Exception e) {
            return "";
        }
    }

    private String extractRawJson(String json, String key) {
        try {
            String search = "\"" + key + "\":";
            int start = json.indexOf(search) + search.length();
            while (Character.isWhitespace(json.charAt(start))) start++;
            int openBraces = 0, end = start;
            for (; end < json.length(); end++) {
                char c = json.charAt(end);
                if (c == '{') openBraces++;
                if (c == '}') openBraces--;
                if (openBraces < 0 || (openBraces == 0 && (c == ',' || c == '}'))) break;
            }
            return json.substring(start, end).trim();
        } catch (Exception e) {
            return "{}";
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
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
}
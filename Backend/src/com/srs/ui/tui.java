package com.srs.ui;

import com.srs.db.DBconnections;
import com.srs.api.DocumentGenerator; 
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.Scanner;
import java.util.Map;

public class tui {
    private DBconnections db;
    private Scanner scn;
    private String currentUser = null;
    private int currentUserId = -1;

    public tui(DBconnections db) {
        this.db = db;
        this.scn = new Scanner(System.in);
    }

    public void start() {
        System.out.println("--- 🛡️ Sovereign SRS Engine ---");
        while (currentUser == null) {
            showAuthMenu();
        }
        showDashboard();
    }

    private void showAuthMenu() {
        System.out.println("\n1. Login\n2. Sign Up\n3. Exit");
        System.out.print("Select > ");
        String choice = scn.nextLine();

        if (choice.equals("1")) handleLogin();
        else if (choice.equals("2")) handleSignup();
        else if (choice.equals("3")) System.exit(0);
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String user = scn.nextLine();
        System.out.print("Password: ");
        String pass = scn.nextLine();

        try (Connection conn = db.getConnection()) {
            String sql = "SELECT id, username FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user);
                pstmt.setString(2, pass);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    currentUserId = rs.getInt("id");
                    currentUser = rs.getString("username");
                    System.out.println("✅ Welcome back, " + currentUser);
                } else {
                    System.out.println("❌ Invalid credentials.");
                }
            }
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    private void handleSignup() {
        System.out.print("Full Name: ");
        String name = scn.nextLine();
        System.out.print("Username: ");
        String user = scn.nextLine();
        System.out.print("Password: ");
        String pass = scn.nextLine();

        try (Connection conn = db.getConnection()) {
            String sql = "INSERT INTO users (full_name, username, password) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, user);
                pstmt.setString(3, pass);
                pstmt.executeUpdate();
                System.out.println("✅ Account created! Please login.");
            }
        } catch (Exception e) { System.out.println("❌ Signup failed (Username might be taken)."); }
    }

    private void showDashboard() {
        while (true) {
            System.out.println("\n--- 📊 Dashboard (User: " + currentUser + ") ---");
            System.out.println("1. Create New Project");
            System.out.println("2. View/Edit Existing Projects");
            System.out.println("3. Logout");
            System.out.print("Select > ");
            String choice = scn.nextLine();

            if (choice.equals("1")) createProject();
            else if (choice.equals("2")) listProjects();
            else if (choice.equals("3")) { currentUser = null; return; }
        }
    }

    private void createProject() {
        System.out.print("Project Name: ");
        String name = scn.nextLine();
        try (Connection conn = db.getConnection()) {
            String sql = "INSERT INTO projects (user_id, project_name) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, currentUserId);
                pstmt.setString(2, name);
                pstmt.executeUpdate();
                System.out.println("✅ Project '" + name + "' created.");
            }
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    private void listProjects() {
        try (Connection conn = db.getConnection()) {
            String sql = "SELECT id, project_name FROM projects WHERE user_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentUserId);
                ResultSet rs = pstmt.executeQuery();
                System.out.println("\nYour Projects:");
                while (rs.next()) {
                    System.out.println(rs.getInt("id") + ". " + rs.getString("project_name"));
                }
                System.out.print("Enter Project ID to open (or 0 to go back): ");
                String input = scn.nextLine();
                if (!input.equals("0")) {
                    openProject(Integer.parseInt(input));
                }
            }
        } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
    }

    private void openProject(int pid) {
        System.out.println("\n--- Project Control ---");
        System.out.println("1. Edit SRS (via Text File)");
        System.out.println("2. Export to PDF");
        System.out.println("3. Back");
        System.out.print("Select > ");
        String choice = scn.nextLine();

        if (choice.equals("1")) handleFileEdit(pid);
        else if (choice.equals("2")) handlePdfExport(pid);
    }

    private void handleFileEdit(int pid) {
        File tempFile = new File("edit_srs_" + pid + ".txt");
        try {
            String contentJson = "{}";
            try (Connection conn = db.getConnection()) {
                String sql = "SELECT content_json FROM srs_docs WHERE project_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, pid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) contentJson = rs.getString("content_json");
            }

            PrintWriter writer = new PrintWriter(tempFile);
            writer.println("--- SRS EDITOR (Project ID: " + pid + ") ---");
            writer.println("INSTRUCTIONS: Write your content after the '=' for each field.");
            writer.println("Save and close file, then press Enter in CLI to sync.");
            writer.println("------------------------------------------");
            
            String[] keys = {"Purpose", "Scope", "Features", "Security"};
            JsonObject json = JsonParser.parseString(contentJson).isJsonObject() ? 
                             JsonParser.parseString(contentJson).getAsJsonObject() : new JsonObject();
            
            for (String k : keys) {
                String val = json.has(k) ? json.get(k).getAsString() : "";
                writer.println(k + "=" + val);
            }
            writer.close();

            System.out.println("📝 File created: " + tempFile.getName());
            System.out.println("👉 Please edit the file, SAVE IT, and then press ENTER here to sync.");
            scn.nextLine();

            JsonObject updatedJson = new JsonObject();
            for (String line : Files.readAllLines(tempFile.toPath())) {
                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    updatedJson.addProperty(parts[0].trim(), parts.length > 1 ? parts[1].trim() : "");
                }
            }

            saveToDb(pid, updatedJson.toString());
            System.out.println("✅ Database updated. Deleting temp file...");
            tempFile.delete();

        } catch (Exception e) { System.out.println("File Error: " + e.getMessage()); }
    }

    private void handlePdfExport(int pid) {
        try (Connection conn = db.getConnection()) {
            String sql = "SELECT p.project_name, s.content_json FROM projects p " +
                         "LEFT JOIN srs_docs s ON p.id = s.project_id WHERE p.id = ?";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pid);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("project_name");
                String rawJson = rs.getString("content_json");
                
                String formattedContent = formatJsonToText(rawJson);

                byte[] pdfData = DocumentGenerator.generateIEEEReport(name, currentUser, formattedContent);

                String filename = name.replaceAll("\\s+", "_") + "_SRS.pdf";
                try (FileOutputStream fos = new FileOutputStream(filename)) {
                    fos.write(pdfData);
                }

                System.out.println("🚀 Exported Successfully: " + filename);
            }
        } catch (Exception e) {
            System.out.println("❌ Export failed: " + e.getMessage());
        }
    }

    private String formatJsonToText(String jsonStr) {
        if (jsonStr == null || jsonStr.equals("{}")) return "No data provided.";
        
        StringBuilder sb = new StringBuilder();
        try {
            JsonObject obj = JsonParser.parseString(jsonStr).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                sb.append(entry.getKey().toUpperCase()).append(":\n");
                sb.append(entry.getValue().getAsString()).append("\n\n");
            }
        } catch (Exception e) {
            return jsonStr;
        }
        return sb.toString();
    }

    private void saveToDb(int pid, String json) {
        try (Connection conn = db.getConnection()) {
            String sql = "INSERT INTO srs_docs (project_id, content_json, version) VALUES (?, ?, '1.0') " +
                         "ON CONFLICT(project_id) DO UPDATE SET content_json=excluded.content_json";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, pid);
            ps.setString(2, json);
            ps.executeUpdate();
        } catch (Exception e) { System.out.println("DB Save Error: " + e.getMessage()); }
    }
}
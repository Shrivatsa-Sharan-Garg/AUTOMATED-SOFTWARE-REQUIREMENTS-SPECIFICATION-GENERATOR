package com.srs.db;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;

public class DBconnections {
    private String dbUrl;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ SQLite Driver JAR not found in classpath!");
        }
    }

    public DBconnections(String dbUrl) {
        this.dbUrl = dbUrl;
        try {
            Files.createDirectories(Paths.get("src/db")); 
        } catch (IOException e) {
            System.err.println("Failed to create DB directory: " + e.getMessage());
        }
        runSqlFile("src/sql/schema.sql"); 
    }

    public void runSqlFile(String filePath) {
        try {
            String sql = Files.readString(Paths.get(filePath));
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                
                for (String query : sql.split(";")) {
                    if (!query.trim().isEmpty()) {
                        stmt.execute(query);
                    }
                }
                System.out.println("✅ Successfully executed SQL file: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("❌ SQL Error in " + filePath + ": " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }
}
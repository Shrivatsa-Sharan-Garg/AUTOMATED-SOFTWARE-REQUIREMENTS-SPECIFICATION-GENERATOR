package com.srs;

import com.srs.api.handler; 
import com.srs.db.DBconnections;
import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.InetSocketAddress;

public class Main { 
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.configure().directory("./src").load();
        String dbUrl = dotenv.get("DB_URL");

        DBconnections db = new DBconnections(dbUrl); 
        
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/login", new handler(db));
        server.createContext("/api/signup", new handler(db));
        server.createContext("/api/save-srs", new handler(db));
        server.createContext("/api/get-template", new handler(db)); 
        server.createContext("/api/download-pdf", new handler(db));

        System.out.println("🚀 Sovereign Engine active at http://localhost:8080");
        server.setExecutor(null);
        server.start();
    }
}
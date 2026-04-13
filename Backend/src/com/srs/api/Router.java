package com.srs.api;

import com.sun.net.httpserver.HttpServer;
import com.srs.db.DBconnections;

public class Router {
    public static void setupRoutes(HttpServer server, DBconnections db) {
        handler mainHandler = new handler(db);

        server.createContext("/api/login", mainHandler);
        server.createContext("/api/signup", mainHandler);
        server.createContext("/api/save-srs", mainHandler);
        server.createContext("/api/get-srs", mainHandler); 
        server.createContext("/api/get-template", mainHandler);
        server.createContext("/api/download-pdf", mainHandler);
    }
}
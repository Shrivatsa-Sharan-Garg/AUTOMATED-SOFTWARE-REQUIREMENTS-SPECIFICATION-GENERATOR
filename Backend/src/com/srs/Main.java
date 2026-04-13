package com.srs;

import com.srs.api.Router;
import com.srs.db.DBconnections;
import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.InetSocketAddress;

public class Main { 
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.configure().directory("./src").load();
        String dbUrl = dotenv.get("DB_URL");
        DBconnections db = new DBconnections(dbUrl); 

        boolean isCliMode = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--cli")) {
                isCliMode = true;
                break;
            }
        }

        if (isCliMode) {
            System.out.println("📟 Sovereign Engine: Launching Console Mode...");
            // SovereignTUI tui = new SovereignTUI(db); // Placeholder for your TUI class
            // tui.start();
        } else {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            Router.setupRoutes(server, db);

            System.out.println("🚀 Sovereign Engine active at http://localhost:8080");
            server.setExecutor(null);
            server.start();
        }
    }
}
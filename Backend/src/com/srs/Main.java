package com.srs;

import com.srs.api.Router;
import com.srs.db.DBconnections;
import com.srs.ui.tui;
import com.sun.net.httpserver.HttpServer;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.InetSocketAddress;

public class Main { 
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.configure().directory(".").load();
        String dbUrl = dotenv.get("DB_URL");
        DBconnections db = new DBconnections(dbUrl); 

        boolean startServer = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--server")) {
                startServer = true;
                break;
            }
        }

        if (startServer) {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            Router.setupRoutes(server, db);
            System.out.println("[SERVER] 🚀 Sovereign Engine active at http://localhost:8080");
            server.setExecutor(null);
            server.start();
        } else {
            System.out.println("[CLI] 📟 Sovereign Engine: Console Mode Started.");
            tui console = new tui(db);
            console.start();
        }
    }
}
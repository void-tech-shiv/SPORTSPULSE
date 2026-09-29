package sportspulse.web;

import com.sun.net.httpserver.HttpServer;
import sportspulse.utils.FileManager;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebMain {
    public static void main(String[] args) {
        try {
            // Initialize data folder to avoid issues
            FileManager.initializeDataDirectory();
            
            // Create server on port 8080
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            
            // API Contexts
            server.createContext("/api/players", new PlayerApiHandler());
            server.createContext("/api/teams", new TeamApiHandler());
            server.createContext("/api/matches", new MatchApiHandler());
            server.createContext("/api/events", new EventsApiHandler());
            server.createContext("/api/algorithms/search", new AlgorithmsApiHandler());
            
            // Static files context
            String frontendDir = "frontend";
            server.createContext("/", new StaticFileHandler(frontendDir));
            
            // Use a default executor
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            
            System.out.println("========================================");
            System.out.println("       SPORTSPULSE WEB SERVER");
            System.out.println("========================================");
            System.out.println("Server running:");
            System.out.println("http://localhost:8080");
            System.out.println("========================================");
            System.out.println("Press Ctrl+C to stop the server.");
            
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

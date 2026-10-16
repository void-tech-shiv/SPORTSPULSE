package sportspulse.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sportspulse.dao.PlayerDAO;
import sportspulse.models.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlayerApiHandler implements HttpHandler {
    private final PlayerDAO playerDAO = new PlayerDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        try {
            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange, path);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePost(exchange);
            } else if ("PUT".equalsIgnoreCase(method)) {
                handlePut(exchange, path);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                handleDelete(exchange, path);
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length > 3) {
            String id = parts[3];
            Optional<Player> player = playerDAO.getPlayerById(id);
            if (player.isPresent()) {
                sendResponse(exchange, 200, JsonConverter.playerToJson(player.get()));
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Player not found\"}");
            }
        } else {
            List<Player> players = playerDAO.getAllPlayers();
            sendResponse(exchange, 200, JsonConverter.playersToJson(players));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Map<String, String> data = JsonConverter.parseJson(readBody(exchange));
        String id = data.get("id");

        if (id == null || id.trim().isEmpty()) {
            sendResponse(exchange, 400, "{\"error\":\"Player ID is required\"}");
            return;
        }
        if (playerDAO.getPlayerById(id).isPresent()) {
            sendResponse(exchange, 409, "{\"error\":\"Player with this ID already exists\"}");
            return;
        }

        try {
            Player player = new Player(
                id, data.get("name"), data.get("teamId"), data.get("position"),
                Integer.parseInt(data.getOrDefault("age", "0")),
                Integer.parseInt(data.getOrDefault("matchesPlayed", "0")),
                Integer.parseInt(data.getOrDefault("goals", "0")),
                Integer.parseInt(data.getOrDefault("assists", "0")),
                Integer.parseInt(data.getOrDefault("wickets", "0")),
                Integer.parseInt(data.getOrDefault("runs", "0")),
                Double.parseDouble(data.getOrDefault("fitnessScore", "0.0")),
                Double.parseDouble(data.getOrDefault("performanceScore", "0.0"))
            );
            playerDAO.addPlayer(player);
            sendResponse(exchange, 201, JsonConverter.playerToJson(player));
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid player data\"}");
        }
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            sendResponse(exchange, 400, "{\"error\":\"Missing player ID\"}");
            return;
        }

        String id = parts[3];
        Optional<Player> optionalPlayer = playerDAO.getPlayerById(id);
        if (optionalPlayer.isEmpty()) {
            sendResponse(exchange, 404, "{\"error\":\"Player not found\"}");
            return;
        }

        Map<String, String> data = JsonConverter.parseJson(readBody(exchange));
        Player player = optionalPlayer.get();

        if (data.containsKey("name")) player.setName(data.get("name"));
        if (data.containsKey("teamId")) player.setTeamId(data.get("teamId"));
        if (data.containsKey("position")) player.setPosition(data.get("position"));
        if (data.containsKey("age")) player.setAge(Integer.parseInt(data.get("age")));
        if (data.containsKey("matchesPlayed")) player.setMatchesPlayed(Integer.parseInt(data.get("matchesPlayed")));
        if (data.containsKey("goals")) player.setGoals(Integer.parseInt(data.get("goals")));
        if (data.containsKey("assists")) player.setAssists(Integer.parseInt(data.get("assists")));
        if (data.containsKey("wickets")) player.setWickets(Integer.parseInt(data.get("wickets")));
        if (data.containsKey("runs")) player.setRuns(Integer.parseInt(data.get("runs")));
        if (data.containsKey("fitnessScore")) player.setFitnessScore(Double.parseDouble(data.get("fitnessScore")));
        if (data.containsKey("performanceScore")) player.setPerformanceScore(Double.parseDouble(data.get("performanceScore")));

        playerDAO.updatePlayer(player);
        sendResponse(exchange, 200, JsonConverter.playerToJson(player));
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            sendResponse(exchange, 400, "{\"error\":\"Missing player ID\"}");
            return;
        }

        String id = parts[3];
        if (playerDAO.getPlayerById(id).isEmpty()) {
            sendResponse(exchange, 404, "{\"error\":\"Player not found\"}");
            return;
        }

        playerDAO.deletePlayer(id);
        sendResponse(exchange, 200, "{\"success\":true}");
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes());
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

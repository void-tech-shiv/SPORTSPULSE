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
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length > 3) {
            // /api/players/{id}
            String id = parts[3];
            Optional<Player> p = playerDAO.getPlayerById(id);
            if (p.isPresent()) {
                sendResponse(exchange, 200, JsonConverter.playerToJson(p.get()));
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Player not found\"}");
            }
        } else {
            // /api/players
            List<Player> players = playerDAO.getAllPlayers();
            sendResponse(exchange, 200, JsonConverter.playersToJson(players));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, String> data = JsonConverter.parseJson(body);

        try {
            Player p = new Player(
                data.get("id"), data.get("name"), data.get("teamId"), data.get("position"),
                Integer.parseInt(data.getOrDefault("age", "0")),
                Integer.parseInt(data.getOrDefault("matchesPlayed", "0")),
                Integer.parseInt(data.getOrDefault("goals", "0")),
                Integer.parseInt(data.getOrDefault("assists", "0")),
                Integer.parseInt(data.getOrDefault("wickets", "0")),
                Integer.parseInt(data.getOrDefault("runs", "0")),
                Double.parseDouble(data.getOrDefault("fitnessScore", "0.0")),
                Double.parseDouble(data.getOrDefault("performanceScore", "0.0"))
            );
            playerDAO.addPlayer(p);
            sendResponse(exchange, 201, JsonConverter.playerToJson(p));
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid player data\"}");
        }
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            sendResponse(exchange, 400, "{\"error\": \"Missing player ID\"}");
            return;
        }
        String id = parts[3];
        Optional<Player> optionalPlayer = playerDAO.getPlayerById(id);
        if (optionalPlayer.isEmpty()) {
            sendResponse(exchange, 404, "{\"error\": \"Player not found\"}");
            return;
        }

        String body = readBody(exchange);
        Map<String, String> data = JsonConverter.parseJson(body);
        Player p = optionalPlayer.get();

        if (data.containsKey("name")) p.setName(data.get("name"));
        if (data.containsKey("teamId")) p.setTeamId(data.get("teamId"));
        if (data.containsKey("position")) p.setPosition(data.get("position"));
        if (data.containsKey("age")) p.setAge(Integer.parseInt(data.get("age")));
        if (data.containsKey("matchesPlayed")) p.setMatchesPlayed(Integer.parseInt(data.get("matchesPlayed")));
        if (data.containsKey("goals")) p.setGoals(Integer.parseInt(data.get("goals")));
        if (data.containsKey("assists")) p.setAssists(Integer.parseInt(data.get("assists")));
        if (data.containsKey("wickets")) p.setWickets(Integer.parseInt(data.get("wickets")));
        if (data.containsKey("runs")) p.setRuns(Integer.parseInt(data.get("runs")));
        if (data.containsKey("fitnessScore")) p.setFitnessScore(Double.parseDouble(data.get("fitnessScore")));
        if (data.containsKey("performanceScore")) p.setPerformanceScore(Double.parseDouble(data.get("performanceScore")));

        playerDAO.updatePlayer(p);
        sendResponse(exchange, 200, JsonConverter.playerToJson(p));
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            sendResponse(exchange, 400, "{\"error\": \"Missing player ID\"}");
            return;
        }
        String id = parts[3];
        if (playerDAO.getPlayerById(id).isEmpty()) {
            sendResponse(exchange, 404, "{\"error\": \"Player not found\"}");
            return;
        }
        playerDAO.deletePlayer(id);
        sendResponse(exchange, 200, "{\"success\": true}");
    }

    private String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes());
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

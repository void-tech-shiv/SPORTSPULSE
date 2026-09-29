package sportspulse.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sportspulse.dao.MatchDAO;
import sportspulse.models.Match;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MatchApiHandler implements HttpHandler {
    private final MatchDAO matchDAO = new MatchDAO();

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
            String id = parts[3];
            Optional<Match> m = matchDAO.getMatchById(id);
            if (m.isPresent()) {
                sendResponse(exchange, 200, JsonConverter.matchToJson(m.get()));
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Match not found\"}");
            }
        } else {
            List<Match> matches = matchDAO.getAllMatches();
            sendResponse(exchange, 200, JsonConverter.matchesToJson(matches));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, String> data = JsonConverter.parseJson(body);

        try {
            Match m = new Match(
                data.get("id"), data.get("competitionId"), data.get("teamAId"), data.get("teamBId"),
                data.get("date"), data.get("venue"), data.get("score"), data.get("status")
            );
            matchDAO.addMatch(m);
            sendResponse(exchange, 201, JsonConverter.matchToJson(m));
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid match data\"}");
        }
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            sendResponse(exchange, 400, "{\"error\": \"Missing match ID\"}");
            return;
        }
        String id = parts[3];
        Optional<Match> optionalMatch = matchDAO.getMatchById(id);
        if (optionalMatch.isEmpty()) {
            sendResponse(exchange, 404, "{\"error\": \"Match not found\"}");
            return;
        }

        String body = readBody(exchange);
        Map<String, String> data = JsonConverter.parseJson(body);
        Match m = optionalMatch.get();

        if (data.containsKey("competitionId")) m.setCompetitionId(data.get("competitionId"));
        if (data.containsKey("teamAId")) m.setTeamAId(data.get("teamAId"));
        if (data.containsKey("teamBId")) m.setTeamBId(data.get("teamBId"));
        if (data.containsKey("date")) m.setDate(data.get("date"));
        if (data.containsKey("venue")) m.setVenue(data.get("venue"));
        if (data.containsKey("score")) m.setScore(data.get("score"));
        if (data.containsKey("status")) m.setStatus(data.get("status"));

        matchDAO.updateMatch(m);
        sendResponse(exchange, 200, JsonConverter.matchToJson(m));
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            sendResponse(exchange, 400, "{\"error\": \"Missing match ID\"}");
            return;
        }
        String id = parts[3];
        if (matchDAO.getMatchById(id).isEmpty()) {
            sendResponse(exchange, 404, "{\"error\": \"Match not found\"}");
            return;
        }
        matchDAO.deleteMatch(id);
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

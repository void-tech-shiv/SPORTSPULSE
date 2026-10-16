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
            Optional<Match> match = matchDAO.getMatchById(id);
            if (match.isPresent()) {
                sendResponse(exchange, 200, JsonConverter.matchToJson(match.get()));
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Match not found\"}");
            }
        } else {
            List<Match> matches = matchDAO.getAllMatches();
            sendResponse(exchange, 200, JsonConverter.matchesToJson(matches));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        Map<String, String> data = JsonConverter.parseJson(readBody(exchange));
        String id = data.get("id");

        if (id == null || id.trim().isEmpty()) {
            sendResponse(exchange, 400, "{\"error\":\"Match ID is required\"}");
            return;
        }
        if (matchDAO.getMatchById(id).isPresent()) {
            sendResponse(exchange, 409, "{\"error\":\"Match with this ID already exists\"}");
            return;
        }

        try {
            String score = data.get("score");
            if (score == null || score.trim().isEmpty()) score = "0-0";
            String status = data.get("status");
            if (status == null || status.trim().isEmpty()) status = "Scheduled";

            Match match = new Match(
                id,
                data.get("competitionId"),
                data.get("teamAId"),
                data.get("teamBId"),
                data.get("date"),
                data.get("venue"),
                score,
                status
            );
            matchDAO.addMatch(match);
            sendResponse(exchange, 201, JsonConverter.matchToJson(match));
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\":\"Invalid match data\"}");
        }
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            sendResponse(exchange, 400, "{\"error\":\"Missing match ID\"}");
            return;
        }

        String id = parts[3];
        Optional<Match> optionalMatch = matchDAO.getMatchById(id);
        if (optionalMatch.isEmpty()) {
            sendResponse(exchange, 404, "{\"error\":\"Match not found\"}");
            return;
        }

        Map<String, String> data = JsonConverter.parseJson(readBody(exchange));
        Match match = optionalMatch.get();
        if (data.containsKey("competitionId")) match.setCompetitionId(data.get("competitionId"));
        if (data.containsKey("teamAId")) match.setTeamAId(data.get("teamAId"));
        if (data.containsKey("teamBId")) match.setTeamBId(data.get("teamBId"));
        if (data.containsKey("date")) match.setDate(data.get("date"));
        if (data.containsKey("venue")) match.setVenue(data.get("venue"));
        if (data.containsKey("score")) match.setScore(data.get("score"));
        if (data.containsKey("status")) match.setStatus(data.get("status"));

        matchDAO.updateMatch(match);
        sendResponse(exchange, 200, JsonConverter.matchToJson(match));
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            sendResponse(exchange, 400, "{\"error\":\"Missing match ID\"}");
            return;
        }

        String id = parts[3];
        if (matchDAO.getMatchById(id).isEmpty()) {
            sendResponse(exchange, 404, "{\"error\":\"Match not found\"}");
            return;
        }

        matchDAO.deleteMatch(id);
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

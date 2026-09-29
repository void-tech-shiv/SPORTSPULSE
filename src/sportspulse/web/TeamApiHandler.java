package sportspulse.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sportspulse.dao.TeamDAO;
import sportspulse.models.Team;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TeamApiHandler implements HttpHandler {
    private final TeamDAO teamDAO = new TeamDAO();

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
            Optional<Team> t = teamDAO.getTeamById(id);
            if (t.isPresent()) {
                sendResponse(exchange, 200, JsonConverter.teamToJson(t.get()));
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Team not found\"}");
            }
        } else {
            List<Team> teams = teamDAO.getAllTeams();
            sendResponse(exchange, 200, JsonConverter.teamsToJson(teams));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = readBody(exchange);
        Map<String, String> data = JsonConverter.parseJson(body);

        try {
            Team t = new Team(
                data.get("id"), data.get("name"), data.get("sport"), data.get("coach")
            );
            teamDAO.addTeam(t);
            sendResponse(exchange, 201, JsonConverter.teamToJson(t));
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\": \"Invalid team data\"}");
        }
    }

    private void handlePut(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            sendResponse(exchange, 400, "{\"error\": \"Missing team ID\"}");
            return;
        }
        String id = parts[3];
        Optional<Team> optionalTeam = teamDAO.getTeamById(id);
        if (optionalTeam.isEmpty()) {
            sendResponse(exchange, 404, "{\"error\": \"Team not found\"}");
            return;
        }

        String body = readBody(exchange);
        Map<String, String> data = JsonConverter.parseJson(body);
        Team t = optionalTeam.get();

        if (data.containsKey("name")) t.setName(data.get("name"));
        if (data.containsKey("sport")) t.setSport(data.get("sport"));
        if (data.containsKey("coach")) t.setCoach(data.get("coach"));

        teamDAO.updateTeam(t);
        sendResponse(exchange, 200, JsonConverter.teamToJson(t));
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            sendResponse(exchange, 400, "{\"error\": \"Missing team ID\"}");
            return;
        }
        String id = parts[3];
        if (teamDAO.getTeamById(id).isEmpty()) {
            sendResponse(exchange, 404, "{\"error\": \"Team not found\"}");
            return;
        }
        teamDAO.deleteTeam(id);
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

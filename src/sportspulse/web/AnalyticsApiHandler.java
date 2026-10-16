package sportspulse.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sportspulse.dao.MatchDAO;
import sportspulse.dao.PlayerDAO;
import sportspulse.dao.TeamDAO;
import sportspulse.models.Match;
import sportspulse.models.Player;
import sportspulse.models.Team;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsApiHandler implements HttpHandler {
    private final PlayerDAO playerDAO = new PlayerDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final MatchDAO matchDAO = new MatchDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        try {
            if (path.endsWith("/top-scorers")) {
                handleTopScorers(exchange);
            } else if (path.endsWith("/summary") || path.equals("/api/analytics") || path.equals("/api/analytics/")) {
                handleSummary(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\":\"Analytics endpoint not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Failed to calculate analytics\"}");
        }
    }

    private void handleTopScorers(HttpExchange exchange) throws IOException {
        List<Player> players = new ArrayList<>(playerDAO.getAllPlayers());
        players.sort((a, b) -> {
            int goals = Integer.compare(b.getGoals(), a.getGoals());
            if (goals != 0) return goals;
            return Double.compare(b.getPerformanceScore(), a.getPerformanceScore());
        });

        if (players.size() > 5) {
            players = new ArrayList<>(players.subList(0, 5));
        }

        sendResponse(exchange, 200, JsonConverter.playersToJson(players));
    }

    private void handleSummary(HttpExchange exchange) throws IOException {
        List<Player> players = playerDAO.getAllPlayers();
        List<Team> teams = teamDAO.getAllTeams();
        List<Match> matches = matchDAO.getAllMatches();

        double avgPerformance = 0.0;
        double avgFitness = 0.0;
        int totalGoals = 0;
        int completedMatches = 0;

        for (Player player : players) {
            avgPerformance += player.getPerformanceScore();
            avgFitness += player.getFitnessScore();
            totalGoals += player.getGoals();
        }

        if (!players.isEmpty()) {
            avgPerformance /= players.size();
            avgFitness /= players.size();
        }

        for (Match match : matches) {
            if ("completed".equalsIgnoreCase(match.getStatus())) {
                completedMatches++;
            }
        }

        String response = String.format(
            "{\"players\":%d,\"teams\":%d,\"matches\":%d,\"completedMatches\":%d,\"totalGoals\":%d,\"avgPerformance\":%.2f,\"avgFitness\":%.2f}",
            players.size(), teams.size(), matches.size(), completedMatches, totalGoals, avgPerformance, avgFitness
        );
        sendResponse(exchange, 200, response);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

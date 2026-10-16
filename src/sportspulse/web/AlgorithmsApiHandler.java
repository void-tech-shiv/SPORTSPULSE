package sportspulse.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import sportspulse.algorithms.DynamicProgramming;
import sportspulse.algorithms.Heuristics;
import sportspulse.algorithms.NPCompleteness;
import sportspulse.algorithms.NetworkFlow;
import sportspulse.algorithms.StringAlgorithms;
import sportspulse.algorithms.SuffixStructures;
import sportspulse.dao.MatchDAO;
import sportspulse.dao.PlayerDAO;
import sportspulse.dao.TeamDAO;
import sportspulse.models.Match;
import sportspulse.models.Player;
import sportspulse.models.Team;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgorithmsApiHandler implements HttpHandler {
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
        Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());

        try {
            if (path.endsWith("/search")) {
                handleSearch(exchange, params.getOrDefault("q", ""));
            } else if (path.endsWith("/run") || path.endsWith("/simulate")) {
                handleRun(exchange, params);
            } else {
                sendResponse(exchange, 200,
                    "{\"endpoints\":[\"/api/algorithms/search?q=...\",\"/api/algorithms/run?module=m1\"]}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\":\"Algorithm execution failed\"}");
        }
    }

    private void handleSearch(HttpExchange exchange, String query) throws IOException {
        String searchString = query == null ? "" : query.trim().toLowerCase();
        if (searchString.isEmpty()) {
            sendResponse(exchange, 400, "{\"error\":\"Missing q query parameter\"}");
            return;
        }

        List<Player> matchedPlayers = new ArrayList<>();
        for (Player p : playerDAO.getAllPlayers()) {
            String data = (p.getName() + " " + p.getPosition() + " " + p.getTeamId()).toLowerCase();
            if (!StringAlgorithms.kmpSearch(data, searchString).isEmpty()) {
                matchedPlayers.add(p);
            }
        }

        List<Team> matchedTeams = new ArrayList<>();
        for (Team t : teamDAO.getAllTeams()) {
            String data = (t.getName() + " " + t.getSport() + " " + t.getCoach()).toLowerCase();
            if (!StringAlgorithms.kmpSearch(data, searchString).isEmpty()) {
                matchedTeams.add(t);
            }
        }

        List<Match> matchedMatches = new ArrayList<>();
        for (Match m : matchDAO.getAllMatches()) {
            String data = (m.getId() + " " + m.getTeamAId() + " " + m.getTeamBId() + " " +
                m.getDate() + " " + m.getVenue() + " " + m.getStatus()).toLowerCase();
            if (!StringAlgorithms.kmpSearch(data, searchString).isEmpty()) {
                matchedMatches.add(m);
            }
        }

        String response = "{\"players\":" + JsonConverter.playersToJson(matchedPlayers)
            + ",\"teams\":" + JsonConverter.teamsToJson(matchedTeams)
            + ",\"matches\":" + JsonConverter.matchesToJson(matchedMatches) + "}";
        sendResponse(exchange, 200, response);
    }

    private void handleRun(HttpExchange exchange, Map<String, String> params) throws IOException {
        String module = params.getOrDefault("module", "").toLowerCase();
        if (module.isEmpty()) {
            sendResponse(exchange, 400, "{\"error\":\"Missing module parameter\"}");
            return;
        }

        long started = System.nanoTime();
        String title;
        String logs;

        switch (module) {
            case "m1":
                title = "M1 String Algorithms";
                logs = runStringAlgorithms(params.getOrDefault("pattern", "Mbappe"));
                break;
            case "m2":
                title = "M2 Suffix Structures";
                logs = runSuffixStructures(params.getOrDefault("pattern", "goal"));
                break;
            case "m3":
                title = "M3 Dynamic Programming";
                logs = runDynamicProgramming(parseInt(params.get("budget"), 50));
                break;
            case "m4":
                title = "M4 Network Flow";
                logs = runNetworkFlow();
                break;
            case "m5":
                title = "M5 NP-Completeness";
                logs = runNPCompleteness();
                break;
            case "m6":
                title = "M6 Randomized & Parallel / Heuristics";
                logs = runHeuristics();
                break;
            default:
                sendResponse(exchange, 400, "{\"error\":\"Unknown module\"}");
                return;
        }

        long timeMs = (System.nanoTime() - started) / 1_000_000L;
        String response = "{\"module\":\"" + escape(module) + "\",\"title\":\"" + escape(title)
            + "\",\"logs\":\"" + escape(logs) + "\",\"timeMs\":" + timeMs + "}";
        sendResponse(exchange, 200, response);
    }

    private String runStringAlgorithms(String pattern) {
        String text = "Lionel Messi passes to Kylian Mbappe, Mbappe shoots... Goal! What a goal by Mbappe!";
        List<Integer> kmp = StringAlgorithms.kmpSearch(text, pattern);
        List<Integer> rk = StringAlgorithms.rabinKarpSearch(text, pattern);

        return "Text: " + text + "\n"
            + "Pattern: " + pattern + "\n\n"
            + "KMP matches: " + kmp + "\n"
            + "Rabin-Karp matches: " + rk + "\n"
            + "Algorithms agree: " + kmp.equals(rk);
    }

    private String runSuffixStructures(String pattern) {
        String report = "The match started with high intensity. Messi passes to Mbappe, Mbappe scores a brilliant goal! "
            + "The crowd goes wild. Later, Messi passes to Neymar, but Neymar misses the goal. The match ends in a draw.";
        String text = report.toLowerCase();
        String search = pattern.toLowerCase();

        int[] suffixArray = SuffixStructures.buildSuffixArray(text);
        int[] lcp = SuffixStructures.buildLCPArray(text, suffixArray);
        List<Integer> matches = SuffixStructures.search(text, search, suffixArray);

        int maxLcp = 0;
        int maxIndex = -1;
        for (int i = 0; i < lcp.length; i++) {
            if (lcp[i] > maxLcp) {
                maxLcp = lcp[i];
                maxIndex = i;
            }
        }

        String repeated = "None";
        if (maxIndex >= 0 && maxLcp > 0) {
            int start = suffixArray[maxIndex];
            repeated = report.substring(start, Math.min(report.length(), start + maxLcp));
        }

        return "Report length: " + report.length() + " characters\n"
            + "Suffix array entries: " + suffixArray.length + "\n"
            + "LCP array entries: " + lcp.length + "\n"
            + "Search pattern: " + pattern + "\n"
            + "Matches: " + matches + "\n"
            + "Longest repeated substring: " + repeated + " (length " + maxLcp + ")";
    }

    private String runDynamicProgramming(int budget) {
        int safeBudget = Math.max(1, Math.min(budget, 500));
        int[] scores = {10, 22, 9, 33, 21, 50, 41, 60};
        List<Integer> lis = DynamicProgramming.longestIncreasingSubsequence(scores);

        String[] match1 = {"Pass", "Dribble", "Shoot", "Save", "Corner", "Goal"};
        String[] match2 = {"Tackle", "Pass", "Shoot", "Foul", "Corner", "Goal"};
        List<String> lcs = DynamicProgramming.longestCommonSubsequence(match1, match2);

        List<Player> available = new ArrayList<>();
        available.add(new Player("P1", "Messi", "T1", "FWD", 35, 10, 8, 5, 0, 0, 30.0, 95.0));
        available.add(new Player("P2", "Ronaldo", "T2", "FWD", 38, 10, 7, 2, 0, 0, 25.0, 85.0));
        available.add(new Player("P3", "Neymar", "T1", "FWD", 31, 10, 5, 6, 0, 0, 20.0, 75.0));
        available.add(new Player("P4", "De Bruyne", "T3", "MID", 32, 10, 2, 10, 0, 0, 15.0, 80.0));
        available.add(new Player("P5", "Van Dijk", "T4", "DEF", 32, 10, 1, 0, 0, 0, 10.0, 60.0));
        List<Player> selected = DynamicProgramming.knapsack(available, safeBudget);

        int totalCost = 0;
        int totalPoints = 0;
        List<String> selectedNames = new ArrayList<>();
        for (Player player : selected) {
            selectedNames.add(player.getName());
            totalCost += (int) player.getFitnessScore();
            totalPoints += (int) player.getPerformanceScore();
        }

        return "Scores: " + Arrays.toString(scores) + "\n"
            + "Longest Increasing Subsequence: " + lis + " (length " + lis.size() + ")\n\n"
            + "Match 1 events: " + Arrays.toString(match1) + "\n"
            + "Match 2 events: " + Arrays.toString(match2) + "\n"
            + "Longest Common Subsequence: " + lcs + " (length " + lcs.size() + ")\n\n"
            + "Fantasy budget: " + safeBudget + "\n"
            + "Selected players: " + selectedNames + "\n"
            + "Total cost: " + totalCost + "\n"
            + "Total projected points: " + totalPoints;
    }

    private String runNetworkFlow() {
        int[][] capacity = new int[6][6];
        capacity[0][1] = 16;
        capacity[0][2] = 13;
        capacity[1][2] = 10;
        capacity[1][3] = 12;
        capacity[2][1] = 4;
        capacity[2][4] = 14;
        capacity[3][2] = 9;
        capacity[3][5] = 20;
        capacity[4][3] = 7;
        capacity[4][5] = 4;
        int maxFlow = NetworkFlow.maxFlowDinic(capacity, 0, 5);

        boolean[][] graph = {
            {true, true, false, false},
            {false, true, true, false},
            {false, false, true, true},
            {true, false, false, true}
        };
        int[] assignments = NetworkFlow.bipartiteMatching(graph);
        int matched = 0;
        for (int assignment : assignments) if (assignment != -1) matched++;

        return "Dinic maximum resource flow: " + maxFlow + "\n"
            + "Player-to-position assignments: " + Arrays.toString(assignments) + "\n"
            + "Successful assignments: " + matched + "/" + assignments.length;
    }

    private String runNPCompleteness() {
        int[][] clauses = {
            {1, 2, 3},
            {-1, -2, 4},
            {-3, 4, -1}
        };
        boolean[] assignment = NPCompleteness.solve3SAT(4, clauses);

        boolean[][] graph = {
            {false, true, true, false, false},
            {true, false, true, true, false},
            {true, true, false, true, true},
            {false, true, true, false, true},
            {false, false, true, true, false}
        };
        List<Integer> exact = NPCompleteness.vertexCoverExact(graph);
        List<Integer> approximate = NPCompleteness.vertexCoverApproximate(graph);

        StringBuilder assignmentText = new StringBuilder();
        if (assignment == null) {
            assignmentText.append("UNSAT");
        } else {
            for (int i = 1; i <= 4; i++) {
                if (i > 1) assignmentText.append(", ");
                assignmentText.append("P").append(i).append("=").append(assignment[i] ? "Selected" : "Benched");
            }
        }

        return "3-SAT assignment: " + assignmentText + "\n"
            + "Exact minimum vertex cover: " + exact + "\n"
            + "2-approximate vertex cover: " + approximate;
    }

    private String runHeuristics() {
        int numPlayers = 50;
        int targetRosterSize = 15;
        boolean[] roster = Heuristics.simulatedAnnealing(numPlayers, 10000.0, 0.003, targetRosterSize);
        double rosterFitness = Heuristics.calculateRosterFitness(roster);

        List<Integer> selectedIds = new ArrayList<>();
        for (int i = 0; i < roster.length; i++) {
            if (roster[i]) selectedIds.add(i);
        }

        int[] schedule = Heuristics.geneticAlgorithm(20, 100, 500, 0.05);
        double scheduleFitness = Heuristics.calculateScheduleFitness(schedule);

        return "Simulated Annealing roster size: " + selectedIds.size() + "/" + targetRosterSize + "\n"
            + "Selected player IDs: " + selectedIds + "\n"
            + String.format("Roster fitness: %.2f\n\n", rosterFitness)
            + "Genetic Algorithm schedule: " + Arrays.toString(schedule) + "\n"
            + String.format("Schedule fitness: %.2f", scheduleFitness);
    }

    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return params;

        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    private int parseInt(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

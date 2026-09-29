package sportspulse.web;

import sportspulse.models.Player;
import sportspulse.models.Team;
import sportspulse.models.Match;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class JsonConverter {

    public static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    public static String playerToJson(Player p) {
        if (p == null) return "{}";
        return String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"teamId\":\"%s\",\"position\":\"%s\",\"age\":%d,\"matchesPlayed\":%d,\"goals\":%d,\"assists\":%d,\"wickets\":%d,\"runs\":%d,\"fitnessScore\":%.2f,\"performanceScore\":%.2f}",
            escapeJson(p.getId()), escapeJson(p.getName()), escapeJson(p.getTeamId()), escapeJson(p.getPosition()),
            p.getAge(), p.getMatchesPlayed(), p.getGoals(), p.getAssists(), p.getWickets(), p.getRuns(),
            p.getFitnessScore(), p.getPerformanceScore()
        );
    }

    public static String playersToJson(List<Player> players) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < players.size(); i++) {
            sb.append(playerToJson(players.get(i)));
            if (i < players.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String teamToJson(Team t) {
        if (t == null) return "{}";
        return String.format(
            "{\"id\":\"%s\",\"name\":\"%s\",\"sport\":\"%s\",\"coach\":\"%s\"}",
            escapeJson(t.getId()), escapeJson(t.getName()), escapeJson(t.getSport()), escapeJson(t.getCoach())
        );
    }

    public static String teamsToJson(List<Team> teams) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < teams.size(); i++) {
            sb.append(teamToJson(teams.get(i)));
            if (i < teams.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String matchToJson(Match m) {
        if (m == null) return "{}";
        return String.format(
            "{\"id\":\"%s\",\"competitionId\":\"%s\",\"teamAId\":\"%s\",\"teamBId\":\"%s\",\"date\":\"%s\",\"venue\":\"%s\",\"score\":\"%s\",\"status\":\"%s\"}",
            escapeJson(m.getId()), escapeJson(m.getCompetitionId()), escapeJson(m.getTeamAId()), escapeJson(m.getTeamBId()),
            escapeJson(m.getDate()), escapeJson(m.getVenue()), escapeJson(m.getScore()), escapeJson(m.getStatus())
        );
    }

    public static String matchesToJson(List<Match> matches) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < matches.size(); i++) {
            sb.append(matchToJson(matches.get(i)));
            if (i < matches.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
    
    public static String eventToJson(sportspulse.models.MatchEvent e) {
        if (e == null) return "{}";
        return String.format(
            "{\"matchId\":\"%s\",\"timestamp\":\"%s\",\"playerId\":\"%s\",\"eventType\":\"%s\",\"description\":\"%s\"}",
            escapeJson(e.getMatchId()), escapeJson(e.getTimestamp()), escapeJson(e.getPlayerId()), escapeJson(e.getEventType()), escapeJson(e.getDescription())
        );
    }
    
    public static String toJson(List<sportspulse.models.MatchEvent> events) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < events.size(); i++) {
            sb.append(eventToJson(events.get(i)));
            if (i < events.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // Very basic JSON parser for single level objects (e.g. {"key": "value", "key2": 123})
    public static Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.trim().isEmpty()) return map;
        
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }
        
        // This is a naive split that doesn't handle commas inside string values well,
        // but works for basic cases assuming our POST data is simple enough.
        // A better approach for this assignment without libs:
        boolean inQuotes = false;
        StringBuilder currentKey = new StringBuilder();
        StringBuilder currentValue = new StringBuilder();
        boolean parsingKey = true;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            
            if (c == '"' && (i == 0 || json.charAt(i-1) != '\\')) {
                inQuotes = !inQuotes;
                continue;
            }
            
            if (!inQuotes) {
                if (c == ':') {
                    parsingKey = false;
                    continue;
                }
                if (c == ',') {
                    map.put(currentKey.toString().trim(), currentValue.toString().trim());
                    currentKey = new StringBuilder();
                    currentValue = new StringBuilder();
                    parsingKey = true;
                    continue;
                }
                if (Character.isWhitespace(c)) {
                    continue; // skip whitespace outside quotes
                }
            }
            
            if (parsingKey) {
                currentKey.append(c);
            } else {
                currentValue.append(c);
            }
        }
        
        if (currentKey.length() > 0) {
            map.put(currentKey.toString().trim(), currentValue.toString().trim());
        }
        
        return map;
    }
}

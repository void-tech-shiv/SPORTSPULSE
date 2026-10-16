package sportspulse.dao;

import sportspulse.db.Database;
import sportspulse.models.MatchEvent;
import sportspulse.utils.FileManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    private static final String FILENAME = "events.csv";

    public List<MatchEvent> getAllEvents() {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT match_id,event_timestamp,player_id,event_type,description FROM match_events ORDER BY id")) {
                ResultSet rs = statement.executeQuery();
                List<MatchEvent> events = new ArrayList<>();
                while (rs.next()) {
                    events.add(new MatchEvent(
                            rs.getString("match_id"),
                            rs.getString("event_timestamp"),
                            rs.getString("player_id"),
                            rs.getString("event_type"),
                            rs.getString("description")
                    ));
                }
                return events;
            } catch (SQLException e) {
                System.err.println("PostgreSQL event operation failed; using CSV fallback: " + e.getMessage());
            }
        }

        return getAllEventsFromFile();
    }

    private List<MatchEvent> getAllEventsFromFile() {
        List<String> lines = FileManager.readLines(FILENAME);
        List<MatchEvent> events = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",", 5);
            if (parts.length >= 5) {
                events.add(new MatchEvent(parts[0], parts[1], parts[2], parts[3], parts[4]));
            }
        }
        return events;
    }
}

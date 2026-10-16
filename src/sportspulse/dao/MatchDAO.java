package sportspulse.dao;

import sportspulse.db.Database;
import sportspulse.models.Match;
import sportspulse.utils.FileManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchDAO {
    private static final String FILENAME = "matches.csv";
    private static final String HEADER = "id,competitionId,teamAId,teamBId,date,venue,score,status";

    public List<Match> getAllMatches() {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT id,competition_id,team_a_id,team_b_id,match_date,venue,score,status FROM matches ORDER BY id")) {
                ResultSet rs = statement.executeQuery();
                List<Match> matches = new ArrayList<>();
                while (rs.next()) {
                    matches.add(mapMatch(rs));
                }
                return matches;
            } catch (SQLException e) {
                logDatabaseFallback(e);
            }
        }
        return getAllMatchesFromFile();
    }

    public void saveAllMatches(List<Match> matches) {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    for (Match match : matches) {
                        upsertMatch(connection, match);
                    }
                    connection.commit();
                    return;
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                logDatabaseFallback(e);
            }
        }
        saveAllMatchesToFile(matches);
    }

    public void addMatch(Match match) {
        if (writeMatchToDatabase(match)) return;
        List<Match> matches = getAllMatchesFromFile();
        matches.add(match);
        saveAllMatchesToFile(matches);
    }

    public Optional<Match> getMatchById(String id) {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT id,competition_id,team_a_id,team_b_id,match_date,venue,score,status FROM matches WHERE id=?")) {
                statement.setString(1, id);
                ResultSet rs = statement.executeQuery();
                return rs.next() ? Optional.of(mapMatch(rs)) : Optional.empty();
            } catch (SQLException e) {
                logDatabaseFallback(e);
            }
        }
        return getAllMatchesFromFile().stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public void updateMatch(Match updatedMatch) {
        if (writeMatchToDatabase(updatedMatch)) return;

        List<Match> matches = getAllMatchesFromFile();
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).getId().equals(updatedMatch.getId())) {
                matches.set(i, updatedMatch);
                break;
            }
        }
        saveAllMatchesToFile(matches);
    }

    public void deleteMatch(String id) {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM matches WHERE id=?")) {
                statement.setString(1, id);
                statement.executeUpdate();
                return;
            } catch (SQLException e) {
                logDatabaseFallback(e);
            }
        }

        List<Match> matches = getAllMatchesFromFile();
        matches.removeIf(m -> m.getId().equals(id));
        saveAllMatchesToFile(matches);
    }

    private boolean writeMatchToDatabase(Match match) {
        if (!Database.isConfigured()) return false;
        try (Connection connection = Database.getConnection()) {
            upsertMatch(connection, match);
            return true;
        } catch (SQLException e) {
            logDatabaseFallback(e);
            return false;
        }
    }

    private void upsertMatch(Connection connection, Match match) throws SQLException {
        String sql = "INSERT INTO matches (id,competition_id,team_a_id,team_b_id,match_date,venue,score,status) " +
                "VALUES (?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET competition_id=EXCLUDED.competition_id,team_a_id=EXCLUDED.team_a_id," +
                "team_b_id=EXCLUDED.team_b_id,match_date=EXCLUDED.match_date,venue=EXCLUDED.venue,score=EXCLUDED.score," +
                "status=EXCLUDED.status,updated_at=now()";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, match.getId());
            statement.setString(2, match.getCompetitionId());
            statement.setString(3, match.getTeamAId());
            statement.setString(4, match.getTeamBId());
            statement.setString(5, match.getDate());
            statement.setString(6, match.getVenue());
            statement.setString(7, match.getScore());
            statement.setString(8, match.getStatus());
            statement.executeUpdate();
        }
    }

    private Match mapMatch(ResultSet rs) throws SQLException {
        return new Match(
                rs.getString("id"),
                rs.getString("competition_id"),
                rs.getString("team_a_id"),
                rs.getString("team_b_id"),
                rs.getString("match_date"),
                rs.getString("venue"),
                rs.getString("score"),
                rs.getString("status")
        );
    }

    private List<Match> getAllMatchesFromFile() {
        List<String> lines = FileManager.readLines(FILENAME);
        List<Match> matches = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",", -1);
            if (parts.length >= 8) {
                matches.add(new Match(
                        parts[0], parts[1], parts[2], parts[3],
                        parts[4], parts[5], parts[6], parts[7]
                ));
            }
        }
        return matches;
    }

    private void saveAllMatchesToFile(List<Match> matches) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Match m : matches) {
            lines.add(String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                    m.getId(), m.getCompetitionId(), m.getTeamAId(), m.getTeamBId(),
                    m.getDate(), m.getVenue(), m.getScore(), m.getStatus()));
        }
        FileManager.writeLines(FILENAME, lines);
    }

    private void logDatabaseFallback(SQLException e) {
        System.err.println("PostgreSQL match operation failed; using CSV fallback: " + e.getMessage());
    }
}

package sportspulse.dao;

import sportspulse.db.Database;
import sportspulse.models.Team;
import sportspulse.utils.FileManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamDAO {
    private static final String FILENAME = "teams.csv";
    private static final String HEADER = "id,name,sport,coach,playerIds";

    public List<Team> getAllTeams() {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT id,name,sport,coach,player_ids FROM teams ORDER BY id")) {
                ResultSet rs = statement.executeQuery();
                List<Team> teams = new ArrayList<>();
                while (rs.next()) {
                    teams.add(mapTeam(rs));
                }
                return teams;
            } catch (SQLException e) {
                logDatabaseFallback(e);
            }
        }
        return getAllTeamsFromFile();
    }

    public void saveAllTeams(List<Team> teams) {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    for (Team team : teams) {
                        upsertTeam(connection, team);
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
        saveAllTeamsToFile(teams);
    }

    public void addTeam(Team team) {
        if (writeTeamToDatabase(team)) return;
        List<Team> teams = getAllTeamsFromFile();
        teams.add(team);
        saveAllTeamsToFile(teams);
    }

    public Optional<Team> getTeamById(String id) {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT id,name,sport,coach,player_ids FROM teams WHERE id=?")) {
                statement.setString(1, id);
                ResultSet rs = statement.executeQuery();
                return rs.next() ? Optional.of(mapTeam(rs)) : Optional.empty();
            } catch (SQLException e) {
                logDatabaseFallback(e);
            }
        }
        return getAllTeamsFromFile().stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public void updateTeam(Team updatedTeam) {
        if (writeTeamToDatabase(updatedTeam)) return;

        List<Team> teams = getAllTeamsFromFile();
        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getId().equals(updatedTeam.getId())) {
                teams.set(i, updatedTeam);
                break;
            }
        }
        saveAllTeamsToFile(teams);
    }

    public void deleteTeam(String id) {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM teams WHERE id=?")) {
                statement.setString(1, id);
                statement.executeUpdate();
                return;
            } catch (SQLException e) {
                logDatabaseFallback(e);
            }
        }

        List<Team> teams = getAllTeamsFromFile();
        teams.removeIf(t -> t.getId().equals(id));
        saveAllTeamsToFile(teams);
    }

    private boolean writeTeamToDatabase(Team team) {
        if (!Database.isConfigured()) return false;
        try (Connection connection = Database.getConnection()) {
            upsertTeam(connection, team);
            return true;
        } catch (SQLException e) {
            logDatabaseFallback(e);
            return false;
        }
    }

    private void upsertTeam(Connection connection, Team team) throws SQLException {
        String sql = "INSERT INTO teams (id,name,sport,coach,player_ids) VALUES (?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET name=EXCLUDED.name,sport=EXCLUDED.sport,coach=EXCLUDED.coach," +
                "player_ids=EXCLUDED.player_ids,updated_at=now()";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, team.getId());
            statement.setString(2, team.getName());
            statement.setString(3, team.getSport());
            statement.setString(4, team.getCoach());
            statement.setArray(5, connection.createArrayOf("text", team.getPlayerIds().toArray(new String[0])));
            statement.executeUpdate();
        }
    }

    private Team mapTeam(ResultSet rs) throws SQLException {
        Team team = new Team(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("sport"),
                rs.getString("coach")
        );

        java.sql.Array sqlArray = rs.getArray("player_ids");
        if (sqlArray != null) {
            Object array = sqlArray.getArray();
            if (array instanceof String[]) {
                for (String playerId : (String[]) array) {
                    team.addPlayer(playerId);
                }
            }
        }
        return team;
    }

    private List<Team> getAllTeamsFromFile() {
        List<String> lines = FileManager.readLines(FILENAME);
        List<Team> teams = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            if (parts.length >= 4) {
                Team team = new Team(parts[0], parts[1], parts[2], parts[3]);
                if (parts.length >= 5 && !parts[4].isBlank()) {
                    String rawIds = parts[4].replace("\"", "").trim();
                    for (String playerId : rawIds.split("[;,]")) {
                        if (!playerId.isBlank()) team.addPlayer(playerId.trim());
                    }
                }
                teams.add(team);
            }
        }
        return teams;
    }

    private void saveAllTeamsToFile(List<Team> teams) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Team t : teams) {
            String playerIds = String.join(";", t.getPlayerIds());
            lines.add(String.format("%s,%s,%s,%s,%s",
                    t.getId(), t.getName(), t.getSport(), t.getCoach(), playerIds));
        }
        FileManager.writeLines(FILENAME, lines);
    }

    private void logDatabaseFallback(SQLException e) {
        System.err.println("PostgreSQL team operation failed; using CSV fallback: " + e.getMessage());
    }
}

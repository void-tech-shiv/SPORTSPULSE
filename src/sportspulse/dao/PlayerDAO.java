package sportspulse.dao;

import sportspulse.db.Database;
import sportspulse.models.Player;
import sportspulse.utils.FileManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerDAO {
    private static final String FILENAME = "players.csv";
    private static final String HEADER = "id,name,teamId,position,age,matchesPlayed,goals,assists,wickets,runs,fitnessScore,performanceScore";

    public List<Player> getAllPlayers() {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT id,name,team_id,position,age,matches_played,goals,assists,wickets,runs,fitness_score,performance_score FROM players ORDER BY id")) {
                ResultSet rs = statement.executeQuery();
                List<Player> players = new ArrayList<>();
                while (rs.next()) {
                    players.add(mapPlayer(rs));
                }
                return players;
            } catch (SQLException e) {
                logDatabaseFallback(e);
            }
        }
        return getAllPlayersFromFile();
    }

    public void saveAllPlayers(List<Player> players) {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    for (Player player : players) {
                        upsertPlayer(connection, player);
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
        saveAllPlayersToFile(players);
    }

    public void addPlayer(Player player) {
        if (writePlayerToDatabase(player)) return;
        List<Player> players = getAllPlayersFromFile();
        players.add(player);
        saveAllPlayersToFile(players);
    }

    public Optional<Player> getPlayerById(String id) {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT id,name,team_id,position,age,matches_played,goals,assists,wickets,runs,fitness_score,performance_score FROM players WHERE id=?")) {
                statement.setString(1, id);
                ResultSet rs = statement.executeQuery();
                return rs.next() ? Optional.of(mapPlayer(rs)) : Optional.empty();
            } catch (SQLException e) {
                logDatabaseFallback(e);
            }
        }
        return getAllPlayersFromFile().stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public void updatePlayer(Player updatedPlayer) {
        if (writePlayerToDatabase(updatedPlayer)) return;

        List<Player> players = getAllPlayersFromFile();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(updatedPlayer.getId())) {
                players.set(i, updatedPlayer);
                break;
            }
        }
        saveAllPlayersToFile(players);
    }

    public void deletePlayer(String id) {
        if (Database.isConfigured()) {
            try (Connection connection = Database.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM players WHERE id=?")) {
                statement.setString(1, id);
                statement.executeUpdate();
                return;
            } catch (SQLException e) {
                logDatabaseFallback(e);
            }
        }

        List<Player> players = getAllPlayersFromFile();
        players.removeIf(p -> p.getId().equals(id));
        saveAllPlayersToFile(players);
    }

    private boolean writePlayerToDatabase(Player player) {
        if (!Database.isConfigured()) return false;
        try (Connection connection = Database.getConnection()) {
            upsertPlayer(connection, player);
            return true;
        } catch (SQLException e) {
            logDatabaseFallback(e);
            return false;
        }
    }

    private void upsertPlayer(Connection connection, Player player) throws SQLException {
        String sql = "INSERT INTO players (id,name,team_id,position,age,matches_played,goals,assists,wickets,runs,fitness_score,performance_score) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON CONFLICT (id) DO UPDATE SET name=EXCLUDED.name,team_id=EXCLUDED.team_id,position=EXCLUDED.position," +
                "age=EXCLUDED.age,matches_played=EXCLUDED.matches_played,goals=EXCLUDED.goals,assists=EXCLUDED.assists," +
                "wickets=EXCLUDED.wickets,runs=EXCLUDED.runs,fitness_score=EXCLUDED.fitness_score," +
                "performance_score=EXCLUDED.performance_score,updated_at=now()";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.getId());
            statement.setString(2, player.getName());
            statement.setString(3, player.getTeamId());
            statement.setString(4, player.getPosition());
            statement.setInt(5, player.getAge());
            statement.setInt(6, player.getMatchesPlayed());
            statement.setInt(7, player.getGoals());
            statement.setInt(8, player.getAssists());
            statement.setInt(9, player.getWickets());
            statement.setInt(10, player.getRuns());
            statement.setDouble(11, player.getFitnessScore());
            statement.setDouble(12, player.getPerformanceScore());
            statement.executeUpdate();
        }
    }

    private Player mapPlayer(ResultSet rs) throws SQLException {
        return new Player(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("team_id"),
                rs.getString("position"),
                rs.getInt("age"),
                rs.getInt("matches_played"),
                rs.getInt("goals"),
                rs.getInt("assists"),
                rs.getInt("wickets"),
                rs.getInt("runs"),
                rs.getDouble("fitness_score"),
                rs.getDouble("performance_score")
        );
    }

    private List<Player> getAllPlayersFromFile() {
        List<String> lines = FileManager.readLines(FILENAME);
        List<Player> players = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",");
            if (parts.length >= 12) {
                players.add(new Player(
                        parts[0], parts[1], parts[2], parts[3],
                        Integer.parseInt(parts[4]), Integer.parseInt(parts[5]),
                        Integer.parseInt(parts[6]), Integer.parseInt(parts[7]),
                        Integer.parseInt(parts[8]), Integer.parseInt(parts[9]),
                        Double.parseDouble(parts[10]), Double.parseDouble(parts[11])
                ));
            }
        }
        return players;
    }

    private void saveAllPlayersToFile(List<Player> players) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Player p : players) {
            lines.add(String.format("%s,%s,%s,%s,%d,%d,%d,%d,%d,%d,%.2f,%.2f",
                    p.getId(), p.getName(), p.getTeamId(), p.getPosition(),
                    p.getAge(), p.getMatchesPlayed(), p.getGoals(), p.getAssists(),
                    p.getWickets(), p.getRuns(), p.getFitnessScore(), p.getPerformanceScore()));
        }
        FileManager.writeLines(FILENAME, lines);
    }

    private void logDatabaseFallback(SQLException e) {
        System.err.println("PostgreSQL player operation failed; using CSV fallback: " + e.getMessage());
    }
}

package sportspulse.dao;

import sportspulse.models.Player;
import sportspulse.utils.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerDAO {
    private static final String FILENAME = "players.csv";
    private static final String HEADER = "id,name,teamId,position,age,matchesPlayed,goals,assists,wickets,runs,fitnessScore,performanceScore";

    public List<Player> getAllPlayers() {
        List<String> lines = FileManager.readLines(FILENAME);
        List<Player> players = new ArrayList<>();
        // Skip header (index 0)
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

    public void saveAllPlayers(List<Player> players) {
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

    public void addPlayer(Player player) {
        List<Player> players = getAllPlayers();
        players.add(player);
        saveAllPlayers(players);
    }

    public Optional<Player> getPlayerById(String id) {
        return getAllPlayers().stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public void updatePlayer(Player updatedPlayer) {
        List<Player> players = getAllPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getId().equals(updatedPlayer.getId())) {
                players.set(i, updatedPlayer);
                break;
            }
        }
        saveAllPlayers(players);
    }

    public void deletePlayer(String id) {
        List<Player> players = getAllPlayers();
        players.removeIf(p -> p.getId().equals(id));
        saveAllPlayers(players);
    }
}

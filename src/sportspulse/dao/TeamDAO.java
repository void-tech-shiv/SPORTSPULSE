package sportspulse.dao;

import sportspulse.models.Team;
import sportspulse.utils.FileManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TeamDAO {
    private static final String FILENAME = "teams.csv";
    private static final String HEADER = "id,name,sport,coach,playerIds";

    public List<Team> getAllTeams() {
        List<String> lines = FileManager.readLines(FILENAME);
        List<Team> teams = new ArrayList<>();
        // Skip header (index 0)
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",", -1);
            if (parts.length >= 4) {
                Team team = new Team(parts[0], parts[1], parts[2], parts[3]);
                if (parts.length >= 5 && !parts[4].isEmpty()) {
                    String[] ids = parts[4].split(";");
                    for (String pid : ids) {
                        team.addPlayer(pid);
                    }
                }
                teams.add(team);
            }
        }
        return teams;
    }

    public void saveAllTeams(List<Team> teams) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Team t : teams) {
            String playerIds = String.join(";", t.getPlayerIds());
            lines.add(String.format("%s,%s,%s,%s,%s",
                    t.getId(), t.getName(), t.getSport(), t.getCoach(), playerIds));
        }
        FileManager.writeLines(FILENAME, lines);
    }

    public void addTeam(Team team) {
        List<Team> teams = getAllTeams();
        teams.add(team);
        saveAllTeams(teams);
    }

    public Optional<Team> getTeamById(String id) {
        return getAllTeams().stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public void updateTeam(Team updatedTeam) {
        List<Team> teams = getAllTeams();
        for (int i = 0; i < teams.size(); i++) {
            if (teams.get(i).getId().equals(updatedTeam.getId())) {
                teams.set(i, updatedTeam);
                break;
            }
        }
        saveAllTeams(teams);
    }

    public void deleteTeam(String id) {
        List<Team> teams = getAllTeams();
        teams.removeIf(t -> t.getId().equals(id));
        saveAllTeams(teams);
    }
}

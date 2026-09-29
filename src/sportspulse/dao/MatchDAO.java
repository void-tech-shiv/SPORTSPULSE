package sportspulse.dao;

import sportspulse.models.Match;
import sportspulse.utils.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchDAO {
    private static final String FILENAME = "matches.csv";
    private static final String HEADER = "id,competitionId,teamAId,teamBId,date,venue,score,status";

    public List<Match> getAllMatches() {
        List<String> lines = FileManager.readLines(FILENAME);
        List<Match> matches = new ArrayList<>();
        // Skip header (index 0)
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

    public void saveAllMatches(List<Match> matches) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Match m : matches) {
            lines.add(String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                    m.getId(), m.getCompetitionId(), m.getTeamAId(), m.getTeamBId(),
                    m.getDate(), m.getVenue(), m.getScore(), m.getStatus()));
        }
        FileManager.writeLines(FILENAME, lines);
    }

    public void addMatch(Match match) {
        List<Match> matches = getAllMatches();
        matches.add(match);
        saveAllMatches(matches);
    }

    public Optional<Match> getMatchById(String id) {
        return getAllMatches().stream().filter(m -> m.getId().equals(id)).findFirst();
    }

    public void updateMatch(Match updatedMatch) {
        List<Match> matches = getAllMatches();
        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i).getId().equals(updatedMatch.getId())) {
                matches.set(i, updatedMatch);
                break;
            }
        }
        saveAllMatches(matches);
    }

    public void deleteMatch(String id) {
        List<Match> matches = getAllMatches();
        matches.removeIf(m -> m.getId().equals(id));
        saveAllMatches(matches);
    }
}

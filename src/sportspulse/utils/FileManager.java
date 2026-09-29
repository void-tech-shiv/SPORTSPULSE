package sportspulse.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {
    private static final String DATA_DIR = "data";

    public static void initializeDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        createFileIfNotExists("players.csv", "id,name,teamId,position,age,matchesPlayed,goals,assists,wickets,runs,fitnessScore,performanceScore");
        createFileIfNotExists("teams.csv", "id,name,sport,coach,playerIds");
        createFileIfNotExists("competitions.csv", "id,name,year,sport");
        createFileIfNotExists("matches.csv", "id,competitionId,teamAId,teamBId,date,venue,score,status");
        createFileIfNotExists("events.csv", "matchId,timestamp,playerId,eventType,description");
        createFileIfNotExists("reports.txt", "Report ID, Content");
    }

    private static void createFileIfNotExists(String filename, String header) {
        Path filePath = Paths.get(DATA_DIR, filename);
        if (!Files.exists(filePath)) {
            try {
                Files.write(filePath, (header + System.lineSeparator()).getBytes());
            } catch (IOException e) {
                System.err.println("Error creating file " + filename + ": " + e.getMessage());
            }
        }
    }

    public static java.util.List<String> readLines(String filename) {
        Path filePath = Paths.get(DATA_DIR, filename);
        try {
            if (Files.exists(filePath)) {
                return Files.readAllLines(filePath);
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
        }
        return new java.util.ArrayList<>();
    }

    public static void writeLines(String filename, java.util.List<String> lines) {
        Path filePath = Paths.get(DATA_DIR, filename);
        try {
            Files.write(filePath, lines);
        } catch (IOException e) {
            System.err.println("Error writing to file " + filename + ": " + e.getMessage());
        }
    }
}

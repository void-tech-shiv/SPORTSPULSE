package sportspulse.cli;

import sportspulse.dao.MatchDAO;
import sportspulse.models.Match;
import sportspulse.utils.ConsoleUtils;
import sportspulse.utils.InputValidator;

import java.util.List;
import java.util.Optional;

public class MatchMenu {
    private MatchDAO matchDAO;

    public MatchMenu() {
        this.matchDAO = new MatchDAO();
    }

    public void display() {
        boolean running = true;
        while (running) {
            ConsoleUtils.clearScreen();
            ConsoleUtils.printHeader("MATCH MANAGEMENT");
            System.out.println("1. Schedule Match");
            System.out.println("2. View All Matches");
            System.out.println("3. Search Match by Date");
            System.out.println("4. Update Match Details");
            System.out.println("5. Delete Match");
            System.out.println("6. Record Match Event");
            System.out.println("7. Back to Main Menu");
            
            int choice = InputValidator.getIntInput("\nEnter choice: ");
            switch (choice) {
                case 1:
                    scheduleMatch();
                    break;
                case 2:
                    viewAllMatches();
                    break;
                case 3:
                    searchMatch();
                    break;
                case 4:
                    updateMatch();
                    break;
                case 5:
                    deleteMatch();
                    break;
                case 6:
                    System.out.println("Event recording not fully implemented in this phase.");
                    InputValidator.waitForEnter();
                    break;
                case 7:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    InputValidator.waitForEnter();
            }
        }
    }

    private void scheduleMatch() {
        System.out.println("\n--- Schedule Match ---");
        String id = InputValidator.getStringInput("Enter match ID: ");
        if (matchDAO.getMatchById(id).isPresent()) {
            System.out.println("Match with this ID already exists.");
            InputValidator.waitForEnter();
            return;
        }

        String compId = InputValidator.getStringInput("Enter competition ID: ");
        String teamA = InputValidator.getStringInput("Enter Team A ID: ");
        String teamB = InputValidator.getStringInput("Enter Team B ID: ");
        String date = InputValidator.getStringInput("Enter Date (YYYY-MM-DD): ");
        String venue = InputValidator.getStringInput("Enter Venue: ");
        
        Match newMatch = new Match(id, compId, teamA, teamB, date, venue, "0-0", "Scheduled");
        matchDAO.addMatch(newMatch);
        System.out.println("Match scheduled successfully!");
        InputValidator.waitForEnter();
    }

    private void viewAllMatches() {
        System.out.println("\n--- All Matches ---");
        List<Match> matches = matchDAO.getAllMatches();
        if (matches.isEmpty()) {
            System.out.println("No matches scheduled.");
        } else {
            for (Match m : matches) {
                System.out.println(m);
            }
        }
        InputValidator.waitForEnter();
    }

    private void searchMatch() {
        System.out.println("\n--- Search Match ---");
        String query = InputValidator.getStringInput("Enter date (YYYY-MM-DD) to search: ");
        
        List<Match> matches = matchDAO.getAllMatches();
        boolean found = false;
        
        for (Match m : matches) {
            if (m.getDate().equals(query)) {
                System.out.println("- Found match: " + m);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No matches found for '" + query + "'.");
        }
        InputValidator.waitForEnter();
    }
    
    private void updateMatch() {
        System.out.println("\n--- Update Match ---");
        String id = InputValidator.getStringInput("Enter match ID to update: ");
        Optional<Match> optionalMatch = matchDAO.getMatchById(id);
        
        if (optionalMatch.isEmpty()) {
            System.out.println("Match not found.");
            InputValidator.waitForEnter();
            return;
        }
        
        Match m = optionalMatch.get();
        System.out.println("Current details: " + m);
        
        String score = InputValidator.getStringInput("Enter new score (or press enter to keep current): ");
        if (!score.trim().isEmpty()) m.setScore(score);
        
        String status = InputValidator.getStringInput("Enter new status (or press enter to keep current): ");
        if (!status.trim().isEmpty()) m.setStatus(status);
        
        matchDAO.updateMatch(m);
        System.out.println("Match updated successfully!");
        InputValidator.waitForEnter();
    }
    
    private void deleteMatch() {
        System.out.println("\n--- Delete Match ---");
        String id = InputValidator.getStringInput("Enter match ID to delete: ");
        if (matchDAO.getMatchById(id).isEmpty()) {
            System.out.println("Match not found.");
            InputValidator.waitForEnter();
            return;
        }
        
        matchDAO.deleteMatch(id);
        System.out.println("Match deleted successfully!");
        InputValidator.waitForEnter();
    }
}

package sportspulse.cli;

import sportspulse.dao.PlayerDAO;
import sportspulse.dao.TeamDAO;
import sportspulse.models.Player;
import sportspulse.utils.ConsoleUtils;
import sportspulse.utils.InputValidator;

import java.util.List;

public class AnalyticsMenu {
    private PlayerDAO playerDAO;
    private TeamDAO teamDAO;

    public AnalyticsMenu() {
        this.playerDAO = new PlayerDAO();
        this.teamDAO = new TeamDAO();
    }

    public void display() {
        boolean running = true;
        while (running) {
            ConsoleUtils.clearScreen();
            ConsoleUtils.printHeader("PERFORMANCE ANALYTICS");
            System.out.println("1. Top Scorers (Sorting)");
            System.out.println("2. Team Performance Rankings");
            System.out.println("3. Player Trends (Graphs)");
            System.out.println("4. Back to Main Menu");
            
            int choice = InputValidator.getIntInput("\nEnter choice: ");
            switch (choice) {
                case 1:
                    displayTopScorers();
                    break;
                case 2:
                case 3:
                    System.out.println("Feature not fully implemented in this phase.");
                    InputValidator.waitForEnter();
                    break;
                case 4:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    InputValidator.waitForEnter();
            }
        }
    }

    private void displayTopScorers() {
        System.out.println("\n--- Top Scorers ---");
        List<Player> players = playerDAO.getAllPlayers();
        
        if (players.isEmpty()) {
            System.out.println("No players found.");
            InputValidator.waitForEnter();
            return;
        }

        // Sort players by goals (descending)
        players.sort((p1, p2) -> Integer.compare(p2.getGoals(), p1.getGoals()));

        int count = 0;
        for (Player p : players) {
            if (count >= 5) break; // Top 5
            System.out.println((count + 1) + ". " + p.getName() + " - " + p.getGoals() + " goals");
            count++;
        }
        InputValidator.waitForEnter();
    }
}

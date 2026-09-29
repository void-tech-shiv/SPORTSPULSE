package sportspulse.cli;

import sportspulse.dao.PlayerDAO;
import sportspulse.models.Player;
import sportspulse.utils.ConsoleUtils;
import sportspulse.utils.InputValidator;
import sportspulse.algorithms.StringAlgorithms;

import java.util.List;
import java.util.Optional;

public class PlayerMenu {
    private PlayerDAO playerDAO;

    public PlayerMenu() {
        this.playerDAO = new PlayerDAO();
    }

    public void display() {
        boolean running = true;
        while (running) {
            ConsoleUtils.clearScreen();
            ConsoleUtils.printHeader("PLAYER MANAGEMENT");
            System.out.println("1. Add Player");
            System.out.println("2. View All Players");
            System.out.println("3. Search Player");
            System.out.println("4. Update Player");
            System.out.println("5. Delete Player");
            System.out.println("6. Compare Players");
            System.out.println("7. Rank Players");
            System.out.println("8. View Player Performance");
            System.out.println("9. Back to Main Menu");
            
            int choice = InputValidator.getIntInput("\nEnter choice: ");
            switch (choice) {
                case 1:
                    addPlayer();
                    break;
                case 2:
                    viewAllPlayers();
                    break;
                case 3:
                    searchPlayer();
                    break;
                case 4:
                    updatePlayer();
                    break;
                case 5:
                    deletePlayer();
                    break;
                case 6:
                case 7:
                case 8:
                    System.out.println("Feature not fully implemented in this phase.");
                    InputValidator.waitForEnter();
                    break;
                case 9:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    InputValidator.waitForEnter();
            }
        }
    }

    private void addPlayer() {
        System.out.println("\n--- Add Player ---");
        String id = InputValidator.getStringInput("Enter player ID: ");
        if (playerDAO.getPlayerById(id).isPresent()) {
            System.out.println("Player with this ID already exists.");
            InputValidator.waitForEnter();
            return;
        }

        String name = InputValidator.getStringInput("Enter player name: ");
        String teamId = InputValidator.getStringInput("Enter team ID: ");
        String position = InputValidator.getStringInput("Enter position: ");
        int age = InputValidator.getIntInput("Enter age: ");
        
        Player newPlayer = new Player(id, name, teamId, position, age, 0, 0, 0, 0, 0, 0.0, 0.0);
        playerDAO.addPlayer(newPlayer);
        System.out.println("Player added successfully!");
        InputValidator.waitForEnter();
    }

    private void viewAllPlayers() {
        System.out.println("\n--- All Players ---");
        List<Player> players = playerDAO.getAllPlayers();
        if (players.isEmpty()) {
            System.out.println("No players found.");
        } else {
            for (Player p : players) {
                System.out.println(p);
            }
        }
        InputValidator.waitForEnter();
    }

    private void searchPlayer() {
        System.out.println("\n--- Search Player (Using KMP Algorithm) ---");
        String query = InputValidator.getStringInput("Enter player name or position to search: ").toLowerCase();
        System.out.println("Searching for '" + query + "'...");

        List<Player> players = playerDAO.getAllPlayers();
        boolean found = false;
        
        for (Player p : players) {
            String searchString = (p.getName() + " " + p.getPosition() + " " + p.getTeamId()).toLowerCase();
            List<Integer> matches = StringAlgorithms.kmpSearch(searchString, query);
            if (!matches.isEmpty()) {
                System.out.println("- Found match: " + p);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No players found matching '" + query + "'.");
        }
        InputValidator.waitForEnter();
    }
    
    private void updatePlayer() {
        System.out.println("\n--- Update Player ---");
        String id = InputValidator.getStringInput("Enter player ID to update: ");
        Optional<Player> optionalPlayer = playerDAO.getPlayerById(id);
        
        if (optionalPlayer.isEmpty()) {
            System.out.println("Player not found.");
            InputValidator.waitForEnter();
            return;
        }
        
        Player p = optionalPlayer.get();
        System.out.println("Current details: " + p);
        
        String name = InputValidator.getStringInput("Enter new name (or press enter to keep current): ");
        if (!name.trim().isEmpty()) p.setName(name);
        
        String position = InputValidator.getStringInput("Enter new position (or press enter to keep current): ");
        if (!position.trim().isEmpty()) p.setPosition(position);
        
        playerDAO.updatePlayer(p);
        System.out.println("Player updated successfully!");
        InputValidator.waitForEnter();
    }
    
    private void deletePlayer() {
        System.out.println("\n--- Delete Player ---");
        String id = InputValidator.getStringInput("Enter player ID to delete: ");
        if (playerDAO.getPlayerById(id).isEmpty()) {
            System.out.println("Player not found.");
            InputValidator.waitForEnter();
            return;
        }
        
        playerDAO.deletePlayer(id);
        System.out.println("Player deleted successfully!");
        InputValidator.waitForEnter();
    }
}

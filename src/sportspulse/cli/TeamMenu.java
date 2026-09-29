package sportspulse.cli;

import sportspulse.dao.TeamDAO;
import sportspulse.models.Team;
import sportspulse.utils.ConsoleUtils;
import sportspulse.utils.InputValidator;

import java.util.List;
import java.util.Optional;

public class TeamMenu {
    private TeamDAO teamDAO;

    public TeamMenu() {
        this.teamDAO = new TeamDAO();
    }

    public void display() {
        boolean running = true;
        while (running) {
            ConsoleUtils.clearScreen();
            ConsoleUtils.printHeader("TEAM MANAGEMENT");
            System.out.println("1. Add Team");
            System.out.println("2. View All Teams");
            System.out.println("3. Search Team");
            System.out.println("4. Update Team");
            System.out.println("5. Delete Team");
            System.out.println("6. Back to Main Menu");
            
            int choice = InputValidator.getIntInput("\nEnter choice: ");
            switch (choice) {
                case 1:
                    addTeam();
                    break;
                case 2:
                    viewAllTeams();
                    break;
                case 3:
                    searchTeam();
                    break;
                case 4:
                    updateTeam();
                    break;
                case 5:
                    deleteTeam();
                    break;
                case 6:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    InputValidator.waitForEnter();
            }
        }
    }

    private void addTeam() {
        System.out.println("\n--- Add Team ---");
        String id = InputValidator.getStringInput("Enter team ID: ");
        if (teamDAO.getTeamById(id).isPresent()) {
            System.out.println("Team with this ID already exists.");
            InputValidator.waitForEnter();
            return;
        }

        String name = InputValidator.getStringInput("Enter team name: ");
        String sport = InputValidator.getStringInput("Enter sport: ");
        String coach = InputValidator.getStringInput("Enter coach name: ");
        
        Team newTeam = new Team(id, name, sport, coach);
        teamDAO.addTeam(newTeam);
        System.out.println("Team added successfully!");
        InputValidator.waitForEnter();
    }

    private void viewAllTeams() {
        System.out.println("\n--- All Teams ---");
        List<Team> teams = teamDAO.getAllTeams();
        if (teams.isEmpty()) {
            System.out.println("No teams found.");
        } else {
            for (Team t : teams) {
                System.out.println(t);
            }
        }
        InputValidator.waitForEnter();
    }

    private void searchTeam() {
        System.out.println("\n--- Search Team ---");
        String query = InputValidator.getStringInput("Enter team name to search: ").toLowerCase();
        
        List<Team> teams = teamDAO.getAllTeams();
        boolean found = false;
        
        for (Team t : teams) {
            if (t.getName().toLowerCase().contains(query)) {
                System.out.println("- Found match: " + t);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No teams found matching '" + query + "'.");
        }
        InputValidator.waitForEnter();
    }
    
    private void updateTeam() {
        System.out.println("\n--- Update Team ---");
        String id = InputValidator.getStringInput("Enter team ID to update: ");
        Optional<Team> optionalTeam = teamDAO.getTeamById(id);
        
        if (optionalTeam.isEmpty()) {
            System.out.println("Team not found.");
            InputValidator.waitForEnter();
            return;
        }
        
        Team t = optionalTeam.get();
        System.out.println("Current details: " + t);
        
        String name = InputValidator.getStringInput("Enter new name (or press enter to keep current): ");
        if (!name.trim().isEmpty()) t.setName(name);
        
        String coach = InputValidator.getStringInput("Enter new coach (or press enter to keep current): ");
        if (!coach.trim().isEmpty()) t.setCoach(coach);
        
        teamDAO.updateTeam(t);
        System.out.println("Team updated successfully!");
        InputValidator.waitForEnter();
    }
    
    private void deleteTeam() {
        System.out.println("\n--- Delete Team ---");
        String id = InputValidator.getStringInput("Enter team ID to delete: ");
        if (teamDAO.getTeamById(id).isEmpty()) {
            System.out.println("Team not found.");
            InputValidator.waitForEnter();
            return;
        }
        
        teamDAO.deleteTeam(id);
        System.out.println("Team deleted successfully!");
        InputValidator.waitForEnter();
    }
}

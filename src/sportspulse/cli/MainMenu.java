package sportspulse.cli;

import sportspulse.utils.ConsoleUtils;
import sportspulse.utils.InputValidator;

public class MainMenu {
    private PlayerMenu playerMenu = new PlayerMenu();
    private TeamMenu teamMenu = new TeamMenu();
    private MatchMenu matchMenu = new MatchMenu();
    private AnalyticsMenu analyticsMenu = new AnalyticsMenu();
    private AlgorithmMenu algorithmMenu = new AlgorithmMenu();

    public void start() {
        boolean running = true;
        
        while (running) {
            ConsoleUtils.clearScreen();
            ConsoleUtils.printHeader("SPORTSPULSE\nSports Performance Analytics");
            
            System.out.println("1. Player Management");
            System.out.println("2. Team Management");
            System.out.println("3. Competition Management");
            System.out.println("4. Match Management");
            System.out.println("5. Match Events");
            System.out.println("6. String Algorithms");
            System.out.println("7. Suffix Structures");
            System.out.println("8. Dynamic Programming");
            System.out.println("9. Network Flow");
            System.out.println("10. NP-Completeness & Approximation");
            System.out.println("11. Randomized Algorithms");
            System.out.println("12. Parallel Algorithms");
            System.out.println("13. Performance Analytics");
            System.out.println("14. Player Comparison");
            System.out.println("15. Search Reports");
            System.out.println("16. Exit");
            
            int choice = InputValidator.getIntInput("\nEnter choice: ");
            
            switch (choice) {
                case 1:
                    playerMenu.display();
                    break;
                case 2:
                    teamMenu.display();
                    break;
                case 3:
                case 5:
                    System.out.println("Feature not implemented in Phase 1.");
                    InputValidator.waitForEnter();
                    break;
                case 4:
                    matchMenu.display();
                    break;
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                    algorithmMenu.display();
                    break;
                case 13:
                case 14:
                case 15:
                    analyticsMenu.display();
                    break;
                case 16:
                    System.out.println("Exiting SportsPulse. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select a number between 1 and 16.");
                    InputValidator.waitForEnter();
            }
        }
    }
}

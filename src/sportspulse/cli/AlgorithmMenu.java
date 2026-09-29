package sportspulse.cli;

import sportspulse.utils.ConsoleUtils;
import sportspulse.utils.InputValidator;
import sportspulse.algorithms.StringAlgorithms;
import sportspulse.algorithms.SuffixStructures;
import sportspulse.algorithms.DynamicProgramming;
import sportspulse.algorithms.NetworkFlow;
import sportspulse.algorithms.NPCompleteness;
import sportspulse.algorithms.Heuristics;
import sportspulse.models.MatchReport;
import sportspulse.models.Player;
import sportspulse.utils.PerformanceTimer;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class AlgorithmMenu {
    public void display() {
        boolean running = true;
        while (running) {
            ConsoleUtils.clearScreen();
            ConsoleUtils.printHeader("ALGORITHM DEMONSTRATION");
            System.out.println("1. M1 String Algorithms");
            System.out.println("2. M2 Suffix Structures");
            System.out.println("3. M3 Dynamic Programming");
            System.out.println("4. M4 Network Flow");
            System.out.println("5. M5 NP-Completeness");
            System.out.println("6. M6 Randomized & Parallel");
            System.out.println("7. Back to Main Menu");
            
            int choice = InputValidator.getIntInput("\nEnter choice: ");
            switch (choice) {
                case 1:
                    demonstrateStringAlgorithms();
                    break;
                case 2:
                    demonstrateSuffixStructures();
                    break;
                case 3:
                    demonstrateDynamicProgramming();
                    break;
                case 4:
                    demonstrateNetworkFlow();
                    break;
                case 5:
                    demonstrateNPCompleteness();
                    break;
                case 6:
                    demonstrateHeuristics();
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

    private void demonstrateStringAlgorithms() {
        System.out.println("\n--- M1: String Algorithms Demonstration ---");
        String text = "Lionel Messi passes to Kylian Mbappe, Mbappe shoots... Goal! What a goal by Mbappe!";
        System.out.println("Text: " + text);
        String pattern = InputValidator.getStringInput("Enter a word to search (e.g., 'Mbappe', 'Goal'): ");

        System.out.println("\n1. KMP Algorithm");
        PerformanceTimer timer = new PerformanceTimer();
        timer.start();
        List<Integer> kmpMatches = StringAlgorithms.kmpSearch(text, pattern);
        timer.stop();
        System.out.println("Matches found at indices: " + kmpMatches);

        System.out.println("\n2. Rabin-Karp Algorithm");
        timer.start();
        List<Integer> rkMatches = StringAlgorithms.rabinKarpSearch(text, pattern);
        timer.stop();
        System.out.println("Matches found at indices: " + rkMatches);

        InputValidator.waitForEnter();
    }

    private void demonstrateSuffixStructures() {
        System.out.println("\n--- M2: Suffix Structures Demonstration ---");
        
        // Mock a MatchReport
        String reportContent = "The match started with high intensity. Messi passes to Mbappe, Mbappe scores a brilliant goal! The crowd goes wild. Later, Messi passes to Neymar, but Neymar misses the goal. The match ends in a draw.";
        MatchReport report = new MatchReport("M001", "Admin", reportContent);
        System.out.println("Match Report: " + report.getContent());
        
        PerformanceTimer timer = new PerformanceTimer();
        
        // Build Suffix Array
        System.out.println("\n1. Building Suffix Array...");
        timer.start();
        int[] suffixArray = SuffixStructures.buildSuffixArray(report.getContent().toLowerCase());
        timer.stop();
        System.out.println("Suffix Array built successfully.");

        // Build LCP Array
        System.out.println("\n2. Building LCP (Longest Common Prefix) Array...");
        timer.start();
        int[] lcpArray = SuffixStructures.buildLCPArray(report.getContent().toLowerCase(), suffixArray);
        timer.stop();
        System.out.println("LCP Array built successfully.");

        // Report Indexing (Search)
        System.out.println("\n3. Report Indexing (Suffix Array Search)");
        String pattern = InputValidator.getStringInput("Enter a word to search in the report (e.g., 'messi', 'goal'): ");
        timer.start();
        List<Integer> matches = SuffixStructures.search(report.getContent().toLowerCase(), pattern.toLowerCase(), suffixArray);
        timer.stop();
        System.out.println("Matches found at indices: " + matches);

        // Report Similarity (LCP Application - Find Longest Repeated Substring)
        System.out.println("\n4. Report Similarity (Finding Longest Repeated Substring using LCP)");
        int maxLcp = 0;
        int maxIndex = -1;
        for (int i = 0; i < lcpArray.length; i++) {
            if (lcpArray[i] > maxLcp) {
                maxLcp = lcpArray[i];
                maxIndex = i;
            }
        }
        
        if (maxIndex != -1 && maxLcp > 0) {
            int startIdx = suffixArray[maxIndex];
            String repeatedSubstring = report.getContent().substring(startIdx, startIdx + maxLcp);
            System.out.println("Longest repeated substring in report: '" + repeatedSubstring + "' (Length: " + maxLcp + ")");
        } else {
            System.out.println("No repeated substrings found.");
        }

        InputValidator.waitForEnter();
    }

    private void demonstrateDynamicProgramming() {
        System.out.println("\n--- M3: Dynamic Programming Demonstration ---");
        PerformanceTimer timer = new PerformanceTimer();

        // 1. Longest Increasing Subsequence (Player Form)
        System.out.println("\n1. Player Form Sequence (Longest Increasing Subsequence)");
        int[] scores = {10, 22, 9, 33, 21, 50, 41, 60};
        System.out.println("Player's recent match scores: " + Arrays.toString(scores));
        
        timer.start();
        List<Integer> lis = DynamicProgramming.longestIncreasingSubsequence(scores);
        timer.stop();
        System.out.println("Longest Form Sequence (Increasing Scores): " + lis);
        System.out.println("Max continuous improvement length: " + lis.size());

        // 2. Longest Common Subsequence (Match Event Similarity)
        System.out.println("\n2. Match Event Similarity (Longest Common Subsequence)");
        String[] match1Events = {"Pass", "Dribble", "Shoot", "Save", "Corner", "Goal"};
        String[] match2Events = {"Tackle", "Pass", "Shoot", "Foul", "Corner", "Goal"};
        System.out.println("Match 1 Events: " + Arrays.toString(match1Events));
        System.out.println("Match 2 Events: " + Arrays.toString(match2Events));
        
        timer.start();
        List<String> lcs = DynamicProgramming.longestCommonSubsequence(match1Events, match2Events);
        timer.stop();
        System.out.println("Longest Common Event Sequence: " + lcs);
        System.out.println("Similarity Score (Length): " + lcs.size());

        // 3. 0/1 Knapsack (Fantasy Team Selection)
        System.out.println("\n3. Fantasy Team Selection (0/1 Knapsack)");
        List<Player> availablePlayers = new ArrayList<>();
        // ID, Name, Team, Position, Age, Matches, Goals, Assists, Wickets, Runs, Fitness (Cost), Performance (Points)
        availablePlayers.add(new Player("P1", "Messi", "T1", "FWD", 35, 10, 8, 5, 0, 0, 30.0, 95.0)); // Cost 30, Value 95
        availablePlayers.add(new Player("P2", "Ronaldo", "T2", "FWD", 38, 10, 7, 2, 0, 0, 25.0, 85.0)); // Cost 25, Value 85
        availablePlayers.add(new Player("P3", "Neymar", "T1", "FWD", 31, 10, 5, 6, 0, 0, 20.0, 75.0)); // Cost 20, Value 75
        availablePlayers.add(new Player("P4", "De Bruyne", "T3", "MID", 32, 10, 2, 10, 0, 0, 15.0, 80.0)); // Cost 15, Value 80
        availablePlayers.add(new Player("P5", "Van Dijk", "T4", "DEF", 32, 10, 1, 0, 0, 0, 10.0, 60.0)); // Cost 10, Value 60

        System.out.println("Available Players:");
        for (Player p : availablePlayers) {
            System.out.println("- " + p.getName() + " (Cost/Fitness: " + (int)p.getFitnessScore() + ", Expected Points: " + (int)p.getPerformanceScore() + ")");
        }
        
        int budget = InputValidator.getIntInput("Enter total budget for fantasy team (e.g., 50): ");
        timer.start();
        List<Player> selectedTeam = DynamicProgramming.knapsack(availablePlayers, budget);
        timer.stop();
        
        System.out.println("\nSelected Fantasy Team (Maximized Points):");
        int totalCost = 0;
        int totalPoints = 0;
        for (Player p : selectedTeam) {
            System.out.println("- " + p.getName());
            totalCost += (int) p.getFitnessScore();
            totalPoints += (int) p.getPerformanceScore();
        }
        System.out.println("Total Cost: " + totalCost + " / " + budget);
        System.out.println("Total Projected Points: " + totalPoints);

        InputValidator.waitForEnter();
    }

    private void demonstrateNetworkFlow() {
        System.out.println("\n--- M4: Network Flow Demonstration ---");
        PerformanceTimer timer = new PerformanceTimer();

        // 1. Max Flow (Resource Allocation)
        System.out.println("\n1. Resource Allocation (Dinic's Max Flow)");
        System.out.println("Modeling resource flow from HQ (Source) through intermediaries to Departments (Sink).");
        // Create a simple flow network (6 nodes: 0=Source, 5=Sink)
        int[][] capacity = new int[6][6];
        capacity[0][1] = 16;
        capacity[0][2] = 13;
        capacity[1][2] = 10;
        capacity[1][3] = 12;
        capacity[2][1] = 4;
        capacity[2][4] = 14;
        capacity[3][2] = 9;
        capacity[3][5] = 20;
        capacity[4][3] = 7;
        capacity[4][5] = 4;

        timer.start();
        int maxFlow = NetworkFlow.maxFlowDinic(capacity, 0, 5);
        timer.stop();
        
        System.out.println("Maximum resources that can be allocated: " + maxFlow);

        // 2. Bipartite Matching (Player-Position Assignment)
        System.out.println("\n2. Player-Position Assignment (Bipartite Matching)");
        System.out.println("Given 4 players and 4 positions, finding optimal valid assignment.");
        
        // 4 players, 4 positions
        boolean[][] bipartiteGraph = new boolean[][] {
            {true, true, false, false}, // Player 0 can play Pos 0, 1
            {false, true, true, false}, // Player 1 can play Pos 1, 2
            {false, false, true, true}, // Player 2 can play Pos 2, 3
            {true, false, false, true}  // Player 3 can play Pos 0, 3
        };
        
        timer.start();
        int[] assignments = NetworkFlow.bipartiteMatching(bipartiteGraph);
        timer.stop();
        
        int matchCount = 0;
        for (int pos = 0; pos < assignments.length; pos++) {
            if (assignments[pos] != -1) {
                System.out.println("Position " + pos + " assigned to Player " + assignments[pos]);
                matchCount++;
            } else {
                System.out.println("Position " + pos + " could not be filled.");
            }
        }
        System.out.println("Total successful assignments: " + matchCount);

        InputValidator.waitForEnter();
    }

    private void demonstrateNPCompleteness() {
        System.out.println("\n--- M5: NP-Completeness Demonstration ---");
        PerformanceTimer timer = new PerformanceTimer();

        // 1. Selection Constraints (3-SAT)
        System.out.println("\n1. Selection Constraints (3-SAT)");
        System.out.println("Resolving scheduling conflicts and roster constraints.");
        System.out.println("Constraints (Clauses):");
        System.out.println("1. Player 1 OR Player 2 OR Player 3 must play");
        System.out.println("2. NOT Player 1 OR NOT Player 2 OR Player 4 must play (Conflict between 1 & 2)");
        System.out.println("3. NOT Player 3 OR Player 4 OR NOT Player 1 must play");

        int numVariables = 4;
        int[][] clauses = {
            {1, 2, 3},
            {-1, -2, 4},
            {-3, 4, -1}
        };

        timer.start();
        boolean[] satAssignment = NPCompleteness.solve3SAT(numVariables, clauses);
        timer.stop();

        if (satAssignment != null) {
            System.out.println("Satisfiable Assignment Found:");
            for (int i = 1; i <= numVariables; i++) {
                System.out.println("Player " + i + ": " + (satAssignment[i] ? "Selected" : "Benched"));
            }
        } else {
            System.out.println("No satisfiable assignment exists. Constraints are conflicting.");
        }

        // 2. Conflict Analysis (Vertex Cover)
        System.out.println("\n2. Conflict Analysis (Vertex Cover)");
        System.out.println("Finding minimum security/camera coverage for stadium pathways.");
        System.out.println("Graph has 5 locations (0-4), edges represent pathways between them.");

        boolean[][] adjMatrix = {
            {false, true, true, false, false},
            {true, false, true, true, false},
            {true, true, false, true, true},
            {false, true, true, false, true},
            {false, false, true, true, false}
        };

        timer.start();
        List<Integer> exactCover = NPCompleteness.vertexCoverExact(adjMatrix);
        timer.stop();
        System.out.println("Exact Minimum Vertex Cover (Locations to secure): " + exactCover);

        timer.start();
        List<Integer> approxCover = NPCompleteness.vertexCoverApproximate(adjMatrix);
        timer.stop();
        System.out.println("Approximate Vertex Cover (2-Appx): " + approxCover);

        InputValidator.waitForEnter();
    }

    private void demonstrateHeuristics() {
        System.out.println("\n--- M6: Optimization & Heuristics Demonstration ---");
        PerformanceTimer timer = new PerformanceTimer();

        // 1. Roster Optimization (Simulated Annealing)
        System.out.println("\n1. Roster Optimization (Simulated Annealing)");
        System.out.println("Optimizing a 15-player roster selection out of 50 candidates.");
        
        int numPlayers = 50;
        int targetRosterSize = 15;
        double initialTemp = 10000;
        double coolingRate = 0.003;

        timer.start();
        boolean[] optimalRoster = Heuristics.simulatedAnnealing(numPlayers, initialTemp, coolingRate, targetRosterSize);
        timer.stop();

        double finalFitness = Heuristics.calculateRosterFitness(optimalRoster);
        System.out.println("Optimization Complete.");
        System.out.printf("Final Roster Fitness Score: %.2f\n", finalFitness);
        System.out.print("Selected Player IDs: ");
        for (int i = 0; i < numPlayers; i++) {
            if (optimalRoster[i]) {
                System.out.print(i + " ");
            }
        }
        System.out.println();

        // 2. Advanced Scheduling (Genetic Algorithm)
        System.out.println("\n2. Advanced Scheduling (Genetic Algorithm)");
        System.out.println("Finding an optimal season schedule to minimize travel penalty for 20 matches.");
        
        int numMatches = 20;
        int populationSize = 100;
        int generations = 500;
        double mutationRate = 0.05;

        timer.start();
        int[] optimalSchedule = Heuristics.geneticAlgorithm(numMatches, populationSize, generations, mutationRate);
        timer.stop();

        double scheduleFitness = Heuristics.calculateScheduleFitness(optimalSchedule);
        System.out.println("Evolution Complete.");
        System.out.printf("Final Schedule Fitness: %.2f\n", scheduleFitness);
        System.out.println("Optimal Sequence of Matches:");
        System.out.println(Arrays.toString(optimalSchedule));

        InputValidator.waitForEnter();
    }
}

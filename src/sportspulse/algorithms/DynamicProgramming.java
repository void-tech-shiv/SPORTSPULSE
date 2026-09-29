package sportspulse.algorithms;

import sportspulse.models.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DynamicProgramming {

    /**
     * Finds the Longest Increasing Subsequence (LIS) length and the sequence itself.
     * Time Complexity: O(N^2)
     * @param scores array of player match scores
     * @return A list containing the LIS
     */
    public static List<Integer> longestIncreasingSubsequence(int[] scores) {
        if (scores == null || scores.length == 0) return new ArrayList<>();

        int n = scores.length;
        int[] dp = new int[n];
        int[] parent = new int[n];
        
        int maxLength = 0;
        int endIndex = 0;

        for (int i = 0; i < n; i++) {
            dp[i] = 1;
            parent[i] = -1;
            for (int j = 0; j < i; j++) {
                if (scores[i] > scores[j] && dp[i] < dp[j] + 1) {
                    dp[i] = dp[j] + 1;
                    parent[i] = j;
                }
            }
            if (dp[i] > maxLength) {
                maxLength = dp[i];
                endIndex = i;
            }
        }

        // Reconstruct path
        List<Integer> lis = new ArrayList<>();
        int curr = endIndex;
        while (curr != -1) {
            lis.add(scores[curr]);
            curr = parent[curr];
        }
        Collections.reverse(lis);

        return lis;
    }

    /**
     * Finds the Longest Common Subsequence (LCS) of two string arrays (e.g., event sequences).
     * Time Complexity: O(M * N)
     * @param seq1 first sequence of events
     * @param seq2 second sequence of events
     * @return A list of strings representing the longest common subsequence
     */
    public static List<String> longestCommonSubsequence(String[] seq1, String[] seq2) {
        if (seq1 == null || seq2 == null) return new ArrayList<>();
        int m = seq1.length;
        int n = seq2.length;
        
        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (seq1[i - 1].equals(seq2[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        // Reconstruct LCS
        List<String> lcs = new ArrayList<>();
        int i = m, j = n;
        while (i > 0 && j > 0) {
            if (seq1[i - 1].equals(seq2[j - 1])) {
                lcs.add(seq1[i - 1]);
                i--;
                j--;
            } else if (dp[i - 1][j] > dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        Collections.reverse(lcs);

        return lcs;
    }

    /**
     * 0/1 Knapsack to select optimal players for a Fantasy Team.
     * Cost is derived from (int) player.getFitnessScore().
     * Value is derived from (int) player.getPerformanceScore().
     * Time Complexity: O(N * maxBudget)
     * @param players list of available players
     * @param maxBudget maximum allowed budget
     * @return List of selected players that maximize value within budget
     */
    public static List<Player> knapsack(List<Player> players, int maxBudget) {
        if (players == null || players.isEmpty() || maxBudget <= 0) {
            return new ArrayList<>();
        }

        int n = players.size();
        int[][] dp = new int[n + 1][maxBudget + 1];

        for (int i = 1; i <= n; i++) {
            Player p = players.get(i - 1);
            int weight = (int) p.getFitnessScore();
            int value = (int) p.getPerformanceScore();

            for (int w = 0; w <= maxBudget; w++) {
                if (weight <= w) {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - weight] + value);
                } else {
                    dp[i][w] = dp[i - 1][w];
                }
            }
        }

        // Reconstruct selected items
        List<Player> selectedPlayers = new ArrayList<>();
        int i = n;
        int w = maxBudget;
        while (i > 0 && w > 0) {
            if (dp[i][w] != dp[i - 1][w]) {
                Player p = players.get(i - 1);
                selectedPlayers.add(p);
                w -= (int) p.getFitnessScore();
            }
            i--;
        }

        return selectedPlayers;
    }
}

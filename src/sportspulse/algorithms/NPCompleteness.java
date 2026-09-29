package sportspulse.algorithms;

import java.util.*;

public class NPCompleteness {

    /**
     * Solves a 3-SAT problem using simple backtracking.
     * 
     * @param numVariables The number of boolean variables (1 to numVariables)
     * @param clauses A list of clauses, where each clause is an array of 3 integers. 
     *                Positive integer 'x' means x is true, negative integer '-x' means x is false.
     * @return A boolean array representing the assignment (index 1 to numVariables) if satisfiable, or null if unsatisfiable.
     */
    public static boolean[] solve3SAT(int numVariables, int[][] clauses) {
        boolean[] assignment = new boolean[numVariables + 1];
        if (backtrack3SAT(clauses, assignment, 1, numVariables)) {
            return assignment;
        }
        return null;
    }

    private static boolean backtrack3SAT(int[][] clauses, boolean[] assignment, int currentVar, int numVariables) {
        if (currentVar > numVariables) {
            // All variables assigned, check if all clauses are satisfied
            for (int[] clause : clauses) {
                if (!isClauseSatisfied(clause, assignment)) {
                    return false;
                }
            }
            return true; // Satisfied
        }

        // Try setting current variable to true
        assignment[currentVar] = true;
        if (backtrack3SAT(clauses, assignment, currentVar + 1, numVariables)) {
            return true;
        }

        // Try setting current variable to false
        assignment[currentVar] = false;
        if (backtrack3SAT(clauses, assignment, currentVar + 1, numVariables)) {
            return true;
        }

        return false;
    }

    private static boolean isClauseSatisfied(int[] clause, boolean[] assignment) {
        for (int literal : clause) {
            int var = Math.abs(literal);
            boolean isPositive = literal > 0;
            // If the literal matches the assignment, the clause is true
            if (assignment[var] == isPositive) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a minimum Vertex Cover exactly using backtracking.
     * Note: Since Vertex Cover is NP-Hard, this is only suitable for small graphs.
     * 
     * @param adjMatrix The adjacency matrix of the graph.
     * @return A list of vertices included in the minimum vertex cover.
     */
    public static List<Integer> vertexCoverExact(boolean[][] adjMatrix) {
        if (adjMatrix == null || adjMatrix.length == 0) return new ArrayList<>();
        int n = adjMatrix.length;
        
        List<Integer> bestCover = null;
        
        // Iterate through all possible subsets of vertices (2^n)
        int totalSubsets = 1 << n;
        for (int i = 0; i < totalSubsets; i++) {
            List<Integer> currentCover = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    currentCover.add(j);
                }
            }
            
            if (isVertexCover(adjMatrix, currentCover)) {
                if (bestCover == null || currentCover.size() < bestCover.size()) {
                    bestCover = currentCover;
                }
            }
        }
        
        return bestCover != null ? bestCover : new ArrayList<>();
    }

    /**
     * Finds an approximate Vertex Cover in O(V+E) time.
     * This provides a 2-approximation (the cover is at most twice the optimal size).
     * 
     * @param adjMatrix The adjacency matrix of the graph.
     * @return A list of vertices included in the approximate vertex cover.
     */
    public static List<Integer> vertexCoverApproximate(boolean[][] adjMatrix) {
        if (adjMatrix == null || adjMatrix.length == 0) return new ArrayList<>();
        int n = adjMatrix.length;
        
        List<Integer> cover = new ArrayList<>();
        boolean[] visited = new boolean[n];
        
        for (int u = 0; u < n; u++) {
            if (!visited[u]) {
                for (int v = 0; v < n; v++) {
                    if (adjMatrix[u][v] && !visited[v]) {
                        // Include both endpoints of the edge in the cover
                        visited[u] = true;
                        visited[v] = true;
                        cover.add(u);
                        cover.add(v);
                        break; // Move to the next unvisited vertex
                    }
                }
            }
        }
        
        return cover;
    }

    private static boolean isVertexCover(boolean[][] adjMatrix, List<Integer> cover) {
        int n = adjMatrix.length;
        boolean[] inCover = new boolean[n];
        for (int v : cover) {
            inCover[v] = true;
        }
        
        // Check if every edge has at least one endpoint in the cover
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (adjMatrix[i][j]) {
                    if (!inCover[i] && !inCover[j]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

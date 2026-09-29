package sportspulse.algorithms;

import java.util.*;

public class NetworkFlow {

    static class Edge {
        int v, flow, cap, rev;
        Edge(int v, int cap, int rev) {
            this.v = v;
            this.cap = cap;
            this.rev = rev;
        }
    }

    /**
     * Calculates the maximum flow using Dinic's Algorithm.
     * This is used for Resource Allocation modeling.
     * @param capacity The capacity matrix representing the flow network.
     * @param source The source node index.
     * @param sink The sink node index.
     * @return The maximum possible flow.
     */
    public static int maxFlowDinic(int[][] capacity, int source, int sink) {
        int n = capacity.length;
        List<List<Edge>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        
        // Build adjacency list from capacity matrix
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                if (capacity[u][v] > 0) {
                    addEdge(adj, u, v, capacity[u][v]);
                }
            }
        }
        
        int[] level = new int[n];
        int maxFlow = 0;
        
        while (bfs(adj, level, source, sink)) {
            int[] ptr = new int[n];
            while (true) {
                int pushed = dfs(adj, level, ptr, source, sink, Integer.MAX_VALUE);
                if (pushed == 0) break;
                maxFlow += pushed;
            }
        }
        
        return maxFlow;
    }
    
    private static void addEdge(List<List<Edge>> adj, int u, int v, int cap) {
        adj.get(u).add(new Edge(v, cap, adj.get(v).size()));
        adj.get(v).add(new Edge(u, 0, adj.get(u).size() - 1));
    }
    
    private static boolean bfs(List<List<Edge>> adj, int[] level, int source, int sink) {
        Arrays.fill(level, -1);
        level[source] = 0;
        Queue<Integer> q = new LinkedList<>();
        q.offer(source);
        
        while (!q.isEmpty()) {
            int u = q.poll();
            for (Edge e : adj.get(u)) {
                if (e.cap - e.flow > 0 && level[e.v] == -1) {
                    level[e.v] = level[u] + 1;
                    q.offer(e.v);
                }
            }
        }
        return level[sink] != -1;
    }
    
    private static int dfs(List<List<Edge>> adj, int[] level, int[] ptr, int u, int sink, int pushed) {
        if (pushed == 0) return 0;
        if (u == sink) return pushed;
        
        for (int cid = ptr[u]; cid < adj.get(u).size(); ++cid) {
            ptr[u] = cid;
            Edge e = adj.get(u).get(cid);
            if (level[u] + 1 != level[e.v] || e.cap - e.flow == 0) continue;
            
            int tr = dfs(adj, level, ptr, e.v, sink, Math.min(pushed, e.cap - e.flow));
            if (tr == 0) continue;
            
            e.flow += tr;
            adj.get(e.v).get(e.rev).flow -= tr;
            return tr;
        }
        return 0;
    }

    /**
     * Bipartite Matching for Player-Position Assignment.
     * Given a boolean matrix where graph[player][position] is true if the player can play the position.
     * 
     * @param bipartiteGraph the bipartite graph connectivity matrix
     * @return An array of size numPositions where array[i] is the player assigned to position i, or -1 if unassigned.
     */
    public static int[] bipartiteMatching(boolean[][] bipartiteGraph) {
        if (bipartiteGraph == null || bipartiteGraph.length == 0) return new int[0];
        int numPlayers = bipartiteGraph.length;
        int numPositions = bipartiteGraph[0].length;
        
        int[] positionMatch = new int[numPositions];
        Arrays.fill(positionMatch, -1);
        
        for (int u = 0; u < numPlayers; u++) {
            boolean[] visited = new boolean[numPositions];
            bipartiteMatchDfs(bipartiteGraph, u, visited, positionMatch);
        }
        return positionMatch;
    }
    
    private static boolean bipartiteMatchDfs(boolean[][] graph, int u, boolean[] visited, int[] positionMatch) {
        for (int v = 0; v < graph[0].length; v++) {
            if (graph[u][v] && !visited[v]) {
                visited[v] = true;
                if (positionMatch[v] < 0 || bipartiteMatchDfs(graph, positionMatch[v], visited, positionMatch)) {
                    positionMatch[v] = u;
                    return true;
                }
            }
        }
        return false;
    }
}

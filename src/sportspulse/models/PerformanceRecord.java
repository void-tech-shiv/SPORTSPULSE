package sportspulse.models;

public class PerformanceRecord {
    private String playerId;
    private String matchId;
    private double score;
    private double fitness;
    private int goals;
    private int assists;
    private int wickets;
    private int runs;

    public PerformanceRecord(String playerId, String matchId, double score, double fitness, int goals, int assists, int wickets, int runs) {
        this.playerId = playerId;
        this.matchId = matchId;
        this.score = score;
        this.fitness = fitness;
        this.goals = goals;
        this.assists = assists;
        this.wickets = wickets;
        this.runs = runs;
    }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getFitness() { return fitness; }
    public void setFitness(double fitness) { this.fitness = fitness; }

    public int getGoals() { return goals; }
    public void setGoals(int goals) { this.goals = goals; }

    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }

    public int getWickets() { return wickets; }
    public void setWickets(int wickets) { this.wickets = wickets; }

    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }

    @Override
    public String toString() {
        return "PerformanceRecord{" +
                "playerId='" + playerId + '\'' +
                ", matchId='" + matchId + '\'' +
                ", score=" + score +
                ", fitness=" + fitness +
                ", goals=" + goals +
                ", assists=" + assists +
                ", wickets=" + wickets +
                ", runs=" + runs +
                '}';
    }
}

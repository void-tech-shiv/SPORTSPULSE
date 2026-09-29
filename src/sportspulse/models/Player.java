package sportspulse.models;

public class Player {
    private String id;
    private String name;
    private String teamId;
    private String position;
    private int age;
    private int matchesPlayed;
    private int goals;
    private int assists;
    private int wickets;
    private int runs;
    private double fitnessScore;
    private double performanceScore;

    public Player(String id, String name, String teamId, String position, int age, int matchesPlayed, 
                  int goals, int assists, int wickets, int runs, double fitnessScore, double performanceScore) {
        this.id = id;
        this.name = name;
        this.teamId = teamId;
        this.position = position;
        this.age = age;
        this.matchesPlayed = matchesPlayed;
        this.goals = goals;
        this.assists = assists;
        this.wickets = wickets;
        this.runs = runs;
        this.fitnessScore = fitnessScore;
        this.performanceScore = performanceScore;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public int getMatchesPlayed() { return matchesPlayed; }
    public void setMatchesPlayed(int matchesPlayed) { this.matchesPlayed = matchesPlayed; }

    public int getGoals() { return goals; }
    public void setGoals(int goals) { this.goals = goals; }

    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }

    public int getWickets() { return wickets; }
    public void setWickets(int wickets) { this.wickets = wickets; }

    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }

    public double getFitnessScore() { return fitnessScore; }
    public void setFitnessScore(double fitnessScore) { this.fitnessScore = fitnessScore; }

    public double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(double performanceScore) { this.performanceScore = performanceScore; }

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", teamId='" + teamId + '\'' +
                ", position='" + position + '\'' +
                ", age=" + age +
                ", matchesPlayed=" + matchesPlayed +
                ", goals=" + goals +
                ", assists=" + assists +
                ", wickets=" + wickets +
                ", runs=" + runs +
                ", fitnessScore=" + fitnessScore +
                ", performanceScore=" + performanceScore +
                '}';
    }
}

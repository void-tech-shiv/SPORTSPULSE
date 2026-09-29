package sportspulse.models;

public class Match {
    private String id;
    private String competitionId;
    private String teamAId;
    private String teamBId;
    private String date;
    private String venue;
    private String score;
    private String status;

    public Match(String id, String competitionId, String teamAId, String teamBId, String date, String venue, String score, String status) {
        this.id = id;
        this.competitionId = competitionId;
        this.teamAId = teamAId;
        this.teamBId = teamBId;
        this.date = date;
        this.venue = venue;
        this.score = score;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCompetitionId() { return competitionId; }
    public void setCompetitionId(String competitionId) { this.competitionId = competitionId; }

    public String getTeamAId() { return teamAId; }
    public void setTeamAId(String teamAId) { this.teamAId = teamAId; }

    public String getTeamBId() { return teamBId; }
    public void setTeamBId(String teamBId) { this.teamBId = teamBId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Match{" +
                "id='" + id + '\'' +
                ", competitionId='" + competitionId + '\'' +
                ", teamAId='" + teamAId + '\'' +
                ", teamBId='" + teamBId + '\'' +
                ", date='" + date + '\'' +
                ", venue='" + venue + '\'' +
                ", score='" + score + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

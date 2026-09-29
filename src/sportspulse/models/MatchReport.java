package sportspulse.models;

public class MatchReport {
    private String matchId;
    private String author;
    private String content;

    public MatchReport(String matchId, String author, String content) {
        this.matchId = matchId;
        this.author = author;
        this.content = content;
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Match Report [" + matchId + "] by " + author + "\n" + content;
    }
}

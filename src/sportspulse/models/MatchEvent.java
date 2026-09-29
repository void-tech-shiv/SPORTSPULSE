package sportspulse.models;

public class MatchEvent {
    private String matchId;
    private String timestamp;
    private String playerId;
    private String eventType;
    private String description;

    public MatchEvent(String matchId, String timestamp, String playerId, String eventType, String description) {
        this.matchId = matchId;
        this.timestamp = timestamp;
        this.playerId = playerId;
        this.eventType = eventType;
        this.description = description;
    }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "MatchEvent{" +
                "matchId='" + matchId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", playerId='" + playerId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

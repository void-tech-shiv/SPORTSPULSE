package sportspulse.models;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private String id;
    private String name;
    private String sport;
    private String coach;
    private List<String> playerIds;

    public Team(String id, String name, String sport, String coach) {
        this.id = id;
        this.name = name;
        this.sport = sport;
        this.coach = coach;
        this.playerIds = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    public String getCoach() { return coach; }
    public void setCoach(String coach) { this.coach = coach; }

    public List<String> getPlayerIds() { return playerIds; }
    
    public void addPlayer(String playerId) {
        if (!this.playerIds.contains(playerId)) {
            this.playerIds.add(playerId);
        }
    }
    
    public void removePlayer(String playerId) {
        this.playerIds.remove(playerId);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", sport='" + sport + '\'' +
                ", coach='" + coach + '\'' +
                ", playersCount=" + playerIds.size() +
                '}';
    }
}

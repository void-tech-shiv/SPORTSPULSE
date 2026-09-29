package sportspulse;

import sportspulse.dao.PlayerDAO;
import sportspulse.dao.TeamDAO;
import sportspulse.dao.MatchDAO;
import sportspulse.models.Player;
import sportspulse.models.Team;
import sportspulse.models.Match;

public class TestApp {
    public static void main(String[] args) {
        System.out.println("Starting DAO tests...");
        try {
            // Test PlayerDAO
            PlayerDAO playerDAO = new PlayerDAO();
            Player p1 = new Player("p1", "Test Player", "t1", "Forward", 25, 10, 5, 2, 0, 0, 9.5, 8.5);
            playerDAO.addPlayer(p1);
            System.out.println("Player added. Count: " + playerDAO.getAllPlayers().size());
            
            p1.setName("Updated Player");
            playerDAO.updatePlayer(p1);
            System.out.println("Player updated. Name: " + playerDAO.getPlayerById("p1").get().getName());
            
            playerDAO.deletePlayer("p1");
            System.out.println("Player deleted. Count: " + playerDAO.getAllPlayers().size());

            // Test TeamDAO
            TeamDAO teamDAO = new TeamDAO();
            Team t1 = new Team("t1", "Test Team", "Soccer", "Coach Smith");
            t1.addPlayer("p1");
            teamDAO.addTeam(t1);
            System.out.println("Team added. Count: " + teamDAO.getAllTeams().size());
            
            t1.setName("Updated Team");
            teamDAO.updateTeam(t1);
            System.out.println("Team updated. Name: " + teamDAO.getTeamById("t1").get().getName());
            
            teamDAO.deleteTeam("t1");
            System.out.println("Team deleted. Count: " + teamDAO.getAllTeams().size());

            // Test MatchDAO
            MatchDAO matchDAO = new MatchDAO();
            Match m1 = new Match("m1", "c1", "t1", "t2", "2024-01-01", "Stadium", "1-0", "Completed");
            matchDAO.addMatch(m1);
            System.out.println("Match added. Count: " + matchDAO.getAllMatches().size());
            
            m1.setScore("2-1");
            matchDAO.updateMatch(m1);
            System.out.println("Match updated. Score: " + matchDAO.getMatchById("m1").get().getScore());
            
            matchDAO.deleteMatch("m1");
            System.out.println("Match deleted. Count: " + matchDAO.getAllMatches().size());

            System.out.println("All DAO tests completed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

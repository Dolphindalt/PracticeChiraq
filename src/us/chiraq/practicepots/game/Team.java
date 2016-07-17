package us.chiraq.practicepots.game;

import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.game.fight.TeamDuel;
import us.chiraq.practicepots.profile.Profile;
import us.chiraq.practicepots.profile.ProfileManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class Team {
    private static List<Team> teams = new ArrayList<Team>();
    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();
    private ProfileManager pm = this.main.getProfileManager();
    private Player leader;
    private List<Player> members;
    private List<Player> invitedPlayers;
    private Set<Profile> profiles;
    private BukkitTask task;
    private boolean inFight;
    private Team opponent;
    private TeamDuel duel;
    private Map<Team, Ladder> challenges;
    private Team challengingTeam;

    public Team(final Player leader) {
        this.leader = leader;
        this.members = new ArrayList<Player>();
        this.invitedPlayers = new ArrayList<Player>();
        this.profiles = new HashSet<Profile>();
        this.challenges = new HashMap<Team, Ladder>();
        this.addPlayer(leader);

        Team.getTeams().add(this);
    }

    public void sendMessage(String message) {
        for (Player player : this.members) {
            player.sendMessage(message);
        }
    }

    /*public Set<PlayerScoreboard> getScoreboards() {
        HashSet<PlayerScoreboard> toReturn = new HashSet<PlayerScoreboard>();
        if (this.members != null) {
            for (Player player : this.members) {
                toReturn.add(PlayerScoreboard.getScoreboard(player));
            }
        }
        return toReturn;
    }*/

    public void delete() {
        Team team = this;
        for (Profile teamProf : team.getProfiles()) {
            teamProf.setTeam(null);
        }
        if (this.duel != null) {
            if (this.duel.getTask() != null) {
                this.duel.getTask().cancel();
            }
            this.duel.setWinner(team.getOpponent());
        }
        for (Player teamPlayer : team.getMembers()) {
            //this.resetScoreboard(teamPlayer);
            this.pm.sendToSpawn(teamPlayer);
        }
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        this.members = null;
        this.leader = null;
        this.profiles = null;
        Team.getTeams().remove(this);
    }

    /*public void resetScoreboard(Player player) {
        PlayerScoreboard playerScoreboard = PlayerScoreboard.getScoreboard(player);
        List<String> toLoop = this.lf.getStringList("SCOREBOARD.TEAM_FIGHT_INFORMATION");
        toLoop.addAll(this.lf.getStringList("SCOREBOARD.TEAM_INFORMATION"));
        for (String string : toLoop) {
            Entry entry = playerScoreboard.getEntry(string);
            if (entry == null) continue;
            entry.setCancelled(true);
        }
        if (playerScoreboard.getEntry("enderpearl") != null) {
            playerScoreboard.getEntry("enderpearl").setCancelled(true);
        }
    }*/

    public void removePlayer(Player player) {
        if (this.members.contains((Object)player) && !player.getName().equalsIgnoreCase(this.leader.getName())) {
            this.members.remove((Object)player);
            Profile profile = Profile.getProfile(player.getUniqueId());
            profile.setTeam(null);
            this.profiles.remove(profile);
            //this.resetScoreboard(player);
            this.pm.sendToSpawn(player);
        }
    }

    public void addPlayer(Player player) {
        this.members.add(player);
        Profile profile = Profile.getProfile(player.getUniqueId());
        profile.setTeam(this);
        this.profiles.add(profile);
        this.pm.giveTeamItems(player);
    }

    public static Team getTeam(String name) {
        for (Team team : Team.getTeams()) {
            if (!team.getLeader().getName().equalsIgnoreCase(name)) continue;
            return team;
        }
        return null;
    }

    public static List<Team> getTeams() {
        return teams;
    }

    public Nanny getMain() {
        return this.main;
    }

    public LangFile getLf() {
        return this.lf;
    }

    public ProfileManager getPm() {
        return this.pm;
    }

    public Player getLeader() {
        return this.leader;
    }

    public List<Player> getMembers() {
        return this.members;
    }

    public List<Player> getInvitedPlayers() {
        return this.invitedPlayers;
    }

    public Set<Profile> getProfiles() {
        return this.profiles;
    }

    public BukkitTask getTask() {
        return this.task;
    }

    public boolean isInFight() {
        return this.inFight;
    }

    public Team getOpponent() {
        return this.opponent;
    }

    public TeamDuel getDuel() {
        return this.duel;
    }

    public Map<Team, Ladder> getChallenges() {
        return this.challenges;
    }

    public Team getChallengingTeam() {
        return this.challengingTeam;
    }

    public void setMain(Nanny main) {
        this.main = main;
    }

    public void setLf(LangFile lf) {
        this.lf = lf;
    }

    public void setPm(ProfileManager pm) {
        this.pm = pm;
    }

    public void setLeader(Player leader) {
        this.leader = leader;
    }

    public void setMembers(List<Player> members) {
        this.members = members;
    }

    public void setInvitedPlayers(List<Player> invitedPlayers) {
        this.invitedPlayers = invitedPlayers;
    }

    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public void setInFight(boolean inFight) {
        this.inFight = inFight;
    }

    public void setOpponent(Team opponent) {
        this.opponent = opponent;
    }

    public void setDuel(TeamDuel duel) {
        this.duel = duel;
    }

    public void setChallenges(Map<Team, Ladder> challenges) {
        this.challenges = challenges;
    }

    public void setChallengingTeam(Team challengingTeam) {
        this.challengingTeam = challengingTeam;
    }

}


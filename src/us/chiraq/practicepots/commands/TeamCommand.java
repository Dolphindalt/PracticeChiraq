package us.chiraq.practicepots.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mkremins.fanciful.FancyMessage;
import us.chiraq.practicepots.Nanny;
import us.chiraq.practicepots.files.types.LangFile;
import us.chiraq.practicepots.game.Ladder;
import us.chiraq.practicepots.game.Team;
import us.chiraq.practicepots.game.fight.TeamDuel;
import us.chiraq.practicepots.profile.Profile;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class TeamCommand
implements CommandExecutor,
TabCompleter {
    private Nanny main = Nanny.getInstance();
    private LangFile lf = this.main.getLangFile();

    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.lf.getString("NOT_PLAYER"));
            return true;
        }
        Player player = (Player)sender;
        Profile profile = Profile.getProfile(player.getUniqueId());
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("accept")) {
                Team team = profile.getTeam();
                if (team == null) {
                    player.sendMessage(this.lf.getString("TEAM_COMMAND.NOT_IN"));
                    return true;
                }
                if (!team.getLeader().getName().equalsIgnoreCase(player.getName())) {
                    player.sendMessage(this.lf.getString("TEAM_COMMAND.NOT_LEADER"));
                    return true;
                }
                if (team.isInFight()) {
                    player.sendMessage(this.lf.getString("NOPE"));
                    return true;
                }
                Team otherTeam = Team.getTeam(args[1]);
                if (otherTeam != null && otherTeam.getChallengingTeam() == team && team.getChallenges().containsKey(otherTeam)) {
                    Ladder ladder = team.getChallenges().get(otherTeam);
                    if (!otherTeam.isInFight()) {
                        new TeamDuel(team, otherTeam, ladder, ladder.getArenas().get(new Random().nextInt(ladder.getArenas().size())));
                        return true;
                    }
                }
                player.sendMessage(this.lf.getString("ERROR"));
                return true;
            }
            if (args[0].equalsIgnoreCase("join")) {
                if (profile.getTeam() != null) {
                    player.sendMessage(this.lf.getString("TEAM_COMMAND.ALREADY_IN_TEAM"));
                    return true;
                }
                if (!profile.isInSpawn()) {
                    player.sendMessage(this.lf.getString("PLAYER_NOT_IN_SPAWN"));
                    return true;
                }
                Player teamLeader = Bukkit.getPlayer((String)args[1]);
                if (teamLeader == null) {
                    player.sendMessage(this.lf.getString("NOT_FOUND"));
                    return true;
                }
                Profile leaderProfile = Profile.getProfile(teamLeader.getUniqueId());
                if (leaderProfile.getTeam() == null) {
                    player.sendMessage(this.lf.getString("TEAM_COMMAND.NOT_VALID_TEAM"));
                    return true;
                }
                Team team = leaderProfile.getTeam();
                if (!team.getInvitedPlayers().contains((Object)player)) {
                    player.sendMessage(this.lf.getString("TEAM_COMMAND.NOT_VALID_INVITE"));
                    return true;
                }
                team.getInvitedPlayers().remove((Object)player);
                team.addPlayer(player);
                team.sendMessage(this.lf.getString("TEAM_COMMAND.JOINED").replace("%PLAYER%", player.getName()));
                return true;
            }
            if (args[0].equalsIgnoreCase("invite")) {
                Team team = profile.getTeam();
                if (team == null) {
                    player.sendMessage(this.lf.getString("TEAM_COMMAND.NOT_IN"));
                    return true;
                }
                if (!team.getLeader().getName().equalsIgnoreCase(player.getName())) {
                    player.sendMessage(this.lf.getString("TEAM_COMMAND.NOT_LEADER"));
                    return true;
                }
                Player invitedPlayer = Bukkit.getPlayer((String)args[1]);
                if (invitedPlayer == null) {
                    player.sendMessage(this.lf.getString("NOT_FOUND"));
                    return true;
                }
                if (invitedPlayer.getName().equalsIgnoreCase(player.getName())) {
                    player.sendMessage(this.lf.getString("NOPE"));
                    return true;
                }
                if (team.getMembers().contains((Object)invitedPlayer)) {
                    player.sendMessage(this.lf.getString("TEAM_COMMAND.ALREADY_IN"));
                    return true;
                }
                if (team.getInvitedPlayers().contains((Object)invitedPlayer)) {
                    player.sendMessage(this.lf.getString("TEAM_COMMAND.ALREADY_INVITED"));
                    return true;
                }
                Profile invitedProfile = Profile.getProfile(invitedPlayer.getUniqueId());
                if (!invitedProfile.isInSpawn()) {
                    player.sendMessage(this.lf.getString("NOT_IN_SPAWN"));
                    return true;
                }
                player.sendMessage(this.lf.getString("TEAM_COMMAND.REQUEST_SENT").replace("%PLAYER%", invitedPlayer.getName()));
                new FancyMessage(this.lf.getString("TEAM_COMMAND.REQUESTED").replace("%PLAYER%", player.getName())).command("/team join " + player.getName()).send(invitedPlayer);
                team.getInvitedPlayers().add(invitedPlayer);
                return true;
            }
        }
        for (String message : this.lf.getStringList("TEAM_COMMAND.USAGE")) {
            sender.sendMessage(message);
        }
        return true;
    }

    @SuppressWarnings("deprecation")
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        ArrayList<String> toReturn = new ArrayList<String>();
        if (args.length == 2) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                toReturn.add(player.getName());
            }
        }
        return toReturn;
    }
}


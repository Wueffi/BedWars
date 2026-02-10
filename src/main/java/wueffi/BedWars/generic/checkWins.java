package wueffi.BedWars.generic;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import wueffi.BedWars.utils.BedChecker;
import wueffi.MiniGameCore.api.MiniGameCoreAPI;
import wueffi.MiniGameCore.utils.Lobby;
import wueffi.MiniGameCore.utils.Team;

public class checkWins {
    private final Plugin plugin;
    private BukkitRunnable checkTask;
    private final Lobby lobby;
    private final BedChecker bedChecker;

    public checkWins(Plugin plugin, Lobby lobby, BedChecker bedChecker) {
        this.plugin = plugin;
        this.lobby = lobby;
        this.bedChecker = bedChecker;
    }

    public void startChecking() {
        checkTask = new BukkitRunnable() {
            @Override
            public void run() {
                playerUnderYZero(lobby);
                int aliveTeams = 0;
                Team lastAliveTeam = null;

                for (Team team : lobby.getTeamList()) {
                    int teamAliveCount = 0;
                    if (team.getAlivePlayers() > 0 || bedChecker.getBedStatus().getOrDefault(team.getColor(), false)) {
                        teamAliveCount++;
                    }

                    if (teamAliveCount > 0) {
                        aliveTeams++;
                        lastAliveTeam = team;
                    }
                }

                if (aliveTeams == 1 && lastAliveTeam != null) {
                    MiniGameCoreAPI.winTeam(lobby, lastAliveTeam);
                    stopChecking();
                }
            }
        };
        checkTask.runTaskTimer(plugin, 0L, 1L);
    }

    public void stopChecking() {
        if (checkTask != null) {
            checkTask.cancel();
        }
    }

    public void playerUnderYZero(Lobby lobby) {
        for (Player player : lobby.getPlayers()) {
            if (player.getLocation().y() <=0) {
                player.setHealth(0);
            }
        }
    }
}
package wueffi.BedWars;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import wueffi.BedWars.generic.CopyWorldAndConfig;
import wueffi.BedWars.utils.GameListener;
import wueffi.MiniGameCore.managers.LobbyManager;

public final class BedWarsMain extends JavaPlugin {
    public LobbyManager lobbyManager;
    private int gamesPlayed = 0;

    @Override
    public void onEnable() {
        getLogger().info("Starting up BedWars Plugin...");

        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);
        getLogger().info("Registered Events!");

        CopyWorldAndConfig.setup(this);

        int pluginId = 32270;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(
                new SingleLineChart("games_played", () -> {
                    int count = gamesPlayed;
                    gamesPlayed = 0;
                    return count;
                })
        );
    }

    @Override
    public void onDisable() {
    }

    public void incrementGamesPlayed() {
        gamesPlayed++;
    }
}

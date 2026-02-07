package wueffi.BedWars;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import wueffi.BedWars.generic.CopyWorldAndConfig;
import wueffi.BedWars.utils.GameListener;
import wueffi.MiniGameCore.managers.LobbyManager;

public final class BedWarsMain extends JavaPlugin {
    public LobbyManager lobbyManager;
    @Override
    public void onEnable() {
        getLogger().info("Starting up BedWars Plugin...");

        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);
        getLogger().info("Registered Events!");

        CopyWorldAndConfig.setup(this);
    }

    @Override
    public void onDisable() {
    }
}

package me.rarehyperion.timepreserve;

import me.rarehyperion.timepreserve.data.ServerState;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class TP extends JavaPlugin implements Listener {

    private BukkitTask task = null;
    private World overworld = null;

    private ServerState state;

    @Override
    public void onLoad() {
        this.state = ServerState.load(this, this.getDataFolder());
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.overworld = this.getOverworld();

        this.saveDefaultConfig();

        if(this.state.paused && !this.getServer().getOnlinePlayers().isEmpty()) {
            this.resumeGame();
        } else if(!this.state.paused && this.getServer().getOnlinePlayers().isEmpty()) {
            this.pauseGame();
        }
    }

    @Override
    public void onDisable() {
        if(this.task != null) {
            this.getLogger().info("Cancelling ongoing tasks.");
            this.task.cancel();
            this.task = null;
        }

        ServerState.save(this, this.getDataFolder(), this.state);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if(this.task != null) {
            this.task.cancel();
            this.task = null;

            this.getLogger().info("Server is no longer inactive.");
        }

        if (this.overworld == null) {
            this.getLogger().warning("Unable to find an overworld to resume.");
            return;
        }

        if(Boolean.FALSE.equals(this.overworld.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))) {
            this.resumeGame();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if(this.getServer().getOnlinePlayers().size() <= 1) {
            this.task = this.getServer().getScheduler().runTaskLater(this, () -> {
                this.pauseGame();
                this.task = null;
            }, this.getConfig().getLong("idleTimeout", 5L) * 20 * 60);
        }
    }

    private World getOverworld() {
        return this.getServer().getWorlds().stream()
                .filter(world -> world.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElse(null);
    }

    private void pauseGame() {
        if(this.state.paused) {
            this.getLogger().warning("Overworld already has been paused.");
            return;
        }

        if(this.overworld == null) {
            this.getLogger().warning("Unable to find an overworld to pause.");
            return;
        }

        this.getLogger().info("Server is inactive, pausing...");

        if(this.getConfig().getBoolean("pauseWeather", true)) {
            this.state.saved.put("DO_WEATHER_CYCLE", this.overworld.getGameRuleValue(GameRule.DO_WEATHER_CYCLE));
            this.overworld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        }

        if(this.getConfig().getBoolean("pauseDaylight", true)) {
            this.state.saved.put("DO_DAYLIGHT_CYCLE", this.overworld.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE));
            this.overworld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        }

        this.state.paused = true;
    }

    private void resumeGame() {
        if(!this.state.paused) {
            this.getLogger().warning("Overworld already has been resumed.");
            return;
        }

        if(this.overworld == null) {
            this.getLogger().warning("Unable to find an overworld to resume.");
            return;
        }

        this.getLogger().info("Server is no longer inactive, resuming...");

        final Map<String, Object> saved = this.state.saved;

        if(this.getConfig().getBoolean("pauseWeather", true)) {
            this.overworld.setGameRule(GameRule.DO_WEATHER_CYCLE, (boolean) saved.getOrDefault("DO_WEATHER_CYCLE", true));
        }

        if(this.getConfig().getBoolean("pauseDaylight", true)) {
            this.overworld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, (boolean) saved.getOrDefault("DO_DAYLIGHT_CYCLE", true));
        }

        this.state.paused = false;
    }

}

package me.rarehyperion.timepreserve;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class TP extends JavaPlugin implements Listener {

    private BukkitTask task = null;
    private World overworld = null;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.overworld = this.getOverworld();

        this.saveDefaultConfig();

        if(this.overworld != null) {
            this.timeMovement(false);
            this.getLogger().info("Server is inactive, paused time.");
        }
    }

    @Override
    public void onDisable() {
        if(this.task != null) {
            this.getLogger().info("Cancelling ongoing tasks.");
            this.task.cancel();
            this.task = null;
        }
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
            this.timeMovement(true);
            this.getLogger().info("Overworld has been resumed.");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if(this.getServer().getOnlinePlayers().size() <= 1) {
            this.getLogger().info("Server is now inactive, waiting...");

            this.task = this.getServer().getScheduler().runTaskLater(this, () -> {
                if(this.overworld == null) {
                    this.getLogger().warning("Unable to find an overworld to paused.");
                    return;
                }

                if(Boolean.TRUE.equals(this.overworld.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))) {
                    this.getLogger().warning("Overworld already has paused.");
                    return;
                }

               this.timeMovement(false);

                this.getLogger().info("Overworld has been paused.");
                this.task = null;
            }, this.getConfig().getLong("idleTimeout") * 20 * 60);
        }
    }

    private World getOverworld() {
        return this.getServer().getWorlds().stream()
                .filter(world -> world.getEnvironment() == World.Environment.NORMAL)
                .findFirst()
                .orElse(null);
    }

    private void timeMovement(final boolean shouldMove) {
        this.overworld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, shouldMove);

        if (getConfig().getBoolean("effectWeather")) {
            this.overworld.setGameRule(GameRule.DO_WEATHER_CYCLE, shouldMove);
        }
    }

}

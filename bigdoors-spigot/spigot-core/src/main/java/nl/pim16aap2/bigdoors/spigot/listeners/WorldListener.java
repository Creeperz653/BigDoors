package nl.pim16aap2.bigdoors.spigot.listeners;

import nl.pim16aap2.bigdoors.managers.PowerBlockManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Process world (un)load events.
 *
 * @author Pim
 */
public class WorldListener implements Listener
{
    private static @Nullable WorldListener instance;

    private final @NotNull PowerBlockManager powerBlockManager;

    private WorldListener(final @NotNull PowerBlockManager powerBlockManager)
    {
        this.powerBlockManager = powerBlockManager;
        for (final @NotNull World world : Bukkit.getWorlds())
            powerBlockManager.loadWorld(world.getName());
    }

    /**
     * Initializes the {@link WorldListener}. If it has already been initialized, it'll return that instance instead.
     *
     * @param powerBlockManager The {@link PowerBlockManager}.
     * @return The instance of this {@link WorldListener}.
     */
    public static WorldListener init(final @NotNull PowerBlockManager powerBlockManager)
    {
        return instance == null ? instance = new WorldListener(powerBlockManager) : instance;
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldLoad(final WorldLoadEvent event)
    {
        powerBlockManager.loadWorld(event.getWorld().getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldUnload(final WorldUnloadEvent event)
    {
        powerBlockManager.unloadWorld(event.getWorld().getName());
    }

}
package nl.pim16aap2.bigdoors.spigot.listeners;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.restartable.Restartable;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.util.InnerUtil;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a listener that keeps track redstone changes.
 *
 * @author Pim
 */
public class RedstoneListener extends Restartable implements Listener
{
    private static @Nullable RedstoneListener INSTANCE;
    private final @NonNull BigDoorsSpigot bigDoorsSpigot;
    private final @NonNull Set<Material> powerBlockTypes = new HashSet<>();
    private boolean isRegistered = false;

    private RedstoneListener(final @NonNull BigDoorsSpigot bigDoorsSpigot)
    {
        super(bigDoorsSpigot);
        this.bigDoorsSpigot = bigDoorsSpigot;
        restart();
    }

    /**
     * Initializes the {@link RedstoneListener}. If it has already been initialized, it'll return that instance
     * instead.
     *
     * @param plugin The {@link BigDoorsSpigot} plugin.
     * @return The instance of this {@link RedstoneListener}.
     */
    public static @NonNull RedstoneListener init(final @NonNull BigDoorsSpigot plugin)
    {
        return (INSTANCE == null) ? INSTANCE = new RedstoneListener(plugin) : INSTANCE;
    }

    @Override
    public void restart()
    {
        powerBlockTypes.clear();

        if (bigDoorsSpigot.getConfigLoader().enableRedstone())
        {
            register();
            powerBlockTypes.addAll(bigDoorsSpigot.getConfigLoader().powerBlockTypes());
            return;
        }
        unregister();
    }

    /**
     * Registers this listener if it isn't already registered.
     */
    private void register()
    {
        if (isRegistered)
            return;
        Bukkit.getPluginManager().registerEvents(this, bigDoorsSpigot.getPlugin());
        isRegistered = true;
    }

    /**
     * Unregisters this listener if it isn't already unregistered.
     */
    private void unregister()
    {
        if (!isRegistered)
            return;
        HandlerList.unregisterAll(this);
        isRegistered = false;
    }

    @Override
    public void shutdown()
    {
        powerBlockTypes.clear();
        unregister();
    }

    private void checkDoors(final @NonNull Location loc)
    {
        BigDoors.get().getPlatform().getPowerBlockManager().doorsFromPowerBlockLoc(
            new Vector3Di(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), loc.getWorld().getName()).whenComplete(
            (doorList, throwable) -> doorList.forEach(
                door -> BigDoors.get().getDoorOpener()
                                .animateDoorAsync(door, DoorActionCause.REDSTONE, null, 0, false,
                                                  DoorActionType.TOGGLE)));
    }

    /**
     * Processes a redstone event. This means that it looks for any power blocks around the block that was changed.
     *
     * @param event The event.
     */
    private void processRedstoneEvent(final @NonNull BlockRedstoneEvent event)
    {
        try
        {
            Block block = event.getBlock();
            Location location = block.getLocation();
            int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

            if (powerBlockTypes.contains(location.getWorld().getBlockAt(x, y, z - 1).getType())) // North
                checkDoors(new Location(location.getWorld(), x, y, z - 1));

            if (powerBlockTypes.contains(location.getWorld().getBlockAt(x + 1, y, z).getType())) // East
                checkDoors(new Location(location.getWorld(), x + 1, y, z));

            if (powerBlockTypes.contains(location.getWorld().getBlockAt(x, y, z + 1).getType())) // South
                checkDoors(new Location(location.getWorld(), x, y, z + 1));

            if (powerBlockTypes.contains(location.getWorld().getBlockAt(x - 1, y, z).getType())) // West
                checkDoors(new Location(location.getWorld(), x - 1, y, z));

            if (y < 254 && powerBlockTypes.contains(location.getWorld().getBlockAt(x, y + 1, z).getType())) // Above
                checkDoors(new Location(location.getWorld(), x, y + 1, z));

            if (y > 0 && powerBlockTypes.contains(location.getWorld().getBlockAt(x, y - 1, z).getType())) // Under
                checkDoors(new Location(location.getWorld(), x, y - 1, z));
        }
        catch (Exception e)
        {
            bigDoorsSpigot.getPLogger().logThrowable(e, "Exception thrown while handling redstone event!");
        }
    }

    /**
     * Listens to redstone changes and checks if there are any doors attached to it. Any doors that are found are then
     * toggled, if possible.
     *
     * @param event The {@link BlockRedstoneEvent}.
     */
    @EventHandler
    public void onBlockRedstoneChange(final @NonNull BlockRedstoneEvent event)
    {
        // Only boolean status is allowed, so a varying degree of "on" has no effect.
        if (event.getOldCurrent() != 0 && event.getNewCurrent() != 0)
            return;

        if (!BigDoors.get().getPlatform().getPowerBlockManager().isBigDoorsWorld(event.getBlock().getWorld().getName()))
            return;

        CompletableFuture.runAsync(() -> processRedstoneEvent(event)).exceptionally(InnerUtil::exceptionally);
    }
}

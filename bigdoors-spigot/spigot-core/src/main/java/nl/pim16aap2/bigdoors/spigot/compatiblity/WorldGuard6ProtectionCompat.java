package nl.pim16aap2.bigdoors.spigot.compatiblity;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import nl.pim16aap2.bigdoors.logging.IPLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Compatibility hook for version 6 of WorldGuard.
 *
 * @author Pim
 * @see IProtectionCompat
 */
class WorldGuard6ProtectionCompat implements IProtectionCompat
{
    private static final ProtectionCompat COMPAT = ProtectionCompat.WORLDGUARD;
    private final WorldGuardPlugin worldGuard;
    private final boolean success;
    private final Method m;

    private final IPLogger logger;

    @SuppressWarnings("unused")
    public WorldGuard6ProtectionCompat(JavaPlugin bigDoors, IPLogger logger)
    {
        this.logger = logger;

        final @Nullable Plugin wgPlugin =
            Bukkit.getServer().getPluginManager().getPlugin(ProtectionCompat.getName(COMPAT));

        // WorldGuard may not be loaded
        if (!(wgPlugin instanceof WorldGuardPlugin))
            throw new IllegalStateException("Plugin " + wgPlugin + " is not the expected WorldGuardPlugin!");

        worldGuard = (WorldGuardPlugin) wgPlugin;

        try
        {
            m = worldGuard.getClass().getMethod("canBuild", Player.class, Location.class);
            success = true;
        }
        catch (NoSuchMethodException | SecurityException e)
        {
            throw new RuntimeException("Failed to access canBuild method!", e);
        }
    }

    @Override
    public boolean canBreakBlock(Player player, Location loc)
    {
        try
        {
            return (boolean) (m.invoke(worldGuard, player, loc));
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            logger.logThrowable(e);
        }
        return false;
    }

    @SuppressWarnings("DuplicatedCode") // This class will need to be rewritten anyway.
    @Override
    public boolean canBreakBlocksBetweenLocs(Player player, Location loc1, Location loc2)
    {
        if (loc1.getWorld() != loc2.getWorld())
            return false;

        final int x1 = Math.min(loc1.getBlockX(), loc2.getBlockX());
        final int y1 = Math.min(loc1.getBlockY(), loc2.getBlockY());
        final int z1 = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        final int x2 = Math.max(loc1.getBlockX(), loc2.getBlockX());
        final int y2 = Math.max(loc1.getBlockY(), loc2.getBlockY());
        final int z2 = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int xPos = x1; xPos <= x2; ++xPos)
            for (int yPos = y1; yPos <= y2; ++yPos)
                for (int zPos = z1; zPos <= z2; ++zPos)
                    if (!canBreakBlock(player, new Location(loc1.getWorld(), xPos, yPos, zPos)))
                        return false;
        return true;
    }

    @Override
    public boolean success()
    {
        return success;
    }

    @Override
    public String getName()
    {
        return worldGuard.getName();
    }
}

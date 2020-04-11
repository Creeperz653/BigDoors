package nl.pim16aap2.bigdoors.spigot.compatiblity;

import com.palmergames.bukkit.towny.object.TownyPermission.ActionType;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Compatibility hook for the new version of PlotSquared.
 *
 * @author Pim
 * @see IProtectionCompat
 */
public class TownyOldProtectionCompat implements IProtectionCompat
{
    @SuppressWarnings("unused")
    private final BigDoorsSpigot plugin;
    private boolean success = false;
    private static final ProtectionCompat compat = ProtectionCompat.TOWNY;

    public TownyOldProtectionCompat(final @NotNull BigDoorsSpigot plugin)
    {
        this.plugin = plugin;
        success = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBreakBlock(final @NotNull Player player, final @NotNull Location loc)
    {
        return PlayerCacheUtil.getCachePermission(player, loc, loc.getBlock().getType(), ActionType.DESTROY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBreakBlocksBetweenLocs(final @NotNull Player player, final @NotNull Location loc1,
                                             final @NotNull Location loc2)
    {
        if (loc1.getWorld() != loc2.getWorld())
            return false;

        int x1 = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int y1 = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int z1 = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int x2 = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int y2 = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int z2 = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        for (int xPos = x1; xPos <= x2; ++xPos)
            for (int yPos = y1; yPos <= y2; ++yPos)
                for (int zPos = z1; zPos <= z2; ++zPos)
                    if (!canBreakBlock(player, new Location(loc1.getWorld(), xPos, yPos, zPos)))
                        return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean success()
    {
        return success;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getName()
    {
        return ProtectionCompat.getName(compat);
    }
}


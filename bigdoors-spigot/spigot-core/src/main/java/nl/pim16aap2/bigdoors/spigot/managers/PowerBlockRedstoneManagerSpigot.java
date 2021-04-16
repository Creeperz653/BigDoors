package nl.pim16aap2.bigdoors.spigot.managers;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.api.IPowerBlockRedstoneManager;
import nl.pim16aap2.bigdoors.spigot.util.SpigotAdapter;
import nl.pim16aap2.bigdoors.util.vector.Vector3DiConst;
import org.bukkit.Location;

/**
 * Represents an implementation of {@link IPowerBlockRedstoneManager} for the Spigot platform.
 *
 * @author Pim
 */
public final class PowerBlockRedstoneManagerSpigot implements IPowerBlockRedstoneManager
{
    private static final @NonNull PowerBlockRedstoneManagerSpigot INSTANCE = new PowerBlockRedstoneManagerSpigot();

    private PowerBlockRedstoneManagerSpigot()
    {
    }

    /**
     * Gets the instance of this {@link PowerBlockRedstoneManagerSpigot}.
     *
     * @return The instance of this {@link PowerBlockRedstoneManagerSpigot}.
     */
    public static @NonNull PowerBlockRedstoneManagerSpigot get()
    {
        return INSTANCE;
    }

    private boolean isPoweredRedstone(final @NonNull Location loc)
    {
//        if (loc.getBlock().isBlockIndirectlyPowered())
        return true;
    }

    @Override
    public boolean isBlockPowered(final @NonNull IPWorld world, final @NonNull Vector3DiConst position)
    {
        final Location loc = new Location(SpigotAdapter.getBukkitWorld(world), position.getX(), position.getY(),
                                          position.getZ());
        // TODO: Check if this is sufficient.
        return loc.getBlock().isBlockIndirectlyPowered();

//        for (PBlockFace face : PBlockFace.values())
//        {
//            if (face == PBlockFace.NONE)
//                continue;
//            Vector3Di dirVec = PBlockFace.getDirection(face);
//            if (isPoweredRedstone(loc.clone().add(dirVec.getX(), dirVec.getY(), dirVec.getZ())))
//                return true;
//        }
//        return false;
    }
}

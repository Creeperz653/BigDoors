package nl.pim16aap2.bigdoors.spigot.v1_14_R1;

import nl.pim16aap2.bigdoors.api.ICustomCraftFallingBlock;
import nl.pim16aap2.bigdoors.api.INMSBlock;
import nl.pim16aap2.bigdoors.api.IPLocationConst;
import nl.pim16aap2.bigdoors.api.factories.IFallingBlockFactory;
import nl.pim16aap2.bigdoors.spigot.util.SpigotAdapter;
import nl.pim16aap2.bigdoors.spigot.util.implementations.PWorldSpigot;
import nl.pim16aap2.bigdoors.util.Constants;
import nl.pim16aap2.bigdoors.util.PLogger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * V1_14_R1 implementation of {@link IFallingBlockFactory}.
 *
 * @author Pim
 * @see IFallingBlockFactory
 */
public class FallingBlockFactory_V1_14_R1 implements IFallingBlockFactory
{
    /** {@inheritDoc} */
    @NotNull
    @Override
    public ICustomCraftFallingBlock fallingBlockFactory(final @NotNull IPLocationConst loc,
                                                        final @NotNull INMSBlock block)
    {
        World bukkitWorld = SpigotAdapter.getBukkitWorld(loc.getWorld());

        // TODO: Don't violate @NotNull.
        CustomEntityFallingBlock_V1_14_R1 fBlockNMS =
            new CustomEntityFallingBlock_V1_14_R1(bukkitWorld, loc.getX(), loc.getY(), loc.getZ(),
                                                  ((NMSBlock_V1_14_R1) block).getMyBlockData());

        CustomCraftFallingBlock_V1_14_R1 ret = new CustomCraftFallingBlock_V1_14_R1(Bukkit.getServer(), fBlockNMS);
        ret.setCustomName(Constants.BIGDOORSENTITYNAME);
        ret.setCustomNameVisible(false);
        return ret;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public INMSBlock nmsBlockFactory(final @NotNull IPLocationConst loc)
    {
        if (!(loc.getWorld() instanceof PWorldSpigot))
        {
            PLogger.get().logException(new IllegalArgumentException());
            return null; // TODO: Don't violate @NotNull.
        }
        return new NMSBlock_V1_14_R1((PWorldSpigot) loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
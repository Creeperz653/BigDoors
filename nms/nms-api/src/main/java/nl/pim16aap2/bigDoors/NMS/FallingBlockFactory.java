package nl.pim16aap2.bigDoors.NMS;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

public interface FallingBlockFactory
{
    CustomCraftFallingBlock fallingBlockFactory(Location loc, NMSBlock block, byte matData, Material mat);

    NMSBlock nmsBlockFactory(World world, int x, int y, int z);

    /**
     * Verifies that the current implementation of the falling-block related NMS code (factory, (Craft)entity, NMSBlock)
     * is valid.
     *
     * @param world an existing bukkit world.
     * @param loc A location.
     *
     * @throws Exception When verification failed.
     */
    default void verify(World world, Location loc)
        throws Exception
    {
    }
}

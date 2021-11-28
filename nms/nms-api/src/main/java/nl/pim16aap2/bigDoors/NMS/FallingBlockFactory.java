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
     * Do not forget to ensure that there is a valid block at the provided location in the given
     * world before calling this method.
     *
     * @param world an existing bukkit world.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param z The z-coordinate.
     *
     * @throws Exception When verification failed.
     */
    default void verify(World world, int x, int y, int z)
        throws Exception
    {
    }
}

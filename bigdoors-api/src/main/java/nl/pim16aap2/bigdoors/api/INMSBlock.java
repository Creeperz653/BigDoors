package nl.pim16aap2.bigdoors.api;

import nl.pim16aap2.bigdoors.util.RotateDirection;
import org.bukkit.Location;

/**
 * Represents an NMS block.
 *
 * @author pim
 */
public interface INMSBlock
{
    /**
     * Check if this block can rotate.
     *
     * @return True if this block can rotate.
     */
    public boolean canRotate();

    /**
     * Rotate this block in a provided {@link RotateDirection}.
     *
     * @param rotDir The {@link RotateDirection} to rotate this block in.
     */
    public void rotateBlock(RotateDirection rotDir);

    /**
     * Place the block at a given location.
     *
     * @param loc The location where the block will be placed.
     */
    public void putBlock(Location loc);

    /**
     * Delete the block at the original location.
     */
    public void deleteOriginalBlock();
}
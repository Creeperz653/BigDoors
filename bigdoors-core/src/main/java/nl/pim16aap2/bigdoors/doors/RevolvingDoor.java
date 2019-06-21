package nl.pim16aap2.bigdoors.doors;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.util.Mutable;
import nl.pim16aap2.bigdoors.util.MyBlockFace;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Vector2D;
import org.bukkit.Chunk;
import org.bukkit.Location;

/**
 * Represents a Revolving Door doorType.
 *
 * @author pim
 * @see DoorBase
 */
public class RevolvingDoor extends DoorBase
{
    RevolvingDoor(BigDoors plugin, long doorUID, DoorType type)
    {
        super(plugin, doorUID, type);
    }

    RevolvingDoor(BigDoors plugin, long doorUID)
    {
        super(plugin, doorUID, DoorType.REVOLVINGDOOR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector2D[] calculateChunkRange()
    {
        Chunk minChunk = min.getChunk();
        Chunk maxChunk = max.getChunk();

        return new Vector2D[] { new Vector2D(minChunk.getX(), minChunk.getZ()),
                                new Vector2D(maxChunk.getX(), maxChunk.getZ()) };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultOpenDirection()
    {
        openDir = RotateDirection.CLOCKWISE;
    }

    // This type never faces any direction.
    /**
     * {@inheritDoc}
     */
    @Override
    public MyBlockFace calculateCurrentDirection()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getNewLocations(MyBlockFace openDirection, RotateDirection rotateDirection, Location newMin,
                                Location newMax, int blocksMoved, Mutable<MyBlockFace> newEngineSide)
    {
        newMin.setX(min.getBlockX());
        newMin.setY(min.getBlockY());
        newMin.setZ(min.getBlockZ());

        newMax.setX(max.getBlockX());
        newMax.setY(max.getBlockY());
        newMax.setZ(max.getBlockZ());
    }
}
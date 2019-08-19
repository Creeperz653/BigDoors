package nl.pim16aap2.bigdoors.doors;

import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.moveblocks.WindmillMover;
import nl.pim16aap2.bigdoors.util.PBlockFace;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Vector2D;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Windmill doorType.
 *
 * @author Pim
 * @see HorizontalAxisAlignedBase
 */
public class Windmill extends HorizontalAxisAlignedBase
{
    protected Windmill(final @NotNull PLogger pLogger, final long doorUID, final @NotNull DoorData doorData,
                       final @NotNull DoorType type)
    {
        super(pLogger, doorUID, doorData, type);
    }

    protected Windmill(final @NotNull PLogger pLogger, final long doorUID, final @NotNull DoorData doorData)
    {
        this(pLogger, doorUID, doorData, DoorType.WINDMILL);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always true for this type.
     */
    @Override
    public boolean isOpenable()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always true for this type.
     */
    @Override
    public boolean isCloseable()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Vector2D[] calculateChunkRange()
    {
        Chunk minChunk = min.getChunk();
        Chunk maxChunk = max.getChunk();

        return new Vector2D[]{new Vector2D(minChunk.getX(), minChunk.getZ()),
                              new Vector2D(maxChunk.getX(), maxChunk.getZ())};
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public RotateDirection cycleOpenDirection()
    {
        // This type goes exactly the other way as most usual axis aligned ones.
        if (!onNorthSouthAxis())
            return getOpenDir().equals(RotateDirection.EAST) ? RotateDirection.WEST : RotateDirection.EAST;
        return getOpenDir().equals(RotateDirection.NORTH) ? RotateDirection.SOUTH : RotateDirection.NORTH;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always {@link PBlockFace#NONE} for this type.
     */
    @Override
    @NotNull
    public PBlockFace calculateCurrentDirection()
    {
        return PBlockFace.NONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultOpenDirection()
    {
        if (onNorthSouthAxis())
            setOpenDir(RotateDirection.NORTH);
        else
            setOpenDir(RotateDirection.EAST);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always the same as {@link #getOpenDir()}, as this type makes no distinction between opening and closing.
     */
    @NotNull
    @Override
    public RotateDirection getCurrentToggleDir()
    {
        return getOpenDir();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always returns the current min and max coordinates of this door, as this type doesn't change the locations.
     */
    @Override
    protected boolean getPotentialNewCoordinates(final @NotNull Location newMin, final @NotNull Location newMax)
    {
        newMin.setX(newMin.getBlockX());
        newMin.setY(newMin.getBlockY());
        newMin.setZ(newMin.getBlockZ());

        newMax.setX(newMax.getBlockX());
        newMax.setY(newMax.getBlockY());
        newMax.setZ(newMax.getBlockZ());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerBlockMover(final @NotNull DoorActionCause cause, final double time,
                                      final boolean instantOpen, final @NotNull Location newMin,
                                      final @NotNull Location newMax)
    {
        // TODO: Get rid of this.
        double fixedTime = time < 0.5 ? 5 : time;

        doorOpener.registerBlockMover(
            new WindmillMover(this, fixedTime, doorOpener.getMultiplier(this), getCurrentToggleDir(),
                              cause == DoorActionCause.PLAYER ? getPlayerUUID() : null));
    }
}
package nl.pim16aap2.bigdoors.doors.slidingdoor;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import nl.pim16aap2.bigdoors.api.annotations.PersistentVariable;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doors.DoorOpeningUtility;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.IBlocksToMoveArchetype;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.IStationaryDoorArchetype;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.ITimerToggleableArchetype;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.api.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.api.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.api.util.Cuboid;
import nl.pim16aap2.bigdoors.api.util.CuboidConst;
import nl.pim16aap2.bigdoors.api.util.PBlockFace;
import nl.pim16aap2.bigdoors.api.util.RotateDirection;
import nl.pim16aap2.bigdoors.api.util.Util;
import nl.pim16aap2.bigdoors.api.util.vector.Vector2Di;
import nl.pim16aap2.bigdoors.api.util.vector.Vector3DiConst;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents a Sliding Door doorType.
 *
 * @author Pim
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SlidingDoor extends AbstractDoorBase
    implements IStationaryDoorArchetype, IBlocksToMoveArchetype, ITimerToggleableArchetype
{
    private static final @NotNull DoorType DOOR_TYPE = DoorTypeSlidingDoor.get();

    @Getter
    @Setter
    @Accessors(chain = true)
    @PersistentVariable
    protected int blocksToMove;

    @Getter
    @Setter
    @Accessors(chain = true)
    @PersistentVariable
    protected int autoCloseTime;

    @Getter
    @Setter
    @Accessors(chain = true)
    @PersistentVariable
    protected int autoOpenTime;

    public SlidingDoor(final @NotNull DoorData doorData, final int blocksToMove, final int autoCloseTime,
                       final int autoOpenTime)
    {
        super(doorData);
        this.blocksToMove = blocksToMove;
        this.autoCloseTime = autoCloseTime;
        this.autoOpenTime = autoOpenTime;
    }

    public SlidingDoor(final @NotNull DoorData doorData, final int blocksToMove)
    {
        this(doorData, blocksToMove, -1, -1);
    }

    private SlidingDoor(final @NotNull DoorData doorData)
    {
        this(doorData, -1); // Add tmp/default values
    }

    @Override
    public @NotNull DoorType getDoorType()
    {
        return DOOR_TYPE;
    }

    @Override
    public @NotNull Vector2Di[] calculateChunkRange()
    {
        final @NotNull Vector3DiConst dimensions = getDimensions();

        int distanceX = 0;
        int distanceZ = 0;
        if (getOpenDir().equals(RotateDirection.NORTH) || getOpenDir().equals(RotateDirection.SOUTH))
            distanceZ = (getBlocksToMove() > 0 ? Math.max(dimensions.getZ(), getBlocksToMove()) :
                         Math.min(-dimensions.getZ(), getBlocksToMove())) / 16 + 1;
        else
            distanceX = (getBlocksToMove() > 0 ? Math.max(dimensions.getX(), getBlocksToMove()) :
                         Math.min(-dimensions.getX(), getBlocksToMove())) / 16 + 1;

        return new Vector2Di[]{
            new Vector2Di(getEngineChunk().getX() - distanceX, getEngineChunk().getY() - distanceZ),
            new Vector2Di(getEngineChunk().getX() + distanceX, getEngineChunk().getY() + distanceZ)};
    }

    @Override
    public @NotNull RotateDirection cycleOpenDirection()
    {
        return getOpenDir().equals(RotateDirection.NORTH) ? RotateDirection.EAST :
               getOpenDir().equals(RotateDirection.EAST) ? RotateDirection.SOUTH :
               getOpenDir().equals(RotateDirection.SOUTH) ? RotateDirection.WEST : RotateDirection.NORTH;
    }

    @Override
    public synchronized @NotNull RotateDirection getCurrentToggleDir()
    {
        return isOpen() ? RotateDirection.getOpposite(getOpenDir()) : getOpenDir();
    }

    @Override
    public synchronized @NotNull Optional<Cuboid> getPotentialNewCoordinates()
    {
        final @NotNull Vector3DiConst vec = PBlockFace.getDirection(Util.getPBlockFace(getCurrentToggleDir()));
        return Optional.of(getCuboid().clone().move(0, getBlocksToMove() * vec.getY(), 0));
    }

    @Override
    protected @NotNull BlockMover constructBlockMover(final @NotNull DoorActionCause cause, final double time,
                                                      final boolean skipAnimation, final @NotNull CuboidConst newCuboid,
                                                      final @NotNull IPPlayer responsible,
                                                      final @NotNull DoorActionType actionType)
        throws Exception
    {
        final @NotNull RotateDirection currentToggleDir = getCurrentToggleDir();
        return new SlidingMover(this, time, skipAnimation, getBlocksToMove(), currentToggleDir,
                                DoorOpeningUtility.getMultiplier(this), responsible, newCuboid, cause, actionType);
    }
}

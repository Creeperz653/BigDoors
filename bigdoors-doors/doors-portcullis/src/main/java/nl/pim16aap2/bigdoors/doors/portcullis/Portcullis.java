package nl.pim16aap2.bigdoors.doors.portcullis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.pim16aap2.bigdoors.annotations.PersistentVariable;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoor;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.doors.DoorOpeningUtility;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.IDiscreteMovement;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.ITimerToggleable;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.util.Cuboid;
import nl.pim16aap2.bigdoors.util.PBlockFace;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;

import java.util.Optional;

/**
 * Represents a Portcullis doorType.
 *
 * @author Pim
 * @see DoorBase
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Portcullis extends AbstractDoor implements IDiscreteMovement, ITimerToggleable
{
    @EqualsAndHashCode.Exclude
    private static final DoorType DOOR_TYPE = DoorTypePortcullis.get();

    @Getter
    @Setter
    @PersistentVariable
    protected int blocksToMove;

    @Getter
    @Setter
    @PersistentVariable
    protected int autoCloseTime;

    @Getter
    @Setter
    @PersistentVariable
    protected int autoOpenTime;

    public Portcullis(final DoorBase doorBase, final int blocksToMove, final int autoCloseTime,
                      final int autoOpenTime)
    {
        super(doorBase);
        this.blocksToMove = blocksToMove;
        this.autoCloseTime = autoCloseTime;
        this.autoOpenTime = autoOpenTime;
    }

    public Portcullis(final DoorBase doorBase, final int blocksToMove)
    {
        this(doorBase, blocksToMove, -1, -1);
    }

    private Portcullis(final DoorBase doorBase)
    {
        this(doorBase, -1); // Add tmp/default values
    }

    @Override
    public DoorType getDoorType()
    {
        return DOOR_TYPE;
    }

    @Override
    public synchronized RotateDirection getCurrentToggleDir()
    {
        return isOpen() ? RotateDirection.DOWN : RotateDirection.UP;
    }

    @Override
    public synchronized Optional<Cuboid> getPotentialNewCoordinates()
    {
        final Vector3Di vec = PBlockFace.getDirection(Util.getPBlockFace(getCurrentToggleDir()));
        return Optional.of(getCuboid().move(getBlocksToMove() * vec.x(), 0, getBlocksToMove() * vec.z()));
    }

    @Override
    protected BlockMover constructBlockMover(final DoorActionCause cause, final double time,
                                             final boolean skipAnimation, final Cuboid newCuboid,
                                             final IPPlayer responsible,
                                             final DoorActionType actionType)
        throws Exception
    {
        final int directedBlocksToMove = getOpenDir().equals(RotateDirection.UP) ?
                                         getBlocksToMove() : -getBlocksToMove();
        return new VerticalMover(this, time, skipAnimation, directedBlocksToMove,
                                 DoorOpeningUtility.getMultiplier(this), responsible, newCuboid, cause, actionType);
    }
}

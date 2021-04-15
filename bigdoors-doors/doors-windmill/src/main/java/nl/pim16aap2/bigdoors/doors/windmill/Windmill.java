package nl.pim16aap2.bigdoors.doors.windmill;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.bigdoors.annotations.PersistentVariable;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doors.DoorOpeningUtility;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.IHorizontalAxisAlignedDoorArchetype;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.IPerpetualMoverArchetype;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.IStationaryDoorArchetype;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.util.CuboidConst;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Windmill doorType.
 *
 * @author Pim
 */
public class Windmill extends AbstractDoorBase
    implements IHorizontalAxisAlignedDoorArchetype, IStationaryDoorArchetype, IPerpetualMoverArchetype
{
    @NonNull
    private static final DoorType DOOR_TYPE = DoorTypeWindmill.get();

    /**
     * The number of quarter circles (so 90 degree rotations) this door will make before stopping.
     *
     * @return The number of quarter circles this door will rotate.
     */
    @Getter
    @PersistentVariable
    private int quarterCircles = 1;

    public Windmill(final @NonNull DoorData doorData, final int quarterCircles)
    {
        super(doorData);
        this.quarterCircles = quarterCircles;
    }

    public Windmill(final @NonNull DoorData doorData)
    {
        this(doorData, 1);
    }

    @Override
    public @NonNull DoorType getDoorType()
    {
        return DOOR_TYPE;
    }

    @Override
    public boolean isNorthSouthAligned()
    {
        return getOpenDir() == RotateDirection.EAST || getOpenDir() == RotateDirection.WEST;
    }

    @Override
    public @NonNull RotateDirection cycleOpenDirection()
    {
        return getOpenDir().equals(RotateDirection.NORTH) ? RotateDirection.EAST :
               getOpenDir().equals(RotateDirection.EAST) ? RotateDirection.SOUTH :
               getOpenDir().equals(RotateDirection.SOUTH) ? RotateDirection.WEST : RotateDirection.NORTH;
    }

    @Override
    protected @NonNull BlockMover constructBlockMover(final @NonNull DoorActionCause cause, final double time,
                                                      final boolean skipAnimation, final @NonNull CuboidConst newCuboid,
                                                      final @NonNull IPPlayer responsible,
                                                      final @NonNull DoorActionType actionType)
        throws Exception
    {
        // TODO: Get rid of this.
        double fixedTime = time < 0.5 ? 5 : time;

        return new WindmillMover<>(this, fixedTime, DoorOpeningUtility.getMultiplier(this), getCurrentToggleDir(),
                                   responsible, cause, actionType);
    }

    @Override
    public boolean equals(final @Nullable Object o)
    {
        if (!super.equals(o))
            return false;
        if (getClass() != o.getClass())
            return false;

        final @NonNull Windmill other = (Windmill) o;
        return quarterCircles == other.quarterCircles;
    }
}

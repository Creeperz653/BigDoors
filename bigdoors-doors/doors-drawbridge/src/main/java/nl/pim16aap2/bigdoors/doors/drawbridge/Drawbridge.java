package nl.pim16aap2.bigdoors.doors.drawbridge;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.annotations.PersistentVariable;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doors.DoorOpeningUtility;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.IHorizontalAxisAlignedDoorArchetype;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.IMovingDoorArchetype;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.ITimerToggleableArchetype;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.api.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.api.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.moveblocks.BlockMover;
import nl.pim16aap2.bigdoors.api.util.Cuboid;
import nl.pim16aap2.bigdoors.api.util.CuboidConst;
import nl.pim16aap2.bigdoors.api.util.RotateDirection;
import nl.pim16aap2.bigdoors.api.util.vector.Vector2Di;
import nl.pim16aap2.bigdoors.api.util.vector.Vector3DiConst;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents a DrawBrige doorType.
 *
 * @author Pim
 */
@EqualsAndHashCode(callSuper = true)
public class Drawbridge extends AbstractDoorBase
    implements IHorizontalAxisAlignedDoorArchetype, IMovingDoorArchetype, ITimerToggleableArchetype
{
    private static final @NotNull DoorType DOOR_TYPE = DoorTypeDrawbridge.get();

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

    /**
     * Describes if this drawbridge's vertical position points (when taking the engine Y value as center) up <b>(=
     * TRUE)</b> or down <b>(= FALSE)</b>
     *
     * @return True if this {@link Drawbridge}'s vertical stance points up.
     */
    @Getter
    @PersistentVariable
    protected boolean modeUp;

    public Drawbridge(final @NotNull DoorData doorData, final int autoCloseTime, final int autoOpenTime,
                      final boolean modeUp)
    {
        super(doorData);
        this.autoOpenTime = autoOpenTime;
        this.autoCloseTime = autoCloseTime;
        this.modeUp = modeUp;
    }

    public Drawbridge(final @NotNull DoorData doorData, final boolean modeUp)
    {
        this(doorData, -1, -1, modeUp);
    }

    private Drawbridge(final @NotNull DoorData doorData)
    {
        this(doorData, false); // Add tmp/default values
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

        final int xLen = dimensions.getX();
        final int yLen = dimensions.getY();
        final int zLen = dimensions.getZ();

        final int radius;
        if (dimensions.getY() != 1)
            radius = yLen / 16 + 1;
        else
            radius = Math.max(xLen, zLen) / 16 + 1;

        return new Vector2Di[]{
            new Vector2Di(getEngineChunk().getX() - radius, getEngineChunk().getY() - radius),
            new Vector2Di(getEngineChunk().getX() + radius, getEngineChunk().getY() + radius)};
    }

    @Override
    public synchronized @NotNull RotateDirection getCurrentToggleDir()
    {
        return isOpen() ? RotateDirection.getOpposite(getOpenDir()) : getOpenDir();
    }

    @Override
    public synchronized @NotNull Optional<Cuboid> getPotentialNewCoordinates()
    {
        final @NotNull RotateDirection rotateDirection = getCurrentToggleDir();
        final double angle;
        if (rotateDirection == RotateDirection.NORTH || rotateDirection == RotateDirection.WEST)
            angle = -Math.PI / 2;
        else if (rotateDirection == RotateDirection.SOUTH || rotateDirection == RotateDirection.EAST)
            angle = Math.PI / 2;
        else
        {
            BigDoors.get().getPLogger()
                    .severe("Invalid open direction \"" + rotateDirection.name() + "\" for door: " + getDoorUID());
            return Optional.empty();
        }

        final @NotNull Cuboid cuboid = getCuboid().clone();
        if (rotateDirection == RotateDirection.NORTH || rotateDirection == RotateDirection.SOUTH)
            return Optional.of(cuboid.updatePositions(vec -> vec.rotateAroundXAxis(getEngine(), angle)));
        else
            return Optional.of(cuboid.updatePositions(vec -> vec.rotateAroundZAxis(getEngine(), angle)));
    }

    @Override
    protected @NotNull BlockMover constructBlockMover(final @NotNull DoorActionCause cause, final double time,
                                                      final boolean skipAnimation, final @NotNull CuboidConst newCuboid,
                                                      final @NotNull IPPlayer responsible,
                                                      final @NotNull DoorActionType actionType)
        throws Exception
    {
        return new BridgeMover<>(time, this, getCurrentToggleDir(), skipAnimation,
                                 DoorOpeningUtility.getMultiplier(this), responsible, newCuboid, cause, actionType);
    }

    @Override
    public boolean isNorthSouthAligned()
    {
        final @NotNull RotateDirection openDir = getOpenDir();
        return openDir == RotateDirection.NORTH || openDir == RotateDirection.SOUTH;
    }
}

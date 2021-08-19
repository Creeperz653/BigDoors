package nl.pim16aap2.bigdoors.doors.slidingdoor;

import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoor;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.tooluser.creator.Creator;
import nl.pim16aap2.bigdoors.util.Constants;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class DoorTypeSlidingDoor extends DoorType
{
    private static final int TYPE_VERSION = 1;

    private static final DoorTypeSlidingDoor INSTANCE = new DoorTypeSlidingDoor();

    private DoorTypeSlidingDoor()
    {
        super(Constants.PLUGIN_NAME, "SlidingDoor", TYPE_VERSION, Arrays
            .asList(RotateDirection.NORTH, RotateDirection.EAST,
                    RotateDirection.SOUTH, RotateDirection.WEST), "door.type.sliding_door");
    }

    /**
     * Obtains the instance of this type.
     *
     * @return The instance of this type.
     */
    public static DoorTypeSlidingDoor get()
    {
        return INSTANCE;
    }

    @Override
    public Class<? extends AbstractDoor> getDoorClass()
    {
        return SlidingDoor.class;
    }

    @Override
    public Creator getCreator(final IPPlayer player)
    {
        return new CreatorSlidingDoor(player);
    }

    @Override
    public Creator getCreator(final IPPlayer player, final @Nullable String name)
    {
        return new CreatorSlidingDoor(player, name);
    }
}

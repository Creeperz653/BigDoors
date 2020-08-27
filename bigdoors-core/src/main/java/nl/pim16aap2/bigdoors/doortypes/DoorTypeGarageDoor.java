package nl.pim16aap2.bigdoors.doortypes;

import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doors.GarageDoor;
import nl.pim16aap2.bigdoors.tooluser.creator.Creator;
import nl.pim16aap2.bigdoors.tooluser.creator.CreatorGarageDoor;
import nl.pim16aap2.bigdoors.util.Constants;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class DoorTypeGarageDoor extends DoorType
{
    private static final int TYPE_VERSION = 1;
    private static final List<Parameter> PARAMETERS;

    static
    {
        List<Parameter> parameterTMP = new ArrayList<>(3);
        parameterTMP.add(new Parameter(ParameterType.INTEGER, "autoCloseTimer"));
        parameterTMP.add(new Parameter(ParameterType.INTEGER, "autoOpenTimer"));
        parameterTMP.add(new Parameter(ParameterType.INTEGER, "northSouth"));
        PARAMETERS = Collections.unmodifiableList(parameterTMP);
    }

    @NotNull
    private static final DoorTypeGarageDoor instance = new DoorTypeGarageDoor();

    private DoorTypeGarageDoor()
    {
        super(Constants.PLUGINNAME, "GarageDoor", TYPE_VERSION, PARAMETERS,
              Arrays.asList(RotateDirection.NORTH, RotateDirection.EAST,
                            RotateDirection.SOUTH, RotateDirection.WEST));
    }

    /**
     * Obtains the instance of this type.
     *
     * @return The instance of this type.
     */
    @NotNull
    public static DoorTypeGarageDoor get()
    {
        return instance;
    }

    @Override
    @NotNull
    protected Optional<AbstractDoorBase> instantiate(final @NotNull AbstractDoorBase.DoorData doorData,
                                                     final @NotNull Object... typeData)
    {
        final int autoCloseTimer = (int) typeData[0];
        final int autoOpenTimer = (int) typeData[1];
        final boolean onNorthSouthAxis = ((int) typeData[2]) == 1;

        return Optional.of(new GarageDoor(doorData,
                                          autoCloseTimer,
                                          autoOpenTimer,
                                          onNorthSouthAxis));
    }

    @Override
    @NotNull
    public Creator getCreator(final @NotNull IPPlayer player)
    {
        return new CreatorGarageDoor(player);
    }

    @Override
    @NotNull
    public Creator getCreator(final @NotNull IPPlayer player, final @Nullable String name)
    {
        return new CreatorGarageDoor(player, name);
    }

    @Override
    @NotNull
    protected Object[] generateTypeData(final @NotNull AbstractDoorBase door)
    {
        if (!(door instanceof GarageDoor))
            throw new IllegalArgumentException(
                "Trying to get the type-specific data for a GarageDoor from type: " + door.getDoorType().toString());

        final @NotNull GarageDoor garageDoor = (GarageDoor) door;
        return new Object[]{garageDoor.getAutoCloseTime(),
                            garageDoor.getAutoOpenTime(),
                            garageDoor.isNorthSouthAligned() ? 1 : 0};
    }
}
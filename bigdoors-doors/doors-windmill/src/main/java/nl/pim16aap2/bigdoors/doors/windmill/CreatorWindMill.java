package nl.pim16aap2.bigdoors.doors.windmill;

import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoor;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.tooluser.creator.Creator;
import nl.pim16aap2.bigdoors.tooluser.step.IStep;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CreatorWindMill extends Creator
{
    private static final DoorType DOOR_TYPE = DoorTypeWindmill.get();

    public CreatorWindMill(final IPPlayer player, final @Nullable String name)
    {
        super(player, name);
    }

    public CreatorWindMill(final IPPlayer player)
    {
        this(player, null);
    }

    @Override
    protected List<IStep> generateSteps()
        throws InstantiationException
    {
        return Arrays.asList(factorySetName.construct(),
                             factorySetFirstPos.messageKey("creator.windmill.step_1").construct(),
                             factorySetSecondPos.messageKey("creator.windmill.step_2").construct(),
                             factorySetEnginePos.messageKey("creator.windmill.step_3").construct(),
                             factorySetPowerBlockPos.construct(),
                             factorySetOpenDir.construct(),
                             factoryConfirmPrice.construct(),
                             factoryCompleteProcess.messageKey("creator.windmill.success").construct());
    }

    @Override
    protected void giveTool()
    {
        giveTool("tool_user.base.stick_name", "creator.windmill.stick_lore", "creator.windmill.init");
    }

    @Override
    protected AbstractDoor constructDoor()
    {
        return new Windmill(constructDoorData());
    }

    @Override
    protected DoorType getDoorType()
    {
        return DOOR_TYPE;
    }
}

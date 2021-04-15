package nl.pim16aap2.bigdoors.doors.revolvingdoor;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doors.bigdoor.CreatorBigDoor;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.tooluser.step.IStep;
import nl.pim16aap2.bigdoors.util.messages.Message;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class CreatorRevolvingDoor extends CreatorBigDoor
{
    @Getter(onMethod = @__({@Override}))
    private final @NonNull DoorType doorType = DoorTypeRevolvingDoor.get();

    public CreatorRevolvingDoor(final @NonNull IPPlayer player, final @Nullable String name)
    {
        super(player);
        if (name != null)
            completeNamingStep(name);
        prepareCurrentStep();
    }

    public CreatorRevolvingDoor(final @NonNull IPPlayer player)
    {
        this(player, null);
    }

    @Override
    protected @NonNull List<IStep> generateSteps()
        throws InstantiationException
    {
        return Arrays.asList(factorySetName.message(Message.CREATOR_GENERAL_GIVENAME).construct(),
                             factorySetFirstPos.message(Message.CREATOR_REVOLVINGDOOR_STEP1).construct(),
                             factorySetSecondPos.message(Message.CREATOR_REVOLVINGDOOR_STEP2).construct(),
                             factorySetEnginePos.message(Message.CREATOR_REVOLVINGDOOR_STEP3).construct(),
                             factorySetPowerBlockPos.message(Message.CREATOR_GENERAL_SETPOWERBLOCK).construct(),
                             factorySetOpenDir.message(Message.CREATOR_GENERAL_SETOPENDIR).construct(),
                             factoryConfirmPrice.message(Message.CREATOR_GENERAL_CONFIRMPRICE).construct(),
                             factoryCompleteProcess.message(Message.CREATOR_REVOLVINGDOOR_SUCCESS).construct());
    }

    @Override
    protected void giveTool()
    {
        giveTool(Message.CREATOR_GENERAL_STICKNAME, Message.CREATOR_REVOLVINGDOOR_STICKLORE,
                 Message.CREATOR_REVOLVINGDOOR_INIT);
    }

    @Override
    protected @NonNull AbstractDoorBase constructDoor()
    {
        return new RevolvingDoor(constructDoorData());
    }
}

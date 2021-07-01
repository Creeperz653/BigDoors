package nl.pim16aap2.bigdoors.doors.elevator;

import nl.pim16aap2.bigdoors.tooluser.creator.CreatorTestsUtil;
import nl.pim16aap2.bigdoors.api.util.Cuboid;
import nl.pim16aap2.bigdoors.api.util.RotateDirection;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreatorElevatorTest extends CreatorTestsUtil
{
    private static final int blocksToMove = 17;

    @BeforeEach
    void setup()
    {
        super.beforeEach();
    }

    @Test
    void createElevator()
    {
        engine = new Cuboid(min, max).getCenterBlock();
        openDirection = RotateDirection.UP;
        String openDirectionName = "0";

        final @NotNull Elevator actualDoor = new Elevator(constructDoorData(), blocksToMove);
        final @NotNull CreatorElevator creator = new CreatorElevator(player);
        testCreation(creator, actualDoor,
                     doorName,
                     min.toLocation(world),
                     max.toLocation(world),
                     powerblock.toLocation(world),
                     openDirectionName,
                     blocksToMove);
    }
}

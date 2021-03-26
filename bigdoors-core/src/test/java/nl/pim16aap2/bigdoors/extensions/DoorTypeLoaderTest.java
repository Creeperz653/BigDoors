package nl.pim16aap2.bigdoors.extensions;

import nl.pim16aap2.bigdoors.util.PLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

class DoorTypeLoaderTest
{
    @Test
    public void test()
    {
        PLogger.get().setConsoleLogLevel(Level.OFF);
        Assertions.assertEquals(9, DoorTypeLoader
            .get().loadDoorTypesFromDirectory("/home/pim/Documents/workspace/BigDoors2/bigdoors-doors/DoorTypes")
            .size());
    }
}

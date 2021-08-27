package nl.pim16aap2.bigdoors.commands;

import lombok.SneakyThrows;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.restartable.Restartable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static nl.pim16aap2.bigdoors.UnitTestUtil.initPlatform;

class RestartTest
{
    @Test
    @SneakyThrows
    void test()
    {
        initPlatform();
        final var restartable = Mockito.mock(Restartable.class);
        BigDoors.get().registerRestartable(restartable);

        final var commandSender = Mockito.mock(IPServer.class, Answers.CALLS_REAL_METHODS);

        Assertions.assertTrue(Restart.run(commandSender).get(1, TimeUnit.SECONDS));
        Mockito.verify(restartable).restart();
    }
}
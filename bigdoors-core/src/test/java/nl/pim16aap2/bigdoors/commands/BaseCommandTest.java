package nl.pim16aap2.bigdoors.commands;

import lombok.SneakyThrows;
import lombok.val;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.UnitTestUtil;
import nl.pim16aap2.bigdoors.api.IBigDoorsInnerPlatform;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.util.DoorAttribute;
import nl.pim16aap2.bigdoors.util.DoorRetriever;
import nl.pim16aap2.bigdoors.util.pair.BooleanPair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static nl.pim16aap2.bigdoors.commands.CommandTestingUtil.*;

class BaseCommandTest
{
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private BaseCommand baseCommand;

    private IBigDoorsInnerPlatform platform;

    @Mock
    private DoorRetriever doorRetriever;

    @Mock
    private AbstractDoorBase door;

    @Mock
    private ICommandSender commandSender;

    @BeforeEach
    void init()
    {
        platform = UnitTestUtil.initPlatform();
        MockitoAnnotations.openMocks(this);

        Mockito.when(baseCommand.getCommandSender()).thenReturn(commandSender);
        Mockito.when(baseCommand.getCommand()).thenReturn(CommandDefinition.ADD_OWNER);
        Mockito.when(baseCommand.validInput()).thenCallRealMethod();
        Mockito.when(baseCommand.hasPermission()).thenCallRealMethod();

        initCommandSenderPermissions(commandSender, true, true);
        initDoorRetriever(doorRetriever, door);
    }

    @Test
    @SneakyThrows
    void testHasAccess()
    {
        Assertions.assertTrue(baseCommand.hasAccessToAttribute(door, DoorAttribute.DELETE, true));
        Assertions.assertTrue(baseCommand.hasAccessToAttribute(door, DoorAttribute.DELETE, false));

        val player = Mockito.mock(IPPlayer.class, Answers.CALLS_REAL_METHODS);
        Mockito.when(baseCommand.getCommandSender()).thenReturn(player);

        Mockito.when(door.getDoorOwner(player)).thenReturn(Optional.of(doorOwner3));
        Assertions.assertFalse(baseCommand.hasAccessToAttribute(door, DoorAttribute.DELETE, false));
        Assertions.assertTrue(baseCommand.hasAccessToAttribute(door, DoorAttribute.DELETE, true));

        Mockito.when(door.getDoorOwner(player)).thenReturn(Optional.of(doorOwner1));
        Assertions.assertFalse(baseCommand.hasAccessToAttribute(door, DoorAttribute.DELETE, false));

        Mockito.when(door.getDoorOwner(player)).thenReturn(Optional.of(doorOwner0));
        Assertions.assertTrue(baseCommand.hasAccessToAttribute(door, DoorAttribute.DELETE, false));
    }

    @Test
    @SneakyThrows
    void testBasic()
    {
        Mockito.when(baseCommand.executeCommand(Mockito.any())).thenReturn(CompletableFuture.completedFuture(true));
        val result = baseCommand.run();
        Assertions.assertTrue(result.get(1, TimeUnit.SECONDS));
    }

    @Test
    @SneakyThrows
    void testNegativeExecution()
    {
        Mockito.when(baseCommand.executeCommand(Mockito.any())).thenReturn(CompletableFuture.completedFuture(false));
        val result = baseCommand.run();
        Assertions.assertFalse(result.get(1, TimeUnit.SECONDS));
    }

    @Test
    @SneakyThrows
    void invalidInput()
    {
        Mockito.when(baseCommand.validInput()).thenReturn(false);
        val result = baseCommand.run();
        Assertions.assertFalse(result.get(1, TimeUnit.SECONDS));
    }

    @Test
    @SneakyThrows
    void testPermissionFailure()
    {
        Mockito.when(baseCommand.executeCommand(Mockito.any())).thenReturn(CompletableFuture.completedFuture(true));
        Mockito.when(commandSender.hasPermission(Mockito.any(CommandDefinition.class)))
               .thenReturn(CompletableFuture.completedFuture(new BooleanPair(false, false)));

        val result = baseCommand.run();
        Assertions.assertTrue(result.get(1, TimeUnit.SECONDS));
    }

    @Test
    void testExceptionPermission()
    {
        BigDoors.get().getPLogger().setConsoleLogLevel(Level.OFF);

        Mockito.when(baseCommand.executeCommand(Mockito.any())).thenReturn(CompletableFuture.completedFuture(true));

        final CompletableFuture<BooleanPair> exceptional = new CompletableFuture<>();
        exceptional.completeExceptionally(new IllegalStateException("Testing exception!"));

        Mockito.when(commandSender.hasPermission(Mockito.any(CommandDefinition.class))).thenReturn(exceptional);

        ExecutionException exception =
            Assertions.assertThrows(ExecutionException.class,
                                    () -> baseCommand.startExecution().get(1, TimeUnit.SECONDS));
        Assertions.assertEquals(IllegalStateException.class, exception.getCause().getClass());
    }

    @Test
    void testExecutionException()
    {
        BigDoors.get().getPLogger().setConsoleLogLevel(Level.OFF);

        Mockito.when(baseCommand.executeCommand(Mockito.any())).thenReturn(CompletableFuture.completedFuture(true));
        final CompletableFuture<Boolean> exceptional = new CompletableFuture<>();
        exceptional.completeExceptionally(new IllegalStateException("Testing exception!"));

        Mockito.when(baseCommand.executeCommand(Mockito.any(BooleanPair.class))).thenReturn(exceptional);

        ExecutionException exception =
            Assertions.assertThrows(ExecutionException.class,
                                    () -> baseCommand.startExecution().get(1, TimeUnit.SECONDS));
        Assertions.assertEquals(IllegalStateException.class, exception.getCause().getCause().getCause().getClass());
    }
}

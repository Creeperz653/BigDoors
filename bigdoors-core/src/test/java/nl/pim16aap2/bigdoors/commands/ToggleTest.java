package nl.pim16aap2.bigdoors.commands;

import lombok.SneakyThrows;
import nl.pim16aap2.bigdoors.UnitTestUtil;
import nl.pim16aap2.bigdoors.api.IMessageable;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.factories.IPPlayerFactory;
import nl.pim16aap2.bigdoors.doors.AbstractDoor;
import nl.pim16aap2.bigdoors.doors.DoorToggleRequest;
import nl.pim16aap2.bigdoors.doors.DoorToggleRequestFactory;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.localization.ILocalizer;
import nl.pim16aap2.bigdoors.logging.BasicPLogger;
import nl.pim16aap2.bigdoors.logging.IPLogger;
import nl.pim16aap2.bigdoors.util.CompletableFutureHandler;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static nl.pim16aap2.bigdoors.commands.CommandTestingUtil.initCommandSenderPermissions;

class ToggleTest
{
    private DoorRetriever.AbstractRetriever doorRetriever;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private IPPlayer commandSender;

    @Mock
    private AbstractDoor door;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private DoorToggleRequest.IFactory doorToggleRequestIFactory;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private Toggle.IFactory factory;

    private DoorToggleRequestFactory doorToggleRequestFactory;

    @Mock
    private IMessageable messageableServer;

    @Mock
    private DoorToggleRequest doorToggleRequest;

    @SneakyThrows @BeforeEach
    void init()
    {
        MockitoAnnotations.openMocks(this);

        initCommandSenderPermissions(commandSender, true, true);

        Mockito.when(door.isDoorOwner(Mockito.any(UUID.class))).thenReturn(true);
        Mockito.when(door.isDoorOwner(Mockito.any(IPPlayer.class))).thenReturn(true);

        doorRetriever = DoorRetriever.ofDoor(door);

        final IPLogger logger = new BasicPLogger();
        final CompletableFutureHandler handler = new CompletableFutureHandler(logger);
        final ILocalizer localizer = UnitTestUtil.initLocalizer();

        Mockito.when(doorToggleRequestIFactory.create(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                      Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.any()))
               .thenReturn(doorToggleRequest);

        doorToggleRequestFactory = new DoorToggleRequestFactory(doorToggleRequestIFactory, messageableServer,
                                                                Mockito.mock(IPPlayerFactory.class));

        Mockito.when(factory.newToggle(Mockito.any(ICommandSender.class), Mockito.any(DoorActionType.class),
                                       Mockito.anyDouble(), Mockito.any()))
               .thenAnswer(
                   invoc ->
                   {
                       final DoorRetriever.AbstractRetriever[] retrievers =
                           UnitTestUtil.arrayFromCapturedVarArgs(DoorRetriever.AbstractRetriever.class, invoc, 3);

                       return new Toggle(invoc.getArgument(0, ICommandSender.class), logger, localizer,
                                         invoc.getArgument(1, DoorActionType.class),
                                         invoc.getArgument(2, Double.class), doorToggleRequestFactory,
                                         messageableServer, handler, retrievers);
                   });
    }

    @Test
    @SneakyThrows
    void testSuccess()
    {
        final Toggle toggle = factory.newToggle(commandSender, Toggle.DEFAULT_DOOR_ACTION_TYPE,
                                                Toggle.DEFAULT_SPEED_MULTIPLIER, doorRetriever);
        toggle.executeCommand(new BooleanPair(true, true)).get(1, TimeUnit.SECONDS);
        Mockito.verify(doorToggleRequestIFactory).create(doorRetriever, DoorActionCause.PLAYER, commandSender,
                                                         commandSender, 0.0D, false, DoorActionType.TOGGLE);

        Mockito.when(door.getDoorOwner(commandSender)).thenReturn(Optional.of(CommandTestingUtil.doorOwner0));
        toggle.executeCommand(new BooleanPair(true, false)).get(1, TimeUnit.SECONDS);
        Mockito.verify(doorToggleRequestIFactory, Mockito.times(2)).create(doorRetriever, DoorActionCause.PLAYER,
                                                                           commandSender, commandSender,
                                                                           0.0D, false, DoorActionType.TOGGLE);
    }

    @Test
    @SneakyThrows
    void testExecution()
    {
        // Ensure that supplying multiple door retrievers properly attempts toggling all of them.
        final int count = 10;
        final DoorRetriever.AbstractRetriever[] retrievers = new DoorRetriever.AbstractRetriever[count];
        for (int idx = 0; idx < count; ++idx)
        {
            final AbstractDoor newDoor = Mockito.mock(AbstractDoor.class);
            Mockito.when(newDoor.isDoorOwner(Mockito.any(UUID.class))).thenReturn(true);
            Mockito.when(newDoor.isDoorOwner(Mockito.any(IPPlayer.class))).thenReturn(true);
            retrievers[idx] = DoorRetriever.ofDoor(newDoor);
        }

        final Toggle toggle = factory.newToggle(commandSender, Toggle.DEFAULT_DOOR_ACTION_TYPE,
                                                Toggle.DEFAULT_SPEED_MULTIPLIER, retrievers);
        toggle.executeCommand(new BooleanPair(true, true)).get(1, TimeUnit.SECONDS);

        final Set<DoorRetriever.AbstractRetriever> toggledDoors =
            Mockito.mockingDetails(doorToggleRequestIFactory).getInvocations().stream()
                   .<DoorRetriever.AbstractRetriever>map(invocation -> invocation.getArgument(0))
                   .collect(Collectors.toSet());

        Assertions.assertEquals(count, toggledDoors.size());
        for (int idx = 0; idx < count; ++idx)
            Assertions.assertTrue(toggledDoors.contains(retrievers[idx]));
    }

    @Test
    @SneakyThrows
    void testParameters()
    {
        Mockito.when(door.isCloseable()).thenReturn(true);
        Mockito.when(door.isOpenable()).thenReturn(true);

        Assertions.assertTrue(factory.newToggle(commandSender, doorRetriever).run().get(1, TimeUnit.SECONDS));
        Mockito.verify(doorToggleRequestIFactory, Mockito.times(1))
               .create(doorRetriever, DoorActionCause.PLAYER, commandSender, commandSender,
                       Toggle.DEFAULT_SPEED_MULTIPLIER, false, DoorActionType.TOGGLE);


        Assertions.assertTrue(factory.newToggle(commandSender, 3.141592653589793D, doorRetriever).run()
                                     .get(1, TimeUnit.SECONDS));
        Mockito.verify(doorToggleRequestIFactory, Mockito.times(1))
               .create(doorRetriever, DoorActionCause.PLAYER, commandSender, commandSender,
                       3.141592653589793D, false, DoorActionType.TOGGLE);


        Assertions.assertTrue(
            factory.newToggle(commandSender, DoorActionType.CLOSE, doorRetriever).run().get(1, TimeUnit.SECONDS));
        Mockito.verify(doorToggleRequestIFactory, Mockito.times(1))
               .create(doorRetriever, DoorActionCause.PLAYER, commandSender, commandSender,
                       Toggle.DEFAULT_SPEED_MULTIPLIER, false, DoorActionType.CLOSE);


        Assertions.assertTrue(factory.newToggle(commandSender, DoorActionType.OPEN, 42, doorRetriever).run()
                                     .get(1, TimeUnit.SECONDS));
        Mockito.verify(doorToggleRequestIFactory, Mockito.times(1))
               .create(doorRetriever, DoorActionCause.PLAYER, commandSender,
                       commandSender, 42, false, DoorActionType.OPEN);
    }

    @Test
    @SneakyThrows
    void testServerCommandSender()
    {
        final IPServer serverCommandSender = Mockito.mock(IPServer.class, Answers.CALLS_REAL_METHODS);
        Assertions.assertTrue(factory.newToggle(serverCommandSender, DoorActionType.TOGGLE, doorRetriever).run()
                                     .get(1, TimeUnit.SECONDS));
        Mockito.verify(doorToggleRequestIFactory, Mockito.times(1))
               .create(doorRetriever, DoorActionCause.SERVER, messageableServer, null,
                       Toggle.DEFAULT_SPEED_MULTIPLIER, false, DoorActionType.TOGGLE);
    }

    private void verifyNoOpenerCalls()
    {
        Mockito.verify(doorToggleRequestIFactory, Mockito.never())
               .create(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                       Mockito.anyDouble(), Mockito.anyBoolean(), Mockito.any());
    }

    @Test
    @SneakyThrows
    void testAbort()
    {
        Mockito.when(door.isCloseable()).thenReturn(false);

        Assertions.assertTrue(factory.newToggle(commandSender, DoorActionType.CLOSE, doorRetriever).run()
                                     .get(1, TimeUnit.SECONDS));
        verifyNoOpenerCalls();

        Mockito.when(door.isCloseable()).thenReturn(true);
        initCommandSenderPermissions(commandSender, false, false);
        Mockito.when(door.getDoorOwner(Mockito.any(IPPlayer.class))).thenReturn(Optional.empty());

        Assertions.assertTrue(factory.newToggle(commandSender, DoorActionType.CLOSE, doorRetriever).run()
                                     .get(1, TimeUnit.SECONDS));
        verifyNoOpenerCalls();

        initCommandSenderPermissions(commandSender, true, false);
        Assertions.assertTrue(factory.newToggle(commandSender, DoorActionType.CLOSE, doorRetriever).run()
                                     .get(1, TimeUnit.SECONDS));
        verifyNoOpenerCalls();
    }
}
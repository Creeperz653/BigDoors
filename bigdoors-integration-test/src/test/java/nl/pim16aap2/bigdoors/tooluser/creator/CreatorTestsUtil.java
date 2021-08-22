package nl.pim16aap2.bigdoors.tooluser.creator;

import lombok.SneakyThrows;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IBigDoorsPlatform;
import nl.pim16aap2.bigdoors.api.IBigDoorsToolUtil;
import nl.pim16aap2.bigdoors.api.IConfigLoader;
import nl.pim16aap2.bigdoors.api.IEconomyManager;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.api.IPermissionsManager;
import nl.pim16aap2.bigdoors.api.IProtectionCompatManager;
import nl.pim16aap2.bigdoors.api.PPlayerData;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.managers.DatabaseManager;
import nl.pim16aap2.bigdoors.managers.DoorRegistry;
import nl.pim16aap2.bigdoors.managers.PowerBlockManager;
import nl.pim16aap2.bigdoors.managers.ToolUserManager;
import nl.pim16aap2.bigdoors.testimplementations.TestPLocationFactory;
import nl.pim16aap2.bigdoors.testimplementations.TestPPlayer;
import nl.pim16aap2.bigdoors.testimplementations.TestPPlayerFactory;
import nl.pim16aap2.bigdoors.testimplementations.TestPWorld;
import nl.pim16aap2.bigdoors.testimplementations.TestPWorldFactory;
import nl.pim16aap2.bigdoors.util.DoorOwner;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.messages.Messages;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CreatorTestsUtil
{
    protected static final TestPPlayer PLAYER =
        new TestPPlayer(new PPlayerData(UUID.fromString("f373bb8d-dd2d-496e-a9c5-f9a0c45b2db5"),
                                        "user", 8, 9, true, true));

    protected @NotNull Vector3Di min = new Vector3Di(10, 15, 20);
    protected @NotNull Vector3Di max = new Vector3Di(20, 25, 30);
    protected @NotNull Vector3Di engine = new Vector3Di(20, 15, 25);
    protected @NotNull Vector3Di powerblock = new Vector3Di(40, 40, 40);
    protected @NotNull String doorName = "testDoor123";
    protected @NotNull IPWorld world = new TestPWorld("world");
    protected @NotNull IPWorld world2 = new TestPWorld("world2");
    protected @NotNull RotateDirection openDirection = RotateDirection.COUNTERCLOCKWISE;

    protected final @NotNull DoorOwner doorOwner = new DoorOwner(-1, 0, PLAYER.getPPlayerData());

    @Captor ArgumentCaptor<AbstractDoorBase> doorInsertCaptor;

    @Mock
    protected DatabaseManager databaseManager;

    @Mock
    protected IBigDoorsPlatform platform;

    @Mock
    protected PowerBlockManager powerBlockManager;

    @Mock
    protected IEconomyManager economyManager;

    @Mock
    protected IConfigLoader configLoader;

    @Mock
    protected IPermissionsManager permissionsManager;

    @BeforeEach
    protected void beforeEach()
    {
        MockitoAnnotations.openMocks(this);
        BigDoors.get().setBigDoorsPlatform(platform);

        Messages messages = new Messages(platform, new File("src/test/resources"),
                                         "en_US_TEST", BigDoors.get().getPLogger());

        Mockito.when(platform.getDatabaseManager()).thenReturn(databaseManager);
        Mockito.when(platform.getEconomyManager()).thenReturn(economyManager);
        Mockito.when(platform.getPowerBlockManager()).thenReturn(powerBlockManager);
        Mockito.when(platform.getMessages()).thenReturn(messages);
        Mockito.when(platform.getBigDoorsToolUtil()).thenReturn(Mockito.mock(IBigDoorsToolUtil.class));
        Mockito.when(platform.getPLocationFactory()).thenReturn(new TestPLocationFactory());
        Mockito.when(platform.getPPlayerFactory()).thenReturn(new TestPPlayerFactory());
        Mockito.when(platform.getPWorldFactory()).thenReturn(new TestPWorldFactory());
        Mockito.when(platform.getProtectionCompatManager()).thenReturn(Mockito.mock(IProtectionCompatManager.class));
        Mockito.when(platform.getConfigLoader()).thenReturn(configLoader);
        Mockito.when(platform.getPermissionsManager()).thenReturn(permissionsManager);
        Mockito.when(platform.getDoorRegistry()).thenReturn(DoorRegistry.uncached());
        Mockito.when(platform.getToolUserManager()).thenReturn(Mockito.mock(ToolUserManager.class));

        // Immediately return whatever door was being added to the database as if it was successful.
        Mockito.when(databaseManager.addDoorBase(ArgumentMatchers.any())).thenAnswer(
            (Answer<CompletableFuture<Optional<AbstractDoorBase>>>) invocation ->
                CompletableFuture.completedFuture(Optional.of((AbstractDoorBase) invocation.getArguments()[0])));

        Mockito.when(permissionsManager.hasPermission(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(true);

        Mockito.when(configLoader.maxDoorSize()).thenReturn(OptionalInt.empty());
        Mockito.when(configLoader.maxDoorCount()).thenReturn(OptionalInt.empty());
        Mockito.when(configLoader.maxPowerBlockDistance()).thenReturn(OptionalInt.empty());
        Mockito.when(configLoader.maxBlocksToMove()).thenReturn(OptionalInt.empty());
    }

    protected void setEconomyEnabled(boolean status)
    {
        Mockito.when(economyManager.isEconomyEnabled()).thenReturn(status);
    }

    protected void setEconomyPrice(double price)
    {
        Mockito.when(economyManager.getPrice(ArgumentMatchers.any(), ArgumentMatchers.anyInt()))
               .thenReturn(OptionalDouble.of(price));
    }

    protected void setBuyDoor(boolean status)
    {
        Mockito.when(economyManager.buyDoor(ArgumentMatchers.any(), ArgumentMatchers.any(),
                                            ArgumentMatchers.any(), ArgumentMatchers.anyInt()))
               .thenReturn(status);
    }


    protected AbstractDoorBase.DoorData constructDoorData()
    {
        return new AbstractDoorBase.DoorData(-1, doorName, min, max, engine, powerblock, world,
                                             false, false, openDirection, doorOwner);
    }

    @SneakyThrows
    public void testCreation(final @NotNull Creator creator, AbstractDoorBase actualDoor,
                             final @NotNull Object... input)
    {
        setEconomyEnabled(false);

        int idx = 0;
        for (Object obj : input)
            Assertions.assertTrue(creator.handleInput(obj),
                                  String.format("IDX: %d, Input: %s, Step: %s Error Message: %s",
                                                (idx++), obj.toString(), creator.getCurrentStep().getName(),
                                                PLAYER.getBeforeLastMessage()));

        Mockito.verify(databaseManager).addDoorBase(doorInsertCaptor.capture());
        AbstractDoorBase resultDoor = doorInsertCaptor.getValue();
        Assertions.assertNotNull(resultDoor);
        Assertions.assertEquals(actualDoor, resultDoor);
    }

}
package nl.pim16aap2.bigdoors.tooluser.creator;

import junit.framework.Assert;
import nl.pim16aap2.bigdoors.UnitTestUtil;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.storage.IStorage;
import nl.pim16aap2.bigdoors.testimplementations.TestConfigLoader;
import nl.pim16aap2.bigdoors.testimplementations.TestEconomyManager;
import nl.pim16aap2.bigdoors.testimplementations.TestPPlayer;
import nl.pim16aap2.bigdoors.util.DoorOwner;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class CreatorTestsUtil
{
    protected static final TestPPlayer PLAYER =
        new TestPPlayer(UUID.fromString("f373bb8d-dd2d-496e-a9c5-f9a0c45b2db5"), "user");
    protected static ExecutorService threadPool;

    static
    {
        UnitTestUtil.setupStatic();
    }

    @NotNull
    protected TestConfigLoader getConfigLoader()
    {
        return (TestConfigLoader) UnitTestUtil.PLATFORM.getConfigLoader();
    }

    @NotNull
    protected TestEconomyManager getEconomyManager()
    {
        return (TestEconomyManager) UnitTestUtil.PLATFORM.getEconomyManager();
    }

    @Mock
    protected IStorage fakeStorage;

    @BeforeEach
    public void initMocks()
    {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeEach
    public void setupFakeDatabaseManager()
    {
        try
        {
            UnitTestUtil.setDatabaseStorage(fakeStorage);
            threadPool = UnitTestUtil.getDatabaseManagerThreadPool();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail();
        }
        Assert.assertNotNull(threadPool);
    }

    // Set up basic stuff.
    @BeforeAll
    public static void basicSetup()
    {
        PLogger.get().setConsoleLogging(true);
        PLogger.get().setOnlyLogExceptions(true);
    }

    protected @NotNull Vector3Di min = new Vector3Di(10, 15, 20);
    protected @NotNull Vector3Di max = new Vector3Di(20, 25, 30);
    protected @NotNull Vector3Di engine = new Vector3Di(20, 15, 25);
    protected @NotNull Vector3Di powerblock = new Vector3Di(40, 40, 40);
    protected @NotNull String doorName = "testDoor123";
    protected @NotNull IPWorld world =
        UnitTestUtil.PLATFORM.getPWorldFactory().create(UUID.fromString("f373bb8d-dd2d-496e-a9c5-f9a0c45b2db8"));
    protected @NotNull IPWorld world2 =
        UnitTestUtil.PLATFORM.getPWorldFactory().create(UUID.fromString("9ba0de97-01ef-4b4f-b12c-025ff84a6931"));
    protected @NotNull RotateDirection openDirection = RotateDirection.COUNTERCLOCKWISE;

    protected final @NotNull DoorOwner doorOwner = new DoorOwner(-1, 0, PLAYER);


    protected AbstractDoorBase.DoorData constructDoorData()
    {
        return new AbstractDoorBase.DoorData(-1, doorName, min, max, engine, powerblock, world, false, openDirection,
                                             doorOwner, false);
    }


    /**
     * Sets up hijacking of any calls to the fake database ({@link #fakeStorage}) to insert a new door.
     *
     * @return The reference to the {@link AbstractDoorBase} that was supposed to be inserted into the database.
     */
    @NotNull
    protected AtomicReference<AbstractDoorBase> setupInsertHijack()
    {
        // Door insertion is handled by a thread pool (take a look at the DatabaseManager).
        // When capturing the method call to insert the door, the result will be stored on this AtomicReference,
        // So that when it is compared to the actual door later on, it'll be possible to fail the test if it doesn't
        // match (which isn't possible from a secondary thread).
        final @NotNull AtomicReference<AbstractDoorBase> resultDoorRef = new AtomicReference<>();

        // Capture the call to the fake database that is supposed to insert the door. Then chcck if the door being
        // inserted is the door we will try to create.
        Mockito.when(fakeStorage.insert(Mockito.any())).thenAnswer((Answer<Boolean>) invocation ->
        {
            Object[] args = invocation.getArguments();
            // Yeah, this may throw all kinds of exceptions and whatnot. However, because of it being run on a separate
            // thread, it doesn't matter. It'll either set the variable, or fail to do so.
            // In case of an SQL exception, it'll end up in the log anyway.
            resultDoorRef.set(((AbstractDoorBase) args[0]));

            return Boolean.TRUE;
        });

        return resultDoorRef;
    }

    public void testCreation(final @NotNull Creator creator, AbstractDoorBase actualDoor,
                             final @NotNull Object... input)
        throws InterruptedException
    {
        ((TestEconomyManager) UnitTestUtil.PLATFORM.getEconomyManager()).isEconomyEnabled = false;
        final @NotNull AtomicReference<AbstractDoorBase> resultDoorRef = setupInsertHijack();

        int idx = 0;
        for (Object obj : input)
            Assert.assertTrue(String.format("IDX: %d, Input: %s, Step: %s Error Message: %s",
                                            (idx++), obj.toString(), creator.getCurrentStep().getName(),
                                            PLAYER.getBeforeLastMessage()),
                              creator.handleInput(obj));

        // Wait for the thread pool to finish inserting the door etc,
        threadPool.awaitTermination(20L, TimeUnit.MILLISECONDS);
        AbstractDoorBase resultDoor = resultDoorRef.get();
        Assert.assertNotNull(resultDoor);
        Assert.assertEquals(actualDoor, resultDoor);
    }

}
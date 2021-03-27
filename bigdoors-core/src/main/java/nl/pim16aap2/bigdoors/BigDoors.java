package nl.pim16aap2.bigdoors;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IBigDoorsPlatform;
import nl.pim16aap2.bigdoors.api.IMessagingInterface;
import nl.pim16aap2.bigdoors.api.restartable.RestartableHolder;
import nl.pim16aap2.bigdoors.logging.BasicPLogger;
import nl.pim16aap2.bigdoors.logging.IPLogger;
import nl.pim16aap2.bigdoors.managers.AutoCloseScheduler;
import nl.pim16aap2.bigdoors.managers.DatabaseManager;
import nl.pim16aap2.bigdoors.managers.DoorActivityManager;
import nl.pim16aap2.bigdoors.managers.DoorRegistry;
import nl.pim16aap2.bigdoors.managers.DoorSpecificationManager;
import nl.pim16aap2.bigdoors.managers.DoorTypeManager;
import nl.pim16aap2.bigdoors.managers.PowerBlockManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the core class of BigDoors.
 *
 * @author Pim
 */
public final class BigDoors extends RestartableHolder
{
    private static final @NonNull BigDoors INSTANCE = new BigDoors();

    private IPLogger backupLogger;

    /**
     * The platform to use. e.g. "Spigot".
     */
    private @Nullable IBigDoorsPlatform platform;

    private BigDoors()
    {
    }

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static @NotNull BigDoors get()
    {
        return INSTANCE;
    }

    /**
     * Sets the platform implementing BigDoor's internal API.
     *
     * @param platform The platform implementing BigDoor's internal API.
     */
    public void setBigDoorsPlatform(final @NotNull IBigDoorsPlatform platform)
    {
        if (this.platform != null)
            this.platform.deregisterRestartable(this);
        this.platform = platform;
        this.platform.registerRestartable(this);
    }

    /**
     * gets the platform implementing BigDoor's internal API.
     *
     * @return The platform implementing BigDoor's internal API.
     */
    public @NonNull IBigDoorsPlatform getPlatform()
    {
        if (platform == null)
        {
            IllegalStateException e = new IllegalStateException("No platform currently registered!");
            getPLogger().logThrowable(e);
            throw e;
        }
        return platform;
    }

    /**
     * Gets the {@link DoorRegistry}.
     *
     * @return The {@link DoorRegistry}.
     */
    public @NonNull DoorRegistry getDoorRegistry()
    {
        return getPlatform().getDoorRegistry();
    }

    /**
     * Gets the {@link PowerBlockManager}.
     *
     * @return The {@link PowerBlockManager}.
     */
    public @NonNull PowerBlockManager getPowerBlockManager()
    {
        return getPlatform().getPowerBlockManager();
    }

    /**
     * Gets the {@link DoorActivityManager} instance.
     *
     * @return The {@link DoorActivityManager} instance.
     */
    public @NonNull DoorActivityManager getDoorActivityManager()
    {
        return getPlatform().getDoorActivityManager();
    }

    /**
     * Gets the {@link AutoCloseScheduler} instance.
     *
     * @return The {@link AutoCloseScheduler} instance.
     */
    public @NotNull AutoCloseScheduler getAutoCloseScheduler()
    {
        return getPlatform().getAutoCloseScheduler();
    }

    /**
     * Gets the {@link DoorSpecificationManager} instance.
     *
     * @return The {@link DoorSpecificationManager} instance.
     */
    public @NotNull DoorSpecificationManager getDoorSpecificationManager()
    {
        return getPlatform().getDoorSpecificationManager();
    }

    /**
     * Gets the {@link DoorTypeManager} instance.
     *
     * @return The {@link DoorTypeManager} instance.
     */
    public @NotNull DoorTypeManager getDoorTypeManager()
    {
        return getPlatform().getDoorTypeManager();
    }

    /**
     * Gets the currently used {@link IMessagingInterface}. If the current one isn't set, {@link
     * IBigDoorsPlatform#getMessagingInterface} is used instead.
     *
     * @return The currently used {@link IMessagingInterface}.
     */
    public @NotNull IMessagingInterface getMessagingInterface()
    {
        return getPlatform().getMessagingInterface();
    }

    /**
     * Gets the currently set {@link IPLogger}.
     *
     * @return The currently set {@link IPLogger}..
     */
    public @NonNull IPLogger getPLogger()
    {
        if (platform == null || getPlatform().getPLogger() == null)
            return backupLogger == null ? backupLogger = new BasicPLogger() : backupLogger;
        return getPlatform().getPLogger();
    }

    /**
     * Gets the {@link DatabaseManager}.
     *
     * @return The {@link DatabaseManager}.
     */
    public @NonNull DatabaseManager getDatabaseManager()
    {
        return getPlatform().getDatabaseManager();
    }
}

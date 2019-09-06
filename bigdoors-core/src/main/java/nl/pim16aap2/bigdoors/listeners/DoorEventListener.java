package nl.pim16aap2.bigdoors.listeners;

import com.google.common.base.Preconditions;
import nl.pim16aap2.bigdoors.doors.DoorOpener;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a listener that keeps track of BigDoors related events.
 *
 * @author Pim
 */
public class DoorEventListener implements Listener
{
    @Nullable
    private static DoorEventListener instance;
    @NotNull
    private final DoorOpener doorOpener;

    private DoorEventListener(final @NotNull DoorOpener doorOpener)
    {
        this.doorOpener = doorOpener;
    }

    /**
     * Initializes the {@link DoorEventListener}. If it has already been initialized, it'll return that instance
     * instead.
     *
     * @param doorOpener The {@link DoorOpener} used to open, close, and toggle doors.
     * @return The instance of this {@link DoorEventListener}.
     */
    public static DoorEventListener init(final @NotNull DoorOpener doorOpener)
    {
        return (instance == null) ? instance = new DoorEventListener(doorOpener) : instance;
    }

    /**
     * Gets the instance of the {@link DoorEventListener} if it exists.
     *
     * @return The instance of the {@link DoorEventListener}.
     */
    @NotNull
    public static DoorEventListener get()
    {
        Preconditions.checkState(instance != null,
                                 "Instance has not yet been initialized. Be sure #init() has been invoked");
        return instance;
    }
}

package nl.pim16aap2.bigdoors.spigot.events;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.doors.IDoorBase;
import nl.pim16aap2.bigdoors.api.events.IDoorPrepareCreateEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the event where a door will be created.
 *
 * @author Pim
 */
@ToString
public class DoorPrepareCreateEvent extends DoorEvent implements IDoorPrepareCreateEvent
{
    private static final @NotNull HandlerList HANDLERS_LIST = new HandlerList();

    @Getter
    @Setter
    private boolean isCancelled = false;

    public DoorPrepareCreateEvent(final @NotNull IDoorBase door, final @Nullable IPPlayer responsible)
    {
        super(door, responsible);
    }

    @Override
    public @NotNull IDoorBase getDoor()
    {
        return super.getDoor();
    }

    @Override
    public @NotNull HandlerList getHandlers()
    {
        return HANDLERS_LIST;
    }

    public static @NotNull HandlerList getHandlerList()
    {
        return HANDLERS_LIST;
    }
}

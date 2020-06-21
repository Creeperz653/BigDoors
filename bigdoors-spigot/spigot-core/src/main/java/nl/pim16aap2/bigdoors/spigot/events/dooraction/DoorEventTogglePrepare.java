package nl.pim16aap2.bigdoors.spigot.events.dooraction;

import lombok.Getter;
import lombok.Setter;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.events.dooraction.IDoorEventTogglePrepare;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an action that is going to be applied to a door.
 *
 * @author Pim
 */
public class DoorEventTogglePrepare extends DoorToggleEvent implements IDoorEventTogglePrepare
{
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /**
     * {@inheritDoc}
     */
    @Getter(onMethod = @__({@Override}))
    @Setter(onMethod = @__({@Override}))
    private boolean isCancelled = false;

    /**
     * Constructs a door action event.
     *
     * @param door             The door.
     * @param cause            What caused the action.
     * @param actionType       The type of action.
     * @param responsible      Who is responsible for this door. If null, the door's owner will be used.
     * @param time             The number of seconds the door will take to open. Note that there are other factors that
     *                         affect the total time as well.
     * @param animationSkipped If true, the door will skip the animation and open instantly.
     */
    public DoorEventTogglePrepare(final @NotNull AbstractDoorBase door, final @NotNull DoorActionCause cause,
                                  final @NotNull DoorActionType actionType, final @Nullable IPPlayer responsible,
                                  final double time, final boolean animationSkipped)
    {
        super(door, cause, actionType, responsible, time, animationSkipped);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public HandlerList getHandlers()
    {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList()
    {
        return HANDLERS_LIST;
    }
}

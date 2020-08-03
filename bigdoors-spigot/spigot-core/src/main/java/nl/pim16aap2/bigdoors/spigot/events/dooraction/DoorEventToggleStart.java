package nl.pim16aap2.bigdoors.spigot.events.dooraction;

import lombok.Getter;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.events.dooraction.IDoorEventToggleStart;
import nl.pim16aap2.bigdoors.util.vector.IVector3DiConst;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link IDoorEventToggleStart} for the Spigot platform.
 *
 * @author Pim
 */
public class DoorEventToggleStart extends DoorToggleEvent implements IDoorEventToggleStart
{
    /** {@inheritDoc} */
    @Getter(onMethod = @__({@Override}))
    @NotNull
    IVector3DiConst newMinimum;

    /** {@inheritDoc} */
    @Getter(onMethod = @__({@Override}))
    @NotNull
    IVector3DiConst newMaximum;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    /**
     * Constructs a door action event.
     *
     * @param door          The door.
     * @param cause         What caused the action.
     * @param actionType    The type of action.
     * @param responsible   Who is responsible for this door. This player may be online, but does not have to be.
     * @param time          The number of seconds the door will take to open. Note that there are other factors that
     *                      affect the total time as well.
     * @param skipAnimation If true, the door will skip the animation and open instantly.
     * @param newMinimum    The new minimum coordinates of the door after the toggle.
     * @param newMaximum    The new maximum coordinates of the door after the toggle.
     */
    public DoorEventToggleStart(final @NotNull AbstractDoorBase door, final @NotNull DoorActionCause cause,
                                final @NotNull DoorActionType actionType, final @NotNull IPPlayer responsible,
                                final double time, final boolean skipAnimation,
                                final @NotNull IVector3DiConst newMinimum, final @NotNull IVector3DiConst newMaximum)
    {
        super(door, cause, actionType, responsible, time, skipAnimation);
        this.newMinimum = newMinimum;
        this.newMaximum = newMaximum;
    }

    /** {@inheritDoc} */
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

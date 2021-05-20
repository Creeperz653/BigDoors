package nl.pim16aap2.bigdoors.events.dooraction;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.events.IBigDoorsEvent;

public interface IDoorToggleEvent extends IBigDoorsEvent
{
    /**
     * Gets the door that is the subject of this event.
     *
     * @return The door.
     */
    @NonNull AbstractDoorBase getDoor();

    /**
     * Gets what caused the door action request to be created.
     *
     * @return The cause of the door action request.
     */
    @NonNull DoorActionCause getCause();

    /**
     * Gets the UUID of the player responsible for this door action. This either means the player who directly requested
     * this action or, if it was requested indirectly (e.g. via redstone), the prime owner of the door. Therefore, this
     * player might not be online.
     *
     * @return The player that is responsible for this event.
     */
    @NonNull IPPlayer getResponsible();

    /**
     * Gets the type of the requested action.
     *
     * @return The type of the requested action.
     */
    @NonNull DoorActionType getActionType();

    /**
     * Checks if the event requested the door to skip its animation and open instantly.
     *
     * @return True if the event requested the door to skip its animation and open instantly.
     */
    boolean isAnimationSkipped();

    /**
     * Gets requested duration of the animation. This may differ from the final duration based on other factors (such as
     * speed limits).
     *
     * @return The requested duration of the animation (in seconds).
     */
    double getTime();
}

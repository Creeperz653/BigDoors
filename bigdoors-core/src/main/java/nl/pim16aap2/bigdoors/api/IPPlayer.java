package nl.pim16aap2.bigdoors.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a BigDoors player.
 *
 * @author Pim
 */
// TODO: This might be better suited as an abstract class, as the implementation of the basic stuff is always going to
//       be the same (equals, hashCode, getName, etc).
public interface IPPlayer extends IMessageable
{
    /**
     * Gets the name of this player.
     *
     * @return The name of this player.
     */
    @NotNull
    String getName();

    /**
     * Gets the UUID of this player.
     *
     * @return The UUID of this player.
     */
    @NotNull
    UUID getUUID();

    /**
     * Gets the player as a String in the format '"playerName" (playerUUID)'.
     *
     * @return The player as a String.
     */
    @NotNull
    default String asString()
    {
        return String.format("\"%s\" (%s)", getName(), getUUID().toString());
    }
}
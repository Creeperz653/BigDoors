package nl.pim16aap2.bigdoors.api;

import nl.pim16aap2.bigdoors.api.util.WorldTime;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a BigDoors world.
 *
 * @author Pim
 */
public interface IPWorld extends Cloneable
{
    /**
     * Gets the name of this world.
     *
     * @return The name of this world.
     */
    @NotNull String getWorldName();

    /**
     * Checks if this is a valid world in the current {@link IBigDoorsPlatform}.
     *
     * @return True if this is a valid world in the current {@link IBigDoorsPlatform}.
     */
    boolean exists();

    /**
     * Gets the time in this world.
     *
     * @return Gets the time in this world.
     */
    @NotNull WorldTime getTime();

    @NotNull IPWorld clone();
}

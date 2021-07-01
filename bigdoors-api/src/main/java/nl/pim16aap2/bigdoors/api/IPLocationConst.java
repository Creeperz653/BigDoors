package nl.pim16aap2.bigdoors.api;

import nl.pim16aap2.bigdoors.util.vector.Vector2Di;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a const position in a world.
 *
 * @author Pim
 */
public interface IPLocationConst extends Cloneable
{
    /**
     * Gets the world of this location.
     *
     * @return The world of this location.
     */
    @NotNull IPWorld getWorld();

    /**
     * Gets the chunk coordinates of the chunk this location is in.
     *
     * @return The chunk coordinates of this location.
     */
    default @NotNull Vector2Di getChunk()
    {
        return new Vector2Di(getBlockX() << 4, getBlockZ() << 4);
    }

    /**
     * Gets the X value of this location.
     *
     * @return The X value of this location.
     */
    int getBlockX();

    /**
     * Gets the Y value of this location.
     *
     * @return The Y value of this location.
     */
    int getBlockY();

    /**
     * Gets the Z value of this location.
     *
     * @return The Z value of this location.
     */
    int getBlockZ();

    /**
     * Gets the X value of this location.
     *
     * @return The X value of this location.
     */
    double getX();

    /**
     * Gets the Y value of this location.
     *
     * @return The Y value of this location.
     */
    double getY();

    /**
     * Gets the Z value of this location.
     *
     * @return The Z value of this location.
     */
    double getZ();

    default @NotNull Vector3Di getPosition()
    {
        return new Vector3Di(getBlockX(), getBlockY(), getBlockZ());
    }

    /**
     * Gets the position (so no world) in integers as a String.
     *
     * @return The position in integers as a String.
     */
    default @NotNull String toIntPositionString()
    {
        return String.format("(%d;%d;%d)", getBlockX(), getBlockY(), getBlockZ());
    }

    /**
     * Gets the position (so no world) in double as a String.
     *
     * @return The position in double as a String.
     */
    default @NotNull String toDoublePositionString()
    {
        return String.format("(%.2f;%.2f;%.2f)", getX(), getY(), getZ());
    }
}

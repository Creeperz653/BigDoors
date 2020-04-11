package nl.pim16aap2.bigdoors.api.factories;

import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.util.vector.Vector3Dd;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a factory for {@link IPLocation} objects.
 *
 * @author Pim
 */
public interface IPLocationFactory
{
    /**
     * Creates a new IPLocation.
     *
     * @param world The world.
     * @param x     The x coordinate.
     * @param y     The y coordinate.
     * @param z     The z coordinate.
     * @return A new IPLocation object.
     */
    @NotNull
    IPLocation create(final @NotNull IPWorld world, final double x, final double y, final double z);

    /**
     * Creates a new IPLocation.
     *
     * @param world    The world.
     * @param position The position in the world
     * @return A new IPLocation object.
     */
    @NotNull
    IPLocation create(final @NotNull IPWorld world, final @NotNull Vector3Di position);

    /**
     * Creates a new IPLocation.
     *
     * @param world    The world.
     * @param position The position in the world
     * @return A new IPLocation object.
     */
    @NotNull
    IPLocation create(final @NotNull IPWorld world, final @NotNull Vector3Dd position);

    /**
     * Creates a new IPLocation.
     *
     * @param worldUUID The uuid of the world.
     * @param x         The x coordinate.
     * @param y         The y coordinate.
     * @param z         The z coordinate.
     * @return A new IPLocation object.
     */
    @NotNull
    IPLocation create(final @NotNull UUID worldUUID, final double x, final double y, final double z);

    /**
     * Creates a new IPLocation.
     *
     * @param worldUUID The uuid of the world.
     * @param position  The position in the world
     * @return A new IPLocation object.
     */
    @NotNull
    IPLocation create(final @NotNull UUID worldUUID, final @NotNull Vector3Di position);

    /**
     * Creates a new IPLocation.
     *
     * @param worldUUID The uuid of the world.
     * @param position  The position in the world
     * @return A new IPLocation object.
     */
    @NotNull
    IPLocation create(final @NotNull UUID worldUUID, final @NotNull Vector3Dd position);
}
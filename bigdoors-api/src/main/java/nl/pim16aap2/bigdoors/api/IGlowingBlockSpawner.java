package nl.pim16aap2.bigdoors.api;

import nl.pim16aap2.bigdoors.doors.IDoorBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Represents a glowing block used for highlights
 *
 * @author Pim
 */
public interface IGlowingBlockSpawner
{
    /**
     * Spawns a glowing block.
     *
     * @param player   The player who will see the glowing block.
     * @param world    The world in which the glowing block will be spawned
     * @param time     How long the glowing block will be visible.
     * @param timeUnit The unit of the <code>time</code>. Note that one tick is 50ms, so that is the absolute lowest
     *                 value possible! Any values less than that will mean nothing is spawned.
     * @param x        The x-coordinate of the glowing block. An offset of 0.5 is applied to make it align by default.
     * @param y        The y-coordinate of the glowing block.
     * @param z        The z-coordinate of the glowing block. An offset of 0.5 is applied to make it align by default.
     * @param color    The color of the outline.
     * @return The {@link IGlowingBlock} that was spawned.
     */
    @NotNull Optional<IGlowingBlock> spawnGlowingBlock(@NotNull IPPlayer player, @NotNull IPWorld world,
                                                       int time, @NotNull TimeUnit timeUnit, double x,
                                                       double y, double z, @NotNull PColor color);

    /**
     * Spawns a glowing block.
     *
     * @param player The player who will see the glowing block.
     * @param world  The world in which the glowing block will be spawned
     * @param time   How long the glowing block will be visible (in seconds).
     * @param x      The x-coordinate of the glowing block. An offset of 0.5 is applied to make it align by default.
     * @param y      The y-coordinate of the glowing block.
     * @param z      The z-coordinate of the glowing block. An offset of 0.5 is applied to make it align by default.
     * @param color  The color of the outline.
     * @return The {@link IGlowingBlock} that was spawned.
     */
    default @NotNull Optional<IGlowingBlock> spawnGlowingBlock(@NotNull IPPlayer player,
                                                               @NotNull IPWorld world, int time,
                                                               double x, double y, double z,
                                                               @NotNull PColor color)
    {
        return spawnGlowingBlock(player, world, time, TimeUnit.SECONDS, x, y, z, color);
    }

    /**
     * Spawns a glowing block.
     *
     * @param player   The player who will see the glowing block.
     * @param world    The world in which the glowing block will be spawned
     * @param time     How long the glowing block will be visible.
     * @param timeUnit The unit of the <code>time</code>. Note that one tick is 50ms, so that is the absolute lowest
     *                 value possible! Any values less than that will mean nothing is spawned.
     * @param x        The x-coordinate of the glowing block. An offset of 0.5 is applied to make it align by default.
     * @param y        The y-coordinate of the glowing block.
     * @param z        The z-coordinate of the glowing block. An offset of 0.5 is applied to make it align by default.
     * @return The {@link IGlowingBlock} that was spawned.
     */
    default @NotNull Optional<IGlowingBlock> spawnGlowingBlock(@NotNull IPPlayer player,
                                                               @NotNull IPWorld world, int time,
                                                               @NotNull TimeUnit timeUnit, double x,
                                                               double y, double z)
    {
        return spawnGlowingBlock(player, world, time, timeUnit, x, y, z, PColor.WHITE);
    }

    /**
     * Spawns a glowing block.
     *
     * @param player The player who will see the glowing block.
     * @param world  The world in which the glowing block will be spawned
     * @param time   How long the glowing block will be visible (in seconds).
     * @param x      The x-coordinate of the glowing block. An offset of 0.5 is applied to make it align by default.
     * @param y      The y-coordinate of the glowing block.
     * @param z      The z-coordinate of the glowing block. An offset of 0.5 is applied to make it align by default.
     * @return The {@link IGlowingBlock} that was spawned.
     */
    default @NotNull Optional<IGlowingBlock> spawnGlowingBlock(@NotNull IPPlayer player,
                                                               @NotNull IPWorld world, int time,
                                                               double x, double y, double z)
    {
        return spawnGlowingBlock(player, world, time, x, y, z, PColor.WHITE);
    }

    /**
     * Spawns a glowing block.
     *
     * @param player   The player who will see the glowing block.
     * @param time     How long the glowing block will be visible (in seconds).
     * @param timeUnit The unit of the <code>time</code>. Note that one tick is 50ms, so that is the absolute lowest
     *                 value possible! Any values less than that will mean nothing is spawned.
     * @param location The location where the glowing block will be spawned.
     * @return The {@link IGlowingBlock} that was spawned.
     */
    default @NotNull Optional<IGlowingBlock> spawnGlowingBlock(@NotNull IPPlayer player, int time,
                                                               @NotNull TimeUnit timeUnit,
                                                               @NotNull IPLocationConst location)
    {
        return spawnGlowingBlock(player, location.getWorld(), time, timeUnit, location.getX(), location.getY(),
                                 location.getZ());
    }

    /**
     * Spawns a glowing block.
     *
     * @param player   The player who will see the glowing block.
     * @param time     How long the glowing block will be visible (in seconds).
     * @param location The location where the glowing block will be spawned.
     * @return The {@link IGlowingBlock} that was spawned.
     */
    default @NotNull Optional<IGlowingBlock> spawnGlowingBlock(@NotNull IPPlayer player, int time,
                                                               @NotNull IPLocationConst location)
    {
        return spawnGlowingBlock(player, location.getWorld(), time, location.getX(), location.getY(), location.getZ());
    }

    /**
     * Spawns the glowing blocks required to highlight a door.
     *
     * @param doorBase The door to highlight.
     * @param player   The {@link IPPlayer} for whom to highlight the door.
     * @return The list of {@link IGlowingBlock}s that were spawned.
     */
    default @NotNull List<IGlowingBlock> spawnGlowingBlocks(@NotNull IDoorBase doorBase,
                                                            @NotNull IPPlayer player)
    {
        List<IGlowingBlock> ret = new ArrayList<>(4);
        IPWorld world = doorBase.getWorld();

        spawnGlowingBlock(player, world, 15, doorBase.getPowerBlock().getX(), doorBase.getPowerBlock().getY(),
                          doorBase.getPowerBlock().getZ(), PColor.GOLD);
        spawnGlowingBlock(player, world, 15, doorBase.getEngine().getX(), doorBase.getEngine().getY(),
                          doorBase.getEngine().getZ(), PColor.DARK_PURPLE);
        spawnGlowingBlock(player, world, 15, doorBase.getMinimum().getX(), doorBase.getMinimum().getY(),
                          doorBase.getMinimum().getZ(), PColor.BLUE);
        spawnGlowingBlock(player, world, 15, doorBase.getMaximum().getX(), doorBase.getMaximum().getY(),
                          doorBase.getMaximum().getZ(), PColor.RED);
        return ret;
    }

    /**
     * Spawns a glowing block.
     *
     * @param player   The player who will see the glowing block.
     * @param location The location where the glowing block will be spawned.
     * @param time     How long the glowing block will be visible (in seconds).
     * @param timeUnit The unit of the <code>time</code>. Note that one tick is 50ms, so that is the absolute lowest
     *                 value possible! Any values less than that will mean nothing is spawned.
     * @param color    The color of the outline.
     * @return The {@link IGlowingBlock} that was spawned.
     */
    default @NotNull Optional<IGlowingBlock> spawnGlowingBlock(@NotNull IPPlayer player, int time,
                                                               @NotNull TimeUnit timeUnit,
                                                               @NotNull IPLocationConst location,
                                                               @NotNull PColor color)
    {
        return spawnGlowingBlock(player, location.getWorld(), time, timeUnit, location.getX(), location.getY(),
                                 location.getZ(), color);
    }

    /**
     * Spawns a glowing block.
     *
     * @param player   The player who will see the glowing block.
     * @param location The location where the glowing block will be spawned.
     * @param time     How long the glowing block will be visible (in seconds).
     * @param color    The color of the outline.
     * @return The {@link IGlowingBlock} that was spawned.
     */
    default @NotNull Optional<IGlowingBlock> spawnGlowingBlock(@NotNull IPPlayer player, int time,
                                                               @NotNull IPLocationConst location,
                                                               @NotNull PColor color)
    {
        return spawnGlowingBlock(player, location.getWorld(), time, location.getX(), location.getY(), location.getZ(),
                                 color);
    }
}

package nl.pim16aap2.bigdoors.spigotutil;

import nl.pim16aap2.bigdoors.util.PBlockFace;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.Vector3D;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents various small and Spigot-specific utility functions.
 *
 * @author Pim
 */
public final class SpigotUtil
{
    private static final Map<PBlockFace, BlockFace> toBlockFace = new EnumMap<>(PBlockFace.class);
    private static final Map<BlockFace, PBlockFace> toPBlockFace = new EnumMap<>(BlockFace.class);
    public static boolean printDebugMessages = false;

    static
    {
        for (PBlockFace pbf : PBlockFace.values())
        {
            BlockFace mappedBlockFace;
            if (pbf.equals(PBlockFace.NONE))
                mappedBlockFace = BlockFace.SELF;
            else
                mappedBlockFace = BlockFace.valueOf(pbf.toString());
            toBlockFace.put(pbf, mappedBlockFace);
            toPBlockFace.put(mappedBlockFace, pbf);
        }
    }

    /**
     * Gets the number are available (i.e. either air or liquid blocks) in a given direction for a certain area. Note
     * that the result may be negative depending on the direction.
     * <p>
     * For example, when checking how many blocks are available in downwards direction, it will return -5 if 5 blocks
     * under the area are available.
     *
     * @param min       The minimum coordinates of the area.
     * @param max       The maximum coordinates of the area.
     * @param maxDist   The maximum number of blocks to check.
     * @param direction The direction to check.
     * @param world     The world in which to check.
     * @return The number are available in a given direction. Can be negative depending on the direction.
     */
    public static int getBlocksInDir(final @NotNull Location min, final @NotNull Location max, int maxDist,
                                     final @NotNull PBlockFace direction, final @NotNull World world)
    {
        int startX, startY, startZ, endX, endY, endZ, countX = 0, countY = 0, countZ = 0;
        Vector3D vec = PBlockFace.getDirection(direction);
        maxDist = Math.abs(maxDist);

        startX = vec.getX() == 0 ? min.getBlockX() : vec.getX() == 1 ? max.getBlockX() + 1 : min.getBlockX() - 1;
        startY = vec.getY() == 0 ? min.getBlockY() : vec.getY() == 1 ? max.getBlockY() + 1 : min.getBlockY() - 1;
        startZ = vec.getZ() == 0 ? min.getBlockZ() : vec.getZ() == 1 ? max.getBlockZ() + 1 : min.getBlockZ() - 1;

        endX = vec.getX() == 0 ? max.getBlockX() : startX + vec.getX() * maxDist;
        endY = vec.getY() == 0 ? max.getBlockY() : startY + vec.getY() * maxDist;
        endZ = vec.getZ() == 0 ? max.getBlockZ() : startZ + vec.getZ() * maxDist;

        int stepX = vec.getX() == 0 ? 1 : vec.getX();
        int stepY = vec.getY() == 0 ? 1 : vec.getY();
        int stepZ = vec.getZ() == 0 ? 1 : vec.getZ();

        int ret = 0;
        if (vec.getX() != 0)
            for (int xAxis = startX; xAxis != endX + 1; ++xAxis)
            {
                for (int zAxis = startZ; zAxis != endZ; zAxis += stepZ)
                    for (int yAxis = startY; yAxis != endY + 1; ++yAxis)
                        if (!SpigotUtil.isAirOrLiquid(world.getBlockAt(xAxis, yAxis, zAxis)))
                            return ret;
                ret += stepX;
            }
        else if (vec.getY() != 0)
            for (int yAxis = startY; yAxis != endY + 1; ++yAxis)
            {
                for (int zAxis = startZ; zAxis != endZ; zAxis += stepZ)
                    for (int xAxis = startX; xAxis != endX + 1; ++xAxis)
                        if (!SpigotUtil.isAirOrLiquid(world.getBlockAt(xAxis, yAxis, zAxis)))
                            return ret;
                ret += stepY;
            }
        else if (vec.getZ() != 0)
        {
            for (int zAxis = startZ; zAxis != endZ; zAxis += stepZ)
            {
                for (int xAxis = startX; xAxis != endX + 1; ++xAxis)
                    for (int yAxis = startY; yAxis != endY + 1; ++yAxis)
                        if (!SpigotUtil.isAirOrLiquid(world.getBlockAt(xAxis, yAxis, zAxis)))
                            return ret;
                ret += stepZ;
            }
        }
        else
            ret = 0;
        return ret;
    }

    /**
     * Send a colored message to a specific player.
     *
     * @param player The player that will receive the message.
     * @param color  Color of the message
     * @param msg    The message to be sent.
     */
    public static void messagePlayer(final @NotNull Player player, final @NotNull ChatColor color,
                                     final @NotNull String msg)
    {
        player.sendMessage(color + msg);
    }

    /**
     * Convert a command and its explanation to the help format.
     *
     * @param command     Name of the command.
     * @param explanation Explanation of how to use the command.
     * @return String in the helperformat.
     */
    @NotNull
    public static String helpFormat(final @NotNull String command, final @NotNull String explanation)
    {
        return String.format(ChatColor.GREEN + "/%s: " + ChatColor.BLUE + "%s\n", command, explanation);
    }

    /**
     * Get the {@link PBlockFace} parallel to the given {@link org.bukkit.block.BlockFace}.
     *
     * @param mbf {@link PBlockFace} that will be converted.
     * @return The parallel {@link org.bukkit.block.BlockFace}.
     */
    @NotNull
    public static BlockFace getBukkitFace(final @NotNull PBlockFace mbf)
    {
        return toBlockFace.get(mbf);
    }

    /**
     * Get the {@link org.bukkit.block.BlockFace} parallel to the given {@link PBlockFace}.
     *
     * @param bf {@link org.bukkit.block.BlockFace} that will be converted.
     * @return The parallel {@link PBlockFace}.
     */
    @NotNull
    public static PBlockFace getPBlockFace(final @NotNull BlockFace bf)
    {
        return toPBlockFace.get(bf);
    }

    /**
     * Broadcast a message if debugging is enabled in the config.
     *
     * @param message The message to broadcast.
     */
    public static void broadcastMessage(final @NotNull String message)
    {
        if (printDebugMessages)
            Bukkit.broadcastMessage(message);
    }

    /**
     * Convert a location to a nicely formatted string of x:y:z using integers.
     *
     * @param loc The location to convert to a string.
     * @return A string of the coordinates of the location.
     */
    @NotNull
    public static String locIntToString(final @NotNull Location loc)
    {
        return String.format("(%d;%d;%d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    /**
     * Convert a location to a nicely formatted string of x:y:z using doubles rounded to 2 decimals.
     *
     * @param loc The location to convert to a string.
     * @return A string of the coordinates of the location.
     */
    @NotNull
    public static String locDoubleToString(final @NotNull Location loc)
    {
        return String.format("(%.2f;%.2f;%.2f)", loc.getX(), loc.getY(), loc.getZ());
    }

    @NotNull
    public static Optional<String> nameFromUUID(final @NotNull UUID playerUUID)
    {
        Player player = Bukkit.getPlayer(playerUUID);
        return Optional
            .ofNullable(player != null ? player.getName() : Bukkit.getOfflinePlayer(playerUUID).getName());
    }

    /**
     * Try to get a player's UUID from a given name.
     *
     * @param playerName Name of the player.
     * @return UUID of the player if one was found, otherwise null.
     */
    /*
     * First try to get the UUID from an online player, then try an offline player;
     * the first option is faster.
     */
    @NotNull
    public static Optional<UUID> playerUUIDFromString(final @NotNull String playerName)
    {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null)
            try
            {
                player = Bukkit.getPlayer(UUID.fromString(playerName));
            }
            catch (Exception dontcare)
            {
            }
        if (player != null)
            /*
             * Check if the resulting player's name is a match to the provided playerName,
             * because player retrieval from a name is not exact. "pim" would match
             * "pim16aap2", for example.
             */
            return Optional.ofNullable(player.getName().equals(playerName) ? player.getUniqueId() : null);

        OfflinePlayer offPlayer = null;
        try
        {
            offPlayer = Bukkit.getOfflinePlayer(UUID.fromString(playerName));
        }
        catch (Exception dontcare)
        {
        }
        return Optional.ofNullable(
            offPlayer == null ? null : offPlayer.getName().equals(playerName) ? offPlayer.getUniqueId() : null);
    }

    /**
     * Play a sound for all players in a range of 15 blocks around the provided location.
     *
     * @param loc    The location of the sound.
     * @param sound  The name of the sound.
     * @param volume The volume
     * @param pitch  The pitch
     */
    public static void playSound(final @NotNull Location loc, final @NotNull String sound, final float volume,
                                 final float pitch)
    {
        for (Entity ent : loc.getWorld().getNearbyEntities(loc, 15, 15, 15))
            if (ent instanceof Player)
                ((Player) ent).playSound(loc, sound, volume, pitch);
    }

    /**
     * Retrieve the number of doors a given player is allowed to won.
     *
     * @param player The player for whom to retrieve the limit.
     * @return The limit if one was found, or -1 if unlimited.
     */
    public static int getMaxDoorsForPlayer(final @NotNull Player player)
    {
        if (player.isOp())
            return -1;
        return getHighestPermissionSuffix(player, "bigdoors.own.");
    }

    /**
     * Retrieve the limit of the door size (measured in blocks) a given player can own.
     *
     * @param player The player for whom to retrieve the limit.
     * @return The limit if one was found, or -1 if unlimited.
     */
    public static int getMaxDoorSizeForPlayer(final @NotNull Player player)
    {
        if (player.isOp())
            return -1;
        return getHighestPermissionSuffix(player, "bigdoors.maxsize.");
    }

    /**
     * Get the highest value of a variable in a permission node of a player.
     * <p>
     * For example, retrieve '8' from 'permission.node.8'.
     *
     * @param player         The player whose permissions to check.
     * @param permissionNode The base permission node.
     * @return The highest value of the variable suffix of the permission node or -1 if none was found.
     */
    private static int getHighestPermissionSuffix(final @NotNull Player player,
                                                  final @NotNull String permissionNode)
    {
        int ret = -1;
        for (PermissionAttachmentInfo perms : player.getEffectivePermissions())
            if (perms.getPermission().startsWith(permissionNode))
                try
                {
                    ret = Math.max(ret, Integer.parseInt(perms.getPermission().split(permissionNode)[1]));
                }
                catch (Exception e)
                {
                }
        return ret;
    }

    /**
     * Send a white message to a player.
     *
     * @param player Player to receive the message.
     * @param msg    The message.
     */
    public static void messagePlayer(final @NotNull Player player, final @NotNull String msg)
    {
        messagePlayer(player, ChatColor.WHITE, msg);
    }

    /**
     * Send a number message to a player.
     *
     * @param player The player that will receive the message
     * @param msg    The messages
     */
    public static void messagePlayer(final @NotNull Player player, final @NotNull String[] msg)
    {
        messagePlayer(player, Util.stringFromArray(msg));
    }

    /**
     * Send a number of messages to a player.
     *
     * @param player The player that will receive the message
     * @param color  The color of the message
     * @param msg    The messages
     */
    public static void messagePlayer(final @NotNull Player player, final @NotNull ChatColor color,
                                     final @NotNull String[] msg)
    {
        messagePlayer(player, color, Util.stringFromArray(msg));
    }

    /**
     * Check if a block if air or liquid (water, lava).
     *
     * @param block The block to be checked.
     * @return True if it is air or liquid.
     */
    public static boolean isAirOrLiquid(final @NotNull Block block)
    {
        // Empty means it's air.
        return block.isLiquid() || block.isEmpty();
    }

    /**
     * Certain material types need to be refreshed when being placed down.
     *
     * @param mat Material to be checked.
     * @return True if it needs to be refreshed.
     *
     * @deprecated I'm pretty sure this is no longer needed.
     */
    @Deprecated
    public static boolean needsRefresh(final @NotNull Material mat)
    {
        switch (mat)
        {
            case ACACIA_FENCE:
            case ACACIA_FENCE_GATE:
            case BIRCH_FENCE:
            case BIRCH_FENCE_GATE:
            case DARK_OAK_FENCE:
            case DARK_OAK_FENCE_GATE:
            case JUNGLE_FENCE:
            case JUNGLE_FENCE_GATE:
            case OAK_FENCE:
            case OAK_FENCE_GATE:
            case SPRUCE_FENCE:
            case SPRUCE_FENCE_GATE:
            case NETHER_BRICK_FENCE:

            case COBBLESTONE_WALL:
            case IRON_BARS:

            case WHITE_STAINED_GLASS_PANE:
            case YELLOW_STAINED_GLASS_PANE:
            case PURPLE_STAINED_GLASS_PANE:
            case LIGHT_BLUE_STAINED_GLASS_PANE:
            case MAGENTA_STAINED_GLASS_PANE:
            case GRAY_STAINED_GLASS_PANE:
            case GREEN_STAINED_GLASS_PANE:
            case BLACK_STAINED_GLASS_PANE:
            case LIME_STAINED_GLASS_PANE:
            case BLUE_STAINED_GLASS_PANE:
            case BROWN_STAINED_GLASS_PANE:
            case CYAN_STAINED_GLASS_PANE:
            case RED_STAINED_GLASS_PANE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Check if a block is on the blacklist of types/materials that is not allowed for animations.
     *
     * @param block The block to be checked
     * @return True if the block can be used for animations.
     */
    public static boolean isAllowedBlock(final @Nullable Block block)
    {
        if (block == null || isAirOrLiquid(block))
            return false;

        BlockData blockData = block.getBlockData();
        BlockState blockState = block.getState();

        if (blockData instanceof org.bukkit.block.data.type.Stairs ||
            blockData instanceof org.bukkit.block.data.type.Gate)
            return true;

        if (blockState instanceof org.bukkit.inventory.InventoryHolder
            // Door, Stairs, TrapDoor, sunflower, tall grass, tall seagrass, large fern,
            // peony, rose bush, lilac,
            || blockData instanceof org.bukkit.block.data.Bisected ||
            blockData instanceof org.bukkit.block.data.Rail
            // Cauldron, Composter, Water, Lava
            || blockData instanceof org.bukkit.block.data.Levelled

            || blockData instanceof org.bukkit.block.data.type.Bed ||
            blockData instanceof org.bukkit.block.data.type.BrewingStand ||
            blockData instanceof org.bukkit.block.data.type.Cake ||
            blockData instanceof org.bukkit.block.data.type.CommandBlock ||
            blockData instanceof org.bukkit.block.data.type.EnderChest ||
            blockData instanceof org.bukkit.block.data.type.Ladder ||
            blockData instanceof org.bukkit.block.data.type.Sapling ||
            blockData instanceof org.bukkit.block.data.type.Sign ||
            blockData instanceof org.bukkit.block.data.type.TechnicalPiston ||
            blockData instanceof org.bukkit.block.data.type.WallSign ||
            blockData instanceof org.bukkit.block.data.type.RedstoneWire ||
            blockData instanceof org.bukkit.block.data.type.RedstoneWallTorch ||
            blockData instanceof org.bukkit.block.data.type.Tripwire ||
            blockData instanceof org.bukkit.block.data.type.TripwireHook ||
            blockData instanceof org.bukkit.block.data.type.Repeater ||
            blockData instanceof org.bukkit.block.data.type.Switch ||
            blockData instanceof org.bukkit.block.data.type.Comparator)
            return false;

        Material mat = block.getType();
        switch (mat)
        {
            case WALL_TORCH:

            case PAINTING:

            case ATTACHED_MELON_STEM:
            case ATTACHED_PUMPKIN_STEM:
            case WHITE_TULIP:
            case DANDELION:
            case SUGAR_CANE:
            case NETHER_WART:
            case CHORUS_FLOWER:
            case CHORUS_FRUIT:
            case SEAGRASS:
            case POPPY:
            case OXEYE_DAISY:
            case LILY_OF_THE_VALLEY:
            case LILY_PAD:
            case VINE:
                return false;
            default:
                break;
        }

        String matName = mat.toString();
        // Potted stuff will always work.
        if (matName.startsWith("POTTED"))
            return true;
        if (matName.endsWith("TULIP") || matName.endsWith("BANNER") || matName.endsWith("CARPET") ||
            matName.endsWith("HEAD"))
            return false;
        return true;
    }

    @Deprecated
    public static int tickRateFromSpeed(final double speed)
    {
        int tickRate;
        if (speed > 9)
            tickRate = 1;
        else if (speed > 7)
            tickRate = 2;
        else if (speed > 6)
            tickRate = 3;
        else
            tickRate = 4;
        return tickRate;
    }

    // Return {time, tickRate, distanceMultiplier} for a given door size.
    @Deprecated
    public static double[] calculateTimeAndTickRate(final int doorSize, double time,
                                                    final double speedMultiplier,
                                                    final double baseSpeed)
    {
        double ret[] = new double[3];
        double distance = Math.PI * doorSize / 2;
        if (time == 0.0)
            time = baseSpeed + doorSize / 3.5;
        double speed = distance / time;
        if (speedMultiplier != 1.0 && speedMultiplier != 0.0)
        {
            speed *= speedMultiplier;
            time = distance / speed;
        }

        // Too fast or too slow!
        double maxSpeed = 11;
        if (speed > maxSpeed || speed <= 0)
            time = distance / maxSpeed;

        double distanceMultiplier = speed > 4 ? 1.01 : speed > 3.918 ? 1.08 : speed > 3.916 ? 1.10 :
                                                                              speed > 2.812 ? 1.12 :
                                                                              speed > 2.537 ? 1.19 :
                                                                              speed > 2.2 ? 1.22 :
                                                                              speed > 2.0 ? 1.23 :
                                                                              speed > 1.770 ?
                                                                              1.25 :
                                                                              speed > 1.570 ?
                                                                              1.28 : 1.30;
        ret[0] = time;
        ret[1] = tickRateFromSpeed(speed);
        ret[2] = distanceMultiplier;
        return ret;
    }
}
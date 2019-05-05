package nl.pim16aap2.bigdoors.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public final class Util
{
    // Send a message to a player in a specific color.
    public static void messagePlayer(Player player, ChatColor color, String s)
    {
        player.sendMessage(color + s);
    }

    public static String helpFormat(String command, String explanation)
    {
        return String.format(ChatColor.GREEN + "/%s: " + ChatColor.BLUE + "%s\n", command, explanation);
    }

    public static String errorToString(Error e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static String exceptionToString(Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static void broadcastMessage(String message)
    {
//        if (ConfigLoader.DEBUG)
        Bukkit.broadcastMessage(message);
    }

    public static String locIntToString(Location loc)
    {
        return String.format("(%d;%d;%d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static String locDoubleToString(Location loc)
    {
        return String.format("(%.2f;%.2f;%.2f)", loc.getX(), loc.getY(), loc.getZ());
    }

    public static long chunkHashFromLocation(Location loc)
    {
        return chunkHashFromLocation(loc.getBlockX(), loc.getBlockZ(), loc.getWorld().getUID());
    }

    public static long chunkHashFromLocation(int x, int z, UUID worldUUID)
    {
        int chunk_X = x >> 4;
        int chunk_Z = z >> 4;
        long hash = 3;
        hash = 19 * hash + worldUUID.hashCode();
        hash = 19 * hash + (int) (Double.doubleToLongBits(chunk_X) ^ (Double.doubleToLongBits(chunk_X) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(chunk_Z) ^ (Double.doubleToLongBits(chunk_Z) >>> 32));
        return hash;
    }

    // Doors aren't allowed to have numerical names, to differentiate doorNames from
    // doorUIDs.
    public static boolean isValidDoorName(String name)
    {
        try
        {
            Integer.parseInt(name);
            return false;
        }
        catch (NumberFormatException e)
        {
            return true;
        }
    }

    public static long locationHash(Location loc)
    {
        return loc.hashCode();
    }

    public static long locationHash(int x, int y, int z, UUID worldUUID)
    {
        return locationHash(new Location(Bukkit.getWorld(worldUUID), x, y, z));
    }

    static final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom srnd = new SecureRandom();
    static Random rnd = new Random();

    public static String randomString(int length)
    {
        StringBuilder sb = new StringBuilder(length);
        for (int idx = 0; idx != length; ++idx)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public static String secureRandomString(int length)
    {
        StringBuilder sb = new StringBuilder(length);
        for (int idx = 0; idx != length; ++idx)
            sb.append(chars.charAt(srnd.nextInt(chars.length())));
        return sb.toString();
    }

    public static String nameFromUUID(UUID playerUUID)
    {
        if (playerUUID == null)
            return null;
        String output = null;
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null)
            output = player.getName();
        else
            output = Bukkit.getOfflinePlayer(playerUUID).getName();
        return output;
    }

    public static String playerUUIDStrFromString(String input)
    {
        UUID playerUUID = playerUUIDFromString(input);
        return playerUUID == null ? null : playerUUID.toString();
    }

    public static UUID playerUUIDFromString(String input)
    {
        Player player = null;
        player = Bukkit.getPlayer(input);
        if (player == null)
            try
            {
                player = Bukkit.getPlayer(UUID.fromString(input));
            }
            catch (Exception dontcare)
            {
            }
        if (player != null)
            return player.getName().equals(input) ? player.getUniqueId() : null;

        OfflinePlayer offPlayer = null;
        try
        {
            offPlayer = Bukkit.getOfflinePlayer(UUID.fromString(input));
        }
        catch (Exception dontcare)
        {
        }
        if (offPlayer != null)
            return offPlayer.getName().equals(input) ? offPlayer.getUniqueId() : null;
        return null;
    }

    // Play sound at a location.
    public static void playSound(Location loc, String sound, float volume, float pitch)
    {
        for (Entity ent : loc.getWorld().getNearbyEntities(loc, 15, 15, 15))
            if (ent instanceof Player)
                ((Player) ent).playSound(loc, sound, volume, pitch);
    }

    public static int getMaxDoorsForPlayer(Player player)
    {
        if (player.isOp())
            return -1;
        return getHighestPermissionSuffix(player, "bigdoors.own.");
    }

    public static int getMaxDoorSizeForPlayer(Player player)
    {
        if (player.isOp())
            return -1;
        return getHighestPermissionSuffix(player, "bigdoors.maxsize.");
    }

    private static int getHighestPermissionSuffix(Player player, String permissionNode)
    {
        int ret = -1;
        for (PermissionAttachmentInfo perms : player.getEffectivePermissions())
            if (perms.getPermission().startsWith(permissionNode))
                try
                {
                    ret = Math.max(ret, Integer.valueOf(perms.getPermission().split(permissionNode)[1]));
                }
                catch (Exception e)
                {
                }
        return ret;
    }

    public static double doubleFromString(String input, double defaultVal)
    {
        try
        {
            return input == null ? defaultVal : Double.parseDouble(input);
        }
        catch (NumberFormatException e)
        {
            return defaultVal;
        }
    }

    public static long longFromString(String input, long defaultVal)
    {
        try
        {
            return input == null ? defaultVal : Long.parseLong(input);
        }
        catch (NumberFormatException e)
        {
            return defaultVal;
        }
    }

    // Send a message to a player.
    public static void messagePlayer(Player player, String s)
    {
        messagePlayer(player, ChatColor.WHITE, s);
    }

    public static String stringFromArray(String[] strings)
    {
        StringBuilder builder = new StringBuilder();
        for (String str : strings)
            builder.append(str);
        return builder.toString();
    }

    // Send an array of messages to a player.
    public static void messagePlayer(Player player, String[] str)
    {
        messagePlayer(player, stringFromArray(str));
    }

    // Send an array of messages to a player.
    public static void messagePlayer(Player player, ChatColor color, String[] str)
    {
        messagePlayer(player, color, stringFromArray(str));
    }

    public static boolean isAirOrWater(Material mat)
    {
        return mat.equals(Material.AIR) || mat.equals(Material.WATER) || mat.equals(Material.LAVA);
    }

    // Logs, stairs and glass panes can rotate, but they don't rotate in exactly the
    // same way.
    public static int canRotate(Material mat)
    {
        if (mat.equals(Material.ACACIA_LOG) || mat.equals(Material.BIRCH_LOG) || mat.equals(Material.DARK_OAK_LOG)
            || mat.equals(Material.JUNGLE_LOG) || mat.equals(Material.OAK_LOG) || mat.equals(Material.SPRUCE_LOG))
            return 1;
        if (mat.equals(Material.ACACIA_STAIRS) || mat.equals(Material.BIRCH_STAIRS) || mat.equals(Material.BRICK_STAIRS)
            || mat.equals(Material.COBBLESTONE_STAIRS) || mat.equals(Material.DARK_OAK_STAIRS)
            || mat.equals(Material.JUNGLE_STAIRS) || mat.equals(Material.NETHER_BRICK_STAIRS)
            || mat.equals(Material.PURPUR_STAIRS) || mat.equals(Material.QUARTZ_STAIRS)
            || mat.equals(Material.RED_SANDSTONE_STAIRS) || mat.equals(Material.SANDSTONE_STAIRS)
            || mat.equals(Material.PRISMARINE_STAIRS) || mat.equals(Material.DARK_PRISMARINE_STAIRS)
            || mat.equals(Material.SPRUCE_STAIRS) || mat.equals(Material.OAK_STAIRS)
            || mat.equals(Material.PRISMARINE_BRICK_STAIRS) || mat.equals(Material.RED_SANDSTONE_STAIRS)
            || mat.equals(Material.STONE_BRICK_STAIRS))
            return 2;
        if (mat.equals(Material.WHITE_STAINED_GLASS) || mat.equals(Material.YELLOW_STAINED_GLASS)
            || mat.equals(Material.PURPLE_STAINED_GLASS) || mat.equals(Material.LIGHT_BLUE_STAINED_GLASS)
            || mat.equals(Material.GRAY_STAINED_GLASS) || mat.equals(Material.GREEN_STAINED_GLASS)
            || mat.equals(Material.BLACK_STAINED_GLASS) || mat.equals(Material.LIME_STAINED_GLASS)
            || mat.equals(Material.BLUE_STAINED_GLASS) || mat.equals(Material.BROWN_STAINED_GLASS)
            || mat.equals(Material.CYAN_STAINED_GLASS) || mat.equals(Material.RED_STAINED_GLASS)
            || mat.equals(Material.MAGENTA_STAINED_GLASS) || mat.equals(Material.WHITE_STAINED_GLASS_PANE)
            || mat.equals(Material.YELLOW_STAINED_GLASS_PANE) || mat.equals(Material.PURPLE_STAINED_GLASS_PANE)
            || mat.equals(Material.LIGHT_BLUE_STAINED_GLASS_PANE) || mat.equals(Material.GRAY_STAINED_GLASS_PANE)
            || mat.equals(Material.GREEN_STAINED_GLASS_PANE) || mat.equals(Material.BLACK_STAINED_GLASS_PANE)
            || mat.equals(Material.LIME_STAINED_GLASS_PANE) || mat.equals(Material.BLUE_STAINED_GLASS_PANE)
            || mat.equals(Material.BROWN_STAINED_GLASS_PANE) || mat.equals(Material.CYAN_STAINED_GLASS_PANE)
            || mat.equals(Material.RED_STAINED_GLASS_PANE) || mat.equals(Material.MAGENTA_STAINED_GLASS_PANE))
            return 3;
        if (mat.equals(Material.ANVIL))
            return 4;
        if (mat.equals(Material.COBBLESTONE_WALL))
            return 5;
        if (mat.equals(Material.STRIPPED_ACACIA_LOG) || mat.equals(Material.STRIPPED_BIRCH_LOG)
            || mat.equals(Material.STRIPPED_SPRUCE_LOG) || mat.equals(Material.STRIPPED_DARK_OAK_LOG)
            || mat.equals(Material.STRIPPED_JUNGLE_LOG) || mat.equals(Material.STRIPPED_OAK_LOG))
            return 6;
        if (mat.equals(Material.END_ROD))
            return 7;
        return 0;
    }

    public static boolean needsRefresh(Material xmat)
    {
        switch (xmat)
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

    // Certain blocks don't work in doors, so don't allow their usage.
    public static boolean isAllowedBlock(Material mat)
    {
        if (mat == null)
            return false;

        switch (mat)
        {
        case AIR:
        case WATER:
        case LAVA:

        case ARMOR_STAND:
        case BREWING_STAND:
        case CAULDRON:
        case CHEST:
        case DROPPER:
        case DRAGON_EGG:
        case ENDER_CHEST:
        case HOPPER:
        case JUKEBOX:
        case PAINTING:
        case ACACIA_SIGN:
        case ACACIA_WALL_SIGN:
        case BIRCH_SIGN:
        case BIRCH_WALL_SIGN:
        case DARK_OAK_SIGN:
        case DARK_OAK_WALL_SIGN:
        case JUNGLE_SIGN:
        case JUNGLE_WALL_SIGN:
        case OAK_SIGN:
        case OAK_WALL_SIGN:
        case SPRUCE_SIGN:
        case SPRUCE_WALL_SIGN:
        case SPAWNER:
        case FURNACE:
        case FURNACE_MINECART:
        case CAKE:

        case WHITE_SHULKER_BOX:
        case YELLOW_SHULKER_BOX:
        case PURPLE_SHULKER_BOX:
        case LIGHT_BLUE_SHULKER_BOX:
        case GRAY_SHULKER_BOX:
        case GREEN_SHULKER_BOX:
        case BLACK_SHULKER_BOX:
        case LIME_SHULKER_BOX:
        case BLUE_SHULKER_BOX:
        case BROWN_SHULKER_BOX:
        case CYAN_SHULKER_BOX:
        case RED_SHULKER_BOX:

        case ACACIA_TRAPDOOR:
        case BIRCH_TRAPDOOR:
        case DARK_OAK_TRAPDOOR:
        case IRON_TRAPDOOR:
        case JUNGLE_TRAPDOOR:
        case OAK_TRAPDOOR:
        case SPRUCE_TRAPDOOR:
        case ACACIA_DOOR:
        case BIRCH_DOOR:
        case IRON_DOOR:
        case JUNGLE_DOOR:
        case OAK_DOOR:
        case SPRUCE_DOOR:
        case DARK_OAK_DOOR:

        case CREEPER_HEAD:
        case CREEPER_WALL_HEAD:
        case DRAGON_HEAD:
        case PISTON_HEAD:
        case PLAYER_HEAD:
        case PLAYER_WALL_HEAD:
        case ZOMBIE_HEAD:
        case ZOMBIE_WALL_HEAD:

        case RAIL:
        case DETECTOR_RAIL:
        case ACTIVATOR_RAIL:
        case POWERED_RAIL:

        case REDSTONE:
        case REDSTONE_WIRE:
        case TRAPPED_CHEST:
        case TRIPWIRE:
        case TRIPWIRE_HOOK:
        case REDSTONE_TORCH:
        case REDSTONE_WALL_TORCH:
        case TORCH:

        case BLACK_CARPET:
        case BLUE_CARPET:
        case BROWN_CARPET:
        case CYAN_CARPET:
        case GRAY_CARPET:
        case GREEN_CARPET:
        case LIGHT_BLUE_CARPET:
        case LIGHT_GRAY_CARPET:
        case LIME_CARPET:
        case MAGENTA_CARPET:
        case ORANGE_CARPET:
        case PINK_CARPET:
        case PURPLE_CARPET:
        case RED_CARPET:
        case WHITE_CARPET:
        case YELLOW_CARPET:

        case ACACIA_BUTTON:
        case BIRCH_BUTTON:
        case DARK_OAK_BUTTON:
        case JUNGLE_BUTTON:
        case OAK_BUTTON:
        case SPRUCE_BUTTON:
        case STONE_BUTTON:

        case ROSE_BUSH:
        case ATTACHED_MELON_STEM:
        case ATTACHED_PUMPKIN_STEM:
        case WHITE_TULIP:
        case DANDELION:
        case LILY_PAD:
        case SUGAR_CANE:
        case PUMPKIN_STEM:
        case NETHER_WART:
        case NETHER_WART_BLOCK:
        case VINE:
        case CHORUS_FLOWER:
        case CHORUS_FRUIT:
        case CHORUS_PLANT:
        case SUNFLOWER:

        case ACACIA_SAPLING:
        case BIRCH_SAPLING:
        case DARK_OAK_SAPLING:
        case JUNGLE_SAPLING:
        case OAK_SAPLING:
        case SPRUCE_SAPLING:
        case SHULKER_BOX:
        case LIGHT_GRAY_SHULKER_BOX:
        case MAGENTA_SHULKER_BOX:
        case ORANGE_SHULKER_BOX:
        case PINK_SHULKER_BOX:

        case BLACK_BED:
        case BLUE_BED:
        case BROWN_BED:
        case CYAN_BED:
        case GRAY_BED:
        case GREEN_BED:
        case LIME_BED:
        case MAGENTA_BED:
        case ORANGE_BED:
        case PINK_BED:
        case RED_BED:
        case WHITE_BED:
        case YELLOW_BED:
        case LIGHT_BLUE_BED:
        case LIGHT_GRAY_BED:

        case BLACK_BANNER:
        case BLACK_WALL_BANNER:
        case BLUE_BANNER:
        case BLUE_WALL_BANNER:
        case BROWN_BANNER:
        case BROWN_WALL_BANNER:
        case CYAN_BANNER:
        case CYAN_WALL_BANNER:
        case GRAY_BANNER:
        case GRAY_WALL_BANNER:
        case GREEN_BANNER:
        case GREEN_WALL_BANNER:
        case LIME_BANNER:
        case LIME_WALL_BANNER:
        case MAGENTA_BANNER:
        case MAGENTA_WALL_BANNER:
        case ORANGE_BANNER:
        case ORANGE_WALL_BANNER:
        case PINK_BANNER:
        case PINK_WALL_BANNER:
        case RED_BANNER:
        case RED_WALL_BANNER:
        case WHITE_BANNER:
        case WHITE_WALL_BANNER:
        case YELLOW_BANNER:
        case YELLOW_WALL_BANNER:
        case LIGHT_BLUE_BANNER:
        case LIGHT_BLUE_WALL_BANNER:
        case LIGHT_GRAY_BANNER:
        case LIGHT_GRAY_WALL_BANNER:
            return false;
        default:
            return true;
        }
    }

    public static boolean between(int value, int start, int end)
    {
        return value <= end && value >= start;
    }

    public static int tickRateFromSpeed(double speed)
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
    public static double[] calculateTimeAndTickRate(int doorSize, double time, double speedMultiplier, double baseSpeed)
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

        double distanceMultiplier = speed > 4     ? 1.01 :
                                    speed > 3.918 ? 1.08 :
                                    speed > 3.916 ? 1.10 :
                                    speed > 2.812 ? 1.12 :
                                    speed > 2.537 ? 1.19 :
                                    speed > 2.2   ? 1.22 :
                                    speed > 2.0   ? 1.23 :
                                    speed > 1.770 ? 1.25 :
                                    speed > 1.570 ? 1.28 : 1.30;
        ret[0] = time;
        ret[1] = tickRateFromSpeed(speed);
        ret[2] = distanceMultiplier;
        return ret;
    }
}

package nl.pim16aap2.bigdoors.util;

import nl.pim16aap2.bigdoors.util.vector.IVector3DiConst;
import nl.pim16aap2.bigdoors.util.vector.Vector2Di;
import nl.pim16aap2.bigdoors.util.vector.Vector3Di;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents various small and platform agnostic utility functions.
 *
 * @author Pim
 */
public final class Util
{
    /**
     * Characters to use in (secure) random strings.
     */
    private static final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Used to generate secure random strings. It's more secure than {@link Util#rnd}, but slower.
     */
    private static SecureRandom srnd = new SecureRandom();

    /**
     * Used to generate simple random strings. It's faster than {@link Util#srnd}, but not secure.
     */
    private static Random rnd = new Random();

    private static final Map<PBlockFace, RotateDirection> toRotateDirection = new EnumMap<>(PBlockFace.class);
    private static final Map<RotateDirection, PBlockFace> toPBlockFace = new EnumMap<>(RotateDirection.class);

    static
    {
        for (PBlockFace pbf : PBlockFace.values())
        {
            RotateDirection mappedRotDir;
            try
            {
                mappedRotDir = RotateDirection.valueOf(pbf.toString());
            }
            catch (IllegalArgumentException e)
            {
                mappedRotDir = RotateDirection.NONE;
            }
            toRotateDirection.put(pbf, mappedRotDir);
            toPBlockFace.put(mappedRotDir, pbf);
        }
    }

    /**
     * Obtains the numbers of question marks in a String.
     *
     * @param statement The String.
     * @return The number of question marks in the String.
     */
    public static int countPatternOccurrences(final @NotNull Pattern pattern, final @NotNull String statement)
    {
        int found = 0;
        final Matcher matcher = pattern.matcher(statement);
        while (matcher.find())
            ++found;
        return found;
    }

    /**
     * Gets the {@link RotateDirection} equivalent of a {@link PBlockFace} if it exists.
     *
     * @param pBlockFace The {@link PBlockFace}.
     * @return The {@link RotateDirection} equivalent of a {@link PBlockFace} if it exists and otherwise {@link
     * RotateDirection#NONE}.
     */
    @NotNull
    public static RotateDirection getRotateDirection(final @NotNull PBlockFace pBlockFace)
    {
        return toRotateDirection.get(pBlockFace);
    }

    /**
     * Gets the {@link PBlockFace} equivalent of a {@link RotateDirection} if it exists.
     *
     * @param rotateDirection The {@link RotateDirection}.
     * @return The {@link PBlockFace} equivalent of a {@link RotateDirection} if it exists and otherwise {@link
     * PBlockFace#NONE}.
     */
    @NotNull
    public static PBlockFace getPBlockFace(final @NotNull RotateDirection rotateDirection)
    {
        return toPBlockFace.get(rotateDirection);
    }

    /**
     * Clamp an angle to [-2PI ; 2PI].
     *
     * @param angle The current angle in radians.
     * @return The angle (in radians) clamped to [-2PI ; 2PI].
     */
    public static double clampAngleRad(double angle)
    {
        return angle % (2 * Math.PI);
//        double twoPi = 2 * Math.PI;
//        return (angle + twoPi) % twoPi;
    }

    /**
     * Clamp an angle to [-360 ; 360].
     *
     * @param angle The current angle in degrees.
     * @return The angle (in degrees) clamped to [-360 ; 360].
     */
    public static double clampAngleDeg(double angle)
    {
        return angle % (2 * Math.PI);
    }

    /**
     * Concatenate two arrays.
     *
     * @param <T>
     * @param first  First array.
     * @param second Second array.
     * @return A single concatenated array.
     */
    public static <T> T[] concatArrays(T[] first, T[] second)
    {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * Double the size of a provided array
     *
     * @param <T>
     * @param arr Array to be doubled in size
     * @return A copy of the array but with doubled size.
     */
    public static <T> T[] doubleArraySize(T[] arr)
    {
        return Arrays.copyOf(arr, arr.length * 2);
    }

    /**
     * Truncate an array after a provided new length.
     *
     * @param <T>
     * @param arr       The array to truncate
     * @param newLength The new length of the array.
     * @return A truncated array
     */
    public static <T> T[] truncateArray(T[] arr, int newLength)
    {
        return Arrays.copyOf(arr, newLength);
    }

    /**
     * Check if a given string is a valid door name. Numerical names aren't allowed, to make sure they don't get
     * confused for doorUIDs.
     *
     * @param name The name to test for validity,
     * @return True if the name is allowed.
     */
    public static boolean isValidDoorName(String name)
    {
        try
        {
            Long.parseLong(name);
            return false;
        }
        catch (NumberFormatException e)
        {
            return true;
        }
    }

    /**
     * Generate an insecure random alphanumeric string of a given length.
     *
     * @param length Length of the resulting string
     * @return An insecure random alphanumeric string.
     */
    public static String randomInsecureString(int length)
    {
        StringBuilder sb = new StringBuilder(length);
        for (int idx = 0; idx != length; ++idx)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    /**
     * Generate a secure random alphanumeric string of a given length.
     *
     * @param length Length of the resulting string
     * @return A secure random alphanumeric string.
     */
    public static String secureRandomString(int length)
    {
        StringBuilder sb = new StringBuilder(length);
        for (int idx = 0; idx != length; ++idx)
            sb.append(chars.charAt(srnd.nextInt(chars.length())));
        return sb.toString();
    }

    /**
     * Obtains a random integer value.
     *
     * @param min The lower bound (inclusive).
     * @param max The lower bound (inclusive).
     * @return A random integer value.
     */
    public static int getRandomNumber(int min, int max)
    {

        if (min >= max)
        {
            throw new IllegalArgumentException("max must be greater than min");
        }

        return rnd.nextInt((max - min) + 1) + min;
    }

    /**
     * Try to convert a string to a double. Use the default value in case of failure.
     *
     * @param input The string to be converted to a double.
     * @return Double converted from the string if possible, and defaultVal otherwise.
     */
    public static Pair<Boolean, Double> doubleFromString(String input)
    {
        boolean isDouble = false;
        double value = 0;

        try
        {
            if (input != null)
            {
                value = Double.parseDouble(input);
                isDouble = true;
            }
        }
        catch (final NumberFormatException unhandled)
        {
            // This exception doesn't need to be handled, as it means the default values will be returned.
        }
        return new Pair<>(isDouble, value);
    }

    /**
     * Try to convert a string to a long. Use the default value in case of failure.
     *
     * @param input The string to be converted to a long.
     * @return Long converted from the string if possible, and defaultVal otherwise.
     */
    public static Pair<Boolean, Long> longFromString(final String input)
    {
        boolean isLong = false;
        long value = 0;

        try
        {
            if (input != null)
            {
                value = Long.parseLong(input);
                isLong = true;
            }
        }
        catch (final NumberFormatException unhandled)
        {
            // This exception doesn't need to be handled, as it means the default values will be returned.
        }
        return new Pair<>(isLong, value);
    }

    /**
     * Gets the chunk coordinates of a position.
     *
     * @param position The position.
     * @return The chunk coordinates.
     */
    @NotNull
    public static Vector2Di getChunkCoords(final @NotNull IVector3DiConst position)
    {
        return new Vector2Di(position.getX() << 4, position.getZ() << 4);
    }

    /**
     * Gets the 'simple' hash of the chunk given its coordinates. 'simple' here refers to the fact that the world of
     * this chunk will not be taken into account.
     *
     * @param chunkX The x-coordinate of the chunk.
     * @param chunkZ The z-coordinate of the chunk.
     * @return The simple hash of the chunk.
     */
    public static long simpleChunkHashFromChunkCoordinates(int chunkX, int chunkZ)
    {
        long hash = 3;
        hash = 19 * hash + (int) (Double.doubleToLongBits(chunkX) ^ (Double.doubleToLongBits(chunkX) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(chunkZ) ^ (Double.doubleToLongBits(chunkZ) >>> 32));
        return hash;
    }

    /**
     * Gets the 'simple' hash of the chunk that encompasses the given coordinates. 'simple' here refers to the fact that
     * the world of this chunk will not be taken into account.
     *
     * @param posX The x-coordinate of the location.
     * @param posZ The z-coordinate of the location.
     * @return The simple hash of the chunk.
     */
    public static long simpleChunkHashFromLocation(int posX, int posZ)
    {
        return simpleChunkHashFromChunkCoordinates(posX >> 4, posZ >> 4);
    }

    /**
     * Gets the 'simple' hash of a location. 'simple' here refers to the fact that the world of this location will not
     * be taken into account.
     *
     * @param x The x-coordinate of the location.
     * @param y The z-coordinate of the location.
     * @param z The z-coordinate of the location.
     * @return The simple hash of the location.
     */
    public static long simpleLocationhash(int x, int y, int z)
    {
        int hash = 3;
        hash = 19 * hash + (int) (Double.doubleToLongBits(x) ^ Double.doubleToLongBits(x) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(y) ^ Double.doubleToLongBits(y) >>> 32);
        hash = 19 * hash + (int) (Double.doubleToLongBits(z) ^ Double.doubleToLongBits(z) >>> 32);
        return hash;
    }

    /**
     * Converts worldspace coordinates to chunkspace coordinates.
     *
     * @param position The position in world space coordinates.
     * @return The coordinates in chunkspace coordinates.
     */
    public static Vector3Di getChunkSpacePosition(final @NotNull IVector3DiConst position)
    {
        return getChunkSpacePosition(position.getX(), position.getY(), position.getZ());
    }

    /**
     * Converts world space coordinates to chunk space coordinates.
     *
     * @param x The x coordinate in world space.
     * @param y The y coordinate in world space.
     * @param z The z coordinate in world space.
     * @return The coordinates in chunkspace coordinates.
     */
    public static Vector3Di getChunkSpacePosition(int x, int y, int z)
    {
        return new Vector3Di(x % 16, y, z % 16);
    }

    /**
     * Gets the 'simple' hash of a location in chunk-space. 'simple' here refers to the fact that the world of this
     * location will not be taken into account.
     *
     * @param x The x-coordinate of the location.
     * @param y The z-coordinate of the location.
     * @param z The z-coordinate of the location.
     * @return The simple hash of the location in chunk-space.
     */
    public static int simpleChunkSpaceLocationhash(final int x, final int y, final int z)
    {
        int chunkSpaceX = x % 16;
        int chunkSpaceZ = z % 16;
        return (y << 8) + (chunkSpaceX << 4) + chunkSpaceZ;
    }

    /**
     * Convert an array of strings to a single string.
     *
     * @param strings Input array of string
     * @return Resulting concatenated string.
     */
    public static String stringFromArray(String[] strings)
    {
        StringBuilder builder = new StringBuilder();
        for (String str : strings)
            builder.append(str);
        return builder.toString();
    }

    /**
     * Check if a given value is between two other values. Matches inclusively.
     *
     * @param test Value to be compared.
     * @param low  Minimum value.
     * @param high Maximum value.
     * @return True if the value is in the provided range or if it equals the low and/or the high value.
     */
    public static boolean between(int test, int low, int high)
    {
        return test <= high && test >= low;
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
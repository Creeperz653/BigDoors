package nl.pim16aap2.bigdoors.util.vector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPLocationConst;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.util.PLocation;

@AllArgsConstructor
public class Vector3DdConst
{
    @Getter
    protected double x, y, z;

    public Vector3DdConst(final @NonNull Vector3DdConst other)
    {
        this(other.getX(), other.getY(), other.getZ());
    }

    public Vector3DdConst(final @NonNull Vector3DiConst other)
    {
        this(other.getX(), other.getY(), other.getZ());
    }

    public double getDistance(final double x, final double y, final double z)
    {
        return Math.sqrt(Math.pow(getX() - x, 2) + Math.pow(getY() - y, 2) + Math.pow(getZ() - z, 2));
    }

    public double getDistance(final @NonNull Vector3DiConst point)
    {
        return getDistance(point.getX(), point.getY(), point.getZ());
    }

    public double getDistance(final @NonNull Vector3DdConst point)
    {
        return getDistance(point.getX(), point.getY(), point.getZ());
    }

    public double getDistance(final @NonNull IPLocationConst loc)
    {
        return getDistance(loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Converts this object to a String to a certain number of decimal places.
     *
     * @param decimals The number of digits after the dot to display.
     * @return The String representing this object.
     */
    public @NonNull String toString(final int decimals)
    {
        final @NonNull String placeholder = "%." + decimals + "f";
        return String.format(placeholder + ", " + placeholder + ", " + placeholder, x, y, z);
    }

    @Override
    public @NonNull String toString()
    {
        return "(" + x + ":" + y + ":" + z + ")";
    }

    public @NonNull IPLocation toLocation(final @NonNull IPWorld world)
    {
        return new PLocation(world, this);
    }

    @Override
    public @NonNull Vector3Dd clone()
    {
        return new Vector3Dd(this);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 19 * hash + Double.valueOf(x).hashCode();
        hash = 19 * hash + Double.valueOf(y).hashCode();
        hash = 19 * hash + Double.valueOf(z).hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Vector3DdConst))
            return false;

        Vector3DdConst other = (Vector3DdConst) o;
        return x == other.getX() && y == other.getY() && z == other.getZ();
    }
}

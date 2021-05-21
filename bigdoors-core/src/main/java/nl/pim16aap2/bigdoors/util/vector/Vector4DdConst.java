package nl.pim16aap2.bigdoors.util.vector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public sealed class Vector4DdConst permits Vector4Dd
{
    @Getter
    protected double x, y, z, w;

    public Vector4DdConst(final @NonNull Vector4DdConst other)
    {
        this(other.getX(), other.getY(), other.getZ(), other.getW());
    }

    public Vector4DdConst(final @NonNull Vector4DiConst other)
    {
        this(other.getX(), other.getY(), other.getZ(), other.getW());
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
        return String.format(placeholder + ", " + placeholder + ", " + placeholder + ", " + placeholder, x, y, z, w);
    }

    @Override
    public @NonNull String toString()
    {
        return "(" + x + ":" + y + ":" + z + ":" + w + ")";
    }

    @Override
    public @NonNull Vector4Dd clone()
    {
        return new Vector4Dd(this);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Vector4DdConst))
            return false;

        Vector4DdConst other = (Vector4DdConst) o;
        return x == other.getX() && y == other.getY() && z == other.getZ() && w == other.getW();
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 19 * hash + Double.valueOf(x).hashCode();
        hash = 19 * hash + Double.valueOf(y).hashCode();
        hash = 19 * hash + Double.valueOf(z).hashCode();
        hash = 19 * hash + Double.valueOf(w).hashCode();
        return hash;
    }
}

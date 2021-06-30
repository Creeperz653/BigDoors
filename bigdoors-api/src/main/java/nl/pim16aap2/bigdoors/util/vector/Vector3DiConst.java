package nl.pim16aap2.bigdoors.util.vector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.util.PLocation;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class Vector3DiConst
{
    @Getter
    protected int x, y, z;

    public Vector3DiConst(final @NotNull Vector3DiConst other)
    {
        this(other.getX(), other.getY(), other.getZ());
    }

    public @NotNull IPLocation toLocation(final @NotNull IPWorld world)
    {
        return new PLocation(world, this);
    }

    public double getDistance(final @NotNull Vector3DdConst point)
    {
        return Math.sqrt(Math.pow(getX() - point.getX(), 2) + Math.pow(getY() - point.getY(), 2) +
                             Math.pow(getZ() - point.getZ(), 2));
    }

    public double getDistance(final @NotNull Vector3DiConst point)
    {
        return Math.sqrt(Math.pow(getX() - point.getX(), 2) + Math.pow(getY() - point.getY(), 2) +
                             Math.pow(getZ() - point.getZ(), 2));
    }

    @Override
    public @NotNull Vector3Di clone()
    {
        return new Vector3Di(this);
    }

    @Override
    public @NotNull String toString()
    {
        return "(" + x + ":" + y + ":" + z + ")";
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 19 * hash + x;
        hash = 19 * hash + y;
        hash = 19 * hash + z;
        return hash;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Vector3DiConst))
            return false;

        Vector3DiConst other = (Vector3DiConst) o;
        return x == other.getX() && y == other.getY() && z == other.getZ();
    }
}

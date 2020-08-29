package nl.pim16aap2.bigdoors.testimplementations;

import lombok.Getter;
import lombok.Setter;
import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.vector.Vector2Di;
import nl.pim16aap2.bigdoors.util.vector.Vector3DdConst;
import nl.pim16aap2.bigdoors.util.vector.Vector3DiConst;
import org.jetbrains.annotations.NotNull;

public final class TestPLocation implements IPLocation
{
    @Getter(onMethod = @__({@Override}))
    @Setter(onMethod = @__({@Override}))
    @NotNull
    private IPWorld world;

    @Getter(onMethod = @__({@Override}))
    @Setter(onMethod = @__({@Override}))
    private double x;

    @Getter(onMethod = @__({@Override}))
    @Setter(onMethod = @__({@Override}))
    private double y;

    @Getter(onMethod = @__({@Override}))
    @Setter(onMethod = @__({@Override}))
    private double z;


    public TestPLocation(final @NotNull IPWorld world, final double x, final double y, final double z)
    {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public @NotNull Vector2Di getChunk()
    {
        return new Vector2Di(getBlockX() << 4, getBlockZ() << 4);
    }

    @Override
    public int getBlockX()
    {
        return (int) x;
    }

    @Override
    public int getBlockY()
    {
        return (int) y;
    }

    @Override
    public int getBlockZ()
    {
        return (int) z;
    }

    @Override
    public @NotNull IPLocation add(final double x, final double y, final double z)
    {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    @Override
    public @NotNull IPLocation add(final @NotNull Vector3DiConst vector)
    {
        return add(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public @NotNull IPLocation add(final @NotNull Vector3DdConst vector)
    {
        return add(vector.getX(), vector.getY(), vector.getZ());
    }

    @Override
    public @NotNull IPLocation clone()
    {
        try
        {
            TestPLocation cloned = (TestPLocation) super.clone();
            cloned.world = world.clone();
            return cloned;
        }
        catch (CloneNotSupportedException e)
        {
            // TODO: Only log to file! It's already dumped in the console because it's thrown.
            Error er = new Error(e);
            PLogger.get().logThrowable(er);
            throw er;
        }
    }

    @Override
    public String toString()
    {
        return toIntPositionString();
    }
}

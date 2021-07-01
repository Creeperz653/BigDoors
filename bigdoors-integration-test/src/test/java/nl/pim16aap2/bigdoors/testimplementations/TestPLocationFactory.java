package nl.pim16aap2.bigdoors.testimplementations;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.api.factories.IPLocationFactory;
import nl.pim16aap2.bigdoors.api.util.vector.Vector3DdConst;
import nl.pim16aap2.bigdoors.api.util.vector.Vector3DiConst;
import org.jetbrains.annotations.NotNull;

public class TestPLocationFactory implements IPLocationFactory
{
    @Override
    public @NotNull IPLocation create(final @NotNull IPWorld world, final double x, final double y, final double z)
    {
        return new TestPLocation(world, x, y, z);
    }

    @Override
    public @NotNull IPLocation create(final @NotNull IPWorld world, final @NotNull Vector3DiConst position)
    {
        return create(world, position.getX(), position.getY(), position.getZ());
    }

    @Override
    public @NotNull IPLocation create(final @NotNull IPWorld world, final @NotNull Vector3DdConst position)
    {
        return create(world, position.getX(), position.getY(), position.getZ());
    }

    @Override
    public @NotNull IPLocation create(final @NotNull String worldName, final double x, final double y, final double z)
    {
        return create(BigDoors.get().getPlatform().getPWorldFactory().create(worldName), x, y, z);
    }

    @Override
    public @NotNull IPLocation create(final @NotNull String worldName, final @NotNull Vector3DiConst position)
    {
        return create(BigDoors.get().getPlatform().getPWorldFactory().create(worldName),
                      position.getX(), position.getY(), position.getZ());
    }

    @Override
    public @NotNull IPLocation create(final @NotNull String worldName, final @NotNull Vector3DdConst position)
    {
        return create(BigDoors.get().getPlatform().getPWorldFactory().create(worldName),
                      position.getX(), position.getY(), position.getZ());
    }
}

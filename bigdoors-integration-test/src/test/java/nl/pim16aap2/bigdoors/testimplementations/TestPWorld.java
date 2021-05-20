package nl.pim16aap2.bigdoors.testimplementations;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.util.WorldTime;

public final class TestPWorld implements IPWorld
{
    @Getter
    private final String worldName;
    private final boolean exists;
    private final WorldTime time;

    public TestPWorld(final @NonNull String name)
    {
        worldName = name;
        exists = true;
        time = new WorldTime(0);
    }

    @Override
    public boolean exists()
    {
        return exists;
    }

    @Override
    public @NonNull WorldTime getTime()
    {
        return time;
    }

    @Override
    public @NonNull IPWorld clone()
    {
        return new TestPWorld(worldName);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        return worldName.equals(((IPWorld) o).getWorldName());
    }

    @Override
    public int hashCode()
    {
        return worldName.hashCode();
    }
}

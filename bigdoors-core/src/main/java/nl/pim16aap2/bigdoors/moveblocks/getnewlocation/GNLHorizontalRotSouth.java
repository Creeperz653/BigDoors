package nl.pim16aap2.bigdoors.moveblocks.getnewlocation;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.api.factories.IPLocationFactory;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import org.jetbrains.annotations.NotNull;

public class GNLHorizontalRotSouth implements IGetNewLocation
{
    private final int zMin;
    private final IPWorld world;
    private final RotateDirection rotDir;
    private final IPLocationFactory locationFactory = BigDoors.get().getPlatform().getPLocationFactory();

    public GNLHorizontalRotSouth(final @NotNull IPWorld world, final int xMin, final int xMax, final int zMin,
                                 final int zMax, final @NotNull RotateDirection rotDir)
    {
        this.rotDir = rotDir;
        this.world = world;
        this.zMin = zMin;
    }

    @NotNull
    @Override
    public IPLocation getNewLocation(final double radius, final double xPos, final double yPos, final double zPos)
    {
        IPLocation oldPos = locationFactory.create(world, xPos, yPos, zPos);
        IPLocation newPos = oldPos;

        newPos.setX(oldPos.getX() + (rotDir == RotateDirection.CLOCKWISE ? -radius : radius));
        newPos.setY(oldPos.getY());
        newPos.setZ(zMin);
        return newPos;
    }
}
package nl.pim16aap2.bigdoors.moveblocks;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPExecutor;
import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.PBlockData;
import nl.pim16aap2.bigdoors.api.PSound;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doors.RevolvingDoor;
import nl.pim16aap2.bigdoors.util.PBlockFace;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.vector.Vector3Dd;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.TimerTask;
import java.util.function.BiFunction;

/**
 * Represents a {@link BlockMover} for {@link RevolvingDoor}s.
 *
 * @author Pim
 */
public class RevolvingDoorMover extends BlockMover
{
    private static final double maxSpeed = 3;
    private static final double minSpeed = 0.1;
    private final BiFunction<PBlockData, Double, Vector3Dd> getGoalPos;
    private final double time;
    private int tickRate;
    private final double endStepSum;
    private final RotateDirection rotateDirection;

    /**
     * The number of quarter circles to turn.
     */
    private static final int quarterCircles = 2;

    public RevolvingDoorMover(final @NotNull AbstractDoorBase door, final double time, final double multiplier,
                              final @NotNull RotateDirection rotateDirection, final @Nullable IPPlayer player)
    {
        super(door, 30, false, PBlockFace.UP, RotateDirection.NONE, -1, player, door.getMinimum(),
              door.getMaximum());

        this.time = time;
        this.rotateDirection = rotateDirection;

        double speed = 1 * multiplier;
        speed = speed > maxSpeed ? 3 : Math.max(speed, minSpeed);
        tickRate = Util.tickRateFromSpeed(speed);
        tickRate = 3;

        int endCount = (int) (20.0 / ((double) tickRate) * time * ((double) quarterCircles));
        double step = (Math.PI / 2.0 * ((double) quarterCircles)) / ((double) endCount) * -1.0;
        endStepSum = endCount * step;

        switch (rotateDirection)
        {
            case CLOCKWISE:
                getGoalPos = this::getGoalPosClockwise;
                break;
            case COUNTERCLOCKWISE:
                getGoalPos = this::getGoalPosCounterClockwise;
                break;
            default:
                getGoalPos = null;
                PLogger.get().dumpStackTrace("Failed to open door \"" + getDoorUID()
                                                 + "\". Reason: Invalid rotateDirection \"" +
                                                 rotateDirection.toString() + "\"");
                return;
        }
        super.constructFBlocks();
    }

    private Vector3Dd getGoalPosClockwise(final double radius, final double startAngle, final double startY,
                                          final double stepSum)
    {
        final double posX = 0.5 + door.getEngine().getX() - radius * Math.sin(startAngle + stepSum);
        final double posZ = 0.5 + door.getEngine().getZ() - radius * Math.cos(startAngle + stepSum);
        return new Vector3Dd(posX, startY, posZ);
    }

    private Vector3Dd getGoalPosClockwise(final @NotNull PBlockData block, final double stepSum)
    {
        return getGoalPosClockwise(block.getRadius(), block.getStartAngle(), block.getStartY(), stepSum);
    }

    private Vector3Dd getGoalPosCounterClockwise(final double radius, final double startAngle, final double startY,
                                                 final double stepSum)
    {
        final double posX = 0.5 + door.getEngine().getX() - radius * Math.sin(startAngle - stepSum);
        final double posZ = 0.5 + door.getEngine().getZ() - radius * Math.cos(startAngle - stepSum);
        return new Vector3Dd(posX, startY, posZ);
    }

    private Vector3Dd getGoalPosCounterClockwise(final @NotNull PBlockData block, final double stepSum)
    {
        return getGoalPosCounterClockwise(block.getRadius(), block.getStartAngle(), block.getStartY(), stepSum);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IPLocation getNewLocation(double radius, double xAxis, double yAxis, double zAxis)
    {
        // TODO: Redo all this, it's too hacky.
        final double startAngle = getStartAngle((int) xAxis, (int) yAxis, (int) zAxis);
        Vector3Dd newPos;
        if (rotateDirection == RotateDirection.CLOCKWISE)
            newPos = getGoalPosClockwise(radius, startAngle, yAxis, endStepSum);
        else
            newPos = getGoalPosCounterClockwise(radius, startAngle, yAxis, endStepSum);
        return locationFactory.create(world, newPos.getX(), newPos.getY(), newPos.getZ());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void animateEntities()
    {
        super.moverTask = new TimerTask()
        {
            double counter = 0;
            int endCount = (int) (20.0 / ((double) tickRate) * time * ((double) quarterCircles));
            int totalTicks = (int) (endCount * 1.1);
            long startTime = System.nanoTime();
            long lastTime;
            long currentTime = System.nanoTime();
            double step = (Math.PI / 2.0 * ((double) quarterCircles)) / ((double) endCount) * -1.0;

            @Override
            public void run()
            {
                ++counter;
                lastTime = currentTime;
                currentTime = System.nanoTime();
                startTime += currentTime - lastTime;

                if (counter > totalTicks)
                {
                    playSound(PSound.THUD, 2f, 0.15f);
                    final @NotNull IPExecutor<Object> executor = BigDoors.get().getPlatform().newPExecutor();
                    executor.runSync(() -> putBlocks(false));
                    executor.cancel(this, moverTaskID);
                }
                else
                {
                    final double stepSum = Math.max(endStepSum, counter * step);
                    for (PBlockData block : savedBlocks)
                    {
                        if (Math.abs(block.getRadius()) > 2 * Double.MIN_VALUE)
                        {
                            Vector3Dd vec = getGoalPos.apply(block, stepSum)
                                                      .subtract(block.getFBlock().getPosition());
                            vec.multiply(0.101);
                            block.getFBlock().setVelocity(vec);
                        }
                    }
                }
            }
        };
        moverTaskID = BigDoors.get().getPlatform().newPExecutor().runAsyncRepeated(moverTask, 14, tickRate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected float getRadius(int xAxis, int yAxis, int zAxis)
    {
        double deltaA = (door.getEngine().getX() - xAxis);
        double deltaB = door.getEngine().getZ() - zAxis;
        return (float) Math.sqrt(Math.pow(deltaA, 2) + Math.pow(deltaB, 2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected float getStartAngle(int xAxis, int yAxis, int zAxis)
    {
        return (float) Math.atan2(door.getEngine().getX() - xAxis, door.getEngine().getZ() - zAxis);
    }
}
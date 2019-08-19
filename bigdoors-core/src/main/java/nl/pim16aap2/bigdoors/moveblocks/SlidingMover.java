package nl.pim16aap2.bigdoors.moveblocks;

import nl.pim16aap2.bigdoors.api.PBlockData;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.spigotutil.SpigotUtil;
import nl.pim16aap2.bigdoors.util.PBlockFace;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SlidingMover extends BlockMover
{
    private boolean NS;
    private int tickRate;
    private int moveX, moveZ;

    public SlidingMover(final double time, final @NotNull DoorBase door, final boolean instantOpen,
                        final int blocksToMove, final @NotNull RotateDirection openDirection, final double multiplier,
                        @Nullable final UUID playerUUID, final @NotNull Location finalMin,
                        final @NotNull Location finalMax)
    {
        super(door, time, instantOpen, PBlockFace.UP, openDirection, blocksToMove, playerUUID, finalMin, finalMax);

        NS = openDirection.equals(RotateDirection.NORTH) || openDirection.equals(RotateDirection.SOUTH);

        moveX = NS ? 0 : blocksToMove;
        moveZ = NS ? blocksToMove : 0;

        double speed = 1;
        double pcMult = multiplier;
        pcMult = pcMult == 0.0 ? 1.0 : pcMult;
        int maxSpeed = 6;

        // If the time isn't default, calculate speed.
        if (time != 0.0)
        {
            speed = Math.abs(blocksToMove) / time;
            this.time = time;
        }

        // If the non-default exceeds the max-speed or isn't set, calculate default speed.
        if (time == 0.0 || speed > maxSpeed)
        {
            speed = 1.4 * pcMult;
            speed = speed > maxSpeed ? maxSpeed : speed;
            this.time = Math.abs(blocksToMove) / speed;
        }

        tickRate = SpigotUtil.tickRateFromSpeed(speed);

        super.constructFBlocks();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Location getNewLocation(double radius, double xAxis, double yAxis, double zAxis)
    {
        return new Location(world, xAxis + moveX, yAxis, zAxis + moveZ);
    }

    private int getBlocksMoved()
    {
        return super.blocksMoved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void animateEntities()
    {
        new BukkitRunnable()
        {
            double counter = 0;
            int endCount = (int) (20 / tickRate * time);
            double step = ((double) getBlocksMoved()) / ((double) endCount);
            double stepSum = 0;
            int totalTicks = (int) (endCount * 1.1);
            long startTime = System.nanoTime();
            long lastTime;
            long currentTime = System.nanoTime();
            PBlockData firstBlockData = savedBlocks.stream().filter(block -> block.getFBlock() != null).findFirst()
                                                   .orElse(null);

            @Override
            public void run()
            {
                ++counter;
                if (counter == 0 || (counter < endCount - 27 / tickRate && counter % (5 * tickRate / 4) == 0))
                    SpigotUtil.playSound(door.getEngine(), "bd.dragging2", 0.5f, 0.6f);

                lastTime = currentTime;
                currentTime = System.nanoTime();
                startTime += currentTime - lastTime;

                if (counter < endCount - 1)
                    stepSum = step * counter;
                else
                    stepSum = getBlocksMoved();

                if (isAborted.get() || counter > totalTicks ||
                    firstBlockData == null)
                {
                    SpigotUtil.playSound(door.getEngine(), "bd.thud", 2f, 0.15f);
                    for (PBlockData savedBlock : savedBlocks)
                        savedBlock.getFBlock().setVelocity(new Vector(0D, 0D, 0D));
                    Bukkit.getScheduler().callSyncMethod(plugin, () ->
                    {
                        putBlocks(false);
                        return null;
                    });
                    cancel();
                }
                else
                {
                    Location loc = firstBlockData.getStartLocation();

                    if (NS)
                        loc.setZ(loc.getZ() + stepSum);
                    else
                        loc.setX(loc.getX() + stepSum);

                    if (firstBlockData.getStartLocation().getBlockY() != yMin)
                        loc.setY(loc.getY() - .010001);
                    Vector vec = loc.toVector().subtract(firstBlockData.getFBlock().getLocation().toVector());
                    vec.multiply(0.101);

                    for (PBlockData block : savedBlocks)
                        block.getFBlock().setVelocity(vec);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 14, tickRate);
    }
}
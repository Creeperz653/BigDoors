package nl.pim16aap2.bigdoors.moveblocks;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.PBlockData;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.spigotutil.SpigotUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

class VerticalMover extends BlockMover
{
    private int tickRate;

    public VerticalMover(final BigDoors plugin, final World world, final double time, final DoorBase door,
                         final boolean instantOpen, final int blocksToMove, final double multiplier)
    {
        super(plugin, world, door, time, instantOpen, null, null, blocksToMove);

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

        // If the non-default exceeds the max-speed or isn't set, calculate default
        // speed.
        if (time == 0.0 || speed > maxSpeed)
        {
            speed = blocksToMove < 0 ? 1.7 : 0.8 * pcMult;
            speed = speed > maxSpeed ? maxSpeed : speed;
            this.time = Math.abs(blocksToMove) / speed;
        }

        tickRate = SpigotUtil.tickRateFromSpeed(speed);

        super.constructFBlocks();
    }

    private int getBlocksMoved()
    {
        return super.blocksMoved;
    }

    // Method that takes care of the rotation aspect.
    @Override
    protected void animateEntities()
    {
        new BukkitRunnable()
        {
            double counter = 0;
            int endCount = (int) (20 / tickRate * time);
            double step = ((double) getBlocksMoved()) / (endCount);
            double stepSum = 0;
            int totalTicks = (int) (endCount * 1.1);
            long startTime = System.nanoTime();
            long lastTime;
            long currentTime = System.nanoTime();
            PBlockData firstBlockData = savedBlocks.stream().filter(block -> block.getFBlock() != null).findFirst()
                                                   .orElse(null);
            boolean hasFinished = false;

            @Override
            public void run()
            {
                if (counter == 0 || (counter < endCount - 27 / tickRate && counter % (5 * tickRate / 4) == 0))
                    SpigotUtil.playSound(door.getEngine(), "bd.dragging2", 0.5f, 0.6f);

                lastTime = currentTime;
                currentTime = System.nanoTime();
                long msSinceStart = (currentTime - startTime) / 1000000;
                if (!plugin.getDatabaseManager().isPaused())
                    counter = msSinceStart / (50 * tickRate);
                else
                    startTime += currentTime - lastTime;

                if (counter < endCount - 1)
                    stepSum = step * counter;
                else
                    stepSum = getBlocksMoved();

                if (!plugin.getDatabaseManager().canGo() || counter > totalTicks || firstBlockData == null ||
                        isAborted.get())
                {
                    SpigotUtil.playSound(door.getEngine(), "bd.thud", 2f, 0.15f);
                    for (PBlockData block : savedBlocks)
                        block.getFBlock().setVelocity(new Vector(0D, 0D, 0D));
                    Bukkit.getScheduler().callSyncMethod(plugin, () ->
                    {
                        if (!hasFinished)
                        {
                            putBlocks(false);
                            hasFinished = true;
                        }
                        return null;
                    });
                    cancel();
                }
                else
                {
                    Location loc = firstBlockData.getStartLocation();
                    loc.add(0, stepSum, 0);
                    Vector vec = loc.toVector().subtract(firstBlockData.getFBlock().getLocation().toVector());
                    vec.multiply(0.101);

                    for (PBlockData mbd : savedBlocks)
                        mbd.getFBlock().setVelocity(vec);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 14, tickRate);
    }

    @Override
    protected Location getNewLocation(double radius, double xAxis, double yAxis, double zAxis)
    {
        return new Location(world, xAxis, yAxis + blocksMoved, zAxis);
    }
}
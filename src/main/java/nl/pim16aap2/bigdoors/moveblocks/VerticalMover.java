package nl.pim16aap2.bigdoors.moveblocks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.Door;
import nl.pim16aap2.bigdoors.nms.CustomCraftFallingBlock_Vall;
import nl.pim16aap2.bigdoors.nms.FallingBlockFactory_Vall;
import nl.pim16aap2.bigdoors.nms.NMSBlock_Vall;
import nl.pim16aap2.bigdoors.util.MyBlockData;
import nl.pim16aap2.bigdoors.util.MyBlockFace;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Util;

public class VerticalMover implements BlockMover
{
    private FallingBlockFactory_Vall  fabf;
    private Door                      door;
    private double                    time;
    private World                    world;
    private BigDoors                plugin;
    private int                   tickRate;
    private boolean            instantOpen;
    private int               blocksToMove;
    private int           xMin, xMax, yMin;
    private int           yMax, zMin, zMax;
    private List<MyBlockData> savedBlocks = new ArrayList<>();

    public VerticalMover(BigDoors plugin, World world, double time, Door door, boolean instantOpen, int blocksToMove, double multiplier)
    {
        plugin.getAutoCloseScheduler().cancelTimer(door.getDoorUID());
        this.plugin = plugin;
        this.world  = world;
        this.door   = door;
        fabf        = plugin.getFABF();
        this.instantOpen  = instantOpen;
        this.blocksToMove = blocksToMove;

        xMin = door.getMinimum().getBlockX();
        yMin = door.getMinimum().getBlockY();
        zMin = door.getMinimum().getBlockZ();
        xMax = door.getMaximum().getBlockX();
        yMax = door.getMaximum().getBlockY();
        zMax = door.getMaximum().getBlockZ();

        double speed  = 1;
        double pcMult = multiplier;
        pcMult = pcMult == 0.0 ? 1.0 : pcMult;
        int maxSpeed  = 6;

        // If the time isn't default, calculate speed.
        if (time != 0.0)
        {
            speed     = Math.abs(blocksToMove) / time;
            this.time = time;
        }

        // If the non-default exceeds the max-speed or isn't set, calculate default speed.
        if (time == 0.0 || speed > maxSpeed)
        {
            speed     = blocksToMove < 0 ? 1.7 : 0.8 * pcMult;
            speed     = speed > maxSpeed ? maxSpeed : speed;
            this.time = Math.abs(blocksToMove) / speed;
        }

        tickRate = Util.tickRateFromSpeed(speed);

        int index = 0;
        int yAxis = yMin;
        do
        {
            int zAxis = zMin;
            do
            {
                for (int xAxis = xMin; xAxis <= xMax; xAxis++)
                {
                    Location startLocation = new Location(world, xAxis + 0.5, yAxis, zAxis + 0.5);
                    Location newFBlockLocation = new Location(world, xAxis + 0.5, yAxis - 0.020, zAxis + 0.5);
                    // Move the lowest blocks up a little, so the client won't predict they're touching through the ground, which would make them slower than the rest.
                    if (yAxis == yMin)
                        newFBlockLocation.setY(newFBlockLocation.getY() + .010001);
                    Block vBlock  = world.getBlockAt(startLocation);
                    Material mat  = vBlock.getType();

                    if (Util.isAllowedBlock(vBlock))
                    {
                        NMSBlock_Vall block  = fabf.nmsBlockFactory(world, xAxis, yAxis, zAxis);

                        CustomCraftFallingBlock_Vall fBlock = null;
                        if (!instantOpen)
                             fBlock = fallingBlockFactory(newFBlockLocation, mat, block);
                        savedBlocks.add(index, new MyBlockData(mat, fBlock, 0, block, 0, startLocation));
                    }
                    else
                        savedBlocks.add(index, null);
                    ++index;
                }
                ++zAxis;
            }
            while (zAxis <= zMax);
            ++yAxis;
        }
        while (yAxis <= yMax);

        for (MyBlockData mbd : savedBlocks)
            if (mbd.getBlock() != null)
                mbd.getBlock().deleteOriginalBlock();

        if (!instantOpen)
            rotateEntities();
        else
            putBlocks(false);
    }

    // Put the door blocks back, but change their state now.
    @Override
    public void putBlocks(boolean onDisable)
    {
        int index = 0;
        double yAxis = yMin;
        do
        {
            double zAxis = zMin;
            do
            {
                for (int xAxis = xMin; xAxis <= xMax; ++xAxis)
                {
                    Material mat = savedBlocks.get(index).getMat();
                    if (!mat.equals(Material.AIR))
                    {
                        Location newPos = getNewLocation(xAxis, yAxis, zAxis);

                        if (!instantOpen)
                            savedBlocks.get(index).getFBlock().remove();

                        if (!savedBlocks.get(index).getMat().equals(Material.AIR))
                        {
                            savedBlocks.get(index).getBlock().putBlock(newPos);

                            Block b = world.getBlockAt(newPos);
                            BlockState bs = b.getState();
                            bs.update();
                        }
                    }
                    ++index;
                }
                ++zAxis;
            }
            while (zAxis <= zMax);
            ++yAxis;
        }
        while (yAxis <= yMax);
        savedBlocks.clear();

        // Tell the door object it has been opened and what its new coordinates are.
        updateCoords(door, null, blocksToMove > 0 ? RotateDirection.UP : RotateDirection.DOWN, blocksToMove);
        toggleOpen  (door);
        if (!onDisable)
            plugin.getAutoCloseScheduler().scheduleAutoClose(door, time, onDisable);

        // Change door availability to true, so it can be opened again.
        // Wait for a bit if instantOpen is enabled.
        int timer = onDisable   ?  0 :
                    instantOpen ? 40 : plugin.getConfigLoader().coolDown() * 20;

        if (timer > 0)
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    plugin.getDatabaseManager().setDoorAvailable(door.getDoorUID());
                }
            }.runTaskLater(plugin, timer);
        else
            plugin.getDatabaseManager().setDoorAvailable(door.getDoorUID());

        if (!onDisable)
            goAgain();
    }

    private void goAgain()
    {
        int autoCloseTimer = door.getAutoClose();
        if (autoCloseTimer < 0 || !door.isOpen())
            return;

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                plugin.getDatabaseManager().setDoorAvailable(door.getDoorUID());
                plugin.getDoorOpener(door.getType()).openDoor(plugin.getDatabaseManager().getDoor(null, door.getDoorUID()), time, instantOpen, false);
            }
        }.runTaskLater(plugin, autoCloseTimer * 20);
    }

    private Location getNewLocation(double xAxis, double yAxis, double zAxis)
    {
        return new Location(world, xAxis, yAxis + blocksToMove, zAxis);
    }

    // Method that takes care of the rotation aspect.
    private void rotateEntities()
    {
        new BukkitRunnable()
        {
            double counter   = 0;
            int endCount     = (int) (20 / tickRate * time);
            double step      = ((double) blocksToMove) / ((double) endCount);
            double stepSum   = 0;
            int totalTicks   = (int) (endCount * 1.1);
            long startTime   = System.nanoTime();
            long lastTime;
            long currentTime = System.nanoTime();
            MyBlockData firstBlockData = savedBlocks.stream().filter(block -> !block.getMat().equals(Material.AIR)).findFirst().orElse(null);

            @Override
            public void run()
            {
                if (counter == 0 || (counter < endCount - 27 / tickRate && counter % (5 * tickRate / 4) == 0))
                    Util.playSound(door.getEngine(), "bd.dragging2", 0.5f, 0.6f);

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
                    stepSum = blocksToMove;

                if (!plugin.getDatabaseManager().canGo() || !door.canGo() || counter > totalTicks || firstBlockData == null)
                {
                    Util.playSound(door.getEngine(), "bd.thud", 2f, 0.15f);
                    for (int idx = 0; idx < savedBlocks.size(); ++idx)
                        if (!savedBlocks.get(idx).getMat().equals(Material.AIR))
                            savedBlocks.get(idx).getFBlock().setVelocity(new Vector(0D, 0D, 0D));
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
                    loc.add(0, stepSum, 0);
                    Vector vec = loc.toVector().subtract(firstBlockData.getFBlock().getLocation().toVector());
                    vec.multiply(0.101);

                    for (MyBlockData block : savedBlocks)
                        if (!block.getMat().equals(Material.AIR))
                            block.getFBlock().setVelocity(vec);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 14, tickRate);
    }

    // Toggle the open status of a drawbridge.
    private void toggleOpen(Door door)
    {
        door.setOpenStatus(!door.isOpen());
    }

    // Update the coordinates of a door based on its location, direction it's pointing in and rotation direction.
    private void updateCoords(Door door, MyBlockFace currentDirection, RotateDirection rotDirection, int moved)
    {
        int xMin = door.getMinimum().getBlockX();
        int yMin = door.getMinimum().getBlockY();
        int zMin = door.getMinimum().getBlockZ();
        int xMax = door.getMaximum().getBlockX();
        int yMax = door.getMaximum().getBlockY();
        int zMax = door.getMaximum().getBlockZ();

        Location newMax = new Location(door.getWorld(), xMax, yMax + moved, zMax);
        Location newMin = new Location(door.getWorld(), xMin, yMin + moved, zMin);

        door.setMaximum(newMax);
        door.setMinimum(newMin);

        plugin.getDatabaseManager().updateDoorCoords(door.getDoorUID(), !door.isOpen(), newMin.getBlockX(), newMin.getBlockY(), newMin.getBlockZ(), newMax.getBlockX(), newMax.getBlockY(), newMax.getBlockZ());
    }

    private CustomCraftFallingBlock_Vall fallingBlockFactory(Location loc, Material mat, NMSBlock_Vall block)
    {
        CustomCraftFallingBlock_Vall entity = fabf.fallingBlockFactory(plugin, loc, block, mat);
        Entity bukkitEntity = (Entity) entity;
        bukkitEntity.setCustomName("BigDoorsEntity");
        bukkitEntity.setCustomNameVisible(false);
        return entity;
    }

    @Override
    public long getDoorUID()
    {
        return door.getDoorUID();
    }

    @Override
    public Door getDoor()
    {
        return door;
    }
}

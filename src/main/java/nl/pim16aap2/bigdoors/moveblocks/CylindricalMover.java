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
import nl.pim16aap2.bigdoors.moveblocks.cylindrical.getnewlocation.GetNewLocation;
import nl.pim16aap2.bigdoors.moveblocks.cylindrical.getnewlocation.GetNewLocationEast;
import nl.pim16aap2.bigdoors.moveblocks.cylindrical.getnewlocation.GetNewLocationNorth;
import nl.pim16aap2.bigdoors.moveblocks.cylindrical.getnewlocation.GetNewLocationSouth;
import nl.pim16aap2.bigdoors.moveblocks.cylindrical.getnewlocation.GetNewLocationWest;
import nl.pim16aap2.bigdoors.nms.CustomCraftFallingBlock_Vall;
import nl.pim16aap2.bigdoors.nms.FallingBlockFactory_Vall;
import nl.pim16aap2.bigdoors.nms.NMSBlock_Vall;
import nl.pim16aap2.bigdoors.util.MyBlockData;
import nl.pim16aap2.bigdoors.util.MyBlockFace;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.Util;

public class CylindricalMover implements BlockMover
{
    private GetNewLocation gnl;
    private final FallingBlockFactory_Vall fabf;
    private final double time;
    private final Door door;
    private final World world;
    private final int dx, dz;
    private final BigDoors plugin;
    private final int tickRate;
    private double endStepSum;
    private final double multiplier;
    private final boolean instantOpen;
    private double startStepSum;
    private final RotateDirection rotDirection;
    private final int stepMultiplier;
    private final int xMin, xMax, yMin;
    private final int yMax, zMin, zMax;
    private final MyBlockFace currentDirection;
    private final Location turningPoint;
    private final List<MyBlockData> savedBlocks = new ArrayList<>();

    public CylindricalMover(BigDoors plugin, World world, RotateDirection rotDirection, double time,
        Location pointOpposite, MyBlockFace currentDirection, Door door, boolean instantOpen)
    {
        this.rotDirection = rotDirection;
        this.currentDirection = currentDirection;
        this.plugin = plugin;
        this.world = world;
        this.door = door;
        turningPoint = door.getEngine();
        fabf = plugin.getFABF();
        this.instantOpen = instantOpen;
        stepMultiplier = rotDirection == RotateDirection.CLOCKWISE ? -1 : 1;

        xMin = turningPoint.getBlockX() < pointOpposite.getBlockX() ? turningPoint.getBlockX() :
            pointOpposite.getBlockX();
        yMin = turningPoint.getBlockY() < pointOpposite.getBlockY() ? turningPoint.getBlockY() :
            pointOpposite.getBlockY();
        zMin = turningPoint.getBlockZ() < pointOpposite.getBlockZ() ? turningPoint.getBlockZ() :
            pointOpposite.getBlockZ();
        xMax = turningPoint.getBlockX() > pointOpposite.getBlockX() ? turningPoint.getBlockX() :
            pointOpposite.getBlockX();
        yMax = turningPoint.getBlockY() > pointOpposite.getBlockY() ? turningPoint.getBlockY() :
            pointOpposite.getBlockY();
        zMax = turningPoint.getBlockZ() > pointOpposite.getBlockZ() ? turningPoint.getBlockZ() :
            pointOpposite.getBlockZ();
        int xLen = Math.abs(door.getMaximum().getBlockX() - door.getMinimum().getBlockX());
        int zLen = Math.abs(door.getMaximum().getBlockZ() - door.getMinimum().getBlockZ());
        int doorSize = Math.max(xLen, zLen) + 1;
        double vars[] = Util.calculateTimeAndTickRate(doorSize, time, plugin.getConfigLoader().bdMultiplier(), 3.7);
        this.time = vars[0];
        tickRate = (int) vars[1];
        multiplier = vars[2];

        dx = pointOpposite.getBlockX() > turningPoint.getBlockX() ? 1 : -1;
        dz = pointOpposite.getBlockZ() > turningPoint.getBlockZ() ? 1 : -1;

        double xAxis = turningPoint.getX();
        do
        {
            double zAxis = turningPoint.getZ();
            do
            {
                // Get the radius of this pillar.
                double radius = Math.abs(xAxis - turningPoint.getBlockX()) > Math
                    .abs(zAxis - turningPoint.getBlockZ()) ? Math.abs(xAxis - turningPoint.getBlockX()) :
                        Math.abs(zAxis - turningPoint.getBlockZ());

                for (double yAxis = yMin; yAxis <= yMax; yAxis++)
                {
                    Location startLocation = new Location(world, xAxis + 0.5, yAxis, zAxis + 0.5);
                    Location newFBlockLocation = new Location(world, xAxis + 0.5, yAxis - 0.020, zAxis + 0.5);
                    // Move the lowest blocks up a little, so the client won't predict they're
                    // touching through the ground, which would make them slower than the rest.
                    if (yAxis == yMin)
                        newFBlockLocation.setY(newFBlockLocation.getY() + .010001);

                    Block vBlock = world.getBlockAt((int) xAxis, (int) yAxis, (int) zAxis);
                    Material mat = vBlock.getType();

                    if (!mat.equals(Material.AIR))
                    {
                        NMSBlock_Vall block = fabf.nmsBlockFactory(world, (int) xAxis, (int) yAxis, (int) zAxis);
                        NMSBlock_Vall block2 = null;
                        boolean canRotate = false;

                        // Certain blocks cannot be used the way normal blocks can (heads, (ender)
                        // chests etc).
                        if (Util.isAllowedBlock(vBlock))
                        {
                            if (block.canRotate())
                            {
                                block2 = block;
                                block2.rotateBlock(rotDirection);
                            }
                            vBlock.setType(Material.AIR);
                        }
                        else
                            mat = Material.AIR;

                        CustomCraftFallingBlock_Vall fBlock = null;

                        if (!instantOpen)
                            fBlock = fallingBlockFactory(newFBlockLocation, mat, block);

                        if (fBlock != null)
                            savedBlocks.add(new MyBlockData(mat, fBlock, radius,
                                                            block2 == null ? block : block2, canRotate ? 1 : 0, startLocation));
                    }
                }
                zAxis += dz;
            }
            while (zAxis >= pointOpposite.getBlockZ() && dz == -1 || zAxis <= pointOpposite.getBlockZ() && dz == 1);
            xAxis += dx;
        }
        while (xAxis >= pointOpposite.getBlockX() && dx == -1 || xAxis <= pointOpposite.getBlockX() && dx == 1);

        switch (currentDirection)
        {
        case NORTH:
            gnl = new GetNewLocationNorth(world, xMin, xMax, zMin, zMax, rotDirection);
            startStepSum = Math.PI;
            endStepSum = rotDirection == RotateDirection.CLOCKWISE ? Math.PI / 2 : 3 * Math.PI / 2;
            break;
        case EAST:
            gnl = new GetNewLocationEast(world, xMin, xMax, zMin, zMax, rotDirection);
            startStepSum = Math.PI / 2;
            endStepSum = rotDirection == RotateDirection.CLOCKWISE ? 0 : Math.PI;
            break;
        case SOUTH:
            gnl = new GetNewLocationSouth(world, xMin, xMax, zMin, zMax, rotDirection);
            startStepSum = 0;
            endStepSum = rotDirection == RotateDirection.CLOCKWISE ? 3 * Math.PI / 2 : Math.PI / 2;
            break;
        case WEST:
            gnl = new GetNewLocationWest(world, xMin, xMax, zMin, zMax, rotDirection);
            startStepSum = 3 * Math.PI / 2;
            endStepSum = rotDirection == RotateDirection.CLOCKWISE ? Math.PI : 0;
            break;
        default:
            plugin.dumpStackTrace("Invalid currentDirection for cylindrical mover: " + currentDirection.toString());
            break;
        }

        if (!instantOpen)
            rotateEntities();
        else
            putBlocks(false);
    }

    // Put the door blocks back, but change their state now.
    @Override
    public void putBlocks(boolean onDisable)
    {
        for (MyBlockData savedBlock : savedBlocks)
        {
            /*
             * 0-3: Vertical oak, spruce, birch, then jungle 4-7: East/west oak, spruce,
             * birch, jungle 8-11: North/south oak, spruce, birch, jungle 12-15: Uses oak,
             * spruce, birch, jungle bark texture on all six faces
             */

            Material mat = savedBlock.getMat();

            if (!mat.equals(Material.AIR))
            {
                Location newPos = gnl.getNewLocation(savedBlock.getRadius(), savedBlock.getStartX(),
                                                     savedBlock.getStartY(), savedBlock.getStartZ());

                if (!instantOpen)
                    savedBlock.getFBlock().remove();

                if (!savedBlock.getMat().equals(Material.AIR))
                {
                    savedBlock.getBlock().putBlock(newPos);
                    Block b = world.getBlockAt(newPos);
                    BlockState bs = b.getState();
                    bs.update();
                }
            }
        }

        // Tell the door object it has been opened and what its new coordinates are.
        updateCoords(door, currentDirection, rotDirection, -1);
        toggleOpen(door);
        if (!onDisable)
            plugin.removeBlockMover(this);

        // Change door availability to true, so it can be opened again.
        // Wait for a bit if instantOpen is enabled.
        int timer = onDisable ? 0 : instantOpen ? 40 : plugin.getConfigLoader().coolDown() * 20;

        if (timer > 0)
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    plugin.getCommander().setDoorAvailable(door.getDoorUID());
                }
            }.runTaskLater(plugin, timer);
        else
            plugin.getCommander().setDoorAvailable(door.getDoorUID());

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
                plugin.getCommander().setDoorAvailable(door.getDoorUID());
                plugin.getDoorOpener(door.getType()).openDoor(plugin.getCommander().getDoor(null, door.getDoorUID()),
                                                              time, instantOpen, false);
            }
        }.runTaskLater(plugin, autoCloseTimer * 20);
    }

    // Method that takes care of the rotation aspect.
    private void rotateEntities()
    {
        new BukkitRunnable()
        {
            Location center = new Location(world, turningPoint.getBlockX() + 0.5, yMin, turningPoint.getBlockZ() + 0.5);
            boolean replace = false;
            double counter = 0;
            int endCount = (int) (20 / tickRate * time);
            double step = (Math.PI / 2) / endCount * stepMultiplier;
            double stepSum = startStepSum;
            int totalTicks = (int) (endCount * multiplier);
            int replaceCount = endCount / 2;
            long startTime = System.nanoTime();
            long lastTime;
            long currentTime = System.nanoTime();

            @Override
            public void run()
            {
                if (counter == 0 || (counter < endCount - 27 / tickRate && counter % (5 * tickRate / 4) == 0))
                    Util.playSound(door.getEngine(), "bd.dragging2", 0.5f, 0.6f);

                lastTime = currentTime;
                currentTime = System.nanoTime();
                long msSinceStart = (currentTime - startTime) / 1000000;
                if (!plugin.getCommander().isPaused())
                    counter = msSinceStart / (50 * tickRate);
                else
                    startTime += currentTime - lastTime;

                if (counter < endCount - 1)
                    stepSum = startStepSum + step * counter;
                else
                    stepSum = endStepSum;

                replace = false;
                if (counter == replaceCount)
                    replace = true;

                if (!plugin.getCommander().canGo() || !door.canGo() || counter > totalTicks)
                {
                    Util.playSound(door.getEngine(), "bd.closing-vault-door", 0.2f, 1f);
                    for (MyBlockData savedBlock : savedBlocks)
                        if (!savedBlock.getMat().equals(Material.AIR))
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
                    // It is not pssible to edit falling block blockdata (client won't update it),
                    // so delete the current fBlock and replace it by one that's been rotated.
                    // Also, this stuff needs to be done on the main thread.
                    if (replace)
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                        {
                            for (MyBlockData block : savedBlocks)
                                if (block.canRot() != 0 && block.canRot() != 5)
                                {
                                    Material mat = block.getMat();
                                    Location loc = block.getFBlock().getLocation();
                                    Vector veloc = block.getFBlock().getVelocity();
                                    // For some reason respawning fblocks puts them higher than they were, which has
                                    // to be counteracted.
                                    if (block.getStartLocation().getBlockY() != yMin)
                                        loc.setY(loc.getY() - .010001);
                                    CustomCraftFallingBlock_Vall fBlock;
                                    // Because the block in savedBlocks is already rotated where applicable, just
                                    // use that block now.
                                    fBlock = fallingBlockFactory(loc, mat, block.getBlock());

                                    block.getFBlock().remove();
                                    block.setFBlock(fBlock);
                                    block.getFBlock().setVelocity(veloc);
                                }
                        }, 0);

                    double sin = Math.sin(stepSum);
                    double cos = Math.cos(stepSum);

                    for (MyBlockData block : savedBlocks)
                        if (!block.getMat().equals(Material.AIR))
                        {
                            double radius = block.getRadius();
                            int yPos = block.getStartLocation().getBlockY();

                            if (radius != 0)
                            {
                                Location loc;
                                double addX = radius * sin;
                                double addZ = radius * cos;

                                loc = new Location(null, center.getX() + addX, yPos, center.getZ() + addZ);

                                Vector vec = loc.toVector().subtract(block.getFBlock().getLocation().toVector());
                                vec.multiply(0.101);
                                block.getFBlock().setVelocity(vec);
                            }
                        }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 14, tickRate);
    }

    // Toggle the open status of a drawbridge.
    private void toggleOpen(Door door)
    {
        door.setOpenStatus(!door.isOpen());
    }

    // Update the coordinates of a door based on its location, direction it's
    // pointing in and rotation direction.
    @SuppressWarnings("null")
    private void updateCoords(Door door, MyBlockFace currentDirection, RotateDirection rotDirection, int moved)
    {
        int xMin = door.getMinimum().getBlockX();
        int yMin = door.getMinimum().getBlockY();
        int zMin = door.getMinimum().getBlockZ();
        int xMax = door.getMaximum().getBlockX();
        int yMax = door.getMaximum().getBlockY();
        int zMax = door.getMaximum().getBlockZ();
        int xLen = xMax - xMin;
        int zLen = zMax - zMin;
        Location newMax = null;
        Location newMin = null;

        switch (currentDirection)
        {
        case NORTH:
            if (rotDirection == RotateDirection.CLOCKWISE)
            {
                newMin = new Location(door.getWorld(), xMin, yMin, zMax);
                newMax = new Location(door.getWorld(), (xMin + zLen), yMax, zMax);
            }
            else
            {
                newMin = new Location(door.getWorld(), (xMin - zLen), yMin, zMax);
                newMax = new Location(door.getWorld(), xMax, yMax, zMax);
            }
            break;

        case EAST:
            if (rotDirection == RotateDirection.CLOCKWISE)
            {
                newMin = new Location(door.getWorld(), xMin, yMin, zMin);
                newMax = new Location(door.getWorld(), xMin, yMax, (zMax + xLen));
            }
            else
            {
                newMin = new Location(door.getWorld(), xMin, yMin, (zMin - xLen));
                newMax = new Location(door.getWorld(), xMin, yMax, zMin);
            }
            break;

        case SOUTH:
            if (rotDirection == RotateDirection.CLOCKWISE)
            {
                newMin = new Location(door.getWorld(), (xMin - zLen), yMin, zMin);
                newMax = new Location(door.getWorld(), xMax, yMax, zMin);
            }
            else
            {
                newMin = new Location(door.getWorld(), xMin, yMin, zMin);
                newMax = new Location(door.getWorld(), (xMin + zLen), yMax, zMin);
            }
            break;

        case WEST:
            if (rotDirection == RotateDirection.CLOCKWISE)
            {
                newMin = new Location(door.getWorld(), xMax, yMin, (zMin - xLen));
                newMax = new Location(door.getWorld(), xMax, yMax, zMax);
            }
            else
            {
                newMin = new Location(door.getWorld(), xMax, yMin, zMin);
                newMax = new Location(door.getWorld(), xMax, yMax, (zMax + xLen));
            }
            break;
        default:
            plugin.dumpStackTrace("Invalid currentDirection for cylindrical mover: " + currentDirection.toString());
            break;
        }
        door.setMaximum(newMax);
        door.setMinimum(newMin);

        plugin.getCommander().updateDoorCoords(door.getDoorUID(), !door.isOpen(), newMin.getBlockX(),
                                               newMin.getBlockY(), newMin.getBlockZ(), newMax.getBlockX(),
                                               newMax.getBlockY(), newMax.getBlockZ());
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

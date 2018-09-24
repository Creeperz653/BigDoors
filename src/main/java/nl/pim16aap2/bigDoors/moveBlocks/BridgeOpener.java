package nl.pim16aap2.bigDoors.moveBlocks;

import java.util.logging.Level;

import org.bukkit.World;

import net.md_5.bungee.api.ChatColor;
import nl.pim16aap2.bigDoors.BigDoors;
import nl.pim16aap2.bigDoors.Door;
import nl.pim16aap2.bigDoors.util.DoorDirection;
import nl.pim16aap2.bigDoors.util.RotateDirection;
import nl.pim16aap2.bigDoors.util.Util;

public class BridgeOpener implements Opener
{
	private BigDoors        plugin;
	private RotateDirection upDown;

	public BridgeOpener(BigDoors plugin)
	{
		this.plugin = plugin;
	}
	
	// Check if the new position is free.
	private boolean isNewPosFree(Door door, RotateDirection upDown, DoorDirection cardinal)
	{
		int startX  = 0, startY = 0, startZ = 0;
		int endX    = 0, endY   = 0, endZ   = 0;
		World world = door.getWorld();

		if (upDown.equals(RotateDirection.UP))
		{
			switch (cardinal)
			{
			// North West = Min X, Min Z
			// South West = Min X, Max Z
			// North East = Max X, Min Z
			// South East = Max X, Max X
			case NORTH:
				startX = door.getMinimum().getBlockX();
				endX   = door.getMaximum().getBlockX();
				
				startY = door.getMinimum().getBlockY() + 1;
				endY   = door.getMinimum().getBlockY() + door.getMaximum().getBlockZ() - door.getMinimum().getBlockZ();
				
				startZ = door.getMinimum().getBlockZ();
				endZ   = door.getMinimum().getBlockZ();
				break;
				
			case SOUTH:
				startX = door.getMinimum().getBlockX();
				endX   = door.getMaximum().getBlockX();
				
				startY = door.getMinimum().getBlockY() + 1;
				endY   = door.getMinimum().getBlockY() + door.getMaximum().getBlockZ() - door.getMinimum().getBlockZ();
				
				startZ = door.getMaximum().getBlockZ();
				endZ   = door.getMaximum().getBlockZ();
				break;
				
			case EAST:
				startX = door.getMaximum().getBlockX();
				endX   = door.getMaximum().getBlockX();
				
				startY = door.getMinimum().getBlockY() + 1;
				endY   = door.getMinimum().getBlockY() + door.getMaximum().getBlockX() - door.getMinimum().getBlockX();
				
				startZ = door.getMinimum().getBlockZ();
				endZ   = door.getMaximum().getBlockZ();
				break;
				
			case WEST:
				startX = door.getMinimum().getBlockX();
				endX   = door.getMinimum().getBlockX();
				
				startY = door.getMinimum().getBlockY() + 1;
				endY   = door.getMinimum().getBlockY() + door.getMaximum().getBlockX() - door.getMinimum().getBlockX();
				
				startZ = door.getMinimum().getBlockZ();
				endZ   = door.getMaximum().getBlockZ();
				break;
			}
		}
		else
		{
			switch (cardinal)
			{
			// North West = Min X, Min Z
			// South West = Min X, Max Z
			// North East = Max X, Min Z
			// South East = Max X, Max X
			case NORTH:
				startX = door.getMinimum().getBlockX();
				endX   = door.getMaximum().getBlockX();
				
				startY = door.getMinimum().getBlockY();
				endY   = door.getMinimum().getBlockY();
				
				startZ = door.getMinimum().getBlockZ() - door.getMaximum().getBlockY() + door.getMinimum().getBlockY();
				endZ   = door.getMinimum().getBlockZ() - 1;
				break;
				
			case SOUTH:
				startX = door.getMinimum().getBlockX();
				endX   = door.getMaximum().getBlockX();
				
				startY = door.getMinimum().getBlockY();
				endY   = door.getMinimum().getBlockY();
				
				startZ = door.getMinimum().getBlockZ() + 1;
				endZ   = door.getMinimum().getBlockZ() + door.getMaximum().getBlockY() - door.getMinimum().getBlockY();
				break;
				
			case EAST:
				startX = door.getMinimum().getBlockX() + 1;
				endX   = door.getMaximum().getBlockX() + door.getMaximum().getBlockY() - door.getMinimum().getBlockY();
				
				startY = door.getMinimum().getBlockY();
				endY   = door.getMinimum().getBlockY();
				
				startZ = door.getMinimum().getBlockZ();
				endZ   = door.getMaximum().getBlockZ();
				break;
				
			case WEST:
				startX = door.getMinimum().getBlockX() - door.getMaximum().getBlockY() + door.getMinimum().getBlockY();
				endX   = door.getMinimum().getBlockX() - 1;
				
				startY = door.getMinimum().getBlockY();
				endY   = door.getMinimum().getBlockY();
				
				startZ = door.getMinimum().getBlockZ();
				endZ   = door.getMaximum().getBlockZ();
				break;
			}
		}
		
		for (int xAxis = startX; xAxis <= endX; ++xAxis)
			for (int yAxis = startY; yAxis <= endY; ++yAxis)
				for (int zAxis = startZ; zAxis <= endZ; ++zAxis)
					if (!Util.isAir(world.getBlockAt(xAxis, yAxis, zAxis).getType()))
						return false;
		return true;
	}

	// Check if the bridge should go up or down.
	public RotateDirection getUpDown(Door door)
	{
		int height = Math.abs(door.getMinimum().getBlockY() - door.getMaximum().getBlockY());
		if (height > 0)
			return RotateDirection.DOWN;
		return RotateDirection.UP;
	}
	
	// Figure out which way the bridge should go.
	private DoorDirection getOpenDirection(Door door)
	{
		RotateDirection upDown = getUpDown(door);
		DoorDirection cDir     = getCurrentDirection(door);
		boolean NS  = cDir    == DoorDirection.NORTH || cDir == DoorDirection.SOUTH;
		
		if (upDown.equals(RotateDirection.UP))
			return isNewPosFree(door, upDown, door.getEngSide()) ? door.getEngSide() : null;
		
		if (door.getOpenDir().equals(RotateDirection.CLOCKWISE       ) && !door.isOpen() ||
			door.getOpenDir().equals(RotateDirection.COUNTERCLOCKWISE) &&  door.isOpen())
			return  NS && isNewPosFree(door, upDown, DoorDirection.SOUTH) ? DoorDirection.SOUTH :
				   !NS && isNewPosFree(door, upDown, DoorDirection.EAST ) ? DoorDirection.EAST  : null;
		if (door.getOpenDir().equals(RotateDirection.CLOCKWISE       ) &&  door.isOpen() ||
			door.getOpenDir().equals(RotateDirection.COUNTERCLOCKWISE) && !door.isOpen())
			return  NS && isNewPosFree(door, upDown, DoorDirection.NORTH) ? DoorDirection.NORTH :
				   !NS && isNewPosFree(door, upDown, DoorDirection.WEST ) ? DoorDirection.WEST  : null;
		
		return 	 NS && isNewPosFree(door, upDown, DoorDirection.NORTH) ? DoorDirection.NORTH :
				!NS && isNewPosFree(door, upDown, DoorDirection.EAST ) ? DoorDirection.EAST  : 
				 NS && isNewPosFree(door, upDown, DoorDirection.SOUTH) ? DoorDirection.SOUTH : 
				!NS && isNewPosFree(door, upDown, DoorDirection.WEST ) ? DoorDirection.WEST  : null;
	}

	// Get the "current direction". In this context this means on which side of the drawbridge the engine is.
	private DoorDirection getCurrentDirection(Door door)
	{	
		return door.getEngSide();
	}

	private boolean chunksLoaded(Door door)
	{
		// Return true if the chunk at the max and at the min of the chunks were loaded correctly.
		if (door.getWorld() == null)
			plugin.getMyLogger().logMessage("World is null for door \""    + door.getName().toString() + "\"",          true, false);
		if (door.getWorld().getChunkAt(door.getMaximum()) == null)
			plugin.getMyLogger().logMessage("Chunk at maximum for door \"" + door.getName().toString() + "\" is null!", true, false);
		if (door.getWorld().getChunkAt(door.getMinimum()) == null)
			plugin.getMyLogger().logMessage("Chunk at minimum for door \"" + door.getName().toString() + "\" is null!", true, false);
		
		return door.getWorld().getChunkAt(door.getMaximum()).load() && door.getWorld().getChunkAt(door.getMinimum()).isLoaded();
	}

	private int getDoorSize(Door door)
	{
		int xLen = Math.abs(door.getMaximum().getBlockX() - door.getMinimum().getBlockX());
		int yLen = Math.abs(door.getMaximum().getBlockY() - door.getMinimum().getBlockY());
		int zLen = Math.abs(door.getMaximum().getBlockZ() - door.getMinimum().getBlockZ());
		xLen = xLen == 0 ? 1 : xLen;
		yLen = yLen == 0 ? 1 : yLen;
		zLen = zLen == 0 ? 1 : zLen;
		return xLen * yLen * zLen;
	}
	
	@Override
	public boolean openDoor(Door door, double time)
	{
		return openDoor(door, time, false, false);
	}

	@Override
	public boolean openDoor(Door door, double time, boolean instantOpen, boolean silent)
	{
		if (plugin.getCommander().isDoorBusy(door.getDoorUID()))
		{
			if (!silent)
				plugin.getMyLogger().myLogger(Level.INFO, "Bridge " + door.getName() + " is not available right now!");
			return true;
		}

		if (!chunksLoaded(door))
		{
			plugin.getMyLogger().logMessage(ChatColor.RED + "Chunk for bridge " + door.getName() + " is not loaded!", true, false);
			return true;
		}

		DoorDirection currentDirection = getCurrentDirection(door);
		if (currentDirection == null)
		{
			plugin.getMyLogger().logMessage("Current direction is null for bridge " + door.getName() + " (" + door.getDoorUID() + ")!", true, false);
			return false;
		}
		this.upDown = getUpDown(door);

		if (upDown == null)
		{
			plugin.getMyLogger().logMessage("UpDown direction is null for bridge " + door.getName() + " (" + door.getDoorUID() + ")!", true, false);
			return false;
		}
		DoorDirection openDirection = getOpenDirection(door);
		if (openDirection == null)
		{
			plugin.getMyLogger().logMessage("OpenDirection direction is null for bridge " + door.getName() + " (" + door.getDoorUID() + ")!", true, false);
			return false;
		}
		
		// Make sure the doorSize does not exceed the total doorSize.
		// If it does, open the door instantly.
		int maxDoorSize = plugin.getConfigLoader().getInt("maxDoorSize");
		if (maxDoorSize != -1)
			if(getDoorSize(door) > maxDoorSize)
				instantOpen = true;
		
		// Change door availability so it cannot be opened again (just temporarily, don't worry!).
		plugin.getCommander().setDoorBusy(door.getDoorUID());

		plugin.addBlockMover(new BridgeMover(plugin, door.getWorld(), time, door, this.upDown, openDirection, instantOpen));
		
		return true;
	}
}
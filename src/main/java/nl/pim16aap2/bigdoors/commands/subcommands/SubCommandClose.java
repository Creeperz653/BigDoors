package nl.pim16aap2.bigdoors.commands.subcommands;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.Door;
import nl.pim16aap2.bigdoors.commands.CommandManager;
import nl.pim16aap2.bigdoors.commands.CommandPermissionException;
import nl.pim16aap2.bigdoors.commands.CommandSenderNotPlayerException;
import nl.pim16aap2.bigdoors.util.RotateDirection;

public class SubCommandClose extends SubCommandToggle
{
    private static final String name = "close";
    private static final String permission = "bigdoors.user.toggle";
    private static final String help = "Close a door.";

    public SubCommandClose(final BigDoors plugin, final CommandManager commandManager)
    {
        super(plugin, commandManager);
    }

    public boolean readyToClose(Door door)
    {
        return (door.getOpenDir().equals(RotateDirection.NONE) || door.isOpen());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
        throws CommandSenderNotPlayerException, CommandPermissionException
    {
        ArrayList<Door> doors = new ArrayList<>();
        double time = parseDoorsAndTime(sender, args, doors);

        for (Door door : doors)
            if (readyToClose(door))
                toggleDoorCommand(sender, door, time);
            else
                plugin.getMyLogger().returnToSender(sender, Level.INFO, ChatColor.RED,
                                                    plugin.getMessages().getString("GENERAL.Door") + " \"" + door.getName() +
                                                    "\" " + plugin.getMessages().getString("GENERAL.DoorAlreadyClosed"));
        return true;
    }

    @Override
    public String getHelp(CommandSender sender)
    {
        return help;
    }

    @Override
    public String getHelpArguments()
    {
        return argsHelp;
    }

    @Override
    public String getPermission()
    {
        return permission;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getMinArgCount()
    {
        return minArgCount;
    }
}

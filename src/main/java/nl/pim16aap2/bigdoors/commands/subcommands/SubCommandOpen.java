package nl.pim16aap2.bigdoors.commands.subcommands;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.Door;
import nl.pim16aap2.bigdoors.commands.CommandData;
import nl.pim16aap2.bigdoors.commands.CommandManager;
import nl.pim16aap2.bigdoors.commands.CommandPermissionException;
import nl.pim16aap2.bigdoors.commands.CommandSenderNotPlayerException;
import nl.pim16aap2.bigdoors.util.RotateDirection;

public class SubCommandOpen extends SubCommandToggle
{
    private final String help = "Open a door";
    private static final CommandData command = CommandData.OPEN;

    public SubCommandOpen(final BigDoors plugin, final CommandManager commandManager)
    {
        super(plugin, commandManager);
    }

    public boolean readyToOpen(Door door)
    {
        return (door.getOpenDir().equals(RotateDirection.NONE) || !door.isOpen());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
        throws CommandSenderNotPlayerException, CommandPermissionException
    {
        ArrayList<Door> doors = new ArrayList<>();
        double time = parseDoorsAndTime(sender, args, doors);

        for (Door door : doors)
            if (readyToOpen(door))
                execute(sender, door, time);
            else
                plugin.getMyLogger().returnToSender(sender, Level.INFO, ChatColor.RED,
                                                    plugin.getMessages().getString("GENERAL.Door") + " \"" + door.getName() +
                                                    "\" " + plugin.getMessages().getString("GENERAL.DoorAlreadyOpen"));
        return doors.size() > 0;
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
    public int getMinArgCount()
    {
        return minArgCount;
    }

    @Override
    public CommandData getCommandData()
    {
        return command;
    }

    @Override
    public String getPermission()
    {
        return CommandData.getPermission(command);
    }

    @Override
    public String getName()
    {
        return CommandData.getCommandName(command);
    }
}
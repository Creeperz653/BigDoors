package nl.pim16aap2.bigdoors.commands.subcommands;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.commands.*;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.managers.CommandManager;
import nl.pim16aap2.bigdoors.spigotutil.DoorAttribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubCommandChangePowerBlock extends SubCommand
{
    protected static final String help = "Change the location of the powerblock of a door.";
    protected static final String argsHelp = "<doorUID/Name>";
    protected static final int minArgCount = 2;
    protected static final CommandData command = CommandData.CHANGEPOWERBLOCK;

    public SubCommandChangePowerBlock(final BigDoors plugin, final CommandManager commandManager)
    {
        super(plugin, commandManager);
        init(help, argsHelp, minArgCount, command);
    }

    public boolean execute(Player player, DoorBase door) throws CommandActionNotAllowedException
    {
        if (!plugin.getDatabaseManager().hasPermissionForAction(player, door.getDoorUID(), DoorAttribute.RELOCATEPOWERBLOCK))
            throw new CommandActionNotAllowedException();
        plugin.getDatabaseManager().startPowerBlockRelocator(player, door);
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
        throws CommandSenderNotPlayerException, CommandPermissionException, CommandActionNotAllowedException,
        CommandInvalidVariableException
    {
        if (!(sender instanceof Player))
            throw new CommandSenderNotPlayerException();

        Player player = (Player) sender;
        String doorArg = args[getMinArgCount() - 1];
        DoorBase door = plugin.getDatabaseManager().getDoor(doorArg, player);
        if (door == null)
            throw new CommandInvalidVariableException(doorArg, "door");
        return execute(player, door);
    }
}
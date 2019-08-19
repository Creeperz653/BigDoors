package nl.pim16aap2.bigdoors.commands.subcommands;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.commands.CommandData;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.exceptions.CommandPermissionException;
import nl.pim16aap2.bigdoors.exceptions.CommandSenderNotPlayerException;
import nl.pim16aap2.bigdoors.managers.CommandManager;
import nl.pim16aap2.bigdoors.util.messages.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SubCommandListDoors extends SubCommand
{
    protected static final String help = "Returns a list of all your doors";
    protected static final String argsHelp = "[doorName]";
    protected static final int minArgCount = 1;
    protected static final CommandData command = CommandData.LISTDOORS;

    public SubCommandListDoors(final BigDoors plugin, final CommandManager commandManager)
    {
        super(plugin, commandManager);
        init(help, argsHelp, minArgCount, command);
    }

    public boolean execute(CommandSender sender, List<DoorBase> doors)
    {
        if (doors.size() == 0)
        {
            plugin.getPLogger().sendMessageToTarget(sender, Level.INFO, messages.getString(Message.ERROR_NODOORSFOUND));
            return true;
        }
        StringBuilder builder = new StringBuilder();
        for (DoorBase door : doors)
            builder.append(door.getBasicInfo()).append("\n");
        plugin.getPLogger().sendMessageToTarget(sender, Level.INFO, builder.toString());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
                             @NotNull String[] args)
        throws CommandSenderNotPlayerException, CommandPermissionException
    {
        List<DoorBase> doors = new ArrayList<>();
        String name = args.length == minArgCount + 1 ? args[minArgCount] : null;
        // If a player requested the door, just get the doors they own.
        if (sender instanceof Player)
            doors.addAll(plugin.getDatabaseManager().getDoors(((Player) sender).getUniqueId(), name)
                               .orElse(new ArrayList<>()));
        else if (name != null)
        {
            // TODO: Shouldn't this use UIDs instead?
            // If the console requested the door(s), first try to get all doors with the provided name.
            doors.addAll(plugin.getDatabaseManager().getDoors(name).orElse(new ArrayList<>()));
            // If no door with the provided name could be found, list all doors owned by the
            // player with that name instead.
            if (doors.size() == 0)
                plugin.getDatabaseManager().getPlayerUUIDFromString(name).ifPresent(
                    P -> doors.addAll(plugin.getDatabaseManager().getDoors(P).orElse(new ArrayList<>())));
        }
        else
            return false;
        return execute(sender, doors);
    }
}
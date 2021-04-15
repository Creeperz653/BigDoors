package nl.pim16aap2.bigdoors.spigot.commands.subcommands;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.exceptions.CommandPermissionException;
import nl.pim16aap2.bigdoors.exceptions.CommandSenderNotPlayerException;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.spigot.commands.CommandData;
import nl.pim16aap2.bigdoors.spigot.managers.CommandManager;
import nl.pim16aap2.bigdoors.spigot.util.SpigotUtil;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.messages.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class SubCommandListDoors extends SubCommand
{
    protected static final String help = "Returns a list of all your doors";
    protected static final String argsHelp = "[doorName]";
    protected static final int minArgCount = 1;
    protected static final CommandData command = CommandData.LISTDOORS;

    public SubCommandListDoors(final @NonNull BigDoorsSpigot plugin, final @NonNull CommandManager commandManager)
    {
        super(plugin, commandManager);
        init(help, argsHelp, minArgCount, command);
    }

    public boolean execute(final @NonNull CommandSender sender, final @NonNull List<AbstractDoorBase> doors)
    {
        if (doors.isEmpty())
        {
            plugin.getPLogger().sendMessageToTarget(sender, Level.INFO, messages.getString(Message.ERROR_NODOORSFOUND));
            return true;
        }
        StringBuilder builder = new StringBuilder();
        for (AbstractDoorBase door : doors)
            builder.append(door.getBasicInfo()).append("\n");
        plugin.getPLogger().sendMessageToTarget(sender, Level.INFO, builder.toString());
        return true;
    }

    @Override
    public boolean onCommand(final @NonNull CommandSender sender, final @NonNull Command cmd,
                             final @NonNull String label, final @NonNull String[] args)
        throws CommandSenderNotPlayerException, CommandPermissionException
    {
        String name = args.length == minArgCount + 1 ? args[minArgCount] : null;

        if (sender instanceof Player)
            BigDoors.get().getDatabaseManager().getDoors(((Player) sender).getUniqueId(), name).whenComplete(
                (doorList, throwable) -> execute(sender, doorList));

        else if (name != null)
            // If the console requested the door(s), first try to get all doors with the provided name.
            BigDoors.get().getDatabaseManager().getDoors(name).whenComplete(
                (doorList, throwable) ->
                {
                    // If no door with the provided name could be found, list all doors owned by the
                    // player with that name instead.
                    if (doorList.isEmpty())
                    {
                        try
                        {
                            Optional<UUID> playerUUID = SpigotUtil.playerUUIDFromString(name);
                            if (playerUUID.isPresent())
                                doorList = BigDoors.get().getDatabaseManager().getDoors(playerUUID.get()).get();
                        }
                        catch (InterruptedException e)
                        {
                            BigDoors.get().getPLogger().logThrowable(e);
                            Thread.currentThread().interrupt();
                        }
                        catch (ExecutionException e)
                        {
                            plugin.getPLogger().logThrowable(e);
                        }
                    }
                    execute(sender, doorList);
                }).exceptionally(Util::exceptionally);
        else
            return false;
        return true;
    }
}

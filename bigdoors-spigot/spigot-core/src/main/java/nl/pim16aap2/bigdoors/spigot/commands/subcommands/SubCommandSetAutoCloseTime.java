package nl.pim16aap2.bigdoors.spigot.commands.subcommands;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doors.doorArchetypes.ITimerToggleableArchetype;
import nl.pim16aap2.bigdoors.exceptions.CommandActionNotAllowedException;
import nl.pim16aap2.bigdoors.exceptions.CommandPermissionException;
import nl.pim16aap2.bigdoors.exceptions.CommandPlayerNotFoundException;
import nl.pim16aap2.bigdoors.exceptions.CommandSenderNotPlayerException;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.spigot.commands.CommandData;
import nl.pim16aap2.bigdoors.spigot.managers.CommandManager;
import nl.pim16aap2.bigdoors.spigot.util.SpigotAdapter;
import nl.pim16aap2.bigdoors.spigot.waitforcommand.WaitForCommand;
import nl.pim16aap2.bigdoors.util.DoorAttribute;
import nl.pim16aap2.bigdoors.util.messages.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Level;

public class SubCommandSetAutoCloseTime extends SubCommand
{
    protected static final String help = "Changes/Sets time (in s) for doors to automatically close. -1 to disable.";
    protected static final String argsHelp = "{door} <time>";
    protected static final int minArgCount = 2;
    protected static final CommandData command = CommandData.SETAUTOCLOSETIME;

    public SubCommandSetAutoCloseTime(final @NotNull BigDoorsSpigot plugin,
                                      final @NotNull CommandManager commandManager)
    {
        super(plugin, commandManager);
        init(help, argsHelp, minArgCount, command);
    }

    private void sendResultMessage(final @NotNull CommandSender sender, final int time)
    {
        plugin.getPLogger()
              .sendMessageToTarget(sender, Level.INFO,
                                   time < 0 ?
                                   messages.getString(Message.COMMAND_SETTIME_SUCCESS, Integer.toString(time)) :
                                   messages.getString(Message.COMMAND_SETTIME_DISABLED));
    }

    public boolean execute(final @NotNull CommandSender sender, final @NotNull AbstractDoorBase door,
                           final @NotNull String timeArg)
        throws IllegalArgumentException
    {
        if (!(door instanceof ITimerToggleableArchetype))
            throw new IllegalArgumentException(
                "Doors of type: " + door.getDoorType().toString() + " do not have the \"autoCloseTimer\" property!");

        ITimerToggleableArchetype doorWithTimer = (ITimerToggleableArchetype) door;

        int time = CommandManager.getIntegerFromArg(timeArg);
        if (!(sender instanceof Player))
        {
            doorWithTimer.setAutoCloseTime(time);
            BigDoors.get().getDatabaseManager().updateDoorTypeData(door);
            BigDoors.get().getAutoCloseScheduler()
                    .scheduleAutoClose(door.getDoorOwner().getPlayer(),
                                       (AbstractDoorBase & ITimerToggleableArchetype) doorWithTimer, time, false);
            sendResultMessage(sender, time);
            return true;
        }

        final IPPlayer player = SpigotAdapter.wrapPlayer((Player) sender);
        BigDoors.get().getDatabaseManager()
                .hasPermissionForAction(player, door.getDoorUID(), DoorAttribute.CHANGETIMER).whenComplete(
            (isAllowed, throwable) ->
            {
                if (!isAllowed)
                {
                    commandManager.handleException(new CommandActionNotAllowedException(), sender, null, null);
                    return;
                }
                doorWithTimer.setAutoCloseTime(time);
                BigDoors.get().getDatabaseManager().updateDoorTypeData(door);
                BigDoors.get().getAutoCloseScheduler()
                        .scheduleAutoClose(player, (AbstractDoorBase & ITimerToggleableArchetype) doorWithTimer, time,
                                           false);
                sendResultMessage(sender, time);
            });
        return true;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd,
                             final @NotNull String label, final @NotNull String[] args)
        throws CommandSenderNotPlayerException, CommandPermissionException, CommandPlayerNotFoundException,
               CommandActionNotAllowedException, IllegalArgumentException
    {
        Optional<WaitForCommand> commandWaiter = commandManager.isCommandWaiter(sender, getName());
        if (commandWaiter.isPresent())
            return commandManager.commandWaiterExecute(commandWaiter.get(), args, minArgCount);

        commandManager.getDoorFromArg(sender, args[1], cmd, args).whenComplete(
            (optionalDoorBase, throwable) -> optionalDoorBase.ifPresent(door -> execute(sender, door, args[2])));
        return true;
    }
}
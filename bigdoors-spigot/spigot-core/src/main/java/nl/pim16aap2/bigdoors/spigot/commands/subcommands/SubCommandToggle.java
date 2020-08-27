package nl.pim16aap2.bigdoors.spigot.commands.subcommands;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.doors.DoorOpener;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionCause;
import nl.pim16aap2.bigdoors.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.exceptions.CommandActionNotAllowedException;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.spigot.commands.CommandData;
import nl.pim16aap2.bigdoors.spigot.managers.CommandManager;
import nl.pim16aap2.bigdoors.spigot.util.SpigotAdapter;
import nl.pim16aap2.bigdoors.util.DoorAttribute;
import nl.pim16aap2.bigdoors.util.Pair;
import nl.pim16aap2.bigdoors.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SubCommandToggle extends SubCommand
{
    protected final String help = "Toggle a door";
    protected final String argsHelp = "<doorUID/Name1> <doorUID/Name2> ... [time (decimal!)]";
    protected final int minArgCount = 2;
    protected final CommandData command = CommandData.TOGGLE;
    protected DoorActionType actionType = DoorActionType.TOGGLE;

    public SubCommandToggle(final @NotNull BigDoorsSpigot plugin, final @NotNull CommandManager commandManager)
    {
        super(plugin, commandManager);
        init(help, argsHelp, minArgCount, command);
    }

    public void execute(final @NotNull CommandSender sender, final @NotNull AbstractDoorBase door)
    {
        execute(sender, door, 0.0D);
    }

    private void toggleDoor(final @NotNull CommandSender sender, final @NotNull AbstractDoorBase door,
                            final double time)
    {
        final @Nullable IPPlayer player = sender instanceof Player ? SpigotAdapter.wrapPlayer((Player) sender) : null;
        final @NotNull DoorActionCause cause = player == null ? DoorActionCause.SERVER : DoorActionCause.PLAYER;
        DoorOpener.get().animateDoorAsync(door, cause, player, time, false, DoorActionType.TOGGLE);
    }

    public void execute(final @NotNull CommandSender sender, final @NotNull AbstractDoorBase door, final double time)
    {
        if (!(sender instanceof Player))
        {
            toggleDoor(sender, door, time);
            return;
        }
        BigDoors.get().getDatabaseManager()
                .hasPermissionForAction(SpigotAdapter.wrapPlayer((Player) sender), door.getDoorUID(),
                                        DoorAttribute.TOGGLE)
                .whenComplete(
                    (isAllowed, throwable) ->
                    {
                        if (!isAllowed)
                            commandManager.handleException(new CommandActionNotAllowedException(), sender, null, null);
                        else
                            toggleDoor(sender, door, time);
                        // No need to print result message here, that'll be done by the opening process of the door itself.
                    });
    }

    @NotNull
    private CompletableFuture<Double> parseDoorsAndTime(final @NotNull CommandSender sender,
                                                        final @NotNull String[] args,
                                                        final @NotNull List<AbstractDoorBase> doors)
        throws IllegalArgumentException
    {
        final String lastStr = args[args.length - 1];
        // First try to get a long from the last string. If it's successful, it must be a door UID.
        // If it isn't successful (-1), try to get parse it as a double. If that is successful, it
        // must be the speed. If that isn't successful either (0.0), it must be a door name.
        final @NotNull Pair<Boolean, Long> lastUID = Util.longFromString(lastStr);

        double time = 0.0d;
        if (!lastUID.first)
        {
            final @NotNull Pair<Boolean, Double> timeVal = Util.doubleFromString(lastStr);
            if (timeVal.first)
                time = timeVal.second;
        }

        int index = args.length;
        // If the time variable was specified, decrement endIDX by 1, as the last argument is not a door!
        if (time != 0.0D)
            --index;
        final int doorCount = index;

        double finalTime = time;
        return CompletableFuture.supplyAsync(
            () ->
            {
                int currentPos = doorCount;
                while (currentPos-- > 1)
                {
                    try
                    {
                        // TODO: Error when trying to toggle an invalid door.
                        commandManager.getDoorFromArg(sender, args[currentPos], null, null).get().ifPresent(doors::add);
                    }
                    catch (InterruptedException | ExecutionException e)
                    {
                        plugin.getPLogger().logException(e, "Failed to obtain door \"" + args[currentPos] + "\"");
                    }
                }
                return finalTime;
            });
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd,
                             final @NotNull String label, final @NotNull String[] args)
        throws IllegalArgumentException
    {
        final List<AbstractDoorBase> doors = new ArrayList<>();
        parseDoorsAndTime(sender, args, doors).whenComplete(
            (time, throwable) ->
            {
                for (AbstractDoorBase door : doors)
                    execute(sender, door, time);
            });
        return true;
    }
}
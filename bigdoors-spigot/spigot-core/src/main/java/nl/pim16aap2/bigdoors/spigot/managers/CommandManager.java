package nl.pim16aap2.bigdoors.spigot.managers;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.exceptions.CommandActionNotAllowedException;
import nl.pim16aap2.bigdoors.exceptions.CommandPermissionException;
import nl.pim16aap2.bigdoors.exceptions.CommandPlayerNotFoundException;
import nl.pim16aap2.bigdoors.exceptions.CommandSenderNotPlayerException;
import nl.pim16aap2.bigdoors.exceptions.NotEnoughDoorsException;
import nl.pim16aap2.bigdoors.exceptions.TooManyDoorsException;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.spigot.commands.CommandData;
import nl.pim16aap2.bigdoors.spigot.commands.ICommand;
import nl.pim16aap2.bigdoors.spigot.commands.subcommands.SubCommand;
import nl.pim16aap2.bigdoors.spigot.util.SpigotUtil;
import nl.pim16aap2.bigdoors.spigot.waitforcommand.WaitForCommand;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.messages.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Manages all {@link ICommand}s and {@link SubCommand}s.
 *
 * @author Pim
 */
public class CommandManager implements CommandExecutor
{
    private static final @NonNull String helpMessage = ChatColor.BLUE
        + "{}: Not required when used from GUI, <>: always required, []: optional\n";
    private final @NonNull BigDoorsSpigot plugin;
    private Map<String, ICommand> commands;
    private Map<CommandData, ICommand> commandsShortcut;

    public CommandManager(final BigDoorsSpigot plugin)
    {
        this.plugin = plugin;
        commands = new HashMap<>();
        commandsShortcut = new EnumMap<>(CommandData.class);
    }

    /**
     * Gets the UUID of a player from an input argument.
     *
     * @param playerArg The player name or UUID.
     * @return The UUID of the player.
     *
     * @throws CommandPlayerNotFoundException If no player was found.
     */
    public static @NonNull UUID getPlayerFromArg(final @NonNull String playerArg)
        throws CommandPlayerNotFoundException
    {
        Optional<UUID> playerUUID = SpigotUtil.playerUUIDFromString(playerArg);
        if (playerUUID.isEmpty())
            throw new CommandPlayerNotFoundException(playerArg);
        return playerUUID.get();
    }

    /**
     * Checks if a {@link CommandSender} has access to a {@link ICommand} or not. If the {@link CommandSender}.
     *
     * @param sender  The {@link CommandSender}.
     * @param command The {@link ICommand}.
     * @return True if the {@link CommandSender} has access to the {@link ICommand}.
     */
    public static boolean permissionForCommand(@NonNull CommandSender sender, @NonNull ICommand command)
    {
        return (sender instanceof Player ?
                ((Player) sender).hasPermission(command.getPermission()) || ((Player) sender).isOp() : true);
    }

    /**
     * Gets a long from a String.
     *
     * @param testLong A potential long in String form.
     * @return The long value.
     *
     * @throws IllegalArgumentException If the input argument was not a long.
     */
    public static long getLongFromArg(@NonNull String testLong)
        throws IllegalArgumentException
    {
        try
        {
            return Long.parseLong(testLong);
        }
        catch (Exception uncaught)
        {
            throw new IllegalArgumentException("\"" + testLong + "\" is not a valid long");
        }
    }

    /**
     * Gets a integer from a String.
     *
     * @param testInt A potential integer in String form.
     * @return The integer value.
     *
     * @throws IllegalArgumentException If the input argument was not an integer.
     */
    public static int getIntegerFromArg(@NonNull String testInt)
        throws IllegalArgumentException
    {
        try
        {
            return Integer.parseInt(testInt);
        }
        catch (Exception uncaught)
        {
            throw new IllegalArgumentException("\"" + testInt + "\" is not a valid integer");
        }
    }

    /**
     * Gets a float from a String.
     *
     * @param testFloat A potential float in String form.
     * @return The float value.
     *
     * @throws IllegalArgumentException If the input argument was not a float.
     */
    public static float getFloatFromArg(@NonNull String testFloat)
        throws IllegalArgumentException
    {
        try
        {
            return Float.parseFloat(testFloat);
        }
        catch (Exception uncaught)
        {
            throw new IllegalArgumentException("\"" + testFloat + "\" is not a valid float");
        }
    }

    /**
     * Gets the help message.
     *
     * @return The help message.
     */
    public static @NonNull String getHelpMessage()
    {
        return helpMessage;
    }

    /**
     * Registers an {@link ICommand} in this manager.
     *
     * @param command The {@link ICommand}.
     */
    public void registerCommand(final @NonNull ICommand command)
    {
        commands.put(command.getName().toLowerCase(), command);
        commandsShortcut.put(command.getCommandData(), command);
        plugin.getCommand(command.getName()).setExecutor(this);
    }

    /**
     * Registers a shortcut to a {@link SubCommand}, so the whole command tree doesn't have to get traversed to find
     * it.
     *
     * @param subCommand The {@link SubCommand}.
     */
    public void registerCommandShortcut(final @NonNull SubCommand subCommand)
    {
        commandsShortcut.put(subCommand.getCommandData(), subCommand);
    }

    /**
     * Gets an {@link ICommand} that is registered in this class.
     *
     * @param command The {@link CommandData} of the {@link ICommand}.
     * @return The {@link ICommand}.
     */
    public @NonNull ICommand getCommand(final @NonNull CommandData command)
    {
        return commandsShortcut.get(command);
    }

    /**
     * Handles an exception that occurred while a command was used.
     *
     * @param exception The exception to handle.
     * @param sender    The {@link CommandSender} that executed the command.
     * @param cmd       The command.
     * @param args      The arguments of the command.
     */
    // TODO: Don't violate NonNull, or change to Nullable.
    public void handleException(final @NonNull Exception exception, final @NonNull CommandSender sender,
                                final @NonNull Command cmd, final @NonNull String[] args)
    {
        if (exception instanceof CommandSenderNotPlayerException)
        {
            plugin.getPLogger()
                  .sendMessageToTarget(sender, Level.INFO, plugin.getMessages().getString(
                      Message.ERROR_COMMAND_NOTAPLAYER));
        }
        else if (exception instanceof CommandPermissionException)
        {
            plugin.getPLogger()
                  .sendMessageToTarget(sender, Level.INFO,
                                       plugin.getMessages().getString(Message.ERROR_COMMAND_NOPERMISSION));
        }
        else if (exception instanceof IllegalArgumentException)
        {
            plugin.getPLogger().sendMessageToTarget(sender, Level.INFO, ChatColor.RED + exception.getMessage());
        }
        else if (exception instanceof CommandPlayerNotFoundException)
        {
            plugin.getPLogger().sendMessageToTarget(sender, Level.INFO,
                                                    plugin.getMessages().getString(Message.ERROR_PLAYERNOTFOUND,
                                                                                   ((CommandPlayerNotFoundException) exception)
                                                                                       .getPlayerArg()));
        }
        else if (exception instanceof CommandActionNotAllowedException)
        {
            plugin.getPLogger()
                  .sendMessageToTarget(sender, Level.INFO,
                                       plugin.getMessages().getString(Message.ERROR_NOPERMISSIONFORACTION));
        }
        else
        {
            plugin.getPLogger()
                  .sendMessageToTarget(sender, Level.INFO, plugin.getMessages().getString(Message.ERROR_GENERALERROR));
            StringBuilder sb = new StringBuilder();
            for (String str : args)
                sb.append(str).append(str.equals(args[args.length - 1]) ? "" : ", ");
            plugin.getPLogger()
                  .logThrowable(exception, "An exception occurred while processing command \"" + cmd.getName()
                      + "\" with args: \"" + sb.toString() + "\"!");
        }
    }

    /**
     * Executes a command.
     *
     * @param sender The {@link CommandSender} that executed the command.
     * @param cmd    The command.
     * @param label  The label of the command.
     * @param args   The arguments of the command.
     * @return True if execution of the command was successful.
     */
    @Override
    public boolean onCommand(final @NonNull CommandSender sender, final @NonNull Command cmd,
                             final @NonNull String label, final @NonNull String[] args)
    {
        ICommand command = commands.get(cmd.getName().toLowerCase());
        try
        {
            if (!permissionForCommand(sender, command))
                throw new CommandPermissionException();
            return command.onCommand(sender, cmd, label, args);
        }
        catch (Exception e)
        {
            handleException(e, sender, cmd, args);
        }
        return true;
    }

    /**
     * Gets the {@link WaitForCommand} of a {@link CommandSender} for a command if one exists.
     *
     * @param sender      The {@link CommandSender}.
     * @param commandName The name of the {@link ICommand}.
     * @return The {@link WaitForCommand} of a {@link CommandSender} for a command if one exists.
     */
    public @NonNull Optional<WaitForCommand> isCommandWaiter(final @NonNull CommandSender sender,
                                                             final @NonNull String commandName)
    {
        if (!(sender instanceof Player))
            return Optional.empty();
        return plugin.getCommandWaiter((Player) sender).filter(CW -> CW.getCommand().equals(commandName));
    }

    /**
     * Executes the command of a {@link WaitForCommand}.
     *
     * @param commandWaiter The {@link WaitForCommand}.
     * @param args          The arguments of the command.
     * @param minArgCount   The minimum number of arguments of the command.
     * @return True if command execution was successful.
     *
     * @throws CommandPlayerNotFoundException   When a {@link Player} specified in the arguments was not found.
     * @throws CommandActionNotAllowedException When the action associated with the command was not allowed.
     */
    public boolean commandWaiterExecute(final @NonNull WaitForCommand commandWaiter, final @NonNull String args[],
                                        final int minArgCount)
        throws CommandActionNotAllowedException, CommandPlayerNotFoundException
    {
        commandWaiter.abortSilently();
        if (args.length == minArgCount)
            return commandWaiter.executeCommand(args);
        return false;
    }

    /**
     * Gets the {@link AbstractDoorBase} from a String. If the {@link CommandSender} is a {@link Player}, only {@link
     * AbstractDoorBase}s owned by them are considered, otherwise all doors are considered and the owner of any of the
     * resulting ones will be the original creator.
     *
     * @param sender  The {@link CommandSender}.
     * @param doorArg The name or UID of the  {@link AbstractDoorBase}.
     * @return The {@link AbstractDoorBase} if exactly 1 door was found.
     */
    public @NonNull CompletableFuture<Optional<AbstractDoorBase>> getDoorFromArg(final @NonNull CommandSender sender,
                                                                                 final @NonNull String doorArg,
                                                                                 final @NonNull Command cmd,
                                                                                 final @NonNull String[] args)
    {
        CompletableFuture<Optional<AbstractDoorBase>> door = null;

        if (sender instanceof Player)
        {
            door = BigDoors.get().getDatabaseManager().getDoors(((Player) sender).getUniqueId(), doorArg)
                           .<Optional<AbstractDoorBase>>handleAsync(
                               (doors, ex) ->
                               {
                                   if (doors.isEmpty())
                                   {
                                       handleException(new NotEnoughDoorsException(), sender, cmd, args);
                                       return Optional.empty();
                                   }
                                   else if (doors.size() > 1)
                                   {
                                       handleException(new TooManyDoorsException(), sender, cmd, args);
                                       return Optional.empty();
                                   }
                                   return Optional.of(doors.get(0));
                               }).exceptionally(Util::exceptionallyOptional);
        }
        else
        {
            try
            {
                door = BigDoors.get().getDatabaseManager().getDoor(Long.parseLong(doorArg))
                               .exceptionally(Util::exceptionallyOptional);
            }
            catch (NumberFormatException e)
            {
                plugin.getPLogger().info("\"" + doorArg + "\" " +
                                             plugin.getMessages().getString(Message.ERROR_INVALIDDOORID, doorArg));
            }
        }
        if (door == null)
        {
            handleException(new IllegalArgumentException("\"" + doorArg + "\" is not a valid door!"),
                            sender, cmd, args);
            door = CompletableFuture.completedFuture(Optional.empty());
        }
        return door;
    }
}

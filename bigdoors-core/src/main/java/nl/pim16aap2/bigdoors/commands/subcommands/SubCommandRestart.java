package nl.pim16aap2.bigdoors.commands.subcommands;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.commands.CommandData;
import nl.pim16aap2.bigdoors.exceptions.CommandPermissionException;
import nl.pim16aap2.bigdoors.exceptions.CommandSenderNotPlayerException;
import nl.pim16aap2.bigdoors.managers.CommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class SubCommandRestart extends SubCommand
{
    protected static final String help = "Restart the plugin. Reinitializes almost everything.";
    protected static final String argsHelp = null;
    protected static final int minArgCount = 0;
    protected static final CommandData command = CommandData.RESTART;

    public SubCommandRestart(final BigDoors plugin, final CommandManager commandManager)
    {
        super(plugin, commandManager);
        init(help, argsHelp, minArgCount, command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label,
                             @NotNull String[] args)
        throws CommandSenderNotPlayerException, CommandPermissionException
    {
        plugin.restart();
        plugin.getPLogger().sendMessageToTarget(sender, Level.INFO, "BigDoors has been restarted!");
        return true;
    }
}
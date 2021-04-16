package nl.pim16aap2.bigdoors.spigot.waitforcommand;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.exceptions.CommandActionNotAllowedException;
import nl.pim16aap2.bigdoors.exceptions.CommandPlayerNotFoundException;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.spigot.commands.subcommands.SubCommand;
import nl.pim16aap2.bigdoors.spigot.util.AbortableTask;
import nl.pim16aap2.bigdoors.spigot.util.SpigotUtil;
import nl.pim16aap2.bigdoors.util.messages.Message;
import org.bukkit.entity.Player;

/**
 * Represents a delayed command.
 *
 * @author Pim
 */
public abstract class WaitForCommand extends AbortableTask
{
    protected final @NonNull BigDoorsSpigot plugin;
    protected final @NonNull SubCommand subCommand;
    protected @NonNull Player player;
    protected boolean isFinished = false;

    protected WaitForCommand(final @NonNull BigDoorsSpigot plugin, final @NonNull SubCommand subCommand)
    {
        this.plugin = plugin;
        this.subCommand = subCommand;
        plugin.addCommandWaiter(this);
    }

    @Override
    public final void abort(final boolean onDisable)
    {
        if (!onDisable)
        {
            killTask();
            plugin.removeCommandWaiter(this);
            if (!isFinished)
                SpigotUtil.messagePlayer(player, plugin.getMessages().getString(Message.COMMAND_TIMEOUTORFAIL));
        }
    }

    @Override
    public final void abortSilently()
    {
        setFinished(true);
        abort();
    }

    /**
     * Gets the name of the command that this waiter is waiting for.
     *
     * @return The name of the command that this waiter is waiting for.
     */
    public final @NonNull String getCommand()
    {
        return subCommand.getName();
    }

    /**
     * Executes the command that this waiter is waiting for.
     *
     * @param args The arguments of the command.
     * @return True if command execution was successful.
     *
     * @throws CommandPlayerNotFoundException   When a player specified in the arguments could not be found.
     * @throws CommandActionNotAllowedException When the player executing the command does not have access to this
     *                                          action.
     * @throws IllegalArgumentException         If at least one of the provided arguments is illegal.
     */
    public abstract boolean executeCommand(final @NonNull String[] args)
        throws CommandPlayerNotFoundException, CommandActionNotAllowedException, IllegalArgumentException;

    /**
     * Gets the player that will execute the command.
     *
     * @return The player that will execute the command.
     */
    public final @NonNull Player getPlayer()
    {
        return player;
    }

    /**
     * Changes the finished status of this waiter.
     *
     * @param finished The finished status of this waiter.
     */
    void setFinished(final boolean finished)
    {
        isFinished = finished;
    }
}

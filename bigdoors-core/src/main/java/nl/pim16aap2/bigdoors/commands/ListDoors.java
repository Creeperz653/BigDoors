package nl.pim16aap2.bigdoors.commands;

import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.util.DoorRetriever;
import nl.pim16aap2.bigdoors.util.pair.BooleanPair;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a command to list a number of doors matching a single {@link DoorRetriever}. This is basically only useful
 * for String-based lookups (as there aren't duplicate matches otherwise), but I don't judge.
 *
 * @author Pim
 */
@ToString
public class ListDoors extends BaseCommand
{
    final @NonNull DoorRetriever doorRetriever;

    protected ListDoors(final @NonNull ICommandSender commandSender, final @NonNull DoorRetriever doorRetriever)
    {
        super(commandSender);
        this.doorRetriever = doorRetriever;
    }

    /**
     * Runs the {@link ListDoors} command.
     *
     * @param commandSender The {@link ICommandSender} responsible for retrieving the information for the doors.
     *                      <p>
     *                      This is also the entity that will be informed about the doors that were found.
     * @param doorRetriever A {@link DoorRetriever} representing any number of {@link AbstractDoorBase}s.
     * @return See {@link BaseCommand#run()}.
     */
    public static @NonNull CompletableFuture<Boolean> run(final @NonNull ICommandSender commandSender,
                                                          final @NonNull DoorRetriever doorRetriever)
    {
        return new ListDoors(commandSender, doorRetriever).run();
    }

    @Override
    public @NonNull CommandDefinition getCommand()
    {
        return CommandDefinition.LIST_DOORS;
    }

    @Override
    protected @NonNull CompletableFuture<Boolean> executeCommand(final @NonNull BooleanPair permissions)
    {
        final @NonNull CompletableFuture<List<AbstractDoorBase>> doors;
        if (permissions.second || !(getCommandSender() instanceof IPPlayer))
            doors = doorRetriever.getDoors();
        else
            doors = doorRetriever.getDoors((IPPlayer) getCommandSender());

        return doors.thenAccept(this::sendDoorList).thenApply(val -> true);
    }

    private void sendDoorList(final @NonNull List<AbstractDoorBase> doors)
    {
        if (doors.isEmpty())
        {
            // TODO: Localization
            getCommandSender().sendMessage("No doors found!");
            return;
        }

        // TODO: Localization
        final StringBuilder sb = new StringBuilder("List of doors:\n");
        for (val door : doors)
            sb.append("  ").append(door.getBasicInfo()).append("\n");
        getCommandSender().sendMessage(sb.toString());
    }
}
package nl.pim16aap2.bigdoors.commands;

import lombok.ToString;
import lombok.val;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.commands.ICommandSender;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.api.util.DoorAttribute;
import nl.pim16aap2.bigdoors.util.DoorRetriever;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Represents the command that is used to change whether or not a door is locked.
 *
 * @author Pim
 */
@ToString
public class Lock extends DoorTargetCommand
{
    private final boolean lockStatus;

    protected Lock(final @NotNull ICommandSender commandSender, final @NotNull DoorRetriever doorRetriever,
                   final boolean lockStatus)
    {
        super(commandSender, doorRetriever, DoorAttribute.LOCK);
        this.lockStatus = lockStatus;
    }

    /**
     * Runs the {@link Lock} command.
     *
     * @param commandSender The {@link ICommandSender} responsible for changing the locked status of the door.
     * @param doorRetriever A {@link DoorRetriever} representing the {@link AbstractDoorBase} for which the locked
     *                      status will be modified.
     * @param lock          The new lock status.
     * @return See {@link BaseCommand#run()}.
     */
    public static @NotNull CompletableFuture<Boolean> run(final @NotNull ICommandSender commandSender,
                                                          final @NotNull DoorRetriever doorRetriever,
                                                          final boolean lock)
    {
        return new Lock(commandSender, doorRetriever, lock).run();
    }

    @Override
    public @NotNull CommandDefinition getCommand()
    {
        return CommandDefinition.LOCK;
    }

    @Override
    protected @NotNull CompletableFuture<Boolean> performAction(final @NotNull AbstractDoorBase door)
    {
        val event = BigDoors.get().getPlatform().getBigDoorsEventFactory()
                            .createDoorPrepareLockChangeEvent(door, lockStatus,
                                                              getCommandSender().getPlayer().orElse(null));
        BigDoors.get().getPlatform().callDoorEvent(event);

        if (event.isCancelled())
        {
            BigDoors.get().getPLogger().logMessage(Level.FINEST, "Event " + event + " was cancelled!");
            return CompletableFuture.completedFuture(true);
        }

        return door.setLocked(lockStatus).syncData().thenApply(x -> true);
    }
}

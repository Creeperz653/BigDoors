package nl.pim16aap2.bigdoors.commands;

import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.ICommandSender;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.doors.AbstractDoorBase;
import nl.pim16aap2.bigdoors.util.DoorAttribute;
import nl.pim16aap2.bigdoors.util.DoorRetriever;
import nl.pim16aap2.bigdoors.util.Util;
import nl.pim16aap2.bigdoors.util.pair.BooleanPair;

import java.util.concurrent.CompletableFuture;

/**
 * Represents the command that adds co-owners to a given door.
 *
 * @author Pim
 */
@ToString
public class AddOwner extends BaseCommand
{
    private final @NonNull DoorRetriever doorRetriever;
    private final @NonNull IPPlayer targetPlayer;
    private final int targetPermissionLevel;

    public AddOwner(@NonNull ICommandSender commandSender, @NonNull DoorRetriever doorRetriever,
                    @NonNull IPPlayer targetPlayer, int targetPermissionLevel)
    {
        super(commandSender);
        this.doorRetriever = doorRetriever;
        this.targetPlayer = targetPlayer;
        this.targetPermissionLevel = targetPermissionLevel;
    }

    @Override
    public @NonNull CommandDefinition getCommand()
    {
        return CommandDefinition.ADD_OWNER;
    }

    @Override
    protected boolean validInput()
    {
        return targetPermissionLevel == 1 || targetPermissionLevel == 2;
    }

    @Override
    protected @NonNull CompletableFuture<Boolean> executeCommand(@NonNull BooleanPair permissions)
    {
        final CompletableFuture<Boolean> ret = new CompletableFuture<>();

        getDoor(doorRetriever).thenApply(door ->
                                         {
                                             if (door.isEmpty() || !isAllowed(door.get(), permissions.second))
                                                 ret.complete(false);
                                             return door.orElse(null);
                                         })
                              .thenCompose(door -> BigDoors.get().getDatabaseManager().addOwner(door, targetPlayer,
                                                                                                targetPermissionLevel))
                              .thenAccept(ret::complete)
                              .exceptionally(t -> Util.exceptionallyCompletion(t, null, ret));

        return ret.exceptionally(t -> Util.exceptionally(t, false));
    }

    private boolean isAllowed(@NonNull AbstractDoorBase door, boolean hasBypassPermission)
    {
        if (!getCommandSender().isPlayer() || hasBypassPermission)
            return true;

        val doorOwner = getCommandSender().getPlayer().flatMap(door::getDoorOwner);
        if (doorOwner.isEmpty())
        {
            // TODO: Localization
            getCommandSender().sendMessage("You are not an owner of this door!");
            return false;
        }

        if (doorOwner.get().getPermission() > DoorAttribute.getPermissionLevel(DoorAttribute.ADDOWNER))
        {
            // TODO: Localization
            getCommandSender().sendMessage("Your are not allowed to add co-owners to this door!");
            return false;
        }

        if (doorOwner.get().getPermission() < targetPermissionLevel)
        {
            // TODO: Localization
            getCommandSender().sendMessage("You cannot add co-owners with a higher permission level!");
            return false;
        }

        return true;
    }
}

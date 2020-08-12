package nl.pim16aap2.bigdoors.tooluser;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPLocationConst;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.api.IRestartable;
import nl.pim16aap2.bigdoors.managers.ToolUserManager;
import nl.pim16aap2.bigdoors.util.Cuboid;
import nl.pim16aap2.bigdoors.util.PLogger;
import nl.pim16aap2.bigdoors.util.messages.Message;
import nl.pim16aap2.bigdoors.util.messages.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class ToolUser implements IRestartable
{
    @Getter
    @NonNull
    protected final IPPlayer player;
    @NotNull
    protected final Messages messages = BigDoors.get().getPlatform().getMessages();
    @NotNull
    protected final Procedure<?> procedure;

    /**
     * Keeps track of whether this {@link ToolUser} is active or not.
     */
    protected boolean active = true;

    protected ToolUser(final @NotNull IPPlayer player)
    {
        this.player = player;
        procedure = getProcedure();
        ToolUserManager.get().registerToolUser(this);
    }

    /**
     * Gets the {@link Procedure} that this {@link ToolUser} will go through.
     *
     * @return The {@link Procedure} for this {@link ToolUser}.
     */
    @NotNull
    protected abstract Procedure<?> getProcedure();

    /**
     * Takes care of the final part of the process. This unregisters this {@link ToolUser} and removes the tool from the
     * player's inventory (if they still have it).
     */
    protected final void cleanUpProcess()
    {
        ToolUserManager.get().removeToolUser(this);
        removeTool();
        active = false;
    }

    @Override
    public void restart()
    {
        cleanUpProcess();
    }

    @Override
    public void shutdown()
    {
        cleanUpProcess();
    }

    /**
     * Adds the BigDoors tool from the player's inventory.
     *
     * @param name    The name of the tool.
     * @param lore    The lore of the tool.
     * @param message The message to send to the player after giving them the tool.
     */
    protected final void giveTool(final @NotNull Message name, final @NotNull Message lore,
                                  final @Nullable Message message)
    {
        BigDoors.get().getPlatform().getBigDoorsToolUtil()
                .giveToPlayer(player, messages.getString(name), messages.getString(lore));

        if (message != null)
            player.sendMessage(messages.getString(message));
    }

    /**
     * Removes the BigDoors tool from the player's inventory.
     */
    protected final void removeTool()
    {
        BigDoors.get().getPlatform().getBigDoorsToolUtil().removeTool(player);
    }

    /**
     * Gets the message for the current step.
     *
     * @return The message of the current step if possible. Otherwise, an empty String is returned.
     */
    @NotNull
    public String getCurrentStepMessage()
    {
        return procedure.getMessage();
    }

    /**
     * Prepares the next step. For example by sending the player some instructions about what they should do.
     */
    protected void prepareCurrentStep()
    {
        sendMessage();
        if (!procedure.waitForUserInput())
            procedure.applyStepExecutor(null);
    }

    /**
     * Sends the localized message of the current step to the player that owns this object.
     */
    protected void sendMessage()
    {
        final @NotNull String message = procedure.getMessage();
        if (message.isEmpty())
            PLogger.get().warn("Missing translation for step: " + procedure.getStepClassName());
        else
            player.sendMessage(message);
    }

    /**
     * Handles user input for the given step.
     *
     * @param obj The
     * @return True if the input was processed successfully.
     */
    public boolean handleInput(final @NotNull Object obj)
    {
        if (!active)
            return false;

        try
        {
            if (procedure.applyStepExecutor(obj))
            {
                // The process may have been cancelled, so check to make sure.
                if (active)
                    prepareCurrentStep();
                return true;
            }
            // Repeat the instruction for the current step if the input was incorrect.
            sendMessage();
        }
        catch (Exception e)
        {
            PLogger.get().logException(e);
        }

        return false;
    }

    /**
     * Checks if a player is allowed to break the block in a given location.
     * <p>
     * If the player is not allowed to break blocks in the location, a message will be sent to them (provided the name
     * of the compat isn't empty).
     *
     * @param loc The location to check.
     * @return True if the player is allowed to break the block at the given location.
     */
    protected boolean playerHasAccessToLocation(final @NotNull IPLocationConst loc)
    {
        final @NotNull Optional<String> result = BigDoors.get().getPlatform().getProtectionCompatManager()
                                                         .canBreakBlock(player, loc);

        result.ifPresent(
            compat ->
            {
                if (!compat.isEmpty())
                    player.sendMessage(messages.getString(Message.ERROR_NOPERMISSIONFORLOCATION, compat));
            });
        return !result.isPresent();
    }

    /**
     * Checks if a player is allowed to break all blocks in a given cuboid.
     * <p>
     * If the player is not allowed to break one or more blocks in the cuboid, a message will be sent to them. (provided
     * the name of the compat isn't empty).
     *
     * @param cuboid The cuboid to check.
     * @param world  The world to check in.
     * @return True if the player is allowed to break all blocks inside the cuboid.
     */
    protected boolean playerHasAccessToCuboid(final @NotNull Cuboid cuboid, final @NotNull IPWorld world)
    {
        final @NotNull Optional<String> result = BigDoors.get().getPlatform().getProtectionCompatManager()
                                                         .canBreakBlocksBetweenLocs(player, cuboid.getMin(),
                                                                                    cuboid.getMax(), world);

        result.ifPresent(
            compat ->
            {
                if (!compat.isEmpty())
                    player.sendMessage(messages.getString(Message.ERROR_NOPERMISSIONFORLOCATION, compat));
            });
        return !result.isPresent();
    }
}

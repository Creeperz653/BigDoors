package nl.pim16aap2.bigdoors.commands;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import lombok.ToString;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.localization.ILocalizer;
import nl.pim16aap2.bigdoors.logging.IPLogger;
import nl.pim16aap2.bigdoors.util.CompletableFutureHandler;
import nl.pim16aap2.bigdoors.util.pair.BooleanPair;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Represents the menu command, which is used to open the menu for a user.
 *
 * @author Pim
 */
@ToString
public class Menu extends BaseCommand
{
    private final @Nullable IPPlayer target;

    @AssistedInject //
    Menu(@Assisted ICommandSender commandSender, IPLogger logger, ILocalizer localizer,
         @Assisted @Nullable IPPlayer target, CompletableFutureHandler handler)
    {
        super(commandSender, logger, localizer, handler);
        this.target = target;
    }

    @Override
    public CommandDefinition getCommand()
    {
        return CommandDefinition.MENU;
    }

    @Override
    protected boolean availableForNonPlayers()
    {
        return false;
    }

    @Override
    protected CompletableFuture<Boolean> executeCommand(BooleanPair permissions)
    {
        // You need the bypass permission to open menus that aren't your own.
        if (!permissions.second && !getCommandSender().equals(target))
            return CompletableFuture.completedFuture(false);

        throw new UnsupportedOperationException("This command has not yet been implemented!");
    }

    @AssistedFactory
    interface IFactory
    {
        /**
         * Creates (but does not execute!) a new {@link Menu} command.
         *
         * @param commandSender
         *     The {@link ICommandSender} responsible for opening the menu.
         *     <p>
         *     This is the entity to which the menu will be shown, and as such this must be an {@link IPPlayer} (menus
         *     aren't supported for servers/command blocks).
         * @param target
         *     The {@link IPPlayer} whose doors will be used.
         *     <p>
         *     When this is null (default), the command sender's own doors will be used.
         * @return See {@link BaseCommand#run()}.
         */
        Menu newMenu(ICommandSender commandSender, @Nullable IPPlayer target);

        /**
         * See {@link #newMenu(ICommandSender, IPPlayer)}.
         */
        default Menu newMenu(ICommandSender commandSender)
        {
            return newMenu(commandSender, null);
        }
    }
}
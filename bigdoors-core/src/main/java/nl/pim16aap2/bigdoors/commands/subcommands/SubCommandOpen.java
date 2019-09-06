package nl.pim16aap2.bigdoors.commands.subcommands;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.commands.CommandData;
import nl.pim16aap2.bigdoors.managers.CommandManager;
import org.jetbrains.annotations.NotNull;

public class SubCommandOpen extends SubCommandToggle
{
    protected static final CommandData command = CommandData.OPEN;
    protected final String help = "Open a door";

    public SubCommandOpen(final @NotNull BigDoors plugin, final @NotNull CommandManager commandManager)
    {
        super(plugin, commandManager);
        init(help, argsHelp, minArgCount, command);
        super.actionType = DoorActionType.OPEN;
    }
}

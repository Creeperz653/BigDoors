package nl.pim16aap2.bigdoors.commands.subcommands;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.events.dooraction.DoorActionType;
import nl.pim16aap2.bigdoors.commands.CommandData;
import nl.pim16aap2.bigdoors.managers.CommandManager;
import org.jetbrains.annotations.NotNull;

public class SubCommandClose extends SubCommandToggle
{
    protected final String help = "Close a door.";
    protected final CommandData command = CommandData.CLOSE;

    public SubCommandClose(final @NotNull BigDoors plugin, final @NotNull CommandManager commandManager)
    {
        super(plugin, commandManager);
        init(help, argsHelp, minArgCount, command);
        super.actionType = DoorActionType.CLOSE;
    }
}

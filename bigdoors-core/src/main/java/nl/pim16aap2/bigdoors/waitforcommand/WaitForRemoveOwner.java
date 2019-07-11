package nl.pim16aap2.bigdoors.waitforcommand;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.commands.subcommands.SubCommandRemoveOwner;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.exceptions.CommandActionNotAllowedException;
import nl.pim16aap2.bigdoors.exceptions.CommandPlayerNotFoundException;
import nl.pim16aap2.bigdoors.spigotutil.SpigotUtil;
import nl.pim16aap2.bigdoors.util.DoorOwner;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class WaitForRemoveOwner extends WaitForCommand
{
    private final DoorBase door;
    private final SubCommandRemoveOwner subCommand;

    public WaitForRemoveOwner(final BigDoors plugin, final SubCommandRemoveOwner subCommand, final Player player,
                              final DoorBase door)
    {
        super(plugin, subCommand);
        this.subCommand = subCommand;
        this.player = player;
        this.door = door;
        SpigotUtil.messagePlayer(player, plugin.getMessages().getString("COMMAND.RemoveOwner.Init"));
        SpigotUtil.messagePlayer(player, plugin.getMessages().getString("COMMAND.RemoveOwner.ListOfOwners"));

        ArrayList<DoorOwner> doorOwners = plugin.getDatabaseManager().getDoorOwners(door.getDoorUID());
        StringBuilder builder = new StringBuilder();
        for (DoorOwner owner : doorOwners)
            builder.append(owner.getPlayerName()).append(", ");
        SpigotUtil.messagePlayer(player, builder.toString());
    }

    @Override
    public boolean executeCommand(String[] args)
            throws CommandPlayerNotFoundException, CommandActionNotAllowedException, IllegalArgumentException
    {
        abortSilently();
        return subCommand.execute(player, door, args[2]);
    }
}
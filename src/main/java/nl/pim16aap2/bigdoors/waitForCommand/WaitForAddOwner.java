package nl.pim16aap2.bigdoors.waitForCommand;

import java.util.UUID;

import org.bukkit.entity.Player;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.Door;
import nl.pim16aap2.bigdoors.util.DoorAttribute;
import nl.pim16aap2.bigdoors.util.Util;

public class WaitForAddOwner extends WaitForCommand
{
    private long doorUID;

    public WaitForAddOwner(BigDoors plugin, Player player, long doorUID)
    {
        super(plugin);
        this.player  = player;
        command = "addowner";
        this.doorUID = doorUID;
        Util.messagePlayer(player, plugin.getMessages().getString("COMMAND.AddOwner.Init"));
        plugin.addCommandWaiter(this);
    }

    @Override
    public boolean executeCommand(String[] args)
    {
        if (!plugin.getCommander().hasPermissionForAction(player, doorUID, DoorAttribute.ADDOWNER))
            return true;

        // Example: /BigDoors addOwner pim16aap2 1
        if (args.length >= 2)
        {
            UUID playerUUID = plugin.getCommander().playerUUIDFromName(args[1]);
            Door door = plugin.getCommander().getDoor(player.getUniqueId(), doorUID);
            int permission = 1;
            try
            {
                if (args.length == 3)
                    permission = Integer.parseInt(args[2]);
            }
            catch (Exception e)
            {
                Util.messagePlayer(player, plugin.getMessages().getString("GENERAL.InvalidInput.Integer"));
            }
            if (playerUUID != null)
            {
                if (plugin.getCommander().addOwner(playerUUID, door, permission))
                {
                    Util.messagePlayer(player, plugin.getMessages().getString("COMMAND.AddOwner.Success"));
                    isFinished = true;
                    abort();
                    return true;
                }
                Util.messagePlayer(player, plugin.getMessages().getString("COMMAND.AddOwner.Fail"));
                abort();
                return true;
            }
            Util.messagePlayer(player, plugin.getMessages().getString("GENERAL.PlayerNotFound") + ": \"" + args[1] + "\"");
            abort();
            return true;
        }
        abort();
        return false;
    }
}

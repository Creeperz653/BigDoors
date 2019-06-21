package nl.pim16aap2.bigdoors.toolusers;

import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.commands.CommandData;
import nl.pim16aap2.bigdoors.commands.subcommands.SubCommandInfo;
import nl.pim16aap2.bigdoors.doors.DoorBase;
import nl.pim16aap2.bigdoors.spigotutil.Util;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PowerBlockInspector extends ToolUser
{
    public PowerBlockInspector(BigDoors plugin, Player player, long doorUID)
    {
        super(plugin, player);
        this.doorUID = doorUID;
        Util.messagePlayer(player, messages.getString("CREATOR.PBINSPECTOR.Init"));
        triggerGiveTool();
    }

    @Override
    protected void triggerGiveTool()
    {
        giveToolToPlayer(messages.getString("CREATOR.PBINSPECTOR.StickLore"    ).split("\n"),
                         messages.getString("CREATOR.PBINSPECTOR.StickReceived").split("\n"));
    }

    @Override
    protected void triggerFinishUp()
    {
        finishUp();
    }

    // Take care of the selection points.
    @Override
    public void selector(Location loc)
    {
        done = true;
        DoorBase door = plugin.getDatabaseManager().doorFromPowerBlockLoc(loc);
        if (door != null)
        {
            ((SubCommandInfo) plugin.getCommand(CommandData.INFO)).execute(player, door);
            setIsDone(true);
        }
    }
}
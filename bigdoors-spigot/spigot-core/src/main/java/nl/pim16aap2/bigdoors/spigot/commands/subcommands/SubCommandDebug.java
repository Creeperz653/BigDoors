package nl.pim16aap2.bigdoors.spigot.commands.subcommands;

import nl.pim16aap2.bigdoors.exceptions.CommandPermissionException;
import nl.pim16aap2.bigdoors.exceptions.CommandSenderNotPlayerException;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.spigot.commands.CommandData;
import nl.pim16aap2.bigdoors.spigot.managers.CommandManager;
import nl.pim16aap2.bigdoors.spigot.util.SpigotAdapter;
import nl.pim16aap2.bigdoors.tooluser.creator.CreatorWindMill;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/*
 * This class really does whatever I want to test at a given point.
 * I do not guarantee I will clean it up between releases and most
 * definitely do not guarantee that this command will have the same
 * or even remotely similar effects between commits, let alone release.
 * As such, this command should not be used by anyone, hence the weird
 * permission node (so it isn't accidentally included by bigdoors.*).
 * I would hardcode my own username, but I think that that'd be a bit much.
 */
public class SubCommandDebug extends SubCommand
{
    protected static final String help = "Do not use this unless you are me... What?";
    protected static final String argsHelp = null;
    protected static final int minArgCount = 1;
    protected static final CommandData command = CommandData.DEBUG;

    public SubCommandDebug(final @NotNull BigDoorsSpigot plugin, final @NotNull CommandManager commandManager)
    {
        super(plugin, commandManager);
        init(help, argsHelp, minArgCount, command);
    }

    public boolean execute(CommandSender sender)
    {
        if (!(sender instanceof Player))
            return false;

//        new CreatorBigDoor(SpigotAdapter.wrapPlayer((Player) sender));

        new CreatorWindMill(SpigotAdapter.wrapPlayer((Player) sender), "test");

//        BigDoors.get().getDatabaseManager().updateDoorCoords(236L, false, 128, 76, 140, 131, 79, 140);
//        BigDoors.get().getDatabaseManager().getDoor(236L).ifPresent(door -> BigDoors.get().getDatabaseManager().fillDoor((door)));
//        if (sender instanceof Player)
//            plugin.getGlowingBlockSpawner()
//                  .spawnGlowinBlock(((Player) sender).getUniqueId(), ((Player) sender).getWorld().getName(), 60,
//                                    128, 76, 140);
//        long worldTime = ((Player) sender).getWorld().getTime();
//        Bukkit.broadcastMessage("WorldTime: " + worldTime +
//                                    ", WorldTimeObject: " + new WorldTime(worldTime).toString());


        return true;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd,
                             final @NotNull String label, final @NotNull String[] args)
        throws CommandSenderNotPlayerException, CommandPermissionException
    {
        return execute(sender);
    }
}
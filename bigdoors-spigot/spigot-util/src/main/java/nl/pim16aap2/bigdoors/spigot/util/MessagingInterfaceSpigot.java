package nl.pim16aap2.bigdoors.spigot.util;

import nl.pim16aap2.bigdoors.api.IMessagingInterface;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.logging.IPLogger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Implementation of {@link IMessagingInterface} for Spigot.
 *
 * @author Pim
 * @see IMessagingInterface
 */
public class MessagingInterfaceSpigot implements IMessagingInterface
{
    private final String formattedName;

    public MessagingInterfaceSpigot(final JavaPlugin plugin)
    {
        formattedName = IPLogger.formatName(plugin.getName());
    }

    @Override
    public void writeToConsole(final Level level, final String message)
    {
        Bukkit.getLogger().log(level, formattedName + message);
    }

    @Override
    public void messagePlayer(IPPlayer player, String message)
    {

        Player bukkitPlayer = Bukkit.getPlayer(player.getUUID());
        if (bukkitPlayer == null)
            return;
        SpigotUtil.messagePlayer(bukkitPlayer, message);
    }

    @Override
    public void sendMessageToTarget(final Object target, final Level level,
                                    final String message)
    {
        if (target instanceof Player)
            SpigotUtil.messagePlayer((Player) target, message);
        else
            writeToConsole(level, ChatColor.stripColor(message));
    }

    @Override
    public void broadcastMessage(final String message)
    {
        Bukkit.broadcastMessage(message);
    }
}

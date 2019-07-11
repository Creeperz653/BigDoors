package nl.pim16aap2.bigDoors.compatiblity;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import nl.pim16aap2.bigDoors.BigDoors;
import nl.pim16aap2.bigDoors.util.Util;

/**
 * Class that manages all objects of {@link ProtectionCompat}.
 *
 * @author Pim
 */
public class ProtectionCompatManager implements Listener
{
    private static final String BYPASSPERMISSION = "bigdoors.admin.bypasscompat";

    private final ArrayList<IProtectionCompat> protectionCompats;
    private FakePlayerCreator fakePlayerCreator;
    private final BigDoors plugin;

    /**
     * Constructor of {@link ProtectionCompatManager}.
     *
     * @param plugin The instance of {@link BigDoors}.
     */
    public ProtectionCompatManager(final BigDoors plugin)
    {
        this.plugin = plugin;
        fakePlayerCreator = new FakePlayerCreator(plugin);
        protectionCompats = new ArrayList<>();
        restart();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reinitialize all protection compats.
     */
    public void restart()
    {
        protectionCompats.clear();
        for (Plugin p : plugin.getServer().getPluginManager().getPlugins())
            loadFromPluginName(p.getName());
    }

    /**
     * Check if a player is allowed to bypass the compatibility checks. Players can
     * bypass the check if they are OP or if they have the
     * {@link ProtectionCompatManager#BYPASSPERMISSION} permission node.
     *
     * @param player The {@link Player} to check the permissions for.
     * @return True if the player can bypass the checks.
     */
    private boolean canByPass(Player player)
    {
        if (player.isOp())
            return true;

        // offline players don't have permissions, so use Vault if that's the case.
        if (!player.hasMetadata(FakePlayerCreator.FAKEPLAYERMETADATA))
            return player.hasPermission(BYPASSPERMISSION);
        return plugin.getVaultManager().hasPermission(player, BYPASSPERMISSION);
    }

    /**
     * Get an online player from a player {@link UUID} in a given world. If the
     * player with the given UUID is not online, a fake-online player is created.
     *
     * @param playerUUID The {@link UUID} of the player to get.
     * @param world      The {@link World} the player is in.
     * @return An online {@link Player}. Either fake or real.
     * @see FakePlayerCreator
     */
    private Player getPlayer(UUID playerUUID, World world)
    {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null)
            player = fakePlayerCreator.getFakePlayer(Bukkit.getOfflinePlayer(playerUUID), world);
        return player;
    }

    /**
     * Check if a player can break a block at a given location.
     *
     * @param playerUUID The {@link UUID} of the player to check for.
     * @param loc        The {@link Location} to check.
     * @return The name of the {@link IProtectionCompat} that objects, if any, or
     *         null if allowed by all compats.
     */
    public String canBreakBlock(UUID playerUUID, Location loc)
    {
        if (protectionCompats.size() == 0)
            return null;

        Player fakePlayer = getPlayer(playerUUID, loc.getWorld());
        if (canByPass(fakePlayer))
            return null;

        for (IProtectionCompat compat : protectionCompats)
            try
            {
                if (!compat.canBreakBlock(fakePlayer, loc))
                    return compat.getName();
            }
            catch (Exception e)
            {
                plugin.getMyLogger().warn("Failed to use \"" + compat.getPlugin().getName() + "\"! Please send this error to pim16aap2:");
                e.printStackTrace();
                plugin.getMyLogger().logMessageToLogFile(compat.getPlugin().getName() + "\n" + Util.exceptionToString(e));
            }
        return null;
    }

    /**
     * Check if a player can break all blocks between two locations.
     *
     * @param playerUUID The {@link UUID} of the player to check for.
     * @param loc1       The start {@link Location} to check.
     * @param loc1       The end {@link Location} to check.
     * @return The name of the {@link IProtectionCompat} that objects, if any, or
     *         null if allowed by all compats.
     */
    public String canBreakBlocksBetweenLocs(UUID playerUUID, Location loc1, Location loc2)
    {
        if (protectionCompats.size() == 0)
            return null;

        Player fakePlayer = getPlayer(playerUUID, loc1.getWorld());
        if (canByPass(fakePlayer))
            return null;

        for (IProtectionCompat compat : protectionCompats)
            try
            {
                if (!compat.canBreakBlocksBetweenLocs(fakePlayer, loc1, loc2))
                    return compat.getName();
            }
            catch (Exception e)
            {
                plugin.getMyLogger().warn("Failed to use \"" + compat.getPlugin().getName() + "\"! Please send this error to pim16aap2:");
                e.printStackTrace();
                plugin.getMyLogger().logMessageToLogFile(Util.exceptionToString(e));
            }
        return null;
    }

    /**
     * Check if an {@link IProtectionCompat} is already loaded.
     *
     * @param compatClass The class of the {@link IProtectionCompat} to check.
     * @return True if the compat has already been loaded.
     */
    private boolean protectionAlreadyLoaded(Class<? extends IProtectionCompat> compatClass)
    {
        for (IProtectionCompat compat : protectionCompats)
            if (compat.getClass().equals(compatClass))
                return true;
        return false;
    }

    /**
     * Add a {@link IProtectionCompat} to the list of loaded compats if it loaded
     * successfully.
     *
     * @param hook The compat to add.
     */
    private void addProtectionCompat(IProtectionCompat hook)
    {
        if (hook.success())
        {
            protectionCompats.add(hook);
            plugin.getMyLogger().info("Successfully hooked into \"" + hook.getPlugin().getName() + "\"!");
        }
        else
            plugin.getMyLogger().info("Failed to hook into \"" + hook.getPlugin().getName() + "\"!");
    }

    /**
     * Load a compat for the plugin enabled in the event if needed.
     *
     * @param event The event of the plugin that is loaded.
     */
    @EventHandler
    protected void onPluginEnable(final PluginEnableEvent event)
    {
        loadFromPluginName(event.getPlugin().getName());
    }

    /**
     * Load a compat for a plugin with a given name if allowed and possible.
     *
     * @param compatName The name of the plugin to load a compat for.
     */
    private void loadFromPluginName(final String compatName)
    {
        ProtectionCompat compat = ProtectionCompat.getFromName(compatName);
        if (compat == null)
            return;

        if (!compat.isEnabled().apply(plugin.getConfigLoader()))
            return;

        try
        {
            Class<? extends IProtectionCompat> compatClass = compat.getClass(plugin.getServer().getPluginManager()
                .getPlugin(ProtectionCompat.getName(compat)).getDescription().getVersion());

            // No need to load compats twice.
            if (protectionAlreadyLoaded(compatClass))
                return;

            addProtectionCompat(compatClass.getConstructor(BigDoors.class).newInstance(plugin));
        }
        catch (NoClassDefFoundError e)
        {
            plugin.getMyLogger().logMessageToConsole("NoClassDefFoundError: "
                    + "Failed to initialize \"" + compatName + "\" compatibility hook!");
            plugin.getMyLogger().logMessageToConsole(
                    "Now resuming normal startup with \"" + compatName + "\" Compatibility Hook disabled!");
        }
        catch (NullPointerException e)
        {
            plugin.getMyLogger().logMessageToConsoleOnly("Could not find \"" + compatName + "\"! Hook not enabled!");
        }
        catch (Exception e)
        {
            plugin.getMyLogger().logMessageToConsole(
                    "Failed to initialize \"" + compatName + "\" compatibility hook!");
            plugin.getMyLogger().logMessageToConsole(
                    "Now resuming normal startup with \"" + compatName + "\" Compatibility Hook disabled!");
            plugin.getMyLogger().logMessageToLogFile(Util.exceptionToString(e));
        }
    }
}
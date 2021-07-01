package nl.pim16aap2.bigdoors.spigot.managers;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IEconomyManager;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.api.IPermissionsManager;
import nl.pim16aap2.bigdoors.api.restartable.IRestartable;
import nl.pim16aap2.bigdoors.doortypes.DoorType;
import nl.pim16aap2.bigdoors.api.doortypes.IDoorType;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import nl.pim16aap2.bigdoors.spigot.util.SpigotAdapter;
import nl.pim16aap2.bigdoors.api.util.Util;
import nl.pim16aap2.bigdoors.api.util.messages.Message;
import nl.pim16aap2.jcalculator.JCalculator;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Manages all interactions with Vault.
 *
 * @author Pim
 */
public final class VaultManager implements IRestartable, IEconomyManager, IPermissionsManager
{
    private static final @NotNull VaultManager INSTANCE = new VaultManager();
    private final @NotNull Map<IDoorType, Double> flatPrices;
    private boolean economyEnabled = false;
    private boolean permissionsEnabled = false;
    private @Nullable Economy economy = null;
    private @Nullable Permission perms = null;
    @SuppressWarnings("NullAway.Init") // This class needs to be rewritten to avoid this.
    private BigDoorsSpigot bigDoorsSpigot;

    private VaultManager()
    {
        flatPrices = new HashMap<>();
        if (isVaultInstalled())
        {
            economyEnabled = setupEconomy();
            permissionsEnabled = setupPermissions();
        }
    }

    /**
     * Initializes this object.
     *
     * @param bigDoorsSpigot The {@link BigDoorsSpigot} instance.
     * @return The {@link VaultManager} instance.
     */
    public static @NotNull VaultManager init(final @NotNull BigDoorsSpigot bigDoorsSpigot)
    {
        if (!bigDoorsSpigot.isRestartableRegistered(INSTANCE))
            bigDoorsSpigot.registerRestartable(INSTANCE);
        INSTANCE.bigDoorsSpigot = bigDoorsSpigot;
        INSTANCE.init();
        return INSTANCE;
    }

    @Override
    public boolean buyDoor(final @NotNull IPPlayer player, final @NotNull IPWorld world, final @NotNull IDoorType type,
                           final int blockCount)
    {
        if (!economyEnabled)
            return true;

        final @Nullable Player spigotPlayer = SpigotAdapter.getBukkitPlayer(player);
        if (spigotPlayer == null)
        {
            BigDoors.get().getPLogger().logThrowable(
                new NullPointerException("Failed to obtain Spigot player: " + player.getUUID().toString()));
            return false;
        }

        final @NotNull OptionalDouble priceOpt = getPrice(type, blockCount);
        if (priceOpt.isEmpty())
            return true;

        final double price = priceOpt.getAsDouble();
        if (withdrawPlayer(spigotPlayer, world.getWorldName(), price))
        {
            player.sendMessage(bigDoorsSpigot.getMessages().getString(Message.CREATOR_GENERAL_MONEYWITHDRAWN,
                                                                      Double.toString(price)));
            return true;
        }

        player.sendMessage(bigDoorsSpigot.getMessages().getString(Message.CREATOR_GENERAL_INSUFFICIENTFUNDS,
                                                                  Double.toString(price)));
        return false;
    }

    @Override
    public boolean isEconomyEnabled()
    {
        return economyEnabled;
    }

    /**
     * Tries to get a flat price from the config for a {@link DoorType}. Useful in case the price is set to zero, so the
     * plugin won't have to parse the formula every time if it is disabled.
     *
     * @param type The {@link DoorType}.
     */
    private void getFlatPrice(final @NotNull DoorType type)
    {
        Util.parseDouble(bigDoorsSpigot.getConfigLoader().getPrice(type))
            .ifPresent(price -> flatPrices.put(type, price));
    }

    /**
     * Initializes the {@link VaultManager}.
     */
    private void init()
    {
        for (DoorType type : BigDoors.get().getDoorTypeManager().getEnabledDoorTypes())
            getFlatPrice(type);
    }

    /**
     * Checks if a player has a specific permission node.
     *
     * @param player     The player.
     * @param permission The permission node.
     * @return True if the player has the node.
     */
    public boolean hasPermission(final @NotNull Player player, final @NotNull String permission)
    {
        return permissionsEnabled && perms != null && perms.playerHas(player.getWorld().getName(), player, permission);
    }

    /**
     * Evaluates the price formula given a specific blockCount using {@link JCalculator}
     *
     * @param formula    The formula of the price.
     * @param blockCount The number of blocks in the door.
     * @return The price of the door given the formula and the blockCount variabel.
     */
    private double evaluateFormula(final @NotNull String formula, final int blockCount)
    {
        try
        {
            return JCalculator.getResult(formula, new String[]{"blockCount"}, new double[]{blockCount});
        }
        catch (Exception e)
        {
            bigDoorsSpigot.getPLogger()
                          .logThrowable(e, "Failed to determine door creation price! Please contact pim16aap2! "
                              + "Include this: \"" + formula + "\" and stacktrace:");
            return 0.0d;
        }
    }

    @Override
    public @NotNull OptionalDouble getPrice(final @NotNull IDoorType type, final int blockCount)
    {
        if (!economyEnabled)
            return OptionalDouble.empty();

        // TODO: Store flat prices as OptionalDoubles.
        final double price = flatPrices
            .getOrDefault(type, evaluateFormula(bigDoorsSpigot.getConfigLoader().getPrice(type), blockCount));

        return price <= 0 ? OptionalDouble.empty() : OptionalDouble.of(price);
    }

    /**
     * Checks if the player has a certain amount of money in their bank account.
     *
     * @param player The player whose bank account to check.
     * @param amount The amount of money.
     * @return True if the player has at least this much money.
     */
    private boolean has(final @NotNull OfflinePlayer player, final double amount)
    {
        final boolean defaultValue = true;
        if (economy == null)
        {
            BigDoors.get().getPLogger().warn(
                "Economy not enabled! Could not subtract " + amount + " from the balance of player: " + player);
            return defaultValue;
        }

        try
        {
            return economy.has(player, amount);
        }
        catch (Exception e)
        {
            bigDoorsSpigot.getPLogger().logThrowable(e, "Failed to check balance of player \"" + player.getName() +
                "\" (" + player.getUniqueId() + ")! Please contact pim16aap2!");
        }
        return defaultValue;
    }

    /**
     * Withdraw a certain amount of money from a player's bank account in a certain world.
     *
     * @param player    The player.
     * @param worldName The name of the world.
     * @param amount    The amount of money.
     * @return True if the money was successfully withdrawn from the player's accounts.
     */
    private boolean withdrawPlayer(final @NotNull OfflinePlayer player, final @NotNull String worldName,
                                   final double amount)
    {
        final boolean defaultValue = true;
        if (economy == null)
        {
            BigDoors.get().getPLogger().warn("Economy not enabled! Could not subtract " + amount +
                                                 " from the balance of player: " + player + " in world: " + worldName);
            return defaultValue;
        }

        try
        {
            if (has(player, amount))
                return economy.withdrawPlayer(player, worldName, amount).type
                    .equals(EconomyResponse.ResponseType.SUCCESS);
            return false;
        }
        catch (Exception e)
        {
            bigDoorsSpigot.getPLogger().logThrowable(e, "Failed to subtract money from player \"" + player.getName() +
                "\" (" + player.getUniqueId() + ")! Please contact pim16aap2!");
        }
        return defaultValue;
    }

    /**
     * Withdraw a certain amount of money from a player's bank account in a certain world.
     *
     * @param player    The player.
     * @param worldName The name of the world.
     * @param amount    The amount of money.
     * @return True if the money was successfully withdrawn from the player's accounts.
     */
    private boolean withdrawPlayer(final @NotNull Player player, final @NotNull String worldName, final double amount)
    {
        return withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), worldName, amount);
    }

    /**
     * Checks if Vault is installed on this server.
     *
     * @return True if vault is installed on this server.
     */
    private boolean isVaultInstalled()
    {
        try
        {
            return bigDoorsSpigot.getPlugin().getServer().getPluginManager().getPlugin("Vault") != null;
        }
        catch (NullPointerException e)
        {
            return false;
        }
    }

    /**
     * Initialize the economy dependency. Assumes Vault is installed on this server. See {@link #isVaultInstalled()}.
     *
     * @return True if the initialization process was successful.
     */
    private boolean setupEconomy()
    {
        try
        {
            final RegisteredServiceProvider<Economy> economyProvider = bigDoorsSpigot.getPlugin().getServer()
                                                                                     .getServicesManager()
                                                                                     .getRegistration(Economy.class);
            if (economyProvider == null)
                return false;

            economy = economyProvider.getProvider();
            return true;
        }
        catch (Exception e)
        {
            BigDoors.get().getPLogger().logThrowable(e);
            return false;
        }
    }

    /**
     * Initialize the permissions dependency. Assumes Vault is installed on this server. See {@link
     * #isVaultInstalled()}.
     *
     * @return True if the initialization process was successful.
     */
    private boolean setupPermissions()
    {
        try
        {
            final RegisteredServiceProvider<Permission> permissionProvider =
                bigDoorsSpigot.getPlugin().getServer().getServicesManager().getRegistration(Permission.class);

            if (permissionProvider == null)
                return false;

            perms = permissionProvider.getProvider();
            return true;
        }
        catch (Exception e)
        {
            BigDoors.get().getPLogger().logThrowable(e);
            return false;
        }
    }

    @Override
    public void restart()
    {
        shutdown();
        init();
    }

    @Override
    public void shutdown()
    {
        flatPrices.clear();
    }

    @Override
    public @NotNull OptionalInt getMaxPermissionSuffix(final @NotNull IPPlayer player,
                                                       final @NotNull String permissionBase)
    {
        final @Nullable Player bukkitPlayer = SpigotAdapter.getBukkitPlayer(player);
        if (bukkitPlayer == null)
        {
            BigDoors.get().getPLogger().logThrowable(
                new IllegalArgumentException("Failed to obtain BukkitPlayer for player: " + player.asString()));
            return OptionalInt.empty();
        }

        final int permissionBaseLength = permissionBase.length();
        final @NotNull Set<PermissionAttachmentInfo> playerPermissions = bukkitPlayer.getEffectivePermissions();
        int ret = -1;
        for (final PermissionAttachmentInfo permission : playerPermissions)
            if (permission.getPermission().startsWith(permissionBase))
            {
                final OptionalInt suffix = Util.parseInt(permission.getPermission().substring(permissionBaseLength));
                if (suffix.isPresent())
                    ret = Math.max(ret, suffix.getAsInt());
            }
        return ret > 0 ? OptionalInt.of(ret) : OptionalInt.empty();
    }

    @Override
    public boolean hasPermission(final @NotNull IPPlayer player, final @NotNull String permissionNode)
    {
        final @Nullable Player bukkitPlayer = SpigotAdapter.getBukkitPlayer(player);
        if (bukkitPlayer == null)
        {
            BigDoors.get().getPLogger().logThrowable(
                new IllegalArgumentException("Failed to obtain BukkitPlayer for player: " + player.asString()));
            return false;
        }

        return bukkitPlayer.hasPermission(permissionNode);
    }

    @Override
    public boolean isOp(final @NotNull IPPlayer player)
    {
        final @Nullable Player bukkitPlayer = SpigotAdapter.getBukkitPlayer(player);
        if (bukkitPlayer == null)
        {
            BigDoors.get().getPLogger().logThrowable(
                new IllegalArgumentException("Failed to obtain BukkitPlayer for player: " + player.asString()));
            return false;
        }
        return bukkitPlayer.isOp();
    }
}

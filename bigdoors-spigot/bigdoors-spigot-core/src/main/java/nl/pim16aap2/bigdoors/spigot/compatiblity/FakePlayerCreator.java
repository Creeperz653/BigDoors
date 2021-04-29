package nl.pim16aap2.bigdoors.spigot.compatiblity;

import com.mojang.authlib.GameProfile;
import lombok.NonNull;
import nl.pim16aap2.bigdoors.spigot.BigDoorsSpigot;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Class used to create a fake-online player who is actually offline.
 *
 * @author Pim
 */
class FakePlayerCreator
{
    static final String FAKEPLAYERMETADATA = "isBigDoorsFakePlayer";

    private final String NMSbase;
    private final String CraftBase;
    private final BigDoorsSpigot bigDoorsSpigot;
    private Class<?> CraftOfflinePlayer;
    private Class<?> CraftWorld;
    private Class<?> World;
    private Class<?> WorldServer;
    private Class<?> EntityPlayer;
    private Class<?> MinecraftServer;
    private Class<?> PlayerInteractManager;
    private Method getProfile;
    private Method getHandle;
    private Method getServer;
    private Method getBukkitEntity;
    private Constructor<?> EntityPlayerConstructor;
    private Constructor<?> PlayerInteractManagerConstructor;
    private Field uuid;
    private boolean success = false;

    FakePlayerCreator(final @NonNull BigDoorsSpigot bigDoorsSpigot)
    {
        this.bigDoorsSpigot = bigDoorsSpigot;

        NMSbase = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + ".";
        CraftBase = "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]
            + ".";
        try
        {
            CraftOfflinePlayer = getCraftClass("CraftOfflinePlayer");
            CraftWorld = getCraftClass("CraftWorld");
            WorldServer = getNMSClass("WorldServer");
            EntityPlayer = getNMSClass("EntityPlayer");
            MinecraftServer = getNMSClass("MinecraftServer");
            PlayerInteractManager = getNMSClass("PlayerInteractManager");
            EntityPlayerConstructor = EntityPlayer.getConstructor(MinecraftServer, WorldServer, GameProfile.class,
                                                                  PlayerInteractManager);
            getBukkitEntity = EntityPlayer.getMethod("getBukkitEntity");
            getHandle = CraftWorld.getMethod("getHandle");
            getProfile = CraftOfflinePlayer.getMethod("getProfile");
            getServer = MinecraftServer.getMethod("getServer");
            uuid = getNMSClass("Entity").getDeclaredField("uniqueID");
            uuid.setAccessible(true);

            World = getNMSClass("World");
            try
            {
                PlayerInteractManagerConstructor = PlayerInteractManager.getConstructor(WorldServer);
            }
            catch (Exception e)
            {
                PlayerInteractManagerConstructor = PlayerInteractManager.getConstructor(World);
            }
        }
        catch (ClassNotFoundException | NoSuchMethodException | SecurityException | NoSuchFieldException e)
        {
            bigDoorsSpigot.getPLogger().logThrowable(e);
            return;
        }
        catch (LinkageError e)
        {
            bigDoorsSpigot.getPLogger().logThrowable(e);
            return;
        }
        success = true;
    }

    private @NonNull Class<?> getNMSClass(final @NonNull String name)
        throws LinkageError, ClassNotFoundException
    {
        return Class.forName(NMSbase + name);
    }

    private @NonNull Class<?> getCraftClass(final @NonNull String name)
        throws LinkageError, ClassNotFoundException
    {
        return Class.forName(CraftBase + name);
    }

    /**
     * Construct a fake-online {@link Player} from an {@link OfflinePlayer}.
     *
     * @param oPlayer The {@link OfflinePlayer} to use as base for the fake online {@link Player}.
     * @param world   The world the fake {@link Player} is supposedly in.
     * @return The fake-online {@link Player}
     */
    @NonNull Optional<Player> getFakePlayer(final @NonNull OfflinePlayer oPlayer, final @NonNull World world)
    {
        if (!success)
            return Optional.empty();

        Player player = null;

        try
        {
            Object coPlayer = CraftOfflinePlayer.cast(oPlayer);
            GameProfile gProfile = (GameProfile) getProfile.invoke(coPlayer);

            Object craftServer = CraftWorld.cast(world);
            Object worldServer = getHandle.invoke(craftServer);
            Object minecraftServer = getServer.invoke(worldServer);
            Object playerInteractManager = PlayerInteractManagerConstructor.newInstance(worldServer);

            Object ePlayer = EntityPlayerConstructor.newInstance(minecraftServer, worldServer, gProfile,
                                                                 playerInteractManager);
            uuid.set(ePlayer, oPlayer.getUniqueId());
            player = (Player) getBukkitEntity.invoke(ePlayer);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e)
        {
            bigDoorsSpigot.getPLogger().logThrowable(e);
        }

        if (player != null)
            player.setMetadata(FAKEPLAYERMETADATA, new FixedMetadataValue(bigDoorsSpigot.getPlugin(), true));

        return Optional.ofNullable(player);
    }
}

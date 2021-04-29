package nl.pim16aap2.bigdoors.spigot.util.api;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IBigDoorsPlatform;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents the implementation of {@link IBigDoorsPlatform} for the Spigot platform.
 *
 * @author Pim
 */
public abstract class BigDoorsSpigotAbstract implements Listener, IBigDoorsPlatform
{
    public abstract @NonNull IPlatformManagerSpigot getPlatformManagerSpigot();

    public abstract @NonNull JavaPlugin getJavaPlugin();
}

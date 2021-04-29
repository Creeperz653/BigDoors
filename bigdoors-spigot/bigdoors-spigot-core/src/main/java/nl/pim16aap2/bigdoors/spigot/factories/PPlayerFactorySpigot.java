package nl.pim16aap2.bigdoors.spigot.factories;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.BigDoors;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.PPlayerData;
import nl.pim16aap2.bigdoors.api.factories.IPPlayerFactory;
import nl.pim16aap2.bigdoors.spigot.util.implementations.OfflinePPlayerSpigot;
import nl.pim16aap2.bigdoors.spigot.util.implementations.PPlayerSpigot;
import nl.pim16aap2.bigdoors.util.InnerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an implementation of {@link IPPlayerFactory} for the Spigot platform.
 *
 * @author Pim
 */
public class PPlayerFactorySpigot implements IPPlayerFactory
{
    @Override
    public @NonNull IPPlayer create(final @NonNull PPlayerData playerData)
    {
        Player player = Bukkit.getPlayer(playerData.getUUID());
        if (player != null)
            return new PPlayerSpigot(player);
        return new OfflinePPlayerSpigot(playerData);
    }

    @Override
    public @NonNull CompletableFuture<Optional<IPPlayer>> create(final @NonNull UUID uuid)
    {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null)
            return CompletableFuture.completedFuture(Optional.of(new PPlayerSpigot(player)));

        return BigDoors.get().getDatabaseManager().getPlayerData(uuid)
                       .thenApply(playerData -> playerData.<IPPlayer>map(OfflinePPlayerSpigot::new))
                       .exceptionally(InnerUtil::exceptionallyOptional);
    }
}

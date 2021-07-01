package nl.pim16aap2.bigdoors.spigot.util.api;

import nl.pim16aap2.bigdoors.api.IGlowingBlock;
import nl.pim16aap2.bigdoors.api.restartable.IRestartableHolder;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IGlowingBlockFactory
{
    /**
     * Spawns a glowing block.
     *
     * @param player            The player who will see the glowing block.
     * @param world             The world in which the glowing block will be spawned.
     * @param restartableHolder The {@link IRestartableHolder} where the resulting {@link IGlowingBlock} will be
     *                          (de)registered when it is (de)spawned.
     * @return The {@link IGlowingBlock} that was spawned.
     */
    @NotNull Optional<IGlowingBlock> createGlowingBlock(@NotNull Player player, @NotNull World world,
                                                        @NotNull IRestartableHolder restartableHolder);
}

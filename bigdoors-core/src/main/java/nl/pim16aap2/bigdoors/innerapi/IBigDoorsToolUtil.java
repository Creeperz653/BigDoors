package nl.pim16aap2.bigdoors.innerapi;

import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.tooluser.ToolUser;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a set of utility functions around the tool used by {@link ToolUser}s.
 *
 * @author Pim
 */
public interface IBigDoorsToolUtil
{
    /**
     * Gives a player a BigDoors.
     *
     * @param player The player to give the tool to.
     * @param name   The itemname of the tool.
     * @param lore   The lore of the tool.
     */
    void giveToPlayer(@NotNull IPPlayer player, @NotNull String name, @NotNull String lore);

    /**
     * Removes any BigDoors tools from a player's inventory.
     *
     * @param player The player whose inventory will be checked.
     */
    void removeTool(@NotNull IPPlayer player);

    /**
     * Checks if a player is currently holding a BigDoors tool in their main hand.
     *
     * @param player The player to check.
     * @return True if they are currently holding a BigDoors tool in their main hand.
     */
    boolean isPlayerHoldingTool(@NotNull IPPlayer player);
}

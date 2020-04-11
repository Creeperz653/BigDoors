package nl.pim16aap2.bigdoors.api;

import org.jetbrains.annotations.NotNull;

/**
 * Analyzes blocks to obtain various information.
 *
 * @author Pim
 */
public interface IBlockAnalyzer
{
    /**
     * Checks if placement of this block should be deferred to the second pass or not.
     * <p>
     * See {@link PBlockData#deferPlacement()}
     * <p>
     * This method assume
     *
     * @param location The location of the block.
     * @return True if this block should be placed on the second pass, false otherwise.
     */
    boolean placeOnSecondPass(final @NotNull IPLocation location);

    /**
     * Check if a block if air or liquid (water, lava).
     *
     * @param location The location of the block.
     * @return True if it is air or liquid.
     */
    boolean isAirOrLiquid(final @NotNull IPLocation location);

    /**
     * Check if a block is on the blacklist of types/materials that is not allowed for animations.
     *
     * @param location The location of the block.
     * @return True if the block can be used for animations.
     */
    boolean isAllowedBlock(final @NotNull IPLocation location);


    /**
     * Represents the status of a material.
     *
     * @author Pim
     */
    enum MaterialStatus
    {
        /**
         * The material is blacklisted and cannot in any way be used as an animated block.
         */
        BLACKLISTED,

        /**
         * The material represents a secondary block. It cannot exist on its own. It must be broken before the block it
         * depends on and placed after.
         * <p>
         * For example: Torches.
         */
        GREYLISTED,

        /**
         * This material is allowed.
         */
        WHITELISTED,

        /**
         * The material type was not mapped.
         */
        UNMAPPED
    }
}








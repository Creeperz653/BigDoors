package nl.pim16aap2.bigdoors.doors;

import lombok.NonNull;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.IPWorld;
import nl.pim16aap2.bigdoors.util.Cuboid;
import nl.pim16aap2.bigdoors.util.CuboidConst;
import nl.pim16aap2.bigdoors.util.DoorOwner;
import nl.pim16aap2.bigdoors.util.RotateDirection;
import nl.pim16aap2.bigdoors.util.vector.Vector2Di;
import nl.pim16aap2.bigdoors.util.vector.Vector2DiConst;
import nl.pim16aap2.bigdoors.util.vector.Vector3DiConst;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a door.
 *
 * @author Pim
 */
public interface IDoorBase
{
    /**
     * Checks if this door can be opened instantly (i.e. skip the animation).
     *
     * @return True, if the door can skip its animation.
     */
    boolean canSkipAnimation();

    /**
     * Checks if the power block of a door is powered.
     *
     * @return True if the power block is receiving a redstone signal.
     */
    boolean isPowerBlockActive();

    /**
     * Checks if this door can be opened right now.
     *
     * @return True if this door can be opened right now.
     */
    boolean isOpenable();

    /**
     * Changes the open-status of this door. True if open, False if closed.
     *
     * @param bool The new open-status of the door.
     */
    @NonNull IDoorBase setOpen(boolean bool);

    /**
     * Changes the lock status of this door. Locked doors cannot be opened.
     *
     * @param locked New lock status.
     */
    @NonNull IDoorBase setLocked(boolean locked);

    /**
     * Checks if this door can be closed right now.
     *
     * @return True if this door can be closed right now.
     */
    boolean isCloseable();

    /**
     * Handles a change in redstone current for this door's powerblock.
     *
     * @param newCurrent The new current of the powerblock.
     */
    void onRedstoneChange(int newCurrent);

    /**
     * Gets the direction the door would go given its current state..
     *
     * @return The direction the door would go if it were to be toggled.
     */
    @NonNull RotateDirection getCurrentToggleDir();

    /**
     * Gets the {@link CuboidConst} representing the area taken up by this door.
     *
     * @return The {@link CuboidConst} representing the area taken up by this door.
     */
    @NonNull CuboidConst getCuboid();

    /**
     * Finds the new minimum and maximum coordinates (represented by a {@link Cuboid}) of this door that would be the
     * result of toggling it.
     *
     * @return The {@link Cuboid} that would represent the door if it was toggled right now.
     */
    @NonNull Optional<Cuboid> getPotentialNewCoordinates();

    /**
     * Cycle the {@link RotateDirection} direction this {@link IDoorBase} will open in. By default it'll set and return
     * the opposite direction of the current direction.
     * <p>
     * Note that this does not actually change the open direction; it merely tells you which direction comes next!
     *
     * @return The new {@link RotateDirection} direction this {@link IDoorBase} will open in.
     */
    @NonNull RotateDirection cycleOpenDirection();

    /**
     * Calculates the Min and Max coordinates of the range of Vector2Dis that this {@link IDoorBase} might interact
     * with.
     *
     * @return 2 {@link Vector2Di}. Min and Max coordinates of Vector2Dis in animation range.
     */
    @Deprecated
    @NonNull Vector2Di[] calculateChunkRange();

    /**
     * Calculates the Min and Max coordinates of the range of Vector2Dis that this {@link IDoorBase} might currently
     * exists in.
     *
     * @return 2 {@link Vector2Di}. Min and Max coordinates of Vector2Dis in current range.
     */
    @Deprecated
    @NonNull Vector2Di[] calculateCurrentChunkRange();

    /**
     * Check if a provided {@link Vector2DiConst} is in range of the door. Range in this case refers to all Vector2Dis
     * this {@link IDoorBase} could potentially occupy using animated blocks.
     *
     * @param chunk The chunk to check
     * @return True if the {@link Vector2DiConst} is in range of the door.
     */
    @Deprecated
    boolean chunkInRange(@NonNull IPWorld otherWorld, @NonNull Vector2DiConst chunk);

    /**
     * Gets the name of this door.
     *
     * @return The name of this door.
     */
    @NonNull String getName();

    /**
     * Changes the name of the door.
     *
     * @param name The new name of this door.
     */
    @NonNull IDoorBase setName(@NonNull String name);

    /**
     * Gets the IPWorld this {@link IDoorBase} exists in.
     *
     * @return The IPWorld this {@link IDoorBase} exists in
     */
    @NonNull IPWorld getWorld();

    /**
     * Gets the UID of the {@link IDoorBase} as used in the database. Guaranteed to be unique and available.
     *
     * @return The UID of the {@link IDoorBase} as used in the database.
     */
    long getDoorUID();

    /**
     * Check if the {@link IDoorBase} is currently locked. When locked, doors cannot be opened.
     *
     * @return True if the {@link IDoorBase} is locked
     */
    boolean isLocked();

    /**
     * Check if the {@link IDoorBase} is currently open.
     *
     * @return True if the {@link IDoorBase} is open
     */
    boolean isOpen();

    /**
     * Gets the prime owner (permission = 0) of this door. In most cases, this will be the original creator of the
     * door.
     *
     * @return The prime owner of this door.
     */
    @NonNull DoorOwner getPrimeOwner();

    /**
     * Gets all {@link DoorOwner}s of this door, including the original creator.
     * <p>
     * Note that this collection is returned as an {@link Collections#unmodifiableCollection(Collection)}.
     *
     * @return All {@link DoorOwner}s of this door, including the original creator.
     */
    @NonNull Collection<@NonNull DoorOwner> getDoorOwners();

    /**
     * Attempts to get the {@link DoorOwner} of this door represented by an {@link IPPlayer}.
     *
     * @param player The player that may or may not be an owner of this door.
     * @return The {@link DoorOwner} of this door for the given player, if this player is a {@link DoorOwner} of this
     * door.
     */
    @NonNull Optional<DoorOwner> getDoorOwner(@NonNull IPPlayer player);

    /**
     * Attempts to get the {@link DoorOwner} of this door represented by the UUID of a player.
     *
     * @param player The UUID of the player that may or may not be an owner of this door.
     * @return The {@link DoorOwner} of this door for the given player, if this player is a {@link DoorOwner} of this
     * door.
     */
    @NonNull Optional<DoorOwner> getDoorOwner(@NonNull UUID player);

    /**
     * Gets the {@link RotateDirection} this {@link IDoorBase} will open if currently closed.
     * <p>
     * Note that if it's currently in the open status, it is supposed go in the opposite direction, as the closing
     * direction is the opposite of the opening direction. This isn't taken into account by this method.
     * <p>
     * If you want to get the direction the door would go in if it were toggled given its current state, use {@link
     * #getCurrentToggleDir()} instead.
     *
     * @return The {@link RotateDirection} this {@link IDoorBase} will open in.
     */
    @NonNull RotateDirection getOpenDir();

    /**
     * Sets the {@link RotateDirection} this {@link IDoorBase} will open if currently closed.
     * <p>
     * Note that if it's currently in the open status, it is supposed go in the opposite direction, as the closing
     * direction is the opposite of the opening direction.
     *
     * @param rotateDirection The {@link RotateDirection} this {@link IDoorBase} will open in.
     * @return This {@link IDoorBase}.
     */
    @NonNull IDoorBase setOpenDir(@NonNull RotateDirection rotateDirection);

    /**
     * Gets the position of power block of this door.
     *
     * @return The position of the power block of this door.
     */
    @NonNull Vector3DiConst getPowerBlock();

    /**
     * Updates the position of the powerblock.
     *
     * @param pos The new position.
     * @return This {@link IDoorBase}.
     */
    @NonNull IDoorBase setPowerBlockPosition(@NonNull Vector3DiConst pos);

    /**
     * Gets the position of the engine of this door.
     *
     * @return The position of the engine block of this door.
     */
    @NonNull Vector3DiConst getEngine();

    /**
     * Updates the position of the engine.
     *
     * @param pos The new position.
     * @return This {@link IDoorBase}.
     */
    @NonNull IDoorBase setEngine(@NonNull Vector3DiConst pos);

    /**
     * Gets the minimum position of this door.
     *
     * @return The minimum coordinates of this door.
     */
    @NonNull Vector3DiConst getMinimum();

    /**
     * Changes the position of this {@link IDoorBase}. The min/max order of the positions doesn't matter.
     *
     * @param posA The first new position.
     * @return This {@link IDoorBase}.
     */
    @NonNull IDoorBase setCoordinates(@NonNull Vector3DiConst posA, @NonNull Vector3DiConst posB);

    /**
     * Changes the position of this {@link IDoorBase}. The min/max order of the positions doesn't matter.
     *
     * @param newCuboid The {@link CuboidConst} representing the area the door will take up from now on.
     * @return This {@link IDoorBase}.
     */
    @NonNull IDoorBase setCoordinates(@NonNull CuboidConst newCuboid);

    /**
     * Gets a copy of the maximum position of this door.
     *
     * @return A copy of the maximum position of this door.
     */
    @NonNull Vector3DiConst getMaximum();

    /**
     * Gets the the Vector2Di coordinates of the min and max Vector2Dis that are in range of this door.
     * <p>
     * [0] contains the lower bound chunk coordinates, [1] contains the upper bound chunk coordinates.
     *
     * @return The Vector2Di coordinates of the min and max Vector2Dis in range of this door.
     */
    @Deprecated
    @NonNull Vector2Di[] getChunkRange();

    /**
     * Retrieve the Vector2Di the power block of this {@link IDoorBase} resides in. If invalidated or not calculated
     * yet, it is (re)calculated first.
     * <p>
     * It's calculated once and then stored until invalidated.
     *
     * @return The Vector2Di the power block of this {@link IDoorBase} resides in.
     */
    @NonNull Vector2DiConst getEngineChunk();

    /**
     * Retrieve the total number of blocks this {@link IDoorBase} is made out of. If invalidated or not calculated *
     * yet, it is (re)calculated first.
     * <p>
     * It's calculated once and then stored until invalidated.
     *
     * @return Total number of blocks this {@link IDoorBase} is made out of.
     */
    int getBlockCount();

    /**
     * Gets the dimensions of this door.
     * <p>
     * If a door has a min and max X value of 120, for example, it would have a X-dimension of 0. If the min X value is
     * 119 instead, it would have an X-dimension of 1.
     *
     * @return The dimensions of this door.
     */
    @NonNull Vector3DiConst getDimensions();

    /**
     * @return The simple hash of the chunk in which the power block resides.
     */
    long getSimplePowerBlockChunkHash();

    /**
     * Gets basic information of this door: uid, permission, and name.
     *
     * @return Basic {@link IDoorBase} info
     */
    @NonNull String getBasicInfo();

    /**
     * @return String with (almost) all data of this door.
     */
    @Override
    @NonNull String toString();

    @Override
    boolean equals(Object o);
}

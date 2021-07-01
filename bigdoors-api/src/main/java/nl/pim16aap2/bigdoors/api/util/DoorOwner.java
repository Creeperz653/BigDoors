package nl.pim16aap2.bigdoors.api.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.pim16aap2.bigdoors.api.IPPlayer;
import nl.pim16aap2.bigdoors.api.PPlayerData;
import org.jetbrains.annotations.NotNull;

/**
 * Contains all details needed about a doorOwner.
 *
 * @author Pim
 */
@AllArgsConstructor
public class DoorOwner
{
    /**
     * The UID of the door that is owned.
     */
    @Getter
    long doorUID;

    /**
     * The permission level at which the player owns the door.
     */
    @Getter
    int permission;

    /**
     * The {@link IPPlayer} object represented by this {@link DoorOwner}.
     */
    @Getter
    @NotNull PPlayerData pPlayerData;

    /**
     * Get a basic overview of this door owner. Useful for debugging.
     */
    @Override
    public @NotNull String toString()
    {
        return "doorUID: " + doorUID + ". player: " + getPPlayerData().toString() + ". Permission: " + permission;
    }

    @Override
    public final int hashCode()
    {
        return getPPlayerData().getUUID().hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        DoorOwner other = (DoorOwner) o;
        return getPPlayerData().equals(other.getPPlayerData()) &&
            doorUID == other.doorUID &&
            permission == other.permission;
    }

    @Override
    public @NotNull DoorOwner clone()
    {
        // TODO: Clone player as well?
        return new DoorOwner(doorUID, getPermission(), getPPlayerData());
    }
}

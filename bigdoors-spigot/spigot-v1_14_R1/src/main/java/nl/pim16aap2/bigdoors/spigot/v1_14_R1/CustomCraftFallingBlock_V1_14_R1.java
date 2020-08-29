package nl.pim16aap2.bigdoors.spigot.v1_14_R1;

import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.WorldServer;
import nl.pim16aap2.bigdoors.api.ICustomCraftFallingBlock;
import nl.pim16aap2.bigdoors.api.IPLocation;
import nl.pim16aap2.bigdoors.spigot.util.SpigotAdapter;
import nl.pim16aap2.bigdoors.util.vector.Vector3Dd;
import nl.pim16aap2.bigdoors.util.vector.Vector3DdConst;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_14_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * V1_14_R1 implementation of {@link ICustomCraftFallingBlock}.
 *
 * @author Pim
 * @see ICustomCraftFallingBlock
 */
public class CustomCraftFallingBlock_V1_14_R1 extends CraftEntity implements FallingBlock, ICustomCraftFallingBlock
{
    CustomCraftFallingBlock_V1_14_R1(final @NotNull Server server,
                                     final @NotNull CustomEntityFallingBlock_V1_14_R1 entity)
    {
        super((org.bukkit.craftbukkit.v1_14_R1.CraftServer) server, entity);
        setVelocity(new Vector(0, 0, 0));
        setDropItem(false);
        entity.noclip = true;
    }

    @Override
    public boolean teleport(@NotNull Vector3DdConst newPosition, @NotNull Vector3DdConst rotation,
                            @NotNull TeleportMode teleportMode)
    {
        super.entity.setLocation(newPosition.getX(), newPosition.getY(), newPosition.getZ(), entity.yaw, entity.pitch);
        ((WorldServer) entity.world).chunkCheck(entity);
        return true;
    }

    @Override
    public void setVelocity(final @NotNull Vector3DdConst vector)
    {
        entity.setMot(new Vec3D(vector.getX(), vector.getY(), vector.getZ()));
        entity.velocityChanged = true;
    }

    @Override
    public @NotNull IPLocation getPLocation()
    {
        return SpigotAdapter.wrapLocation(super.getLocation());
    }

    @Override
    public @NotNull Vector3Dd getPosition()
    {
        Location bukkitLocation = super.getLocation();
        return new Vector3Dd(bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ());
    }

    @Override
    public @NotNull Vector3Dd getPVelocity()
    {
        Vector bukkitVelocity = super.getVelocity();
        return new Vector3Dd(bukkitVelocity.getX(), bukkitVelocity.getY(), bukkitVelocity.getZ());
    }

    @Override
    public @NotNull CustomEntityFallingBlock_V1_14_R1 getHandle()
    {
        return (CustomEntityFallingBlock_V1_14_R1) entity;
    }

    @Override
    public boolean isOnGround()
    {
        return false;
    }

    @Override
    public @NotNull String toString()
    {
        return "CraftFallingBlock";
    }

    @Override
    public @NotNull EntityType getType()
    {
        return EntityType.FALLING_BLOCK;
    }

    @Override
    @Deprecated
    public @NotNull Material getMaterial()
    {
        return CraftMagicNumbers.getMaterial(getHandle().getBlock()).getItemType();
    }

    @Override
    public @NotNull BlockData getBlockData()
    {
        return CraftBlockData.fromData(getHandle().getBlock());
    }

    @Override
    public boolean getDropItem()
    {
        return false;
    }

    @Override
    public void setDropItem(final boolean drop)
    {
        getHandle().dropItem = false;
    }

    @Override
    public boolean canHurtEntities()
    {
        return false;
    }

    @Override
    public void setHurtEntities(final boolean hurtEntities)
    {
        getHandle().hurtEntities = false;
    }

    @Override
    public void setTicksLived(final int value)
    {
        super.setTicksLived(value);

        // Second field for EntityFallingBlock
        getHandle().ticksLived = value;
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Not currently implemented.
     */
    @Deprecated
    @Override
    public void setHeadPose(final @NotNull Vector3DdConst pose)
    {
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Not currently implemented.
     */
    @Deprecated
    @Override
    public void setBodyPose(final @NotNull Vector3DdConst eulerAngle)
    {
    }
}

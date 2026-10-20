package com.hbm.tileentity.machine;

import java.util.Iterator;
import java.util.List;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineTeleporter extends TileEntityLoadedBase implements IEnergyReceiverMK2, IFluidStandardReceiver, IBufPacketReceiver, net.minecraft.util.ITickable {

	public long power = 0;
	public int targetX = -1;
	public int targetY = -1;
	public int targetZ = -1;
	public int targetDim = 0;
	public static final int maxPower = 1_500_000;
	public static final int consumption = 1_000_000;
	public static final int flucu = 100;

	public FluidTank tank;

	public TileEntityMachineTeleporter() {
		tank = new FluidTank(Fluids.NMASS, 16000);
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {
			this.subscribeToAllAround(tank.getTankType(), this);

			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) this.trySubscribe(this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);

			if(this.targetY != -1) {
				List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.pos.getX() + 0.25, this.pos.getY(), this.pos.getZ() + 0.25, this.pos.getX() + 0.75, this.pos.getY() + 2, this.pos.getZ() + 0.75));

				if(!entities.isEmpty()) {
					for(Entity e : entities) {
						teleport(e);
					}
				}
			}

			networkPackNT(15);

		} else {

			if(this.targetY != -1 && power >= consumption && this.tank.getFill() >= flucu) {
				double x = this.pos.getX() + 0.5 + this.world.rand.nextGaussian() * 0.25D;
				double y = this.pos.getY() + 1 + this.world.rand.nextDouble() * 2D;
				double z = this.pos.getZ() + 0.5 + this.world.rand.nextGaussian() * 0.25D;
				this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.REDSTONE, x, y, z, 0.4F, 0.8F, 1F);
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(power);
		BufferUtil.writeIntArray(buf, new int[] {targetX, targetY, targetZ, targetDim});
		this.tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.power = buf.readLong();
		int[] target = BufferUtil.readIntArray(buf);
		this.targetX = target[0];
		this.targetY = target[1];
		this.targetZ = target[2];
		this.targetDim = target[3];
		this.tank.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		power = nbt.getLong("power");
		targetX = nbt.getInteger("x1");
		targetY = nbt.getInteger("y1");
		targetZ = nbt.getInteger("z1");
		targetDim = nbt.getInteger("dim");
		this.tank.readFromNBT(nbt, "tt");

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("power", power);
		nbt.setInteger("x1", targetX);
		nbt.setInteger("y1", targetY);
		nbt.setInteger("z1", targetZ);
		nbt.setInteger("dim", targetDim);
		tank.writeToNBT(nbt, "tt");

		return nbt;
	}

	public void teleport(Entity entity) {

		if(this.power < consumption) return;
		if(entity.dimension != this.targetDim && tank.getFill() < flucu) return; // N-MASS is required for cross-dimension teleporting
		this.world.playSound(null, this.pos.getX() + 0.5, this.pos.getY() + 1.5, this.pos.getZ() + 0.5, net.minecraft.init.SoundEvents.ENTITY_ENDERMEN_TELEPORT, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);

		if(entity.dimension != this.targetDim) {
			this.tank.setFill(this.tank.getFill() - flucu);
		}

		if((entity instanceof EntityPlayerMP)) {

			EntityPlayerMP player = (EntityPlayerMP) entity;
			if(entity.dimension == this.targetDim) {
				player.setPositionAndUpdate(this.targetX + 0.5D, this.targetY + 1.5D + entity.getYOffset(), this.targetZ + 0.5D);
			} else {
				teleportPlayerInterdimensionally(player, this.targetX + 0.5D, this.targetY + 1.5D + entity.getYOffset(), this.targetZ + 0.5D, this.targetDim);
			}

		} else {

			if(entity.dimension == this.targetDim) {
				entity.setPositionAndRotation(this.targetX + 0.5D, this.targetY + 1.5D + entity.getYOffset(), this.targetZ + 0.5D, entity.rotationYaw, entity.rotationPitch);

				try {
					EntityTrackerEntry entry = com.hbm.util.TrackerUtil.getTrackerEntry((WorldServer)this.world, entity.getEntityId());
					SPacketEntityTeleport packet = new SPacketEntityTeleport(entity);
					// Preserve original floor rotation and leave the tracker counter untouched.
					ReflectionHelper.setPrivateValue(SPacketEntityTeleport.class, packet, (byte)MathHelper.floor(entity.rotationYaw * 256.0F / 360.0F), "yaw", "field_149455_e");
					ReflectionHelper.setPrivateValue(SPacketEntityTeleport.class, packet, (byte)MathHelper.floor(entity.rotationPitch * 256.0F / 360.0F), "pitch", "field_149453_f");
					entry.sendPacketToTrackedPlayers(packet);
				} catch(Exception ex) { }
			} else {
				teleportEntityInterdimensionally(entity, this.targetX + 0.5D, this.targetY + 1.5D + entity.getYOffset(), this.targetZ + 0.5D, this.targetDim);
			}

			// I have done nothing but teleport bread for three days
			if(entity instanceof EntityItem) {
				EntityItem entityItem = (EntityItem) entity;
				if(entityItem.getItem().getItem() == Items.BREAD) {
					WorldServer newWorld = this.world.getMinecraftServer().getWorld(this.targetDim);
					EntityLiving slime = new EntitySlime(newWorld);
					slime.setLocationAndAngles(this.targetX + 0.5D, this.targetY + 1.5D + slime.getYOffset(), this.targetZ + 0.5D, entity.rotationYaw, entity.rotationPitch);
					slime.onInitialSpawn(newWorld.getDifficultyForLocation(slime.getPosition()), null);
					newWorld.spawnEntity(slime);
				}
			}
		}

		this.world.playSound(null, entity.posX, entity.posY, entity.posZ, net.minecraft.init.SoundEvents.ENTITY_ENDERMEN_TELEPORT, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);

		this.power -= consumption;
		this.markDirty();
	}

	/** Teleports a player to a different dimension, gracefully copied from PlayerList */
	public static boolean teleportPlayerInterdimensionally(EntityPlayerMP player, double x, double y, double z, int dim) {

		int prevDim = player.dimension;
		WorldServer prevWorld = player.server.getWorld(prevDim);
		WorldServer newWorld = player.server.getWorld(dim);
		player.dimension = dim;

		if(newWorld == null) return false;

		PlayerList man = player.server.getPlayerList();
		NetHandlerPlayServer net = player.connection;
		net.sendPacket(new SPacketRespawn(player.dimension, player.world.getDifficulty(), newWorld.getWorldInfo().getTerrainType(), player.interactionManager.getGameType()));
		prevWorld.removeEntityDangerously(player);
		player.isDead = false;

		if(player.isEntityAlive()) {
			player.setLocationAndAngles(x, y, z, player.rotationYaw, player.rotationPitch);
			newWorld.spawnEntity(player);
			newWorld.updateEntityWithOptionalForce(player, false);
		}

		player.setWorld(newWorld);

		man.preparePlayer(player, prevWorld);
		net.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
		player.interactionManager.setWorld(newWorld);
		man.updateTimeAndWeatherForPlayer(player, newWorld);
		man.syncPlayerInventory(player);
		Iterator iterator = player.getActivePotionEffects().iterator();

		while(iterator.hasNext()) {
			PotionEffect potioneffect = (PotionEffect) iterator.next();
			player.connection.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
		}
		FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, prevDim, dim);
		return true;
	}

	/** Teleports non-player entities to different dimensions, gracefully copied from Entity */
	public static boolean teleportEntityInterdimensionally(Entity oldEntity, double x, double y, double z, int dim) {

		MinecraftServer minecraftserver = oldEntity.getServer();
		WorldServer newWorld = minecraftserver.getWorld(dim);

		if(newWorld == null) return false;

		oldEntity.world.removeEntity(oldEntity);
		oldEntity.isDead = false;

		Entity entity = EntityList.createEntityByIDFromName(EntityList.getKey(oldEntity), newWorld);

		if(entity != null) {
			// Original copyDataFrom included Dimension in NBT; the native private helper
			// removes it. Retain the original serialized data and portal cooldown/direction.
			entity.readFromNBT(oldEntity.writeToNBT(new NBTTagCompound()));
			entity.timeUntilPortal = oldEntity.timeUntilPortal;
			ReflectionHelper.setPrivateValue(Entity.class, entity, oldEntity.getTeleportDirection(), "teleportDirection", "field_181018_ap");
			entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
			newWorld.updateEntityWithOptionalForce(entity, false);
			entity.setWorld(newWorld);

			net.minecraft.world.gen.ChunkProviderServer provider = newWorld.getChunkProvider();
			provider.loadChunk(((int) Math.floor(x)) >> 4, ((int) Math.floor(z)) >> 4);
			newWorld.spawnEntity(entity);
		}

		oldEntity.isDead = true;
		newWorld.resetUpdateEntityTick();

		return true;
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tank};
	}
}

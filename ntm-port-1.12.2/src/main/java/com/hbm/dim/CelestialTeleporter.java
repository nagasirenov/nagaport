package com.hbm.dim;

import java.util.ArrayDeque;
import java.util.Queue;

import com.hbm.entity.missile.EntityRideableRocket;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class CelestialTeleporter extends Teleporter {

	private final WorldServer sourceServer;
	private final WorldServer targetServer;

	private double x;
	private double y;
	private double z;

	private boolean grounded; // Should we be placed directly on the first ground block below?

	private Entity entity;

	public CelestialTeleporter(WorldServer sourceServer, WorldServer targetServer, Entity entity, double x, double y, double z, boolean grounded) {
		super(targetServer);
		this.sourceServer = sourceServer;
		this.targetServer = targetServer;
		this.entity = entity;
		this.x = x;
		this.y = y;
		this.z = z;
		this.grounded = grounded;
	}

	@Override
	public void placeInPortal(Entity entity, float yaw) {
		int ix = (int)x;
		int iy = (int)y;
		int iz = (int)z;

		if(grounded) {
			for(int i = targetServer.getHeight(); i > 0; i--) {
				if(com.hbm.world.WorldBlockState.read(targetServer, ix, i, iz).getBlock() != Blocks.AIR) {
					y = i + 5;
					break;
				}
			}
		} else {
			com.hbm.world.WorldBlockState.read(targetServer, ix, MathHelper.clamp(iy, 1, 255), iz); // dummy load to maybe gen chunk
		}

		entity.setPosition(x, y, z);
	}

	private void runTeleport() {
		MinecraftServer mcServer = net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
		PlayerList manager = mcServer.getPlayerList();

		// If this entity got teleported with a player rider, switch to the rider!
		if(entity.isBeingRidden() && entity.getPassengers().get(0) instanceof EntityPlayerMP) {
			entity = entity.getPassengers().get(0);
		}

		// Store these since they change after transfer
		int fromDimension = entity.dimension;

		entity.posX = x;
		entity.posZ = z;

		if(entity instanceof EntityPlayerMP) {
			EntityPlayerMP playerMP = (EntityPlayerMP) entity;
			Entity ridingEntity = entity.getRidingEntity();

			manager.transferPlayerToDimension(playerMP, targetServer.provider.getDimension(), this);

			if(ridingEntity != null && !ridingEntity.isDead) {
				ridingEntity.dimension = fromDimension;
				ridingEntity.world.removeEntity(ridingEntity);
				ridingEntity.isDead = false;

				manager.transferEntityToWorld(ridingEntity, fromDimension, sourceServer, targetServer, this);

				Entity newEntity = EntityList.createEntityByIDFromName(EntityList.getKey(ridingEntity), targetServer);
				if(newEntity != null) {
					newEntity.readFromNBT(ridingEntity.writeToNBT(new NBTTagCompound()));
					newEntity.posX = x;
					newEntity.posZ = z;
					targetServer.spawnEntity(newEntity);
					newEntity.dimension = targetServer.provider.getDimension();
					playerMP.startRiding(newEntity);
				}

				ridingEntity.isDead = true;
				sourceServer.resetUpdateEntityTick();
				targetServer.resetUpdateEntityTick();

				// Ensure rocket stickiness
				if(newEntity instanceof EntityRideableRocket) {
					((EntityRideableRocket) newEntity).setThrower(playerMP);
				}

				// Send another packet to the client to make sure they load in correctly!
				playerMP.setPositionAndUpdate(x, 900, z);
			}
		} else {
			entity.world.removeEntity(entity);
			entity.isDead = false;

			manager.transferEntityToWorld(entity, fromDimension, sourceServer, targetServer, this);

			Entity newEntity = EntityList.createEntityByIDFromName(EntityList.getKey(entity), targetServer);
			if(newEntity != null) {
				newEntity.readFromNBT(entity.writeToNBT(new NBTTagCompound()));
				newEntity.posX = x;
				newEntity.posZ = z;
				targetServer.spawnEntity(newEntity);
				newEntity.dimension = targetServer.provider.getDimension();
			}

			entity.isDead = true;
			sourceServer.resetUpdateEntityTick();
			targetServer.resetUpdateEntityTick();
		}
	}

	public static void runQueuedTeleport() {
		CelestialTeleporter teleporter = queue.poll();
		if(teleporter != null) teleporter.runTeleport();
	}

	private static Queue<CelestialTeleporter> queue = new ArrayDeque<>();

	public static void teleport(Entity entity, int dim, double x, double y, double z, boolean grounded) {
		if(entity.dimension == dim) return; // ignore if we're teleporting to the same place

		MinecraftServer mcServer = net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
		Side sidex = FMLCommonHandler.instance().getEffectiveSide();
		if(sidex == Side.SERVER) {
			WorldServer sourceServer = mcServer.getWorld(entity.dimension);
			WorldServer targetServer = mcServer.getWorld(dim);

			queue.add(new CelestialTeleporter(sourceServer, targetServer, entity, x, y, z, grounded));
		}
	}

}

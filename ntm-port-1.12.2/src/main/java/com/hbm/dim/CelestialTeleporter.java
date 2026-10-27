package com.hbm.dim;

import java.util.ArrayDeque;
import java.util.Queue;

import com.hbm.entity.missile.EntityRideableRocket;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

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
		if(targetServer == null || sourceServer == null || entity == null || entity.isDead) return;

		MinecraftServer mcServer = net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
		PlayerList manager = mcServer.getPlayerList();

		// If this entity got teleported with a player rider, switch to the rider!
		if(entity.isBeingRidden() && !entity.getPassengers().isEmpty() && entity.getPassengers().get(0) instanceof EntityPlayerMP) {
			entity = entity.getPassengers().get(0);
		}

		// Store these since they change after transfer
		int fromDimension = entity.dimension;
		int toDimension = targetServer.provider.getDimension();

		entity.posX = x;
		entity.posZ = z;

		if(entity instanceof EntityPlayerMP) {
			EntityPlayerMP playerMP = (EntityPlayerMP) entity;
			Entity ridingEntity = entity.getRidingEntity();

			manager.transferPlayerToDimension(playerMP, toDimension, this);

			if(ridingEntity != null && !ridingEntity.isDead) {
				sourceServer.removeEntityDangerously(ridingEntity);
				ridingEntity.isDead = false;
				ridingEntity.dimension = toDimension;

				manager.transferEntityToWorld(ridingEntity, fromDimension, sourceServer, targetServer, this);

				ridingEntity.setPosition(x, y, z);
				playerMP.setPosition(x, y, z);

				// Ensure rocket stickiness
				if(ridingEntity instanceof EntityRideableRocket) {
					((EntityRideableRocket) ridingEntity).setThrower(playerMP);
				}

				playerMP.startRiding(ridingEntity, true);

				net.minecraft.entity.EntityTrackerEntry entry = com.hbm.util.TrackerUtil.getTrackerEntry(targetServer, ridingEntity.getEntityId());
				if(entry != null) {
					entry.updatePlayerEntity(playerMP);
				}

				playerMP.connection.sendPacket(new net.minecraft.network.play.server.SPacketSetPassengers(ridingEntity));
				playerMP.setPositionAndUpdate(x, y, z);

				sourceServer.resetUpdateEntityTick();
				targetServer.resetUpdateEntityTick();
			}
		} else {
			sourceServer.removeEntityDangerously(entity);
			entity.isDead = false;
			entity.dimension = toDimension;

			manager.transferEntityToWorld(entity, fromDimension, sourceServer, targetServer, this);
			entity.setPosition(x, y, z);

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
		if(entity == null || entity.dimension == dim) return; // ignore if we're teleporting to the same place

		MinecraftServer mcServer = net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance();
		Side sidex = FMLCommonHandler.instance().getEffectiveSide();
		if(sidex == Side.SERVER) {
			if(!DimensionManager.isDimensionRegistered(dim)) return;
			WorldServer sourceServer = mcServer.getWorld(entity.dimension);
			if(sourceServer == null) {
				DimensionManager.initDimension(entity.dimension);
				sourceServer = mcServer.getWorld(entity.dimension);
			}
			WorldServer targetServer = mcServer.getWorld(dim);
			if(targetServer == null) {
				DimensionManager.initDimension(dim);
				targetServer = mcServer.getWorld(dim);
			}
			if(sourceServer == null || targetServer == null) return;

			queue.add(new CelestialTeleporter(sourceServer, targetServer, entity, x, y, z, grounded));
		}
	}

}

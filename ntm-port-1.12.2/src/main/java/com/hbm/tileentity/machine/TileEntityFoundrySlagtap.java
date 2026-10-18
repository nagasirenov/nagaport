package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDynamicSlag.TileEntitySlag;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.Compat;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityFoundrySlagtap extends TileEntityFoundryOutlet implements ICrucibleAcceptor {

	@Override
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) {
		if(filter != null && (filter != stack.material ^ invertFilter)) return false;
		if(isClosed()) return false;
		if(side != ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite()) return false;

		Vec3d start = new Vec3d(x + 0.5, y - 0.125, z + 0.5);
		Vec3d end = new Vec3d(x + 0.5, y + 0.125 - 15, z + 0.5);

		RayTraceResult mop = world.rayTraceBlocks(start, end, true, true, true);

		if(mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK) {
			return false;
		}

		return true;
	}

	@Override
	public MaterialStack flow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) {

		if(stack == null || stack.material == null || stack.amount <= 0) {
			return null;
		}

		Vec3d start = new Vec3d(x + 0.5, y - 0.125, z + 0.5);
		Vec3d end = new Vec3d(x + 0.5, y + 0.125 - 15, z + 0.5);

		RayTraceResult mop = world.rayTraceBlocks(start, end, true, true, true);

		if(mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK) {
			return null;
		}

		int hitX = mop.getBlockPos().getX();
		int hitY = mop.getBlockPos().getY();
		int hitZ = mop.getBlockPos().getZ();
		Block hit = world.getBlock(hitX, hitY, hitZ);
		Block above = world.getBlock(hitX, hitY + 1, hitZ);

		boolean didFlow = false;

		if(hit == ModBlocks.slag) {
			TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, hitX, hitY, hitZ);
			if(tile.mat == stack.material) {
				int transfer = Math.min(tile.maxAmount - tile.amount, stack.amount);
				tile.amount += transfer;
				stack.amount -= transfer;
				didFlow = didFlow || transfer > 0;
				world.markBlockForUpdate(hitX, hitY, hitZ);
				world.scheduleBlockUpdate(hitX, hitY, hitZ, ModBlocks.slag, 1);
			}
		} else if(hit.isReplaceable(world, hitX, hitY, hitZ)) {
			world.setBlock(hitX, hitY, hitZ, ModBlocks.slag);
			TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, hitX, hitY, hitZ);
			tile.mat = stack.material;
			int transfer = Math.min(tile.maxAmount, stack.amount);
			tile.amount += transfer;
			stack.amount -= transfer;
			didFlow = didFlow || transfer > 0;
			world.markBlockForUpdate(hitX, hitY, hitZ);
			world.scheduleBlockUpdate(hitX, hitY, hitZ, ModBlocks.slag, 1);
		}

		if(stack.amount > 0 && above.isReplaceable(world, hitX, hitY + 1, hitZ)) {
			world.setBlock(hitX, hitY + 1, hitZ, ModBlocks.slag);
			TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, hitX, hitY + 1, hitZ);
			tile.mat = stack.material;
			int transfer = Math.min(tile.maxAmount, stack.amount);
			tile.amount += transfer;
			stack.amount -= transfer;
			didFlow = didFlow || transfer > 0;
			world.markBlockForUpdate(hitX, hitY + 1, hitZ);
			world.scheduleBlockUpdate(hitX, hitY + 1, hitZ, ModBlocks.slag, 1);
		}

		if(didFlow) {
			ForgeDirection dir = side.getOpposite();

			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "foundry");
			data.setInteger("color", stack.material.moltenColor);
			data.setByte("dir", (byte) dir.ordinal());
			data.setFloat("off", 0.375F);
			data.setFloat("base", 0F);
			data.setFloat("len", Math.max(1F, yCoord - (float) (Math.ceil(hitY))));
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, xCoord + 0.5D - dir.offsetX * 0.125, yCoord + 0.125, zCoord + 0.5D - dir.offsetZ * 0.125), new TargetPoint(worldObj.provider.dimensionId, xCoord + 0.5, yCoord, zCoord + 0.5, 50));
		}

		if(stack.amount <= 0) {
			stack = null;
		}

		return stack;
	}

	@Override public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return false; }
	@Override public MaterialStack pour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return stack; }
}

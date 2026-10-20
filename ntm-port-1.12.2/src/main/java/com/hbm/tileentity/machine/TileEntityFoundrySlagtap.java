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
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
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
		Block hit = world.getBlockState(new BlockPos(hitX, hitY, hitZ)).getBlock();
		Block above = world.getBlockState(new BlockPos(hitX, hitY + 1, hitZ)).getBlock();

		boolean didFlow = false;

		if(hit == ModBlocks.slag) {
			TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, hitX, hitY, hitZ);
			if(tile.mat == stack.material) {
				int transfer = Math.min(tile.maxAmount - tile.amount, stack.amount);
				tile.amount += transfer;
				stack.amount -= transfer;
				didFlow = didFlow || transfer > 0;
				IBlockState changed = world.getBlockState(new BlockPos(hitX, hitY, hitZ));
				world.notifyBlockUpdate(new BlockPos(hitX, hitY, hitZ), changed, changed, 3);
				world.scheduleUpdate(new BlockPos(hitX, hitY, hitZ), ModBlocks.slag, 1);
			}
		} else if(hit.isReplaceable(world, new BlockPos(hitX, hitY, hitZ))) {
			world.setBlockState(new BlockPos(hitX, hitY, hitZ), ModBlocks.slag.getDefaultState(), 3);
			TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, hitX, hitY, hitZ);
			tile.mat = stack.material;
			int transfer = Math.min(tile.maxAmount, stack.amount);
			tile.amount += transfer;
			stack.amount -= transfer;
			didFlow = didFlow || transfer > 0;
			IBlockState changed = world.getBlockState(new BlockPos(hitX, hitY, hitZ));
			world.notifyBlockUpdate(new BlockPos(hitX, hitY, hitZ), changed, changed, 3);
			world.scheduleUpdate(new BlockPos(hitX, hitY, hitZ), ModBlocks.slag, 1);
		}

		if(stack.amount > 0 && above.isReplaceable(world, new BlockPos(hitX, hitY + 1, hitZ))) {
			world.setBlockState(new BlockPos(hitX, hitY + 1, hitZ), ModBlocks.slag.getDefaultState(), 3);
			TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, hitX, hitY + 1, hitZ);
			tile.mat = stack.material;
			int transfer = Math.min(tile.maxAmount, stack.amount);
			tile.amount += transfer;
			stack.amount -= transfer;
			didFlow = didFlow || transfer > 0;
			IBlockState changed = world.getBlockState(new BlockPos(hitX, hitY + 1, hitZ));
			world.notifyBlockUpdate(new BlockPos(hitX, hitY + 1, hitZ), changed, changed, 3);
			world.scheduleUpdate(new BlockPos(hitX, hitY + 1, hitZ), ModBlocks.slag, 1);
		}

		if(didFlow) {
			ForgeDirection dir = side.getOpposite();

			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "foundry");
			data.setInteger("color", stack.material.moltenColor);
			data.setByte("dir", (byte) dir.ordinal());
			data.setFloat("off", 0.375F);
			data.setFloat("base", 0F);
			data.setFloat("len", Math.max(1F, this.pos.getY() - (float) (Math.ceil(hitY))));
			PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, this.pos.getX() + 0.5D - dir.offsetX * 0.125, this.pos.getY() + 0.125, this.pos.getZ() + 0.5D - dir.offsetZ * 0.125), new TargetPoint(this.world.provider.getDimension(), this.pos.getX() + 0.5, this.pos.getY(), this.pos.getZ() + 0.5, 50));
		}

		if(stack.amount <= 0) {
			stack = null;
		}

		return stack;
	}

	@Override public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return false; }
	@Override public MaterialStack pour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return stack; }
}

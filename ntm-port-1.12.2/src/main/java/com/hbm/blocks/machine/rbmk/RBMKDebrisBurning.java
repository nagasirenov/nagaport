package com.hbm.blocks.machine.rbmk;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockNTMSand.EnumSandType;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class RBMKDebrisBurning extends RBMKDebris {

	@Override
	public int tickRate(World world) {

		return 100 + world.rand.nextInt(20);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(!world.isRemote) {

			if(rand.nextInt(5) == 0) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "rbmkflame");
				data.setInteger("maxAge", 300);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x + 0.25 + rand.nextDouble() * 0.5, y + 1.75, z + 0.25 + rand.nextDouble() * 0.5), new TargetPoint(world.provider.getDimension(), x + 0.5, y + 1.75, z + 0.5, 75));
				MainRegistry.proxy.effectNT(data);
				world.playSound(null, x + 0.5F, y + 0.5, z + 0.5, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F);
			}

			ForgeDirection dir = ForgeDirection.getOrientation(rand.nextInt(6));
			Block block = world.getBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ)).getBlock();
			int meta = block.getMetaFromState(world.getBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ)));

			if(rand.nextInt(10) == 0 && block == Blocks.AIR) {
				world.setBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ), ModBlocks.gas_meltdown.getDefaultState());
			}

			//Foam helps stop the fire; Boron smothers it. 1.66% chance every 100-120 seconds for one side
			int chance = block == ModBlocks.foam_layer || block == ModBlocks.block_foam ||
					block == ModBlocks.sand_boron_layer || (block == ModBlocks.sand_mix && meta == EnumSandType.BORON.ordinal()) ? 10 : 100;

			if(rand.nextInt(chance) == 0) {
				world.setBlockState(pos, ModBlocks.pribris.getDefaultState());
			} else {
				world.scheduleUpdate(pos, this, this.tickRate(world));
			}
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(!world.isRemote) {
			if(world.rand.nextInt(3) == 0) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "rbmkflame");
				data.setInteger("maxAge", 300);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x + 0.25 + world.rand.nextDouble() * 0.5, y + 1.75, z + 0.25 + world.rand.nextDouble() * 0.5), new TargetPoint(world.provider.getDimension(), x + 0.5, y + 1.75, z + 0.5, 75));
				MainRegistry.proxy.effectNT(data);
			}
		}

		world.scheduleUpdate(pos, this, this.tickRate(world));
	}
}

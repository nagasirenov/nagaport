package com.hbm.blocks.machine.rbmk;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockNTMSand.EnumSandType;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class RBMKDebrisRadiating extends RBMKDebrisBurning {

	@Override
	public int tickRate(World world) {

		return 20 + world.rand.nextInt(20);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(!world.isRemote) {

			radiate(world, x, y, z);

			if(rand.nextInt(5) == 0) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "rbmkflame");
				data.setInteger("maxAge", 300);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x + rand.nextDouble(), y + 1.75, z + rand.nextDouble()), new TargetPoint(world.provider.getDimension(), x + 0.5, y + 1.75, z + 0.5, 75));
				//MainRegistry.proxy.effectNT(data);
				world.playSound(null, x + 0.5F, y + 0.5, z + 0.5, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F);

			}

			ForgeDirection dir = ForgeDirection.getOrientation(rand.nextInt(6));
			Block block = world.getBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ)).getBlock();
			int m = block.getMetaFromState(world.getBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ)));

			if(rand.nextInt(10) == 0 && block == Blocks.AIR) {
				world.setBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ), ModBlocks.gas_meltdown.getDefaultState());
			}

			//Boron sand helps stop the fission reaction; 0.66% chance every 20-40 ticks for one side
			int chance = block == ModBlocks.sand_boron_layer || (block == ModBlocks.sand_mix && m == EnumSandType.BORON.ordinal()) ? 25 : 1000;

			if(rand.nextInt(chance) == 0) {

				int meta = getMetaFromState(world.getBlockState(pos));

				if(meta < 15) {
					world.setBlockState(pos, getStateFromMeta(meta + 1), 2);
					world.scheduleUpdate(pos, this, this.tickRate(world));
				} else {
					world.setBlockState(pos, ModBlocks.pribris_burning.getDefaultState());
				}

			} else {
				world.scheduleUpdate(pos, this, this.tickRate(world));
			}
		}
	}

	private void radiate(World world, int x, int y, int z) {

		float rads = 1000000F;
		double range = 100D;

		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).grow(range, range, range));

		for(EntityLivingBase e : entities) {

			Vec3d vec = new Vec3d(e.posX - (x + 0.5), (e.posY + e.getEyeHeight()) - (y + 0.5), e.posZ - (z + 0.5));
			double len = vec.length();
			vec = vec.normalize();

			float res = 0;

			for(int i = 1; i < len; i++) {

				int ix = (int)Math.floor(x + 0.5 + vec.x * i);
				int iy = (int)Math.floor(y + 0.5 + vec.y * i);
				int iz = (int)Math.floor(z + 0.5 + vec.z * i);

				res += world.getBlockState(new BlockPos(ix, iy, iz)).getBlock().getExplosionResistance(null);
			}

			if(res < 1)
				res = 1;

			float eRads = rads;
			eRads /= (float)res;
			eRads /= (float)(len * len);

			ContaminationUtil.contaminate(e, HazardType.RADIATION, ContaminationType.CREATIVE, eRads);

			if(len < 5) {
				e.attackEntityFrom(DamageSource.IN_FIRE, 100);
			}

			if(e instanceof EntityPlayer && len < 10) {
				EntityPlayer p = (EntityPlayer) e;

				if(!p.getHeldItemMainhand().isEmpty() && p.getHeldItemMainhand().getItem() == ModItems.marshmallow && p.getHeldItemMainhand().getItemDamage() != 1 && p.getRNG().nextInt(100) == 0) {
					p.getHeldItemMainhand().setItemDamage(1);
				}
			}
		}
	}
}

package com.hbm.blocks.generic;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import com.hbm.lib.ForgeDirection;

public class BlockHazard extends Block implements ITooltipProvider {

	protected float rad = 0.0F;
	private ExtDisplayEffect extEffect = null;

	private boolean beaconable = false;

	public BlockHazard() {
		this(Material.IRON);
	}

	public BlockHazard(Material mat) {
		super(mat);
	}

	public BlockHazard setDisplayEffect(ExtDisplayEffect extEffect) {
		this.extEffect = extEffect;
		return this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(extEffect == null)
			return;

		switch(extEffect) {
		case RADFOG:
		case SCHRAB:
		case FLAMES:
			sPart(world, x, y, z, rand);
			break;

		case SPARKS:
			break;

		case LAVAPOP:
			world.spawnParticle(net.minecraft.util.EnumParticleTypes.LAVA, x + rand.nextFloat(), y + 1.1F, z + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
			break;

		default: break;
		}
	}

	private void sPart(World world, int x, int y, int z, Random rand) {

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

			if(dir == ForgeDirection.DOWN && this.extEffect == ExtDisplayEffect.FLAMES)
				continue;

			if(com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock().getDefaultState().getMaterial() == Material.AIR) {

				double ix = x + 0.5F + dir.offsetX + rand.nextDouble() * 3 - 1.5D;
				double iy = y + 0.5F + dir.offsetY + rand.nextDouble() * 3 - 1.5D;
				double iz = z + 0.5F + dir.offsetZ + rand.nextDouble() * 3 - 1.5D;

				if(dir.offsetX != 0)
					ix = x + 0.5F + dir.offsetX * 0.5 + rand.nextDouble() * dir.offsetX;
				if(dir.offsetY != 0)
					iy = y + 0.5F + dir.offsetY * 0.5 + rand.nextDouble() * dir.offsetY;
				if(dir.offsetZ != 0)
					iz = z + 0.5F + dir.offsetZ * 0.5 + rand.nextDouble() * dir.offsetZ;

				if(this.extEffect == ExtDisplayEffect.RADFOG) {
					world.spawnParticle(net.minecraft.util.EnumParticleTypes.TOWN_AURA, ix, iy, iz, 0.0, 0.0, 0.0);
				}
				if(this.extEffect == ExtDisplayEffect.SCHRAB) {
					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "schrabfog");
					data.setDouble("posX", ix);
					data.setDouble("posY", iy);
					data.setDouble("posZ", iz);
					MainRegistry.proxy.effectNT(data);
				}
				if(this.extEffect == ExtDisplayEffect.FLAMES) {
					world.spawnParticle(net.minecraft.util.EnumParticleTypes.FLAME, ix, iy, iz, 0.0, 0.0, 0.0);
					world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, ix, iy, iz, 0.0, 0.0, 0.0);
					world.spawnParticle(net.minecraft.util.EnumParticleTypes.SMOKE_NORMAL, ix, iy, iz, 0.0, 0.1, 0.0);
				}
			}
		}
	}

	/** NTM construction hook for the native protected sound setter. */
	public BlockHazard withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	public BlockHazard makeBeaconable() {
		this.beaconable = true;
		return this;
	}

	public boolean isBeaconBase(IBlockAccess worldObj, int x, int y, int z, int beaconX, int beaconY, int beaconZ) {
		return beaconable;
	}


		@Override
	public void updateTick(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, java.util.Random rand) {
		updateTick(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
	}

public void updateTick(World world, int x, int y, int z, Random rand) {

		if(this.rad > 0) {
			ChunkRadiationManager.proxy.incrementRad(world, x, y, z, rad);
			world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
		}
	}

	@Override
	public int tickRate(World world) {

		if(this.rad > 0)
			return 20;

		return super.tickRate(world);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		// who wrote this???
		rad = HazardSystem.getHazardLevelFromStack(new ItemStack(this), HazardRegistry.RADIATION) * 0.1F;

		if(this.rad > 0)
			world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, this.tickRate(world));
	}

	public static enum ExtDisplayEffect {
		RADFOG,
		SPARKS,
		SCHRAB,
		FLAMES,
		LAVAPOP
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) { }

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		if(this == ModBlocks.block_schraranium
				|| this == ModBlocks.block_schraranium
				|| this == ModBlocks.block_schrabidate
				|| this == ModBlocks.block_solinium
				|| this == ModBlocks.block_schrabidium_fuel)
			return EnumRarity.RARE;

		return EnumRarity.COMMON;
	}
}

package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.atmosphere.IBlockSealable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityFurnaceBrick;
import com.hbm.util.ItemStackUtil;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import java.util.Random;

public class MachineBrickFurnace extends BlockThermalFurnace implements IBlockSealable {

	private final Random rand = new Random();
	private static boolean keepInventory;

	public MachineBrickFurnace(boolean blockState) {
		super(Material.IRON, blockState);
	}


	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityFurnaceBrick();
	}

	@SideOnly(Side.CLIENT)
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(ModBlocks.machine_furnace_brick_off);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		this.setDefaultDirection(world, pos);
	}

	private void setDefaultDirection(World world, BlockPos pos) {
		if(!world.isRemote) {
			IBlockState nZ = world.getBlockState(pos.north());
			IBlockState pZ = world.getBlockState(pos.south());
			IBlockState nX = world.getBlockState(pos.west());
			IBlockState pX = world.getBlockState(pos.east());

			byte meta = 3;

			if(nZ.isFullBlock() && !pZ.isFullBlock()) meta = 3;
			if(pZ.isFullBlock() && !nZ.isFullBlock()) meta = 2;
			if(nX.isFullBlock() && !pX.isFullBlock()) meta = 5;
			if(pX.isFullBlock() && !nX.isFullBlock()) meta = 4;

			world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, (int) (meta)), 2);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		if(i == 0) world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 2), 2);
		if(i == 1) world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 5), 2);
		if(i == 2) world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 3), 2);
		if(i == 3) world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 4), 2);

		if(itemStack.hasDisplayName()) ((TileEntityFurnaceBrick)world.getTileEntity(pos)).setCustomName(itemStack.getDisplayName());
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			TileEntityFurnaceBrick entity = (TileEntityFurnaceBrick) world.getTileEntity(pos);
			if(entity != null) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		} else {
			return false;
		}
	}

	public static void updateBlockState(boolean isProcessing, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
		int i = world.getBlockState(pos).getValue(METADATA);
		TileEntity entity = world.getTileEntity(pos);
		keepInventory = true;

		if(isProcessing) {
			world.setBlockState(pos, ModBlocks.machine_furnace_brick_on.getDefaultState());
		} else {
			world.setBlockState(pos, ModBlocks.machine_furnace_brick_off.getDefaultState());
		}

		keepInventory = false;
		world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, i), 2);

		if(entity != null) {
			entity.validate();
			world.setTileEntity(pos, entity);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        Block block = state.getBlock();
		if(!keepInventory) ItemStackUtil.spillItems(world, x, y, z, block, rand);
		super.breakBlock(world, pos, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(isActive) {
			int meta = world.getBlockState(pos).getValue(METADATA);
			float cX = x + 0.5F;
			float cY = y + rand.nextFloat() * 0.375F;
			float cZ = z + 0.5F;
			float off = 0.52F;
			float var = rand.nextFloat() * 0.6F - 0.3F;
			rand.nextFloat();
			rand.nextFloat();

			if(meta == 4) {
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, cX - off, cY, cZ + var, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticleTypes.FLAME, cX - off, cY, cZ + var, 0.0D, 0.0D, 0.0D);
			} else if(meta == 5) {
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, cX + off, cY, cZ + var, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticleTypes.FLAME, cX + off, cY, cZ + var, 0.0D, 0.0D, 0.0D);
			} else if(meta == 2) {
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, cX + var, cY, cZ - off, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticleTypes.FLAME, cX + var, cY, cZ - off, 0.0D, 0.0D, 0.0D);
			} else if(meta == 3) {
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, cX + var, cY, cZ + off, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticleTypes.FLAME, cX + var, cY, cZ + off, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	public boolean isSealed(World world, int x, int y, int z) {
		return false;
	}
}

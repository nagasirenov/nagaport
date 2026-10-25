package com.hbm.blocks.bomb;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.IMultiblock;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityLaunchTable;
import com.hbm.tileentity.machine.TileEntityDummy;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class LaunchTable extends BlockContainer implements IMultiblock, IBomb {

	public static final net.minecraft.block.properties.PropertyInteger ORIENTATION = net.minecraft.block.properties.PropertyInteger.create("orientation", 0, 3);

	public LaunchTable(Material p_i45386_1_) {
		super(p_i45386_1_);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ORIENTATION, 0));
	}

	@Override
	protected net.minecraft.block.state.BlockStateContainer createBlockState() {
		return new net.minecraft.block.state.BlockStateContainer(this, ORIENTATION);
	}

	@Override
	public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(ORIENTATION, meta & 3);
	}

	@Override
	public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
		return state.getValue(ORIENTATION);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityLaunchTable();
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return Item.getItemFromBlock(ModBlocks.struct_launcher_core_large);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			TileEntityLaunchTable entity = (TileEntityLaunchTable) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(entity != null) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onBlockPlacedBy(World worldObj, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int xCoord = nativePlacementPos.getX(), yCoord = nativePlacementPos.getY(), zCoord = nativePlacementPos.getZ();
		int d = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		for(int k = -4; k <= 4; k++)
			for(int l = -4; l <= 4; l++)
				if(l != 0 && k != 0)
					if(!com.hbm.world.WorldBlockState.read(worldObj, xCoord + k, yCoord, zCoord + l).getBlock().isReplaceable(worldObj, new net.minecraft.util.math.BlockPos(xCoord + k, yCoord, zCoord + l))) {
						com.hbm.world.WorldBlockDestruction.destroy(worldObj, xCoord, yCoord, zCoord, true);
						return;
					}

		if(d == 0) {

			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(worldObj, xCoord, yCoord, zCoord, 0, 2);
			for(int i = 1; i < 12; i++)
				{ net.minecraft.world.World nativePlacementWorld = worldObj; int nativePlacementX = xCoord + 3; int nativePlacementY = yCoord + i; int nativePlacementZ = zCoord; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(worldObj, xCoord + i, yCoord, zCoord, xCoord, yCoord, zCoord, ModBlocks.dummy_port_launch_table);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(worldObj, xCoord, yCoord, zCoord + i, xCoord, yCoord, zCoord, ModBlocks.dummy_plate_launch_table);
		}
		if(d == 1) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(worldObj, xCoord, yCoord, zCoord, 2, 2);
			for(int i = 1; i < 12; i++)
				{ net.minecraft.world.World nativePlacementWorld = worldObj; int nativePlacementX = xCoord; int nativePlacementY = yCoord + i; int nativePlacementZ = zCoord + 3; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(worldObj, xCoord + i, yCoord, zCoord, xCoord, yCoord, zCoord, ModBlocks.dummy_plate_launch_table);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(worldObj, xCoord, yCoord, zCoord + i, xCoord, yCoord, zCoord, ModBlocks.dummy_port_launch_table);
		}
		if(d == 2) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(worldObj, xCoord, yCoord, zCoord, 1, 2);
			for(int i = 1; i < 12; i++)
				{ net.minecraft.world.World nativePlacementWorld = worldObj; int nativePlacementX = xCoord - 3; int nativePlacementY = yCoord + i; int nativePlacementZ = zCoord; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(worldObj, xCoord + i, yCoord, zCoord, xCoord, yCoord, zCoord, ModBlocks.dummy_port_launch_table);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(worldObj, xCoord, yCoord, zCoord + i, xCoord, yCoord, zCoord, ModBlocks.dummy_plate_launch_table);
		}
		if(d == 3) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(worldObj, xCoord, yCoord, zCoord, 3, 2);
			for(int i = 1; i < 12; i++)
				{ net.minecraft.world.World nativePlacementWorld = worldObj; int nativePlacementX = xCoord; int nativePlacementY = yCoord + i; int nativePlacementZ = zCoord - 3; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(worldObj, xCoord + i, yCoord, zCoord, xCoord, yCoord, zCoord, ModBlocks.dummy_plate_launch_table);

			for(int i = -4; i <= 4; i++)
				if(i != 0)
					placeDummy(worldObj, xCoord, yCoord, zCoord + i, xCoord, yCoord, zCoord, ModBlocks.dummy_port_launch_table);
		}

		for(int i = -4; i <= 4; i++)
			for(int j = -4; j <= 4; j++)
				if(i != 0 && j != 0)
					placeDummy(worldObj, xCoord + i, yCoord, zCoord + j, xCoord, yCoord, zCoord, ModBlocks.dummy_port_launch_table);
	}

	private void placeDummy(World world, int x, int y, int z, int xCoord, int yCoord, int zCoord, Block block) {

		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, block);

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(te instanceof TileEntityDummy) {
			TileEntityDummy dummy = (TileEntityDummy) te;
			dummy.targetX = xCoord;
			dummy.targetY = yCoord;
			dummy.targetZ = zCoord;
		}
	}

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {

		if(!world.isRemote) {
			TileEntityLaunchTable entity = (TileEntityLaunchTable) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(entity.canLaunch()) {
				entity.launchFromDesignator();
				return BombReturnCode.LAUNCHED;
			}

			return BombReturnCode.ERROR_MISSING_COMPONENT;
		}

		return BombReturnCode.UNDEFINED;
	}

	public static boolean keepInventory = false;
	private final static Random field_149933_a = new Random();

	@Override
	public void breakBlock(World p_149749_1_, BlockPos pos, IBlockState state) {
		int p_149749_2_ = pos.getX(), p_149749_3_ = pos.getY(), p_149749_4_ = pos.getZ();
		Block p_149749_5_ = state.getBlock();
		int p_149749_6_ = p_149749_5_.getMetaFromState(state);
		if(!keepInventory) {
			ISidedInventory tileentityfurnace = (ISidedInventory) p_149749_1_.getTileEntity(new net.minecraft.util.math.BlockPos(p_149749_2_, p_149749_3_, p_149749_4_));

			if(tileentityfurnace != null) {
				for(int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1) {
					ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

					if(itemstack != null && !itemstack.isEmpty()) {
						float f = field_149933_a.nextFloat() * 0.8F + 0.1F;
						float f1 = field_149933_a.nextFloat() * 0.8F + 0.1F;
						float f2 = field_149933_a.nextFloat() * 0.8F + 0.1F;

						while(itemstack.getCount() > 0) {
							int j1 = field_149933_a.nextInt(21) + 10;

							if(j1 > itemstack.getCount()) {
								j1 = itemstack.getCount();
							}

							// Capture before depletion: native empty stacks hide their item.
							net.minecraft.item.Item dropItem = itemstack.getItem();
							int dropMeta = itemstack.getItemDamage();
							NBTTagCompound dropTag = itemstack.getTagCompound();
							itemstack.shrink(j1);
							EntityItem entityitem = new EntityItem(p_149749_1_, p_149749_2_ + f, p_149749_3_ + f1, p_149749_4_ + f2, new ItemStack(dropItem, j1, dropMeta));

							if(dropTag != null) {
								entityitem.getItem().setTagCompound(dropTag.copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (float) field_149933_a.nextGaussian() * f3;
							entityitem.motionY = (float) field_149933_a.nextGaussian() * f3 + 0.2F;
							entityitem.motionZ = (float) field_149933_a.nextGaussian() * f3;
							p_149749_1_.spawnEntity(entityitem);
						}
					}
				}

				p_149749_1_.updateComparatorOutputLevel(new net.minecraft.util.math.BlockPos(p_149749_2_, p_149749_3_, p_149749_4_), p_149749_5_);
			}
		}

		super.breakBlock(p_149749_1_, pos, state);
	}

}

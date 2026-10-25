package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IMultiblock;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityDummy;
import com.hbm.tileentity.machine.TileEntityMachineSatDock;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MachineSatDock extends BlockContainer implements IMultiblock {

    private final Random field_149933_a = new Random();
	public MachineSatDock(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityMachineSatDock();
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

		private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 12 * 0.0625F, 1.0F);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return BOUNDS;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			TileEntityMachineSatDock entity = (TileEntityMachineSatDock) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(entity != null)
			{
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void breakBlock(World p_149749_1_, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state)
    {
        int p_149749_2_ = pos.getX(), p_149749_3_ = pos.getY(), p_149749_4_ = pos.getZ();
        Block p_149749_5_ = state.getBlock();
		IInventory tileentityfurnace = (IInventory) p_149749_1_.getTileEntity(new net.minecraft.util.math.BlockPos(p_149749_2_, p_149749_3_, p_149749_4_));

		if (tileentityfurnace != null) {
			for (int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1) {
				ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

				if (itemstack != null && !itemstack.isEmpty()) {
					float f = this.field_149933_a.nextFloat() * 0.8F + 0.1F;
					float f1 = this.field_149933_a.nextFloat() * 0.8F + 0.1F;
					float f2 = this.field_149933_a.nextFloat() * 0.8F + 0.1F;

					while (itemstack.getCount() > 0) {
						int j1 = this.field_149933_a.nextInt(21) + 10;

						if (j1 > itemstack.getCount()) {
							j1 = itemstack.getCount();
						}

						// Capture before depletion: native empty stacks hide their item.
						net.minecraft.item.Item dropItem = itemstack.getItem();
						int dropMeta = itemstack.getItemDamage();
						NBTTagCompound dropTag = itemstack.getTagCompound();
						itemstack.shrink(j1);
						EntityItem entityitem = new EntityItem(p_149749_1_, p_149749_2_ + f, p_149749_3_ + f1,
								p_149749_4_ + f2, new ItemStack(dropItem, j1, dropMeta));

						if (dropTag != null) {
							entityitem.getItem()
									.setTagCompound(dropTag.copy());
						}

						float f3 = 0.05F;
						entityitem.motionX = (float) this.field_149933_a.nextGaussian() * f3;
						entityitem.motionY = (float) this.field_149933_a.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float) this.field_149933_a.nextGaussian() * f3;
						p_149749_1_.spawnEntity(entityitem);
					}
				}
			}

			p_149749_1_.updateComparatorOutputLevel(pos, p_149749_5_);
		}

		super.breakBlock(p_149749_1_, pos, state);
    }

	@Override
	public void onBlockPlacedBy(World worldObj, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int xCoord = nativePlacementPos.getX(), yCoord = nativePlacementPos.getY(), zCoord = nativePlacementPos.getZ();

		for(int k = -1; k <= 1; k++)
			for(int l = -1; l <= 1; l++)
				if(l != 0 || k != 0)
					if(!com.hbm.world.WorldBlockState.read(worldObj, xCoord + k, yCoord, zCoord + l).getBlock().isReplaceable(worldObj, new net.minecraft.util.math.BlockPos(xCoord + k, yCoord, zCoord + l))) {
						com.hbm.world.WorldBlockDestruction.destroy(worldObj, xCoord, yCoord, zCoord, true);
						return;
					}

		for(int k = -1; k <= 1; k++)
			for(int l = -1; l <= 1; l++)
				if(l != 0 || k != 0)
					placeDummy(worldObj, xCoord + k, yCoord, zCoord + l, xCoord, yCoord, zCoord, ModBlocks.dummy_plate_cargo);
	}

	private void placeDummy(World world, int x, int y, int z, int xCoord, int yCoord, int zCoord, Block block) {

		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, block);

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(te instanceof TileEntityDummy) {
			TileEntityDummy dummy = (TileEntityDummy)te;
			dummy.targetX = xCoord;
			dummy.targetY = yCoord;
			dummy.targetZ = zCoord;
		}
	}

}

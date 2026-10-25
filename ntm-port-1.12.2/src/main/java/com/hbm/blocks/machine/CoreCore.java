package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityCore;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CoreCore extends BlockContainer {

	public CoreCore(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityCore();
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
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {

			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			return true;

		} else {
			return false;
		}
	}

	private Random rand = new Random();

	// shitty copy pasted crap for the 50th time because i hate this block
	@Override
	public void breakBlock(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		Block b = state.getBlock();
		int m = b.getMetaFromState(state);
		TileEntityCore core = (TileEntityCore) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(core != null) {
			for(int i1 = 0; i1 < core.getSizeInventory(); ++i1) {
				ItemStack itemstack = core.getStackInSlot(i1);

				if(itemstack != null && !itemstack.isEmpty()) {
					float f = this.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

					while(itemstack.getCount() > 0) {
						int j1 = this.rand.nextInt(21) + 10;
						if(j1 > itemstack.getCount()) j1 = itemstack.getCount();

						// Capture before depletion: native empty stacks hide their item.
						net.minecraft.item.Item dropItem = itemstack.getItem();
						int dropMeta = itemstack.getItemDamage();
						NBTTagCompound dropTag = itemstack.getTagCompound();
						itemstack.shrink(j1);
						EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(dropItem, j1, dropMeta));
						if(dropTag != null) entityitem.getItem().setTagCompound(dropTag.copy());

						float f3 = 0.05F;
						entityitem.motionX = (float) this.rand.nextGaussian() * f3;
						entityitem.motionY = (float) this.rand.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float) this.rand.nextGaussian() * f3;
						world.spawnEntity(entityitem);
					}
				}
			}

			world.updateComparatorOutputLevel(new net.minecraft.util.math.BlockPos(x, y, z), b);
		}

		super.breakBlock(world, pos, state);
	}
}

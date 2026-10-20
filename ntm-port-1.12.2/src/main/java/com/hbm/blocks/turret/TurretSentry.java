package com.hbm.blocks.turret;

import java.util.Random;

import com.hbm.main.MainRegistry;
import com.hbm.tileentity.turret.TileEntityTurretSentry;

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

public class TurretSentry extends BlockContainer {

	public TurretSentry() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityTurretSentry();
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
	
	Random rand = new Random();
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int meta) {

		TileEntityTurretSentry sentry = (TileEntityTurretSentry) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(sentry != null) {
			for(int i = 0; i < sentry.getSizeInventory(); ++i) {
				ItemStack itemstack = sentry.getStackInSlot(i);

				if(itemstack != null && !itemstack.isEmpty()) {
					float oX = this.rand.nextFloat() * 0.8F + 0.1F;
					float oY = this.rand.nextFloat() * 0.8F + 0.1F;
					float oZ = this.rand.nextFloat() * 0.8F + 0.1F;

					while(itemstack.getCount() > 0) {
						int toDrop = this.rand.nextInt(21) + 10;

						if(toDrop > itemstack.getCount()) {
							toDrop = itemstack.getCount();
						}

						// Capture before depletion: native empty stacks hide their item.
						net.minecraft.item.Item dropItem = itemstack.getItem();
						int dropMeta = itemstack.getItemDamage();
						NBTTagCompound dropTag = itemstack.getTagCompound();
						itemstack.shrink(toDrop);
						EntityItem entityitem = new EntityItem(world, x + oX, y + oY, z + oZ, new ItemStack(dropItem, toDrop, dropMeta));

						if(dropTag != null) {
							entityitem.getItem().setTagCompound(dropTag.copy());
						}

						float jump = 0.05F;
						entityitem.motionX = (float) this.rand.nextGaussian() * jump;
						entityitem.motionY = (float) this.rand.nextGaussian() * jump + 0.2F;
						entityitem.motionZ = (float) this.rand.nextGaussian() * jump;
						world.spawnEntity(entityitem);
					}
				}
			}

			world.func_147453_f(x, y, z, b);
		}

		super.breakBlock(world, x, y, z, b, meta);
	}
}

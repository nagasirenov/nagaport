package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemLock;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityLockableBase;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockDecoContainer extends BlockDecoModel implements ITileEntityProvider {
	
	Class<? extends TileEntity> tile;
	
	public BlockDecoContainer(Material mat, Class<? extends Enum> theEnum, boolean multiName, boolean multiTexture, Class<? extends TileEntity> tile) {
		super(mat, theEnum, multiName, multiTexture);
		this.tile = tile;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		try {
			return tile.newInstance();
		} catch (Exception e) {
			MainRegistry.logger.error("BlockDecoContainer attempted to create a TE, but couldn't. How does that even happen?");
			return null;
		}
	}
	
	@Override
	public boolean eventReceived(IBlockState state, World world, BlockPos pos, int eventNo, int eventArg) {
		super.eventReceived(state, world, pos, eventNo, eventArg);
		TileEntity tileentity = world.getTileEntity(pos);
		return tileentity != null ? tileentity.receiveClientEvent(eventNo, eventArg) : false;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		
		if(world.isRemote) {
			return true;
		} else {
			TileEntity entity = world.getTileEntity(pos);
			if(entity instanceof TileEntityLockableBase) { //annoying accommodations for the filing cabinet, but whatever, could potentially be useful
				if(!player.getHeldItem(hand).isEmpty() && (player.getHeldItem(hand).getItem() instanceof ItemLock || player.getHeldItem(hand).getItem() == ModItems.key_kit)) {
					return false;
				} else if(!player.isSneaking() && ((TileEntityLockableBase) entity).canAccess(player)) {
					FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
					return true;
				}
			} else if(!player.isSneaking()) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
        int x=pos.getX(), y=pos.getY(), z=pos.getZ();
		IInventory inventory = (IInventory) world.getTileEntity(pos);
		Random rand = world.rand;
		
		if(inventory != null) {
			for(int i1 = 0; i1 < inventory.getSizeInventory(); ++i1) {
				ItemStack itemstack = inventory.getStackInSlot(i1);

				if (itemstack != null && !itemstack.isEmpty()) {
					float f = rand.nextFloat() * 0.8F + 0.1F;
					float f1 = rand.nextFloat() * 0.8F + 0.1F;
					float f2 = rand.nextFloat() * 0.8F + 0.1F;

					while(itemstack.getCount() > 0) {
						int j1 = rand.nextInt(21) + 10;

						if (j1 > itemstack.getCount()) {
							j1 = itemstack.getCount();
						}
						
						// Capture before depletion: native empty stacks hide their item.
						net.minecraft.item.Item dropItem = itemstack.getItem();
						int dropMeta = itemstack.getItemDamage();
						NBTTagCompound dropTag = itemstack.getTagCompound();
						itemstack.shrink(j1);
						EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(dropItem, j1, dropMeta));
						
						if(dropTag != null) {
							entityitem.getItem().setTagCompound(dropTag.copy());
						}
						
						float f3 = 0.05F;
						entityitem.motionX = (float) rand.nextGaussian() * f3;
						entityitem.motionY = (float) rand.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float) rand.nextGaussian() * f3;
						world.spawnEntity(entityitem);
					}
				}

				world.updateComparatorOutputLevel(pos, state.getBlock());
			}

			super.breakBlock(world, pos, state);
		}
	}
}

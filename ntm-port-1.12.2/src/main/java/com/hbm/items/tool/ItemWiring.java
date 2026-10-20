package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityPylonBase;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemWiring extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {

		if(!player.isSneaking()) {
			
			Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
			
			if(b instanceof BlockDummyable) {
				int[] core = ((BlockDummyable)b).findCore(world, x, y, z);
				
				if(core != null) {
					x = core[0];
					y = core[1];
					z = core[2];
				}
			}
			
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(te != null && te instanceof TileEntityPylonBase) {

				if(stack.getTagCompound() == null) {
					stack.setTagCompound(new NBTTagCompound());

					stack.getTagCompound().setInteger("x", x);
					stack.getTagCompound().setInteger("y", y);
					stack.getTagCompound().setInteger("z", z);

					if(!world.isRemote) {
						player.sendMessage(new TextComponentString("Wire start"));
					}
				} else if(!world.isRemote) {

					int x1 = stack.getTagCompound().getInteger("x");
					int y1 = stack.getTagCompound().getInteger("y");
					int z1 = stack.getTagCompound().getInteger("z");

					if(world.getTileEntity(new net.minecraft.util.math.BlockPos(x1, y1, z1)) instanceof TileEntityPylonBase) {

						TileEntityPylonBase first = (TileEntityPylonBase) world.getTileEntity(new net.minecraft.util.math.BlockPos(x1, y1, z1));
						TileEntityPylonBase second = ((TileEntityPylonBase) te);
						
						switch (TileEntityPylonBase.canConnect(first, second)) {
							case 0:
								first.addConnection(x, y, z);
								second.addConnection(x1, y1, z1);
								player.sendMessage(new TextComponentString("Wire end"));
								break;
							case 1:
								player.sendMessage(new TextComponentString("Wire error - Pylons are not the same type"));
								break;
							case 2:
								player.sendMessage(new TextComponentString("Wire error - Cannot connect to the same pylon"));
								break;
							case 3:
								player.sendMessage(new TextComponentString("Wire error - Pylon is too far away"));
								break;
						}
						
						stack.setTagCompound(null);

					} else {

						if(!world.isRemote) {
							player.sendMessage(new TextComponentString("Wire error"));
						}
						stack.setTagCompound(null);
					}
				}

				player.swingItem();
				return true;
			}
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		if(itemstack.getTagCompound() != null) {
			list.add("Wire start x: " + itemstack.getTagCompound().getInteger("x"));
			list.add("Wire start y: " + itemstack.getTagCompound().getInteger("y"));
			list.add("Wire start z: " + itemstack.getTagCompound().getInteger("z"));
		} else {
			list.add("Right-click poles to connect");
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inhand) {

		if(world.isRemote) {
			if(stack.getTagCompound() != null) {
				Vec3d vec = new Vec3d(
						entity.posX - stack.getTagCompound().getInteger("x"),
						entity.posY - stack.getTagCompound().getInteger("y"),
						entity.posZ - stack.getTagCompound().getInteger("z"));
				
				MainRegistry.proxy.displayTooltip(stack.getDisplayName() + ": " + ((int) vec.length()) + "m", MainRegistry.proxy.ID_CABLE);
			}
		}
	}
}

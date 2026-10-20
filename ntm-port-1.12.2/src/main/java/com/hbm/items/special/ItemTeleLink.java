package com.hbm.items.special;

import java.util.List;

import com.hbm.tileentity.machine.TileEntityMachineTeleporter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemTeleLink extends Item {

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		
		if(!player.isSneaking() && !world.isRemote) {
			
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(!(te instanceof TileEntityMachineTeleporter)) {
				
				if(stack.getTagCompound() == null) {
					stack.setTagCompound(new NBTTagCompound());
				}
	
				stack.getTagCompound().setInteger("x", x);
				stack.getTagCompound().setInteger("y", y);
				stack.getTagCompound().setInteger("z", z);
				stack.getTagCompound().setInteger("dim", player.dimension);
				world.playSoundAtEntity(player, "hbm:item.techBleep", 1.0F, 1.0F);
				player.sendMessage(new TextComponentString(TextFormatting.AQUA + "[TeleLink] Set teleporter exit to " + x + ", " + y + ", " + z + "."));
				player.swingItem();
				
				return true;
				
			} else {
				
				if(!stack.hasTagCompound()) {
					world.playSoundAtEntity(player, "hbm:item.techBoop", 1.0F, 1.0F);
					player.sendMessage(new TextComponentString(TextFormatting.RED + "[TeleLink] No destination set!"));
					return false;
				}
				
				int x1 = stack.getTagCompound().getInteger("x");
				int y1 = stack.getTagCompound().getInteger("y");
				int z1 = stack.getTagCompound().getInteger("z");
				int dim = stack.getTagCompound().getInteger("dim");
				
				TileEntityMachineTeleporter tele = (TileEntityMachineTeleporter) te;

				tele.targetX = x1;
				tele.targetY = y1;
				tele.targetZ = z1;
				tele.targetDim = dim;
				
				tele.markDirty();
				world.playSoundAtEntity(player, "hbm:item.techBleep", 1.0F, 1.0F);
				player.sendMessage(new TextComponentString(TextFormatting.AQUA + "[TeleLink] Teleporters destination has been set!"));
				player.swingItem();
				return true;
			}
		}

		return false;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		if (itemstack.getTagCompound() != null) {
			list.add("X: " + itemstack.getTagCompound().getInteger("x"));
			list.add("Y: " + itemstack.getTagCompound().getInteger("y"));
			list.add("Z: " + itemstack.getTagCompound().getInteger("z"));
			list.add("D: " + itemstack.getTagCompound().getInteger("dim"));
		} else {
			list.add(TextFormatting.RED + "Select exit location first!");
		}
	}

}

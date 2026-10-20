package com.hbm.items.tool;

import java.util.List;

import com.google.common.collect.Multimap;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.tileentity.machine.rbmk.TileEntityCraneConsole;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKDisplay;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemRBMKTool extends Item {
	
	@Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fX, float fY, float fZ) {
		
		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
		
		if(b instanceof RBMKBase) {
			
			int[] pos = ((BlockDummyable)b).findCore(world, x, y, z);
			
			if(pos != null && !world.isRemote) {
				
				if(!stack.hasTagCompound())
					stack.setTagCompound(new NBTTagCompound());

				stack.getTagCompound().setInteger("posX", pos[0]);
				stack.getTagCompound().setInteger("posY", pos[1]);
				stack.getTagCompound().setInteger("posZ", pos[2]);
				
				player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".linked").setStyle(new Style().setColor(TextFormatting.YELLOW)));
			}
			
			return true;
		}
		
		//TODO: at this point just add a fucking interface
		if(b == ModBlocks.rbmk_console && stack.hasTagCompound()) {
			
			if(!world.isRemote) {
				
				int[] pos = ((BlockDummyable)b).findCore(world, x, y, z);
				
				TileEntityRBMKConsole console = (TileEntityRBMKConsole)world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
				int tx = stack.getTagCompound().getInteger("posX");
				int ty = stack.getTagCompound().getInteger("posY");
				int tz = stack.getTagCompound().getInteger("posZ");
				console.setTarget(tx, ty, tz);
				player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".set").setStyle(new Style().setColor(TextFormatting.YELLOW)));
			}
			
			return true;
		}
		
		if(b == ModBlocks.rbmk_crane_console && stack.hasTagCompound()) {
			
			if(!world.isRemote) {
				
				int[] pos = ((BlockDummyable)b).findCore(world, x, y, z);
				
				TileEntityCraneConsole console = (TileEntityCraneConsole)world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
				int tx = stack.getTagCompound().getInteger("posX");
				int ty = stack.getTagCompound().getInteger("posY");
				int tz = stack.getTagCompound().getInteger("posZ");
				console.setTarget(tx, ty, tz);
				player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".set").setStyle(new Style().setColor(TextFormatting.YELLOW)));
			}
			
			return true;
		}
		
		if(b == ModBlocks.rbmk_display && stack.hasTagCompound()) {
			
			if(!world.isRemote) {
				
				TileEntityRBMKDisplay console = (TileEntityRBMKDisplay) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
				int tx = stack.getTagCompound().getInteger("posX");
				int ty = stack.getTagCompound().getInteger("posY");
				int tz = stack.getTagCompound().getInteger("posZ");
				console.setTarget(tx, ty, tz);
				player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".set").setStyle(new Style().setColor(TextFormatting.YELLOW)));
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		
		for(String s : I18nUtil.resolveKeyArray(this.getTranslationKey() + ".desc"))
			list.add(TextFormatting.YELLOW + s);
	}

	@Override
	public Multimap getAttributeModifiers(ItemStack stack) {
		
		Multimap multimap = super.getAttributeModifiers(stack);
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", 2, 0));
		return multimap;
	}
}

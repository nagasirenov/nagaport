package com.hbm.items.tool;

import java.util.List;

import com.google.common.collect.Multimap;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.tileentity.machine.TileEntitySolarMirror;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemMirrorTool extends Item {
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fX, float fY, float fZ) {
		
		Block b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();
		
		if(b == ModBlocks.machine_solar_boiler) {
			
			int[] pos = ((BlockDummyable)b).findCore(world, x, y, z);
			
			if(pos != null && !world.isRemote) {
				
				if(!stack.hasTagCompound())
					stack.setTagCompound(new NBTTagCompound());

				stack.getTagCompound().setInteger("posX", pos[0]);
				stack.getTagCompound().setInteger("posY", pos[1] + 1);
				stack.getTagCompound().setInteger("posZ", pos[2]);
				
				player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".linked").setStyle(new Style().setColor(TextFormatting.YELLOW)));
			}
			
			return true;
		}
		
		if(b == ModBlocks.solar_mirror && stack.hasTagCompound()) {
			
			if(!world.isRemote) {
				TileEntitySolarMirror mirror = (TileEntitySolarMirror) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
				int tx = stack.getTagCompound().getInteger("posX");
				int ty = stack.getTagCompound().getInteger("posY");
				int tz = stack.getTagCompound().getInteger("posZ");

				boolean withinReach = new Vec3d(x - tx, y - ty, z - tz).length() <= 100;
				boolean withinAngle = (x - tx) * (x - tx) + (z - tz) * (z - tz) <= (y - ty) * (y - ty);
				
				if(!withinReach) player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".reach").setStyle(new Style().setColor(TextFormatting.RED)));
				else if(!withinAngle) player.sendMessage(new TextComponentTranslation(this.getTranslationKey() + ".angle").setStyle(new Style().setColor(TextFormatting.RED)));
				else mirror.setTarget(tx, ty, tz);
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

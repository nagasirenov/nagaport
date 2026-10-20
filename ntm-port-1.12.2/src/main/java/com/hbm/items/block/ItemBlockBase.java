package com.hbm.items.block;

import java.util.List;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;

public class ItemBlockBase extends ItemBlock {
	
	public ItemBlockBase(Block block) {
		super(block);
		
		if(block instanceof IBlockMulti) {
			this.setMaxDamage(0);
			this.setHasSubtypes(true);
		}
	}
	
	@Override
	public int getMetadata(int meta) {
		if(block instanceof IBlockMulti)
			return meta;
		else
			return super.getMetadata(meta);
	}
	
	@Override
	public String getTranslationKey(ItemStack stack) {
		
		if(block instanceof IBlockMulti) {
			return ((IBlockMulti) block).getUnlocalizedName(stack);

		} else {
			return super.getTranslationKey(stack);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if(block instanceof IBlockMulti) {
			String override = ((IBlockMulti) block).getOverrideDisplayName(stack);
			if(override != null) {
				return override;
			}
		}
		return ("" + I18nUtil.resolveKey(this.getTranslationKey(stack) + ".name")).trim();
	}
	
	/** Native tooltip entry; the existing NTM callbacks still require the viewing player.
	 * World may be null in recipe browsers; no player can be inferred from it.
	 * Preserve virtual dispatch to ItemBlockLore/Remap/BlastInfo's internal overload.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		addInformation(stack, Minecraft.getMinecraft().player, list, flag.isAdvanced());
	}

	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		
		if(block instanceof ITooltipProvider) {
			((ITooltipProvider) block).addInformation(stack, player, list, bool);
		}
		
		if(block instanceof IPersistentInfoProvider && stack.hasTagCompound() && stack.getTagCompound().hasKey(IPersistentNBT.NBT_PERSISTENT_KEY)) {
			NBTTagCompound data = stack.getTagCompound().getCompoundTag(IPersistentNBT.NBT_PERSISTENT_KEY);
			((IPersistentInfoProvider) block).addInformation(stack, data, player, list, bool);
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		
		if(block instanceof ITooltipProvider) {
			return ((ITooltipProvider) block).getRarity(stack);
		}
		
		return EnumRarity.COMMON;
	}

}

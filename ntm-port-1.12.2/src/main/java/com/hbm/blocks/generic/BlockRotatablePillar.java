package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

public class BlockRotatablePillar extends BlockRotatedPillar implements ITooltipProvider {

	@SideOnly(Side.CLIENT)
	protected IIcon iconSide;
	@SideOnly(Side.CLIENT)
	protected IIcon field_150164_N;

	private String textureTop;

	public BlockRotatablePillar(Material mat, String top) {
		super(mat);
		textureTop = top;
	}

	/** NTM construction hook for the native protected sound setter. */
	public BlockRotatablePillar withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {

		this.field_150164_N = reg.registerIcon(textureTop);
		// The side bind carries the legacy side texture; the registry name does not.
		String side = com.hbm.blocks.LegacyBlockTextures.texture(this);
		this.iconSide = reg.registerIcon(side != null ? side : this.getRegistryName().toString());
	}

	protected IIcon getSideIcon(int p_150163_1_) {
		return iconSide;
	}

	/** Legacy icon view for the native fallback models. */
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public IIcon getLegacySideIcon() {
		return iconSide;
	}

	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public IIcon getLegacyTopIcon() {
		return field_150164_N;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) { }

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		if(this == ModBlocks.block_schrabidium_cluster)
			return EnumRarity.RARE;

		return EnumRarity.COMMON;
	}
}

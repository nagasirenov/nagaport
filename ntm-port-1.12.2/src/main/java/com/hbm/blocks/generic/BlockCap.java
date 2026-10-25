package com.hbm.blocks.generic;

import java.util.Locale;
import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.util.EnumUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.item.Item;
import com.hbm.render.icon.IIcon;

public class BlockCap extends BlockEnumMulti {
	
	protected IIcon[] iconsTop;

	public BlockCap() {
		super(Material.IRON, EnumCapBlock.class, true, true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		
		Enum[] enums = theEnum.getEnumConstants();
		this.icons = new IIcon[enums.length];
		this.iconsTop = new IIcon[enums.length];
		
		for(int i = 0; i < icons.length; i++) {
			Enum num = enums[i];
			this.icons[i] = reg.registerIcon(this.getTextureMultiName(num));
			this.iconsTop[i] = reg.registerIcon(this.getTextureMultiName(num) + "_top");
		}
	}
	
	@Override public String getTextureMultiName(Enum num) { return this.getRegistryName().toString() + "_" + num.name().toLowerCase(Locale.US); }
	@Override public String getUnlocalizedMultiName(Enum num) { return super.getTranslationKey() + "_" + num.name().toLowerCase(Locale.US); }
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return side == 0 || side == 1 ? this.iconsTop[meta % this.iconsTop.length] : this.icons[meta % this.icons.length];
	}
	
	@Override
	public net.minecraft.item.Item getItemDropped(net.minecraft.block.state.IBlockState state, Random rand, int j) {
		int meta = state.getBlock().getMetaFromState(state);
		
		EnumCapBlock cap = EnumUtil.grabEnumSafely(EnumCapBlock.class, meta);

		if(cap == EnumCapBlock.NUKA) return ModItems.cap_nuka;
		if(cap == EnumCapBlock.QUANTUM) return ModItems.cap_quantum;
		if(cap == EnumCapBlock.SPARKLE) return ModItems.cap_sparkle;
		if(cap == EnumCapBlock.RAD) return ModItems.cap_rad;
		if(cap == EnumCapBlock.KORL) return ModItems.cap_korl;
		if(cap == EnumCapBlock.FRITZ) return ModItems.cap_fritz;

		return null;
	}

	@Override public int quantityDropped(Random rand) { return 128; }

	public static enum EnumCapBlock {
		NUKA, QUANTUM, SPARKLE, RAD, KORL, FRITZ
	}
}

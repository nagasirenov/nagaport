package com.hbm.blocks;

import com.hbm.util.EnumUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;

import java.util.Locale;

public class BlockEnumMulti extends BlockMulti {

	public Class<? extends Enum> theEnum;
	public boolean multiName;
	private boolean multiTexture;

	public BlockEnumMulti(Material mat, Class<? extends Enum> theEnum, boolean multiName, boolean multiTexture) {
		super(mat);
		this.theEnum = theEnum;
		this.multiName = multiName;
		this.multiTexture = multiTexture;
	}

	protected IIcon[] icons;

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {

		if(multiTexture) {
			Enum[] enums = theEnum.getEnumConstants();
			this.icons = new IIcon[enums.length];

			for(int i = 0; i < icons.length; i++) {
				Enum num = enums[i];
				this.icons[i] = reg.registerIcon(this.getTextureMultiName(num));
			}
		} else {
			// The original used the setBlockTextureName base, not the registry name.
			String base = com.hbm.blocks.LegacyBlockTextures.texture(this);
			if(base == null) base = this.getTextureName();
			if(base == null) base = this.getRegistryName().toString();
			this.blockIcon = reg.registerIcon(base);
		}
	}

	public String getUnlocalizedName(ItemStack stack) {

		if(this.multiName) {
			Enum num = EnumUtil.grabEnumSafely(this.theEnum, stack.getItemDamage());
			return getUnlocalizedMultiName(num);
		}

		return this.getTranslationKey();
	}

	public String getTextureMultiName(Enum num) {
		return this.getRegistryName().toString() + "." + num.name().toLowerCase(Locale.US);
	}

	public String getUnlocalizedMultiName(Enum num) {
		return super.getTranslationKey() + "." + num.name().toLowerCase(Locale.US);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return multiTexture ? this.icons[meta % this.icons.length] : this.blockIcon;
	}

	@Override
	public int getSubCount() {
		return this.theEnum.getEnumConstants().length;
	}
}

package com.hbm.items.special;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.lib.RefStrings;
import com.hbm.render.icon.RGBMutatorInterpolatedComponentRemap;
import com.hbm.render.icon.TextureAtlasSpriteMutatable;
import com.hbm.render.icon.LegacyIconRegister;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.ResourceLocation;

public class ItemAutogen extends Item {

	MaterialShapes shape;

	private HashMap<NTMMaterial, String> textureOverrides = new HashMap();
	private HashMap<NTMMaterial, IIcon> iconMap = new HashMap();
	private String overrideUnlocalizedName = null;
	@SideOnly(Side.CLIENT)
	private IIcon defaultIcon;

	public ItemAutogen(MaterialShapes shape) {
		this.setHasSubtypes(true);
		this.shape = shape;
	}

	/** add override texture */
	public ItemAutogen aot(NTMMaterial mat, String tex) {
		textureOverrides.put(mat, tex);
		return this;
	}
	public ItemAutogen oun(String overrideUnlocalizedName) {
		this.overrideUnlocalizedName = overrideUnlocalizedName;
		return this;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		if(!(reg instanceof LegacyIconRegister)) {
			throw new IllegalArgumentException("ItemAutogen requires the NTM texture-atlas adapter");
		}
		LegacyIconRegister adapter = (LegacyIconRegister) reg;
		ResourceLocation itemName = getRegistryName();
		if(itemName == null) throw new IllegalStateException("ItemAutogen must be registered before its icon callback");
		// The original built icon names from the setTextureName base, not the
		// registry name; the declared legacy texture carries that base.
		String legacyBase = com.hbm.render.item.NativeItemTextureRegistry.texture(this);
		if(legacyBase == null) legacyBase = itemName.toString();
		defaultIcon = adapter.registerIcon(legacyBase);
		iconMap.clear();

		for(NTMMaterial mat : Mats.orderedList) {
			if(!textureOverrides.containsKey(mat) && mat.solidColorLight != mat.solidColorDark && (shape == null || mat.autogen.contains(shape))) { //only generate icons if there is no override, color variation is available and if the icon will actually be used
				String placeholderName = legacyBase + "-" + mat.names[0]; //the suffix keeps material-specific atlas entries distinct
				TextureAtlasSpriteMutatable mutableIcon = new TextureAtlasSpriteMutatable(placeholderName, new RGBMutatorInterpolatedComponentRemap(0xFFFFFF, 0x505050, mat.solidColorLight, mat.solidColorDark));
				iconMap.put(mat, adapter.registerGeneratedSprite(mutableIcon));
			}
		}

		for(Entry<NTMMaterial, String> tex : textureOverrides.entrySet()) {
			iconMap.put(tex.getKey(), reg.registerIcon(RefStrings.MODID + ":" + tex.getValue()));
		}
	}


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void getSubItems(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {
		if(this.isInCreativeTab(tab)) {
			getSubItems(this, tab, list);
		}
	}

@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {

		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.autogen.contains(this.shape)) {
				list.add(new ItemStack(item, 1, mat.id));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {

		NTMMaterial mat = Mats.matById.get(meta);

		if(mat != null) {
			IIcon override = iconMap.get(mat);
			if(override != null) {
				return override;
			}
		}

		return this.defaultIcon;
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int layer) {

		if(this.getIconFromDamage(stack.getItemDamage()) != this.defaultIcon) {
			return 0xffffff; //custom textures don't need tints
		}

		NTMMaterial mat = Mats.matById.get(stack.getItemDamage());

		if(mat != null) {
			return mat.moltenColor;
		}

		return 0xffffff;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		NTMMaterial mat = Mats.matById.get(stack.getItemDamage());

		if(mat == null) {
			return "UNDEFINED";
		}

		String matName = I18nUtil.resolveKey(mat.getUnlocalizedName());
		return I18nUtil.format(this.getUnlocalizedNameInefficiently(stack) + ".name", matName);
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		return overrideUnlocalizedName != null ? "item." + overrideUnlocalizedName : super.getTranslationKey(stack);
	}
}

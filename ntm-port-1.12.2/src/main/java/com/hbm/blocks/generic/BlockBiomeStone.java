package com.hbm.blocks.generic;

import java.util.Locale;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.BlockEnums.EnumBiomeType;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockBiomeStone extends BlockEnumMulti {

	public BlockBiomeStone() {
		super(Material.ROCK, EnumBiomeType.class, true, true);
	}

	protected IIcon[] iconsTop;
	protected IIcon[] iconsLayer;

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {

		Enum[] enums = theEnum.getEnumConstants();
		this.icons = new IIcon[enums.length];
		this.iconsTop = new IIcon[enums.length];
		this.iconsLayer = new IIcon[enums.length];

		for(int i = 0; i < icons.length; i++) {
			Enum num = enums[i];
			this.icons[i] = reg.registerIcon(this.getRegistryName().toString() + "." + num.name().toLowerCase(Locale.US));
			this.iconsTop[i] = reg.registerIcon(this.getRegistryName().toString() + "_top." + num.name().toLowerCase(Locale.US));
			this.iconsLayer[i] = reg.registerIcon(this.getRegistryName().toString() + "_layer." + num.name().toLowerCase(Locale.US));
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {

		int meta = com.hbm.world.WorldBlockState.readMetadata(world, x, y, z);
		if(side == 0) return this.iconsTop[meta % this.icons.length];
		if(side == 1) return this.iconsTop[meta % this.icons.length];

		if(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() == this && com.hbm.world.WorldBlockState.readMetadata(world, x, y + 1, z) == meta) {
			return this.getIcon(side, meta);
		} else {
			return this.iconsLayer[meta % this.icons.length];
		}
	}
}

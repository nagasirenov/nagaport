package com.hbm.blocks.generic;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockMultiSlabMeta extends BlockMultiSlab {

	public int[] metas;

	public BlockMultiSlabMeta(Block single, Material mat, Block[] slabMaterials, int...metas) {
		super(single != null, mat);
		this.single = single;
		this.slabMaterials = slabMaterials;
		this.metas = new int[slabMaterials.length]; //initialized to 0s
		for(int i = 0; i < metas.length; i++) {
			this.metas[i] = metas[i];
		}

		this.useNeighborBrightness = true;

		if(single == null) {
			for(int i = 0; i < slabMaterials.length; i++) {
				recipeGen.add(new Object[] {slabMaterials[i], metas[i], this, i});
			}
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public com.hbm.render.icon.IIcon getIcon(int side, int meta) {
		meta = (meta & 7) % slabMaterials.length;
		Block block = slabMaterials[meta];
		return getLegacyIcon(block, side, metas[meta]);
	}

	@Override
	public String getTranslationKey(int meta) {
		meta = (meta & 7) % slabMaterials.length;
		return super.getTranslationKey(meta) + "." + metas[meta];
	}

	@Override
	protected int getMaterialMetadata(int index) {
		return metas[index];
	}
}

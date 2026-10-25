package com.hbm.blocks.generic;

import java.awt.Color;
import com.hbm.blocks.BlockFallingNT;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockFallingTint extends BlockFallingNT {
	@Override
	public net.minecraft.block.Block setSoundType(net.minecraft.block.SoundType sound) {
		return super.setSoundType(sound);
	}

    public static final PropertyInteger TEXTURE = PropertyInteger.create("texture", 0, 3);
    private final String[] textureNames;
    public BlockFallingTint(Material mat) { this(mat, new String[0]); }
    public BlockFallingTint(Material mat, String... extraTextures) { super(mat); textureNames = extraTextures.clone(); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, META, TEXTURE); }
    @Override public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) { return state.withProperty(TEXTURE, textureIndex(pos)); }
    public static int textureIndex(BlockPos pos) {
        long l = (long)(pos.getX() * 3129871) ^ (long)pos.getY() * 116129781L ^ (long)pos.getZ();
        l = l * l * 42317861L + l * 11L;
        return (int)(l >> 16 & 3L);
    }
    public String[] getExtraTextures() { return textureNames.clone(); }
    public static int color(int meta) { return Color.HSBtoRGB(0F, 0F, 1F - meta / 15F); }
}

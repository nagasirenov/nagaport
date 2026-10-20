package com.hbm.blocks.machine;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;

/** Native state/render contract for the single-block thermal furnace family. */
public abstract class BlockThermalFurnace extends BlockContainer {
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    protected final boolean isActive;
    protected BlockThermalFurnace(Material material, boolean active) {
        super(material);
        isActive = active;
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }
    public final boolean isActive() { return isActive; }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
    @Override public IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(METADATA, metadata); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
}

package com.hbm.blocks.generic;

import com.hbm.blocks.BlockMulti;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Native state stores the original variant * 4 + yaw metadata unchanged. */
public class BlockDecoToaster extends BlockMulti implements INBTBlockTransformable {
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    public BlockDecoToaster(Material material) { super(material); setDefaultState(blockState.getBaseState().withProperty(META, 0)); }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, META); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(META, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(META); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.block.state.BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, net.minecraft.util.EnumFacing side) {
        return net.minecraft.block.state.BlockFaceShape.UNDEFINED;
    }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public int damageDropped(IBlockState state) { return damageDropped(getMetaFromState(state)); }
    @Override public int damageDropped(int meta) { return (Math.abs(meta) % 12) / 4; }
    @Override public int getSubCount() { return 3; }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        int yaw = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + .5D) & 3;
        world.setBlockState(pos, getStateFromMeta(stack.getItemDamage() * 4 + yaw), 2);
    }
    @Override public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaDecoModel(meta, coordBaseMode);
    }

    private static final AxisAlignedBB EAST_WEST = new AxisAlignedBB(.25F, 0, .375F, .75F, .325F, .625F);
    private static final AxisAlignedBB NORTH_SOUTH = new AxisAlignedBB(.375F, 0, .25F, .625F, .325F, .75F);
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return (getMetaFromState(state) & 1) == 0 ? EAST_WEST : NORTH_SOUTH;
    }

}

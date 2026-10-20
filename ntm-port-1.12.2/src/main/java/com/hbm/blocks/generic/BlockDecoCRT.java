package com.hbm.blocks.generic;

import com.hbm.blocks.BlockMulti;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Four original CRT variants: persisted metadata is variant * 4 + placement yaw. */
public class BlockDecoCRT extends BlockMulti implements INBTBlockTransformable {
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    public BlockDecoCRT(Material material) {
        super(material);
        setDefaultState(blockState.getBaseState().withProperty(META, 0));
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, META); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(META, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(META); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
        return BlockFaceShape.UNDEFINED;
    }
    @Override public int damageDropped(int meta) { return (Math.abs(meta) % 16) / 4; }
    @Override public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(this, 1, damageDropped(state));
    }
    @Override public ItemStack getPickBlock(IBlockState state, RayTraceResult hit, World world, BlockPos pos, EntityPlayer player) {
        return getItem(world, pos, state);
    }
    @Override public int getSubCount() { return 4; }
    @Override public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        int yaw = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + .5D) & 3;
        world.setBlockState(pos, getStateFromMeta(stack.getItemDamage() * 4 + yaw), 2);
    }
    @Override public int transformMeta(int meta, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaDecoModel(meta, coordBaseMode);
    }
}

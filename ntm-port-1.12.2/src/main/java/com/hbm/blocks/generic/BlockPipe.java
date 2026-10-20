package com.hbm.blocks.generic;

import java.util.List;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Decorative pipe metadata retains the original low two bits and pillar-axis bits. */
public class BlockPipe extends Block implements ITooltipProvider, INBTBlockTransformable {
    private final String sideString;
    public int rType;
    public BlockPipe(Material material, String texture, int renderType) {
        super(material);
        sideString = texture;
        rType = renderType;
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
        setSoundType(ModSoundTypes.grate);
    }
    public String getSideTexture() { return sideString; }
    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
    @Override public IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(METADATA, metadata & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) { return BlockFaceShape.UNDEFINED; }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, int metadata, EntityLivingBase placer, EnumHand hand) {
        int axis = side.getAxis() == EnumFacing.Axis.Y ? 0 : side.getAxis() == EnumFacing.Axis.Z ? 8 : 4;
        return getStateFromMeta((metadata & 3) | axis);
    }
    @Override public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean extended) {
        list.add("Purely decorative");
    }
    @Override public int transformMeta(int metadata, int coordBaseMode) {
        return INBTBlockTransformable.transformMetaPillar(metadata, coordBaseMode);
    }
}

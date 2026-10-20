package com.hbm.blocks.generic;

import java.util.List;
import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Native state retains the original post metadata and the original connection predicate. */
public class BlockMetalFence extends BlockFence implements IBlockMulti {
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    public BlockMetalFence(Material material) {
        super(material, material.getMaterialMapColor());
        setDefaultState(blockState.getBaseState().withProperty(META, 0).withProperty(NORTH, false)
                .withProperty(SOUTH, false).withProperty(WEST, false).withProperty(EAST, false));
    }
    @Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, META, NORTH, SOUTH, WEST, EAST); }
    @Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(META, meta & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(META); }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public int damageDropped(IBlockState state) { return rectify(getMetaFromState(state)); }
    public int damageDropped(int meta) { return rectify(meta); }
    @Override public String getUnlocalizedName(ItemStack stack) {
        return stack.getItemDamage() == 1 ? getTranslationKey() + "_post" : getTranslationKey();
    }
    public boolean canConnectFenceTo(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if(block == ModBlocks.fence_gate) return true;
        return block == this || block == Blocks.OAK_FENCE_GATE ||
                state.getMaterial().isOpaque() && state.isFullCube() && state.getMaterial() != Material.GOURD;
    }
    /** Keep the native inherited selection/rotation and lead paths, but not its changed connection rules. */
    @Override public boolean canConnectTo(IBlockAccess world, BlockPos pos, EnumFacing side) { return canConnectFenceTo(world, pos); }
    @Override public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(NORTH, canConnectFenceTo(world, pos.north()))
                .withProperty(SOUTH, canConnectFenceTo(world, pos.south()))
                .withProperty(WEST, canConnectFenceTo(world, pos.west()))
                .withProperty(EAST, canConnectFenceTo(world, pos.east()));
    }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = getActualState(state, world, pos);
        return new AxisAlignedBB(state.getValue(WEST) ? 0 : .375, 0, state.getValue(NORTH) ? 0 : .375,
                state.getValue(EAST) ? 1 : .625, 1, state.getValue(SOUTH) ? 1 : .625);
    }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        AxisAlignedBB box = getBoundingBox(state, world, pos);
        return new AxisAlignedBB(box.minX, 0, box.minZ, box.maxX, 1, box.maxZ);
    }
    @Override public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
            AxisAlignedBB entityBox, List<AxisAlignedBB> list, Entity entity, boolean actualState) {
        boolean north = canConnectFenceTo(world, pos.north()), south = canConnectFenceTo(world, pos.south());
        boolean west = canConnectFenceTo(world, pos.west()), east = canConnectFenceTo(world, pos.east());
        // Unlike vanilla fence, the original override sets maxY=1 and calls
        // the inherited Block collision-box accessor, not vanilla addCollisionBoxes.
        if(north || south) addCollisionBoxToList(pos, entityBox, list,
                new AxisAlignedBB(.375, 0, north ? 0 : .375, .625, 1, south ? 1 : .625));
        if(west || east || !north && !south) addCollisionBoxToList(pos, entityBox, list,
                new AxisAlignedBB(west ? 0 : .375, 0, .375, east ? 1 : .625, 1, .625));
    }
    @Override public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for(int i = 0; i < getSubCount(); i++) list.add(new ItemStack(this, 1, i));
    }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            net.minecraft.entity.player.EntityPlayer player, net.minecraft.util.EnumHand hand,
            EnumFacing side, float hitX, float hitY, float hitZ) {
        return world.isRemote || net.minecraft.item.ItemLead.attachToFence(player, world, pos);
    }
    @Override public int getSubCount() { return 2; }
}

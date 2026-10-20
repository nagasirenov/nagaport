package com.hbm.blocks.network;

import com.hbm.blocks.IBlockMulti;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.network.TileEntityCableBaseNT;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class PowerCableBox extends BlockContainer implements IBlockMulti {

    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);

    public PowerCableBox(Material material) {
        super(material);
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityCableBaseNT();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty<?>[] {METADATA},
                new IUnlistedProperty<?>[] {ConnectionState.CONNECTIONS});
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        // Preserve every persisted nibble, including metadata outside the creative variants.
        return getDefaultState().withProperty(METADATA, metadata & 15);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(METADATA);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return ((IExtendedBlockState) state)
                .withProperty(ConnectionState.CONNECTIONS, connections(world, pos, tile));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for(int metadata = 0; metadata < 5; metadata++) {
            items.add(new ItemStack(this, 1, metadata));
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state) % 5;
    }

    @Override
    public boolean hasCustomBreakingProgress(IBlockState state) {
        return BoxDuctRenderState.family(this) >= 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        // Original render pass 0 retained binary alpha testing and terrain mipmaps.
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public int getSubCount() {
        return 1;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
            List<AxisAlignedBB> boxes, Entity entity, boolean actualState) {
        int mask = connections(world, pos, world.getTileEntity(pos));
        for(AxisAlignedBB box : collisionBoxes(getMetaFromState(state), mask)) {
            addCollisionBoxToList(pos, entityBox, boxes, box);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return bounds(getMetaFromState(state), connections(world, pos, world.getTileEntity(pos)));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        return getBoundingBox(state, world, pos).offset(pos);
    }

    /** Local-coordinate collision boxes in the original center, +X, -X, +Y, -Y, +Z, -Z order. */
    public static List<AxisAlignedBB> collisionBoxes(int metadata, int mask) {
        List<AxisAlignedBB> boxes = new ArrayList<>();
        double lower = lowerBound(metadata), upper = 1D - lower;
        if(mask == 0) {
            boxes.add(new AxisAlignedBB(lower, lower, lower, upper, upper, upper));
        } else if((mask & 0b001111) == 0) {
            boxes.add(new AxisAlignedBB(0, lower, lower, 1, upper, upper));
        } else if((mask & 0b110011) == 0) {
            boxes.add(new AxisAlignedBB(lower, 0, lower, upper, 1, upper));
        } else if((mask & 0b111100) == 0) {
            boxes.add(new AxisAlignedBB(lower, lower, 0, upper, upper, 1));
        } else {
            boxes.add(new AxisAlignedBB(lower, lower, lower, upper, upper, upper));
            if((mask & ConnectionState.POS_X) != 0) boxes.add(new AxisAlignedBB(upper, lower, lower, 1, upper, upper));
            if((mask & ConnectionState.NEG_X) != 0) boxes.add(new AxisAlignedBB(0, lower, lower, lower, upper, upper));
            if((mask & ConnectionState.POS_Y) != 0) boxes.add(new AxisAlignedBB(lower, upper, lower, upper, 1, upper));
            if((mask & ConnectionState.NEG_Y) != 0) boxes.add(new AxisAlignedBB(lower, 0, lower, upper, lower, upper));
            if((mask & ConnectionState.POS_Z) != 0) boxes.add(new AxisAlignedBB(lower, lower, upper, upper, upper, 1));
            if((mask & ConnectionState.NEG_Z) != 0) boxes.add(new AxisAlignedBB(lower, lower, 0, upper, upper, lower));
        }
        return boxes;
    }

    /** Original merged selection/collision envelope; compound collision remains separate above. */
    public static AxisAlignedBB bounds(int metadata, int mask) {
        double lower = lowerBound(metadata), upper = 1D - lower;
        if(mask == 0) return new AxisAlignedBB(lower, lower, lower, upper, upper, upper);
        if((mask & 0b001111) == 0) return new AxisAlignedBB(0, lower, lower, 1, upper, upper);
        if((mask & 0b110011) == 0) return new AxisAlignedBB(lower, 0, lower, upper, 1, upper);
        if((mask & 0b111100) == 0) return new AxisAlignedBB(lower, lower, 0, upper, upper, 1);
        return new AxisAlignedBB(
                (mask & ConnectionState.NEG_X) != 0 ? 0 : lower,
                (mask & ConnectionState.NEG_Y) != 0 ? 0 : lower,
                (mask & ConnectionState.NEG_Z) != 0 ? 0 : lower,
                (mask & ConnectionState.POS_X) != 0 ? 1 : upper,
                (mask & ConnectionState.POS_Y) != 0 ? 1 : upper,
                (mask & ConnectionState.POS_Z) != 0 ? 1 : upper);
    }

    private static double lowerBound(int metadata) {
        double lower = 0.125D;
        for(int i = 0; i < 5; i++) {
            if(metadata > i) lower += 0.0625D;
        }
        return lower;
    }

    private int connections(IBlockAccess world, BlockPos pos, TileEntity tile) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        // Preserve the owners' directional predicate order and the legacy mask layout.
        boolean nX = canConnectTo(world, x, y, z, Library.NEG_X, tile);
        boolean pX = canConnectTo(world, x, y, z, Library.POS_X, tile);
        boolean nY = canConnectTo(world, x, y, z, Library.NEG_Y, tile);
        boolean pY = canConnectTo(world, x, y, z, Library.POS_Y, tile);
        boolean nZ = canConnectTo(world, x, y, z, Library.NEG_Z, tile);
        boolean pZ = canConnectTo(world, x, y, z, Library.POS_Z, tile);
        return (pX ? ConnectionState.POS_X : 0) | (nX ? ConnectionState.NEG_X : 0)
             | (pY ? ConnectionState.POS_Y : 0) | (nY ? ConnectionState.NEG_Y : 0)
             | (pZ ? ConnectionState.POS_Z : 0) | (nZ ? ConnectionState.NEG_Z : 0);
    }

    public boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection direction, TileEntity tile) {
        if(tile instanceof TileEntityCableBaseNT) {
            return Library.canConnect(world, x + direction.offsetX, y + direction.offsetY,
                    z + direction.offsetZ, direction);
        }
        return false;
    }
}

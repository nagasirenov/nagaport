package com.hbm.blocks.network;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class FluidDuctStandard extends FluidDuctBase implements IBlockMulti, ILookOverlay {

    public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
    private static final double MIN = 5D / 16D, MAX = 11D / 16D;
    private static final AxisAlignedBB CORE = new AxisAlignedBB(MIN, MIN, MIN, MAX, MAX, MAX);
    private static final AxisAlignedBB X_AXIS = new AxisAlignedBB(0, MIN, MIN, 1, MAX, MAX);
    private static final AxisAlignedBB Y_AXIS = new AxisAlignedBB(MIN, 0, MIN, MAX, 1, MAX);
    private static final AxisAlignedBB Z_AXIS = new AxisAlignedBB(MIN, MIN, 0, MAX, MAX, 1);
    private static final AxisAlignedBB[] ARMS = {
        new AxisAlignedBB(MAX, MIN, MIN, 1, MAX, MAX),
        new AxisAlignedBB(0, MIN, MIN, MIN, MAX, MAX),
        new AxisAlignedBB(MIN, MAX, MIN, MAX, 1, MAX),
        new AxisAlignedBB(MIN, 0, MIN, MAX, MIN, MAX),
        new AxisAlignedBB(MIN, MIN, MAX, MAX, MAX, 1),
        new AxisAlignedBB(MIN, MIN, 0, MAX, MAX, MIN)
    };

    public FluidDuctStandard(Material material) {
        super(material);
        setSoundType(ModSoundTypes.pipe);
        setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty<?>[] {METADATA},
                new IUnlistedProperty<?>[] {ConnectionState.CONNECTIONS, FluidConnectionState.FLUID_COLOR});
    }

    @Override
    public IBlockState getStateFromMeta(int metadata) {
        return getDefaultState().withProperty(METADATA, metadata & 15);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(METADATA);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        FluidType type = Fluids.NONE;
        int color = 0xff00ff;
        if(tile instanceof TileEntityPipeBaseNT) {
            type = ((TileEntityPipeBaseNT) tile).getType();
            color = type.getColor();
        }
        return ((IExtendedBlockState) state)
                .withProperty(ConnectionState.CONNECTIONS, connections(world, pos, type))
                .withProperty(FluidConnectionState.FLUID_COLOR, color);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for(int metadata = 0; metadata < getSubCount(); metadata++) {
            items.add(new ItemStack(this, 1, metadata));
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return rectify(getMetaFromState(state));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityPipeBaseNT) {
            FluidType type = ((TileEntityPipeBaseNT) tile).getType();
            return new ItemStack(ModItems.fluid_duct, 1, type.getID());
        }
        return super.getPickBlock(state, target, world, pos, player);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
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
    public int getSubCount() {
        return 3;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox,
            List<AxisAlignedBB> boxes, Entity entity, boolean actualState) {
        TileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof TileEntityPipeBaseNT)) return;
        int mask = connections(world, pos, ((TileEntityPipeBaseNT) tile).getType());
        if(mask == 0) {
            // An isolated original duct has all six arms, but no center collision box.
            for(AxisAlignedBB arm : ARMS) addCollisionBoxToList(pos, entityBox, boxes, arm);
        } else if((mask & 0b001111) == 0) {
            addCollisionBoxToList(pos, entityBox, boxes, X_AXIS);
        } else if((mask & 0b110011) == 0) {
            addCollisionBoxToList(pos, entityBox, boxes, Y_AXIS);
        } else if((mask & 0b111100) == 0) {
            addCollisionBoxToList(pos, entityBox, boxes, Z_AXIS);
        } else {
            addCollisionBoxToList(pos, entityBox, boxes, CORE);
            for(int i = 0; i < ARMS.length; i++) {
                if((mask & (32 >> i)) != 0) addCollisionBoxToList(pos, entityBox, boxes, ARMS[i]);
            }
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEntityPipeBaseNT
                ? bounds(connections(world, pos, ((TileEntityPipeBaseNT) tile).getType())) : NULL_AABB;
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        // The original left shared mutable bounds stale when the tile was missing.
        // Keep the block selectable, without inventing a physical collision shape.
        return tile instanceof TileEntityPipeBaseNT
                ? bounds(connections(world, pos, ((TileEntityPipeBaseNT) tile).getType())) : FULL_BLOCK_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        return getBoundingBox(state, world, pos).offset(pos);
    }

    public static AxisAlignedBB bounds(int mask) {
        if(mask == 0) return FULL_BLOCK_AABB;
        if((mask & 0b001111) == 0) return X_AXIS;
        if((mask & 0b110011) == 0) return Y_AXIS;
        if((mask & 0b111100) == 0) return Z_AXIS;
        return new AxisAlignedBB(
                (mask & ConnectionState.NEG_X) != 0 ? 0 : MIN,
                (mask & ConnectionState.NEG_Y) != 0 ? 0 : MIN,
                (mask & ConnectionState.NEG_Z) != 0 ? 0 : MIN,
                (mask & ConnectionState.POS_X) != 0 ? 1 : MAX,
                (mask & ConnectionState.POS_Y) != 0 ? 1 : MAX,
                (mask & ConnectionState.POS_Z) != 0 ? 1 : MAX);
    }

    private int connections(IBlockAccess world, BlockPos pos, FluidType type) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        return (canConnectTo(world, x, y, z, Library.POS_X, type) ? ConnectionState.POS_X : 0)
             | (canConnectTo(world, x, y, z, Library.NEG_X, type) ? ConnectionState.NEG_X : 0)
             | (canConnectTo(world, x, y, z, Library.POS_Y, type) ? ConnectionState.POS_Y : 0)
             | (canConnectTo(world, x, y, z, Library.NEG_Y, type) ? ConnectionState.NEG_Y : 0)
             | (canConnectTo(world, x, y, z, Library.POS_Z, type) ? ConnectionState.POS_Z : 0)
             | (canConnectTo(world, x, y, z, Library.NEG_Z, type) ? ConnectionState.NEG_Z : 0);
    }

    public boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection direction, FluidType type) {
        return Library.canConnectFluid(world, x + direction.offsetX, y + direction.offsetY,
                z + direction.offsetZ, direction, type);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void printHook(Pre event, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if(!(tile instanceof TileEntityPipeBaseNT)) return;
        TileEntityPipeBaseNT duct = (TileEntityPipeBaseNT) tile;
        List<String> text = new ArrayList<>();
        text.add("&[" + duct.getType().getColor() + "&]" + duct.getType().getLocalizedName());
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
}

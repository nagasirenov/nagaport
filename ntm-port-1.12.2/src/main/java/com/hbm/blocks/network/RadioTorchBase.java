package com.hbm.blocks.network;

import java.util.List;
import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.main.MainRegistry;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.RefStrings;
import com.hbm.render.icon.IIconRegister;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.network.TileEntityRadioTorchBase;
import com.hbm.tileentity.network.TileEntityRadioTorchLogic;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Native common owner for the original torch-like RTTY family. */
public abstract class RadioTorchBase extends BlockBase implements ITileEntityProvider, IGUIProvider, ILookOverlay, ITooltipProvider {
    private static final String[] MODEL_NAMES = {"sender", "receiver", "counter", "logic", "reader", "controller"};
    private static final Class<?>[] MODEL_OWNERS = {RadioTorchSender.class, RadioTorchReceiver.class, RadioTorchCounter.class,
            RadioTorchLogic.class, RadioTorchReader.class, RadioTorchController.class};
    private static final Class<?>[] TILE_OWNERS = {com.hbm.tileentity.network.TileEntityRadioTorchSender.class,
            com.hbm.tileentity.network.TileEntityRadioTorchReceiver.class, com.hbm.tileentity.network.TileEntityRadioTorchCounter.class,
            com.hbm.tileentity.network.TileEntityRadioTorchLogic.class, com.hbm.tileentity.network.TileEntityRadioTorchReader.class,
            com.hbm.tileentity.network.TileEntityRadioTorchController.class};
    private static int nativeFamily(Block block) {
        if(block == null || block.getRegistryName() == null || !RefStrings.MODID.equals(block.getRegistryName().getNamespace())) return -1;
        for(int i = 0; i < MODEL_NAMES.length; i++) if(block.getClass() == MODEL_OWNERS[i]
                && block.getRegistryName().getPath().equals("tile.radio_torch_" + MODEL_NAMES[i])) return i;
        return -1;
    }
    public static boolean isNativeModelOwner(Block block) { return nativeFamily(block) >= 0; }
    public static boolean isNativeBreakingOwner(TileEntity tile, IBlockState state) {
        int family = state == null ? -1 : nativeFamily(state.getBlock());
        return family >= 0 && tile != null && tile.getClass() == TILE_OWNERS[family];
    }
    @Override public boolean hasCustomBreakingProgress(IBlockState state) { return isNativeModelOwner(this); }
    private final String iconName;
    public RadioTorchBase() { this("rtty_programmer"); }
    protected RadioTorchBase(String iconName) {
        super(Material.CIRCUITS);
        this.iconName = iconName;
        setDefaultState(blockState.getBaseState().withProperty(RadioTorchModelState.META, 0));
    }
    @Override protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty<?>[]{RadioTorchModelState.META},
                new IUnlistedProperty<?>[]{RadioTorchModelState.ACTIVE});
    }
    @Override public IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(RadioTorchModelState.META, metadata & 15); }
    @Override public int getMetaFromState(IBlockState state) { return state.getValue(RadioTorchModelState.META); }
    @Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        // Read the tile only while building the snapshot, never from a cached baked model.
        TileEntity tile = world.getTileEntity(pos);
        int signal = tile instanceof TileEntityRadioTorchBase ? ((TileEntityRadioTorchBase)tile).lastState
                : tile instanceof TileEntityRadioTorchLogic ? ((TileEntityRadioTorchLogic)tile).lastState : 0;
        return ((IExtendedBlockState)state).withProperty(RadioTorchModelState.ACTIVE, signal > 0);
    }
    @Override public boolean hasTileEntity(IBlockState state) { return true; }
    @Override public TileEntity createTileEntity(World world, IBlockState state) { return createNewTileEntity(world, getMetaFromState(state)); }
    @Override public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int parameter) {
        super.eventReceived(state, world, pos, id, parameter);
        TileEntity tile = world.getTileEntity(pos);
        return tile != null && tile.receiveClientEvent(id, parameter);
    }
    @Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
    @Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT; }
    @Override public boolean isOpaqueCube(IBlockState state) { return false; }
    @Override public boolean isFullCube(IBlockState state) { return false; }
    @Override public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) { return true; }
    @Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) { return BlockFaceShape.UNDEFINED; }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return NULL_AABB; }
    @Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        ForgeDirection dir = ForgeDirection.getOrientation(getMetaFromState(state) & 7);
        return new AxisAlignedBB(dir.offsetX == 1 ? 0F : .375F, dir.offsetY == 1 ? 0F : .375F, dir.offsetZ == 1 ? 0F : .375F,
                dir.offsetX == -1 ? 1F : .625F, dir.offsetY == -1 ? 1F : .625F, dir.offsetZ == -1 ? 1F : .625F);
    }
    @Override public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int metadata, EntityLivingBase placer) {
        return getStateFromMeta(side.getIndex());
    }
    @Override public void neighborChanged(IBlockState state, World world, BlockPos pos, Block changed, BlockPos fromPos) {
        ForgeDirection dir = ForgeDirection.getOrientation(getMetaFromState(state));
        Block support = world.getBlockState(pos.add(-dir.offsetX, -dir.offsetY, -dir.offsetZ)).getBlock();
        if(!canBlockStay(world, pos.getX(), pos.getY(), pos.getZ(), dir, support)) {
            dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }
    @Override public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        if(!super.canPlaceBlockOnSide(world, pos, side)) return false;
        ForgeDirection dir = ForgeDirection.getOrientation(side.getIndex());
        Block support = world.getBlockState(pos.offset(side.getOpposite())).getBlock();
        return canBlockStay(world, pos.getX(), pos.getY(), pos.getZ(), dir, support);
    }
    public boolean canBlockStay(World world, int x, int y, int z, ForgeDirection dir, Block block) {
        BlockPos support = new BlockPos(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ);
        IBlockState state = world.getBlockState(support);
        // Preserve the original unusual air query at the torch position, not the support.
        return com.hbm.lib.FacingCompat.isBlockSideSolid(world, support.getX(), support.getY(), support.getZ(), dir)
                || block.hasComparatorInputOverride(state) || state.canProvidePower()
                || (state.isFullCube() && !block.isAir(state, world, new BlockPos(x, y, z)));
    }
    @Override public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(world.isRemote && !player.isSneaking()) {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return !player.isSneaking();
    }
    @Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return null; }
    @Override public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) { addStandardInfo(stack, player, list, ext); }
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister register) { blockIcon = register.registerIcon(RefStrings.MODID + ":" + iconName); }
}

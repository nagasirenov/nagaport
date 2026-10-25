package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.ModItems;
import com.hbm.tileentity.network.TileEntityDroneWaypoint;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class DroneWaypoint extends BlockContainer implements ILookOverlay, ITooltipProvider {

	public DroneWaypoint() {
		super(Material.CIRCUITS);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDroneWaypoint();
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, net.minecraft.util.EnumFacing side) {
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, pos.getX(), pos.getY(), pos.getZ())) & 7;
		ForgeDirection dir = ForgeDirection.getOrientation(meta);
		return new AxisAlignedBB(
				dir.offsetX == 1 ? 0D : 0.375D,
				dir.offsetY == 1 ? 0D : 0.375D,
				dir.offsetZ == 1 ? 0D : 0.375D,
				dir.offsetX == -1 ? 1D : 0.625D,
				dir.offsetY == -1 ? 1D : 0.625D,
				dir.offsetZ == -1 ? 1D : 0.625D
		);
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d vec0, Vec3d vec1) {
		return this.rayTrace(pos, vec0, vec1, getBoundingBox(state, world, pos));
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ, int meta, net.minecraft.entity.EntityLivingBase placer) {
		return this.getStateFromMeta(facing.getIndex());
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		ForgeDirection dir = ForgeDirection.getOrientation(meta);
		net.minecraft.block.state.IBlockState nativeAirState = com.hbm.world.WorldBlockState.read(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ);
		Block b = nativeAirState.getBlock();

		if(!com.hbm.lib.FacingCompat.isBlockSideSolid(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir) && (!nativeAirState.isNormalCube() || b.isAir(nativeAirState, world, pos))) {
			this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), 0);
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		}
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, net.minecraft.util.EnumFacing side) {
		if(!super.canPlaceBlockOnSide(world, pos, side)) return false;

		ForgeDirection dir = ForgeDirection.getOrientation(side.getIndex());
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		net.minecraft.block.state.IBlockState nativeAirState = com.hbm.world.WorldBlockState.read(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ);
		Block b = nativeAirState.getBlock();

		return com.hbm.lib.FacingCompat.isBlockSideSolid(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir) || (nativeAirState.isNormalCube() && !b.isAir(nativeAirState, world, pos));
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.drone_linker) return false;

		if(world.isRemote) return true;

		TileEntityDroneWaypoint tile = (TileEntityDroneWaypoint) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		tile.addHeight(player.isSneaking() ? - 1 : 1);
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntityDroneWaypoint tile = (TileEntityDroneWaypoint) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		List<String> text = new ArrayList();

		text.add("Waypoint distance: " + tile.height);

		if(tile.nextY != -1) {
			text.add("Next waypoint: " + tile.nextX + " / " + tile.nextY + " / " + tile.nextZ);
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
}

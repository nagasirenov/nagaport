package com.hbm.blocks.network;

import com.hbm.tileentity.network.TileEntityDroneWaypointRequest;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class DroneWaypointRequest extends BlockContainer {

	public DroneWaypointRequest() {
		super(Material.CIRCUITS);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDroneWaypointRequest();
	}

	@Override
	public int getRenderType() {
		return RadioTorchBase.renderID;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d vec0, Vec3d vec1) {

		int meta = world.getBlockMetadata(x, y, z) & 7;
		ForgeDirection dir = ForgeDirection.getOrientation(meta);

		this.setBlockBounds(
				dir.offsetX == 1 ? 0F : 0.375F,
				dir.offsetY == 1 ? 0F : 0.375F,
				dir.offsetZ == 1 ? 0F : 0.375F,
				dir.offsetX == -1 ? 1F : 0.625F,
				dir.offsetY == -1 ? 1F : 0.625F,
				dir.offsetZ == -1 ? 1F : 0.625F
				);

		return super.collisionRayTrace(state, world, pos, vec0, vec1);
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float fX, float fY, float fZ, int meta) {
		return side;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {

		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
		ForgeDirection dir = ForgeDirection.getOrientation(meta);
		Block b = com.hbm.world.WorldBlockState.read(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ).getBlock();

		if(!com.hbm.lib.FacingCompat.isBlockSideSolid(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir) && (!b.renderAsNormalBlock() || b.isAir(world, x, y, z))) {
			this.dropBlockAsItem(world, x, y, z, meta, 0);
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		}
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		if(!super.canPlaceBlockOnSide(world, x, y, z, side)) return false;

		ForgeDirection dir = ForgeDirection.getOrientation(side);
		Block b = com.hbm.world.WorldBlockState.read(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ).getBlock();

		return com.hbm.lib.FacingCompat.isBlockSideSolid(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir) || (b.renderAsNormalBlock() && !b.isAir(world, x, y, z));
	}
}

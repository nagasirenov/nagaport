package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

import java.util.List;

/**
 * Base class for all torch-like RTTY blocks
 * @author hbm
 */
public abstract class RadioTorchBase extends BlockContainer implements IGUIProvider, ILookOverlay, ITooltipProvider {

	public RadioTorchBase() {
		super(Material.circuits);
	}

	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public int getRenderType() {
		return renderID;
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

		int meta = world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ()) & 7;
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

		int meta = world.getBlockMetadata(x, y, z);
		ForgeDirection dir = ForgeDirection.getOrientation(meta);
		Block b = world.getBlock(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ);

		if(!canBlockStay(world, x, y, z, dir, b)) {
			this.dropBlockAsItem(world, x, y, z, meta, 0);
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		if(!super.canPlaceBlockOnSide(world, x, y, z, side)) return false;

		ForgeDirection dir = ForgeDirection.getOrientation(side);
		Block b = world.getBlock(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ);

		return canBlockStay(world, x, y, z, dir, b);
	}

	public boolean canBlockStay(World world, int x, int y, int z, ForgeDirection dir, Block b) {
		return com.hbm.lib.FacingCompat.isBlockSideSolid(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir) || b.hasComparatorInputOverride() || b.canProvidePower() || (b.renderAsNormalBlock() && !b.isAir(world, x, y, z));
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote && !player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			return true;
		} else {
			return !player.isSneaking();
		}
	}

	@Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return null; }

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}
}

package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorBelt;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.entity.item.EntityMovingItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import com.hbm.lib.ForgeDirection;

import java.util.List;

public abstract class BlockConveyorBase extends Block implements IConveyorBelt, ITooltipProvider {
	/** Legacy direction/path nibble, retained losslessly for every saved metadata value. */
	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
	protected static final AxisAlignedBB BELT_BOUNDS = new AxisAlignedBB(0, 0, 0, 1, 0.25, 1);

	public BlockConveyorBase() {
		super(Material.IRON);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return ConveyorModelState.create(this, METADATA);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(METADATA, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(METADATA);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return ConveyorModelState.extend(state, world, pos);
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
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		// Collision height does not grant the legacy non-normal conveyor solid attachment faces.
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return BELT_BOUNDS;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return getBoundingBox(state, world, pos);
	}

	@Override
	public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos) {
		return true;
	}

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {

		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		//snapping point
		Vec3d snap = this.getClosestSnappingPosition(world, x, y, z, itemPos);
		itemPos = this.getTravelOriginAfterSnap(world, x, y, z, itemPos);
		//snapping point + speed
		Vec3d dest = new Vec3d(snap.x - dir.offsetX * speed, snap.y - dir.offsetY * speed, snap.z - dir.offsetZ * speed);
		//delta to get to that point
		Vec3d motion = new Vec3d((dest.x - itemPos.x), (dest.y - itemPos.y), (dest.z - itemPos.z));
		double len = motion.length();
		//the effective destination towards "dest" after taking speed into consideration
		Vec3d ret = new Vec3d(itemPos.x + motion.x / len * speed, itemPos.y + motion.y / len * speed, itemPos.z + motion.z / len * speed);
		return ret;
	}

	/** Mirrors the legacy Vec3 component clamps performed while computing a snapping point. */
	protected Vec3d getTravelOriginAfterSnap(World world, int x, int y, int z, Vec3d itemPos) {
		return new Vec3d(MathHelper.clamp(itemPos.x, x, x + 1), itemPos.y, MathHelper.clamp(itemPos.z, z, z + 1));
	}

	public ForgeDirection getInputDirection(World world, int x, int y, int z) {
		return ForgeDirection.getOrientation(getMetaFromState(world.getBlockState(new BlockPos(x, y, z))));
	}

	public ForgeDirection getOutputDirection(World world, int x, int y, int z) {
		return ForgeDirection.getOrientation(getMetaFromState(world.getBlockState(new BlockPos(x, y, z)))).getOpposite();
	}

	public ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3d itemPos) {
		return ForgeDirection.getOrientation(getMetaFromState(world.getBlockState(new BlockPos(x, y, z))));
	}

	@Override
	public Vec3d getClosestSnappingPosition(World world, int x, int y, int z, Vec3d itemPos) {

		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);

		double itemX = MathHelper.clamp(itemPos.x, x, x + 1);
		double itemZ = MathHelper.clamp(itemPos.z, z, z + 1);

		double posX = x + 0.5;
		double posZ = z + 0.5;

		if(dir.offsetX != 0) {
			posX = itemX;
		}
		if(dir.offsetZ != 0) {
			posZ = itemZ;
		}

		return new Vec3d(posX, y + 0.25, posZ);
	}

	@Override
	public void onEntityCollision(World world, BlockPos blockPos, IBlockState state, Entity entity) {
		int x = blockPos.getX();
		int y = blockPos.getY();
		int z = blockPos.getZ();

		if(!world.isRemote) {

			if(entity instanceof EntityItem && entity.ticksExisted > 10 && !entity.isDead) {

				EntityMovingItem item = new EntityMovingItem(world);
				item.setItemStack(((EntityItem) entity).getItem().copy());
				Vec3d pos = new Vec3d(entity.posX, entity.posY, entity.posZ);
				Vec3d snap = this.getClosestSnappingPosition(world, x, y, z, pos);
				item.setPositionAndRotation(snap.x, snap.y, snap.z, 0, 0);
				world.spawnEntity(item);

				entity.setDead();
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) {
			world.setBlockState(pos, getStateFromMeta(2), 2);
		}
		if(i == 1) {
			world.setBlockState(pos, getStateFromMeta(5), 2);
		}
		if(i == 2) {
			world.setBlockState(pos, getStateFromMeta(3), 2);
		}
		if(i == 3) {
			world.setBlockState(pos, getStateFromMeta(4), 2);
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}

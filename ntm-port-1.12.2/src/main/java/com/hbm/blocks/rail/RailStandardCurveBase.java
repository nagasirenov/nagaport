package com.hbm.blocks.rail;


import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.Library;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class RailStandardCurveBase extends BlockRailNTM implements IRailNTM, IRenderBlock {
	
	protected int width = 4;

	public RailStandardCurveBase() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return null;
	}


	@Override
	public Vec3d getSnappingPos(World world, int x, int y, int z, double trainX, double trainY, double trainZ) {
		return snapAndMove(world, x, y, z, trainX, trainY, trainZ, 0, 0, 0, 0, new RailContext());
	}

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info, MoveContext context) {
		return snapAndMove(world, x, y, z, trainX, trainY, trainZ, motionX, motionY, motionZ, speed, info);
	}
	
	/* Very simple function determining the snapping position and adding the motion value to it, if desired. */
	public Vec3d snapAndMove(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info) {
		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return new Vec3d(trainX, trainY, trainZ);
		int cX = pos[0];
		int cY = pos[1];
		int cZ = pos[2];
		int meta = metadata(world, cX, cY, cZ) - this.offset;
		ForgeDirection dir = ForgeDirection.getOrientation(meta);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		double turnRadius = width;
		double axisDist = width + 0.5D;

		Vec3d vec = new Vec3d(trainX, trainY, trainZ);
		double axisX = cX + 0.5  + dir.offsetX * 0.5 + rot.offsetX * axisDist;
		double axisZ = cZ + 0.5  + dir.offsetZ * 0.5 + rot.offsetZ * axisDist;
		
		Vec3d dist = new Vec3d(vec.x - axisX, 0, vec.z - axisZ);
		dist = dist.normalize();
		dist = new Vec3d(dist.x * turnRadius, dist.y, dist.z * turnRadius);
		
		double moveAngle = Math.atan2(motionX, motionZ) * 180D / Math.PI + 90;
		
		if(speed == 0) {
			info.dist(0).pos(new BlockPos(x, y, z)).yaw((float) moveAngle);
			return new Vec3d(axisX + dist.x, y, axisZ + dist.z);
		}
		
		double angleDeg = Math.atan2(dist.x, dist.z) * 180D / Math.PI + 90;
		if(dir == Library.NEG_X) angleDeg -= 90;
		if(dir == Library.POS_X) angleDeg += 90;
		if(dir == Library.POS_Z) angleDeg += 180;
		angleDeg = MathHelper.wrapDegrees(angleDeg);
		double length90Deg = turnRadius * Math.PI / 2D;
		double angularChange = speed / length90Deg * 90D;
		
		ForgeDirection moveDir = ForgeDirection.UNKNOWN;
		
		if(Math.abs(motionX) > Math.abs(motionZ)) {
			moveDir = motionX > 0 ? Library.POS_X : Library.NEG_X;
		} else {
			moveDir = motionZ > 0 ? Library.POS_Z : Library.NEG_Z;
		}
		
		if(moveDir == dir || moveDir == rot.getOpposite()) {
			angularChange *= -1;
		}
		
		double effAngle = angleDeg + angularChange;
		moveAngle += angularChange;
		
		if(effAngle > 90) {
			double angleOvershoot = effAngle - 90D;
			moveAngle -= angleOvershoot;
			double lengthOvershoot = angleOvershoot * length90Deg / 90D;
			info.dist(lengthOvershoot * Math.signum(speed * angularChange)).pos(new BlockPos(cX - dir.offsetX * width + rot.offsetX * (width + 1), y, cZ - dir.offsetZ * width + rot.offsetZ * (width + 1))).yaw((float) moveAngle);
			return new Vec3d(axisX - dir.offsetX * turnRadius, y + 0.1875, axisZ - dir.offsetZ * turnRadius);
		}
		
		if(effAngle < 0) {
			double angleOvershoot = -effAngle;
			moveAngle -= angleOvershoot;
			double lengthOvershoot = angleOvershoot * length90Deg / 90D;
			info.dist(-lengthOvershoot * Math.signum(speed * angularChange)).pos(new BlockPos(cX + dir.offsetX , y, cZ + dir.offsetZ)).yaw((float) moveAngle);
			return new Vec3d(axisX - rot.offsetX * turnRadius, y + 0.1875, axisZ -rot.offsetZ * turnRadius);
		}
		
		double radianChange = angularChange * Math.PI / 180D;
		dist = dist.rotateYaw((float) radianChange);
		
		return new Vec3d(axisX + dist.x, y + 0.1875, axisZ + dist.z);
	}

	@Override
	public TrackGauge getGauge(World world, int x, int y, int z) {
		return TrackGauge.STANDARD;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, width, 0, width, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		this.setBlockBounds(0F, 0F, 0F, 1F, 0.125F, 1F);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		this.setBlockBounds(0F, 0F, 0F, 1F, 0.125F, 1F);
		return new AxisAlignedBB(x, y, z, x + 1, y + 0.125, z + 1);
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		dir = dir.getOpposite();

		int dX = dir.offsetX;
		int dZ = dir.offsetZ;
		int rX = rot.offsetX;
		int rZ = rot.offsetZ;
		
		return world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX, y, z + dZ)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX, y, z + dZ)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + rX, y, z + rZ)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + rX, y, z + rZ)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX + rX, y, z + dZ + rZ)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX + rX, y, z + dZ + rZ)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX + rX * 2, y, z + dZ + rZ * 2)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX + rX * 2, y, z + dZ + rZ * 2)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2 + rX, y, z + dZ * 2 + rZ)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 2 + rX, y, z + dZ * 2 + rZ)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2 + rX * 2, y, z + dZ * 2 + rZ * 2)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 2 + rX * 2, y, z + dZ * 2 + rZ * 2)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX, y, z + dZ * 3 + rZ)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 3 + rX, y, z + dZ * 3 + rZ)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 2, y, z + dZ * 3 + rZ * 2)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 2, y, z + dZ * 3 + rZ * 2)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2 + rX * 3, y, z + dZ * 2 + rZ * 3)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 2 + rX * 3, y, z + dZ * 2 + rZ * 3)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 3, y, z + dZ * 3 + rZ * 3)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 3, y, z + dZ * 3 + rZ * 3)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 3, y, z + dZ * 4 + rZ * 3)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 3, y, z + dZ * 4 + rZ * 3)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 4, y, z + dZ * 3 + rZ * 4)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 4, y, z + dZ * 3 + rZ * 4)) &&
				world.getBlockState(new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 4, y, z + dZ * 4 + rZ * 4)).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 4, y, z + dZ * 4 + rZ * 4));
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		
		BlockDummyable.safeRem = true;
		
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		dir = dir.getOpposite();

		int dX = dir.offsetX;
		int dZ = dir.offsetZ;
		int rX = rot.offsetX;
		int rZ = rot.offsetZ;
		
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX, y, z + dZ), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + rX, y, z + rZ), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX + rX, y, z + dZ + rZ), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX + rX * 2, y, z + dZ + rZ * 2), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2 + rX, y, z + dZ * 2 + rZ), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2 + rX * 2, y, z + dZ * 2 + rZ * 2), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX, y, z + dZ * 3 + rZ), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 2, y, z + dZ * 3 + rZ * 2), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 2 + rX * 3, y, z + dZ * 2 + rZ * 3), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 3, y, z + dZ * 3 + rZ * 3), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 3, y, z + dZ * 4 + rZ * 3), this.getStateFromMeta(dir.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 3 + rX * 4, y, z + dZ * 3 + rZ * 4), this.getStateFromMeta(rot.ordinal()), 3);
		world.setBlockState(new net.minecraft.util.math.BlockPos(x + dX * 4 + rX * 4, y, z + dZ * 4 + rZ * 4), this.getStateFromMeta(rot.ordinal()), 3);
		
		BlockDummyable.safeRem = false;
	}


}

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

public class RailNarrowStraight extends BlockRailNTM implements IRailNTM, IRenderBlock {

	public RailNarrowStraight() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return null;
	}


	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 2, 2, 0, 0};
	}

	@Override
	public int getOffset() {
		return 2;
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

		Vec3d vec = new Vec3d(trainX, trainY, trainZ);
		
		if(dir == Library.POS_X || dir == Library.NEG_X) {
			double targetX = trainX;
			if(motionX > 0) {
				targetX += speed;
				info.yaw(-90F);
			} else {
				targetX -= speed;
				info.yaw(90F);
			}
			vec = new Vec3d(MathHelper.clamp(targetX, cX - 2, cX + 3), y, cZ + 0.5);
			info.dist(Math.abs(targetX - vec.x) * Math.signum(speed));
			info.pos(new BlockPos(cX + (motionX * speed > 0 ? 3 : -3), y, cZ));
		} else {
			double targetZ = trainZ;
			if(motionZ > 0) {
				targetZ += speed;
				info.yaw(0F);
			} else {
				targetZ -= speed;
				info.yaw(180F);
			}
			vec = new Vec3d(cX + 0.5, y, MathHelper.clamp(targetZ, cZ - 2,cZ + 3));
			info.dist(Math.abs(targetZ - vec.z) * Math.signum(speed));
			info.pos(new BlockPos(cX, y, cZ + (motionZ * speed > 0 ? 3 : -3)));
		}
		
		return vec;
	}

	@Override
	public TrackGauge getGauge(World world, int x, int y, int z) {
		return TrackGauge.NARROW;
	}


}

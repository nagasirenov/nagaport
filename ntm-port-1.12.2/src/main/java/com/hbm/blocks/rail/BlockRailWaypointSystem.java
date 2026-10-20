package com.hbm.blocks.rail;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.entity.train.EntityRailCarBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ParticleUtil;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockRailWaypointSystem extends BlockRailNTM implements IRailNTM {
	
	public List<RailDef> railDefs = new ArrayList();
	
	public BlockRailWaypointSystem(Material mat) {
		super(mat);
	}
	
	/** Whether the train at position FROM can move towards the waypoint TO along the supplied railDef, also supplies world and core position */
	public boolean canCross(World world, int x, int y, int z, Vec3d from, Vec3d to, RailDef def) {
		return true;
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

	public Vec3d snapAndMove(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info) {
		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return new Vec3d(trainX, trainY, trainZ);
		int cX = pos[0];
		int cY = pos[1];
		int cZ = pos[2];
		int meta = metadata(world, cX, cY, cZ);
		double moveAngle = Math.atan2(motionX, motionZ) * 180D / Math.PI + 90;
		Vec3d trainPos = new Vec3d(trainX, trainY, trainZ);
		
		//convert nodes to a list of links with in-world positions
		Vec3d train = new Vec3d(trainX, trainY, trainZ);
		Vec3d core = new Vec3d(cX + 0.5, cY, cZ + 0.5);
		List<List<Pair<Vec3d[], RailDef>>> links = new ArrayList();
		
		for(RailDef def : railDefs) {
			List<Pair<Vec3d[], RailDef>> linkList = new ArrayList();
			links.add(linkList);
			
			for(int i = 0; i < def.nodes.size() - 1; i++) {
				Vec3d vec1 = getPositionFromNode(world, x, y, z, core, def.nodes.get(i), meta);
				Vec3d vec2 = getPositionFromNode(world, x, y, z, core, def.nodes.get(i + 1), meta);
				ParticleUtil.spawnDroneLine(world, vec1.x, vec1.y, vec1.z, vec2.x - vec1.x, vec2.y - vec1.y, vec2.z - vec1.z, 0xff0000);
				linkList.add(new Pair<Vec3d[], RailDef>(new Vec3d[] {vec1, vec2}, def));
			}
		}
		
		//find closest node
		Pair<Vec3d[], RailDef> closest = null;
		Vec3d startingPos = null;
		/** closest chain of link definitions */
		List<Pair<Vec3d[], RailDef>> cDef = null;
		double angularDiff = 0;
		double linkAngle = 0;
		double dist = Double.MAX_VALUE;
		/** direction */
		boolean d = true;
		
		for(List<Pair<Vec3d[], RailDef>> chain : links) {
			for(Pair<Vec3d[], RailDef> link : chain) {
				Vec3d[] array = link.getKey();
				Vec3d point = getClosestPointOnLink(array[0], array[1], train);
				
				if(point != null) {
					Vec3d delta = point.subtractReverse(train);
					double length = delta.length();
					
					if(!canCross(world, cX, cY, cZ, trainPos, point, link.getValue())) continue;
					
					linkAngle = EntityRailCarBase.generateYaw(array[1], array[0]);
					angularDiff = BobMathUtil.angularDifference(linkAngle, -moveAngle);
					if(angularDiff < -180) { angularDiff += 180; linkAngle += 180; d = false; }
					if(angularDiff > 0) { angularDiff -= 180; linkAngle -= 180; d = false; }
					
					if(length < dist) {
						closest = link;
						startingPos = point;
						cDef = chain;
						dist = length;
					}
				}
			}
		}
		
		if(closest == null) {
			return new Vec3d(trainX, trainY, trainZ);
		}
		
		double distRemaining = speed;
		boolean engaged = false;
		Vec3d currentPos = startingPos;
		for(int i = d ? 0 : cDef.size() - 1; d ? (i < cDef.size()) : (i >= 0); i += d ? 1 : -1) {
			
			Pair<Vec3d[], RailDef> link = cDef.get(i);
			Vec3d[] array = link.getKey();
			
			if(!engaged) {
				if(link == closest) {
					engaged = true;
				} else {
					continue;
				}
			}
			
			Vec3d nextNode = array[d?1:0];
			Vec3d delta = nextNode.subtractReverse(currentPos);
			
			if(!canCross(world, cX, cY, cZ, currentPos, nextNode, link.getValue())) break;
			
			double len = delta.length();
			if(len >= distRemaining) {
				info.overshoot = 0;
				double newYaw = EntityRailCarBase.generateYaw(nextNode, currentPos);
				if(Math.abs(BobMathUtil.angularDifference(newYaw, moveAngle)) < 45) info.yaw = (float) newYaw;
				else info.yaw = (float) moveAngle;
				delta.normalize();
				return new Vec3d(currentPos.x - delta.x * distRemaining / len, currentPos.y - delta.y * distRemaining / len, currentPos.z - delta.z * distRemaining / len);
			}
			
			distRemaining -= len;
			currentPos = nextNode;
		}

		info.overshoot = distRemaining;
		info.pos = new BlockPos(currentPos.x, currentPos.y, currentPos.z);
		
		return currentPos;
	}
	
	public Vec3d getClosestPointOnLink(Vec3d pointA, Vec3d pointB, Vec3d pointP) {
		Vec3d ap = new Vec3d(pointP.x - pointA.x, 0, pointP.z - pointA.z);
		Vec3d ab = new Vec3d(pointB.x - pointA.x, 0, pointB.z - pointA.z);
		
		double magAB = ab.x * ab.x + ab.z * ab.z;
		double dotProd = ap.x * ab.x + ap.z * ab.z;
		double dist = dotProd / magAB;
		
		if(dist < 0) return pointA;
		if(dist > 1) return pointB;
		if(dist < 0 || dist > 1) return null;
		
		return new Vec3d(pointA.x + ab.x * dist, pointA.y + (pointB.y - pointA.y) * dist, pointA.z + ab.z * dist);
	}
	
	/** Creates the in-world position for a node based on the node itself and the core position */
	public Vec3d getPositionFromNode(World world, int x, int y, int z, Vec3d core, Vec3d node, int meta) {
		float rotation = 0;
		if(meta == 12) rotation = 90F / 180F * (float) Math.PI;
		if(meta == 14) rotation = 180F / 180F * (float) Math.PI;
		if(meta == 13) rotation = 270F / 180F * (float) Math.PI;
		Vec3d copy = new Vec3d(node.x, node.y, node.z);
		copy = copy.rotateYaw(rotation);
		return core.add(copy.x, copy.y, copy.z);
	}
	
	public class RailDef {
		String name;
		public List<Vec3d> nodes = new ArrayList();
		
		public RailDef(String name) {
			this.name = name;
		}
	}
}

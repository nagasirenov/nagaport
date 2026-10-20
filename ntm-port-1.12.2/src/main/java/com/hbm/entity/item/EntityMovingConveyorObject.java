package com.hbm.entity.item;

import com.hbm.util.fauxpointtwelve.BlockPos;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public abstract class EntityMovingConveyorObject extends Entity {
	
	protected int turnProgress;
	protected double syncPosX;
	protected double syncPosY;
	protected double syncPosZ;
	@SideOnly(Side.CLIENT) protected double velocityX;
	@SideOnly(Side.CLIENT) protected double velocityY;
	@SideOnly(Side.CLIENT) protected double velocityZ;

	public EntityMovingConveyorObject(World world) {
		super(world);
		this.noClip = true;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean canBeAttackedWithItem() {
		return true;
	}

	@Override
	public boolean hitByEntity(Entity attacker) {

		if(attacker instanceof EntityPlayer) {
			this.setDead();
		}
		
		return false;
	}

	@Override
	protected boolean canTriggerWalking() {
		return true;
	}

	@Override
	public void onUpdate() {
		
		if(world.isRemote) {
			if(this.turnProgress > 0) {
				double interpX = this.posX + (this.syncPosX - this.posX) / (double) this.turnProgress;
				double interpY = this.posY + (this.syncPosY - this.posY) / (double) this.turnProgress;
				double interpZ = this.posZ + (this.syncPosZ - this.posZ) / (double) this.turnProgress;
				--this.turnProgress;
				this.setPosition(interpX, interpY, interpZ);
			} else {
				this.setPosition(this.posX, this.posY, this.posZ);
			}
		}

		if(!world.isRemote) {
			
			ticksExisted++;
			
			if(this.ticksExisted <= 5) {
				return;
			}

			int blockX = (int) Math.floor(posX);
			int blockY = (int) Math.floor(posY);
			int blockZ = (int) Math.floor(posZ);
			
			Block b = world.getBlockState(new net.minecraft.util.math.BlockPos(blockX, blockY, blockZ)).getBlock();
			boolean isOnConveyor = b instanceof IConveyorBelt && ((IConveyorBelt) b).canItemStay(world, blockX, blockY, blockZ, new Vec3d(posX, posY, posZ));
			
			if(!isOnConveyor) {
				
				if(onLeaveConveyor()) {
					return;
				}
			} else {
				
				Vec3d target = ((IConveyorBelt) b).getTravelLocation(world, blockX, blockY, blockZ, new Vec3d(posX, posY, posZ), getMoveSpeed());
				this.motionX = target.x - posX;
				this.motionY = target.y - posY;
				this.motionZ = target.z - posZ;
			}
			
			BlockPos lastPos = new BlockPos(posX, posY, posZ);
			this.move(MoverType.SELF, motionX, motionY, motionZ);
			BlockPos newPos = new BlockPos(posX, posY, posZ);
			
			if(!lastPos.equals(newPos)) {
				
				IBlockState newState = world.getBlockState(new net.minecraft.util.math.BlockPos(newPos.getX(), newPos.getY(), newPos.getZ()));
				Block newBlock = newState.getBlock();
				
				if(newBlock instanceof IEnterableBlock) {
					
					ForgeDirection dir = ForgeDirection.UNKNOWN;

					if(lastPos.getX() > newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() == newPos.getZ()) dir = ForgeDirection.EAST;
					else if(lastPos.getX() < newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() == newPos.getZ()) dir = ForgeDirection.WEST;
					else if(lastPos.getX() == newPos.getX() && lastPos.getY() > newPos.getY() && lastPos.getZ() == newPos.getZ()) dir = ForgeDirection.UP;
					else if(lastPos.getX() == newPos.getX() && lastPos.getY() < newPos.getY() && lastPos.getZ() == newPos.getZ()) dir = ForgeDirection.DOWN;
					else if(lastPos.getX() == newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() > newPos.getZ()) dir = ForgeDirection.SOUTH;
					else if(lastPos.getX() == newPos.getX() && lastPos.getY() == newPos.getY() && lastPos.getZ() < newPos.getZ()) dir = ForgeDirection.NORTH;
					
					IEnterableBlock enterable = (IEnterableBlock) newBlock;
					enterBlock(enterable, newPos, dir);
					
				} else {
					
					if(!newState.getMaterial().isSolid()) {
						
						newBlock = world.getBlockState(new net.minecraft.util.math.BlockPos(newPos.getX(), newPos.getY() - 1, newPos.getZ())).getBlock();
						
						if(newBlock instanceof IEnterableBlock) {
							
							IEnterableBlock enterable = (IEnterableBlock) newBlock;
							enterBlockFalling(enterable, newPos);
						}
					}
				}
			}
		}
	}

	public abstract void enterBlock(IEnterableBlock enterable, BlockPos pos, ForgeDirection dir);
	
	public void enterBlockFalling(IEnterableBlock enterable, BlockPos pos) {
		this.enterBlock(enterable, pos.add(0, -1, 0), ForgeDirection.UP);
	}
	
	/**
	 * @return true if the update loop should end
	 */
	public abstract boolean onLeaveConveyor();
	
	public double getMoveSpeed() {
		return 0.0625D;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setVelocity(double motionX, double motionY, double motionZ) {
		this.velocityX = this.motionX = motionX;
		this.velocityY = this.motionY = motionY;
		this.velocityZ = this.motionZ = motionZ;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int theNumberThree, boolean teleport) {
		this.syncPosX = x;
		this.syncPosY = y;
		this.syncPosZ = z;
		this.turnProgress = theNumberThree + 2; //use 4-ply for extra smoothness
		this.motionX = this.velocityX;
		this.motionY = this.velocityY;
		this.motionZ = this.velocityZ;
	}
}

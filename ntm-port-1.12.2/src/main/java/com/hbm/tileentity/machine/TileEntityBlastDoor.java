package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.DummyBlockBlast;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.TEVaultPacket;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityBlastDoor extends TileEntityLockableBase implements net.minecraft.util.ITickable {

	public boolean isOpening = false;
	//0: closed, 1: opening/closing, 2:open
	public int state = 0;
	public long sysTime;
	private int timer = 0;
	public boolean redstoned = false;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
    public void update() {

		if(!this.world.isRemote) {

			if(!isLocked() && this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ())) || this.world.isBlockPowered(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY() + 6, this.pos.getZ()))) {

				if(!redstoned) {
					this.tryToggle();
				}
				redstoned = true;

			} else {
				redstoned = false;
			}

	    	if(state != 1) {
	    		timer = 0;
	    	} else {
	    		timer++;

    			if(isOpening) {
    				if(timer >= 0) {
    					removeDummy(this.pos.getX(), this.pos.getY() + 1, this.pos.getZ());
    				}
    				if(timer >= 20) {
    					removeDummy(this.pos.getX(), this.pos.getY() + 2, this.pos.getZ());
    				}
    				if(timer >= 40) {
    					removeDummy(this.pos.getX(), this.pos.getY() + 3, this.pos.getZ());
    				}
    				if(timer >= 60) {
    					removeDummy(this.pos.getX(), this.pos.getY() + 4, this.pos.getZ());
    				}
    				if(timer >= 80) {
    					removeDummy(this.pos.getX(), this.pos.getY() + 5, this.pos.getZ());
    				}
    			} else {
    				if(timer >= 20) {
    					placeDummy(this.pos.getX(), this.pos.getY() + 5, this.pos.getZ());
    				}
    				if(timer >= 40) {
    					placeDummy(this.pos.getX(), this.pos.getY() + 4, this.pos.getZ());
    				}
    				if(timer >= 60) {
    					placeDummy(this.pos.getX(), this.pos.getY() + 3, this.pos.getZ());
    				}
    				if(timer >= 80) {
    					placeDummy(this.pos.getX(), this.pos.getY() + 2, this.pos.getZ());
    				}
    				if(timer >= 100) {
    					placeDummy(this.pos.getX(), this.pos.getY() + 1, this.pos.getZ());
    				}
    			}

	    		if(timer >= 100) {

	    			if(isOpening)
	    				finishOpen();
	    			else
	    				finishClose();
	    		}
	    	}

	    	PacketDispatcher.wrapper.sendToAllAround(new TEVaultPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), isOpening, state, 0, 0), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 250));
		}
    }

	public void open() {
		if(state == 0) {
	    	PacketDispatcher.wrapper.sendToAllAround(new TEVaultPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), isOpening, state, 1, 0), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 250));
			isOpening = true;
			state = 1;

			com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.reactorStart", 0.5F,
					0.75F);
		}
	}

	public void finishOpen() {
		state = 2;

		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.reactorStop", 0.5F,
				1.0F);
	}

	public void close() {
		if(state == 2) {
	    	PacketDispatcher.wrapper.sendToAllAround(new TEVaultPacket(this.pos.getX(), this.pos.getY(), this.pos.getZ(), isOpening, state, 1, 0), new TargetPoint(this.world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 250));
			isOpening = false;
			state = 1;

			com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.reactorStart", 0.5F,
					0.75F);
		}
	}

	public void finishClose() {
		state = 0;

		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.reactorStop", 0.5F,
				1.0F);
	}

	public void openNeigh() {

		TileEntity te0 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ()));
		TileEntity te1 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ()));
		TileEntity te2 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() + 1));
		TileEntity te3 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() - 1));

		if(te0 instanceof TileEntityBlastDoor) {

			if(((TileEntityBlastDoor)te0).canOpen() && (!((TileEntityBlastDoor)te0).isLocked() || ((TileEntityBlastDoor)te0).lock == lock)) {
				((TileEntityBlastDoor)te0).open();
				((TileEntityBlastDoor)te0).openNeigh();
			}
		}

		if(te1 instanceof TileEntityBlastDoor) {

			if(((TileEntityBlastDoor)te1).canOpen() && (!((TileEntityBlastDoor)te1).isLocked() || ((TileEntityBlastDoor)te1).lock == lock)) {
				((TileEntityBlastDoor)te1).open();
				((TileEntityBlastDoor)te1).openNeigh();
			}
		}

		if(te2 instanceof TileEntityBlastDoor) {

			if(((TileEntityBlastDoor)te2).canOpen() && (!((TileEntityBlastDoor)te2).isLocked() || ((TileEntityBlastDoor)te2).lock == lock)) {
				((TileEntityBlastDoor)te2).open();
				((TileEntityBlastDoor)te2).openNeigh();
			}
		}

		if(te3 instanceof TileEntityBlastDoor) {

			if(((TileEntityBlastDoor)te3).canOpen() && (!((TileEntityBlastDoor)te3).isLocked() || ((TileEntityBlastDoor)te3).lock == lock)) {
				((TileEntityBlastDoor)te3).open();
				((TileEntityBlastDoor)te3).openNeigh();
			}
		}
	}

	@Override
	public void lock() {
		super.lock();
		lockNeigh();
	}

	public void closeNeigh() {

		TileEntity te0 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ()));
		TileEntity te1 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ()));
		TileEntity te2 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() + 1));
		TileEntity te3 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() - 1));

		if(te0 instanceof TileEntityBlastDoor) {

			if(((TileEntityBlastDoor)te0).canClose() && (!((TileEntityBlastDoor)te0).isLocked() || ((TileEntityBlastDoor)te0).lock == lock)) {
				((TileEntityBlastDoor)te0).close();
				((TileEntityBlastDoor)te0).closeNeigh();
			}
		}

		if(te1 instanceof TileEntityBlastDoor) {

			if(((TileEntityBlastDoor)te1).canClose() && (!((TileEntityBlastDoor)te1).isLocked() || ((TileEntityBlastDoor)te1).lock == lock)) {
				((TileEntityBlastDoor)te1).close();
				((TileEntityBlastDoor)te1).closeNeigh();
			}
		}

		if(te2 instanceof TileEntityBlastDoor) {

			if(((TileEntityBlastDoor)te2).canClose() && (!((TileEntityBlastDoor)te2).isLocked() || ((TileEntityBlastDoor)te2).lock == lock)) {
				((TileEntityBlastDoor)te2).close();
				((TileEntityBlastDoor)te2).closeNeigh();
			}
		}

		if(te3 instanceof TileEntityBlastDoor) {

			if(((TileEntityBlastDoor)te3).canClose() && (!((TileEntityBlastDoor)te3).isLocked() || ((TileEntityBlastDoor)te3).lock == lock)) {
				((TileEntityBlastDoor)te3).close();
				((TileEntityBlastDoor)te3).closeNeigh();
			}
		}
	}

	public void lockNeigh() {

		TileEntity te0 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ()));
		TileEntity te1 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ()));
		TileEntity te2 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() + 1));
		TileEntity te3 = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() - 1));

		if(te0 instanceof TileEntityBlastDoor) {

			if(!((TileEntityBlastDoor)te0).isLocked()) {
				((TileEntityBlastDoor)te0).setPins(this.lock);
				((TileEntityBlastDoor)te0).lock();
				((TileEntityBlastDoor)te0).setMod(lockMod);
			}
		}

		if(te1 instanceof TileEntityBlastDoor) {

			if(!((TileEntityBlastDoor)te1).isLocked()) {
				((TileEntityBlastDoor)te1).setPins(this.lock);
				((TileEntityBlastDoor)te1).lock();
				((TileEntityBlastDoor)te1).setMod(lockMod);
			}
		}

		if(te2 instanceof TileEntityBlastDoor) {

			if(!((TileEntityBlastDoor)te2).isLocked()) {
				((TileEntityBlastDoor)te2).setPins(this.lock);
				((TileEntityBlastDoor)te2).lock();
				((TileEntityBlastDoor)te2).setMod(lockMod);
			}
		}

		if(te3 instanceof TileEntityBlastDoor) {

			if(!((TileEntityBlastDoor)te3).isLocked()) {
				((TileEntityBlastDoor)te3).setPins(this.lock);
				((TileEntityBlastDoor)te3).lock();
				((TileEntityBlastDoor)te3).setMod(lockMod);
			}
		}
	}

	public boolean canOpen() {
		return state == 0;
	}

	public boolean canClose() {
		return state == 2;
	}

	public void tryToggle() {

		if(canOpen()) {
			open();
			openNeigh();
		} else if(canClose()) {
			close();
			closeNeigh();
		}
	}

	public boolean placeDummy(int x, int y, int z) {

		Block present = com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock();
		if(!present.isReplaceable(this.world, new net.minecraft.util.math.BlockPos(x, y, z)) && present != ModBlocks.dummy_block_blast) com.hbm.world.WorldBlockDestruction.destroy(this.world, x, y, z, false);

		com.hbm.world.WorldBlockState.writeLegacyBlock(this.world, x, y, z, ModBlocks.dummy_block_blast);

		TileEntity te = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(te instanceof TileEntityDummy) {
			TileEntityDummy dummy = (TileEntityDummy)te;
			dummy.targetX = this.pos.getX();
			dummy.targetY = this.pos.getY();
			dummy.targetZ = this.pos.getZ();
		}

		return true;
	}

	public void removeDummy(int x, int y, int z) {

		if(com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock() == ModBlocks.dummy_block_blast) {
			DummyBlockBlast.safeBreak = true;
			com.hbm.world.WorldBlockState.write(this.world, x, y, z, Blocks.AIR.getStateFromMeta(0), 3);
			DummyBlockBlast.safeBreak = false;
		}
	}

	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		isOpening = nbt.getBoolean("isOpening");
		state = nbt.getInteger("state");
		sysTime = nbt.getLong("sysTime");
		timer = nbt.getInteger("timer");
		redstoned = nbt.getBoolean("redstoned");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setBoolean("isOpening", isOpening);
		nbt.setInteger("state", state);
		nbt.setLong("sysTime", sysTime);
		nbt.setInteger("timer", timer);
		nbt.setBoolean("redstoned", redstoned);
		return nbt;
	}

}

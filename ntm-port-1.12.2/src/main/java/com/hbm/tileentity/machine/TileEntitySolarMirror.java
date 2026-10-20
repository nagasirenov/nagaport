package com.hbm.tileentity.machine;

import com.hbm.tileentity.TileEntityTickingBase;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;

public class TileEntitySolarMirror extends TileEntityTickingBase {

	public int tX;
	public int tY;
	public int tZ;
	public boolean isOn;

	@Override
	public String getInventoryName() {
		return null;
	}

	@Override
	public void update() {
		
		if(!this.world.isRemote) {
			
			if(this.world.getTotalWorldTime() % 20 == 0)
				this.networkPackNT(200);
			
			if(tY < this.pos.getY()) {
				isOn = false;
				return;
			}
			
			int sun = this.world.getLightFor(EnumSkyBlock.SKY, this.pos) - this.world.getSkylightSubtracted() - 11;
			
			if(sun <= 0 || !this.world.canSeeSky(this.pos.up())) {
				isOn = false;
				return;
			}
			
			isOn = true;
			
			TileEntity te = this.world.getTileEntity(new BlockPos(tX, tY - 1, tZ));
			
			if(te instanceof TileEntitySolarBoiler) {
				TileEntitySolarBoiler boiler = (TileEntitySolarBoiler)te;
				boiler.heat += sun;
			}
		} else {
			
			TileEntity te = this.world.getTileEntity(new BlockPos(tX, tY - 1, tZ));
			
			if(isOn && te instanceof TileEntitySolarBoiler) {
				TileEntitySolarBoiler boiler = (TileEntitySolarBoiler)te;
				boiler.primary.add(new SolarMirrorPosition(this.pos.getX(), this.pos.getY(), this.pos.getZ()));
			}
			
			if(this.world.getTotalWorldTime() % 20 == 0)
				this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
		}
	}

	@Override public void serialize(ByteBuf buf) {
		buf.writeInt(this.tX);
		buf.writeInt(this.tY);
		buf.writeInt(this.tZ);
		buf.writeBoolean(this.isOn);
	}

	@Override public void deserialize(ByteBuf buf) {
		this.tX = buf.readInt();
		this.tY = buf.readInt();
		this.tZ = buf.readInt();
		this.isOn = buf.readBoolean();
	}
	
	public void setTarget(int x, int y, int z) {
		tX = x;
		tY = y;
		tZ = z;
		this.markDirty();
		this.networkPackNT(200);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		tX = nbt.getInteger("targetX");
		tY = nbt.getInteger("targetY");
		tZ = nbt.getInteger("targetZ");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("targetX", tX);
		nbt.setInteger("targetY", tY);
		nbt.setInteger("targetZ", tZ);
		return nbt;
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 25,
					this.pos.getY() - 25,
					this.pos.getZ() - 25,
					this.pos.getX() + 25,
					this.pos.getY() + 25,
					this.pos.getZ() + 25
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}

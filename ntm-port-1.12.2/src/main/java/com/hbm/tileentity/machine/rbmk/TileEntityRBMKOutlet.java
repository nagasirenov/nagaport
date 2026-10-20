package com.hbm.tileentity.machine.rbmk;

import api.hbm.fluid.IFluidStandardSender;
import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import com.hbm.lib.ForgeDirection;

public class TileEntityRBMKOutlet extends TileEntityLoadedBase implements IFluidStandardSender, IBufPacketReceiver, net.minecraft.util.ITickable {
	
	public FluidTank steam;
	
	public TileEntityRBMKOutlet() {
		steam = new FluidTank(Fluids.SUPERHOTSTEAM, 32000);
	}
	
	@Override
	public void update() {
		
		if(!this.world.isRemote) {
			
			if(RBMKDials.getReasimBoilers(this.world)) for(int i = 2; i < 6; i++) {
				ForgeDirection dir = ForgeDirection.getOrientation(i);
				Block b = com.hbm.world.WorldBlockState.read(this.world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ).getBlock();
				
				if(b instanceof RBMKBase) {
					int[] pos = ((RBMKBase)b).findCore(this.world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ);
					
					if(pos != null) {
						TileEntity te = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
						
						if(te instanceof TileEntityRBMKBase) {
							TileEntityRBMKBase rbmk = (TileEntityRBMKBase) te;
							
							int prov = Math.min(steam.getMaxFill() - steam.getFill(), rbmk.reasimSteam);
							rbmk.reasimSteam -= prov;
							steam.setFill(steam.getFill() + prov);
						}
					}
				}
			}
			
			fillFluidInit();
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.steam.readFromNBT(nbt, "tank");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.steam.writeToNBT(nbt, "tank");
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		this.steam.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.steam.deserialize(buf);
	}

	public void fillFluidInit() {
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			this.sendFluid(steam, this.world, this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, dir);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {steam};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {steam};
	}

}

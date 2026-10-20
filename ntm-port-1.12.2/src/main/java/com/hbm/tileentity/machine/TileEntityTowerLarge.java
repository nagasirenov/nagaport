package com.hbm.tileentity.machine;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.ClientConfig;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IConfigurableMachine;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityTowerLarge extends TileEntityCondenser {
	
	//Configurable values
	public static int inputTankSizeTL = 10_000;
	public static int outputTankSizeTL = 10_000;
	
	public TileEntityTowerLarge() {
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.SPENTSTEAM, inputTankSizeTL);
		tanks[1] = new FluidTank(Fluids.WATER, outputTankSizeTL);
	}
	
	@Override
	public String getConfigName() {
		return "condenserTowerLarge";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		inputTankSizeTL = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSizeTL);
		outputTankSizeTL = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSizeTL);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:inputTankSize").value(inputTankSizeTL);
		writer.name("I:outputTankSize").value(outputTankSizeTL);
	}

	@Override
	public void update() {
		super.update();
		
		if(this.world.isRemote) {
			
			if(ClientConfig.COOLING_TOWER_PARTICLES.get() && (this.waterTimer > 0 && this.world.getTotalWorldTime() % 4 == 0)) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "tower");
				data.setFloat("lift", 0.5F);
				data.setFloat("base", 1F);
				data.setFloat("max", 10F);
				data.setInteger("life", 750 + this.world.rand.nextInt(250));
	
				data.setDouble("posX", this.pos.getX() + 0.5 + this.world.rand.nextDouble() * 3 - 1.5);
				data.setDouble("posZ", this.pos.getZ() + 0.5 + this.world.rand.nextDouble() * 3 - 1.5);
				data.setDouble("posY", this.pos.getY() + 1);
				
				MainRegistry.proxy.effectNT(data);
			}
		}
	}

	@Override
	public void subscribeToAllAround(FluidType type, TileEntity te) {
		
		for(int i = 2; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
			this.trySubscribe(this.tanks[0].getTankType(), this.world, this.pos.getX() + dir.offsetX * 5, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 5, dir);
			this.trySubscribe(this.tanks[0].getTankType(), this.world, this.pos.getX() + dir.offsetX * 5 + rot.offsetX * 3, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 5 + rot.offsetZ * 3, dir);
			this.trySubscribe(this.tanks[0].getTankType(),this.world,  this.pos.getX() + dir.offsetX * 5 + rot.offsetX * -3, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 5 + rot.offsetZ * -3, dir);
		}
	}

	@Override
	public void sendFluidToAll(FluidTank tank, TileEntity te) {
		
		for(int i = 2; i < 6; i++) {
			ForgeDirection dir = ForgeDirection.getOrientation(i);
			ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
			this.sendFluid(this.tanks[1], this.world, this.pos.getX() + dir.offsetX * 5, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 5, dir);
			this.sendFluid(this.tanks[1], this.world, this.pos.getX() + dir.offsetX * 5 + rot.offsetX * 3, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 5 + rot.offsetZ * 3, dir);
			this.sendFluid(this.tanks[1], this.world,  this.pos.getX() + dir.offsetX * 5 + rot.offsetX * -3, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 5 + rot.offsetZ * -3, dir);
		}
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 4,
					this.pos.getY(),
					this.pos.getZ() - 4,
					this.pos.getX() + 5,
					this.pos.getY() + 13,
					this.pos.getZ() + 5
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

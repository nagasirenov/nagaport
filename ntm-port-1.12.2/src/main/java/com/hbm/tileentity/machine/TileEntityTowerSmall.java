package com.hbm.tileentity.machine;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.config.ClientConfig;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IConfigurableMachine;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityTowerSmall extends TileEntityCondenser {
	
	//Configurable values
	public static int inputTankSizeTS = 1_000;
	public static int outputTankSizeTS = 1_000;

	public TileEntityTowerSmall() {
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.SPENTSTEAM, inputTankSizeTS);
		tanks[1] = new FluidTank(Fluids.WATER, outputTankSizeTS);
	}
	
	@Override
	public String getConfigName() {
		return "condenserTowerSmall";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		inputTankSizeTS = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSizeTS);
		outputTankSizeTS = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSizeTS);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:inputTankSize").value(inputTankSizeTS);
		writer.name("I:outputTankSize").value(outputTankSizeTS);
	}

	@Override
	public void update() {
		super.update();
		
		if(this.world.isRemote) {
			
			if(ClientConfig.COOLING_TOWER_PARTICLES.get() && (this.waterTimer > 0 && this.world.getTotalWorldTime() % 2 == 0)) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "tower");
				data.setFloat("lift", 1F);
				data.setFloat("base", 0.5F);
				data.setFloat("max", 4F);
				data.setInteger("life", 250 + this.world.rand.nextInt(250));
	
				data.setDouble("posX", this.pos.getX() + 0.5);
				data.setDouble("posZ", this.pos.getZ() + 0.5);
				data.setDouble("posY", this.pos.getY() + 18);
				
				MainRegistry.proxy.effectNT(data);
			}
		}
	}

	@Override
	public void subscribeToAllAround(FluidType type, TileEntity te) {
		this.trySubscribe(this.tanks[0].getTankType(), this.world, this.pos.getX() + 3, this.pos.getY(), this.pos.getZ(), Library.POS_X);
		this.trySubscribe(this.tanks[0].getTankType(), this.world, this.pos.getX() - 3, this.pos.getY(), this.pos.getZ(), Library.NEG_X);
		this.trySubscribe(this.tanks[0].getTankType(), this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ() + 3, Library.POS_Z);
		this.trySubscribe(this.tanks[0].getTankType(), this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ() - 3, Library.NEG_Z);
	}

	@Override
	public void sendFluidToAll(FluidTank tank, TileEntity te) {
		this.sendFluid(this.tanks[1], this.world, this.pos.getX() + 3, this.pos.getY(), this.pos.getZ(), Library.POS_X);
		this.sendFluid(this.tanks[1], this.world, this.pos.getX() - 3, this.pos.getY(), this.pos.getZ(), Library.NEG_X);
		this.sendFluid(this.tanks[1], this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ() + 3, Library.POS_Z);
		this.sendFluid(this.tanks[1], this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ() - 3, Library.NEG_Z);
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 2,
					this.pos.getY(),
					this.pos.getZ() - 2,
					this.pos.getX() + 3,
					this.pos.getY() + 20,
					this.pos.getZ() + 3
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

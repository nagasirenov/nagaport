package com.hbm.tileentity.machine;

import java.util.List;
import java.util.Random;

import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BobMathUtil;

import api.hbm.fluid.IFluidStandardReceiver;
import api.hbm.fluidmk2.IFillableItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityRefueler extends TileEntityLoadedBase implements IFluidStandardReceiver, net.minecraft.util.ITickable {

	public double fillLevel;
	public double prevFillLevel;

	private boolean isOperating = false;
	private int operatingTime;

	public FluidTank tank;

	public TileEntityRefueler() {
		super();
		tank = new FluidTank(Fluids.KEROSENE, 100);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		if(!this.world.isRemote) {
			trySubscribe(tank.getTankType(), this.world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ, dir);

			isOperating = false;

			List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(this.pos.getX() + 0.5, this.pos.getY(), this.pos.getZ() + 0.5, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5).expand(0.5, 0.0, 0.5));

			for(EntityPlayer player : players) {
				for(int i = 0; i < 5; i++) {

					ItemStack stack = player.getEquipmentInSlot(i);
					if(stack == null) continue;

					if(fillFillable(stack)) {
						isOperating = true;
					}

					if(stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
						for(ItemStack mod : ArmorModHandler.pryMods(stack)) {
							if(mod == null) continue;

							if(fillFillable(mod)) {
								ArmorModHandler.applyMod(stack, mod);
								isOperating = true;
							}
						}
					}
				}
			}

			if(isOperating) {
				if(operatingTime % 20 == 0)
					com.hbm.sound.VanillaWorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "random.fizz", 0.2F, 0.5F);

				operatingTime++;
			} else {
				operatingTime = 0;
			}

			networkPackNT(150);
		} else {
			if(isOperating) {
				Random rand = this.world.rand;

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "fluidfill");
				data.setInteger("color", tank.getTankType().getColor());
				data.setDouble("posX", this.pos.getX() + 0.5 + rand.nextDouble() * 0.0625 + dir.offsetX * 0.5 + rot.offsetX * 0.25);
				data.setDouble("posZ", this.pos.getZ() + 0.5 + rand.nextDouble() * 0.0625 + dir.offsetZ * 0.5 + rot.offsetZ * 0.25);
				data.setDouble("posY", this.pos.getY() + 0.375);
				data.setDouble("mX", -dir.offsetX + rand.nextGaussian() * 0.1);
				data.setDouble("mZ", -dir.offsetZ + rand.nextGaussian() * 0.1);
				data.setDouble("mY", 0D);

				MainRegistry.proxy.effectNT(data);
			}

			prevFillLevel = fillLevel;

			double targetFill = (double)tank.getFill() / (double)tank.getMaxFill();
			fillLevel = BobMathUtil.interp(fillLevel, targetFill, targetFill > fillLevel || !isOperating ? 0.1F : 0.01F);
		}


	}

	private boolean fillFillable(ItemStack stack) {
		if(stack.getItem() instanceof IFillableItem) {
			IFillableItem fillable = (IFillableItem) stack.getItem();
			if(fillable.acceptsFluid(tank.getTankType(), stack)) {
				int prevFill = tank.getFill();
				tank.setFill(fillable.tryFill(tank.getTankType(), tank.getFill(), stack));
				return tank.getFill() < prevFill;
			}
		}

		return false;
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(isOperating);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		isOperating = buf.readBoolean();
		tank.deserialize(buf);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		tank.readFromNBT(nbt, "t");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		tank.writeToNBT(nbt, "t");
		return nbt;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tank };
	}
}

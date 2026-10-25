package com.hbm.tileentity.machine.rbmk;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.handler.CompatHandler;
import com.hbm.inventory.container.ContainerRBMKHeater;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.inventory.gui.GUIRBMKHeater;
import com.hbm.lib.Library;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType;
import com.hbm.util.fauxpointtwelve.DirPos;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
public class TileEntityRBMKHeater extends TileEntityRBMKSlottedBase implements IFluidStandardTransceiverMK2, SimpleComponent, CompatHandler.OCComponent {

	@Override
	public boolean isEmpty() {
		for(int i = 0; i < this.getSizeInventory(); i++) {
			if(this.getStackInSlot(i) != null && !this.getStackInSlot(i).isEmpty()) return false;
		}
		return true;
	}

	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) {}
	@Override public int getFieldCount() { return 0; }

	@Override
	public void clear() {
		for(int i = 0; i < this.getSizeInventory(); i++) {
		this.setInventorySlotContents(i, null);
		}
	}

	public FluidTank feed;
	public FluidTank steam;
	
	public TileEntityRBMKHeater() {
		super(1);
		this.feed = new FluidTank(Fluids.COOLANT, 16_000);
		this.steam = new FluidTank(Fluids.COOLANT_HOT, 16_000);
	}

	@Override
	public String getName() {
		return "container.rbmkHeater";
	}
	
	@Override
	public void update() {
		
		if(!this.world.isRemote) {
			
			feed.setType(0, slots);

			if(feed.getTankType().hasTrait(FT_Heatable.class)) {
				FT_Heatable trait = feed.getTankType().getTrait(FT_Heatable.class);
				HeatingStep step = trait.getFirstStep();
				steam.setTankType(step.typeProduced);
				double tempRange = this.heat - steam.getTankType().temperature;
				double eff = trait.getEfficiency(HeatingType.HEATEXCHANGER);
				
				if(tempRange > 0 && eff > 0) {
					double TU_PER_DEGREE = 2_000D * eff; //based on 1mB of water absorbing 200 TU as well as 0.1°C from an RBMK column
					int inputOps = feed.getFill() / step.amountReq;
					int outputOps = (steam.getMaxFill() - steam.getFill()) / step.amountProduced;
					int tempOps = (int) Math.floor((tempRange * TU_PER_DEGREE) / step.heatReq);
					int ops = Math.min(inputOps, Math.min(outputOps, tempOps));

					feed.setFill(feed.getFill() - step.amountReq * ops);
					steam.setFill(steam.getFill() + step.amountProduced * ops);
					this.heat -= (step.heatReq * ops / TU_PER_DEGREE) * trait.getEfficiency(HeatingType.HEATEXCHANGER);
				}
				
				if(eff <= 0) {
					feed.setTankType(Fluids.NONE);
					steam.setTankType(Fluids.NONE);
				}
				
			} else {
				feed.setTankType(Fluids.NONE);
				steam.setTankType(Fluids.NONE);
			}
			
			this.trySubscribe(feed.getTankType(), this.world, this.pos.getX(), this.pos.getY() - 1, this.pos.getZ(), Library.NEG_Y);
			for(DirPos pos : getOutputPos()) {
				if(this.steam.getFill() > 0) this.tryProvide(steam, this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
			}
		}
		
		super.update();
	}

	protected DirPos[] getOutputPos() {
		
		if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY() - 1, this.pos.getZ()).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(this.world) + 1, this.pos.getZ(), Library.POS_Y),
					new DirPos(this.pos.getX() + 1, this.pos.getY() - 1, this.pos.getZ(), Library.POS_X),
					new DirPos(this.pos.getX() - 1, this.pos.getY() - 1, this.pos.getZ(), Library.NEG_X),
					new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() + 1, Library.POS_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() - 1, Library.NEG_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ(), Library.NEG_Y)
			};
		} else if(com.hbm.world.WorldBlockState.read(this.world, this.pos.getX(), this.pos.getY() - 2, this.pos.getZ()).getBlock() == ModBlocks.rbmk_loader) {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(this.world) + 1, this.pos.getZ(), Library.POS_Y),
					new DirPos(this.pos.getX() + 1, this.pos.getY() - 2, this.pos.getZ(), Library.POS_X),
					new DirPos(this.pos.getX() - 1, this.pos.getY() - 2, this.pos.getZ(), Library.NEG_X),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() + 1, Library.POS_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() - 1, Library.NEG_Z),
					new DirPos(this.pos.getX(), this.pos.getY() - 3, this.pos.getZ(), Library.NEG_Y)
			};
		} else {
			return new DirPos[] {
					new DirPos(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(this.world) + 1, this.pos.getZ(), Library.POS_Y)
			};
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		feed.readFromNBT(nbt, "feed");
		steam.readFromNBT(nbt, "steam");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		feed.writeToNBT(nbt, "feed");
		steam.writeToNBT(nbt, "steam");
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		this.feed.serialize(buf);
		this.steam.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.feed.deserialize(buf);
		this.steam.deserialize(buf);
	}

	@Override
	public void onMelt(int reduce) {
		
		int count = 1 + this.world.rand.nextInt(2);
		
		for(int i = 0; i < count; i++) {
			spawnDebris(DebrisType.BLANK);
		}
		
		super.onMelt(reduce);
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.HEATEX;
	}

	@Override
	public NBTTagCompound getNBTForConsole() {
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("water", this.feed.getFill());
		data.setInteger("maxWater", this.feed.getMaxFill());
		data.setInteger("steam", this.steam.getFill());
		data.setInteger("maxSteam", this.steam.getMaxFill());
		data.setShort("type", (short)this.feed.getTankType().getID());
		data.setShort("hottype", (short)this.steam.getTankType().getID());
		return data;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {feed, steam};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {steam};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {feed};
	}

	//opencomputers stuff

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "rbmk_heater";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getHeat(Context context, Arguments args) {
		return new Object[] {heat};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getFill(Context context, Arguments args) {
		return new Object[] {feed.getFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getFillMax(Context context, Arguments args) {
		return new Object[] {feed.getMaxFill()};
	}
	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getExport(Context context, Arguments args) {
		return new Object[] {steam.getFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getExportMax(Context context, Arguments args) {
		return new Object[] {steam.getMaxFill()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getFillType(Context context, Arguments args) {
		return new Object[] {feed.getTankType().getName()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getExportType(Context context, Arguments args) {
		return new Object[] {steam.getTankType().getName()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getInfo(Context context, Arguments args) {
		return new Object[] {heat, feed.getFill(), feed.getMaxFill(), steam.getFill(), steam.getMaxFill(), feed.getTankType().getName(), steam.getTankType().getName(), this.pos.getX(), this.pos.getY(), this.pos.getZ()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getCoordinates(Context context, Arguments args) {
		return new Object[] {this.pos.getX(), this.pos.getY(), this.pos.getZ()};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerRBMKHeater(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIRBMKHeater(player.inventory, this);
	}
}

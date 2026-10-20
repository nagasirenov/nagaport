package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.container.ContainerFurnaceCombo;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.gui.GUIFurnaceCombo;
import com.hbm.inventory.recipes.CombinationRecipes;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachinePolluting;
import com.hbm.util.Tuple.Pair;

import api.hbm.fluid.IFluidStandardSender;
import api.hbm.tile.IHeatSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityFurnaceCombination extends TileEntityMachinePolluting implements IFluidStandardSender, IGUIProvider, IFluidCopiable {

	public boolean wasOn;
	public int progress;
	public static int processTime = 20_000;
	
	public int heat;
	public static int maxHeat = 100_000;
	public static double diffusion = 0.25D;
	
	public FluidTank tank;

	public TileEntityFurnaceCombination() {
		super(4, 50);
		this.tank = new FluidTank(Fluids.NONE, 24_000);
	}

	@Override
	public String getDefaultName() {
		return "container.furnaceCombination";
	}

	@Override
	public void update() {
		
		if(!this.world.isRemote) {
			this.tryPullHeat();
			
			if(this.world.getTotalWorldTime() % 20 == 0) {
				for(int i = 2; i < 6; i++) {
					ForgeDirection dir = ForgeDirection.getOrientation(i);
					ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
					
					for(int y = this.pos.getY(); y <= this.pos.getY() + 1; y++) {
						for(int j = -1; j <= 1; j++) {
							if(tank.getFill() > 0) this.sendFluid(tank, this.world, this.pos.getX() + dir.offsetX * 2 + rot.offsetX * j, y, this.pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * j, dir);
							this.sendSmoke(this.pos.getX() + dir.offsetX * 2 + rot.offsetX * j, y, this.pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * j, dir);
						}
					}
				}
	
				for(int x = this.pos.getX() - 1; x <= this.pos.getX() + 1; x++) {
					for(int z = this.pos.getZ() - 1; z <= this.pos.getZ() + 1; z++) {
						if(tank.getFill() > 0) this.sendFluid(tank, this.world, x, this.pos.getY() + 2, z, ForgeDirection.UP);
						this.sendSmoke(x, this.pos.getY() + 2, z, ForgeDirection.UP);
					}
				}
			}
			
			this.wasOn = false;
			
			tank.unloadTank(2, 3, slots);
			
			if(canSmelt()) {
				int burn = heat / 100;
				
				if(burn > 0) {
					this.wasOn = true;
					this.progress += burn;
					this.heat -= burn;
					
					if(progress >= processTime) {
						this.markChanged();
						progress -= this.processTime;
						
						Pair<ItemStack, FluidStack> pair = CombinationRecipes.getOutput(slots[0]);
						ItemStack out = pair.getKey();
						FluidStack fluid = pair.getValue();
						
						if(out != null)  {
							if(slots[1] == null) {
								slots[1] = out.copy();
							} else {
								slots[1].stackSize += out.stackSize;
							}
						}
						
						if(fluid != null) {
							if(tank.getTankType() != fluid.type) {
								tank.setTankType(fluid.type);
							}
							
							tank.setFill(tank.getFill() + fluid.fill);
						}
						
						this.decrStackSize(0, 1);
					}
					
					List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(this.pos.getX() - 0.5, this.pos.getY() + 2, this.pos.getZ() - 0.5, this.pos.getX() + 1.5, this.pos.getY() + 4, this.pos.getZ() + 1.5));
					
					for(Entity e : entities) e.setFire(5);
					
					if(this.world.getTotalWorldTime() % 10 == 0) com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY() + 1, this.pos.getZ(), "hbm:weapon.flamethrowerShoot", 0.25F, 0.5F);
					if(this.world.getTotalWorldTime() % 20 == 0) this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 3);
				}
			} else {
				this.progress = 0;
			}
			
			this.networkPackNT(50);
		} else {
			
			if(this.wasOn && this.world.rand.nextInt(15) == 0) {
				this.world.spawnParticle("lava", this.pos.getX() + 0.5 + this.world.rand.nextGaussian() * 0.5, this.pos.getY() + 2, this.pos.getZ() + 0.5 + this.world.rand.nextGaussian() * 0.5, 0, 0, 0);
			}
		}
	}
	
	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(wasOn);
		buf.writeInt(heat);
		buf.writeInt(progress);
		tank.serialize(buf);
	}
	
	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		wasOn = buf.readBoolean();
		heat = buf.readInt();
		progress = buf.readInt();
		tank.deserialize(buf);
	}
	
	public boolean canSmelt() {
		if(slots[0] == null) return false;
		Pair<ItemStack, FluidStack> pair = CombinationRecipes.getOutput(slots[0]);
		
		if(pair == null) return false;
		
		ItemStack out = pair.getKey();
		FluidStack fluid = pair.getValue();
		
		if(out != null) {
			if(slots[1] != null) {
				if(!out.isItemEqual(slots[1])) return false;
				if(out.stackSize + slots[1].stackSize > slots[1].getMaxStackSize()) return false;
			}
		}
		
		if(fluid != null) {
			if(tank.getTankType() != fluid.type && tank.getFill() > 0) return false;
			if(tank.getTankType() == fluid.type && tank.getFill()  + fluid.fill > tank.getMaxFill()) return false;
		}
		
		return true;
	}
	
	protected void tryPullHeat() {
		
		if(this.heat >= this.maxHeat) return;
		
		TileEntity con = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ()));
		
		if(con instanceof IHeatSource) {
			IHeatSource source = (IHeatSource) con;
			int diff = source.getHeatStored() - this.heat;
			
			if(diff == 0) {
				return;
			}
			
			if(diff > 0) {
				diff = (int) Math.ceil(diff * diffusion);
				source.useUpHeat(diff);
				this.heat += diff;
				if(this.heat > this.maxHeat)
					this.heat = this.maxHeat;
				return;
			}
		}
		
		this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
	}
	
	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int meta = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 0, 1 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return i == 0 && CombinationRecipes.getOutput(itemStack) != null;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return i == 1;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.tank.readFromNBT(nbt, "tank");
		this.progress = nbt.getInteger("prog");
		this.heat = nbt.getInteger("heat");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.tank.writeToNBT(nbt, "tank");
		nbt.setInteger("prog", progress);
		nbt.setInteger("heat", heat);
		return nbt;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerFurnaceCombo(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIFurnaceCombo(player.inventory, this);
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 1,
					this.pos.getY(),
					this.pos.getZ() - 1,
					this.pos.getX() + 2,
					this.pos.getY() + 2.125,
					this.pos.getZ() + 2
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tank, smoke, smoke_leaded, smoke_poison};
	}
}

package com.hbm.tileentity.machine;

import java.math.BigInteger;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.container.ContainerMachineAnnihilator;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.gui.GUIMachineAnnihilator;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.saveddata.AnnihilatorSavedData;
import com.hbm.saveddata.AnnihilatorSavedData.AnnihilatorPool;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.ParticleUtil;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityMachineAnnihilator extends TileEntityMachineBase implements IFluidStandardReceiverMK2, IControlReceiver, IGUIProvider {
	
	public String pool = "Recycling";
	public int timer;
	
	public FluidTank tank;
	public BigInteger monitorBigInt = BigInteger.ZERO;

	public TileEntityMachineAnnihilator() {
		super(11);
		
		this.tank = new FluidTank(Fluids.NONE, 2_500_000);
	}

	@Override
	public String getDefaultName() {
		return "container.annihilator";
	}

	@Override
	public void update() {
		
		if(!this.world.isRemote) {
			
			this.tank.setType(1, slots);
			
			if(this.pool != null && !this.pool.isEmpty()) {
				
				for(DirPos pos : getConPos()) {
					if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), this.world, pos);
				}
				
				AnnihilatorSavedData data = AnnihilatorSavedData.getData(this.world);
				boolean didSomething = false;
				
				if(slots[0] != null) {
					onDestroy(slots[0]);
					tryAddPayout(data.pushToPool(pool, slots[0], false));
					this.slots[0] = null;
					this.markChanged();
					didSomething = true;
				}
				if(tank.getFill() > 0) {
					FT_Polluting.pollute(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), tank.getTankType(), FluidReleaseType.BURN, tank.getFill() * 2);
					tryAddPayout(data.pushToPool(pool, tank.getTankType(), tank.getFill(), false));
					tank.setFill(0);
					this.markChanged();
					didSomething = true;
				}
				
				if(didSomething) {
					ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
					ParticleUtil.spawnGasFlame(this.world, this.pos.getX() + 0.5 - dir.offsetX * 3, this.pos.getY() + 8.75, this.pos.getZ() + 0.5 - dir.offsetZ * 3, this.world.rand.nextGaussian() * 0.05, 0.1, this.world.rand.nextGaussian() * 0.05);

					if(this.world.getTotalWorldTime() % 3 == 0)
						com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX() + 0.5 - dir.offsetX * 3, this.pos.getY() + 8.75, this.pos.getZ() + 0.5 - dir.offsetZ * 3, "hbm:weapon.flamethrowerShoot", getVolume(1F), 0.5F + this.world.rand.nextFloat() * 0.25F);
				}
				
				if(slots[8] != null) {
					if(slots[8].getItem() instanceof IItemFluidIdentifier) {
						IItemFluidIdentifier id = (IItemFluidIdentifier) slots[8].getItem();
						FluidType type = id.getType(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), slots[8]);
						monitor(data, type);
					} else {
						monitor(data, new ComparableStack(slots[8]).makeSingular());
					}
				}
				
				if(slots[9] != null) {
					ItemStack single = slots[9].copy();
					single.setCount(1);
					onDestroy(single);
					ItemStack payout = data.pushToPool(pool, single, true);
					this.decrStackSize(9, 1);
					if(payout != null) {
						if(slots[10] == null) {
							slots[10] = payout;
						} else if(slots[10] != null && slots[10].getItem() == payout.getItem() && slots[10].getItemDamage() == payout.getItemDamage() &&
								ItemStack.areItemStackTagsEqual(slots[10], payout) && slots[10].getMaxStackSize() >= slots[10].stackSize + payout.stackSize) {
							slots[10].stackSize += payout.stackSize;
						}
					}
				}
			}
			
			this.networkPackNT(25);
		}
	}
	
	public void onDestroy(ItemStack stack) {
		float radiation = HazardSystem.getHazardLevelFromStack(stack, HazardRegistry.RADIATION);
		if(radiation > 0) {
			ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
			ChunkRadiationManager.proxy.incrementRad(this.world, this.pos.getX() - dir.offsetX * 3, this.pos.getY() + 9, this.pos.getZ() - dir.offsetZ * 3, Math.min(radiation * 5F, 1_000F));
		}
	}
	
	public DirPos[] getConPos() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		
		return new DirPos[] {
				new DirPos(this.pos.getX() + dir.offsetX * 5, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 5, dir),
				new DirPos(this.pos.getX() + dir.offsetX * 3 + rot.offsetX * 2, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 3 + rot.offsetZ * 2, rot),
				new DirPos(this.pos.getX() + dir.offsetX * 3 - rot.offsetX * 2, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 3 - rot.offsetZ * 2, rot.getOpposite())
		};
	}
	
	public void monitor(AnnihilatorSavedData data, Object type) {
		AnnihilatorPool pool = data.pools.get(this.pool);
		if(pool != null) {
			this.monitorBigInt = pool.items.get(type);
			if(this.monitorBigInt == null) this.monitorBigInt = BigInteger.ZERO;
		} else {
			this.monitorBigInt = BigInteger.ZERO;
		}
	}
	
	public void tryAddPayout(ItemStack payout) {
		if(payout == null) return;
		
		for(int i = 2; i <= 7; i++) {
			if(slots[i] != null && slots[i].getItem() == payout.getItem() && slots[i].getItemDamage() == payout.getItemDamage() &&
					ItemStack.areItemStackTagsEqual(slots[i], payout) && slots[i].getMaxStackSize() >= slots[i].stackSize + payout.stackSize) {
				slots[i].stackSize += payout.stackSize;
				this.markDirty();
				return;
			}
		}
		
		for(int i = 2; i <= 7; i++) {
			if(slots[i] == null) {
				slots[i] = payout;
				this.markDirty();
				return;
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0) return true; // trash
		if(slot == 1 && stack.getItem() instanceof IItemFluidIdentifier) return true;
		if(slot == 8) return true; // monitor
		if(slot == 9) return true; // payout request
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return slot >= 2 && slot <= 7;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] {0, 2, 3, 4, 5, 6, 7};
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.tank.readFromNBT(nbt, "t");
		this.pool = nbt.getString("pool");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.tank.writeToNBT(nbt, "t");
		nbt.setString("pool", pool);
		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		ByteBufUtils.writeUTF8String(buf, this.pool == null ? "" : this.pool);
		byte[] array = this.monitorBigInt.toByteArray();
		buf.writeInt(array.length);
		for(byte b : array) buf.writeByte(b);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.pool = ByteBufUtils.readUTF8String(buf);
		byte[] array = new byte[buf.readInt()];
		for(int i = 0 ; i < array.length; i++) array[i] = buf.readByte();
		this.monitorBigInt = new BigInteger(array);
	}

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tank}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tank}; }
	@Override public ConnectionPriority getFluidPriority() { return ConnectionPriority.LOW; }

	@Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return new ContainerMachineAnnihilator(player.inventory, this); }
	@Override @SideOnly(Side.CLIENT) public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIMachineAnnihilator(player.inventory, this); }

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUsableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		if(data.hasKey("pool")) {
			String pool = data.getString("pool");
			if(pool != null && !pool.isEmpty()) {
				this.pool = pool;
				this.markChanged();
			}
		}
	}
	
	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX() - 5,
					this.pos.getY(),
					this.pos.getZ() - 5,
					this.pos.getX() + 6,
					this.pos.getY() + 8,
					this.pos.getZ() + 6
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

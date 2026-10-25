package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.entity.item.EntityMovingItem;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemStamp;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.fauxpointtwelve.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityConveyorPress extends TileEntityMachineBase implements IEnergyReceiverMK2 {

	public int usage = 100;
	public long power = 0;
	public final static long maxPower = 50000;

	public double speed = 0.125;
	public double press;
	public double renderPress;
	public double lastPress;
	private double syncPress;
	private int turnProgress;
	protected boolean isRetracting = false;
	private int delay;

	public ItemStack syncStack;

	public TileEntityConveyorPress() {
		super(1);
	}

	@Override
	public String getDefaultName() {
		return "";
	}

	@Override
	public void update() {

		if(!this.world.isRemote) {

			this.updateConnections();

			if(delay <= 0) {

				if(isRetracting) {

					if(this.canRetract()) {
						this.press -= speed;
						this.power -= this.usage;

						if(press <= 0) {
							press = 0;
							this.isRetracting = false;
							delay = 0;
						}
					}

				} else {

					if(this.canExtend()) {
						this.press += speed;
						this.power -= this.usage;

						if(press >= 1) {
							press = 1;
							this.isRetracting = true;
							delay = 5;
							this.process();
						}
					}
				}

			} else {
				delay--;
			}

			this.networkPackNT(50);
		} else {

			// approach-based interpolation, GO!
			this.lastPress = this.renderPress;

			if(this.turnProgress > 0) {
				this.renderPress = this.renderPress + ((this.syncPress - this.renderPress) / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.renderPress = this.syncPress;
			}
		}
	}

	protected void updateConnections() {
		for(DirPos pos : getConPos()) this.trySubscribe(this.world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
	}

	protected DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ(), Library.POS_X),
				new DirPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ(), Library.NEG_X),
				new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() + 1, Library.POS_Z),
				new DirPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() - 1, Library.NEG_Z),
		};
	}

	public boolean canExtend() {

		if(this.power < usage) return false;
		if(slots[0] == null) return false;

		List<EntityMovingItem> items = this.world.getEntitiesWithinAABB(EntityMovingItem.class, new AxisAlignedBB(this.pos.getX(), this.pos.getY() + 1, this.pos.getZ(), this.pos.getX() + 1, this.pos.getY() + 1.5, this.pos.getZ() + 1));
		if(items.isEmpty()) return false;

		for(EntityMovingItem item : items) {
			ItemStack stack = item.getItemStack();
			if(PressRecipes.getOutput(stack, slots[0]) != null && stack.getCount() == 1) {

				double d0 = 0.35;
				double d1 = 0.65;
				if(item.posX > this.pos.getX() + d0 && item.posX < this.pos.getX() + d1 && item.posZ > this.pos.getZ() + d0 && item.posZ < this.pos.getZ() + d1) {
					item.setPosition(this.pos.getX() + 0.5, item.posY, this.pos.getZ() + 0.5);
				}

				return true;
			}
		}

		return false;
	}

	public void process() {

		List<EntityMovingItem> items = this.world.getEntitiesWithinAABB(EntityMovingItem.class, new AxisAlignedBB(this.pos.getX(), this.pos.getY() + 1, this.pos.getZ(), this.pos.getX() + 1, this.pos.getY() + 1.5, this.pos.getZ() + 1));

		for(EntityMovingItem item : items) {
			ItemStack stack = item.getItemStack();
			ItemStack output = PressRecipes.getOutput(stack, slots[0]);

			if(output != null && stack.getCount() == 1) {
				item.setDead();
				EntityMovingItem out = new EntityMovingItem(this.world);
				out.setPosition(item.posX, item.posY, item.posZ);
				out.setItemStack(output.copy());
				this.world.spawnEntity(out);
			}
		}

		com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:block.pressOperate", getVolume(1.5F), 1.0F);

		if(slots[0].getMaxDamage() != 0) {
			slots[0].setItemDamage(slots[0].getItemDamage() + 1);
			if(slots[0].getItemDamage() >= slots[0].getMaxDamage()) {
				slots[0] = null;
			}
		}
	}

	public boolean canRetract() {
		return this.power >= usage;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeLong(this.power);
		buf.writeDouble(this.press);
		BufferUtil.writeItemStack(buf, slots[0]);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		this.power = buf.readLong();
		this.syncPress = buf.readDouble();
		this.syncStack = BufferUtil.readItemStack(buf);

		this.turnProgress = 2;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return stack.getItem() instanceof ItemStamp;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 0 };
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, net.minecraft.util.EnumFacing nativeFace) {
		int j = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.DOWN;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.power = nbt.getLong("power");
		this.press = nbt.getDouble("press");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setDouble("press", press);
		return nbt;
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					this.pos.getX() - 1,
					this.pos.getY(),
					this.pos.getZ() - 1,
					this.pos.getX() + 2,
					this.pos.getY() + 3,
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
}

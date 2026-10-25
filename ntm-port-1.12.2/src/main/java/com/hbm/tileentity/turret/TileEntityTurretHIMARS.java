package com.hbm.tileentity.turret;

import java.util.ArrayList;
import java.util.List;

import com.hbm.entity.projectile.EntityArtilleryRocket;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.inventory.gui.GUITurretHIMARS;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemAmmoHIMARS;
import com.hbm.items.weapon.ItemAmmoHIMARS.HIMARSRocket;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;

import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TileEntityTurretHIMARS extends TileEntityTurretBaseArtillery implements IGUIProvider {

	public short mode = 0;
	public static final short MODE_AUTO = 0;
	public static final short MODE_MANUAL = 1;

	public int typeLoaded = -1;
	public int ammo = 0;
	public float crane;
	public float lastCrane;

	@Override
	@SideOnly(Side.CLIENT)
	public List<ItemStack> getAmmoTypesForDisplay() {

		if(ammoStacks != null)
			return ammoStacks;

		ammoStacks = new ArrayList();

		List list = new ArrayList();
		net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> sublist = net.minecraft.util.NonNullList.create();
		ModItems.ammo_himars.getSubItems(MainRegistry.weaponTab, sublist);
		list.addAll(sublist);
		this.ammoStacks.addAll(list);

		return ammoStacks;
	}

	@Override
	protected List<Integer> getAmmoList() {
		return new ArrayList();
	}

	@Override
	public String getDefaultName() {
		return "container.turretHIMARS";
	}

	@Override
	public long getMaxPower() {
		return 1_000_000;
	}

	@Override
	public double getBarrelLength() {
		return 0.5D;
	}

	@Override
	public double getAcceptableInaccuracy() {
		return 5D; //they're guided missiles so who gives a shit
	}

	@Override
	public double getHeightOffset() {
		return 5D;
	}

	@Override
	public double getDecetorRange() {
		return 5000D;
	}

	@Override
	public double getDecetorGrace() {
		return 250D;
	}

	@Override
	public double getTurretYawSpeed() {
		return 1D;
	}

	@Override
	public double getTurretPitchSpeed() {
		return 0.5D;
	}

	@Override
	public boolean doLOSCheck() {
		return false;
	}

	@Override
	protected void alignTurret() {

		Vec3d pos = this.getTurretPos();

		Vec3d delta = new Vec3d(tPos.x - pos.x, tPos.y - pos.y, tPos.z - pos.z);
		double targetYaw = -Math.atan2(delta.x, delta.z);
		double targetPitch = Math.PI / 4D;

		this.turnTowardsAngle(targetPitch, targetYaw);
	}

	public int getSpareRocket() {

		for(int i = 1; i < 10; i++) {
			if(slots[i] != null) {
				if(slots[i].getItem() == ModItems.ammo_himars) {
					return slots[i].getItemDamage();
				}
			}
		}

		return -1;
	}

	@Override
	public void update() {

		if(this.world.isRemote) {
			this.lastRotationPitch = this.rotationPitch;
			this.lastRotationYaw = this.rotationYaw;
			this.lastCrane = this.crane;
			this.rotationPitch = this.syncRotationPitch;
			this.rotationYaw = this.syncRotationYaw;
		}

		if(!this.world.isRemote) {

			if(this.mode == this.MODE_MANUAL) {
				if(!this.targetQueue.isEmpty()) {
					this.tPos = this.targetQueue.get(0);
				}
			} else {
				this.targetQueue.clear();
			}

			this.aligned = false;

			this.updateConnections();

			if(this.target != null && !target.isEntityAlive()) {
				this.target = null;
				this.stattrak++;
			}

			if(target != null && this.mode != this.MODE_MANUAL) {
				if(!this.entityInLOS(this.target)) {
					this.target = null;
				}
			}

			if(target != null) {
				this.tPos = this.getEntityPos(target);
			} else {
				if(this.mode != this.MODE_MANUAL) {
					this.tPos = null;
				}
			}

			if(isOn() && hasPower()) {

				if(!this.hasAmmo() || this.crane > 0) {

					this.turnTowardsAngle(0, this.rotationYaw);

					if(this.aligned) {

						if(this.hasAmmo()) {
							this.crane -= 0.0125F;
						} else {
							this.crane += 0.0125F;

							if(this.crane >= 1F) {
								int available = this.getSpareRocket();

								if(available != -1) {
									HIMARSRocket type = ItemAmmoHIMARS.itemTypes[available];
									this.typeLoaded = available;
									this.ammo = type.amount;
									this.conusmeAmmo(new ComparableStack(ModItems.ammo_himars, 1, available));
								}
							}
						}
					}

					this.crane = MathHelper.clamp(this.crane, 0F, 1F);

				} else {

					if(tPos != null) {
						this.alignTurret();
					}
				}

			} else {

				this.target = null;
				this.tPos = null;
			}

			if(!isOn()) this.targetQueue.clear();

			if(this.target != null && !target.isEntityAlive()) {
				this.target = null;
				this.tPos = null;
				this.stattrak++;
			}

			if(isOn() && hasPower()) {
				searchTimer--;

				this.setPower(this.getPower() - this.getConsumption());

				if(searchTimer <= 0) {
					searchTimer = this.getDecetorInterval();

					if(this.target == null && this.mode != this.MODE_MANUAL)
						this.seekNewTarget();
				}
			} else {
				searchTimer = 0;
			}

			if(this.aligned && crane <= 0) {
				this.updateFiringTick();
			}

			this.power = Library.chargeTEFromItems(slots, 10, this.power, this.getMaxPower());

			this.networkPackNT(250);

		} else {

			//this will fix the interpolation error when the turret crosses the 360° point
			if(Math.abs(this.lastRotationYaw - this.rotationYaw) > Math.PI) {

				if(this.lastRotationYaw < this.rotationYaw)
					this.lastRotationYaw += Math.PI * 2;
				else
					this.lastRotationYaw -= Math.PI * 2;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeShort(this.mode);
		buf.writeShort(this.typeLoaded);
		buf.writeInt(this.ammo);
		buf.writeFloat(this.crane);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.mode = buf.readShort();
		this.typeLoaded = buf.readShort();
		this.ammo = buf.readInt();
		this.crane = buf.readFloat();
	}

	public boolean hasAmmo() {
		return this.typeLoaded >= 0 && this.ammo > 0;
	}

	int timer;

	@Override
	public void updateFiringTick() {

		timer++;

		int delay = 40;

		if(timer % delay == 0) {

			if(this.hasAmmo() && this.tPos != null) {
				this.spawnShell(this.typeLoaded);
				this.ammo--;
				com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:weapon.rocketFlame", 25.0F, 1.0F);
			}

			if(this.mode == this.MODE_MANUAL && !this.targetQueue.isEmpty()) {
				this.targetQueue.remove(0);
				this.tPos = null;
			}
		}
	}

	public void spawnShell(int type) {

		Vec3d pos = this.getTurretPos();
		com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(this.getBarrelLength(), 0, 0);
		vec.rotateAroundZRad((float) -this.rotationPitch);
		vec.rotateAroundYRad((float) -(this.rotationYaw + Math.PI * 0.5));

		EntityArtilleryRocket proj = new EntityArtilleryRocket(this.world);
		proj.setPositionAndRotation(pos.x + vec.x, pos.y + vec.y, pos.z + vec.z, 0.0F, 0.0F);
		proj.shoot(vec.x, vec.y, vec.z, 25F, 0.0F);

		if(this.target != null)
			proj.setTarget(this.target);
		else
			proj.setTarget(tPos.x, tPos.y, tPos.z);

		proj.setType(type);

		this.world.spawnEntity(proj);
	}

	@Override
	public void handleButtonPacket(int value, int meta) {
		if(meta == 5) {
			this.mode++;
			if(this.mode > 1)
				this.mode = 0;

			this.tPos = null;
			this.targetQueue.clear();

		} else{
			super.handleButtonPacket(value, meta);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.mode = nbt.getShort("mode");
		this.typeLoaded = nbt.getShort("type");
		this.ammo = nbt.getInteger("ammo");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setShort("mode", this.mode);
		nbt.setInteger("type", this.typeLoaded);
		nbt.setInteger("ammo", this.ammo);
		return nbt;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTurretBase(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretHIMARS(player.inventory, this);
	}
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] addCoords(Context context, Arguments args) {
		this.mode = MODE_MANUAL;
		targetQueue.add(new Vec3d(args.checkDouble(0), args.checkDouble(1), args.checkDouble(2)));
		return new Object[] {};
	}
}

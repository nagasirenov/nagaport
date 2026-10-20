package com.hbm.tileentity.turret;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Liquid;
import com.hbm.inventory.gui.GUITurretFritz;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.factory.XFactoryFlamer;
import com.hbm.items.weapon.sedna.factory.GunFactory.EnumAmmo;

import api.hbm.fluid.IFluidStandardReceiver;
import com.hbm.tileentity.IFluidCopiable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class TileEntityTurretFritz extends TileEntityTurretBaseNT implements IFluidStandardReceiver, IFluidCopiable {

	public FluidTank tank;

	public TileEntityTurretFritz() {
		super();
		this.tank = new FluidTank(Fluids.DIESEL, 16000);
	}

	@Override
	public String getDefaultName() {
		return "container.turretFritz";
	}

	@Override
	protected List<Integer> getAmmoList() {
		return null;
	}

	@SideOnly(Side.CLIENT)
	public List<ItemStack> getAmmoTypesForDisplay() {

		if(ammoStacks != null)
			return ammoStacks;

		ammoStacks = new ArrayList();

		ammoStacks.add(new ItemStack(ModItems.ammo_standard, 1, EnumAmmo.FLAME_DIESEL.ordinal()));

		for(FluidType type : Fluids.getInNiceOrder()) {
			if(type.hasTrait(FT_Combustible.class) && type.hasTrait(FT_Liquid.class)) {
				ammoStacks.add(new ItemStack(ModItems.fluid_icon, 1, type.getID()));
			}
		}

		return ammoStacks;
	}

	@Override
	public double getDecetorRange() {
		return 48D;
	}

	@Override
	public double getDecetorGrace() {
		return 2D;
	}

	@Override
	public double getTurretElevation() {
		return 45D;
	}

	@Override
	public long getMaxPower() {
		return 10000;
	}

	@Override
	public double getBarrelLength() {
		return 2.25D;
	}

	@Override
	public double getAcceptableInaccuracy() {
		return 15;
	}

	@Override
	public void updateFiringTick() {

		if(this.tank.getTankType().hasTrait(FT_Flammable.class) && this.tank.getTankType().hasTrait(FT_Liquid.class) && this.tank.getFill() >= 2) {

			FT_Flammable trait = this.tank.getTankType().getTrait(FT_Flammable.class);
			this.tank.setFill(this.tank.getFill() - 2);

			Vec3d pos = this.getTurretPos();
			com.hbm.util.Vec3NT vec = new com.hbm.util.Vec3NT(this.getBarrelLength(), 0, 0);
			vec.rotateAroundZRad((float) -this.rotationPitch);
			vec.rotateAroundYRad((float) -(this.rotationYaw + Math.PI * 0.5));

			float damage = Math.min((float) (trait.getHeatEnergy() / 500_000F), 20F);
			EntityBulletBaseMK4 proj = new EntityBulletBaseMK4(this.world, tank.getTankType() == Fluids.BALEFIRE ? XFactoryFlamer.flame_nograv_bf : XFactoryFlamer.flame_nograv, damage, 0.05F, (float) rotationYaw, (float) rotationPitch);
			proj.setPositionAndRotation(pos.x + vec.x, pos.y + vec.y, pos.z + vec.z, proj.rotationYaw, proj.rotationPitch);
			this.world.spawnEntityInWorld(proj);

			com.hbm.sound.WorldSoundEffects.play(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), "hbm:weapon.flamethrowerShoot", 2F, 1F + this.world.rand.nextFloat() * 0.5F);
		}
	}

	public int getDelay() {
		return 2;
	}

	@Override
	public void update() {
		super.update();

		if(!this.world.isRemote) {
			tank.setType(9, 9, slots);
			
			for(int i = 1; i < 9; i++) {
				tank.loadTank(i, 9, slots);
			}

			for(int i = 1; i < 10; i++) {

				if(slots[i] != null && slots[i].getItem() == ModItems.ammo_standard && slots[i].getItemDamage() == EnumAmmo.FLAME_DIESEL.ordinal()) {
					if(this.tank.getTankType() == Fluids.DIESEL && this.tank.getFill() + 1000 <= this.tank.getMaxFill()) {
						this.tank.setFill(this.tank.getFill() + 1000);
						this.decrStackSize(i, 1);
					}
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		tank.deserialize(buf);
	}

	@Override //TODO: clean this shit up
	protected void updateConnections() {
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		this.trySubscribe(this.world, this.pos.getX() + dir.offsetX * -1 + rot.offsetX * 0, this.pos.getY(), this.pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * 0, dir.getOpposite());
		this.trySubscribe(this.world, this.pos.getX() + dir.offsetX * -1 + rot.offsetX * -1, this.pos.getY(), this.pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * -1, dir.getOpposite());
		this.trySubscribe(this.world, this.pos.getX() + dir.offsetX * 0 + rot.offsetX * -2, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * -2, rot.getOpposite());
		this.trySubscribe(this.world, this.pos.getX() + dir.offsetX * 1 + rot.offsetX * -2, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * -2, rot.getOpposite());
		this.trySubscribe(this.world, this.pos.getX() + dir.offsetX * 0 + rot.offsetX * 1, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * 1, rot);
		this.trySubscribe(this.world, this.pos.getX() + dir.offsetX * 1 + rot.offsetX * 1, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * 1, rot);
		this.trySubscribe(this.world, this.pos.getX() + dir.offsetX * 2 + rot.offsetX * 0, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 0, dir);
		this.trySubscribe(this.world, this.pos.getX() + dir.offsetX * 2 + rot.offsetX * -1, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * -1, dir);

		this.trySubscribe(tank.getTankType(), this.world, this.pos.getX() + dir.offsetX * -1 + rot.offsetX * 0, this.pos.getY(), this.pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * 0, dir.getOpposite());
		this.trySubscribe(tank.getTankType(), this.world, this.pos.getX() + dir.offsetX * -1 + rot.offsetX * -1, this.pos.getY(), this.pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * -1, dir.getOpposite());
		this.trySubscribe(tank.getTankType(), this.world, this.pos.getX() + dir.offsetX * 0 + rot.offsetX * -2, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * -2, rot.getOpposite());
		this.trySubscribe(tank.getTankType(), this.world, this.pos.getX() + dir.offsetX * 1 + rot.offsetX * -2, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * -2, rot.getOpposite());
		this.trySubscribe(tank.getTankType(), this.world, this.pos.getX() + dir.offsetX * 0 + rot.offsetX * 1, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * 1, rot);
		this.trySubscribe(tank.getTankType(), this.world, this.pos.getX() + dir.offsetX * 1 + rot.offsetX * 1, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * 1, rot);
		this.trySubscribe(tank.getTankType(), this.world, this.pos.getX() + dir.offsetX * 2 + rot.offsetX * 0, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 0, dir);
		this.trySubscribe(tank.getTankType(), this.world, this.pos.getX() + dir.offsetX * 2 + rot.offsetX * -1, this.pos.getY(), this.pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * -1, dir);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.tank.readFromNBT(nbt, "diesel");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.tank.writeToNBT(nbt, "diesel");
		return nbt;
	}

	@Override
	public int[] getSlotsForFace(net.minecraft.util.EnumFacing nativeFace) {
		int side = com.hbm.lib.ForgeDirection.fromEnumFacing(nativeFace).ordinal();
		return new int[] { 1, 2, 3, 4, 5, 6, 7, 8 };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public boolean canConnect(FluidType type, ForgeDirection dir) {
		return dir != ForgeDirection.UP && dir != ForgeDirection.DOWN;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretFritz(player.inventory, this);
	}

	@Override
	public FluidTank getTankToPaste() {
		return tank;
	}
}

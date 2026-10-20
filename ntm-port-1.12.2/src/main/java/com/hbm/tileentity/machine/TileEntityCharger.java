package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.lib.ForgeDirection;

public class TileEntityCharger extends TileEntityLoadedBase implements IEnergyReceiverMK2, IBufPacketReceiver, net.minecraft.util.ITickable {

	private List<EntityPlayer> players = new ArrayList();
	private long charge = 0;
	private int lastOp = 0;

	boolean particles = false;

	public int usingTicks;
	public int lastUsingTicks;
	public static final int delay = 20;

	@Override
	public void update() {

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getOpposite();

		if(!this.world.isRemote) {
			this.trySubscribe(this.world, this.pos.getX() + dir.offsetX, this.pos.getY(), this.pos.getZ() + dir.offsetZ, dir);

			players = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.pos.getX() + 0.5, this.pos.getY(), this.pos.getZ() + 0.5, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5).expand(0.5, 0.0, 0.5));

			charge = 0;

			for(EntityPlayer player : players) {

				for(int i = 0; i < 5; i++) {

					ItemStack stack = com.hbm.util.LivingEquipmentRead.getEquipmentNullable(player, i);

					if(stack != null && stack.getItem() instanceof IBatteryItem) {
						IBatteryItem battery = (IBatteryItem) stack.getItem();
						charge += Math.min(battery.getMaxCharge(stack) - battery.getCharge(stack), battery.getChargeRate(stack));
					}
				}
			}

			particles = lastOp > 0;

			if(particles) {

				lastOp--;

				if(this.world.getTotalWorldTime() % 20 == 0)
					com.hbm.sound.VanillaWorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "random.fizz", 0.2F, 0.5F);
			}

			networkPackNT(50);
		}

		lastUsingTicks = usingTicks;

		if((charge > 0 || particles) && usingTicks < delay) {
			usingTicks++;
			if(usingTicks == 2)
				com.hbm.sound.VanillaWorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "tile.piston.out", 0.5F, 0.5F);
		}
		if((charge <= 0 && !particles) && usingTicks > 0) {
			usingTicks--;
			if(usingTicks == 4)
				com.hbm.sound.VanillaWorldSoundEffects.play(this.world, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, "tile.piston.in", 0.5F, 0.5F);
		}

		if(particles) {
			Random rand = this.world.rand;
			this.world.spawnParticle(net.minecraft.util.EnumParticleTypes.CRIT_MAGIC,
					this.pos.getX() + 0.5 + rand.nextDouble() * 0.0625 + dir.offsetX * 0.75,
					this.pos.getY() + 0.1,
					this.pos.getZ() + 0.5 + rand.nextDouble() * 0.0625 + dir.offsetZ * 0.75,
					-dir.offsetX + rand.nextGaussian() * 0.1,
					0,
					-dir.offsetZ + rand.nextGaussian() * 0.1);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(this.charge);
		buf.writeBoolean(this.particles);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.charge = buf.readLong();
		this.particles = buf.readBoolean();
	}

	@Override
	public long getPower() {
		return 0;
	}

	@Override
	public long getMaxPower() {
		return charge;
	}

	@Override
	public void setPower(long power) { }

	@Override
	public long transferPower(long power) {

		if(this.usingTicks < delay || power == 0)
			return power;

		for(EntityPlayer player : players) {

			for(int i = 0; i < 5; i++) {

				ItemStack stack = com.hbm.util.LivingEquipmentRead.getEquipmentNullable(player, i);

				if(stack != null && stack.getItem() instanceof IBatteryItem) {
					IBatteryItem battery = (IBatteryItem) stack.getItem();

					long toCharge = Math.min(battery.getMaxCharge(stack) - battery.getCharge(stack), battery.getChargeRate(stack));
					toCharge = Math.min(toCharge, Math.max(power / 5, 1));
					battery.chargeBattery(stack, toCharge);
					power -= toCharge;

					lastOp = 4;
				}
			}
		}

		return power;
	}
}

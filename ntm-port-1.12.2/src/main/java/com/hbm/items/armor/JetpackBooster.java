package com.hbm.items.armor;

import java.util.List;

import com.hbm.dim.CelestialBody;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;

import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.ArmorUtil;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class JetpackBooster extends JetpackFueledBase {

	public JetpackBooster(FluidType fuel, int maxFuel) {
		super(fuel, maxFuel);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
		return "hbm:textures/models/JetPack.png";
	}

	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

		HbmPlayerProps props = HbmPlayerProps.getData(player);

		if(!world.isRemote) {

			if(getFuel(stack) > 0 && props.isJetpackActive()) {

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "jetpack");
				data.setInteger("player", player.getEntityId());
				data.setInteger("mode", 1);
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, player.posX, player.posY, player.posZ), new TargetPoint(world.provider.getDimension(), player.posX, player.posY, player.posZ, 100));
			}
		}

		if(getFuel(stack) > 0 && props.isJetpackActive()) {
			float gravity = CelestialBody.getGravity(player);

			if(player.motionY < 0.6D)
				player.motionY += 0.1D * Math.min(gravity / AstronomyUtil.STANDARD_GRAVITY, 1);

			Vec3d look = player.getLookVec();

			if(new Vec3d(player.motionX, player.motionY, player.motionZ).length() < 5) {
				player.motionX += look.x * 0.25;
				player.motionY += look.y * 0.25;
				player.motionZ += look.z * 0.25;

				if(look.y > 0)
					player.fallDistance = 0;
			}

			com.hbm.sound.WorldSoundEffects.play(world, player.posX, player.posY, player.posZ, "hbm:weapon.flamethrowerShoot", 0.25F, 1.0F);
			this.useUpFuel(player, stack, 1);
			ArmorUtil.resetFlightTime(player);
		}
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

		list.add("High-powered vectorized jetpack.");
		list.add("Highly increased fuel consumption.");

		super.addInformation(stack, player, list, ext);
	}
}

package com.hbm.tileentity.machine;

import java.util.List;
import java.util.Random;

import com.hbm.config.RadiationConfig;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.hazard.type.HazardTypeNeutron;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityDecon extends TileEntity implements net.minecraft.util.ITickable {

	@Override
	public void update() {
		if(!this.world.isRemote) {
			List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.pos.getX() - 0.5, this.pos.getY(), this.pos.getZ() - 0.5, this.pos.getX() + 1.5, this.pos.getY() + 2, this.pos.getZ() + 1.5));
			if(!entities.isEmpty()) {
				for(EntityLivingBase e : entities) {
					HbmLivingProps.incrementRadiation(e, -0.5F);
					e.removePotionEffect(HbmPotion.radiation);
					HbmLivingProps.getCont(e).clear();
				}

				deconNeutron(entities);
			}
		} else {
			Random rand = this.world.rand;

			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("type", "vanillaExt");
			nbt.setString("mode", "townaura");
			nbt.setDouble("posX", this.pos.getX() + 0.125 + rand.nextDouble() * 0.75);
			nbt.setDouble("posY", this.pos.getY() + 1.1);
			nbt.setDouble("posZ", this.pos.getZ() + 0.125 + rand.nextDouble() * 0.75);
			nbt.setDouble("mX", 0.0);
			nbt.setDouble("mY", 0.04);
			nbt.setDouble("mZ", 0.0);
			MainRegistry.proxy.effectNT(nbt);
		}
	}

	private void deconNeutron(List<EntityLivingBase> entities) {
		for(EntityLivingBase e : entities) {
			float neut = HbmLivingProps.getNeutronActivation(e);

			if(neut > 0 && !RadiationConfig.disableNeutron) {
				ContaminationUtil.contaminate(e, HazardType.RADIATION, ContaminationType.RAD_BYPASS, neut / 20F);
				HbmLivingProps.setNeutronActivation(e, neut * 0.899916F);// 20 minute half life not  really
			}

			if(e instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) e;
				for(int i = 0; i < player.inventory.mainInventory.size(); i++) {
					HazardTypeNeutron.decay(player.inventory.getStackInSlot(i), 0.899916F);
				}
				for(int i = 0; i < player.inventory.armorInventory.size(); i++) {
					HazardTypeNeutron.decay(player.inventory.armorItemInSlot(i), 0.899916F);
				}
			}
		}
	}
}

package com.hbm.tileentity.machine;

import java.util.List;
import java.util.Random;

import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;

import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityBroadcaster extends TileEntityLoadedBase implements net.minecraft.util.ITickable {

	private AudioWrapper audio;

	@Override
	public void update() {

		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(this.pos.getX() + 0.5 - 25, this.pos.getY() + 0.5 - 25, this.pos.getZ() + 0.5 - 25, this.pos.getX() + 0.5 + 25, this.pos.getY() + 0.5 + 25, this.pos.getZ() + 0.5 + 25));

		for(int i = 0; i < list.size(); i++) {
			if(list.get(i) instanceof EntityLivingBase) {
				EntityLivingBase e = (EntityLivingBase)list.get(i);
				double d = Math.sqrt(Math.pow(e.posX - (this.pos.getX() + 0.5), 2) + Math.pow(e.posY - (this.pos.getY() + 0.5), 2) + Math.pow(e.posZ - (this.pos.getZ() + 0.5), 2));

				if(d <= 25) {
					if(e.getActivePotionEffect(net.minecraft.init.MobEffects.NAUSEA) == null || e.getActivePotionEffect(net.minecraft.init.MobEffects.NAUSEA).getDuration() < 100)
						e.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.NAUSEA, 300, 0));
				}

				if(d <= 15) {
					double t = (15 - d) / 15 * 10;
					e.attackEntityFrom(ModDamageSource.broadcast, (float) t);
				}
			}
		}

		if (this.world.isRemote) {
			if(audio == null) {
				audio = createAudioLoop();
				audio.startSound();
			} else if(!audio.isPlaying()) {
				audio = rebootAudio(audio);
			}

			audio.keepAlive();
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		Random rand = new Random(this.pos.getX() + this.pos.getY() + this.pos.getZ());
		return MainRegistry.proxy.getLoopedSound("hbm:block.broadcast" + (rand.nextInt(3) + 1), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 25F, 25F, 1.0F, 20);
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX(),
					this.pos.getY(),
					this.pos.getZ(),
					this.pos.getX() + 1,
					this.pos.getY() + 2,
					this.pos.getZ() + 1
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

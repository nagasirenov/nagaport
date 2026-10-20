package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.particle.ParticleExplosionSmall;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import com.hbm.particle.NativeVanillaParticles;
import com.hbm.main.ModSoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class ExplosionSmallCreator implements IParticleCreator {
	
	public static final double speedOfSound = (17.15D) * 0.5;
	
	public static void composeEffect(World world, double x, double y, double z, int cloudCount, float cloudScale, float cloudSpeedMult) {
		
		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "explosionSmall");
		data.setInteger("cloudCount", cloudCount);
		data.setFloat("cloudScale", cloudScale);
		data.setFloat("cloudSpeedMult", cloudSpeedMult);
		data.setInteger("debris", 15);
		IParticleCreator.sendPacket(world, x, y, z, 200, data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

		int cloudCount = data.getInteger("cloudCount");
		float cloudScale = data.getFloat("cloudScale");
		float cloudSpeedMult = data.getFloat("cloudSpeedMult");
		int debris = data.getInteger("debris");
		
		float dist = (float) player.getDistance(x, y, z);
		float soundRange = 200F;

		if(dist <= soundRange) {
			net.minecraft.util.SoundEvent sound = dist <= soundRange * 0.4 ? ModSoundEvents.WEAPON_EXPLOSION_SMALL_NEAR : ModSoundEvents.WEAPON_EXPLOSION_SMALL_FAR;
			PositionedSoundRecord positionedsoundrecord = new PositionedSoundRecord(sound, SoundCategory.BLOCKS, 100F, 0.9F + rand.nextFloat() * 0.2F, (float) x, (float) y, (float) z);
			Minecraft.getMinecraft().getSoundHandler().playDelayedSound(positionedsoundrecord, (int) (dist / speedOfSound));
		}
		
		for(int i = 0; i < cloudCount; i++) {
			ParticleExplosionSmall particle = new ParticleExplosionSmall(world, x, y, z, cloudScale, cloudSpeedMult);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
		
		Block b = Blocks.AIR;
		int meta = 0;
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			IBlockState state = world.getBlockState(new BlockPos((int) Math.floor(x) + dir.offsetX, (int) Math.floor(y) + dir.offsetY, (int) Math.floor(z) + dir.offsetZ));
			b = state.getBlock();
			meta = b.getMetaFromState(state);
			if(b != Blocks.AIR) break;
		}
		
		if(b != Blocks.AIR) for(int i = 0; i < debris; i++) {
			NativeVanillaParticles.BlockDust fx = new NativeVanillaParticles.BlockDust(world, x, y + 0.1, z, world.rand.nextGaussian() * 0.2, 0.5F + world.rand.nextDouble() * 0.7, world.rand.nextGaussian() * 0.2, b, meta);
			fx.multipleParticleScaleBy(2);
			fx.setMaxAge(50 + rand.nextInt(20));
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}
}

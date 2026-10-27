package com.hbm.potion;

import java.lang.reflect.Field;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.PotionConfig;
import com.hbm.config.ServerConfig;
import com.hbm.entity.mob.EntityTaintCrab;
import com.hbm.entity.mob.EntityCreeperTainted;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public class HbmPotion extends Potion {
	private static boolean initialized;

	public static HbmPotion taint;
	public static HbmPotion radiation;
	public static HbmPotion bang;
	public static HbmPotion mutation;
	public static HbmPotion radx;
	public static HbmPotion lead;
	public static HbmPotion radaway;
	//public static HbmPotion telekinesis;
	public static HbmPotion phosphorus;
	public static HbmPotion stability;
	public static HbmPotion potionsickness;
	public static HbmPotion death;

	public static HbmPotion run;

	public static HbmPotion nitan;
	public static HbmPotion flashbang;

	public static HbmPotion slippery; //t


	public HbmPotion(int id, boolean isBad, int color) {
		super(isBad, color);
	}

	public static synchronized void init() {
		if(initialized) return;

		taint = registerPotion(PotionConfig.taintID, true, 0x800080, "potion.hbm_taint", 0, 0);
		radiation = registerPotion(PotionConfig.radiationID, true, 0x84C128, "potion.hbm_radiation", 1, 0);
		bang = registerPotion(PotionConfig.bangID, true, 0x111111, "potion.hbm_bang", 3, 0);
		mutation = registerPotion(PotionConfig.mutationID, false, 0x800080, "potion.hbm_mutation", 2, 0);
		radx = registerPotion(PotionConfig.radxID, false, 0xBB4B00, "potion.hbm_radx", 5, 0);
		lead = registerPotion(PotionConfig.leadID, true, 0x767682, "potion.hbm_lead", 6, 0);
		radaway = registerPotion(PotionConfig.radawayID, false, 0xBB4B00, "potion.hbm_radaway", 7, 0);
		//telekinesis = registerPotion(PotionConfig.telekinesisID, true, 0x00F3FF, "potion.hbm_telekinesis", 0, 1);
		phosphorus = registerPotion(PotionConfig.phosphorusID, true, 0xFFFF00, "potion.hbm_phosphorus", 1, 1);
		stability = registerPotion(PotionConfig.stabilityID, false, 0xD0D0D0, "potion.hbm_stability", 2, 1);
		potionsickness = registerPotion(PotionConfig.potionsicknessID, false, 0xff8080, "potion.hbm_potionsickness", 3, 1);
		death = registerPotion(PotionConfig.deathID, false, 1118481, "potion.hbm_death", 4, 1);
		run = registerPotion(PotionConfig.runID, true, 1118481, "potion.hbm_run", 14, 0);
		nitan = registerPotion(PotionConfig.nitanID, false, 8388736, "potion.hbm_nitan", 3, 1);
		flashbang = registerPotion(PotionConfig.flashbangID, false, 0xD0D0D0, "potion.hbm_flashbang", 15, 1);
		slippery = registerPotion(PotionConfig.slipperyID, false, 0xD0D0D0, "potion.hbm_slippery", 15, 0);
		initialized = true;
	}

	public static HbmPotion registerPotion(int id, boolean isBad, int color, String name, int x, int y) {


		HbmPotion effect = new HbmPotion(id, isBad, color);
		effect.setPotionName(name);
		effect.setIconIndex(x, y);
		String registryPath = name.startsWith("potion.") ? name.substring("potion.".length()) : name;
		effect.setRegistryName(new ResourceLocation(RefStrings.MODID, registryPath));

		return effect;
	}

	@SubscribeEvent
	public static void registerPotions(RegistryEvent.Register<Potion> event) {
		if(!initialized) {
			throw new IllegalStateException("HBM potions were not initialized before the potion registry event");
		}

		event.getRegistry().registerAll(
				taint,
				radiation,
				bang,
				mutation,
				radx,
				lead,
				radaway,
				phosphorus,
				stability,
				potionsickness,
				death,
				run,
				nitan,
				flashbang,
				slippery);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getStatusIconIndex() {
		ResourceLocation loc = new ResourceLocation("hbm","textures/gui/potions.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(loc);
		return super.getStatusIconIndex();
	}

	public void performEffect(EntityLivingBase entity, int level) {

		if(entity.world.isRemote) return;

		if(this == taint) {

			if(!(entity instanceof EntityCreeperTainted) && !(entity instanceof EntityTaintCrab) && entity.world.rand.nextInt(40) == 0)
				entity.attackEntityFrom(ModDamageSource.taint, (level + 1));

			if(ServerConfig.TAINT_TRAILS.get() && !entity.world.isRemote) {

				int x = (int) Math.floor(entity.posX);
				int y = (int) Math.floor(entity.posY);
				int z = (int) Math.floor(entity.posZ);

				net.minecraft.block.state.IBlockState nativeAirState = com.hbm.world.WorldBlockState.read(entity.world, x, y - 1, z);
				Block b = nativeAirState.getBlock();
				if(y > 1 && b.isNormalCube(nativeAirState) && !b.isAir(nativeAirState, entity.world, new net.minecraft.util.math.BlockPos(x, y - 1, z))) {
					com.hbm.world.WorldBlockState.writeLegacyBlock(entity.world, x, y - 1, z, ModBlocks.taint, 14, 2);
				}
			}
		}
		if(this == radiation) {
			ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, (float)(level + 1F) * 0.05F);
		}
		if(this == radaway) {
			HbmLivingProps.incrementRadiation(entity, -(level + 1));

		}
		if(this == slippery) {
			entity.motionY += 0.026D;

		}
		if(this == bang) {

			entity.attackEntityFrom(ModDamageSource.bang, 1000);
			entity.setHealth(0.0F);

			if (!(entity instanceof EntityPlayer))
				entity.setDead();

			com.hbm.sound.WorldSoundEffects.play(entity.world, entity.posX, entity.posY, entity.posZ, "hbm:weapon.laserBang", 100.0F, 1.0F);
			ExplosionLarge.spawnParticles(entity.world, entity.posX, entity.posY, entity.posZ, 10);

			if(entity instanceof EntityCow) {
				EntityCow cow = (EntityCow) entity;
				int toDrop = cow.isChild() ? 10 : 3;
				cow.entityDropItem(new ItemStack(ModItems.cheese, toDrop), 1.0F);
			}
		}
		if(this == run) {

			entity.attackEntityFrom(ModDamageSource.run, 1000);
			entity.setHealth(0.0F);
			//World world = Minecraft.getMinecraft().theWorld;

			new ExplosionVNT(entity.world, entity.posX, entity.posY, entity.posZ, 12).makeAmat().explode();
			com.hbm.sound.WorldSoundEffects.play(entity.world, entity.posX, entity.posY, entity.posZ, "hbm:weapon.mukeExplosion", 100.0F, 1.0F);

			if (!(entity instanceof EntityPlayer))
				entity.setDead();
		}
		if(this == lead) {
			entity.attackEntityFrom(ModDamageSource.lead, (level + 1));
		}
		if(this == phosphorus) {
			entity.setFire(1);
		}
        if(this == nitan && !entity.world.isRemote) {
        	if(entity instanceof EntityPlayer) {
				HbmPlayerProps props = HbmPlayerProps.getData((EntityPlayer) entity);

				if (props.nitanCount == 3) {
					entity.attackEntityFrom(ModDamageSource.nitan, 1000);
				}
			}

		}
		if(this == flashbang && !entity.world.isRemote){
			if(entity instanceof EntityZombie || entity instanceof EntitySkeleton){
				entity.setFire(20);
				}
			entity.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SLOWNESS,5,10));
		}
	}

	public boolean isReady(int par1, int par2) {

		if(this == taint) {
			return par1 % 2 == 0;
		}


		if(this == radiation || this == radaway || this == phosphorus || this == nitan || this == slippery) {

			return true;
		}

		if(this == bang) {
			return par1 <= 10;
		}
		if(this == run) {
			return par1 <= 10;
		}

		if(this == lead) {
			int k = 60;
			return k > 0 ? par1 % k == 0 : true;
		}

		return false;
	}

	public static boolean getIsBadEffect(Potion potion) {

		try {
			Field isBadEffect = ReflectionHelper.findField(Potion.class, "isBadEffect", "field_76418_K");
			boolean ret = isBadEffect.getBoolean(potion);
			return ret;

		} catch (Exception x) {
			return false;
		}
	}
}

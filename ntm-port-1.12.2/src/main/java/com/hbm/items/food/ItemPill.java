package com.hbm.items.food;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.config.VersatileConfig;
import com.hbm.entity.logic.EntityBomber;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.packet.PacketDispatcher;
import com.hbm.potion.HbmPotion;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;

public class ItemPill extends ItemFood {

	public ItemPill(int hunger) {
		super(hunger, false);
		this.setAlwaysEdible();
	}

	Random rand = new Random();

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		if(!world.isRemote) {

			VersatileConfig.applyPotionSickness(player, 5);

			if(this == ModItems.pill_iodine) {
				player.removePotionEffect(net.minecraft.init.MobEffects.BLINDNESS);
				player.removePotionEffect(net.minecraft.init.MobEffects.NAUSEA);
				player.removePotionEffect(net.minecraft.init.MobEffects.MINING_FATIGUE);
				player.removePotionEffect(net.minecraft.init.MobEffects.HUNGER);
				player.removePotionEffect(net.minecraft.init.MobEffects.SLOWNESS);
				player.removePotionEffect(net.minecraft.init.MobEffects.POISON);
				player.removePotionEffect(net.minecraft.init.MobEffects.WEAKNESS);
				player.removePotionEffect(net.minecraft.init.MobEffects.WITHER);
				player.removePotionEffect(HbmPotion.radiation);
			}

			if(this == ModItems.plan_c) {
				for(int i = 0; i < 10; i++)
					player.attackEntityFrom(rand.nextBoolean() ? ModDamageSource.euthanizedSelf : ModDamageSource.euthanizedSelf2, 1000);
			}

			if(this == ModItems.pill_red) {
				player.addPotionEffect(new PotionEffect(HbmPotion.death, 60 * 60 * 20, 0));
			}

			if(this == ModItems.radx) {
				player.addPotionEffect(new PotionEffect(HbmPotion.radx, 3 * 60 * 20, 0));
			}

			if(this == ModItems.siox) {
				HbmLivingProps.setAsbestos(player, 0);
				HbmLivingProps.setBlackLung(player, Math.min(HbmLivingProps.getBlackLung(player), HbmLivingProps.maxBlacklung / 5));
			}

			if(this == ModItems.pill_herbal) {
				HbmLivingProps.setAsbestos(player, 0);
				HbmLivingProps.setBlackLung(player, Math.min(HbmLivingProps.getBlackLung(player), HbmLivingProps.maxBlacklung / 5));
				HbmLivingProps.incrementRadiation(player, -100F);

				player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.NAUSEA, 10 * 20, 0));
				player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.WEAKNESS, 10 * 60 * 20, 2));
				player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.MINING_FATIGUE, 10 * 60 * 20, 2));
				player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.POISON, 5 * 20, 2));

				PotionEffect eff = new PotionEffect(HbmPotion.potionsickness, 10 * 60 * 20);
				eff.setCurativeItems(new ArrayList());
				player.addPotionEffect(eff);
			}

			if(this == ModItems.xanax) {
				float digamma = HbmLivingProps.getDigamma(player);
				HbmLivingProps.setDigamma(player, Math.max(digamma - 0.5F, 0F));
			}
			if(this == ModItems.animan) {
				int x = (int)MathHelper.floor(player.posX);
				int y = (int)MathHelper.floor(player.posY);
				int z = (int)MathHelper.floor(player.posZ);
				com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:player.oil", 1.0F, 1.0F);

				player.addPotionEffect(new PotionEffect(HbmPotion.run, 10 * 20, 0));

				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setString("mode", "normal");
				nbt.setInteger("count", 25);
				nbt.setInteger("entity", player.getEntityId());
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(player.dimension, x, y, z, 25));

				com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:player.vomit", 1.0F, 1.0F);
				player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.HUNGER, 60, 19));

			}
			if(this == ModItems.chocolate) {
				if(rand.nextInt(25) == 0) {
					player.attackEntityFrom(ModDamageSource.overdose, 1000);
				}
				player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.HASTE, 60 * 20, 3));
				player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 60 * 20, 3));
				player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 60 * 20, 3));
			}

			if(this == ModItems.fmn) {
				float digamma = HbmLivingProps.getDigamma(player);
				HbmLivingProps.setDigamma(player, Math.min(digamma, 2F));
				player.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.BLINDNESS, 60, 0));
			}

			if(this == ModItems.five_htp) {
				HbmLivingProps.setDigamma(player, 0);
				player.addPotionEffect(new PotionEffect(HbmPotion.stability, 10 * 60 * 20, 0));
			}
		}
	}



	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		String unloc = this.getTranslationKey() + ".desc"; //this should really be an interface...
		String loc = I18nUtil.resolveKey(unloc);

		if(!unloc.equals(loc)) {
			String[] locs = loc.split("\\$");

			for(String s : locs) {
				list.add(s);
			}
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack p_77626_1_) {
		return 10;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if(!VersatileConfig.hasPotionSickness(player)) {
			player.setActiveHand(hand);
			return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.SUCCESS, stack);
		}
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, stack);
	}

}

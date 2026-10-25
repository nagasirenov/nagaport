package com.hbm.items.special;

import java.util.function.BiConsumer;

import com.hbm.config.VersatileConfig;
import com.hbm.items.ItemCustomLore;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import com.hbm.util.EnchantmentUtil;
import com.hbm.util.Tuple.Pair;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class ItemSimpleConsumable extends ItemCustomLore {

	//if java is giving me the power of generics and delegates then i'm going to use them, damn it!
	private BiConsumer<ItemStack, EntityPlayer> useAction;
	private BiConsumer<ItemStack, EntityPlayer> useActionServer;
	private BiConsumer<ItemStack, Pair<EntityLivingBase, EntityLivingBase>> hitAction;
	private BiConsumer<ItemStack, Pair<EntityLivingBase, EntityLivingBase>> hitActionServer;

	@Override
	public ItemSimpleConsumable setTranslationKey(String key) {
		super.setTranslationKey(key);
		return this;
	}

	@Override
	public ItemSimpleConsumable setFull3D() {
		super.setFull3D();
		return this;
	}

	public ItemSimpleConsumable setTextureName(String texture) {
		com.hbm.render.item.NativeItemTextureRegistry.texture(this, texture);
		return this;
	}

	@Override
	public ItemSimpleConsumable setRarity(EnumRarity rarity) {
		super.setRarity(rarity);
		return this;
	}

	@Override
	public ItemSimpleConsumable setEffect() {
		super.setEffect();
		return this;
	}
	public ItemSimpleConsumable() {
		this.setCreativeTab(MainRegistry.consumableTab);
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(this.useAction != null)
			this.useAction.accept(stack, player);

		if(!world.isRemote && this.useActionServer != null)
			this.useActionServer.accept(stack, player);

		return stack;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase entityPlayer) {

		if(this.hitAction != null)
			this.hitAction.accept(stack, new Pair(entity, entityPlayer));

		if(!entity.world.isRemote && this.hitActionServer != null)
			this.hitActionServer.accept(stack, new Pair(entity, entityPlayer));

		return false;
	}

	public static void giveSoundAndDecrement(ItemStack stack, EntityLivingBase entity, String sound, ItemStack container) {
		stack.shrink(1);
		entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, com.hbm.main.ModSoundEvents.resolve(sound), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
		ItemSimpleConsumable.tryAddItem(entity, container);
	}

	public static void addPotionEffect(EntityLivingBase entity, Potion effect, int duration, int level) {

		if(!entity.isPotionActive(effect)) {
			entity.addPotionEffect(new PotionEffect(effect, duration, level));
		} else {
			int d = entity.getActivePotionEffect(effect).getDuration() + duration;
			entity.addPotionEffect(new PotionEffect(effect, d, level));
		}
	}

	public static void tryAddItem(EntityLivingBase entity, ItemStack stack) {
		if(entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			if(!player.inventory.addItemStackToInventory(stack)) {
				player.dropItem(stack, false);
			}
		}
	}

	public static void doRadaway(ItemStack stack, EntityPlayer user, int duration) {
		giveSoundAndDecrement(stack, user, "hbm:item.radaway", new ItemStack(ModItems.iv_empty));
		addPotionEffect(user, HbmPotion.radaway, duration, 0);
	}

	//this formatting style probably already has a name but i will call it "the greg"
	public ItemSimpleConsumable setUseAction(		BiConsumer<ItemStack, EntityPlayer> delegate) {								this.useAction = delegate;			return this; }
	public ItemSimpleConsumable setUseActionServer(	BiConsumer<ItemStack, EntityPlayer> delegate) {								this.useActionServer = delegate;	return this; }
	public ItemSimpleConsumable setHitAction(		BiConsumer<ItemStack, Pair<EntityLivingBase, EntityLivingBase>> delegate) {	this.hitAction = delegate;			return this; }
	public ItemSimpleConsumable setHitActionServer(	BiConsumer<ItemStack, Pair<EntityLivingBase, EntityLivingBase>> delegate) {	this.hitActionServer = delegate;	return this; }

	public static void init() {

		/// SYRINGES ///
		ModItems.syringe_antidote = new ItemSimpleConsumable()
				.setUseActionServer((stack, user) -> { effectAntidote(stack, user, user); }).setHitActionServer((stack, pair) -> { effectAntidote(stack, pair.key, pair.value); })
				.setTranslationKey("syringe_antidote").setFull3D();

		ModItems.syringe_poison = new ItemSimpleConsumable()
				.setUseActionServer((stack, user) -> { effectPoison(stack, user, user); }).setHitActionServer((stack, pair) -> { effectPoison(stack, pair.key, pair.value); })
				.setTranslationKey("syringe_poison").setFull3D();

		ModItems.syringe_awesome = new ItemSimpleConsumable()
				.setUseActionServer((stack, user) -> { effectAwesome(stack, user, user); }).setHitActionServer((stack, pair) -> { effectAwesome(stack, pair.key, pair.value); })
				.setRarity(EnumRarity.UNCOMMON).setEffect().setTranslationKey("syringe_awesome").setFull3D();

		/// BLOOD BAGS ///
		ModItems.iv_empty = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			giveSoundAndDecrement(stack, user, "hbm:item.syringe", new ItemStack(ModItems.iv_blood));
			user.setHealth(Math.max(user.getHealth() - 5F, 0F));
			if(user.getHealth() <= 0) user.onDeath(DamageSource.MAGIC);
		}).setTranslationKey("iv_empty");

		ModItems.iv_blood = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			giveSoundAndDecrement(stack, user, "hbm:item.radaway", new ItemStack(ModItems.iv_empty));
			user.heal(5F);
		}).setTranslationKey("iv_blood");

		ModItems.iv_xp_empty = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			if(EnchantmentUtil.getTotalExperience(user) >= 100) {
				giveSoundAndDecrement(stack, user, "hbm:item.syringe", new ItemStack(ModItems.iv_xp));
				EnchantmentUtil.setExperience(user, EnchantmentUtil.getTotalExperience(user) - 100);
			}
		}).setTranslationKey("iv_xp_empty");

		ModItems.iv_xp = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			giveSoundAndDecrement(stack, user, "random.orb", new ItemStack(ModItems.iv_xp_empty));
			EnchantmentUtil.addExperience(user, 100, false);
		}).setTranslationKey("iv_xp");

		/// RADAWAY ///
		ModItems.radaway = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			doRadaway(stack, user, 140);
		}).setTranslationKey("radaway");

		ModItems.radaway_strong = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			doRadaway(stack, user, 350);
		}).setTranslationKey("radaway_strong");

		ModItems.radaway_flush = new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			doRadaway(stack, user, 500);
		}).setTranslationKey("radaway_flush");
	}

	public static void effectAntidote(ItemStack stack, EntityLivingBase affected, EntityLivingBase source) {
		if(VersatileConfig.hasPotionSickness(affected)) return;
		affected.clearActivePotions();
		giveSoundAndDecrement(stack, source, "hbm:item.syringe", new ItemStack(ModItems.syringe_empty));
		VersatileConfig.applyPotionSickness(affected, 5);
	}

	public static void effectPoison(ItemStack stack, EntityLivingBase affected, EntityLivingBase source) {
		if(affected == source) affected.attackEntityFrom(affected.getRNG().nextBoolean() ? ModDamageSource.euthanizedSelf : ModDamageSource.euthanizedSelf2, 30);
		else affected.attackEntityFrom(ModDamageSource.euthanized(source, source), 30);
		giveSoundAndDecrement(stack, source, "hbm:item.syringe", new ItemStack(ModItems.syringe_empty));
	}

	public static void effectAwesome(ItemStack stack, EntityLivingBase affected, EntityLivingBase source) {
		if(VersatileConfig.hasPotionSickness(affected)) return;
		giveSoundAndDecrement(stack, source, "hbm:item.syringe", new ItemStack(ModItems.syringe_empty));
		affected.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.REGENERATION, 50 * 20, 9));
		affected.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.RESISTANCE, 50 * 20, 9));
		affected.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.FIRE_RESISTANCE, 50 * 20, 0));
		affected.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 50 * 20, 24));
		affected.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.HASTE, 50 * 20, 9));
		affected.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SPEED, 50 * 20, 6));
		affected.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST, 50 * 20, 9));
		affected.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.HEALTH_BOOST, 50 * 20, 9));
		affected.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.ABSORPTION, 50 * 20, 4));
		affected.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.NAUSEA, 5 * 20, 4));
		affected.addPotionEffect(new PotionEffect(HbmPotion.radx, 50 * 20, 9));
		VersatileConfig.applyPotionSickness(affected, 5);
	}
}

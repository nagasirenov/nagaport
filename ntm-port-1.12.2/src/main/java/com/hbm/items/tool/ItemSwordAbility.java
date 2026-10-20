package com.hbm.items.tool;

import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.handler.ability.AvailableAbilities;
import com.hbm.handler.ability.IWeaponAbility;
import com.hbm.items.ModItems;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class ItemSwordAbility extends ItemSword {

	private EnumRarity rarity = EnumRarity.common;
	// was there a reason for this to be private?
	protected float damage;
	protected double movement;
	private AvailableAbilities abilities = new AvailableAbilities();

	public ItemSwordAbility(float damage, double movement, ToolMaterial material) {
		super(material);
		this.damage = damage;
		this.movement = movement;
	}

	public ItemSwordAbility addAbility(IWeaponAbility weaponAbility, int level) {
		this.abilities.addAbility(weaponAbility, level);
		return this;
	}

	// <insert obvious Rarity joke here>
	public ItemSwordAbility setRarity(EnumRarity rarity) {
		this.rarity = rarity;
		return this;
	}

	public EnumRarity getRarity(ItemStack stack) {
		return this.rarity != EnumRarity.common ? this.rarity : super.getRarity(stack);
	}

	public boolean hitEntity(ItemStack stack, EntityLivingBase victim, EntityLivingBase attacker) {

		if(!attacker.world.isRemote && attacker instanceof EntityPlayer && canOperate(stack)) {

			// hacky hacky hack
			if(this == ModItems.mese_gavel)
				attacker.world.playSoundAtEntity(victim, "hbm:weapon.whack", 3.0F, 1.F);

			this.abilities.getWeaponAbilities().forEach((ability, level) -> {
				ability.onHit(level, attacker.world, (EntityPlayer) attacker, victim, this);
			});
		}

		stack.damageItem(1, attacker);

		return true;
	}

	@Override
	public Multimap getItemAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot slot) {

		Multimap multimap = HashMultimap.create();
		if(slot == net.minecraft.inventory.EntityEquipmentSlot.OFFHAND) return multimap;
		multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", (double) this.damage, 0));
		multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", movement, 1));
		return multimap;
	}

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		abilities.addInformation(list);
	}

	protected boolean canOperate(ItemStack stack) {
		return true;
	}
}

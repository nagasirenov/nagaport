package com.hbm.items.tool;

import com.google.common.collect.Multimap;
import com.hbm.items.ModItems;

import api.hbm.block.IToolable.ToolType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemToolingWeapon extends ItemTooling {
	
	protected float damage = 0;

	public ItemToolingWeapon(ToolType type, int durability, float damage) {
		super(type, durability);
		this.damage = damage;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase player) {
		
		World world = player.world;

		if(this == ModItems.wrench) {

			Vec3d vec = player.getLookVec();

			double dX = vec.x * 0.5;
			double dY = vec.y * 0.5;
			double dZ = vec.z * 0.5;

			entity.motionX += dX;
			entity.motionY += dY;
			entity.motionZ += dZ;
			world.playSound(null, entity.posX, entity.posY, entity.posZ, com.hbm.main.ModSoundEvents.resolve("random.anvil_land"), net.minecraft.util.SoundCategory.PLAYERS, 3.0F, 0.75F);
		}
		
		return false;
	}

	@Override
	public Multimap getAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot slot, ItemStack stack) {
		
		Multimap multimap = super.getAttributeModifiers(slot, stack);
		if(slot == net.minecraft.inventory.EntityEquipmentSlot.OFFHAND) return multimap;
		multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage, 0));
		
		if(this == ModItems.wrench) {
			multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Wrench modifier", -0.1, 1));
		}
		
		return multimap;
	}
}

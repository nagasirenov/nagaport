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
		
		World world = player.worldObj;

		if(this == ModItems.wrench) {

			Vec3d vec = player.getLookVec();

			double dX = vec.x * 0.5;
			double dY = vec.y * 0.5;
			double dZ = vec.z * 0.5;

			entity.motionX += dX;
			entity.motionY += dY;
			entity.motionZ += dZ;
			world.playSoundAtEntity(entity, "random.anvil_land", 3.0F, 0.75F);
		}
		
		return false;
	}

	@Override
	public Multimap getAttributeModifiers(ItemStack stack) {
		
		Multimap multimap = super.getAttributeModifiers(stack);
		multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", damage, 0));
		
		if(this == ModItems.wrench) {
			multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Wrench modifier", -0.1, 1));
		}
		
		return multimap;
	}
}

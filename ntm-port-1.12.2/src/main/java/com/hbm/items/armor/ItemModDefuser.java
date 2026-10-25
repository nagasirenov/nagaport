package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextFormatting;

public class ItemModDefuser extends ItemArmorMod {

	public ItemModDefuser() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		list.add(TextFormatting.YELLOW + "Defuses nearby creepers");
		list.add("");
		super.addInformation(itemstack, player, list, bool);
	}

	@Override
	public void addDesc(List list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.YELLOW + "  " + stack.getDisplayName() + " (Defuses creepers)");
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(entity.world.isRemote || entity.world.getTotalWorldTime() % 20 != 0) return;

		List<EntityCreeper> creepers = entity.world.getEntitiesWithinAABB(EntityCreeper.class, entity.getEntityBoundingBox().grow(5, 5, 5));

		for(EntityCreeper creeper : creepers) defuse(creeper, entity, true);
	}

	public static boolean defuse(EntityCreeper creeper, EntityLivingBase entity, boolean dropItem) {

		creeper.setCreeperState(-1);
		net.minecraft.network.datasync.DataParameter<Boolean> ignited = net.minecraftforge.fml.relauncher.ReflectionHelper.getPrivateValue(net.minecraft.entity.monster.EntityCreeper.class, null, "field_184715_c", "IGNITED"); creeper.getDataManager().set(ignited, Boolean.FALSE);

		if(!creeper.world.isRemote) {
			EntityAICreeperSwell toRem = null;
			for(Object o : creeper.tasks.taskEntries) {
				EntityAITaskEntry entry = (EntityAITaskEntry) o;

				if(entry.action instanceof EntityAICreeperSwell) {
					toRem = (EntityAICreeperSwell) entry.action;
					break;
				}
			}

			if(toRem != null) {
				creeper.tasks.removeTask(toRem);
				if(dropItem) {
					com.hbm.sound.WorldSoundEffects.play(creeper.world, creeper.posX, creeper.posY, creeper.posZ, "hbm:item.pinBreak", 1.0F, 1.0F);
					creeper.dropItem(ModItems.safety_fuse, 1);
					creeper.attackEntityFrom(DamageSource.causeMobDamage(entity), 1.0F);
					creeper.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.WEAKNESS, 0, 200));
				}
				creeper.getEntityData().setBoolean("hfr_defused", true);
				return true;
			}
		}

		return false;
	}
}

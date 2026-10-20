package com.hbm.items.tool;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Multimap;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WeaponSpecial extends ItemSword {

	Random rand = new Random();

	public WeaponSpecial(ToolMaterial p_i45356_1_) {
		super(p_i45356_1_);
	}

	@Override
	public EnumRarity getRarity(ItemStack p_77613_1_) {
		if(this == ModItems.schrabidium_hammer) {
			return EnumRarity.rare;
		}
		if(this == ModItems.ullapool_caber) {
			return EnumRarity.uncommon;
		}
		if(this == ModItems.shimmer_sledge || this == ModItems.shimmer_axe) {
			return EnumRarity.epic;
		}

		return EnumRarity.common;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase entityPlayer) {
		World world = entity.world;

		if(this == ModItems.schrabidium_hammer) {
			if(!world.isRemote) {
				entity.setHealth(0.0F);
			}
			world.playSoundAtEntity(entity, "hbm:weapon.bonk", 3.0F, 1.0F);
		}

		if(this == ModItems.bottle_opener) {
			if(!world.isRemote) {
				int i = rand.nextInt(7);
				if(i == 0)
					entity.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.BLINDNESS, 5 * 60 * 20, 0));
				if(i == 1)
					entity.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SLOWNESS, 5 * 60 * 20, 2));
				if(i == 2)
					entity.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 5 * 60 * 20, 2));
				if(i == 3)
					entity.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.NAUSEA, 1 * 60 * 20, 0));
			}
			world.playSoundAtEntity(entity, "random.anvil_land", 3.0F, 1.F);
		}

		if(this == ModItems.ullapool_caber) {
			if(!world.isRemote) {
				world.createExplosion(null, entity.posX, entity.posY, entity.posZ, 7.5F, true);
			}

			stack.damageItem(505, entityPlayer);
		}

		if(this == ModItems.shimmer_sledge) {
			Vec3d vec = entityPlayer.getLookVec();
			double dX = vec.x * 5;
			double dY = vec.y * 5;
			double dZ = vec.z * 5;

			entity.motionX += dX;
			entity.motionY += dY;
			entity.motionZ += dZ;
			world.playSoundAtEntity(entity, "hbm:weapon.bang", 3.0F, 1.F);
		}

		if(this == ModItems.shimmer_axe) {
			entity.setHealth(entity.getHealth() / 2);

			world.playSoundAtEntity(entity, "hbm:weapon.slice", 3.0F, 1.F);
		}

		if(this == ModItems.wood_gavel) {
			world.playSoundAtEntity(entity, "hbm:weapon.whack", 3.0F, 1.F);
		}

		if(this == ModItems.lead_gavel) {
			world.playSoundAtEntity(entity, "hbm:weapon.whack", 3.0F, 1.F);

			entity.addPotionEffect(new PotionEffect(HbmPotion.lead.id, 15 * 20, 4));
		}

		if(this == ModItems.diamond_gavel) {

			float ded = entity.getMaxHealth() / 3;
			entity.setHealth(entity.getHealth() - ded);

			world.playSoundAtEntity(entity, "hbm:weapon.whack", 3.0F, 1.F);
		}

		if(this == ModItems.wrench) {

			Vec3d vec = entityPlayer.getLookVec();

			double dX = vec.x * 0.5;
			double dY = vec.y * 0.5;
			double dZ = vec.z * 0.5;

			entity.motionX += dX;
			entity.motionY += dY;
			entity.motionZ += dZ;
			world.playSoundAtEntity(entity, "random.anvil_land", 3.0F, 0.75F);
		}

		if(this == ModItems.memespoon && !world.isRemote) {

			if(!(entityPlayer instanceof EntityPlayer))
				return false;

			if(entityPlayer.fallDistance >= 2) {
				world.playSoundAtEntity(entity, "hbm:weapon.bang", 3.0F, 0.75F);
				entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) entityPlayer), 50F);
			}

			if(entityPlayer.fallDistance >= 20 && !((EntityPlayer) entityPlayer).capabilities.isCreativeMode) {
				ExplosionVNT vnt = new ExplosionVNT(world, entity.posX, entity.posY + entity.height / 2D, entity.posZ, 15, entityPlayer);
				vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 150).setupPiercing(25, 0.5F));
				vnt.setPlayerProcessor(new PlayerProcessorStandard());
				ExplosionCreator.composeEffectSmall(world, entity.posX, entity.posY + entity.height / 2D, entity.posZ);
				vnt.explode();
			}
		}

		if(this == ModItems.stopsign || this == ModItems.sopsign)
			world.playSoundAtEntity(entity, "hbm:weapon.stop", 1.0F, 1.0F);

		return false;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int i, float f1, float f2, float f3) {

		if(this == ModItems.shimmer_sledge) {
			if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() != Blocks.air && com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock().getExplosionResistance(null) < 6000) {

				EntityRubble rubble = new EntityRubble(world);
				rubble.posX = x + 0.5F;
				rubble.posY = y;
				rubble.posZ = z + 0.5F;

				rubble.setMetaBasedOnBlock(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock(), com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ));

				Vec3d vec = player.getLookVec();
				double dX = vec.x * 5;
				double dY = vec.y * 5;
				double dZ = vec.z * 5;

				rubble.motionX += dX;
				rubble.motionY += dY;
				rubble.motionZ += dZ;
				world.playSoundAtEntity(rubble, "hbm:weapon.bang", 3.0F, 1.0F);

				if(!world.isRemote) {

					world.spawnEntityInWorld(rubble);
					world.func_147480_a(x, y, z, false);
				}
			}
			return true;
		}

		if(this == ModItems.shimmer_axe) {

			com.hbm.sound.WorldSoundEffects.play(world, x + 0.5, y + 0.5, z + 0.5, "hbm:weapon.kapeng", 3.0F, 1.0F);

			if(!world.isRemote) {
				if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() != Blocks.air && com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock().getExplosionResistance(null) < 6000) {
					world.func_147480_a(x, y, z, false);
				}
				if(com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock() != Blocks.air && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().getExplosionResistance(null) < 6000) {
					world.func_147480_a(x, y + 1, z, false);
				}
				if(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() != Blocks.air && com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock().getExplosionResistance(null) < 6000) {
					world.func_147480_a(x, y - 1, z, false);
				}
			}
			return true;
		}

		return false;
	}

	@Override
	public Multimap getItemAttributeModifiers() {
		Multimap multimap = super.getItemAttributeModifiers();
		if(this == ModItems.schrabidium_hammer) {
			multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", -0.5, 1));
		}
		if(this == ModItems.shimmer_sledge || this == ModItems.shimmer_axe) {
			multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", -0.2, 1));
		}
		if(this == ModItems.wrench_flipped) {
			multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", -0.1, 1));
		}
		return multimap;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean b) {

		if(entity instanceof EntityPlayer) {
			if(ArmorUtil.checkForFiend((EntityPlayer) entity)) {
				com.hbm.advancement.NtmAdvancements.award(((EntityPlayer) entity), MainRegistry.achFiend);
			} else if(ArmorUtil.checkForFiend2((EntityPlayer) entity)) {
				com.hbm.advancement.NtmAdvancements.award(((EntityPlayer) entity), MainRegistry.achFiend2);
			}
		}
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {
		if(this == ModItems.schrabidium_hammer) {
			list.add("Even though it says \"+1000000000");
			list.add("damage\", it's actually \"onehit anything\"");
		}
		if(this == ModItems.ullapool_caber) {
			list.add("High-yield Scottish face removal.");
			list.add("A sober person would throw it...");
		}
		if(this == ModItems.bottle_opener) {
			list.add("My very own bottle opener.");
			list.add("Use with caution!");
		}
		if(this == ModItems.shimmer_sledge) {
			if(MainRegistry.polaroidID == 11) {
				list.add("shimmer no");
				list.add("drop that hammer");
				list.add("you're going to hurt somebody");
				list.add("shimmer no");
				list.add("shimmer pls");
			} else {
				list.add("Breaks everything, even portals.");
			}
		}
		if(this == ModItems.shimmer_axe) {
			if(MainRegistry.polaroidID == 11) {
				list.add("shim's toolbox does an e-x-p-a-n-d");
			} else {
				list.add("Timber!");
			}
		}
		if(this == ModItems.wrench_flipped) {
			list.add("Wrench 2: The Wrenchening");
		}
		if(this == ModItems.memespoon) {
			list.add(TextFormatting.DARK_GRAY + "Level 10 Shovel");
			list.add(TextFormatting.AQUA + "Deals crits while the wielder is rocket jumping");
			list.add(TextFormatting.RED + "20% slower firing speed");
			list.add(TextFormatting.RED + "No random critical hits");
		}

		if(this == ModItems.wood_gavel) {
			list.add("Thunk!");
		}
		if(this == ModItems.lead_gavel) {
			list.add("You are hereby sentenced to lead poisoning.");
		}
		if(this == ModItems.diamond_gavel) {
			list.add("The joke! It makes sense now!!");
			list.add("");
			list.add(TextFormatting.BLUE + "Deals as much damage as it needs to.");
		}
	}

}

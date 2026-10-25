package com.hbm.items.special;

import java.util.List;

import org.apache.logging.log4j.Level;

import com.hbm.config.BombConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.config.WeaponConfig;
import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.effect.EntityRagingVortex;
import com.hbm.entity.effect.EntityVortex;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.interfaces.IBomb;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemDrop extends Item {

	@Override
	public boolean onEntityItemUpdate(EntityItem entityItem) {
		if (entityItem != null) {

			if(this == ModItems.beta) {
				entityItem.setDead();
				return true;
			}

			ItemStack stack = entityItem.getItem();

			if (stack.getItem() != null && stack.getItem() == ModItems.detonator_deadman) {
				if (!entityItem.world.isRemote) {

					if(stack.getTagCompound() != null) {

						 int x = stack.getTagCompound().getInteger("x");
						 int y = stack.getTagCompound().getInteger("y");
						 int z = stack.getTagCompound().getInteger("z");

						 if(com.hbm.world.WorldBlockState.read(entityItem.world, x, y, z).getBlock() instanceof IBomb)
						 {
							if(!entityItem.world.isRemote)
							{
								((IBomb)com.hbm.world.WorldBlockState.read(entityItem.world, x, y, z).getBlock()).explode(entityItem.world, x, y, z);

					    		if(GeneralConfig.enableExtendedLogging)
					    			MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by dead man's switch!");
							}
						 }
					}

					entityItem.world.createExplosion(entityItem, entityItem.posX, entityItem.posY,
							entityItem.posZ, 0.0F, true);
					entityItem.setDead();
				}
			}
			if (stack.getItem() != null && stack.getItem() == ModItems.detonator_de) {
				if (!entityItem.world.isRemote && WeaponConfig.dropDead) {
					entityItem.world.createExplosion(entityItem, entityItem.posX, entityItem.posY,
							entityItem.posZ, 15.0F, true);

		    		if(GeneralConfig.enableExtendedLogging)
		    			MainRegistry.logger.log(Level.INFO, "[DET] Detonated dead man's explosive at " + ((int)entityItem.posX) + " / " + ((int)entityItem.posY) + " / " + ((int)entityItem.posZ) + "!");
				}

				entityItem.setDead();
			}

			if (entityItem.onGround) {

				if (stack.getItem() != null && stack.getItem() == ModItems.cell_antimatter && WeaponConfig.dropCell) {
					if (!entityItem.world.isRemote) {
						new ExplosionVNT(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, 5F).makeAmat().explode();
					}
				}
				if (stack.getItem() != null && stack.getItem() == ModItems.particle_amat && WeaponConfig.dropCell) {
					if (!entityItem.world.isRemote) {
						new ExplosionVNT(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, 5F).makeAmat().explode();
					}
				}

				if (stack.getItem() != null && stack.getItem() == ModItems.pellet_antimatter && WeaponConfig.dropCell) {
					if (!entityItem.world.isRemote) {
						new ExplosionVNT(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, 20F).makeAmat().explode();
					}
				}
				if (stack.getItem() != null && stack.getItem() == ModItems.cell_anti_schrabidium && WeaponConfig.dropCell) {
					if (!entityItem.world.isRemote) {
						EntityNukeExplosionMK3 ex = EntityNukeExplosionMK3.statFacFleija(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, BombConfig.aSchrabRadius);
						if(!ex.isDead) {
							com.hbm.sound.VanillaWorldSoundEffects.play(entityItem.world, entityItem.posX, entityItem.posY, entityItem.posZ, "random.explode", 100.0F, entityItem.world.rand.nextFloat() * 0.1F + 0.9F);
							entityItem.world.spawnEntity(ex);

							EntityCloudFleija cloud = new EntityCloudFleija(entityItem.world, BombConfig.aSchrabRadius);
							cloud.posX = entityItem.posX;
							cloud.posY = entityItem.posY;
							cloud.posZ = entityItem.posZ;
							entityItem.world.spawnEntity(cloud);
						}
					}
				}
				if (stack.getItem() != null && stack.getItem() == ModItems.singularity && WeaponConfig.dropSing) {
					if (!entityItem.world.isRemote) {

						EntityVortex bl = new EntityVortex(entityItem.world, 1.5F);
						bl.posX = entityItem.posX;
						bl.posY = entityItem.posY;
						bl.posZ = entityItem.posZ;
						entityItem.world.spawnEntity(bl);
					}
				}
				if (stack.getItem() != null && stack.getItem() == ModItems.singularity_counter_resonant && WeaponConfig.dropSing) {
					if (!entityItem.world.isRemote) {

						EntityVortex bl = new EntityVortex(entityItem.world, 2.5F);
						bl.posX = entityItem.posX;
						bl.posY = entityItem.posY;
						bl.posZ = entityItem.posZ;
						entityItem.world.spawnEntity(bl);
					}
				}
				if (stack.getItem() != null && stack.getItem() == ModItems.singularity_super_heated && WeaponConfig.dropSing) {
					if (!entityItem.world.isRemote) {

						EntityVortex bl = new EntityVortex(entityItem.world, 2.5F);
						bl.posX = entityItem.posX;
						bl.posY = entityItem.posY;
						bl.posZ = entityItem.posZ;
						entityItem.world.spawnEntity(bl);
					}
				}
				if (stack.getItem() != null && stack.getItem() == ModItems.black_hole && WeaponConfig.dropSing) {
					if (!entityItem.world.isRemote) {
						EntityBlackHole bl = new EntityBlackHole(entityItem.world, 1.5F);
						bl.posX = entityItem.posX;
						bl.posY = entityItem.posY;
						bl.posZ = entityItem.posZ;
						entityItem.world.spawnEntity(bl);
					}
				}
				if (stack.getItem() != null && stack.getItem() == ModItems.singularity_spark && WeaponConfig.dropSing) {
					if (!entityItem.world.isRemote) {
						EntityRagingVortex bl = new EntityRagingVortex(entityItem.world, 3.5F);
						bl.posX = entityItem.posX;
						bl.posY = entityItem.posY;
						bl.posZ = entityItem.posZ;
						entityItem.world.spawnEntity(bl);
					}
				}
				if (stack.getItem() != null && stack.getItem() == ModItems.crystal_xen && WeaponConfig.dropCrys) {
					if (!entityItem.world.isRemote) {
						ExplosionChaos.floater(entityItem.world, (int)entityItem.posX, (int)entityItem.posY, (int)entityItem.posZ, 25, 75);
						ExplosionChaos.move(entityItem.world, (int)entityItem.posX, (int)entityItem.posY, (int)entityItem.posZ, 25, 0, 75, 0);
					}
				}

				entityItem.setDead();
				return true;
			}
		}
		return false;
    }

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag)
	{
		if (this == ModItems.cell_antimatter) {
			list.add("Warning: Exposure to matter will");
			list.add("lead to violent annihilation!");
		}
		if (this == ModItems.pellet_antimatter) {
			list.add("Very heavy antimatter cluster.");
			list.add("Gets rid of black holes.");
		}
		if (this == ModItems.cell_anti_schrabidium) {
			list.add("Warning: Exposure to matter will");
			list.add("create a fólkvangr field!");
		}
		if (this == ModItems.singularity) {
			list.add("You may be asking:");
			list.add("\"But HBM, a manifold with an undefined");
			list.add("state of spacetime? How is this possible?\"");
			list.add("Long answer short:");
			list.add("\"I have no idea!\"");
		}
		if (this == ModItems.singularity_counter_resonant) {
			list.add("Nullifies resonance of objects in");
			list.add("non-euclidean space, creates variable");
			list.add("gravity well. Spontaneously spawns");
			list.add("tesseracts. If a tesseract happens to");
			list.add("appear near you, do not look directly");
			list.add("at it.");
		}
		if (this == ModItems.singularity_super_heated) {
			list.add("Continuously heats up matter by");
			list.add("resonating every planck second.");
			list.add("Tends to catch fire or to create");
			list.add("small plasma arcs. Not edible.");
		}
		if (this == ModItems.black_hole) {
			list.add("Contains a regular singularity");
			list.add("in the center. Large enough to");
			list.add("stay stable. It's not the end");
			list.add("of the world as we know it,");
			list.add("and I don't feel fine.");
		}
		if (this == ModItems.detonator_deadman) {
			list.add("Shift right-click to set position,");
			list.add("drop to detonate!");
			if(itemstack.getTagCompound() == null)
			{
				list.add("No position set!");
			} else {
				list.add("Set pos to " + itemstack.getTagCompound().getInteger("x") + ", " + itemstack.getTagCompound().getInteger("y") + ", " + itemstack.getTagCompound().getInteger("z"));
			}
		}
		if (this == ModItems.detonator_de) {
			list.add("Explodes when dropped!");
		}

		list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.drop") + "]");
	}


        @Override
    public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
        net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
        int side = facing != null ? facing.getIndex() : 0;
        return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
    }

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_)
    {
		if(this != ModItems.detonator_deadman) {
			return false;
		}

		if(stack.getTagCompound() == null)
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		if(player.isSneaking())
		{
			stack.getTagCompound().setInteger("x", x);
			stack.getTagCompound().setInteger("y", y);
			stack.getTagCompound().setInteger("z", z);

			if(world.isRemote)
			{
				player.sendMessage(new TextComponentString("Position set!"));
			}

	        world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("hbm:item.techBoop"), net.minecraft.util.SoundCategory.PLAYERS, 2.0F, 1.0F);

			return true;
		}

		return false;
    }

}

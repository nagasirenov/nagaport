package com.hbm.items.weapon;

import java.util.List;

import com.hbm.entity.grenade.*;
import com.hbm.items.ModItems;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ItemGrenade extends Item {

	public int fuse = 4;

	public ItemGrenade(int fuse) {
		this.maxStackSize = 16;
		this.fuse = fuse;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World p_77659_2_, EntityPlayer p_77659_3_, net.minecraft.util.EnumHand nativeHand) {
		ItemStack p_77659_1_ = p_77659_3_.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_));
	}

	public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_) {
		if (!p_77659_3_.capabilities.isCreativeMode) {
			p_77659_1_.shrink(1);
		}

		p_77659_2_.playSound(null, p_77659_3_.posX, p_77659_3_.posY, p_77659_3_.posZ, com.hbm.main.ModSoundEvents.resolve("random.bow"), net.minecraft.util.SoundCategory.PLAYERS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		//TODO:
		/*
		 * kill all this stupid bullshit
		 * make a PROPER grenade entity base class
		 * have all the grenade items be an NBT stat in the entity instead of having new entities for every fucking grenade type
		 * register explosion effects with some lambdas to save on LOC
		 * jesus christ why do i keep doing this
		 */
		if (!p_77659_2_.isRemote) {
			if (this == ModItems.grenade_generic) {
				p_77659_2_.spawnEntity(new EntityGrenadeGeneric(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_strong) {
				p_77659_2_.spawnEntity(new EntityGrenadeStrong(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_frag) {
				EntityGrenadeFrag frag = new EntityGrenadeFrag(p_77659_2_, p_77659_3_);
				frag.shooter = p_77659_3_;
				p_77659_2_.spawnEntity(frag);
			}
			if (this == ModItems.grenade_fire) {
				EntityGrenadeFire fire = new EntityGrenadeFire(p_77659_2_, p_77659_3_);
				fire.shooter = p_77659_3_;
				p_77659_2_.spawnEntity(fire);
			}
			if (this == ModItems.grenade_cluster) {
				p_77659_2_.spawnEntity(new EntityGrenadeCluster(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_flare) {
				p_77659_2_.spawnEntity(new EntityGrenadeFlare(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_electric) {
				p_77659_2_.spawnEntity(new EntityGrenadeElectric(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_poison) {
				p_77659_2_.spawnEntity(new EntityGrenadePoison(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_gas) {
				p_77659_2_.spawnEntity(new EntityGrenadeGas(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_schrabidium) {
				p_77659_2_.spawnEntity(new EntityGrenadeSchrabidium(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_nuke) {
				p_77659_2_.spawnEntity(new EntityGrenadeNuke(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_nuclear) {
				p_77659_2_.spawnEntity(new EntityGrenadeNuclear(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_pulse) {
				p_77659_2_.spawnEntity(new EntityGrenadePulse(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_plasma) {
				p_77659_2_.spawnEntity(new EntityGrenadePlasma(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_tau) {
				p_77659_2_.spawnEntity(new EntityGrenadeTau(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_lemon) {
				p_77659_2_.spawnEntity(new EntityGrenadeLemon(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_mk2) {
				p_77659_2_.spawnEntity(new EntityGrenadeMk2(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_aschrab) {
				p_77659_2_.spawnEntity(new EntityGrenadeASchrab(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_zomg) {
				p_77659_2_.spawnEntity(new EntityGrenadeZOMG(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_shrapnel) {
				p_77659_2_.spawnEntity(new EntityGrenadeShrapnel(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_black_hole) {
				p_77659_2_.spawnEntity(new EntityGrenadeBlackHole(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_gascan) {
				p_77659_2_.spawnEntity(new EntityGrenadeGascan(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_cloud) {
				p_77659_2_.spawnEntity(new EntityGrenadeCloud(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_pink_cloud) {
				p_77659_2_.spawnEntity(new EntityGrenadePC(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_smart) {
				p_77659_2_.spawnEntity(new EntityGrenadeSmart(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_mirv) {
				p_77659_2_.spawnEntity(new EntityGrenadeMIRV(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_breach) {
				p_77659_2_.spawnEntity(new EntityGrenadeBreach(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_burst) {
				p_77659_2_.spawnEntity(new EntityGrenadeBurst(p_77659_2_, p_77659_3_));
			}

			if (this == ModItems.grenade_if_generic) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFGeneric(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_he) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFHE(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_bouncy) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFBouncy(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_sticky) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFSticky(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_impact) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFImpact(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_incendiary) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFIncendiary(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_toxic) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFToxic(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_concussion) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFConcussion(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_brimstone) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFBrimstone(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_mystery) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFMystery(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_spark) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFSpark(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_hopwire) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFHopwire(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.grenade_if_null) {
				p_77659_2_.spawnEntity(new EntityGrenadeIFNull(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.nuclear_waste_pearl) {
				p_77659_2_.spawnEntity(new EntityWastePearl(p_77659_2_, p_77659_3_));
			}
			if (this == ModItems.stick_dynamite) {
				p_77659_2_.spawnEntity(new EntityGrenadeDynamite(p_77659_2_, p_77659_3_));
			}
		}

		return p_77659_1_;
	}

	@Override
	public EnumRarity getRarity(ItemStack p_77613_1_) {

		if (this == ModItems.grenade_schrabidium || this == ModItems.grenade_aschrab || this == ModItems.grenade_cloud) {
			return EnumRarity.RARE;
		}

		if (this == ModItems.grenade_plasma || this == ModItems.grenade_zomg || this == ModItems.grenade_black_hole || this == ModItems.grenade_pink_cloud) {
			return EnumRarity.EPIC;
		}

		if (this == ModItems.grenade_nuke || this == ModItems.grenade_nuclear || this == ModItems.grenade_tau || this == ModItems.grenade_lemon || this == ModItems.grenade_mk2 || this == ModItems.grenade_pulse || this == ModItems.grenade_gascan) {
			return EnumRarity.UNCOMMON;
		}

		return EnumRarity.COMMON;
	}

	private String translateFuse() {
		if(fuse == -1)
			return "Impact";

		if(fuse == 0)
			return "Instant";

		return fuse + "s";
	}


		@Override
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	public void addInformation(net.minecraft.item.ItemStack stack, net.minecraft.world.World worldIn, java.util.List<java.lang.String> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
		addInformation(stack, (net.minecraft.entity.player.EntityPlayer) null, tooltip, flagIn.isAdvanced());
	}

public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {

		list.add("Fuse: " + translateFuse());

		if (this == ModItems.grenade_smart) {
			list.add("");
			list.add("\"Why did it not blow up????\"");
			list.add(TextFormatting.ITALIC + "If it didn't blow up it means it worked.");
		}

		if (this == ModItems.grenade_if_generic) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"How do you like " + TextFormatting.RESET + TextFormatting.GRAY + "them" + TextFormatting.ITALIC + " apples?\"");
		}
		if (this == ModItems.grenade_if_he) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"You better run, you better take cover!\"");
		}
		if (this == ModItems.grenade_if_bouncy) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"Boing!\"");
		}
		if (this == ModItems.grenade_if_sticky) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"This one is the booger grenade.\"");
		}
		if (this == ModItems.grenade_if_impact) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"Tossable boom.\"");
		}
		if (this == ModItems.grenade_if_incendiary) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"Flaming wheel of destruction!\"");
		}
		if (this == ModItems.grenade_if_toxic) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"TOXIC SHOCK\"");
		}
		if (this == ModItems.grenade_if_concussion) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"Oof ouch owie, my bones!\"");
		}
		if (this == ModItems.grenade_if_brimstone) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"Zoop!\"");
		}
		if (this == ModItems.grenade_if_mystery) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"It's a mystery!\"");
		}
		if (this == ModItems.grenade_if_spark) {
			list.add("");
			//list.add(EnumChatFormatting.ITALIC + "\"31-31-31-31-31-31-31-31-31-31-31-31-31\"");
			list.add(TextFormatting.ITALIC + "\"We can't rewind, we've gone too far.\"");
		}
		if (this == ModItems.grenade_if_hopwire) {
			list.add("");
			list.add(TextFormatting.ITALIC + "\"All I ever wished for was a bike that didn't fall over.\"");
		}
		if (this == ModItems.grenade_if_null) {
			list.add("");
			list.add(TextFormatting.ITALIC + "java.lang.NullPointerException");
		}
	}

	public static int getFuseTicks(Item grenade) {
		return ((ItemGrenade)grenade).fuse * 20;
	}
}

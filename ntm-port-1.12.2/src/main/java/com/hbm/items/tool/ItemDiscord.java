package com.hbm.items.tool;

import java.util.List;

import com.hbm.lib.Library;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemDiscord extends Item {

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		RayTraceResult pos = Library.rayTrace(player, 100, 1);

		if(pos != null && pos.typeOfHit == RayTraceResult.Type.BLOCK) {

			if(!world.isRemote) {

				if(player.isRiding())
					player.mountEntity(null);

				net.minecraft.util.EnumFacing side = pos.sideHit;

				com.hbm.sound.VanillaWorldSoundEffects.play(world, player.posX, player.posY, player.posZ, "mob.endermen.portal", 1.0F, 1.0F);

				player.setPositionAndUpdate(pos.hitVec.x + side.getFrontOffsetX(), pos.hitVec.y + side.getFrontOffsetY() - 1, pos.hitVec.z + side.getFrontOffsetZ());

				com.hbm.sound.VanillaWorldSoundEffects.play(world, player.posX, player.posY, player.posZ, "mob.endermen.portal", 1.0F, 1.0F);
				player.fallDistance = 0.0F;
			}

			for(int i = 0; i < 32; ++i)
				world.spawnParticle(net.minecraft.util.EnumParticleTypes.PORTAL, player.posX, player.posY + player.getRNG().nextDouble() * 2.0D, player.posZ, player.getRNG().nextGaussian(), 0.0D, player.getRNG().nextGaussian());
		}

		return stack;
	}

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World tooltipWorld, List<String> list, net.minecraft.client.util.ITooltipFlag tooltipFlag) {

		list.add(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "I've seen the Rod of Discord and honestly");
		list.add(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "it's not as amazing as people say.");
		list.add("");
		list.add(TextFormatting.RED + "" + TextFormatting.ITALIC + "Rod of Discord is crucial in so many boss fights.");
		list.add(TextFormatting.RED + "" + TextFormatting.ITALIC + "Imagine getting coiled by worm bosses.");
		list.add("");
		list.add(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "Oh, you mean the Terraria item.");
		list.add(TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "Idk about that thing.");
	}
}

package com.hbm.items.tool;

import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.util.ChatBuilder;
import com.hbm.util.Vec3NT;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemRangefinder extends Item {
	
	public static final int META_POLARIZED = 1;

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(world.isRemote) return stack;

		Vec3NT start = new Vec3NT(player.posX, player.posY + player.eyeHeight, player.posZ);
		Vec3NT startOriginal = new Vec3NT(start); // why the fuck
		Vec3NT end = new Vec3NT(start).add(new Vec3NT(player.getLookVec()).multiply(300));
		
		RayTraceResult mop = world.rayTraceBlocks(start.toVec3d(), end.toVec3d(), false, true, false);
		
		if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
			double dist = startOriginal.distanceTo(mop.hitVec);
			String msg = ((int)(dist * 10D)) / 10D + "m";
			if(stack.getItemDamage() == META_POLARIZED) msg = TextFormatting.LIGHT_PURPLE + msg + TextFormatting.RESET;
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(ChatBuilder.start(msg).flush(), MainRegistry.proxy.ID_DETONATOR, 5000), (EntityPlayerMP) player);
		}
		
		return stack;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String name = super.getItemStackDisplayName(stack);
		if(stack.getItemDamage() == META_POLARIZED) name = TextFormatting.LIGHT_PURPLE + name + TextFormatting.RESET;
		return name;
	}
}

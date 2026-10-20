package com.hbm.packet.toserver;

import java.util.Random;

import com.hbm.entity.missile.EntityBobmazon;
import com.hbm.handler.BobmazonOfferFactory;
import com.hbm.inventory.gui.GUIScreenBobmazon.Offer;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ItemBobmazonPacket implements IMessage {

	int offer;

	public ItemBobmazonPacket() { }

	public ItemBobmazonPacket(EntityPlayer player, Offer offer) {
		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.bobmazon)
			this.offer = BobmazonOfferFactory.standard.indexOf(offer);
		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.bobmazon_hidden)
			this.offer = BobmazonOfferFactory.special.indexOf(offer);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		offer = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(offer);
	}

	public static class Handler implements IMessageHandler<ItemBobmazonPacket, IMessage> {
		
		@Override
		public IMessage onMessage(ItemBobmazonPacket m, MessageContext ctx) {
			EntityPlayerMP p = ctx.getServerHandler().playerEntity;
			World world = p.world;
			if(world.provider.getDimension() != 0)
			{
				p.sendMessage(new TextComponentString("[BOBMAZON] Out Of Range!"));
				return null;
			}
			Offer offer = null;
			if(com.hbm.util.NativeHeldItem.getMainHandNullable(p) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(p).getItem() == ModItems.bobmazon) offer = BobmazonOfferFactory.standard.get(m.offer);
			if(com.hbm.util.NativeHeldItem.getMainHandNullable(p) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(p).getItem() == ModItems.bobmazon_hidden) offer = BobmazonOfferFactory.special.get(m.offer);
			
			if(offer == null) {
				p.sendMessage(new TextComponentString("[BOBMAZON] There appears to be a mismatch between the offer you have requested and the offers that exist."));
				p.sendMessage(new TextComponentString("[BOBMAZON] Engaging fail-safe..."));
				p.attackEntityFrom(ModDamageSource.nuclearBlast, 1000);
				p.motionY = 2.0D;
				return null;
			}

			
			ItemStack stack = offer.offer;
			
			ResourceLocation req = offer.requirement.achievement;
			if(req != null && com.hbm.advancement.NtmAdvancements.has(p, req) || p.capabilities.isCreativeMode) {
				
				if(countCaps(p) >= offer.cost || p.capabilities.isCreativeMode) {
					
					payCaps(p, offer.cost);
					p.inventoryContainer.detectAndSendChanges();
					
					Random rand = world.rand;
					EntityBobmazon bob = new EntityBobmazon(world);
					bob.posX = p.posX + rand.nextGaussian() * 10;
					bob.posY = 300;
					bob.posZ = p.posZ + rand.nextGaussian() * 10;
					bob.payload = stack.copy();
					
					world.spawnEntityInWorld(bob);
				} else {
					p.sendMessage(new TextComponentString("[BOBMAZON] Not enough caps!"));
				}
				
			} else {
				p.sendMessage(new TextComponentString("[BOBMAZON] Achievement requirement not met!"));
			}
			
			return null;
		}
		
		private int countCaps(EntityPlayer player) {
			
			int count = 0;
			
			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack != null) {
					
					Item item = stack.getItem();
					
					if(item == ModItems.cap_fritz ||
							item == ModItems.cap_korl ||
							item == ModItems.cap_nuka ||
							item == ModItems.cap_quantum ||
							item == ModItems.cap_rad ||
							item == ModItems.cap_sparkle)
						count += stack.stackSize;
					
				}
			}
			
			return count;
		}
		
		private void payCaps(EntityPlayer player, int price) {
			
			if(price == 0)
				return;
			
			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack != null) {
					
					Item item = stack.getItem();
					
					if(item == ModItems.cap_fritz ||
							item == ModItems.cap_korl ||
							item == ModItems.cap_nuka ||
							item == ModItems.cap_quantum ||
							item == ModItems.cap_rad ||
							item == ModItems.cap_sparkle) {
						
						int size = stack.stackSize;
						for(int j = 0; j < size; j++) {
							
							player.inventory.decrStackSize(i, 1);
							price--;
							
							if(price == 0)
								return;
						}
					}
				}
			}
		}
	}
}

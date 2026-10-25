package com.hbm.packet.toclient;

import com.hbm.util.BufferUtil;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class HeldItemNBTPacket implements IMessage {

	private ItemStack stack;

	public HeldItemNBTPacket() { }

	public HeldItemNBTPacket(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeShort(Item.getIdFromItem(stack.getItem()));
		buf.writeByte(stack.getCount());
		buf.writeShort(stack.getItemDamage());
		NBTTagCompound nbtTagCompound = null;
		nbtTagCompound = stack.getTagCompound();
		BufferUtil.writeNBT(buf, nbtTagCompound);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		short id = buf.readShort();
		if(id >= 0) {
			byte quantity = buf.readByte();
			short meta = buf.readShort();
			stack = new ItemStack(Item.getItemById(id), quantity, meta);
			stack.setTagCompound(BufferUtil.readNBT(buf));
		}
	}

	public static class Handler implements IMessageHandler<HeldItemNBTPacket, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(HeldItemNBTPacket m, MessageContext ctx) {
			try {
				EntityPlayer player = Minecraft.getMinecraft().player;
				if(m.stack == null) return null;

				ItemStack held = com.hbm.util.NativeHeldItem.getMainHandNullable(player);
				if(held == null) return null;
				if(held.getItem() != m.stack.getItem()) return null;
				if(held.getItemDamage() != m.stack.getItemDamage()) return null;

				held.setTagCompound(m.stack.getTagCompound());

			} catch(Exception x) { } finally { }
			return null;
		}
	}
}

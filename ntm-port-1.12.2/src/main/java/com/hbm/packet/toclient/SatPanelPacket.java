package com.hbm.packet.toclient;

import java.io.IOException;

import com.hbm.items.tool.ItemSatInterface;
import com.hbm.saveddata.satellites.Satellite;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class SatPanelPacket implements IMessage {

	PacketBuffer buffer;
	int type;

	public SatPanelPacket() {

	}

	public SatPanelPacket(Satellite sat) {
		type = sat.getID();

		this.buffer = new PacketBuffer(Unpooled.buffer());
		NBTTagCompound nbt = new NBTTagCompound();
		sat.writeToNBT(nbt);

		try {
			buffer.writeNBTTagCompoundToBuffer(nbt);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void fromBytes(ByteBuf buf) {

		type = buf.readInt();

		if (buffer == null) {
			buffer = new PacketBuffer(Unpooled.buffer());
		}
		buffer.writeBytes(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {

		buf.writeInt(type);

		if (buffer == null) {
			buffer = new PacketBuffer(Unpooled.buffer());
		}
		buf.writeBytes(buffer);
	}

	public static class Handler implements IMessageHandler<SatPanelPacket, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(SatPanelPacket m, MessageContext ctx) {

			Minecraft.getMinecraft();

			try {

				NBTTagCompound nbt = m.buffer.readNBTTagCompoundFromBuffer();
				ItemSatInterface.currentSat = Satellite.create(m.type);

				if(nbt != null)
					ItemSatInterface.currentSat.readFromNBT(nbt);

			} catch (Exception x) {
			} finally {
				m.buffer.release();
			}
			return null;
		}
	}
}

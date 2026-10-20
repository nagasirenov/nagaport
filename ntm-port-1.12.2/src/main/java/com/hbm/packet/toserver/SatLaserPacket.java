package com.hbm.packet.toserver;

import com.hbm.items.ISatChip;
import com.hbm.items.tool.ItemSatInterface;
import com.hbm.saveddata.SatelliteSavedData;
import com.hbm.saveddata.satellites.Satellite;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class SatLaserPacket implements IMessage {

	//0: Add
	//1: Subtract
	//2: Set
	int x;
	int z;
	int freq;

	public SatLaserPacket()
	{

	}

	public SatLaserPacket(int x, int z, int freq)
	{
		this.x = x;
		this.z = z;
		this.freq = freq;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		z = buf.readInt();
		freq = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(z);
		buf.writeInt(freq);
	}

	public static class Handler implements IMessageHandler<SatLaserPacket, IMessage> {

		@Override
		public IMessage onMessage(SatLaserPacket m, MessageContext ctx) {

			EntityPlayer p = ctx.getServerHandler().playerEntity;

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(p) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(p).getItem() instanceof ItemSatInterface) {

				int freq = ISatChip.getFreqS(com.hbm.util.NativeHeldItem.getMainHandNullable(p));

				if(freq == m.freq) {
				    Satellite sat = SatelliteSavedData.getData(p.world, (int)p.posX, (int)p.posZ).getSatFromFreq(m.freq);

				    if(sat != null)
				    	sat.onClick(p.world, m.x, m.z);
				}
			}

			return null;
		}
	}
}

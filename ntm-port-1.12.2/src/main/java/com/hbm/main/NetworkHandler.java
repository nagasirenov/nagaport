package com.hbm.main;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.ScheduledMessageHandler;
import com.hbm.packet.threading.ThreadedPacket;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper;
import net.minecraftforge.fml.relauncher.Side;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.entity.player.EntityPlayerMP;


import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.List;

import static net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec.INBOUNDPACKETTRACKER;

// Essentially the `SimpleNetworkWrapper` from FML but doesn't flush the packets immediately. Also now with a custom codec!
public class NetworkHandler {

	// Network codec for allowing packets to be "precompiled".
	@ChannelHandler.Sharable
	private static class PrecompilingNetworkCodec extends MessageToMessageCodec<FMLProxyPacket, Object> {

		private final TByteObjectHashMap<Class<? extends IMessage>> discriminators = new TByteObjectHashMap<>();
		private final TObjectByteHashMap<Class<? extends IMessage>> types = new TObjectByteHashMap<>();

		public void addDiscriminator(int discriminator, Class<? extends IMessage> type) {
			discriminators.put((byte) discriminator, type);
			types.put(type, (byte) discriminator);
		}

		@Override
		public void handlerAdded(ChannelHandlerContext ctx) {
			ctx.attr(INBOUNDPACKETTRACKER).set(new ThreadLocal<>());
		}

		@Override
		protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) {
			ByteBuf outboundBuf = PooledByteBufAllocator.DEFAULT.heapBuffer();
			byte discriminator;
			Class<?> msgClass = msg.getClass();
			discriminator = types.get(msgClass);
			outboundBuf.writeByte(discriminator);

			if(msg instanceof ThreadedPacket) // Precompiled packet to avoid race conditions/speed up serialization.
				outboundBuf.writeBytes(((ThreadedPacket) msg).getCompiledBuffer());
			else if(msg instanceof IMessage)
				((IMessage) msg).toBytes(outboundBuf);
			else
				throw new CodecException("Unknown packet codec requested during encoding, expected IMessage/PrecompiledPacket, got " + msg.getClass().getName());

			FMLProxyPacket proxy = new FMLProxyPacket(new net.minecraft.network.PacketBuffer(Unpooled.buffer().writeBytes(outboundBuf)), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
			outboundBuf.release();
			WeakReference<FMLProxyPacket> ref = ctx.attr(INBOUNDPACKETTRACKER).get().get();
			FMLProxyPacket old = ref == null ? null : ref.get();
			if (old != null) {
				proxy.setDispatcher(old.getDispatcher());
			}
			out.add(proxy);
		}

		@Override
		protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception {
			ByteBuf inboundBuf = msg.payload();
			byte discriminator = inboundBuf.readByte();
			Class<?> originalMsgClass = discriminators.get(discriminator);

			if(originalMsgClass == null)
				throw new CodecException("Undefined message for discriminator " + discriminator + " in channel " + msg.channel());

			Object newMsg = originalMsgClass.newInstance();
			ctx.attr(INBOUNDPACKETTRACKER).get().set(new WeakReference<>(msg));

			try {
				if(newMsg instanceof IMessage) // pretty much always the case
					((IMessage) newMsg).fromBytes(inboundBuf.slice());
				else
					throw new CodecException("Unknown packet codec requested during decoding, expected IMessage/PrecompiledPacket, got " + msg.getClass().getName());

				out.add(newMsg);
			} finally {
				// This final packet copies only String/primitives; it never retains the slice.
				// FMLProxyPacket is not ReferenceCounted and this custom codec owns its
				// payload. Balance successful and malformed new-packet decoding only.
				// Existing handlers have different buffer ownership and remain untouched.
				if(originalMsgClass == com.hbm.packet.toclient.LegacyEntitySoundPacket.class)
					inboundBuf.release();
			}
		}
	}

	private static FMLEmbeddedChannel clientChannel;
	private static FMLEmbeddedChannel serverChannel;

	private static PrecompilingNetworkCodec packetCodec;

	public NetworkHandler(String name) {
		packetCodec = new PrecompilingNetworkCodec();
		EnumMap<Side, FMLEmbeddedChannel> channels = NetworkRegistry.INSTANCE.newChannel(name, packetCodec);
		clientChannel = channels.get(Side.CLIENT);
		serverChannel = channels.get(Side.SERVER);
	}

	public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, int discriminator, Side side) {

		packetCodec.addDiscriminator(discriminator, requestMessageType);

		FMLEmbeddedChannel channel;
		if(side.isClient())
			channel = clientChannel;
		else
			channel = serverChannel;
		String type = channel.findChannelHandlerNameForType(PrecompilingNetworkCodec.class);
		IMessageHandler<REQ, REPLY> delegate;

		try {
			delegate = messageHandler.newInstance();
		} catch(InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Could not instantiate packet handler " + messageHandler.getName(), e);
		}

		SimpleChannelHandlerWrapper<REQ, REPLY> handler;
		handler = new SimpleChannelHandlerWrapper<>(new ScheduledMessageHandler<>(delegate, side), side, requestMessageType);
		channel.pipeline().addAfter(type, messageHandler.getName(), handler);
	}

	/** Build a vanilla-sendable packet through the existing registered FML codec. */
	public net.minecraft.network.Packet<?> getPacketFrom(IMessage message) {
		PacketThreading.lock.lock();
		try {
			return serverChannel.generatePacketFrom(message);
		} finally {
			PacketThreading.lock.unlock();
		}
	}

	public static void flush() {
		clientChannel.flush();
		serverChannel.flush();
	}

	public void sendToServer(IMessage message) { // No thread protection needed here, since the client never threads packets to the server.
		clientChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		clientChannel.write(message);
	}

	public void sendToDimension(IMessage message, int dimensionId) {
		if(!Thread.currentThread().getName().contains(PacketThreading.threadPrefix)) {
			try {
				PacketThreading.lock.lock();
				serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
				serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
				serverChannel.write(message);
			} finally {
				PacketThreading.lock.unlock();
			}
		} else {
			serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
			serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
			serverChannel.write(message);
		}
	}

	public void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
		if(!Thread.currentThread().getName().contains(PacketThreading.threadPrefix)) {
			try {
				PacketThreading.lock.lock();
				serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
				serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
				serverChannel.write(message);
			} finally {
				PacketThreading.lock.unlock();
			}
		} else {
			serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
			serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
			serverChannel.write(message);
		}
	}

	public void sendToAllAround(ByteBuf message, NetworkRegistry.TargetPoint point) {
		if(!Thread.currentThread().getName().contains(PacketThreading.threadPrefix)) {
			try {
				PacketThreading.lock.lock();
				serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
				serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
				serverChannel.write(message);
			} finally {
				PacketThreading.lock.unlock();
			}
		} else {
			serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
			serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
			serverChannel.write(message);
		}
	}

	public void sendTo(IMessage message, EntityPlayerMP player) {
		if(!Thread.currentThread().getName().contains(PacketThreading.threadPrefix)) {
			try {
				PacketThreading.lock.lock();
				serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
				serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
				serverChannel.write(message);
			} finally {
				PacketThreading.lock.unlock();
			}
		} else {
			serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
			serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
			serverChannel.write(message);
		}
	}

	public void sendToAll(IMessage message) {
		if(!Thread.currentThread().getName().contains(PacketThreading.threadPrefix)) {
			try {
				PacketThreading.lock.lock();
				serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
				serverChannel.write(message);
			} finally {
				PacketThreading.lock.unlock();
			}
		} else {
			serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
			serverChannel.write(message);
		}
	}
}

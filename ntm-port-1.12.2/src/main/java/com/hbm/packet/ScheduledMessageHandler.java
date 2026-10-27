package com.hbm.packet;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Runs every registered packet handler on the owning game thread.
 *
 * <p>Forge invokes receive handlers on the Netty thread. Mutating a world, TE,
 * entity or inventory there races the server/client tick. This wrapper is
 * installed once at registration so the rule is uniform instead of being
 * re-implemented (and forgotten) in individual handlers. Data decoded by
 * {@code fromBytes} must not reference the Netty-owned ByteBuf; BufPacket and
 * EntityBufPacket copy their payload explicitly.</p>
 */
public class ScheduledMessageHandler<REQ extends IMessage, REPLY extends IMessage> implements IMessageHandler<REQ, REPLY> {

	private final IMessageHandler<? super REQ, ? extends REPLY> delegate;
	private final Side side;

	public ScheduledMessageHandler(IMessageHandler<? super REQ, ? extends REPLY> delegate, Side side) {
		this.delegate = delegate;
		this.side = side;
	}

	@Override
	public REPLY onMessage(REQ message, MessageContext ctx) {
		if(side == Side.SERVER) {
			if(ctx.getServerHandler() == null || ctx.getServerHandler().player == null) return null;

			EntityPlayerMP player = ctx.getServerHandler().player;
			player.getServerWorld().addScheduledTask(() -> delegate.onMessage(message, ctx));
		} else {
			scheduleClient(() -> delegate.onMessage(message, ctx));
		}

		return null;
	}

	private static void scheduleClient(Runnable task) {
		// Keep the common packet class free of a static reference to a
		// client-only class; this class is also loaded on dedicated servers.
		try {
			Class<?> scheduler = Class.forName("com.hbm.packet.ClientPacketScheduler");
			scheduler.getMethod("schedule", Runnable.class).invoke(null, task);
		} catch(ReflectiveOperationException e) {
			throw new RuntimeException("Client packet scheduler is unavailable", e);
		}
	}
}

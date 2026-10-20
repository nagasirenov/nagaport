package com.hbm.packet.toserver;

import com.hbm.advancement.NtmAdvancements;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Fixed inventory action only: no client-provided award ID, player ID or borrowed payload. */
public final class LegacyInventoryActionPacket implements IMessage {
    @Override public void fromBytes(ByteBuf buffer) {
        if(buffer.isReadable()) throw new IllegalArgumentException("Inventory action packet must be empty");
    }
    @Override public void toBytes(ByteBuf buffer) { }
    public static final class Handler implements IMessageHandler<LegacyInventoryActionPacket, IMessage> {
        @Override public IMessage onMessage(LegacyInventoryActionPacket message, MessageContext context) {
            final EntityPlayerMP sender = context.getServerHandler().player;
            sender.getServerWorld().addScheduledTask(() -> NtmAdvancements.award(sender, NtmAdvancements.OPEN_INVENTORY));
            return null;
        }
    }
}

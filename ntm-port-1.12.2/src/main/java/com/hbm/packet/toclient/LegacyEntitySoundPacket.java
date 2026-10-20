package com.hbm.packet.toclient;

import java.util.Objects;

import com.hbm.sound.SoundCategoryResolver;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Scoped NTM transport; not a replacement for other mods' native sound packets. */
public final class LegacyEntitySoundPacket implements IMessage {
    private String name;
    private int x;
    private int y;
    private int z;
    private float volume;
    private int pitch;

    public LegacyEntitySoundPacket() { }

    public LegacyEntitySoundPacket(String name, double x, double y, double z, float volume, float pitch) {
        // The old built-in packet rejected null here, not at the World/event boundary.
        this.name = Objects.requireNonNull(name, "soundName");
        this.x = (int)(x * 8.0D);
        this.y = (int)(y * 8.0D);
        this.z = (int)(z * 8.0D);
        this.volume = volume;
        this.pitch = Math.max(0, Math.min(255, (int)(pitch * 63.0F)));
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        name = packet.readString(256);
        x = packet.readInt();
        y = packet.readInt();
        z = packet.readInt();
        volume = packet.readFloat();
        pitch = packet.readUnsignedByte();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        PacketBuffer packet = new PacketBuffer(buffer);
        packet.writeString(name);
        packet.writeInt(x);
        packet.writeInt(y);
        packet.writeInt(z);
        packet.writeFloat(volume);
        packet.writeByte(pitch);
    }

    public static final class Handler implements IMessageHandler<LegacyEntitySoundPacket, IMessage> {
        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(LegacyEntitySoundPacket message, MessageContext context) {
            Minecraft client = Minecraft.getMinecraft();
            // Resource metadata and the sound engine belong to the client main thread.
            client.addScheduledTask(() -> {
                ResourceLocation location = new ResourceLocation(message.name);
                client.getSoundHandler().playSound(new PositionedSoundRecord(location,
                    SoundCategoryResolver.get(location), message.volume, message.pitch / 63.0F,
                    false, 0, ISound.AttenuationType.LINEAR,
                    (float)(message.x / 8.0D), (float)(message.y / 8.0D), (float)(message.z / 8.0D)));
            });
            return null;
        }
    }
}

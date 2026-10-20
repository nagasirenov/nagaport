package com.hbm.sound;

import java.lang.reflect.Field;
import java.util.function.Function;

import com.hbm.packet.toclient.LegacyEntitySoundPacket;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Downstream transport only. No World/entity call sites are activated by this class. */
public final class EntitySoundTransport {
    private EntitySoundTransport() { }

    private static final class ServerFields {
        private static final Field SERVER = ReflectionHelper.findField(ServerWorldEventHandler.class, "server", "field_72783_a");
        private static final Field WORLD = ReflectionHelper.findField(ServerWorldEventHandler.class, "world", "field_72782_b");
    }

    /** The caller owns event posting, live listener traversal and proven source coordinates. */
    public static void dispatch(IWorldEventListener listener, SoundEvent sound, SoundCategory category,
            double x, double y, double z, float volume, float pitch,
            Function<IMessage, Packet<?>> encoder) {
        if(listener.getClass() != ServerWorldEventHandler.class) {
            // Subclasses and all other observers retain their own native implementation.
            listener.playSoundToAllNearExcept(null, sound, category, x, y, z, volume, pitch);
            return;
        }
        try {
            MinecraftServer server = (MinecraftServer) ServerFields.SERVER.get(listener);
            WorldServer world = (WorldServer) ServerFields.WORLD.get(listener);
            PlayerList players = server.getPlayerList();
            int dimension = world.provider.getDimension();
            ResourceLocation name = sound == null ? null : sound.getSoundName();
            // Read the event's resource name only for this built-in packet path. Null is
            // passed into the packet constructor so custom observers still receive null.
            LegacyEntitySoundPacket message = new LegacyEntitySoundPacket(
                name == null ? null : name.toString(),
                x, y, z, volume, pitch);
            players.sendToAllNearExcept(null, x, y, z,
                volume > 1.0F ? (double)(16.0F * volume) : 16.0D,
                dimension, encoder.apply(message));
        } catch(IllegalAccessException failure) {
            throw new IllegalStateException("Cannot read native server sound listener", failure);
        }
    }

    /** Scoped coordinate aliases: native observers own the real event, while the
     * exact built-in listener retains the audited original client resource name.
     * This overload neither resolves arbitrary names nor changes registry entries.
     */
    public static void dispatchResource(IWorldEventListener listener, SoundEvent sound, SoundCategory category,
            String resourceName, double x, double y, double z, float volume, float pitch,
            Function<IMessage, Packet<?>> encoder) {
        if(listener.getClass() != ServerWorldEventHandler.class) {
            listener.playSoundToAllNearExcept(null, sound, category, x, y, z, volume, pitch);
            return;
        }
        try {
            MinecraftServer server = (MinecraftServer) ServerFields.SERVER.get(listener);
            WorldServer world = (WorldServer) ServerFields.WORLD.get(listener);
            PlayerList players = server.getPlayerList();
            int dimension = world.provider.getDimension();
            LegacyEntitySoundPacket message = new LegacyEntitySoundPacket(resourceName, x, y, z, volume, pitch);
            players.sendToAllNearExcept(null, x, y, z,
                volume > 1.0F ? (double)(16.0F * volume) : 16.0D,
                dimension, encoder.apply(message));
        } catch(IllegalAccessException failure) {
            throw new IllegalStateException("Cannot read native server sound listener", failure);
        }
    }
}

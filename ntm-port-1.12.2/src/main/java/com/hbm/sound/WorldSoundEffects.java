package com.hbm.sound;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import com.hbm.main.ModSoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Bounded bridge for the original coordinate World.playSoundEffect callback contract. */
public final class WorldSoundEffects {
    private WorldSoundEffects() { }

    private static final class ListenerField {
        // Read the exact World-declared field, not a subclass field. Cache only the
        // Field descriptor, never a world's mutable list or a copy of its contents.
        private static final Field FIELD = ReflectionHelper.findField(World.class, "eventListeners", "field_73021_x");
    }

    public static void play(World world, double x, double y, double z, String name, float volume, float pitch) {
        // Like invokevirtual, fail on a null receiver only after all arguments ran.
        Objects.requireNonNull(world, "world");
        ResourceLocation location = new ResourceLocation(name);
        SoundEvent sound = ModSoundEvents.worldEffect(location);
        SoundCategory category = ModSoundEvents.worldEffectCategory(location);
        // Original indexed, live-list iteration: preserve order, duplicate listeners,
        // additions/removals or field replacement during callbacks, and empty-world silence.
        for(int i = 0; i < listeners(world).size(); i++) {
            IWorldEventListener listener = listeners(world).get(i);
            float callbackPitch = pitch;
            if(listener.getClass() == ServerWorldEventHandler.class) {
                if(SoundEvent.REGISTRY.getObject(location) != sound)
                    throw new IllegalStateException("World sound event is not registered: " + location);
                // Only the built-in packet path encoded pitch as unsigned byte / 63F.
                // Other observers and subclass overrides own their raw-pitch handling.
                callbackPitch = Math.max(0, Math.min(255, (int)(pitch * 63.0F))) / 63.0F;
            }
            listener.playSoundToAllNearExcept(null, sound, category, x, y, z, volume, callbackPitch);
        }
        // Deliberately do not invoke World.playSound: native Forge adds an entity
        // event absent from this original coordinate contract. RenderGlobal's callback
        // remains silent, including before a client player exists.
    }

    @SuppressWarnings("unchecked")
    private static List<IWorldEventListener> listeners(World world) {
        try {
            return (List<IWorldEventListener>) ListenerField.FIELD.get(world);
        } catch(IllegalAccessException failure) {
            throw new IllegalStateException("Cannot access native world sound listeners", failure);
        }
    }
}

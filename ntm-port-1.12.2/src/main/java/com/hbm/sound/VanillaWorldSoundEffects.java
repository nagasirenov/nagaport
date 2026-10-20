package com.hbm.sound;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import com.hbm.packet.PacketDispatcher;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Coordinate-only adaptation for fifteen audited vanilla resource names.
 * Native observers receive vanilla registry identities. Only the exact built-in
 * server listener uses the original resource name and packet quantization; client
 * resource aliases retain the original samples and reloadable category metadata.
 * This does not intercept other world/entity sounds or register vanilla events.
 */
public final class VanillaWorldSoundEffects {
    private VanillaWorldSoundEffects() { }

    private static final class ListenerField {
        private static final Field FIELD = ReflectionHelper.findField(World.class, "eventListeners", "field_73021_x");
    }

    public static void play(World world, double x, double y, double z, String name, float volume, float pitch) {
        // All original receiver/argument expressions have already run, in order.
        Objects.requireNonNull(world, "world");
        SoundEvent sound = event(name);
        SoundCategory category = category(name);
        // Keep the original live indexed traversal, including field replacement.
        for(int i = 0; i < listeners(world).size(); i++) {
            EntitySoundTransport.dispatchResource(listeners(world).get(i), sound, category, name,
                x, y, z, volume, pitch, message -> PacketDispatcher.wrapper.getPacketFrom(message));
        }
        // No World.playSound entity hook; native RenderGlobal remains a no-op.
    }

    private static SoundEvent event(String name) {
        switch(name) {
            case "ambient.weather.thunder": return SoundEvents.ENTITY_LIGHTNING_THUNDER;
            case "random.explode": return SoundEvents.ENTITY_GENERIC_EXPLODE;
            case "random.fizz": return SoundEvents.BLOCK_FIRE_EXTINGUISH;
            case "random.click": return SoundEvents.BLOCK_LEVER_CLICK;
            case "fire.fire": return SoundEvents.BLOCK_FIRE_AMBIENT;
            case "random.break": return SoundEvents.ENTITY_ITEM_BREAK;
            case "mob.zombie.woodbreak": return SoundEvents.ENTITY_ZOMBIE_BREAK_DOOR_WOOD;
            case "mob.endermen.portal": return SoundEvents.ENTITY_ENDERMEN_TELEPORT;
            case "fireworks.blast": return SoundEvents.ENTITY_FIREWORK_BLAST;
            case "fire.ignite": return SoundEvents.ITEM_FLINTANDSTEEL_USE;
            case "tile.piston.out": return SoundEvents.BLOCK_PISTON_EXTEND;
            case "tile.piston.in": return SoundEvents.BLOCK_PISTON_CONTRACT;
            case "minecart.base": return SoundEvents.ENTITY_MINECART_RIDING;
            case "game.neutral.swim.splash": return SoundEvents.ENTITY_GENERIC_SPLASH;
            case "random.orb": return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            default: throw new IllegalArgumentException("Not an audited vanilla coordinate sound: " + name);
        }
    }

    private static SoundCategory category(String name) {
        switch(name) {
            case "ambient.weather.thunder": return SoundCategory.WEATHER;
            case "random.explode": return SoundCategory.BLOCKS;
            case "random.fizz": return SoundCategory.BLOCKS;
            case "random.click": return SoundCategory.BLOCKS;
            case "fire.fire": return SoundCategory.BLOCKS;
            case "random.break": return SoundCategory.PLAYERS;
            case "mob.zombie.woodbreak": return SoundCategory.HOSTILE;
            case "mob.endermen.portal": return SoundCategory.HOSTILE;
            case "fireworks.blast": return SoundCategory.AMBIENT;
            case "fire.ignite": return SoundCategory.BLOCKS;
            case "tile.piston.out": return SoundCategory.BLOCKS;
            case "tile.piston.in": return SoundCategory.BLOCKS;
            case "minecart.base": return SoundCategory.NEUTRAL;
            case "game.neutral.swim.splash": return SoundCategory.NEUTRAL;
            case "random.orb": return SoundCategory.PLAYERS;
            default: throw new IllegalArgumentException("Not an audited vanilla coordinate sound: " + name);
        }
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

package com.hbm.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.block.Block;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

/** Focused native vanilla variants for packet effects. Constructors never enqueue particles.
 * Vanilla owns updates, rendering, queues and caps; only explicit packet motion/collision is configured.
 */
public final class NativeVanillaParticles {
    private NativeVanillaParticles() {}
    private interface Configurable {
        void motion(double x, double y, double z);
        void collision(boolean enabled);
    }
    public static void setMotion(Particle particle, double x, double y, double z) {
        ((Configurable) particle).motion(x, y, z);
    }
    public static void setCollision(Particle particle, boolean enabled) {
        ((Configurable) particle).collision(enabled);
    }
    /** Resolve packet names using the native registry; unknown names remain silent. */
    public static void spawnNamed(World world, String name, double x, double y, double z, double mx, double my, double mz) {
        String[] parts = name.split("_");
        EnumParticleTypes type = EnumParticleTypes.getByName(parts[0]);
        if(type == null) return;
        try {
            int[] arguments = new int[type.getArgumentCount()];
            if(type == EnumParticleTypes.BLOCK_CRACK || type == EnumParticleTypes.BLOCK_DUST) {
                if(parts.length < 2) return;
                Block block = Block.getBlockById(Integer.parseInt(parts[1]));
                int meta = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
                arguments[0] = Block.getStateId(block.getStateFromMeta(meta));
            } else {
                if(parts.length < 2 && arguments.length > 0) return;
                for(int i = 0; i < arguments.length && i + 1 < parts.length; i++) arguments[i] = Integer.parseInt(parts[i + 1]);
            }
            world.spawnParticle(type, x, y, z, mx, my, mz, arguments);
        } catch(IllegalArgumentException ignored) {
            // Malformed optional visual packets must not interrupt packet handling.
        }
    }
    public static final class Cloud extends ParticleCloud implements Configurable {
        public Cloud(World world, double x, double y, double z, double mx, double my, double mz) { super(world, x, y, z, mx, my, mz); }
        public void motion(double x, double y, double z) { motionX = x; motionY = y; motionZ = z; }
        public void collision(boolean enabled) { canCollide = enabled; }
    }
    public static final class Flame extends ParticleFlame implements Configurable {
        public Flame(World world, double x, double y, double z, double mx, double my, double mz) { super(world, x, y, z, mx, my, mz); }
        public void motion(double x, double y, double z) { motionX = x; motionY = y; motionZ = z; }
        public void collision(boolean enabled) { canCollide = enabled; }
    }
    public static final class Reddust extends ParticleRedstone implements Configurable {
        public Reddust(World world, double x, double y, double z, float r, float g, float b) { super(world, x, y, z, r, g, b); }
        public void motion(double x, double y, double z) { motionX = x; motionY = y; motionZ = z; }
        public void collision(boolean enabled) { canCollide = enabled; }
    }
    public static final class Smoke extends ParticleSmokeNormal implements Configurable {
        public Smoke(World world, double x, double y, double z, double mx, double my, double mz) { super(world, x, y, z, mx, my, mz, 1F); }

        public Smoke(World world, double x, double y, double z, double mx, double my, double mz, float scale) { super(world, x, y, z, mx, my, mz, scale); }
        public void motion(double x, double y, double z) { motionX = x; motionY = y; motionZ = z; }
        public void collision(boolean enabled) { canCollide = enabled; }
    }
    public static final class BlockDust extends ParticleBlockDust implements Configurable {
        public BlockDust(World world, double x, double y, double z, double mx, double my, double mz, Block block, int meta) { super(world, x, y, z, mx, my, mz, block.getStateFromMeta(meta)); init(); }
        public void motion(double x, double y, double z) { motionX = x; motionY = y; motionZ = z; }
        public void collision(boolean enabled) { canCollide = enabled; }
    }
    public static final class FireworkSpark extends ParticleFirework.Spark implements Configurable {
        public FireworkSpark(World world, double x, double y, double z, double mx, double my, double mz, ParticleManager manager) { super(world, x, y, z, mx, my, mz, manager); }
        public void motion(double x, double y, double z) { motionX = x; motionY = y; motionZ = z; }
        public void collision(boolean enabled) { canCollide = enabled; }
    }
    public static final class LargeExplode extends ParticleExplosionLarge implements Configurable {
        public LargeExplode(TextureManager textures, World world, double x, double y, double z, double mx, double my, double mz) { super(textures, world, x, y, z, mx, my, mz); }
        public void motion(double x, double y, double z) { motionX = x; motionY = y; motionZ = z; }
        public void collision(boolean enabled) { canCollide = enabled; }
    }
    public static final class Explode extends ParticleExplosion implements Configurable {
        public Explode(World world, double x, double y, double z, double mx, double my, double mz) { super(world, x, y, z, mx, my, mz); }
        public void motion(double x, double y, double z) { motionX = x; motionY = y; motionZ = z; }
        public void collision(boolean enabled) { canCollide = enabled; }
    }
    public static final class Aura extends ParticleSuspendedTown implements Configurable {
        public Aura(World world, double x, double y, double z, double mx, double my, double mz) { super(world, x, y, z, mx, my, mz); }
        public void motion(double x, double y, double z) { motionX = x; motionY = y; motionZ = z; }
        public void collision(boolean enabled) { canCollide = enabled; }
    }
    public static final class Crit extends ParticleCrit implements Configurable {
        public Crit(World world, double x, double y, double z, double mx, double my, double mz) { super(world, x, y, z, mx, my, mz); }
        public void motion(double x, double y, double z) { motionX = x; motionY = y; motionZ = z; }
        public void collision(boolean enabled) { canCollide = enabled; }
    }
}

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.hbm.entity.effect.EntityEMPBlast;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;

/** Entire production entity; real target entity construction, metadata codec, and NBT.
 * Native World constructor runs; unavailable chunk/save services fail if unexpectedly used.
 * This effect has no world queries and deliberately never calls super.onUpdate().
 * No Minecraft/Forge stand-ins, substituted entity implementation, or GL execution. */
public final class EmpEffectMinecraftTest {
    private static int checks;
    private static World world;
    /** Supplies native entity dimension/remote state without loading chunks, saves, or a game client. */
    private static final class FixtureWorld extends World {
        FixtureWorld(boolean remote) {
            super(null, new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), remote);
        }
        @Override protected IChunkProvider createChunkProvider() {
            throw new UnsupportedOperationException("EMP fixture must not create chunks");
        }
        @Override protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
            throw new UnsupportedOperationException("EMP fixture must not query chunks");
        }
    }
    private static void check(boolean value, String label) {
        checks++;
        if(!value) throw new AssertionError(label);
    }
    private static void sameFloat(float expected, float actual, String label) {
        check(Float.floatToIntBits(expected) == Float.floatToIntBits(actual), label);
    }
    private static void basic(EntityEMPBlast e, boolean frustum, int maxAge) {
        check(e.width == 1.5F && e.height == 1.5F, "native dimensions");
        check(e.getEntityBoundingBox().maxX - e.getEntityBoundingBox().minX == 1.5D,
              "native size updates bounding box");
        check(e.ignoreFrustumCheck == frustum, "constructor-specific frustum flag");
        check(e.isImmuneToFire(), "fire immunity");
        check(e.age == 0 && e.scale == 0 && !e.isDead, "initial age/scale/liveness");
        check(e.maxAge == 100 && e.getMaxAge() == maxAge, "public maxAge field is not watched lifetime");
        check(e.getBrightness() == 1.0F && e.getBrightnessForRender() == 15728880,
              "native no-argument brightness callbacks");
    }
    private static void constructorsAndTicks() {
        EntityEMPBlast defaultEntity = new EntityEMPBlast(world);
        basic(defaultEntity, true, 0);
        defaultEntity.maxAge = 100000;
        defaultEntity.onUpdate();
        check(defaultEntity.isDead && defaultEntity.age == 0 && defaultEntity.scale == 1,
              "default watched zero still dies on first update despite public field");
        for(int lifetime : new int[]{Integer.MIN_VALUE, -1, 0, 1, 2, 3, 100, 32768, Integer.MAX_VALUE}) {
            EntityEMPBlast e = new EntityEMPBlast(world, lifetime);
            basic(e, false, lifetime);
            e.prevPosX = -11; e.prevPosY = -12; e.prevPosZ = -13;
            e.setPosition(11, 12, 13);
            e.motionX = 1; e.motionY = 2; e.motionZ = 3;
            int steps = lifetime > 0 && lifetime <= 100 ? lifetime : 1;
            for(int tick = 1; tick <= steps; tick++) {
                e.onUpdate();
                boolean dead = tick >= lifetime;
                check(e.isDead == dead && e.age == (dead ? 0 : tick), "lifetime/death tick " + lifetime + "/" + tick);
                sameFloat(tick, e.scale, "scale advances including death tick");
                check(e.ticksExisted == 0 && e.prevPosX == -11 && e.prevPosY == -12 && e.prevPosZ == -13,
                      "no added vanilla super.onUpdate bookkeeping");
                check(e.posX == 11 && e.posY == 12 && e.posZ == 13 && e.motionX == 1 && e.motionY == 2 && e.motionZ == 3,
                      "effect update does not add movement or physics");
            }
        }
        for(int age : new int[]{Integer.MIN_VALUE, -2, -1, 0, 1, 99, 100, Integer.MAX_VALUE - 1, Integer.MAX_VALUE}) {
            for(int lifetime : new int[]{Integer.MIN_VALUE, -1, 0, 1, 100, Integer.MAX_VALUE}) {
                EntityEMPBlast e = new EntityEMPBlast(world, lifetime);
                e.age = age;
                int incremented = age + 1;
                boolean dead = incremented >= lifetime;
                e.onUpdate();
                check(e.age == (dead ? 0 : incremented) && e.isDead == dead, "signed increment/overflow before comparison");
            }
        }
        for(float scale : new float[]{-2.75F, -1F, 0F, 1.25F, 16777216F, Float.MAX_VALUE,
                                      Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            EntityEMPBlast e = new EntityEMPBlast(world, 1);
            e.scale = scale; e.onUpdate();
            sameFloat(scale + 1F, e.scale, "float increment semantics including death, large and nonfinite values");
        }
        DeathObserver observed = new DeathObserver(3);
        observed.age = 2; observed.scale = 4.5F; observed.onUpdate();
        check(observed.deathCalls == 1 && observed.ageAtDeath == 0 && observed.scaleAtDeath == 4.5F,
              "age resets before actual native death; scale increments afterward");
        check(observed.isDead && observed.age == 0 && observed.scale == 5.5F, "native setDead executes");
        EntityEMPBlast changed = new EntityEMPBlast(world, 100);
        changed.age = 40; changed.scale = 40; changed.setMaxAge(20);
        check(!changed.isDead && changed.age == 40 && changed.scale == 40, "setter has no extra lifetime side effects");
        changed.onUpdate();
        check(changed.isDead && changed.age == 0 && changed.scale == 41, "shortened lifetime checked at next update");
    }
    /** Observation hook only; construction and onUpdate are the complete production implementation. */
    private static final class DeathObserver extends EntityEMPBlast {
        int deathCalls, ageAtDeath;
        float scaleAtDeath;
        DeathObserver(int lifetime) { super(EmpEffectMinecraftTest.world, lifetime); }
        @Override public void setDead() {
            deathCalls++; ageAtDeath = age; scaleAtDeath = scale;
            super.setDead();
        }
    }
    @SuppressWarnings("unchecked")
    private static DataParameter<Integer> maxAgeKey() throws Exception {
        Field f = EntityEMPBlast.class.getDeclaredField("MAX_AGE");
        f.setAccessible(true);
        return (DataParameter<Integer>)f.get(null);
    }
    private static SPacketEntityMetadata wire(SPacketEntityMetadata sent) throws Exception {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        try {
            sent.writePacketData(buffer);
            SPacketEntityMetadata received = new SPacketEntityMetadata();
            received.readPacketData(buffer);
            check(buffer.readableBytes() == 0, "native packet consumes complete metadata frame");
            check(sent.getEntityId() == received.getEntityId(), "native packet entity ID");
            return received;
        } finally { buffer.release(); }
    }
    private static void metadata() throws Exception {
        EntityEMPBlast sender = new EntityEMPBlast(world), receiver = new EntityEMPBlast(world);
        DataParameter<Integer> key = maxAgeKey();
        check(key.getSerializer() == DataSerializers.VARINT, "native VARINT serializer");
        EntityDataManager data = sender.getDataManager();
        check(data.get(key) == 0 && sender.getMaxAge() == 0, "native registered zero default");
        Set<Integer> ids = new HashSet<>();
        for(EntityDataManager.DataEntry<?> entry : data.getAll())
            check(ids.add(entry.getKey().getId()), "unique metadata ID alongside native inherited entries");
        check(ids.contains(key.getId()) && ids.size() > 1, "effect entry coexists with native entity entries");
        data.setClean();
        check(!data.isDirty() && data.getDirty() == null, "native clean metadata baseline");
        sender.setMaxAge(0);
        check(!data.isDirty(), "setting unchanged maxAge does not dirty native metadata");
        for(int value : new int[]{1, 2, 127, 128, 255, 32767, 32768, 65535, Integer.MAX_VALUE,
                                 -1, Integer.MIN_VALUE, 0}) {
            sender.setMaxAge(value);
            check(sender.getMaxAge() == value && sender.maxAge == 100, "native watcher stores full signed integer only");
            check(data.isDirty(), "changed maxAge is dirty");
            SPacketEntityMetadata received = wire(new SPacketEntityMetadata(sender.getEntityId(), data, false));
            check(!data.isDirty() && data.getDirty() == null, "incremental packet consumes native dirty set");
            List<EntityDataManager.DataEntry<?>> entries = received.getDataManagerEntries();
            check(entries != null && entries.size() == 1, "only changed effect field in incremental packet");
            check(entries.get(0).getKey().getId() == key.getId()
                  && entries.get(0).getKey().getSerializer() == DataSerializers.VARINT,
                  "wire metadata key and serializer");
            receiver.age = 17; receiver.scale = 9.5F;
            receiver.getDataManager().setEntryValues(entries);
            check(receiver.getMaxAge() == value && receiver.maxAge == 100, "received metadata applies to real entity");
            check(receiver.age == 17 && receiver.scale == 9.5F && !receiver.isDead,
                  "metadata application leaves effect animation/liveness unchanged");
            sender.setMaxAge(value);
            check(!data.isDirty(), "same value remains clean after send");
        }
        sender.setMaxAge(513);
        SPacketEntityMetadata full = wire(new SPacketEntityMetadata(sender.getEntityId(), data, true));
        check(full.getDataManagerEntries().size() == data.getAll().size(), "full snapshot includes native and effect entries");
        check(!data.isDirty(), "full native metadata snapshot clears dirtiness");
        EntityEMPBlast lateReceiver = new EntityEMPBlast(world);
        lateReceiver.getDataManager().setEntryValues(full.getDataManagerEntries());
        check(lateReceiver.getMaxAge() == 513, "full snapshot transfers maxAge without a new dirty mutation");
    }
    private static void nbt() throws Exception {
        Method write = EntityEMPBlast.class.getDeclaredMethod("writeEntityToNBT", NBTTagCompound.class);
        Method read = EntityEMPBlast.class.getDeclaredMethod("readEntityFromNBT", NBTTagCompound.class);
        write.setAccessible(true); read.setAccessible(true);
        int[] ages = {Integer.MIN_VALUE, -32769, -32768, -1, 0, 1, 32767, 32768, 65535, Integer.MAX_VALUE};
        float[] scales = {-65537F, -32769F, -1.75F, 0F, 1.75F, 32767F, 32768F, 65535F,
                          Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.MAX_VALUE};
        for(int age : ages) for(float scale : scales) {
            EntityEMPBlast sender = new EntityEMPBlast(world, 123456);
            sender.age = age; sender.scale = scale;
            NBTTagCompound tag = new NBTTagCompound(); tag.setString("unrelated", "keep");
            write.invoke(sender, tag);
            check(tag.getKeySet().size() == 3 && "keep".equals(tag.getString("unrelated")),
                  "effect serializer writes only two fields and preserves caller data");
            check(tag.hasKey("age", 2) && tag.hasKey("scale", 2), "both effect values retain SHORT tag type");
            check(tag.getShort("age") == (short)age && tag.getShort("scale") == (short)scale,
                  "original signed-short narrowing, fractional truncation, nonfinite casts");
            check(!tag.hasKey("maxAge") && !tag.hasKey("MaxAge"), "watched lifetime not silently persisted");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(tag, bytes);
            NBTTagCompound restored = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));
            EntityEMPBlast receiver = new EntityEMPBlast(world, 99);
            read.invoke(receiver, restored);
            check(receiver.age == (short)age && receiver.scale == (float)(short)scale, "native compressed NBT effect roundtrip");
            check(receiver.getMaxAge() == 99 && receiver.maxAge == 100 && !receiver.isDead,
                  "effect NBT read leaves lifetime and liveness untouched");
        }
        EntityEMPBlast receiver = new EntityEMPBlast(world, 777);
        receiver.age = 12; receiver.scale = 13;
        read.invoke(receiver, new NBTTagCompound());
        check(receiver.age == 0 && receiver.scale == 0 && receiver.getMaxAge() == 777, "missing tags use native zero defaults");
        NBTTagCompound otherTypes = new NBTTagCompound();
        otherTypes.setInteger("age", 65537); otherTypes.setString("scale", "12");
        read.invoke(receiver, otherTypes);
        check(receiver.age == 1 && receiver.scale == 0, "native numeric coercion/wrong-type defaults unchanged");
        EntityEMPBlast sender = new EntityEMPBlast(world, 500);
        sender.age = 32768; sender.scale = 1.75F; sender.setPosition(4, 5, 6);
        NBTTagCompound root = new NBTTagCompound();
        check(sender.writeToNBT(root) == root, "native complete Entity writer returns supplied root");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(root, bytes);
        EntityEMPBlast loaded = new EntityEMPBlast(world);
        loaded.readFromNBT(CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray())));
        check(loaded.age == -32768 && loaded.scale == 1 && loaded.posX == 4 && loaded.posY == 5 && loaded.posZ == 6,
              "native full Entity NBT lifecycle invokes actual effect callbacks");
        check(loaded.getMaxAge() == 0 && loaded.maxAge == 100, "save/load retains original missing-maxAge behavior");
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        world = new FixtureWorld(false);
        constructorsAndTicks();
        world = new FixtureWorld(true);
        constructorsAndTicks();
        world = new FixtureWorld(false);
        metadata();
        nbt();
        System.out.println("PASS: " + checks + " checks; complete production EntityEMPBlast construction/update/death, native EntityDataManager and SPacketEntityMetadata full/dirty roundtrips, native compressed NBT and Entity callbacks, constructor/overflow/truncation edge cases.");
        System.out.println("LIMIT: bounded native-World headless entity fixture; renderer and extracted native factory registration compile only. No client startup, GL state execution, GPU, live tracking, gameplay or runnable-mod claim.");
    }
}

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hbm.handler.RocketMetadata;
import io.netty.buffer.Unpooled;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
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

/** Entire production RocketMetadata, real metadata packets and native Entity fixtures.
 * ExtractedRocketEntity/DummyEntity are explicitly source-extracted caller fragments,
 * not complete production entities. ExtractedStageExpressions contain arithmetic only.
 * No substitute Minecraft, Forge or domain classes, Unsafe, game client or GL execution.
 */
public final class RocketMetadataMinecraftTest {
    private static int checks;
    private static World world;
    private static final class FixtureWorld extends World {
        FixtureWorld(boolean remote) {
            super(null, new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), remote);
        }
        @Override protected IChunkProvider createChunkProvider() {
            throw new UnsupportedOperationException("Metadata fixture must not create chunks");
        }
        @Override protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
            throw new UnsupportedOperationException("Metadata fixture must not query chunks");
        }
    }
    private static void check(boolean value, String label) {
        checks++;
        if(!value) throw new AssertionError(label);
    }
    private static SPacketEntityMetadata wire(SPacketEntityMetadata sent) throws Exception {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        try {
            sent.writePacketData(buffer);
            SPacketEntityMetadata received = new SPacketEntityMetadata();
            received.readPacketData(buffer);
            check(buffer.readableBytes() == 0, "metadata frame fully consumed");
            check(received.getEntityId() == sent.getEntityId(), "native packet entity ID");
            return received;
        } finally { buffer.release(); }
    }
    private static List<EntityDataManager.DataEntry<?>> transfer(ExtractedRocketEntity sender,
            ExtractedRocketEntity receiver, boolean all) throws Exception {
        SPacketEntityMetadata packet = wire(new SPacketEntityMetadata(sender.getEntityId(), sender.getDataManager(), all));
        List<EntityDataManager.DataEntry<?>> entries = packet.getDataManagerEntries();
        if(entries != null) receiver.getDataManager().setEntryValues(entries);
        check(!sender.getDataManager().isDirty(), "packet consumes dirty flags");
        return entries;
    }
    private static void basic() throws Exception {
        ExtractedRocketEntity entity = new ExtractedRocketEntity(world);
        EntityDataManager data = entity.getDataManager();
        RocketMetadata keys = ExtractedRocketEntity.ROCKET;
        check(keys.getCapsule(data) == 0 && keys.getStageCount(data) == 0, "capsule/count zero defaults");
        check(entity.getState() == ExtractedRocketEntity.RocketState.AWAITING && entity.getStateTimer() == 0,
            "state and timer defaults");
        check(data.get(ExtractedRocketEntity.DRIVE).isEmpty(), "native absent stack default");
        check(data.get(NativeMetadataParent.EXTRA) == (byte) 73, "fixture superclass key survives subclass initialization");
        for(int i = 0; i < 5; i++) check(keys.getFirst(data, i) == 0 && keys.getSecond(data, i) == 0, "all five stage pair defaults");
        Set<Integer> ids = new HashSet<>();
        int integers = 0;
        for(EntityDataManager.DataEntry<?> entry : data.getAll()) {
            check(ids.add(entry.getKey().getId()), "keys unique with native Entity and fixture superclass");
            if(entry.getKey().getSerializer() == DataSerializers.VARINT) integers++;
        }
        check(integers >= 14, "twelve rocket VARINTs plus state/timer");
        check(ExtractedRocketEntity.DRIVE.getSerializer() == DataSerializers.ITEM_STACK, "native stack serializer");
        data.setClean();
        keys.setCapsule(data, 0); keys.setStageCount(data, 0);
        for(int i = 0; i < 5; i++) { keys.setFirst(data, i, 0); keys.setSecond(data, i, 0); }
        entity.setStateTimer(0); entity.setDrive(null);
        check(!data.isDirty() && data.getDirty() == null, "unchanged zero/empty writes stay clean");
    }
    private static void signedPackets() throws Exception {
        ExtractedRocketEntity sender = new ExtractedRocketEntity(world), receiver = new ExtractedRocketEntity(world);
        RocketMetadata keys = ExtractedRocketEntity.ROCKET;
        EntityDataManager data = sender.getDataManager(), target = receiver.getDataManager();
        data.setClean();
        for(int value : new int[]{1, 127, 128, 255, 32767, 32768, 65535, Integer.MAX_VALUE, -1, Integer.MIN_VALUE, 0}) {
            keys.setCapsule(data, value); keys.setStageCount(data, value);
            for(int i = 0; i < 5; i++) { keys.setFirst(data, i, value); keys.setSecond(data, i, ~value); }
            List<EntityDataManager.DataEntry<?>> entries = transfer(sender, receiver, false);
            check(entries != null && entries.size() == 12, "every changed rocket field in incremental packet");
            for(EntityDataManager.DataEntry<?> entry : entries)
                check(entry.getKey().getSerializer() == DataSerializers.VARINT, "signed packed field uses native VARINT");
            check(keys.getCapsule(target) == value && keys.getStageCount(target) == value, "full signed capsule/count range");
            for(int i = 0; i < 5; i++) check(keys.getFirst(target, i) == value && keys.getSecond(target, i) == ~value, "full signed pair range");
            keys.setCapsule(data, value); keys.setStageCount(data, value);
            for(int i = 0; i < 5; i++) { keys.setFirst(data, i, value); keys.setSecond(data, i, ~value); }
            check(!data.isDirty(), "same packed values remain clean");
        }
        keys.setCapsule(data, 42); keys.setStageCount(data, 5);
        ExtractedRocketEntity lateReceiver = new ExtractedRocketEntity(world);
        List<EntityDataManager.DataEntry<?>> full = transfer(sender, lateReceiver, true);
        check(full.size() == data.getAll().size(), "full packet includes all inherited and own metadata");
        check(keys.getCapsule(lateReceiver.getDataManager()) == 42 && keys.getStageCount(lateReceiver.getDataManager()) == 5,
            "late full snapshot works without later mutation");
    }
    /** Raw fixture of the source-audited production update order, not RocketStruct execution. */
    private static void writeRaw(EntityDataManager data, int capsule, int[][] stages) {
        RocketMetadata keys = ExtractedRocketEntity.ROCKET;
        keys.setCapsule(data, capsule);
        keys.setStageCount(data, stages.length);
        for(int i = 0; i < stages.length; i++) {
            keys.setFirst(data, i, stages[i][0]);
            keys.setSecond(data, i, stages[i][1]);
        }
    }
    private static void capacityAndStaleSlots() throws Exception {
        ExtractedRocketEntity entity = new ExtractedRocketEntity(world), receiver = new ExtractedRocketEntity(world);
        EntityDataManager data = entity.getDataManager();
        RocketMetadata keys = ExtractedRocketEntity.ROCKET;
        int[][] five = {{10,11},{20,21},{30,31},{40,41},{50,51}};
        writeRaw(data, 77, five); transfer(entity, receiver, false);
        writeRaw(data, 77, new int[][]{{10,11}});
        List<EntityDataManager.DataEntry<?>> entries = transfer(entity, receiver, false);
        check(entries.size() == 1, "shrinking same-prefix rocket dirties count only");
        for(int i = 1; i < 5; i++)
            check(keys.getFirst(data, i) == five[i][0] && keys.getSecond(receiver.getDataManager(), i) == five[i][1], "shrink retains stale trailing pairs");
        writeRaw(data, 77, five);
        check(transfer(entity, receiver, false).size() == 1, "regrowing unchanged pairs dirties count only");
        int[][] six = {{100,101},{200,201},{300,301},{400,401},{500,501},{600,601}};
        boolean failed = false;
        try { writeRaw(data, 88, six); } catch(IndexOutOfBoundsException expected) { failed = true; }
        check(failed && keys.getCapsule(data) == 88 && keys.getStageCount(data) == 6, "six-stage write fails after capsule/count, not clamped or prevalidated");
        for(int i = 0; i < 5; i++) check(keys.getFirst(data, i) == six[i][0] && keys.getSecond(data, i) == six[i][1], "overflow preserves all first-five mutations");
        transfer(entity, receiver, false);
        check(keys.getStageCount(receiver.getDataManager()) == 6, "partially mutated over-capacity count remains native metadata");
        failed = false;
        try { keys.getFirst(data, 5); } catch(IndexOutOfBoundsException expected) { failed = true; }
        check(failed, "over-capacity read is not a fabricated default");
        keys.setStageCount(data, -1);
        int visited = 0;
        for(int i = 0; i < keys.getStageCount(data); i++) { keys.getFirst(data, i); visited++; }
        check(visited == 0, "negative count retains zero-iteration behavior");
    }
    private static void stateAndDrive() throws Exception {
        ExtractedRocketEntity sender = new ExtractedRocketEntity(world), receiver = new ExtractedRocketEntity(world);
        EntityDataManager data = sender.getDataManager();
        for(ExtractedRocketEntity.RocketState state : ExtractedRocketEntity.RocketState.values()) {
            sender.stateTimer = 100; sender.setStateTimer(Integer.MIN_VALUE); sender.setState(state);
            check(sender.getState() == state && sender.getStateTimer() == 0 && sender.stateTimer == 0, "state ordinal and both timer resets");
            receiver.stateTimer = 91; transfer(sender, receiver, false);
            check(receiver.getState() == state && receiver.getStateTimer() == 0 && receiver.stateTimer == 91, "network apply does not call state setter/local reset");
        }
        for(int timer : new int[]{Integer.MIN_VALUE, -1, 0, Integer.MAX_VALUE}) {
            sender.setStateTimer(timer); transfer(sender, receiver, false);
            check(receiver.getStateTimer() == timer, "timer is a signed int, not clamped");
        }
        data.set(ExtractedRocketEntity.STATE, -1);
        boolean failed = false;
        try { sender.getState(); } catch(ArrayIndexOutOfBoundsException expected) { failed = true; }
        check(failed, "invalid ordinal retains direct-index failure");
        sender.setState(ExtractedRocketEntity.RocketState.AWAITING);
        ItemStack stack = new ItemStack(Items.DIAMOND, 3, 9);
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("destination", "preserve"); stack.setTagCompound(tag);
        sender.setDrive(stack);
        check(data.get(ExtractedRocketEntity.DRIVE) == stack, "setter preserves caller reference, no new defensive copying");
        transfer(sender, receiver, false);
        ItemStack received = receiver.getDataManager().get(ExtractedRocketEntity.DRIVE);
        check(received != stack && received.getItem() == Items.DIAMOND && received.getCount() == 3 && received.getItemDamage() == 9,
            "native stack packet has independent item/count/damage");
        check("preserve".equals(received.getTagCompound().getString("destination")), "native stack NBT preserved");
        ItemStack copy = DataSerializers.ITEM_STACK.copyValue(stack);
        check(copy != stack && copy.getTagCompound() != stack.getTagCompound(), "native serializer deeply copies stack/NBT");
        sender.setDrive(stack);
        check(!data.isDirty(), "same stack reference stays clean");
        stack.getTagCompound().setString("destination", "mutated-in-place");
        sender.setDrive(stack);
        check(!data.isDirty(), "existing alias mutation does not silently gain dirty marking");
        sender.setDrive(null); transfer(sender, receiver, false);
        check(data.get(ExtractedRocketEntity.DRIVE).isEmpty() && receiver.getDataManager().get(ExtractedRocketEntity.DRIVE).isEmpty(), "null converted to native empty across wire");
        sender.setDrive(ItemStack.EMPTY);
        check(!data.isDirty(), "same empty stack remains clean");
    }
    private static void dummy() throws Exception {
        ExtractedDummyEntity sender = new ExtractedDummyEntity(world), receiver = new ExtractedDummyEntity(world);
        check(sender.getDataManager().get(ExtractedDummyEntity.PARENT_ID) == 0, "dummy parent default zero");
        check(ExtractedDummyEntity.PARENT_ID.getSerializer() == DataSerializers.VARINT, "dummy native serializer");
        sender.getDataManager().setClean();
        for(int id : new int[]{1, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, 0}) {
            sender.getDataManager().set(ExtractedDummyEntity.PARENT_ID, id);
            List<EntityDataManager.DataEntry<?>> entries = wire(new SPacketEntityMetadata(sender.getEntityId(), sender.getDataManager(), false)).getDataManagerEntries();
            check(entries.size() == 1, "dummy incremental packet only parent ID");
            receiver.getDataManager().setEntryValues(entries);
            check(receiver.getDataManager().get(ExtractedDummyEntity.PARENT_ID) == id, "dummy signed parent ID roundtrip");
        }
    }
    private static void arithmetic() {
        int[] cases = {Integer.MIN_VALUE, -65537, -32768, -1, 0, 1, 127, 128, 255, 256, 32767, 32768, 65535, 65536, Integer.MAX_VALUE};
        for(int f : cases) for(int n : cases) for(int t : cases) {
            int fc = cases[Math.floorMod(f, cases.length)], tc = cases[Math.floorMod(t, cases.length)];
            int[] packed = ExtractedStageExpressions.pack(f, n, t, fc, tc);
            // Independent arithmetic construction, with Java integer overflow and OR retained.
            int first = (f * 65536) | n, second = (t * 65536) | (fc * 256) | tc;
            check(packed[0] == first && packed[1] == second, "unmasked signed packing expressions");
            int[] decoded = ExtractedStageExpressions.unpack(packed[0], packed[1]);
            check(decoded[0] == Math.floorDiv(first, 65536) && decoded[1] == (first & 65535)
                && decoded[2] == Math.floorDiv(second, 65536) && decoded[3] == ((second & 65535) / 256)
                && decoded[4] == (second & 255), "arithmetic shift and unsigned low fields");
        }
        check(Arrays.equals(ExtractedStageExpressions.unpack(0,0), new int[]{0,0,0,0,0}), "zero metadata counts stay zero, not new-stage defaults one");
        check(ExtractedStageExpressions.unpack(0, 0x80000000)[2] == -32768, "upper part sign extension not changed to unsigned");
    }
    private static void keyGroupContract() {
        List<DataParameter<Integer>> empty = new ArrayList<>();
        boolean failed = false;
        try { new RocketMetadata(ExtractedRocketEntity.STATE, ExtractedRocketEntity.TIMER, empty, empty); }
        catch(IllegalArgumentException expected) { failed = true; }
        check(failed, "new key-group constructor rejects non-five capacity, independently of runtime stage count");
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        for(boolean remote : new boolean[]{false,true}) {
            world = new FixtureWorld(remote);
            basic(); signedPackets(); capacityAndStaleSlots(); stateAndDrive(); dummy(); keyGroupContract();
        }
        arithmetic();
        System.out.println("PASS: " + checks + " checks; whole production RocketMetadata, native EntityDataManager and SPacketEntityMetadata full/dirty signed-int/stack roundtrips, capacity/partial-write/stale-slot behavior; explicitly extracted state/drive/dummy declarations and packing expressions.");
        System.out.println("LIMIT: no complete RocketStruct, EntityRideableRocket, dummy update lifecycle, MissilePart resolution, chunk ticket, NBT execution, renderer, live entity tracking, gameplay or runnable-mod claim. NBT and other unchanged behavior source-audited only.");
    }
}

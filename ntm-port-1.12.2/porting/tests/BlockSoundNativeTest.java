import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hbm.blocks.ModSoundType;
import com.hbm.blocks.ModSoundType.ModEnvelopedSoundType;
import com.hbm.blocks.ModSoundType.SubType;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.main.MainRegistry;
import com.hbm.main.ModSoundEvents;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundList;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundListSerializer;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LegacyV2Adapter;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.RegistryManager;

/** Complete production sound classes, real native event registry, parser, and resource packs.
 * Only MainRegistry is an explicit NTM-only collaborator supplying the original shared RNG.
 */
public final class BlockSoundNativeTest {
    private static int checks;
    private static void check(boolean condition, String label) {
        checks++;
        if (!condition) throw new AssertionError(label);
    }
    private static void equal(float actual, float expected, String label) {
        check(Float.floatToIntBits(actual) == Float.floatToIntBits(expected), label + ": " + actual + " != " + expected);
    }
    private static void sounds(ModSoundType type, SoundEvent place, SoundEvent broken, SoundEvent step) {
        check(type.getPlaceSound() == place, "exact place event identity");
        check(type.getBreakSound() == broken, "exact break event identity");
        check(type.getStepSound() == step, "exact step event identity");
        check(type.getHitSound() == step, "legacy hit uses step event");
        check(type.getFallSound() == step, "legacy landing uses step event");
    }
    private static SoundEvent fixture(String id) {
        SoundEvent event = new SoundEvent(new ResourceLocation("hbm", id)).setRegistryName("hbm", id);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }
    private static void select(ModSoundType type, int action) {
        switch (action) {
            case 0: type.getPlaceSound(); break;
            case 1: type.getBreakSound(); break;
            case 2: type.getStepSound(); break;
            case 3: type.getHitSound(); break;
            case 4: type.getFallSound(); break;
            default: throw new AssertionError(action);
        }
    }
    private static final class SoundBlock extends Block {
        SoundBlock(SoundType sound) { super(Material.IRON); setSoundType(sound); }
    }
    private static final class ConstructorSound extends ModSoundType {
        ConstructorSound(String place, String broken, String step) { super(place, broken, step, 0.3F, 0.4F); }
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        ModMetadata metadata = new ModMetadata(); metadata.modId = "hbm"; metadata.name = "Native block sounds"; metadata.version = "1";
        DummyModContainer owner = new DummyModContainer(metadata);
        Loader.instance().setupTestHarness(owner); Loader.instance().setActiveModContainer(owner);
        check(ModSoundType.class.getSuperclass() == SoundType.class, "extends genuine native SoundType directly");
        int originalSize = ForgeRegistries.SOUND_EVENTS.getValuesCollection().size();
        SoundEvent[] owned = {ModSoundEvents.SLICER, ModSoundEvents.BLOCK_OPENC, ModSoundEvents.BLOCK_CLOSEC,
            ModSoundEvents.STEP_METAL_BLOCK, ModSoundEvents.BLOCK_PIPE_PLACED, ModSoundEvents.STEP_MORKITE, ModSoundEvents.ITEM_BATTERY, ModSoundEvents.BLOCK_CRATE_BREAK, ModSoundEvents.ITEM_UPGRADE_PLUG, ModSoundEvents.WEAPON_EXPLOSION_LARGE_NEAR, ModSoundEvents.WEAPON_EXPLOSION_LARGE_FAR, ModSoundEvents.WEAPON_CASING_SMALL, ModSoundEvents.WEAPON_CASING_MEDIUM, ModSoundEvents.WEAPON_CASING_LARGE, ModSoundEvents.WEAPON_CASING_SHELL, ModSoundEvents.WEAPON_EXPLOSION_SMALL_NEAR, ModSoundEvents.WEAPON_EXPLOSION_SMALL_FAR};
        String[] names = {"entity.slicer", "block.openc", "block.closec", "step.metalBlock", "block.pipePlaced", "step.morkite", "item.battery", "block.crateBreak", "item.upgradePlug", "weapon.explosionLargeNear", "weapon.explosionLargeFar", "weapon.casing.small", "weapon.casing.medium", "weapon.casing.large", "weapon.casing.shell", "weapon.explosionSmallNear", "weapon.explosionSmallFar"};
        for (int i = 0; i < owned.length; i++) {
            ResourceLocation name = new ResourceLocation("hbm", names[i]);
            check(owned[i].getRegistryName().equals(name), "original event key canonicalized by native ResourceLocation");
            check(owned[i].getSoundName().equals(name), "registry and audio name agree");
            check(!ForgeRegistries.SOUND_EVENTS.containsKey(name), "event is not prematurely registered");
            check(ModSoundEvents.resolve("hbm:" + names[i]) == owned[i], "pre-registration original string resolves stable object");
        }
        sounds(ModSoundTypes.legacyMetal, SoundEvents.BLOCK_STONE_PLACE, SoundEvents.BLOCK_STONE_BREAK, SoundEvents.BLOCK_STONE_STEP);
        equal(ModSoundTypes.legacyMetal.getVolume(), 1F, "original metal volume");
        equal(ModSoundTypes.legacyMetal.getPitch(), 1.5F, "original metal pitch");
        sounds(ModSoundTypes.grate, SoundEvents.BLOCK_STONE_PLACE, SoundEvents.BLOCK_STONE_BREAK, ModSoundEvents.STEP_METAL_BLOCK);
        sounds(ModSoundTypes.pipe, ModSoundEvents.BLOCK_PIPE_PLACED, ModSoundEvents.BLOCK_PIPE_PLACED, SoundEvents.BLOCK_STONE_STEP);
        sounds(ModSoundTypes.mork, SoundEvents.BLOCK_STONE_PLACE, ModSoundEvents.STEP_MORKITE, SoundEvents.BLOCK_STONE_STEP);
        check(new SoundBlock(ModSoundTypes.pipe).getSoundType() == ModSoundTypes.pipe, "preInit block retains native custom sound object");
        equal(ModSoundTypes.grate.getVolume(), 0.5F, "grate volume"); equal(ModSoundTypes.grate.getPitch(), 1F, "grate pitch");
        equal(ModSoundTypes.pipe.getVolume(), 0.85F, "pipe volume"); equal(ModSoundTypes.mork.getVolume(), 0.85F, "morkite volume");
        MinecraftForge.EVENT_BUS.register(ModSoundEvents.class);
        MinecraftForge.EVENT_BUS.post(new RegistryEvent.Register<SoundEvent>(RegistryManager.ACTIVE.getName(ForgeRegistries.SOUND_EVENTS), ForgeRegistries.SOUND_EVENTS));
        check(ForgeRegistries.SOUND_EVENTS.getValuesCollection().size() == originalSize + 62, "real registration adds prior17 and45 bounded world-effect events");
        for (SoundEvent sound : owned) {
            check(ForgeRegistries.SOUND_EVENTS.getValue(sound.getRegistryName()) == sound, "native Forge registry retains object identity");
            check(SoundEvent.REGISTRY.getObject(sound.getRegistryName()) == sound, "native Minecraft registry resolves same object");
            check(ModSoundEvents.resolve(sound.getRegistryName().toString()) == sound, "post-registration identity unchanged");
        }
        int registeredSize = ForgeRegistries.SOUND_EVENTS.getValuesCollection().size();
        boolean rejected = false;
        try { ModSoundType.mod("unregistered_probe", 1, 1); } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "unregistered arbitrary strings fail explicitly, no made-up fallback");
        check(ForgeRegistries.SOUND_EVENTS.getValuesCollection().size() == registeredSize, "factory failure does not mutate native registry");
        check(ModSoundEvents.resolve("minecraft:block.stone.step") == SoundEvents.BLOCK_STONE_STEP, "registered native string resolves exact event");
        SoundEvent dig = fixture("dig.fixture"), step = fixture("step.fixture");
        SoundEvent place = ModSoundEvents.BLOCK_PIPE_PLACED, broken = ModSoundEvents.STEP_MORKITE, customStep = ModSoundEvents.STEP_METAL_BLOCK;
        sounds(ModSoundType.mod("fixture", 0.6F, 0.7F), dig, dig, step);
        sounds(ModSoundType.customPlace("fixture", "hbm:block.pipePlaced", 0.6F, 0.7F), place, dig, step);
        sounds(ModSoundType.customBreak("fixture", "hbm:step.morkite", 0.6F, 0.7F), dig, broken, step);
        sounds(ModSoundType.customStep("fixture", "hbm:step.metalBlock", 0.6F, 0.7F), dig, dig, customStep);
        sounds(ModSoundType.customDig("fixture", "hbm:block.pipePlaced", 0.6F, 0.7F), place, place, step);
        sounds(ModSoundType.customPlace(SoundType.STONE, "hbm:block.pipePlaced", 0.6F, 0.7F), place, SoundEvents.BLOCK_STONE_BREAK, SoundEvents.BLOCK_STONE_STEP);
        sounds(ModSoundType.customBreak(SoundType.STONE, "hbm:step.morkite", 0.6F, 0.7F), SoundEvents.BLOCK_STONE_PLACE, broken, SoundEvents.BLOCK_STONE_STEP);
        sounds(ModSoundType.customStep(SoundType.STONE, "hbm:step.metalBlock", 0.6F, 0.7F), SoundEvents.BLOCK_STONE_PLACE, SoundEvents.BLOCK_STONE_BREAK, customStep);
        sounds(ModSoundType.customDig(SoundType.STONE, "hbm:block.pipePlaced", 0.6F, 0.7F), place, place, SoundEvents.BLOCK_STONE_STEP);
        ModSoundType all = ModSoundType.placeBreakStep("hbm:block.pipePlaced", "hbm:step.morkite", "hbm:step.metalBlock", 0.6F, 0.7F);
        sounds(all, place, broken, customStep);
        sounds(new ConstructorSound("hbm:block.pipePlaced", "hbm:step.morkite", "hbm:step.metalBlock"), place, broken, customStep);
        sounds(all.enveloped(), place, broken, customStep);
        equal(all.enveloped().getVolume(), 0.6F, "no envelope preserves base volume");
        equal(all.enveloped().getPitch(), 0.7F, "no envelope preserves base pitch");
        ModEnvelopedSoundType env = all.enveloped(new Random(49219L));
        Random oracle = new Random(49219L);
        env.volumeFunction((in, rng, subtype) -> in + subtype.ordinal() + rng.nextFloat());
        env.pitchFunction((in, rng, subtype) -> in - subtype.ordinal() + rng.nextFloat());
        equal(env.getVolume(), 0.6F + SubType.PLACE.ordinal() + oracle.nextFloat(), "default probable subtype is PLACE");
        equal(env.getPitch(), 0.7F - SubType.PLACE.ordinal() + oracle.nextFloat(), "default pitch uses shared RNG sequence");
        for (int i = 0; i < 1000; i++) {
            int action = i % 5; SubType expected = action == 0 ? SubType.PLACE : action == 1 ? SubType.BREAK : SubType.STEP;
            select(env, action);
            equal(env.getVolume(), 0.6F + expected.ordinal() + oracle.nextFloat(), "volume envelope preserves subtype and RNG " + i);
            equal(env.getPitch(), 0.7F - expected.ordinal() + oracle.nextFloat(), "pitch envelope preserves subtype and RNG " + i);
            equal(env.getPitch(), 0.7F - expected.ordinal() + oracle.nextFloat(), "repeated queries draw again " + i);
        }
        env.volumeFunction(null).pitchFunction(null);
        equal(env.getVolume(), 0.6F, "nullable volume envelope disabled"); equal(env.getPitch(), 0.7F, "nullable pitch envelope disabled");
        MainRegistry.instance.rand.setSeed(7601L); oracle = new Random(7601L);
        for (int i = 0; i < 1000; i++) {
            int action = i % 5;
            for (ModSoundType type : new ModSoundType[] {ModSoundTypes.pipe, ModSoundTypes.mork}) {
                select(type, action);
                float expected = 0.85F; if (action == 1) expected -= 0.15F;
                expected += oracle.nextFloat() * 0.2F;
                equal(type.getPitch(), expected, "original pipe/mork formula shared RNG order " + i);
                equal(type.getVolume(), 0.85F, "randomized type keeps fixed volume");
                check(expected >= (action == 1 ? 0.70F : 0.85F) && expected < (action == 1 ? 0.90F : 1.05F), "original pitch envelope range");
            }
        }
        verifyResources(new FolderResourcePack(new File(args[0])), "directory");
        try (FileResourcePack pack = new FileResourcePack(new File(args[1]))) { verifyResources(pack, "jar"); }
        System.out.println("PASS native block sounds: " + checks + " assertions; real registry event, five getters, every factory, deterministic envelopes, actual SoundHandler loading, native resource parser and directory/jar lookup");
    }
    private static void verifyResources(IResourcePack pack, String label) throws Exception {
        JsonObject json;
        try (InputStream in = pack.getInputStream(new ResourceLocation("hbm", "sounds.json"))) {
            json = new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
        Gson gson = new GsonBuilder().registerTypeAdapter(SoundList.class, new SoundListSerializer()).create();
        SimpleReloadableResourceManager manager = new SimpleReloadableResourceManager(new MetadataSerializer());
        manager.reloadResourcePack(pack);
        // Constructor does not start OpenAL. Invoke only the real load/validate path,
        // not onResourceManagerReload, which also starts a hardware audio device.
        SoundHandler handler = new SoundHandler(manager, null);
        Method nativeLoad = SoundHandler.class.getDeclaredMethod("loadSoundResource", ResourceLocation.class, SoundList.class);
        nativeLoad.setAccessible(true);
        Method nativeMap = SoundHandler.class.getDeclaredMethod("getSoundMap", InputStream.class);
        nativeMap.setAccessible(true);
        Map<String, SoundList> parsed;
        try (InputStream in = pack.getInputStream(new ResourceLocation("hbm", "sounds.json"))) {
            parsed = (Map<String, SoundList>) nativeMap.invoke(handler, in);
        }
        LegacyV2Adapter legacy = new LegacyV2Adapter(pack);
        Method legacyPath = LegacyV2Adapter.class.getDeclaredMethod("fudgePath", ResourceLocation.class);
        legacyPath.setAccessible(true);
        ResourceLocation language = new ResourceLocation("hbm", "lang/en_us.lang");
        check(((ResourceLocation) legacyPath.invoke(legacy, language)).getPath().equals("lang/en_US.lang"), label + " legacy adapter repairs old language country case");
        ResourceLocation soundJson = new ResourceLocation("hbm", "sounds.json");
        check(legacyPath.invoke(legacy, soundJson) == soundJson, label + " legacy adapter does not rewrite sounds.json");
        String[] keys = {"block.pipePlaced", "step.metalBlock", "step.morkite", "item.battery", "block.crateBreak", "item.upgradePlug", "weapon.explosionLargeNear", "weapon.explosionLargeFar", "weapon.casing.small", "weapon.casing.medium", "weapon.casing.large", "weapon.casing.shell", "weapon.explosionSmallNear", "weapon.explosionSmallFar"};
        int[] counts = {1, 4, 3, 1, 5, 1, 1, 1, 3, 3, 3, 3, 3, 2};
        Set<String> expectedPaths = new HashSet<>(Arrays.asList("sounds/block/pipeplaced.ogg", "sounds/footsteps/metalstep1.ogg", "sounds/footsteps/metalstep2.ogg", "sounds/footsteps/metalstep3.ogg", "sounds/footsteps/metalstep4.ogg", "sounds/mining/morkiteminingb_1.ogg", "sounds/mining/morkiteminingb_2.ogg", "sounds/mining/morkiteminingb_3.ogg", "sounds/tool/battery.ogg", "sounds/block/cratebreak1.ogg", "sounds/block/cratebreak2.ogg", "sounds/block/cratebreak3.ogg", "sounds/block/cratebreak4.ogg", "sounds/block/cratebreak5.ogg", "sounds/tool/upgradeplug.ogg", "sounds/weapon/explosionlargenear.ogg", "sounds/weapon/explosionlargefar.ogg", "sounds/weapon/casing/small1.ogg", "sounds/weapon/casing/small2.ogg", "sounds/weapon/casing/small3.ogg", "sounds/weapon/casing/medium1.ogg", "sounds/weapon/casing/medium2.ogg", "sounds/weapon/casing/medium3.ogg", "sounds/weapon/casing/large1.ogg", "sounds/weapon/casing/large2.ogg", "sounds/weapon/casing/large3.ogg", "sounds/weapon/casing/shell1.ogg", "sounds/weapon/casing/shell2.ogg", "sounds/weapon/casing/shell3.ogg", "sounds/weapon/explosionsmallnear1.ogg", "sounds/weapon/explosionsmallnear2.ogg", "sounds/weapon/explosionsmallnear3.ogg", "sounds/weapon/explosionsmallfar1.ogg", "sounds/weapon/explosionsmallfar2.ogg"));
        Set<String> paths = new HashSet<>();
        for (int i = 0; i < keys.length; i++) {
            ResourceLocation eventKey = new ResourceLocation("hbm", keys[i]);
            check(ModSoundEvents.resolve("hbm:" + keys[i]).getSoundName().equals(eventKey), label + " native event key canonicalization");
            SoundList list = gson.fromJson(json.get(keys[i]), SoundList.class);
            check(list.getSounds().size() == counts[i], label + " original sample count");
            nativeLoad.invoke(handler, eventKey, parsed.get(keys[i]));
            check(handler.getAccessor(eventKey) != null, label + " real SoundHandler creates native event accessor");
            check(handler.getAccessor(eventKey).getWeight() == counts[i], label + " real SoundHandler validates and retains every original sample");
            check(handler.getAccessor(eventKey).cloneEntry() != SoundHandler.MISSING_SOUND, label + " loaded event does not select missing sound");
            for (Sound sound : list.getSounds()) {
                ResourceLocation location = sound.getSoundAsOggLocation();
                check(location.getNamespace().equals("hbm"), label + " native sample namespace, not default minecraft: " + location);
                check(sound.getType() == Sound.Type.FILE && !sound.isStreaming(), label + " sample type and streaming unchanged");
                equal(sound.getVolume(), 1F, label + " original sample gain"); equal(sound.getPitch(), 1F, label + " original sample pitch");
                check(sound.getWeight() == 1, label + " original equal probability");
                check(pack.resourceExists(location), label + " exact native lowercase OGG exists: " + location);
                check(legacyPath.invoke(legacy, location) == location, label + " legacy adapter cannot repair OGG casing or namespace");
                check(legacy.resourceExists(location), label + " native legacy-pack wrapper finds renamed OGG");
                try (InputStream in = pack.getInputStream(location)) {
                    check(in.read() == 'O' && in.read() == 'g' && in.read() == 'g' && in.read() == 'S', label + " original OGG data readable");
                }
                paths.add(location.getPath());
            }
        }
        check(paths.equals(expectedPaths), label + " exactly the original eight block samples, battery sample, five crate samples, foundry insertion sample, and nineteen particle samples");
        // Exercise the actual native rejection too: unqualified legacy file strings
        // remain minecraft: all the way through SoundHandler.validateSoundResource.
        SoundList unqualified = gson.fromJson("{\"sounds\":[\"block/pipePlaced\"]}", SoundList.class);
        ResourceLocation probe = new ResourceLocation("hbm", "namespace_probe");
        nativeLoad.invoke(handler, probe, unqualified);
        check(handler.getAccessor(probe).getWeight() == 0, label + " native loader does not rebase unqualified sample to hbm");
        check(handler.getAccessor(probe).cloneEntry() == SoundHandler.MISSING_SOUND, label + " namespace bug would remain genuinely silent");
    }
}

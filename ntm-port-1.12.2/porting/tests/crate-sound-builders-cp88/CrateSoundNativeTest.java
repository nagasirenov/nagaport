import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import com.hbm.blocks.*;
import com.hbm.blocks.generic.*;

/** A finite native construction-boundary test, not a complete crate gameplay test. */
public final class CrateSoundNativeTest {
    private static int checks;
    private static final List<String> order = new ArrayList<>();
    private static Block current;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static Block withSound(Block owner, SoundType sound) {
        if (owner instanceof BlockCrate) return ((BlockCrate) owner).withSoundType(sound);
        return ((BlockStorageCrate) owner).withSoundType(sound);
    }
    private static Material material() { order.add("material"); return Material.IRON; }
    private static Block capture(Block owner) {
        current = owner;
        check(owner.getDefaultState().getMaterial() == Material.IRON, "native constructor before receiver capture");
        order.add("receiver"); return owner;
    }
    private static String name() {
        check(current.getSoundType() == SoundType.STONE, "sound is not set before name argument");
        order.add("name"); return "order";
    }
    private static SoundType sound() {
        check(current.getTranslationKey().equals("tile.order"), "name setter before sound argument");
        check(current.getSoundType() == SoundType.STONE, "argument evaluated before sound setter");
        order.add("sound"); return ModSoundTypes.legacyMetal;
    }
    private static float hardness() {
        check(current.getSoundType() == ModSoundTypes.legacyMetal, "sound setter before hardness argument");
        order.add("hardness"); return 5F;
    }
    private static float resistance() {
        check(current.getBlockHardness(current.getDefaultState(), null, BlockPos.ORIGIN) == 5F, "hardness setter before resistance argument");
        order.add("resistance"); return 10F;
    }
    private static CreativeTabs tab() {
        check(current.getExplosionResistance(null) == 6F, "resistance setter before tab argument");
        order.add("tab"); return CreativeTabs.REDSTONE;
    }
    private static SoundType nullSound() { order.add("argument"); return null; }
    private static String badName() { order.add("name throws"); throw new IllegalArgumentException("expected"); }
    private static SoundType badSound() { order.add("sound throws"); throw new IllegalArgumentException("expected"); }
    private static Block badReceiver() { order.add("receiver throws"); throw new IllegalArgumentException("expected"); }
    private static void testOrder(boolean storage) {
        order.clear();
        Block result = storage
            ? ((BlockStorageCrate) capture(new BlockStorageCrate(material())).setTranslationKey(name())).withSoundType(sound()).setHardness(hardness()).setResistance(resistance()).setCreativeTab(tab())
            : ((BlockCrate) capture(new BlockCrate(material())).setTranslationKey(name())).withSoundType(sound()).setHardness(hardness()).setResistance(resistance()).setCreativeTab(tab());
        check(result == current, "fresh owner exact receiver through complete chain and cast");
        check(order.equals(Arrays.asList("material", "receiver", "name", "sound", "hardness", "resistance", "tab")), "complete argument and setter order");
        check(result.getCreativeTab() == CreativeTabs.REDSTONE, "final setter applied to same owner");
        order.clear();
        try {
            if (storage) ((BlockStorageCrate) (Block) null).withSoundType(nullSound());
            else ((BlockCrate) (Block) null).withSoundType(nullSound());
            throw new AssertionError("Missing null receiver failure");
        } catch (NullPointerException expected) {
            check(order.equals(Arrays.asList("argument")), "null receiver still evaluates sound argument first");
        }
        order.clear();
        try {
            if (storage) ((BlockStorageCrate) badReceiver()).withSoundType(nullSound());
            else ((BlockCrate) badReceiver()).withSoundType(nullSound());
            throw new AssertionError("Missing receiver failure");
        } catch (IllegalArgumentException expected) {
            check(order.equals(Arrays.asList("receiver throws")), "receiver exception prevents sound evaluation");
        }
        Block b = storage ? new BlockStorageCrate(Material.IRON) : new BlockCrate(Material.IRON);
        order.clear();
        try {
            if (storage) ((BlockStorageCrate) b.setTranslationKey(badName())).withSoundType(nullSound());
            else ((BlockCrate) b.setTranslationKey(badName())).withSoundType(nullSound());
            throw new AssertionError("Missing name failure");
        } catch (IllegalArgumentException expected) {
            check(order.equals(Arrays.asList("name throws")) && b.getSoundType() == SoundType.STONE, "name exception prevents later sound mutation/evaluation");
        }
        order.clear();
        try {
            if (storage) ((BlockStorageCrate) b.setTranslationKey("partial")).withSoundType(badSound()).setHardness(20F);
            else ((BlockCrate) b.setTranslationKey("partial")).withSoundType(badSound()).setHardness(20F);
            throw new AssertionError("Missing sound failure");
        } catch (IllegalArgumentException expected) {
            check(order.equals(Arrays.asList("sound throws")), "sound exception order");
            check(b.getTranslationKey().equals("tile.partial") && b.getSoundType() == SoundType.STONE, "preceding name remains and sound does not mutate");
            check(b.getBlockHardness(b.getDefaultState(), null, BlockPos.ORIGIN) == 0F, "sound exception prevents following hardness setter");
        }
    }
    public static void main(String[] args) {
        Bootstrap.register();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        net.minecraftforge.fml.common.ModMetadata metadata = new net.minecraftforge.fml.common.ModMetadata();
        metadata.modId = "hbm"; metadata.name = "Crate construction probe"; metadata.version = "1";
        net.minecraftforge.fml.common.DummyModContainer owner = new net.minecraftforge.fml.common.DummyModContainer(metadata);
        net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);
        net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
        Block unrelated = new Block(Material.ROCK);
        Block[] blocks = CrateConstruction.create();
        check(blocks.length == 11, "eleven exact construction slices");
        for (int i = 0; i < blocks.length; i++) {
            Block b = blocks[i]; SoundType expected = CrateConstruction.expected[i];
            check(b.getClass() == (i < 5 ? BlockCrate.class : BlockStorageCrate.class), "unchanged named owner class");
            check(b.getTranslationKey().equals("tile." + CrateConstruction.names[i]), "exact unchanged name");
            check(b.getDefaultState().getMaterial() == CrateConstruction.materials[i], "exact constructor material");
            check(b.getBlockHardness(b.getDefaultState(), null, BlockPos.ORIGIN) == CrateConstruction.hardness[i], "exact trailing hardness");
            check(b.getExplosionResistance(null) == CrateConstruction.resistance[i] * 3F / 5F, "exact trailing native resistance");
            check(b.getCreativeTab() == CrateConstruction.tabs[i], "exact trailing creative tab or null");
            check(b.getSoundType() == expected, "exact original sound argument identity");
            check(b.getSoundType(b.getDefaultState(), null, BlockPos.ORIGIN, null) == expected, "native contextual getter exact reference");
            check(b.getSoundType().getBreakSound() == expected.getBreakSound(), "break identity");
            check(b.getSoundType().getPlaceSound() == expected.getPlaceSound(), "place identity");
            check(b.getSoundType().getStepSound() == expected.getStepSound(), "step identity");
            check(b.getSoundType().getHitSound() == expected.getHitSound(), "hit identity");
            check(b.getSoundType().getFallSound() == expected.getFallSound(), "fall identity");
            check(b.getSoundType().getVolume() == expected.getVolume() && b.getSoundType().getPitch() == expected.getPitch(), "volume and pitch retained");
            SoundType custom = new SoundType(0.37F, 1.23F, expected.getBreakSound(), expected.getStepSound(), expected.getPlaceSound(), expected.getHitSound(), expected.getFallSound());
            for (SoundType value : new SoundType[]{expected, SoundType.GLASS, SoundType.SAND, SoundType.WOOD, ModSoundTypes.legacyMetal, custom, null}) {
                check(withSound(b, value) == b, "builder returns identical owner");
                check(b.getSoundType() == value, "native setter stores exact reference including null");
                check(b.getSoundType(b.getDefaultState(), null, BlockPos.ORIGIN, null) == value, "native contextual getter exact reference including null");
            }
            withSound(b, expected);
            check(b.setTranslationKey("identity") == b, "native name setter returns exact receiver, making cast legal");
            check(b.setHardness(3F) == b && b.setResistance(4F) == b && b.setCreativeTab(null) == b, "all following native setters retain receiver");
            check(unrelated.getSoundType() == SoundType.STONE, "no unrelated block or global sound mutation");
        }
        testOrder(false); testOrder(true);
        check(ModSoundTypes.legacyMetal != SoundType.METAL, "no modern metal substitution");
        check(ModSoundTypes.legacyMetal.getBreakSound() == SoundType.STONE.getBreakSound(), "legacy metal stone break");
        check(ModSoundTypes.legacyMetal.getPlaceSound() == SoundType.STONE.getPlaceSound(), "legacy metal stone place");
        check(ModSoundTypes.legacyMetal.getStepSound() == SoundType.STONE.getStepSound(), "legacy metal stone step");
        check(ModSoundTypes.legacyMetal.getHitSound() == SoundType.STONE.getStepSound() && ModSoundTypes.legacyMetal.getFallSound() == SoundType.STONE.getStepSound(), "legacy metal hit/fall unchanged");
        check(ModSoundTypes.legacyMetal.getVolume() == 1F && ModSoundTypes.legacyMetal.getPitch() == 1.5F, "legacy metal original volume and pitch");
        System.out.println("PASS " + checks + " finite native assertions: eleven exact crate construction slices; actual native setter/getters; receiver/null/custom identity; chain and exception order; untouched unrelated block.");
    }
}

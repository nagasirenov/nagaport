import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import com.hbm.blocks.generic.*;

/** Construction-boundary test only; not acceptance of complete terrain owners/gameplay. */
public final class WasteTerrainSoundNativeTest {
    private static int checks;
    private static final List<String> order = new ArrayList<>();
    private static Block current;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static Block withSound(Block block, SoundType sound) {
        if (block instanceof WasteEarth) return ((WasteEarth) block).withSoundType(sound);
        if (block instanceof WasteLog) return ((WasteLog) block).withSoundType(sound);
        return ((WasteLeaves) block).withSoundType(sound);
    }
    private static Material material() { order.add("material"); return Material.GROUND; }
    private static boolean tick() { order.add("tick"); return true; }
    private static Block capture(Block b) {
        current = b; order.add("receiver");
        check(b.getSoundType() == SoundType.STONE, "genuine native Block constructor sound precedes registry setters");
        return b;
    }
    private static String name() { order.add("name"); check(current.getSoundType() == SoundType.STONE, "name argument precedes sound mutation"); return "order"; }
    private static SoundType sound() { order.add("sound"); check(current.getTranslationKey().equals("tile.order"), "native name setter precedes sound argument"); check(current.getSoundType() == SoundType.STONE, "sound argument precedes mutation"); return SoundType.GLASS; }
    private static float hardness() { order.add("hardness"); check(current.getSoundType() == SoundType.GLASS, "hardness follows sound mutation"); return 5F; }
    private static float resistance() { order.add("resistance"); check(current.getBlockHardness(current.getDefaultState(), null, BlockPos.ORIGIN) == 5F, "hardness precedes resistance"); return 10F; }
    private static float light() { order.add("light"); check(current.getSoundType() == SoundType.GLASS, "light follows sound mutation"); return 1F; }
    private static CreativeTabs tab() { order.add("tab"); check(current.getSoundType() == SoundType.GLASS, "tab follows sound mutation"); return CreativeTabs.REDSTONE; }
    private static SoundType argument() { order.add("argument"); return null; }
    private static Block badReceiver() { order.add("receiver throws"); throw new IllegalArgumentException(); }
    private static String badName() { order.add("name throws"); throw new IllegalArgumentException(); }
    private static Material badMaterial() { order.add("material throws"); throw new IllegalArgumentException(); }
    private static boolean badTick() { order.add("tick throws"); throw new IllegalArgumentException(); }
    private static SoundType badSound() { order.add("sound throws"); throw new IllegalArgumentException(); }
    private static void testOrder(int shape) {
        order.clear(); Block result;
        if (shape == 0) {
            result = ((WasteEarth) capture(new WasteEarth(material(), tick())).setTranslationKey(name())).withSoundType(sound()).setCreativeTab(tab()).setHardness(hardness()).setResistance(resistance());
            check(order.equals(Arrays.asList("material", "tick", "receiver", "name", "sound", "tab", "hardness", "resistance")), "earth exact evaluation order");
        } else if (shape == 1) {
            result = ((WasteLog) capture(new WasteLog(material())).setTranslationKey(name())).withSoundType(sound()).setCreativeTab(tab()).setHardness(hardness()).setResistance(resistance());
            check(order.equals(Arrays.asList("material", "receiver", "name", "sound", "tab", "hardness", "resistance")), "log exact evaluation order");
        } else if (shape == 2) {
            result = ((WasteLeaves) capture(new WasteLeaves(material())).setTranslationKey(name())).withSoundType(sound()).setCreativeTab(tab()).setHardness(hardness());
            check(order.equals(Arrays.asList("material", "receiver", "name", "sound", "tab", "hardness")), "leaves exact evaluation order");
        } else {
            result = ((WasteEarth) capture(new WasteEarth(material(), tick())).setTranslationKey(name())).withSoundType(sound()).setLightLevel(light()).setCreativeTab(tab()).setHardness(hardness());
            check(order.equals(Arrays.asList("material", "tick", "receiver", "name", "sound", "light", "tab", "hardness")), "mycelium light branch exact evaluation order");
            check(result.getLightValue(result.getDefaultState()) == 15, "light persists through later native setters");
        }
        check(result == current && result.getSoundType() == SoundType.GLASS, "same native receiver through cast and sound assignment");
        check(result.getCreativeTab() == CreativeTabs.REDSTONE, "same receiver retains tab");
        check(result.getBlockHardness(result.getDefaultState(), null, BlockPos.ORIGIN) == 5F, "same receiver retains hardness");
        check(result.getTickRandomly() == (shape != 1), "constructor ticking survives all chains");
    }
    private static void testFailures(int owner) {
        order.clear();
        try {
            if (owner == 0) ((WasteEarth) (Block) null).withSoundType(argument());
            else if (owner == 1) ((WasteLog) (Block) null).withSoundType(argument());
            else ((WasteLeaves) (Block) null).withSoundType(argument());
            throw new AssertionError("No null receiver exception");
        } catch (NullPointerException expected) { check(order.equals(Arrays.asList("argument")), "null receiver still evaluates sound argument"); }
        order.clear();
        try {
            if (owner == 0) ((WasteEarth) badReceiver()).withSoundType(argument());
            else if (owner == 1) ((WasteLog) badReceiver()).withSoundType(argument());
            else ((WasteLeaves) badReceiver()).withSoundType(argument());
            throw new AssertionError("No receiver exception");
        } catch (IllegalArgumentException expected) { check(order.equals(Arrays.asList("receiver throws")), "receiver failure prevents sound argument evaluation"); }
        order.clear();
        try {
            if (owner == 0) ((WasteEarth) new WasteEarth(badMaterial(), tick()).setTranslationKey(name())).withSoundType(argument());
            else if (owner == 1) ((WasteLog) new WasteLog(badMaterial()).setTranslationKey(name())).withSoundType(argument());
            else ((WasteLeaves) new WasteLeaves(badMaterial()).setTranslationKey(name())).withSoundType(argument());
            throw new AssertionError("No constructor argument exception");
        } catch (IllegalArgumentException expected) { check(order.equals(Arrays.asList("material throws")), "first constructor argument failure stops later arguments and setters"); }
        Block b = owner == 0 ? new WasteEarth(Material.GROUND, false) : owner == 1 ? new WasteLog(Material.WOOD) : new WasteLeaves(Material.LEAVES);
        float originalHardness = b.getBlockHardness(b.getDefaultState(), null, BlockPos.ORIGIN);
        order.clear();
        try {
            if (owner == 0) ((WasteEarth) b.setTranslationKey(badName())).withSoundType(argument());
            else if (owner == 1) ((WasteLog) b.setTranslationKey(badName())).withSoundType(argument());
            else ((WasteLeaves) b.setTranslationKey(badName())).withSoundType(argument());
            throw new AssertionError("No name exception");
        } catch (IllegalArgumentException expected) { check(order.equals(Arrays.asList("name throws")) && b.getSoundType() == SoundType.STONE, "name failure prevents sound evaluation/mutation"); }
        order.clear();
        try {
            if (owner == 0) ((WasteEarth) b.setTranslationKey("partial")).withSoundType(badSound()).setHardness(20F);
            else if (owner == 1) ((WasteLog) b.setTranslationKey("partial")).withSoundType(badSound()).setHardness(20F);
            else ((WasteLeaves) b.setTranslationKey("partial")).withSoundType(badSound()).setHardness(20F);
            throw new AssertionError("No sound exception");
        } catch (IllegalArgumentException expected) {
            check(order.equals(Arrays.asList("sound throws")), "sound exception order");
            check(b.getTranslationKey().equals("tile.partial") && b.getSoundType() == SoundType.STONE, "prior name survives sound failure, sound unchanged");
            check(b.getBlockHardness(b.getDefaultState(), null, BlockPos.ORIGIN) == originalHardness, "sound failure prevents subsequent setter");
        }
    }
    public static void main(String[] args) {
        Bootstrap.register();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        net.minecraftforge.fml.common.ModMetadata metadata = new net.minecraftforge.fml.common.ModMetadata();
        metadata.modId = "hbm"; metadata.name = "Waste terrain sound boundary probe"; metadata.version = "1";
        net.minecraftforge.fml.common.DummyModContainer owner = new net.minecraftforge.fml.common.DummyModContainer(metadata);
        net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);
        net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
        Block unrelated = new Block(Material.ROCK); Block[] blocks = SoundConstruction.create();
        check(blocks.length == 7, "seven exact current registry construction slices");
        for (int i = 0; i < blocks.length; i++) {
            Block b = blocks[i]; SoundType expected = SoundConstruction.expected[i]; IBlockState state = b.getDefaultState();
            check(b.getClass() == SoundConstruction.owners[i], "unchanged concrete NTM owner identity");
            check(b.getTranslationKey().equals("tile." + SoundConstruction.names[i]), "exact name");
            check(state.getMaterial() == SoundConstruction.materials[i], "native constructor material");
            check(b.getTickRandomly() == SoundConstruction.ticks[i], "exact constructor random-tick flag, including frozen false");
            check(state.isOpaqueCube() == !(b instanceof WasteLeaves), "native construction retains actual owner opaque override");
            check(state.isFullCube() == !(b instanceof WasteLeaves), "native construction retains actual owner full-cube override");
            check(b.getLightOpacity(state) == (b instanceof WasteLeaves ? 0 : 255), "native constructor dispatch keeps cached light opacity");
            check(b.getLightValue(state) == SoundConstruction.light[i], "exact chain light level");
            check(b.getBlockHardness(state, null, BlockPos.ORIGIN) == SoundConstruction.hardness[i], "exact hardness");
            check(b.getExplosionResistance(null) == SoundConstruction.resistance[i], "exact native resistance including hardness default");
            check(b.getCreativeTab() == SoundConstruction.tabs[i], "exact creative tab");
            check(b.getSoundType() == expected, "exact original sound object");
            check(b.getSoundType(state, null, BlockPos.ORIGIN, null) == expected, "native contextual sound getter");
            check(b.getSoundType().getBreakSound() == expected.getBreakSound(), "break event identity");
            check(b.getSoundType().getPlaceSound() == expected.getPlaceSound(), "place event identity");
            check(b.getSoundType().getStepSound() == expected.getStepSound(), "step event identity");
            check(b.getSoundType().getHitSound() == expected.getHitSound(), "hit event identity");
            check(b.getSoundType().getFallSound() == expected.getFallSound(), "fall event identity");
            check(b.getSoundType().getVolume() == expected.getVolume() && b.getSoundType().getPitch() == expected.getPitch(), "volume/pitch");
            SoundType custom = new SoundType(.37F, 1.23F, expected.getBreakSound(), expected.getStepSound(), expected.getPlaceSound(), expected.getHitSound(), expected.getFallSound());
            for (SoundType value : new SoundType[]{SoundType.PLANT, SoundType.GLASS, SoundType.WOOD, SoundType.SAND, custom, null}) {
                check(withSound(b, value) == b, "builder returns identical concrete receiver");
                check(b.getSoundType() == value, "native setter stores exact custom/null argument");
                check(b.getSoundType(state, null, BlockPos.ORIGIN, null) == value, "contextual getter preserves exact custom/null argument");
                check(b.getDefaultState() == state && b.getTickRandomly() == SoundConstruction.ticks[i], "sound assignment leaves block state and constructor ticking untouched");
            }
            withSound(b, expected);
            check(b.setTranslationKey("identity") == b && b.setCreativeTab(null) == b && b.setHardness(3F) == b && b.setResistance(4F) == b && b.setLightLevel(.2F) == b, "native fluent setters retain receiver");
            check(unrelated.getSoundType() == SoundType.STONE, "no unrelated/global block mutation");
        }
        for (int shape = 0; shape < 4; shape++) testOrder(shape);
        for (int i = 0; i < 3; i++) testFailures(i);
        order.clear();
        try { ((WasteEarth) new WasteEarth(material(), badTick()).setTranslationKey(name())).withSoundType(argument()); throw new AssertionError("No tick exception"); }
        catch (IllegalArgumentException expected) { check(order.equals(Arrays.asList("material", "tick throws")), "second constructor argument failure preserves earlier evaluation and stops chain"); }
        System.out.println("PASS " + checks + " finite native assertions: seven exact registry construction slices (three complete chains, four ending before unported texture calls); exact owners/receivers/materials/ticking/sounds/properties; native construction defaults and four chain/failure-order variants.");
    }
}

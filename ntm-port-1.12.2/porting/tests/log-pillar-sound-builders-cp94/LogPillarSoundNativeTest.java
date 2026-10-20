import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import com.hbm.blocks.generic.*;

/** Finite native construction-boundary test, not complete log/pillar gameplay acceptance. */
public final class LogPillarSoundNativeTest {
    private static int checks;
    private static final List<String> order = new ArrayList<>();
    private static Block current;
    private static int activeShape;
    private static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    private static Block withSound(Block block, SoundType sound) {
        if (block instanceof BlockLogNT) return ((BlockLogNT) block).withSoundType(sound);
        return ((BlockRotatablePillar) block).withSoundType(sound);
    }
    private static SoundType initial(Block b) { return b instanceof BlockLogNT ? SoundType.WOOD : SoundType.STONE; }
    private static String side() { order.add("side"); return "side"; }
    private static String top() { order.add("top"); return "top"; }
    private static Material material() { order.add("material"); return Material.CLOTH; }
    private static Block capture(Block b) {
        current = b; order.add("receiver");
        check(b.getSoundType() == initial(b), "genuine native constructor sound precedes registry setters");
        return b;
    }
    private static String name() { order.add("name"); check(current.getSoundType() == initial(current), "name argument precedes sound mutation"); return "order"; }
    private static SoundType sound() { order.add("sound"); check(current.getTranslationKey().equals("tile.order"), "name setter precedes sound argument"); check(current.getSoundType() == initial(current), "sound argument precedes mutation"); return SoundType.GLASS; }
    private static float hardness() { order.add("hardness"); check(current.getSoundType() == (activeShape == 1 ? initial(current) : SoundType.GLASS), "hardness argument observes exactly preceding sound state"); return 5F; }
    private static float resistance() { order.add("resistance"); check(current.getBlockHardness(current.getDefaultState(), null, BlockPos.ORIGIN) == 5F, "hardness before resistance"); return 10F; }
    private static CreativeTabs tab() { order.add("tab"); check(current.getSoundType() == (activeShape == 0 ? initial(current) : SoundType.GLASS), "tab argument observes exactly preceding sound state"); return CreativeTabs.REDSTONE; }
    private static SoundType argument() { order.add("argument"); return null; }
    private static Block badReceiver() { order.add("receiver throws"); throw new IllegalArgumentException(); }
    private static String badName() { order.add("name throws"); throw new IllegalArgumentException(); }
    private static String badTop() { order.add("top throws"); throw new IllegalArgumentException(); }
    private static SoundType badSound() { order.add("sound throws"); throw new IllegalArgumentException(); }
    private static void testOrder(int shape) {
        order.clear(); activeShape = shape; Block result;
        if (shape == 0) {
            result = ((BlockRotatablePillar) capture(new BlockRotatablePillar(material(), top())).setTranslationKey(name()).setCreativeTab(tab())).withSoundType(sound()).setHardness(hardness()).setResistance(resistance());
            check(order.equals(Arrays.asList("material", "top", "receiver", "name", "tab", "sound", "hardness", "resistance")), "pillar exact evaluation order");
        } else if (shape == 1) {
            result = ((BlockLogNT) capture(new BlockLogNT(side(), top())).setTranslationKey(name()).setHardness(hardness())).withSoundType(sound()).setCreativeTab(tab());
            check(order.equals(Arrays.asList("side", "top", "receiver", "name", "hardness", "sound", "tab")), "pink log exact evaluation order");
        } else {
            result = ((BlockLogNT) capture(new BlockLogNT(side(), top())).setTranslationKey(name())).withSoundType(sound()).setCreativeTab(tab()).setHardness(hardness()).setResistance(resistance());
            check(order.equals(Arrays.asList("side", "top", "receiver", "name", "sound", "tab", "hardness", "resistance")), "other logs exact evaluation order");
        }
        check(result == current && result.getSoundType() == SoundType.GLASS, "same native receiver through cast and sound assignment");
        check(result.getCreativeTab() == CreativeTabs.REDSTONE, "same receiver retains tab");
        check(result.getBlockHardness(result.getDefaultState(), null, BlockPos.ORIGIN) == 5F, "same receiver retains hardness");
    }
    private static void testFailures(boolean log) {
        order.clear();
        try { if (log) ((BlockLogNT) (Block) null).withSoundType(argument()); else ((BlockRotatablePillar) (Block) null).withSoundType(argument()); throw new AssertionError("No null receiver exception"); }
        catch (NullPointerException expected) { check(order.equals(Arrays.asList("argument")), "null receiver still evaluates argument"); }
        order.clear();
        try { if (log) ((BlockLogNT) badReceiver()).withSoundType(argument()); else ((BlockRotatablePillar) badReceiver()).withSoundType(argument()); throw new AssertionError("No receiver exception"); }
        catch (IllegalArgumentException expected) { check(order.equals(Arrays.asList("receiver throws")), "receiver failure prevents sound evaluation"); }
        order.clear();
        try { if (log) ((BlockLogNT) new BlockLogNT(side(), badTop()).setTranslationKey(name())).withSoundType(argument()); else ((BlockRotatablePillar) new BlockRotatablePillar(material(), badTop()).setTranslationKey(name())).withSoundType(argument()); throw new AssertionError("No constructor argument exception"); }
        catch (IllegalArgumentException expected) { check(order.equals(Arrays.asList(log ? "side" : "material", "top throws")), "constructor argument failure stops subsequent chain"); }
        Block b = log ? new BlockLogNT("s", "t") : new BlockRotatablePillar(Material.CLOTH, "t");
        float originalHardness = b.getBlockHardness(b.getDefaultState(), null, BlockPos.ORIGIN);
        order.clear();
        try { if (log) ((BlockLogNT) b.setTranslationKey(badName())).withSoundType(argument()); else ((BlockRotatablePillar) b.setTranslationKey(badName())).withSoundType(argument()); throw new AssertionError("No name exception"); }
        catch (IllegalArgumentException expected) { check(order.equals(Arrays.asList("name throws")) && b.getSoundType() == initial(b), "name failure prevents sound evaluation and mutation"); }
        order.clear();
        try { if (log) ((BlockLogNT) b.setTranslationKey("partial")).withSoundType(badSound()).setHardness(20F); else ((BlockRotatablePillar) b.setTranslationKey("partial")).withSoundType(badSound()).setHardness(20F); throw new AssertionError("No sound exception"); }
        catch (IllegalArgumentException expected) {
            check(order.equals(Arrays.asList("sound throws")), "sound exception order");
            check(b.getTranslationKey().equals("tile.partial") && b.getSoundType() == initial(b), "prior name survives, sound unchanged");
            check(b.getBlockHardness(b.getDefaultState(), null, BlockPos.ORIGIN) == originalHardness, "sound failure prevents subsequent setter");
        }
    }
    public static void main(String[] args) {
        Bootstrap.register();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        net.minecraftforge.fml.common.ModMetadata metadata = new net.minecraftforge.fml.common.ModMetadata();
        metadata.modId = "hbm"; metadata.name = "Log pillar sound boundary probe"; metadata.version = "1";
        net.minecraftforge.fml.common.DummyModContainer owner = new net.minecraftforge.fml.common.DummyModContainer(metadata);
        net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);
        net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
        Block unrelated = new Block(Material.ROCK); Block[] blocks = SoundConstruction.create();
        check(blocks.length == 10, "ten exact registry construction slices");
        for (int i = 0; i < blocks.length; i++) {
            Block b = blocks[i]; SoundType expected = SoundConstruction.expected[i];
            check(b.getClass() == SoundConstruction.owners[i], "unchanged concrete NTM owner identity");
            check(b.getTranslationKey().equals("tile." + SoundConstruction.names[i]), "exact name");
            check(b.getDefaultState().getMaterial() == SoundConstruction.materials[i], "native constructor material");
            check(b.getBlockHardness(b.getDefaultState(), null, BlockPos.ORIGIN) == SoundConstruction.hardness[i], "exact hardness");
            check(b.getExplosionResistance(null) == SoundConstruction.resistance[i], "exact native resistance including retained pink-log constructor resistance");
            check(b.getCreativeTab() == SoundConstruction.tabs[i], "exact creative tab/null");
            check(b.getSoundType() == expected, "exact original sound object identity");
            check(b.getSoundType(b.getDefaultState(), null, BlockPos.ORIGIN, null) == expected, "native contextual sound getter");
            check(b.getSoundType().getBreakSound() == expected.getBreakSound(), "break event identity");
            check(b.getSoundType().getPlaceSound() == expected.getPlaceSound(), "place event identity");
            check(b.getSoundType().getStepSound() == expected.getStepSound(), "step event identity");
            check(b.getSoundType().getHitSound() == expected.getHitSound(), "hit event identity");
            check(b.getSoundType().getFallSound() == expected.getFallSound(), "fall event identity");
            check(b.getSoundType().getVolume() == expected.getVolume() && b.getSoundType().getPitch() == expected.getPitch(), "volume/pitch retained");
            SoundType custom = new SoundType(.37F, 1.23F, expected.getBreakSound(), expected.getStepSound(), expected.getPlaceSound(), expected.getHitSound(), expected.getFallSound());
            for (SoundType value : new SoundType[]{SoundType.CLOTH, SoundType.GLASS, SoundType.WOOD, SoundType.SAND, custom, null}) {
                check(withSound(b, value) == b, "builder returns same concrete receiver");
                check(b.getSoundType() == value, "native setter stores exact argument including custom/null");
                check(b.getSoundType(b.getDefaultState(), null, BlockPos.ORIGIN, null) == value, "contextual getter preserves custom/null");
            }
            withSound(b, expected);
            check(b.setTranslationKey("identity") == b && b.setCreativeTab(null) == b && b.setHardness(3F) == b && b.setResistance(4F) == b, "native fluent setters retain receiver so closed casts are legal");
            check(unrelated.getSoundType() == SoundType.STONE, "no unrelated/global block mutation");
        }
        for (int shape = 0; shape < 3; shape++) testOrder(shape);
        testFailures(false); testFailures(true);
        System.out.println("PASS " + checks + " finite native assertions: 7 full log construction chains and 3 pillar chains ending before still-unported texture calls; exact objects/receiver/material/properties; native constructor defaults; all three chain shapes and failure order.");
    }
}

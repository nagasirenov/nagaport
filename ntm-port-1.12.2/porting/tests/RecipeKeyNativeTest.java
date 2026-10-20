import com.hbm.config.GeneralConfig;
import com.hbm.inventory.RecipesCommon;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import com.hbm.inventory.RecipesCommon.*;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

/** Entire production recipe-key root on real native APIs; controlled NTM collaborators only. */
public final class RecipeKeyNativeTest {
    private static int checks;
    private enum Meta { FIRST, SECOND, THIRD }
    private static final class DisplayKey extends ComparableStack {
        int calls;
        DisplayKey() { super(Items.STICK,4,0); }
        @Override public ItemStack toStack() { calls++; return new ItemStack(Items.DYE,stacksize,3); }
    }
    private static void check(boolean condition, String message) {
        checks++; if(!condition) throw new AssertionError(message);
    }
    private static void fails(Class<? extends Throwable> expected, Runnable action, String label) {
        try { action.run(); } catch(Throwable thrown) { check(expected.isInstance(thrown), label+": "+thrown); return; }
        throw new AssertionError(label+": no exception");
    }
    private static int itemHash(Item item, int size, int meta) {
        int hash=1; hash=31*hash+Item.REGISTRY.getNameForObject(item).toString().hashCode(); hash=31*hash+meta; return 31*hash+size;
    }
    private static int blockHash(Block block, int meta) {
        return 31*(31+Block.REGISTRY.getNameForObject(block).toString().hashCode())+meta;
    }
    private static NBTTagCompound tag(int n) {
        NBTTagCompound nbt=new NBTTagCompound(); nbt.setInteger("number",n);
        NBTTagCompound nested=new NBTTagCompound(); nested.setString("kind","original"); nbt.setTag("nested",nested); return nbt;
    }
    private static ItemStack tagged(int size,int meta,int n) {
        ItemStack stack=new ItemStack(Items.DYE,size,meta); stack.setTagCompound(tag(n)); return stack;
    }
    private static void arrays() {
        check(RecipesCommon.copyStackArray(null)==null,"null copy array");
        check(RecipesCommon.objectToStackArray(null)==null,"null object array");
        ItemStack a=tagged(7,3,9); ItemStack[] original={a,null,new ItemStack(Items.STICK)};
        ItemStack[] copy=RecipesCommon.copyStackArray(original);
        check(copy!=original && copy.length==3 && copy[0]!=a && copy[1]==null && copy[2]!=original[2],"nullable copy array shape");
        check(copy[0].getTagCompound()!=a.getTagCompound() && copy[0].getTagCompound().equals(a.getTagCompound()),"deep stack NBT copy");
        copy[0].getTagCompound().getCompoundTag("nested").setString("kind","changed");
        check(a.getTagCompound().getCompoundTag("nested").getString("kind").equals("original"),"nested copy independent");
        ItemStack[] aliases=RecipesCommon.objectToStackArray(new Object[]{a,null,"not a stack",new ComparableStack(a)});
        check(aliases.length==4 && aliases[0]==a && aliases[1]==null && aliases[2]==null && aliases[3]==null,"object conversion keeps alias, filters nonstack");
        aliases[0].setCount(11); check(a.getCount()==11,"object conversion mutation visible");
        check(RecipesCommon.copyStackArray(new ItemStack[0]).length==0,"zero length array");
        ItemStack[] nativeEmpty=RecipesCommon.copyStackArray(new ItemStack[]{ItemStack.EMPTY});
        check(nativeEmpty[0]!=null && nativeEmpty[0].isEmpty(),"EMPTY remains nonnull native empty, not nullable hole");
        check(RecipesCommon.objectToStackArray(new Object[]{ItemStack.EMPTY})[0]==ItemStack.EMPTY,"object conversion EMPTY same alias");
    }
    private static void comparables() {
        ComparableStack nullStack=new ComparableStack((ItemStack)null);
        check(nullStack.item==ModItems.nothing && nullStack.stacksize==1 && nullStack.meta==0,"null ItemStack fallback");
        check(new ComparableStack((Item)null).item==ModItems.nothing,"null item fallback");
        ComparableStack unnamed=new ComparableStack(new Item(),3,8);
        check(unnamed.hashCode()==itemHash(ModItems.nothing,3,8) && unnamed.item==ModItems.nothing,"unnamed unregistered item fallback mutates key");
        check(new ComparableStack(Blocks.STONE).item==Item.getItemFromBlock(Blocks.STONE),"block identity constructor");
        check(new ComparableStack(Blocks.STONE,4,Meta.THIRD).meta==2,"block enum ordinal");
        check(new ComparableStack(Items.DYE,4,Meta.SECOND).meta==1,"item enum ordinal");
        ItemStack tagged=tagged(7,3,99); ComparableStack key=new ComparableStack(tagged);
        check(key.item==Items.DYE && key.stacksize==7 && key.meta==3,"stack key fields");
        check(key.toStack()!=tagged && !key.toStack().hasTagCompound(),"plain key intentionally discards NBT");
        check(key.makeSingular()==key && key.stacksize==1,"makeSingular mutates same object");
        for(Item item:new Item[]{Items.DYE,Items.COAL,Items.STICK,Items.BLAZE_ROD,Items.IRON_INGOT}) {
            for(int size:new int[]{Integer.MIN_VALUE,-5,0,1,7,64,130,Integer.MAX_VALUE}) {
                for(int meta:new int[]{0,1,3,32766,OreDictionary.WILDCARD_VALUE}) {
                    ComparableStack a=new ComparableStack(item,size,meta), b=a.copy(), c=a.copy(19);
                    check(a!=b && a.equals(b) && b.equals(a),"same exact class identity/metadata/count equals");
                    check(a.hashCode()==itemHash(item,size,meta),"registry-name String hash and signed integer overflow");
                    check(c.item==item && c.meta==meta && c.stacksize==19 && a.stacksize==size,"copy(size) retains identity/meta and changes only copy count");
                    check(a.compareTo(c)==0,"ordering ignores stacksize");
                }
            }
        }
        ResourceLocation name=Item.REGISTRY.getNameForObject(Items.DYE);
        check(name.hashCode()!=name.toString().hashCode(),"fixture detects ResourceLocation/String hash mismatch");
        ComparableStack specific=new ComparableStack(Items.DYE,1,3), wildcard=new ComparableStack(Items.DYE,1,OreDictionary.WILDCARD_VALUE);
        check(specific.equals(wildcard) && wildcard.equals(specific),"original symmetric wildcard equals");
        check(specific.hashCode()!=wildcard.hashCode(),"original wildcard equals/hash anomaly retained");
        check(!specific.equals(new ComparableStack(Items.DYE,2,3)) && !specific.equals(new ComparableStack(Items.STICK,1,3)),"size/item affect equals");
        check(!specific.equals(null) && !specific.equals(new Object()),"strict equals type/null");
        check(!specific.equals(new NBTStack(Items.DYE,1,3)) && !new NBTStack(Items.DYE,1,3).equals(specific),"NBT subclass distinct map-key class");
        ComparableStack mutable=new ComparableStack(Items.DYE,1,3); mutable.item=null;
        check(mutable.hashCode()==itemHash(ModItems.nothing,1,3) && mutable.item==ModItems.nothing,"hash mutates null item to nothing");
        Item unregistered=new Item().setRegistryName(new ResourceLocation("recipe_probe","unregistered"));
        check(unregistered.getRegistryName()!=null && Item.REGISTRY.getNameForObject(unregistered)==null,"named does not mean registered");
        mutable=new ComparableStack(unregistered,5,2);
        check(mutable.hashCode()==itemHash(ModItems.nothing,5,2) && mutable.item==ModItems.nothing,"hash mutates unregistered item to nothing");
        check(new ComparableStack(Items.DYE,7,3).compareTo(new ComparableStack(Items.DYE,1,5))<0,"metadata ordering");
        int expected=Integer.signum(Integer.compare(Item.getIdFromItem(Items.DYE),Item.getIdFromItem(Items.STICK)));
        check(Integer.signum(specific.compareTo(new ComparableStack(Items.STICK)))==expected,"native numeric ID ordering remains separate from stable hash");
        List<ItemStack> extract=specific.extractForNEI(); check(extract.size()==1 && extract.get(0).getItem()==Items.DYE,"plain extraction single stack");
        fails(UnsupportedOperationException.class,()->extract.add(new ItemStack(Items.STICK)),"plain extraction fixed-size list");
        check(specific.extractForCyclingDisplay(1).getItem()==Items.DYE,"one entry cycling");
        fails(ArithmeticException.class,()->specific.extractForCyclingDisplay(0),"nonempty zero cycle still fails");
        check(specific.toString().equals("1x"+Items.DYE.getTranslationKey()+"@3"),"plain key string");
    }
    private static void fallbackDiagnostics() throws Exception {
        final List<String> messages=new ArrayList<>();
        org.apache.logging.log4j.core.appender.AbstractAppender capture=new org.apache.logging.log4j.core.appender.AbstractAppender("recipe-key-capture",null,null) {
            @Override public void append(org.apache.logging.log4j.core.LogEvent event) { messages.add(event.getMessage().getFormattedMessage()); }
        };
        org.apache.logging.log4j.core.Logger logger=(org.apache.logging.log4j.core.Logger)MainRegistry.logger;
        capture.start(); logger.addAppender(capture);
        PrintStream stderr=System.err; ByteArrayOutputStream trace=new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(trace,true,"UTF-8")); GeneralConfig.enableSilentCompStackErrors=false;
            ComparableStack nullKey=new ComparableStack(Items.STICK); nullKey.item=null; nullKey.hashCode();
            new ComparableStack(new Item()).hashCode();
            check(messages.size()==2 && messages.get(0).contains("null item") && messages.get(1).contains("does not seem to be registered"),"original distinct fallback error logging retained");
            check(trace.toString("UTF-8").contains("dumpStack"),"original fallback stack dump retained");
            int before=trace.size(); GeneralConfig.enableSilentCompStackErrors=true;
            nullKey.item=null; nullKey.hashCode(); new ComparableStack(new Item()).hashCode();
            check(messages.size()==2 && trace.size()==before,"silent config suppresses both logging and stack dump");
        } finally { GeneralConfig.enableSilentCompStackErrors=true; System.setErr(stderr); logger.removeAppender(capture); capture.stop(); }
    }
    private static void matchingAndCache() {
        ComparableStack need=new ComparableStack(Items.DYE,4,3);
        check(!need.matchesRecipe(null,false),"nullable no-match");
        for(int available:new int[]{1,3,4,5,64}) for(int meta:new int[]{2,3,4}) for(boolean ignore:new boolean[]{false,true}) {
            ItemStack stack=tagged(available,meta,7);
            check(need.matchesRecipe(stack,ignore)==(meta==3 && (ignore || available>=4)),"plain matching identity/meta/size without NBT");
        }
        check(!need.matchesRecipe(new ItemStack(Items.STICK,64,3),true),"wrong item fails");
        check(new ComparableStack(Items.DYE,4,OreDictionary.WILDCARD_VALUE).matchesRecipe(new ItemStack(Items.DYE,4,5),false),"wildcard recipe accepts actual meta");
        check(!need.matchesRecipe(new ItemStack(Items.DYE,4,OreDictionary.WILDCARD_VALUE),true),"actual wildcard is not recipe wildcard");
        Map<ComparableStack,Integer> cache=new HashMap<>();
        ItemStack original=tagged(8,3,1); cache.put(new ComparableStack(original).makeSingular(),200);
        original.setCount(1); original.getTagCompound().setInteger("number",900);
        check(cache.get(new ComparableStack(original).makeSingular())==200,"singular cache ignores supplied count/NBT");
        check(cache.get(new ComparableStack(Items.DYE,1,4))==null,"singular cache still distinguishes metadata");
        cache.put(new ComparableStack(Items.STICK).makeSingular(),0);
        check(cache.containsKey(new ComparableStack(Items.STICK)) && cache.get(new ComparableStack(Items.STICK))==0,"zero cache result is present, not miss");
        ComparableStack wildcard=new ComparableStack(Items.DYE,1,OreDictionary.WILDCARD_VALUE);
        cache.put(wildcard,300);
        check(cache.size()==3 && cache.get(new ComparableStack(Items.DYE,1,3))==200,"wildcard hash anomaly is not repaired by cache");
        ComparableStack mutable=new ComparableStack(Items.COAL,1,0); cache.put(mutable,1600); mutable.stacksize=2;
        check(!cache.containsKey(mutable),"external public key mutation can invalidate hash lookup, unchanged contract");
    }
    private static void nbt() {
        NBTTagCompound source=tag(12); NBTStack need=new NBTStack(Items.DYE,4,3).withNBT(source);
        check(need.nbt==source && need.withNBT(source)==need,"withNBT retains caller alias and fluent identity");
        check(need.initNBT()==need && need.nbt==source,"initNBT preserves existing object");
        check(need.setInt("added",9)==need && source.getInteger("added")==9,"setInt mutates alias");
        NBTStack from=new NBTStack(tagged(7,3,19)); ItemStack stack=tagged(7,3,19); from=new NBTStack(stack);
        check(from.nbt==stack.getTagCompound() && from.stacksize==7 && from.meta==3,"stack NBT constructor aliases source tag");
        from.setInt("number",20); check(stack.getTagCompound().getInteger("number")==20,"source tag sees key mutation");
        fails(NullPointerException.class,()->new NBTStack((ItemStack)null),"NBTStack null ctor unlike ComparableStack still throws");
        NBTStack copied=need.copy(); NBTStack resized=need.copy(2); ItemStack materialized=need.toStack();
        check(copied!=need && copied.nbt!=source && copied.nbt.equals(source) && resized.stacksize==2,"NBT key copies clone NBT");
        check(materialized.getTagCompound()!=source && materialized.getTagCompound().equals(source),"toStack copies NBT");
        copied.nbt.getCompoundTag("nested").setString("kind","changed");
        check(source.getCompoundTag("nested").getString("kind").equals("original"),"deep NBT key copy");
        check(need.matchesRecipe(materialized,false),"matching materialized NBT");
        materialized.getTagCompound().setString("unrelated","allowed"); check(need.matchesRecipe(materialized,false),"extra top-level tags ignored");
        materialized.getTagCompound().getCompoundTag("nested").setString("extra","not ignored");
        check(!need.matchesRecipe(materialized,false),"nested tag comparison exact, not recursive subset");
        materialized=need.toStack(); materialized.getTagCompound().removeTag("added"); check(!need.matchesRecipe(materialized,true),"missing required key");
        materialized=need.toStack(); materialized.getTagCompound().setLong("number",12); check(!need.matchesRecipe(materialized,true),"NBT numeric type matters");
        check(!need.matchesRecipe(new ItemStack(Items.DYE,9,3),true),"required tags absent");
        check(!need.matchesRecipe(null,true),"null NBT candidate");
        check(!need.matchesRecipe(new ItemStack(Items.STICK,9,3),true),"NBT wrong item");
        ItemStack small=need.toStack(); small.setCount(1); check(!need.matchesRecipe(small,false) && need.matchesRecipe(small,true),"NBT size mode");
        NBTStack untagged=new NBTStack(Items.DYE,1,3); NBTStack empty=new NBTStack(Items.DYE,1,3).initNBT();
        check(untagged.matchesRecipe(stack,true) && empty.matchesRecipe(new ItemStack(Items.DYE,1,3),false),"null and empty constraints match no-tag candidate");
        check(untagged.copy().nbt==null && untagged.copy(5).nbt==null && !untagged.toStack().hasTagCompound(),"null constraints stay nullable");
        NBTStack a=new NBTStack(Items.DYE,1,3).setInt("value",1), b=new NBTStack(Items.DYE,1,3).setInt("value",2);
        check(a.equals(b) && a.hashCode()==b.hashCode(),"original NBT key equality/hash ignore NBT values");
        Map<ComparableStack,String> cache=new HashMap<>(); cache.put(a,"first"); cache.put(b,"second");
        check(cache.size()==1 && cache.get(a).equals("second"),"original differing NBT keys replace map value");
        check(a.compareTo(b)==0 && a.compareTo(untagged)>0 && untagged.compareTo(a)<0,"NBT ordering compares only presence, not values");
        ComparableStack plain=new ComparableStack(Items.DYE); OreDictStack ore=new OreDictStack("any");
        check(a.compareTo(plain)>0 && plain.compareTo(a)<0 && a.compareTo(ore)>0 && ore.compareTo(a)<0,"NBT higher than plain higher than ore");
        check(plain.compareTo(ore)>0 && ore.compareTo(plain)<0,"plain vs ore precedence");
        check(a.makeSingular()==a,"inherited singular keeps NBT instance");
        check(a.extractForNEI().get(0).getTagCompound().equals(a.nbt),"virtual NBT extraction retains tags");
    }
    private static void ores() {
        String exact="recipeKeyProbeExact", wildcard="recipeKeyProbeWildcard";
        ItemStack first=tagged(1,3,5); OreDictionary.registerOre(exact,first);
        OreDictionary.registerOre(exact,new ItemStack(Items.COAL,1,1));
        OreDictionary.registerOre(wildcard,new ItemStack(Items.DYE,1,OreDictionary.WILDCARD_VALUE));
        OreDictStack need=new OreDictStack(exact,4);
        check(need.toStacks()==OreDictionary.getOres(exact),"ore list live alias");
        check(need.copy()!=need && need.copy().name.equals(exact) && need.copy().stacksize==4 && need.copy(7).stacksize==7,"ore copies");
        check(need.hashCode()==31*(31+exact.hashCode())+4,"ore string/count hash");
        check(need.equals(new OreDictStack(exact,4)) && !need.equals(new OreDictStack(exact,3)) && !need.equals(new OreDictStack(wildcard,4)),"ore name/count equality");
        check(!need.equals(null) && !need.equals(new Object()) && !need.equals(new ComparableStack(Items.DYE,4,3)),"ore strict type equality");
        check(new OreDictStack(null,1).equals(new OreDictStack(null,1)) && new OreDictStack(null,1).hashCode()==962,"null ore name equality/hash only");
        check(need.compareTo(new OreDictStack(exact,9))==0 && need.compareTo(new OreDictStack(wildcard,4))==exact.compareTo(wildcard),"ore ordering ignores size");
        check(!need.matchesRecipe(null,true),"null ore candidate");
        for(int size:new int[]{1,3,4,10}) for(int meta:new int[]{2,3,4}) for(boolean ignore:new boolean[]{false,true}) {
            check(need.matchesRecipe(new ItemStack(Items.DYE,size,meta),ignore)==(meta==3 && (ignore || size>=4)),"real exact ore/size match");
        }
        check(new OreDictStack(wildcard,1).matchesRecipe(new ItemStack(Items.DYE,1,7),false),"real native ore wildcard registration");
        check(!need.matchesRecipe(new ItemStack(Items.DIAMOND),true),"no ore IDs false");
        List<String> keys=Arrays.asList(new ComparableStack(Items.DYE,3,3).getDictKeys());
        check(keys.contains(exact) && keys.contains(wildcard),"getDictKeys resolves native names");
        check(new ComparableStack(Item.getItemFromBlock(Blocks.BARRIER)).getDictKeys().length==0,"empty dictionary key array");
        DisplayKey display=new DisplayKey();
        check(Arrays.asList(display.getDictKeys()).contains(exact) && display.calls==1 && display.item==Items.STICK && display.stacksize==4,"positive-key dictionary lookup preserves virtual toStack without mutating domain fields");
        ComparableStack absentDisplay=new ComparableStack(Items.DYE,4,3) {
            @Override public ItemStack toStack() { return null; }
        };
        check(absentDisplay.getDictKeys().length==0 && absentDisplay.item==Items.DYE && absentDisplay.stacksize==4 && absentDisplay.meta==3,"positive virtual toStack null result preserves original dictionary no-keys contract without mutation");
        List<ItemStack> extracted=need.extractForNEI();
        check(extracted.size()==2 && extracted.get(0).getItem()==Items.DYE && extracted.get(1).getItem()==Items.COAL,"ore extraction preserves registration order");
        check(extracted.get(0).getCount()==4 && extracted.get(1).getCount()==4 && need.toStacks().get(0).getCount()==1,"ore extracted count copied, dictionary untouched");
        check(extracted.get(0).getTagCompound()!=need.toStacks().get(0).getTagCompound(),"ore extraction deep NBT copy");
        extracted.clear(); check(need.toStacks().size()==2,"extraction list independent mutable list");
        ItemStack variant=new ItemStack(Items.DYE,11,9); MainRegistry.proxy.result=Arrays.asList(variant); int before=MainRegistry.proxy.calls;
        List<ItemStack> expanded=new OreDictStack(wildcard,6).extractForNEI();
        check(MainRegistry.proxy.calls==before+1 && MainRegistry.proxy.received.getCount()==6 && MainRegistry.proxy.received.getItemDamage()==OreDictionary.WILDCARD_VALUE,"wildcard display delegates count-adjusted copy");
        check(expanded.size()==1 && expanded.get(0)==variant && variant.getCount()==11,"proxy list entry alias/count not rewritten");
        check(OreDictionary.getOres(wildcard).get(0).getCount()==1,"wildcard dictionary original not mutated");
        OreDictStack absent=new OreDictStack("recipeKeyProbeAbsent",1);
        check(absent.extractForNEI().isEmpty() && absent.extractForCyclingDisplay(0).getItem()==ModItems.nothing,"empty cycling fallback occurs before zero-cycle arithmetic");
        check(need.toString().equals("4x"+exact),"ore string representation");
    }
    private static void metaBlocks() {
        for(Block block:new Block[]{Blocks.STONE,Blocks.DIRT,Blocks.COAL_BLOCK,Blocks.FURNACE}) for(int meta:new int[]{-1,0,1,7,32767,Integer.MAX_VALUE}) {
            MetaBlock key=new MetaBlock(block,meta), copy=new MetaBlock(block,meta);
            check(key.equals(copy) && key.hashCode()==blockHash(block,meta) && key.getID()==key.hashCode(),"block identity/meta legacy String hash");
            check(!key.equals(new MetaBlock(block,meta+1)) && !key.equals(null) && !key.equals(new Object()),"block exact metadata/class/null equality");
        }
        check(new MetaBlock(Blocks.STONE).meta==0,"block default metadata");
        MetaBlock missing=new MetaBlock(null,3); check(missing.equals(new MetaBlock(null,3)) && !missing.equals(new MetaBlock(Blocks.STONE,3)),"nullable MetaBlock equality");
        fails(NullPointerException.class,()->missing.hashCode(),"unhashable null block unchanged");
        Block unnamed=new Block(Material.ROCK), named=new Block(Material.ROCK).setRegistryName(new ResourceLocation("minecraft","recipe_key_unregistered"));
        check(Block.REGISTRY.getNameForObject(unnamed)==null && Block.REGISTRY.getNameForObject(named)==null,"actual Forge block registry returns null for unregistered identity despite vanilla defaulted registry superclass");
        check(named.getRegistryName()!=null,"block assigned name alone is not membership");
        MetaBlock unnamedKey=new MetaBlock(unnamed,2), namedKey=new MetaBlock(named,2);
        fails(NullPointerException.class,()->unnamedKey.hashCode(),"unnamed block has no original hash fallback");
        fails(NullPointerException.class,()->namedKey.hashCode(),"named unregistered block has no original hash fallback");
        check(unnamedKey.block==unnamed && namedKey.block==named,"failed block hash does not mutate domain key");
    }
    private static void nativeEmptyBoundary() {
        check(ItemStack.EMPTY.getItem()==Items.AIR && ItemStack.EMPTY.getCount()==0,"native EMPTY accessor semantics");
        ComparableStack empty=new ComparableStack(ItemStack.EMPTY);
        check(empty.item==Items.AIR && empty.stacksize==0,"non-null EMPTY not silently converted to nullable fallback");
        check(new ComparableStack((ItemStack)null).item!=empty.item,"null sentinel distinct from native EMPTY");
        check(empty.getDictKeys().length==0 && new ComparableStack(Items.AIR,1,0).getDictKeys().length==0,"native absent key dictionary lookup returns no names without exception");
        for(int count:new int[]{0,-1,-7,Integer.MIN_VALUE}) {
            ComparableStack raw=new ComparableStack(Items.DYE,count,3);
            check(raw.stacksize==count && raw.copy().stacksize==count,"domain signed count retained independently of native stack");
            ItemStack materialized=raw.toStack();
            check(materialized.isEmpty() && materialized.getItem()==Items.AIR && materialized.getCount()==0,"native zero/negative stack masks identity/count");
            ComparableStack roundtrip=new ComparableStack(materialized);
            check(roundtrip.item==Items.AIR && roundtrip.stacksize==0,"lossy empty materialization is explicit, not clamped balance repair");
            materialized.setCount(5); check(materialized.getItem()==Items.DYE && materialized.getCount()==5 && materialized.getItemDamage()==3,"noncanonical empty retains underlying native identity on resurrection");
            NBTStack nbt=new NBTStack(Items.DYE,count,3).setInt("keep",7); ItemStack nbtEmpty=nbt.toStack();
            check(nbtEmpty!=ItemStack.EMPTY && nbtEmpty.isEmpty() && nbtEmpty.getTagCompound().getInteger("keep")==7,"nonpositive NBT materialization remains independent tagged object");
        }
        check(!ItemStack.EMPTY.hasTagCompound(),"shared EMPTY not tagged by root");
        check(!new ComparableStack(Items.DYE,0,3).matchesRecipe(new ItemStack(Items.DYE,0,3),true),"empty stack cannot expose old item identity even when ignoring size");
        check(!new OreDictStack("recipeKeyProbeExact",1).matchesRecipe(ItemStack.EMPTY,false),"positive ore size rejects empty before native dictionary");
        check(!new OreDictStack("recipeKeyProbeExact",1).matchesRecipe(ItemStack.EMPTY,true),"native empty ore candidate rejected before dictionary");
        for(int count:new int[]{0,-1,-7,Integer.MIN_VALUE}) {
            ComparableStack key=new ComparableStack(Items.DYE,count,3);
            check(Arrays.asList(key.getDictKeys()).contains("recipeKeyProbeExact") && key.stacksize==count && key.item==Items.DYE && key.meta==3,"singular dictionary lookup preserves signed domain-key fields");
            NBTStack nbtKey=new NBTStack(Items.DYE,count,3).setInt("v",7);
            check(Arrays.asList(nbtKey.getDictKeys()).contains("recipeKeyProbeExact") && nbtKey.stacksize==count && nbtKey.nbt.getInteger("v")==7,"NBT dictionary lookup is count/NBT independent without mutation");
            check(!new OreDictStack("recipeKeyProbeExact",count).matchesRecipe(ItemStack.EMPTY,false),"nonpositive ore requirement still rejects absent native candidate");
            check(!new ComparableStack(Items.AIR,count,0).matchesRecipe(ItemStack.EMPTY,true),"AIR key does not turn native absence into recipe input");
            check(!new NBTStack(Items.AIR,count,0).matchesRecipe(ItemStack.EMPTY,true),"NBT AIR key rejects native absence");
        }
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register(); ModItems.nothing=Item.getItemFromBlock(Blocks.BARRIER);
        arrays(); comparables(); fallbackDiagnostics(); matchingAndCache(); nbt(); ores(); metaBlocks(); nativeEmptyBoundary();
        System.out.println("PASS RecipesCommon complete native root: "+checks+" assertions; original key anomalies and native nonpositive-stack limits explicit; no gameplay claim.");
    }
}

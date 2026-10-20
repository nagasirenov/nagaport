import com.hbm.util.ItemStackUtil;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;
import net.minecraftforge.oredict.OreDictionary;

/** Whole production utility, real native NBT/ItemStack/World/EntityItem. No game launch. */
public final class ItemStackUtilMinecraftTest {
    static int checks;
    static void check(boolean b, String label) { checks++; if(!b) throw new AssertionError(label); }
    static void fails(Class<? extends Throwable> kind, Runnable r, String label) {
        try { r.run(); } catch(Throwable e) { check(kind.isInstance(e), label+": "+e); return; }
        throw new AssertionError(label+": no exception");
    }
    static ItemStack tagged(int n) {
        ItemStack s = new ItemStack(Items.DYE,n,3);
        NBTTagCompound tag = new NBTTagCompound(); tag.setFloat("hfrHazRadiation",4.5F); tag.setFloat("ntmNeutron",0.25F);
        NBTTagCompound inner = new NBTTagCompound(); inner.setString("proof","original"); tag.setTag("nested",inner); s.setTagCompound(tag); return s;
    }
    static NBTTagCompound entry(int slot, ItemStack s) { NBTTagCompound t=s.writeToNBT(new NBTTagCompound()); t.setByte("slot",(byte)slot); return t; }
    static ItemStack holder(NBTTagCompound... records) {
        ItemStack h=new ItemStack(Items.CHEST_MINECART); NBTTagCompound n=new NBTTagCompound(); NBTTagList l=new NBTTagList();
        for(NBTTagCompound r:records) l.appendTag(r); n.setTag("items",l); h.setTagCompound(n); return h;
    }
    static final class FixtureWorld extends World {
        TileEntity tile; List<EntityItem> drops=new ArrayList<>(); List<Integer> countsAtSpawn=new ArrayList<>();
        ItemStack source; int comparator; BlockPos lookup; BlockPos updated; Block notified;
        FixtureWorld() { super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false); provider.setWorld(this); }
        @Override protected IChunkProvider createChunkProvider() { return null; }
        @Override protected boolean isChunkLoaded(int x,int z,boolean allowEmpty) { return false; }
        @Override public TileEntity getTileEntity(BlockPos p) { lookup=p; return tile; }
        @Override public boolean spawnEntity(Entity e) { check(e instanceof EntityItem,"native EntityItem"); drops.add((EntityItem)e); countsAtSpawn.add(source == null ? -1 : source.getCount()); return false; }
        @Override public void updateComparatorOutputLevel(BlockPos p, Block b) { comparator++; updated=p; notified=b; }
    }
    static void spill(long seed,int size) {
        FixtureWorld w=new FixtureWorld(); TileEntityChest chest=new TileEntityChest(); w.tile=chest;
        ItemStack source=tagged(size); w.source=source; NBTTagCompound before=source.getTagCompound().copy(); chest.setInventorySlotContents(4,source);
        // Chest clamps to 64, so set count after insertion to exercise oversized positive inputs too.
        source.setCount(size); Random actual=new Random(seed), expected=new Random(seed);
        float ox=expected.nextFloat()*0.8F+0.1F, oy=expected.nextFloat()*0.8F+0.1F, oz=expected.nextFloat()*0.8F+0.1F;
        List<Integer> counts=new ArrayList<>(), remainder=new ArrayList<>(); List<double[]> motion=new ArrayList<>(); int left=size;
        // Original's literal RNG/math order, with integer count instead of old ItemStack fields.
        while(left>0) { int n=expected.nextInt(21)+10; if(n>left)n=left; left-=n; counts.add(n); remainder.add(left);
            float m=0.05F; motion.add(new double[]{(float)expected.nextGaussian()*m,(float)expected.nextGaussian()*m+0.2F,(float)expected.nextGaussian()*m}); }
        ItemStackUtil.spillItems(w,-17,64,33,Blocks.CHEST,actual);
        check(w.drops.size()==counts.size(),"drop count seed "+seed+" size "+size); int total=0;
        for(int i=0;i<counts.size();i++) { EntityItem e=w.drops.get(i); ItemStack got=e.getItem(); total+=got.getCount();
            check(got.getItem()==Items.DYE && got.getMetadata()==3 && got.getCount()==counts.get(i),"final/native drop identity/count/meta");
            check(before.equals(got.getTagCompound()) && got.getTagCompound()!=source.getTagCompound(),"deep copied radiation NBT");
            check(e.posX==-17+ox && e.posY==64+oy && e.posZ==33+oz,"legacy float position arithmetic");
            double[] m=motion.get(i); check(e.motionX==m[0]&&e.motionY==m[1]&&e.motionZ==m[2],"seeded Gaussian motion");
            check(w.countsAtSpawn.get(i).equals(remainder.get(i)),"decrement before spawn even when spawn rejects");
        }
        check(total==size && source.isEmpty() && chest.getStackInSlot(4)==source,"drained same live alias; native chest not cleared");
        check(actual.nextLong()==expected.nextLong(),"exact supplied RNG consumption");
        check(w.comparator==1 && w.updated.equals(new BlockPos(-17,64,33)) && w.notified==Blocks.CHEST && w.lookup.equals(w.updated),"native coordinates/comparator notification");
        if(w.drops.size()>1) { w.drops.get(0).getItem().getTagCompound().getCompoundTag("nested").setString("proof","changed"); check("original".equals(w.drops.get(1).getItem().getTagCompound().getCompoundTag("nested").getString("proof")),"independent emitted NBT"); }
    }
    public static void main(String[] args) {
        Bootstrap.register();
        check(ItemStackUtil.carefulCopy(null)==null && ItemStackUtil.carefulCopyWithSize(null,7)==null,"nullable single copy");
        ItemStack s=tagged(7), copy=ItemStackUtil.carefulCopyWithSize(s,130);
        check(copy.getCount()==130 && s.getCount()==7 && copy.getTagCompound().equals(s.getTagCompound()) && copy.getTagCompound()!=s.getTagCompound(),"oversized independent copy");
        ItemStack[] a={s,null,new ItemStack(Items.STICK)}; ItemStack[] b=ItemStackUtil.carefulCopyArray(a);
        check(b!=a && b[0]!=s && b[1]==null && b[2]!=a[2],"nullable array independent copy");
        b=ItemStackUtil.carefulCopyArray(a,1,1); check(b.length==3&&b[0]==null&&b[1]==null&&b[2]==null,"inclusive partial shape");
        b=ItemStackUtil.carefulCopyArrayTruncate(a,1,2); check(b.length==2&&b[0]==null&&b[1]!=a[2],"truncate shape");
        check(ItemStackUtil.carefulCopyArray(null,0,0)==null && ItemStackUtil.carefulCopyArrayTruncate(null,0,0)==null,"explicit null range");
        fails(NullPointerException.class,()->ItemStackUtil.carefulCopyArray(null),"original convenience-null throws");
        fails(ArrayIndexOutOfBoundsException.class,()->ItemStackUtil.carefulCopyArray(a,-1,0),"invalid range throws");
        fails(NegativeArraySizeException.class,()->ItemStackUtil.carefulCopyArrayTruncate(a,2,0),"negative truncate throws");
        check(ItemStackUtil.carefulCopyArray(a,2,1).length==3,"reversed copy empty shape");
        check(ItemStackUtil.carefulCopyArrayTruncate(a,2,1).length==0,"zero-length truncate");
        NBTTagCompound display=new NBTTagCompound(); display.setString("Name","lost"); s.getTagCompound().setTag("display",display);
        check(ItemStackUtil.addTooltipToStack(s,"line")==s,"tooltip same reference");
        check(!s.getTagCompound().getCompoundTag("display").hasKey("Name") && s.getTagCompound().getFloat("hfrHazRadiation")==4.5F,"display overwrite preserves unrelated NBT");
        check(s.getTagCompound().getCompoundTag("display").getTagList("Lore",8).getStringTagAt(0).equals("\u00a7r\u00a77line"),"exact lore prefix");
        ItemStack label=new ItemStack(Items.STICK,130); ItemStackUtil.addStackSizeLabel(label);
        check(label.getCount()==130&&label.getTagCompound().getCompoundTag("display").getTagList("Lore",8).getStringTagAt(0).equals("\u00a7r\u00a77\u00a7c2x64 + 2"),"oversized display label");
        ItemStack small=new ItemStack(Items.STICK,64); ItemStackUtil.addStackSizeLabel(small); check(!small.hasTagCompound(),"64 does not create tag");
        ItemStack h=new ItemStack(Items.CHEST_MINECART); check(ItemStackUtil.readStacksFromNBT(h,9)==null,"untagged holder null");
        ItemStackUtil.addStacksToNBT(h,s,null,ItemStack.EMPTY,a[2]); b=ItemStackUtil.readStacksFromNBT(h,24);
        check(b.length==24&&b[0].getCount()==7&&b[1]==null&&b[2]==null&&b[3].getItem()==Items.STICK&&b[23]==null,"sparse/default empty records do not erase slot0");
        check(b[0].getTagCompound().equals(s.getTagCompound()),"NBT roundtrip payload");
        b=ItemStackUtil.readStacksFromNBT(holder(entry(5,s))); check(b.length==1&&b[0]==null,"count0 list count, not largest slot");
        b=ItemStackUtil.readStacksFromNBT(holder(entry(0,s),entry(0,a[2])),1); check(b[0].getItem()==Items.DYE,"count limits records processed");
        b=ItemStackUtil.readStacksFromNBT(holder(entry(0,s),entry(0,a[2])),2); check(b[0].getItem()==Items.STICK,"last valid duplicate wins");
        b=ItemStackUtil.readStacksFromNBT(holder(entry(0,s),new NBTTagCompound()),3); check(b[0].getItem()==Items.DYE,"invalid duplicate does not erase");
        b=ItemStackUtil.readStacksFromNBT(holder(entry(128,s),entry(-1,s),entry(127,s)),130); check(b[127]!=null&&b[0]==null&&b[128]==null,"signed byte range");
        check(ItemStackUtil.readStacksFromNBT(holder()).length==0,"empty tagged list zero length");
        fails(NegativeArraySizeException.class,()->ItemStackUtil.readStacksFromNBT(holder(),-1),"negative count throws when tagged");
        check(ItemStackUtil.readStacksFromNBT(small,-1)==null,"no tag returns before count validation");
        fails(NullPointerException.class,()->ItemStackUtil.addTooltipToStack(null,"x"),"null tooltip unchanged");
        fails(NullPointerException.class,()->ItemStackUtil.addStacksToNBT(null,s),"null writer unchanged");
        fails(NullPointerException.class,()->ItemStackUtil.readStacksFromNBT(null),"null reader unchanged");
        check(ItemStackUtil.areStacksCompatible(s,s.copy()),"compatible tags"); ItemStack different=s.copy(); different.getTagCompound().setInteger("other",1); check(!ItemStackUtil.areStacksCompatible(s,different),"tag mismatch");
        check(ItemStackUtil.areStacksCompatible(new ItemStack(Items.DYE,1,3),new ItemStack(Items.DYE,12,3)),"compatibility ignores count");
        ItemStackUtil.addNBTFromString(s,"{marker:7}"); check(s.getTagCompound().getInteger("marker")==7&&!s.getTagCompound().hasKey("hfrHazRadiation"),"JSON replaces NBT");
        NBTTagCompound saved=s.getTagCompound(); ItemStackUtil.addNBTFromString(s,"{broken"); check(s.getTagCompound()==saved,"invalid JSON ignored");
        ItemStackUtil.addNBTFromString(null,"{broken"); check(true,"null stack plus malformed JSON is still ignored");
        fails(NullPointerException.class,()->ItemStackUtil.addNBTFromString(null,"{x:1}"),"null stack valid compound still throws");
        ItemStackUtil.addNBTFromString(s,"[1,2]"); check(s.getTagCompound()==saved,"noncompound JSON ignored");
        ItemStackUtil.addTooltipToStack(ItemStack.EMPTY,"x"); ItemStackUtil.addStacksToNBT(ItemStack.EMPTY,s); ItemStackUtil.addNBTFromString(ItemStack.EMPTY,"{x:1}");
        check(ItemStackUtil.carefulCopyWithSize(ItemStack.EMPTY,64)==ItemStack.EMPTY&&!ItemStack.EMPTY.hasTagCompound()&&ItemStack.EMPTY.isEmpty(),"shared EMPTY not mutated");
        check("minecraft".equals(ItemStackUtil.getModIdFromItemStack(new ItemStack(Items.STICK))),"registered namespace");
        Item named=new Item().setRegistryName(new ResourceLocation("fixture","not_registered"));
        check(named.getRegistryName()!=null&&Item.REGISTRY.getNameForObject(named)==null,"assigned name is not membership");
        check(ItemStackUtil.getModIdFromItemStack(new ItemStack(named))==null,"named unregistered has no mod id");
        check(ItemStackUtil.getModIdFromItemStack(new ItemStack(new Item()))==null,"unnamed unregistered has no mod id");
        OreDictionary.registerOre("itemStackUtilProbe",new ItemStack(Items.DYE,1,3)); check(ItemStackUtil.getOreDictNames(new ItemStack(Items.DYE,1,3)).contains("itemStackUtilProbe"),"native ore lookup");
        for(long seed:new long[]{0,1,42,123456789}) for(int n:new int[]{1,9,10,21,64,130}) spill(seed,n);
        FixtureWorld multi=new FixtureWorld(); TileEntityChest multiChest=new TileEntityChest(); multi.tile=multiChest;
        multiChest.setInventorySlotContents(2,tagged(1)); multiChest.setInventorySlotContents(8,new ItemStack(Items.STICK,1));
        Random multiRng=new Random(99), multiExpected=new Random(99); List<double[]> positions=new ArrayList<>();
        for(int i=0;i<2;i++) { positions.add(new double[]{multiExpected.nextFloat()*0.8F+0.1F,multiExpected.nextFloat()*0.8F+0.1F,multiExpected.nextFloat()*0.8F+0.1F}); multiExpected.nextInt(21); for(int j=0;j<3;j++) multiExpected.nextGaussian(); }
        ItemStackUtil.spillItems(multi,0,0,0,Blocks.CHEST,multiRng);
        check(multi.drops.size()==2 && multi.drops.get(0).getItem().getItem()==Items.DYE && multi.drops.get(1).getItem().getItem()==Items.STICK,"slot iteration order");
        for(int i=0;i<2;i++) { EntityItem e=multi.drops.get(i); double[] p=positions.get(i); check(e.posX==p[0]&&e.posY==p[1]&&e.posZ==p[2],"fresh offsets for each occupied slot"); }
        check(multiRng.nextLong()==multiExpected.nextLong(),"multi-slot exact RNG tail");
        FixtureWorld empty=new FixtureWorld(); Random rng=new Random(13), expected=new Random(13); ItemStackUtil.spillItems(empty,1,2,3,Blocks.CHEST,rng); check(empty.comparator==0&&rng.nextLong()==expected.nextLong(),"no tile no notification or RNG");
        empty.tile=new TileEntityChest(); rng=new Random(13); expected=new Random(13); ItemStackUtil.spillItems(empty,1,2,3,Blocks.CHEST,rng); check(empty.comparator==1&&empty.drops.isEmpty()&&rng.nextLong()==expected.nextLong(),"native EMPTY slots no RNG, comparator still updated");
        empty.tile=new TileEntity() {}; fails(ClassCastException.class,()->ItemStackUtil.spillItems(empty,1,2,3,Blocks.CHEST,new Random()),"noninventory tile cast unchanged");
        System.out.println("PASS ItemStackUtil native assertions: "+checks+"; not radiation gameplay");
    }
}

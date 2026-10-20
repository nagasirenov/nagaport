import com.hbm.util.NullableInventoryStackNbt;
import com.hbm.util.Tuple.Quartet;
import com.hbm.lib.ForgeDirection;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/** Runtime checks on source-extracted production bodies, not complete game owners. */
public class NativeRemainingNbtTest {
    static int checks;
    static Field rawCount;
    static void check(boolean yes, String message) { if (!yes) throw new AssertionError(message); checks++; }
    interface Action { void run() throws Exception; }
    static void npe(Action action, String label) throws Exception { try { action.run(); throw new AssertionError(label); } catch (NullPointerException expected) { checks++; } }
    static NBTTagCompound record(String id, int count) {
        NBTTagCompound n=new NBTTagCompound(); n.setString("id",id); n.setByte("Count",(byte)count); n.setShort("Damage",(short)7);
        NBTTagCompound tag=new NBTTagCompound(); tag.setString("unknown","retained"); n.setTag("tag",tag); return n;
    }
    static NBTTagCompound record(int count) { return record("minecraft:iron_ingot",count); }
    static NBTTagCompound root(String key, NBTTagCompound value) { NBTTagCompound n=new NBTTagCompound(); n.setTag(key,value); return n; }
    static void stack(ItemStack actual, NBTTagCompound source, int count, String label) throws Exception {
        check(actual!=null && actual!=ItemStack.EMPTY,label+" keeps owned nullable boundary");
        check(rawCount.getInt(actual)==count,label+" raw signed count");
        check(actual.isEmpty()==(count<=0),label+" native empty state");
        check(actual.getTagCompound()==source.getCompoundTag("tag"),label+" tag alias");
    }
    static void singletonOwners() throws Exception {
        Object[] owners={new ExtractedRemainingNbt.BlockSkeletonHolder(),new ExtractedRemainingNbt.PistonInserter(),new ExtractedRemainingNbt.EntityRideableRocket(),new ExtractedRemainingNbt.TileEntityCraneConsole(),new ExtractedRemainingNbt.EntityArtilleryShell()};
        String[] fields={"item","slot","navDrive","loadedItem","cargo"},keys={"item","stack","drive","held","cargo"};
        for(int i=0;i<owners.length;i++) {
            Object owner=owners[i]; Field f=owner.getClass().getField(fields[i]); Method read=owner.getClass().getMethod("read",NBTTagCompound.class),write=owner.getClass().getMethod("write",NBTTagCompound.class);
            for(int count:new int[]{-128,-1,0,1,64,127}) {
                NBTTagCompound rec=record(count); read.invoke(owner,root(keys[i],rec)); ItemStack loaded=(ItemStack)f.get(owner); stack(loaded,rec,count,fields[i]);
                NBTTagCompound saved=new NBTTagCompound(); saved.setString("other","untouched"); write.invoke(owner,saved);
                check(saved.getCompoundTag(keys[i]).equals(loaded.writeToNBT(new NBTTagCompound())),fields[i]+" exact native serialization even empty-count");
                check(saved.getString("other").equals("untouched"),fields[i]+" unrelated root preserved");
                if(count>0) { read.invoke(owner,saved); check(((ItemStack)f.get(owner)).getCount()==count,fields[i]+" populated roundtrip"); }
            }
            for(NBTTagCompound bad:new NBTTagCompound[]{new NBTTagCompound(),record("unknown:absent",1),record("minecraft:air",1)}) {
                read.invoke(owner,root(keys[i],bad)); check(f.get(owner)==null,fields[i]+" invalid becomes null");
            }
            f.set(owner,new ItemStack(Items.IRON_INGOT)); read.invoke(owner,new NBTTagCompound()); check(f.get(owner)==null,fields[i]+" missing clears previous");
            NBTTagCompound stale=record(2),out=root(keys[i],stale); write.invoke(owner,out); check(out.getTag(keys[i])==stale,fields[i]+" nullable writer retains old output tag");
        }
        ExtractedRemainingNbt.PistonInserter p=new ExtractedRemainingNbt.PistonInserter(); NBTTagCompound pn=new NBTTagCompound(); pn.setInteger("extend",-9); pn.setBoolean("retract",true); pn.setBoolean("state",true); p.read(pn);
        check(p.extend==-9 && p.isRetracting && p.lastState,"piston persisted scalars"); check(p.write(new NBTTagCompound()).equals(pn),"piston complete body roundtrip");
        ExtractedRemainingNbt.TileEntityCraneConsole c=new ExtractedRemainingNbt.TileEntityCraneConsole(); NBTTagCompound cn=new NBTTagCompound(); cn.setBoolean("crane",true);
        String[] integers={"craneRotationOffset","centerX","centerY","centerZ","spanF","spanB","spanL","spanR","height"};
        for(int i=0;i<integers.length;i++)cn.setInteger(integers[i],i-4); cn.setDouble("posFront",1.25); cn.setDouble("posLeft",-4.75); c.read(cn); check(c.write(new NBTTagCompound()).equals(cn),"crane all scalar fields roundtrip");
        ExtractedRemainingNbt.EntityArtilleryShell shell=new ExtractedRemainingNbt.EntityArtilleryShell(); check(shell.groundDespawn()==1200,"null cargo despawn");
        ItemStack zero=NullableInventoryStackNbt.read(record(0)); shell.setCargo(zero); check(shell.cargo==zero && shell.groundDespawn()==0,"actual setter alias and zero-count non-null cargo consumer");
    }
    static void loot() throws Exception {
        ExtractedRemainingNbt.BlockLoot l=new ExtractedRemainingNbt.BlockLoot(); List<?> alias=l.items;
        NBTTagCompound n=new NBTTagCompound(); n.setInteger("count",3); n.setTag("item0",record(3)); n.setTag("item1",record("unknown:absent",2)); n.setTag("item2",record(0)); n.setDouble("x0",2.5); n.setDouble("y0",-1.25); n.setDouble("z0",9);
        l.read(n); check(l.items==alias && l.items.size()==2,"loot retains list identity and skips invalid only"); check(l.items.get(0).getX()==2.5 && l.items.get(0).getY()==-1.25 && l.items.get(0).getZ()==9,"loot coordinates");
        stack(l.items.get(1).getW(),n.getCompoundTag("item2"),0,"loot zero"); l.read(n); check(l.items.size()==4,"loot read appends without reset");
        n.setInteger("count",-1); l.read(n); check(l.items.size()==4,"loot negative count no-op");
        l.items.clear(); l.items.add(null); l.items.add(new Quartet<ItemStack,Double,Double,Double>(null,0D,0D,0D)); l.items.add(new Quartet<ItemStack,Double,Double,Double>(new ItemStack(Items.IRON_INGOT,2),1D,2D,3D));
        NBTTagCompound stale=record(5),out=root("item0",stale); l.write(out); check(out.getInteger("count")==3,"loot writer counts holes"); check(out.getTag("item0")==stale && !out.hasKey("item1") && out.hasKey("item2"),"loot skips holes without deleting stale output");
        ExtractedRemainingNbt.BlockLoot again=new ExtractedRemainingNbt.BlockLoot(); NBTTagCompound clean=l.write(new NBTTagCompound()); again.read(clean); check(again.items.size()==1 && again.items.get(0).getW().getCount()==2,"loot sparse writer/read");
    }
    static NBTTagCompound slot(int index,NBTTagCompound rec) { rec.setByte("Slot",(byte)index); return rec; }
    static void cart() throws Exception {
        ExtractedRemainingNbt.EntityMinecartContainerBase c=new ExtractedRemainingNbt.EntityMinecartContainerBase(); ItemStack[] old=c.slots;
        NBTTagList list=new NBTTagList(); list.appendTag(slot(0,record(2))); list.appendTag(slot(53,record(0))); list.appendTag(slot(54,record(1))); list.appendTag(slot(255,record(1))); list.appendTag(slot(0,record("unknown:absent",1))); list.appendTag(record(7));
        NBTTagCompound n=new NBTTagCompound(); n.setTag("Items",list); c.read(n);
        check(c.slots!=old && c.slots.length==54,"minecart recreates exact concrete crate size"); check(c.slots[0].getCount()==7,"missing Slot defaults zero, last entry wins"); stack(c.slots[53],list.getCompoundTagAt(1),0,"minecart unsigned upper valid slot");
        NBTTagCompound out=new NBTTagCompound(); c.write(out); NBTTagList saved=out.getTagList("Items",10); check(saved.tagCount()==2 && saved.getCompoundTagAt(0).getByte("Slot")==0 && saved.getCompoundTagAt(1).getByte("Slot")==53,"minecart writer ordered nonnull slots including zero");
        NBTTagList duplicate=new NBTTagList(); duplicate.appendTag(slot(1,record(2))); duplicate.appendTag(slot(1,record("unknown:absent",1))); n.setTag("Items",duplicate); c.read(n); check(c.slots[1]==null,"minecart last invalid duplicate clears");
        NBTTagCompound lower=new NBTTagCompound(); lower.setTag("items",list); c.read(lower); check(Arrays.stream(c.slots).allMatch(Objects::isNull),"minecart uppercase list schema preserved");
    }
    static void particle() throws Exception {
        ExtractedRemainingNbt.TileEntityPASource p=new ExtractedRemainingNbt.TileEntityPASource(); NBTTagCompound pt=new NBTTagCompound();
        pt.setInteger("x",-3);pt.setInteger("y",4);pt.setInteger("z",5);pt.setInteger("dir",-3);pt.setInteger("momentum",72);pt.setInteger("defocus",-8);pt.setInteger("dist",99);pt.setTag("input1",record(3));pt.setTag("input2",record(0));
        p.read(root("particle",pt)); check(p.particle.x==-3 && p.particle.y==4 && p.particle.z==5 && p.particle.dir==ForgeDirection.values()[3],"particle actual constructor and enum normalization");
        check(p.particle.momentum==72 && p.particle.defocus==-8 && p.particle.distanceTraveled==99 && !p.particle.invalid,"particle scalar restore");
        Field source=p.particle.getClass().getDeclaredField("source");source.setAccessible(true);check(source.get(p.particle)==p,"particle constructor source alias");
        stack(p.particle.input1,pt.getCompoundTag("input1"),3,"particle input1");stack(p.particle.input2,pt.getCompoundTag("input2"),0,"particle input2");
        Object previous=p.particle;p.read(new NBTTagCompound());check(p.particle==previous,"absent particle does not clear");
        NBTTagCompound out=p.write(new NBTTagCompound()).getCompoundTag("particle");check(out.getByte("dir")==3 && out.getInteger("momentum")==72 && out.getCompoundTag("input2").equals(p.particle.input2.writeToNBT(new NBTTagCompound())),"particle full writer native stack behavior");
        pt.setTag("input2",record(2));p.read(root("particle",pt));NBTTagCompound round=p.write(new NBTTagCompound());p.read(round);check(p.particle.input1.getCount()==3 && p.particle.input2.getCount()==2,"particle paired positive roundtrip");
        pt.removeTag("input1");p.read(root("particle",pt));check(p.particle.input1==null,"particle missing input remains null");npe(()->p.write(new NBTTagCompound()),"original unguarded particle writer must fail");
        p.particle=null;NBTTagCompound stale=root("particle",pt);p.write(stale);check(stale.getTag("particle")==pt,"null particle writer retains stale output");
    }
    static void consumers() throws Exception {
        ItemStack shell=new ItemStack(Items.IRON_INGOT,1,8),cargo=new ItemStack(Items.GOLD_INGOT,4);shell.setTagCompound(new NBTTagCompound());ExtractedRemainingNbt.craftCargo(shell,cargo);
        ExtractedRemainingNbt.EntityArtilleryShell proj=new ExtractedRemainingNbt.EntityArtilleryShell();ExtractedRemainingNbt.turretCargo(shell,proj);check(proj.cargo.getItem()==Items.GOLD_INGOT && proj.cargo.getCount()==4,"actual crafting writer to turret loader");
        NBTTagCompound save=new NBTTagCompound();proj.write(save);proj.read(save);check(proj.cargo.getCount()==4 && proj.groundDespawn()==0,"cargo persistence to actual consumer");
        List<String> tips=new ArrayList<>();ExtractedRemainingNbt.ammoTooltip(shell,tips,"Y:","R:");check(tips.equals(Collections.singletonList("Y:"+cargo.getDisplayName())),"actual cargo tooltip valid path");
        tips.clear();ExtractedRemainingNbt.ammoTooltip(new ItemStack(Items.IRON_INGOT),tips,"Y:","R:");check(tips.equals(Collections.singletonList("R:Empty")),"untagged tooltip empty branch");
        shell.setTagCompound(new NBTTagCompound());npe(()->ExtractedRemainingNbt.ammoTooltip(shell,new ArrayList<String>(),"", ""),"tagged missing cargo original null dereference");
        ItemStack old=proj.cargo;ItemStack noncargo=new ItemStack(Items.IRON_INGOT,1,7);noncargo.setTagCompound(new NBTTagCompound());ExtractedRemainingNbt.turretCargo(noncargo,proj);check(proj.cargo==old,"noncargo metadata leaves projectile unchanged");
        ExtractedRemainingNbt.turretCargo(shell,proj);check(proj.cargo==null,"tagged absent cargo explicitly sets null");
        NBTTagCompound control=new NBTTagCompound();control.setInteger("slot",4);ExtractedRemainingNbt.guiStack(control,cargo);ItemStack pattern=ExtractedRemainingNbt.autocrafter(control);check(pattern.getItem()==Items.GOLD_INGOT && pattern.getCount()==4 && control.getInteger("slot")==4,"actual GUI stack writer to autocrafter loader fragment");
        check(ExtractedRemainingNbt.autocrafter(new NBTTagCompound())==null,"autocrafter missing stack boundary");
    }
    interface Loader { ItemStack load(NBTTagCompound n); }
    static void imc() throws Exception {
        Loader[] loaders={ExtractedRemainingNbt::IMCBlastFurnace_output,ExtractedRemainingNbt::IMCBlastFurnace_input1,ExtractedRemainingNbt::IMCBlastFurnace_input2,n->ExtractedRemainingNbt.IMCCentrifuge_output(n,0),ExtractedRemainingNbt::IMCCentrifuge_input,ExtractedRemainingNbt::IMCCrystallizer_output,ExtractedRemainingNbt::IMCCrystallizer_input};
        String[] keys={"output","input1","input2","output1","input","output","input"};
        for(int i=0;i<loaders.length;i++) {
            Loader loader=loaders[i];check(loader.load(new NBTTagCompound())==null,"IMC missing native record "+i);
            for(String id:new String[]{"unknown:absent","minecraft:air"})check(loader.load(root(keys[i],record(id,1)))==null,"IMC invalid native record "+i);
            NBTTagCompound numeric=record(1);numeric.setShort("id",(short)265);check(loader.load(root(keys[i],numeric))==null,"IMC does not invent numeric world importer "+i);
            for(int count:new int[]{-1,0,3}) {NBTTagCompound rec=record(count);stack(loader.load(root(keys[i],rec)),rec,count,"IMC expression "+i);}
        }
        NBTTagCompound n=new NBTTagCompound();for(int i=0;i<4;i++)n.setTag("output"+(i+1),record(i+1));for(int i=0;i<4;i++)check(ExtractedRemainingNbt.IMCCentrifuge_output(n,i).getCount()==i+1,"centrifuge one-based output index");
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();rawCount=ItemStack.class.getDeclaredField("stackSize");rawCount.setAccessible(true);
        singletonOwners();loot();cart();particle();consumers();imc();
        System.out.println("PASS actual remaining nullable NBT production slices: "+checks+" checks");
        System.out.println("LIMIT: extracted complete NBT bodies omit superclass calls; rocket/shell/control/IMC are bounded native-stack fragments, not game/world/network/recipe-registry acceptance.");
    }
}

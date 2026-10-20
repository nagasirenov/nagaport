package inventoryproduction;

import com.hbm.items.ItemInventory;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Proxy;
import java.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;

/** Compiles and calls the complete current production base and real engine APIs. */
public final class ProductionInventoryTest {
    static int checks;
    static void check(boolean condition,String message) { checks++; if(!condition)throw new AssertionError(message); }
    public static final class Inventory extends ItemInventory {
        Inventory() { target=new ItemStack(Items.APPLE);slots=new ItemStack[4]; }
        public int getSizeInventory() { return slots.length; }
        public String getName() { return "container.inventory"; }
        public boolean hasCustomName() { return target.hasDisplayName(); }
        static int raw(ItemStack stack) { return quantity(stack); }
        static void write(ItemStack owner,ItemStack... stacks) { writeStacksToNBT(owner,stacks); }
        static ItemStack[] read(ItemStack owner,int size) { return readStacksFromNBT(owner,size); }
    }
    /** Actual World subclass with a spawn observer, not a replacement platform class. */
    static final class ObservedWorld extends World {
        final List<EntityItem> drops=new ArrayList<>();
        ObservedWorld() {
            super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);
            provider.setWorld(this);
        }
        protected IChunkProvider createChunkProvider() { return null; }
        protected boolean isChunkLoaded(int x,int z,boolean allowEmpty) { return false; }
        public boolean spawnEntity(Entity entity) {
            check(entity instanceof EntityItem,"production spawns real EntityItem");
            drops.add((EntityItem)entity);
            return false; // Original algorithm ignores failure; no actual server/chunk spawn claimed.
        }
    }
    static final class ObservedPlayer extends EntityPlayer {
        final List<ITextComponent> messages=new ArrayList<>();
        ObservedPlayer(World world) { super(world,new GameProfile(new UUID(1,2),"InventoryOracle")); }
        public boolean isSpectator() { return false; }
        public boolean isCreative() { return false; }
        public void sendMessage(ITextComponent message) { messages.add(message); }
    }
    static ItemStack stack(int count) {
        ItemStack result=new ItemStack(Items.DIAMOND,count,9);
        NBTTagCompound tag=new NBTTagCompound();tag.setString("payload","kept");
        result.setTagCompound(tag);return result;
    }
    static NBTTagCompound slot(int index,int count,String item) {
        NBTTagCompound tag=new NBTTagCompound();
        tag.setByte("slot",(byte)index);tag.setString("id",item);tag.setByte("Count",(byte)count);tag.setShort("Damage",(short)9);
        return tag;
    }
    static ItemStack owner(NBTTagCompound... tags) {
        ItemStack owner=new ItemStack(Items.APPLE);
        NBTTagList list=new NBTTagList();for(NBTTagCompound tag:tags)list.appendTag(tag);
        NBTTagCompound compound=new NBTTagCompound();compound.setTag("items",list);owner.setTagCompound(compound);return owner;
    }
    public static void main(String[] args)throws Exception {
        Bootstrap.register();
        int[] counts={Integer.MIN_VALUE,-65536,-257,-256,-129,-128,-1,0,1,64,127,128,256,Integer.MAX_VALUE};
        int[] amounts={Integer.MIN_VALUE,-2,-1,0,1,3,64,Integer.MAX_VALUE};
        for(int count:counts) {
            Inventory inventory=new Inventory();ItemStack original=stack(count);
            check(Inventory.raw(original)==count,"production raw reader "+count);
            inventory.setInventorySlotContents(0,original);
            check(Inventory.raw(original)==Math.min(count,64),"production signed upper clamp "+count);
            check(inventory.slots[0]==(count==0?null:original),"production exact zero cleanup "+count);
            if(count!=0)check(inventory.getStackInSlot(0)==original,"native boundary nonnull identity "+count);
            inventory=new Inventory();inventory.slots[0]=original=stack(count);
            Slot slot=new Slot(inventory,0,0,0);
            check(slot.getStack()==original,"actual Slot gets raw object "+count);
            check(slot.getHasStack()==(count>0),"actual Slot emptiness "+count);
            check(inventory.isEmpty()==(count<=0)&&inventory.slots[0]==original,"native emptiness without domain mutation "+count);
            check(inventory.removeStackFromSlot(0)==original&&inventory.slots[0]==null,"production remove exact identity "+count);
            for(int amount:amounts) {
                inventory=new Inventory();original=stack(count);inventory.slots[0]=original;
                ItemStack result=inventory.decrStackSize(0,amount);
                if(count>amount) {
                    check(result!=original&&Inventory.raw(result)==amount,"production split identity/count");
                    check(Inventory.raw(original)==count-amount,"production split source signed overflow");
                    check(inventory.slots[0]==((count-amount)==0?null:original),"production split zero cleanup");
                    check(result.getTagCompound()!=original.getTagCompound()&&result.getTagCompound().equals(original.getTagCompound()),"production split deep NBT");
                } else {
                    check(result==original&&Inventory.raw(result)==count,"production whole decrement identity/count");
                    check(inventory.slots[0]==null,"production whole decrement vacancy");
                }
            }
        }
        Inventory inventory=new Inventory();
        check(inventory.getStackInSlot(0)==ItemStack.EMPTY&&inventory.getStackInSlotNullable(0)==null,"native absent vs nullable domain");
        check(!new Slot(inventory,0,0,0).getHasStack(),"actual Slot handles empty inventory");
        check(inventory.decrStackSize(0,1)==ItemStack.EMPTY,"absent decrement sentinel");
        check(inventory.removeStackFromSlot(0)==ItemStack.EMPTY,"absent remove sentinel");
        inventory.setInventorySlotContents(0,ItemStack.EMPTY);check(inventory.slots[0]==null,"incoming sentinel only");
        inventory.setInventorySlotContents(0,null);check(inventory.slots[0]==null,"incoming nullable domain");
        ItemStack over=stack(100);
        try { inventory.setInventorySlotContents(9,over);throw new AssertionError("invalid index accepted"); }
        catch(ArrayIndexOutOfBoundsException expected) { check(Inventory.raw(over)==64,"clamp before invalid-index failure"); }
        ItemStack repaired=new ItemStack(Items.DIAMOND,64,65536);repaired.setItemDamage(0);
        check(repaired.getCount()==0&&!repaired.isEmpty()&&Inventory.raw(repaired)==64,"production mapped cached-empty fallback");
        try { Inventory.raw(null);throw new AssertionError("null raw accepted"); }
        catch(NullPointerException expected) { checks++; }
        check(Inventory.read(new ItemStack(Items.APPLE),4)==null,"absent owner NBT");
        ItemStack nbtOwner=new ItemStack(Items.APPLE,0);
        Inventory.write(nbtOwner,stack(0),stack(-1),null,stack(128));
        NBTTagList written=nbtOwner.getTagCompound().getTagList("items",10);
        check(written.tagCount()==3,"writer keeps raw zero/negative and omits null");
        check(written.getCompoundTagAt(0).getByte("Count")==0&&written.getCompoundTagAt(1).getByte("Count")==-1&&written.getCompoundTagAt(2).getByte("Count")==-128,"writer preserves signed byte records");
        ItemStack[] restored=Inventory.read(nbtOwner,4);
        check(restored[0]!=null&&Inventory.raw(restored[0])==0&&Inventory.raw(restored[1])==-1&&restored[2]==null&&Inventory.raw(restored[3])==-128,"reader keeps raw zero/negative and sparse slots");
        restored=Inventory.read(owner(slot(0,1,"minecraft:diamond"),slot(0,-1,"minecraft:diamond"),slot(1,4,"missing:item")),3);
        check(Inventory.raw(restored[0])==-1&&restored[1]==null&&restored[2]==null,"valid duplicate overwrite and missing registry");
        restored=Inventory.read(owner(slot(1,1,"minecraft:diamond")),0);
        check(restored.length==1&&restored[0]==null,"automatic capacity is tagCount not highest index");
        restored=Inventory.read(owner(slot(0,1,"minecraft:diamond"),slot(0,2,"minecraft:diamond")),1);
        check(Inventory.raw(restored[0])==1,"reader iterates requested count not tagCount");
        restored=Inventory.read(owner(slot(128,1,"minecraft:diamond"),slot(0,2,"minecraft:air")),4);
        check(Arrays.equals(restored,new ItemStack[4]),"signed slot and AIR rejected");
        inventory=new Inventory();inventory.target.setTagCompound(new NBTTagCompound());inventory.target.getTagCompound().setString("keep","yes");
        ItemStack[] alias=inventory.slots;inventory.slots[0]=stack(1);inventory.slots[1]=stack(-1);inventory.clear();
        check(inventory.slots==alias&&Arrays.equals(alias,new ItemStack[4]),"clear preserves array identity and nullable vacancy");
        check(inventory.target.getTagCompound().getString("keep").equals("yes")&&inventory.target.getTagCompound().getTagList("items",10).tagCount()==0,"clear persists without deleting unrelated tags");
        for(int field:new int[]{-1,0,1,Integer.MAX_VALUE}) { inventory.setField(field,7);check(inventory.getField(field)==0&&inventory.getFieldCount()==0,"fieldless native contract"); }
        check(inventory.getDisplayName() instanceof TextComponentTranslation,"translation display");
        inventory.target.setStackDisplayName("Custom Inventory");
        check(inventory.getDisplayName() instanceof TextComponentString&&inventory.getDisplayName().getUnformattedText().equals("Custom Inventory"),"custom display");
        check(inventory.isUsableByPlayer(null)&&inventory.isItemValidForSlot(-1,null),"default admission preserved");
        ObservedWorld world=new ObservedWorld();ObservedPlayer player=new ObservedPlayer(world);
        player.setPosition(10,70,-4);player.motionX=.3;player.motionY=.4;player.motionZ=-.2;
        inventory=new Inventory();inventory.player=player;
        List<String> sounds=new ArrayList<>();
        world.addEventListener((IWorldEventListener)Proxy.newProxyInstance(ProductionInventoryTest.class.getClassLoader(),new Class<?>[]{IWorldEventListener.class},(proxy,method,values)->{
            if(method.getName().equals("playSoundToAllNearExcept"))sounds.add(values[1].toString()+":"+values[6]+":"+values[7]);
            return null;
        }));
        inventory.openInventory(player);inventory.closeInventory(player);check(sounds.size()==2,"real sound bridge emits open/close listener callbacks");
        Set<Integer> exactThresholds=new HashSet<>();
        for(int n=5900;n<6050;n++) {
            NBTTagCompound sized=new NBTTagCompound();byte[] bytes=new byte[n];new Random(9917).nextBytes(bytes);sized.setByteArray("padding",bytes);
            java.io.ByteArrayOutputStream compressed=new java.io.ByteArrayOutputStream();CompressedStreamTools.writeCompressed(sized,compressed);
            int size=compressed.size();
            if(size==5999||size==6000) { check(inventory.checkNBT(sized)==sized,"production exact <=6000 keeps NBT identity");exactThresholds.add(size); }
            if(size==6001) { check(inventory.checkNBT(sized)==null,"production exact >6000 resets NBT");exactThresholds.add(size); }
        }
        check(exactThresholds.equals(new HashSet<>(Arrays.asList(5999,6000,6001))),"production exact threshold cases found");
        player.messages.clear();world.drops.clear();
        ItemStack first=stack(37),last=new ItemStack(Items.GOLD_INGOT,7,4);last.setTagCompound(first.getTagCompound().copy());
        ItemStack negative=stack(-1);inventory.slots[0]=first;inventory.slots[1]=last;inventory.slots[2]=negative;
        NBTTagCompound tooLarge=new NBTTagCompound();byte[] payload=new byte[7000];new Random(37).nextBytes(payload);tooLarge.setByteArray("padding",payload);inventory.target.setTagCompound(tooLarge);
        inventory.markDirty();
        check(player.messages.size()==1&&player.messages.get(0).getUnformattedText().contains("6kB"),"real ejection warning");
        check(inventory.target.getTagCompound()==null,"overflow resets target NBT");
        check(inventory.slots[0]==first&&inventory.slots[1]==last&&Inventory.raw(first)==0&&Inventory.raw(last)==0&&inventory.slots[2]==negative&&Inventory.raw(negative)==-1,"ejection keeps raw slot identities and negative count");
        int diamonds=0,gold=0;
        for(EntityItem entity:world.drops) {
            ItemStack drop=entity.getItem();
            if(drop.getItem()==Items.DIAMOND) { diamonds+=drop.getCount();check(drop.getItemDamage()==9,"diamond metadata"); }
            else { check(drop.getItem()==Items.GOLD_INGOT,"final drop not AIR");gold+=drop.getCount();check(drop.getItemDamage()==4,"gold metadata"); }
            check(drop.getCount()>0&&drop.getCount()<=30,"ejection chunk count");
            check(drop.getTagCompound()!=first.getTagCompound()&&drop.getTagCompound()!=last.getTagCompound()&&drop.getTagCompound().getString("payload").equals("kept"),"ejection deep-copied tag");
            check(entity.posX>=10.1&&entity.posX<=10.9&&entity.posY>=70.1&&entity.posY<=70.9&&entity.posZ>=-3.9&&entity.posZ<=-3.1,"ejection position range");
        }
        check(diamonds==37&&gold==7,"all items accounted despite observer spawn false");
        inventory.markDirty();check(inventory.slots[0]==null&&inventory.slots[1]==null&&inventory.slots[2]==negative,"next dirty removes only depleted zeros");
        for(int damage:new int[]{65536,Integer.MAX_VALUE}) {
            world.drops.clear();player.messages.clear();
            ItemStack malformed=new ItemStack(Items.DIAMOND,7,damage);
            NBTTagCompound rawTag=new NBTTagCompound();rawTag.setString("edge","full signed damage");malformed.setTagCompound(rawTag);
            check(malformed.isEmpty()&&malformed.getItem()==Items.AIR&&Inventory.raw(malformed)==7,"malformed positive raw fixture");
            EntityItem referenceProbe=new EntityItem(world,0,0,0,malformed);
            check(referenceProbe.getItem()==malformed,"actual EntityItem retains supplied raw empty reference");
            inventory=new Inventory();inventory.player=player;inventory.slots[0]=malformed;
            inventory.target.setTagCompound(tooLarge.copy());inventory.markDirty();
            check(world.drops.size()==1,"malformed final single drop");
            ItemStack drop=world.drops.get(0).getItem();
            NBTTagCompound rawSnapshot=drop.writeToNBT(new NBTTagCompound());
            Item recovered=Item.getByNameOrId(rawSnapshot.getString("id"));
            check(recovered==Items.DIAMOND,"malformed ejection raw registry identity");
            check(Inventory.raw(drop)==7&&recovered.getDamage(drop)==damage,"malformed full raw count and int damage");
            check(drop.isEmpty()&&drop.getItem()==Items.AIR&&drop.getCount()==0,"native malformed drop remains engine-empty");
            check(drop.getTagCompound()!=rawTag&&drop.getTagCompound().equals(rawTag),"malformed final drop deep tag");
            check(world.drops.get(0).getItem()==drop,"actual spawned EntityItem retains raw reference");
            check(Inventory.raw(malformed)==0&&inventory.slots[0]==malformed&&inventory.target.getTagCompound()==null,"malformed source consumed and NBT reset");
        }
        world.drops.clear();player.messages.clear();
        ItemStack cachedEmpty=new ItemStack(Items.DIAMOND,7,65536);cachedEmpty.setItemDamage(9);
        NBTTagCompound repairTag=new NBTTagCompound();repairTag.setString("edge","cached repair");cachedEmpty.setTagCompound(repairTag);
        check(!cachedEmpty.isEmpty()&&cachedEmpty.getItem()==Items.AIR&&cachedEmpty.getCount()==0&&Inventory.raw(cachedEmpty)==7,"repaired dynamic-nonempty cached-empty fixture");
        inventory=new Inventory();inventory.player=player;inventory.slots[0]=cachedEmpty;inventory.target.setTagCompound(tooLarge.copy());inventory.markDirty();
        check(world.drops.size()==1,"cached-empty final single drop");
        ItemStack repairedDrop=world.drops.get(0).getItem();
        check(repairedDrop.getItem()==Items.DIAMOND&&repairedDrop.getItemDamage()==9&&Inventory.raw(repairedDrop)==7,"cached-empty ejection original item damage count");
        check(repairedDrop.getTagCompound()!=repairTag&&repairedDrop.getTagCompound().equals(repairTag),"cached-empty final copied tag");
        System.out.println("PASS complete production ItemInventory checks="+checks+"; actual native Slot, NBT, sounds, World/EntityPlayer/EntityItem subclasses; spawn observer only");
    }
}

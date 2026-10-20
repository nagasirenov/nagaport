import com.google.gson.*;
import com.hbm.advancement.*;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.util.AchievementHandler;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import net.minecraft.advancements.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceOutput;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.*;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.stats.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.*;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import sun.misc.Unsafe;

/** Actual native manager/parser/progress/stat persistence and Forge-dispatched production code.
 * Startup/chunk simulation, reward XP delivery, registry-table contents and network transport are
 * controlled boundaries. No Advancement, PlayerAdvancements, stat, registry or event API is mocked.
 */
public final class NativeAchievementsTest {
    private static int checks;
    private static final Unsafe UNSAFE;
    static { try { Field f=Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true); UNSAFE=(Unsafe)f.get(null); } catch(Exception e) { throw new ExceptionInInitializerError(e); } }
    private static void check(boolean ok,String what) { checks++; if(!ok) throw new AssertionError(what); }
    private static void set(Object target,Class<?> type,String name,Object value) throws Exception { Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(target,value); }
    public static class ServerBoundary extends DedicatedServer {
        AdvancementManager manager;
        public ServerBoundary() { super(null,null,null,null,null,null); }
        @Override public AdvancementManager getAdvancementManager() { return manager; }
        @Override public FunctionManager getFunctionManager() { return null; }
    }
    public static class WorldBoundary extends WorldServer {
        GameRules rules;
        Scoreboard board;
        int spawnedXp;
        @Override public boolean spawnEntity(Entity entity) { if(entity instanceof EntityXPOrb) spawnedXp += ((EntityXPOrb)entity).getXpValue(); return true; }
        public WorldBoundary() { super(null,null,null,0,null); }
        @Override public GameRules getGameRules() { return rules; }
        @Override public Scoreboard getScoreboard() { return board; }
        @Override public LootTableManager getLootTableManager() { return null; }
    }
    public static class PlayerBoundary extends EntityPlayerMP {
        int rewardExperience;
        public PlayerBoundary() { super(null,null,null,null); }
        @Override public float getLuck() { return 0; }
        @Override public void addExperience(int value) { rewardExperience+=value; }
        @Override public String getName() { return "NativeAchievementProbe"; }
        @Override public void onItemPickup(Entity entity,int count) { } // Isolate network/entity tracker delivery only.
    }
    public static class CustomPickaxe extends ItemPickaxe { CustomPickaxe() { super(Item.ToolMaterial.DIAMOND); } }
    public static class CompletionListener {
        int completions;
        @SubscribeEvent public void completed(net.minecraftforge.event.entity.player.AdvancementEvent event) { completions++; }
    }
    public static class NestedCraftListener {
        final PlayerBoundary target; final boolean fml; boolean nested;
        NestedCraftListener(PlayerBoundary target,boolean fml) { this.target=target;this.fml=fml; }
        @SubscribeEvent public void crafted(PlayerEvent.ItemCraftedEvent event) {
            if(event.player!=target || nested) return;
            nested=true;
            if(fml) FMLCommonHandler.instance().firePlayerCraftingEvent(target,new ItemStack(Blocks.CRAFTING_TABLE),event.craftMatrix);
            else MinecraftForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(target,new ItemStack(Blocks.CRAFTING_TABLE),event.craftMatrix));
            nested=false;
        }
    }
    public static class NtmCraftDispatchBoundary {
        @SubscribeEvent public void crafted(PlayerEvent.ItemCraftedEvent event) { AchievementHandler.fire(event.player,event.crafting); }
    }
    public static class CancelPickup {
        @SubscribeEvent public void cancel(EntityItemPickupEvent event) { event.setCanceled(true); }
    }
    private static ServerBoundary server;
    private static WorldBoundary world;
    private static int serial;
    private static PlayerBoundary player(String name) throws Exception {
        PlayerBoundary p=(PlayerBoundary)UNSAFE.allocateInstance(PlayerBoundary.class);
        p.world=world;
        p.inventory=new InventoryPlayer(p);p.capabilities=new PlayerCapabilities();p.capturedDrops=new ArrayList<>();
        set(p,EntityPlayerMP.class,"server",server);
        StatisticsManagerServer stats=new StatisticsManagerServer(server,new File(name+"-stats.json"));
        stats.readStatFile();
        set(p,EntityPlayerMP.class,"statsFile",stats);
        set(p,EntityPlayerMP.class,"advancements",new PlayerAdvancements(server,new File(name+"-advancements.json"),p));
        return p;
    }
    private static PlayerBoundary fresh() throws Exception { return player("player-"+(serial++)); }
    private static int count(PlayerBoundary p,ResourceLocation id) { return p.getStatFile().readStat(NtmAdvancements.counter(id)); }
    private static void externalGrant(PlayerBoundary p,ResourceLocation id) { p.getAdvancements().grantCriterion(server.manager.getAdvancement(id),NtmAdvancements.CRITERION); }
    private static void pickup(PlayerBoundary p,ItemStack stack) { MinecraftForge.EVENT_BUS.post(new PlayerEvent.ItemPickupEvent(p,null,stack)); }
    private static void craft(PlayerBoundary p,ItemStack stack) { MinecraftForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(p,stack,new InventoryBasic("Craft fixture",false,9))); }
    private static void smelt(PlayerBoundary p,ItemStack stack) { MinecraftForge.EVENT_BUS.post(new PlayerEvent.ItemSmeltedEvent(p,stack)); }
    private static Item register(String name) {
        ResourceLocation id=new ResourceLocation(name);
        if(ForgeRegistries.ITEMS.containsKey(id)) return ForgeRegistries.ITEMS.getValue(id);
        Item item=new Item().setRegistryName(id).setHasSubtypes(true); ForgeRegistries.ITEMS.register(item); return item;
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
        ModMetadata metadata=new ModMetadata();metadata.modId="hbm";metadata.name="Native achievement fixture";metadata.version="1";
        final File modRoot=new File(args[0]);
        DummyModContainer owner=new DummyModContainer(metadata) { @Override public File getSource() { return modRoot; } };Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);
        Loader.instance().getActiveModList().add(owner);
        JsonArray baseline=new JsonParser().parse(new InputStreamReader(new FileInputStream(args[1]),StandardCharsets.UTF_8)).getAsJsonArray();
        for(Field f:com.hbm.items.ModItems.class.getFields()) f.set(null,register("hbm:item."+f.getName()));
        for(Field f:com.hbm.blocks.ModBlocks.class.getFields()) {
            ResourceLocation id=new ResourceLocation("hbm","tile."+f.getName());
            Block block=new Block(Material.IRON).setRegistryName(id);ForgeRegistries.BLOCKS.register(block);
            if(ForgeRegistries.ITEMS.containsKey(id)) throw new AssertionError("Fixture block identity must precede icon registration: "+id);
            ForgeRegistries.ITEMS.register(new ItemBlock(block).setRegistryName(id));f.set(null,block);
        }
        for(JsonElement element:baseline) register(element.getAsJsonObject().get("icon").getAsString());
        server=(ServerBoundary)UNSAFE.allocateInstance(ServerBoundary.class);
        world=(WorldBoundary)UNSAFE.allocateInstance(WorldBoundary.class);world.rules=new GameRules();world.rules.setOrCreateGameRule("announceAdvancements","false");world.board=new Scoreboard();
        set(world,World.class,"provider",new WorldProviderSurface());
        server.manager=new AdvancementManager(new File("empty-world-advancements"));
        check(!server.manager.hasErrored(),"native AdvancementManager loaded production JSON without errors");
        NtmAdvancements.registerCounters();NtmAdvancements.registerCounters();
        net.minecraft.util.text.TextComponentTranslation inventoryDescription=(net.minecraft.util.text.TextComponentTranslation)server.manager.getAdvancement(NtmAdvancements.OPEN_INVENTORY).getDisplay().getDescription();
        check(inventoryDescription.getFormatArgs().length==1 && inventoryDescription.getFormatArgs()[0] instanceof net.minecraft.util.text.TextComponentKeybind,"inventory description retains native dynamic keybinding argument");
        Advancement root=server.manager.getAdvancement(NtmAdvancements.ROOT);check(root.getParent()==null && !root.getDisplay().shouldAnnounceToChat() && !root.getDisplay().shouldShowToast() && root.getRewards()==AdvancementRewards.EMPTY,"presentation-only root has no parent/reward/chat/toast");
        check(AchievementDefinitions.all().size()==70,"70 gameplay definitions, no rotConsum");
        int nativeCount=0;
        for(Advancement a:server.manager.getAdvancements()) if(a.getId().getNamespace().equals("hbm")) nativeCount++;
        check(nativeCount==71,"all 71 definitions genuinely parsed and native-built");
        for(JsonElement element:baseline) {
            JsonObject row=element.getAsJsonObject();ResourceLocation id=new ResourceLocation(row.get("id").getAsString());
            Advancement a=server.manager.getAdvancement(id); AchievementDefinitions.Definition d=AchievementDefinitions.all().get(id);
            check(a!=null && d!=null && NtmAdvancements.id(row.get("legacy").getAsString()).equals(id),"legacy/native mapping "+id);
            check(Objects.equals(d.parent,row.get("parent").isJsonNull()?null:new ResourceLocation(row.get("parent").getAsString())),"exact original gameplay parent "+id);
            check(a.getCriteria().size()==1 && a.getCriteria().containsKey("earned"),"impossible single criterion "+id);
            check(a.getRewards()==AdvancementRewards.EMPTY,"no extra rewards "+id);
            check(NtmAdvancements.icon(id).getItem()==ForgeRegistries.ITEMS.getValue(d.icon) && NtmAdvancements.icon(id).getMetadata()==d.metadata,"native icon identity/meta "+id);
        }
        Loader.instance().setActiveModContainer(owner);
        CompletionListener completions=new CompletionListener();MinecraftForge.EVENT_BUS.register(completions);
        for(AchievementDefinitions.Definition d:AchievementDefinitions.all().values()) {
            PlayerBoundary p=fresh();int before=completions.completions;
            if(d.parent!=null) {
                check(!NtmAdvancements.award(p,d.id) && !NtmAdvancements.has(p,d.id) && count(p,d.id)==0,"premature event discarded "+d.id);
                externalGrant(p,d.parent);
                check(!NtmAdvancements.has(p,d.id),"parent grant does not replay child "+d.id);
                // Deliberately grant only direct parent. No stricter recursive ancestry rule.
                AchievementDefinitions.Definition parent=AchievementDefinitions.all().get(d.parent);
                if(parent.parent!=null) check(!NtmAdvancements.has(p,parent.parent),"no implicit grandparent "+d.id);
                before=completions.completions;
            }
            check(NtmAdvancements.award(p,d.id) && NtmAdvancements.has(p,d.id) && count(p,d.id)==1,"qualified native award "+d.id);
            check(!NtmAdvancements.award(p,d.id) && count(p,d.id)==2,"repeat increments integer without re-completion "+d.id);
            check(completions.completions==before+2,"exactly root and first award native events "+d.id);
            check(p.rewardExperience==0,"no hidden XP reward "+d.id);
        }
        PlayerBoundary persistent=player("persistent");ResourceLocation rad=NtmAdvancements.id("radPoison"),death=NtmAdvancements.id("radDeath");
        NtmAdvancements.award(persistent,rad);NtmAdvancements.award(persistent,rad);NtmAdvancements.award(persistent,death);
        persistent.getAdvancements().save();persistent.getStatFile().saveStatFile();
        check(new File("persistent-advancements.json").isFile() && new File("persistent-stats.json").isFile(),"real native persistence files written");
        PlayerBoundary reloaded=player("persistent");
        check(NtmAdvancements.has(reloaded,rad) && NtmAdvancements.has(reloaded,death) && count(reloaded,rad)==2,"relogin/restart reload both native stores");
        Advancement old=server.manager.getAdvancement(rad);server.manager.reload();reloaded.getAdvancements().reload();
        check(server.manager.getAdvancement(rad)!=old && NtmAdvancements.has(reloaded,rad),"manager reload resolves current objects");
        NtmAdvancements.award(reloaded,rad);check(count(reloaded,rad)==3,"post-reload repeated counter");
        PlayerBoundary replacement=fresh();reloaded.getAdvancements().setPlayer(replacement);set(replacement,EntityPlayerMP.class,"advancements",reloaded.getAdvancements());set(replacement,EntityPlayerMP.class,"statsFile",reloaded.getStatFile());
        check(NtmAdvancements.has(replacement,death) && count(replacement,rad)==3,"native respawn player replacement retains stores");
        NtmAdvancements.award(replacement,death);check(count(replacement,death)==2,"replacement can still award through native progress owner");
        PlayerBoundary noop=fresh();check(!NtmAdvancements.award(noop,null) && !NtmAdvancements.has(noop,null),"rotConsum null no-op");
        FakePlayer fake=(FakePlayer)UNSAFE.allocateInstance(FakePlayer.class);fake.world=world;set(fake,EntityPlayerMP.class,"server",server);
        set(fake,EntityPlayerMP.class,"statsFile",new StatisticsManagerServer(server,new File("fake-stats.json")));set(fake,EntityPlayerMP.class,"advancements",new PlayerAdvancements(server,new File("fake-advancements.json"),fake));
        check(!NtmAdvancements.award(fake,rad) && fake.getStatFile().readStat(NtmAdvancements.counter(rad))==0 && !fake.getAdvancements().getProgress(server.manager.getAdvancement(rad)).isDone(),"standard Forge FakePlayer changes neither store");
        check(!fake.getAdvancements().grantCriterion(server.manager.getAdvancement(rad),"earned"),"genuine native progress also rejects FakePlayer");
        Loader.instance().setActiveModContainer(owner);
        MinecraftForge.EVENT_BUS.register(new LegacyAchievementEvents());
        PlayerBoundary events=fresh();pickup(events,new ItemStack(Blocks.LOG,3,2));check(!NtmAdvancements.has(events,NtmAdvancements.MINE_WOOD),"wood before explicit inventory action discarded");
        NtmAdvancements.award(events,NtmAdvancements.OPEN_INVENTORY);check(!NtmAdvancements.has(events,NtmAdvancements.MINE_WOOD),"inventory action does not replay old pickup");
        for(int meta=0;meta<4;meta++) pickup(events,new ItemStack(Blocks.LOG,3,meta));
        for(int meta=0;meta<2;meta++) pickup(events,new ItemStack(Blocks.LOG2,3,meta));
        check(count(events,NtmAdvancements.MINE_WOOD)==6,"both vanilla logs/all metadata count once per captured pickup event");
        pickup(events,new ItemStack(Blocks.PLANKS));check(count(events,NtmAdvancements.MINE_WOOD)==6,"non-log pickup negative");
        PlayerBoundary realPickup=fresh();NtmAdvancements.award(realPickup,NtmAdvancements.OPEN_INVENTORY);
        EntityItem drop=new EntityItem(world,0,0,0,new ItemStack(Blocks.LOG2,3,1));drop.setNoPickupDelay();drop.onCollideWithPlayer(realPickup);
        check(count(realPickup,NtmAdvancements.MINE_WOOD)==1 && drop.isDead,"actual EntityItem successful pickup uses captured stack after entity removal; count="+count(realPickup,NtmAdvancements.MINE_WOOD)+" stack="+drop.getItem()+" dead="+drop.isDead);
        PlayerBoundary rejectedPickup=fresh();NtmAdvancements.award(rejectedPickup,NtmAdvancements.OPEN_INVENTORY);
        CancelPickup cancellation=new CancelPickup();MinecraftForge.EVENT_BUS.register(cancellation);
        EntityItem rejected=new EntityItem(world,0,0,0,new ItemStack(Blocks.LOG));rejected.setNoPickupDelay();rejected.onCollideWithPlayer(rejectedPickup);
        check(!rejected.getItem().isEmpty() && count(rejectedPickup,NtmAdvancements.MINE_WOOD)==0,"genuine Forge canceled pickup never emits completion");MinecraftForge.EVENT_BUS.unregister(cancellation);
        for(int i=0;i<rejectedPickup.inventory.mainInventory.size();i++) rejectedPickup.inventory.mainInventory.set(i,new ItemStack(Blocks.STONE,64));
        rejected.onCollideWithPlayer(rejectedPickup);check(!rejected.getItem().isEmpty() && count(rejectedPickup,NtmAdvancements.MINE_WOOD)==0,"full native inventory prevents pickup award");
        craft(events,new ItemStack(Blocks.FURNACE));check(!NtmAdvancements.has(events,NtmAdvancements.BUILD_FURNACE),"furnace crafting before pickaxe discarded");
        smelt(events,new ItemStack(Blocks.CRAFTING_TABLE));pickup(events,new ItemStack(Blocks.CRAFTING_TABLE));check(!NtmAdvancements.has(events,NtmAdvancements.BUILD_WORKBENCH),"smelting/picking up crafting table does not qualify crafting predicate");
        craft(events,new ItemStack(Blocks.CRAFTING_TABLE));craft(events,new ItemStack(Items.DIAMOND_PICKAXE));
        CustomPickaxe custom=new CustomPickaxe();ForgeRegistries.ITEMS.register(custom.setRegistryName("hbm","test_pickaxe"));craft(events,new ItemStack(custom));
        check(count(events,NtmAdvancements.BUILD_PICKAXE)==2,"nonwood and subclass ItemPickaxe both qualify");
        craft(events,new ItemStack(Blocks.FURNACE));
        craft(events,new ItemStack(Items.IRON_INGOT));pickup(events,new ItemStack(Items.IRON_INGOT));check(!NtmAdvancements.has(events,NtmAdvancements.ACQUIRE_IRON),"iron crafting/pickup never substitutes extraction");
        smelt(events,new ItemStack(Items.IRON_INGOT,64));smelt(events,new ItemStack(Items.COOKED_FISH,4,0));smelt(events,new ItemStack(Items.COOKED_FISH,4,1));
        check(count(events,NtmAdvancements.ACQUIRE_IRON)==1 && count(events,NtmAdvancements.COOK_FISH)==2,"extraction attempts once irrespective of quantity; both fish metadata");
        for(boolean ntm:new boolean[] {false,true}) {
            for(int meta:new int[] {-1,0,1}) {
                ItemStack output=meta<0?new ItemStack(Items.IRON_INGOT,8):new ItemStack(Items.COOKED_FISH,8,meta);
                PlayerBoundary slotPlayer=fresh(); externalGrant(slotPlayer,NtmAdvancements.BUILD_FURNACE);
                ResourceLocation result=meta<0?NtmAdvancements.ACQUIRE_IRON:NtmAdvancements.COOK_FISH;
                InventoryBasic inventory=new InventoryBasic("Real extraction",false,1); inventory.setInventorySlotContents(0,output.copy());
                Slot slot=ntm?new com.hbm.inventory.SlotSmelting(slotPlayer,inventory,0,0,0):new SlotFurnaceOutput(slotPlayer,inventory,0,0,0);
                check(count(slotPlayer,result)==0,"mere completed furnace output does not award "+ntm+"/"+meta);
                ItemStack taken=slot.decrStackSize(1);check(count(slotPlayer,result)==0,"removing output without player onTake does not award "+ntm+"/"+meta);
                slot.onTake(slotPlayer,taken);check(count(slotPlayer,result)==1,"actual vanilla/NTM onTake grants exactly once "+ntm+"/"+meta);
                taken=slot.decrStackSize(7);slot.onTake(slotPlayer,taken);check(count(slotPlayer,result)==2,"bulk player extraction attempt grants once, not per item "+ntm+"/"+meta);
            }
        }
        for(boolean ntm:new boolean[] {false,true}) {
            for(int meta:new int[] {-1,0,1}) {
                PlayerBoundary shift=fresh(); externalGrant(shift,NtmAdvancements.BUILD_FURNACE);
                shift.inventory=new InventoryPlayer(shift);
                InventoryBasic furnace=new InventoryBasic("Native shift-click furnace",false,3);
                furnace.setInventorySlotContents(2,meta<0?new ItemStack(Items.IRON_INGOT,8):new ItemStack(Items.COOKED_FISH,8,meta));
                ContainerFurnace container=new ContainerFurnace(shift.inventory,furnace);
                if(ntm) container.inventorySlots.set(2,new com.hbm.inventory.SlotSmelting(shift,furnace,2,0,0));
                ResourceLocation id=meta<0?NtmAdvancements.ACQUIRE_IRON:NtmAdvancements.COOK_FISH;
                ItemStack moved=container.transferStackInSlot(shift,2);
                check(moved.getCount()==8 && furnace.getStackInSlot(2).isEmpty(),"genuine native full shift-click moved output "+ntm+"/"+meta);
                check(count(shift,id)==2,"both original shift-click callback attempts preserved "+ntm+"/"+meta);
                check(container.transferStackInSlot(shift,2).isEmpty() && count(shift,id)==2,"empty source shift-click does not award "+ntm+"/"+meta);
            }
        }
        check(world.spawnedXp>0,"actual native and NTM slot XP code executed upstream of award");
        PlayerBoundary emptyOutput=fresh();externalGrant(emptyOutput,NtmAdvancements.BUILD_FURNACE);
        smelt(emptyOutput,ItemStack.EMPTY);craft(emptyOutput,ItemStack.EMPTY);
        check(ItemStack.EMPTY.isEmpty() && ItemStack.EMPTY.getCount()==0 && count(emptyOutput,NtmAdvancements.ACQUIRE_IRON)==0 && count(emptyOutput,NtmAdvancements.COOK_FISH)==0,"shared EMPTY stays empty and never awards");
        for(int amount:new int[] {0,-1}) {
            ItemStack zero=new ItemStack(Items.IRON_INGOT,1);NBTTagCompound tag=new NBTTagCompound();tag.setString("probe","preserved");zero.setTagCompound(tag);zero.setCount(amount);
            NBTTagCompound saved=zero.serializeNBT();smelt(emptyOutput,zero);
            check(zero.serializeNBT().getByte("Count")==amount && zero.serializeNBT().equals(saved) && zero.getTagCompound()==tag,"raw output identity recovery leaves count/NBT/capability serialization unchanged "+amount);
        }
        check(count(emptyOutput,NtmAdvancements.ACQUIRE_IRON)==2,"zero and negative raw item event identities preserved, not shared EMPTY");
        for(boolean workbench:new boolean[] {false,true}) {
            ItemStack[] outputs={new ItemStack(Blocks.CRAFTING_TABLE),new ItemStack(Items.DIAMOND_PICKAXE),new ItemStack(Blocks.FURNACE)};
            ResourceLocation[] ids={NtmAdvancements.BUILD_WORKBENCH,NtmAdvancements.BUILD_PICKAXE,NtmAdvancements.BUILD_FURNACE};
            ResourceLocation[] parents={NtmAdvancements.MINE_WOOD,NtmAdvancements.BUILD_WORKBENCH,NtmAdvancements.BUILD_PICKAXE};
            for(int i=0;i<outputs.length;i++) {
                for(boolean shift:new boolean[] {false,true}) {
                    PlayerBoundary crafter=fresh();externalGrant(crafter,parents[i]);
                    Container container=workbench?new ContainerWorkbench(crafter.inventory,world,BlockPos.ORIGIN):new ContainerPlayer(crafter.inventory,true,crafter);
                    InventoryCraftResult result=workbench?((ContainerWorkbench)container).craftResult:((ContainerPlayer)container).craftResult;
                    result.setInventorySlotContents(0,outputs[i].copy());
                    if(shift) container.transferStackInSlot(crafter,0);
                    else { Slot slot=container.getSlot(0);slot.onTake(crafter,slot.decrStackSize(1)); }
                    check(count(crafter,ids[i])==(shift?2:1),"genuine standard vanilla crafting callbacks retain old attempts "+workbench+"/"+shift+"/"+ids[i]);
                }
            }
        }
        for(boolean fml:new boolean[] {false,true}) {
            PlayerBoundary nested=fresh();externalGrant(nested,NtmAdvancements.MINE_WOOD);
            NestedCraftListener listener=new NestedCraftListener(nested,fml);MinecraftForge.EVENT_BUS.register(listener);
            ContainerWorkbench container=new ContainerWorkbench(nested.inventory,world,BlockPos.ORIGIN);
            container.craftResult.setInventorySlotContents(0,new ItemStack(Blocks.CRAFTING_TABLE));container.transferStackInSlot(nested,0);
            check(count(nested,NtmAdvancements.BUILD_WORKBENCH)==3,"nested programmatic crafting event does not inherit native shift double attempt "+fml);
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        Class<?> callback=Class.forName("com.hbm.advancement.LegacyCraftingCallbacks");
        java.lang.reflect.Method stackGuard=callback.getDeclaredMethod("isVanillaShiftTransfer",StackTraceElement[].class);stackGuard.setAccessible(true);
        StackTraceElement[] srg={new StackTraceElement("net.minecraftforge.fml.common.eventhandler.EventBus","post","",0),new StackTraceElement("net.minecraftforge.fml.common.FMLCommonHandler","firePlayerCraftingEvent","",0),new StackTraceElement("net.minecraft.inventory.SlotCrafting","func_75208_c","",0),new StackTraceElement("net.minecraft.inventory.SlotCrafting","func_75210_a","",0),new StackTraceElement("net.minecraft.inventory.Slot","func_75220_a","",0),new StackTraceElement("net.minecraft.inventory.ContainerWorkbench","func_82846_b","",0)};
        check((Boolean)stackGuard.invoke(null,(Object)srg),"exact cached production SRG callback names accepted");
        srg[5]=new StackTraceElement("other.ContainerWorkbench","func_82846_b","",0);check(!(Boolean)stackGuard.invoke(null,(Object)srg),"same simple class name does not impersonate native chain");
        AchievementHandler.register();check(AchievementHandler.craftingAchievements.size()==35,"full production craft dispatcher has all 35 exact entries");
        for(Map.Entry<ComparableStack,ResourceLocation> entry:AchievementHandler.craftingAchievements.entrySet()) {
            PlayerBoundary p=fresh();AchievementDefinitions.Definition d=AchievementDefinitions.all().get(entry.getValue());
            if(d.parent!=null) externalGrant(p,d.parent);
            ItemStack output=entry.getKey().toStack();output.setCount(64);AchievementHandler.fire(p,output);
            check(NtmAdvancements.has(p,entry.getValue()) && count(p,entry.getValue())==1,"full dispatcher normalizes size for "+output.getItem().getRegistryName());
            output.setCount(0);AchievementHandler.fire(p,output);check(count(p,entry.getValue())==2 && output.isEmpty(),"zero-count original lookup preserved without mutation");
            output.setCount(64);output.setItemDamage(1);AchievementHandler.fire(p,output);check(count(p,entry.getValue())==2,"metadata mismatch excluded for "+output.getItem().getRegistryName());
        }
        MinecraftForge.EVENT_BUS.register(new NtmCraftDispatchBoundary());
        PlayerBoundary ntmShift=fresh();externalGrant(ntmShift,NtmAdvancements.id("burnerPress"));
        ContainerWorkbench ntmContainer=new ContainerWorkbench(ntmShift.inventory,world,BlockPos.ORIGIN);
        ntmContainer.craftResult.setInventorySlotContents(0,new ItemStack(com.hbm.items.ModItems.piston_selenium));ntmContainer.transferStackInSlot(ntmShift,0);
        check(count(ntmShift,NtmAdvancements.id("selenium"))==1,"native shift correction never doubles NTM35 dispatch");
        InventoryActionPacketNativeTest.run(server,world,player("packet-sender"),player("packet-other"));
        System.out.println("PASS "+checks+" native advancement definition/gate/counter/persistence/dispatch assertions");
    }
}

import api.hbm.conveyor.*;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.*;
import com.hbm.entity.item.*;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.storage.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.*;

/** Complete production classes, native world/chunks/entities/data-manager/stack inventory.
 * Fixture worlds intercept only outbound spawn acceptance and unloaded infrastructure.
 * No test overrides production engine movement, synchronization, or item logic. */
public final class MovingItemEngineNativeTest {
    static int checks,ticks; static final List<String> events=new ArrayList<>();
    static final BlockPos POS=new BlockPos(8,64,8); static Block[] owners;
    static Belt belt; static Enterable entry; static Enterable lower; static DataParameter<ItemStack> itemKey; static DataParameter<Boolean> presentKey;
    static void check(boolean v,String text){checks++;if(!v)throw new AssertionError(text);}
    static void num(double a,double b,String text){check(Double.doubleToLongBits(a)==Double.doubleToLongBits(b)||Math.abs(a-b)<1E-11,text+" expected="+b+" actual="+a);}
    static void xyz(Entity e,double x,double y,double z,String text){num(e.posX,x,text+" x");num(e.posY,y,text+" y");num(e.posZ,z,text+" z");}
    static final class NativeWorld extends World {
        final Map<Long,Chunk> chunks=new HashMap<>(); final List<Entity> spawned=new ArrayList<>();
        Entity original; Boolean expectedDead; boolean rejectSpawn,throwSpawn;
        NativeWorld(boolean remote){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),remote);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
        static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
        protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "native engine fixture";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
        protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return chunks.containsKey(key(x,z));}
        public boolean checkLight(BlockPos p){return true;}
        public boolean spawnEntity(Entity e){if(original!=null&&expectedDead!=null)check(original.isDead==expectedDead,"source death state at spawn");events.add("spawn");spawned.add(e);if(throwSpawn)throw new IllegalStateException("fixture spawn failure");return !rejectSpawn;}
        void track(Entity source,boolean dead){original=source;expectedDead=dead;spawned.clear();events.clear();rejectSpawn=false;throwSpawn=false;}
    }
    static class Moving extends EntityMovingItem {
        Moving(World w){super(w);}
        public void setDead(){events.add("dead");super.setDead();}
        void readItem(NBTTagCompound tag){readEntityFromNBT(tag);} NBTTagCompound writeItem(){NBTTagCompound n=new NBTTagCompound();writeEntityToNBT(n);return n;}
        int interpolation(){return turnProgress;}
        Entity lastAttacker; public boolean hitByEntity(Entity e){lastAttacker=e;return super.hitByEntity(e);}
    }
    static class BaseProbe extends EntityMovingConveyorObject {
        boolean leave=true; int leaveCalls; ForgeDirection side; com.hbm.util.fauxpointtwelve.BlockPos entered;
        BaseProbe(World w){super(w);setSize(.375F,.375F);}
        protected void entityInit(){} protected void readEntityFromNBT(NBTTagCompound n){} protected void writeEntityToNBT(NBTTagCompound n){}
        public boolean onLeaveConveyor(){leaveCalls++;events.add("leave");return leave;}
        public void enterBlock(IEnterableBlock e,com.hbm.util.fauxpointtwelve.BlockPos p,ForgeDirection d){entered=p;side=d;events.add("base-enter");}
        boolean walking(){return canTriggerWalking();}
    }
    static final class Player extends EntityPlayer {
        Player(World w){super(w,new GameProfile(new UUID(1,2),"moving-item-probe"));inventoryContainer=new Container(){public boolean canInteractWith(EntityPlayer p){return true;}public void detectAndSendChanges(){events.add("detect");super.detectAndSendChanges();}};}
        public boolean isSpectator(){return false;}public boolean isCreative(){return false;}
    }
    static class Enterable extends Block implements IEnterableBlock {
        boolean accept=true,throwCan,throwEnter; ForgeDirection side; BlockPos entered; IConveyorItem captured; int calls;
        Enterable(){super(Material.IRON);}
        void reset(){accept=true;throwCan=false;throwEnter=false;side=null;entered=null;captured=null;calls=0;}
        public boolean canItemEnter(World w,int x,int y,int z,ForgeDirection d,IConveyorItem item){events.add("can");calls++;check(!((Entity)item).isDead,"alive at can-enter");side=d;entered=new BlockPos(x,y,z);captured=item;if(throwCan)throw new IllegalStateException("can");return accept;}
        public void onItemEnter(World w,int x,int y,int z,ForgeDirection d,IConveyorItem item){events.add("enter");check(!((Entity)item).isDead,"alive at enter callback");check(entered.equals(new BlockPos(x,y,z))&&d==side&&item==captured,"entry arguments stable");if(throwEnter)throw new IllegalStateException("enter");}
        public boolean canPackageEnter(World w,int x,int y,int z,ForgeDirection d,IConveyorPackage item){throw new AssertionError("item must not dispatch package callback");}
        public void onPackageEnter(World w,int x,int y,int z,ForgeDirection d,IConveyorPackage item){throw new AssertionError("item must not dispatch package callback");}
    }
    static final class Belt extends Block implements IConveyorBelt {
        boolean stay=true; Vec3d target; int stayCalls,travelCalls; double speed;
        Belt(){super(Material.IRON);}
        void reset(){stay=true;target=null;stayCalls=travelCalls=0;speed=-1;}
        public boolean canItemStay(World w,int x,int y,int z,Vec3d p){events.add("stay");stayCalls++;check(new BlockPos(p).equals(new BlockPos(x,y,z)),"floor block coordinates supplied to stay");return stay;}
        public Vec3d getTravelLocation(World w,int x,int y,int z,Vec3d p,double s){events.add("travel");travelCalls++;speed=s;return target==null?p:target;}
        public Vec3d getClosestSnappingPosition(World w,int x,int y,int z,Vec3d p){throw new AssertionError("engine tick must not resnap");}
    }
    static Block register(String name,Block b){ForgeRegistries.BLOCKS.register(b.setRegistryName("engine_probe",name));return b;}
    @SuppressWarnings("unchecked") static void setup()throws Exception{
        Bootstrap.register();owners=new Block[]{new BlockConveyorBase(){},new BlockConveyorBendable(){},new BlockConveyor(),new BlockConveyorDouble(),new BlockConveyorTriple(),new BlockConveyorExpress(),new BlockConveyorLift(),new BlockConveyorChute(),new BlockConveyorClassic()};for(int i=0;i<owners.length;i++)register("owner"+i,owners[i]);ModBlocks.conveyor=owners[2];ModBlocks.conveyor_lift=owners[6];ModBlocks.conveyor_chute=owners[7];ModItems.conveyor_wand=new Item();
        belt=(Belt)register("belt",new Belt());entry=(Enterable)register("entry",new Enterable());lower=(Enterable)register("lower",new Enterable());
        java.lang.reflect.Field f=EntityMovingItem.class.getDeclaredField("ITEM");f.setAccessible(true);itemKey=(DataParameter<ItemStack>)f.get(null);f=EntityMovingItem.class.getDeclaredField("HAS_ITEM");f.setAccessible(true);presentKey=(DataParameter<Boolean>)f.get(null);
    }
    static ItemStack stack(){ItemStack s=new ItemStack(Items.STICK,37,3);NBTTagCompound n=new NBTTagCompound();NBTTagCompound inner=new NBTTagCompound();inner.setInteger("value",19);n.setTag("nested",inner);s.setTagCompound(n);return s;}
    static Moving item(NativeWorld w,double x,double y,double z){Moving m=new Moving(w);m.setItemStack(stack());m.setPosition(x,y,z);return m;}
    static void tick(Entity e){ticks++;e.onUpdate();}
    static void coordinateHelper(){
        for(double x:new double[]{-100.9,-1,-.999,-.001,0,.001,.999,1,100.9}){com.hbm.util.fauxpointtwelve.BlockPos p=new com.hbm.util.fauxpointtwelve.BlockPos(x,x,x);check(p.getX()==(int)Math.floor(x)&&p.getY()==(int)Math.floor(x)&&p.getZ()==(int)Math.floor(x),"real mod-owned helper floor");check(p.add(0,0,0)==p&&p.add(0.,0.,0.)==p,"zero offset aliases original");check(p.clone()!=p&&p.clone().equals(p),"clone identity/equality");check(p.mutate(1,2,3)==p&&p.compare(1,2,3),"mutable API preserved");check(p.offset(ForgeDirection.WEST).compare(0,2,3),"direction offset unchanged");}
        TileEntity te=new TileEntity(){};te.setPos(new BlockPos(-13,255,21));check(new com.hbm.util.fauxpointtwelve.BlockPos(te).compare(-13,255,21),"native tile position helper constructor");
        System.out.println("PASS complete real faux BlockPos helper: floor, mutation, offsets, clone, and native tile constructor.");
    }
    static void dataAndNbt()throws Exception{
        NativeWorld w=new NativeWorld(false);Moving m=new Moving(w);
        check(m.noClip&&m.width==.375F&&m.height==.375F,"native constructor size/noclip");check(m.getDataManager().get(itemKey)==ItemStack.EMPTY,"native empty initial registration");check(itemKey.getSerializer()==DataSerializers.ITEM_STACK,"native ITEM_STACK serializer");check(m.getItemStack().getItem()==Item.getItemFromBlock(Blocks.STONE)&&m.getItemStack().getCount()==1,"absent native stack fallback stone");check(m.getItemStack()!=m.getItemStack(),"fallback constructed each time as pristine");
        ItemStack s=stack();m.setItemStack(s);check(m.getItemStack()==s,"setter/getter preserve valid stack alias");m.getDataManager().setClean();s.setCount(23);m.setItemStack(s);check(m.getDataManager().isDirty(),"same-instance mutation still explicitly dirtied");List<EntityDataManager.DataEntry<?>> dirty=m.getDataManager().getDirty();check(dirty.size()==1&&dirty.get(0).getKey()==itemKey,"one allocated item key dirty");ItemStack copied=(ItemStack)dirty.get(0).getValue();check(copied!=s&&copied.getTagCompound()!=s.getTagCompound()&&ItemStack.areItemStacksEqual(copied,s),"native data-entry snapshot deep copy");check(!m.getDataManager().isDirty(),"native getDirty clears dirty flag");
        Moving client=new Moving(new NativeWorld(true));PacketBuffer buffer=new PacketBuffer(Unpooled.buffer());EntityDataManager.writeEntries(m.getDataManager().getAll(),buffer);client.getDataManager().setEntryValues(EntityDataManager.readEntries(buffer));buffer.release();check(ItemStack.areItemStacksEqual(client.getItemStack(),s)&&client.getItemStack()!=s,"native metadata packet ItemStack/NBT roundtrip");
        tick(client);EntityItem remoteCache=client.cacheForRender;ItemStack next=stack();next.setCount(7);next.getTagCompound().getCompoundTag("nested").setInteger("value",88);m.setItemStack(next);PacketBuffer update=new PacketBuffer(Unpooled.buffer());EntityDataManager.writeEntries(m.getDataManager().getDirty(),update);client.getDataManager().setEntryValues(EntityDataManager.readEntries(update));update.release();tick(client);check(client.cacheForRender==remoteCache&&ItemStack.areItemStacksEqual(remoteCache.getItem(),next),"native metadata receive refreshes existing render cache on next client tick");m.setItemStack(s);
        NBTTagCompound saved=m.writeItem();check(saved.hasKey("Item",10)&&saved.getKeySet().size()==1,"original Item compound schema only");Moving restored=new Moving(w);restored.readItem(serialized(saved));check(!restored.isDead&&ItemStack.areItemStacksEqual(restored.getItemStack(),s),"NBT valid stack roundtrip");check(restored.getItemStack()!=s&&restored.getItemStack().getTagCompound()!=s.getTagCompound(),"NBT isolated copy");
        for(int count:new int[]{0,-1}){NBTTagCompound invalid=saved.copy();invalid.getCompoundTag("Item").setByte("Count",(byte)count);Moving bad=new Moving(w);bad.readItem(invalid);check(bad.isDead&&bad.getDataManager().get(itemKey).isEmpty(),"nonpositive NBT raw stack killed despite fallback");}
        for(NBTTagCompound invalid:new NBTTagCompound[]{new NBTTagCompound(),unknownStack(),wrongType()}){Moving bad=new Moving(w);bad.readItem(invalid);check(bad.isDead&&bad.getDataManager().get(itemKey).isEmpty(),"missing/unknown/wrong-type NBT raw stack killed");}
        m.setItemStack(null);check(m.getDataManager().get(itemKey)==ItemStack.EMPTY&&m.getItemStack().getItem()==Item.getItemFromBlock(Blocks.STONE),"null setter normalized without serializer null");check(new ItemStack(m.writeItem().getCompoundTag("Item")).getItem()==Item.getItemFromBlock(Blocks.STONE),"uninitialized save retains original stone fallback");m.setItemStack(ItemStack.EMPTY);check(m.getItemStack()==ItemStack.EMPTY&&m.getDataManager().get(presentKey),"explicit native EMPTY remains present-empty, never stone");ItemStack zero=new ItemStack(Items.STICK,0);m.setItemStack(zero);check(m.getDataManager().get(itemKey)==zero&&zero.isEmpty()&&zero.getItem()==Items.AIR&&m.getItemStack()==zero,"present zero stack returned unchanged; native empty getItem is AIR");
        System.out.println("PASS real native ITEM_STACK registration, aliasing, same-instance forced dirty, deep snapshot/packet synchronization, and valid/invalid NBT schema.");
    }
    static NBTTagCompound serialized(NBTTagCompound n)throws java.io.IOException{java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();net.minecraft.nbt.CompressedStreamTools.writeCompressed(n,bytes);return net.minecraft.nbt.CompressedStreamTools.readCompressed(new java.io.ByteArrayInputStream(bytes.toByteArray()));}
    static NBTTagCompound unknownStack(){NBTTagCompound n=new NBTTagCompound(),s=new NBTTagCompound();s.setString("id","engine_probe:missing");s.setByte("Count",(byte)1);n.setTag("Item",s);return n;}
    static NBTTagCompound wrongType(){NBTTagCompound n=new NBTTagCompound();n.setString("Item","not a compound");return n;}
    static void transferConsumption()throws Exception{
        NativeWorld w=new NativeWorld(false);Moving server=item(w,8.5,64.5,8.5);Moving client=new Moving(new NativeWorld(true));
        transferMetadata(server,client,true);tick(client);check(ItemStack.areItemStacksEqual(server.getItemStack(),client.getItemStack()),"initial present stack packet");EntityItem cache=client.cacheForRender;server.getDataManager().setClean();
        ItemStack live=server.getItemStack();live.shrink(live.getCount());check(live.isEmpty(),"native transfer consumes aliased stack to zero");for(int i=0;i<4;i++)check(server.getItemStack()==live&&server.getItemStack().isEmpty(),"consumed present stack repeated getter cannot manufacture stone");server.setItemStack(live);transferMetadata(server,client,false);tick(client);check(client.getItemStack().isEmpty()&&client.getDataManager().get(presentKey)&&client.cacheForRender==cache&&cache.getItem().isEmpty(),"dirty metadata preserves present-empty and clears existing cache without stone");
        Moving newClient=new Moving(new NativeWorld(true));transferMetadata(server,newClient,true);tick(newClient);check(newClient.getItemStack().isEmpty()&&newClient.cacheForRender.getItem().isEmpty(),"initial full metadata preserves already-consumed empty stack");
        server.setItemStack(null);transferMetadata(server,client,false);tick(client);check(!client.getDataManager().get(presentKey)&&client.getItemStack().getItem()==Item.getItemFromBlock(Blocks.STONE)&&cache.getItem().getItem()==Item.getItemFromBlock(Blocks.STONE),"explicit null packet restores only missing-stack fallback");server.setItemStack(ItemStack.EMPTY);transferMetadata(server,client,false);tick(client);check(client.getDataManager().get(presentKey)&&client.getItemStack().isEmpty()&&cache.getItem().isEmpty(),"explicit empty packet distinguishes null from present-empty");
        for(int action=0;action<3;action++)for(int failure=0;failure<3;failure++){
            NativeWorld target=new NativeWorld(false);Moving consumed=item(target,8.5,64.5,8.5);ItemStack tracked=consumed.getItemStack();tracked.setCount(0);target.track(consumed,true);target.rejectSpawn=failure==1;target.throwSpawn=failure==2;boolean threw=false;
            if(action==2){Player p=new Player(target);events.clear();check(!consumed.processInitialInteract(p,EnumHand.MAIN_HAND)&&!consumed.isDead&&p.inventory.isEmpty()&&events.isEmpty(),"consumed-empty pickup cannot insert stone or kill");}
            else{try{if(action==0)consumed.hitByEntity(null);else consumed.onLeaveConveyor();}catch(IllegalStateException e){threw=true;}check(threw==(failure==2)&&consumed.isDead&&events.equals(Arrays.asList("dead","spawn")),"consumed-empty drop retains death/spawn failure order");check(((EntityItem)target.spawned.get(0)).getItem().isEmpty(),"consumed-empty hit/leave never spawns stone");}
            for(int i=0;i<3;i++)check(consumed.getItemStack()==tracked&&consumed.getItemStack().isEmpty(),"post-action consumed getter stays empty");
        }
        System.out.println("PASS reachable transfer-to-zero accounting: aliased mutation, repeated getters, hit/leave/pickup, empty cache, full/dirty metadata and null-versus-empty distinction.");
    }
    static void transferMetadata(Moving server,Moving client,boolean full)throws java.io.IOException{PacketBuffer b=new PacketBuffer(Unpooled.buffer());EntityDataManager.writeEntries(full?server.getDataManager().getAll():server.getDataManager().getDirty(),b);client.getDataManager().setEntryValues(EntityDataManager.readEntries(b));b.release();}
    static void interpolationAndCache(){
        for(boolean teleport:new boolean[]{false,true})for(int steps:new int[]{-3,-2,-1,0,1,3,10}){
            NativeWorld w=new NativeWorld(true);Moving m=item(w,1,2,3);m.rotationYaw=11;m.rotationPitch=12;m.setVelocity(.2,-.3,.4);m.motionX=9;m.motionY=9;m.motionZ=9;((Entity)m).setPositionAndRotationDirect(6,12,18,90,80,steps,teleport);num(m.motionX,.2,"sync restores cached velocity x");num(m.motionY,-.3,"sync restores cached velocity y");num(m.motionZ,.4,"sync restores cached velocity z");check(m.interpolation()==steps+2,"original plus-two interpolation delay");xyz(m,1,2,3,"sync schedules not teleports");
            int total=Math.max(steps+2,0);for(int i=1;i<=Math.max(total,1);i++){tick(m);double factor=total==0?0:(double)i/total;xyz(m,1+5*factor,2+10*factor,3+15*factor,"linear client interpolation");check(m.ticksExisted==0,"client never increments server-only ticks");check(m.rotationYaw==11&&m.rotationPitch==12,"original ignores packet rotation");check(m.cacheForRender!=null&&ItemStack.areItemStacksEqual(m.cacheForRender.getItem(),m.getItemStack()),"client cache populated after superclass tick");}EntityItem cache=m.cacheForRender;tick(m);check(cache==m.cacheForRender&&cache.ticksExisted==0,"cache reused not independently ticked");ItemStack equal=m.getItemStack().copy();ItemStack previous=cache.getItem();m.setItemStack(equal);tick(m);check(cache.getItem()==previous,"equal contents do not replace cache stack");ItemStack changed=stack();changed.setCount(2);m.setItemStack(changed);tick(m);check(cache==m.cacheForRender&&cache.getItem()==changed,"changed synchronized item updates same cached entity");check(w.spawned.isEmpty()&&!m.isDead,"client interpolation never spawns or leaves");
        }
        NativeWorld remote=new NativeWorld(true);Moving empty=new Moving(remote);tick(empty);check(empty.cacheForRender.getItem().getItem()==Item.getItemFromBlock(Blocks.STONE),"client initial empty uses stone render fallback");
        System.out.println("PASS native callback dispatch for interpolation, cached velocity, ignored rotation/teleport, client-only item cache identity/equality updates.");
    }
    static void delayAndLeave(){
        for(int failure=0;failure<3;failure++){
            NativeWorld w=new NativeWorld(false);Moving m=item(w,8.25,64.5,8.75);m.motionX=.2;m.motionY=-.3;m.motionZ=.4;w.track(m,true);w.rejectSpawn=failure==1;w.throwSpawn=failure==2;
            for(int i=1;i<=5;i++){tick(m);check(m.ticksExisted==i&&!m.isDead&&w.spawned.isEmpty(),"exact five server tick grace");xyz(m,8.25,64.5,8.75,"no grace movement");}boolean threw=false;try{tick(m);}catch(IllegalStateException e){threw=true;}check(threw==(failure==2),"spawn exception propagated");check(m.ticksExisted==6&&m.isDead&&events.equals(Arrays.asList("dead","spawn")),"sixth tick leaves/dead before spawn even rejection/exception");EntityItem drop=(EntityItem)w.spawned.get(0);xyz(drop,8.65,63.9,9.55,"drop forward projection");num(drop.motionX,.4,"drop doubled x");num(drop.motionY,.1,"drop fixed upward y");num(drop.motionZ,.8,"drop doubled z");check(drop.velocityChanged&&drop.getItem()==m.getItemStack(),"drop motion dirty and original stack alias");xyz(m,8.25,64.5,8.75,"leave early return prevents moving dead source");check(m.cacheForRender==null,"server no cache");events.clear();check(m.onLeaveConveyor()&&events.isEmpty(),"already dead leave guard no repeat drop");
        }
        NativeWorld w=new NativeWorld(false);BaseProbe p=new BaseProbe(w);p.setPosition(8.5,64.5,8.5);p.ticksExisted=5;p.motionX=.125;tick(p);check(p.leaveCalls==1&&p.posX==8.5,"base leave true halts");p.leave=false;tick(p);num(p.posX,8.625,"base leave false permits native movement");check(p.canBeCollidedWith()&&p.canBeAttackedWithItem()&&p.walking(),"base physical callbacks preserved");check(!p.hitByEntity(new EntityItem(w))&&!p.isDead,"base nonplayer hit survives");check(!p.hitByEntity(new Player(w))&&p.isDead,"base player hit kills");
        System.out.println("PASS exact <=5 direct-callback counter gate, leave/dead/spawn motion and failure order, base leave continuation and physical callbacks.");
    }
    static void worldDispatch(){
        for(boolean remote:new boolean[]{false,true}){
            NativeWorld w=new NativeWorld(remote);for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if(!w.chunks.containsKey(NativeWorld.key(x,z))){Chunk c=new Chunk(w,x,z);c.markLoaded(true);w.chunks.put(NativeWorld.key(x,z),c);}
            Moving m=item(w,8.5,64.5,8.5);w.getChunk(POS).addEntity(m);w.track(m,true);
            for(int i=1;i<=3;i++){w.updateEntityWithOptionalForce(m,true);check(m.ticksExisted==i*(remote?1:2),"native World counter increment plus preserved server engine increment");check(m.isDead==(!remote&&i==3),"native World dispatch leaves on third update, not sixth world tick");}
            check(w.spawned.size()==(remote?0:1),"native World dispatch drop side guard");
        }
        System.out.println("PASS real World.updateEntityWithOptionalForce dispatch: original counter increment retained, <=5 gate reached on third server world update.");
    }
    static void routing(){
        int[][] deltas={{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1},{1,1,0},{1,0,1},{1,1,1}};ForgeDirection[] dirs={ForgeDirection.WEST,ForgeDirection.EAST,ForgeDirection.DOWN,ForgeDirection.UP,ForgeDirection.NORTH,ForgeDirection.SOUTH,ForgeDirection.UNKNOWN,ForgeDirection.UNKNOWN,ForgeDirection.UNKNOWN};
        for(boolean negative:new boolean[]{false,true})for(int i=0;i<deltas.length;i++)for(boolean accept:new boolean[]{false,true}){
            NativeWorld w=new NativeWorld(false);BlockPos from=negative?new BlockPos(-8,64,-8):POS;int[] d=deltas[i];BlockPos to=from.add(d[0],d[1],d[2]);belt.reset();entry.reset();entry.accept=accept;w.setBlockState(from,belt.getDefaultState(),2);w.setBlockState(to,entry.getDefaultState(),2);Moving m=item(w,from.getX()+.5,from.getY()+.5,from.getZ()+.5);m.ticksExisted=5;belt.target=new Vec3d(to).add(.5,.5,.5);events.clear();tick(m);xyz(m,belt.target.x,belt.target.y,belt.target.z,"native noClip travel");check(m.isDead==accept&&entry.side==dirs[i]&&entry.entered.equals(to),"entry face opposite travel, diagonal unknown");check(entry.captured==m&&belt.stayCalls==1&&belt.travelCalls==1,"original item and single stay/travel callbacks");num(belt.speed,.0625,"base travel speed");num(m.motionX,d[0],"travel motion x");num(m.motionY,d[1],"travel motion y");num(m.motionZ,d[2],"travel motion z");check(events.equals(accept?Arrays.asList("stay","travel","can","enter","dead"):Arrays.asList("stay","travel","can")),"travel/entry/death exact event order");
        }
        for(Block obstacle:new Block[]{Blocks.AIR,Blocks.STONE,entry}){
            NativeWorld w=new NativeWorld(false);belt.reset();entry.reset();lower.reset();w.setBlockState(POS,belt.getDefaultState(),2);BlockPos to=POS.east();w.setBlockState(to,obstacle.getDefaultState(),2);w.setBlockState(to.down(),lower.getDefaultState(),2);Moving m=item(w,8.5,64.5,8.5);m.ticksExisted=5;belt.target=new Vec3d(9.5,64.5,8.5);tick(m);check(entry.calls==(obstacle==entry?1:0),"direct entry priority");check(lower.calls==(obstacle==Blocks.AIR?1:0),"falling only across nonsolid destination");if(obstacle==Blocks.AIR)check(lower.side==ForgeDirection.UP&&lower.entered.equals(to.down())&&m.isDead,"falling callback lower coordinate/up face");if(obstacle==Blocks.STONE)check(!m.isDead,"solid destination does not fall");
        }
        NativeWorld w=new NativeWorld(false);belt.reset();lower.reset();w.setBlockState(POS,belt.getDefaultState(),2);w.setBlockState(POS.down(),lower.getDefaultState(),2);Moving same=item(w,8.25,64.5,8.25);same.ticksExisted=5;belt.target=new Vec3d(8.75,64.5,8.75);tick(same);check(lower.calls==0&&!same.isDead,"same-cell travel never attempts falling");
        belt.reset();belt.stay=false;Moving reject=item(w,8.5,64.5,8.5);reject.ticksExisted=5;w.track(reject,true);tick(reject);check(reject.isDead&&belt.stayCalls==1&&belt.travelCalls==0&&events.equals(Arrays.asList("stay","dead","spawn")),"canItemStay rejection leaves before travel");
        Moving dead=item(w,8.5,64.5,8.5);dead.isDead=true;dead.ticksExisted=5;belt.reset();belt.target=new Vec3d(8.75,64.5,8.5);tick(dead);num(dead.posX,8.75,"no new already-dead movement guard added to base");
        for(boolean remote:new boolean[]{false,true})for(int mode=0;mode<4;mode++){
            NativeWorld direct=new NativeWorld(remote);Moving m=item(direct,8,64,8);entry.reset();entry.accept=mode!=1;entry.throwCan=mode==2;entry.throwEnter=mode==3;events.clear();boolean threw=false;try{m.enterBlock(entry,new com.hbm.util.fauxpointtwelve.BlockPos(2,3,4),ForgeDirection.SOUTH);}catch(IllegalStateException e){threw=true;}check(threw==(mode>=2)&&m.isDead==(mode==0),"entry denial/exception alive, no invented direct-call side guard");check(events.equals(mode==0?Arrays.asList("can","enter","dead"):mode==3?Arrays.asList("can","enter"):Arrays.asList("can")),"direct callback/death order");m.isDead=true;events.clear();m.enterBlock(entry,new com.hbm.util.fauxpointtwelve.BlockPos(2,3,4),ForgeDirection.SOUTH);check(events.isEmpty(),"dead item direct entry guard");
        }
        System.out.println("PASS native tick travel, all six entry faces and diagonal UNKNOWN, negative coordinates, falling priority/material/cell gates, denial and exception ordering.");
    }
    static void hitAndPickup(){
        for(boolean remote:new boolean[]{false,true})for(boolean alreadyDead:new boolean[]{false,true})for(boolean attack:new boolean[]{false,true})for(int failure=0;failure<3;failure++){
            NativeWorld w=new NativeWorld(remote);Moving m=item(w,8.25,64.5,8.75);m.isDead=alreadyDead;w.track(m,true);w.rejectSpawn=failure==1;w.throwSpawn=failure==2;boolean active=!remote&&!alreadyDead,threw=false;boolean result=false;try{result=attack?m.attackEntityFrom(DamageSource.GENERIC,0):m.hitByEntity(null);}catch(IllegalStateException e){threw=true;}check(threw==(active&&failure==2),"hit spawn exception guard");if(!threw)check(result==attack,"attack true and hit false legacy return");check(w.spawned.size()==(active?1:0)&&m.isDead==(alreadyDead||active),"hit server/alive guard");check(events.equals(active?Arrays.asList("dead","spawn"):Collections.emptyList()),"hit death before spawn even rejected");if(active){EntityItem drop=(EntityItem)w.spawned.get(0);xyz(drop,8.25,64.5,8.75,"hit drop exact coordinates");check(drop.getItem()==m.getItemStack(),"hit stack alias");}
        }
        NativeWorld damageWorld=new NativeWorld(false);Moving damaged=item(damageWorld,8,64,8);Player responsible=new Player(damageWorld);EntityItem projectile=new EntityItem(damageWorld);damageWorld.track(damaged,true);check(damaged.attackEntityFrom(new EntityDamageSourceIndirect("probe",projectile,responsible),99)&&damaged.lastAttacker==responsible,"native damage dispatch forwards true responsible entity, not projectile");
        for(EnumHand hand:EnumHand.values())for(boolean remote:new boolean[]{false,true})for(boolean dead:new boolean[]{false,true})for(int room:new int[]{0,1,37}){
            NativeWorld w=new NativeWorld(remote);Moving m=item(w,8,64,8);m.isDead=dead;ItemStack original=m.getItemStack();Player p=new Player(w);for(int i=0;i<p.inventory.mainInventory.size();i++)p.inventory.mainInventory.set(i,new ItemStack(Items.DIAMOND,64));if(room>0){ItemStack partial=stack();partial.setCount(64-room);p.inventory.mainInventory.set(0,partial);}w.track(m,false);events.clear();boolean result=((Entity)m).processInitialInteract(p,hand);boolean inserted=!remote&&!dead&&room>0;boolean accepted=!remote&&!dead&&room==37;check(!result&&m.isDead==(dead||accepted),"native hand callback false, kills on inventory true hand="+hand+" remote="+remote+" dead="+dead+" room="+room+" actualDead="+m.isDead);check(events.equals(accepted?Arrays.asList("detect","dead"):Collections.emptyList()),"inventory sync before death, no drop");check(w.spawned.isEmpty()&&original.getCount()==37&&original.getTagCompound().getCompoundTag("nested").getInteger("value")==19,"pickup passes copy, no source mutation/spawn");if(room>0)check(p.inventory.mainInventory.get(0).getCount()==(inserted?64:64-room),"native partial/full insertion behavior unchanged");
        }
        System.out.println("PASS hit/damage server/alive guards, success/rejected/throwing drop order, both native interaction hands, full/partial/no inventory capacity and copy semantics.");
    }
    static void realOwnerIntegration(){
        for(int i=0;i<owners.length;i++)for(int meta=0;meta<16;meta++)for(boolean negative:new boolean[]{false,true}){
            NativeWorld w=new NativeWorld(false);Block b=owners[i];BlockPos p=negative?new BlockPos(-8,64,-8):POS;w.setBlockState(p,b.getStateFromMeta(meta),2);w.setBlockState(p.down(),owners[2].getStateFromMeta(2),2);IConveyorBelt cb=(IConveyorBelt)b;
            EntityItem loose=new EntityItem(w,p.getX()+.2,p.getY()+.8,p.getZ()+.8,stack());loose.ticksExisted=11;Vec3d snap=cb.getClosestSnappingPosition(w,p.getX(),p.getY(),p.getZ(),new Vec3d(loose.posX,loose.posY,loose.posZ));w.track(loose,false);b.onEntityCollision(w,p,w.getBlockState(p),loose);check(loose.isDead&&w.spawned.size()==1&&w.spawned.get(0).getClass()==EntityMovingItem.class,"frozen owner creates actual complete production moving item");EntityMovingItem moving=(EntityMovingItem)w.spawned.get(0);xyz(moving,snap.x,snap.y,snap.z,"real owner snapping integration");check(moving.getItemStack()!=loose.getItem()&&ItemStack.areItemStacksEqual(moving.getItemStack(),loose.getItem()),"real owner deep-copies stack");w.original=null;w.spawned.clear();
            for(int tick=1;tick<=40&&!moving.isDead;tick++){
                double x=moving.posX,y=moving.posY,z=moving.posZ;BlockPos current=new BlockPos(x,y,z);Block at=w.getBlockState(current).getBlock();boolean stay=at instanceof IConveyorBelt&&((IConveyorBelt)at).canItemStay(w,current.getX(),current.getY(),current.getZ(),new Vec3d(x,y,z));Vec3d expected=tick<=5?new Vec3d(x,y,z):stay?((IConveyorBelt)at).getTravelLocation(w,current.getX(),current.getY(),current.getZ(),new Vec3d(x,y,z),moving.getMoveSpeed()):null;
                tick(moving);if(expected!=null){xyz(moving,expected.x,expected.y,expected.z,"complete owner travel engine integration");check(!moving.isDead,"native travel remains live without receivers");}else{check(moving.isDead&&w.spawned.size()==1&&w.spawned.get(0) instanceof EntityItem,"real engine leaves to native item after owner path");}check(moving.cacheForRender==null,"integrated server engine never builds client cache");
            }
        }
        for(Block b:owners)for(int failure=0;failure<3;failure++){
            NativeWorld w=new NativeWorld(false);w.setBlockState(POS,b.getStateFromMeta(2),2);EntityItem loose=new EntityItem(w,8.5,64.25,8.5,stack());loose.ticksExisted=11;w.track(loose,false);w.rejectSpawn=failure==1;w.throwSpawn=failure==2;boolean threw=false;try{b.onEntityCollision(w,POS,w.getBlockState(POS),loose);}catch(IllegalStateException e){threw=true;}check(threw==(failure==2)&&loose.isDead==(failure!=2),"real owner source death follows spawn attempt even failed");check(w.spawned.get(0).getClass()==EntityMovingItem.class,"real engine spawn attempted on failure");
        }
        System.out.println("PASS all nine frozen native owners x 16 metadata x positive/negative coordinates: actual loose-item conversion, complete engine ticks, travel/snap/leave and conversion failure ordering.");
    }
    public static void main(String[] args)throws Exception{setup();coordinateHelper();dataAndNbt();transferConsumption();interpolationAndCache();delayAndLeave();worldDispatch();routing();hitAndPickup();realOwnerIntegration();System.out.println("PASS "+checks+" assertions and "+ticks+" actual engine ticks on genuine native 1.12.2 classes.");}
}

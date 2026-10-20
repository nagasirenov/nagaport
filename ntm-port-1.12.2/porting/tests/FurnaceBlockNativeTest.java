import java.lang.reflect.Field;
import java.util.*;
import com.mojang.authlib.GameProfile;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineElectricFurnace;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

/** Complete block, no fake Minecraft classes or extracted production block methods. */
public final class FurnaceBlockNativeTest {
    static int checks;
    static final BlockPos POS=new BlockPos(8,64,8);
    static MachineElectricFurnace off,on;
    static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    static final class Particle {
        final EnumParticleTypes type; final double x,y,z,vx,vy,vz;
        Particle(EnumParticleTypes t,double a,double b,double c,double d,double e,double f){type=t;x=a;y=b;z=c;vx=d;vy=e;vz=f;}
    }
    static class NativeWorld extends World {
        final Map<Long,Chunk> chunks=new HashMap<>();
        final List<EntityItem> drops=new ArrayList<>();
        final List<Particle> particles=new ArrayList<>();
        final List<String> events=new ArrayList<>();
        boolean rejectDrops,failSet,removingTile; int comparatorCalls,explicitComparators;
        NativeWorld(boolean client) {
            super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),client);
            provider.setWorld(this);chunkProvider=createChunkProvider();
            // Real chunks are populated only in this bounded fixture, no generation/save/network.
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++) {
                Chunk chunk=new Chunk(this,x,z);chunk.markLoaded(true);chunks.put(key(x,z),chunk);
            }
        }
        static long key(int x,int z){return ((long)x<<32) ^ (z & 0xffffffffL);}
        @Override protected IChunkProvider createChunkProvider(){return new IChunkProvider(){
            public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}
            public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}
            public boolean tick(){return false;} public String makeString(){return "native furnace fixture";}
            public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}
        };}
        @Override protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return chunks.containsKey(key(x,z));}
        @Override public boolean checkLight(BlockPos pos){return true;}
        @Override public boolean setBlockState(BlockPos pos,IBlockState state,int flags){
            if(failSet)throw new IllegalStateException("deliberate native-world fixture failure");
            if(pos.equals(POS))events.add("set:"+state.getBlock().getTranslationKey()+":"+state.getBlock().getMetaFromState(state)+":"+flags);
            return super.setBlockState(pos,state,flags);
        }
        @Override public boolean spawnEntity(Entity entity){drops.add((EntityItem)entity);events.add("drop");return !rejectDrops;}
        @Override public void updateComparatorOutputLevel(BlockPos pos,Block block){
            comparatorCalls++;if(!removingTile)explicitComparators++;events.add(removingTile?"removal-comparator":"comparator");super.updateComparatorOutputLevel(pos,block);
        }
        @Override public void removeTileEntity(BlockPos pos){events.add("remove");removingTile=true;super.removeTileEntity(pos);removingTile=false;}
        @Override public void setTileEntity(BlockPos pos,TileEntity tile){events.add("tile:"+(tile==null?"null":tile.isInvalid()));super.setTileEntity(pos,tile);}
        @Override public void spawnParticle(EnumParticleTypes t,double x,double y,double z,double vx,double vy,double vz,int... args){particles.add(new Particle(t,x,y,z,vx,vy,vz));}
        void clearEvents(){drops.clear();particles.clear();events.clear();comparatorCalls=0;explicitComparators=0;}
        TileEntityMachineElectricFurnace furnace(){return (TileEntityMachineElectricFurnace)getTileEntity(POS);}
    }
    static final class CachedOpaqueBlock extends Block {
        private boolean changed;
        CachedOpaqueBlock(){super(Material.ROCK);changed=true;}
        @Override public boolean isOpaqueCube(IBlockState state){return !changed;}
    }
    static final class DynamicOpaqueBlock extends Block {
        private boolean changed;
        DynamicOpaqueBlock(){super(Material.ROCK);changed=true;}
        @Override public boolean isOpaqueCube(IBlockState state){return changed;}
    }
    static final class Player extends EntityPlayer {
        Player(World w){super(w,new GameProfile(new UUID(1,2),"furnace-probe"));}
        public boolean isSpectator(){return false;} public boolean isCreative(){return false;}
    }
    static Random blockRandom(MachineElectricFurnace b)throws Exception{
        Field f=MachineElectricFurnace.class.getDeclaredField("field_149933_a");f.setAccessible(true);return (Random)f.get(b);
    }
    static ItemStack stack(int count,boolean tag){
        ItemStack s=new ItemStack(Items.STICK,count,3);
        if(tag){NBTTagCompound n=new NBTTagCompound();n.setString("marker","last-count-retained");NBTTagCompound inner=new NBTTagCompound();inner.setInteger("nested",17);n.setTag("nested",inner);s.setTagCompound(n);}return s;
    }
    static void legacyDrop(NativeWorld w,ItemStack source,Random rand){
        if(source==null||source.isEmpty())return;
        Item item=source.getItem();int meta=source.getItemDamage(),left=source.getCount();NBTTagCompound tag=source.getTagCompound();
        float x=rand.nextFloat()*.8F+.1F,y=rand.nextFloat()*.8F+.1F,z=rand.nextFloat()*.8F+.1F;
        while(left>0){int count=rand.nextInt(21)+10;if(count>left)count=left;left-=count;source.setCount(left);
            EntityItem e=new EntityItem(w,POS.getX()+x,POS.getY()+y,POS.getZ()+z,new ItemStack(item,count,meta));
            if(tag!=null)e.getItem().setTagCompound(tag.copy());float motion=.05F;
            e.motionX=(float)rand.nextGaussian()*motion;e.motionY=(float)rand.nextGaussian()*motion+.2F;e.motionZ=(float)rand.nextGaussian()*motion;w.spawnEntity(e);
        }
    }
    static void registryAndStates(){
        check(off.getTranslationKey().equals("tile.machine_electric_furnace_off"),"legacy translation key/registry prefix");
        for(MachineElectricFurnace b:new MachineElectricFurnace[]{off,on})for(int meta=0;meta<16;meta++){
            IBlockState state=b.getStateFromMeta(meta);
            check(b.getMetaFromState(state)==meta,"all 16 metadata roundtrip");
            check(state.getMaterial()==Material.IRON&&state.getRenderType()==EnumBlockRenderType.MODEL,"iron/native MODEL");
            check(state.isOpaqueCube()&&state.isFullCube(),"unchanged opaque full block");
            check(b.getItemDropped(state,new Random(1),0)==Item.getItemFromBlock(off),"active/off drops off item");
            check(b.getPickBlock(state,null,null,POS,null).getItem()==Item.getItemFromBlock(off),"native pick delegates off item");

        }
    }
    static void placement(){
        for(int mask=0;mask<16;mask++){
            NativeWorld w=new NativeWorld(false);BlockPos[] faces={POS.north(),POS.south(),POS.west(),POS.east()};
            for(int f=0;f<4;f++)if((mask&(1<<f))!=0)w.setBlockState(faces[f],Blocks.STONE.getDefaultState());
            check(w.setBlockState(POS,off.getDefaultState()),"native World/Chunk initial placement");
            int expected=3;if((mask&1)!=0&&(mask&2)==0)expected=3;if((mask&2)!=0&&(mask&1)==0)expected=2;if((mask&4)!=0&&(mask&8)==0)expected=5;if((mask&8)!=0&&(mask&4)==0)expected=4;
            check(off.getMetaFromState(w.getBlockState(POS))==expected,"ordered neighbor orientation mask "+mask);
            check(w.furnace()!=null&&w.furnace().getWorld()==w&&w.furnace().getPos().equals(POS),"native tile creation/world/pos");
        }
        NativeWorld cachedWorld=new NativeWorld(false),dynamicWorld=new NativeWorld(false);
        Block cached=new CachedOpaqueBlock(),dynamic=new DynamicOpaqueBlock();
        net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(cached.setRegistryName("furnace_probe","cached_opaque"));
        net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(dynamic.setRegistryName("furnace_probe","dynamic_opaque"));
        check(cached.getDefaultState().isFullBlock()&&!cached.getDefaultState().isOpaqueCube(),"cached predicate differs from dynamic false opacity");
        check(!dynamic.getDefaultState().isFullBlock()&&dynamic.getDefaultState().isOpaqueCube(),"cached predicate differs from dynamic true opacity");
        cachedWorld.setBlockState(POS.south(),cached.getDefaultState());cachedWorld.setBlockState(POS,off.getDefaultState());
        dynamicWorld.setBlockState(POS.south(),dynamic.getDefaultState());dynamicWorld.setBlockState(POS,off.getDefaultState());
        check(off.getMetaFromState(cachedWorld.getBlockState(POS))==2,"legacy cached full-block predicate chooses north");
        check(off.getMetaFromState(dynamicWorld.getBlockState(POS))==3,"dynamic-only opacity ignored by legacy predicate");
        NativeWorld w=new NativeWorld(false);w.setBlockState(POS,off.getDefaultState());Player p=new Player(w);
        for(float yaw:new float[]{0,45,90,135,180,225,270,315,360,-45,-90,-180,720}){
            p.rotationYaw=yaw;ItemStack named=stack(1,true).setStackDisplayName("A named furnace");
            off.onBlockPlacedBy(w,POS,w.getBlockState(POS),p,named);
            int quadrant=((int)Math.floor(yaw*4.0F/360.0F+.5D))&3;
            check(off.getMetaFromState(w.getBlockState(POS))==new int[]{2,5,3,4}[quadrant],"yaw mapping "+yaw);
            check("A named furnace".equals(w.furnace().customName),"placement custom name");
        }
        NativeWorld client=new NativeWorld(true);client.setBlockState(POS,off.getStateFromMeta(11));off.onBlockAdded(client,POS,client.getBlockState(POS));
        check(off.getMetaFromState(client.getBlockState(POS))==11,"client added callback does not select direction");
    }
    static void swaps()throws Exception{
        for(boolean client:new boolean[]{false,true})for(int meta=0;meta<16;meta++){
            NativeWorld w=new NativeWorld(client);w.setBlockState(POS,off.getDefaultState());w.setBlockState(POS,off.getStateFromMeta(meta),2);
            TileEntityMachineElectricFurnace tile=w.furnace();ItemStack items=stack(64,true);tile.slots[0]=items;tile.setCustomName("preserved");
            for(int changed=0;changed<16;changed++){
                w.setBlockState(POS,off.getStateFromMeta(changed),2);
                check(w.furnace()==tile&&!tile.isInvalid()&&tile.slots[0]==items&&items.getCount()==64,"metadata-only edit retains tile and inventory");
            }
            w.setBlockState(POS,off.getStateFromMeta(meta),2);
            for(boolean processing:new boolean[]{true,false,true,true,false,false}){
                w.clearEvents();int invalidates=tile.invalidates;MachineElectricFurnace before=(MachineElectricFurnace)w.getBlockState(POS).getBlock();
                MachineElectricFurnace.updateBlockState(processing,w,POS.getX(),POS.getY(),POS.getZ());
                check(w.getBlockState(POS).getBlock()==(processing?on:off),"swap identity");
                check(((MachineElectricFurnace)w.getBlockState(POS).getBlock()).getMetaFromState(w.getBlockState(POS))==meta,"swap preserves every metadata");
                check(w.furnace()==tile&&!tile.isInvalid(),"original tile revalidated/restored");
                check(tile.slots[0]==items&&items.getCount()==64&&"preserved".equals(tile.customName),"contents/name/alias preserved");
                check(w.drops.isEmpty()&&w.explicitComparators==0,"no explicit break drops/comparator during on/off");
                check(w.events.get(w.events.size()-1).equals("tile:false"),"tile restored after metadata");
                if(before!=(processing?on:off))check(tile.invalidates>invalidates,"native replacement invalidated old tile before restoration");
            }
        }
    }
    static void drops()throws Exception{
        for(int count:new int[]{1,9,10,21,31,64,130})for(boolean tagged:new boolean[]{false,true})for(long seed:new long[]{0,1,42,971})for(boolean rejected:new boolean[]{false,true}){
            NativeWorld w=new NativeWorld(false),reference=new NativeWorld(false);w.setBlockState(POS,off.getDefaultState());
            TileEntityMachineElectricFurnace tile=w.furnace();ItemStack source=stack(count,tagged);NBTTagCompound sourceTag=source.getTagCompound();tile.slots[0]=source;tile.slots[1]=ItemStack.EMPTY;tile.slots[3]=stack(17,tagged);
            Random rng=blockRandom(off),expected=new Random(seed);rng.setSeed(seed);w.clearEvents();w.rejectDrops=rejected;
            legacyDrop(reference,stack(count,tagged),expected);legacyDrop(reference,stack(17,tagged),expected);
            check(w.setBlockToAir(POS),"native replacement invokes complete breakBlock");
            check(w.getTileEntity(POS)==null&&tile.isInvalid(),"super break native tile removal");
            check(source.isEmpty()&&tile.slots[3].isEmpty(),"source inventory depletion");
            check(w.explicitComparators==1&&w.comparatorCalls==2&&w.events.indexOf("comparator")>w.events.lastIndexOf("drop")&&w.events.indexOf("remove")>w.events.indexOf("comparator"),"drops then comparator then superclass removal");
            check(w.drops.size()==reference.drops.size()&&rng.nextLong()==expected.nextLong(),"complete lifecycle RNG/split parity even rejected spawn");
            int total=0;
            for(int i=0;i<w.drops.size();i++){
                EntityItem got=w.drops.get(i),want=reference.drops.get(i);total+=got.getItem().getCount();
                check(ItemStack.areItemStacksEqual(got.getItem(),want.getItem()),"drop item metadata NBT including last item");
                check(got.posX==want.posX&&got.posY==want.posY&&got.posZ==want.posZ,"legacy positions");
                check(got.motionX==want.motionX&&got.motionY==want.motionY&&got.motionZ==want.motionZ,"legacy motion");
                if(tagged)check(got.getItem().getTagCompound()!=sourceTag,"tag copied not shared");
            }
            check(total==count+17,"drop counts conserved");
        }
        NativeWorld empty=new NativeWorld(false);empty.setBlockState(POS,off.getDefaultState());empty.clearEvents();empty.setBlockToAir(POS);
        check(empty.drops.isEmpty()&&empty.explicitComparators==1&&empty.comparatorCalls==2&&empty.getTileEntity(POS)==null,"empty tile still comparator/removal");
        for(ItemStack absent:new ItemStack[]{null,ItemStack.EMPTY,stack(0,true),stack(-1,true)}){
            NativeWorld w=new NativeWorld(false);w.setBlockState(POS,off.getDefaultState());w.furnace().slots[0]=absent;
            Random rng=blockRandom(off);rng.setSeed(42);Random untouched=new Random(42);w.clearEvents();w.setBlockToAir(POS);
            check(w.drops.isEmpty()&&rng.nextLong()==untouched.nextLong(),"empty/invalid absent slots skip RNG and drops");
            check(w.explicitComparators==1&&w.comparatorCalls==2&&w.getTileEntity(POS)==null,"empty inventory still complete comparator/removal lifecycle");
        }
        NativeWorld missing=new NativeWorld(false);off.breakBlock(missing,POS,off.getDefaultState());
        check(missing.drops.isEmpty()&&missing.explicitComparators==0&&missing.comparatorCalls==1,"missing tile no comparator/drops");
    }
    static void particles(){
        for(MachineElectricFurnace b:new MachineElectricFurnace[]{off,on})for(int meta=0;meta<16;meta++)for(long seed:new long[]{0,1,42}){
            NativeWorld w=new NativeWorld(true);Random actual=new Random(seed),expected=new Random(seed);
            b.randomDisplayTick(b.getStateFromMeta(meta),w,POS,actual);
            float y=0,z=0;if(b==on){y=POS.getY()+0.0F+expected.nextFloat()*6.0F/16.0F;z=expected.nextFloat()*.6F-.3F;}
            int count=b==on&&meta>=2&&meta<=5?2:0;
            check(w.particles.size()==count&&actual.nextLong()==expected.nextLong(),"particle eligibility and RNG even invalid orientation");
            if(count!=0){float x=POS.getX()+.5F,zz=POS.getZ()+.5F,offset=.52F;
                if(meta==4){x-=offset;zz+=z;}else if(meta==5){x+=offset;zz+=z;}else if(meta==2){x+=z;zz-=offset;}else {x+=z;zz+=offset;}
                for(int i=0;i<2;i++){Particle p=w.particles.get(i);check(p.type==(i==0?EnumParticleTypes.SMOKE_NORMAL:EnumParticleTypes.FLAME),"smoke/flame order");check(p.x==x&&p.y==y&&p.z==zz&&p.vx==0&&p.vy==0&&p.vz==0,"particle exact offsets and speed");}
            }
        }
    }
    static void activationAndFailure()throws Exception{
        NativeWorld server=new NativeWorld(false),client=new NativeWorld(true);Player p=new Player(server);p.setSneaking(true);
        check(!off.onBlockActivated(server,POS,off.getDefaultState(),p,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0),"server sneaking declines");
        check(off.onBlockActivated(client,POS,off.getDefaultState(),p,EnumHand.OFF_HAND,EnumFacing.UP,0,0,0),"client consumes even sneaking");
        p.setSneaking(false);check(off.onBlockActivated(server,POS,off.getDefaultState(),p,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0),"missing server tile consumes without GUI");
        // Preserve original keepInventory failure behavior; intentionally no new try/finally fix.
        server.setBlockState(POS,off.getDefaultState());server.failSet=true;
        try{MachineElectricFurnace.updateBlockState(true,server,8,64,8);throw new AssertionError("expected failure");}catch(IllegalStateException expected){}
        Field keep=MachineElectricFurnace.class.getDeclaredField("keepInventory");keep.setAccessible(true);
        check(keep.getBoolean(null),"original failure leaves keepInventory true");keep.setBoolean(null,false);
    }
    public static void main(String[] args)throws Exception{
        Bootstrap.register();off=new MachineElectricFurnace(false);on=new MachineElectricFurnace(true);off.setTranslationKey("machine_electric_furnace_off");on.setTranslationKey("machine_electric_furnace_on");
        ModBlocks.machine_electric_furnace_off=off;ModBlocks.machine_electric_furnace_on=on;
        net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(off.setRegistryName(new ResourceLocation("hbm",off.getTranslationKey())));net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(on.setRegistryName(new ResourceLocation("hbm",on.getTranslationKey())));
        net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(new ItemBlock(off).setRegistryName(off.getRegistryName()));net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(new ItemBlock(on).setRegistryName(on.getRegistryName()));
        registryAndStates();placement();swaps();drops();particles();activationAndFailure();
        System.out.println("PASS: FurnaceBlockNativeTest "+checks+" checks; complete block, real World/Chunk lifecycle, controlled NTM tile/registry/GUI owner");
        System.out.println("NOT PROVEN: GUI opening, production tile tick in this probe, networking, live game/server/chunk reload/save, GL/render acceptance");
    }
}

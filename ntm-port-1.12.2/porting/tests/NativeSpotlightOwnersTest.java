import java.lang.reflect.Method;
import java.util.*;
import com.hbm.blocks.BlockEnums.LightType;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.*;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Lights;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityData;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.storage.*;

/** Complete production owners; genuine native World/Chunk callback and tile lifecycle. */
public final class NativeSpotlightOwnersTest {
    static int checks;
    static final BlockPos P=new BlockPos(8,64,8);
    static Spotlight[] lamps=new Spotlight[6];
    static SpotlightBeam beam;
    static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    static int meta(World w,BlockPos p){IBlockState s=w.getBlockState(p);return s.getBlock().getMetaFromState(s);}
    static int bits(World w,BlockPos p){return ((TileEntityData)w.getTileEntity(p)).metadata;}
    static boolean beamAt(World w,BlockPos p){return w.getBlockState(p).getBlock()==beam;}
    static final class Scheduled {final BlockPos pos;final Block block;final int delay;Scheduled(BlockPos p,Block b,int d){pos=p;block=b;delay=d;}}
    static class NativeWorld extends World {
        final Map<Long,Chunk> chunks=new HashMap<>();
        final List<EntityItem> drops=new ArrayList<>();
        final List<Scheduled> schedules=new ArrayList<>();
        final List<String> mutations=new ArrayList<>();
        BlockPos refuse;
        NativeWorld(boolean client){
            super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),client);
            provider.setWorld(this);chunkProvider=createChunkProvider();
            for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}
        }
        static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
        @Override protected IChunkProvider createChunkProvider(){return new IChunkProvider(){
            public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}
            public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}
            public boolean tick(){return false;}public String makeString(){return "native spotlight fixture";}
            public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}
        };}
        @Override protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return chunks.containsKey(key(x,z));}
        @Override public boolean checkLight(BlockPos pos){return true;}
        @Override public boolean setBlockState(BlockPos pos,IBlockState state,int flags){
            mutations.add(pos+":"+state.getBlock().getRegistryName()+":"+state.getBlock().getMetaFromState(state)+":"+flags);
            if(pos.equals(refuse))return false;
            return super.setBlockState(pos,state,flags);
        }
        @Override public void scheduleUpdate(BlockPos pos,Block block,int delay){schedules.add(new Scheduled(pos,block,delay));}
        @Override public boolean spawnEntity(Entity e){drops.add((EntityItem)e);return true;}
        void clear(){mutations.clear();schedules.clear();drops.clear();}
    }
    static void register(Block b,String name){net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(b.setRegistryName("spotlight_probe",name));net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));}
    static void metadataBoundsAndDrops()throws Exception {
        NativeWorld w=new NativeWorld(true);
        Method silk=Spotlight.class.getDeclaredMethod("getSilkTouchDrop",IBlockState.class);silk.setAccessible(true);
        for(int i=0;i<lamps.length;i++)for(int m=0;m<16;m++){
            Spotlight b=lamps[i];IBlockState s=b.getStateFromMeta(m);w.setBlockState(P,s,2);
            check(b.getMetaFromState(s)==m&&meta(w,P)==m,"native registry/chunk all16 "+i+"/"+m);
            check(b.getSoundType()==SoundType.STONE,"original default stone sound");
            check(b.getLightValue(s)==(b.isOn?15:0)&&b.getBlockHardness(s,w,P)==.5F,"on/off illumination and hardness");
            check(!s.isOpaqueCube()&&!s.isFullCube()&&s.getRenderType()==EnumBlockRenderType.MODEL,"native render/fullness");
            check(b.isPassable(w,P)&&s.getCollisionBoundingBox(w,P)==null,"walkthrough and no collision");
            check(s.getMapColor(w,P)==MapColor.AIR,"map transparent");
            ForgeDirection dir=ForgeDirection.getOrientation(m>>1);check(b.getDirection(m)==dir&&b.getDirection(w,8,64,8)==dir,"orientation including UNKNOWN");
            check(b.isBroken(m)==((m&1)!=0),"broken bit");
            float[] a=b.type==LightType.FLUORESCENT?new float[]{.5F,.5F,.1F}:b.type==LightType.HALOGEN?new float[]{.35F,.25F,.2F}:new float[]{.25F,.2F,.15F};
            if(dir==ForgeDirection.EAST||dir==ForgeDirection.WEST)a=new float[]{a[2],a[1],a[0]};
            else if(dir==ForgeDirection.UP||dir==ForgeDirection.DOWN)a=new float[]{a[1],a[2],a[0]};
            float[] o={.5F-dir.offsetX*(.5F-a[0]),.5F-dir.offsetY*(.5F-a[1]),.5F-dir.offsetZ*(.5F-a[2])};
            AxisAlignedBB bb=s.getBoundingBox(w,P);check(bb.equals(new AxisAlignedBB(o[0]-a[0],o[1]-a[1],o[2]-a[2],o[0]+a[0],o[1]+a[1],o[2]+a[2])),"exact legacy float bounds "+i+"/"+m);
            check(s.getSelectedBoundingBox(w,P).equals(bb.offset(P)),"native selection world offset");
            for(EnumFacing face:EnumFacing.values())check(s.getBlockFaceShape(w,P,face)==BlockFaceShape.UNDEFINED&&!b.isSideSolid(s,w,P,face),"no side attachment");
            Item expected=Item.getItemFromBlock(lamps[i&~1]);check(b.getItemDropped(s,new Random(0),99)==expected&&b.damageDropped(s)==0,"normal drop on variant zero meta");
            check(b.getItem(w,P,s).getItem()==expected&&b.getPickBlock(s,null,w,P,null).getItem()==expected&&((ItemStack)silk.invoke(b,s)).getItem()==expected,"pick/silk on variant");
            String asset=new String[]{"cage_lamp","fluorescent_lamp","flood_lamp"}[i/2];
            check(b.getModelResource().toString().equals("hbm:models/lights/"+asset+".obj"),"model resource");
            check(b.getModelTexture().toString().equals("hbm:blocks/"+asset+(b.isOn?"":"_off")),"texture asset");
            for(boolean disabled:new boolean[]{false,true})for(int rotation=0;rotation<4;rotation++){
                Spotlight.disableOnGeneration=disabled;
                check(b.transformMeta(m,rotation)==(INBTBlockTransformable.transformMetaDeco(m>>1,rotation)<<1)+(disabled?1:0),"structure direction/broken transformation");
                check(b.transformBlock(b)==(disabled?lamps[i|1]:b),"structure identity transformation");
            }
        }
        Spotlight.disableOnGeneration=true;
        for(int m=0;m<16;m++){
            IBlockState s=beam.getStateFromMeta(m);w.setBlockState(P,s,2);
            check(beam.getMetaFromState(s)==m&&meta(w,P)==m,"beam native all16");
            check(s.getRenderType()==EnumBlockRenderType.INVISIBLE&&!s.isOpaqueCube()&&!s.isFullCube(),"invisible non-full beam");
            check(beam.isAir(s,w,P)&&beam.isLeaves(s,w,P)&&beam.canBeReplacedByLeaves(s,w,P),"air/leaves/tree replacement callbacks");
            check(s.getCollisionBoundingBox(w,P)==null&&s.getBoundingBox(w,P).equals(new AxisAlignedBB(0,0,0,0,0,0)),"beam zero selection/no collision");
            check(beam.getDrops(w,P,s,0).isEmpty()&&beam.quantityDropped(new Random())==0,"no beam drops");
            check(beam.getLightValue(s)==15&&beam.getLightOpacity(s)==0&&beam.getBlockHardness(s,w,P)==-1,"beam light/opacity/unbreakable");
        }
        System.out.println("PASS metadata all16, native bounds/selection/collision/passability/attachment, drops/pick/silk, light/sound and generation transforms.");
    }
    static void placementAndModular(){
        NativeWorld w=new NativeWorld(false);
        for(Spotlight lamp:lamps)for(EnumFacing face:EnumFacing.values()){
            check(lamp.getMetaFromState(lamp.getStateForPlacement(w,P,face,.2F,.7F,.9F,15,null))==face.getIndex()*2,"placement side and broken reset");
            BlockPos support=P.offset(face.getOpposite());
            check(!lamp.canPlaceBlockOnSide(w,P,face),"air rejects support");
            w.setBlockState(support,Blocks.STONE.getDefaultState(),2);check(lamp.canPlaceBlockOnSide(w,P,face),"stone support");
            w.setBlockState(P,Blocks.STONE.getDefaultState(),2);check(!lamp.canPlaceBlockOnSide(w,P,face),"occupied target rejected by superclass");w.setBlockToAir(P);
            for(Block slab:new Block[]{Blocks.STONE_SLAB,Blocks.WOODEN_SLAB,Blocks.DOUBLE_STONE_SLAB,Blocks.DOUBLE_WOODEN_SLAB})for(int m=0;m<16;m++){
                w.setBlockState(support,slab.getStateFromMeta(m),2);
                boolean expected=((BlockSlab)slab).isDouble()||face==((m&8)==0?EnumFacing.DOWN:EnumFacing.UP);
                check(lamp.canPlaceBlockOnSide(w,P,face)==expected,"exact slab half/double support "+slab+"/"+m+"/"+face);
            }
            w.setBlockToAir(support);
        }
        NativeWorld client=new NativeWorld(true);
        SpotlightModular fluoro=(SpotlightModular)lamps[2];
        for(Spotlight target:lamps)for(int m=0;m<16;m++){
            client.setBlockState(P,target.getStateFromMeta(m),2);
            check(fluoro.canConnectTo(client,8,64,8)==(target==fluoro),"modular same-instance only independent of metadata");
        }
        check(fluoro.getPartName(0).equals("FluoroSingle")&&fluoro.getPartName(1).equals("FluoroCap")&&fluoro.getPartName(2).equals("FluoroMid"),"modular groups");
        System.out.println("PASS side placement, native superclass replaceability, all slab variants/halves/directions and modular identity.");
    }
    static void directionBitsAndTile(){
        NativeWorld w=new NativeWorld(false);w.setBlockState(P,beam.getStateFromMeta(2),2);TileEntityData tile=(TileEntityData)w.getTileEntity(P);
        check(tile!=null&&tile.getWorld()==w&&tile.getPos().equals(P),"actual production tile created by native Chunk");
        for(int bits=0;bits<64;bits++)for(ForgeDirection direction:ForgeDirection.VALID_DIRECTIONS)for(boolean on:new boolean[]{false,true}){
            tile.metadata=bits;int expected=on?bits|direction.flag:bits&~direction.flag;
            check(SpotlightBeam.applyDirection(bits,direction,on)==expected,"direction bit pure");
            check(SpotlightBeam.setDirection(w,8,64,8,direction,on)==expected&&tile.metadata==expected&&meta(w,P)==2,"direction tile mutation distinct block color");
            List<ForgeDirection> dirs=SpotlightBeam.getDirections(w,8,64,8);int accumulated=0;for(ForgeDirection d:dirs)accumulated|=d.flag;check(accumulated==expected,"direction decode order/bits");
        }
        tile.metadata=63;NBTTagCompound nbt=tile.writeToNBT(new NBTTagCompound());TileEntityData read=new TileEntityData();read.readFromNBT(nbt);check(read.metadata==63&&nbt.getInteger("meta")==63,"production TileEntityData full six-bit NBT roundtrip");
        check(SpotlightBeam.getDirections(w,9,64,8).isEmpty()&&SpotlightBeam.setDirection(w,9,64,8,ForgeDirection.EAST,true)==0,"missing tile cancels");
        w.setBlockState(P,beam.getStateFromMeta(2),2);check(w.getTileEntity(P)==tile&&tile.metadata==63,"unchanged beam state retains actual tile bits");
        w.setBlockState(P,beam.getStateFromMeta(15),2);check(w.getTileEntity(P)!=tile&&tile.isInvalid()&&bits(w,P)==0,"external metadata mutation exposes unchanged shared TileEntityData refresh boundary");
        System.out.println("PASS six-bit combinations in actual TileEntityData, native placement/unchanged-state retention, NBT and missing tile cancellation.");
    }
    static void beamPropagation(){
        for(ForgeDirection d:ForgeDirection.VALID_DIRECTIONS)for(int length:new int[]{0,1,2,8,32})for(int color:new int[]{0,1,2,15}){
            NativeWorld w=new NativeWorld(false);Spotlight.propagateBeam(w,8,64,8,d,length,color);
            for(int distance=1;distance<=Math.max(1,length);distance++){
                BlockPos at=P.add(d.offsetX*distance,d.offsetY*distance,d.offsetZ*distance);boolean expected=distance<length;
                check(beamAt(w,at)==expected,"exact length-1 beam extent "+d+"/"+length+"/"+distance);
                if(expected)check(meta(w,at)==color&&bits(w,at)==d.flag,"beam color/incoming flag");
            }
            Spotlight.unpropagateBeam(w,8,64,8,d);for(int n=1;n<length;n++)check(!beamAt(w,P.add(d.offsetX*n,d.offsetY*n,d.offsetZ*n)),"recursive removal extent");
        }
        NativeWorld w=new NativeWorld(false);Spotlight.propagateBeam(w,4,64,8,ForgeDirection.EAST,9,1);Spotlight.propagateBeam(w,8,64,4,ForgeDirection.SOUTH,9,2);
        check(bits(w,P)==(ForgeDirection.EAST.flag|ForgeDirection.SOUTH.flag)&&meta(w,P)==1,"crossing merges incoming bits and retains first color");
        Spotlight.unpropagateBeam(w,4,64,8,ForgeDirection.EAST);check(beamAt(w,P)&&bits(w,P)==ForgeDirection.SOUTH.flag&&meta(w,P)==1,"crossing survives first source removal retaining color");
        check(!beamAt(w,P.west())&&!beamAt(w,P.east())&&beamAt(w,P.north())&&beamAt(w,P.south()),"crossing rays removed independently");
        Spotlight.unpropagateBeam(w,8,64,4,ForgeDirection.SOUTH);check(!beamAt(w,P),"last ray removes crossing");
        NativeWorld blocked=new NativeWorld(false);blocked.setBlockState(P.east(3),Blocks.GLASS.getDefaultState(),2);Spotlight.propagateBeam(blocked,8,64,8,ForgeDirection.EAST,8,0);
        check(beamAt(blocked,P.east(2))&&!beamAt(blocked,P.east(4))&&blocked.getBlockState(P.east(3)).getBlock()==Blocks.GLASS,"any non-air block cuts off including transparent glass");
        NativeWorld refused=new NativeWorld(false);refused.refuse=P.east();Spotlight.propagateBeam(refused,8,64,8,ForgeDirection.EAST,8,0);check(!beamAt(refused,P.east())&&!beamAt(refused,P.east(2)),"failed beam placement aborts through missing tile");
        System.out.println("PASS recursive exact-length six-direction beams, colors, crossings/independent removal, glass cutoff and failed placement cancellation.");
    }
    static void lifecycleAndPower(){
        for(int i=0;i<lamps.length;i+=2)for(EnumFacing facing:EnumFacing.values()){
            Spotlight on=lamps[i],off=lamps[i+1];NativeWorld w=new NativeWorld(false);BlockPos support=P.offset(facing.getOpposite());w.setBlockState(support,Blocks.STONE.getDefaultState(),2);
            w.setBlockState(P,off.getStateFromMeta(facing.getIndex()*2),2);check(w.getBlockState(P).getBlock()==on,"native off added immediately powers on");check(beamAt(w,P.offset(facing)),"added generates beam");
            w.clear();w.setBlockState(support,Blocks.REDSTONE_BLOCK.getDefaultState(),3);check(w.isBlockPowered(P),"real native redstone power");
            check(w.getBlockState(P).getBlock()==on&&!w.schedules.isEmpty(),"power schedules delayed off without immediate swap");
            for(Scheduled scheduled:w.schedules)check(scheduled.pos.equals(P)&&scheduled.block==on&&scheduled.delay==4,"scheduled original four ticks");
            on.updateTick(w,P,w.getBlockState(P),new Random(0));check(w.getBlockState(P).getBlock()==off&&meta(w,P)==facing.getIndex()*2,"native delayed callback swap metadata");
            check(!beamAt(w,P.offset(facing)),"native break callback unpropagates old on beam");
            w.setBlockState(support,Blocks.STONE.getDefaultState(),3);check(w.getBlockState(P).getBlock()==on&&beamAt(w,P.offset(facing)),"native unpower neighbor immediate on/repropagation");
            TileEntity old=w.getTileEntity(P.offset(facing));w.setBlockToAir(P);check(!beamAt(w,P.offset(facing))&&old.isInvalid(),"source destruction recursively removes native tiles");
        }
        NativeWorld w=new NativeWorld(false);Spotlight on=lamps[2],off=lamps[3];w.setBlockState(P.west(),Blocks.STONE.getDefaultState(),2);w.setBlockState(P,on.getStateFromMeta(10),2);
        w.setBlockState(P.east(3),Blocks.STONE.getDefaultState(),3);check(beamAt(w,P.east(2))&&!beamAt(w,P.east(4)),"native beam break cutoff callback");
        w.setBlockToAir(P.east(3));check(beamAt(w,P.east(3))&&beamAt(w,P.east(7))&&!beamAt(w,P.east(8)),"native neighbor callback back-propagates to actual source");
        w.clear();on.neighborChanged(w.getBlockState(P),w,P,Blocks.AIR,P.west());on.neighborChanged(w.getBlockState(P),w,P,beam,P.east());check(w.mutations.isEmpty()&&w.schedules.isEmpty(),"original AIR and beam neighbor ignore guards");
        w.setBlockState(P.west(),Blocks.AIR.getDefaultState(),2);w.clear();on.neighborChanged(w.getBlockState(P),w,P,Blocks.STONE,P.west());
        check(w.getBlockState(P).getBlock()==Blocks.AIR&&!beamAt(w,P.east())&&w.drops.size()==1&&w.drops.get(0).getItem().getItem()==Item.getItemFromBlock(on)&&w.drops.get(0).getItem().getMetadata()==0,"support loss drops on item meta0 and clears source/beam");
        for(int m=0;m<16;m++){
            NativeWorld powered=new NativeWorld(false);powered.setBlockState(P.west(),Blocks.REDSTONE_BLOCK.getDefaultState(),2);powered.setBlockState(P,off.getStateFromMeta(m),2);
            check(powered.getBlockState(P).getBlock()==off&&meta(powered,P)==m,"all metadata powered off stays off");
            powered.setBlockState(P,on.getStateFromMeta(m),2);check(powered.schedules.isEmpty()==((m&1)==1),"broken bit bypasses added power scheduling");
            on.updateTick(powered,P,powered.getBlockState(P),new Random());check(powered.getBlockState(P).getBlock()==off&&meta(powered,P)==m,"tick intentionally does not bypass broken bit");
        }
        NativeWorld pulse=new NativeWorld(false);pulse.setBlockState(P.west(),Blocks.REDSTONE_BLOCK.getDefaultState(),2);pulse.setBlockState(P,on.getStateFromMeta(10),2);pulse.setBlockState(P.west(),Blocks.STONE.getDefaultState(),2);on.updateTick(pulse,P,pulse.getBlockState(P),new Random());check(pulse.getBlockState(P).getBlock()==on,"power vanished before tick keeps on");
        NativeWorld client=new NativeWorld(true);client.setBlockState(P,off.getStateFromMeta(10),2);client.clear();off.onBlockAdded(client,P,client.getBlockState(P));off.neighborChanged(client.getBlockState(P),client,P,Blocks.STONE,P.west());on.updateTick(client,P,on.getStateFromMeta(10),new Random());check(client.mutations.isEmpty()&&client.schedules.isEmpty()&&!beamAt(client,P.east()),"remote guards no gameplay mutation");
        System.out.println("PASS native World/Chunk on/off/break/neighbor dispatch, real redstone, delay4 callback recording, support drops, cutoff/repropagation and remote/broken guards.");
    }
    static void repair(){
        for(boolean client:new boolean[]{false,true})for(boolean hasTrait:new boolean[]{false,true}){
            NativeWorld w=new NativeWorld(client);Spotlight on=lamps[2],off=lamps[3];
            BlockPos[] positions={P,P.up(),P.up().north(),P.up().north().east()};
            for(int j=0;j<positions.length;j++)w.setBlockState(positions[j],off.getStateFromMeta(13),2);
            w.setBlockState(P.south(),lamps[1].getStateFromMeta(13),2);
            CelestialBody body=CelestialBody.getBody(w);if(hasTrait)body.trait=new CBT_Lights();CBT_Lights existing=body.trait;
            w.clear();check(off.onBlockActivated(w,P,w.getBlockState(P),null,EnumHand.OFF_HAND,EnumFacing.DOWN,.2F,.4F,.7F),"broken activation handled without consumption");
            for(BlockPos pos:positions)check(w.getBlockState(pos).getBlock()==on&&meta(w,pos)==12,"same-identity recursive repaired cluster");
            check(w.getBlockState(P.south()).getBlock()==lamps[1]&&meta(w,P.south())==13,"different type not repaired");
            if(client)check(body.lookups==0&&body.modifications==0&&body.trait==existing,"client repair mutates blocks but no celestial side effects");
            else {
                check(body.lookups==positions.length&&body.modifications==positions.length&&body.trait.positions.size()==positions.length,"server celestial lookup/add/modify once each repaired lamp");
                check(body.trait.positions.equals(Arrays.asList(positions)),"recursive celestial notification order");
                for(Block b:body.trait.blocks)check(b==on,"celestial receives on block");
                if(hasTrait)check(body.trait==existing,"existing celestial trait retained");
            }
            w.clear();check(!on.onBlockActivated(w,P,w.getBlockState(P),null,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0)&&w.mutations.isEmpty(),"intact activation returns false no work");
        }
        // Broken ON blocks recurse using the original same-instance predicate and clear each bit.
        NativeWorld w=new NativeWorld(false);Spotlight on=lamps[0];w.setBlockState(P,on.getStateFromMeta(13),2);w.setBlockState(P.up(),on.getStateFromMeta(13),2);on.onBlockActivated(w,P,w.getBlockState(P),null,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0);check(meta(w,P)==12&&meta(w,P.up())==12,"same on-instance broken cluster terminates after bit cleared");
        System.out.println("PASS recursive repair same-block identity, original client behavior, no item consumption and bounded celestial trait side-effect calls/order.");
    }
    public static void main(String[] args)throws Exception {
        Bootstrap.register();
        for(int i=0;i<lamps.length;i++){LightType type=LightType.values()[i/2];lamps[i]=type==LightType.FLUORESCENT?new SpotlightModular(Material.IRON,8,type,(i&1)==0):new Spotlight(Material.IRON,i<2?2:32,type,(i&1)==0);register(lamps[i],"lamp_"+i);}
        ModBlocks.spotlight_incandescent=lamps[0];ModBlocks.spotlight_incandescent_off=lamps[1];ModBlocks.spotlight_fluoro=lamps[2];ModBlocks.spotlight_fluoro_off=lamps[3];ModBlocks.spotlight_halogen=lamps[4];ModBlocks.spotlight_halogen_off=lamps[5];
        beam=new SpotlightBeam();register(beam,"beam");ModBlocks.spotlight_beam=beam;TileEntity.register("spotlight_probe_data",TileEntityData.class);
        metadataBoundsAndDrops();placementAndModular();directionBitsAndTile();beamPropagation();lifecycleAndPower();repair();
        System.out.println("PASS NativeSpotlightOwnersTest: "+checks+" checks with complete production owners and genuine native World/Chunk.");
    }
}

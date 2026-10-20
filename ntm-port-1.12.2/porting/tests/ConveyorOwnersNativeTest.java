import api.hbm.block.IToolable;
import api.hbm.block.IToolable.ToolType;
import api.hbm.conveyor.*;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.*;
import com.hbm.conveyorprobe.Events;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.storage.*;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.*;

/** Complete production owners on native World/Chunk/BlockState/Entity/ItemStack, not a GL or entity-tick test. */
public final class ConveyorOwnersNativeTest {
    static int checks, movements;
    static final BlockPos POS = new BlockPos(8,64,8);
    static Block[] blocks;
    static Block enterable,both;
    static LegacyConveyorOwnerOracle.OracleBase[] oracles;
    static LegacyConveyorOwnerOracle.OracleClassic classicOracle=new LegacyConveyorOwnerOracle.OracleClassic();
    static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    static void number(double actual,double expected,String label){check(Double.doubleToLongBits(actual)==Double.doubleToLongBits(expected)||Math.abs(actual-expected)<1E-12,label+" expected="+expected+" actual="+actual);}
    static void vector(Vec3d actual,LegacyConveyorOwnerOracle.MutableVec expected,String label){number(actual.x,expected.xCoord,label+" x");number(actual.y,expected.yCoord,label+" y");number(actual.z,expected.zCoord,label+" z");}
    static LegacyConveyorOwnerOracle.MutableVec mutable(Vec3d v){return new LegacyConveyorOwnerOracle.MutableVec(v.x,v.y,v.z);}
    static final class NativeWorld extends World {
        final Map<Long,Chunk> chunks=new HashMap<>();
        final List<Entity> spawned=new ArrayList<>();
        final List<Integer> flags=new ArrayList<>();
        final List<IBlockState> states=new ArrayList<>();
        Entity original; boolean rejectSpawn,throwSpawn;
        NativeWorld(boolean client){
            super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),client);
            provider.setWorld(this);chunkProvider=createChunkProvider();
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}
        }
        static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
        protected IChunkProvider createChunkProvider(){return new IChunkProvider(){
            public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}
            public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}
            public boolean tick(){return false;}public String makeString(){return "native conveyor fixture";}
            public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}
        };}
        protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return chunks.containsKey(key(x,z));}
        public boolean checkLight(BlockPos p){return true;}
        public boolean setBlockState(BlockPos p,IBlockState state,int f){if(p.equals(POS)){flags.add(f);states.add(state);}return super.setBlockState(p,state,f);}
        public boolean spawnEntity(Entity e){check(original==null||!original.isDead,"original remains alive until spawn attempt");spawned.add(e);Events.events.add("spawn");if(throwSpawn)throw new IllegalStateException("fixture spawn failure");return !rejectSpawn;}
        void clear(){spawned.clear();flags.clear();states.clear();Events.events.clear();original=null;rejectSpawn=false;throwSpawn=false;}
    }
    static final class Player extends EntityPlayer {
        Player(World w){super(w,new GameProfile(new UUID(1,2),"conveyor-probe"));}
        public boolean isSpectator(){return false;}public boolean isCreative(){return false;}
    }
    static class Enterable extends Block implements IEnterableBlock {
        Enterable(){super(Material.IRON);}
        public boolean canItemEnter(World w,int x,int y,int z,ForgeDirection d,IConveyorItem e){return true;}
        public void onItemEnter(World w,int x,int y,int z,ForgeDirection d,IConveyorItem e){}
        public boolean canPackageEnter(World w,int x,int y,int z,ForgeDirection d,IConveyorPackage e){return true;}
        public void onPackageEnter(World w,int x,int y,int z,ForgeDirection d,IConveyorPackage e){}
    }
    static final class Both extends Enterable implements IConveyorBelt {
        public boolean canItemStay(World w,int x,int y,int z,Vec3d v){return true;}
        public Vec3d getTravelLocation(World w,int x,int y,int z,Vec3d v,double s){return v;}
        public Vec3d getClosestSnappingPosition(World w,int x,int y,int z,Vec3d v){return v;}
    }
    static final class ObservedItem extends EntityItem {
        ObservedItem(World w,Vec3d p,ItemStack s){super(w,p.x,p.y,p.z,s);}
        public void setDead(){Events.events.add("dead");super.setDead();}
    }
    static Block register(String name,Block b){ForgeRegistries.BLOCKS.register(b.setRegistryName("conveyor_probe",name));return b;}
    static void setup(){
        Bootstrap.register();
        blocks=new Block[]{new BlockConveyorBase(){},new BlockConveyorBendable(){},new BlockConveyor(),new BlockConveyorDouble(),new BlockConveyorTriple(),new BlockConveyorExpress(),new BlockConveyorLift(),new BlockConveyorChute(),new BlockConveyorClassic()};
        for(int i=0;i<blocks.length;i++)register("owner"+i,blocks[i]);
        ModBlocks.conveyor=blocks[2];ModBlocks.conveyor_lift=blocks[6];ModBlocks.conveyor_chute=blocks[7];
        ModItems.conveyor_wand=new Item().setHasSubtypes(true).setRegistryName("conveyor_probe","wand");ForgeRegistries.ITEMS.register(ModItems.conveyor_wand);
        ItemBlock classicItem=new ItemBlock(blocks[8]);classicItem.setRegistryName(blocks[8].getRegistryName());ForgeRegistries.ITEMS.register(classicItem);try{java.lang.reflect.Field f=Item.class.getDeclaredField("BLOCK_TO_ITEM");f.setAccessible(true);((Map<Block,Item>)f.get(null)).put(blocks[8],classicItem);}catch(ReflectiveOperationException e){throw new AssertionError(e);}
        enterable=register("enterable",new Enterable());both=register("both",new Both());
        oracles=new LegacyConveyorOwnerOracle.OracleBase[]{new LegacyConveyorOwnerOracle.OracleBase(){},new LegacyConveyorOwnerOracle.OracleBendable(){},new LegacyConveyorOwnerOracle.OracleStandard(),new LegacyConveyorOwnerOracle.OracleDouble(),new LegacyConveyorOwnerOracle.OracleTriple(),new LegacyConveyorOwnerOracle.OracleExpress(),new LegacyConveyorOwnerOracle.OracleLift(),new LegacyConveyorOwnerOracle.OracleChute()};
    }
    static void statesAndBounds(){
        NativeWorld w=new NativeWorld(false);
        for(int i=0;i<blocks.length;i++){
            Block b=blocks[i];check(b.getDefaultState().getValue(BlockConveyorBase.METADATA)==0,"default meta zero "+i);
            check(b.getSoundType()==SoundType.STONE,"original default stone sound type "+i);
            number(b.getSoundType().getVolume(),1,"original standard stone sound volume");number(b.getSoundType().getPitch(),1,"original standard stone sound pitch");

            check(b.getBlockState().getValidStates().size()==16,"exactly sixteen listed states "+i);
            check(b.getBlockState().getProperties().size()==1,"single listed metadata property "+i);
            for(int meta=0;meta<16;meta++){
                IBlockState state=b.getStateFromMeta(meta);check(b.getMetaFromState(state)==meta,"metadata nibble roundtrip "+i+"/"+meta);
                check(state.getMaterial()==Material.IRON&&state.getRenderType()==EnumBlockRenderType.MODEL,"native material MODEL");
                check(!state.isOpaqueCube()&&!state.isFullCube()&&b.getRenderLayer()==BlockRenderLayer.CUTOUT_MIPPED,"nonopaque nonfull cutout mipped");
                check(((IExtendedBlockState)state).getValue(ConveyorModelState.NEIGHBORS)==null,"clean listed state no neighbor snapshot");
                check(b.getStateFromMeta(meta+16)==state&&b.getStateFromMeta(meta-16)==state,"nibble mask compatible with saved metadata");
                if(i>=2&&i<=7){check(b.getItemDropped(state,new Random(1),0)==ModItems.conveyor_wand,"wand drop");check(b.damageDropped(state)==(i==3?2:i==4?3:i==5?1:0),"wand drop metadata");}
                if(i==8){check(b.getItemDropped(state,new Random(1),0)==Item.getItemFromBlock(b),"Classic native self drop");check(b.damageDropped(state)==0,"Classic zero damage");}
                if(i>=2){NonNullList<ItemStack> drops=NonNullList.create();b.getDrops(drops,w,POS,state,3);check(drops.size()==1&&drops.get(0).getCount()==1,"native getDrops count");check(drops.get(0).getItem()==(i==8?Item.getItemFromBlock(b):ModItems.conveyor_wand)&&drops.get(0).getItemDamage()==b.damageDropped(state),"native getDrops identity/metadata");}

            }
            w.setBlockState(POS,b.getStateFromMeta(2),2);
            for(Block below:new Block[]{Blocks.AIR,blocks[2],enterable,both})for(Block above:new Block[]{Blocks.AIR,blocks[2],enterable,both}){
                w.setBlockState(POS.down(),below.getDefaultState(),2);w.setBlockState(POS.up(),above.getDefaultState(),2);
                IBlockState state=w.getBlockState(POS);boolean top=below instanceof IConveyorBelt&&!(above instanceof IConveyorBelt)&&!(above instanceof IEnterableBlock);
                double height=i==7?1:i==6?(top?.5:1):.25;
                AxisAlignedBB expected=new AxisAlignedBB(0,0,0,1,height,1);
                check(state.getBoundingBox(w,POS).equals(expected),"local selection bounds "+i);
                check(state.getCollisionBoundingBox(w,POS).equals(expected),"local collision bounds "+i);
                check(state.getSelectedBoundingBox(w,POS).equals(expected.offset(POS)),"world translated selection bounds "+i);
                List<AxisAlignedBB> boxes=new ArrayList<>();state.addCollisionBoxToList(w,POS,new AxisAlignedBB(POS).grow(.1),boxes,null,false);
                check(boxes.size()==1&&boxes.get(0).equals(expected.offset(POS)),"native translated collision dispatch "+i);
                if(i==6||i==7)for(EnumFacing f:EnumFacing.values())check(b.shouldSideBeRendered(state,w,POS,f),"vertical owner never side culls");
            }
        }
        BlockConveyorClassic c=(BlockConveyorClassic)blocks[8];boolean failed=false;try{c.getModelTexture();}catch(IllegalStateException e){failed=e.getMessage().contains("explicit");}check(failed,"Classic never guesses material");
        failed=false;try{c.setModelTexture(null);}catch(NullPointerException e){failed=true;}check(failed,"Classic rejects null material");ResourceLocation texture=new ResourceLocation("hbm","blocks/conveyor");check(c.setModelTexture(texture)==c&&c.getModelTexture()==texture,"Classic explicit fluent atlas material");
        System.out.println("PASS all nine native listed-state/render/drop/bounds contracts, including full Classic owner.");
    }
    static void faceSupport(){
        NativeWorld w=new NativeWorld(false);BlockFence fence=(BlockFence)Blocks.OAK_FENCE;
        for(int i=0;i<blocks.length;i++)for(int meta=0;meta<16;meta++){
            Block b=blocks[i];IBlockState state=b.getStateFromMeta(meta);w.setBlockState(POS,state,2);
            for(EnumFacing face:EnumFacing.values()){
                check(b.getBlockFaceShape(w,state,POS,face)==BlockFaceShape.UNDEFINED,"owner undefined support face "+i+"/"+meta+"/"+face);
                check(state.getBlockFaceShape(w,POS,face)==BlockFaceShape.UNDEFINED,"native state dispatch undefined support face");
                check(!state.isSideSolid(w,POS,face),"native side solidity remains false");
                check(!fence.canConnectTo(w,POS,face),"native fence cannot connect to conveyor face");
                if(face!=EnumFacing.DOWN)check(!Blocks.TORCH.canPlaceBlockAt(w,POS.offset(face)),"native torch cannot attach above or beside conveyor");
            }
            check(!state.isTopSolid()&&!state.isNormalCube(),"full-height collision never implies top or normal solidity");
            check(!b.canPlaceTorchOnTop(state,w,POS),"native conveyor torch-top support remains false");
        }
        w.setBlockState(POS,Blocks.STONE.getDefaultState(),2);
        for(EnumFacing face:EnumFacing.values()){
            check(fence.canConnectTo(w,POS,face),"positive stone control native fence connection");
            if(face!=EnumFacing.DOWN)check(Blocks.TORCH.canPlaceBlockAt(w,POS.offset(face)),"positive stone control native torch placement");
        }
        check(Blocks.STONE.canPlaceTorchOnTop(Blocks.STONE.getDefaultState(),w,POS),"positive stone control torch-top callback");
        System.out.println("PASS all owner/metadata/faces UNDEFINED, native side/top solidity and fence/torch attachment rejection, with stone positive controls.");
    }
    static void placementAndScrew(){
        NativeWorld w=new NativeWorld(false);Player p=new Player(w);
        for(Block b:blocks)for(float yaw:new float[]{-720,-361,-315,-225,-180,-135,-90,-45,-44.99F,0,44.99F,45,90,135,180,225,270,315,360,720}){
            w.setBlockState(POS,b.getStateFromMeta(15),2);w.clear();p.rotationYaw=yaw;b.onBlockPlacedBy(w,POS,w.getBlockState(POS),p,ItemStack.EMPTY);
            int expected=new int[]{2,5,3,4}[(int)Math.floor(yaw*4.0F/360.0F+.5D)&3];check(w.getBlockState(POS).getBlock()==b&&b.getMetaFromState(w.getBlockState(POS))==expected,"placement quadrant");check(w.flags.equals(Collections.singletonList(2)),"placement flag2 exactly once");
        }
        for(int i=1;i<=7;i++)for(int meta=0;meta<16;meta++)for(boolean sneak:new boolean[]{false,true}){
            Block b=blocks[i];IToolable tool=(IToolable)b;p.setSneaking(sneak);
            for(ToolType type:ToolType.values()){
                w.setBlockState(POS,b.getStateFromMeta(meta),2);w.clear();boolean handled=tool.onScrew(w,p,POS.getX(),POS.getY(),POS.getZ(),0,.2F,.4F,.7F,type);
                if(type!=ToolType.SCREWDRIVER){check(!handled&&w.states.isEmpty(),"other tool no mutation");continue;}
                int path=meta>=6&&meta<=9?1:meta>=10&&meta<=13?2:0;int expected=meta;Block target=b;
                if(!sneak){int primary=meta;if(primary>9)primary-=8;if(primary>5)primary-=4;expected=ForgeDirection.getOrientation(primary).getRotation(ForgeDirection.UP).ordinal()+(i<6?path*4:0);}
                else if(i==6)target=blocks[7];else if(i==7)target=blocks[2];else if(path<2)expected+=4;else{expected-=8;if(i==2)target=blocks[6];}
                check(handled&&w.flags.equals(Collections.singletonList(3)),"screw one native set flag3");check(w.getBlockState(POS).getBlock()==target&&target.getMetaFromState(w.getBlockState(POS))==(expected&15),"screw direction/path/replacement "+i+"/"+meta+"/"+sneak);
            }
        }
        System.out.println("PASS native placement yaw boundaries and all tool/path/sneak replacement combinations, flags2/3 preserved.");
    }
    static void movement(){
        NativeWorld w=new NativeWorld(false);LegacyConveyorOwnerOracle.OracleWorld ow=new LegacyConveyorOwnerOracle.OracleWorld(w);
        double[][] points={{.5,.25,.5},{-.4,.25,1.4},{1.4,.25,-.4},{0,0,0},{1,1,1},{.35,.25,.65},{.3500001,.25,.6499999},{.3499999,.25,.6500001},{.25,.7,.75},{.75,.1,.25},{.5,1.7,.5}};
        Block[] neighbors={Blocks.AIR,blocks[2],enterable,both};
        for(int i=0;i<blocks.length;i++)for(int meta=0;meta<16;meta++){
            Block b=blocks[i];IConveyorBelt belt=(IConveyorBelt)b;w.setBlockState(POS,b.getStateFromMeta(meta),2);
            for(Block below:neighbors)for(Block above:neighbors){
                w.setBlockState(POS.down(),below.getDefaultState(),2);w.setBlockState(POS.up(),above.getDefaultState(),2);
                if(i<8){check(((BlockConveyorBase)b).getInputDirection(w,8,64,8)==oracles[i].getInputDirection(ow,8,64,8),"input direction "+i+"/"+meta);check(((BlockConveyorBase)b).getOutputDirection(w,8,64,8)==oracles[i].getOutputDirection(ow,8,64,8),"output direction "+i+"/"+meta);}
                for(double[] q:points){
                    Vec3d v=new Vec3d(8+q[0],64+q[1],8+q[2]);String label=i+"/"+meta+"/"+Arrays.toString(q);check(belt.canItemStay(w,8,64,8,v),"stay always true");
                    vector(belt.getClosestSnappingPosition(w,8,64,8,v),i<8?oracles[i].getClosestSnappingPosition(ow,8,64,8,mutable(v)):classicOracle.getClosestSnappingPosition(ow,8,64,8,mutable(v)),"pristine snap "+label);
                    if(i<8)check(((BlockConveyorBase)b).getTravelDirection(w,8,64,8,v)==oracles[i].getTravelDirection(ow,8,64,8,mutable(v)),"pristine travel direction "+label);
                    for(double speed:new double[]{0,.01,.1,.5}){
                        LegacyConveyorOwnerOracle.MutableVec expected=i<8?oracles[i].getTravelLocation(ow,8,64,8,mutable(v),speed):classicOracle.getTravelLocation(ow,8,64,8,mutable(v),speed);
                        vector(belt.getTravelLocation(w,8,64,8,v,speed),expected,"pristine mutable snap movement "+label+"/"+speed);movements++;
                    }
                    check(v.x==8+q[0]&&v.y==64+q[1]&&v.z==8+q[2],"native input Vec3d remains immutable");
                }
            }
        }
        System.out.println("PASS "+movements+" complete-owner travel comparisons against pristine mutable-vector movement bodies, all 16 metadata and vertical conveyor/enterable combinations.");
    }
    static void negativeAndHeightEdges(){
        NativeWorld w=new NativeWorld(false);LegacyConveyorOwnerOracle.OracleWorld ow=new LegacyConveyorOwnerOracle.OracleWorld(w);
        for(int i=0;i<blocks.length;i++)for(int y:new int[]{0,255})for(int meta:new int[]{2,5,6,9,10,13,15}){
            Block b=blocks[i];BlockPos p=new BlockPos(-8,y,-8);w.setBlockState(p,b.getStateFromMeta(meta),2);w.setBlockState(p.down(),blocks[2].getDefaultState(),2);w.setBlockState(p.up(),Blocks.AIR.getDefaultState(),2);
            for(double h:new double[]{.1,.25,.2500001,.7}){
                Vec3d v=new Vec3d(-8.4,y+h,-6.6);LegacyConveyorOwnerOracle.MutableVec expected=i<8?oracles[i].getTravelLocation(ow,-8,y,-8,mutable(v),.1):classicOracle.getTravelLocation(ow,-8,y,-8,mutable(v),.1);
                vector(((IConveyorBelt)b).getTravelLocation(w,-8,y,-8,v,.1),expected,"negative coordinate/height-edge pristine movement "+i+"/"+y+"/"+meta);movements++;
            }
            if(i==6&&y==255){check(((BlockConveyorLift)b).getTravelDirection(w,-8,y,-8,new Vec3d(-7.5,y+.25,-7.5))==ForgeDirection.getOrientation(meta),"lift y255 gameplay top ignores render-only max-height gate");number(b.getCollisionBoundingBox(b.getStateFromMeta(meta),w,p).maxY,.5,"lift y255 collision half height");}
        }
        System.out.println("PASS negative-coordinate/y0/y255 movement and lift max-height gameplay/render predicate separation.");
    }
    static ItemStack taggedStack(){ItemStack s=new ItemStack(Items.STICK,37,3);NBTTagCompound n=new NBTTagCompound();NBTTagCompound inner=new NBTTagCompound();inner.setInteger("value",19);n.setTag("nested",inner);s.setTagCompound(n);return s;}
    static void conversion(){
        for(Block b:blocks)for(boolean remote:new boolean[]{false,true}){
            NativeWorld w=new NativeWorld(remote);w.setBlockState(POS,b.getStateFromMeta(6),2);w.setBlockState(POS.down(),blocks[2].getDefaultState(),2);
            for(int age:new int[]{0,10,11})for(boolean dead:new boolean[]{false,true}){
                ObservedItem source=new ObservedItem(w,new Vec3d(8.1,64.8,8.9),taggedStack());source.ticksExisted=age;source.isDead=dead;w.clear();w.original=source;b.onEntityCollision(w,POS,w.getBlockState(POS),source);
                boolean expected=!remote&&age>10&&!dead;check(w.spawned.size()==(expected?1:0),"server/age>10/alive collision conversion guards");check(source.isDead==(dead||expected),"conversion death guard");
            }
            Player player=new Player(w);player.ticksExisted=100;w.clear();b.onEntityCollision(w,POS,w.getBlockState(POS),player);check(w.spawned.isEmpty()&&!player.isDead,"non-item guard");
            if(remote)continue;
            for(boolean reject:new boolean[]{false,true}){
                ItemStack input=taggedStack();ObservedItem source=new ObservedItem(w,new Vec3d(7.2,64.8,9.2),input);source.ticksExisted=11;w.clear();w.original=source;w.rejectSpawn=reject;
                Vec3d snap=((IConveyorBelt)b).getClosestSnappingPosition(w,8,64,8,new Vec3d(source.posX,source.posY,source.posZ));b.onEntityCollision(w,POS,w.getBlockState(POS),source);
                check(Events.events.equals(Arrays.asList("construct","stack","position","spawn","dead")),"exact construct-copy-position-spawn-death ordering, including failed spawn");
                EntityMovingItem moving=(EntityMovingItem)w.spawned.get(0);check(source.isDead&&moving.stack!=input&&ItemStack.areItemStacksEqual(moving.stack,input),"deep stack copy and original killed even rejected spawn");check(moving.stack.getTagCompound()!=input.getTagCompound(),"NBT copy not alias");
                number(moving.posX,snap.x,"converted snap x");number(moving.posY,snap.y,"converted snap y");number(moving.posZ,snap.z,"converted snap z");check(moving.rotationYaw==0&&moving.rotationPitch==0,"converted zero rotation");
                moving.stack.setCount(1);moving.stack.getTagCompound().getCompoundTag("nested").setInteger("value",0);check(input.getCount()==37&&input.getTagCompound().getCompoundTag("nested").getInteger("value")==19,"conversion preserves original count and nested tags");
            }
            ObservedItem source=new ObservedItem(w,new Vec3d(8.5,64.25,8.5),taggedStack());source.ticksExisted=11;w.clear();w.original=source;w.throwSpawn=true;boolean thrown=false;try{b.onEntityCollision(w,POS,w.getBlockState(POS),source);}catch(IllegalStateException e){thrown=true;}check(thrown&&!source.isDead&&!Events.events.contains("dead"),"spawn exception preserves original post-call death ordering");
        }
        System.out.println("PASS native entity guards, real ItemStack/NBT copy, snapping, and spawn success/reject/exception ordering.");
    }
    static void extendedStates(){
        NativeWorld w=new NativeWorld(false);
        for(Block b:blocks){
            w.setBlockState(POS,b.getStateFromMeta(13),2);
            for(int mask=0;mask<64;mask++){
                for(EnumFacing face:EnumFacing.values())w.setBlockState(POS.offset(face),(mask&(1<<face.getIndex()))!=0?blocks[face.getIndex()+2].getDefaultState():Blocks.AIR.getDefaultState(),2);
                IBlockState ext=b.getExtendedState(w.getBlockState(POS),w,POS);check(ConveyorModelState.neighbors(ext)==(mask|768),"six independent native neighbor belt bits");check(b.getMetaFromState(ext)==13,"snapshot preserves metadata");check(((IExtendedBlockState)ext).getClean()==b.getStateFromMeta(13),"extended clean native state identity");
            }
            w.setBlockState(POS.down(),enterable.getDefaultState(),2);w.setBlockState(POS.up(),both.getDefaultState(),2);
            for(EnumFacing f:EnumFacing.HORIZONTALS)w.setBlockState(POS.offset(f),Blocks.AIR.getDefaultState(),2);
            int mask=ConveyorModelState.neighbors(b.getExtendedState(b.getStateFromMeta(5),w,POS));check(mask==(768|ConveyorModelState.BELOW_ENTERABLE|ConveyorModelState.ABOVE_ENTERABLE|2),"enterable independent from belt classification");
            for(int y:new int[]{0,255}){BlockPos edge=new BlockPos(8,y,8);w.setBlockState(edge,b.getStateFromMeta(2),2);IBlockState ext=b.getExtendedState(b.getStateFromMeta(2),w,edge);int bits=ConveyorModelState.neighbors(ext);check(((bits&ConveyorModelState.ABOVE_MIN_Y)!=0)==(y>0),"renderer minimum height gate");check(((bits&ConveyorModelState.BELOW_MAX_Y)!=0)==(y<255),"renderer maximum height gate");}
        }
        System.out.println("PASS native extended-state snapshots, clean identity, all 64 neighbor masks, enterable distinctions and y0/y255 render bounds.");
    }
    public static void main(String[] args){setup();statesAndBounds();faceSupport();placementAndScrew();movement();negativeAndHeightEdges();conversion();extendedStates();System.out.println("PASS conveyor owners: "+checks+" assertions; "+movements+" travel vectors.");}
}

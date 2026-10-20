import java.util.*;import java.io.*;import java.lang.reflect.*;import java.nio.*;
import com.hbm.blocks.ModBlocks;import com.hbm.blocks.machine.*;import com.hbm.items.ModItems;import com.hbm.items.machine.*;import com.hbm.tileentity.machine.*;import com.hbm.inventory.material.*;import com.hbm.inventory.material.Mats.MaterialStack;import com.hbm.inventory.RecipesCommon;import com.hbm.util.*;import com.hbm.util.i18n.*;import com.hbm.main.*;import com.hbm.render.block.nativeblock.*;import com.hbm.render.tileentity.*;import api.hbm.block.IToolable;import com.hbm.lib.ForgeDirection;import com.hbm.inventory.OreDictManager.DictFrame;
import com.mojang.authlib.GameProfile;import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.block.state.*;import net.minecraft.client.*;import net.minecraft.client.renderer.*;import net.minecraft.client.renderer.block.model.*;import net.minecraft.client.renderer.texture.*;import net.minecraft.client.renderer.vertex.*;import net.minecraft.client.resources.*;import net.minecraft.client.resources.data.MetadataSerializer;import net.minecraft.entity.*;import net.minecraft.entity.item.*;import net.minecraft.entity.player.*;import net.minecraft.init.*;import net.minecraft.item.*;import net.minecraft.nbt.*;import net.minecraft.profiler.*;import net.minecraft.tileentity.*;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.world.*;import net.minecraft.world.chunk.*;import net.minecraft.world.storage.*;import net.minecraftforge.common.property.*;import net.minecraftforge.client.event.*;import net.minecraftforge.client.model.*;import net.minecraftforge.common.model.*;import net.minecraftforge.fml.common.*;import net.minecraftforge.fml.common.registry.*;import org.lwjgl.opengl.*;import org.lwjgl.BufferUtils;
public class NativeFoundryBlocksTest {
 static int checks,faces;static File resources;static final BlockPos P=new BlockPos(4,20,4);static FoundryBlock[] owners;static NTMMaterial iron;static ItemMold molds;
 static void check(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}static void close(double a,double b,String m){check(Math.abs(a-b)<.000004,m+" actual="+a+" expected="+b);}
 static void set(Object o,Class<?> c,String n,Object v)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);f.set(o,v);}static Object get(Object o,Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static class Player extends EntityPlayer {Player(World world){super(world,new GameProfile(new UUID(1,2),"foundry-owner-probe"));}public boolean isSpectator(){return false;}public boolean isCreative(){return false;}}
 static class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();final List<String> notifications=new ArrayList<>();int dirty,renders,scheduled;boolean powered;final List<Entity> spawned=new ArrayList<>();final List<SoundEvent> sounds=new ArrayList<>();
  NativeWorld(boolean client){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),client);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  @Override protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "foundry native chunks, no generation";}public boolean isChunkGeneratedAt(int x,int z){return chunks.containsKey(key(x,z));}};}
  @Override protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return chunks.containsKey(key(x,z));}
  @Override public boolean checkLight(BlockPos pos){return true;}
  @Override public void notifyBlockUpdate(BlockPos pos,IBlockState oldState,IBlockState newState,int flags){notifications.add(pos+":"+flags);super.notifyBlockUpdate(pos,oldState,newState,flags);}
  @Override public void markBlockRangeForRenderUpdate(int x1,int y1,int z1,int x2,int y2,int z2){renders++;super.markBlockRangeForRenderUpdate(x1,y1,z1,x2,y2,z2);}
  @Override public void markChunkDirty(BlockPos pos,TileEntity tile){dirty++;super.markChunkDirty(pos,tile);}
  @Override public void scheduleUpdate(BlockPos pos,Block block,int delay){check(block==ModBlocks.slag&&delay==1,"slag preserves exact one-tick block schedule");scheduled++;}
  @Override public boolean isBlockPowered(BlockPos pos){return powered;}
  @Override public boolean spawnEntity(Entity entity){spawned.add(entity);return true;}
  @Override public void playSound(EntityPlayer player,double x,double y,double z,SoundEvent event,SoundCategory category,float volume,float pitch){check(category==SoundCategory.PLAYERS&&volume==1&&pitch==1,"original insertion sound category and gain");sounds.add(event);}
  void clear(){notifications.clear();dirty=0;renders=0;scheduled=0;spawned.clear();sounds.clear();}
  <T extends TileEntity>T put(BlockPos pos,Block owner){setBlockState(pos,owner.getDefaultState(),3);return (T)getTileEntity(pos);}
 }

 static void setup()throws Exception {
  Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());ModMetadata metadata=new ModMetadata();metadata.modId="hbm";metadata.name="Native foundry owner proof";metadata.version="1";Loader.instance().setupTestHarness(new DummyModContainer(metadata));
  owners=new FoundryBlock[]{new FoundryBasin(),new FoundryMold(),new FoundryChannel(),new FoundryTank(),new FoundryOutlet(),new FoundrySlagtap()};String[] names={"basin","mold","channel","tank","outlet","slagtap"};
  Map<Block,Item> items=(Map<Block,Item>)get(null,Item.class,"BLOCK_TO_ITEM");
  for(int i=0;i<6;i++){FoundryBlock b=owners[i];b.setRegistryName("hbm","tile.foundry_"+names[i]);ForgeRegistries.BLOCKS.register(b);ModBlocks.class.getField("foundry_"+names[i]).set(null,b);ItemBlock item=new ItemBlock(b);item.setRegistryName(b.getRegistryName());ForgeRegistries.ITEMS.register(item);items.put(b,item);TileEntity.register("hbm:probe_foundry_"+names[i],b.createNewTileEntity(null,0).getClass());}
  iron=new NTMMaterial(11,new DictFrame("ProbeIron")).setMoltenColor(0xEAB167).smeltable(NTMMaterial.SmeltingBehavior.SMELTABLE);
  molds=new ItemMold();molds.setRegistryName("hbm","probe_mold");ForgeRegistries.ITEMS.register(molds);ModItems.mold=molds;ModItems.scraps=new Item().setRegistryName("hbm","probe_scraps");ForgeRegistries.ITEMS.register(ModItems.scraps);
  molds.molds.put(0,new ItemMold.Mold(0,72,iron,new ItemStack(Items.IRON_INGOT)));molds.molds.put(1,new ItemMold.Mold(1,648,iron,new ItemStack(Blocks.IRON_BLOCK)));
 }
 static void states() {
  NativeWorld w=new NativeWorld(false);Player p=new Player(w);
  for(int k=0;k<6;k++)for(int m=0;m<16;m++){
   FoundryBlock b=owners[k];IBlockState s=b.getStateFromMeta(m);check(b.getMetaFromState(s)==m&&b.getStateFromMeta(m+16)==s,"lossless native metadata nibble");check(s.getMaterial()==Material.ROCK&&s.getRenderType()==EnumBlockRenderType.MODEL&&!s.isOpaqueCube()&&!s.isFullCube(),"native nonopaque foundry declaration");check(b.getRenderLayer()==BlockRenderLayer.CUTOUT_MIPPED,"original pass0 mipmapped atlas sampling");
   w.setBlockState(P,s,2);TileEntity old=w.getTileEntity(P);w.setBlockState(P,b.getStateFromMeta((m+1)&15),2);check(w.getTileEntity(P)==old,"actual Chunk same owner metadata retains tile");w.setBlockState(P,s,2);
   for(EnumFacing f:EnumFacing.values()){boolean solid=k==0||k==3?f!=EnumFacing.UP:k==1?f==EnumFacing.DOWN:false;check(s.isSideSolid(w,P,f)==solid&&s.getBlockFaceShape(w,P,f)==(solid?BlockFaceShape.SOLID:BlockFaceShape.UNDEFINED),"original per-face support");check(b.shouldSideBeRendered(s,w,P,f),"housing controls original face suppression itself");}
   List<AxisAlignedBB> boxes=new ArrayList<>();s.addCollisionBoxToList(w,P,new AxisAlignedBB(P).grow(.2),boxes,null,false);check(boxes.size()==(k<=1?5:k==2?1:k==3?1:m>=2&&m<=5?1:0),"actual component collision count "+k+"/"+m);
   if(k<=1){double h=k==0?1:.5;check(boxes.get(0).equals(new AxisAlignedBB(4,20,4,5,20.125,5)),"original solid bottom collision");close(boxes.get(1).maxY,20+h,"original rim height");close(s.getBoundingBox(w,P).maxY,k==0?(double).999F:.5,"selection height distinction");}
  }
  for(int k=4;k<6;k++)for(float yaw:new float[]{-361,-315,-225,-135,-45,0,44.99F,45,90,135,180,225,270,315,360}){FoundryBlock b=owners[k];w.setBlockState(P,b.getDefaultState(),2);TileEntity old=w.getTileEntity(P);p.rotationYaw=yaw;b.onBlockPlacedBy(w,P,w.getBlockState(P),p,ItemStack.EMPTY);int expected=new int[]{2,5,3,4}[(int)Math.floor(yaw*4F/360F+.5)&3];check(b.getMetaFromState(w.getBlockState(P))==expected&&w.getTileEntity(P)==old,"native yaw placement and tile identity");}
  for(int mask=0;mask<16;mask++){
   w.setBlockState(P,owners[2].getDefaultState(),2);int index=0;for(EnumFacing f:EnumFacing.HORIZONTALS)w.setBlockState(P.offset(f),((mask&(1<<index++))!=0?owners[2]:Blocks.AIR).getDefaultState(),2);
   IBlockState s=w.getBlockState(P);List<AxisAlignedBB> boxes=new ArrayList<>();s.addCollisionBoxToList(w,P,new AxisAlignedBB(P).grow(.2),boxes,null,false);check(boxes.size()==1+Integer.bitCount(mask),"channel all sixteen actual neighbor collision masks");FoundryModelState data=FoundryModelState.get(owners[2].getExtendedState(s,w,P));check(Integer.bitCount(data.neighbors)==Integer.bitCount(mask),"current channel adjacency snapshot");
  }
  for(EnumFacing f:EnumFacing.HORIZONTALS)for(int k=0;k<6;k++)for(int meta=2;meta<=5;meta++){
   w.setBlockState(P,owners[2].getDefaultState(),2);w.setBlockState(P.offset(f),owners[k].getStateFromMeta(meta),2);check(((FoundryChannel)owners[2]).canConnectTo(w,4,20,4,ForgeDirection.getOrientation(f.getIndex()))==(k==1||k==2||(k>=4&&meta==f.getIndex())),"channel exact owner/orientation connection");
  }
  System.out.println("PASS complete native foundry owners: metadata, actual World/Chunk retention, support, bounds and connected collision callbacks.");
 }
 static void actions(){
  for(int k=0;k<4;k++)for(EnumHand hand:EnumHand.values()){
   NativeWorld w=new NativeWorld(false);Player p=new Player(w);FoundryBlock b=owners[k];TileEntityFoundryBase tile=w.put(P,b);p.setHeldItem(hand,new ItemStack(Items.IRON_SHOVEL));tile.amount=11;tile.type=iron;w.clear();check(b.onBlockActivated(w,P,w.getBlockState(P),p,hand,EnumFacing.UP,0,0,0),"native hand shovel handled");check(tile.amount==0&&tile.type==null&&ItemScraps.value.amount==11&&ItemScraps.value.material==iron,"shovel exact material quantum recovery");check(w.notifications.size()==1,"one original block update");check(p.getHeldItem(hand).getItem()==Items.IRON_SHOVEL&&p.getHeldItem(hand).getItemDamage()==0,"no invented tool wear");
   tile.amount=8;tile.type=iron;w.clear();w.setBlockToAir(P);check(w.getTileEntity(P)==null&&tile.amount==0&&w.spawned.size()==1,"break returns remaining material then native tile removal");close(w.spawned.get(0).posY,20+(k==0?(double).999F:k==3?1:.5),"drop selection-height position");
  }
  for(int k=0;k<2;k++)for(EnumHand hand:EnumHand.values()){
   NativeWorld w=new NativeWorld(false);Player p=new Player(w);FoundryCastingBase b=(FoundryCastingBase)owners[k];TileEntityFoundryCastingBase tile=w.put(P,b);ItemStack held=new ItemStack(molds,5,k==0?1:0);NBTTagCompound tag=new NBTTagCompound();tag.setString("keep","tagged-mold");held.setTagCompound(tag);p.setHeldItem(hand,held);w.clear();check(b.onBlockActivated(w,P,w.getBlockState(P),p,hand,EnumFacing.UP,0,0,0),"mold insertion exact branch");check(held.getCount()==4&&tile.slots[0].getCount()==1&&tile.slots[0]!=held&&tile.slots[0].getTagCompound()!=held.getTagCompound()&&tile.slots[0].getTagCompound().equals(tag),"native actual hand mold copied once with deep NBT");check(w.sounds.equals(Collections.singletonList(ModSoundEvents.ITEM_UPGRADE_PLUG)),"actual original sound identity");
   tile.slots[1]=new ItemStack(Items.DIAMOND,3);tile.amount=5;tile.type=iron;p.setHeldItem(hand,new ItemStack(Items.IRON_SHOVEL));check(b.onBlockActivated(w,P,w.getBlockState(P),p,hand,EnumFacing.UP,0,0,0)&&tile.slots[1]==null&&tile.amount==5,"output extraction wins before shovel");check(!b.onScrew(w,p,4,20,4,0,0,0,0,IToolable.ToolType.SCREWDRIVER),"filled mold cannot be removed");tile.amount=0;check(b.onScrew(w,p,4,20,4,0,0,0,0,IToolable.ToolType.SCREWDRIVER)&&tile.slots[0]==null,"empty mold removal original guard");
  }
  for(int k=4;k<6;k++){
   NativeWorld w=new NativeWorld(false);Player p=new Player(w);FoundryOutlet b=(FoundryOutlet)owners[k];TileEntityFoundryOutlet tile=w.put(P,b);w.clear();check(b.onBlockActivated(w,P,w.getBlockState(P),p,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0)&&tile.invertRedstone,"empty hand toggles redstone");p.setHeldItem(EnumHand.OFF_HAND,new ItemStack(ModItems.scraps,7));ItemScraps.value=new MaterialStack(iron,5);check(b.onBlockActivated(w,P,w.getBlockState(P),p,EnumHand.OFF_HAND,EnumFacing.UP,0,0,0)&&tile.filter==iron&&p.getHeldItem(EnumHand.OFF_HAND).getCount()==7,"filter reads actual hand without consuming scraps");check(!b.onScrew(w,p,4,20,4,0,0,0,0,IToolable.ToolType.HAND_DRILL)&&tile.invertFilter,"original server hand-drill false result preserved");check(!b.onScrew(w,p,4,20,4,0,0,0,0,IToolable.ToolType.SCREWDRIVER)&&tile.filter==null&&!tile.invertFilter,"original server screwdriver false result preserved");
  }
  NativeWorld client=new NativeWorld(true);Player p=new Player(client);for(FoundryBlock b:owners){client.put(P,b);client.clear();check(b.onBlockActivated(client,P,client.getBlockState(P),p,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0)&&client.notifications.isEmpty()&&client.spawned.isEmpty(),"client activation never mutates mechanics");}
  System.out.println("PASS actual native both-hand activation, priority, copied stack/NBT, sound identity, shovel accounting, tools and break lifecycle.");
 }
 static final Map<String,TextureAtlasSprite> sprites=new LinkedHashMap<>();
 static class Sprite extends TextureAtlasSprite {Sprite(String name,int index){super(name);setIconWidth(16);setIconHeight(16);initSprite(1024,64,(index%64)*16,16,false);}}
 static TextureAtlasSprite sprite(ResourceLocation name){return sprites.computeIfAbsent(name.toString(),n->new Sprite(n,sprites.size()));}
 static void geometry(FoundryGeometry.Kind kind,boolean inventory,int meta,FoundryModelState data){
  String title=kind==FoundryGeometry.Kind.SLAGTAP?"Outlet":kind.name().substring(0,1)+kind.name().substring(1).toLowerCase(java.util.Locale.ROOT);List<LegacyFoundryGeometryOracle.Face> expected=LegacyFoundryGeometryOracle.build(title,inventory,meta,data);FoundryMaterials materials=new FoundryMaterials(kind,NativeFoundryBlocksTest::sprite);
  for(boolean fluid:new boolean[]{false,true}){
   List<BakedQuad> got=FoundryGeometry.build(kind,inventory,fluid,meta,data,materials);List<LegacyFoundryGeometryOracle.Face> chosen=new ArrayList<>();for(LegacyFoundryGeometryOracle.Face f:expected)if((f.light==240)==fluid)chosen.add(f);check(got.size()==chosen.size(),"pristine face count "+kind+"/"+inventory+"/"+fluid+"/"+meta);
   for(int i=0;i<got.size();i++){faces++;BakedQuad q=got.get(i);LegacyFoundryGeometryOracle.Face f=chosen.get(i);String label=kind+"/"+inventory+"/"+fluid+"/"+meta+"/"+i;check((!(inventory||fluid)||q.getFace().getIndex()==f.side)&&q.getSprite()==materials.get(f.texture),"pristine face/material "+label);check(q.shouldApplyDiffuseLighting()==inventory,"original world byte shade not double-lit "+label);check(q.getFormat().equals(fluid?DefaultVertexFormats.BLOCK:DefaultVertexFormats.ITEM),"native packed quad format");
    int[] packed=q.getVertexData();int stride=q.getFormat().getIntegerSize(),uv=q.getFormat().getUvOffsetById(0)/4;double[][] points=new double[4][3];Set<String> seen=new HashSet<>();
    for(int v=0;v<4;v++){int at=v*stride;double x=Float.intBitsToFloat(packed[at])-f.offset[0],y=Float.intBitsToFloat(packed[at+1])-f.offset[1],z=Float.intBitsToFloat(packed[at+2])-f.offset[2];points[v]=new double[]{x,y,z};double[] values={x,y,z};for(int axis=0;axis<3;axis++)check(Math.abs(values[axis]-f.bounds[axis])<.000004||Math.abs(values[axis]-f.bounds[axis+3])<.000004,"pristine box corner "+label);int axis=f.side<2?1:f.side<4?2:0;close(values[axis],f.bounds[axis+(f.side%2==1?3:0)],"pristine plane "+label);check(seen.add(x+","+y+","+z),"four distinct corners "+label);check(packed[at+3]==f.color,"exact original byte color "+label+" actual="+Integer.toHexString(packed[at+3])+" expected="+Integer.toHexString(f.color));
     double u=f.side<2?x:f.side==2?f.bounds[0]+f.bounds[3]-x:f.side==3?x:f.side==4?z:f.bounds[2]+f.bounds[5]-z;double vv=f.side<2?z:1-y;
     if(f.mirror&&(f.side==2||f.side==5))u+=f.side==2?1-f.bounds[0]-f.bounds[3]:1-f.bounds[2]-f.bounds[5];
     if(f.side>=2&&(f.bounds[1]<0||f.bounds[4]>1))vv=Math.abs(y-f.bounds[1])<.000004?1:0;
     close(Float.intBitsToFloat(packed[at+uv]),q.getSprite().getInterpolatedU(u*16),"pristine partial-bounds U "+label);close(Float.intBitsToFloat(packed[at+uv+1]),q.getSprite().getInterpolatedV(vv*16),"pristine partial-bounds V "+label);
     if(fluid)check(packed[at+q.getFormat().getUvOffsetById(1)/4]==240,"original packed block15 sky0 molten light");
    }
    double[] a=points[0],b=points[1],c=points[2];double nx=(b[1]-a[1])*(c[2]-a[2])-(b[2]-a[2])*(c[1]-a[1]),ny=(b[2]-a[2])*(c[0]-a[0])-(b[0]-a[0])*(c[2]-a[2]),nz=(b[0]-a[0])*(c[1]-a[1])-(b[1]-a[1])*(c[0]-a[0]);EnumFacing face=EnumFacing.byIndex(f.side);check(nx*face.getXOffset()+ny*face.getYOffset()+nz*face.getZOffset()>0,"original outward face winding "+label);
   }
  }
 }
 static void geometryAll(){
  FoundryModelState empty=new FoundryModelState(0,0,0,72,0,false,false,false);
  for(FoundryGeometry.Kind k:FoundryGeometry.Kind.values())for(int meta=0;meta<16;meta++){geometry(k,true,meta,null);geometry(k,false,meta,empty);}
  for(FoundryGeometry.Kind k:new FoundryGeometry.Kind[]{FoundryGeometry.Kind.CHANNEL,FoundryGeometry.Kind.TANK})for(int mask=0;mask<64;mask++)for(int amount:new int[]{0,1,36,72,108})for(int color:new int[]{0,0x123456,0xEAB167,0xffffff})geometry(k,false,5,new FoundryModelState(mask,k==FoundryGeometry.Kind.TANK?(~mask)&60:0,amount,72,color,true,false,false));
  for(int meta=0;meta<16;meta++)for(boolean filter:new boolean[]{false,true})for(boolean closed:new boolean[]{false,true})for(FoundryGeometry.Kind k:new FoundryGeometry.Kind[]{FoundryGeometry.Kind.OUTLET,FoundryGeometry.Kind.SLAGTAP})geometry(k,false,meta,new FoundryModelState(0,0,0,0,0,false,filter,closed));
  System.out.println("PASS "+faces+" native faces independently matched to original five renderer callback bodies: geometry, order, suppression, material, winding, partial UV, byte color, fill and packed molten light.");
 }
 static final class LightingWorld implements net.minecraft.world.IBlockAccess {
  final net.minecraft.util.math.BlockPos pos=new net.minecraft.util.math.BlockPos(8,64,8);
  final int own=0x00A00070;
  public net.minecraft.tileentity.TileEntity getTileEntity(net.minecraft.util.math.BlockPos p){return null;}
  public int getCombinedLight(net.minecraft.util.math.BlockPos p,int emission){if(p.equals(pos))return own;return 0x00100010*((Math.abs(p.getX()+2*p.getY()+3*p.getZ())%8)+1);}
  public IBlockState getBlockState(net.minecraft.util.math.BlockPos p){return p.equals(pos)?owners[3].getDefaultState():net.minecraft.init.Blocks.STONE.getDefaultState();}
  public boolean isAirBlock(net.minecraft.util.math.BlockPos p){return false;}
  public net.minecraft.world.biome.Biome getBiome(net.minecraft.util.math.BlockPos p){return net.minecraft.init.Biomes.PLAINS;}
  public int getStrongPower(net.minecraft.util.math.BlockPos p,EnumFacing side){return 0;}
  public net.minecraft.world.WorldType getWorldType(){return net.minecraft.world.WorldType.DEFAULT;}
  public boolean isSideSolid(net.minecraft.util.math.BlockPos p,EnumFacing side,boolean fallback){return !p.equals(pos);}
 }
 static void lighting(IBakedModel model,IBlockState extended){
  LightingWorld world=new LightingWorld();IBlockState state=extended;
  net.minecraft.client.renderer.color.BlockColors colors=new net.minecraft.client.renderer.color.BlockColors();
  for(boolean forge:new boolean[]{false,true}){
   net.minecraft.client.renderer.BufferBuilder out=new net.minecraft.client.renderer.BufferBuilder(4096);out.begin(7,DefaultVertexFormats.BLOCK);
   if(forge){
    net.minecraftforge.client.model.pipeline.VertexLighterFlat lighter=new net.minecraftforge.client.model.pipeline.VertexLighterFlat(colors);
    net.minecraftforge.client.model.pipeline.VertexBufferConsumer consumer=new net.minecraftforge.client.model.pipeline.VertexBufferConsumer(out);consumer.setOffset(world.pos);
    lighter.setParent(consumer);lighter.setWorld(world);lighter.setState(state);lighter.setBlockPos(world.pos);lighter.updateBlockInfo();
    for(BakedQuad q:model.getQuads(state,null,0))q.pipe(lighter);lighter.resetBlockInfo();
   }else check(new net.minecraft.client.renderer.BlockModelRenderer(colors).renderModelFlat(world,model,state,world.pos,out,true,0),"native vanilla flat fallback emits model");
   out.finishDrawing();check(out.getVertexCount()==model.getQuads(state,null,0).size()*4,"actual foundry housing faces emitted "+forge);
   java.nio.ByteBuffer bytes=out.getByteBuffer();
   for(int v=0;v<out.getVertexCount();v++){
    check(bytes.getInt(v*28+24)==world.own,"native "+(forge?"Forge VertexLighterFlat":"vanilla flat")+" light samples block, not differently-lit neighbors at vertex "+v+": "+Integer.toHexString(bytes.getInt(v*28+24)));
    int expected=model.getQuads(state,null,0).get(v/4).getVertexData()[(v%4)*7+3];
    check(bytes.getInt(v*28+12)==expected,"native renderer preserves original precomputed shadow without double diffuse "+forge);
   }
  }
 }

 static void snapshots()throws Exception{
  NativeWorld w=new NativeWorld(true);TileEntityFoundryTank tile=w.put(P,owners[3]);tile.type=iron;tile.amount=1;IBlockState raw=w.getBlockState(P),first=owners[3].getExtendedState(raw,w,P);FoundryModelState a=FoundryModelState.get(first);tile.amount=1296;IBlockState second=owners[3].getExtendedState(raw,w,P);FoundryModelState b=FoundryModelState.get(second);check(a.amount==1&&b.amount==1296&&a!=b,"immutable per-render snapshots never retain mutable tile");
  FoundryMaterials materials=new FoundryMaterials(FoundryGeometry.Kind.TANK,NativeFoundryBlocksTest::sprite);IBakedModel model=new FoundryBakedModel(FoundryGeometry.Kind.TANK,0,false,materials);lighting(model,first);check(!model.isAmbientOcclusion()&&!model.isBuiltInRenderer()&&model.isGui3d(),"native housing model contract");for(EnumFacing side:EnumFacing.values())check(model.getQuads(first,side,0).isEmpty(),"no accidental native culling/double faces");check(model.getQuads(first,null,0).size()==model.getQuads(second,null,0).size(),"amount is solely dynamic molten TESR, not stale chunk housing");List<BakedQuad> one=FoundryGeometry.build(FoundryGeometry.Kind.TANK,false,true,0,a,materials),half=FoundryGeometry.build(FoundryGeometry.Kind.TANK,false,true,0,b,materials);check(!Arrays.equals(one.get(0).getVertexData(),half.get(0).getVertexData()),"current snapshot changes actual molten vertices");
  for(EnumFacing side:EnumFacing.HORIZONTALS){w.setBlockState(P.offset(side),owners[4].getStateFromMeta(side.getIndex()),2);check(FoundryModelState.get(owners[3].getExtendedState(raw,w,P)).outlet(side),"outlet current direction opens exact tank face");w.setBlockState(P.offset(side),owners[5].getStateFromMeta(side.getIndex()),2);check(!FoundryModelState.get(owners[3].getExtendedState(raw,w,P)).outlet(side),"slagtap deliberately never opens tank housing");}
  tile.amount=123;tile.type=iron;net.minecraft.network.PacketBuffer buffer=new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());tile.getUpdatePacket().writePacketData(buffer);net.minecraft.network.play.server.SPacketUpdateTileEntity packet=new net.minecraft.network.play.server.SPacketUpdateTileEntity();try{packet.readPacketData(buffer);}catch(IOException e){throw new AssertionError(e);}NativeWorld receiving=new NativeWorld(true);TileEntityFoundryTank client=receiving.put(P,owners[3]);client.onDataPacket(null,packet);client.update();check(FoundryModelState.get(owners[3].getExtendedState(receiving.getBlockState(P),receiving,P)).amount==123&&receiving.renders>0,"native serialized tile update reaches current block snapshot and redraw");
 }
 static void assetsAndLoader()throws Exception {
  Method initialize=MaterialTextureAtlasSpriteNativeTest.class.getDeclaredMethod("initializeNativeClientSettings");initialize.setAccessible(true);initialize.invoke(null);SimpleReloadableResourceManager resourceManager=new SimpleReloadableResourceManager(new MetadataSerializer());resourceManager.reloadResourcePack(new FolderResourcePack(resources));set(Minecraft.getMinecraft(),Minecraft.class,"resourceManager",resourceManager);
  FoundryBlockModelHandler.registerModels(new ModelRegistryEvent());Map<?,?> registered=(Map<?,?>)get(null,ModelLoader.class,"customModels");
  Class<?> type=Class.forName("com.hbm.main.FoundryBlockModelHandler$Loader");Constructor<?> constructor=type.getDeclaredConstructor();constructor.setAccessible(true);ICustomModelLoader loader=(ICustomModelLoader)constructor.newInstance();
  for(int i=0;i<6;i++){FoundryGeometry.Kind kind=FoundryGeometry.Kind.values()[i];FoundryBlock block=owners[i];String route="native_foundry/"+block.getRegistryName().getPath();com.google.gson.JsonObject json=new com.google.gson.JsonParser().parse(new FileReader(new File(resources,"assets/hbm/blockstates/"+block.getRegistryName().getPath()+".json"))).getAsJsonObject();check(json.getAsJsonObject("variants").entrySet().size()==16,"all sixteen actual blockstate JSON routes");
   for(ResourceLocation texture:FoundryMaterials.textures(kind).values())check(new File(resources,"assets/"+texture.getNamespace()+"/textures/"+texture.getPath()+".png").isFile(),"original declared atlas texture exists "+texture);
   for(int meta=0;meta<16;meta++)for(String context:new String[]{"world","item"}){ResourceLocation location=new ResourceLocation("hbm",route+"/"+meta+"/"+context);for(String prefix:new String[]{"","block/","models/block/"}){ResourceLocation routed=new ResourceLocation("hbm",prefix+location.getPath());check(loader.accepts(routed),"actual native Variant models/block prefix route");IModel model=loader.loadModel(routed);IBakedModel baked=model.bake(TRSRTransformation.identity(),DefaultVertexFormats.ITEM,NativeFoundryBlocksTest::sprite);check(baked instanceof FoundryBakedModel&&model.getTextures().contains(new ResourceLocation("hbm","blocks/brick_fire")),"actual native loader bakes and preserves original particle");}
    if(context.equals("world"))check(json.getAsJsonObject("variants").getAsJsonObject("metadata="+meta).get("model").getAsString().equals(location.toString()),"JSON exact world route");
   }
  }
  check(registered.size()>=96,"ModelRegistryEvent queues 96 actual metadata item routes after real ItemBlocks");loader.onResourceManagerReload(null);check(loader.loadModel(new ResourceLocation("hbm","native_foundry/tile.foundry_tank/0/item")).bake(TRSRTransformation.identity(),DefaultVertexFormats.ITEM,NativeFoundryBlocksTest::sprite)!=loader.loadModel(new ResourceLocation("hbm","native_foundry/tile.foundry_tank/0/item")).bake(TRSRTransformation.identity(),DefaultVertexFormats.ITEM,NativeFoundryBlocksTest::sprite),"native rebake creates fresh atlas-bound models");
  System.out.println("PASS real ModelRegistryEvent, six JSON owners, all metadata/prefix native loader routes, original PNG assets and fresh rebake.");
 }
 public static void main(String[] args)throws Exception{resources=new File(args[0]);setup();states();actions();geometryAll();snapshots();assetsAndLoader();System.out.println("PASS "+checks+" actual native foundry owner/model checks; full seven owners, eight tiles, models and TESRs compile. GL invocation proof follows separately.");}
}

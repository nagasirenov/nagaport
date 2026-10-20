import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.hbm.blocks.network.*;
import com.hbm.render.block.*;
import com.hbm.render.block.nativeblock.*;
import com.hbm.render.mesh.*;
import com.hbm.render.tileentity.NetworkBreakingRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.*;
import com.hbm.main.DiodeNetworkModelHandler;
import com.hbm.lib.*;
import api.hbm.block.IToolable.ToolType;
import api.hbm.energymk2.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class DiodeNetworkNativeTest {
 static long checks,cases;static final BlockPos POS=new BlockPos(8,64,8);static File resources;
 static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static void close(double a,double b,String why){check(Math.abs(a-b)<.000003,why+": "+a+" != "+b);}
 static final class Sprite extends TextureAtlasSprite {Sprite(String s,int i){super(s);setIconWidth(16);setIconHeight(16);initSprite(256,256,16*i,16,false);}}
 static TextureAtlasSprite[] diode=new TextureAtlasSprite[3],partitioner=new TextureAtlasSprite[6];
 static class Face {float[][] xyz,uv;float[] normal;}static class Part {String name;List<Face> faces=new ArrayList<>();Part(String n){name=n;}}
 static List<Part> parse(Reader source)throws Exception {
  List<float[]> points=new ArrayList<>(),uvs=new ArrayList<>();List<Part> groups=new ArrayList<>();BufferedReader in=new BufferedReader(source);String line;Part group=null;
  while((line=in.readLine())!=null){String[] p=line.trim().split("\\s+");if(p[0].equals("o")||p[0].equals("g")){group=new Part(p[1]);groups.add(group);}
   else if(p[0].equals("v"))points.add(new float[]{Float.parseFloat(p[1]),Float.parseFloat(p[2]),Float.parseFloat(p[3])});
   else if(p[0].equals("vt"))uvs.add(new float[]{Float.parseFloat(p[1]),1-Float.parseFloat(p[2])});
   else if(p[0].equals("f")){check(p.length==4||p.length==5,"source triangle/quad");if(group==null){group=new Part("Default");groups.add(group);}Face f=new Face();f.xyz=new float[p.length-1][];f.uv=new float[p.length-1][];
    for(int i=1;i<p.length;i++){String[] k=p[i].split("/");f.xyz[i-1]=points.get(Integer.parseInt(k[0])-1);f.uv[i-1]=uvs.get(Integer.parseInt(k[1])-1);}
    double ax=f.xyz[1][0]-f.xyz[0][0],ay=f.xyz[1][1]-f.xyz[0][1],az=f.xyz[1][2]-f.xyz[0][2],bx=f.xyz[2][0]-f.xyz[0][0],by=f.xyz[2][1]-f.xyz[0][1],bz=f.xyz[2][2]-f.xyz[0][2];
    double nx=ay*bz-az*by,ny=az*bx-ax*bz,nz=ax*by-ay*bx,length=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);f.normal=length<1E-4?new float[3]:new float[]{(float)(nx/length),(float)(ny/length),(float)(nz/length)};group.faces.add(f);
   }
  }return groups;
 }
 static double[] rotate(float[] v,float yaw){double c=MathHelper.cos(yaw),s=MathHelper.sin(yaw);return new double[]{v[0]*c+v[2]*s,v[1],v[2]*c-v[0]*s};}
 static double[] transform(double[] p,LegacyNetworkOracle.Layer l,boolean item){double[] m=l.matrix;double x=p[0]+l.tx,y=p[1]+l.ty,z=p[2]+l.tz;return new double[]{m[0]*x+m[1]*y+m[2]*z+m[3]+(item?.5:0),m[4]*x+m[5]*y+m[6]*z+m[7]+(item?.5:0),m[8]*x+m[9]*y+m[10]*z+m[11]+(item?.5:0)};}
 static class Vertex {double[] p;float u,v;int color;Vertex(double[] p,float u,float v,int c){this.p=p;this.u=u;this.v=v;color=c;}}
 static List<Vertex> part(List<Part> mesh,LegacyNetworkOracle.Layer layer,TextureAtlasSprite sprite,boolean item){
  Part selected=null;for(Part p:mesh)if(p.name.equals(layer.part))selected=p;List<Vertex> out=new ArrayList<>();if(selected==null)return out;
  for(Face f:selected.faces){double[] normal=rotate(f.normal,layer.rotation);int shade=item?255:Math.max(0,Math.min(255,(int)(Math.max(.45,normal[1]*.3+.7-Math.abs(normal[0])*.1+Math.abs(normal[2])*.1)*255F)));int color=0xff000000|shade|shade<<8|shade<<16;
   for(int i=0;i<f.xyz.length;i++){double[] v=rotate(f.xyz[i],layer.rotation);for(int ax=0;ax<3;ax++)v[ax]=(float)v[ax];Vertex e=new Vertex(transform(v,layer,item),sprite.getInterpolatedU(f.uv[i][0]*16D),sprite.getInterpolatedV(f.uv[i][1]*16D),color);out.add(e);if(i==2&&f.xyz.length==3)out.add(e);}
  }return out;
 }
 static void compareObj(List<BakedQuad> got,List<Part> original,List<LegacyNetworkOracle.Layer> layers,TextureAtlasSprite[] sprites,boolean item,int skip){int index=skip;
  for(LegacyNetworkOracle.Layer layer:layers)if(layer.part!=null){List<Vertex> want=part(original,layer,sprites[layer.sprite],item);check(want.size()%4==0,"whole primitive groups");for(int j=0;j<want.size();j+=4){BakedQuad q=got.get(index++);int[] data=q.getVertexData();int stride=q.getFormat().getIntegerSize();check(q.getSprite()==sprites[layer.sprite],"original group/material ordering");check(q.shouldApplyDiffuseLighting()==item,"OBJ shade path");
    for(int v=0;v<4;v++){Vertex e=want.get(j+v);for(int ax=0;ax<3;ax++)close(Float.intBitsToFloat(data[v*stride+ax]),e.p[ax],"original OBJ transformed coordinate");close(Float.intBitsToFloat(data[v*stride+4]),e.u,"original OBJ U");close(Float.intBitsToFloat(data[v*stride+5]),e.v,"original OBJ V");int shade=data[v*stride+3]&255;check(Math.abs(shade-(e.color&255))<=1,"original OBJ normal shader");}
  }}check(index==got.size(),"exact original part count");}
 static final int[][] CORNERS={{4,0,1,5},{2,6,7,3},{3,1,0,2},{6,4,5,7},{2,0,4,6},{7,5,1,3}};
 static void compareBoxes(List<BakedQuad> got,List<LegacyNetworkOracle.Layer> layers,boolean item){int index=0;for(LegacyNetworkOracle.Layer l:layers)if(l.part==null){BakedQuad q=got.get(index++);check(q.getSprite()==diode[l.sprite],"original plate/coil material");int[] data=q.getVertexData();int stride=q.getFormat().getIntegerSize(),uv=q.getFormat().getUvOffsetById(0)/4;double[] b=l.box;
   for(int v=0;v<4;v++){int corner=CORNERS[l.face][v];double x=(corner&1)==0?b[0]:b[3],y=(corner&2)==0?b[1]:b[4],z=(corner&4)==0?b[2]:b[5];double[] pos=transform(new double[]{x,y,z},l,item);for(int ax=0;ax<3;ax++)close(Float.intBitsToFloat(data[v*stride+ax]),pos[ax],"original cuboid bounds/GL transform");double u=l.face<2?x:l.face==2?b[0]+b[3]-x:l.face==3?x:l.face==4?z:b[2]+b[5]-z,w=l.face<2?z:1-y;
    close(Float.intBitsToFloat(data[v*stride+uv]),diode[l.sprite].getInterpolatedU(16*u),"original RenderBlocks U");close(Float.intBitsToFloat(data[v*stride+uv+1]),diode[l.sprite].getInterpolatedV(16*w),"original RenderBlocks V");}
  }check(index==12,"original two six-face cuboids");}
 static ObjMesh readMesh(String asset)throws Exception {try(Reader r=new FileReader(new File(resources,"assets/hbm/models/blocks/"+asset+".obj"))){return ObjMeshParser.parse(r,asset,false);}}
 static void geometry(File pristine)throws Exception {
  ObjMesh cable=readMesh("cable_neo"),buffer=readMesh("crane_buffer");List<Part> oc,op;try(Reader r=new FileReader(new File(pristine,"src/main/resources/assets/hbm/models/blocks/cable_neo.obj"))){oc=parse(r);}try(Reader r=new FileReader(new File(pristine,"src/main/resources/assets/hbm/models/blocks/crane_buffer.obj"))){op=parse(r);}
  for(int meta=0;meta<16;meta++)for(int mask=0;mask<64;mask++){List<BakedQuad> got=RenderDiode.bake(cable,meta,mask,false,diode);List<LegacyNetworkOracle.Layer> want=LegacyNetworkOracle.layers(true,false,meta,mask);compareBoxes(got,want,false);compareObj(got,oc,want,diode,false,12);cases++;}
  List<BakedQuad> got=RenderDiode.bake(cable,0,0,true,diode);List<LegacyNetworkOracle.Layer> want=LegacyNetworkOracle.layers(true,true,0,0);compareBoxes(got,want,true);compareObj(got,oc,want,diode,true,12);cases++;
  for(int meta=0;meta<16;meta++){got=RenderPartitioner.bake(buffer,meta,false,partitioner);want=LegacyNetworkOracle.layers(false,false,meta,0);compareObj(got,op,want,partitioner,false,0);cases++;}
  compareObj(RenderPartitioner.bake(buffer,0,true,partitioner),op,LegacyNetworkOracle.layers(false,true,0,0),partitioner,true,0);cases++;
 }
 static final class NativeWorld extends World {
  final Map<BlockPos,TileEntity> tiles=new HashMap<>();final Map<BlockPos,IBlockState> states=new HashMap<>();final List<net.minecraft.entity.Entity> spawned=new ArrayList<>();int updates,placementFlags;
  NativeWorld(boolean remote){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),remote);provider.setWorld(this);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected boolean isChunkLoaded(int x,int z,boolean empty){return true;}
  @Override public int getCombinedLight(BlockPos p,int e){return p.equals(POS)?0x00A00070:0x00300040;}
  @Override public TileEntity getTileEntity(BlockPos p){return tiles.get(p);}@Override public IBlockState getBlockState(BlockPos p){return states.getOrDefault(p,Blocks.AIR.getDefaultState());}
  @Override public boolean setBlockState(BlockPos p,IBlockState state,int flags){states.put(p,state);placementFlags=flags;return true;}
  @Override public void notifyBlockUpdate(BlockPos p,IBlockState a,IBlockState b,int flags){updates++;check(flags==3,"tool sync flags");}
  @Override public boolean spawnEntity(net.minecraft.entity.Entity e){spawned.add(e);return true;}
  @Override public void markChunkDirty(BlockPos p,TileEntity tile){}
  @Override public void updateComparatorOutputLevel(BlockPos pos,Block block){}
  @Override public void removeTileEntity(BlockPos pos){tiles.remove(pos);}
 }
 static final class Player extends net.minecraft.entity.player.EntityPlayer {Player(World w){super(w,new com.mojang.authlib.GameProfile(new UUID(1,2),"network-probe"));}public boolean isSpectator(){return false;}public boolean isCreative(){return false;}}
 static class Connector extends TileEntity implements IEnergyConnectorMK2 {ForgeDirection allowed=ForgeDirection.UNKNOWN;public boolean canConnect(ForgeDirection side){return side==allowed;}}
 static class Receiver extends com.hbm.tileentity.TileEntityLoadedBase implements IEnergyReceiverMK2 {long power;public long getPower(){return power;}public long getMaxPower(){return 1000;}public void setPower(long p){power=p;}}
 static class Conductor extends Connector implements IEnergyConductorMK2 {Nodespace.PowerNode node;public Nodespace.PowerNode createNode(){return node;}}
 static void owners()throws Exception {
  CableDiode d=new CableDiode(Material.IRON);CranePartitioner p=new CranePartitioner();NativeWorld w=new NativeWorld(false);Player player=new Player(w);w.states.put(POS,d.getDefaultState());
  check(d.getRenderType(d.getDefaultState())==EnumBlockRenderType.MODEL&&p.getRenderType(p.getDefaultState())==EnumBlockRenderType.MODEL,"native render entries");
  check(p.getRenderLayer()==BlockRenderLayer.CUTOUT_MIPPED,"partitioner original alpha holes use native cutout");
  check(!d.isOpaqueCube(d.getDefaultState())&&!p.isFullCube(p.getDefaultState()),"nonopaque owners");
  for(int meta=0;meta<16;meta++){check(d.getMetaFromState(d.getStateFromMeta(meta))==meta&&p.getMetaFromState(p.getStateFromMeta(meta))==meta,"lossless metadata 0..15");}
  ObjMesh cable=readMesh("cable_neo");DiodeBakedModel model=new DiodeBakedModel(cable,0,false,diode);
  int[] bits={4,8,1,2,16,32};for(int mask=0;mask<64;mask++){w.tiles.clear();for(EnumFacing f:EnumFacing.values())if((mask&bits[f.getIndex()])!=0){Connector c=new Connector();c.allowed=ForgeDirection.fromEnumFacing(f.getOpposite());w.tiles.put(POS.offset(f),c);}
   IBlockState extended=d.getExtendedState(d.getDefaultState(),w,POS);check(((IExtendedBlockState)extended).getValue(ConnectionState.CONNECTIONS)==mask,"actual native contextual adjacency");check(model.getQuads(extended,null,0).size()==RenderDiode.bake(cable,0,mask,false,diode).size(),"snapshot to model entry");}
  check(model.getQuads(null,EnumFacing.UP,0).isEmpty(),"unculled general model quads");
  for(int yaw=0;yaw<4;yaw++){player.rotationYaw=yaw*90;player.setPosition(8.5,64,9.5);d.onBlockPlacedBy(w,POS,d.getDefaultState(),player,ItemStack.EMPTY);check(d.getMetaFromState(w.getBlockState(POS))==new int[]{2,5,3,4}[yaw],"original piston placement horizontal");p.onBlockPlacedBy(w,POS,p.getDefaultState(),player,ItemStack.EMPTY);check(p.getMetaFromState(w.getBlockState(POS))==new int[]{2,5,3,4}[yaw],"original partitioner yaw placement");check(w.placementFlags==2,"placement notification");}
  player.setPosition(8.5,68,8.5);d.onBlockPlacedBy(w,POS,d.getDefaultState(),player,ItemStack.EMPTY);check(d.getMetaFromState(w.getBlockState(POS))==1,"up diode placement");player.setPosition(8.5,60,8.5);d.onBlockPlacedBy(w,POS,d.getDefaultState(),player,ItemStack.EMPTY);check(d.getMetaFromState(w.getBlockState(POS))==0,"down diode placement");
  List<AxisAlignedBB> boxes=new ArrayList<>();AxisAlignedBB query=new AxisAlignedBB(7,63,7,10,66,10);d.addCollisionBoxToList(d.getDefaultState(),w,POS,query,boxes,null,false);check(boxes.size()==1&&boxes.get(0).equals(Block.FULL_BLOCK_AABB.offset(POS)),"original diode full collision");boxes.clear();p.addCollisionBoxToList(p.getDefaultState(),w,POS,query,boxes,null,false);check(boxes.size()==1&&boxes.get(0).equals(new AxisAlignedBB(0,0,0,1,.75,1).offset(POS)),"partitioner 3/4 collision");
  CableDiode.TileEntityDiode tile=(CableDiode.TileEntityDiode)d.createNewTileEntity(w,0);tile.setWorld(w);tile.setPos(POS);w.tiles.clear();w.tiles.put(POS,tile);w.states.put(POS,d.getStateFromMeta(2));
  for(int i=1;i<=12;i++){check(d.onScrew(w,player,8,64,8,0,0,0,0,ToolType.SCREWDRIVER),"screw increment");check(tile.getMaxPower()==(long)Math.pow(10,Math.min(11,i+1)),"10^level throughput and upper bound");}
  for(int i=0;i<12;i++)d.onScrew(w,player,8,64,8,0,0,0,0,ToolType.HAND_DRILL);check(tile.getMaxPower()==10,"lower bound one");
  for(int i=0;i<5;i++)d.onScrew(w,player,8,64,8,0,0,0,0,ToolType.DEFUSER);check(tile.getPriority()==IEnergyReceiverMK2.ConnectionPriority.NORMAL,"priority cycles exactly five");
  check(!d.onScrew(w,player,8,64,8,0,0,0,0,ToolType.WRENCH),"unrecognized tool false");NativeWorld remote=new NativeWorld(true);check(d.onScrew(remote,player,8,64,8,0,0,0,0,ToolType.SCREWDRIVER)&&remote.updates==0,"client tools acknowledge without mutation");
  NBTTagCompound tag=new NBTTagCompound();tag.setInteger("x",8);tag.setInteger("y",64);tag.setInteger("z",8);tag.setInteger("level",4);tag.setByte("p",(byte)4);tile.readFromNBT(tag);NBTTagCompound saved=tile.writeToNBT(new NBTTagCompound());check(saved.getInteger("level")==4&&saved.getByte("p")==4,"original NBT fields");check(tile.getUpdatePacket().getNbtCompound().getInteger("level")==4&&tile.getUpdateTag().getByte("p")==4,"native incremental and initial sync");
  tag.setInteger("level",1);tag.setByte("p",(byte)2);tile.readFromNBT(tag);tile.setPower(0);Conductor target=new Conductor();target.allowed=ForgeDirection.NORTH;target.node=new Nodespace.PowerNode();target.node.net=new Nodespace.Network();w.tiles.put(POS.south(),target);Nodespace.nodes.put(POS.south(),target.node);check(tile.transferPower(25)==15&&tile.getPower()==10&&target.node.net.accepted==10,"native complete embedded tile network cap branch");check(tile.transferPower(5)==5,"within tick cap");tile.update();check(tile.getPower()==0,"tick resets throughput");
  Nodespace.nodes.clear();Receiver rec=new Receiver();w.tiles.put(POS.south(),rec);check(tile.transferPower(25)==0&&rec.power==25&&tile.getPower()==0,"original direct receiver branch deliberately bypasses diode accumulation");
  tile.update();rec.power=0;for(int i=0;i<10;i++)check(tile.transferPower(1)==0,"ten direct pulses");check(tile.transferPower(1)==1&&rec.power==10,"eleventh pulse brake");
  w.tiles.clear();api.hbm.tile.ILoadedTile.TileAccessCache.cache.clear();Nodespace.nodes.clear();for(EnumFacing f:EnumFacing.values()){Conductor c=new Conductor();c.allowed=ForgeDirection.fromEnumFacing(f.getOpposite());c.node=new Nodespace.PowerNode();c.node.net=new Nodespace.Network();w.tiles.put(POS.offset(f),c);Nodespace.nodes.put(POS.offset(f),c.node);}tile.update();for(EnumFacing f:EnumFacing.values())check(Nodespace.nodes.get(POS.offset(f)).net.subscriptions==(f==EnumFacing.SOUTH?0:1),"original subscriptions exclude output");Nodespace.nodes.clear();
  w.states.put(POS,p.getStateFromMeta(2));CranePartitioner.TileEntityCranePartitioner machine=(CranePartitioner.TileEntityCranePartitioner)p.createNewTileEntity(w,2);machine.setWorld(w);machine.setPos(POS);w.tiles.clear();w.tiles.put(POS,machine);
  for(int meta=2;meta<6;meta++){w.states.put(POS,p.getStateFromMeta(meta));check(p.canItemEnter(w,8,64,8,ForgeDirection.getOrientation(meta),null),"partitioner directional entry");check(!p.canItemEnter(w,8,64,8,ForgeDirection.getOrientation(meta).getOpposite(),null),"reverse entry rejected");Vec3d snap=p.getClosestSnappingPosition(w,8,64,8,new Vec3d(8.2,64.5,8.8));close(snap.y,64.25,"quarter height conveyor snap");Vec3d moved=p.getTravelLocation(w,8,64,8,new Vec3d(8.2,64.25,8.8),.1);check(Double.isFinite(moved.x)&&Double.isFinite(moved.z),"native movement formula invoked");}
  check(!p.canPackageEnter(w,8,64,8,ForgeDirection.NORTH,null),"packages remain rejected");check(!p.onBlockActivated(w,POS,p.getDefaultState(),player,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0),"native inherited activation remains false");
  p.onItemEnter(w,8,64,8,ForgeDirection.NORTH,()->new ItemStack(Items.IRON_INGOT,7));check(com.hbm.util.InventoryUtil.start==0&&com.hbm.util.InventoryUtil.end==95,"crystallizable input slots");p.onItemEnter(w,8,64,8,ForgeDirection.NORTH,()->new ItemStack(Items.STICK,1));check(com.hbm.util.InventoryUtil.start==96&&com.hbm.util.InventoryUtil.end==103,"invalid type declog slots");
  machine.slots[0]=new ItemStack(Items.IRON_INGOT,7);machine.slots[1]=new ItemStack(Items.STICK,4);machine.slots[2]=ItemStack.EMPTY;w.spawned.clear();machine.update();check(w.spawned.size()==3,"partitioner chunked output preserves invalid full stack");check(machine.slots[0].getCount()==1&&machine.slots[1]==null&&machine.slots[2]==null,"partitioner residual/empty native semantics");check(machine.getSlotsForFace(EnumFacing.NORTH).length==104&&machine.canExtractItem(96,ItemStack.EMPTY,EnumFacing.DOWN)&&!machine.canExtractItem(95,ItemStack.EMPTY,EnumFacing.DOWN),"104 slots and declog extraction boundary");
 }
 static final class LightingWorld implements IBlockAccess {
  final IBlockState state;final int own=0x00A00070;LightingWorld(IBlockState state){this.state=state;}
  public TileEntity getTileEntity(BlockPos p){return null;}public int getCombinedLight(BlockPos p,int e){return p.equals(POS)?own:0x00100010*((Math.abs(p.getX()+2*p.getY()+3*p.getZ())%8)+1);}
  public IBlockState getBlockState(BlockPos p){return p.equals(POS)?state:Blocks.STONE.getDefaultState();}public boolean isAirBlock(BlockPos p){return false;}
  public Biome getBiome(BlockPos p){return Biomes.PLAINS;}public int getStrongPower(BlockPos p,EnumFacing f){return 0;}public WorldType getWorldType(){return WorldType.DEFAULT;}public boolean isSideSolid(BlockPos p,EnumFacing f,boolean b){return !p.equals(POS);}
 }
 static void lighting()throws Exception {
  CableDiode owner=new CableDiode(Material.IRON);IBlockState state=((IExtendedBlockState)owner.getDefaultState()).withProperty(ConnectionState.CONNECTIONS,63);LightingWorld world=new LightingWorld(state);DiodeBakedModel model=new DiodeBakedModel(readMesh("cable_neo"),0,false,diode);
  try {
   for(BlockRenderLayer layer:BlockRenderLayer.values())check(owner.canRenderInLayer(state,layer)==(layer==BlockRenderLayer.SOLID||layer==BlockRenderLayer.CUTOUT_MIPPED),"native owner exposes exactly the two lighting passes");
   net.minecraftforge.client.ForgeHooksClient.setRenderLayer(BlockRenderLayer.SOLID);check(model.isAmbientOcclusion()&&model.getQuads(state,null,0).size()==12,"only original standard cubes in AO pass");
   net.minecraft.client.renderer.color.BlockColors colors=new net.minecraft.client.renderer.color.BlockColors();net.minecraft.client.renderer.BufferBuilder cubeBuffer=new net.minecraft.client.renderer.BufferBuilder(65536);cubeBuffer.begin(7,DefaultVertexFormats.BLOCK);
   net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo smooth=new net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo(colors);net.minecraftforge.client.model.pipeline.VertexBufferConsumer cubeConsumer=new net.minecraftforge.client.model.pipeline.VertexBufferConsumer(cubeBuffer);cubeConsumer.setOffset(POS);smooth.setParent(cubeConsumer);smooth.setWorld(world);smooth.setState(state);smooth.setBlockPos(POS);smooth.updateBlockInfo();for(BakedQuad q:model.getQuads(state,null,0))q.pipe(smooth);smooth.resetBlockInfo();cubeBuffer.finishDrawing();check(cubeBuffer.getVertexCount()==48,"real native SmoothAo pipeline emits all cuboid vertices");
   net.minecraftforge.client.ForgeHooksClient.setRenderLayer(BlockRenderLayer.CUTOUT_MIPPED);check(!model.isAmbientOcclusion(),"OBJ arms never enter SmoothAo");List<BakedQuad> quads=model.getQuads(state,null,0);check(!quads.isEmpty(),"all connected arms in flat pass");
   for(boolean forge:new boolean[]{false,true}){
    net.minecraft.client.renderer.BufferBuilder out=new net.minecraft.client.renderer.BufferBuilder(65536);out.begin(7,DefaultVertexFormats.BLOCK);
    if(forge){net.minecraftforge.client.model.pipeline.VertexLighterFlat flat=new net.minecraftforge.client.model.pipeline.VertexLighterFlat(colors);net.minecraftforge.client.model.pipeline.VertexBufferConsumer sink=new net.minecraftforge.client.model.pipeline.VertexBufferConsumer(out);sink.setOffset(POS);flat.setParent(sink);flat.setWorld(world);flat.setState(state);flat.setBlockPos(POS);flat.updateBlockInfo();for(BakedQuad q:quads)q.pipe(flat);flat.resetBlockInfo();}
    else check(new net.minecraft.client.renderer.BlockModelRenderer(colors).renderModelFlat(world,model,state,POS,out,true,0),"vanilla fallback emits original arms");
    out.finishDrawing();check(out.getVertexCount()==quads.size()*4,"exact native OBJ vertex count");java.nio.ByteBuffer data=out.getByteBuffer();for(int v=0;v<out.getVertexCount();v++){check(data.getInt(v*28+24)==world.own,"OBJ samples own block brightness, not neighbor");check(data.getInt(v*28+12)==quads.get(v/4).getVertexData()[v%4*7+3],"OBJ original shader is not re-darkened by AO or diffuse");}
   }
   net.minecraftforge.client.ForgeHooksClient.setRenderLayer(BlockRenderLayer.TRANSLUCENT);check(model.getQuads(state,null,0).isEmpty(),"no duplicate third-pass geometry");
  } finally {net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);}
 }
 static void nativeDamageAndAlpha()throws Exception {
  CableDiode owner=new CableDiode(Material.IRON);NativeWorld world=new NativeWorld(false);world.states.put(POS,owner.getDefaultState());for(EnumFacing face:EnumFacing.values())world.states.put(POS.offset(face),owner.getDefaultState());
  DiodeBakedModel model=new DiodeBakedModel(readMesh("cable_neo"),0,false,diode);TextureAtlasSprite cracks=new Sprite("cracks",7);IBakedModel damage=net.minecraftforge.client.ForgeHooksClient.getDamageModel(model,cracks,owner.getDefaultState(),world,POS);
  check(damage.isAmbientOcclusion(),"default native damage has one AO flag and must be bypassed for these exact owners");
  check(damage.getQuads(owner.getDefaultState(),null,0).size()==RenderDiode.bake(readMesh("cable_neo"),0,63,false,diode).size(),"native damage model resolves actual contextual arms without missing unlisted state");for(BakedQuad q:damage.getQuads(owner.getDefaultState(),null,0))check(q.getSprite()==cracks,"native damage retexture path");
  DiodeBakedModel inventory=new DiodeBakedModel(readMesh("cable_neo"),0,true,diode);int inventoryCount=inventory.getQuads(null,null,0).size();try{for(BlockRenderLayer layer:BlockRenderLayer.values()){net.minecraftforge.client.ForgeHooksClient.setRenderLayer(layer);check(inventory.getQuads(null,null,0).size()==inventoryCount,"inventory retains cubes and arms independent of ambient chunk pass");}}finally{net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);}
  for(String name:new String[]{"cable_diode","hadron_coil_alloy","cable_neo","crane_partitioner_side","crane_partitioner_back"}){java.awt.image.BufferedImage image=javax.imageio.ImageIO.read(new File(resources,"assets/hbm/textures/blocks/"+name+".png"));boolean transparent=false;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int alpha=(image.getRGB(x,y)>>>24)&255;check(alpha==0||alpha==255,"original materials have no fractional alpha");transparent|=alpha==0;}check(transparent==!(name.equals("cable_diode")||name.equals("hadron_coil_alloy")),"layer selection matches original material alpha");}
 }
 static Object get(Object target,Class<?> type,String name)throws Exception{java.lang.reflect.Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(target);}
 static void set(Object target,Class<?> type,String name,Object value)throws Exception{java.lang.reflect.Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
 static void registry()throws Exception {
  CableDiode diodeOwner=new CableDiode(Material.IRON);CranePartitioner partOwner=new CranePartitioner();Block[] owners={diodeOwner,partOwner};String[] names={"cable_diode","crane_partitioner"};String[] kinds={"diode","partitioner"};
  for(int i=0;i<2;i++){Block b=owners[i];ForgeRegistries.BLOCKS.register(b.setRegistryName("hbm","tile."+names[i]));ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));}
  Block foreign=new CableDiode(Material.IRON).setRegistryName("foreign","tile.cable_diode"),subclass=new CranePartitioner(){}.setRegistryName("hbm","external_partition"),unknown=new CableDiode(Material.IRON).setRegistryName("hbm","other_diode");for(Block b:new Block[]{foreign,subclass,unknown})ForgeRegistries.BLOCKS.register(b);
  SimpleReloadableResourceManager manager=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());manager.reloadResources(Collections.singletonList(new FolderResourcePack(resources)));
  sun.misc.Unsafe unsafe=(sun.misc.Unsafe)get(null,sun.misc.Unsafe.class,"theUnsafe");net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);set(mc,net.minecraft.client.Minecraft.class,"resourceManager",manager);
  net.minecraft.launchwrapper.Launch.blackboard=new HashMap<>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",true);
  TextureMap atlas=new TextureMap("textures");ModelManager modelManager=new ModelManager(atlas);new ModelLoader(manager,atlas,new net.minecraft.client.renderer.BlockModelShapes(modelManager));
  DiodeNetworkModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());Map<?,?> states=(Map<?,?>)get(null,ModelLoader.class,"customStateMappers"),items=(Map<?,?>)get(null,ModelLoader.class,"customModels");check(items.size()==32,"two owners each sixteen item routes");
  for(Block b:new Block[]{foreign,subclass,unknown}){check(DiodeNetworkModelHandler.owner(b)==null&&!states.containsKey(b.delegate),"foreign/subclass/unspecified ownership untouched");}
  for(int i=0;i<2;i++){Block b=owners[i];net.minecraft.client.renderer.block.statemap.IStateMapper mapper=(net.minecraft.client.renderer.block.statemap.IStateMapper)states.get(b.delegate);check(mapper!=null,"native world state mapper installed");Map<IBlockState,ModelResourceLocation> routes=mapper.putStateModelLocations(b);check(routes.size()==16,"all sixteen listed metadata states");
   for(Map.Entry<IBlockState,ModelResourceLocation> e:routes.entrySet()){String expected="hbm:native_diode_network/"+kinds[i]+"/"+b.getMetaFromState(e.getKey())+"/world#normal";check(e.getValue().toString().equals(expected),"exact world metadata route");IModel loaded=ModelLoaderRegistry.getModel(e.getValue());check(loaded!=ModelLoaderRegistry.getMissingModel(),"registry resolves actual resource loader");IBakedModel baked=loaded.bake(loaded.getDefaultState(),DefaultVertexFormats.ITEM,id->new Sprite(id.toString(),0));check(!baked.getQuads(e.getKey(),null,0).isEmpty(),"loader to atlas model bake");check(loaded.getTextures().size()==(i==0?3:6),"all original materials declared");}
  }
  DiodeNetworkModelHandler.Loader loader=new DiodeNetworkModelHandler.Loader();loader.onResourceManagerReload(manager);for(String bad:new String[]{"hbm:native_diode_network/diode/16/world","foreign:native_diode_network/diode/0/world","hbm:native_diode_network/unknown/0/world","hbm:native_diode_network/diode/a/item","hbm:native_diode_network/diode/0/garbage"})check(!loader.accepts(new ResourceLocation(bad)),"invalid model not claimed");
  ResourceLocation route=new ResourceLocation("hbm","models/block/native_diode_network/diode/0/item");check(loader.accepts(route),"normalized route accepted");IModel before=loader.loadModel(route);IBakedModel b1=before.bake(before.getDefaultState(),DefaultVertexFormats.ITEM,id->new Sprite("old",0));loader.onResourceManagerReload(manager);IModel after=loader.loadModel(route);IBakedModel b2=after.bake(after.getDefaultState(),DefaultVertexFormats.ITEM,id->new Sprite("fresh",1));check(before!=after&&b1.getParticleTexture()!=b2.getParticleTexture(),"resource reload refreshes source model/atlas");
  DiodeNetworkModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());check(((Map<?,?>)get(null,ModelLoader.class,"customModels")).size()==32,"repeated registry stable");
  breakingDispatcher(mc,unsafe,owners,new Block[]{foreign,subclass,unknown});
 }
 static final class CapturingBreaking extends NetworkBreakingRenderer<TileEntity> {
  BufferBuilder captured;TextureMap texture;int draws;CapturingBreaking(boolean diode){super(diode);}
  @Override protected void draw(BufferBuilder buffer,TextureMap atlas){captured=buffer;texture=atlas;draws++;}
 }
 static void breakingDispatcher(net.minecraft.client.Minecraft mc,sun.misc.Unsafe unsafe,Block[] owners,Block[] unowned)throws Exception {
  net.minecraft.client.settings.GameSettings settings=(net.minecraft.client.settings.GameSettings)unsafe.allocateInstance(net.minecraft.client.settings.GameSettings.class);settings.ambientOcclusion=2;mc.gameSettings=settings;
  final TextureAtlasSprite crack=new Sprite("native-crack",7);TextureMap atlas=new TextureMap("textures"){@Override public TextureAtlasSprite getAtlasSprite(String name){check(name.startsWith("minecraft:blocks/destroy_stage_"),"native crack stage lookup");return crack;}};
  set(mc,net.minecraft.client.Minecraft.class,"textureMapBlocks",atlas);final IBakedModel[] active={null};
  BlockModelShapes shapes=new BlockModelShapes(new ModelManager(atlas)){@Override public IBakedModel getModelForState(IBlockState state){return active[0];}};
  BlockRendererDispatcher blocks=(BlockRendererDispatcher)unsafe.allocateInstance(BlockRendererDispatcher.class);set(blocks,BlockRendererDispatcher.class,"blockModelShapes",shapes);set(blocks,BlockRendererDispatcher.class,"blockModelRenderer",new BlockModelRenderer(new net.minecraft.client.renderer.color.BlockColors()));set(mc,net.minecraft.client.Minecraft.class,"blockRenderDispatcher",blocks);
  TileEntityRendererDispatcher dispatcher=TileEntityRendererDispatcher.instance;
  for(int kind=0;kind<2;kind++){
   boolean isDiode=kind==0;Block owner=owners[kind];TileEntity tile=isDiode?new CableDiode.TileEntityDiode():new CranePartitioner.TileEntityCranePartitioner();NativeWorld world=new NativeWorld(false);tile.setWorld(world);tile.setPos(POS);world.tiles.put(POS,tile);world.states.put(POS,owner.getStateFromMeta(3));
   check(owner.getDefaultState().hasCustomBreakingProgress(),"actual RenderGlobal special damage dispatch predicate");check(tile.canRenderBreaking(),"actual RenderGlobal ordinary damage skip predicate");
   check(dispatcher.getRenderer(tile) instanceof NetworkBreakingRenderer,"ModelRegistryEvent installed native existing tile renderer");
   CapturingBreaking capture=new CapturingBreaking(isDiode);TileEntitySpecialRenderer<?> saved=dispatcher.renderers.put(tile.getClass(),capture);
   try {
    dispatcher.render(tile,1.25,2.5,3.75,.5F,-1,1);check(capture.draws==0,"actual native dispatcher normal tile phase is a true no-op");
    for(Block foreign:unowned){world.states.put(POS,foreign.getDefaultState());check(!foreign.getDefaultState().hasCustomBreakingProgress()&&!tile.canRenderBreaking(),"foreign/subclass/unknown native damage predicates untouched");dispatcher.render(tile,1,2,3,.5F,4,1);check(capture.draws==0,"foreign/subclass/unknown actual renderer rejects draw");}
    world.states.put(POS,owner.getStateFromMeta(3));TileEntity sub=isDiode?new CableDiode.TileEntityDiode(){}:new CranePartitioner.TileEntityCranePartitioner(){};sub.setWorld(world);sub.setPos(POS);check(!sub.canRenderBreaking()&&!capture.owns(sub,world.getBlockState(POS)),"tile subclasses are not claimed");
    for(EnumFacing f:EnumFacing.values()){Connector c=new Connector();c.allowed=ForgeDirection.fromEnumFacing(f.getOpposite());world.tiles.put(POS.offset(f),c);}
    active[0]=isDiode?new DiodeBakedModel(readMesh("cable_neo"),3,false,diode):new AtlasObjBakedModel(RenderPartitioner.bake(readMesh("crane_buffer"),3,false,partitioner),partitioner[0]);
    IBlockState extended=owner.getExtendedState(world.getBlockState(POS),world,POS);List<BakedQuad> original=isDiode?((DiodeBakedModel)active[0]).getAllQuads(extended):active[0].getQuads(extended,null,0);
    List<BakedQuad> replaced=NetworkBreakingRenderer.breakingQuads(original,crack,isDiode);List<LegacyNetworkOracle.Layer> oracle=LegacyNetworkOracle.layers(isDiode,false,3,63,true);
    int q=0;for(LegacyNetworkOracle.Layer l:oracle){if(l.part==null){check(replaced.get(q++).getSprite()==(l.sprite==3?crack:diode[l.sprite]),"pristine damage override then coil override semantics");}}
    for(int i=isDiode?6:0;i<original.size();i++)check(replaced.get(i)==original.get(i),"pristine clear override retains original OBJ/partitioner material and light wrapper");
    for(boolean ambient:new boolean[]{false,true}){settings.ambientOcclusion=ambient?2:0;for(int stage:new int[]{0,5,9}){
     dispatcher.render(tile,1.25,2.5,3.75,.5F,stage,1);BufferBuilder output=capture.captured;check(output.getVertexCount()==original.size()*4,"native dispatcher emits all current topology vertices");check(output.getVertexFormat().getColorOffset()<0&&output.getVertexFormat().getSize()==24,"legacy damage has no vertex color channel");check(capture.texture==atlas,"atlas materials retained");
     BufferBuilder ref=new BufferBuilder(65536);ref.begin(7,DefaultVertexFormats.BLOCK);ref.setTranslation(1.25-POS.getX(),2.5-POS.getY(),3.75-POS.getZ());BlockModelRenderer renderer=blocks.getBlockModelRenderer();
     if(isDiode){IBakedModel cubes=new AtlasObjBakedModel(replaced.subList(0,12),crack);if(ambient)renderer.renderModelSmooth(world,cubes,world.getBlockState(POS),POS,ref,true,0);else renderer.renderModelFlat(world,cubes,world.getBlockState(POS),POS,ref,true,0);}
     int start=isDiode?12:0;renderer.renderModelFlat(world,new AtlasObjBakedModel(replaced.subList(start,replaced.size()),replaced.get(start).getSprite()),world.getBlockState(POS),POS,ref,true,0);ref.finishDrawing();
     java.nio.ByteBuffer expected=ref.getByteBuffer(),actual=output.getByteBuffer();for(int v=0;v<output.getVertexCount();v++){for(int axis=0;axis<3;axis++)close(actual.getFloat(v*24+axis*4),expected.getFloat(v*28+axis*4),"native damage camera-relative geometry");for(int uv=0;uv<2;uv++)close(actual.getFloat(v*24+12+uv*4),expected.getFloat(v*28+16+uv*4),"native damage atlas UV");check(actual.getInt(v*24+20)==expected.getInt(v*28+24),"native damage keeps standard-cube smooth/flat lightmap");if(v>=start*4)check(actual.getInt(v*24+20)==0x00A00070,"native damage OBJ keeps original own-block lightmap");}
    }}
    int count=capture.draws;dispatcher.render(tile,1,2,3,.5F,10,1);dispatcher.render(tile,1,2,3,.5F,-1,1);check(capture.draws==count,"invalid and ordinary stage do not enter GPU boundary");
   } finally {dispatcher.renderers.put(tile.getClass(),saved);}
  }
 }
 public static void main(String[] args)throws Exception {Bootstrap.register();net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(CableDiode.TileEntityDiode.class,new ResourceLocation("hbm","tileentity_cable_diode"));resources=new File(args[0],"src/main/resources");for(int i=0;i<diode.length;i++)diode[i]=new Sprite("diode"+i,i);for(int i=0;i<partitioner.length;i++)partitioner[i]=new Sprite("partitioner"+i,i);geometry(new File(args[1]));owners();lighting();nativeDamageAndAlpha();registry();System.out.println("PASS native damage dispatcher/predicates and colorless buffer parity; GPU upload/state bracket audited, not executed in a GL context.");System.out.println("PASS "+checks+" native assertions; "+cases+" pristine renderer geometry/metadata cases. Real owner placement, collision, tool, contextual state, embedded tile and model registry paths invoked. NTM transport collaborators explicitly remain test doubles.");}
}

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
import com.hbm.main.SplitterModelHandler;
import com.hbm.tileentity.network.TileEntityCraneSplitter;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.render.tileentity.SplitterBreakingRenderer;
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

public final class SplitterNativeTest {
 static long checks,cases;static final BlockPos POS=new BlockPos(8,64,8);static File resources;
 static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static void close(double a,double b,String why){check(Math.abs(a-b)<.000003,why+": "+a+" != "+b);}
 static final class Sprite extends TextureAtlasSprite {Sprite(String s,int i){super(s);setIconWidth(16);setIconHeight(16);initSprite(256,256,16*i,16,false);}}
 static TextureAtlasSprite[] sprites=new TextureAtlasSprite[11];
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
 static double[] transform(double[] p,LegacySplitterOracle.Layer l,boolean item){double[] m=l.matrix;double x=p[0]+l.tx,y=p[1]+l.ty,z=p[2]+l.tz;return new double[]{m[0]*x+m[1]*y+m[2]*z+m[3]+(item?.5:0),m[4]*x+m[5]*y+m[6]*z+m[7]+(item?.5:0),m[8]*x+m[9]*y+m[10]*z+m[11]+(item?.5:0)};}
 static class Vertex {double[] p;float u,v;int color;Vertex(double[] p,float u,float v,int c){this.p=p;this.u=u;this.v=v;color=c;}}
 static List<Vertex> part(List<Part> mesh,LegacySplitterOracle.Layer layer,TextureAtlasSprite sprite,boolean item){
  Part selected=null;for(Part p:mesh)if(p.name.equals(layer.part))selected=p;List<Vertex> out=new ArrayList<>();if(selected==null)return out;
  for(Face f:selected.faces){double[] normal=rotate(f.normal,layer.rotation);int shade=item?255:Math.max(0,Math.min(255,(int)(Math.max(.45F,(float)normal[1]*.3F+.7F-(float)Math.abs(normal[0])*.1F+(float)Math.abs(normal[2])*.1F)*255F)));int color=0xff000000|shade|shade<<8|shade<<16;
   for(int i=0;i<f.xyz.length;i++){double[] v=rotate(f.xyz[i],layer.rotation);for(int ax=0;ax<3;ax++)v[ax]=(float)v[ax];Vertex e=new Vertex(transform(v,layer,item),sprite.getInterpolatedU(f.uv[i][0]*16D),sprite.getInterpolatedV(f.uv[i][1]*16D),color);out.add(e);if(i==2&&f.xyz.length==3)out.add(e);}
  }return out;
 }
 static void compareObj(List<BakedQuad> got,List<Part> original,List<LegacySplitterOracle.Layer> layers,TextureAtlasSprite[] sprites,boolean item,int skip){int index=skip;
  for(LegacySplitterOracle.Layer layer:layers)if(layer.part!=null){List<Vertex> want=part(original,layer,sprites[layer.sprite],item);check(want.size()%4==0,"whole primitive groups");for(int j=0;j<want.size();j+=4){BakedQuad q=got.get(index++);int[] data=q.getVertexData();int stride=q.getFormat().getIntegerSize();check(q.getSprite()==sprites[layer.sprite],"original group/material ordering");check(q.shouldApplyDiffuseLighting()==item,"OBJ shade path");
    for(int v=0;v<4;v++){Vertex e=want.get(j+v);for(int ax=0;ax<3;ax++)close(Float.intBitsToFloat(data[v*stride+ax]),e.p[ax],"original OBJ transformed coordinate");close(Float.intBitsToFloat(data[v*stride+4]),e.u,"original OBJ U");close(Float.intBitsToFloat(data[v*stride+5]),e.v,"original OBJ V");int shade=data[v*stride+3]&255;check(shade==(e.color&255),"original OBJ normal shader");}
  }}check(index==got.size(),"exact original part count");}
 static ObjMesh readMesh()throws Exception {try(Reader r=new FileReader(new File(resources,"assets/hbm/models/blocks/splitter.obj"))){return ObjMeshParser.parse(r,"splitter",false);}}
 static void geometry(File pristine)throws Exception {
  ObjMesh mesh=readMesh();List<Part> original;try(Reader r=new FileReader(new File(pristine,"src/main/resources/assets/hbm/models/blocks/splitter.obj"))){original=parse(r);}
  for(int meta=0;meta<16;meta++)for(boolean item:new boolean[]{false,true}){compareObj(RenderSplitter.bake(mesh,meta,item,sprites),original,LegacySplitterOracle.layers(item,meta),sprites,item,0);cases++;}
  check(RenderSplitter.bake(mesh,0,true,sprites).size()==RenderSplitter.bake(mesh,12,false,sprites).size()*2,"inventory retains both authored halves");
 }
 static final class NativeWorld extends World {
  final Map<BlockPos,TileEntity> tiles=new HashMap<>();final Map<BlockPos,IBlockState> states=new HashMap<>();final List<net.minecraft.entity.Entity> spawned=new ArrayList<>();int dirties;
  NativeWorld(boolean remote){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),remote);provider.setWorld(this);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected boolean isChunkLoaded(int x,int z,boolean empty){return true;}
  @Override public int getCombinedLight(BlockPos p,int e){return p.equals(POS)?0x00A00070:0x00300040;}
  @Override public TileEntity getTileEntity(BlockPos p){return tiles.get(p);}@Override public IBlockState getBlockState(BlockPos p){return states.getOrDefault(p,Blocks.AIR.getDefaultState());}
  @Override public boolean setBlockState(BlockPos p,IBlockState state,int flags){states.put(p.toImmutable(),state);return true;}
  @Override public boolean spawnEntity(net.minecraft.entity.Entity e){spawned.add(e);return true;}
  @Override public void markChunkDirty(BlockPos p,TileEntity tile){dirties++;check(p.equals(tile.getPos()),"native dirty tile position");}
  @Override public void updateComparatorOutputLevel(BlockPos pos,Block block){}
  @Override public void removeTileEntity(BlockPos pos){tiles.remove(pos);}
  @Override public void scheduleUpdate(BlockPos pos,Block block,int delay){}
 }
 static final class Player extends net.minecraft.entity.player.EntityPlayer {Player(World w){super(w,new com.mojang.authlib.GameProfile(new UUID(1,2),"splitter-probe"));}public boolean isSpectator(){return false;}public boolean isCreative(){return false;}}
 static TileEntityCraneSplitter tile(NativeWorld world,BlockPos pos){TileEntityCraneSplitter tile=new TileEntityCraneSplitter();tile.setWorld(world);tile.setPos(pos);world.tiles.put(pos,tile);return tile;}
 static void stackMachine()throws Exception {
  NativeWorld world=new NativeWorld(false);TileEntityCraneSplitter tile=tile(world,POS);ItemStack input=new ItemStack(Items.DIAMOND,64,3);NBTTagCompound tag=new NBTTagCompound();tag.setString("splitter","retained");tag.setTag("nested",new NBTTagCompound());input.setTagCompound(tag);
  for(int l=1;l<=16;l++)for(int r=1;r<=16;r++){
   tile=tile(world,POS);tile.leftRatio=(byte)l;tile.rightRatio=(byte)r;LegacySplitterLogic oracle=new LegacySplitterLogic();oracle.leftRatio=(byte)l;oracle.rightRatio=(byte)r;
   for(int count:new int[]{1,2,3,7,16,31,64,1,1,0,17}){
    input.setCount(count);ItemStack[] result=tile.splitStack(input);LegacySplitterLogic.ItemStack[] expected=oracle.splitStack(new LegacySplitterLogic.ItemStack(count));
    check(result[0].getCount()==expected[0].stackSize&&result[1].getCount()==expected[1].stackSize,"pristine split rounding and carried position");check(result[0].getCount()+result[1].getCount()==count&&input.getCount()==count,"native count conservation and nonmutation");
    for(ItemStack out:result)if(!out.isEmpty()){check(out.getItem()==Items.DIAMOND&&out.getItemDamage()==3,"item/damage retained");check(out.getTagCompound().equals(tag)&&out.getTagCompound()!=tag,"deep native NBT copy");}
    NBTTagCompound saved=tile.writeToNBT(new NBTTagCompound());check(saved.getBoolean("pos")==oracle.position&&saved.getByte("count")==oracle.remaining,"pristine phase NBT");check(saved.getByte("left")==l&&saved.getByte("right")==r,"ratio NBT fields");
    TileEntityCraneSplitter loaded=tile(world,POS);loaded.readFromNBT(saved);tile=loaded;
   }
  }
  check(world.dirties==16*16*11,"every split marks native chunk dirty exactly once");
  NBTTagCompound old=new NBTTagCompound();old.setByte("left",(byte)-9);old.setByte("right",(byte)0);tile.readFromNBT(old);check(tile.leftRatio==1&&tile.rightRatio==1,"original absent/nonpositive ratios fallback only");old.setByte("left",(byte)40);old.setByte("right",(byte)99);tile.readFromNBT(old);check(tile.leftRatio==40&&tile.rightRatio==99,"original NBT above tool clamp not rebalanced");
  io.netty.buffer.ByteBuf buffer=io.netty.buffer.Unpooled.buffer();tile.serialize(buffer);check(buffer.readableBytes()==2,"original two-byte wire payload");TileEntityCraneSplitter target=new TileEntityCraneSplitter();target.deserialize(buffer);check(target.leftRatio==40&&target.rightRatio==99,"ratio network roundtrip");buffer.release();
  class Tick extends TileEntityCraneSplitter {int sent,range;public void networkPackNT(int value){sent++;range=value;}}
  Tick tick=new Tick();tick.setWorld(world);((net.minecraft.util.ITickable)tick).update();check(tick.sent==1&&tick.range==15,"native ITickable server range callback");tick.setWorld(new NativeWorld(true));tick.update();check(tick.sent==1,"remote tile skips packet");
 }
 static void owners()throws Exception {
  CraneSplitter block=new CraneSplitter();NativeWorld w=new NativeWorld(false);Player player=new Player(w);check(Arrays.equals(block.getDimensions(),new int[]{0,0,0,0,0,1})&&block.getOffset()==0,"original multiblock footprint");
  for(int m=0;m<16;m++)check(block.getMetaFromState(block.getStateFromMeta(m))==m,"metadata roundtrip");check(block.getRenderType(block.getDefaultState())==EnumBlockRenderType.MODEL&&!block.isFullCube(block.getDefaultState())&&!block.isOpaqueCube(block.getDefaultState()),"native renderer and original noncube ownership");
  for(int meta=12;meta<16;meta++){
   w.states.clear();w.tiles.clear();w.spawned.clear();w.states.put(POS,block.getStateFromMeta(meta));ForgeDirection forward=ForgeDirection.getOrientation(meta-10),right=forward.getRotation(ForgeDirection.DOWN);BlockPos other=POS.add(right.offsetX,0,right.offsetZ);
   com.hbm.handler.MultiblockHandlerXR.fillSpace(w,POS.getX(),POS.getY(),POS.getZ(),block.getDimensions(),block,forward);
   check(w.states.size()==2&&w.getBlockState(other).getBlock()==block,"original left-core right-dummy placement");check(block.getMetaFromState(w.getBlockState(other))==right.ordinal(),"dummy direction metadata mapping");
   check(Arrays.equals(block.findCore(w,other.getX(),other.getY(),other.getZ()),new int[]{8,64,8}),"actual native base finds core through right half");TileEntityCraneSplitter te=tile(w,POS);te.leftRatio=2;te.rightRatio=3;
   for(BlockPos pos:new BlockPos[]{POS,other}){check(block.getTravelDirection(w,pos.getX(),pos.getY(),pos.getZ(),null)==forward,"both halves match travel orientation");for(ForgeDirection d:ForgeDirection.values())check(block.canItemEnter(w,pos.getX(),pos.getY(),pos.getZ(),d,null)==(d==forward),"exact input direction gate");}
   EntityMovingItem original=new EntityMovingItem(w);ItemStack stack=new ItemStack(Items.DIAMOND,13,4);NBTTagCompound tag=new NBTTagCompound();tag.setString("route","original");stack.setTagCompound(tag);original.setItemStack(stack);
   original.enterBlock(block,new com.hbm.util.fauxpointtwelve.BlockPos(other.getX(),other.getY(),other.getZ()),forward);check(original.isDead&&w.spawned.size()==2,"real moving-entity callback consumes input and spawns both halves exactly once");
   int count=0;for(int i=0;i<2;i++){EntityMovingItem out=(EntityMovingItem)w.spawned.get(i);ItemStack result=out.getItemStack();count+=result.getCount();check(result.getCount()==(i==0?6:7),"original 2:3 carried split rounding");check(result.getItem()==Items.DIAMOND&&result.getItemDamage()==4&&result.getTagCompound().equals(tag)&&result.getTagCompound()!=tag,"real entity data manager preserves independent native stacks/NBT");BlockPos at=i==0?POS:other;close(out.posX,at.getX()+.5,"spawn X");close(out.posY,64.25,"spawn snapping height");close(out.posZ,at.getZ()+.5,"spawn Z");NBTTagCompound stored=new NBTTagCompound();out.writeToNBT(stored);ItemStack restored=new ItemStack(stored.getCompoundTag("Item"));check(ItemStack.areItemStacksEqual(result,restored),"real moving entity NBT roundtrip");}
   check(count==13&&stack.getCount()==13,"input/output conservation");original.enterBlock(block,new com.hbm.util.fauxpointtwelve.BlockPos(8,64,8),forward);check(w.spawned.size()==2,"already dead entity cannot split twice");
   for(int amount:new int[]{0,1}){w.spawned.clear();TileEntityCraneSplitter single=tile(w,POS);EntityMovingItem incoming=new EntityMovingItem(w);incoming.setItemStack(new ItemStack(Items.STICK,amount));incoming.enterBlock(block,new com.hbm.util.fauxpointtwelve.BlockPos(8,64,8),forward);check(incoming.isDead&&w.spawned.size()==amount,"zero and empty right halves never create entities");if(amount==1)check(((EntityMovingItem)w.spawned.get(0)).getItemStack().getCount()==1,"single-item count conserved");}w.tiles.put(POS,te);
   te.leftRatio=16;te.rightRatio=16;player.setSneaking(false);check(block.onScrew(w,player,8,64,8,0,0,0,0,ToolType.SCREWDRIVER)&&te.leftRatio==16,"left tool upper clamp");player.setSneaking(true);block.onScrew(w,player,other.getX(),other.getY(),other.getZ(),0,0,0,0,ToolType.SCREWDRIVER);check(te.rightRatio==15&&te.leftRatio==16,"right tool changes right ratio only");te.leftRatio=1;block.onScrew(w,player,8,64,8,0,0,0,0,ToolType.SCREWDRIVER);check(te.leftRatio==1,"tool lower clamp");check(!block.onScrew(w,player,8,64,8,0,0,0,0,ToolType.WRENCH),"other tool rejected");
  }
  check(block.onScrew(new NativeWorld(true),player,8,64,8,0,0,0,0,ToolType.WRENCH),"original client tool branch acknowledges before type check");check(!block.canPackageEnter(w,8,64,8,ForgeDirection.NORTH,null),"original package gate");block.onPackageEnter(w,8,64,8,ForgeDirection.NORTH,null);check(!block.onBlockActivated(w,POS,block.getDefaultState(),player,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0),"original no GUI activation");
  LegacySplitterLogic oracle=new LegacySplitterLogic();LegacySplitterLogic.MoveWorld ow=new LegacySplitterLogic.MoveWorld();
  for(int meta=0;meta<16;meta++){w.states.put(POS,block.getStateFromMeta(meta));ow.metadata=meta;for(double x:new double[]{7.7,8,8.2,8.5,9,9.2})for(double z:new double[]{7.9,8,8.9,9.1}){Vec3d input=new Vec3d(x,64.3,z);Vec3d got=block.getTravelLocation(w,8,64,8,input,.125);LegacySplitterLogic.MutableVec want=oracle.getTravelLocation(ow,8,64,8,new LegacySplitterLogic.MutableVec(x,64.3,z),.125);close(got.x,want.xCoord,"pristine mutable snapping X");close(got.y,want.yCoord,"pristine mutable snapping Y");close(got.z,want.zCoord,"pristine mutable snapping Z");check(input.x==x&&input.z==z,"native Vec3d remains immutable");}}
 }
 static final class LightingWorld implements IBlockAccess {
  final IBlockState state;final int own=0x00A00070;LightingWorld(IBlockState state){this.state=state;}
  public TileEntity getTileEntity(BlockPos p){return null;}public int getCombinedLight(BlockPos p,int e){return p.equals(POS)?own:0x00100010*((Math.abs(p.getX()+2*p.getY()+3*p.getZ())%8)+1);}
  public IBlockState getBlockState(BlockPos p){return p.equals(POS)?state:Blocks.STONE.getDefaultState();}public boolean isAirBlock(BlockPos p){return false;}
  public Biome getBiome(BlockPos p){return Biomes.PLAINS;}public int getStrongPower(BlockPos p,EnumFacing f){return 0;}public WorldType getWorldType(){return WorldType.DEFAULT;}public boolean isSideSolid(BlockPos p,EnumFacing f,boolean b){return !p.equals(POS);}
 }
 static void lighting()throws Exception {
  CraneSplitter owner=new CraneSplitter();net.minecraft.client.renderer.color.BlockColors colors=new net.minecraft.client.renderer.color.BlockColors();ObjMesh mesh=readMesh();
  for(int meta=0;meta<16;meta++){IBlockState state=owner.getStateFromMeta(meta);LightingWorld world=new LightingWorld(state);IBakedModel model=new AtlasObjBakedModel(RenderSplitter.bake(mesh,meta,false,sprites),sprites[0]);check(!model.isAmbientOcclusion(),"original normal shader not AO");List<BakedQuad> quads=model.getQuads(state,null,0);
   for(boolean forge:new boolean[]{false,true}){BufferBuilder out=new BufferBuilder(65536);out.begin(7,DefaultVertexFormats.BLOCK);
    if(forge){net.minecraftforge.client.model.pipeline.VertexLighterFlat flat=new net.minecraftforge.client.model.pipeline.VertexLighterFlat(colors);net.minecraftforge.client.model.pipeline.VertexBufferConsumer sink=new net.minecraftforge.client.model.pipeline.VertexBufferConsumer(out);sink.setOffset(POS);flat.setParent(sink);flat.setWorld(world);flat.setState(state);flat.setBlockPos(POS);flat.updateBlockInfo();for(BakedQuad q:quads)q.pipe(flat);flat.resetBlockInfo();}
    else check(new BlockModelRenderer(colors).renderModelFlat(world,model,state,POS,out,true,0),"vanilla fallback emits geometry");
    out.finishDrawing();check(out.getVertexCount()==quads.size()*4,"native emitted vertex count");java.nio.ByteBuffer data=out.getByteBuffer();for(int v=0;v<out.getVertexCount();v++){check(data.getInt(v*28+24)==world.own,"both flat pipelines sample owner brightness");check(data.getInt(v*28+12)==quads.get(v/4).getVertexData()[v%4*7+3],"original normal shade not multiplied again");}
   }
   BufferBuilder damage=NetworkBreakingRenderer.buildBuffer(world,state,POS,quads,new Sprite("crack",14),false,true,new BlockModelRenderer(colors),0,0,0);check(damage.getVertexCount()==quads.size()*4&&damage.getVertexFormat().getColorOffset()<0,"splitter damage retains complete original colorless draw");List<BakedQuad> damageQuads=NetworkBreakingRenderer.breakingQuads(quads,new Sprite("crack",14),false);for(int i=0;i<quads.size();i++)check(damageQuads.get(i)==quads.get(i),"original direct icons ignore override cracks");for(int v=0;v<damage.getVertexCount();v++)check(damage.getByteBuffer().getInt(v*24+20)==world.own,"damage retains owner lightmap");
  }
 }
 static Object get(Object target,Class<?> type,String name)throws Exception{java.lang.reflect.Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(target);}
 static void set(Object target,Class<?> type,String name,Object value)throws Exception{java.lang.reflect.Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
 static void registry()throws Exception {
  CraneSplitter block=new CraneSplitter();ForgeRegistries.BLOCKS.register(block.setRegistryName("hbm","tile.crane_splitter"));ForgeRegistries.ITEMS.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
  Block foreign=new CraneSplitter().setRegistryName("foreign","tile.crane_splitter"),subclass=new CraneSplitter(){}.setRegistryName("hbm","foreign_splitter"),unknown=new CraneSplitter().setRegistryName("hbm","other_splitter");for(Block b:new Block[]{foreign,subclass,unknown})ForgeRegistries.BLOCKS.register(b);
  SimpleReloadableResourceManager manager=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());manager.reloadResources(Collections.singletonList(new FolderResourcePack(resources)));
  sun.misc.Unsafe unsafe=(sun.misc.Unsafe)get(null,sun.misc.Unsafe.class,"theUnsafe");net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);set(mc,net.minecraft.client.Minecraft.class,"resourceManager",manager);
  net.minecraft.launchwrapper.Launch.blackboard=new HashMap<>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",true);
  TextureMap atlas=new TextureMap("textures");new ModelLoader(manager,atlas,new BlockModelShapes(new ModelManager(atlas)));
  SplitterModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());Map<?,?> states=(Map<?,?>)get(null,ModelLoader.class,"customStateMappers"),items=(Map<?,?>)get(null,ModelLoader.class,"customModels");check(items.size()==16,"all sixteen native item routes installed");
  for(Block b:new Block[]{foreign,subclass,unknown})check(!CraneSplitter.isNativeModelOwner(b)&&!states.containsKey(b.delegate),"foreign subclass and unknown owner untouched");
  net.minecraft.client.renderer.block.statemap.IStateMapper mapper=(net.minecraft.client.renderer.block.statemap.IStateMapper)states.get(block.delegate);check(mapper!=null,"native world mapper");Map<IBlockState,ModelResourceLocation> routes=mapper.putStateModelLocations(block);check(routes.size()==16,"sixteen native world routes");
  for(Map.Entry<IBlockState,ModelResourceLocation> entry:routes.entrySet()){check(entry.getValue().toString().equals("hbm:native_splitter/"+block.getMetaFromState(entry.getKey())+"/world#normal"),"exact world metadata route");IModel loaded=ModelLoaderRegistry.getModel(entry.getValue());check(loaded!=ModelLoaderRegistry.getMissingModel(),"actual registry resolves custom OBJ loader");IBakedModel baked=loaded.bake(loaded.getDefaultState(),DefaultVertexFormats.BLOCK,id->new Sprite(id.toString(),0));check(!baked.getQuads(entry.getKey(),null,0).isEmpty(),"loader atlas bake");check(loaded.getTextures().size()==12,"eleven authored OBJ materials plus original crane_side particle declared");for(ResourceLocation id:loaded.getTextures()){IResource resource=manager.getResource(new ResourceLocation(id.getNamespace(),"textures/"+id.getPath()+".png"));check(resource.getInputStream().read()>=0,"atlas material exists");resource.close();}}
  SplitterModelHandler.Loader loader=new SplitterModelHandler.Loader();loader.onResourceManagerReload(manager);for(String bad:new String[]{"hbm:native_splitter/16/world","foreign:native_splitter/0/world","hbm:native_splitter/a/item","hbm:native_splitter/0/unknown","hbm:native_splitter/0/world/extra"})check(!loader.accepts(new ResourceLocation(bad)),"invalid route not claimed");
  ResourceLocation route=new ResourceLocation("hbm","models/block/native_splitter/0/item");check(loader.accepts(route),"Forge normalized route accepted");IModel before=loader.loadModel(route);IBakedModel baked=before.bake(before.getDefaultState(),DefaultVertexFormats.ITEM,id->new Sprite(id.toString(),0));check(baked.getQuads(null,null,0).size()==RenderSplitter.bake(readMesh(),0,true,sprites).size(),"item loader includes both halves");
  File zip=File.createTempFile("splitter-resources-",".jar");try(java.util.zip.ZipOutputStream output=new java.util.zip.ZipOutputStream(new FileOutputStream(zip))){String[] paths={"assets/hbm/models/blocks/splitter.obj","pack.mcmeta"};for(String path:paths){File f=new File(resources,path);if(!f.exists())continue;output.putNextEntry(new java.util.zip.ZipEntry(path));java.nio.file.Files.copy(f.toPath(),output);output.closeEntry();}}
  SimpleReloadableResourceManager jarManager=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());FileResourcePack pack=new FileResourcePack(zip);jarManager.reloadResources(Collections.singletonList(pack));loader.onResourceManagerReload(jarManager);IModel after=loader.loadModel(route);IBakedModel jar=after.bake(after.getDefaultState(),DefaultVertexFormats.ITEM,id->new Sprite("reloaded",1));check(before!=after&&baked.getParticleTexture()!=jar.getParticleTexture(),"folder-to-JAR reload refreshes mesh and atlas");check(jar.getQuads(null,null,0).size()==baked.getQuads(null,null,0).size(),"folder/JAR geometry parity");zip.delete();
  SplitterModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());check(((Map<?,?>)get(null,ModelLoader.class,"customModels")).size()==16,"repeat registry stable");
  TileEntityCraneSplitter tile=tile(new NativeWorld(false),POS);NativeWorld w=(NativeWorld)tile.getWorld();w.states.put(POS,block.getStateFromMeta(12));SplitterBreakingRenderer renderer=new SplitterBreakingRenderer();check(block.getDefaultState().hasCustomBreakingProgress()&&tile.canRenderBreaking()&&renderer.owns(tile,w.getBlockState(POS)),"exact native damage dispatch predicates");check(((Object)TileEntityRendererDispatcher.instance.getRenderer(tile)) instanceof SplitterBreakingRenderer,"actual ModelRegistryEvent binds splitter damage TESR");renderer.render(tile,0,0,0,0,-1,1);renderer.render(tile,0,0,0,0,10,1);
  for(Block b:new Block[]{foreign,subclass,unknown}){w.states.put(POS,b.getDefaultState());check(!b.getDefaultState().hasCustomBreakingProgress()&&!tile.canRenderBreaking()&&!renderer.owns(tile,w.getBlockState(POS)),"unknown mod damage ownership stays native");renderer.render(tile,0,0,0,0,1,1);}
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(TileEntityCraneSplitter.class,new ResourceLocation("hbm","tileentity_splitter"));resources=new File(args[0],"src/main/resources");for(int i=0;i<sprites.length;i++)sprites[i]=new Sprite("splitter"+i,i);geometry(new File(args[1]));stackMachine();owners();lighting();registry();System.out.println("PASS "+checks+" native assertions; "+cases+" pristine renderer cases. Complete owner/entity/data/NBT/ratio/tool/multiblock/model/registry and folder/JAR loader paths executed. No live GL/game or unknown-mod visual acceptance.");
 }
}

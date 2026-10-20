import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.hbm.blocks.network.*;
import com.hbm.tileentity.network.*;
import com.hbm.render.block.*;
import com.hbm.render.block.nativeblock.*;
import com.hbm.render.mesh.*;
import com.hbm.render.tileentity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.*;
import com.hbm.main.RadioTorchModelHandler;
import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
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
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.property.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class RadioTorchOwnerModelTest {
 static long checks;static final BlockPos POS=new BlockPos(8,64,8);
 static final String[] NAMES={"sender","receiver","counter","logic","reader","controller"};
 static final String[] OFF={"rtty_sender_off","rtty_rec_off","rtty_counter","rtty_logic_off","rtty_reader","rtty_controller"};
 static final String[] ON={"rtty_sender_on","rtty_rec_on","rtty_counter","rtty_logic_on","rtty_reader","rtty_controller"};
 static RadioTorchBase[] blocks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static class Sprite extends TextureAtlasSprite {Sprite(String n,int i){super(n);setIconWidth(16);setIconHeight(16);initSprite(512,512,i*32,32,false);}}
 static final Map<String,TextureAtlasSprite> sprites=new HashMap<>();
 static TextureAtlasSprite sprite(String n){return sprites.computeIfAbsent(n,k->new Sprite(k,sprites.size()));}
 static Object field(Object o,Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();int updates;List<net.minecraft.entity.Entity> drops=new ArrayList<>();
  NativeWorld(boolean client){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),client);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "radio native probe";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return chunks.containsKey(key(x,z));}
  public boolean checkLight(BlockPos p){return true;}
  public int getCombinedLight(BlockPos p,int emission){return p.equals(POS)?0x00a00070:0x00200020;}
  public boolean spawnEntity(net.minecraft.entity.Entity e){drops.add(e);return true;}
 }
 static void setup()throws Exception {
  Bootstrap.register();blocks=new RadioTorchBase[]{new RadioTorchSender(),new RadioTorchReceiver(),new RadioTorchCounter(),new RadioTorchLogic(),new RadioTorchReader(),new RadioTorchController()};Map<Block,Item> map=(Map<Block,Item>)field(null,Item.class,"BLOCK_TO_ITEM");
  for(int i=0;i<blocks.length;i++){RadioTorchBase b=blocks[i];b.setBlockName("radio_torch_"+NAMES[i]);ForgeRegistries.BLOCKS.register(b);ItemBlock item=new ItemBlock(b);item.setRegistryName(b.getRegistryName());ForgeRegistries.ITEMS.register(item);map.put(b,item);}
  Field u=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");u.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)u.get(null);net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);Field instance=net.minecraft.client.Minecraft.class.getDeclaredField("instance");instance.setAccessible(true);instance.set(null,mc);Field resources=net.minecraft.client.Minecraft.class.getDeclaredField("resourceManager");resources.setAccessible(true);resources.set(mc,new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer()));
  RadioTorchModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());
 }
 static void owners()throws Exception {
  NativeWorld w=new NativeWorld(false);
  for(int i=0;i<blocks.length;i++)for(int meta=0;meta<16;meta++){
   RadioTorchBase b=blocks[i];IBlockState state=b.getStateFromMeta(meta);w.setBlockState(POS,state,2);
   check(b.getMetaFromState(w.getBlockState(POS))==meta,"native chunk metadata "+i+"/"+meta);
   check(!state.isOpaqueCube()&&!state.isFullCube()&&state.getRenderType()==EnumBlockRenderType.MODEL,"native model shape");
   check(state.getCollisionBoundingBox(w,POS)==null&&b.getRenderLayer()==BlockRenderLayer.CUTOUT,"no collision and cutout");
   com.hbm.lib.ForgeDirection d=com.hbm.lib.ForgeDirection.getOrientation(meta&7);
   check(state.getBoundingBox(w,POS).equals(new AxisAlignedBB(d.offsetX==1?0:.375,d.offsetY==1?0:.375,d.offsetZ==1?0:.375,d.offsetX==-1?1:.625,d.offsetY==-1?1:.625,d.offsetZ==-1?1:.625)),"original six-side selection");
   for(EnumFacing side:EnumFacing.values())check(state.getBlockFaceShape(w,POS,side)==BlockFaceShape.UNDEFINED&&b.shouldSideBeRendered(state,w,POS,side),"no hidden faces/attachment");
   TileEntity tile=w.getTileEntity(POS);check(tile!=null&&b.hasTileEntity(state),"native actual Chunk auto-created tile");
   IBlockState off=b.getExtendedState(state,w,POS);
   if(tile instanceof TileEntityRadioTorchBase)((TileEntityRadioTorchBase)tile).lastState=15;
   if(tile instanceof TileEntityRadioTorchLogic)((TileEntityRadioTorchLogic)tile).lastState=15;
   IBlockState on=b.getExtendedState(state,w,POS);
   check(!((IExtendedBlockState)off).getValue(RadioTorchModelState.ACTIVE),"old snapshot immutable");
   check(((IExtendedBlockState)on).getValue(RadioTorchModelState.ACTIVE)==(i==0||i==1||i==3),"base and separate logic snapshot");
   for(EnumFacing side:EnumFacing.values())check(b.getWeakPower(state,w,POS,side)==((i==1||i==3)?15:0),"original output value/directions");
   check(state.canProvidePower()==(i==1||i==3),"native power capability");
   w.setBlockToAir(POS);check(w.getTileEntity(POS)==null&&tile.isInvalid(),"native breakBlock removes tile");
  }
  for(RadioTorchBase b:new RadioTorchBase[]{blocks[0],blocks[1],blocks[2],blocks[3]})for(EnumFacing side:EnumFacing.values()){
   BlockPos support=POS.offset(side.getOpposite());w.setBlockToAir(POS);w.setBlockToAir(support);
   check(!b.canPlaceBlockOnSide(w,POS,side),"no air support");w.setBlockState(support,Blocks.STONE.getDefaultState(),2);check(b.canPlaceBlockOnSide(w,POS,side),"stone support");
   IBlockState placed=b.getStateForPlacement(w,POS,side,.1F,.2F,.3F,15,null);check(b.getMetaFromState(placed)==side.getIndex(),"placement ignores item meta retains clicked side");
   w.setBlockState(POS,placed,2);w.setBlockToAir(support);b.neighborChanged(placed,w,POS,Blocks.AIR,support);check(w.isAirBlock(POS),"unsupported torch drops/removes");
  }
  w.setBlockState(POS,blocks[0].getStateFromMeta(1),2);TileEntity eventTile=new TileEntity(){public boolean receiveClientEvent(int id,int parameter){return id==17&&parameter==23;}};w.setTileEntity(POS,eventTile);check(blocks[0].eventReceived(w.getBlockState(POS),w,POS,17,23),"inherited container event forwarding preserved");check(!blocks[0].eventReceived(w.getBlockState(POS),w,POS,17,22),"tile event return preserved");w.removeTileEntity(POS);check(!blocks[0].eventReceived(w.getBlockState(POS),w,POS,17,23),"missing tile event false");
  RadioTorchProgrammer programmer=new RadioTorchProgrammer();check(programmer.createNewTileEntity(w,0)==null,"unregistered programmer remains null tile");
  System.out.println("PASS native all16 metadata, six side placement/support/loss, real Chunk tile creation/removal, power callbacks and immutable dynamic state.");
 }
 static class Face {float[][] xyz,uv;}
 static List<Face> obj(File file)throws Exception {
  List<float[]> p=new ArrayList<>(),uv=new ArrayList<>();List<Face> out=new ArrayList<>();
  try(BufferedReader in=new BufferedReader(new FileReader(file))){String l;while((l=in.readLine())!=null){String[] a=l.trim().split("\\s+");
   if(a[0].equals("v"))p.add(new float[]{Float.parseFloat(a[1]),Float.parseFloat(a[2]),Float.parseFloat(a[3])});
   if(a[0].equals("vt"))uv.add(new float[]{Float.parseFloat(a[1]),1-Float.parseFloat(a[2])});
   if(a[0].equals("f")){Face f=new Face();f.xyz=new float[a.length-1][];f.uv=new float[a.length-1][];for(int i=1;i<a.length;i++){String[] k=a[i].split("/");f.xyz[i-1]=p.get(Integer.parseInt(k[0])-1);f.uv[i-1]=uv.get(Integer.parseInt(k[1])-1);}out.add(f);}
  }}return out;
 }
 static double[] rotate(float[] v,float y,float z){double c=net.minecraft.util.math.MathHelper.cos(z),s=net.minecraft.util.math.MathHelper.sin(z),x=v[0]*c+v[1]*s,yy=v[1]*c-v[0]*s;c=net.minecraft.util.math.MathHelper.cos(y);s=net.minecraft.util.math.MathHelper.sin(y);return new double[]{x*c+v[2]*s,yy,v[2]*c-x*s};}
 static void geometry(List<BakedQuad> got,List<Face> faces,int meta,int signal,boolean logic,TextureAtlasSprite texture){
  OriginalRTTYOracle.Layout layout=OriginalRTTYOracle.layout(meta,signal,logic);check(layout.icon==(signal>0?1:0)&&!layout.shade&&layout.color==-1&&layout.brightness==0x00a00070,"original texture/white/own light");
  List<float[]> expected=new ArrayList<>();for(Face face:faces)for(int i=0;i<face.xyz.length;i++){double[] v=rotate(face.xyz[i],layout.yaw,layout.flip);float[] vert={(float)(layout.x+(float)v[0]),(float)(layout.y+(float)v[1]),(float)(layout.z+(float)v[2]),texture.getInterpolatedU(face.uv[i][0]*16F),texture.getInterpolatedV(face.uv[i][1]*16F)};expected.add(vert);if(i%3==2)expected.add(vert);}
  check(got.size()==expected.size()/4,"original continuous whole OBJ stream");
  for(int q=0;q<got.size();q++){BakedQuad b=got.get(q);check(b.getSprite()==texture&&!b.hasTintIndex()&&!b.shouldApplyDiffuseLighting(),"exact icon/no diffuse/tint");int[] data=b.getVertexData();check(b.getFormat().equals(DefaultVertexFormats.BLOCK),"native BLOCK format");for(int i=0;i<4;i++){float[] v=expected.get(q*4+i);for(int k=0;k<3;k++)check(data[i*7+k]==Float.floatToIntBits(v[k]),"exact original XYZ");check(data[i*7+3]==-1,"unshaded white");check(data[i*7+4]==Float.floatToIntBits(v[3])&&data[i*7+5]==Float.floatToIntBits(v[4]),"exact original UV");}}
 }
 static class LightWorld implements IBlockAccess {
  IBlockState state;LightWorld(IBlockState s){state=s;}
  public TileEntity getTileEntity(BlockPos p){return null;}public IBlockState getBlockState(BlockPos p){return p.equals(POS)?state:Blocks.STONE.getDefaultState();}public int getCombinedLight(BlockPos p,int e){return p.equals(POS)?0x00a00070:0x00200020;}public boolean isAirBlock(BlockPos p){return false;}public net.minecraft.world.biome.Biome getBiome(BlockPos p){return Biomes.PLAINS;}public int getStrongPower(BlockPos p,EnumFacing f){return 0;}public WorldType getWorldType(){return WorldType.DEFAULT;}public boolean isSideSolid(BlockPos p,EnumFacing f,boolean b){return !p.equals(POS);}
 }
 static void light(IBakedModel model,IBlockState state){
  LightWorld w=new LightWorld(state);net.minecraft.client.renderer.color.BlockColors colors=new net.minecraft.client.renderer.color.BlockColors();
  for(boolean forge:new boolean[]{true,false}){net.minecraft.client.renderer.BufferBuilder out=new net.minecraft.client.renderer.BufferBuilder(65536);out.begin(7,DefaultVertexFormats.BLOCK);
   if(forge){net.minecraftforge.client.model.pipeline.VertexLighterFlat l=new net.minecraftforge.client.model.pipeline.VertexLighterFlat(colors);net.minecraftforge.client.model.pipeline.VertexBufferConsumer c=new net.minecraftforge.client.model.pipeline.VertexBufferConsumer(out);c.setOffset(POS);l.setParent(c);l.setWorld(w);l.setState(state);l.setBlockPos(POS);l.updateBlockInfo();for(BakedQuad q:model.getQuads(state,null,0))q.pipe(l);l.resetBlockInfo();}
   else check(new net.minecraft.client.renderer.BlockModelRenderer(colors).renderModelFlat(w,model,state,POS,out,true,0),"fallback emits");
   out.finishDrawing();for(int v=0;v<out.getVertexCount();v++){check(out.getByteBuffer().getInt(v*28+24)==0x00a00070,"block own packed light Forge="+forge);check(out.getByteBuffer().getInt(v*28+12)==-1,"white unshaded Forge="+forge);}
  }
 }
 static final class CapturingBreaking extends RadioTorchBreakingRenderer<TileEntity> {
  BufferBuilder captured;TextureMap texture;int draws;
  @Override protected void draw(BufferBuilder buffer,TextureMap atlas){captured=buffer;texture=atlas;draws++;}
 }
 static void set(Object o,Class<?> type,String name,Object value)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static void breakingDispatcher(ObjMesh mesh)throws Exception {
  sun.misc.Unsafe unsafe=(sun.misc.Unsafe)field(null,sun.misc.Unsafe.class,"theUnsafe");net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.getMinecraft();
  net.minecraft.client.settings.GameSettings settings=(net.minecraft.client.settings.GameSettings)unsafe.allocateInstance(net.minecraft.client.settings.GameSettings.class);mc.gameSettings=settings;
  TextureAtlasSprite crack=sprite("unrequested_crack");TextureMap atlas=new TextureMap("textures"){@Override public TextureAtlasSprite getAtlasSprite(String name){check(name.startsWith("minecraft:blocks/destroy_stage_"),"native stage lookup");return crack;}};
  set(mc,net.minecraft.client.Minecraft.class,"textureMapBlocks",atlas);final IBakedModel[] active={null};BlockModelShapes shapes=new BlockModelShapes(new ModelManager(atlas)){@Override public IBakedModel getModelForState(IBlockState state){return active[0];}};
  BlockRendererDispatcher renderer=(BlockRendererDispatcher)unsafe.allocateInstance(BlockRendererDispatcher.class);set(renderer,BlockRendererDispatcher.class,"blockModelShapes",shapes);set(renderer,BlockRendererDispatcher.class,"blockModelRenderer",new BlockModelRenderer(new net.minecraft.client.renderer.color.BlockColors()));set(mc,net.minecraft.client.Minecraft.class,"blockRenderDispatcher",renderer);
  TileEntityRendererDispatcher dispatcher=TileEntityRendererDispatcher.instance;
  for(int family=0;family<6;family++){
   RadioTorchBase owner=blocks[family];NativeWorld world=new NativeWorld(false);world.setBlockState(POS,owner.getStateFromMeta(1),2);TileEntity tile=world.getTileEntity(POS);
   check(owner.getDefaultState().hasCustomBreakingProgress()&&tile.canRenderBreaking(),"RenderGlobal custom route and standard damage skip predicates");check(dispatcher.getRenderer(tile) instanceof RadioTorchBreakingRenderer,"actual model handler registered exact tile TESR");
   CapturingBreaking capture=new CapturingBreaking();TileEntitySpecialRenderer<?> saved=dispatcher.renderers.put(tile.getClass(),capture);
   try {
    dispatcher.render(tile,1.25,2.5,3.75,.5F,-1,1);dispatcher.render(tile,1.25,2.5,3.75,.5F,10,1);check(capture.draws==0,"ordinary/invalid stages no render work");
    for(int meta=0;meta<16;meta++){
     world.setBlockState(POS,owner.getStateFromMeta(meta),2);tile=world.getTileEntity(POS);IBlockState state=world.getBlockState(POS);active[0]=new RadioTorchBakedModel(mesh,meta,sprite("hbm:blocks/"+OFF[family]),sprite("hbm:blocks/"+ON[family]));
     for(boolean signal:new boolean[]{false,true}){
      if(tile instanceof TileEntityRadioTorchBase)((TileEntityRadioTorchBase)tile).lastState=signal?15:0;
      if(tile instanceof TileEntityRadioTorchLogic)((TileEntityRadioTorchLogic)tile).lastState=signal?15:0;
      List<BakedQuad> expected=active[0].getQuads(owner.getExtendedState(state,world,POS),null,0);List<BakedQuad> retained=NetworkBreakingRenderer.breakingQuads(expected,crack,false);for(int i=0;i<expected.size();i++)check(retained.get(i)==expected.get(i),"original ignores override: exact material/UV/light quad retained");
      for(boolean ambient:new boolean[]{false,true}){settings.ambientOcclusion=ambient?2:0;for(int stage:new int[]{0,5,9}){
       dispatcher.render(tile,1.25,2.5,3.75,.5F,stage,1);BufferBuilder output=capture.captured;check(capture.texture==atlas&&output.getVertexCount()==expected.size()*4,"actual dispatcher original geometry/atlas");check(output.getVertexFormat().getColorOffset()<0&&output.getVertexFormat().getSize()==24,"original damage disables colors");java.nio.ByteBuffer data=output.getByteBuffer();
       for(int v=0;v<output.getVertexCount();v++){int[] q=expected.get(v/4).getVertexData();int at=v%4*7;for(int axis=0;axis<3;axis++){float want=Float.intBitsToFloat(q[at+axis])+new float[]{1.25F,2.5F,3.75F}[axis];check(Math.abs(data.getFloat(v*24+axis*4)-want)<.000001F,"camera-relative original damage geometry");}check(data.getInt(v*24+12)==q[at+4]&&data.getInt(v*24+16)==q[at+5],"original icon UV, no crack replacement");check(data.getInt(v*24+20)==0x00a00070,"original own-position damage light");}
      }}
     }
    }
    int count=capture.draws;
    Block[] excluded={new RadioTorchSender().setRegistryName("foreign","tile.radio_torch_sender"),new RadioTorchSender(){}.setRegistryName("hbm","other_sender"),new RadioTorchSender().setRegistryName("hbm","other_sender_name"),new RadioTorchProgrammer()};
    for(Block foreign:excluded){world.setBlockState(POS,foreign.getDefaultState(),2);check(!foreign.getDefaultState().hasCustomBreakingProgress()&&!tile.canRenderBreaking()&&!capture.owns(tile,world.getBlockState(POS)),"foreign/subclass/unregistered/programmer not claimed");dispatcher.render(tile,1,2,3,.5F,4,1);check(capture.draws==count,"unowned damage not rendered");}
    check(!capture.owns(new TileEntityRadioTorchBase(),owner.getDefaultState()),"base class is not registered tile owner");
    world.setBlockState(POS,owner.getDefaultState(),2);TileEntityRadioTorchSender subclass=new TileEntityRadioTorchSender(){};subclass.setWorld(world);subclass.setPos(POS);check(!subclass.canRenderBreaking()&&!capture.owns(subclass,owner.getDefaultState()),"tile subclasses not silently claimed");
   } finally {dispatcher.renderers.put(tile.getClass(),saved);}
  }
  System.out.println("PASS original non-retextured breaking: actual handler and native dispatcher, six exact block/tile owners x16 metadata x2 states x2 AO x3 damage stages; exact original-icon UV/geometry/light, colorless output, ordinary/invalid/unowned no-op.");
 }
 static void legacyRotations(File jar,List<Face> faces)throws Exception {
  try(java.net.URLClassLoader cl=new java.net.URLClassLoader(new java.net.URL[]{jar.toURI().toURL()},RadioTorchOwnerModelTest.class.getClassLoader())){Class<?> v=Class.forName("azw",true,cl);Method create=v.getMethod("a",double.class,double.class,double.class),x=v.getMethod("a",float.class),z=v.getMethod("c",float.class),y=v.getMethod("b",float.class);Field fx=v.getField("a"),fy=v.getField("b"),fz=v.getField("c");for(int meta=0;meta<16;meta++){OriginalRTTYOracle.Layout layout=OriginalRTTYOracle.layout(meta,0,false);for(Face face:faces)for(float[] point:face.xyz){Object actual=create.invoke(null,(double)point[0],(double)point[1],(double)point[2]);x.invoke(actual,0F);z.invoke(actual,layout.flip);y.invoke(actual,layout.yaw);double[] want=rotate(point,layout.yaw,layout.flip);check(Double.doubleToLongBits(fx.getDouble(actual))==Double.doubleToLongBits(want[0])&&Double.doubleToLongBits(fy.getDouble(actual))==Double.doubleToLongBits(want[1])&&Double.doubleToLongBits(fz.getDouble(actual))==Double.doubleToLongBits(want[2]),"original 1.7 Vec3 actual bytecode rotation");}}}
 }
 static void model(File resources,File pristine,File legacy)throws Exception {
  File asset=new File(pristine,"assets/hbm/models/blocks/rtty.obj");ObjMesh mesh;try(Reader in=new FileReader(asset)){mesh=ObjMeshParser.parse(in,asset.toString(),false);}List<Face> faces=obj(asset);legacyRotations(legacy,faces);breakingDispatcher(mesh);
  for(int owner=0;owner<6;owner++)for(int meta=0;meta<16;meta++){
   TextureAtlasSprite off=sprite("hbm:blocks/"+OFF[owner]),on=sprite("hbm:blocks/"+ON[owner]);RadioTorchBakedModel b=new RadioTorchBakedModel(mesh,meta,off,on);check(!b.isAmbientOcclusion()&&!b.isGui3d()&&!b.isBuiltInRenderer(),"nonAO native world model");
   for(boolean active:new boolean[]{false,true}){IBlockState state=((IExtendedBlockState)blocks[owner].getStateFromMeta(meta)).withProperty(RadioTorchModelState.ACTIVE,active);List<BakedQuad> q=b.getQuads(state,null,0);geometry(q,faces,meta,active?15:0,owner==3,active?on:off);check(q==b.getQuads(state,null,99),"bounded stable cache");for(EnumFacing f:EnumFacing.values())check(b.getQuads(state,f,0).isEmpty(),"unculled stream");try{q.clear();throw new AssertionError("mutable quads");}catch(UnsupportedOperationException expected){checks++;}if(owner==0&&meta<6)light(b,state);}
  }
  ICustomModelLoader loader=(ICustomModelLoader)field(null,RadioTorchModelHandler.class,"LOADER");
  net.minecraft.client.resources.data.MetadataSerializer serializer=new net.minecraft.client.resources.data.MetadataSerializer();SimpleReloadableResourceManager manager=new SimpleReloadableResourceManager(serializer);manager.reloadResources(Collections.singletonList(new FolderResourcePack(resources)));loader.onResourceManagerReload(manager);
  Map<?,?> custom=(Map<?,?>)field(null,ModelLoader.class,"customModels");
  for(int i=0;i<6;i++){
   com.google.gson.JsonObject json=new com.google.gson.JsonParser().parse(new FileReader(new File(resources,"assets/hbm/blockstates/tile.radio_torch_"+NAMES[i]+".json"))).getAsJsonObject().getAsJsonObject("variants");
   for(int meta=0;meta<16;meta++){
    ResourceLocation route=new ResourceLocation(json.getAsJsonObject("metadata="+meta).get("model").getAsString());check(loader.accepts(route)&&loader.accepts(new ResourceLocation("hbm","block/"+route.getPath())),"actual JSON loader route");
    IModel raw=loader.loadModel(route);IBakedModel b=raw.bake(raw.getDefaultState(),DefaultVertexFormats.BLOCK,id->sprite(id.toString()));check(b instanceof RadioTorchBakedModel,"actual loader bakes native owner");
    ModelResourceLocation item=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(Item.getItemFromBlock(blocks[i]).delegate,meta));check(item!=null&&loader.accepts(item),"actual all16 item registrations");IModel itemModel=loader.loadModel(item);check(itemModel instanceof ItemLayerModel&&itemModel.getTextures().contains(new ResourceLocation("hbm","blocks/"+OFF[i])),"original flat inventory off icon");
   }
  }
  for(String invalid:new String[]{"native_rtty/tile.radio_torch_programmer/item","native_rtty/tile.radio_torch_sender/16/world","native_rtty/tile.radio_torch_sender/-1/world","native_rtty/tile.radio_torch_sender/1/world/extra"})check(!loader.accepts(new ResourceLocation("hbm",invalid)),"reject invented route");
  System.out.println("PASS independent pinned RenderRTTY/OBJ oracle: six owners x16 metadata x2 signal states; native Forge and vanilla flat packed light/white; exact loader/JSON/flat item registrations.");
 }
 public static void main(String[] args)throws Exception {setup();owners();model(new File(args[0]),new File(args[1]),new File(args[2]));System.out.println("PASS radio owner/model "+checks+" assertions. No live GL/full-game claim.");}
}

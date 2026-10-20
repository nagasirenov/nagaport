import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;
import javax.vecmath.Matrix4f;
import com.mojang.authlib.GameProfile;
import com.hbm.blocks.generic.BlockDecoCRT;
import com.hbm.main.CRTModelHandler;
import com.hbm.render.block.RenderCRT;
import com.hbm.render.block.nativeblock.*;
import com.hbm.render.mesh.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.client.model.pipeline.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public final class CRTNativeTest {
 static long checks,vertices;static final BlockPos POS=new BlockPos(8,64,8);static BlockDecoCRT block;
 static sun.misc.Unsafe unsafe;static SimpleReloadableResourceManager resources;static List<Part> original;static ObjMesh originalMesh;
 static final Map<String,TextureAtlasSprite> sprites=new LinkedHashMap<>();
 static final String[] VARIANTS={"crt_clean","crt_broken","crt_blinking","crt_bsod"};
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static void close(float got,float want,String label){check(Math.abs(got-want)<0.000003F,label+": "+got+" != "+want);}
 static Field field(Class<?> c,String name)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}
 static Object get(Object o,Class<?> c,String name)throws Exception{return field(c,name).get(o);}
 static void set(Object o,Class<?> c,String name,Object v)throws Exception{field(c,name).set(o,v);}
 interface Throwing {void run()throws Exception;}
 static void rejects(Throwing f,String message)throws Exception{boolean yes=false;try{f.run();}catch(Exception expected){yes=true;}check(yes,message);}
 static final class Sprite extends TextureAtlasSprite {Sprite(String name,int index){super(name);setIconWidth(16);setIconHeight(16);initSprite(2048,128,16*index,32,false);}}
 static TextureAtlasSprite sprite(String name){return sprites.computeIfAbsent(name,k->new Sprite(k,sprites.size()+1));}
 static TextureAtlasSprite variant(int i){return sprite("hbm:blocks/"+VARIANTS[i]);}
 static final class Face {float[][] xyz,uv;float[] normal;}
 static final class Part {String name;List<Face> faces=new ArrayList<>();Part(String n){name=n;}}
 static List<Part> originalObj(Reader source)throws Exception {
  List<float[]> points=new ArrayList<>(),uvs=new ArrayList<>();List<Part> groups=new ArrayList<>();BufferedReader in=new BufferedReader(source);String line;Part group=null;
  while((line=in.readLine())!=null){String[] p=line.trim().split("\\s+");if(p[0].equals("o")||p[0].equals("g")){group=new Part(p[1]);groups.add(group);}
   else if(p[0].equals("v"))points.add(new float[]{Float.parseFloat(p[1]),Float.parseFloat(p[2]),Float.parseFloat(p[3])});
   else if(p[0].equals("vt"))uvs.add(new float[]{Float.parseFloat(p[1]),1-Float.parseFloat(p[2])});
   else if(p[0].equals("f")){check(p.length==4||p.length==5,"original triangle/quad primitive");if(group==null){group=new Part("Default");groups.add(group);}Face f=new Face();f.xyz=new float[p.length-1][];f.uv=new float[p.length-1][];
    for(int i=1;i<p.length;i++){String[] k=p[i].split("/");f.xyz[i-1]=points.get(Integer.parseInt(k[0])-1);f.uv[i-1]=uvs.get(Integer.parseInt(k[1])-1);}
    double ax=f.xyz[1][0]-f.xyz[0][0],ay=f.xyz[1][1]-f.xyz[0][1],az=f.xyz[1][2]-f.xyz[0][2],bx=f.xyz[2][0]-f.xyz[0][0],by=f.xyz[2][1]-f.xyz[0][1],bz=f.xyz[2][2]-f.xyz[0][2];
    double nx=ay*bz-az*by,ny=az*bx-ax*bz,nz=ax*by-ay*bx,length=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);
    f.normal=length<1E-4D?new float[3]:new float[]{(float)(nx/length),(float)(ny/length),(float)(nz/length)};group.faces.add(f);
   }
  }return groups;
 }
 static double[] rotate(float[] v,float yaw,float pitch){
  double cz=net.minecraft.util.math.MathHelper.cos(pitch),sz=net.minecraft.util.math.MathHelper.sin(pitch),cy=net.minecraft.util.math.MathHelper.cos(yaw),sy=net.minecraft.util.math.MathHelper.sin(yaw);
  double x=v[0]*cz+v[1]*sz,y=v[1]*cz-v[0]*sz;return new double[]{x*cy+v[2]*sy,y,v[2]*cy-x*sy};
 }
 static final class Vertex {float x,y,z,u,v,nx,ny,nz;int color;TextureAtlasSprite sprite;}
 static List<Vertex> stream(List<Part> parts,String group,TextureAtlasSprite sprite,boolean item,float yaw,float pitch,double scale,double x,double y,double z){
  List<Vertex> out=new ArrayList<>();List<Part> selected=parts;
  if(group!=null){Part last=null;for(Part p:parts)if(p.name.equals(group))last=p;selected=last==null?Collections.emptyList():Collections.singletonList(last);}
  for(Part p:selected)for(Face f:p.faces){double[] n=rotate(f.normal,yaw,pitch);int color=OriginalCRTOracle.color(n,!item,group==null);
   for(int i=0;i<f.xyz.length;i++){double[] q=rotate(f.xyz[i],yaw,pitch);Vertex v=new Vertex();v.x=(float)(x+(float)q[0]*scale);v.y=(float)(y+(float)q[1]*scale);v.z=(float)(z+(float)q[2]*scale);v.nx=(float)n[0];v.ny=(float)n[1];v.nz=(float)n[2];v.color=color;v.sprite=sprite;
    v.u=sprite.getInterpolatedU(group==null?f.uv[i][0]*16F:f.uv[i][0]*16D);v.v=sprite.getInterpolatedV(group==null?f.uv[i][1]*16F:f.uv[i][1]*16D);out.add(v);
    if(i%3==2&&(group==null||f.xyz.length==3))out.add(v);
   }
  }return out;
 }
 static void geometry(List<BakedQuad> actual,List<Vertex> want,boolean item,String context){
  check(actual.size()==want.size()/4,context+" original stream quad count "+actual.size()+" != "+want.size()/4);
  for(int i=0;i<actual.size();i++){BakedQuad q=actual.get(i);String label=context+" quad="+i;int[] bytes=q.getVertexData();
   check(q.getSprite()==want.get(i*4).sprite,label+" original material/part order");check(q.shouldApplyDiffuseLighting()==item&&!q.hasTintIndex(),label+" native diffuse and untinted RGB");check(q.getFormat().equals(item?DefaultVertexFormats.ITEM:DefaultVertexFormats.BLOCK)&&bytes.length==28,label+" real native vertex format");
   for(int v=0;v<4;v++){int k=v*7;Vertex e=want.get(i*4+v);vertices++;
    check(Float.floatToIntBits(e.x)==bytes[k]&&Float.floatToIntBits(e.y)==bytes[k+1]&&Float.floatToIntBits(e.z)==bytes[k+2],label+" exact original XYZ at vertex "+v+" actual="+Float.intBitsToFloat(bytes[k])+","+Float.intBitsToFloat(bytes[k+1])+","+Float.intBitsToFloat(bytes[k+2])+" expected="+e.x+","+e.y+","+e.z);
    check(Float.floatToIntBits(e.u)==bytes[k+4]&&Float.floatToIntBits(e.v)==bytes[k+5],label+" exact original uncontracted UV");check(e.color==bytes[k+3],label+" original whole/part shader: "+Integer.toHexString(bytes[k+3])+" != "+Integer.toHexString(e.color));
    if(item){int n=bytes[k+6];check((byte)n==(byte)(e.nx*127F)&&(byte)(n>>8)==(byte)(e.ny*127F)&&(byte)(n>>16)==(byte)(e.nz*127F),label+" original signed-byte normals");}
   }
  }
 }
 static final class LightingWorld implements IBlockAccess {
  final IBlockState state;final int own=0x00A00070;
  LightingWorld(IBlockState state){this.state=state;}
  public TileEntity getTileEntity(BlockPos p){return null;}
  public int getCombinedLight(BlockPos p,int emission){return p.equals(POS)?own:0x00100010*((Math.abs(p.getX()+2*p.getY()+3*p.getZ())%8)+1);}
  public IBlockState getBlockState(BlockPos p){return p.equals(POS)?state:Blocks.STONE.getDefaultState();}
  public boolean isAirBlock(BlockPos p){return false;}
  public net.minecraft.world.biome.Biome getBiome(BlockPos p){return Biomes.PLAINS;}
  public int getStrongPower(BlockPos p,EnumFacing side){return 0;}
  public WorldType getWorldType(){return WorldType.DEFAULT;}
  public boolean isSideSolid(BlockPos p,EnumFacing side,boolean fallback){return !p.equals(POS);}
 }
 static final class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();int flags=-1,writes;
  NativeWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "crt native proof";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return chunks.containsKey(key(x,z));}
  public boolean checkLight(BlockPos p){return true;}
  public boolean setBlockState(BlockPos p,IBlockState s,int f){flags=f;writes++;return super.setBlockState(p,s,f);}
 }
 static final class Player extends EntityPlayer {Player(World w){super(w,new GameProfile(new UUID(1,2),"crt-proof"));}public boolean isSpectator(){return false;}public boolean isCreative(){return false;}}
 static List<Vertex> oracle(int meta,boolean item,List<Part> source){
  OriginalCRTOracle.Layout layout=OriginalCRTOracle.layout(meta,item);List<Vertex> out=new ArrayList<>();
  check(layout.layers.size()==(item?1:2),"original whole item versus Monitor+Screen layer count");
  for(OriginalCRTOracle.Layer layer:layout.layers){double x=layer.x,y=layer.y,z=layer.z;if(item){x+=.5;y+=.5;z+=.5;}
   out.addAll(stream(source,layer.group,variant(layer.variant),item,layer.yaw,0,1,x,y,z));}
  return out;
 }
 static void setup()throws Exception{
  Bootstrap.register();net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="CRT native proof";md.version="probe";net.minecraftforge.fml.common.DummyModContainer c=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(c);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(c);
  block=new BlockDecoCRT(Material.IRON);ForgeRegistries.BLOCKS.register(block.setRegistryName("hbm","tile.deco_crt"));
  unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);resources=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);set(mc,net.minecraft.client.Minecraft.class,"resourceManager",resources);net.minecraft.launchwrapper.Launch.blackboard=new HashMap<>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
  rejects(()->CRTModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent()),"missing ItemBlock rejects instead of binding AIR");
  ItemBlock item=new ItemBlock(block){public int getMetadata(int damage){return damage;}};item.setHasSubtypes(true);ForgeRegistries.ITEMS.register(item.setRegistryName(block.getRegistryName()));((Map<Block,Item>)get(null,Item.class,"BLOCK_TO_ITEM")).put(block,item);
 }
 static void mechanics()throws Exception{
  NativeWorld world=new NativeWorld();Player player=new Player(world);OriginalCRTOracle.Owner owner=new OriginalCRTOracle.Owner();
  check(block.getSubCount()==owner.getSubCount()&&block.getSubCount()==4,"original four variants");
  NonNullList<ItemStack> creative=NonNullList.create();block.getSubBlocks(net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS,creative);check(creative.size()==4,"complete native BlockMulti creative list");for(int v=0;v<4;v++)check(creative.get(v).getItem()==Item.getItemFromBlock(block)&&creative.get(v).getMetadata()==v,"creative exact variant item");
  for(int meta=0;meta<16;meta++){IBlockState state=block.getStateFromMeta(meta);check(block.getMetaFromState(state)==meta,"16 actual native state round trips");check(state.getRenderType()==EnumBlockRenderType.MODEL&&!state.isFullCube()&&!state.isOpaqueCube(),"MODEL nonfull nonopaque");check(!block.hasTileEntity(state)&&block.getRenderLayer()==BlockRenderLayer.SOLID,"ordinary solid non-tile owner");check(block.damageDropped(state)==owner.damageDropped(meta),"pristine dropped variant");check(block.getItemDropped(state,new Random(7),0)==Item.getItemFromBlock(block),"own drop identity");world.setBlockState(POS,state,2);ItemStack pick=block.getPickBlock(state,new RayTraceResult(new Vec3d(8,64,8),EnumFacing.UP,POS),world,POS,player);check(pick.getItem()==Item.getItemFromBlock(block)&&pick.getMetadata()==owner.damageDropped(meta),"native pick preserves variant not orientation");
   for(int mode=-1;mode<=4;mode++)check(block.transformMeta(meta,mode)==owner.transformMeta(meta,mode),"pristine structure transform");
  }
  for(int meta=-64;meta<=64;meta++)check(block.damageDropped(meta)==owner.damageDropped(meta),"original signed/out-of-range drop formula");
  for(int variant=0;variant<4;variant++)for(int half=-2160;half<=2160;half++){float yaw=half*.5F;player.rotationYaw=yaw;world.setBlockState(POS,block.getDefaultState(),2);world.writes=0;block.onBlockPlacedBy(world,POS,world.getBlockState(POS),player,new ItemStack(Item.getItemFromBlock(block),1,variant));OriginalCRTOracle.World expected=OriginalCRTOracle.placed(variant,yaw);check(block.getMetaFromState(world.getBlockState(POS))==expected.meta&&world.writes==expected.writes&&world.flags==expected.flags,"original four variant placement yaw rounding and notify flags");}
  System.out.println("PASS complete CRT owner, shared BlockMulti/BlockBase, creative/drop/pick, 16 states, 17,284 actual World/Chunk placements and pristine structure transforms");
 }
 static ResourceLocation route(int meta,boolean item){return new ResourceLocation("hbm","native_crt/tile.deco_crt/"+meta+(item?"/item":"/world"));}
 static IBakedModel bake(ResourceLocation id,int meta,boolean item)throws Exception{
  IModel model=ModelLoaderRegistry.getModel(id);check(model!=ModelLoaderRegistry.getMissingModel(),"native custom loader "+id);Set<ResourceLocation> want=Collections.singleton(new ResourceLocation("hbm","blocks/"+VARIANTS[item?meta:meta/4]));check(new HashSet<>(model.getTextures()).equals(want),"exact single original variant texture");check(model.getDependencies().isEmpty(),"no synthetic geometry dependencies");IBakedModel result=model.bake(model.getDefaultState(),item?DefaultVertexFormats.ITEM:DefaultVertexFormats.BLOCK,r->{check(want.contains(r),"native sprite request bound");return sprite(r.toString());});
  check(!result.isAmbientOcclusion()&&result.isGui3d()&&!result.isBuiltInRenderer(),"flat native 3D baked model");check(result.getParticleTexture()==variant(item?meta:meta/4)&&result.getOverrides()==ItemOverrideList.NONE,"particle and no overrides");for(EnumFacing side:EnumFacing.values())check(result.getQuads(null,side,0).isEmpty(),"unculled general bucket");List<BakedQuad> q=result.getQuads(null,null,0);check(q==result.getQuads(null,null,7),"stable immutable quads");rejects(()->q.add(null),"immutable quad list");return result;
 }
 static int[][] render(IBakedModel model,IBlockState state,boolean forge,boolean smooth)throws Exception{
  LightingWorld world=new LightingWorld(state);BlockColors colors=new BlockColors();BufferBuilder out=new BufferBuilder(65536);out.begin(7,DefaultVertexFormats.BLOCK);
  if(forge){VertexLighterFlat lighter=smooth?new VertexLighterSmoothAo(colors):new VertexLighterFlat(colors);VertexBufferConsumer consumer=new VertexBufferConsumer(out);consumer.setOffset(POS);lighter.setParent(consumer);lighter.setWorld(world);lighter.setState(state);lighter.setBlockPos(POS);lighter.updateBlockInfo();for(BakedQuad q:model.getQuads(state,null,0))q.pipe(lighter);lighter.resetBlockInfo();}
  else check(new BlockModelRenderer(colors).renderModelFlat(world,model,state,POS,out,true,0),"vanilla fallback emits CRT");
  out.finishDrawing();check(out.getVertexCount()==model.getQuads(state,null,0).size()*4,"native draw complete vertex count");int[][] data=new int[out.getVertexCount()][7];for(int v=0;v<data.length;v++)for(int e=0;e<7;e++)data[v][e]=out.getByteBuffer().getInt(v*28+e*4);return data;
 }
 static void lighting(IBakedModel model,int meta)throws Exception{
  IBlockState state=block.getStateFromMeta(meta);List<BakedQuad> q=model.getQuads(state,null,0);List<OriginalCRTOracle.Layer> layers=OriginalCRTOracle.layout(meta,false).layers;int monitorVertices=0;for(Part p:original)if(p.name.equals("Monitor"))for(Face f:p.faces)monitorVertices+=f.xyz.length==3?4:f.xyz.length;
  for(boolean smooth:new boolean[]{false,true}){int[][] data=render(model,state,true,smooth);int[][] baseline=render(new CRTBakedModel(originalMesh,meta%4,false,variant(meta/4)),state,true,smooth);for(int v=0;v<data.length;v++){int expected=smooth?baseline[v][6]:v<monitorVertices?layers.get(0).light:layers.get(1).light;if(meta>=8&&v>=monitorVertices)expected=240;check(data[v][6]==expected,"native Forge "+(smooth?"smooth":"flat")+" exact original packed light metadata="+meta+" v="+v+" actual="+Integer.toHexString(data[v][6])+" expected="+Integer.toHexString(expected));check(data[v][3]==(smooth?baseline[v][3]:q.get(v/4).getVertexData()[v%4*7+3]),"Monitor/native color unchanged; no doubled flat shader");}}
  int[][] vanilla=render(model,state,false,false);int fallbackLoss=0;for(int v=0;v<vanilla.length;v++){check(vanilla[v][6]==0x00a00070,"vanilla uses environment light for all CRT vertices");if(meta>=8&&v>=monitorVertices&&vanilla[v][6]!=240)fallbackLoss++;}
  IBakedModel damaged=net.minecraftforge.client.ForgeHooksClient.getDamageModel(model,sprite("minecraft:blocks/destroy_stage_3"),state,new LightingWorld(state),POS);List<BakedQuad> damagedQ=damaged.getQuads(state,null,0);check(damagedQ.size()==q.size(),"native damage wrapper preserves quad count");for(int i=0;i<q.size();i++)check(Arrays.equals(damagedQ.get(i).getVertexData(),new BakedQuadRetextured(q.get(i),sprite("minecraft:blocks/destroy_stage_3")).getVertexData()),"native damage retexture exact geometry and UV");int[][] damage=render(damaged,state,true,false);int damageLoss=0;for(int v=monitorVertices;v<damage.length;v++)if(meta>=8&&damage[v][6]!=240)damageLoss++;
  if(meta>=8)check(fallbackLoss>0&&damageLoss>0,"explicit gates detect vanilla and native damage wrapper light loss");
  System.out.println("PASS native CRT Flat/SmoothAo draw metadata="+meta+"; OPEN GATES vanilla screen-light loss="+fallbackLoss+", native damage wrapper loss="+damageLoss+" vertices");
 }
 static void event()throws Exception{
  Class<?> type=CRTModelHandler.class;net.minecraftforge.fml.common.Mod.EventBusSubscriber ann=type.getAnnotation(net.minecraftforge.fml.common.Mod.EventBusSubscriber.class);check(ann!=null&&ann.modid().equals("hbm")&&Arrays.equals(ann.value(),new net.minecraftforge.fml.relauncher.Side[]{net.minecraftforge.fml.relauncher.Side.CLIENT}),"automatic client-only native subscriber");int n=0;for(Method m:type.getDeclaredMethods())if(m.isAnnotationPresent(net.minecraftforge.fml.common.eventhandler.SubscribeEvent.class)&&Arrays.equals(m.getParameterTypes(),new Class<?>[]{net.minecraftforge.client.event.ModelRegistryEvent.class})){m.invoke(null,new net.minecraftforge.client.event.ModelRegistryEvent());n++;}check(n==1,"one model registry subscription");
 }
 static void reload(IResourcePack pack)throws Exception{resources.reloadResources(Collections.singletonList(pack));ModelLoaderRegistry.clearModelCache(resources);}
 static void registry(IResourcePack pack)throws Exception{
  reload(pack);TextureMap atlas=new TextureMap("textures");ModelManager holder=new ModelManager(atlas);BlockModelShapes shapes=new BlockModelShapes(holder);new ModelLoader(resources,atlas,shapes);event();reload(pack);Map<?,?> custom=(Map<?,?>)get(null,ModelLoader.class,"customModels");check(custom.size()==4,"exact four native item metadata routes");ModelLoader.onRegisterAllBlocks(shapes);Map<IBlockState,ModelResourceLocation> states=shapes.getBlockStateMapper().getVariants(block);check(states.size()==16&&new HashSet<>(states.values()).size()==16,"all native metadata state routes");ModelBlockDefinition definition;try(Reader r=new InputStreamReader(pack.getInputStream(new ResourceLocation("hbm","blockstates/tile.deco_crt.json")),StandardCharsets.UTF_8)){definition=ModelBlockDefinition.parseFromReader(r,block.getRegistryName());}
  net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> baked=new net.minecraft.util.registry.RegistrySimple<>();
  for(int meta=0;meta<16;meta++){IBlockState state=block.getStateFromMeta(meta);ModelResourceLocation mapped=states.get(state);check(mapped.getVariant().equals("meta="+meta),"native exact metadata property name");check(definition.hasVariant(mapped.getVariant()),"packaged exact state route");List<Variant> choices=definition.getVariant(mapped.getVariant()).getVariantList();check(choices.size()==1,"one geometry route");Variant v=choices.get(0);check(v.getRotation()==ModelRotation.X0_Y0&&!v.isUvLock(),"no extra rotation or UV lock");check(v.getModelLocation().getPath().equals("block/"+route(meta,false).getPath()),"exact world route");IBakedModel model=bake(v.getModelLocation(),meta,false);baked.putObject(mapped,model);geometry(model.getQuads(state,null,0),oracle(meta,false,original),false,"CRT metadata="+meta);lighting(model,meta);}
  Item item=Item.getItemFromBlock(block);for(int meta=0;meta<4;meta++){ModelResourceLocation id=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,meta));check(id!=null&&id.getPath().equals(route(meta,true).getPath())&&id.getVariant().equals("inventory"),"item model route exact");IBakedModel inventory=bake(id,meta,true);baked.putObject(id,inventory);geometry(inventory.getQuads(null,null,0),oracle(meta,true,original),true,"CRT inventory variant="+meta);perspectives(inventory);}
  for(String name:VARIANTS){ResourceLocation id=new ResourceLocation("hbm","textures/blocks/"+name+".png");check(pack.resourceExists(id),"exact-case PNG");try(InputStream in=pack.getInputStream(id)){check(ImageIO.read(in)!=null,"original texture decodes");}}check(pack.resourceExists(new ResourceLocation("hbm","textures/blocks/crt_blinking.png.mcmeta")),"blinking animation metadata packaged");
  ModelManager manager=(ModelManager)unsafe.allocateInstance(ModelManager.class);set(manager,ModelManager.class,"modelRegistry",baked);set(manager,ModelManager.class,"defaultModel",baked.getObject(states.values().iterator().next()));ItemModelMesher mesher=new ItemModelMesher(manager);ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();for(int meta=0;meta<4;meta++){ModelResourceLocation id=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,meta));check(mesher.getItemModel(new ItemStack(item,1,meta))==baked.getObject(id),"actual native ItemModelMesher variant identity");}BlockModelShapes actual=new BlockModelShapes(manager);ModelLoader.onRegisterAllBlocks(actual);actual.reloadModels();for(Map.Entry<IBlockState,ModelResourceLocation> e:states.entrySet())check(actual.getModelForState(e.getKey())==baked.getObject(e.getValue()),"actual BlockModelShapes world model identity");
  System.out.println("PASS "+pack.getPackName()+": 16 world +4 item exact vertices/materials, genuine native registry/model dispatch/draw");
 }
 static void perspectives(IBakedModel model)throws Exception{
  for(ItemCameraTransforms.TransformType type:ItemCameraTransforms.TransformType.values()){org.apache.commons.lang3.tuple.Pair<? extends IBakedModel,Matrix4f> pair=model.handlePerspective(type);check(pair.getLeft()==model,"native perspective retains selected variant model");if(type==ItemCameraTransforms.TransformType.FIXED){Matrix4f m=pair.getRight();close(m.m00,0,"fixed RY90 xx");close(m.m02,.625F,"fixed original block-helper scale, not wand scale");close(m.m11,.625F,"fixed original helper y scale");close(m.m20,-.625F,"fixed RY90 zx");close(m.m22,0,"fixed RY90 zz");close(m.m13,.005F,"fixed original ordered helper translation");check(m.m03==0&&m.m23==0&&m.m33==1,"fixed affine offsets");m.m13=100;check(model.handlePerspective(type).getRight().m13!=100,"fixed defensive matrix copy");}}
  ItemCameraTransforms t=model.getItemCameraTransforms();close(t.gui.scale.x,.625F,"native GUI block transform");close(t.ground.scale.x,.25F,"native ground block transform");close(t.getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND).scale.x,.4F,"native held block transform");
  int frameHooks=0;for(Method m:CRTModelHandler.class.getDeclaredMethods())if(m.isAnnotationPresent(net.minecraftforge.fml.common.eventhandler.SubscribeEvent.class)&&Arrays.equals(m.getParameterTypes(),new Class<?>[]{net.minecraftforge.client.event.RenderItemInFrameEvent.class}))frameHooks++;check(frameHooks==1,"one bounded native item-frame hook");
 }
 static final class AlteredPack implements IResourcePack {
  final IResourcePack original;final String missing;final boolean translate;AlteredPack(IResourcePack p,String m,boolean shift){original=p;missing=m;translate=shift;}
  boolean absent(ResourceLocation id){return id.getNamespace().equals("hbm")&&id.getPath().equals("models/blocks/"+missing+".obj");}
  public InputStream getInputStream(ResourceLocation id)throws IOException {
   if(absent(id))throw new FileNotFoundException(id.toString());if(!translate||!id.getPath().endsWith(".obj"))return original.getInputStream(id);
   StringBuilder changed=new StringBuilder();try(BufferedReader in=new BufferedReader(new InputStreamReader(original.getInputStream(id),StandardCharsets.UTF_8))){String line;while((line=in.readLine())!=null){if(line.startsWith("v ")){String[] p=line.trim().split("\\s+");line="v "+(Float.parseFloat(p[1])+.0625F)+" "+p[2]+" "+p[3];}changed.append(line).append('\n');}}
   return new ByteArrayInputStream(changed.toString().getBytes(StandardCharsets.UTF_8));
  }
  public boolean resourceExists(ResourceLocation id){return !absent(id)&&original.resourceExists(id);}public Set<String> getResourceDomains(){return original.getResourceDomains();}
  public <T extends net.minecraft.client.resources.data.IMetadataSection>T getPackMetadata(net.minecraft.client.resources.data.MetadataSerializer m,String k)throws IOException{return original.getPackMetadata(m,k);}
  public java.awt.image.BufferedImage getPackImage()throws IOException{return original.getPackImage();}public String getPackName(){return "Controlled OBJ mutation";}
 }
 static void resourcesAndIsolation(IResourcePack pack)throws Exception{
  IBakedModel before=bake(route(8,false),8,false);IModel descriptor=ModelLoaderRegistry.getModel(route(8,false));List<BakedQuad> retained=before.getQuads(null,null,0);AlteredPack altered=new AlteredPack(pack,null,true);List<Part> shifted;try(Reader r=new InputStreamReader(altered.getInputStream(new ResourceLocation("hbm","models/blocks/crt.obj")),StandardCharsets.UTF_8)){shifted=originalObj(r);}reload(altered);check(ModelLoaderRegistry.getModel(route(8,false))!=descriptor,"reload invalidates descriptor and parsed mesh");geometry(bake(route(8,false),8,false).getQuads(null,null,0),oracle(8,false,shifted),false,"changed replacement CRT OBJ");geometry(retained,oracle(8,false,original),false,"retained original immutable after reload");reload(pack);geometry(bake(route(8,false),8,false).getQuads(null,null,0),oracle(8,false,original),false,"restored pristine CRT");
  reload(new AlteredPack(pack,"crt",false));boolean failed=false;try{ModelLoaderRegistry.getModel(route(0,false));}catch(Exception expected){String reasons="";for(Throwable x=expected;x!=null;x=x.getCause())reasons+=x.toString();check(reasons.contains("crt.obj"),"missing mesh error names CRT asset");failed=true;}check(failed,"missing CRT mesh fails explicitly");reload(pack);
  ICustomModelLoader loader=(ICustomModelLoader)get(null,CRTModelHandler.class,"LOADER");for(String prefix:new String[]{"","block/","models/","models/block/"})check(loader.accepts(new ResourceLocation("hbm",prefix+route(0,false).getPath())),"loader native prefix normalization");check(!loader.accepts(new ResourceLocation("foreign",route(0,false).getPath())),"foreign namespace unclaimed");for(String bad:new String[]{"-1/world","16/world","0/world/extra","0/other","4/item","15/item","00/world"}){ResourceLocation id=new ResourceLocation("hbm","native_crt/tile.deco_crt/"+bad);check(!loader.accepts(id),"invalid route unclaimed "+bad);rejects(()->loader.loadModel(id),"invalid route explicitly rejects "+bad);}
  ForgeRegistries.BLOCKS.register(new BlockDecoCRT(Material.IRON).setRegistryName("foreign","tile.deco_crt"));ForgeRegistries.BLOCKS.register(new BlockDecoCRT(Material.IRON).setRegistryName("hbm","tile.unknown_crt"));event();check(((Map<?,?>)get(null,ModelLoader.class,"customModels")).size()==4,"unrelated owners add no item routes");
  System.out.println("PASS native CRT changed/restored/missing resource lifecycle, retained immutability, bounded loader and owner isolation");
 }
 static void controlledMesh()throws Exception{
  String fixture="v -0.25 0 0\nv 0.75 0 0\nv 0.75 1 0\nv -0.25 1 0\nv 0 0 1\nvt 0.123456 0.234567\nvt 1 0\nvt 1 1\nvt 0 1\ng Monitor\nf 1/1 2/2 3/3 4/4\ng Screen\nf 1/1 5/2 3/3 2/4\ng Monitor\nf 4/1 3/2 2/3 1/4\ng screen\nf 1/1 2/2 3/3 4/4\ng Tail\nf 1/1 2/2 3/3 4/4\n";ObjMesh mesh=ObjMeshParser.parse(new StringReader(fixture),"controlled CRT resource",false);List<Part> expected=originalObj(new StringReader(fixture));for(boolean item:new boolean[]{false,true})for(int meta=0;meta<(item?4:16);meta++){List<BakedQuad> actual=RenderCRT.bake(mesh,meta,item,variant(item?meta:meta/4));geometry(actual,oracle(meta,item,expected),item,"controlled exact-case duplicate groups and original whole stream");}
  String absent=fixture.replace("g Screen","g Missing");ObjMesh missing=ObjMeshParser.parse(new StringReader(absent),"missing original group",false);geometry(RenderCRT.bake(missing,8,false,variant(2)),oracle(8,false,originalObj(new StringReader(absent))),false,"absent Screen original silent no-op");
  System.out.println("PASS controlled CRT quadrilateral replacement stream, whole inventory tails, named world groups, duplicate last-wins and exact-case lookup");
 }
 static void wrapperIsolation()throws Exception{
  List<BakedQuad> source=RenderCRT.bake(originalMesh,0,false,variant(0));BakedQuad original=source.get(source.size()-1);int[] saved=original.getVertexData().clone();CRTPackedLightQuad lit=new CRTPackedLightQuad(original,240);check(Arrays.equals(saved,original.getVertexData()),"wrapper never mutates source vertex data");for(int v=0;v<4;v++)for(int k=0;k<7;k++)check(lit.getVertexData()[v*7+k]==(k==6?240:saved[v*7+k]),"wrapper changes only packed light bytes");
  BufferBuilder direct=new BufferBuilder(4096);direct.begin(7,DefaultVertexFormats.BLOCK);lit.pipe(new VertexBufferConsumer(direct));direct.finishDrawing();for(int v=0;v<4;v++)for(int k=0;k<7;k++)check(direct.getByteBuffer().getInt(v*28+k*4)==(k==6?240:saved[v*7+k]),"direct nonlighter consumer receives exact 240 and unchanged attributes");
  IBlockState state=block.getStateFromMeta(8);LightingWorld world=new LightingWorld(state);VertexLighterFlat lighter=new VertexLighterFlat(new BlockColors());BufferBuilder out=new BufferBuilder(4096);out.begin(7,DefaultVertexFormats.BLOCK);VertexBufferConsumer parent=new VertexBufferConsumer(out);lighter.setParent(parent);lighter.setWorld(world);lighter.setState(state);lighter.setBlockPos(POS);lighter.updateBlockInfo();lit.pipe(lighter);check(get(lighter,QuadGatheringTransformer.class,"parent")==parent,"native parent restored after wrapped draw");source.get(0).pipe(lighter);out.finishDrawing();for(int v=0;v<8;v++)check(out.getByteBuffer().getInt(v*28+24)==(v<4?240:world.own),"quad-local light override cannot leak into subsequent Monitor draw");
  BufferBuilder unused=new BufferBuilder(4096);unused.begin(7,DefaultVertexFormats.BLOCK);IVertexConsumer failing=new VertexTransformer(new VertexBufferConsumer(unused)){public void put(int e,float... values){throw new IllegalStateException("controlled consumer failure");}};lighter.setParent(failing);rejects(()->lit.pipe(lighter),"controlled native consumer throws");check(get(lighter,QuadGatheringTransformer.class,"parent")==failing,"finally restores exact native parent after output failure");
  rejects(()->new CRTPackedLightQuad(RenderCRT.bake(originalMesh,0,true,variant(0)).get(0),240),"light adapter rejects inventory format lacking UV1");System.out.println("PASS packed-light adapter direct and native consumers, source immutability, subsequent Monitor isolation, exception restoration");
 }
 static void wrongOwner(String mode)throws Exception{
  Bootstrap.register();unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);resources=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);set(mc,net.minecraft.client.Minecraft.class,"resourceManager",resources);net.minecraft.launchwrapper.Launch.blackboard=new HashMap<>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);Block wrong=mode.equals("subclass")?new BlockDecoCRT(Material.IRON){}:new Block(Material.IRON){};ForgeRegistries.BLOCKS.register(wrong.setRegistryName("hbm","tile.deco_crt"));CRTModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());check(((Map<?,?>)get(null,ModelLoader.class,"customModels")).isEmpty(),"exact registry name wrong class/subclass untouched");check(get(null,CRTModelHandler.class,"ownedItem")==null,"wrong owner has no frame hook identity");System.out.println("PASS isolated exact native owner negative: "+mode);
 }
 public static void main(String[] args)throws Exception{
  if(args.length>3){wrongOwner(args[3]);return;}setup();try(Reader r=new InputStreamReader(new FileInputStream(new File(args[2],"assets/hbm/models/blocks/crt.obj")),StandardCharsets.UTF_8)){original=originalObj(r);}try(Reader r=new InputStreamReader(new FileInputStream(new File(args[2],"assets/hbm/models/blocks/crt.obj")),StandardCharsets.UTF_8)){originalMesh=ObjMeshParser.parse(r,"pristine CRT baseline",false);}int faces=0;for(Part p:original)for(Face f:p.faces){check(f.xyz.length==3,"pristine CRT uses triangles");faces++;}System.out.println("ORIGINAL CRT: "+original.size()+" ordered groups and "+faces+" triangles");mechanics();controlledMesh();wrapperIsolation();IResourcePack folder=new FolderResourcePack(new File(args[0]));registry(folder);resourcesAndIsolation(folder);try(FileResourcePack jar=new FileResourcePack(new File(args[1]))){registry(jar);resourcesAndIsolation(jar);}System.out.println("PASS CRT native complete-owner proof: "+checks+" assertions, "+vertices+" exact complete vertices; no Minecraft/Forge or NTM owner substitutes.");System.out.println("Boundaries: CPU-headless native compile/draw only. Vanilla fallback and native damage screen brightness loss are confirmed OPEN GATES. Exact FIXED matrix verified here; separate frame-run.py covers bounded native GL ordinary/machine frames. Full-game/held/GUI visual acceptance remains OPEN.");
 }
}

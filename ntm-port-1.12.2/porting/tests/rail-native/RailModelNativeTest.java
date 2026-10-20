import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.gson.*;
import com.hbm.blocks.rail.*;
import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public final class RailModelNativeTest {
 static long checks;static final BlockPos POS=new BlockPos(8,64,8);static final List<Owner> owners=new ArrayList<>();
 static final Map<String,TextureAtlasSprite> sprites=new LinkedHashMap<>();static sun.misc.Unsafe unsafe;static SimpleReloadableResourceManager resources;
 static final class Owner {String owner,name,asset,texture,sign;transient Block block;ResourceLocation route(int meta,boolean item){return new ResourceLocation("hbm","native_rail/tile."+name+"/"+meta+"/"+(item?"item":"world"));}}
 static final class Sprite extends TextureAtlasSprite {Sprite(String name,int index){super(name);setIconWidth(16);setIconHeight(16);initSprite(2048,128,index*16,32,false);}}
 static TextureAtlasSprite sprite(ResourceLocation name){return sprites.computeIfAbsent(name.toString(),key->new Sprite(key,sprites.size()+1));}
 static void ck(boolean b,String label){checks++;if(!b)throw new AssertionError(label);}
 static void set(Object o,Class<?> c,String n,Object value)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);f.set(o,value);}
 static Object get(Object o,Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static void setup(File manifest)throws Exception{
  JsonObject data;try(Reader r=new FileReader(manifest)){data=new JsonParser().parse(r).getAsJsonObject();}
  Map<String,Block> realRegistrations=RailRegistrationProbe.create();ck(realRegistrations.size()==12,"exact12 ModBlocks registration statements invoked");
  for(JsonElement e:data.getAsJsonArray("owners")){Owner o=new Gson().fromJson(e,Owner.class);o.block=realRegistrations.get(o.name);ck(o.block.getClass().getSimpleName().equals(o.owner)&&o.block.getTranslationKey().equals("tile."+o.name),"actual shared construction and translation key");o.block.setRegistryName("hbm","tile."+o.name);ForgeRegistries.BLOCKS.register(o.block);ForgeRegistries.ITEMS.register(new ItemBlock(o.block){@Override public int getMetadata(int damage){return damage;}}.setHasSubtypes(true).setRegistryName(o.block.getRegistryName()));owners.add(o);}
  ck(owners.size()==12,"all twelve original real owners");
 }
 static void event()throws Exception{
  Class<?> c=com.hbm.main.RailModelHandler.class;net.minecraftforge.fml.common.Mod.EventBusSubscriber a=c.getAnnotation(net.minecraftforge.fml.common.Mod.EventBusSubscriber.class);ck(a!=null&&a.modid().equals("hbm")&&Arrays.equals(a.value(),new net.minecraftforge.fml.relauncher.Side[]{net.minecraftforge.fml.relauncher.Side.CLIENT}),"actual client-only subscriber");int found=0;
  for(Method m:c.getDeclaredMethods())if(m.isAnnotationPresent(net.minecraftforge.fml.common.eventhandler.SubscribeEvent.class)){ck(Modifier.isStatic(m.getModifiers())&&Arrays.equals(m.getParameterTypes(),new Class<?>[]{net.minecraftforge.client.event.ModelRegistryEvent.class}),"native model event signature");m.invoke(null,new net.minecraftforge.client.event.ModelRegistryEvent());found++;}ck(found==1,"one registration event");
 }
 static IBakedModel bake(ResourceLocation id)throws Exception{
  net.minecraftforge.client.model.IModel model=net.minecraftforge.client.model.ModelLoaderRegistry.getModel(id);ck(model!=net.minecraftforge.client.model.ModelLoaderRegistry.getMissingModel(),"native loader returns real model "+id);Set<ResourceLocation> asked=new HashSet<>();IBakedModel baked=model.bake(model.getDefaultState(),id.getPath().endsWith("item")?DefaultVertexFormats.ITEM:DefaultVertexFormats.BLOCK,r->{asked.add(r);return sprite(r);});ck(asked.equals(new HashSet<>(model.getTextures())),"all advertised sprites actually resolved");ck(!baked.isAmbientOcclusion()&&baked.isGui3d()&&!baked.isBuiltInRenderer(),"native original flat3d model");for(EnumFacing side:EnumFacing.values())ck(baked.getQuads(null,side,0).isEmpty(),"unculled general bucket");return baked;
 }
 static IBlockState signState(Owner o,int meta,int sign){IBlockState s=o.block.getStateFromMeta(meta);return s instanceof IExtendedBlockState?((IExtendedBlockState)s).withProperty(BlockRailNTM.SWITCH,sign):s;}
 static final class LightWorld implements IBlockAccess {
  final IBlockState state;final int own=0x00A00070;LightWorld(IBlockState s){state=s;}
  public TileEntity getTileEntity(BlockPos p){return null;}public int getCombinedLight(BlockPos p,int min){return p.equals(POS)?own:0x00100010*((Math.abs(p.getX()+2*p.getY()+3*p.getZ())%8)+1);}
  public IBlockState getBlockState(BlockPos p){return p.equals(POS)?state:Blocks.STONE.getDefaultState();}public boolean isAirBlock(BlockPos p){return false;}public net.minecraft.world.biome.Biome getBiome(BlockPos p){return Biomes.PLAINS;}public int getStrongPower(BlockPos p,EnumFacing f){return 0;}public WorldType getWorldType(){return WorldType.DEFAULT;}public boolean isSideSolid(BlockPos p,EnumFacing f,boolean d){return !p.equals(POS);}
 }
 static void light(IBakedModel model,IBlockState state,String label){
  LightWorld world=new LightWorld(state);net.minecraft.client.renderer.color.BlockColors colors=new net.minecraft.client.renderer.color.BlockColors();List<BakedQuad> quads=model.getQuads(state,null,0);if(quads.isEmpty())return;
  for(boolean forge:new boolean[]{false,true}){net.minecraft.client.renderer.BufferBuilder out=new net.minecraft.client.renderer.BufferBuilder(65536);out.begin(7,DefaultVertexFormats.BLOCK);
   if(forge){net.minecraftforge.client.model.pipeline.VertexLighterFlat lighter=new net.minecraftforge.client.model.pipeline.VertexLighterFlat(colors);net.minecraftforge.client.model.pipeline.VertexBufferConsumer sink=new net.minecraftforge.client.model.pipeline.VertexBufferConsumer(out);sink.setOffset(POS);lighter.setParent(sink);lighter.setWorld(world);lighter.setState(state);lighter.setBlockPos(POS);lighter.updateBlockInfo();for(BakedQuad q:quads)q.pipe(lighter);lighter.resetBlockInfo();}
   else ck(new net.minecraft.client.renderer.BlockModelRenderer(colors).renderModelFlat(world,model,state,POS,out,true,0),"vanilla emitted "+label);
   out.finishDrawing();ck(out.getVertexCount()==quads.size()*4,"native vertex count");java.nio.ByteBuffer bytes=out.getByteBuffer();for(int v=0;v<out.getVertexCount();v++){ck(bytes.getInt(v*28+24)==world.own,"original core packed light "+label+" forge="+forge+" vertex="+v+" got="+Integer.toHexString(bytes.getInt(v*28+24)));ck(bytes.getInt(v*28+12)==quads.get(v/4).getVertexData()[v%4*7+3],"original shader without doubled diffuse "+label);}
  }
 }
 static void reload(IResourcePack pack)throws Exception{resources.reloadResources(Collections.singletonList(pack));net.minecraftforge.client.model.ModelLoaderRegistry.clearModelCache(resources);}
 static void registry(IResourcePack pack)throws Exception{
  if(resources==null){Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=(sun.misc.Unsafe)f.get(null);resources=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);set(mc,net.minecraft.client.Minecraft.class,"resourceManager",resources);}
  resources.reloadResources(Collections.singletonList(pack));net.minecraft.launchwrapper.Launch.blackboard=new HashMap<String,Object>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);TextureMap atlas=new TextureMap("textures");ModelManager manager=new ModelManager(atlas);net.minecraft.client.renderer.BlockModelShapes shapes=new net.minecraft.client.renderer.BlockModelShapes(manager);new net.minecraftforge.client.model.ModelLoader(resources,atlas,shapes);event();reload(pack);net.minecraftforge.client.model.ModelLoader.onRegisterAllBlocks(shapes);
  Map<?,?> custom=(Map<?,?>)get(null,net.minecraftforge.client.model.ModelLoader.class,"customModels");ck(custom.size()==192,"all192 real ItemBlock metadata routes");net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> bakedRegistry=new net.minecraft.util.registry.RegistrySimple<>();
  for(Owner o:owners){Map<IBlockState,ModelResourceLocation> mappings=shapes.getBlockStateMapper().getVariants(o.block);ck(mappings.size()==(o.owner.equals("MachineFunnel")?1:16),"actual owner state enumeration "+o.owner);ModelBlockDefinition json;try(Reader r=new InputStreamReader(pack.getInputStream(new ResourceLocation("hbm","blockstates/tile."+o.name+".json")),StandardCharsets.UTF_8)){json=ModelBlockDefinition.parseFromReader(r,o.block.getRegistryName());}
   for(int meta=0;meta<16;meta++){IBlockState state=o.block.getStateFromMeta(meta);ModelResourceLocation mapped=mappings.get(state);ck(mapped!=null&&json.hasVariant(mapped.getVariant()),"real state JSON mapping "+o.name+meta);List<Variant> variants=json.getVariant(mapped.getVariant()).getVariantList();ck(variants.size()==1,"single route");Variant variant=variants.get(0);ck(variant.getRotation()==ModelRotation.X0_Y0&&!variant.isUvLock(),"no added rotation or UV lock");ck(variant.getModelLocation().getPath().equals("block/"+o.route(o.owner.equals("MachineFunnel")?0:meta,false).getPath()),"exact native world route");IBakedModel world=bake(variant.getModelLocation());bakedRegistry.putObject(mapped,world);
    for(int sign=0;sign<3;sign++){IBlockState extended=signState(o,meta,sign);List<BakedQuad> quads=world.getQuads(extended,null,0);ck(quads==world.getQuads(extended,null,99),"immutable cached random-independent geometry");try{quads.add(null);throw new AssertionError("mutable geometry");}catch(UnsupportedOperationException expected){checks++;}if(meta==12||meta==14||o.owner.equals("RailNarrowStraight")&&meta==0||o.owner.equals("MachineFunnel")&&meta==0)light(world,extended,o.name+"/"+meta+"/"+sign);}
    Item item=Item.getItemFromBlock(o.block);ModelResourceLocation itemRoute=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,meta));ck(itemRoute!=null&&itemRoute.getPath().equals(o.route(meta,true).getPath()),"actual inventory event route");IBakedModel inventory=bake(itemRoute);ck(!inventory.getQuads(null,null,0).isEmpty(),"all item geometries exist");bakedRegistry.putObject(itemRoute,inventory);
   }
   for(ResourceLocation texture:net.minecraftforge.client.model.ModelLoaderRegistry.getModel(o.route(12,false)).getTextures())try(InputStream in=pack.getInputStream(new ResourceLocation(texture.getNamespace(),"textures/"+texture.getPath()+".png"))){ck(javax.imageio.ImageIO.read(in)!=null,"original PNG decodes");}
  }
  ModelManager bakedManager=(ModelManager)unsafe.allocateInstance(ModelManager.class);set(bakedManager,ModelManager.class,"modelRegistry",bakedRegistry);net.minecraft.client.renderer.ItemModelMesher mesher=new net.minecraft.client.renderer.ItemModelMesher(bakedManager);net.minecraftforge.client.model.ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();for(Owner o:owners)for(int m=0;m<16;m++){Item item=Item.getItemFromBlock(o.block);ModelResourceLocation route=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,m));ck(mesher.getItemModel(new ItemStack(item,1,m))==bakedRegistry.getObject(route),"real ItemModelMesher resolves "+o.name+m);}
  System.out.println("PASS actual Forge registry/state JSON/loader/item mesher and world flat lighting: "+pack.getPackName());
 }
 static final class ChangedPack implements IResourcePack {
  final IResourcePack parent;final String missing;final boolean changed;ChangedPack(IResourcePack p,String m,boolean c){parent=p;missing=m;changed=c;}
  boolean absent(ResourceLocation r){return r.getPath().equals(missing);}
  public InputStream getInputStream(ResourceLocation r)throws IOException{if(absent(r))throw new FileNotFoundException(r.toString());if(!changed||!r.getPath().endsWith(".obj"))return parent.getInputStream(r);StringBuilder out=new StringBuilder();try(BufferedReader in=new BufferedReader(new InputStreamReader(parent.getInputStream(r),StandardCharsets.UTF_8))){String line;while((line=in.readLine())!=null){if(line.startsWith("v ")){String[] f=line.split("\\s+");line="v "+(Float.parseFloat(f[1])+.0625F)+" "+f[2]+" "+f[3];}out.append(line).append('\n');}}return new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));}
  public boolean resourceExists(ResourceLocation r){return !absent(r)&&parent.resourceExists(r);}public Set<String> getResourceDomains(){return parent.getResourceDomains();}public <T extends net.minecraft.client.resources.data.IMetadataSection>T getPackMetadata(net.minecraft.client.resources.data.MetadataSerializer m,String k)throws IOException{return parent.getPackMetadata(m,k);}public java.awt.image.BufferedImage getPackImage()throws IOException{return parent.getPackImage();}public String getPackName(){return "rail controlled resource mutation";}
 }
 static long geometryHash(IBakedModel b,IBlockState s){long hash=1;for(BakedQuad q:b.getQuads(s,null,0))hash=31*hash+Arrays.hashCode(q.getVertexData());return hash;}
 static void reloadChecks(IResourcePack pack)throws Exception{
  Map<Owner,IBakedModel> retained=new LinkedHashMap<>();Map<Owner,Long> hashes=new LinkedHashMap<>();for(Owner o:owners){IBakedModel b=bake(o.route(12,false));retained.put(o,b);hashes.put(o,geometryHash(b,signState(o,12,1)));}reload(new ChangedPack(pack,null,true));for(Owner o:owners){ck(geometryHash(bake(o.route(12,false)),signState(o,12,1))!=hashes.get(o),"changed mesh after actual resource reload "+o.asset);ck(geometryHash(retained.get(o),signState(o,12,1))==hashes.get(o),"retained mesh immutable");}reload(pack);for(Owner o:owners)ck(geometryHash(bake(o.route(12,false)),signState(o,12,1))==hashes.get(o),"restored original mesh "+o.asset);
  for(Owner o:owners){reload(new ChangedPack(pack,o.asset,false));boolean failed=false;try{bake(o.route(12,false));}catch(Exception e){StringBuilder msg=new StringBuilder();for(Throwable t=e;t!=null;t=t.getCause())msg.append(t.toString());ck(msg.toString().contains(o.asset),"missing failure names asset");failed=true;}ck(failed,"missing original mesh rejected "+o.asset);}reload(pack);
  System.out.println("PASS all12 original meshes changed/restored/missing resource reload: "+pack.getPackName());
 }
 public static void main(String[] args)throws Exception{Bootstrap.register();setup(new File(args[1]));IResourcePack folder=new FolderResourcePack(new File(args[0]));registry(folder);reloadChecks(folder);try(FileResourcePack jar=new FileResourcePack(new File(args[2]))){registry(jar);reloadChecks(jar);}System.out.println("PASS "+checks+" real native rail model registration/loader/flat-light assertions. CPU headless, not launched-game or CP37 visual approval.");}
}

import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import com.hbm.blocks.machine.SolarMirror;
import com.hbm.blocks.machine.SolarMirrorModelState;
import com.hbm.blocks.machine.SolarMirrorModelState.Snapshot;
import com.hbm.render.block.RenderMirror;
import com.hbm.render.block.nativeblock.SolarMirrorBakedModel;
import com.hbm.render.mesh.*;
import com.hbm.tileentity.machine.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.client.resources.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import solaroracle.OriginalMirrorOracle;

public final class NativeSolarMirrorTest {
 static long checks,vertices,cases;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static final class Sprite extends TextureAtlasSprite {
  Sprite(String name,String root)throws Exception{super(name);java.awt.image.BufferedImage image=javax.imageio.ImageIO.read(new File(root,"assets/hbm/textures/blocks/solar_mirror.png"));setIconWidth(image.getWidth());setIconHeight(image.getHeight());int[] rgba=image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth());setFramesTextureData(Collections.singletonList(new int[][]{rgba}));initSprite(1024,512,48,96,false);}
 }
 static Object get(Object o,Class<?> c,String name)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 static void set(Object o,Class<?> c,String name,Object value)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static void equal(float expected,int actual,String label){check(Float.floatToIntBits(expected)==actual,label+" actual="+Float.intBitsToFloat(actual)+" expected="+expected);}
 static void compare(List<BakedQuad> actual,OriginalMirrorOracle.Result expected,String context){
  check(actual.size()==expected.vertices.size()/4,context+" pristine stream count "+actual.size()+" != "+expected.vertices.size()/4);
  for(int i=0;i<actual.size();i++){
   BakedQuad q=actual.get(i);int[] data=q.getVertexData();check(data.length==28&&q.getFormat().equals(DefaultVertexFormats.BLOCK),context+" native BLOCK layout");check(!q.hasTintIndex()&&!q.shouldApplyDiffuseLighting(),context+" no second tint/diffuse shade");
   for(int v=0;v<4;v++){
    OriginalMirrorOracle.CapturedVertex e=expected.vertices.get(i*4+v);int k=v*7;String label=context+" q="+i+" v="+v;vertices++;
    equal(e.x,data[k],label+" X");equal(e.y,data[k+1],label+" Y");equal(e.z,data[k+2],label+" Z");equal(e.u,data[k+4],label+" U");equal(e.v,data[k+5],label+" V");check(e.color==data[k+3],label+" original unrotated-normal shader RGB");check(e.brightness==0x00e000a0,label+" reference captured original block brightness");
   }
   final int[] calls={0};
   q.pipe(new IVertexConsumer(){
    public VertexFormat getVertexFormat(){return DefaultVertexFormats.ITEM;}
    public void setQuadTint(int tint){check(tint==-1,"pipeline no tint");}
    public void setQuadOrientation(EnumFacing face){}
    public void setApplyDiffuseLighting(boolean diffuse){check(!diffuse,"pipeline already shaded");}
    public void setTexture(TextureAtlasSprite texture){check(texture==q.getSprite(),"pipeline exact atlas sprite");}
    public void put(int e,float... values){if(getVertexFormat().getElement(e).getUsage()==VertexFormatElement.EnumUsage.NORMAL){check(values[0]==0&&values[1]==0&&values[2]==0,"pipeline samples block position, no rotated/directional normal");calls[0]++;}}
   });check(calls[0]==4,"four block-position normal samples");
  }
  check(expected.tx==0&&expected.ty==0&&expected.tz==0,"pristine translation restored");check(!expected.inventory3d,"pristine inventory is flat");check(expected.inventoryVertices==0,"actual pristine renderInventoryBlock emits nothing");cases++;
 }
 static void geometry(String root,Sprite sprite)throws Exception{
  String actual=new String(Files.readAllBytes(Paths.get(root,"assets/hbm/models/machines/solar_mirror.obj")),StandardCharsets.UTF_8);
  String header="v -0.340123 0.862345 0.712345\nv 0.712345 1.232345 0.312345\nv -0.112345 1.712345 -0.412345\nv 0.312345 0.512345 -0.712345\nvt 0.1234567 0.8765432\nvt 0.2345678 0.7654321\nvt 0.3456789 0.6543219\nvt 0.4567891 0.5432198\n";
  List<String> models=Arrays.asList(actual,header+"o Base\nf 1/1 2/2 3/3\no Mirror\nf 3/3 2/2 1/1\n",header+"o Base\nf 1/1 2/2 3/3 4/4\no Mirror\nf 1/1 2/2 3/3 4/4\nf 4/4 3/3 2/2 1/1\n",header+"o Base\nf 1/1 2/2 3/3\no Base\nf 3/3 2/2 1/1\no Mirror\nf 1/1 2/2 3/3\no Mirror\nf 2/2 3/3 4/4\n",header+"o Base\nf 1/1 2/2 3/3\n",header+"o Mirror\nf 1/1 2/2 3/3\n",header+"o mirror\nf 1/1 2/2 3/3\no base\nf 2/2 3/3 4/4\n");
  List<int[]> targets=new ArrayList<>();for(int x:new int[]{-31,-1,0,1,43})for(int z:new int[]{-53,-1,0,1,71})for(int y:new int[]{-9,0,1,17})targets.add(new int[]{x,y,z});targets.add(new int[]{0,72,0});targets.add(new int[]{50000,7,50000});targets.add(new int[]{50000,7,0});
  Random random=new Random(0x50A1L);for(int i=0;i<48;i++)targets.add(new int[]{random.nextInt(2001)-1000,1+random.nextInt(250),random.nextInt(2001)-1000});
  for(int m=0;m<models.size();m++){
   String source=models.get(m);ObjMesh mesh=ObjMeshParser.parse(new StringReader(source),"test-"+m,false);
   for(int[] t:targets){Snapshot snapshot=new Snapshot(true,t[1]>0,t[0],t[1],t[2]);OriginalMirrorOracle.Result expected=OriginalMirrorOracle.render(source,sprite,true,t[0],t[1],t[2]);check(expected.rendered,"actual pristine world renderer reports success");List<BakedQuad> baked=RenderMirror.bake(mesh,sprite,snapshot);compare(baked,expected,"model="+m+" target="+Arrays.toString(t));for(BakedQuad q:baked)check(q.getSprite()==sprite,"correct atlas sprite identity");}
   check(RenderMirror.bake(mesh,sprite,Snapshot.ABSENT).isEmpty(),"absent tile produces no Base or Mirror");check(!OriginalMirrorOracle.render(source,sprite,false,3,4,5).rendered,"actual pristine absent tile returns false");
  }
  ObjMesh mesh=ObjMeshParser.parse(new StringReader(actual),"real",false);SolarMirrorBakedModel model=new SolarMirrorBakedModel(mesh,sprite);SolarMirror owner=new SolarMirror(Material.IRON);
  check(!model.isAmbientOcclusion()&&!model.isBuiltInRenderer()&&!model.isGui3d(),"world native shading/renderer flags");check(model.getParticleTexture()==sprite&&model.getOverrides()==ItemOverrideList.NONE,"particle and override identity");
  check(model.getQuads(null,null,0).isEmpty()&&model.getQuads(net.minecraft.init.Blocks.STONE.getDefaultState(),null,0).isEmpty()&&model.getQuads(owner.getDefaultState(),null,0).isEmpty(),"null, foreign, and uncaptured states invisible");
  World world=new World(false);world.state=owner.getDefaultState();TileEntitySolarMirror tile=new TileEntitySolarMirror();tile.setPos(new BlockPos(21,40,-32));tile.tX=-8;tile.tY=61;tile.tZ=19;world.tile=tile;
  IBlockState extended=owner.getExtendedState(world.state,world,tile.getPos());Snapshot snapshot=SolarMirrorModelState.snapshot(extended);check(snapshot.available&&snapshot.pitched&&snapshot.dx==-29&&snapshot.dy==21&&snapshot.dz==51,"actual block immutable relative target snapshot");
  tile.tX=900;tile.tY=-900;tile.tZ=900;compare(model.getQuads(extended,null,0),OriginalMirrorOracle.render(actual,sprite,true,-29,21,51),"captured target not mutable tile");
  for(Field f:Snapshot.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers()))check(Modifier.isFinal(f.getModifiers())&&(f.getType()==int.class||f.getType()==boolean.class),"snapshot owns only final primitive fields");
  for(EnumFacing face:EnumFacing.values())check(model.getQuads(extended,face,0).isEmpty(),"unculled geometry only");
  tile.setPos(new BlockPos(0,-1,0));tile.tY=Integer.MAX_VALUE;Snapshot overflow=Snapshot.capture(tile);check(overflow.pitched&&overflow.dy==Integer.MIN_VALUE,"snapshot preserves absolute-Y pitched branch independently of overflowing relative dy");
  world.tile=null;check(model.getQuads(owner.getExtendedState(world.state,world,tile.getPos()),null,0).isEmpty(),"actual owner missing tile state invisible");world.tile=new TileEntitySolarBoiler();check(!SolarMirrorModelState.snapshot(owner.getExtendedState(world.state,world,BlockPos.ORIGIN)).available,"wrong tile owner invisible");
  check(!owner.isOpaqueCube(owner.getDefaultState())&&!owner.isFullCube(owner.getDefaultState())&&owner.getRenderType(owner.getDefaultState())==EnumBlockRenderType.MODEL,"actual native owner shape flags");check(owner.createNewTileEntity(world,0).getClass()==TileEntitySolarMirror.class,"actual owner factory actual tile");
  System.out.println("PASS pristine RenderMirror + ObjUtil oracle: "+cases+" geometry cases, "+vertices+" native vertices; real asset, triangles, quads, duplicate/missing/case-sensitive groups, absent tiles and immutable state");
 }
 static final class World extends net.minecraft.world.World {
  IBlockState state;TileEntity tile;int sky=15,subtracted,redraws,lights,skyQueries,tileQueries;boolean visible=true;long time;BlockPos seenSky,seenTile,seenLight,renderFrom,renderTo;
  World(boolean remote){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new net.minecraft.world.WorldProviderSurface(),new net.minecraft.profiler.Profiler(),remote);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected boolean isChunkLoaded(int x,int z,boolean empty){return true;}
  public BlockPos getSpawnPoint(){return BlockPos.ORIGIN;}public IBlockState getBlockState(BlockPos p){return state==null?net.minecraft.init.Blocks.AIR.getDefaultState():state;}
  public long getTotalWorldTime(){return time;}public int getSkylightSubtracted(){return subtracted;}
  public int getLightFor(EnumSkyBlock type,BlockPos p){check(type==EnumSkyBlock.SKY,"original SKY channel");lights++;seenLight=p;return sky;}
  public boolean canSeeSky(BlockPos p){skyQueries++;seenSky=p;return visible;}
  public TileEntity getTileEntity(BlockPos p){tileQueries++;seenTile=p;return tile;}
  public void markBlockRangeForRenderUpdate(BlockPos a,BlockPos b){redraws++;renderFrom=a;renderTo=b;}
 }
 static void tiles()throws Exception {
  TileEntity.register("hbm:solar_mirror_native_proof",TileEntitySolarMirror.class);
  BlockPos pos=new BlockPos(-17,63,29);World world=new World(false);TileEntitySolarBoiler boiler=new TileEntitySolarBoiler();world.tile=boiler;TileEntitySolarMirror tile=new TileEntitySolarMirror();tile.setWorld(world);tile.setPos(pos);check(tile.getInventoryName()==null,"original inventory name");
  tile.setTarget(7,93,-11);check(tile.tX==7&&tile.tY==93&&tile.tZ==-11&&tile.dirty==1&&tile.packets==1&&tile.range==200,"target mutation marks dirty and sync radius 200");
  world.time=1;tile.update();check(tile.isOn&&boiler.heat==4&&world.seenLight.equals(pos)&&world.seenSky.equals(pos.up())&&world.seenTile.equals(new BlockPos(7,92,-11)),"server sunlight heat target offset and native positions");check(tile.packets==1,"no off-cadence sync");
  world.time=20;tile.update();check(tile.packets==2&&tile.range==200&&boiler.heat==8,"20-tick sync and heat");
  int reads=world.lights;tile.tY=62;tile.update();check(!tile.isOn&&boiler.heat==8&&world.lights==reads&&tile.packets==3,"below-target early return after sync");
  tile.tY=63;world.time=21;tile.update();check(tile.isOn&&boiler.heat==12,"equal-height target remains active unlike pitched visual branch");
  world.subtracted=4;tile.update();check(!tile.isOn&&boiler.heat==12,"nonpositive sun disabled");world.subtracted=1;world.visible=false;tile.update();check(!tile.isOn&&boiler.heat==12,"covered sky disabled");world.visible=true;tile.update();check(tile.isOn&&boiler.heat==15,"sunlight subtraction preserved");
  world.tile=new TileEntitySolarMirror();tile.update();check(tile.isOn&&boiler.heat==15,"non-boiler target no heat cast");
  tile.tX=Integer.MIN_VALUE;tile.tY=2147483646;tile.tZ=-314159;tile.isOn=true;io.netty.buffer.ByteBuf buffer=io.netty.buffer.Unpooled.buffer();tile.serialize(buffer);check(buffer.readableBytes()==13&&buffer.getInt(0)==tile.tX&&buffer.getInt(4)==tile.tY&&buffer.getInt(8)==tile.tZ&&buffer.getBoolean(12),"wire original int,int,int,bool order");TileEntitySolarMirror copy=new TileEntitySolarMirror();copy.deserialize(buffer);check(copy.tX==tile.tX&&copy.tY==tile.tY&&copy.tZ==tile.tZ&&copy.isOn,"wire roundtrip");buffer.release();
  NBTTagCompound nbt=new NBTTagCompound();check(tile.writeToNBT(nbt)==nbt,"native NBT returns same compound");check(nbt.getInteger("targetX")==tile.tX&&nbt.getInteger("targetY")==tile.tY&&nbt.getInteger("targetZ")==tile.tZ&&!nbt.hasKey("isOn"),"original target NBT keys only");copy.isOn=false;copy.readFromNBT(nbt);check(copy.tX==tile.tX&&copy.tY==tile.tY&&copy.tZ==tile.tZ&&!copy.isOn,"target persistence excludes runtime isOn");
  AxisAlignedBB box=tile.getRenderBoundingBox();check(box.equals(new AxisAlignedBB(-42,38,4,8,88,54))&&tile.getRenderBoundingBox()==box&&tile.getMaxRenderDistanceSquared()==65536D,"original bounds cache and distance");
  World client=new World(true);client.tile=boiler;tile.setWorld(client);tile.setPos(pos);tile.tX=5;tile.tY=80;tile.tZ=9;tile.isOn=true;client.time=20;int packets=tile.packets;tile.update();check(boiler.primary.contains(new SolarMirrorPosition(pos.getX(),pos.getY(),pos.getZ()))&&boiler.primary.size()==1,"client beam target collects immutable legacy-hash position");check(client.redraws==1&&client.renderFrom.equals(pos)&&client.renderTo.equals(pos)&&client.seenTile.equals(new BlockPos(5,79,9))&&tile.packets==packets,"client 20-tick native redraw and no server packets");tile.update();check(boiler.primary.size()==1,"beam source de-duplicated");client.time=21;tile.isOn=false;boiler.primary.clear();tile.update();check(boiler.primary.isEmpty()&&client.redraws==2,"inactive client not collected and off-cadence no redraw");
  System.out.println("PASS actual owner/tile lifecycle, sunlight gates, heat, packets, byte/NBT targets, native client beam collection and redraw; explicit base/boiler collaborators only");
 }
 static void registry(String root,Sprite sprite,String mode)throws Exception{
  net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());net.minecraftforge.fml.common.ModMetadata metadata=new net.minecraftforge.fml.common.ModMetadata();metadata.modId="hbm";metadata.name="Solar mirror proof";metadata.version="1";net.minecraftforge.fml.common.DummyModContainer mod=new net.minecraftforge.fml.common.DummyModContainer(metadata);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(mod);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(mod);
  Block owner=mode.equals("subclass")?new SolarMirror(Material.IRON){}:mode.equals("unrelated")?new Block(Material.IRON):new SolarMirror(Material.IRON);owner.setRegistryName("hbm","tile.solar_mirror");net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(owner);
  Map<Block,Item> itemMap=(Map<Block,Item>)get(null,Item.class,"BLOCK_TO_ITEM");ItemBlock item=new ItemBlock(owner);item.setRegistryName(owner.getRegistryName());net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item);itemMap.put(owner,item);
  List<Block> excluded=new ArrayList<>();for(String name:new String[]{"other:tile.solar_mirror","hbm:solar_mirror","hbm:tile.solar_mirror_unrelated"}){Block b=new SolarMirror(Material.IRON);b.setRegistryName(new ResourceLocation(name));net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(b);excluded.add(b);}
  sun.misc.Unsafe unsafe=(sun.misc.Unsafe)get(null,sun.misc.Unsafe.class,"theUnsafe");net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());resources.reloadResources(Collections.singletonList(new FolderResourcePack(new File(root))));set(mc,net.minecraft.client.Minecraft.class,"resourceManager",resources);
  net.minecraftforge.fml.common.Mod.EventBusSubscriber annotation=com.hbm.main.SolarMirrorModelHandler.class.getAnnotation(net.minecraftforge.fml.common.Mod.EventBusSubscriber.class);check(annotation.modid().equals("hbm")&&Arrays.equals(annotation.value(),new net.minecraftforge.fml.relauncher.Side[]{net.minecraftforge.fml.relauncher.Side.CLIENT}),"actual automatic client subscriber");
  net.minecraftforge.fml.common.eventhandler.EventBus bus=new net.minecraftforge.fml.common.eventhandler.EventBus();bus.register(com.hbm.main.SolarMirrorModelHandler.class);bus.post(new net.minecraftforge.client.event.ModelRegistryEvent());Map<?,?> states=(Map<?,?>)get(null,ModelLoader.class,"customStateMappers");Map<?,?> items=(Map<?,?>)get(null,ModelLoader.class,"customModels");
  for(Block b:excluded)check(!states.containsKey(b.delegate),"foreign namespace and unrelated owner paths untouched");
  if(!mode.equals("normal")){check(!states.containsKey(owner.delegate)&&items.isEmpty(),"exact registry name does not capture "+mode);System.out.println("PASS exact owner registry negative: "+mode+" class left untouched");return;}
  check(states.containsKey(owner.delegate)&&items.size()==16,"actual world mapping and 16 inventory metadata routes");net.minecraft.client.renderer.block.statemap.IStateMapper mapper=(net.minecraft.client.renderer.block.statemap.IStateMapper)states.get(owner.delegate);ModelResourceLocation worldRoute=mapper.putStateModelLocations(owner).get(owner.getDefaultState());check(worldRoute.equals(new ModelResourceLocation("hbm:tile.solar_mirror","normal")),"native original blockstate location");
  com.google.gson.JsonObject json=new com.google.gson.JsonParser().parse(new String(Files.readAllBytes(Paths.get(root,"assets/hbm/blockstates/tile.solar_mirror.json")),StandardCharsets.UTF_8)).getAsJsonObject();String worldModel=json.getAsJsonObject("variants").getAsJsonObject("normal").get("model").getAsString();check(worldModel.equals("hbm:native_solar_mirror/world"),"actual original blockstate routes native model");
  ICustomModelLoader loader=(ICustomModelLoader)get(null,com.hbm.main.SolarMirrorModelHandler.class,"LOADER");loader.onResourceManagerReload(resources);
  for(String prefix:new String[]{"","block/","models/block/"})for(String kind:new String[]{"world","item"})check(loader.accepts(new ResourceLocation("hbm:"+prefix+"native_solar_mirror/"+kind)),"normalized native route");for(String bad:new String[]{"other:native_solar_mirror/world","hbm:native_solar_mirror/nope","hbm:native_solar_mirror/world/extra","hbm:native_solar_mirrors/world"}){ResourceLocation key=new ResourceLocation(bad);check(!loader.accepts(key),"negative loader route");try{loader.loadModel(key);throw new AssertionError("invalid loader route accepted");}catch(IllegalArgumentException expected){checks++;}}
  IModel nativeWorld=loader.loadModel(new ResourceLocation(worldModel));check(nativeWorld.getTextures().equals(Collections.singletonList(new ResourceLocation("hbm:blocks/solar_mirror"))),"world original atlas material");IBakedModel baked=nativeWorld.bake(nativeWorld.getDefaultState(),DefaultVertexFormats.BLOCK,r->sprite);check(baked instanceof SolarMirrorBakedModel&&baked.getQuads(null,null,0).isEmpty(),"actual loader world native model, absent state invisible");
  for(int meta=0;meta<16;meta++)check(new ModelResourceLocation("hbm:native_solar_mirror/item","inventory").equals(items.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,meta))),"inventory original metadata fallback");IModel flat=loader.loadModel(new ResourceLocation("hbm:native_solar_mirror/item"));check(flat instanceof ItemLayerModel,"inventory actual Forge flat ItemLayerModel, not OBJ world geometry");check(flat.getTextures().equals(Collections.singletonList(new ResourceLocation("hbm:blocks/solar_mirror"))),"flat item original atlas material");IBakedModel flatBaked=flat.bake(flat.getDefaultState(),DefaultVertexFormats.ITEM,r->sprite);check(!flatBaked.isGui3d()&&!flatBaked.isBuiltInRenderer()&&!flatBaked.getQuads(null,null,0).isEmpty(),"actual native generated flat item bakes visible quads");
  Object before=get(loader,loader.getClass(),"mesh");check(before!=null,"world mesh loaded");loader.onResourceManagerReload(resources);check(get(loader,loader.getClass(),"mesh")==null,"resource reload invalidates geometry");loader.loadModel(new ResourceLocation(worldModel));check(before!=get(loader,loader.getClass(),"mesh"),"resource reload reparses real OBJ");
  System.out.println("PASS actual Forge registry/event/state mapper, all 16 flat inventory routes, exact loader negatives, native ItemLayerModel and resource reload");
 }
 public static void main(String[] args)throws Exception{net.minecraft.init.Bootstrap.register();Sprite sprite=new Sprite("hbm:blocks/solar_mirror",args[0]);String mode=args.length>1?args[1]:"normal";if(mode.equals("normal")){geometry(args[0],sprite);tiles();}registry(args[0],sprite,mode);System.out.println("PASS SolarMirror native evidence: "+checks+" assertions; no live-game visual, networking integration or full boiler-closure claim");}
}

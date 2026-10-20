import java.nio.file.*;
import java.io.*;
import java.util.*;
import com.hbm.blocks.generic.*;
import com.hbm.render.mesh.*;
import com.hbm.render.block.*;
import com.hbm.render.block.nativeblock.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.*;

public class NativeTapeTest {
 static int checks;
 static void check(boolean condition,String message) { checks++;if(!condition)throw new AssertionError(message); }
 static class Sprite extends TextureAtlasSprite { Sprite(){super("test");initSprite(256,256,32,48,false);} }
 static void verify(ObjMesh mesh,int meta,boolean inventory,boolean computer,List<BakedQuad> actual) {
  List<float[]> vertices=new ArrayList<>();
  float yaw=0;
  if(!inventory) {
   if(meta==2)yaw=90F/180F*(float)Math.PI;
   if(meta==3)yaw=270F/180F*(float)Math.PI;
   if(meta==4)yaw=180F/180F*(float)Math.PI;
   if(computer)yaw-=90F/180F*(float)Math.PI;
  }
  double scale=1,y=0;
  double sin=MathHelper.sin(yaw),cos=MathHelper.cos(yaw);
  for(ObjMesh.Part part:mesh.parts)for(ObjMesh.Face face:part.faces) {
   for(int i=0;i<face.corners.size();i++) {
    ObjMesh.Corner c=face.corners.get(i);
    float[] v={(float)(.5+(float)(c.position.x*cos+c.position.z*sin)*scale),
      (float)(y+(float)c.position.y*scale),
      (float)(.5+(float)(c.position.z*cos-c.position.x*sin)*scale)};
    vertices.add(v);if(i%3==2)vertices.add(v);
   }
  }
  check(actual.size()==vertices.size()/4,"original continuous GL_QUADS stream");
  for(int q=0;q<actual.size();q++)for(int v=0;v<4;v++)for(int axis=0;axis<3;axis++) {
   int[] data=actual.get(q).getVertexData();int stride=actual.get(q).getFormat().getIntegerSize();
   check(Float.floatToIntBits(vertices.get(q*4+v)[axis])==data[v*stride+axis],"original vertex rotation/translation");
  }
 }
 static class RecordingWorld extends net.minecraft.world.World {
  IBlockState current=net.minecraft.init.Blocks.AIR.getDefaultState();
  net.minecraft.tileentity.TileEntity tile;
  final List<net.minecraft.entity.item.EntityItem> drops=new ArrayList<>();int flags,comparators,removals;
  RecordingWorld(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()),new net.minecraft.world.WorldProviderSurface(),new net.minecraft.profiler.Profiler(),false);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return true;}
  public boolean setBlockState(BlockPos pos,IBlockState state,int flags){current=state;this.flags=flags;return true;}
  public IBlockState getBlockState(BlockPos pos){return current;}
  public net.minecraft.tileentity.TileEntity getTileEntity(BlockPos pos){return tile;}
  public void removeTileEntity(BlockPos pos){tile=null;removals++;}
  public void updateComparatorOutputLevel(BlockPos pos,net.minecraft.block.Block block){comparators++;}
  public boolean spawnEntity(net.minecraft.entity.Entity entity){drops.add((net.minecraft.entity.item.EntityItem)entity);return true;}
 }
 static void owners() {
  RecordingWorld world=new RecordingWorld();
  net.minecraft.entity.passive.EntityPig placer=new net.minecraft.entity.passive.EntityPig(world);
  DecoTapeRecorder tape=new DecoTapeRecorder(Material.IRON);DecoSteelPoles poles=new DecoSteelPoles(Material.IRON);
  for(net.minecraft.block.Block block:new net.minecraft.block.Block[]{tape,poles}) {
   for(int meta=0;meta<16;meta++) {
    IBlockState state=block.getStateFromMeta(meta);
    check(block.getMetaFromState(state)==meta,"all persisted metadata");
    check(block.damageDropped(state)==0,"original generic drop damage");
    check(state.getBoundingBox(world,BlockPos.ORIGIN).equals(net.minecraft.block.Block.FULL_BLOCK_AABB),"original full selection bounds");
    check(state.getCollisionBoundingBox(world,BlockPos.ORIGIN).equals(net.minecraft.block.Block.FULL_BLOCK_AABB),"original full collision bounds");
    check(!state.isOpaqueCube()&&!state.isFullCube(),"original nonnormal face behavior");
    check(state.getRenderType()==net.minecraft.util.EnumBlockRenderType.MODEL,"native model callback");
    for(int rot=0;rot<4;rot++)check(tape.transformMeta(meta,rot)==com.hbm.world.gen.nbt.INBTBlockTransformable.transformMetaDeco(meta,rot),"original structure transform");
   }
   for(int angle=-1080;angle<=1080;angle+=7) {
    placer.rotationYaw=angle;block.onBlockPlacedBy(world,BlockPos.ORIGIN,block.getDefaultState(),placer,new net.minecraft.item.ItemStack(net.minecraft.init.Items.STICK));
    int direction=((int)Math.floor(angle*4F/360F+.5D))&3;
    check(block.getMetaFromState(world.current)==new int[]{2,5,3,4}[direction],"original placement yaw");check(world.flags==2,"original notify flags");
   }
  }
  check(tape.createNewTileEntity(world,0)==null,"original tape null tile factory");
 }
 static void routes(String root,TextureAtlasSprite sprite)throws Exception {
  java.lang.reflect.Field field=com.hbm.main.TapeBlockModelHandler.class.getDeclaredField("LOADER");field.setAccessible(true);
  net.minecraftforge.client.model.ICustomModelLoader loader=(net.minecraftforge.client.model.ICustomModelLoader)field.get(null);
  net.minecraft.client.resources.SimpleReloadableResourceManager resources=new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
  resources.reloadResourcePack(root.endsWith(".zip") ? new net.minecraft.client.resources.FileResourcePack(new File(root)) : new net.minecraft.client.resources.FolderResourcePack(new File(root,"src/main/resources")));
  loader.onResourceManagerReload(resources);
  for(String owner:new String[]{"tape_recorder","steel_poles"})for(int meta=0;meta<16;meta++)for(boolean item:new boolean[]{false,true}) {
   net.minecraft.util.ResourceLocation route=new net.minecraft.util.ResourceLocation("hbm:native_tape/tile."+owner+"/"+meta+(item?"/item":"/world"));
   check(loader.accepts(route),"native route accepted");
   net.minecraftforge.client.model.IModel model=loader.loadModel(route);
   String expected=owner.equals("tape_recorder")?"deco_tape_recorder":"steel_beam";
   try(net.minecraft.client.resources.IResource texture=resources.getResource(new net.minecraft.util.ResourceLocation("hbm:textures/blocks/"+expected+".png"))){check(texture.getInputStream().read()>=0,"actual native texture lookup");}
   check(model.getTextures().iterator().next().toString().equals("hbm:blocks/"+expected),"original material route");
   net.minecraft.client.renderer.block.model.IBakedModel baked=model.bake(net.minecraftforge.common.model.TRSRTransformation.identity(),net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK,r->sprite);
   check(baked.getParticleTexture()==sprite&&!baked.getQuads(null,null,0).isEmpty(),"loader to native bake consumer");
  }
  loader.onResourceManagerReload(resources);
  check(!loader.accepts(new net.minecraft.util.ResourceLocation("other:native_tape/tile.tape_recorder/0/world")),"namespace isolation");
  try {loader.loadModel(new net.minecraft.util.ResourceLocation("hbm:native_tape/tile.tape_recorder/16/world"));throw new AssertionError("bad metadata accepted");}catch(IllegalArgumentException expected){checks++;}
 }
 static void registry(boolean replacements)throws Exception {
  net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  net.minecraftforge.fml.common.ModMetadata metadata=new net.minecraftforge.fml.common.ModMetadata();metadata.modId="hbm";metadata.name="Structural probe";metadata.version="1";
  net.minecraftforge.fml.common.Loader.instance().setupTestHarness(new net.minecraftforge.fml.common.DummyModContainer(metadata));
  net.minecraft.block.Block[] blocks=replacements?new net.minecraft.block.Block[]{new net.minecraft.block.Block(Material.IRON){},new net.minecraft.block.Block(Material.IRON){}}:new net.minecraft.block.Block[]{new DecoTapeRecorder(Material.IRON),new DecoSteelPoles(Material.IRON)};String[]names={"tape_recorder","steel_poles"};
  java.lang.reflect.Field mapping=net.minecraft.item.Item.class.getDeclaredField("BLOCK_TO_ITEM");mapping.setAccessible(true);
  Map<net.minecraft.block.Block,net.minecraft.item.Item> items=(Map<net.minecraft.block.Block,net.minecraft.item.Item>)mapping.get(null);
  for(int i=0;i<2;i++){net.minecraft.block.Block b=blocks[i];b.setRegistryName("hbm","tile."+names[i]);net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(b);net.minecraft.item.ItemBlock item=new net.minecraft.item.ItemBlock(b);item.setRegistryName(b.getRegistryName());net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item);items.put(b,item);}
  net.minecraft.block.Block[] unowned={new DecoTapeRecorder(Material.IRON),new DecoSteelPoles(Material.IRON),new DecoTapeRecorder(Material.IRON),new DecoSteelPoles(Material.IRON)};
  String[] namespaces={"other","other","hbm","hbm"},paths={"tile.tape_recorder","tile.steel_poles","tile.other_chain","tile.other_fence"};
  // Deliberately no ItemBlocks: the handler must skip unowned names before
  // checking classes/items, rather than capturing or rejecting addon instances.
  for(int i=0;i<unowned.length;i++){unowned[i].setRegistryName(namespaces[i],paths[i]);net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(unowned[i]);}
  // Model registration uses the real client singleton/resource manager, without a game loop or GL context.
  java.io.File d=new java.io.File(".");
  net.minecraft.client.main.GameConfiguration config=new net.minecraft.client.main.GameConfiguration(
    new net.minecraft.client.main.GameConfiguration.UserInformation(new net.minecraft.util.Session("StructuralProbe","00000000-0000-0000-0000-000000000000","","legacy"),new com.mojang.authlib.properties.PropertyMap(),new com.mojang.authlib.properties.PropertyMap(),java.net.Proxy.NO_PROXY),
    new net.minecraft.client.main.GameConfiguration.DisplayInformation(1,1,false,false),new net.minecraft.client.main.GameConfiguration.FolderInformation(d,d,d,null),
    new net.minecraft.client.main.GameConfiguration.GameInformation(false,"1.12.2","native structural proof"),new net.minecraft.client.main.GameConfiguration.ServerInformation(null,0));
  net.minecraft.client.Minecraft minecraft=new net.minecraft.client.Minecraft(config);minecraft.gameSettings=new net.minecraft.client.settings.GameSettings();
  java.lang.reflect.Field rm=net.minecraft.client.Minecraft.class.getDeclaredField("resourceManager");rm.setAccessible(true);rm.set(minecraft,new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer()));
  net.minecraft.client.renderer.block.statemap.IStateMapper addonMapper=block->java.util.Collections.singletonMap(block.getDefaultState(),new net.minecraft.client.renderer.block.model.ModelResourceLocation("other:replacement","normal"));
  if(replacements)for(net.minecraft.block.Block b:blocks){net.minecraftforge.client.model.ModelLoader.setCustomStateMapper(b,addonMapper);net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(items.get(b),0,new net.minecraft.client.renderer.block.model.ModelResourceLocation("other:replacement","inventory"));}
  java.lang.reflect.Field custom=net.minecraftforge.client.model.ModelLoader.class.getDeclaredField("customModels");custom.setAccessible(true);Map<?,?> models=(Map<?,?>)custom.get(null);int initialModels=models.size();Map<?,?> originalModels=new HashMap<>(models);
  com.hbm.main.TapeBlockModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());
  check(models.size()==initialModels+(replacements?0:32),"native event queues only authorized original owner item metadata routes");
  if(replacements)check(models.equals(originalModels),"same-name replacement addon item model routes are preserved");
  java.lang.reflect.Field sm=net.minecraftforge.client.model.ModelLoader.class.getDeclaredField("customStateMappers");sm.setAccessible(true);Map<?,?> states=(Map<?,?>)sm.get(null);
  for(net.minecraft.block.Block b:unowned)check(!states.containsKey(b.delegate),"foreign namespace and unscoped hbm owners remain untouched by native model event: "+b.getRegistryName());
  if(replacements){for(net.minecraft.block.Block b:blocks)check(states.get(b.delegate)==addonMapper,"same-name replacement addon state mapper preserved");return;}
  for(net.minecraft.block.Block b:blocks) {
   java.util.Map<IBlockState,net.minecraft.client.renderer.block.model.ModelResourceLocation> locations=new net.minecraft.client.renderer.block.statemap.DefaultStateMapper().putStateModelLocations(b);
   check(locations.size()==16,"native default state mapper includes every metadata");
   for(java.util.Map.Entry<IBlockState,net.minecraft.client.renderer.block.model.ModelResourceLocation> entry:locations.entrySet())
    check(entry.getValue().getVariant().equals("metadata="+b.getMetaFromState(entry.getKey())),"native blockstate JSON variant route");
  }
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();if(args.length>1&&args[1].equals("replacements")){registry(true);System.out.println("PASS "+checks+" native tape replacement-owner checks");return;} owners();Sprite sprite=new Sprite();
  for(String name:new String[]{"taperecorder","pole"}) {
   ObjMesh mesh;
   try(Reader reader=Files.newBufferedReader(Paths.get(args[0],"src/main/resources/assets/hbm/models/blocks/"+name+".obj"))) {mesh=ObjMeshParser.parse(reader,name,false);}
   for(int meta=0;meta<16;meta++)for(boolean item:new boolean[]{false,true}) {
    List<BakedQuad> quads=RenderTapeBlock.bake(mesh,name.equals("pole"),meta,item,sprite);
    check(!quads.isEmpty(),"shipped geometry");verify(mesh,meta,item,name.equals("pole"),quads);
    AtlasObjBakedModel baked=new AtlasObjBakedModel(quads,sprite);
    check(baked.getQuads(null,net.minecraft.util.EnumFacing.UP,0).isEmpty(),"original unculled geometry");
    check(!baked.isAmbientOcclusion(),"original explicit flat shading");
   }
  }
  routes(args[0],sprite);routes(args[1],sprite);registry(false);System.out.println("PASS "+checks+" native tape/pole checks");
 }
}

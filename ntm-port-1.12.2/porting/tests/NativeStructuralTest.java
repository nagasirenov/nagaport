import java.util.*;
import com.hbm.blocks.generic.*;
import com.hbm.render.block.*;
import com.hbm.render.block.nativeblock.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
public class NativeStructuralTest {
 static int checks;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static class Sprite extends TextureAtlasSprite {Sprite(String n){super(n);setIconWidth(16);setIconHeight(16);initSprite(256,256,32,48,false);}}
 static class World extends net.minecraft.world.World {
  final Map<BlockPos,IBlockState> blocks=new HashMap<>(); int destroys;boolean dropped;
  World(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()),new net.minecraft.world.WorldProviderSurface(),new net.minecraft.profiler.Profiler(),false);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return true;}
  public IBlockState getBlockState(BlockPos pos){return blocks.getOrDefault(pos,Blocks.AIR.getDefaultState());}
  public boolean setBlockState(BlockPos pos,IBlockState state,int flags){blocks.put(pos,state);return true;}
  public boolean destroyBlock(BlockPos pos,boolean drops){destroys++;dropped=drops;blocks.remove(pos);return true;}
  public int getCombinedLight(BlockPos pos,int light){return 0x00a00060;}
  public boolean isSideSolid(BlockPos pos,EnumFacing side,boolean fallback){return getBlockState(pos).isSideSolid(this,pos,side);}
 }
 static final BlockPos P=new BlockPos(17,31,49);
 static final EnumFacing[] DIR={EnumFacing.WEST,EnumFacing.EAST,EnumFacing.NORTH,EnumFacing.SOUTH};
 static void fences(TextureAtlasSprite panel,TextureAtlasSprite post){
  BlockMetalFence block=new BlockMetalFence(Material.IRON);World world=new World();
  StructuralBakedModel model=new StructuralBakedModel(false,panel,post);
  for(int mask=0;mask<16;mask++)for(int meta=0;meta<16;meta++){
   world.blocks.clear();for(int i=0;i<4;i++)if((mask&(1<<i))!=0)world.blocks.put(P.offset(DIR[i]),Blocks.STONE.getDefaultState());
   IBlockState state=block.getStateFromMeta(meta);check(block.getMetaFromState(state)==meta,"fence metadata");
   check(block.damageDropped(state)==meta%2,"fence native drop metadata");
   state=block.getActualState(state,world,P);
   boolean w=(mask&1)!=0,e=(mask&2)!=0,n=(mask&4)!=0,s=(mask&8)!=0;
   AxisAlignedBB bb=state.getBoundingBox(world,P);
   check(bb.equals(new AxisAlignedBB(w?0:.375,0,n?0:.375,e?1:.625,1,s?1:.625)),"fence selection");
   List<AxisAlignedBB> actual=new ArrayList<>();block.addCollisionBoxToList(state,world,P,new AxisAlignedBB(0,0,0,100,100,100),actual,null,false);
   List<AxisAlignedBB> expected=new ArrayList<>();
   if(n||s)expected.add(new AxisAlignedBB(.375,0,n?0:.375,.625,1,s?1:.625).offset(P));
   if(w||e||!n&&!s)expected.add(new AxisAlignedBB(w?0:.375,0,.375,e?1:.625,1,.625).offset(P));
   check(actual.equals(expected),"original two-arm collision order and 1.0 height");
   boolean hx=w||e,hz=n||s,sp=meta==1||!(!hz&&w&&e)&&!(!hx&&n&&s);
   int count=((hx||!hz)?1:0)+(hz?1:0)+(sp?1:0);
   List<BakedQuad> quads=model.getQuads(state,null,0);check(quads.size()==count*6,"original fence boxes");
   for(BakedQuad q:quads){check(q.shouldApplyDiffuseLighting(),"fence diffuse light");int[]data=q.getVertexData();for(int v=0;v<4;v++)for(int i=0;i<3;i++)check(Float.isFinite(Float.intBitsToFloat(data[v*q.getFormat().getIntegerSize()+i])),"finite degenerate-plane normals/positions");}
   for(EnumFacing face:EnumFacing.values())check(model.getQuads(state,face,0).isEmpty(),"fence original all-face draw");
  }
  Block[] neighbors={block,Blocks.OAK_FENCE_GATE,com.hbm.blocks.ModBlocks.fence_gate,Blocks.STONE,Blocks.PUMPKIN,Blocks.GLASS,Blocks.OAK_FENCE,Blocks.SPRUCE_FENCE_GATE,Blocks.AIR};
  boolean[] connected={true,true,true,true,false,false,false,false,false};
  for(int i=0;i<neighbors.length;i++){world.blocks.put(P,neighbors[i].getDefaultState());check(block.canConnectFenceTo(world,P)==connected[i],"exact old fence connection exclusions "+neighbors[i]);}
 }
 static int placed(int mask,int above,int side,int input){int out=input;
  if(side==2&&(mask&8)!=0)out=2;if(side==3&&(mask&4)!=0)out=3;if(side==4&&(mask&2)!=0)out=4;if(side==5&&(mask&1)!=0)out=5;
  if(out==0){if(above>=0)return above;if(above==-2)return 0;}
  if(out==0){if((mask&8)!=0)out=2;if((mask&4)!=0)out=3;if((mask&2)!=0)out=4;if((mask&1)!=0)out=5;}return out;
 }
 static void chains(TextureAtlasSprite main,TextureAtlasSprite end){
  BlockChain block=new BlockChain(Material.IRON);World world=new World();StructuralBakedModel model=new StructuralBakedModel(true,main,end);
  for(int mask=0;mask<16;mask++)for(int above=-2;above<16;above++)for(EnumFacing side:EnumFacing.values())for(int input=0;input<16;input++){
   world.blocks.clear();for(int i=0;i<4;i++)if((mask&(1<<i))!=0)world.blocks.put(P.offset(DIR[i]),Blocks.STONE.getDefaultState());
   if(above>=0)world.blocks.put(P.up(),block.getStateFromMeta(above));if(above==-2)world.blocks.put(P.up(),Blocks.STONE.getDefaultState());
   IBlockState state=block.getStateForPlacement(world,P,side,.3F,.4F,.5F,input,null,EnumHand.MAIN_HAND);
   check(block.getMetaFromState(state)==placed(mask,above,side.getIndex(),input),"chain placement exact overwrite order");
   check(block.canPlaceBlockAt(world,P)==(above!=-1||mask!=0),"chain support placement");
  }
  for(int meta=0;meta<16;meta++)for(int below=-2;below<16;below++){
   world.blocks.clear();if(below>=0)world.blocks.put(P.down(),block.getStateFromMeta(below));if(below==-2)world.blocks.put(P.down(),Blocks.STONE.getDefaultState());
   IBlockState state=block.getActualState(block.getStateFromMeta(meta),world,P);boolean cap=!(below==-2||below==meta);
   check(state.getValue(BlockChain.END)==cap,"chain cap uses matching metadata or solid below");
   check(state.getCollisionBoundingBox(world,P)==null,"chain no collision");check(block.isLadder(state,world,P,null),"chain ladder");
   if(meta==0||meta>=2&&meta<=5)check(state.getBoundingBox(world,P).minY==(cap?.25:0),"chain cap selection");
   List<BakedQuad> qs=model.getQuads(state,null,0);check(qs.size()==(meta==0?4:meta>=2&&meta<=5?2:0),"chain exact metadata geometry");
   for(BakedQuad q:qs){check(q.getSprite()==(cap?end:main),"chain material");check(!q.shouldApplyDiffuseLighting(),"chain unshaded");}
  }
  world.blocks.clear();
  AxisAlignedBB last=block.getBoundingBox(block.getStateFromMeta(4),world,P);
  for(int meta:new int[]{1,6,7,15})check(block.getBoundingBox(block.getStateFromMeta(meta),world,P).equals(last),"original undefined-metadata selection bounds carry");
  world.blocks.put(P.down(),Blocks.STONE.getDefaultState());
  check(block.getBoundingBox(block.getStateFromMeta(1),world,P).equals(last),"original invalid metadata retains capped minY even on solid support");
  RayTraceResult hit=block.collisionRayTrace(block.getStateFromMeta(4),world,P,new Vec3d(P).add(-1,.5,.5),new Vec3d(P).add(2,.5,.5));
  check(hit!=null&&hit.sideHit==EnumFacing.WEST&&Math.abs(hit.hitVec.x-(P.getX()+.875))<1E-6,"native chain raytrace uses original wall selection bounds");
  for(int mask=0;mask<16;mask++)for(int above=-2;above<16;above++)for(int meta=0;meta<16;meta++){
   world.blocks.clear();for(int i=0;i<4;i++)if((mask&(1<<i))!=0)world.blocks.put(P.offset(DIR[i]),Blocks.STONE.getDefaultState());
   if(above>=0)world.blocks.put(P.up(),block.getStateFromMeta(above));if(above==-2)world.blocks.put(P.up(),Blocks.STONE.getDefaultState());
   int before=world.destroys;block.neighborChanged(block.getStateFromMeta(meta),world,P,Blocks.STONE,P.up());
   boolean support=above==meta||meta==0&&above==-2||meta==2&&(mask&8)!=0||meta==3&&(mask&4)!=0||meta==4&&(mask&2)!=0||meta==5&&(mask&1)!=0;
   check(world.destroys-before==(support?0:1),"chain support loss/drop callback");if(!support)check(world.dropped,"chain drops on loss");
  }
 }
 static void geometry(TextureAtlasSprite main,TextureAtlasSprite secondary) {
  for(int meta=0;meta<16;meta++)for(TextureAtlasSprite sprite:new TextureAtlasSprite[]{main,secondary}){
   List<double[]> expected=StructuralOriginalOracle.chain(meta,sprite);List<BakedQuad> actual=RenderChain.bake(meta,sprite);
   check(expected.size()==actual.size()*4,"original chain vertex count");
   for(int i=0;i<expected.size();i++){BakedQuad q=actual.get(i/4);int[]data=q.getVertexData();int stride=q.getFormat().getIntegerSize(),uv=q.getFormat().getUvOffsetById(0)/4;
    for(int axis=0;axis<3;axis++)check(data[i%4*stride+axis]==Float.floatToIntBits((float)expected.get(i)[axis]),"exact ordered original chain coordinate");
    for(int axis=0;axis<2;axis++)check(data[i%4*stride+uv+axis]==Float.floatToIntBits((float)expected.get(i)[axis+3]),"exact ordered original chain UV");
   }
  }
  int[][] corners={{4,0,1,5},{2,6,7,3},{3,1,0,2},{6,4,5,7},{2,0,4,6},{7,5,1,3}};
  for(int meta=0;meta<16;meta++)for(int mask=0;mask<16;mask++){
   List<StructuralOriginalOracle.Box> boxes=StructuralOriginalOracle.fence(meta,mask,main,secondary);
   List<BakedQuad> actual=RenderFence.bake(meta,(mask&1)!=0,(mask&2)!=0,(mask&4)!=0,(mask&8)!=0,main,secondary);
   check(actual.size()==boxes.size()*6,"verbatim original fence branch boxes");
   for(int b=0;b<boxes.size();b++)for(int face=0;face<6;face++){
    StructuralOriginalOracle.Box box=boxes.get(b);BakedQuad q=actual.get(b*6+face);check(q.getSprite()==box.material,"original panel and override material");check(box.mirror,"original negative-Z positive-X UV mirror flag");
    int[]data=q.getVertexData();int stride=q.getFormat().getIntegerSize(),uv=q.getFormat().getUvOffsetById(0)/4;
    for(int v=0;v<4;v++){
     int c=corners[face][v];double x=box.bounds[(c&1)==0?0:3],y=box.bounds[(c&2)==0?1:4],z=box.bounds[(c&4)==0?2:5];
     for(int axis=0;axis<3;axis++)check(data[v*stride+axis]==Float.floatToIntBits((float)new double[]{x,y,z}[axis]),"native canonical cyclic face preserves original vertex");
     double u=face<2?x:face==2?1-x:face==3?x:face==4?z:1-z;double tv=face<2?z:1-y;
     check(Math.abs(Float.intBitsToFloat(data[v*stride+uv])-box.material.getInterpolatedU(u*16))<1E-6,"original mirrored fence U");
     check(Math.abs(Float.intBitsToFloat(data[v*stride+uv+1])-box.material.getInterpolatedV(tv*16))<1E-6,"original fence V");
    }
   }
  }
 }
 static void routes(String root,TextureAtlasSprite sprite)throws Exception {
  java.lang.reflect.Field f=com.hbm.main.StructuralBlockModelHandler.class.getDeclaredField("LOADER");f.setAccessible(true);
  net.minecraftforge.client.model.ICustomModelLoader loader=(net.minecraftforge.client.model.ICustomModelLoader)f.get(null);
  net.minecraft.client.resources.SimpleReloadableResourceManager resources=new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
  resources.reloadResourcePack(new net.minecraft.client.resources.FolderResourcePack(new java.io.File(root,"src/main/resources")));loader.onResourceManagerReload(resources);
  for(String kind:new String[]{"chain","fence"})for(String mode:new String[]{"world","world_post","item","post"}){
   ResourceLocation route=new ResourceLocation("hbm:native_structural/"+kind+"/"+mode);check(loader.accepts(route),"native route");
   net.minecraftforge.client.model.IModel model=loader.loadModel(route);
   for(ResourceLocation t:model.getTextures())try(net.minecraft.client.resources.IResource resource=resources.getResource(new ResourceLocation(t.getNamespace(),"textures/"+t.getPath()+".png"))){check(resource.getInputStream().read()==137,"original PNG resource");}
   IBakedModel baked=model.bake(net.minecraftforge.common.model.TRSRTransformation.identity(),net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM,t->sprite);
   if(!mode.startsWith("world")){check(!baked.getQuads(null,null,0).isEmpty(),"native generated flat item");check(!baked.isGui3d(),"original inventory false flag");}
  }
  loader.onResourceManagerReload(resources);check(!loader.accepts(new ResourceLocation("other:native_structural/chain/world")),"namespace isolation");
  java.nio.file.Path archive=java.nio.file.Files.createTempFile("structural-resources-",".zip");
  try {
   try(java.util.zip.ZipOutputStream zip=new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(archive))){
    for(String name:new String[]{"chain","chain_end","fence_metal","fence_metal_post"}){String path="assets/hbm/textures/blocks/"+name+".png";zip.putNextEntry(new java.util.zip.ZipEntry(path));java.nio.file.Files.copy(java.nio.file.Paths.get(root,"src/main/resources",path),zip);zip.closeEntry();}
   }
   net.minecraft.client.resources.SimpleReloadableResourceManager zipped=new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
   zipped.reloadResourcePack(new net.minecraft.client.resources.FileResourcePack(archive.toFile()));loader.onResourceManagerReload(zipped);
   for(String name:new String[]{"chain","chain_end","fence_metal","fence_metal_post"})try(net.minecraft.client.resources.IResource r=zipped.getResource(new ResourceLocation("hbm:textures/blocks/"+name+".png"))){check(r.getInputStream().read()==137,"native zipped original material");}
  }finally{java.nio.file.Files.deleteIfExists(archive);}
  for(String kind:new String[]{"chain","fence"}){
   String owner=kind.equals("chain")?"tile.dungeon_chain":"tile.fence_metal";
   com.google.gson.JsonObject json=new com.google.gson.JsonParser().parse(new java.io.FileReader(new java.io.File(root,"src/main/resources/assets/hbm/blockstates/"+owner+".json"))).getAsJsonObject();
   check(json.getAsJsonObject("variants").getAsJsonObject("normal").get("model").getAsString().equals("hbm:native_structural/"+kind+"/world"),"actual blockstate JSON model route");
  }

 }
 static void lighting(TextureAtlasSprite main,TextureAtlasSprite secondary){
  BlockChain chain=new BlockChain(Material.IRON);BlockMetalFence fence=new BlockMetalFence(Material.IRON);
  World world=new World(){public int getCombinedLight(BlockPos pos,int emission){return pos.equals(P)?0x00a00060:0x00100010*((Math.abs(pos.getX()+2*pos.getY()+3*pos.getZ())%8)+1);}};
  net.minecraft.client.renderer.color.BlockColors colors=new net.minecraft.client.renderer.color.BlockColors();
  for(boolean isChain:new boolean[]{false,true})for(int meta:new int[]{0,1,2,3,4,5})for(boolean forge:new boolean[]{false,true})for(boolean smooth:new boolean[]{false,true}){
   if(isChain&&smooth)continue;
   Block block=isChain?chain:fence;world.blocks.put(P,block.getStateFromMeta(meta));IBlockState state=block.getActualState(world.getBlockState(P),world,P);
   IBakedModel model=new StructuralBakedModel(isChain,main,secondary);
   net.minecraft.client.renderer.BufferBuilder out=new net.minecraft.client.renderer.BufferBuilder(4096);out.begin(7,net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK);
   if(forge){
    net.minecraftforge.client.model.pipeline.VertexLighterFlat lighter=smooth?new net.minecraftforge.client.model.pipeline.VertexLighterSmoothAo(colors):new net.minecraftforge.client.model.pipeline.VertexLighterFlat(colors);
    net.minecraftforge.client.model.pipeline.VertexBufferConsumer consumer=new net.minecraftforge.client.model.pipeline.VertexBufferConsumer(out);consumer.setOffset(P);
    lighter.setParent(consumer);lighter.setWorld(world);lighter.setState(state);lighter.setBlockPos(P);lighter.updateBlockInfo();
    for(BakedQuad q:model.getQuads(state,null,0))q.pipe(lighter);lighter.resetBlockInfo();
   }else{net.minecraft.client.renderer.BlockModelRenderer renderer=new net.minecraft.client.renderer.BlockModelRenderer(colors);
    if(smooth)renderer.renderModelSmooth(world,model,state,P,out,true,0);else renderer.renderModelFlat(world,model,state,P,out,true,0);
   }
   out.finishDrawing();List<BakedQuad> quads=model.getQuads(state,null,0);check(out.getVertexCount()==quads.size()*4,"actual native renderer vertex count");
   java.nio.ByteBuffer bytes=out.getByteBuffer();
   for(int v=0;v<out.getVertexCount();v++){
    for(int axis=0;axis<3;axis++){float value=bytes.getFloat(v*28+axis*4);check(Float.isFinite(value),"finite native rendered position");float expected=Float.intBitsToFloat(quads.get(v/4).getVertexData()[(v%4)*7+axis])+(axis==0?P.getX():axis==1?P.getY():P.getZ());check(Math.abs(expected-value)<1E-5,"native absolute vertex position");}
    if(isChain){check(bytes.getInt(v*28+24)==0x00a00060,"chain light uses own block, not neighbor");check(bytes.getInt(v*28+12)==0xffffffff,"chain stays white without diffuse");}
   }
  }
 }
 static void registry(boolean replacements)throws Exception {
  net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  net.minecraftforge.fml.common.ModMetadata metadata=new net.minecraftforge.fml.common.ModMetadata();metadata.modId="hbm";metadata.name="Structural probe";metadata.version="1";
  net.minecraftforge.fml.common.Loader.instance().setupTestHarness(new net.minecraftforge.fml.common.DummyModContainer(metadata));
  Block[] blocks=replacements?new Block[]{new Block(Material.IRON){},new Block(Material.IRON){}}:new Block[]{new BlockChain(Material.IRON),new BlockMetalFence(Material.IRON)};String[]names={"dungeon_chain","fence_metal"};
  java.lang.reflect.Field mapping=net.minecraft.item.Item.class.getDeclaredField("BLOCK_TO_ITEM");mapping.setAccessible(true);
  Map<Block,net.minecraft.item.Item> items=(Map<Block,net.minecraft.item.Item>)mapping.get(null);
  for(int i=0;i<2;i++){Block b=blocks[i];b.setRegistryName("hbm","tile."+names[i]);net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(b);net.minecraft.item.ItemBlock item=new net.minecraft.item.ItemBlock(b);item.setRegistryName(b.getRegistryName());net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item);items.put(b,item);}
  Block[] unowned={new BlockChain(Material.IRON),new BlockMetalFence(Material.IRON),new BlockChain(Material.IRON),new BlockMetalFence(Material.IRON)};
  String[] namespaces={"other","other","hbm","hbm"},paths={"tile.dungeon_chain","tile.fence_metal","tile.other_chain","tile.other_fence"};
  // Deliberately no ItemBlocks: the handler must skip unowned names before
  // checking classes/items, rather than capturing or rejecting addon instances.
  for(int i=0;i<unowned.length;i++){unowned[i].setRegistryName(namespaces[i],paths[i]);net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(unowned[i]);}
  // Model registration uses the real client singleton/resource manager, without a game loop or GL context.
  java.io.File d=new java.io.File(".");
  net.minecraft.client.main.GameConfiguration config=new net.minecraft.client.main.GameConfiguration(
    new net.minecraft.client.main.GameConfiguration.UserInformation(new Session("StructuralProbe","00000000-0000-0000-0000-000000000000","","legacy"),new com.mojang.authlib.properties.PropertyMap(),new com.mojang.authlib.properties.PropertyMap(),java.net.Proxy.NO_PROXY),
    new net.minecraft.client.main.GameConfiguration.DisplayInformation(1,1,false,false),new net.minecraft.client.main.GameConfiguration.FolderInformation(d,d,d,null),
    new net.minecraft.client.main.GameConfiguration.GameInformation(false,"1.12.2","native structural proof"),new net.minecraft.client.main.GameConfiguration.ServerInformation(null,0));
  net.minecraft.client.Minecraft minecraft=new net.minecraft.client.Minecraft(config);minecraft.gameSettings=new net.minecraft.client.settings.GameSettings();
  java.lang.reflect.Field rm=net.minecraft.client.Minecraft.class.getDeclaredField("resourceManager");rm.setAccessible(true);rm.set(minecraft,new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer()));
  net.minecraft.client.renderer.block.statemap.IStateMapper addonMapper=block->java.util.Collections.singletonMap(block.getDefaultState(),new ModelResourceLocation("other:replacement","normal"));
  if(replacements)for(Block b:blocks){net.minecraftforge.client.model.ModelLoader.setCustomStateMapper(b,addonMapper);net.minecraftforge.client.model.ModelLoader.setCustomModelResourceLocation(items.get(b),0,new ModelResourceLocation("other:replacement","inventory"));}
  java.lang.reflect.Field custom=net.minecraftforge.client.model.ModelLoader.class.getDeclaredField("customModels");custom.setAccessible(true);Map<?,?> models=(Map<?,?>)custom.get(null);int initialModels=models.size();Map<?,?> originalModels=new HashMap<>(models);
  com.hbm.main.StructuralBlockModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());
  check(models.size()==initialModels+(replacements?0:32),"native event queues only authorized original owner item metadata routes");
  if(replacements)check(models.equals(originalModels),"same-name replacement addon item model routes are preserved");
  java.lang.reflect.Field sm=net.minecraftforge.client.model.ModelLoader.class.getDeclaredField("customStateMappers");sm.setAccessible(true);Map<?,?> states=(Map<?,?>)sm.get(null);
  for(Block b:unowned)check(!states.containsKey(b.delegate),"foreign namespace and unscoped hbm owners remain untouched by native model event: "+b.getRegistryName());
  if(replacements){for(Block b:blocks)check(states.get(b.delegate)==addonMapper,"same-name replacement addon state mapper preserved");return;}
  for(Block b:blocks){net.minecraft.client.renderer.block.statemap.IStateMapper mapper=(net.minecraft.client.renderer.block.statemap.IStateMapper)states.get(b.delegate);check(mapper!=null,"native state mapper installed");for(Map.Entry<IBlockState,ModelResourceLocation> entry:mapper.putStateModelLocations(b).entrySet())check(entry.getValue().equals(new ModelResourceLocation(b.getRegistryName(),b instanceof BlockMetalFence && b.getMetaFromState(entry.getKey())!=0?"post":"normal")),"all live state combinations route through actual owner blockstate JSON");}
 }
 public static void main(String[]args)throws Exception{Bootstrap.register();if(args.length>1&&args[1].equals("replacements")){registry(true);System.out.println("PASS replacement-owner native model event "+checks+" assertions");return;}Sprite main=new Sprite("main"),other=new Sprite("other");fences(main,other);chains(main,other);geometry(main,other);routes(args[0],main);lighting(main,other);registry(false);System.out.println("PASS structural owners/models "+checks+" assertions");}
}

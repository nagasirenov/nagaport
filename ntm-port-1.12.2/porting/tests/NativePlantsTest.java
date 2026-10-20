import java.util.*;
import com.hbm.blocks.BlockCoral;
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
public class NativePlantsTest {
 static int checks;static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static class Sprite extends TextureAtlasSprite {Sprite(String name,int index){super(name);setIconWidth(16);setIconHeight(16);initSprite(512,512,32*index,48,false);}}
 static class World extends net.minecraft.world.World {
  final Map<BlockPos,IBlockState> blocks=new HashMap<>();int removed;
  World(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()),new net.minecraft.world.WorldProviderSurface(),new net.minecraft.profiler.Profiler(),false);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return true;}
  public IBlockState getBlockState(BlockPos pos){return blocks.getOrDefault(pos,Blocks.AIR.getDefaultState());}
  public boolean setBlockState(BlockPos pos,IBlockState state,int flags){blocks.put(pos,state);if(state.getBlock()==Blocks.AIR)removed++;return true;}
  public int getCombinedLight(BlockPos pos,int light){return ((pos.getY()&15)*16<<16)|((pos.getY()*3&15)*16);}
  public boolean isSideSolid(BlockPos pos,EnumFacing side,boolean fallback){return getBlockState(pos).isSideSolid(this,pos,side);}
  public net.minecraft.world.biome.Biome getBiome(BlockPos pos){return Biomes.PLAINS;}
 }
 static final BlockPos P=new BlockPos(17,31,49);
 static TextureAtlasSprite[] sprites(String kind){String[] names=com.hbm.main.PlantBlockModelHandler.textures(kind);TextureAtlasSprite[] a=new TextureAtlasSprite[names.length];for(int i=0;i<a.length;i++)a[i]=new Sprite(names[i],i);return a;}
 static IBlockState extended(Block b,World w,int meta){IBlockState state=b.getStateFromMeta(meta);w.blocks.put(P,state);return b.getExtendedState(state,w,P);}
 static void compare(List<BakedQuad> quads,List<double[]> expected,String label){
  check(quads.size()*4==expected.size(),label+" count");
  for(int i=0;i<expected.size();i++){BakedQuad q=quads.get(i/4);int stride=q.getFormat().getIntegerSize(),uv=q.getFormat().getUvOffsetById(0)/4;int[] data=q.getVertexData();for(int a=0;a<5;a++)check(data[i%4*stride+(a<3?a:uv+a-3)]==Float.floatToIntBits((float)expected.get(i)[a]),label+" vertex "+i+" component "+a);check(!q.shouldApplyDiffuseLighting(),label+" unshaded");}
 }
 static void geometry()throws Exception {
  World world=new World();BlockCoral coral=new BlockCoral();TextureAtlasSprite[] cs=sprites("coral");PlantBakedModel model=new PlantBakedModel("coral",cs);
  EnumFacing[] order={EnumFacing.NORTH,EnumFacing.SOUTH,EnumFacing.WEST,EnumFacing.EAST,EnumFacing.UP,EnumFacing.DOWN};
  for(int mask=0;mask<64;mask++)for(int meta=0;meta<16;meta++){
   world.blocks.clear();EnumFacing chosen=EnumFacing.DOWN;boolean found=false;
   for(int i=0;i<6;i++)if((mask&(1<<i))!=0){world.blocks.put(P.offset(order[i]),Blocks.STONE.getDefaultState());if(!found){chosen=order[i];found=true;}}
   IBlockState state=extended(coral,world,meta);check(coral.getMetaFromState(state)==meta,"coral full metadata storage");
   check(PlantBakedModel.attachment(world,P)==chosen,"sideways-first attachment");compare(model.getQuads(state,null,0),PlantOriginalOracle.coral(cs[meta%5],chosen),"pristine coral");
   check(model.getQuads(state,EnumFacing.NORTH,0).isEmpty(),"unculled only");
  }
  for(Block neighbor:new Block[]{Blocks.GLASS,Blocks.OAK_FENCE,Blocks.STONE_SLAB,Blocks.LEAVES,Blocks.WATER,Blocks.AIR}){world.blocks.clear();world.blocks.put(P.north(),neighbor.getDefaultState());check(PlantBakedModel.attachment(world,P)==(neighbor.getDefaultState().getMaterial().isSolid()?EnumFacing.NORTH:EnumFacing.DOWN),"original material solidity, not face-solid attachment");}
  BlockHangingVine vine=new BlockHangingVine(Material.VINE);TextureAtlasSprite[] vs=sprites("vine");model=new PlantBakedModel("vine",vs);
  for(Block below:new Block[]{Blocks.AIR,vine,Blocks.STONE,Blocks.WATER}){
   world.blocks.clear();world.blocks.put(P.down(),below.getDefaultState());IBlockState state=extended(vine,world,0);List<BakedQuad> actual=model.getQuads(state,null,0);
   TextureAtlasSprite base=vs[below==vine?0:2],glow=vs[below==Blocks.AIR?4:3];List<double[]> expected=PlantOriginalOracle.vine(base,EnumFacing.DOWN);expected.addAll(PlantOriginalOracle.vine(glow,EnumFacing.DOWN));compare(actual,expected,"pristine vine");check(actual.get(0).getSprite()==base&&actual.get(4).getSprite()==glow,"vine original material branch");
  }
  BlockReeds reeds=new BlockReeds();TextureAtlasSprite[] rs=sprites("reeds");model=new PlantBakedModel("reeds",rs);
  for(int depth=1;depth<=30;depth++)for(boolean enabled:new boolean[]{false,true}){
   world.blocks.clear();for(int i=1;i<depth;i++)world.blocks.put(P.down(i),(i%2==0?Blocks.WATER:Blocks.FLOWING_WATER).getDefaultState());
   IBlockState state=extended(reeds,world,0);com.hbm.config.ClientConfig.RENDER_REEDS.enabled=enabled;int d=enabled?depth:1;
   check(RenderReeds.depth(world,P,enabled)==d,"original water stop depth");List<double[]> expected=new ArrayList<>();for(int i=0;i<d;i++)expected.addAll(NativePlantLegacyOracle.reeds(rs[i==0?0:i==d-1?2:1],i));compare(model.getQuads(state,null,0),expected,"original 1.7 bytecode reeds");
  }
  com.hbm.config.ClientConfig.RENDER_REEDS.enabled=true;world.blocks.clear();for(int y=0;y<31;y++)world.blocks.put(new BlockPos(P.getX(),y,P.getZ()),Blocks.WATER.getDefaultState());check(RenderReeds.depth(world,P,true)==30,"original does not inspect y zero");check(RenderReeds.depth(world,new BlockPos(0,1,0),true)==0,"original y=1 no geometry");
 }
 static void behavior(){
  World world=new World();BlockReeds reeds=new BlockReeds();BlockHangingVine vine=new BlockHangingVine(Material.VINE);BlockCoral coral=new BlockCoral();
  for(Block b:new Block[]{reeds,vine,coral}){IBlockState state=b.getDefaultState();check(state.getCollisionBoundingBox(world,P)==null,"noncolliding plants");check(!state.isFullCube()&&!state.isOpaqueCube(),"nonopaque plants");check(state.getBoundingBox(world,P).equals(Block.FULL_BLOCK_AABB),"original selection bounds");check(b.getRenderLayer()==BlockRenderLayer.CUTOUT_MIPPED,"alpha test layer");}
  for(Block below:new Block[]{Blocks.WATER,Blocks.FLOWING_WATER,Blocks.STONE,Blocks.AIR}){world.blocks.clear();world.blocks.put(P.down(),below.getDefaultState());IBlockState state=extended(reeds,world,0);boolean expected=below==Blocks.WATER||below==Blocks.FLOWING_WATER;check(reeds.canPlaceBlockAt(world,P)==expected,"water-only reed placement");int before=world.removed;reeds.neighborChanged(state,world,P,below,P.down());check(world.removed-before==(expected?0:1),"no-drop reed support loss");}
  for(Block above:new Block[]{vine,Blocks.STONE,Blocks.AIR,Blocks.WATER}){world.blocks.clear();world.blocks.put(P.up(),above.getDefaultState());IBlockState state=extended(vine,world,0);boolean expected=above==vine||above==Blocks.STONE;check(vine.canPlaceBlockAt(world,P)==expected,"vine original UP support");int before=world.removed;vine.neighborChanged(state,world,P,above,P.up());check(world.removed-before==(expected?0:1),"no-drop vine support loss");}
  net.minecraft.entity.item.EntityItem e=new net.minecraft.entity.item.EntityItem(world);e.motionX=2;e.motionY=-4;e.motionZ=6;e.fallDistance=9;vine.onEntityCollision(world,P,vine.getDefaultState(),e);check(e.motionX==1&&e.motionY==-2&&e.motionZ==3&&e.fallDistance==0,"vine collision halves all axes and clears fall");
  check(vine.isShearable(net.minecraft.item.ItemStack.EMPTY,world,P),"shearable");check(vine.onSheared(net.minecraft.item.ItemStack.EMPTY,world,P,20).size()==1,"single shear result");check(reeds.getItemDropped(reeds.getDefaultState(),new Random(),9)==Items.STICK,"reed stick drop");
  for(int meta=0;meta<16;meta++){check(coral.damageDropped(coral.getStateFromMeta(meta))==0,"coral drop meta");check(coral.getItemDropped(coral.getStateFromMeta(meta),new Random(),9)==com.hbm.items.ModItems.powder_calcium,"coral calcium identity");}
  net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> sub=net.minecraft.util.NonNullList.create();coral.getSubBlocks(net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS,sub);check(sub.size()==5,"five creative coral variants");
  for(Block above:new Block[]{Blocks.WATER,Blocks.FLOWING_WATER,Blocks.LAVA,Blocks.AIR,Blocks.STONE}){world.blocks.clear();world.blocks.put(P.up(),above.getDefaultState());check(coral.canPlaceBlockAt(world,P)==above.getDefaultState().getMaterial().isLiquid(),"original liquid above coral placement");}
 }
 static void lighting(){
  World world=new World();net.minecraft.client.renderer.color.BlockColors colors=new net.minecraft.client.renderer.color.BlockColors();
  for(String kind:new String[]{"reeds","vine","coral"}){
   Block block=kind.equals("reeds")?new BlockReeds():kind.equals("vine")?new BlockHangingVine(Material.VINE):new BlockCoral();world.blocks.clear();for(int i=1;i<6;i++)world.blocks.put(P.down(i),Blocks.WATER.getDefaultState());IBlockState state=extended(block,world,0);PlantBakedModel model=new PlantBakedModel(kind,sprites(kind));
   net.minecraft.client.renderer.BufferBuilder out=new net.minecraft.client.renderer.BufferBuilder(4096);out.begin(7,net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK);
   net.minecraftforge.client.model.pipeline.VertexLighterFlat lighter=new net.minecraftforge.client.model.pipeline.VertexLighterFlat(colors);net.minecraftforge.client.model.pipeline.VertexBufferConsumer consumer=new net.minecraftforge.client.model.pipeline.VertexBufferConsumer(out);consumer.setOffset(P);lighter.setParent(consumer);lighter.setWorld(world);lighter.setState(state);lighter.setBlockPos(P);lighter.updateBlockInfo();
   List<BakedQuad> quads=model.getQuads(state,null,0);for(BakedQuad q:quads)q.pipe(lighter);out.finishDrawing();check(out.getVertexCount()==quads.size()*4,"actual pipeline vertex count");java.nio.ByteBuffer bytes=out.getByteBuffer();
   for(int v=0;v<out.getVertexCount();v++){
    int expected=kind.equals("reeds")?state.getPackedLightmapCoords(world,P.down(v/16)):kind.equals("vine")&&v>=16?240:state.getPackedLightmapCoords(world,P);
    check(bytes.getInt(v*28+24)==expected,"exact native "+kind+" packed light vertex "+v+" got "+Integer.toHexString(bytes.getInt(v*28+24))+" expected "+Integer.toHexString(expected));
    check(bytes.getInt(v*28+12)==(kind.equals("reeds")?0xffbfbfbf:0xffffffff),"original shade "+kind);
    for(int a=0;a<3;a++)check(Math.abs(bytes.getFloat(v*28+a*4)-(Float.intBitsToFloat(quads.get(v/4).getVertexData()[(v%4)*7+a])+(a==0?P.getX():a==1?P.getY():P.getZ())))<1E-5,"pipeline original position");
   }
   // The scoped lighter must be usable by the next unrelated quad without retained glow/depth light.
   out=new net.minecraft.client.renderer.BufferBuilder(4096);out.begin(7,net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK);consumer=new net.minecraftforge.client.model.pipeline.VertexBufferConsumer(out);consumer.setOffset(P);lighter.setParent(consumer);
   BakedQuad q=quads.get(0);new BakedQuad(q.getVertexData(),-1,q.getFace(),q.getSprite(),false,q.getFormat()).pipe(lighter);out.finishDrawing();check(out.getByteBuffer().getInt(24)>=world.getCombinedLight(P,0),"lighter restored original world");
   out=new net.minecraft.client.renderer.BufferBuilder(4096);out.begin(7,net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK);new net.minecraft.client.renderer.BlockModelRenderer(colors).renderModelFlat(world,model,state,P,out,true,0);out.finishDrawing();
   boolean mismatch=false;for(int v=0;v<out.getVertexCount();v++){int expected=kind.equals("reeds")?state.getPackedLightmapCoords(world,P.down(v/16)):kind.equals("vine")&&v>=16?240:state.getPackedLightmapCoords(world,P);if(out.getByteBuffer().getInt(v*28+24)!=expected)mismatch=true;}
   check(mismatch!=kind.equals("coral"),"explicit nondefault vanilla fallback boundary "+kind);
  }
  System.out.println("VERIFIED compatibility gate: Forge light pipeline exact; disabled Forge pipeline loses reed-depth and vine-glow light. Coral fallback remains exact.");
 }
 static void set(Object object,Class<?> type,String name,Object value)throws Exception {java.lang.reflect.Field field=type.getDeclaredField(name);field.setAccessible(true);field.set(object,value);}
 static void registry(String root,boolean replacements)throws Exception {
  java.lang.reflect.Field unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)unsafeField.get(null);
  net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);
  net.minecraft.launchwrapper.Launch.blackboard=new HashMap<String,Object>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
  net.minecraft.client.resources.SimpleReloadableResourceManager resources=new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());resources.reloadResources(Collections.singletonList(new net.minecraft.client.resources.FolderResourcePack(new java.io.File(root,"src/main/resources"))));set(mc,net.minecraft.client.Minecraft.class,"resourceManager",resources);
  net.minecraft.client.renderer.texture.TextureMap atlas=new net.minecraft.client.renderer.texture.TextureMap("textures");net.minecraft.client.renderer.block.model.ModelManager manager=new net.minecraft.client.renderer.block.model.ModelManager(atlas);new net.minecraftforge.client.model.ModelLoader(resources,atlas,new net.minecraft.client.renderer.BlockModelShapes(manager));
  Block[] blocks=replacements?new Block[]{new Block(Material.PLANTS){},new Block(Material.WATER){},new Block(Material.VINE){}}:new Block[]{new BlockReeds(),new BlockCoral(),new BlockHangingVine(Material.VINE)};
  String[] names={"plant_reeds","laythe_coral","vine_phosphor"},kinds={"reeds","coral","vine"};
  for(int i=0;i<3;i++){Block b=blocks[i];b.setRegistryName("hbm","tile."+names[i]);net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(b);net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(new net.minecraft.item.ItemBlock(b).setRegistryName(b.getRegistryName()));}
  Block[] unowned={new BlockReeds(),new BlockCoral(),new BlockHangingVine(Material.VINE)};for(int i=0;i<3;i++){unowned[i].setRegistryName(i==0?"other":"hbm","tile.other_"+kinds[i]);net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(unowned[i]);}
  java.lang.reflect.Field f=net.minecraftforge.client.model.ModelLoader.class.getDeclaredField("customModels");f.setAccessible(true);Map<?,?> custom=(Map<?,?>)f.get(null);int before=custom.size();
  com.hbm.main.PlantBlockModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());check(custom.size()==before+(replacements?0:48),"actual 48 owned native item routes, replacement isolation");
  f=net.minecraftforge.client.model.ModelLoader.class.getDeclaredField("customStateMappers");f.setAccessible(true);Map<?,?> mappers=(Map<?,?>)f.get(null);for(Block b:unowned)check(!mappers.containsKey(b.delegate),"unowned addon block skipped");if(replacements){for(Block b:blocks)check(!mappers.containsKey(b.delegate),"same-name addon replacement skipped");return;}
  net.minecraftforge.client.model.ModelLoaderRegistry.clearModelCache(resources);
  net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> bakedRegistry=new net.minecraft.util.registry.RegistrySimple<>();
  for(int b=0;b<3;b++){
   Block block=blocks[b];net.minecraft.item.Item item=net.minecraft.item.Item.getItemFromBlock(block);final TextureAtlasSprite[] sprites=sprites(kinds[b]);final String kind=kinds[b];
   java.util.function.Function<ResourceLocation,TextureAtlasSprite> getter=location->{for(TextureAtlasSprite sprite:sprites)if(location.getPath().endsWith(sprite.getIconName()))return sprite;throw new AssertionError("Unknown sprite "+location);};
   net.minecraft.client.renderer.block.statemap.IStateMapper mapper=(net.minecraft.client.renderer.block.statemap.IStateMapper)mappers.get(block.delegate);check(mapper!=null,"actual state mapper");
   ModelBlockDefinition definition;try(net.minecraft.client.resources.IResource resource=resources.getResource(new ResourceLocation("hbm","blockstates/tile."+names[b]+".json"));java.io.Reader reader=new java.io.InputStreamReader(resource.getInputStream(),java.nio.charset.StandardCharsets.UTF_8)){definition=ModelBlockDefinition.parseFromReader(reader,block.getRegistryName());}
   for(Map.Entry<IBlockState,ModelResourceLocation> entry:mapper.putStateModelLocations(block).entrySet()){
    int variant=kind.equals("coral")?block.getMetaFromState(entry.getKey())%5:0;check(entry.getValue().equals(new ModelResourceLocation(block.getRegistryName(),"variant="+variant)),"all actual metadata states routed");
    check(definition.hasVariant(entry.getValue().getVariant()),"actual blockstate variant exists");ResourceLocation location=definition.getVariant(entry.getValue().getVariant()).getVariantList().get(0).getModelLocation();
    net.minecraftforge.client.model.IModel source=net.minecraftforge.client.model.ModelLoaderRegistry.getModel(location);check(source!=net.minecraftforge.client.model.ModelLoaderRegistry.getMissingModel(),"actual world model loader dispatch");IBakedModel baked=source.bake(source.getDefaultState(),net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK,getter);World world=new World();IBlockState extended=block.getExtendedState(entry.getKey(),world,P);check(!baked.getQuads(extended,null,0).isEmpty(),"loaded model emits world quads");check(baked.getParticleTexture()==sprites[variant],"variant-specific particles");
   }
   for(int meta=0;meta<16;meta++){
    ModelResourceLocation location=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,meta));check(location!=null,"item metadata route");net.minecraftforge.client.model.IModel source=net.minecraftforge.client.model.ModelLoaderRegistry.getModel(location);check(source instanceof net.minecraftforge.client.model.ItemLayerModel,"original flat inventory native model");
    IBakedModel baked=source.bake(source.getDefaultState(),net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM,getter);check(!baked.getQuads(null,null,0).isEmpty(),"native item layer quads");bakedRegistry.putObject(location,baked);
   }
  }
  set(manager,net.minecraft.client.renderer.block.model.ModelManager.class,"modelRegistry",bakedRegistry);net.minecraft.client.renderer.ItemModelMesher mesher=new net.minecraft.client.renderer.ItemModelMesher(manager);net.minecraftforge.client.model.ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();
  for(Block block:blocks)for(int meta=0;meta<16;meta++){net.minecraft.item.Item item=net.minecraft.item.Item.getItemFromBlock(block);ModelResourceLocation route=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,meta));check(mesher.getItemModel(new net.minecraft.item.ItemStack(item,1,meta))==bakedRegistry.getObject(route),"actual inventory mesh metadata lookup");}
  BlockHangingVine vine=(BlockHangingVine)blocks[2];net.minecraft.item.ItemStack shear=vine.onSheared(net.minecraft.item.ItemStack.EMPTY,new World(),P,0).get(0);check(shear.getCount()==1&&shear.getItem()==net.minecraft.item.Item.getItemFromBlock(vine),"actual registered vine shear item");
  java.nio.file.Path zip=java.nio.file.Files.createTempFile("plant-pack-",".zip");
  try{
   try(java.util.zip.ZipOutputStream out=new java.util.zip.ZipOutputStream(java.nio.file.Files.newOutputStream(zip))){for(String kind:kinds)for(String name:com.hbm.main.PlantBlockModelHandler.textures(kind)){String path="assets/hbm/textures/blocks/"+name+".png";out.putNextEntry(new java.util.zip.ZipEntry(path));java.nio.file.Files.copy(java.nio.file.Paths.get(root,"src/main/resources",path),out);out.closeEntry();}}
   resources.reloadResources(Collections.singletonList(new net.minecraft.client.resources.FileResourcePack(zip.toFile())));net.minecraftforge.client.model.ModelLoaderRegistry.clearModelCache(resources);
   for(String kind:kinds)for(String name:com.hbm.main.PlantBlockModelHandler.textures(kind))try(net.minecraft.client.resources.IResource resource=resources.getResource(new ResourceLocation("hbm","textures/blocks/"+name+".png"))){check(resource.getInputStream().read()==137,"packaged original texture loaded after reload");}
   for(String kind:kinds){net.minecraftforge.client.model.IModel source=net.minecraftforge.client.model.ModelLoaderRegistry.getModel(new ResourceLocation("hbm:native_plants/"+kind+"/world/0"));check(source!=net.minecraftforge.client.model.ModelLoaderRegistry.getMissingModel(),"native model dispatch after resource reload");}
  }finally{java.nio.file.Files.deleteIfExists(zip);}
 }

 public static void main(String[] args)throws Exception {Bootstrap.register();if(args.length>2){registry(args[0],true);System.out.println("PASS plant replacement owner isolation "+checks+" assertions");return;}NativePlantLegacyOracle.initialize(args[1]);geometry();behavior();lighting();registry(args[0],false);System.out.println("PASS native plant owners/model geometry/lighting "+checks+" assertions");}
}

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.*;
import sun.misc.Unsafe;
import com.hbm.blocks.generic.*;
import com.hbm.blocks.generic.BlockBarrier;
import com.hbm.blocks.machine.rbmk.*;
import com.hbm.render.block.*;
import com.hbm.render.block.nativeblock.*;
import com.hbm.main.UniversalBlockModelHandler;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.pipeline.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class NativeUniversalBlocksTest {
 static int checks;
 static void check(boolean v,String why){checks++;if(!v)throw new AssertionError(why);}
 static void equal(double a,double b,String why){check(Math.abs(a-b)<1e-6,why+": "+a+" != "+b);}
 static class Sprite extends TextureAtlasSprite {Sprite(){super("probe");setIconWidth(16);setIconHeight(16);initSprite(256,256,32,48,false);}}
 static class WorldProbe extends World {
  final Map<BlockPos,IBlockState> states=new HashMap<>(); net.minecraft.tileentity.TileEntity tile;int flags;
  WorldProbe(boolean remote){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),remote);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean e){return true;}
  public BlockPos getSpawnPoint(){return new BlockPos(0,64,0);}
  public IBlockState getBlockState(BlockPos p){return states.getOrDefault(p,net.minecraft.init.Blocks.AIR.getDefaultState());}
  public boolean setBlockState(BlockPos p,IBlockState state,int f){states.put(p.toImmutable(),state);flags=f;return true;}
  public net.minecraft.tileentity.TileEntity getTileEntity(BlockPos p){return tile;}
 }
 static class Player extends EntityPlayer {
  boolean sneaking;
  Player(World w){super(w,new com.mojang.authlib.GameProfile(new UUID(1,2),"probe"));}
  public boolean isSpectator(){return false;}public boolean isCreative(){return false;}
  public boolean isSneaking(){return sneaking;}
 }
 static void owners(){
  WorldProbe w=new WorldProbe(false);BlockPos p=new BlockPos(4,64,9);Player player=new Player(w);
  BlockSandbags sand=new BlockSandbags(Material.GROUND);BlockBarrier barrier=new BlockBarrier(Material.WOOD);BlockWoodStructure wood=new BlockWoodStructure(Material.WOOD);
  Block[] blocks={sand,barrier,wood,new RBMKDisplay(),new RBMKGauge(),new RBMKKeyPad()};
  for(Block block:blocks)for(int meta=0;meta<16;meta++){
   IBlockState state=block.getStateFromMeta(meta);w.states.clear();w.states.put(p,state);
   check(block.getMetaFromState(state)==meta,"native metadata roundtrip");check(!state.isOpaqueCube()&&!state.isFullCube(),"original noncube");
   check(state.getRenderType()==EnumBlockRenderType.MODEL,"native model callback");
   check(state.getBlockFaceShape(w,p,EnumFacing.NORTH)==BlockFaceShape.UNDEFINED,"not a false support cube");
   if(block==wood){check(block.damageDropped(state)==meta%3,"wood variant drop");check(wood.isSideSolid(state,w,p,EnumFacing.UP)==(meta%3==1),"scaffold support");
    com.hbm.extprop.HbmPlayerProps.instance.isOnLadder=false;wood.onEntityCollision(w,p,state,player);check(com.hbm.extprop.HbmPlayerProps.instance.isOnLadder==(meta%3==1),"player ladder property callback");
    AxisAlignedBB selected=wood.getSelectedBoundingBox(state,w,p).offset(-p.getX(),-p.getY(),-p.getZ());check(meta%3!=1||selected.equals(Block.FULL_BLOCK_AABB),"scaffold broad selection");
   }
   if(block instanceof RBMKMiniPanelBase){
    check(block.hasTileEntity(state),"native container tile ownership");check(block.createTileEntity(w,state)!=null,"concrete tile dispatch");
    for(int next=0;next<16;next++){net.minecraft.tileentity.TileEntity tile=block.createTileEntity(w,state);check(!tile.shouldRefresh(w,p,state,block.getStateFromMeta(next)),"metadata keeps native tile");}
    check(((ISBRHUniversal)block).getRenderBoxes(state,false).get(0).equals(block.getBoundingBox(state,w,p)),"panel collision/model dimensions");
   }
  }
  for(int mask=0;mask<64;mask++)for(int meta=0;meta<16;meta++){
   w.states.clear();for(EnumFacing f:EnumFacing.values())if((mask&1<<f.getIndex())!=0)w.states.put(p.offset(f),net.minecraft.init.Blocks.STONE.getDefaultState());
   for(Block block:new Block[]{sand,barrier}){
    IBlockState state=block.getStateFromMeta(meta);w.states.put(p,state);IBlockState ext=block.getExtendedState(state,w,p);int expected=mask;
    if(block==barrier&&meta>=2&&meta<=5)expected|=1<<EnumFacing.byIndex(meta).getOpposite().getIndex();
    check(UniversalBlockState.connections(ext)==expected,"solid neighbor snapshot");
    if(block==sand){AxisAlignedBB b=block.getBoundingBox(state,w,p);equal(b.minX,(mask&16)!=0?0:.25,"sand west");equal(b.maxX,(mask&32)!=0?1:.75,"sand east");check(state.getCollisionBoundingBox(w,p).equals(b),"native collision calls connected bounds");}
    else {List<AxisAlignedBB> hits=new ArrayList<>();barrier.addCollisionBoxToList(state,w,p,new AxisAlignedBB(p).grow(2),hits,null,false);check(hits.size()==Integer.bitCount(expected&60),"four directional collision slabs, no roof");}
   }
   for(EnumFacing f:EnumFacing.values())w.states.put(p.offset(f),(mask&1<<f.getIndex())!=0?wood.getStateFromMeta(meta):wood.getStateFromMeta((meta+3)&15));
   IBlockState state=wood.getStateFromMeta(meta);w.states.put(p,state);check(UniversalBlockState.connections(wood.getExtendedState(state,w,p))==mask,"wood requires exact raw metadata, not rectified type");
  }
  for(EnumFacing f:EnumFacing.values())for(int meta=0;meta<16;meta++){
   IBlockState placed=barrier.getStateForPlacement(w,p,f,.5F,.5F,.5F,meta,player);check(barrier.getMetaFromState(placed)==(f.getAxis().isHorizontal()?f.getIndex():meta),"barrier placement side");
  }
  for(int angle=-720;angle<=720;angle+=5)for(Block block:new Block[]{barrier,blocks[3]})for(int initial:new int[]{0,1,2,5}){
   player.rotationYaw=angle;IBlockState state=block.getStateFromMeta(initial);w.states.put(p,state);block.onBlockPlacedBy(w,p,state,player,ItemStack.EMPTY);
   int expected=block==barrier&&initial!=0?initial:new int[]{2,5,3,4}[MathHelper.floor(angle*4F/360F+.5D)&3];
   check(block.getMetaFromState(w.getBlockState(p))==expected,"original yaw and barrier preservation");
  }
  RBMKDisplay display=new RBMKDisplay();com.hbm.tileentity.machine.rbmk.TileEntityRBMKDisplay screen=new com.hbm.tileentity.machine.rbmk.TileEntityRBMKDisplay();w.tile=screen;
  check(!display.onScrew(w,player,p.getX(),p.getY(),p.getZ(),0,0,0,0,api.hbm.block.IToolable.ToolType.WRENCH),"wrong display tool does not rotate");
  check(display.onScrew(w,player,p.getX(),p.getY(),p.getZ(),0,0,0,0,api.hbm.block.IToolable.ToolType.SCREWDRIVER)&&screen.rotations==1,"display native world tile rotate dispatch");
  WorldProbe remote=new WorldProbe(true);remote.tile=screen;check(display.onScrew(remote,player,p.getX(),p.getY(),p.getZ(),0,0,0,0,api.hbm.block.IToolable.ToolType.SCREWDRIVER)&&screen.rotations==1,"display client tool does not rotate server controls");
  RBMKKeyPad keypad=new RBMKKeyPad();
  IBlockState keypadState=keypad.getStateFromMeta(2);w.states.put(p,keypadState);com.hbm.tileentity.machine.rbmk.TileEntityRBMKKeyPad disabled=new com.hbm.tileentity.machine.rbmk.TileEntityRBMKKeyPad();disabled.keys[1].active=false;w.tile=disabled;
  check(!keypad.onBlockActivated(w,p,keypadState,player,EnumHand.MAIN_HAND,EnumFacing.NORTH,.25F,.75F,.25F)&&disabled.keys[1].clicks==0,"inactive original keypad key rejects click");
  player.sneaking=true;check(!keypad.onBlockActivated(w,p,keypadState,player,EnumHand.MAIN_HAND,EnumFacing.NORTH,.75F,.75F,.25F),"server sneaking leaves keypad alone");
  check(keypad.onBlockActivated(remote,p,keypadState,player,EnumHand.MAIN_HAND,EnumFacing.NORTH,.75F,.75F,.25F),"client return precedes sneaking as original");player.sneaking=false;

  for(int meta=0;meta<16;meta++)for(EnumFacing side:EnumFacing.values())for(float x:new float[]{0,.25F,.75F,1})for(float y:new float[]{.25F,.75F})for(float z:new float[]{0,.25F,.75F,1}){
   IBlockState state=keypad.getStateFromMeta(meta);w.states.put(p,state);com.hbm.tileentity.machine.rbmk.TileEntityRBMKKeyPad tile=new com.hbm.tileentity.machine.rbmk.TileEntityRBMKKeyPad();w.tile=tile;
   boolean result=keypad.onBlockActivated(w,p,state,player,EnumHand.MAIN_HAND,side,x,y,z);check(result,"original keypad return");
   int hit=0;if(meta==2&&x<.5||meta==3&&x>.5||meta==4&&z>.5||meta==5&&z<.5)hit=1;if(y<.5)hit+=2;
   boolean clicked=x!=0&&x!=1&&z!=0&&z!=1&&side.getAxis()!=EnumFacing.Axis.Y;
   for(int i=0;i<4;i++)check(tile.keys[i].clicks==(clicked&&i==hit?1:0),"original keypad hit mapping");
  }
 }

 static double[] coordinates(AxisAlignedBB b){return new double[]{b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ};}
 static final Set<String> seenBounds=new LinkedHashSet<>();
 static final List<double[]> testBounds=new ArrayList<>();
 static void geometry(){
  Block[] blocks={new BlockSandbags(Material.GROUND),new BlockBarrier(Material.WOOD),new BlockWoodStructure(Material.WOOD),new BlockRebar(),new RBMKDisplay()};
  Sprite sprite=new Sprite();
  for(int kind=0;kind<blocks.length;kind++)for(int meta=0;meta<16;meta++)for(int mask=0;mask<64;mask++)for(boolean item:new boolean[]{false,true})for(boolean simple:new boolean[]{false,true}){
   Block block=blocks[kind];IBlockState state=UniversalBlockState.withConnections(block.getStateFromMeta(meta),kind==1&&meta>=2&&meta<=5?mask|(1<<EnumFacing.byIndex(meta).getOpposite().getIndex()):mask);
   com.hbm.config.ClientConfig.RENDER_REBAR_SIMPLE.value=simple;
   List<LegacyUniversalGeometryOracle.Shape> expected=LegacyUniversalGeometryOracle.build(kind,meta,mask,item,simple);
   List<AxisAlignedBB> actual=((ISBRHUniversal)block).getRenderBoxes(state,item);
   check(actual.size()==expected.size(),"original cuboid draw count including scaffold duplicates kind="+kind+" meta="+meta+" mask="+mask);
   for(int b=0;b<actual.size();b++){
    double[] values=coordinates(actual.get(b));for(int axis=0;axis<6;axis++)check(Double.doubleToLongBits(values[axis])==Double.doubleToLongBits(expected.get(b).bounds[axis]),"exact original cuboid bounds kind="+kind+" box="+b+" axis="+axis);
    if(item)equal(((ISBRHUniversal)block).getInventoryYOffset(state),expected.get(b).offset,"original inventory offset");
    if(seenBounds.add(Arrays.toString(values)))testBounds.add(values);
   }
   if(mask==0&& !simple){
    IBakedModel model=UniversalModelFactory.bake(block,meta,item,sprite);List<BakedQuad> quads=new ArrayList<>(model.getQuads(state,null,0));for(EnumFacing face:EnumFacing.values())quads.addAll(model.getQuads(state,face,0));
    check(quads.size()==actual.size()*6,"actual native model owns exactly all six cuboid faces");check(model.isAmbientOcclusion()&&model.isGui3d()&&!model.isBuiltInRenderer(),"native AO/model flags");
   }
  }
  System.out.println("PASS complete original geometry oracle: five owners, all16 metadata, all64 adjacency masks, item/world, both rebar configurations; "+testBounds.size()+" distinct bounds");
 }
 static Field field(Class<?> c,String name)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}
 static void faceOracle(String jar)throws Exception{
  Unsafe unsafe=(Unsafe)field(Unsafe.class,"theUnsafe").get(null);Sprite sprite=new Sprite();
  try(URLClassLoader legacy=new URLClassLoader(new URL[]{new File(jar).toURI().toURL()},NativeUniversalBlocksTest.class.getClassLoader())){
   Class<?> renderer=Class.forName("blm",true,legacy),block=Class.forName("aji",true,legacy),iconType=Class.forName("rf",true,legacy),tessType=Class.forName("bmh",true,legacy);
   Object r=unsafe.allocateInstance(renderer),tess=tessType.getField("a").get(null);String[] boundFields={"i","k","m","j","l","n"};Field raw=field(tessType,"f"),count=field(tessType,"g"),ptr=field(tessType,"p");
   Object icon=java.lang.reflect.Proxy.newProxyInstance(legacy,new Class<?>[]{iconType},(p,m,a)->{
    if(a!=null&&a.length==1)return m.getName().equals("a")?sprite.getInterpolatedU((Double)a[0]):sprite.getInterpolatedV((Double)a[0]);
    switch(m.getName()){case "a":case "b":return 16;case "c":return sprite.getMinU();case "d":return sprite.getMaxU();case "e":return sprite.getMinV();case "f":return sprite.getMaxV();default:return sprite.getIconName();}
   });
   for(double[] b:testBounds)for(boolean item:new boolean[]{false,true})for(double offset:new double[]{0,.125,.625})for(EnumFacing side:EnumFacing.values()){
    for(int i=0;i<6;i++)field(renderer,boundFields[i]).setDouble(r,b[i]);
    List<double[]> old=new ArrayList<>();
    if(!item&&(side==EnumFacing.NORTH||side==EnumFacing.EAST))old=LegacyUniversalFaceOracle.build(b,sprite,side.getIndex());
    else{
     count.setInt(tess,0);ptr.setInt(tess,0);renderer.getMethod("abcdef".substring(side.getIndex(),side.getIndex()+1),block,double.class,double.class,double.class,iconType).invoke(r,null,0D,0D,0D,icon);
     check(count.getInt(tess)==4,"actual original four face vertices");int[] rawOld=(int[])raw.get(tess);
     for(int v=0;v<4;v++){double[] values=new double[5];for(int c=0;c<5;c++)values[c]=Float.intBitsToFloat(rawOld[v*8+c]);old.add(values);}
    }
    BakedQuad q=UniversalCuboidGeometry.face(new AxisAlignedBB(b[0],b[1],b[2],b[3],b[4],b[5]),sprite,item,offset,side);int[] packed=q.getVertexData();int stride=q.getFormat().getIntegerSize();
    EnumFacing normal=item&&side.getAxis().isHorizontal()?side.rotateYCCW():side;
    check(q.getFace()==normal&&q.shouldApplyDiffuseLighting()&&!q.hasTintIndex(),"native material/normal flags");check(q.getSprite()==sprite,"atlas sprite identity");
    for(int vertex=0;vertex<4;vertex++){
     double[] ov=old.get((vertex+new int[]{0,2,1,0,1,3}[side.getIndex()])%4);
     double[] expected={item?ov[2]:ov[0],ov[1]+offset,item?1-ov[0]:ov[2]};
     for(int axis=0;axis<3;axis++)equal(Float.intBitsToFloat(packed[vertex*stride+axis]),expected[axis],"original +90 rotation/offset position");
     for(int uv=0;uv<2;uv++)check(packed[vertex*stride+4+uv]==Float.floatToIntBits((float)ov[3+uv]),"exact original UV float bits side="+side+" item="+item+" bounds="+Arrays.toString(b));
     check(packed[vertex*stride+3]==-1,"no pre-baked tint or doubled AO");int n=packed[vertex*stride+6];check((byte)n==127*normal.getXOffset()&&(byte)(n>>8)==127*normal.getYOffset()&&(byte)(n>>16)==127*normal.getZOffset(),"exact rotated normal");
    }
   }
  }
  System.out.println("PASS original client face bytecode plus complete original RenderBlocksNT mirror overrides: UV clamps, normals, positions, item offset/rotation");
 }

 static class NativeWorld extends World {
  final Map<Long,net.minecraft.world.chunk.Chunk> chunks=new HashMap<>();
  NativeWorld(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),false);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){net.minecraft.world.chunk.Chunk c=new net.minecraft.world.chunk.Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return new net.minecraft.world.chunk.IChunkProvider(){public net.minecraft.world.chunk.Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public net.minecraft.world.chunk.Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "native panel lifecycle";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return chunks.containsKey(key(x,z));}
  public boolean checkLight(BlockPos p){return true;}
 }
 static void tileLifecycle(){
  NativeWorld world=new NativeWorld();Player player=new Player(world);BlockPos p=new BlockPos(8,64,8);
  for(Block block:new Block[]{new RBMKDisplay(),new RBMKGauge(),new RBMKKeyPad()}){
   world.setBlockState(p,block.getDefaultState(),2);net.minecraft.tileentity.TileEntity tile=world.getTileEntity(p);check(tile!=null,"actual Chunk creates native panel tile");
   tile.getTileData().setString("sentinel","preserved-controls");
   for(int angle=0;angle<360;angle+=15){player.rotationYaw=angle;block.onBlockPlacedBy(world,p,world.getBlockState(p),player,ItemStack.EMPTY);check(world.getTileEntity(p)==tile&&!tile.isInvalid(),"actual placement native Chunk retains tile identity");check(tile.getTileData().getString("sentinel").equals("preserved-controls"),"native facing retains tile data");}
   for(int m=0;m<16;m++){world.setBlockState(p,block.getStateFromMeta(m),2);check(world.getTileEntity(p)==tile,"all16 native metadata updates retain tile");}
   world.setBlockToAir(p);check(tile.isInvalid()&&world.getTileEntity(p)==null,"different owner removes actual native tile");
  }
 }
 static void registry(String root)throws Exception{
  String[] names={"sandbags","wood_barrier","wood_structure","rebar","rbmk_display","rbmk_key_pad","rbmk_gauge"};
  String[] textures={"sandbags","wood_barrier","wood_barrier","rebar","rbmk/rbmk_display","rbmk/rbmk_display","rbmk/rbmk_display"};
  Block[] blocks={new BlockSandbags(Material.GROUND),new BlockBarrier(Material.WOOD),new BlockWoodStructure(Material.WOOD),new BlockRebar(),new RBMKDisplay(),new RBMKKeyPad(),new RBMKGauge()};
  Map<Block,Item> itemMap=(Map<Block,Item>)field(Item.class,"BLOCK_TO_ITEM").get(null);
  for(int i=0;i<blocks.length;i++){Block block=blocks[i];block.setTranslationKey(names[i]);block.setRegistryName("hbm",block.getTranslationKey());ForgeRegistries.BLOCKS.register(block);ItemBlock item=new ItemBlock(block);item.setRegistryName(block.getRegistryName());ForgeRegistries.ITEMS.register(item);itemMap.put(block,item);}
  Block foreign=new BlockSandbags(Material.GROUND).setRegistryName("other","sandbags");ForgeRegistries.BLOCKS.register(foreign);
  Block unknown=new BlockSandbags(Material.GROUND).setRegistryName("hbm","other_sandbags");ForgeRegistries.BLOCKS.register(unknown);
  java.io.File d=new java.io.File(".");
  net.minecraft.client.main.GameConfiguration config=new net.minecraft.client.main.GameConfiguration(
   new net.minecraft.client.main.GameConfiguration.UserInformation(new Session("UniversalProbe","00000000-0000-0000-0000-000000000000","","legacy"),new com.mojang.authlib.properties.PropertyMap(),new com.mojang.authlib.properties.PropertyMap(),java.net.Proxy.NO_PROXY),
   new net.minecraft.client.main.GameConfiguration.DisplayInformation(1,1,false,false),new net.minecraft.client.main.GameConfiguration.FolderInformation(d,d,d,null),new net.minecraft.client.main.GameConfiguration.GameInformation(false,"1.12.2","native universal proof"),new net.minecraft.client.main.GameConfiguration.ServerInformation(null,0));
  net.minecraft.client.Minecraft mc=new net.minecraft.client.Minecraft(config);mc.gameSettings=new net.minecraft.client.settings.GameSettings();
  net.minecraft.client.resources.SimpleReloadableResourceManager resources=new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());resources.reloadResourcePack(new net.minecraft.client.resources.FolderResourcePack(new File(root,"src/main/resources")));field(net.minecraft.client.Minecraft.class,"resourceManager").set(mc,resources);
  Method owns=UniversalBlockModelHandler.class.getDeclaredMethod("owns",Block.class,ResourceLocation.class);owns.setAccessible(true);
  check(!(Boolean)owns.invoke(null,new BlockSandbags(Material.GROUND){},new ResourceLocation("hbm:tile.sandbags")),"same-name addon subclass is not silently captured");
  check(!(Boolean)owns.invoke(null,new BlockBarrier(Material.WOOD),new ResourceLocation("hbm:tile.sandbags")),"same-name unrelated owner is not silently captured");
  UniversalBlockModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());
  Map<?,?> itemRoutes=(Map<?,?>)field(ModelLoader.class,"customModels").get(null),states=(Map<?,?>)field(ModelLoader.class,"customStateMappers").get(null);
  check(!states.containsKey(foreign.delegate)&&!states.containsKey(unknown.delegate),"foreign/unowned custom rendering untouched");
  ICustomModelLoader loader=(ICustomModelLoader)field(UniversalBlockModelHandler.class,"LOADER").get(null);Sprite first=new Sprite(),second=new Sprite();
  for(int i=0;i<blocks.length;i++){
   Block block=blocks[i];net.minecraft.client.renderer.block.statemap.IStateMapper mapper=(net.minecraft.client.renderer.block.statemap.IStateMapper)states.get(block.delegate);check(mapper!=null,"actual state mapper registered");
   Map<IBlockState,ModelResourceLocation> routes=mapper.putStateModelLocations(block);check(routes.size()==16,"all16 real block states mapped, no adjacency state explosion");
   for(Map.Entry<IBlockState,ModelResourceLocation> route:routes.entrySet())check(route.getValue().equals(new ModelResourceLocation(UniversalBlockModelHandler.route(block.getRegistryName(),block.getMetaFromState(route.getKey()),false),"normal")),"real custom mapper targets loader world path");
   for(int meta=0;meta<16;meta++)for(boolean item:new boolean[]{false,true})for(String prefix:new String[]{"","block/","models/block/"}){
    ResourceLocation path=UniversalBlockModelHandler.route(block.getRegistryName(),meta,item);path=new ResourceLocation(path.getNamespace(),prefix+path.getPath());check(loader.accepts(path),"model route accepted");
    IModel model=loader.loadModel(path);ResourceLocation texture=new ResourceLocation("hbm","blocks/"+textures[i]);check(model.getTextures().contains(texture),"original material binding");
    for(ResourceLocation material:model.getTextures())try(net.minecraft.client.resources.IResource resource=resources.getResource(new ResourceLocation(material.getNamespace(),"textures/"+material.getPath()+".png"))){check(javax.imageio.ImageIO.read(resource.getInputStream())!=null,"original atlas PNG loads");}
    IBakedModel baked=model.bake(net.minecraftforge.common.model.TRSRTransformation.identity(),DefaultVertexFormats.ITEM,r->first);check(baked.getParticleTexture()==first,"actual native bake sprite ownership");
    loader.onResourceManagerReload(resources);IBakedModel rebaked=loader.loadModel(path).bake(net.minecraftforge.common.model.TRSRTransformation.identity(),DefaultVertexFormats.ITEM,r->second);check(rebaked!=baked&&rebaked.getParticleTexture()==second,"reload avoids stale cached atlas sprite");
    if(i==3)check(model.getTextures().contains(new ResourceLocation("hbm:blocks/concrete_liquid")),"dynamic concrete fill reserved in atlas");
   }
  }
  check(itemRoutes.size()>=112,"actual registry queues all112 item variants");
  BlockWoodStructure wood=(BlockWoodStructure)blocks[2];List<String> icons=new ArrayList<>();wood.registerBlockIcons(name->{icons.add(name);return null;});check(icons.equals(Collections.singletonList("hbm:wood_barrier")),"existing common icon stitch callback still has original wood material");
  check(!loader.accepts(new ResourceLocation("foreign:native_universal/tile.sandbags/0/world")),"loader namespace isolation");
  for(String bad:new String[]{"tile.sandbags/16/world","tile.sandbags/0/nope","unknown/0/world"})try{loader.loadModel(new ResourceLocation("hbm","native_universal/"+bad));throw new AssertionError("invalid internal route accepted");}catch(IllegalArgumentException expected){checks++;}
  System.out.println("PASS all seven native registry/model routes, original materials, item/world rebakes, unowned blocks left untouched");
 }

 static final class LightingWorld implements IBlockAccess {
  final BlockPos origin=new BlockPos(8,64,8);final IBlockState own;final int mask;int samples;
  LightingWorld(IBlockState own,int mask){this.own=own;this.mask=mask;}
  public net.minecraft.tileentity.TileEntity getTileEntity(BlockPos p){return null;}
  public int getCombinedLight(BlockPos p,int emission){samples++;int x=p.getX()-8,y=p.getY()-64,z=p.getZ()-8;int sky=Math.floorMod(11+3*x+5*y+7*z,16),block=Math.floorMod(7+5*x+3*y+z,16);return sky*16<<16|block*16;}
  public IBlockState getBlockState(BlockPos p){if(p.equals(origin))return own;for(EnumFacing f:EnumFacing.values())if(p.equals(origin.offset(f))&&(mask&1<<f.getIndex())!=0)return net.minecraft.init.Blocks.STONE.getDefaultState();return net.minecraft.init.Blocks.AIR.getDefaultState();}
  public boolean isAirBlock(BlockPos p){return getBlockState(p).getBlock()==net.minecraft.init.Blocks.AIR;}
  public net.minecraft.world.biome.Biome getBiome(BlockPos p){return net.minecraft.init.Biomes.PLAINS;}
  public int getStrongPower(BlockPos p,EnumFacing f){return 0;}
  public WorldType getWorldType(){return WorldType.DEFAULT;}
  public boolean isSideSolid(BlockPos p,EnumFacing f,boolean fallback){return getBlockState(p).isOpaqueCube();}
 }
 static int[][] render(IBakedModel model,LightingWorld world,IBlockState extended,boolean forge,boolean smooth){
  BufferBuilder out=new BufferBuilder(4096);out.begin(7,DefaultVertexFormats.BLOCK);net.minecraft.client.renderer.color.BlockColors colors=new net.minecraft.client.renderer.color.BlockColors();
  if(forge){VertexLighterFlat lighter=smooth?new VertexLighterSmoothAo(colors):new VertexLighterFlat(colors);VertexBufferConsumer consumer=new VertexBufferConsumer(out);consumer.setOffset(world.origin);lighter.setParent(consumer);ForgeBlockModelRenderer.render(lighter,world,model,extended,world.origin,out,true,0);}
  else{BlockModelRenderer renderer=new BlockModelRenderer(colors);if(smooth)renderer.renderModelSmooth(world,model,extended,world.origin,out,true,0);else renderer.renderModelFlat(world,model,extended,world.origin,out,true,0);}
  out.finishDrawing();ByteBuffer bytes=out.getByteBuffer();int[][] result=new int[out.getVertexCount()][7];for(int v=0;v<result.length;v++)for(int k=0;k<7;k++)result[v][k]=bytes.getInt(v*28+k*4);return result;
 }
 static void lightingAndDamage(){
  Sprite sprite=new Sprite(),crack=new Sprite();
  Block[] owners={new BlockSandbags(Material.GROUND),new BlockBarrier(Material.WOOD),new BlockWoodStructure(Material.WOOD),new BlockRebar(),new RBMKDisplay()};
  com.hbm.config.ClientConfig.RENDER_REBAR_SIMPLE.value=false;
  for(Block owner:owners)for(int meta:new int[]{0,1,2,5})for(int mask:new int[]{0,21,42,63}){
   IBlockState state=owner.getStateFromMeta(meta);LightingWorld world=new LightingWorld(state,mask);IBlockState extended=owner.getExtendedState(state,world,world.origin);IBakedModel model=UniversalModelFactory.bake(owner,meta,false,sprite);
   int expected=model.getQuads(extended,null,0).size();for(EnumFacing face:EnumFacing.values())if(state.shouldSideBeRendered(world,world.origin,face))expected+=model.getQuads(extended,face,0).size();
   IBakedModel damage=net.minecraftforge.client.ForgeHooksClient.getDamageModel(model,crack,state,world,world.origin);
   int damageCount=damage.getQuads(extended,null,0).size();for(EnumFacing face:EnumFacing.values())if(state.shouldSideBeRendered(world,world.origin,face))damageCount+=damage.getQuads(extended,face,0).size();
   check(damageCount==expected,"actual Forge damage model keeps connected geometry/cull buckets");
   for(EnumFacing face:EnumFacing.values())for(BakedQuad quad:damage.getQuads(extended,face,0))check(quad.getSprite()==crack,"damage face material");for(BakedQuad quad:damage.getQuads(extended,null,0))check(quad.getSprite()==crack,"damage general material");
   for(boolean forge:new boolean[]{false,true})for(boolean smooth:new boolean[]{false,true}){
    int[][] original=render(model,world,extended,forge,smooth),broken=render(damage,world,extended,forge,smooth);
    check(original.length==expected*4&&broken.length==original.length,"native actual consumer face count with original culling policy");
    if(original.length>0)check(world.samples>0,"actual native world light sampled");
    for(int v=0;v<original.length;v++){
     for(int axis=0;axis<3;axis++){check(Float.isFinite(Float.intBitsToFloat(original[v][axis])),"finite native positions");check(original[v][axis]==broken[v][axis],"damage does not move original geometry");}
     check((original[v][3]>>>24)==255,"native opaque diffuse/AO colors");check(original[v][3]==broken[v][3]&&original[v][6]==broken[v][6],"damage retains native light and shade");
    }
   }
   if(owner instanceof BlockSandbags&&mask==63)check(expected==0,"fully opaque surround culls all sandbag boundary faces");
   if(!(owner instanceof BlockSandbags))check(expected==((ISBRHUniversal)owner).getRenderBoxes(extended,false).size()*6,"other original explicit all-face owners not occluded");
  }
  System.out.println("PASS actual Forge flat/smooth and vanilla flat/smooth consumers, neighbor culling, native world light, exact Forge crack geometry/material routing");
 }
 public static void main(String[] args)throws Exception {net.minecraft.init.Bootstrap.register();owners();tileLifecycle();geometry();registry(args[0]);lightingAndDamage();if(args.length>1)faceOracle(args[1]);System.out.println("PASS "+checks+" native universal owners");}
}

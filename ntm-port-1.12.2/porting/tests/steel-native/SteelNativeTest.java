import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;
import com.mojang.authlib.GameProfile;
import api.hbm.block.IToolable.ToolType;
import com.hbm.blocks.generic.BlockSteelDecoration;
import com.hbm.main.SteelDecorationModelHandler;
import com.hbm.render.block.*;
import com.hbm.render.block.nativeblock.*;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.entity.*;
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
import net.minecraftforge.common.property.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class SteelNativeTest {
 static long checks,vertices;static final BlockPos POS=new BlockPos(8,64,8);
 static final String[] NAMES={"steel_wall","steel_corner"};static BlockSteelDecoration[] blocks;
 static sun.misc.Unsafe unsafe;static SimpleReloadableResourceManager resources;
 static final Map<String,TextureAtlasSprite> sprites=new HashMap<>();
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void eq(float a,float b,String s){check(Float.floatToIntBits(a)==Float.floatToIntBits(b),s+": "+a+" != "+b);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static Object get(Object o,Class<?> c,String n)throws Exception{return field(c,n).get(o);}
 static void set(Object o,Class<?> c,String n,Object v)throws Exception{field(c,n).set(o,v);}
 interface Throwing {void run()throws Exception;}
 static void rejects(Throwing r,String s)throws Exception{boolean b=false;try{r.run();}catch(Exception e){b=true;}check(b,s);}
 static final class Sprite extends TextureAtlasSprite {Sprite(String n){super(n);setIconWidth(16);setIconHeight(16);initSprite(512,128,64,32,false);}}
 static TextureAtlasSprite sprite(String n){return sprites.computeIfAbsent(n,Sprite::new);}
 static TextureAtlasSprite sprite(){return sprite("hbm:blocks/steel_wall");}
 static void setup()throws Exception{
  Bootstrap.register();net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="Steel native proof";md.version="probe";net.minecraftforge.fml.common.DummyModContainer c=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(c);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(c);
  blocks=new BlockSteelDecoration[]{new BlockSteelDecoration(Material.IRON,false),new BlockSteelDecoration(Material.IRON,true)};
  for(int i=0;i<2;i++)ForgeRegistries.BLOCKS.register(blocks[i].setRegistryName("hbm","tile."+NAMES[i]));
  unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);resources=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);set(mc,net.minecraft.client.Minecraft.class,"resourceManager",resources);if(net.minecraft.launchwrapper.Launch.blackboard==null)net.minecraft.launchwrapper.Launch.blackboard=new HashMap<>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
 }
 static final class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();int flags=-1,writes;
  NativeWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "steel native proof";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return chunks.containsKey(key(x,z));}
  public boolean checkLight(BlockPos p){return true;}
  public boolean setBlockState(BlockPos p,IBlockState s,int f){flags=f;writes++;return super.setBlockState(p,s,f);}
 }
 static final class Player extends EntityPlayer {Player(World w){super(w,new GameProfile(new UUID(1,2),"steel-proof"));}public boolean isSpectator(){return false;}public boolean isCreative(){return false;}}
 static void deferredItems()throws Exception{
  rejects(()->SteelDecorationModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent()),"missing ItemBlock does not bind AIR");Map<Block,Item> map=(Map<Block,Item>)get(null,Item.class,"BLOCK_TO_ITEM");
  for(Block b:blocks){ItemBlock i=new ItemBlock(b);i.setHasSubtypes(true);i.setRegistryName(b.getRegistryName());ForgeRegistries.ITEMS.register(i);map.put(b,i);check(Item.getItemFromBlock(b)==i,"deferred exact ItemBlock identity");}
 }
 static void ownerBehavior()throws Exception{
  NativeWorld w=new NativeWorld();Player player=new Player(w);
  for(int i=0;i<2;i++){BlockSteelDecoration b=blocks[i];check(b.isCorner()==(i==1),"exact owner kind");
   for(int m=0;m<16;m++){IBlockState s=b.getStateFromMeta(m);check(b.getMetaFromState(s)==m,"metadata round trip");check(!s.isOpaqueCube()&&!s.isFullCube()&&!s.getBlock().hasTileEntity(s),"nonfull/nonopaque/non-tile");check(s.getRenderType()==EnumBlockRenderType.MODEL&&b.getRenderLayer()==BlockRenderLayer.SOLID,"native solid MODEL");check(b.getItemDropped(s,new Random(1),7)==Item.getItemFromBlock(b)&&b.damageDropped(s)==0,"original own item zero metadata drop");check(b.getSoundType(s,null,POS,null)==SoundType.STONE,"original default sound unchanged");for(EnumFacing f:EnumFacing.values()){check(b.shouldSideBeRendered(s,w,POS,f),"all-side visibility");check(b.getBlockFaceShape(w,s,POS,f)==BlockFaceShape.UNDEFINED,"nonfull face support");}
    w.setBlockState(POS,s,2);check(w.getTileEntity(POS)==null,"actual chunk no decoration tile");
    for(boolean sneak:new boolean[]{false,true})for(ToolType tool:ToolType.values()){w.setBlockState(POS,s,2);player.setSneaking(sneak);w.writes=0;boolean got=b.onScrew(w,player,8,64,8,2,.1F,.2F,.3F,tool);check(got==(tool==ToolType.SCREWDRIVER),"original tool predicate");int expected=tool==ToolType.SCREWDRIVER?BlockSteelDecoration.rotateMetadata(m,sneak):m;check(b.getMetaFromState(w.getBlockState(POS))==expected,"native screwdriver state write");check(w.writes==(expected!=m?1:0),"invalid meta/non-tool no write");if(expected!=m)check(w.flags==3,"screw notify flag three");}
    for(int rotation=-1;rotation<=4;rotation++)check(b.transformMeta(m,rotation)==INBTBlockTransformable.transformMetaDeco(m,rotation),"unchanged structure rotation helper");
   }
   for(float yaw:new float[]{-1080,-720,-360,-315,-225,-135,-45,-44.999F,0,44.999F,45,89,90,135,180,225,270,315,360,721,1080}){player.rotationYaw=yaw;w.setBlockState(POS,b.getStateFromMeta(0),2);w.writes=0;b.onBlockPlacedBy(w,POS,w.getBlockState(POS),player,new ItemStack(Item.getItemFromBlock(b)));int expected=new int[]{3,4,2,5}[MathHelper.floor(yaw*4F/360F+.5D)&3];check(b.getMetaFromState(w.getBlockState(POS))==expected&&w.flags==2&&w.writes==1,"native actual placement yaw "+yaw);}
  }
  for(int prior:new int[]{0,2,3,4,5}){BlockSteelDecoration b=new BlockSteelDecoration(Material.IRON,false);AxisAlignedBB expected=BlockSteelDecoration.wallBounds(prior);b.getBoundingBox(b.getStateFromMeta(prior),w,POS);
   for(int invalid:new int[]{0,1,6,7,8,9,10,11,12,13,14,15}){IBlockState s=b.getStateFromMeta(invalid),snapshot=b.getExtendedState(s,w,POS);check(b.getBoundingBox(s,w,POS).equals(expected)&&b.getCollisionBoundingBox(s,w,POS).equals(expected),"legacy invalid wall retains previous owner bounds");check(BlockSteelDecoration.modelBounds(snapshot).equals(expected),"render snapshot retains previous bounds");b.getBoundingBox(b.getStateFromMeta(2),w,POS);check(BlockSteelDecoration.modelBounds(snapshot).equals(expected),"snapshot immutable after owner changes");b.getBoundingBox(b.getStateFromMeta(prior),w,POS);if(prior==0){b=new BlockSteelDecoration(Material.IRON,false);}}
  }
  Random random=new Random(711);for(int i=0;i<2;i++)for(int m=2;m<=5;m++){BlockSteelDecoration b=blocks[i];IBlockState state=b.getStateFromMeta(m);List<AxisAlignedBB> expected=i==0?Collections.singletonList(BlockSteelDecoration.wallBounds(m)):BlockSteelDecoration.cornerBounds(m);check(state.getBoundingBox(w,POS).equals(i==0?expected.get(0):Block.FULL_BLOCK_AABB),"selection shape");for(int n=0;n<64;n++){AxisAlignedBB query=n==0?new AxisAlignedBB(7,63,7,10,66,10):new AxisAlignedBB(8+random.nextDouble(),64+random.nextDouble(),8+random.nextDouble(),8+random.nextDouble(),64+random.nextDouble(),8+random.nextDouble());List<AxisAlignedBB> got=new ArrayList<>(),want=new ArrayList<>();b.addCollisionBoxToList(state,w,POS,query,got,null,false);for(AxisAlignedBB box:expected){AxisAlignedBB translated=box.offset(POS);if(query.intersects(translated))want.add(translated);}check(got.equals(want),"actual native collision exact translated pieces/order/filter");}}
  for(int m:new int[]{0,1,6,15}){List<AxisAlignedBB> got=new ArrayList<>();blocks[1].addCollisionBoxToList(blocks[1].getStateFromMeta(m),w,POS,new AxisAlignedBB(7,63,7,10,66,10),got,null,false);check(got.isEmpty(),"invalid corner collision empty");}
  System.out.println("PASS complete actual owner state, native World/Chunk placement and screw updates, six tools, drop/no-tile, bounds history and collision filtering.");
 }
 static IBlockState extended(int i,int m){return blocks[i].getExtendedState(blocks[i].getStateFromMeta(m),null,POS);}
 static List<BakedQuad> quads(IBakedModel model,IBlockState s){for(EnumFacing side:EnumFacing.values())check(model.getQuads(s,side,0).isEmpty(),"no culled buckets");return model.getQuads(s,null,0);}
 static void geometry()throws Exception{
  for(int i=0;i<2;i++)for(int m=0;m<16;m++)for(boolean item:new boolean[]{false,true}){IBakedModel model=new SteelDecorationBakedModel(i==1,m,item,sprite());IBlockState state=extended(i,m);List<BakedQuad> q=quads(model,state);int size=item?(i==0?6:16):i==0?6:(m>=2&&m<=5?18:0);check(q.size()==size,"finite geometry count");check(model.isAmbientOcclusion()&&model.isGui3d()&&!model.isBuiltInRenderer(),"original standard AO/native item 3d");check(model.getParticleTexture()==sprite()&&model.getOverrides()==ItemOverrideList.NONE,"original material/no override");for(BakedQuad quad:q){check(quad.getFormat().equals(DefaultVertexFormats.ITEM)&&quad.shouldApplyDiffuseLighting()&&!quad.hasTintIndex(),"native normals/diffuse/no tint");for(int v=0;v<4;v++){vertices++;int[] data=quad.getVertexData();check(data[v*7+3]==-1,"native AO not pre-baked");int normal=data[v*7+6];check((byte)normal==quad.getFace().getXOffset()*127&&(byte)(normal>>8)==quad.getFace().getYOffset()*127&&(byte)(normal>>16)==quad.getFace().getZOffset()*127,"native original normal");for(int a=0;a<3;a++)check(Float.isFinite(Float.intBitsToFloat(data[v*7+a])),"finite vertex");}}
   try{q.add(null);throw new AssertionError("mutable quads");}catch(UnsupportedOperationException expected){checks++;}
  }
  IBlockState snapshot=blocks[0].getExtendedState(blocks[0].getStateFromMeta(2),null,POS);IBakedModel invalid=new SteelDecorationBakedModel(false,0,false,sprite());List<BakedQuad> before=quads(invalid,snapshot);blocks[0].getBoundingBox(blocks[0].getStateFromMeta(5),null,POS);List<BakedQuad> after=quads(invalid,snapshot);for(int j=0;j<6;j++)check(Arrays.equals(before.get(j).getVertexData(),after.get(j).getVertexData()),"invalid model uses frozen snapshot not live global bounds");rejects(()->invalid.getQuads(blocks[0].getDefaultState(),null,0),"missing bounds snapshot rejects");
 }

 static double[] bounds(AxisAlignedBB b){return new double[]{b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ};}
 static void originalGeometry(File jar)throws Exception {
  try(OriginalSteelOracle.LegacyFaces legacy=new OriginalSteelOracle.LegacyFaces(jar)){
   for(int i=0;i<2;i++)for(int m=0;m<16;m++)for(boolean item:new boolean[]{false,true})for(int prior:new int[]{0,2,3,4,5}){
    BlockSteelDecoration owner=new BlockSteelDecoration(Material.IRON,i==1);IBlockState state=owner.getStateFromMeta(m);double[] before=OriginalSteelOracle.selection(false,prior);owner.getBoundingBox(owner.getStateFromMeta(prior),null,POS);IBlockState snapshot=owner.getExtendedState(state,null,POS);OriginalSteelOracle.Layout original=OriginalSteelOracle.layout(i==1,m,item,before);IBakedModel model=new SteelDecorationBakedModel(i==1,m,item,sprite());List<BakedQuad> got=quads(model,snapshot);check(got.size()==original.faces.size(),"pristine original face count");boolean[] matched=new boolean[got.size()];
    for(OriginalSteelOracle.Face face:original.faces){double[][] want=legacy.face(face,sprite(),original,item);int found=-1,shiftFound=-1;for(int q=0;q<got.size()&&found<0;q++){BakedQuad actual=got.get(q);if(matched[q]||actual.getFace().getIndex()!=face.side)continue;int[] data=actual.getVertexData();for(int shift=0;shift<4;shift++){boolean same=true;for(int v=0;v<4;v++)for(int a=0;a<5;a++){float value=Float.intBitsToFloat(data[v*7+(a<3?a:a+1)]);if(Float.floatToIntBits(value)!=Float.floatToIntBits((float)want[(v+shift)%4][a]))same=false;}if(same){found=q;shiftFound=shift;break;}}}
     check(found>=0,"original RenderBlocks/NT exact geometry+UV i="+i+" m="+m+" item="+item+" prior="+prior+" face="+face.side+" bounds="+Arrays.toString(face.bounds));matched[found]=true;check(shiftFound==new int[]{0,2,1,0,1,3}[face.side],"canonical AO winding cyclic shift");vertices+=4;
    }
    check(Arrays.equals(bounds(owner.getBoundingBox(state,null,POS)),OriginalSteelOracle.selection(i==1,m,before)),"pristine original selection including previous history");
   }
  }
  NativeWorld world=new NativeWorld();Player player=new Player(world);Random random=new Random(774);
  for(int i=0;i<2;i++)for(int m=0;m<16;m++){BlockSteelDecoration owner=blocks[i];for(boolean sneak:new boolean[]{false,true})for(ToolType tool:ToolType.values()){world.setBlockState(POS,owner.getStateFromMeta(m),2);world.writes=0;player.setSneaking(sneak);boolean handled=owner.onScrew(world,player,8,64,8,0,0,0,0,tool);OriginalSteelOracle.ScrewResult want=OriginalSteelOracle.screw(i==1,m,sneak,tool==ToolType.SCREWDRIVER);check(handled==want.handled&&world.writes==want.world.writes&&owner.getMetaFromState(world.getBlockState(POS))==want.world.meta,"pristine screwdriver state/write/handled");if(world.writes>0)check(world.flags==want.world.flags,"pristine screw flags");}
   for(int n=0;n<64;n++){AxisAlignedBB query=n==0?new AxisAlignedBB(7,63,7,10,66,10):new AxisAlignedBB(8+random.nextDouble(),64+random.nextDouble(),8+random.nextDouble(),8+random.nextDouble(),64+random.nextDouble(),8+random.nextDouble());List<AxisAlignedBB> got=new ArrayList<>();AxisAlignedBB prior=owner.getBoundingBox(owner.getStateFromMeta(4),null,POS);owner.addCollisionBoxToList(owner.getStateFromMeta(m),world,POS,query,got,null,false);List<OriginalSteelOracle.AxisAlignedBB> want=OriginalSteelOracle.collision(i==1,m,8,64,8,new OriginalSteelOracle.AxisAlignedBB(query.minX,query.minY,query.minZ,query.maxX,query.maxY,query.maxZ),bounds(prior));check(got.size()==want.size(),"pristine collision count");for(int j=0;j<got.size();j++)check(Arrays.equals(bounds(got.get(j)),want.get(j).bounds()),"pristine collision exact piece ordering/filter");}
  }

  for(int i=0;i<2;i++)for(int m=0;m<16;m++)for(BlockPos pos:new BlockPos[]{new BlockPos(-17,-4,-8),new BlockPos(0,0,0),new BlockPos(31,240,-33)}){
   BlockSteelDecoration b=blocks[i];AxisAlignedBB previous=b.getBoundingBox(b.getStateFromMeta(3),null,pos);
   for(AxisAlignedBB query:new AxisAlignedBB[]{new AxisAlignedBB(-1,-1,-1,2,2,2),new AxisAlignedBB(1,0,0,2,1,1),new AxisAlignedBB(-1,0,0,0,1,1),new AxisAlignedBB(.25,.25,.25,.75,.75,.75),new AxisAlignedBB(.1,.1,.1,.9,.9,.9)}){
    AxisAlignedBB translated=query.offset(pos);List<AxisAlignedBB> got=new ArrayList<>();b.addCollisionBoxToList(b.getStateFromMeta(m),world,pos,translated,got,null,false);List<OriginalSteelOracle.AxisAlignedBB> want=OriginalSteelOracle.collision(i==1,m,pos.getX(),pos.getY(),pos.getZ(),new OriginalSteelOracle.AxisAlignedBB(translated.minX,translated.minY,translated.minZ,translated.maxX,translated.maxY,translated.maxZ),bounds(previous));check(got.size()==want.size(),"pristine nonzero/negative collision and face-only contact count");for(int j=0;j<got.size();j++)check(Arrays.equals(bounds(got.get(j)),want.get(j).bounds()),"pristine translated box topology");
   }
  }
  for(int i=0;i<2;i++)for(int n=-2160;n<=2160;n++){float yaw=n*.5F;player.rotationYaw=yaw;world.setBlockState(POS,blocks[i].getStateFromMeta(0),2);world.writes=0;blocks[i].onBlockPlacedBy(world,POS,world.getBlockState(POS),player,ItemStack.EMPTY);OriginalSteelOracle.World want=OriginalSteelOracle.placed(i==1,yaw);check(world.writes==want.writes&&world.flags==want.flags&&blocks[i].getMetaFromState(world.getBlockState(POS))==want.meta,"pristine placement exact yaw boundaries");}
  System.out.println("PASS independent pinned pristine callbacks plus actual original RenderBlocks bytecode and RenderBlocksNT N/E faces: all metadata/world/item x five owner-bound histories, exact XYZ/UV/winding, original collision, tool cycles and 8642 placement scenarios.");
 }
 static final class LightingWorld implements IBlockAccess {
  final IBlockState own;final boolean asymmetric;LightingWorld(IBlockState s,boolean a){own=s;asymmetric=a;}
  public IBlockState getBlockState(BlockPos p){if(p.equals(POS))return own;return asymmetric&&p.getX()<8&&p.getZ()>=8?Blocks.STONE.getDefaultState():Blocks.AIR.getDefaultState();}
  public int getCombinedLight(BlockPos p,int emission){if(!asymmetric)return 0x00f000f0;int x=p.getX()-8,y=p.getY()-64,z=p.getZ()-8;return Math.floorMod(11+3*x+5*y+7*z,16)*16<<16|Math.floorMod(7+5*x+3*y+z,16)*16;}
  public TileEntity getTileEntity(BlockPos p){return null;}public boolean isAirBlock(BlockPos p){return getBlockState(p).getBlock()==Blocks.AIR;}public net.minecraft.world.biome.Biome getBiome(BlockPos p){return Biomes.PLAINS;}public int getStrongPower(BlockPos p,EnumFacing f){return 0;}public WorldType getWorldType(){return WorldType.DEFAULT;}public boolean isSideSolid(BlockPos p,EnumFacing f,boolean fallback){return getBlockState(p).isOpaqueCube();}
 }
 static int[][] render(IBakedModel model,LightingWorld w,boolean forge,boolean smooth){BufferBuilder out=new BufferBuilder(8192);out.begin(7,DefaultVertexFormats.BLOCK);BlockColors colors=new BlockColors();boolean rendered;if(forge){VertexLighterFlat l=smooth?new VertexLighterSmoothAo(colors):new VertexLighterFlat(colors);VertexBufferConsumer sink=new VertexBufferConsumer(out);sink.setOffset(POS);l.setParent(sink);rendered=ForgeBlockModelRenderer.render(l,w,model,w.own,POS,out,true,0);}else{BlockModelRenderer r=new BlockModelRenderer(colors);rendered=smooth?r.renderModelSmooth(w,model,w.own,POS,out,true,0):r.renderModelFlat(w,model,w.own,POS,out,true,0);}out.finishDrawing();check(rendered==(out.getVertexCount()>0),"actual renderer return");int[][] result=new int[out.getVertexCount()][7];ByteBuffer b=out.getByteBuffer();for(int v=0;v<result.length;v++)for(int k=0;k<7;k++)result[v][k]=b.getInt(v*28+k*4);return result;}
 static void lighting()throws Exception{
  for(int i=0;i<2;i++)for(int m=2;m<=5;m++)for(boolean forge:new boolean[]{false,true}){IBlockState s=extended(i,m);IBakedModel model=new SteelDecorationBakedModel(i==1,m,false,sprite());int[][] flat=render(model,new LightingWorld(s,true),forge,false),smooth=render(model,new LightingWorld(s,true),forge,true);check(flat.length==(i==0?24:72)&&smooth.length==flat.length,"actual native world face count");boolean varies=false;Set<Integer> light=new HashSet<>();for(int v=0;v<smooth.length;v++){light.add(smooth[v][6]);varies|=flat[v][3]!=smooth[v][3]||flat[v][6]!=smooth[v][6];check(smooth[v][3]>>>24==255,"opaque AO output");for(int a=0;a<3;a++)check(Float.isFinite(Float.intBitsToFloat(smooth[v][a])),"finite native lit vertex");}check(varies&&light.size()>1,"nonflat actual native AO/light response");IBakedModel damage=net.minecraftforge.client.ForgeHooksClient.getDamageModel(model,sprite("minecraft:blocks/destroy_stage_3"),s,new LightingWorld(s,false),POS);List<BakedQuad> orig=quads(model,s),cracked=quads(damage,s);check(orig.size()==cracked.size(),"damage preserves face count");for(int j=0;j<orig.size();j++)check(Arrays.equals(new BakedQuadRetextured(orig.get(j),sprite("minecraft:blocks/destroy_stage_3")).getVertexData(),cracked.get(j).getVertexData()),"native damage correct geometry/UV remap");}
  System.out.println("PASS actual Forge/vanilla flat and smooth lighting, asymmetric AO response, all-face visibility and native damage retexturing.");
 }
 static ResourceLocation route(int i,int meta,boolean item){return new ResourceLocation("hbm","native_steel/tile."+NAMES[i]+"/"+meta+(item?"/item":"/world"));}
 static void reload(IResourcePack pack)throws Exception{resources.reloadResources(Collections.singletonList(pack));ModelLoaderRegistry.clearModelCache(resources);}
 static IBakedModel bake(ResourceLocation id)throws Exception{IModel m=ModelLoaderRegistry.getModel(id);check(m!=ModelLoaderRegistry.getMissingModel(),"actual loader route");check(m.getTextures().equals(Collections.singleton(new ResourceLocation("hbm","blocks/steel_wall"))),"actual texture dependency");return m.bake(m.getDefaultState(),DefaultVertexFormats.ITEM,t->sprite(t.toString()));}
 static void registry(IResourcePack pack)throws Exception{
  reload(pack);TextureMap atlas=new TextureMap("textures");ModelManager initial=new ModelManager(atlas);BlockModelShapes shapes=new BlockModelShapes(initial);new ModelLoader(resources,atlas,shapes);net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(SteelDecorationModelHandler.class);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ModelRegistryEvent());net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(SteelDecorationModelHandler.class);reload(pack);
  Map<?,?> custom=(Map<?,?>)get(null,ModelLoader.class,"customModels");check(custom.size()==32,"only exact two owners x16 registered");net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> registry=new net.minecraft.util.registry.RegistrySimple<>();Map<IBlockState,ModelResourceLocation> states=new LinkedHashMap<>();ModelLoader.onRegisterAllBlocks(shapes);
  for(int i=0;i<2;i++){Map<IBlockState,ModelResourceLocation> variants=shapes.getBlockStateMapper().getVariants(blocks[i]);check(variants.size()==16,"sixteen native listed states");ModelBlockDefinition json;try(Reader in=new InputStreamReader(pack.getInputStream(new ResourceLocation("hbm","blockstates/tile."+NAMES[i]+".json")),StandardCharsets.UTF_8)){json=ModelBlockDefinition.parseFromReader(in,blocks[i].getRegistryName());}
   for(int m=0;m<16;m++){IBlockState listed=blocks[i].getStateFromMeta(m);ModelResourceLocation mapped=variants.get(listed);check(mapped.getVariant().equals("metadata="+m)&&json.hasVariant(mapped.getVariant()),"native blockstate metadata route");Variant v=json.getVariant(mapped.getVariant()).getVariantList().get(0);check(v.getModelLocation().getPath().equals("block/"+route(i,m,false).getPath())&&v.getRotation()==ModelRotation.X0_Y0&&!v.isUvLock(),"world route no transform");IBakedModel world=bake(v.getModelLocation());registry.putObject(mapped,world);states.put(listed,mapped);check(quads(world,extended(i,m)).size()==(i==0?6:m>=2&&m<=5?18:0),"loader world geometry");Item item=Item.getItemFromBlock(blocks[i]);ModelResourceLocation id=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,m));check(id!=null&&id.getPath().equals(route(i,m,true).getPath())&&id.getVariant().equals("inventory"),"deferred exact item metadata route");IBakedModel inventory=bake(id);registry.putObject(id,inventory);check(quads(inventory,null).size()==(i==0?6:16),"loader inventory geometry");}
  }
  ResourceLocation png=new ResourceLocation("hbm","textures/blocks/steel_wall.png");check(pack.resourceExists(png),"exact-case original texture");try(InputStream in=pack.getInputStream(png)){check(ImageIO.read(in)!=null,"native texture PNG decodes");}
  ModelManager manager=(ModelManager)unsafe.allocateInstance(ModelManager.class);set(manager,ModelManager.class,"modelRegistry",registry);set(manager,ModelManager.class,"defaultModel",registry.getObject(states.values().iterator().next()));ItemModelMesher mesher=new ItemModelMesher(manager);ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();for(int i=0;i<2;i++)for(int m=0;m<16;m++){Item item=Item.getItemFromBlock(blocks[i]);ModelResourceLocation id=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,m));check(mesher.getItemModel(new ItemStack(item,1,m))==registry.getObject(id),"actual item model mesher identity");}BlockModelShapes actual=new BlockModelShapes(manager);ModelLoader.onRegisterAllBlocks(actual);actual.reloadModels();for(Map.Entry<IBlockState,ModelResourceLocation> e:states.entrySet())check(actual.getModelForState(e.getKey())==registry.getObject(e.getValue()),"native block model shapes identity");
  IBakedModel old=bake(route(0,2,false));IModel oldDescriptor=ModelLoaderRegistry.getModel(route(0,2,false));reload(pack);sprites.clear();IBakedModel fresh=bake(route(0,2,false));check(fresh.getParticleTexture()!=old.getParticleTexture()&&ModelLoaderRegistry.getModel(route(0,2,false))!=oldDescriptor,"actual reload renews model and atlas identity");check(quads(old,extended(0,2)).get(0).getSprite()==old.getParticleTexture(),"old baked model immutable");System.out.println("PASS 32 world +32 item native state/loader/mesher routes, original PNG, reload: "+pack.getPackName());
 }
 static void isolation()throws Exception{
  ForgeRegistries.BLOCKS.register(new BlockSteelDecoration(Material.IRON,false).setRegistryName("foreign","tile.steel_wall"));ForgeRegistries.BLOCKS.register(new BlockSteelDecoration(Material.IRON,true).setRegistryName("hbm","tile.unknown_steel"));ForgeRegistries.BLOCKS.register(new Block(Material.IRON){}.setRegistryName("foreign","tile.steel_corner"));SteelDecorationModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());check(((Map<?,?>)get(null,ModelLoader.class,"customModels")).size()==32,"foreign and unknown owners untouched");
  ICustomModelLoader loader=(ICustomModelLoader)get(null,SteelDecorationModelHandler.class,"LOADER");for(String prefix:new String[]{"","block/","models/","models/block/"})check(loader.accepts(new ResourceLocation("hbm",prefix+route(0,2,false).getPath())),"normalized native loader prefix");check(!loader.accepts(new ResourceLocation("foreign",route(0,2,false).getPath()))&&!loader.accepts(new ResourceLocation("hbm","other/model")),"loader bounded namespace");for(String bad:new String[]{"unknown/0/world","tile.steel_wall/16/world","tile.steel_wall/-1/world","tile.steel_wall/no/world","tile.steel_corner/0/other","tile.steel_corner/0/world/extra"}){check(!loader.accepts(new ResourceLocation("hbm","native_steel/"+bad)),"unknown model route not claimed");rejects(()->loader.loadModel(new ResourceLocation("hbm","native_steel/"+bad)),"invalid route rejected");}
 }
 static void wrongOwner(String mode)throws Exception {
  Bootstrap.register();unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);resources=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);set(mc,net.minecraft.client.Minecraft.class,"resourceManager",resources);if(net.minecraft.launchwrapper.Launch.blackboard==null)net.minecraft.launchwrapper.Launch.blackboard=new HashMap<>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);Block wrong=mode.equals("wrong-kind")?new BlockSteelDecoration(Material.IRON,true):new Block(Material.IRON){};
  ForgeRegistries.BLOCKS.register(wrong.setRegistryName("hbm","tile.steel_wall"));
  SteelDecorationModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());
  check(((Map<?,?>)get(null,ModelLoader.class,"customModels")).isEmpty(),"known identity with wrong kind/class is untouched");
  System.out.println("PASS isolated exact owner negative: "+mode);
 }
 public static void main(String[] args)throws Exception{if(args.length>3){wrongOwner(args[3]);return;}setup();deferredItems();ownerBehavior();geometry();originalGeometry(new File(args[2]));lighting();IResourcePack folder=new FolderResourcePack(new File(args[0]));registry(folder);try(FileResourcePack jar=new FileResourcePack(new File(args[1]))){registry(jar);}isolation();System.out.println("PASS steel native family: "+checks+" assertions, "+vertices+" direct native vertices. Explicit sole NTM collaborator: RecipesCommon.ComparableStack tool recipe-key class; complete production owner/interfaces/models/handler/helpers and actual native Minecraft/Forge. Controlled atlas/Minecraft holder/registry flush, not live game or full mod acceptance.");}
}

import java.nio.file.*;
import java.io.*;
import java.util.*;
import com.hbm.blocks.generic.*;
import com.hbm.blocks.BlockMulti;
import com.hbm.render.mesh.*;
import com.hbm.render.block.*;
import com.hbm.render.block.nativeblock.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.*;

public class NativeApplianceTest {
 static int checks;
 static void check(boolean condition,String message) { checks++;if(!condition)throw new AssertionError(message); }
 static class Sprite extends TextureAtlasSprite { Sprite(){super("test");initSprite(256,256,32,48,false);} }
 static void verify(ObjMesh mesh,int meta,boolean inventory,boolean computer,List<BakedQuad> actual) {
  List<float[]> vertices=new ArrayList<>();
  float[] rotations={(float)Math.PI*.5F,0,(float)Math.PI*1.5F,(float)Math.PI};
  float yaw=computer?(inventory?0:new float[]{(float)Math.PI,0,(float)Math.PI*1.5F,(float)Math.PI*.5F}[meta>>2])
      :inventory?(float)Math.PI*-.5F:rotations[meta&3];
  double scale=computer&&inventory?1.2D:1;
  double y=computer?(inventory?.6D:.5D):inventory?.25D:0;
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
 static class VariantProbe extends BlockMulti {
  VariantProbe(){super(Material.IRON);}
  public int getSubCount(){return 7;}
  public int getMetaFromState(IBlockState state){return 13;}
 }
 static class OverrideProbe extends VariantProbe { public int damageDropped(int meta){return meta+41;} }
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
 public static class EventChest extends net.minecraft.tileentity.TileEntityChest {
  int id,arg;
  public boolean receiveClientEvent(int id,int arg){this.id=id;this.arg=arg;return true;}
 }
 static void owners() {
  RecordingWorld world=new RecordingWorld();world.rand.setSeed(17L);
  net.minecraft.entity.passive.EntityPig placer=new net.minecraft.entity.passive.EntityPig(world);
  BlockDecoModel computer=new BlockDecoModel(Material.IRON,com.hbm.blocks.BlockEnums.DecoComputerEnum.class,true,false);
  computer.setBlockBoundsTo(.125F,0F,0F,.875F,.875F,.625F);
  BlockDecoContainer cabinet=new BlockDecoContainer(Material.IRON,com.hbm.blocks.BlockEnums.DecoCabinetEnum.class,true,false,EventChest.class);
  cabinet.setBlockBoundsTo(.1875F,0F,0F,.8125F,1F,.75F);
  for(BlockDecoModel block:new BlockDecoModel[]{computer,cabinet})for(int m=0;m<16;m++) {
   IBlockState state=block.getStateFromMeta(m);
   check(block.getMetaFromState(state)==m,"deco raw state");check(block.damageDropped(state)==(m&3),"deco low variant bits");
   AxisAlignedBB bb=state.getBoundingBox(world,BlockPos.ORIGIN);
   check(bb.maxY==(block==computer?.875:1),"owner bounds");
   check(state.getCollisionBoundingBox(world,BlockPos.ORIGIN).equals(bb),"native collision retains selection bounds");
   for(int rotation=0;rotation<4;rotation++)check((block.transformMeta(m,rotation)&3)==(m&3),"structure rotation preserves variant");
  }
  for(BlockMulti block:new BlockMulti[]{new BlockDecoToaster(Material.IRON),computer,cabinet})for(int variant=0;variant<block.getSubCount();variant++)for(int angle=-720;angle<=720;angle+=15) {
   placer.rotationYaw=angle;net.minecraft.item.ItemStack stack=new net.minecraft.item.ItemStack(net.minecraft.init.Items.STICK,1,variant);
   block.onBlockPlacedBy(world,BlockPos.ORIGIN,block.getDefaultState(),placer,stack);
   int yaw=((int)Math.floor(angle*4F/360F+.5D))&3;
   int direction=(yaw&1)!=1?yaw>>1:yaw==3?2:3;
   check(block.getMetaFromState(world.current)==(block instanceof BlockDecoToaster?variant*4+yaw:(direction<<2)|variant),"native placement original yaw bits");
   check(world.flags==2,"original placement notify flag");
  }
  check(cabinet.hasTileEntity(cabinet.getDefaultState()),"native ITileEntityProvider consumer");
  EventChest chest=(EventChest)cabinet.createTileEntity(world,cabinet.getDefaultState());world.tile=chest;world.current=cabinet.getDefaultState();
  check(cabinet.eventReceived(cabinet.getDefaultState(),world,BlockPos.ORIGIN,7,91)&&chest.id==7&&chest.arg==91,"cabinet tile event forwarding");
  net.minecraft.item.ItemStack stored=new net.minecraft.item.ItemStack(net.minecraft.init.Items.DIAMOND,57,0);
  stored.setTagCompound(new net.minecraft.nbt.NBTTagCompound());stored.getTagCompound().setString("test","kept");chest.setInventorySlotContents(0,stored);
  cabinet.breakBlock(world,BlockPos.ORIGIN,cabinet.getDefaultState());int total=0;
  for(net.minecraft.entity.item.EntityItem entity:world.drops){total+=entity.getItem().getCount();check(entity.getItem().getItem()==net.minecraft.init.Items.DIAMOND&&entity.getItem().getTagCompound().getString("test").equals("kept"),"cabinet drops retain item and NBT");}
  check(total==57&&stored.isEmpty(),"cabinet complete inventory depletion");
  check(world.comparators==27&&world.removals==1,"original per-slot comparator and superclass cleanup");
 }
 static void routes(String root,TextureAtlasSprite sprite)throws Exception {
  java.lang.reflect.Field field=com.hbm.main.DecoApplianceModelHandler.class.getDeclaredField("LOADER");field.setAccessible(true);
  net.minecraftforge.client.model.ICustomModelLoader loader=(net.minecraftforge.client.model.ICustomModelLoader)field.get(null);
  net.minecraft.client.resources.SimpleReloadableResourceManager resources=new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
  resources.reloadResourcePack(new net.minecraft.client.resources.FolderResourcePack(new File(root,"src/main/resources")));
  loader.onResourceManagerReload(resources);
  for(String owner:new String[]{"deco_toaster","deco_computer"})for(int meta=0;meta<16;meta++)for(boolean item:new boolean[]{false,true}) {
   net.minecraft.util.ResourceLocation route=new net.minecraft.util.ResourceLocation("hbm:native_appliance/tile."+owner+"/"+meta+(item?"/item":"/world"));
   check(loader.accepts(route),"native route accepted");
   net.minecraftforge.client.model.IModel model=loader.loadModel(route);
   String[] materials={"toaster_iron","toaster_steel","toaster_wood"};
   String expected=owner.equals("deco_computer")?"deco_computer":materials[item?meta%3:(meta%12)/4];
   check(model.getTextures().iterator().next().toString().equals("hbm:blocks/"+expected),"original material route");
   net.minecraft.client.renderer.block.model.IBakedModel baked=model.bake(net.minecraftforge.common.model.TRSRTransformation.identity(),net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK,r->sprite);
   check(baked.getParticleTexture()==sprite&&!baked.getQuads(null,null,0).isEmpty(),"loader to native bake consumer");
  }
  loader.onResourceManagerReload(resources);
  check(!loader.accepts(new net.minecraft.util.ResourceLocation("other:native_appliance/tile.deco_toaster/0/world")),"namespace isolation");
  try {loader.loadModel(new net.minecraft.util.ResourceLocation("hbm:native_appliance/tile.deco_toaster/16/world"));throw new AssertionError("bad metadata accepted");}catch(IllegalArgumentException expected){checks++;}
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();
  owners();
  VariantProbe base=new VariantProbe();OverrideProbe override=new OverrideProbe();
  check(base.damageDropped(base.getDefaultState())==6,"shared base rectify via native callback");
  check(override.damageDropped(override.getDefaultState())==54,"shared native callback dispatches subclass integer hook");
  BlockDecoToaster toaster=new BlockDecoToaster(Material.IRON);
  for(BlockMulti block:new BlockMulti[]{toaster}) for(int meta=0;meta<16;meta++) {
   IBlockState state=block.getStateFromMeta(meta);
   check(block.getMetaFromState(state)==meta,"state roundtrip");
   check(block.damageDropped(state)==(meta%(block.getSubCount()*4))/4,"drop variant");
   check(!block.isOpaqueCube(state)&&!block.isFullCube(state),"noncube");
   if(block==toaster) {
    AxisAlignedBB bounds=block.getBoundingBox(state,null,BlockPos.ORIGIN);
    check(bounds.minX==((meta&1)==0?.25F:.375F),"toaster x bounds");
    check(bounds.maxY==.325F,"float bound preserved");
   }
  }
  Sprite sprite=new Sprite();
  for(String name:new String[]{"toaster","puter"}) {
   ObjMesh mesh;
   try(Reader reader=Files.newBufferedReader(Paths.get(args[0],"src/main/resources/assets/hbm/models/blocks/"+name+".obj"))) {
    mesh=ObjMeshParser.parse(reader,name,false);
   }
   for(int meta=0;meta<16;meta++)for(boolean inventory:new boolean[]{false,true}) {
    List<BakedQuad> quads=name.equals("puter")?RenderBlockDecoModel.bake(mesh,meta,inventory,sprite):RenderToaster.bake(mesh,meta,inventory,sprite);
    check(!quads.isEmpty(),"shipped geometry exists");
    for(BakedQuad quad:quads) {
     check(quad.getSprite()==sprite,"atlas binding");

    }
    verify(mesh,meta,inventory,name.equals("puter"),quads);
    DecoApplianceBakedModel baked=new DecoApplianceBakedModel(mesh,name.equals("puter"),meta,inventory,sprite);
    check(baked.getQuads(null,net.minecraft.util.EnumFacing.UP,0).isEmpty(),"no cull faces");
    check(baked.getQuads(null,null,0).size()==quads.size(),"model consumer");
   }
  }
  routes(args[0],sprite);
  System.out.println("PASS "+checks+" native appliance owner/model checks");
 }
}

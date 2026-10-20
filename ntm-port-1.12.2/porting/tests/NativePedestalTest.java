import java.util.*;
import java.nio.file.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import com.hbm.blocks.generic.BlockPedestal;
import com.hbm.blocks.generic.BlockPedestal.TileEntityPedestal;
import com.hbm.inventory.recipes.PedestalRecipes;
import com.hbm.render.block.RenderPedestal;

public class NativePedestalTest {
 static int checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static class Sprite extends TextureAtlasSprite {Sprite(String n){super(n);setIconWidth(16);setIconHeight(16);initSprite(256,256,32,48,false);}}
 static class World extends net.minecraft.world.World {
  Map<BlockPos,TileEntity> tiles=new HashMap<>();List<net.minecraft.entity.Entity> drops=new ArrayList<>();int syncs,removed;boolean powered=true;
  IBlockState state;
  World(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new net.minecraft.world.WorldProviderSurface(),new net.minecraft.profiler.Profiler(),false);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return true;}
  public BlockPos getSpawnPoint(){return BlockPos.ORIGIN;}
  public IBlockState getBlockState(BlockPos p){return state;}
  public TileEntity getTileEntity(BlockPos p){return tiles.get(p);}
  public void markChunkDirty(BlockPos p,TileEntity te){}
  public void updateComparatorOutputLevel(BlockPos p,net.minecraft.block.Block b){}
  public void notifyBlockUpdate(BlockPos p,IBlockState a,IBlockState b,int flags){check(flags==3,"sync flags");syncs++;}
  public boolean isBlockPowered(BlockPos p){return powered;}
  public boolean spawnEntity(net.minecraft.entity.Entity e){drops.add(e);return true;}
  public void removeTileEntity(BlockPos p){tiles.remove(p);removed++;}
  public <T extends net.minecraft.entity.Entity> List<T> getEntitiesWithinAABB(Class<? extends T> c,net.minecraft.util.math.AxisAlignedBB bb){return new ArrayList<>();}
  TileEntityPedestal put(BlockPos p){TileEntityPedestal t=new TileEntityPedestal();t.setWorld(this);t.setPos(p);tiles.put(p,t);return t;}
 }
 static class Player extends EntityPlayer {
  Player(World w){super(w,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"probe"));}
  public boolean isSpectator(){return false;} public boolean isCreative(){return false;}
 }
 static void owners() {
  BlockPedestal b=new BlockPedestal();World w=new World();w.state=b.getDefaultState();BlockPos p=BlockPos.ORIGIN;TileEntityPedestal tile=w.put(p);Player player=new Player(w);
  check(b.getRenderType(w.state)==EnumBlockRenderType.MODEL&&!b.isFullCube(w.state)&&!b.isOpaqueCube(w.state),"native block flags");
  for(EnumFacing side:EnumFacing.values())check(b.shouldSideBeRendered(w.state,w,p,side),"all original faces visible");
  check(b.createNewTileEntity(w,0) instanceof TileEntityPedestal,"tile creation");
  for(EnumHand hand:EnumHand.values()){
   ItemStack held=new ItemStack(Items.DIAMOND,37);held.setTagCompound(new NBTTagCompound());held.getTagCompound().setString("kept","yes");player.setHeldItem(hand,held);
   check(b.onBlockActivated(w,p,w.state,player,hand,EnumFacing.UP,0,0,0),"deposit");check(player.getHeldItem(hand).isEmpty()&&tile.item.getCount()==37&&tile.item!=held,"whole stack ownership copy");
   check(b.onBlockActivated(w,p,w.state,player,hand,EnumFacing.UP,0,0,0),"withdraw");check(tile.item==null&&player.getHeldItem(hand).getCount()==37&&player.getHeldItem(hand).getTagCompound().getString("kept").equals("yes"),"withdraw NBT and clear");player.setHeldItem(hand,ItemStack.EMPTY);
  }
  player.setSneaking(true);check(!b.onBlockActivated(w,p,w.state,player,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0),"sneak pass through");player.setSneaking(false);
  tile.item=new ItemStack(Items.EMERALD,11);NBTTagCompound nbt=tile.writeToNBT(new NBTTagCompound());TileEntityPedestal loaded=new TileEntityPedestal();loaded.readFromNBT(nbt);check(loaded.item.getCount()==11,"tile NBT count");loaded.item=null;loaded.writeToNBT(nbt);check(!nbt.hasKey("item"),"stale NBT clear");loaded.readFromNBT(nbt);check(loaded.item==null,"native empty normalization");
  loaded.onDataPacket(null,tile.getUpdatePacket());check(loaded.item.getCount()==11,"native update packet");check(tile.getUpdateTag().hasKey("item"),"initial chunk sync");
  int[][] offsets={{-2,-2},{0,-3},{2,-2},{-3,0},{0,0},{3,0},{-2,2},{0,3},{2,2}};
  PedestalRecipes.PedestalRecipe recipe=new PedestalRecipes.PedestalRecipe();recipe.output=new ItemStack(Items.GOLD_INGOT,9);
  for(int i=0;i<9;i++){w.put(new BlockPos(offsets[i][0],0,offsets[i][1])).item=new ItemStack(Items.DIAMOND,i+1);recipe.input[i]=new PedestalRecipes.Ingredient(Items.DIAMOND,i+1);}PedestalRecipes.recipes.add(recipe);
  w.powered=false;b.neighborChanged(w.state,w,p,b,p);check(com.hbm.particle.helper.ExplosionSmallCreator.calls==0,"unpowered does not craft");w.powered=true;
  ((TileEntityPedestal)w.getTileEntity(new BlockPos(-2,0,-2))).item.setCount(2);b.neighborChanged(w.state,w,p,b,p);check(com.hbm.particle.helper.ExplosionSmallCreator.calls==0,"exact count mismatch does not craft");((TileEntityPedestal)w.getTileEntity(new BlockPos(-2,0,-2))).item.setCount(1);
  b.neighborChanged(w.state,w,p,b,p);check(((TileEntityPedestal)w.getTileEntity(p)).item.getItem()==Items.GOLD_INGOT,"recipe output original position ordering");
  for(int i=0;i<9;i++)if(i!=4)check(((TileEntityPedestal)w.getTileEntity(new BlockPos(offsets[i][0],0,offsets[i][1]))).item==null,"recipe input removal");
  check(com.hbm.particle.helper.ExplosionSmallCreator.calls==1,"one success effect");
  b.breakBlock(w,p,w.state);check(w.drops.size()==1&&w.removed==1,"drop and superclass tile removal");ItemStack drop=((net.minecraft.entity.item.EntityItem)w.drops.get(0)).getItem();check(drop.getItem()==Items.GOLD_INGOT&&drop.getCount()==9,"drop full output");
 }
 static void geometry(String original)throws Exception {
  String source=new String(Files.readAllBytes(Paths.get(original,"src/main/java/com/hbm/render/block/RenderPedestal.java")),"UTF-8");
  java.util.regex.Matcher m=java.util.regex.Pattern.compile("if\\(i == [012]\\) renderer.setRenderBounds\\(([^;]+)\\);").matcher(source);List<double[]> boxes=new ArrayList<>();while(m.find()){String[] s=m.group(1).split(",");double[] box=new double[6];for(int i=0;i<6;i++)box[i]=Double.parseDouble(s[i]);boxes.add(box);}check(boxes.size()==3,"independent original cuboid extraction");
  Sprite top=new Sprite("top"),side=new Sprite("side");
  for(boolean item:new boolean[]{false,true}){List<BakedQuad> qs=RenderPedestal.bake(top,side,item);check(qs.size()==18,"18 original faces");for(int q=0;q<18;q++){BakedQuad quad=qs.get(q);EnumFacing face=EnumFacing.byIndex(q%6);check(quad.getSprite()==(q%6<2?top:side),"original material");check(quad.getFace()==face,"original normal");double[] b=boxes.get(q/6);int[] data=quad.getVertexData();int stride=quad.getFormat().getIntegerSize(),uv=quad.getFormat().getUvOffsetById(0)/4;for(int v=0;v<4;v++){float x=Float.intBitsToFloat(data[v*stride]),y=Float.intBitsToFloat(data[v*stride+1]),z=Float.intBitsToFloat(data[v*stride+2]);check((x==b[0]||x==b[3])&&(y==b[1]||y==b[4])&&(z==b[2]||z==b[5]),"original box corners");double u=face==EnumFacing.NORTH?b[0]+b[3]-x:face==EnumFacing.EAST?b[2]+b[5]-z:face==EnumFacing.WEST?z:x;double vv=q%6<2?z:1-y;check(Math.abs(Float.intBitsToFloat(data[v*stride+uv])-top.getInterpolatedU(u*16))<1e-6,"original standard face U");check(Math.abs(Float.intBitsToFloat(data[v*stride+uv+1])-top.getInterpolatedV(vv*16))<1e-6,"original standard face V");}}}
 }
 static void routes()throws Exception {
  Class<?> cl=Class.forName("com.hbm.main.PedestalModelHandler$Loader");java.lang.reflect.Constructor<?> c=cl.getDeclaredConstructor();c.setAccessible(true);net.minecraftforge.client.model.ICustomModelLoader loader=(net.minecraftforge.client.model.ICustomModelLoader)c.newInstance();
  for(String s:new String[]{"world","item"}){ResourceLocation route=new ResourceLocation("hbm:native_pedestal/"+s);check(loader.accepts(route),"route");net.minecraftforge.client.model.IModel model=loader.loadModel(route);check(model.getTextures().size()==2,"atlas materials");IBakedModel baked=model.bake(net.minecraftforge.common.model.TRSRTransformation.identity(),net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK,r->new Sprite(r.toString()));check(baked.getQuads(null,null,0).size()==18,"loader to native bake");for(EnumFacing side:EnumFacing.values())check(baked.getQuads(null,side,0).isEmpty(),"no duplicate culled stream");check(baked.isAmbientOcclusion()&&!baked.isBuiltInRenderer(),"standard native light path");loader.onResourceManagerReload(null);}
  check(!loader.accepts(new ResourceLocation("wrong:native_pedestal/world")),"namespace isolation");
 }
 static void registry() throws Exception {
  net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  net.minecraftforge.fml.common.ModMetadata metadata=new net.minecraftforge.fml.common.ModMetadata();metadata.modId="hbm";metadata.name="Native pedestal probe";metadata.version="1";
  net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(metadata);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);

  java.lang.reflect.Field mapping=net.minecraft.item.Item.class.getDeclaredField("BLOCK_TO_ITEM");mapping.setAccessible(true);
  Map<net.minecraft.block.Block,net.minecraft.item.Item> items=(Map<net.minecraft.block.Block,net.minecraft.item.Item>)mapping.get(null);
  BlockPedestal block=new BlockPedestal();block.setRegistryName("hbm","tile.pedestal");net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(block);
  net.minecraft.item.ItemBlock item=new net.minecraft.item.ItemBlock(block);item.setRegistryName(block.getRegistryName());net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item);items.put(block,item);
  BlockPedestal foreign=new BlockPedestal();foreign.setRegistryName("other","tile.pedestal");net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(foreign);
  // Model registration uses the real client singleton/resource manager, without a game loop or GL context.
  java.io.File d=new java.io.File(".");
  net.minecraft.client.main.GameConfiguration config=new net.minecraft.client.main.GameConfiguration(
    new net.minecraft.client.main.GameConfiguration.UserInformation(new net.minecraft.util.Session("PedestalProbe","00000000-0000-0000-0000-000000000000","","legacy"),new com.mojang.authlib.properties.PropertyMap(),new com.mojang.authlib.properties.PropertyMap(),java.net.Proxy.NO_PROXY),
    new net.minecraft.client.main.GameConfiguration.DisplayInformation(1,1,false,false),new net.minecraft.client.main.GameConfiguration.FolderInformation(d,d,d,null),
    new net.minecraft.client.main.GameConfiguration.GameInformation(false,"1.12.2","native pedestal proof"),new net.minecraft.client.main.GameConfiguration.ServerInformation(null,0));
  net.minecraft.client.Minecraft minecraft=new net.minecraft.client.Minecraft(config);minecraft.gameSettings=new net.minecraft.client.settings.GameSettings();
  java.lang.reflect.Field rm=net.minecraft.client.Minecraft.class.getDeclaredField("resourceManager");rm.setAccessible(true);rm.set(minecraft,new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer()));

  net.minecraftforge.fml.common.eventhandler.EventBus bus=new net.minecraftforge.fml.common.eventhandler.EventBus();bus.register(com.hbm.main.PedestalModelHandler.class);bus.post(new net.minecraftforge.client.event.ModelRegistryEvent());
  java.lang.reflect.Field custom=net.minecraftforge.client.model.ModelLoader.class.getDeclaredField("customModels");custom.setAccessible(true);Map<?,?> models=(Map<?,?>)custom.get(null);check(models.size()==1,"actual registry item route");
  java.lang.reflect.Field sm=net.minecraftforge.client.model.ModelLoader.class.getDeclaredField("customStateMappers");sm.setAccessible(true);Map<?,?> states=(Map<?,?>)sm.get(null);check(states.containsKey(block.delegate)&&!states.containsKey(foreign.delegate),"actual state mapper exact owner isolation");
  net.minecraft.client.renderer.block.statemap.IStateMapper mapper=(net.minecraft.client.renderer.block.statemap.IStateMapper)states.get(block.delegate);check(mapper.putStateModelLocations(block).get(block.getDefaultState()).equals(new ModelResourceLocation("hbm:native_pedestal/world","normal")),"actual blockstate route");
 }
 public static void main(String[] args)throws Exception{net.minecraft.init.Bootstrap.register();net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(TileEntityPedestal.class,new ResourceLocation("hbm:pedestal_probe"));owners();geometry(args[0]);routes();registry();System.out.println("PASS native pedestal "+checks+" assertions; complete owner callbacks, tile NBT/network, recipe layout, drops, original geometry oracle, native model loader");}
}

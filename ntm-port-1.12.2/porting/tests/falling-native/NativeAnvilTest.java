import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import com.hbm.blocks.machine.NTMAnvil;
public class NativeAnvilTest {
 static long checks,vertices;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static final class Sprite extends TextureAtlasSprite {Sprite(String name,int i){super(name);setIconWidth(16);setIconHeight(16);initSprite(2048,128,16*i,32,false);}}
 static Map<String,TextureAtlasSprite> sprites=new LinkedHashMap<>();
 static TextureAtlasSprite sprite(ResourceLocation r){return sprites.computeIfAbsent(r.toString(),s->new Sprite(s,sprites.size()+1));}
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
  for(Part p:selected)for(Face f:p.faces){double[] n=rotate(f.normal,yaw,pitch);int color=OriginalAtlasOracle.color(n,!item,group==null);
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
 static class World extends net.minecraft.world.World {
  IBlockState state; int flags;
  World(boolean client){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new net.minecraft.world.WorldProviderSurface(),new net.minecraft.profiler.Profiler(),client);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return true;}
  public BlockPos getSpawnPoint(){return BlockPos.ORIGIN;}
  public IBlockState getBlockState(BlockPos p){return state;}
  public boolean setBlockState(BlockPos p,IBlockState s,int f){state=s;flags=f;return true;}
 }
 static class Player extends EntityPlayer {
  int opens;
  Player(World w){super(w,new com.mojang.authlib.GameProfile(UUID.randomUUID(),"probe"));}
  public boolean isSpectator(){return false;} public boolean isCreative(){return false;}
  public void openGui(Object mod,int id,net.minecraft.world.World w,int x,int y,int z){check(id==0,"original GUI id");opens++;}
 }
 static void blocks(){
  for(int tier:new int[]{1,2,3,4,5,6,7,8,1916169}){
   NTMAnvil b=new NTMAnvil(net.minecraft.block.material.Material.IRON,tier);World w=new World(false);Player p=new Player(w);w.state=b.getDefaultState();
   check(b.tier==tier,"tier retained");check(NTMAnvil.tierMap.get(tier).contains(b),"tier map");
   for(int meta=0;meta<16;meta++){
    IBlockState s=b.getStateFromMeta(meta);check(b.getMetaFromState(s)==meta,"state metadata roundtrip");
    check(!b.isOpaqueCube(s)&&!b.isFullCube(s)&&b.getRenderType(s)==EnumBlockRenderType.MODEL,"native shape flags");
    AxisAlignedBB box=b.getBoundingBox(s,w,BlockPos.ORIGIN);check(box.equals(b.getCollisionBoundingBox(s,w,BlockPos.ORIGIN)),"collision-selection equal");
    AxisAlignedBB want=meta==2||meta==3?new AxisAlignedBB(0,0,.25,1,.75,.75):meta==4||meta==5?new AxisAlignedBB(.25,0,0,.75,.75,1):net.minecraft.block.Block.FULL_BLOCK_AABB;
    check(box.equals(want),"exact original valid bounds / explicit invalid default");
   }
   for(int dir=0;dir<4;dir++){p.rotationYaw=dir*90;b.onBlockPlacedBy(w,BlockPos.ORIGIN,b.getDefaultState(),p,net.minecraft.item.ItemStack.EMPTY);check(b.getMetaFromState(w.state)==new int[]{3,4,2,5}[dir]&&w.flags==2,"placement exact original meta and flags");}
   check(((com.hbm.inventory.container.ContainerAnvil)b.provideContainer(0,p,w,0,0,0)).tier==tier,"real factory passes tier to explicit container fixture");
   check(((com.hbm.inventory.gui.GUIAnvil)b.provideGUI(0,p,w,0,0,0)).tier==tier,"real GUI factory passes tier to explicit GUI fixture");
   for(EnumHand hand:EnumHand.values()){check(b.onBlockActivated(w,BlockPos.ORIGIN,w.state,p,hand,EnumFacing.UP,0,0,0),"activate");}
   check(p.opens==2,"both hands native callback");p.setSneaking(true);check(!b.onBlockActivated(w,BlockPos.ORIGIN,w.state,p,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0)&&p.opens==2,"sneak pass through");
   World client=new World(true);check(b.onBlockActivated(client,BlockPos.ORIGIN,w.state,p,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0)&&p.opens==2,"client returns before server effects");
  }
 }
 static Object get(Object o,Class<?> c,String name)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 static void set(Object o,Class<?> c,String name,Object value)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static void models(String root,String skinsFile)throws Exception {
  net.minecraftforge.fml.common.FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="Anvil probe";md.version="1";
  net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);
  List<String> lines=java.nio.file.Files.readAllLines(java.nio.file.Paths.get(skinsFile));List<NTMAnvil> blocks=new ArrayList<>();
  Map<net.minecraft.block.Block,net.minecraft.item.Item> items=(Map<net.minecraft.block.Block,net.minecraft.item.Item>)get(null,net.minecraft.item.Item.class,"BLOCK_TO_ITEM");
  for(String line:lines){String skin=line.split(" ")[0];NTMAnvil b=new NTMAnvil(net.minecraft.block.material.Material.IRON,1);b.setRegistryName("hbm","tile.anvil_"+skin);net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(b);net.minecraft.item.ItemBlock item=new net.minecraft.item.ItemBlock(b);item.setRegistryName(b.getRegistryName());net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item);items.put(b,item);blocks.add(b);}
  List<NTMAnvil> excluded=new ArrayList<>();
  for(String id:new String[]{"other:tile.anvil_iron","hbm:tile.anvil_probe","hbm:unrelated_anvil"}){NTMAnvil b=new NTMAnvil(net.minecraft.block.material.Material.IRON,1);b.setRegistryName(new ResourceLocation(id));net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(b);excluded.add(b);}
  NTMAnvil subclass=new NTMAnvil(net.minecraft.block.material.Material.IRON,1){};subclass.setRegistryName("other","tile.anvil_desh");net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(subclass);excluded.add(subclass);
  sun.misc.Unsafe u=(sun.misc.Unsafe)get(null,sun.misc.Unsafe.class,"theUnsafe");net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)u.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);
  SimpleReloadableResourceManager rm=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());set(mc,net.minecraft.client.Minecraft.class,"resourceManager",rm);rm.reloadResources(Collections.singletonList(new FolderResourcePack(new File(root))));
  net.minecraftforge.fml.common.Mod.EventBusSubscriber annotation=com.hbm.main.AnvilModelHandler.class.getAnnotation(net.minecraftforge.fml.common.Mod.EventBusSubscriber.class);check(annotation.modid().equals("hbm")&&Arrays.equals(annotation.value(),new net.minecraftforge.fml.relauncher.Side[]{net.minecraftforge.fml.relauncher.Side.CLIENT}),"automatic client subscriber");
  net.minecraftforge.fml.common.eventhandler.EventBus bus=new net.minecraftforge.fml.common.eventhandler.EventBus();bus.register(com.hbm.main.AnvilModelHandler.class);bus.post(new net.minecraftforge.client.event.ModelRegistryEvent());
  Map<?,?> custom=(Map<?,?>)get(null,net.minecraftforge.client.model.ModelLoader.class,"customModels");check(custom.size()==192,"12 x16 registered native item routes");Map<?,?> mappings=(Map<?,?>)get(null,net.minecraftforge.client.model.ModelLoader.class,"customStateMappers");
  for(NTMAnvil b:excluded)check(!mappings.containsKey(b.delegate),"unknown namespace/skin/prefix/subclass left untouched without ItemBlock");
  Class<?> lc=Class.forName("com.hbm.main.AnvilModelHandler$Loader");Constructor<?> constructor=lc.getDeclaredConstructor();constructor.setAccessible(true);net.minecraftforge.client.model.ICustomModelLoader loader=(net.minecraftforge.client.model.ICustomModelLoader)constructor.newInstance();loader.onResourceManagerReload(rm);
  List<Part> original;try(Reader reader=new FileReader(new File(root,"assets/hbm/models/blocks/anvil.obj"))){original=originalObj(reader);}check(original.size()==6,"six original OBJ groups");
  int routes=0;for(int n=0;n<lines.size();n++){
   String[] fields=lines.get(n).split(" ");String skin=fields[0];ResourceLocation top=new ResourceLocation("hbm:blocks/"+fields[1]),side=new ResourceLocation("hbm:blocks/"+fields[2]);
   NTMAnvil block=blocks.get(n);net.minecraft.client.renderer.block.statemap.IStateMapper mapper=(net.minecraft.client.renderer.block.statemap.IStateMapper)mappings.get(block.delegate);Map<IBlockState,ModelResourceLocation> states=mapper.putStateModelLocations(block);check(states.size()==16,"all states mapped");
   for(int meta=0;meta<16;meta++)for(boolean item:new boolean[]{false,true}){
    ModelResourceLocation id=new ModelResourceLocation("hbm:native_anvil/"+skin+"/"+meta+(item?"/item":"/world"),item?"inventory":"normal");
    if(item)check(id.equals(custom.get(org.apache.commons.lang3.tuple.Pair.of(items.get(block).delegate,meta))),"exact item route");else check(id.equals(states.get(block.getStateFromMeta(meta))),"exact state route");
    check(loader.accepts(id),"accept registered route");net.minecraftforge.client.model.IModel model=loader.loadModel(id);check(new HashSet<>(model.getTextures()).equals(new HashSet<>(Arrays.asList(top,side))),"independent original texture mapping");
    IBakedModel baked=model.bake(model.getDefaultState(),item?DefaultVertexFormats.ITEM:DefaultVertexFormats.BLOCK,NativeAnvilTest::sprite);
    List<Vertex> expected=new ArrayList<>();float yaw=OriginalAtlasOracle.yaw(meta,item);
    for(String group:OriginalAtlasOracle.groups)expected.addAll(stream(original,group,sprite(group.equals("Top")?top:side),item,yaw,0,1,.5,0,.5));
    geometry(baked.getQuads(null,null,0),expected,item,skin+" "+meta+" "+item);check(baked.getParticleTexture()==sprite(side),"side particle texture");
    for(EnumFacing face:EnumFacing.values())check(baked.getQuads(null,face,0).isEmpty(),"unculled only");routes++;
   }
  }
  Object before=get(loader,lc,"mesh");loader.onResourceManagerReload(rm);check(get(loader,lc,"mesh")==null,"reload invalidates mesh");loader.loadModel(new ResourceLocation("hbm:native_anvil/iron/0/world"));check(get(loader,lc,"mesh")!=before,"reload reparses real OBJ");
  System.out.println("PASS "+routes+" registered world/item routes; 12 materials x16 metadata x2 contexts; original six-part ordered geometry");
 }
 public static void main(String[] args)throws Exception{net.minecraft.init.Bootstrap.register();blocks();models(args[0],args[1]);System.out.println("PASS native anvil: "+checks+" assertions; "+vertices+" complete native vertices; explicit 4 collaborators, no GUI/crafting closure or live visual claim");}
}

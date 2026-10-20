import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.nio.*;
import org.objectweb.asm.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.*;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.util.*;
import io.netty.buffer.Unpooled;
import com.hbm.blocks.BlockBase;
import com.hbm.render.icon.*;
import com.hbm.render.mesh.*;
import com.hbm.render.model.LegacyBaseBakedModel;
import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.entity.projectile.EntityRubble;
import com.hbm.entity.mob.EntityBlockSpider;

/** CPU execution of complete native render methods. Only GL entry points, engine
 * singleton getters and the original mesh upload boundaries are recorded. Native
 * BufferBuilder, model vertices and EntityDataManager packet encoding remain real. */
public class BlockEntityNativeTest {
 static int checks;public static IIcon oldIcon;static final List<String> calls=new ArrayList<>();
 static Minecraft minecraft;static CaptureTessellator tess;static IBakedModel selected;
 static RenderManager manager;static sun.misc.Unsafe unsafe;static float[] color={1,1,1,1};
 static final List<float[]> colors=new ArrayList<>();static final List<float[]> vertices=new ArrayList<>();
 static int depth;static boolean captureBlocks;static final Map<ModelRenderer,Integer> shapes=new IdentityHashMap<>();
 static void ck(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
 static void eq(Object a,Object b,String message){ck(Objects.equals(a,b),message+"\n expected="+a+"\n actual="+b);}
 public static Minecraft getMinecraft(){return minecraft;}public static Tessellator getInstance(){return tess;}
 static void call(String s){calls.add(s);}
 public static void bind(ResourceLocation l){call("texture:"+l);}
 public static void blockDraw(Block b,int meta,float brightness,boolean tint){call("block:"+Block.getIdFromBlock(b)+":"+meta+":"+brightness+":"+tint);}
 public static void pushMatrix(){depth++;call("push");}public static void popMatrix(){depth--;call("pop");}
 public static void glPushMatrix(){pushMatrix();}public static void glPopMatrix(){popMatrix();}
 public static void translate(float x,float y,float z){call("translate:"+x+","+y+","+z);}public static void translate(double x,double y,double z){call("translated:"+x+","+y+","+z);}
 public static void glTranslatef(float x,float y,float z){translate(x,y,z);}public static void glTranslated(double x,double y,double z){translate(x,y,z);}
 public static void rotate(float a,float x,float y,float z){call("rotate:"+a+","+x+","+y+","+z);}public static void glRotatef(float a,float x,float y,float z){rotate(a,x,y,z);}
 public static void scale(float x,float y,float z){call("scale:"+x+","+y+","+z);}public static void scale(double x,double y,double z){call("scaled:"+x+","+y+","+z);}
 public static void glScalef(float x,float y,float z){scale(x,y,z);}public static void glScaled(double x,double y,double z){scale(x,y,z);}
 public static void color(float r,float g,float b,float a){color=new float[]{r,g,b,a};call("color:"+r+","+g+","+b+","+a);}public static void glColor4f(float r,float g,float b,float a){color(r,g,b,a);}
 public static void glEnable(int m){call("enable:"+m);}public static void glDisable(int m){call("disable:"+m);}
 public static void enableTexture2D(){glEnable(3553);}public static void disableTexture2D(){glDisable(3553);}public static void enableLighting(){glEnable(2896);}public static void disableLighting(){glDisable(2896);}public static void enableBlend(){glEnable(3042);}public static void disableBlend(){glDisable(3042);}
 public static void blendFunc(int a,int b){call("blend:"+a+","+b);}public static void glBlendFunc(int a,int b){blendFunc(a,b);}
 public static void modelPart(ModelRenderer model,float scale){Integer i=shapes.get(model);if(i==null){i=shapes.size();shapes.put(model,i);}call("shape:"+i+":"+scale);}
 public static class CaptureTextures extends TextureManager {public CaptureTextures(){super(null);}public void bindTexture(ResourceLocation l){bind(l);}}
 public static class CaptureTessellator extends Tessellator {
  CaptureTessellator(){super(4096);}
  public void draw(){BufferBuilder b=getBuffer();b.finishDrawing();ck(b.getVertexFormat().equals(DefaultVertexFormats.POSITION_TEX_NORMAL),"no vertex color may overwrite original brightness/flash alpha");ByteBuffer data=b.getByteBuffer();int stride=b.getVertexFormat().getSize();for(int i=0;i<b.getVertexCount();i++){float[] v=new float[8];for(int n=0;n<5;n++)v[n]=data.getFloat(i*stride+n*4);for(int n=0;n<3;n++)v[5+n]=data.get(i*stride+20+n)/127F;vertices.add(v);colors.add(color.clone());}b.reset();call("draw");}
 }
 public static class Mesher extends ItemModelMesher {Mesher(){super(new ModelManager(null));}public IBakedModel getItemModel(ItemStack stack){call("select:"+stack.getMetadata());return selected;}}
 public static class ItemRenderer extends RenderItem {public ItemRenderer(){super(null,null,null);}public ItemModelMesher getItemModelMesher(){return mesher;}}
 static Mesher mesher;
 public static class Dispatcher extends BlockRendererDispatcher {public Dispatcher(){super(null,null);}public IBakedModel getModelForState(IBlockState state){return selected;}}
 public static class Mesh implements MeshModel {public ObjMesh getMesh(){return ObjMesh.empty();}public List<String> getPartNames(){return Collections.emptyList();}public void renderAll(){call("all");}public void renderPart(String name){call("part:"+name);}public void renderOnly(String... n){for(String s:n)renderPart(s);}public void renderAllExcept(String... n){throw new AssertionError();}}
 static class Sprite extends TextureAtlasSprite {Sprite(String n){super(n);setIconWidth(16);setIconHeight(16);initSprite(64,64,16,32,false);}}
 public static class IconBlock extends BlockBase {
  final IIcon icon;IconBlock(IIcon i){icon=i;setRegistryName("hbm:test_block_entity");}
  public IIcon getIcon(int s,int meta){return icon;}
 }
 static class FixedModel implements IBakedModel {
  final List<BakedQuad> general;final Map<EnumFacing,List<BakedQuad>> sided=new EnumMap<>(EnumFacing.class);boolean builtin;
  FixedModel(List<BakedQuad> q){general=q;}public List<BakedQuad> getQuads(IBlockState s,EnumFacing face,long seed){return face==null?general:sided.containsKey(face)?sided.get(face):Collections.emptyList();}
  public boolean isAmbientOcclusion(){return false;}public boolean isGui3d(){return true;}public boolean isBuiltInRenderer(){return builtin;}public TextureAtlasSprite getParticleTexture(){throw new AssertionError("particle fallback forbidden");}public ItemCameraTransforms getItemCameraTransforms(){return ItemCameraTransforms.DEFAULT;}public ItemOverrideList getOverrides(){return ItemOverrideList.NONE;}
 }
 static IIcon icon(TextureAtlasSprite sprite)throws Exception{Constructor<LegacyTextureAtlasSprite> c=LegacyTextureAtlasSprite.class.getDeclaredConstructor(TextureAtlasSprite.class);c.setAccessible(true);return c.newInstance(sprite);}
 static void registerItem(Block block,Item item)throws Exception{Field f=Item.class.getDeclaredField("BLOCK_TO_ITEM");f.setAccessible(true);((Map<Block,Item>)f.get(null)).put(block,item);}
 static void set(Object owner,String name,Object value)throws Exception{Field f=owner.getClass()==Class.class?((Class<?>)owner).getDeclaredField(name):owner.getClass().getDeclaredField(name);f.setAccessible(true);f.set(owner instanceof Class?null:owner,value);}
 static final Set<String> transformed=new HashSet<>(Arrays.asList("OriginalTNT","OriginalRubble","OriginalSpider","com.hbm.render.entity.item.RenderTNTPrimedBase","com.hbm.render.entity.projectile.RenderRubble","com.hbm.render.entity.mob.RenderBlockSpider","com.hbm.render.model.ModelBlockSpider","com.hbm.render.model.ModelRubble","com.hbm.render.entity.NativeBlockEntityDraw","com.hbm.render.util.NativeBlockMaterial"));
 static class CpuLoader extends ClassLoader {
  CpuLoader(){super(BlockEntityNativeTest.class.getClassLoader());}
  protected Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException{if(!transformed.contains(name))return super.loadClass(name,resolve);synchronized(getClassLoadingLock(name)){Class<?> c=findLoadedClass(name);if(c==null)c=make(name);if(resolve)resolveClass(c);return c;}}
  Class<?> make(String name)throws ClassNotFoundException{try(InputStream in=getParent().getResourceAsStream(name.replace('.','/')+".class")){ClassReader r=new ClassReader(in);ClassWriter w=new ClassWriter(0);r.accept(new ClassVisitor(Opcodes.ASM5,w){public MethodVisitor visitMethod(int access,String n,String d,String signature,String[] exceptions){return new MethodVisitor(Opcodes.ASM5,super.visitMethod(access,n,d,signature,exceptions)){public void visitMethodInsn(int op,String owner,String method,String desc,boolean itf){
   if(op==Opcodes.INVOKESTATIC&&(owner.equals("org/lwjgl/opengl/GL11")||owner.equals("net/minecraft/client/renderer/GlStateManager")||(owner.equals("net/minecraft/client/Minecraft")&&method.equals("getMinecraft"))||(owner.equals("net/minecraft/client/renderer/Tessellator")&&method.equals("getInstance"))))owner="BlockEntityNativeTest";
   if(captureBlocks&&op==Opcodes.INVOKESTATIC&&owner.equals("com/hbm/render/entity/NativeBlockEntityDraw")&&method.equals("render")){owner="BlockEntityNativeTest";method="blockDraw";}
   if(op==Opcodes.INVOKEVIRTUAL&&owner.equals("net/minecraft/client/model/ModelRenderer")&&method.equals("render")&&desc.equals("(F)V")){op=Opcodes.INVOKESTATIC;owner="BlockEntityNativeTest";method="modelPart";desc="(Lnet/minecraft/client/model/ModelRenderer;F)V";}
   super.visitMethodInsn(op,owner,method,desc,itf);
  }};}},0);byte[] bytes=w.toByteArray();return defineClass(name,bytes,0,bytes.length);}catch(IOException e){throw new ClassNotFoundException(name,e);}}
 }
 static void reset(){calls.clear();shapes.clear();vertices.clear();colors.clear();depth=0;}
 static List<String> trace(Object target,String name,Class<?>[] types,Object... args)throws Exception{reset();target.getClass().getMethod(name,types).invoke(target,args);ck(depth==0,"balanced complete renderer matrix");return new ArrayList<>(calls);}
 static void setup()throws Exception {
  net.minecraft.init.Bootstrap.register();Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=(sun.misc.Unsafe)f.get(null);
  minecraft=(Minecraft)unsafe.allocateInstance(Minecraft.class);minecraft.renderEngine=new CaptureTextures();tess=new CaptureTessellator();manager=(RenderManager)unsafe.allocateInstance(RenderManager.class);manager.renderEngine=minecraft.renderEngine;
  mesher=new Mesher();set(minecraft,"renderItem",unsafe.allocateInstance(ItemRenderer.class));set(minecraft,"blockColors",new BlockColors());set(minecraft,"blockRenderDispatcher",unsafe.allocateInstance(Dispatcher.class));com.hbm.main.ResourceManager.spider=new Mesh();
  oldIcon=icon(new Sprite("minecraft:stone"));
 }
 static void renderTraces()throws Exception {
  captureBlocks=true;CpuLoader loader=new CpuLoader();EntityTNTPrimedBase tnt=new EntityTNTPrimedBase(null);tnt.setBlock(Blocks.STONE);tnt.rotationYaw=37;tnt.rotationPitch=-13;
  Object original=loader.loadClass("OriginalTNT").newInstance(),nativeRender=loader.loadClass("com.hbm.render.entity.item.RenderTNTPrimedBase").getConstructor(RenderManager.class).newInstance(manager);
  Class<?>[] args={EntityTNTPrimedBase.class,double.class,double.class,double.class,float.class,float.class};
  for(int fuse:new int[]{-10,-1,0,1,4,5,9,10,11,79,80,100,150})for(float partial:new float[]{0,.125F,.999F})for(float brightness:new float[]{0,.375F,1}){tnt.fuse=fuse;tnt.testBrightness=brightness;eq(trace(original,"doRender",args,tnt,1.25,-7.,3.5,2F,partial),trace(nativeRender,"doRender",args,tnt,1.25,-7.,3.5,2F,partial),"full original/native TNT swelling, transforms, flash blend/tint/alpha/brightness");}
  ck(net.minecraft.client.renderer.entity.RenderLiving.class.isAssignableFrom(Class.forName("com.hbm.render.entity.mob.RenderBlockSpider",false,loader)),"complete spider renderer compiles against actual native RenderLiving; live GL constructor not executed");
  EntityBlockSpider spider=new EntityBlockSpider(null);Object oldSpider=loader.loadClass("OriginalSpider").newInstance(),newSpider=loader.loadClass("com.hbm.render.model.ModelBlockSpider").newInstance();Class<?>[] modelArgs={Entity.class,float.class,float.class,float.class,float.class,float.class,float.class};
  for(float swing:new float[]{0,.1F,2F,5.5F,-3})for(float amount:new float[]{0,.4F,1}){eq(trace(oldSpider,"render",modelArgs,spider,swing,amount,0F,9F,4F,.0625F),trace(newSpider,"render",modelArgs,spider,swing,amount,0F,9F,4F,.0625F),"original spider alternating legs, translations, carried block brightness");}
  EntityRubble rubble=new EntityRubble(null);rubble.setMetaBasedOnBlock(Blocks.STONE,0);Sprite stone=new Sprite("minecraft:blocks/stone");FixedModel material=new FixedModel(Collections.emptyList());material.sided.put(EnumFacing.DOWN,Collections.singletonList(quad(stone,EnumFacing.DOWN)));selected=material;
  Object oldRubble=loader.loadClass("OriginalRubble").newInstance(),newRubble=loader.loadClass("com.hbm.render.entity.projectile.RenderRubble").getConstructor(RenderManager.class).newInstance(manager);Class<?>[] oldArgs={Entity.class,double.class,double.class,double.class,float.class,float.class},newArgs={EntityRubble.class,double.class,double.class,double.class,float.class,float.class};
  for(int tick:new int[]{0,1,35,359,360,711})for(float partial:new float[]{0,.5F,.999F}){rubble.ticksExisted=tick;eq(trace(oldRubble,"doRender",oldArgs,rubble,1.,-2.,3.,0F,partial),trace(newRubble,"doRender",newArgs,rubble,1.,-2.,3.,0F,partial),"rubble bottom texture, spin and all ten native model pieces");}
 }
 static BakedQuad quad(TextureAtlasSprite sprite,EnumFacing face){return new FaceBakery().makeBakedQuad(new org.lwjgl.util.vector.Vector3f(0,0,0),new org.lwjgl.util.vector.Vector3f(16,16,16),new BlockPartFace(face,0,sprite.getIconName(),new BlockFaceUV(new float[]{0,0,16,16},0)),sprite,face,ModelRotation.X0_Y0,null,true,true);}
 static void helperVertices()throws Exception {
  captureBlocks=false;CpuLoader loader=new CpuLoader();Class<?> helper=loader.loadClass("com.hbm.render.entity.NativeBlockEntityDraw");Method render=helper.getMethod("render",Block.class,int.class,float.class,boolean.class);
  reset();render.invoke(null,Blocks.AIR,0,1F,true);ck(calls.isEmpty(),"default synchronized block ID zero retains original invisible air branch");
  Sprite sprite=new Sprite("hbm:blocks/test");IconBlock block=new IconBlock(icon(sprite));Item item=new ItemBlock(block);registerItem(block,item);selected=LegacyBaseBakedModel.forBlockItem(block,item,0);
  for(float b:new float[]{0,.25F,.625F,1}){reset();render.invoke(null,block,0,b,true);ck(vertices.size()==24,"legacy item model dual side/general views are emitted once, six faces");for(float[] c:colors)ck(Arrays.equals(c,new float[]{b,b,b,1}),"all cube faces use original unshaded inventory brightness");for(float[] v:vertices){ck(v[0]==0||v[0]==1,"native X cube bound");ck(v[1]==0||v[1]==1,"native Y cube bound");ck(v[2]==0||v[2]==1,"native Z cube bound");ck(v[3]>=sprite.getMinU()&&v[3]<=sprite.getMaxU()&&v[4]>=sprite.getMinV()&&v[4]<=sprite.getMaxV(),"actual native atlas UVs");}for(int face=0;face<6;face++){List<float[]> oracle=OriginalCubeFaces.face(face,sprite);boolean match=false;for(int shift=0;shift<4;shift++){boolean same=true;for(int v=0;v<4;v++)for(int c=0;c<5;c++)same &= Math.abs(vertices.get(face*4+(v+shift)%4)[c]-oracle.get(v)[c])<0.00001;match|=same;}ck(match,"actual original cube face positions/UV/winding side="+face+" vertices="+Arrays.deepToString(vertices.subList(face*4,face*4+4).toArray())+" old="+Arrays.deepToString(oracle.toArray()));}eq(calls.get(1),"rotate:90.0,0.0,1.0,0.0","original ordinary branch quarter-turn");}
  for(float alpha:new float[]{0,.2F,.792F,1}){reset();color(1,1,1,alpha);render.invoke(null,block,0,1F,false);for(float[] c:colors)ck(Arrays.equals(c,new float[]{1,1,1,alpha}),"native vertices preserve flash alpha instead of writing opaque ITEM color");}
  Class<?> material=loader.loadClass("com.hbm.render.util.NativeBlockMaterial");Method face=material.getMethod("faceSprite",IBakedModel.class,IBlockState.class,EnumFacing.class);FixedModel m=new FixedModel(new ArrayList<>());
  ck(face.invoke(null,m,Blocks.STONE.getDefaultState(),EnumFacing.DOWN)==null,"no downward material is explicit unresolved gate");m.general.add(quad(sprite,EnumFacing.DOWN));eq("hbm:blocks/test",face.invoke(null,m,null,EnumFacing.DOWN),"unculled native DOWN material");m.sided.put(EnumFacing.DOWN,Collections.singletonList(quad(sprite,EnumFacing.DOWN)));eq("hbm:blocks/test",face.invoke(null,m,null,EnumFacing.DOWN),"duplicate equal material remains unique");m.general.add(quad(new Sprite("hbm:blocks/other"),EnumFacing.DOWN));ck(face.invoke(null,m,null,EnumFacing.DOWN)==null,"two downward materials cannot invent a unique sprite");m.builtin=true;ck(face.invoke(null,m,null,EnumFacing.DOWN)==null,"built-in model is not a particle fallback");
  BakedQuad down=quad(sprite,EnumFacing.DOWN);BakedQuad lightSamplingFace=new BakedQuad(down.getVertexData(),down.getTintIndex(),EnumFacing.WEST,down.getSprite(),false,down.getFormat());eq("hbm:blocks/test",face.invoke(null,new FixedModel(Collections.singletonList(lightSamplingFace)),null,EnumFacing.DOWN),"geometry selects actual bottom despite native block-position light face hint");
  ModelBlock cross;try(Reader reader=new InputStreamReader(BlockEntityNativeTest.class.getClassLoader().getResourceAsStream("assets/minecraft/models/block/cross.json"),"UTF-8")){cross=ModelBlock.deserialize(reader);}List<BakedQuad> crossQuads=new ArrayList<>();for(BlockPart part:cross.getElements())for(Map.Entry<EnumFacing,BlockPartFace> e:part.mapFaces.entrySet())crossQuads.add(new FaceBakery().makeBakedQuad(part.positionFrom,part.positionTo,e.getValue(),sprite,e.getKey(),ModelRotation.X0_Y0,part.partRotation,false,part.shade));ck(!crossQuads.isEmpty()&&face.invoke(null,new FixedModel(crossQuads),Blocks.YELLOW_FLOWER.getDefaultState(),EnumFacing.DOWN)==null,"actual native vanilla flower cross has no DOWN material");
  selected=new FixedModel(crossQuads);Method texture=material.getMethod("texture",Block.class,int.class);eq(new ResourceLocation("hbm:textures/blocks/test.png"),texture.invoke(null,Blocks.YELLOW_FLOWER,0),"exact vanilla side-independent yellow flower resolves its one actual cross material");eq(new ResourceLocation("hbm:textures/blocks/test.png"),texture.invoke(null,Blocks.RED_FLOWER,8),"exact vanilla red-flower subtype routes single actual material");
  boolean unresolved=false;try{texture.invoke(null,Blocks.REEDS,0);}catch(InvocationTargetException expected){unresolved=expected.getCause() instanceof IllegalArgumentException;}ck(unresolved,"non-flower cross-model owner remains explicit gate, not generic all-bush fallback");
  Method path=material.getMethod("texturePath",String.class,boolean.class);eq(new ResourceLocation("minecraft:textures/blocks/stone.png"),path.invoke(null,"minecraft:blocks/stone",true),"native folder once");eq(new ResourceLocation("hbm:textures/blocks/rbmk/debris.png"),path.invoke(null,"hbm:rbmk/debris",false),"legacy nested block folder once");eq(new ResourceLocation("minecraft:textures/blocks/stone.png"),path.invoke(null,"",false),"original empty-name stone fallback");
 }
 static void customVertices(String originalRoot)throws Exception {
  captureBlocks=false;CpuLoader loader=new CpuLoader();Class<?> helper=loader.loadClass("com.hbm.render.entity.NativeBlockEntityDraw");Method render=helper.getMethod("render",Block.class,int.class,float.class,boolean.class);
  Sprite sprite=new Sprite("hbm:blocks/red_barrel");IconBlock block=new IconBlock(icon(sprite));registerItem(block,new ItemBlock(block));ObjMesh mesh;
  try(Reader reader=new InputStreamReader(new FileInputStream(originalRoot+"/src/main/resources/assets/hbm/models/blocks/barrel.obj"),"UTF-8")){mesh=ObjMeshParser.parse(reader,"barrel",false);}
  selected=new FixedModel(com.hbm.render.block.RenderBarrel.bake(mesh,true,sprite));reset();render.invoke(null,block,0,.375F,true);ck(!calls.contains("rotate:90.0,0.0,1.0,0.0"),"native custom barrel retains original rotation, not the ordinary cube quarter-turn");
  List<float[]> expected=new ArrayList<>();for(ObjMesh.Part part:mesh.parts)for(ObjMesh.Face face:part.faces)for(int i=0;i<face.corners.size();i++){ObjMesh.Corner c=face.corners.get(i);float[] v={c.position.x+.5F,c.position.y,c.position.z+.5F,sprite.getInterpolatedU(c.uv.x*16F),sprite.getInterpolatedV(c.uv.y*16F),((byte)(face.normal.x*127F))/127F,((byte)(face.normal.y*127F))/127F,((byte)(face.normal.z*127F))/127F};expected.add(v);if(i%3==2)expected.add(v);}
  ck(vertices.size()==expected.size()/4*4,"original whole-OBJ stream duplication/trailing primitive contract");for(int v=0;v<vertices.size();v++)for(int n=0;n<8;n++)ck(Math.abs(vertices.get(v)[n]-expected.get(v)[n])<.00001,"original barrel vertex/UV/normal survives complete native draw "+v+":"+n);for(float[] c:colors)ck(Arrays.equals(c,new float[]{.375F,.375F,.375F,1}),"custom mesh entity brightness retained");
  reset();color(1,1,1,.42F);render.invoke(null,block,0,1F,false);for(float[] c:colors)ck(Arrays.equals(c,new float[]{1,1,1,.42F}),"custom mesh flash retains fractional alpha");
 }
 static byte[] wire(Entity e)throws Exception {SPacketEntityMetadata p=new SPacketEntityMetadata(123,e.getDataManager(),true);PacketBuffer out=new PacketBuffer(Unpooled.buffer());p.writePacketData(out);byte[] bytes=new byte[out.readableBytes()];out.readBytes(bytes);out.release();return bytes;}
 static void transfer(Entity server,Entity client)throws Exception {if(client.world==null)client.world=(net.minecraft.world.WorldServer)unsafe.allocateInstance(net.minecraft.world.WorldServer.class);byte[] bytes=wire(server);SPacketEntityMetadata copy=new SPacketEntityMetadata();PacketBuffer in=new PacketBuffer(Unpooled.wrappedBuffer(bytes));copy.readPacketData(in);ck(in.readableBytes()==0&&copy.getEntityId()==123,"actual complete native metadata packet decodes");client.getDataManager().setEntryValues(copy.getDataManagerEntries());in.release();}
 static void legacyWire(String jar)throws Exception {
  try(java.net.URLClassLoader old=new java.net.URLClassLoader(new java.net.URL[]{new File(jar).toURI().toURL()},BlockEntityNativeTest.class.getClassLoader())){
   Class<?> watcher=Class.forName("te",true,old),entity=Class.forName("sa",true,old),buffer=Class.forName("et",true,old);Constructor<?> ctor=watcher.getConstructor(entity),bc=buffer.getConstructor(io.netty.buffer.ByteBuf.class);
   for(int[] slots:new int[][]{{12},{16,17},{12,13}})for(int value:new int[]{0,1,255,-1,4095}){
    Object data=ctor.newInstance(new Object[]{null});for(int id:slots)watcher.getMethod("a",int.class,Object.class).invoke(data,id,value);
    io.netty.buffer.ByteBuf bytes=Unpooled.buffer();watcher.getMethod("a",buffer).invoke(data,bc.newInstance(bytes));List<?> decoded=(List<?>)watcher.getMethod("b",buffer).invoke(null,bc.newInstance(bytes));ck(decoded.size()==slots.length&&bytes.readableBytes()==0,"actual original watcher serialization terminator and field count");for(Object field:decoded){Class<?> type=field.getClass();int id=(Integer)type.getMethod("a").invoke(field);ck(Arrays.stream(slots).anyMatch(v->v==id)&&(Integer)type.getMethod("b").invoke(field)==value&&(Integer)type.getMethod("c").invoke(field)==2,"original watched slots carry signed integers, not packed block-state IDs");}bytes.release();
   }
  }
 }
 static void dataWire()throws Exception {
  for(Block block:new Block[]{Blocks.AIR,Blocks.STONE,Blocks.BRICK_BLOCK,Blocks.WOOL})for(int meta:new int[]{0,1,15,255,-1}){
   EntityRubble s=new EntityRubble(null),c=new EntityRubble(null);s.setMetaBasedOnBlock(block,meta);transfer(s,c);ck(c.getBlockId()==Block.getIdFromBlock(block)&&c.getBlockMetadata()==meta,"rubble block ID and raw metadata stay distinct VARINTs");NBTTagCompound n=new NBTTagCompound();s.writeEntityToNBT(n);ck(n.getKeySet().equals(new HashSet<>(Arrays.asList("block","meta"))),"rubble persistence keys/types retained");EntityRubble loaded=new EntityRubble(null);loaded.readEntityFromNBT(n);ck(loaded.getBlockId()==c.getBlockId()&&loaded.getBlockMetadata()==meta,"rubble NBT identity round-trip");
   EntityBlockSpider bs=new EntityBlockSpider(null),bc=new EntityBlockSpider(null);bs.setMaterial(block,meta);transfer(bs,bc);ck(bc.getBlockId()==Block.getIdFromBlock(block)&&bc.getBlockMetadata()==meta,"spider typed native watcher updates after inherited mob fields");
  }
  EntityBlockSpider spider=new EntityBlockSpider(null);ck(spider.getBlockId()==1&&spider.getBlockMetadata()==0,"original spider stone/default metadata");
  Method write=EntityTNTPrimedBase.class.getDeclaredMethod("writeEntityToNBT",NBTTagCompound.class),read=EntityTNTPrimedBase.class.getDeclaredMethod("readEntityFromNBT",NBTTagCompound.class);write.setAccessible(true);read.setAccessible(true);
  for(int fuse:new int[]{-129,-1,0,80,127,128,255}){EntityTNTPrimedBase s=new EntityTNTPrimedBase(null),c=new EntityTNTPrimedBase(null);s.fuse=fuse;s.setBlock(Blocks.TNT);transfer(s,c);ck(c.getBlock()==Blocks.TNT&&c.fuse==80,"only original TNT block ID is synchronized; fuse intentionally remains original local field");NBTTagCompound n=new NBTTagCompound();write.invoke(s,n);ck(n.getKeySet().equals(new HashSet<>(Arrays.asList("Fuse","Tile")))&&n.hasKey("Fuse",1)&&n.hasKey("Tile",3),"TNT original byte/int persistence identity");read.invoke(c,n);ck(c.fuse==(byte)fuse&&c.getBlock()==Blocks.TNT,"original signed fuse-byte persistence retained");}
 }
 public static void main(String[] args)throws Exception {setup();OriginalCubeFaces.initialize(args[0]);OriginalFlowerIcons.verify(args[0]);renderTraces();helperVertices();customVertices(args[1]);dataWire();legacyWire(args[0]);System.out.println("PASS "+checks+" recorded renderer-transform, native BufferBuilder/material and old/native metadata-wire assertions. Complete renderer bodies execute; entity mechanics and launched GPU/game/server behavior are NOT certified.");}
}

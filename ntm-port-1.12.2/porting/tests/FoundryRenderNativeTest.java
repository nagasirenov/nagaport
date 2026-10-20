import java.io.File;
import java.lang.reflect.*;
import java.nio.FloatBuffer;
import java.util.*;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.direct.PlainItemPerspectives;
import com.hbm.render.tileentity.*;
import com.hbm.render.block.nativeblock.*;
import com.hbm.tileentity.machine.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

/** Actual GL and native RenderItem/TESR calls, controlled native services, no game loop. */
public class FoundryRenderNativeTest extends NativeFoundryBlocksTest {
 static class CustomModel implements IBakedModel {
  public List<BakedQuad> getQuads(IBlockState s,EnumFacing side,long seed){return Collections.emptyList();}
  public boolean isAmbientOcclusion(){return false;}public boolean isGui3d(){return true;}public boolean isBuiltInRenderer(){return true;}
  public TextureAtlasSprite getParticleTexture(){return sprite(new ResourceLocation("hbm","blocks/brick_fire"));}
  public ItemCameraTransforms getItemCameraTransforms(){return ItemCameraTransforms.DEFAULT;}public ItemOverrideList getOverrides(){return ItemOverrideList.NONE;}
  public Pair<? extends IBakedModel,Matrix4f> handlePerspective(TransformType type){check(type==TransformType.GUI,"foundry custom uses native GUI context");return Pair.of(this,PlainItemPerspectives.compose(type,new ItemTransformMatrix().build()));}
 }
 static final class NativeRenderItem extends RenderItem {
  final IBakedModel chosen=new CustomModel();NativeRenderItem(TextureManager t,ModelManager m){super(t,m,new ItemColors());}
  @Override public IBakedModel getItemModelWithOverrides(ItemStack stack,World world,EntityLivingBase holder){check(world==null&&holder==null,"foundry exact null inventory holder");return chosen;}
 }
 static final class Recorder extends TileEntityItemStackRenderer {
  float[] matrix;ItemStack seen;int draws;boolean fail;
  @Override public void renderByItem(ItemStack stack,float partial){seen=stack;draws++;matrix=matrix();if(fail){GlStateManager.pushMatrix();GlStateManager.translate(30,40,50);GlStateManager.disableLighting();throw new IllegalStateException("intentional custom mesh failure");}}
 }
 static float[] matrix(){FloatBuffer out=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,out);float[] values=new float[16];out.get(values);return values;}
 static void matrixEquals(float[] a,float[] b,String label){for(int i=0;i<16;i++)close(a[i],b[i],label+"/"+i);}
 static void initial(){GlStateManager.matrixMode(GL11.GL_MODELVIEW);GlStateManager.loadIdentity();GlStateManager.translate(3,4,5);GlStateManager.rotate(17,0,1,0);GlStateManager.enableLighting();GlStateManager.enableCull();GlStateManager.disableBlend();GlStateManager.depthMask(true);OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,37,83);}
 static void damage(TextureManager textures,TextureMap atlas,NativeWorld world)throws Exception {
  java.lang.reflect.Field unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)unsafeField.get(null);
  // Only the native RenderGlobal holder is allocated without startup. Its actual
  // pre/post/damage methods execute; native world/tiles use genuine constructors.
  RenderGlobal global=(RenderGlobal)unsafe.allocateInstance(RenderGlobal.class);Minecraft mc=Minecraft.getMinecraft();set(global,RenderGlobal.class,"mc",mc);set(global,RenderGlobal.class,"renderEngine",textures);set(global,RenderGlobal.class,"damagedBlocks",new HashMap<Integer,DestroyBlockProgress>());
  Method pre=RenderGlobal.class.getDeclaredMethod("preRenderDamagedBlocks"),post=RenderGlobal.class.getDeclaredMethod("postRenderDamagedBlocks");pre.setAccessible(true);post.setAccessible(true);
  Method geometry=RenderFoundryFluid.class.getDeclaredMethod("geometry",FoundryGeometry.Kind.class,int.class,com.hbm.blocks.machine.FoundryModelState.class,FoundryMaterials.class,TextureAtlasSprite.class,int.class);geometry.setAccessible(true);
  net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher dispatcher=net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher.instance;dispatcher.world=world;dispatcher.renderEngine=textures;dispatcher.entityX=4;dispatcher.entityY=20;dispatcher.entityZ=4;dispatcher.renderers.put(TileEntityFoundryChannel.class,new RenderFoundryFluid());dispatcher.renderers.put(TileEntityFoundryTank.class,new RenderFoundryFluid());
  for(int k:new int[]{2,3})for(int amount:new int[]{0,17})for(int stage:new int[]{0,5,9}){
   TileEntityFoundryBase tile=world.put(P,owners[k]);tile.type=iron;tile.amount=amount;check(tile.canRenderBreaking()&&world.getBlockState(P).hasCustomBreakingProgress(),"both native breaking gates enabled only for complete overlay owner");
   com.hbm.blocks.machine.FoundryModelState state=com.hbm.blocks.machine.FoundryModelState.get(owners[k].getExtendedState(world.getBlockState(P),world,P));FoundryGeometry.Kind kind=k==2?FoundryGeometry.Kind.CHANNEL:FoundryGeometry.Kind.TANK;FoundryMaterials materials=new FoundryMaterials(kind,NativeFoundryBlocksTest::sprite);TextureAtlasSprite crack=sprite(new ResourceLocation("minecraft","blocks/destroy_stage_"+stage));int light=0x00900030;
   List<BakedQuad> expected=new ArrayList<>(FoundryGeometry.build(kind,false,false,0,state,materials));int housing=expected.size();expected.addAll(FoundryGeometry.build(kind,false,true,0,state,materials));List<BakedQuad> actual=(List<BakedQuad>)geometry.invoke(null,kind,0,state,materials,crack,light);check(actual.size()==expected.size()&&housing>0,"damage includes all housing plus molten faces exactly once, even empty tank");
   for(int q=0;q<actual.size();q++){BakedQuad target=actual.get(q),source=expected.get(q);check(target.getSprite()==crack,"actual native crack sprite for every housing/fluid face");int[] a=target.getVertexData(),b=source.getVertexData();for(int v=0;v<4;v++){int at=v*7;for(int component=0;component<3;component++)check(a[at+component]==b[at+component],"damage preserves every original vertex position");close(Float.intBitsToFloat(a[at+4]),crack.getInterpolatedU(source.getSprite().getUnInterpolatedU(Float.intBitsToFloat(b[at+4]))),"native damage U remap");close(Float.intBitsToFloat(a[at+5]),crack.getInterpolatedV(source.getSprite().getUnInterpolatedV(Float.intBitsToFloat(b[at+5]))),"native damage V remap");check(a[at+6]==(q<housing?light:240),"damage retains own housing light versus molten packed240");}}
   initial();pre.invoke(global);float[] before=matrix();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);dispatcher.render(tile,.5F,stage);matrixEquals(matrix(),before,"actual native damage dispatcher matrix ownership");check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth&&GL11.glIsEnabled(GL11.GL_BLEND)&&GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL),"TESR preserves actual RenderGlobal damage blend/polygon state");post.invoke(global);
  }
  net.minecraft.client.renderer.vertex.VertexFormat format=(net.minecraft.client.renderer.vertex.VertexFormat)get(null,RenderFoundryFluid.class,"DAMAGE_FORMAT");check(!format.hasColor()&&format.hasUvOffset(1),"original disableColor uses native no-color vertex layout with per-vertex lightmap");
  check(!owners[0].getDefaultState().hasCustomBreakingProgress()&&!owners[1].getDefaultState().hasCustomBreakingProgress()&&!new TileEntityFoundryBasin().canRenderBreaking()&&!new TileEntityFoundryMold().canRenderBreaking(),"basin/mold damage ownership unchanged");
  System.out.println("PASS native housing+molten crack geometry and actual RenderGlobal pre/post -> TileEntityRendererDispatcher damage-stage calls.");
 }

 public static void main(String[] args)throws Exception {
  resources=new File(args[0]);setup();Class<?> contextType=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context");Constructor<?> constructor=contextType.getDeclaredConstructor();constructor.setAccessible(true);
  try(AutoCloseable context=(AutoCloseable)constructor.newInstance()){
   Method client=MaterialTextureAtlasSpriteNativeTest.class.getDeclaredMethod("initializeNativeClientSettings");client.setAccessible(true);client.invoke(null);Minecraft mc=Minecraft.getMinecraft();OpenGlHelper.initializeTextures();
   SimpleReloadableResourceManager manager=new SimpleReloadableResourceManager(new MetadataSerializer());manager.reloadResourcePack(new FolderResourcePack(resources));TextureManager textures=new TextureManager(manager);set(mc,Minecraft.class,"renderEngine",textures);set(mc,Minecraft.class,"resourceManager",manager);
   for(String path:System.getProperty("java.class.path").split(File.pathSeparator))if(path.contains("forgeBin-")&&path.endsWith(".jar"))net.minecraftforge.fml.common.asm.FMLSanityChecker.fmlLocation=new File(path);
   TextureMap atlas=new TextureMap("textures");atlas.setMipmapLevels(0);for(FoundryGeometry.Kind kind:FoundryGeometry.Kind.values())for(ResourceLocation id:FoundryMaterials.textures(kind).values())atlas.registerSprite(id);Method missing=TextureMap.class.getDeclaredMethod("initMissingImage");missing.setAccessible(true);missing.invoke(atlas);atlas.loadTextureAtlas(manager);((Map<ResourceLocation,ITextureObject>)get(textures,TextureManager.class,"mapTextureObjects")).put(TextureMap.LOCATION_BLOCKS_TEXTURE,atlas);set(mc,Minecraft.class,"textureMapBlocks",atlas);
   ModelManager modelManager=new ModelManager(atlas);set(modelManager,ModelManager.class,"modelRegistry",new net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel>());set(modelManager,ModelManager.class,"defaultModel",new CustomModel());NativeRenderItem renderer=new NativeRenderItem(textures,modelManager);set(mc,Minecraft.class,"renderItem",renderer);Item item=new Item();Recorder recorder=new Recorder();item.setTileEntityItemStackRenderer(recorder);ItemStack stack=new ItemStack(item,9,3);net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();tag.setString("actual","kept");stack.setTagCompound(tag);
   NativeWorld w=new NativeWorld(true);TileEntityFoundryMold mold=w.put(P,owners[1]);mold.slots[0]=stack;RenderFoundry foundry=new RenderFoundry();renderer.zLevel=71;
   initial();GlStateManager.pushMatrix();GlStateManager.translate(1,2,3);GlStateManager.translate(.125,.13,.125);double scale=.0625*12/16;GlStateManager.scale(scale,scale,scale);GlStateManager.rotate(90,1,0,0);GlStateManager.translate(1,0,-3);float[] expected=matrix();GlStateManager.popMatrix();float[] before=matrix();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);foundry.render(mold,1,2,3,.25F,-1,1);
   check(recorder.draws==1&&recorder.seen==stack&&stack.getCount()==9&&stack.getTagCompound()==tag,"native selected custom draw preserves exact slot stack and NBT identity");matrixEquals(recorder.matrix,expected,"original custom-first x1/z-3 GUI caller matrix");matrixEquals(matrix(),before,"normal foundry draw restores outer matrix");check(renderer.zLevel==71&&GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth,"normal foundry local z and matrix ownership");close(OpenGlHelper.lastBrightnessX,37,"normal lightmap X restored");close(OpenGlHelper.lastBrightnessY,83,"normal lightmap Y restored");
   atlas.setBlurMipmapDirect(true,true);recorder.fail=true;boolean failed=false;try{foundry.render(mold,1,2,3,0,-1,1);}catch(IllegalStateException e){failed=e.getMessage().contains("intentional");}check(failed,"custom failure propagates");matrixEquals(matrix(),before,"failed nested native GUI draw restores owned matrix");check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth&&renderer.zLevel==71,"failure restores native pushed depths and z");check((Boolean)get(atlas,AbstractTexture.class,"blur")&&(Boolean)get(atlas,AbstractTexture.class,"mipmap"),"failure restores actual atlas filtering cache");check(GL11.glIsEnabled(GL11.GL_LIGHTING)&&GL11.glIsEnabled(GL11.GL_CULL_FACE)&&!GL11.glIsEnabled(GL11.GL_BLEND),"failure restores caller GL enables");
   recorder.fail=false;
   BlockModelShapes shapes=modelManager.getBlockModelShapes();Map<IBlockState,IBakedModel> blockModels=(Map<IBlockState,IBakedModel>)get(shapes,BlockModelShapes.class,"bakedModelStore");
   set(mc,Minecraft.class,"blockRenderDispatcher",new BlockRendererDispatcher(shapes,new net.minecraft.client.renderer.color.BlockColors()));
   Item blockItem=Item.getItemFromBlock(owners[3]);blockItem.setTileEntityItemStackRenderer(recorder);ItemStack output=new ItemStack(blockItem,7,0);output.setTagCompound(tag);mold.slots[0]=null;mold.slots[1]=output;
   blockModels.put(owners[3].getDefaultState(),new CustomModel());int previousDraws=recorder.draws;
   initial();GlStateManager.pushMatrix();GlStateManager.translate(1,2,3);GlStateManager.translate(.125,mold.outHeight(),.125);GlStateManager.scale(scale,scale,scale);GlStateManager.rotate(90,1,0,0);GlStateManager.translate(1,0,-3);float[] fallbackExpected=matrix();GlStateManager.popMatrix();
   foundry.render(mold,1,2,3,0,-1,1);foundry.render(mold,1,2,3,0,-1,1);check(recorder.draws==previousDraws+2&&recorder.seen==output&&output.getCount()==7&&output.getTagCompound()==tag,"approved no-top fallback draws exact native output stack without count/NBT mutation");matrixEquals(recorder.matrix,fallbackExpected,"approved fallback preserves output height and native custom matrix");check(((Set<?>)get(null,RenderFoundry.class,"NO_TOP_WARNED")).size()==1,"no-top compatibility warning once per block across repeated draws");
   final TextureAtlasSprite top=sprite(new ResourceLocation("hbm","blocks/foundry_tank_top"));final BakedQuad topQuad=ConveyorCuboidGeometry.face(top,true,EnumFacing.UP,0,0,0,0,1,1,1,0);
   for(final boolean culled:new boolean[]{false,true}){blockModels.put(owners[3].getDefaultState(),new CustomModel(){@Override public List<BakedQuad> getQuads(IBlockState state,EnumFacing face,long seed){return (culled?face==EnumFacing.UP:face==null)?Collections.singletonList(topQuad):Collections.emptyList();}});Method select=RenderFoundry.class.getDeclaredMethod("topSprite",ItemStack.class);select.setAccessible(true);check(select.invoke(null,output)==top,"known native upward surface selects exact material, not particle");previousDraws=recorder.draws;foundry.render(mold,1,2,3,0,-1,1);check(recorder.draws==previousDraws&&mold.slots[1]==output,"known flat-top path retained without custom fallback");}
   mold.slots[1]=null;
   recorder.fail=false;mold.slots[0]=null;mold.type=null;mold.amount=0; // native EMPTY path must remain harmless
   foundry.render(mold,0,0,0,0,-1,1);mold.slots[0]=new ItemStack(molds,1,0);mold.amount=36;
   // Avoid a custom recipe-item fixture; test actual molten renderer entry with slots empty and positive overridden surface contract.
   TileEntityFoundryMold liquid=new TileEntityFoundryMold(){@Override public int getCapacity(){return 72;}};liquid.type=iron;liquid.amount=36;
   initial();before=matrix();foundry.render(liquid,1,2,3,.5F,-1,1);matrixEquals(matrix(),before,"actual two molten passes restore outer matrix");close(OpenGlHelper.lastBrightnessX,37,"basin molten restores block light");close(OpenGlHelper.lastBrightnessY,83,"basin molten restores sky light");
   RenderFoundryFluid fluid=new RenderFoundryFluid();for(int k:new int[]{2,3}){TileEntityFoundryBase tile=w.put(P,owners[k]);tile.type=iron;for(int amount:new int[]{0,1,tile.getCapacity()/2,tile.getCapacity()}){tile.amount=amount;initial();before=matrix();fluid.render(tile,1,2,3,.5F,-1,1);matrixEquals(matrix(),before,"actual dynamic channel/tank amount TESR matrix restore");check(GL11.glIsEnabled(GL11.GL_LIGHTING)&&GL11.glIsEnabled(GL11.GL_CULL_FACE)&&!GL11.glIsEnabled(GL11.GL_BLEND),"fluid caller state restored");close(OpenGlHelper.lastBrightnessX,37,"channel/tank lightmap X restored");close(OpenGlHelper.lastBrightnessY,83,"channel/tank lightmap Y restored");}}
   damage(textures,atlas,w);check(GL11.glGetError()==GL11.GL_NO_ERROR,"real native GL draws complete without error");System.out.println("PASS "+checks+" actual EGL/native foundry TESR and custom GUI matrix/stack/depth/light/exception checks. Genuine Minecraft constructor and controlled service initialization, no launched game or screenshot claim.");
  }
 }
}

import com.hbm.render.util.NativeMachineItemRenderer;
import com.google.common.collect.*;
import java.util.*;
import java.lang.reflect.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.model.*;
import net.minecraft.util.EnumFacing;
public class NativeMachineItemTest {
 static int checks;static void check(boolean a,String b){checks++;if(!a)throw new AssertionError(b);}
 static IBakedModel model(List<BakedQuad> quads,TextureAtlasSprite sprite){return new BakedItemModel(ImmutableList.copyOf(quads),sprite,ImmutableMap.of(),ItemOverrideList.NONE);}
 static float[] matrix(){java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);org.lwjgl.opengl.GL11.glGetFloat(org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX,b);float[] a=new float[16];b.get(a);return a;}
 static void frameMatrices(){
  org.lwjgl.opengl.GL11.glMatrixMode(org.lwjgl.opengl.GL11.GL_MODELVIEW);
  for(boolean fancy:new boolean[]{false,true}) {
   org.lwjgl.opengl.GL11.glLoadIdentity();org.lwjgl.opengl.GL11.glScalef(.5128205F,.5128205F,.5128205F);org.lwjgl.opengl.GL11.glTranslatef(0,-.05F,0);
   if(fancy){org.lwjgl.opengl.GL11.glRotatef(180,0,1,0);org.lwjgl.opengl.GL11.glTranslatef(-.5F,-.25F,-(.0625F+.021875F)/2);org.lwjgl.opengl.GL11.glTranslatef(0,0,.0625F+.021875F);}
   float[] expected=matrix();org.lwjgl.opengl.GL11.glLoadIdentity();NativeMachineItemRenderer.applyIconFrame(fancy);check(Arrays.equals(expected,matrix()),"legacy icon frame matrix "+fancy);
  }
  for(int age:new int[]{0,71})for(float partial:new float[]{0,.375F})for(float hover:new float[]{0,1.25F}) {
   org.lwjgl.opengl.GL11.glLoadIdentity();org.lwjgl.opengl.GL11.glRotatef(((age+partial)/20F+hover)*57.295776F,0,1,0);org.lwjgl.opengl.GL11.glScalef(1.25F,1.25F,1.25F);org.lwjgl.opengl.GL11.glTranslatef(0,.05F,0);org.lwjgl.opengl.GL11.glRotatef(-90,0,1,0);org.lwjgl.opengl.GL11.glScalef(.25F,.25F,.25F);org.lwjgl.opengl.GL11.glTranslatef(-.5F,-.5F,-.5F);
   float[] expected=matrix();org.lwjgl.opengl.GL11.glLoadIdentity();NativeMachineItemRenderer.applyBlockFrame(age,partial,hover);check(Arrays.equals(expected,matrix()),"legacy block frame matrix");
  }
  org.lwjgl.opengl.GL11.glLoadIdentity();check(org.lwjgl.opengl.GL11.glGetError()==0,"native frame transformations no GL error");
 }
 public static void main(String[] args)throws Exception {
  Constructor<?> c=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();c.setAccessible(true);
  try(AutoCloseable context=(AutoCloseable)c.newInstance()) {
   Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);
   frameMatrices();
   NativeMachineDrawTest.run();
   for(int size:new int[]{1,16,32})for(boolean holes:new boolean[]{false,true}) {
    TextureAtlasSprite sprite=new TextureAtlasSprite("test:machine"+size+holes){};sprite.setIconWidth(size);sprite.setIconHeight(size);sprite.initSprite(128,128,16,32,false);
    int[] pixels=new int[size*size];for(int i=0;i<pixels.length;i++)pixels[i]=holes&&i%3==0?0:0xFFFFFFFF;sprite.setFramesTextureData(Collections.singletonList(new int[][]{pixels}));
    List<BakedQuad> quads=ItemLayerModel.getQuadsForSprite(0,sprite,DefaultVertexFormats.ITEM,Optional.empty());
    List<BakedQuad> fronts=NativeMachineItemRenderer.iconLayers(model(quads,sprite));
    if(fronts==null) for(BakedQuad q:quads)if(q.getFace()==EnumFacing.SOUTH)System.out.println("Front z="+Float.intBitsToFloat(q.getVertexData()[2])+" format="+q.getFormat());
    check(fronts!=null&&fronts.size()==1,"real generated icon recognized size="+size+" holes="+holes);
    List<BakedQuad> extra=new ArrayList<>(quads);extra.add(quads.get(0));check(NativeMachineItemRenderer.iconLayers(model(extra,sprite))==null,"extra geometry never flattened");
    List<BakedQuad> missing=new ArrayList<>(quads);missing.remove(0);check(NativeMachineItemRenderer.iconLayers(model(missing,sprite))==null,"missing side/back not accepted");
    List<BakedQuad> changed=new ArrayList<>(quads);BakedQuad q=changed.get(0);int[] v=q.getVertexData().clone();v[0]^=1;changed.set(0,new BakedQuad(v,q.getTintIndex(),q.getFace(),sprite,q.shouldApplyDiffuseLighting(),q.getFormat()));check(NativeMachineItemRenderer.iconLayers(model(changed,sprite))==null,"changed geometry not flattened");
    List<BakedQuad> two=new ArrayList<>(quads);two.addAll(ItemLayerModel.getQuadsForSprite(1,sprite,DefaultVertexFormats.ITEM,Optional.empty()));check(NativeMachineItemRenderer.iconLayers(model(two,sprite)).size()==2,"multiple passes retained");
   }
   System.out.println("PASS "+checks+" native generated-model ownership and actual GL frame-matrix checks; not full machine draw or game acceptance.");
  }
 }
}

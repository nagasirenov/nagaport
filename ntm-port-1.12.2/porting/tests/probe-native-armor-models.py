#!/usr/bin/env python3
"""Whole native armor/man models plus original fallback formula oracle; explicit NTM mesh fixture."""
import argparse,os,pathlib,subprocess,tempfile,zipfile,re
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2];src=root/'src/main/java';tests=root/'porting/tests'
files=[src/p for p in ['com/hbm/render/model/ModelArmorBase.java','com/hbm/render/model/ModelMan.java','com/hbm/render/model/ModelArmorHEV.java','com/hbm/render/loader/ModelRendererObj.java','com/hbm/render/mesh/MeshModel.java','com/hbm/render/mesh/ObjMesh.java','com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']]
files += [tests/p for p in ['NativeArmorModelsTest.java','MaterialTextureAtlasSpriteNativeTest.java','ForgeEventNativeLauncher.java']]
# Mechanically check the bounded production adaptation against portable CP30 originals.
old=(tests/'fixtures/ModelArmorBase-cp30.java.txt').read_text();expected=old.replace('import net.minecraft.client.renderer.entity.RenderManager;\n','').replace('Render render = RenderManager.instance.getEntityRenderObject(entity);','Render<?> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);').replace('renderPlayer.modelBipedMain','renderPlayer.getMainModel()').replace('RenderBiped renderBiped = (RenderBiped) render;','RenderBiped<?> renderBiped = (RenderBiped<?>) render;').replace('renderBiped.modelBipedMain','(ModelBiped) renderBiped.getMainModel()').replace('this.heldItemLeft','heldItemWeight(this.leftArmPose)').replace('this.heldItemRight','heldItemWeight(this.rightArmPose)').replace('this.onGround','this.swingProgress').replace('MathHelper.sqrt_float','MathHelper.sqrt').replace('if(this.aimedBow)','if(this.rightArmPose == ArmPose.BOW_AND_ARROW || this.leftArmPose == ArmPose.BOW_AND_ARROW)').replace('this.aimedBow = modelBiped.aimedBow;','this.leftArmPose = modelBiped.leftArmPose;\n\t\tthis.rightArmPose = modelBiped.rightArmPose;')
helper='\t/** Original fallback weights: holding = 1, blocking = 3; bow aiming is applied below. */\n\tprivate static int heldItemWeight(ArmPose pose) {\n\t\treturn pose == ArmPose.EMPTY ? 0 : pose == ArmPose.BLOCK ? 3 : 1;\n\t}\n\n'
expected=expected.replace('\tprotected static void bindTexture(ResourceLocation location) {',helper+'\tprotected static void bindTexture(ResourceLocation location) {')
assert (src/'com/hbm/render/model/ModelArmorBase.java').read_text()==expected,'Unexpected base model behavioral edit'
assert (src/'com/hbm/render/model/ModelMan.java').read_text()==(tests/'fixtures/ModelMan-cp30.java.txt').read_text().replace('render.modelBipedMain','render.getMainModel()').replace('import net.minecraft.client.renderer.entity.RenderPlayer;','import net.minecraft.client.renderer.GlStateManager;\nimport net.minecraft.client.renderer.entity.RenderPlayer;').replace('GL11.glEnable(GL11.GL_LIGHTING);','GlStateManager.enableLighting();'),'Unexpected ModelMan edit'
print('PASS exact two-model source adaptation; original public mesh fields/render overloads/draw bodies retained',flush=True)
with tempfile.TemporaryDirectory(prefix='native-armor-models-') as tmp:
 t=pathlib.Path(tmp)
 def add(name,text):
  f=t/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text);files.append(f)
 # Compile the exact original fallback body independently. Old names exist only in this oracle.
 oracle=old.replace('package com.hbm.render.model;','package com.hbm.render.model;').replace('ModelArmorBase','LegacyArmorFallbackOracle').replace('import net.minecraft.client.renderer.entity.RenderManager;\n','')
 start=oracle.index('\t\tRender render =');end=oracle.index('\n\t\t/// FALLBACK ///',start)
 oracle=oracle[:start]+oracle[end:]
 oracle=oracle[:oracle.index('\n\tprivate void copyPropertiesFromBiped')]+ '\n}\n'
 oracle=oracle.replace('public int type;','public int type;\n public int heldItemLeft, heldItemRight; public float onGround; public boolean aimedBow;').replace('MathHelper.sqrt_float','MathHelper.sqrt')
 add('com/hbm/render/model/LegacyArmorFallbackOracle.java',oracle)
 add('com/hbm/main/ResourceManager.java','''package com.hbm.main;
import com.hbm.render.mesh.*;import net.minecraft.util.ResourceLocation;import java.util.*;
public final class ResourceManager {
 public static final class Mesh implements MeshModel {
  public final List<String> calls=new ArrayList<>(); public final List<float[]> matrices=new ArrayList<>();
  public ObjMesh getMesh(){return ObjMesh.empty();} public List<String> getPartNames(){return Collections.emptyList();}
  public void renderAll(){renderPart("ALL");}public void renderOnly(String... names){for(String n:names)renderPart(n);}public void renderAllExcept(String... names){renderAll();}
  public void renderPart(String name){calls.add(name);java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);org.lwjgl.opengl.GL11.glGetFloat(org.lwjgl.opengl.GL11.GL_MODELVIEW_MATRIX,b);float[] f=new float[16];b.get(f);matrices.add(f);}
 }
 public static final Mesh player_manly_af=new Mesh(),armor_hev=new Mesh();
 public static final ResourceLocation player_manly_tex=new ResourceLocation("test:man"),hev_helmet=new ResourceLocation("test:head"),hev_chest=new ResourceLocation("test:body"),hev_arm=new ResourceLocation("test:arm"),hev_leg=new ResourceLocation("test:leg");
}''')
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(t/name).write_bytes(z.read(name))
 assert (t/'liblwjgl64.so').exists(),'Cached native Linux LWJGL jar required'
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 print('PASS complete production ModelArmorBase, ModelMan, ModelArmorHEV and ModelRendererObj compile against genuine native Forge',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+tmp,'-cp',tmp+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NativeArmorModelsTest'],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)

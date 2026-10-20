#!/usr/bin/env python3
"""Native Sedna hand/overlay, typed contexts, geometry and lexical GL proof. No downloads."""
import argparse,hashlib,os,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
names=['render/item/nativeitem/staticfamily/SingleMeshEntityItemRenderer.java','render/util/NativeMachineItemRenderer.java','render/util/NativeGuiItemDraw.java','render/util/NativeItemQuadRenderer.java','render/util/NativeIconExtrusion.java','render/util/NativeItemBaseModel.java','render/item/ItemRenderBoltgun.java','render/item/weapon/sedna/ItemRenderWeaponBase.java','render/item/weapon/sedna/ItemRenderCoilgun.java','render/item/weapon/sedna/nativehand/SednaHandHandler.java','render/item/nativeitem/direct/PlainItemPerspectives.java','render/item/nativeitem/ItemTransformMatrix.java','render/item/nativeitem/StaticMeshItemModel.java','render/item/nativeitem/StaticMeshItemRegistration.java','render/item/nativeitem/AngelCoreFrameCorrection.java','render/item/nativeitem/staticfamily/OrdinaryMeshItemModel.java','render/item/nativeitem/MaterialTextureAtlasSprite.java','render/mesh/MeshModel.java','render/mesh/ObjMesh.java']
files=[root/'src/main/java/com/hbm'/n for n in names]+sorted(p for p in (root/'src/main/java/com/hbm/render/item/nativeitem/context').glob('*.java') if p.name != 'SednaPlayerOwnership.java')
production=list(files)+[root/'src/main/java/com/hbm/main/ModEventHandlerClient.java']
with tempfile.TemporaryDirectory(prefix='native-context-items-') as tmp:
 tmp=pathlib.Path(tmp)
 def fixture(name,body):
  path=tmp/(name+'.java');path.parent.mkdir(parents=True,exist_ok=True);path.write_text(body);files.append(path)
 source=(root/'src/main/java/com/hbm/main/ModEventHandlerClient.java').read_text()
 begin=source.index('public void setupFOV(FOVUpdateEvent event)');end=source.index('\n\t}',begin)+4
 fixture('SednaFovNativeFixture','import net.minecraftforge.client.event.FOVUpdateEvent;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.client.Minecraft;import net.minecraft.item.ItemStack;import com.hbm.render.item.weapon.sedna.ItemRenderWeaponBase;public class SednaFovNativeFixture{'+source[begin:end]+'}')
 fixture('com/hbm/render/anim/HbmAnimations','package com.hbm.render.anim; public class HbmAnimations {public static double recoil,reload;public static double[] getRelevantTransformation(String key){if(key.equals("RECOIL"))return new double[]{recoil,0,0};if(key.equals("RELOAD"))return new double[]{reload,0,0};throw new AssertionError(key);}}')
 fixture('com/hbm/main/ResourceManager','''package com.hbm.main;
 import com.hbm.render.mesh.*;import java.util.*;import org.lwjgl.opengl.GL11;
 public class ResourceManager {
 public static final net.minecraft.util.ResourceLocation boltgun_tex=new net.minecraft.util.ResourceLocation("test","boltgun");
 public static final Draw boltgun=new Draw(),coilgun=new Draw();public static final net.minecraft.util.ResourceLocation flaregun_tex=new net.minecraft.util.ResourceLocation("test","flaregun"),coilgun_tex=new net.minecraft.util.ResourceLocation("test","coilgun");
 public static class Draw implements MeshModel {
 public final List<String> parts=new ArrayList<>(); public final List<javax.vecmath.Matrix4f> matrices=new ArrayList<>();public boolean fail;
 public ObjMesh getMesh(){return ObjMesh.empty();}public List<String> getPartNames(){return Arrays.asList("Gun","Barrel");}
 public void renderPart(String n){parts.add(n);java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,b);float[] f=new float[16];b.get(f);javax.vecmath.Matrix4f m=new javax.vecmath.Matrix4f(f);m.transpose();matrices.add(m);if(fail)throw new IllegalStateException("controlled mesh failure");}
 public void renderAll(){renderPart("All");}public void renderOnly(String...n){throw new AssertionError();}public void renderAllExcept(String...n){throw new AssertionError();}
 }}''')
 fixture('com/hbm/config/ClientConfig','package com.hbm.config;public class ClientConfig{public static final Flag GUN_MODEL_FOV=new Flag();public static class Flag{public boolean value;public boolean get(){return value;}}}')
 fixture('com/hbm/lib/RefStrings','package com.hbm.lib;public class RefStrings{public static final String MODID="hbm";}')
 fixture('com/hbm/items/weapon/sedna/ItemGunBaseNT','package com.hbm.items.weapon.sedna;public class ItemGunBaseNT extends net.minecraft.item.Item{public static float prevAimingProgress,aimingProgress;public static boolean aiming;public static boolean getIsAiming(net.minecraft.item.ItemStack s){return aiming;}public static class SmokeNode{public double forward,lift,side,width,alpha;}}')
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(tmp/name).write_bytes(z.read(name))
 assert(tmp/'liblwjgl64.so').exists(),'Cached native LWJGL required'
 files += [root/'porting/tests/ContextItemNativeTest.java',root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java',root/'porting/tests/SednaItemNativeTest.java',root/'porting/tests/ForgeEventNativeLauncher.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp)]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'ForgeEventNativeLauncher','SednaItemNativeTest'],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
 for f in production:print(hashlib.sha256(f.read_bytes()).hexdigest(),f.relative_to(root))

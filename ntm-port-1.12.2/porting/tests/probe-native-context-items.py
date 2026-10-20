#!/usr/bin/env python3
"""Actual native RenderItem virtual dispatch, cache ownership and EGL proof. No downloads."""
import argparse,hashlib,os,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
names=['render/item/nativeitem/staticfamily/SingleMeshEntityItemRenderer.java','render/util/NativeMachineItemRenderer.java','render/util/NativeGuiItemDraw.java','render/util/NativeItemQuadRenderer.java','render/util/NativeIconExtrusion.java','render/util/NativeItemBaseModel.java','render/item/ItemRenderBoltgun.java','render/item/nativeitem/direct/PlainItemPerspectives.java','render/item/nativeitem/ItemTransformMatrix.java','render/item/nativeitem/StaticMeshItemModel.java','render/item/nativeitem/StaticMeshItemRegistration.java','render/item/nativeitem/AngelCoreFrameCorrection.java','render/item/nativeitem/staticfamily/OrdinaryMeshItemModel.java','render/item/nativeitem/MaterialTextureAtlasSprite.java','render/mesh/MeshModel.java','render/mesh/ObjMesh.java']
files=[root/'src/main/java/com/hbm'/n for n in names]+sorted(p for p in (root/'src/main/java/com/hbm/render/item/nativeitem/context').glob('*.java') if p.name != 'SednaPlayerOwnership.java')
production=list(files)
client=(root/'src/main/java/com/hbm/main/ClientProxy.java').read_text()
main=(root/'src/main/java/com/hbm/main/MainRegistry.java').read_text()
server=(root/'src/main/java/com/hbm/main/ServerProxy.java').read_text()
assert 'contextRenderers.put(ModItems.boltgun, new ItemRenderBoltgun());' in client
assert 'registerItemRenderer(ModItems.boltgun' not in client
assert client.index('nativeContextItems.register(event)') < client.index('MinecraftForge.EVENT_BUS.register(nativeContextItems)')
hook=client[client.index('public void onLoadComplete()'):];hook=hook[:hook.index('\n    }')]
assert hook.index('if(nativeItemOwnershipFinished) return') < hook.index('nativeItemOwnershipFinished = true') < hook.index('ownership.install()') < hook.index('new com.hbm.render.item.nativeitem.staticfamily.StaticItemEntityOwnership')
assert 'if(ownership.install()) {' in hook
assert hook.index('if(ownership.install()) {') < hook.index('nativeOneMeshItems.add(item)') < hook.index('nativeBlockItemDraws.putAll(nativeDecoItemDraws)')
assert 'if(!(item.getTileEntityItemStackRenderer() instanceof com.hbm.render.item.nativeitem.context.NativeSelectiveItemRenderer)) nativeOneMeshItems.add(item);' in hook
assert hook.index('nativeBlockItemDraws.putAll(nativeDecoItemDraws)') < hook.index('SednaPlayerOwnership sedna')
registration=(root/'src/main/java/com/hbm/render/item/nativeitem/context/ContextItemRegistration.java').read_text()
assert 'items.get(i).setTileEntityItemStackRenderer(renderers.get(items.get(i)));' in registration
assert 'after.add(ContextItemModel.create(original, renderers.get(item)));' in registration
assert '@EventHandler\n\tpublic static void LoadComplete(net.minecraftforge.fml.common.event.FMLLoadCompleteEvent event)' in main
assert 'proxy.onLoadComplete();' in main and 'public void onLoadComplete() { }' in server
print('PASS source integration guards: deferred ModelRegistry registration, load-complete transaction, gated dropped ownership, server no-op',flush=True)
with tempfile.TemporaryDirectory(prefix='native-context-items-') as tmp:
 tmp=pathlib.Path(tmp)
 def fixture(name,body):
  path=tmp/(name+'.java');path.parent.mkdir(parents=True,exist_ok=True);path.write_text(body);files.append(path)
 fixture('com/hbm/render/anim/HbmAnimations','package com.hbm.render.anim; public class HbmAnimations {public static double recoil;public static double[] getRelevantTransformation(String key){if(!key.equals("RECOIL"))throw new AssertionError(key);return new double[]{recoil,0,0};}}')
 fixture('com/hbm/main/ResourceManager','''package com.hbm.main;
 import com.hbm.render.mesh.*;import java.util.*;import org.lwjgl.opengl.GL11;
 public class ResourceManager {
 public static final net.minecraft.util.ResourceLocation boltgun_tex=new net.minecraft.util.ResourceLocation("test","boltgun");
 public static final Draw boltgun=new Draw();
 public static class Draw implements MeshModel {
 public final List<String> parts=new ArrayList<>(); public final List<javax.vecmath.Matrix4f> matrices=new ArrayList<>();public boolean fail;
 public ObjMesh getMesh(){return ObjMesh.empty();}public List<String> getPartNames(){return Arrays.asList("Gun","Barrel");}
 public void renderPart(String n){parts.add(n);java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,b);float[] f=new float[16];b.get(f);javax.vecmath.Matrix4f m=new javax.vecmath.Matrix4f(f);m.transpose();matrices.add(m);if(fail)throw new IllegalStateException("controlled mesh failure");}
 public void renderAll(){throw new AssertionError();}public void renderOnly(String...n){throw new AssertionError();}public void renderAllExcept(String...n){throw new AssertionError();}
 }}''')
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(tmp/name).write_bytes(z.read(name))
 assert(tmp/'liblwjgl64.so').exists(),'Cached native LWJGL required'
 files += [root/'porting/tests/ContextItemNativeTest.java',root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp)]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(tmp),'-cp',str(tmp)+os.pathsep+a.classpath,'ContextItemNativeTest'],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)
 for f in production:print(hashlib.sha256(f.read_bytes()).hexdigest(),f.relative_to(root))

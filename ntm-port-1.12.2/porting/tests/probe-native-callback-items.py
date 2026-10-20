#!/usr/bin/env python3
"""Real native quad/tint oracle with existing Mesa EGL for TextureUtil initialization."""
import argparse,os,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
files=[root/"src/main/java/com/hbm"/name for name in ['render/item/ItemRenderBase.java', 'render/item/nativeitem/callback/CallbackItemModel.java', 'render/item/nativeitem/callback/CallbackItemRegistration.java', 'render/util/NativeItemBaseModel.java','render/item/nativeitem/StaticMeshItemModel.java', 'render/item/nativeitem/StaticMeshItemRegistration.java', 'render/item/nativeitem/ItemTransformMatrix.java', 'render/item/nativeitem/AngelCoreFrameCorrection.java', 'render/item/nativeitem/staticfamily/OrdinaryMeshItemModel.java', 'render/item/nativeitem/staticfamily/StaticItemPerspectives.java', 'render/item/nativeitem/MaterialTextureAtlasSprite.java']]+[root/"porting/tests/CallbackItemNativeTest.java",root/"porting/tests/MaterialTextureAtlasSpriteNativeTest.java"]
with tempfile.TemporaryDirectory(prefix='native-item-quads-') as tmp:
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(pathlib.Path(tmp)/name).write_bytes(z.read(name))
 assert (pathlib.Path(tmp)/'liblwjgl64.so').exists(),'Existing native Linux LWJGL JAR required; no downloads or installs.'
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+tmp,'-cp',tmp+os.pathsep+a.classpath,'CallbackItemNativeTest'],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)

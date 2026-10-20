#!/usr/bin/env python3
"""Real native quad/tint oracle with existing Mesa EGL for TextureUtil initialization."""
import argparse,os,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
files=[root/'src/main/java/com/hbm/render/util/NativeItemBaseModel.java',root/'src/main/java/com/hbm/render/util/NativeItemQuadRenderer.java',root/'src/main/java/com/hbm/render/util/NativeGuiItemDraw.java',root/'src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java',root/'porting/tests/NativeItemQuadTest.java',root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java']
with tempfile.TemporaryDirectory(prefix='native-item-quads-') as tmp:
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(pathlib.Path(tmp)/name).write_bytes(z.read(name))
 assert (pathlib.Path(tmp)/'liblwjgl64.so').exists(),'Existing native Linux LWJGL JAR required; no downloads or installs.'
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+tmp,'-cp',tmp+os.pathsep+a.classpath,'NativeItemQuadTest'],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)

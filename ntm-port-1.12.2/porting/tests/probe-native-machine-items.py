#!/usr/bin/env python3
"""Complete native machine roots/consumers, real GL draws, and declared NTM fixtures."""
import argparse,os,pathlib,subprocess,tempfile,zipfile,importlib.util,json,hashlib
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
scope=json.loads((root/'porting/tests/native-machine-item-scope.json').read_text())
for path,digest in scope['production'].items():
 assert hashlib.sha256((root/path).read_bytes()).hexdigest()==digest,('frozen machine slice changed',path)
print('PASS exact '+str(len(scope['production']))+'-file frozen production scope',flush=True)
files=[root/'src/main/java/com/hbm/render/util/NativeItemBaseModel.java',root/'src/main/java/com/hbm/render/util/RenderDecoItem.java',root/'src/main/java/com/hbm/render/util/RenderItemStack.java',root/'src/main/java/com/hbm/render/util/NativeMachineItemRenderer.java',root/'src/main/java/com/hbm/render/util/NativeIconExtrusion.java',root/'src/main/java/com/hbm/render/util/NativeItemQuadRenderer.java',root/'src/main/java/com/hbm/render/util/NativeGuiItemDraw.java',root/'src/main/java/com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java',root/'porting/tests/NativeMachineItemTest.java',root/'porting/tests/NativeMachineDrawTest.java',root/'porting/tests/NativeMachineConsumerTest.java',root/'porting/tests/NativeBaseModelOwnershipTest.java',root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java']
with tempfile.TemporaryDirectory(prefix='native-machine-items-') as tmp:
 spec=importlib.util.spec_from_file_location('fixtures',root/'porting/tests/machine-renderer-fixtures.py'); fixtures=importlib.util.module_from_spec(spec);spec.loader.exec_module(fixtures)
 consumers=fixtures.add(root,pathlib.Path(tmp),files)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(pathlib.Path(tmp)/name).write_bytes(z.read(name))
 assert (pathlib.Path(tmp)/'liblwjgl64.so').exists(),'Existing native Linux LWJGL JAR required; no downloads or installs.'
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 print('PASS all '+str(len(consumers))+' complete machine TESR classes compiled against genuine native APIs; NTM fields/resources are controlled fixtures.',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+tmp,'-cp',tmp+os.pathsep+a.classpath,'NativeMachineItemTest'],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)

#!/usr/bin/env python3
"""Whole panel and real native helper compile; EGL gradient and caller-contract tests."""
import argparse,os,pathlib,subprocess,tempfile,zipfile,hashlib
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[3];src=root/'src/main/java';here=pathlib.Path(__file__).resolve().parent
names=['com/hbm/wiaj/actors/ActorBasicPanel.java','com/hbm/wiaj/actors/ISpecialActor.java','com/hbm/render/util/NativeGuiItemDraw.java','com/hbm/render/util/NativeItemBaseModel.java','com/hbm/render/util/NativeItemQuadRenderer.java','com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']
with tempfile.TemporaryDirectory(prefix='native-actor-panel-') as tmp:
 out=pathlib.Path(tmp);files=[src/n for n in names]+[root/'porting/tests/MaterialTextureAtlasSpriteNativeTest.java',here/'ActorPanelNativeTest.java']
 for name in ['JarScene','WorldInAJar']:
  f=out/(name+'.java');f.write_text('package com.hbm.wiaj; public class '+name+' {}');files.append(f)
 for n in names:print('WHOLE INPUT',hashlib.sha256((src/n).read_bytes()).hexdigest(),n,flush=True)
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(out/n).write_bytes(z.read(n))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+tmp,'-cp',tmp+os.pathsep+a.classpath,'ActorPanelNativeTest'],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)

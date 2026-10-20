#!/usr/bin/env python3
import argparse,hashlib,pathlib,subprocess,tempfile,os
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);a=p.parse_args()
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
h=pathlib.Path(__file__).resolve().parent;m=h.parents[2]
s=(m/'src/main/java/com/hbm/blocks/generic/BlockModDoor.java').read_text();start=s.index('public void registerBlockIcons(');op=s.index('{',start);end=op+1;depth=1
while depth:
 depth+=(s[end]=='{')-(s[end]=='}');end+=1
method=s[start:end]
assert method.count('new HorizontallyFlippedIcon(')==2
with tempfile.TemporaryDirectory(prefix='door-icon-native-') as tmp:
 d=pathlib.Path(tmp);f=d/'DoorIconBinding.java';f.write_text('package com.hbm.render.icon; public class DoorIconBinding { IIcon[] field_150017_a,field_150016_b; String getTextureName(){return "hbm:door_metal";} '+method+'}')
 files=[m/'src/main/java/com/hbm/render/icon'/n for n in ['IIcon.java','IIconRegister.java','LegacyTextureAtlasSprite.java','HorizontallyFlippedIcon.java']]+[h/'DoorIconNativeTest.java',f]
 for x in files[:4]: print('WHOLE INPUT',x.name,hashlib.sha256(x.read_bytes()).hexdigest(),flush=True)
 print('EXACT DOOR HOOK',hashlib.sha256(method.encode()).hexdigest(),flush=True)
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',tmp+os.pathsep+a.classpath,'com.hbm.render.icon.DoorIconNativeTest',str(a.legacy_client)],check=True,cwd=tmp)

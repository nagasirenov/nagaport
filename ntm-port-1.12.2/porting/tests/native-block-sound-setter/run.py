#!/usr/bin/env python3
import argparse,subprocess,tempfile,json,hashlib,re
from pathlib import Path
h=Path(__file__).resolve().parent;p=h.parents[2];r=p.parent
pa=argparse.ArgumentParser();pa.add_argument('--java-home',type=Path,required=True);pa.add_argument('--classpath-file',type=Path,required=True);pa.add_argument('--legacy-jar',type=Path,required=True);a=pa.parse_args();cp=a.classpath_file.read_text().strip();ed=json.loads((h/'edits.json').read_text());pins=json.loads((h/'pins.json').read_text())
for name,pin in pins.items():
 b=(r/name).read_bytes();assert hashlib.sha256(b).hexdigest()==pin['after'];before=b
 for x in reversed(ed):
  if x['path']==name:assert before.count(x['new'].encode())==1;before=before.replace(x['new'].encode(),x['old'].encode())
 assert hashlib.sha256(before).hexdigest()==pin['before']
for name,digest in json.loads((h/'dependencies.json').read_text()).items():assert hashlib.sha256((p/name).read_bytes()).hexdigest()==digest
print('PASS exact byte reversal; only two setter statements change, complete constructor order retained',flush=True)
assert hashlib.sha256(a.legacy_jar.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
with tempfile.TemporaryDirectory(prefix='native-constructor-sound-') as d:
 t=Path(d);s=(h/'SoundSetterTest.template').read_text().replace('CROP',next(x['new'].strip() for x in ed if x['path'].endswith('/BlockCrop.java'))).replace('GRAPHITE',next(x['new'].strip() for x in ed if x['path'].endswith('/BlockGraphiteDrilledBase.java')));(t/'SoundSetterTest.java').write_text(s)
 # Exact production metal initializer and exact native-event constructor body, not a replacement Minecraft class.
 source=(p/'src/main/java/com/hbm/blocks/ModSoundTypes.java').read_text();field=re.search(r'public static final ModSoundType legacyMetal = .*?;',source,re.S)[0]
 source=(p/'src/main/java/com/hbm/blocks/ModSoundType.java').read_text();ctor=re.search(r'protected ModSoundType\(SoundEvent.*?\n\t}',source,re.S)[0]
 (t/'ModSoundTypes.java').write_text('package com.hbm.blocks; import net.minecraft.block.SoundType; import net.minecraft.util.SoundEvent; public class ModSoundTypes {'+field+'} class ModSoundType extends SoundType { protected final SoundEvent placeSound,breakSound,stepSound;'+ctor+'}')
 launcher=h.parent/'ForgeEventNativeLauncher.java'
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',cp,'-sourcepath',str(t/'empty'),'-d',d,str(t/'SoundSetterTest.java'),str(t/'ModSoundTypes.java'),str(launcher)],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+':'+cp,'ForgeEventNativeLauncher','SoundSetterTest',str(a.legacy_jar)],cwd=d,check=True)

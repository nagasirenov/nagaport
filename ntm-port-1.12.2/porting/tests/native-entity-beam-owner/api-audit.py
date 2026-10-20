#!/usr/bin/env python3
"""Pin Forge1614 cached-name contract and genuine native getDisplayNameString bytecode."""
from pathlib import Path
import zipfile,io,hashlib,re,subprocess,os,json,sys
folder=Path(__file__).resolve().parent;java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin';cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip();old=Path('/tmp/nagaport-forge1614-userdev.jar')
def sha(b):return hashlib.sha256(b).hexdigest()
with zipfile.ZipFile(old) as z:
 data=zipfile.ZipFile(io.BytesIO(z.read('forgepatches.zip'))).read('net/minecraft/entity/player/EntityPlayer.java.patch')
 text=data.decode();start=text.index('+    public String getDisplayName()');end=text.index('+    /**',start);fragment=text[start:end]
assert 'this.displayname = ForgeEventFactory.getPlayerDisplayName(this, this.func_70005_c_());' in fragment and 'return this.displayname;' in fragment
native=subprocess.check_output([str(java/'javap'),'-c','-p','-cp',cp,'net.minecraft.entity.player.EntityPlayer']).decode();start=native.index('  public java.lang.String getDisplayNameString();');end=native.index('  public void refreshDisplayName();',start);native=native[start:end]
assert 'Field displayname:Ljava/lang/String;' in native and 'Method getName:()Ljava/lang/String;' in native and 'ForgeEventFactory.getPlayerDisplayName:' in native and 'areturn' in native
name='net/minecraft/entity/player/EntityPlayer.class';classes=[]
for p in cp.split(os.pathsep):
 if Path(p).is_file() and zipfile.is_zipfile(p):
  with zipfile.ZipFile(p) as z:
   if name in z.namelist():classes.append({'jar':str(Path(p).resolve()),'class_sha256':sha(z.read(name))})
assert classes
result={'legacy_userdev_sha256':sha(old.read_bytes()),'legacy_player_patch_sha256':sha(data),'legacy_string_method_patch':fragment,'native_class':classes[0],'native_string_method_javap':native,'mapping':'Forge getDisplayName String -> native getDisplayNameString String; cached field and Forge display-name event preserved, not username or decorated chat component.'}
file=folder/'api-evidence.json'
if '--record-evidence' in sys.argv:file.write_text(json.dumps(result,indent=2)+'\n')
assert result==json.loads(file.read_text());print('PASS original Forge cached display-name contract and actual native STRING method bytecode')

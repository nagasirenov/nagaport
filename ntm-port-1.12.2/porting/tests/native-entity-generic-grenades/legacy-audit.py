#!/usr/bin/env python3
"""Execute genuine vanilla1.7 item lookup; verify Forge1614 null-default registry source contract."""
from pathlib import Path
import zipfile,io,hashlib,json,sys,re,subprocess,tempfile,os
folder=Path(__file__).resolve().parent;legacy=Path('/tmp/nagaport-legacy-client.jar');forge=Path('/tmp/nagaport-forge1614-userdev.jar');srg=Path('/tmp/nagaport1710-srg.zip');cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
def sha(b):return hashlib.sha256(b).hexdigest()
def fragment(s,pattern):
 m=re.search(pattern,s);assert m,pattern
 start=m.start();end=s.index('{',m.end()-1)+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[start:end]
with zipfile.ZipFile(forge) as z:
 reg=z.read('src/main/java/cpw/mods/fml/common/registry/FMLControlledNamespacedRegistry.java');game=z.read('src/main/java/cpw/mods/fml/common/registry/GameData.java');fmlpatch=zipfile.ZipFile(io.BytesIO(z.read('fmlpatches.zip'))).read('net/minecraft/item/Item.java.patch');forgepatch=zipfile.ZipFile(io.BytesIO(z.read('forgepatches.zip'))).read('net/minecraft/item/Item.java.patch')
regmethod=fragment(reg.decode(),r'public I func_148754_a\(int id\)\s*\{');constructor=fragment(game.decode(),r'private GameData\(\)\s*\{')
assert 'I object = getRaw(id);' in regmethod and 'object == null ? this.optionalDefaultObject : object' in regmethod
assert 'new FMLControlledNamespacedRegistry<Item>(null, MAX_ITEM_ID, MIN_ITEM_ID' in constructor
assert 'field_150901_e = GameData.getItemRegistry();' in fmlpatch.decode();assert 'func_150899_d' not in forgepatch.decode()
with zipfile.ZipFile(srg) as z:
 lines=[l for l in z.read('joined.srg').decode().splitlines() if l in ['CL: adb net/minecraft/item/Item','CL: kl net/minecraft/init/Bootstrap'] or 'net/minecraft/item/Item/func_150899_d' in l or 'net/minecraft/item/Item/func_150891_b' in l or 'net/minecraft/init/Bootstrap/func_151354_b' in l]
assert len(lines)==5
result={'legacy_jar_sha256':sha(legacy.read_bytes()),'forge_userdev_sha256':sha(forge.read_bytes()),'srg_sha256':sha(srg.read_bytes()),'mapping':lines,'forge_registry_source_sha256':sha(reg),'forge_game_data_source_sha256':sha(game),'fml_item_patch_sha256':sha(fmlpatch),'forge_item_patch_sha256':sha(forgepatch),'forge_by_id_method':regmethod,'forge_game_data_constructor':constructor,'native_mapping':'Keep raw saved/synchronized ID; only zero becomes null before original generic-grenade cast/fallback. Native ItemAir at zero is a version representation difference, not a grenade payload.'}
file=folder/'legacy-evidence.json'
if '--record-evidence' in sys.argv:file.write_text(json.dumps(result,indent=2)+'\n')
assert result==json.loads(file.read_text())
with tempfile.TemporaryDirectory(prefix='legacy-grenade-ids-') as d:
 subprocess.run([str(java/'javac'),'-encoding','UTF-8','-d',d,str(folder/'LegacyItemIds.java')],check=True)
 subprocess.run([str(java/'java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+str(legacy)+os.pathsep+cp,'oracle.LegacyItemIds',str(legacy)],cwd=d,check=True)
print('PASS pinned Forge1614 null-default item registry contract; no Forge1.7 loaded-world/runtime claim')

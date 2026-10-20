#!/usr/bin/env python3
from pathlib import Path
import json,hashlib,subprocess,zipfile
H=Path(__file__).resolve().parent;R=H.parents[2];sha=lambda b:hashlib.sha256(b).hexdigest()
ct=json.loads((H.parent/'ct-native/freeze-manifest.json').read_text())['production'];generic=json.loads((H.parent/'generic-metadata-native/source-manifest.json').read_text())['complete_production_sources'];checked={}
for name in ['blocks/generic/BlockNTMGlass.java','blocks/generic/BlockNTMGlassCT.java','render/block/ct/CT.java','render/block/ct/CTContext.java','render/block/ct/IBlockCT.java','main/CTModelHandler.java','render/block/RenderBlockCT.java']:
 p='src/main/java/com/hbm/'+name;assert sha((R/p).read_bytes())==ct['ntm-port-1.12.2/'+p];checked[p]=ct['ntm-port-1.12.2/'+p]
for p in ['src/main/java/com/hbm/blocks/BlockBase.java','src/main/java/com/hbm/blocks/generic/BlockGeneric.java']:
 assert sha((R/p).read_bytes())==generic[p];checked[p]=generic[p]
# Compare actual old scalar gameplay hooks, not native vanilla fallback.
original=subprocess.check_output(['git','show','dff345a:ntm-original-pristine/src/main/java/com/hbm/blocks/generic/BlockNTMGlass.java'],cwd=R).decode();native=(R/'src/main/java/com/hbm/blocks/generic/BlockNTMGlass.java').read_text()
for method in ['public int quantityDropped(Random rand)','protected boolean canSilkHarvest()']:
 def body(s):a=s.index(method);return s[a:s.index('}',a)+1]
 assert body(original)==body(native)
assert 'this(layer, name, material, false);' in original and 'this(layer, name, material, false);' in native
for name in ['reinforced_glass.png','reinforced_glass_ct.png','reinforced_brick.png']:
 p='src/main/resources/assets/hbm/textures/blocks/'+name;current=subprocess.check_output(['git','show','HEAD:ntm-port-1.12.2/'+p],cwd=R);old=subprocess.check_output(['git','show','dff345a:ntm-original-pristine/'+p],cwd=R);assert current==old;checked[p]=sha(current)
logs=['ct-native/owners-proof.log','ct-native/integrated-owners-proof.log','ct-native/geometry-proof.log','generic-metadata-native/native-test.log','structure-palette-native/native-proof.log']
for log in logs:
 p=H.parent/log;assert p.is_file();checked[str(p.relative_to(R))]=sha(p.read_bytes())
(H/'reused-owner-hashes.json').write_text(json.dumps(checked,indent=2)+'\n')
print('PASS nine complete owner/render source hashes match prior CT49/Generic native proofs; original glass drop/silk method bodies and false constructor unchanged; three original textures identical. Prior proof logs pinned, not rerun; inherited visual gate retained.')

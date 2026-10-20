#!/usr/bin/env python3
"""Exact frozen source/assets, original provenance, retired-root and lifecycle guards."""
import hashlib,json,pathlib,re
root=pathlib.Path(__file__).resolve().parents[2]
scope=json.loads((root/'porting/tests/native-remaining-items-production-scope.json').read_text())
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
for path,sha in scope['production'].items():assert digest(root/path)==sha,('production drift',path)
for path in scope['deleted']:assert not (root/path).exists(),('retired path returned',path)
pristine=root.parent/'ntm-original-pristine'
for path,sha in scope['original_sources'].items():assert digest(pristine/path)==sha,('pristine oracle drift',path)
original={p.relative_to(pristine).as_posix().lower():p for p in (pristine/'src/main/resources').rglob('*') if p.is_file()}
for path,sha in scope['assets_byte_identical_to_pristine'].items():
 assert digest(root/path)==sha and digest(original[path.lower()])==sha,('original asset drift',path)
for path in scope['production']:
 if not path.endswith('.java'):continue
 source=(root/path).read_text()
 assert 'import net.minecraftforge.client.IItemRenderer;' not in source,('legacy renderer shim returned',path)
for f in (root/'src').rglob('*'):
 if f.is_file() and f.suffix in ('.java','.json'):
  assert 'ItemRenderWeaponMiniFEL' not in f.read_text(),('retired renderer has a live reference',f)
client=(root/'src/main/java/com/hbm/main/ClientProxy.java').read_text()
for stem in ['ingot_steel_dusted','ingot_chainsteel','ingot_meteorite','ingot_meteorite_forged','blade_meteorite']:
 assert f'contextRenderers.put(ModItems.{stem}, new ItemRendererHot("{stem}"));' in client
assert client.index('initializeNativeMissileParts();') < client.index('for(MissilePart part : MissilePart.parts.values())') < client.index('nativeContextItems = new ')
helper=client[client.index('private void initializeNativeMissileParts()'):client.index('//mk3, only use this one')]
assert helper.index('if(nativeMissilePartsInitialized) return;') < helper.index('MissilePart.registerAllParts();') < helper.index('nativeMissilePartsInitialized = true;')
assert helper.count('MissilePart.registerAllParts();')==1 and 'public void registerMissileItems()' in helper and helper.count('initializeNativeMissileParts();')==1
assert 'if(entry.getValue() instanceof com.hbm.render.item.nativeitem.context.NativeOriginalItemRenderer)' in client
assert 'if(block == null || !block.handlesDropped(entity.getItem()))' in (root/'src/main/java/com/hbm/render/item/nativeitem/staticfamily/SingleMeshEntityItemRenderer.java').read_text()
print('PASS',len(scope['production']),'exact production files,',len(scope['deleted']),'retired paths,',len(scope['original_sources']),'pristine source roots,',len(scope['assets_byte_identical_to_pristine']),'original assets; native lifecycle, complete fallback and no legacy renderer guards.')

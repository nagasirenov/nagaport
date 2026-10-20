#!/usr/bin/env python3
"""Exact source inverse, native destination, complete owner/material/resource closure."""
import argparse,csv,hashlib,json,re,subprocess
from pathlib import Path
from PIL import Image
import migrate
import dependency_composition as dependency
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
p=argparse.ArgumentParser();p.add_argument('--record',action='store_true');a=p.parse_args();m=migrate.verify();hashes={};resources={};materials={}
# Independent javac destinations conserve each entire receiver and string expression.
after=list(csv.DictReader((HERE/'after.tsv').open(),delimiter='\t'));native=[r for r in after if r['method']=='texture'];legacy=[r for r in after if r['method']=='setTextureName'];assert len(native)==106 and len(legacy)==23
for old,new in zip(m['selected'],native):
 assert old['path']==new['path']
 assert new['method_element_kind']=='METHOD' and new['method_owner']=='com.hbm.render.item.NativeItemTextureRegistry' and new['method_signature']=='<T>texture(T,java.lang.String)'
 assert new['argument_count']=='2' and new['arguments_kind']=='DECLARED || DECLARED'
 assert new['arguments_type']==old['receiver_type']+' || java.lang.String'
 assert new['arguments_source']==old['receiver_source']+' || '+old['arguments_source']
for old,new in zip(m['excluded'],legacy):assert old['full_invocation']==new['full_invocation'] and old['receiver_type']==new['receiver_type']
# Every owner and transitive ancestor, plus all additional ArmorFSB descendants affected by its constructor.
owners={r.split('\t')[0]:r.split('\t') for r in (HERE/'owners.tsv').read_text().splitlines()};assert len(owners)==29
for name,row in owners.items():
 assert row[2]=='true' and row[4]=='',row
 rel='src/main/java/'+name.replace('.','/')+'.java';data=(ROOT/rel).read_bytes();hashes[rel]=migrate.digest(data)
 old=migrate.baseline(rel)
 if rel not in migrate.FILES:assert data==old.encode(),('Owner changed',rel)
 # The only possible historical specialized icon owner is vanilla ItemArmor (audited separately).
 pristine=subprocess.check_output(['git','show',migrate.BASE+':ntm-original-pristine/'+rel],cwd=REPO).decode()
 forbidden=['registerIcons','getIcon','getIconFromDamage','getIconFromDamageForRenderPass','requiresMultipleRenderPasses','getRenderPasses','getColorFromItemStack','setTextureName','getIconString']
 for method in forbidden:
  assert not re.search(r'\b(?:public|protected)\s+(?:[\w<>\[\]]+\s+)+'+method+r'\s*\(',old),(name,method)
  assert not re.search(r'\b(?:public|protected)\s+(?:[\w<>\[\]]+\s+)+'+method+r'\s*\(',pristine),(name,method,'pristine')
 if row[3]=='true' and name.split('.')[-1]!='ArmorBJJetpack':assert re.search(r'public Item getItemForRenderer\(\) \{ return this; \}',old)
# Validate source damageability inputs to the native inventory fixture. Custom no-durability owners
# retain their metadata-independent CallbackItemRegistration mesh; plain no9 is excluded.
for name in ('ArmorNo9','ArmorTaurun','ArmorTrenchmaster'):
 assert 'this.setMaxDamage(0);' in (ROOT/('src/main/java/com/hbm/items/armor/'+name+'.java')).read_text()
for name in owners:
 text=(ROOT/('src/main/java/'+name.replace('.','/')+'.java')).read_text()
 assert not re.search(r'\b(public|protected)\s+\w+\s+(isDamageable|getMetadata|getHasSubtypes|isFull3D)\s*\(',text),name
 assert 'setHasSubtypes(' not in text and 'setFull3D(' not in text,name
# All selected material expressions are exactly IRON or user-defined enum identities, never leather.
main=(ROOT/'src/main/java/com/hbm/main/MainRegistry.java').read_text();armor=migrate.baseline(migrate.FILES[0])
for row in m['selected']:
 if row['field'] is None:continue
 receiver=migrate.raw(row['receiver_source']);owner=row['owner']
 if owner=='ArmorGasMask':material='ArmorMaterial.IRON';assert 'super(ArmorMaterial.IRON, 0, 0);' in (ROOT/'src/main/java/com/hbm/items/armor/ArmorGasMask.java').read_text()
 else:material=re.match(r'new \w+\(([^,]+),',receiver)[1]
 if material not in ('ArmorMaterial.IRON','ArmorMaterial.CHAIN'):
  src=main if material.startswith('MainRegistry.') else armor;name=material.split('.')[-1]
  declaration=re.search(r'ArmorMaterial '+name+r' = EnumHelper.addArmorMaterial\([^;]+;',src);assert declaration,(owner,material)
  materials[material]=declaration.group()
 else:materials[material]='Native non-leather '+material+' identity.'
# Texture bytes and PNG metadata match original pristine resources. No registry-name inference.
for texture in sorted({r['texture'] for r in m['selected']}):
 rel='src/main/resources/assets/hbm/textures/items/'+texture.split(':')[1]+'.png';data=(ROOT/rel).read_bytes()
 pristine=subprocess.check_output(['git','show',migrate.BASE+':ntm-original-pristine/'+rel],cwd=REPO);assert data==pristine
 with Image.open(ROOT/rel) as image:image.load();size=image.size
 resources[texture]={'path':rel,'sha256':migrate.digest(data),'size':size}
 meta=Path(str(ROOT/rel)+'.mcmeta');assert not meta.exists(),('Unreviewed animation',rel)
# Historical dependency guards remain exact. Only the independently proven one-branch
# metadata fix may be inverted, after checking its exact fixed hash and five drift controls.
dependency.verify_receipt()
# All other dispatch/ownership pipeline sources must remain untouched and client-registered.
protected=['main/ClientProxy.java','main/MainRegistry.java','items/ModItems.java','main/LegacyBaseModelHandler.java','render/tileentity/IItemRendererProvider.java','render/item/NativeItemTextureRegistry.java','render/item/NativeItemTextureModelHandler.java','render/item/nativeitem/callback/CallbackItemRegistration.java','render/item/nativeitem/callback/CallbackItemModel.java']
for name in protected:
 rel='src/main/java/com/hbm/'+name;data=(ROOT/rel).read_bytes()
 if rel==dependency.HANDLER:data=dependency.restore_cp82(data)
 assert data==migrate.baseline(rel).encode(),rel;hashes[rel]=migrate.digest(data)
client=(ROOT/'src/main/java/com/hbm/main/ClientProxy.java').read_text();assert client.count('new com.hbm.render.item.NativeItemTextureModelHandler()')==1
assert 'for(Item item : net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.getValuesCollection())' in client
assert 'for(Item owned : provider.getItemsForRenderer()) nativeCallbacks.put(owned, provider.getRenderer());' in client
assert 'callbacks.register(event);' in client and 'MinecraftForge.EVENT_BUS.register(callbacks);' in client
interface=(ROOT/'src/main/java/com/hbm/render/tileentity/IItemRendererProvider.java').read_text();assert 'return new Item[] { this.getItemForRenderer() };' in interface
# Full source inverse rejects even one-byte unrelated/mutation drift.
for rel,entry in m['files'].items():
 current=(ROOT/rel).read_text();assert migrate.digest(current)==entry['after_sha256']
 for changed in (current+' ',current[:-1],current.replace('hbm:', 'wrong:',1) if 'hbm:' in current else current.replace(':armor',':wrong',1)):
  assert changed!=current and migrate.digest(changed)!=entry['after_sha256']
report={'baseline':migrate.BASE,'owner_and_dispatch_hashes':hashes,'materials':materials,'resources':resources,'closure':list(owners.values()),'mapped_calls':106,'excluded_calls':23}
# Serialize tuple image dimensions through JSON for stable comparison.
report=json.loads(json.dumps(report))
if a.record:(HERE/'closure-audit.json').write_text(json.dumps(report,indent=2)+'\n')
assert report==json.loads((HERE/'closure-audit.json').read_text())
print('PASS 106 exact before/after native destinations; 23 excluded full chains unchanged; 29 compiler-resolved owner/ancestor classes; '+str(len(materials))+' non-leather material identities; '+str(len(resources))+' pristine original PNGs; protected historical registry/renderer/worn-model sources unchanged after strict known dependency inversion.')

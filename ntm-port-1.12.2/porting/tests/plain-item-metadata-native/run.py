#!/usr/bin/env python3
"""Finite offline native metadata regression, CP82 negative control, and original bytecode oracle."""
import argparse,ast,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
BASE='df18fc077648dd18e6d0c1055d82c11dc2ebdb78'
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True)
p.add_argument('--support-root',type=Path,default=ROOT,help='Unchanged CP82 source/resources for an isolated sparse checkout')
p.add_argument('--legacy-jar',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--mapping-zip',type=Path,default=Path('/workspace/shared/nagaport-toolchain/gradle-user-home/caches/modules-2/files-2.1/de.oceanlabs.mcp/mcp/1.7.10/10b95bb342f55d681025bf2d698c893d9c4b4201/mcp-1.7.10-srg.zip'))
p.add_argument('--baseline',action='store_true');a=p.parse_args();support=a.support_root.resolve();cp=a.classpath_file.read_text().strip()
def source(rel):
 own=ROOT/rel
 return own if own.exists() else support/rel
def old(rel):return subprocess.check_output(['git','show',BASE+':ntm-port-1.12.2/'+rel],cwd=REPO,text=True)
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
handler='src/main/java/com/hbm/render/item/NativeItemTextureModelHandler.java'
before='''                // The original default Item icon is metadata-insensitive. A
                // plain subtype item still uses that same image for every meta.
                if(item.getHasSubtypes()) ModelLoader.setCustomMeshDefinition(item, stack -> location);
                else ModelLoader.setCustomModelResourceLocation(item, 0, location);'''
after='''                // The original default Item icon is metadata-insensitive even
                // for non-damageable items that do not declare subtypes.
                ModelLoader.setCustomMeshDefinition(item, stack -> location);'''
assert old(handler).count(before)==1 and source(handler).read_text()==old(handler).replace(before,after),'Production change exceeded exact registration branch'
moditems=old('src/main/java/com/hbm/items/ModItems.java');drop=old('src/main/java/com/hbm/items/special/ItemDrop.java')
assert re.search(r'public class ItemDrop extends Item\s*\{',drop)
for name in ['ItemDrop','setMaxDamage','setHasSubtypes','getMaxDamage','getMetadata','registerIcons','getIcon','getIconFromDamage','getIconFromDamageForRenderPass','requiresMultipleRenderPasses','getRenderPasses','getColorFromItemStack']:
 assert not re.search(r'\b'+name+r'\s*\(',drop),name
pattern=r'(?m)^\s*(\w+) = NativeItemTextureRegistry\.texture\(new ItemDrop\(\)([^\n]+), RefStrings\.MODID \+ "(:[^"\n]+)"\);'
owners=[]
for match in re.finditer(pattern,moditems):
 name,receiver,texture=match.groups();assert not re.search(r'\.(?:setMaxDamage|setHasSubtypes)\(',receiver)
 translation=re.search(r'\.setTranslationKey\("([^"\n]+)"\)',receiver)[1]
 registration=re.findall(r'(?m)^\s*GameRegistry\.registerItem\('+name+r', '+name+r'\.getTranslationKey\(\)\);',moditems);assert len(registration)==1
 # No named existing render owner can supersede the simple default route.
 references=[]
 for f in (support/'src/main/java/com/hbm/render').rglob('*.java'):
  if re.search(r'\bModItems\.'+name+r'\b',f.read_text()):references.append(str(f.relative_to(support)))
 assert not references,(name,references)
 owners.append({'field':name,'translation':translation,'texture':'hbm'+texture,'full3d':'.setFull3D()' in receiver,'declaration':match[0].strip(),'registration':registration[0].strip()})
expected=['cell_antimatter','cell_anti_schrabidium','singularity','singularity_counter_resonant','singularity_super_heated','black_hole','singularity_spark','pellet_antimatter','crystal_xen','detonator_deadman','detonator_de']
assert [v['field'] for v in owners]==expected
(HERE/'reachable-owners.json').write_text(json.dumps(owners,indent=2)+'\n')
with zipfile.ZipFile(a.mapping_zip) as z:mapping=z.read('joined.srg').decode()
required=['CL: adb net/minecraft/item/Item','CL: rf net/minecraft/util/IIcon','CL: rg net/minecraft/client/renderer/texture/IIconRegister','FD: adb/l net/minecraft/item/Item/field_111218_cA','FD: adb/k net/minecraft/item/Item/field_77791_bV','MD: adb/b_ (I)Lrf; net/minecraft/item/Item/func_77617_a (I)Lnet/minecraft/util/IIcon;','MD: adb/a (II)Lrf; net/minecraft/item/Item/func_77618_c (II)Lnet/minecraft/util/IIcon;','MD: adb/a (Lrg;)V net/minecraft/item/Item/func_94581_a (Lnet/minecraft/client/renderer/texture/IIconRegister;)V','MD: adb/f (Ljava/lang/String;)Ladb; net/minecraft/item/Item/func_111206_d (Ljava/lang/String;)Lnet/minecraft/item/Item;']
for line in required:assert line in mapping.splitlines(),line
assert sha(a.legacy_jar)=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
(HERE/'original-item-mapping.txt').write_text('\n'.join(required)+'\n')
vanilla=next(Path(x) for x in cp.split(os.pathsep) if 'forgeBin-' in x and x.endswith('.jar'))
names=ast.literal_eval(re.search(r'names=(\[.*?\])\n',source('porting/tests/item-foundation-native/run.py').read_text())[1])
production=[source('src/main/java/com/hbm/'+name) for name in names]
for f in production:
 rel=str(f.relative_to(ROOT)) if f.is_relative_to(ROOT) else str(f.relative_to(support))
 if rel!=handler:assert f.read_text()==old(rel),'Non-CP82 support source '+rel
with tempfile.TemporaryDirectory(prefix='plain-item-metadata-native-') as tmp:
 t=Path(tmp);jar=t/'item-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for f in (support/'src/main/resources/assets/hbm/textures/items').glob('*.png'):z.write(f,f.relative_to(support/'src/main/resources'))
 foundation=source('porting/tests/item-foundation-native/NativeItemFoundationTest.java')
 if a.baseline:
  baseline=t/'com/hbm/render/item/NativeItemTextureModelHandler.java';baseline.parent.mkdir(parents=True);baseline.write_text(old(handler));production=[baseline if f==source(handler) else f for f in production]
  foundation=t/'NativeItemFoundationTest.java';foundation.write_text(old('porting/tests/item-foundation-native/NativeItemFoundationTest.java'))
 files=production+[foundation,source('porting/tests/ForgeEventNativeLauncher.java'),HERE/'NativeDefaultItemMetadataTest.java',HERE/'OriginalDefaultIcons.java']
 inputs={str(f.relative_to(ROOT)) if f.is_relative_to(ROOT) else str(f.relative_to(support)) if f.is_relative_to(support) else f.name:sha(f) for f in files}
 inputs.update({'legacy_jar':sha(a.legacy_jar),'mapping_zip':sha(a.mapping_zip),'native_forge_jar':sha(vanilla),'baseline':a.baseline})
 (HERE/('baseline-inputs.json' if a.baseline else 'runtime-inputs.json')).write_text(json.dumps(inputs,indent=2)+'\n')
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(t),'-cp',cp,'-d',str(t)]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx1000m','-Djava.awt.headless=true','-cp',str(t)+os.pathsep+cp,'ForgeEventNativeLauncher','NativeDefaultItemMetadataTest',str(support/'src/main/resources'),str(jar),str(vanilla),str(HERE/'reachable-owners.json'),str(a.legacy_jar)],cwd=t,check=True)
 if not a.baseline:
  subprocess.run([str(a.java_home/'bin/java'),'-Xmx1000m','-Djava.awt.headless=true','-cp',str(t)+os.pathsep+cp,'ForgeEventNativeLauncher','NativeItemFoundationTest',str(support/'src/main/resources'),str(jar),str(vanilla)],cwd=t,check=True)
print('PASS exact one-branch production delta; 11 current registered ItemDrop owners and absence of named render-route owners; original SRG bytecode identities verified. No full-mod build.')

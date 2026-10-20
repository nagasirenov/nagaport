#!/usr/bin/env python3
"""Actual Forge native inventory routing; honest receiver/constructor-tail fixtures, no platform stubs."""
import argparse,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
import migrate
import dependency_composition as dependency
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2]
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-jar',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--evidence-prefix',default='');a=p.parse_args()
assert re.fullmatch(r'[A-Za-z0-9_-]*',a.evidence_prefix)
dependency.verify_receipt()
handler=(ROOT/dependency.HANDLER).read_bytes();dependency.restore_cp82(handler)
assert dependency.sha(handler)==dependency.FIXED_SHA,'Current native runtime probe requires the exact companion metadata fix; historical standalone evidence remains in native.log.'
assert dependency.sha((ROOT/'porting/tests/item-foundation-native/NativeItemFoundationTest.java').read_bytes())==dependency.FOUNDATION_SHA,'Requires the exact corrected companion foundation fixture.'
manifest=migrate.verify();cp=a.classpath_file.read_text().strip();vanilla=next(Path(x) for x in cp.split(os.pathsep) if 'forgeBin-' in x and x.endswith('.jar'))
owners={r.split('\t')[0].split('.')[-1]:r.rstrip('\n').split('\t') for r in (HERE/'owners.tsv').read_text().splitlines(keepends=True)}
moditems=(ROOT/'src/main/java/com/hbm/items/ModItems.java').read_text();items=[];registrations={}
for row in manifest['selected']:
 if row['field'] is None:continue
 field=row['field'];receiver=migrate.raw(row['receiver_source']);translation=re.search(r'\.setTranslationKey\("([^"]+)"\)',receiver)[1]
 item={'field':field,'owner':row['owner'],'translation':translation,'texture':row['texture'],'atlas':'hbm:items/'+row['texture'].split(':')[1],'callback':owners[row['owner']][3]=='true','damageable':row['owner'] not in ('ArmorNo9','ArmorTaurun','ArmorTrenchmaster')}
 items.append(item)
 matches=list(re.finditer(r'(?m)^\s*GameRegistry\.registerItem\('+re.escape(field)+r', [^;]+;',moditems));assert len(matches)==1,field
 registrations[field]={'statement':matches[0].group().strip(),'offset':matches[0].start()}
 for_owner=owners[row['owner']];assert for_owner[2]=='true' and not for_owner[4],for_owner
order=sorted(registrations,key=lambda n:registrations[n]['offset'])
scope={'boundary':'Real ItemArmor / Item native receivers, not actual NTM constructors. Exact production registration statements and ArmorFSB constructor tail. Inventory-only runtime proof.','items':items,'registration_order':order,'registration_statements':registrations}
(HERE/(a.evidence_prefix+'native-scope.json')).write_text(json.dumps(scope,indent=2)+'\n')
# Reuse complete unchanged production inventory foundations and their native control suite.
foundation=ROOT/'porting/tests/item-foundation-native';text=(foundation/'run.py').read_text();names=eval(re.search(r'names=(\[.*?\])\n',text)[1])
production=[ROOT/'src/main/java/com/hbm'/n for n in names]+[ROOT/'src/main/java/com/hbm/registry/GameRegistry.java']
with tempfile.TemporaryDirectory(prefix='armor-inventory-native-') as temp:
 t=Path(temp);jar=t/'armor-items.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for f in (ROOT/'src/main/resources/assets/hbm/textures/items').glob('*.png'):z.write(f,f.relative_to(ROOT/'src/main/resources'))
 registration='import java.util.Map;import net.minecraft.item.Item;import com.hbm.registry.GameRegistry;\nfinal class ArmorRegistrationSlice {static void register(Map<String,Item> fields) {\n'
 for name in order:registration+='Item '+name+'=fields.get("'+name+'");\n'
 for name in order:registration+=registrations[name]['statement']+'\n'
 registration+='}}\n';(t/'ArmorRegistrationSlice.java').write_text(registration)
 actual=(ROOT/'src/main/java/com/hbm/items/armor/ArmorFSB.java').read_text();tail='\t\tthis.texture = texture;\n\t\tNativeItemTextureRegistry.texture(this, RefStrings.MODID + ":armor");'
 assert tail in actual
 test=(HERE/'NativeArmorInventoryTextureTest.java').read_text();assert test.count('// EXACT_CONSTRUCTOR_TAIL')==1
 test=test.replace('// EXACT_CONSTRUCTOR_TAIL',tail).replace('import com.hbm.registry.GameRegistry;','import com.hbm.registry.GameRegistry;\nimport com.hbm.lib.RefStrings;')
 (t/'NativeArmorInventoryTextureTest.java').write_text(test)
 files=production+[foundation/'NativeItemFoundationTest.java',ROOT/'porting/tests/ForgeEventNativeLauncher.java',HERE/'OriginalArmorIcons.java',t/'NativeArmorInventoryTextureTest.java',t/'ArmorRegistrationSlice.java']
 inputs={str(f.relative_to(ROOT)) if str(f).startswith(str(ROOT)) else f.name:hashlib.sha256(f.read_bytes()).hexdigest() for f in files}
 inputs['legacy_jar']=hashlib.sha256(a.legacy_jar.read_bytes()).hexdigest();assert inputs['legacy_jar']=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
 (HERE/(a.evidence_prefix+'runtime-inputs.json')).write_text(json.dumps(inputs,indent=2)+'\n')
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(t),'-cp',cp,'-d',str(t)]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx1000m','-Djava.awt.headless=true','-cp',str(t)+os.pathsep+cp,'ForgeEventNativeLauncher','NativeArmorInventoryTextureTest',str(ROOT/'src/main/resources'),str(jar),str(vanilla),str(HERE/(a.evidence_prefix+'native-scope.json')),str(a.legacy_jar)],cwd=t,check=True)

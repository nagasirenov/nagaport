#!/usr/bin/env python3
"""Compile complete native achievement backend/events/dispatcher against genuine Forge.
No Git/network/runtime downloads. The registry tables/MainRegistry and unused creative-tab enumeration are controlled NTM boundaries.
Full production RecipesCommon is compiled unchanged.
"""
import argparse, hashlib, json, os, re, shutil, subprocess, tempfile, zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath',required=True)
a=p.parse_args(); exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).exists(): a.java_home=a.java_home.parent
baseline=json.loads((ROOT/'porting/tests/achievement-definition-baseline.json').read_text())
assert len(baseline)==70 and len([r for r in baseline if r['field']])==63
assert sum(r['parent'] is not None for r in baseline if r['field'])==42
assets=ROOT/'src/main/resources/assets/hbm/advancements'
assert len(list(assets.rglob('*.json')))==71
for r in baseline:
 j=json.loads((assets/(r['id'].split(':')[1]+'.json')).read_text())
 assert j['parent']==(r['parent'] or 'hbm:ntm/root')
 assert j['display']['icon']=={'item':r['icon'],'data':r['meta']}
 assert j['display']['title']=={'translate':r['legacy_stat']}
 expected_description={'translate':r['legacy_stat']+'.desc'}
 if r['legacy']=='openInventory': expected_description['with']=[{'keybind':'key.inventory'}]
 assert j['display']['description']==expected_description
 assert j['display']['frame']==('challenge' if r['special'] else 'task')
 assert not j['display']['hidden'] and not j.get('rewards')
 assert j['criteria']=={'earned':{'trigger':'minecraft:impossible'}}
 assert j['requirements']==[['earned']]
language=json.loads((ROOT/'porting/tests/achievement-language-baseline.json').read_text())
english=dict(line.split('=',1) for line in (ROOT/'src/main/resources/assets/hbm/lang/en_US.lang').read_text().splitlines() if '=' in line and not line.startswith('#'))
assert len(language['values'])==14
assert all(english[key]==value for key,value in language['values'].items())
assert not (ROOT/'src/main/resources/pack.mcmeta').exists(), 'pack format migration requires adapter/path test update'
handler=(ROOT/'src/main/java/com/hbm/util/AchievementHandler.java').read_text()
entries=re.findall(r'craftingAchievements.put\(new ComparableStack\((ModItems|ModBlocks)\.(\w+)\), MainRegistry\.(\w+)\);',handler)
assert len(entries)==35
assert [list(e) for e in entries] == json.loads((ROOT/'porting/tests/achievement-crafting-baseline.json').read_text())
production=['src/main/java/com/hbm/advancement/'+n for n in ['AchievementDefinitions.java','NtmAdvancements.java','LegacyAchievementEvents.java','LegacyCraftingCallbacks.java']]+['src/main/java/com/hbm/util/AchievementHandler.java','src/main/java/com/hbm/inventory/RecipesCommon.java','src/main/java/com/hbm/inventory/SlotSmelting.java','src/main/java/com/hbm/packet/toserver/LegacyInventoryActionPacket.java']
with tempfile.TemporaryDirectory(prefix='hbm-native-achievements-') as tmp:
 t=Path(tmp); classes=t/'classes'; classes.mkdir()
 def write(name,s):
  f=t/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(s);return str(f)
 fixtures=[]
 for owner in ['ModItems','ModBlocks']:
  fields=sorted({f for o,f,_ in entries if o==owner}|({'nothing'} if owner=='ModItems' else set()))
  typ='net.minecraft.item.Item' if owner=='ModItems' else 'net.minecraft.block.Block'
  package='com.hbm.items' if owner=='ModItems' else 'com.hbm.blocks'
  fixtures.append(write(package.replace('.','/')+'/'+owner+'.java','package '+package+'; public final class '+owner+' {\n'+''.join('public static '+typ+' '+f+';\n' for f in fields)+'}\n'))
 fields=''.join('public static final net.minecraft.util.ResourceLocation '+r['field']+' = com.hbm.advancement.NtmAdvancements.id("'+r['legacy']+'");\n' for r in baseline if r['field'])
 fixtures.append(write('com/hbm/main/MainRegistry.java','package com.hbm.main; public final class MainRegistry { public static final CreativeBoundary proxy=new CreativeBoundary(); public static final class CreativeBoundary { public java.util.List<net.minecraft.item.ItemStack> getSubItems(net.minecraft.item.ItemStack stack) { throw new UnsupportedOperationException("Creative enumeration not part of achievement tests"); } } public static final org.apache.logging.log4j.Logger logger=org.apache.logging.log4j.LogManager.getLogger();\n'+fields+'}\n'))
 fixtures.append(write('com/hbm/config/GeneralConfig.java','package com.hbm.config; public final class GeneralConfig { public static boolean enableSilentCompStackErrors=false; }'))
 shutil.copytree(assets,t/'mod/assets/hbm/advancements')
 (t/'baseline.json').write_text(json.dumps(baseline))
 files=[]
 for path in production:
  f=ROOT/path; print('FULL PRODUCTION',path,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True);files.append(str(f))
 files += fixtures+[str(ROOT/'porting/tests'/n) for n in ['ForgeEventNativeLauncher.java','NativeAchievementsTest.java','InventoryActionPacketNativeTest.java','NativeAchievementLanguageTest.java']]
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',str(classes)]+files,check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',str(classes)+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NativeAchievementsTest',str(t/'mod'),str(t/'baseline.json')],check=True,cwd=t)

 # Preserve exact resource filenames and bytes in a test archive, not a release package.
 with zipfile.ZipFile(t/'language-fixture.jar','w') as archive:
  for f in (ROOT/'src/main/resources/assets/hbm/lang').glob('*.lang'):
   archive.write(f,'assets/hbm/lang/'+f.name)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',str(classes)+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NativeAchievementLanguageTest',str(ROOT/'src/main/resources'),str(t/'language-fixture.jar')],check=True,cwd=t)

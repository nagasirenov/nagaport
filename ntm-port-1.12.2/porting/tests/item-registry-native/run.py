#!/usr/bin/env python3
"""Compile exact native-only ModItems statement slices, full real queue/helper, then run Forge events.
This does not compile ModItems or arbitrary item constructors. Native Forge/Minecraft are never stubbed.
"""
import argparse, hashlib, json, os, pathlib, re, subprocess, tempfile
import migrate
HERE=pathlib.Path(__file__).resolve().parent
ROOT=HERE.parents[2]
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--classpath',required=True)
p.add_argument('--java-home',required=True,type=pathlib.Path)
p.add_argument('--texture-source',type=pathlib.Path,default=ROOT/'src/main/java/com/hbm/render/item/NativeItemTextureRegistry.java')
a=p.parse_args()
source=(migrate.ROOT/migrate.REL).read_bytes().decode()
baseline=subprocess.check_output(['git','show',migrate.BASE+':'+migrate.REL],cwd=migrate.ROOT).decode()
transformed,manifest=migrate.transform(baseline)
assert source==transformed
selected=[r for r in manifest['textures'] if r['class'] in ('Item','ItemFood','ItemSoup')]

def assignment(text,name,occurrence):
    matches=list(re.finditer(r'(?m)^\s*'+re.escape(name)+r'\s*=\s*(?:NativeItemTextureRegistry\.texture\()?new ',text))
    assert len(matches)>occurrence,name
    match=matches[occurrence]
    end=text.index(';',match.end())+1
    result=text[match.start():end].lstrip('\r\n')
    assert result.count(';')==1
    return result

def registration(text,name):
    matches=re.findall(r'(?m)^\s*GameRegistry\.registerItem\('+re.escape(name)+r', [^;]+;',text)
    assert len(matches)==1,(name,matches)
    return matches[0].lstrip('\r\n')

rows=[]; init=[]; regs=[]; occurrences={}; initialization=[]
for entry in selected:
    name=entry['item'];occurrence=occurrences.get(name,0);occurrences[name]=occurrence+1
    old=assignment(baseline,name,occurrence);new=assignment(source,name,occurrence)
    init.append(new); regs.append(registration(source,name))
    assert registration(baseline,name).replace('.getUnlocalizedName()', '.getTranslationKey()')==regs[-1]
    assert registration(baseline,name).strip()=='GameRegistry.registerItem(%s, %s.getUnlocalizedName());'%(name,name)
    uloc=re.search(r'\.setUnlocalizedName\("([^"\n]+)"\)',old).group(1)
    texture=re.search(r'\.setTextureName\(RefStrings.MODID\s*\+\s*"([^"\n]+)"\)',old).group(1)
    tab=re.search(r'\.setCreativeTab\(([^)]+)\)',old)
    maximum=re.search(r'\.setMaxStackSize\((\d+)\)',old)
    container=re.search(r'\.setContainerItem\(ModItems\.(\w+)\)',old)
    row={'field':name,'type':entry['class'],'translation':'item.'+uloc,'texture':'hbm'+texture,
         'registry':'hbm:item.'+uloc,'tab':tab.group(1) if tab else None,
         'maximum':int(maximum.group(1)) if maximum else (1 if entry['class']=='ItemSoup' else 64),
         'full3d':'.setFull3D()' in old,'container':container.group(1) if container else None,
         'original_sha256':hashlib.sha256(old.encode()).hexdigest(),
         'production_sha256':hashlib.sha256(new.encode()).hexdigest(),
         'registration_sha256':hashlib.sha256(regs[-1].encode()).hexdigest()}
    if entry['class']=='ItemFood':
        food=re.search(r'new ItemFood\((\d+), ([\d.]+)F, (true|false)\)',old)
        row['food']={'heal':int(food.group(1)),'saturation':float(food.group(2)),'wolf':food.group(3)=='true'}
    elif entry['class']=='ItemSoup':row['food']={'heal':int(re.search(r'new ItemSoup\((\d+)\)',old).group(1)),'saturation':.6,'wolf':False}
    rows.append(row)
    initialization.append({'field':name,'occurrence':occurrence,'original_sha256':row['original_sha256'],'production_sha256':row['production_sha256']})
# Retain every original construction, but register each final field only once, in actual registry-table order.
rows=list({row['field']:row for row in rows}.values())
regs=sorted({registration(source,row['field']) for row in rows},key=source.index)
registration_order=[re.search(r'GameRegistry\.registerItem\((\w+),',statement).group(1) for statement in regs]
# No missing container item may be silently replaced with a placeholder.
assert all(r['container'] is None or r['container'] in {x['field'] for x in rows} for r in rows)
assert len(init)==615 and len(rows)==613
scope={'boundary':'Exact ModItems assignment and registration statement slices, not whole ModItems.',
       'full_production':['com/hbm/registry/GameRegistry.java','com/hbm/lib/RefStrings.java','com/hbm/render/item/NativeItemTextureRegistry.java'],
       'fixtures':['MainRegistry: five distinct actual native CreativeTabs identities; no mod startup or custom tab behavior'],
       'items':rows,'construction_count':len(init),'initialization_statements':initialization,'registration_order':registration_order}
(HERE/'native-scope.json').write_text(migrate.encode_manifest(scope))
with tempfile.TemporaryDirectory(prefix='hbm-moditems-native-') as directory:
    tmp=pathlib.Path(directory);generated=tmp/'com/hbm/items/ModItems.java';generated.parent.mkdir(parents=True)
    generated.write_text('''package com.hbm.items;
import net.minecraft.item.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import com.hbm.main.MainRegistry;
import com.hbm.lib.RefStrings;
import com.hbm.registry.GameRegistry;
import com.hbm.render.item.NativeItemTextureRegistry;
public final class ModItems {
'''+''.join('public static Item '+r['field']+';\n' for r in rows)+'public static void initializeItem() {\n'+'\n'.join(init)+'\n}\npublic static void registerItem() {\n'+'\n'.join(regs)+'\n}\n}\n')
    fixture=tmp/'com/hbm/main/MainRegistry.java';fixture.parent.mkdir(parents=True)
    fixture.write_text('''package com.hbm.main;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
public final class MainRegistry {
 private static CreativeTabs tab(String name) { return new CreativeTabs(name) { public ItemStack createIcon() { return ItemStack.EMPTY; } }; }
 public static final CreativeTabs partsTab=tab("native_probe_parts"), controlTab=tab("native_probe_control"),
 consumableTab=tab("native_probe_consumable"), nukeTab=tab("native_probe_nuke"), weaponTab=tab("native_probe_weapon");
}
''')
    files=[ROOT/'src/main/java/com/hbm/registry/GameRegistry.java',ROOT/'src/main/java/com/hbm/lib/RefStrings.java',a.texture_source,
           ROOT/'porting/tests/ForgeEventNativeLauncher.java',HERE/'ModItemsNativeTest.java',generated,fixture]
    for file in files[:3]:print('FULL PRODUCTION',file.name,hashlib.sha256(file.read_bytes()).hexdigest(),flush=True)
    print('EXTRACTED PRODUCTION',len(init),'exact initialization statements and',len(rows),'exact registration statements; five native-tab fixtures only',flush=True)
    subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',str(tmp)]+list(map(str,files)),check=True)
    subprocess.run([str(a.java_home/'bin/java'),'-cp',str(tmp)+os.pathsep+a.classpath,'ForgeEventNativeLauncher','ModItemsNativeTest',str(HERE/'native-scope.json')],cwd=tmp,check=True)

#!/usr/bin/env python3
"""Bounded source-inverse, full-graph attribution, and exact-method native runtime proof."""
import argparse, hashlib, json, os, re, subprocess, tempfile
from pathlib import Path
H = Path(__file__).resolve().parent
ROOT = H.parents[2]
REPO = ROOT.parent
BASE = 'd5ef20a8f1cb65fbd06faa86a4901870c8bab3f2'
J = Path('/workspace/shared/nagaport-toolchain/java8')
CPFILE = Path('/tmp/nagaport-ae2-native-classpath.txt')
CP = CPFILE.read_text().strip()
OWNERS = ['ItemBlowtorch', 'ItemBoltgun', 'ItemModMinecart', 'ItemCraftingDegradation']
DESCENDANTS = ['ItemTooling', 'ItemToolingWeapon', 'ItemDefuser']
REL = 'src/main/java/com/hbm/items/'
FILES = [REL + 'tool/' + n + '.java' for n in OWNERS] + [REL + 'ModItems.java']
FAMILY = OWNERS + DESCENDANTS

def git(path, tree=BASE):
    return subprocess.check_output(['git', '-C', str(REPO), 'show', tree + ':ntm-port-1.12.2/' + path])

def original(path):
    return subprocess.check_output(['git', '-C', str(REPO), 'show', BASE + ':ntm-original-pristine/' + path])

def transform(path, data):
    if path.endswith('/ModItems.java'):
        lines = data.splitlines(keepends=True); count = 0
        for i, line in enumerate(lines):
            if any(('new ' + n + '(').encode() in line for n in FAMILY):
                assert line.count(b'setUnlocalizedName') == 1
                lines[i] = line.replace(b'setUnlocalizedName', b'setTranslationKey'); count += 1
        assert count == 12
        return b''.join(lines)
    assert data.count(b'setUnlocalizedName') == 2
    data = data.replace(b'setUnlocalizedName', b'setTranslationKey')
    assert data.count(b'this.setTextureName(RefStrings.MODID + ":"+ unlocalizedName);') == 1
    data = data.replace(b'this.setTextureName(RefStrings.MODID + ":"+ unlocalizedName);', b'com.hbm.render.item.NativeItemTextureRegistry.texture(this, RefStrings.MODID + ":"+ unlocalizedName);')
    if path.endswith('/ItemModMinecart.java'):
        assert data.count(b'this.getIconString()') == 2
        data = data.replace(b'this.getIconString()', b'com.hbm.render.item.NativeItemTextureRegistry.texture(this)')
    return data

def extract(text, start):
    begin = text.index(start); brace = text.index('{', begin); depth = 1; end = brace + 1
    while depth:
        if text[end] == '{': depth += 1
        if text[end] == '}': depth -= 1
        end += 1
    return text[begin:end]

def sha(data): return hashlib.sha256(data).hexdigest()

def audit():
    rows = []
    for path in FILES:
        before = git(path); after = (ROOT / path).read_bytes(); expected = transform(path, before)
        assert after == expected, 'Unexpected production edits: ' + path
        # Whole-file equality means every byte outside these substitutions is retained.
        rows.append({'path':path, 'before_sha256':sha(before), 'after_sha256':sha(after)})
    for name in OWNERS:
        path = REL + 'tool/' + name + '.java'
        assert extract(git(path).decode(), 'public Item setUnlocalizedName') == extract(original(path).decode(), 'public Item setUnlocalizedName')
    for name in DESCENDANTS:
        path = REL + 'tool/' + name + '.java'; assert (ROOT/path).read_bytes() == git(path)
        assert not re.search(r'\b(setTextureName|setUnlocalizedName|setTranslationKey|getIconString)\s*\(', (ROOT/path).read_text())
        rows.append({'path':path, 'unchanged_sha256':sha(git(path))})
    assets = ['defuser','screwdriver','screwdriver_desh','hand_drill','hand_drill_desh','wrench_archineer_hd','chemistry_set','chemistry_set_boron','blowtorch','acetylene_torch','boltgun']
    assets += ['cart.'+s for s in ['vanilla','wood','steel','painted']]
    assets += ['cart_overlay.'+s for s in ['empty','crate','destroyer','powder','semtex']]
    resources = []
    for name in assets:
        path = 'src/main/resources/assets/hbm/textures/items/' + name + '.png'
        data = (ROOT/path).read_bytes(); assert data == git(path) == original(path), path
        resources.append({'path':path,'sha256':sha(data)})
    for name in ['NativeItemTextureRegistry','NativeItemTextureModelHandler']:
        path='src/main/java/com/hbm/render/item/'+name+'.java'
        assert (ROOT/path).read_bytes() == git(path)
        rows.append({'path':path, 'unchanged_sha256':sha(git(path))})
    binary_paths=[Path('/tmp/nagaport-legacy-client.jar'),next(Path(s) for s in CP.split(os.pathsep) if 'forgeBin-' in s and s.endswith('.jar'))]
    binary_inputs=[{'path':str(p),'sha256':sha(p.read_bytes())} for p in binary_paths]
    manifest={'base':BASE, 'binary_inputs':binary_inputs, 'production_files':rows, 'original_resources':resources, 'scope':'4 setter owners, 3 inherited descendants, 12 callers, 2 layered-cart texture readers; method slices only'}
    (H/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
    print('PASS exact five-file source inverse; four setters byte-identical to pristine before substitution; three unchanged descendants; 20 byte-identical pristine PNGs; unchanged native texture/model helpers', flush=True)

def attribute(out):
    subprocess.run([str(J/'bin/javac'),'-cp',str(J/'lib/tools.jar'),'-d',str(out),str(H/'FamilyScanner.java')],check=True)
    source_files=sorted((ROOT/'src/main/java').rglob('*.java'))
    for side in ['before','after']:
        sources=[]
        for path in source_files:
            rel=str(path.relative_to(ROOT))
            if side=='before' and rel in FILES:
                tmp=out/'before'/rel; tmp.parent.mkdir(parents=True,exist_ok=True); tmp.write_bytes(git(rel)); sources.append(str(tmp))
            else: sources.append(str(path))
        listing=out/(side+'-sources.txt'); listing.write_text('\n'.join(sources)+'\n')
        with (H/(side+'.tsv')).open('w') as stdout, (H/(side+'.log')).open('w') as stderr:
            subprocess.run([str(J/'bin/java'),'-Xmx1800m','-cp',str(out)+os.pathsep+str(J/'lib/tools.jar'),'FamilyScanner',str(listing),str(CPFILE)],stdout=stdout,stderr=stderr,check=True)
    before=(H/'before.tsv').read_text().splitlines(); after=(H/'after.tsv').read_text().splitlines()
    assert [r for r in before if r.startswith('CLASS\t')] == [r for r in after if r.startswith('CLASS\t')]
    assert len([r for r in after if r.startswith('CLASS\t')]) == 7
    bc=[r for r in before if r.startswith('CALL\t') and 'ModItems.java\t' in r]
    ac=[r for r in after if r.startswith('CALL\t') and 'ModItems.java\t' in r]
    assert len(bc)==len(ac)==12 and [r.replace('setUnlocalizedName','setTranslationKey') for r in bc] == ac
    supers=[r for r in after if r.startswith('CALL\t') and '\tsuper\t' in r]
    assert len(supers)==4 and all('net.minecraft.item.Item#setTranslationKey(java.lang.String):net.minecraft.item.Item' in r for r in supers)
    assert len([r for r in after if r.startswith('CALL\t')]) == 16
    assert not any('setUnlocalizedName' in r for r in after)
    print('PASS full-source attribution: exact seven-class family, 12 same custom/inherited native setter destinations, 4 actual native Item super calls; all nonselected declarations stay outside scope',flush=True)

def runtime(out):
    sources=[]
    for name in FAMILY:
        production=(ROOT/(REL+'tool/'+name+'.java')).read_text()
        if name in OWNERS:
            members='@Override\n'+extract(production,'public Item setTranslationKey')
            parent='Item'
        else:
            members='';parent=re.search(r'public class '+name+r' extends (\w+)',production).group(1)
        if name=='ItemModMinecart':
            members += '\nprivate IIcon[] icons; private IIcon[] bases = new IIcon[4];\n'
            members += extract(production, 'public static enum EnumCartBase')+'\n'+extract(production,'public static enum EnumMinecart')+'\n'
            members += extract(production, 'public void registerIcons')
        # Deliberately bounded owner shells, never substituted platform APIs or collaborators.
        target=out/'slices/com/hbm/items/tool'/f'{name}.java';target.parent.mkdir(parents=True,exist_ok=True)
        target.write_text('package com.hbm.items.tool;\nimport net.minecraft.item.Item;\nimport com.hbm.lib.RefStrings;\nimport com.hbm.render.icon.*;\nimport java.util.Locale;\npublic class '+name+' extends '+parent+' {\n'+members+'\n}\n')
        sources.append(target)
    for path in ['lib/RefStrings.java','render/icon/IIcon.java','render/icon/IIconRegister.java','render/item/NativeItemTextureRegistry.java','render/item/NativeItemTextureModelHandler.java']:
        sources.append(ROOT/'src/main/java/com/hbm'/path)
    sources += [H/'FamilyOracle.java',ROOT/'porting/tests/ForgeEventNativeLauncher.java']
    subprocess.run([str(J/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'empty'),'-cp',CP,'-d',str(out)]+list(map(str,sources)),check=True)
    subprocess.run([str(J/'bin/java'),'-Djava.awt.headless=true','-cp',str(out)+os.pathsep+CP,'ForgeEventNativeLauncher','FamilyOracle','/tmp/nagaport-legacy-client.jar',str(ROOT/'src/main/resources')],cwd=out,check=True)

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--attribute',action='store_true');args=parser.parse_args()
    audit()
    with tempfile.TemporaryDirectory(prefix='texture-side-effect-naming-') as directory:
        out=Path(directory)
        if args.attribute: attribute(out)
        runtime(out)

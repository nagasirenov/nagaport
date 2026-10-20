#!/usr/bin/env python3
"""Compile complete real Generic/Base owners and bounded concrete/RNG slices, never full ModBlocks."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
import zipfile

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
JAVA = ROOT / 'src/main/java'
NAMES = ['brick_concrete', 'brick_concrete_mossy', 'brick_concrete_cracked', 'brick_concrete_broken', 'reinforced_sand', 'brick_light']
PRODUCTION = [JAVA / path for path in [
    'com/hbm/blocks/BlockBase.java', 'com/hbm/blocks/generic/BlockGeneric.java',
    'com/hbm/lib/RefStrings.java', 'com/hbm/render/icon/IIcon.java',
    'com/hbm/render/icon/IIconRegister.java', 'com/hbm/render/icon/LegacyIconRegister.java',
    'com/hbm/render/icon/LegacyTextureAtlasSprite.java',
    'com/hbm/render/item/NativeItemTextureRegistry.java',
    'com/hbm/main/LegacyIconRegistrationHandler.java',
    'com/hbm/render/model/LegacyBaseBakedModel.java', 'com/hbm/items/ItemEnumMulti.java',
    'com/hbm/interfaces/IOrderedEnum.java', 'com/hbm/util/EnumUtil.java',
]]
BASELINE_COMMIT = '0732d318302d807ce8579878a0cdde6b18bbba86'
SOURCE_BASELINE_SHA256 = '00fd338ee83ad053707da076fd9ccf3c06592b67719c4e3ad902da4c8b6edb7d'
LEGACY_SHA256 = 'a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'


def sha(data):
    return hashlib.sha256(data).hexdigest()


def extract_method(source, signature):
    start = source.index(signature)
    opening = source.index('{', start)
    depth = 1
    end = opening + 1
    while depth:
        if source[end] == '{': depth += 1
        if source[end] == '}': depth -= 1
        end += 1
    return source[start:end]


def selected_sources():
    mod = (JAVA / 'com/hbm/blocks/ModBlocks.java').read_text()
    constructions = []
    registrations = []
    for name in NAMES:
        c = re.findall(r'^\s*' + name + r' = .*;$', mod, re.M)
        r = re.findall(r'^\s*GameRegistry.registerBlock\(' + name + r', .*;$', mod, re.M)
        assert len(c) == len(r) == 1, name
        assert len(re.findall(r'\b' + name + r'\s*=', mod)) == 1, 'unique static owner assignment: ' + name
        constructions.append(c[0].strip())
        registrations.append(r[0].strip())
    library = (JAVA / 'com/hbm/lib/Library.java').read_text()
    method = extract_method(library, 'public static Block getRandomConcrete()')
    random_field = re.findall(r'^\s*static Random rand = new Random\(\);', library, re.M)
    assert len(random_field) == 1
    return constructions, registrations, method, random_field[0].strip()


def generate_slices(directory):
    """Public probe API: returns generated Java paths. Complete owners remain in PRODUCTION."""
    directory = Path(directory)
    directory.mkdir(parents=True, exist_ok=True)
    constructions, registrations, method, random_field = selected_sources()
    constructor = directory / 'ConcreteConstructionSlice.java'
    constructor.write_text('''import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.blocks.BlockBase;
import com.hbm.blocks.generic.BlockGeneric;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
/** Six verbatim production statements, NOT the ModBlocks class. */
public final class ConcreteConstructionSlice {
public static Block ''' + ', '.join(NAMES) + ''';
public static void initialize() {
''' + '\n'.join(constructions) + '''
}
public static Block[] blocks() { return new Block[]{''' + ', '.join(NAMES) + '''}; }
}
''')
    random_slice = directory / 'ConcreteRandomSlice.java'
    # The production body is unchanged except its explicit fixture field-owner namespace.
    random_slice.write_text('''import java.util.Random;
import net.minecraft.block.Block;
/** Exact Library random field/method slice; only ModBlocks qualifier names the construction slice. */
public final class ConcreteRandomSlice {
''' + random_field + '\n' + method.replace('ModBlocks.', 'ConcreteConstructionSlice.') + '''
public static void seed(long seed) { rand.setSeed(seed); }
public static long nextLong() { return rand.nextLong(); }
}
''')
    fixture = directory / 'com/hbm/main/MainRegistry.java'
    fixture.parent.mkdir(parents=True, exist_ok=True)
    fixture.write_text('''package com.hbm.main;
/** Static fixture boundary only: no content owner or platform class replacement. */
public final class MainRegistry {
 public static final MainRegistry instance = new MainRegistry();
 public final java.util.Random rand = new java.util.Random();
 public static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("ConcreteNativeProbe");
 public static final net.minecraft.creativetab.CreativeTabs blockTab = net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS;
}
''')
    return [constructor, random_slice, fixture]


def check_source(original_assets=None, record=False):
    constructions, registrations, method, random_field = selected_sources()
    baseline_path = HERE.parent / 'generic-metadata-native/source-baseline.zip'
    assert sha(baseline_path.read_bytes()) == SOURCE_BASELINE_SHA256, 'pinned baseline archive changed'
    with zipfile.ZipFile(baseline_path) as archive:
        baseline = {name: archive.read(name) for name in archive.namelist()}
    for path in ('com/hbm/blocks/BlockBase.java', 'com/hbm/blocks/generic/BlockGeneric.java'):
        original = baseline[Path(path).name]
        assert (JAVA / path).read_bytes() == original, 'complete owner differs from pinned CP54 baseline: ' + path
    old_mod = baseline['ModBlocks.java'].decode().replace('\r\n', '\n')
    for name, statement in zip(NAMES, constructions):
        old = re.findall(r'^\s*' + name + r' = .*;$', old_mod, re.M)
        assert len(old) == 1 and old[0].strip() == statement, 'construction differs from pinned CP54: ' + name
    old_library = baseline['Library.java'].decode().replace('\r\n', '\n')
    assert extract_method(old_library, 'public static Block getRandomConcrete()') == method, 'RNG method differs from pinned CP54'
    pristine_library = baseline['pristine-Library.java'].decode().replace('\r\n', '\n')
    assert extract_method(pristine_library, 'public static Block getRandomConcrete()') == method, 'RNG method differs from original pristine source'
    generic = (JAVA / 'com/hbm/blocks/generic/BlockGeneric.java').read_text()
    base = (JAVA / 'com/hbm/blocks/BlockBase.java').read_text()
    assert 'class BlockGeneric extends BlockBase' in generic
    assert 'class BlockBase extends Block' in base
    # This probe deliberately does not invent a sixteen-state property for metadata-zero sites.
    for text in (generic, base):
        for forbidden in ('getStateFromMeta(', 'getMetaFromState(', 'createBlockState(', 'damageDropped(', 'hasTileEntity(', 'createTileEntity(', 'getPickBlock('):
            assert forbidden not in text, forbidden
    item = (JAVA / 'com/hbm/items/block/ItemBlockBase.java').read_text()
    blast = (JAVA / 'com/hbm/items/block/ItemBlockBlastInfo.java').read_text()
    assert extract_method(item, 'public int getMetadata(int meta)') == '''public int getMetadata(int meta) {
		if(block instanceof IBlockMulti)
			return meta;
		else
			return super.getMetadata(meta);
	}'''
    assert 'class ItemBlockBlastInfo extends ItemBlockBase' in blast
    assert extract_method(blast, 'public ItemBlockBlastInfo(Block block)') == '''public ItemBlockBlastInfo(Block block) {
		super(block);
	}'''
    for text in (item, blast):
        assert 'placeBlockAt(' not in text and 'onItemUse(' not in text
    assert all('ItemBlockBlastInfo.class' in r for r in registrations)
    model_handler = (JAVA / 'com/hbm/main/LegacyBaseModelHandler.java').read_text()
    assert 'LegacyBaseBakedModel.forBlock(block)' in model_handler
    assert 'block instanceof BlockBase' in model_handler
    assert 'getModelRegistry().putObject(location, model)' in model_handler
    assets = {}
    archived = zipfile.ZipFile(original_assets) if original_assets else None
    try:
        for name in NAMES:
            relative = 'assets/hbm/textures/blocks/' + name + '.png'
            data = (ROOT / 'src/main/resources' / relative).read_bytes()
            if archived:
                assert data == archived.read(relative), relative
            assets[relative] = sha(data)
    finally:
        if archived: archived.close()
    manifest = {
        'baseline_commit': BASELINE_COMMIT,
        'source_baseline_zip_sha256': SOURCE_BASELINE_SHA256,
        'source_only_routes_sha256': {path: sha((JAVA / path).read_bytes()) for path in ['com/hbm/items/block/ItemBlockBase.java', 'com/hbm/items/block/ItemBlockBlastInfo.java', 'com/hbm/main/LegacyBaseModelHandler.java']},
        'scope': 'metadata-zero BlockGeneric palette owner and six exact construction slices; no full ModBlocks/Library compile',
        'complete_production_sources': {str(p.relative_to(ROOT)): sha(p.read_bytes()) for p in PRODUCTION},
        'construction_statements': constructions,
        'registration_statements_audited_not_executed': registrations,
        'random_field': random_field,
        'library_method': method,
        'random_slice_only_change': 'ModBlocks. -> ConcreteConstructionSlice.',
        'item_boundary': 'Native ItemBlock stand-in only for isolated native drop/pick/placement mechanics; actual ItemBlockBlastInfo and tooltip graph NOT_RUN',
        'source_only_item_checks': ['real ItemBlockBlastInfo forwards constructor', 'real ItemBlockBase non-IBlockMulti metadata delegates native', 'neither class overrides placement'],
        'texture_sha256': assets,
        'archived_assets_sha256': sha(Path(original_assets).read_bytes()) if original_assets else None,
    }
    evidence = HERE / 'source-manifest.json'
    if record:
        evidence.write_text(json.dumps(manifest, indent=2) + '\n')
    else:
        expected = json.loads(evidence.read_text())
        if original_assets is None:
            manifest['archived_assets_sha256'] = expected['archived_assets_sha256']
        assert manifest == expected, 'source evidence changed; compare against pinned source-manifest.json, never silently bless changed owners/slices'
        print('PASS pinned source-manifest comparison; evidence was not regenerated.', flush=True)
    print('PASS source boundaries: actual Generic/Base inherit native no-property state, no TE/drop/pick overrides; concrete ItemBlockBlastInfo non-subtype route audited, not compiled.', flush=True)
    if archived: print('PASS six palette PNGs byte-identical to archived original HBM JAR.', flush=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java-home', type=Path, required=True)
    parser.add_argument('--classpath-file', type=Path, required=True)
    parser.add_argument('--legacy-jar', type=Path, required=True)
    parser.add_argument('--original-assets-jar', type=Path)
    parser.add_argument('--record-source-evidence', action='store_true', help='Explicit initial evidence capture only; ordinary runs compare the checked-in manifest')
    a = parser.parse_args()
    cp = a.classpath_file.read_text().strip()
    assert sha(a.legacy_jar.read_bytes()) == LEGACY_SHA256
    check_source(a.original_assets_jar, a.record_source_evidence)
    with tempfile.TemporaryDirectory(prefix='concrete-native-') as temp:
        tmp = Path(temp)
        generated = generate_slices(tmp)
        files = PRODUCTION + generated + [HERE / 'GenericMetadataNativeTest.java', HERE.parent / 'ForgeEventNativeLauncher.java', HERE.parent / 'block-registry-native/NativeAtlasEnvironment.java']
        for path in files:
            print('COMPILE', path.name, sha(path.read_bytes()), flush=True)
        for jar in cp.split(os.pathsep):
            if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
                with zipfile.ZipFile(jar) as archive:
                    for name in archive.namelist():
                        if name.endswith('.so') and '/' not in name:
                            (tmp / name).write_bytes(archive.read(name))
        assert (tmp / 'liblwjgl64.so').is_file()
        subprocess.run([str(a.java_home / 'bin/javac'), '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-sourcepath', str(tmp / 'empty-sourcepath'), '-cp', cp, '-d', str(tmp)] + list(map(str, files)), check=True)
        subprocess.run([str(a.java_home / 'bin/java'), '-Djava.awt.headless=true', '-Djava.library.path=' + temp, '-cp', temp + os.pathsep + cp, 'ForgeEventNativeLauncher', 'GenericMetadataNativeTest', str(ROOT / 'src/main/resources'), str(a.legacy_jar)] + ([str(a.original_assets_jar)] if a.original_assets_jar else []), cwd=temp, env=dict(os.environ, MESA_SHADER_CACHE_DISABLE='true'), check=True)

if __name__ == '__main__':
    main()

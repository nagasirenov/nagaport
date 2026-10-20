#!/usr/bin/env python3
"""Build an independent, pinned original Steel oracle, without production-source input.

Import generate(pristine=None, legacy_client=None) from the focused native runner.
With no pristine checkout, use the byte-identical bundled pristine source archive.
The legacy client is never downloaded; its optional supplied path is hash verified.
"""
import argparse
import hashlib
import json
from pathlib import Path
import re
import zipfile

HERE = Path(__file__).resolve().parent
SOURCE_NAMES = {
    'wall': 'src/main/java/com/hbm/render/block/RenderSteelWall.java',
    'corner': 'src/main/java/com/hbm/render/block/RenderSteelCorner.java',
    'owner': 'src/main/java/com/hbm/blocks/generic/DecoBlock.java',
    'nt': 'src/main/java/com/hbm/render/util/RenderBlocksNT.java',
    'transform': 'src/main/java/com/hbm/world/gen/nbt/INBTBlockTransformable.java',
    'registry': 'src/main/java/com/hbm/blocks/ModBlocks.java',
    'client': 'src/main/java/com/hbm/main/ClientProxy.java',
}
ASSET = 'src/main/resources/assets/hbm/textures/blocks/steel_wall.png'
LEGACY_SHA256 = 'a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
METHODS = {
    'wall': ['renderInventoryBlock', 'renderWorldBlock', 'shouldRender3DInInventory'],
    'corner': ['renderInventoryBlock', 'renderWorldBlock', 'shouldRender3DInInventory'],
    'owner': ['onScrew', 'shouldSideBeRendered', 'createNewTileEntity', 'isOpaqueCube',
              'renderAsNormalBlock', 'getItemDropped', 'onBlockPlacedBy',
              'setBlockBoundsBasedOnState', 'getCollisionBoundingBoxFromPool',
              'addCollisionBoxesToList', 'transformMeta'],
    'nt': ['renderFaceZNeg', 'renderFaceXPos'],
    'transform': ['transformMetaDeco'],
}


def sha(data):
    return hashlib.sha256(data).hexdigest()


def method(text, name):
    matches = list(re.finditer(r'public\s+(?:static\s+)?[\w<>]+\s+' + re.escape(name) + r'\([^)]*\)\s*\{', text))
    if len(matches) != 1:
        raise AssertionError('Expected exactly one original method: ' + name)
    start = matches[0].start()
    end = matches[0].end()
    depth = 1
    while depth:
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    return text[start:end]


def inputs(pristine=None):
    names = list(SOURCE_NAMES.values()) + [ASSET]
    if pristine is not None:
        root = Path(pristine)
        return {name: (root / name).read_bytes() for name in names}
    with zipfile.ZipFile(HERE / 'pristine-steel-sources.zip') as archive:
        return {name: archive.read(name) for name in names}


def manifest_for(data):
    return {
        'purpose': 'Pinned pristine Steel wall/corner callbacks, owner behavior, original RenderBlocksNT N/E UV corrections, original registry/texture/registration evidence. No native production files contribute to this oracle.',
        'sources': {name: sha(data[name]) for name in SOURCE_NAMES.values()},
        'assets': {ASSET: sha(data[ASSET])},
        'methods': {
            SOURCE_NAMES[key] + '#' + name: {
                'sha256': sha(method(data[SOURCE_NAMES[key]].decode(), name).encode()),
                'line': data[SOURCE_NAMES[key]].decode()[:data[SOURCE_NAMES[key]].decode().index(method(data[SOURCE_NAMES[key]].decode(), name))].count('\n') + 1,
            }
            for key, names in METHODS.items() for name in names
        },
        'legacy_client': {
            'sha256': LEGACY_SHA256,
            'runtime_classes': {'RenderBlocks': 'blm', 'Block': 'aji', 'IIcon': 'rf', 'Tessellator': 'bmh'},
            'faces_by_side_index': ['a', 'b', 'c', 'd', 'e', 'f'],
            'bounds_fields_xyz_min_max': ['i', 'k', 'm', 'j', 'l', 'n'],
            'vertex_stride_ints': 8,
            'vertex_xyz_uv_offsets': [0, 1, 2, 3, 4],
            'render_type_dispatch': 'blm.b(aji,int,int,int), offsets 14..28: owner.a(ahl,int,int,int) then renderer.a(aji), before type dispatch. These are setBlockBoundsBasedOnState and setRenderBoundsFromBlock.',
        },
    }


def generate(pristine=None, legacy_client=None):
    data = inputs(pristine)
    expected = json.loads((HERE / 'source-manifest.json').read_text())
    if manifest_for(data) != expected:
        raise AssertionError('Pristine Steel source/asset differs from the pinned independent oracle')
    if legacy_client is not None and sha(Path(legacy_client).read_bytes()) != LEGACY_SHA256:
        raise AssertionError('Unexpected original Minecraft client bytecode')
    source = {key: data[name].decode() for key, name in SOURCE_NAMES.items()}
    registry = source['registry']
    for name, hardness in [('steel_wall', '5.0F'), ('steel_corner', '15.0F')]:
        line = next(line for line in registry.splitlines() if name + ' = new ' in line)
        assert 'new DecoBlock(Material.iron)' in line
        assert '.setBlockName("' + name + '")' in line
        assert '.setHardness(' + hardness + ')' in line
        assert '.setResistance(15.0F)' in line
        assert '.setBlockTextureName(RefStrings.MODID + ":steel_wall")' in line
    for name in ['RenderSteelWall', 'RenderSteelCorner']:
        assert 'RenderingRegistry.registerBlockHandler(new ' + name + '());' in source['client']
    output = (HERE / 'OriginalSteelOracle.java.in').read_text()
    for kind in ['wall', 'corner', 'owner']:
        methods = '\n\n'.join(method(source[kind], name) for name in METHODS[kind])
        if kind == 'corner':
            methods = methods.replace('RenderBlocksNT', 'RecorderNT')
        output = output.replace('/* ' + kind.upper() + '_METHODS */', methods)
    output = output.replace('/* TRANSFORM_METHOD */', method(source['transform'], 'transformMetaDeco'))
    nt = '\n\n'.join(method(source['nt'], name) for name in METHODS['nt'])
    nt = nt.replace('Tessellator', 'VertexTessellator').replace('IIcon icon', 'TextureAtlasSprite icon')
    output = output.replace('/* NT_FACE_METHODS */', nt)
    if re.search(r'/\* [A-Z_]+ \*/', output):
        raise AssertionError('Unexpanded original oracle marker')
    return output


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--pristine', type=Path)
    parser.add_argument('--legacy-client', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(generate(args.pristine, args.legacy_client))
    print('PASS pinned pristine Steel callback/owner/UV oracle generated:', args.output)


if __name__ == '__main__':
    main()

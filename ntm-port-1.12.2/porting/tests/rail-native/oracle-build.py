#!/usr/bin/env python3
"""Build the independent source-extracted rail renderer oracle, with pinned inputs.

Import generate(original_root, native_root=None) to obtain Java source for the
OracleRailOriginal class. The manifest comes only from the untouched 1.7.10 tree;
production selectors and geometry are never consulted to generate expectations.
"""
import argparse
import hashlib
import json
import re
from pathlib import Path

HERE = Path(__file__).resolve().parent
MANIFEST = HERE / 'oracle-original-manifest.json'


def methods(text, name):
    results = []
    pattern = r'public\s+(?:static\s+)?[\w<>]+\s+' + re.escape(name) + r'\([^)]*\)\s*\{'
    for match in re.finditer(pattern, text):
        end, depth = match.end(), 1
        while depth:
            depth += (text[end] == '{') - (text[end] == '}')
            end += 1
        results.append(text[match.start():end])
    assert results, name
    return results


def verify(original_root, native_root=None):
    original_root = Path(original_root)
    data = json.loads(MANIFEST.read_text())
    for path, expected in data['sha256'].items():
        actual = hashlib.sha256((original_root / path).read_bytes()).hexdigest()
        assert actual == expected, f'Changed pristine source or asset: {path}'
    if native_root:
        for asset in data['assets']:
            path = 'src/main/resources/assets/hbm/' + asset
            assert (Path(native_root) / path).read_bytes() == (original_root / path).read_bytes(), f'Native asset drift: {asset}'
    return data


def generate(original_root, native_root=None):
    original_root = Path(original_root)
    data = verify(original_root, native_root)
    source = (HERE / 'oracle-original.java.in').read_text()
    renderer = (original_root / data['renderer']).read_text()
    source = source.replace('/* RENDERER_METHODS */', '\n'.join(methods(renderer, name)[0] for name in ['renderInventoryBlock', 'renderWorldBlock']))
    blocks, selects, fields, loads = [], [], [], []
    for owner in data['owners']:
        name = owner['owner']
        source_text = (original_root / owner['source']).read_text()
        methods_text = '\n'.join(methods(source_text, name)[0] for name in ['renderInventory', 'renderWorld'])
        sign = json.dumps(owner.get('sign'))
        blocks.append(f'private static class {name} extends Block implements IRenderBlock {{\n{name}(){{super({json.dumps(owner["texture"])},{sign});}}\n{methods_text}\n}}')
        selects.append(f'case "{name}": block=new {name}();break;')
        field = owner['resource_field']
        fields.append(f'static HFRWavefrontObject {field};')
        loads.append(f'ResourceManager.{field}=new HFRWavefrontObject(new File(originalResources,"assets/hbm/{owner["asset"]}"));')
    source = source.replace('/* OWNER_CLASSES */', '\n'.join(blocks)).replace('/* OWNER_SELECT */', '\n'.join(selects))
    source = source.replace('/* RESOURCE_FIELDS */', '\n'.join(fields)).replace('/* RESOURCE_LOAD */', '\n'.join(loads))
    obj = (original_root / data['obj_util']).read_text()
    source = source.replace('/* ORIGINAL_OBJUTIL */', '\n'.join(methods(obj, 'renderWithIcon') + methods(obj, 'renderPartWithIcon')))
    face = (original_root / data['face_source']).read_text()
    source = source.replace('/* ORIGINAL_FACE_NORMAL */', methods(face, 'calculateFaceNormal')[0])
    assert '/* ORIGINAL_' not in source
    return source


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--original-root', type=Path, required=True)
    parser.add_argument('--native-root', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(generate(args.original_root, args.native_root))
    data = json.loads(MANIFEST.read_text())
    print(f'PASS pinned original inputs: {len(data["sha256"])}; owners: {len(data["owners"])}; exact source-extracted owner methods: {2*len(data["owners"])}; RenderRail methods: 2; ObjUtil overloads: 6')
    print(f'Generated {args.output}')


if __name__ == '__main__':
    main()

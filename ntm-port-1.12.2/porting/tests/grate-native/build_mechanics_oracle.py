#!/usr/bin/env python3
"""Insert hash-pinned original grate owner methods into a recording fixture.

No native production input is used. This oracle executes the pristine callbacks,
not a hand-translated statement of their expected behavior.
"""
import argparse
import hashlib
from pathlib import Path
import re
import zipfile

HERE = Path(__file__).resolve().parent
SOURCE = 'src/main/java/com/hbm/blocks/generic/BlockGrate.java'
SHA256 = '143e929d306c4dc9d78a0a52e7218621c302a6f95f287b76801c7eb118e0e2a9'
METHODS = ['getY', 'isOpaqueCube', 'renderAsNormalBlock',
           'setBlockBoundsBasedOnState', 'getCollisionBoundingBoxFromPool',
           'isSideSolid', 'onBlockPlaced', 'onBlockPlacedBy',
           'onNeighborBlockChange', 'addCollisionBoxesToList', 'addInformation']


def method(source, name):
    matches = list(re.finditer(r'public\s+(?:static\s+)?[\w<>]+\s+' + re.escape(name) + r'\([^)]*\)\s*\{', source))
    if len(matches) != 1:
        raise AssertionError('Expected exactly one original method: ' + name)
    start, end = matches[0].start(), matches[0].end()
    depth = 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


def generate(pristine=None):
    if pristine is None:
        with zipfile.ZipFile(HERE / "pristine-grate-sources.zip") as archive:
            data = archive.read(SOURCE)
    else:
        data = (Path(pristine) / SOURCE).read_bytes()
    if hashlib.sha256(data).hexdigest() != SHA256:
        raise AssertionError('Original grate owner is not the pinned pristine source')
    methods = '\n\n'.join(method(data.decode(), name) for name in METHODS)
    return (HERE / 'OriginalGrateMechanicsOracle.java.in').read_text().replace('/* OWNER_METHODS */', methods)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--pristine', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(generate(args.pristine))
    print('PASS pristine grate owner methods hash verified and extracted:', args.output)

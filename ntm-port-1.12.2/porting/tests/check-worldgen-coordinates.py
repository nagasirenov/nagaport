#!/usr/bin/env python3
"""Static parity only; does not claim production compilation or in-game parity."""
import re
from pathlib import Path

project = Path(__file__).resolve().parents[2]
pristine = project.parent / 'ntm-original-pristine'

def source(root, name):
    return (root / 'src/main/java/com/hbm' / name).read_text()

def calls(text, prefix):
    result = []
    for match in re.finditer(re.escape(prefix) + r'\(', text):
        depth, end = 1, match.end()
        while depth:
            depth += (text[end] == '(') - (text[end] == ')')
            end += 1
        result.append(text[match.end():end-1])
    return result

def args(text):
    result, depth, start = [], 0, 0
    for i, c in enumerate(text):
        depth += (c == '(') - (c == ')')
        if c == ',' and depth == 0:
            result.append(text[start:i].strip())
            start = i + 1
    return result + [text[start:].strip()]

def canonical(value):
    return re.sub(r'\s+', '', value).lower()

def placements(text, legacy):
    result = []
    for raw in calls(text, 'world.setBlock' if legacy else 'world.setBlockState'):
        a = args(raw)
        if legacy:
            if len(a) == 4: a += ['0', '3']
        else:
            if not a[0].startswith('new BlockPos('): continue # explicit flower scatter checked below
            pos = args(a[0][len('new BlockPos('):-1])
            state = re.fullmatch(r'(.+)\.getStateFromMeta\((.*)\)', a[1])
            assert state, a
            a = pos + [state[1], state[2], a[2]]
        result.append(tuple(map(canonical, a)))
    return result

for name in ['lib/HbmWorldGen.java', 'world/generator/DungeonToolbox.java']:
    old, new = source(pristine, name), source(project, name)
    assert placements(old, True) == placements(new, False), name + ': placement/meta/flags drift'
    for method in ['nextInt', 'nextGaussian', 'nextFloat', 'nextBoolean']:
        before = calls(old, 'rand.' + method)
        after = calls(new, 'rand.' + method)
        if name.endswith('DungeonToolbox.java') and method == 'nextInt':
            assert after[-6:] == ['8', '8', '4', '4', '8', '8']
            after = after[:-6]
        assert before == after, name + ': RNG call order/bounds drift'
    for obsolete in ['world.setBlock(', 'world.getBlock(', 'world.getBlockMetadata(', 'world.getHeightValue(', 'world.provider.dimensionId']:
        assert obsolete not in new, (name, obsolete)
    assert 'BlockMatcher.forBlock(' in new

flower = source(project, 'blocks/generic/BlockNTMFlower.java')
assert 'PropertyInteger.create("variant", 0, 15)' in flower
assert 'withProperty(VARIANT, meta & 15)' in flower
assert 'return state.getValue(VARIANT);' in flower
assert 'public boolean canPlaceBlockAt(World world, BlockPos pos)' in flower
reeds = source(project, 'blocks/generic/BlockReeds.java')
assert 'public boolean canPlaceBlockAt(World world, BlockPos pos)' in reeds
assert 'block == Blocks.WATER || block == Blocks.FLOWING_WATER' in reeds
scatter = source(project, 'world/generator/DungeonToolbox.java')
assert 'attempt < 64' in scatter
assert 'world.provider.hasSkyLight() || flowerY < 255' in scatter
assert 'world.setBlockState(pos, state, 2)' in scatter
print('PASS: static placement coordinates/blocks/metadata/flags, RNG call order, ore predicates, flower scatter and hooks; not a runtime check')

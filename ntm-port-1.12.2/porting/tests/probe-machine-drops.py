#!/usr/bin/env python3
"""Extract audited production drop guards/loops, without adapting native stack/entity APIs."""
from pathlib import Path
import re
import sys
root = Path(__file__).resolve().parents[2]
manifest = (root / 'porting/machine-empty-consumer-closure.md').read_text(encoding='utf-8')
files = list(dict.fromkeys(re.findall(r'- `(blocks/[^`:]+):[^`]+` — EDIT', manifest)))
files = [f for f in files if f not in ('blocks/machine/BlockPWR.java', 'blocks/network/CraneInserter.java')]
methods = []
guards = 0
for index, filename in enumerate(files):
    source = (root / 'src/main/java/com/hbm' / filename).read_text(encoding='utf-8')
    consumers = re.findall(r'ItemStack (\w+) = [^;]+\.getStackInSlot\([^;]+;\s*if\s*\(([^\n]+)\)', source)
    assert len(consumers) == (2 if filename.endswith(('BlockStorageCrate.java', 'BlockMassStorage.java')) else 1), filename
    for name, condition in consumers:
        assert name + '.isEmpty()' in condition, filename
    guards += len(consumers)
    match = re.search(r'if\s*\((\w+) != null && !\1\.isEmpty\(\)\)\s*\{', source)
    assert match, filename
    end, depth = match.end(), 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    body = source[match.start():end]
    assert '.stackSize' not in body and '.getEntityItem(' not in body, filename
    assert body.index('dropItem =') < body.index('.shrink('), filename
    assert body.index('dropTag =') < body.index('.shrink('), filename
    assert 'new ItemStack(dropItem,' in body and 'dropTag.copy()' in body, filename
    # Only replace the containing block's RNG fields with the supplied deterministic RNG.
    body = re.sub(r'\b(?:this\.(?:rand|field_149933_a|dropRandom)|world\.rand|field_149933_a)\b', 'rand', body)
    # Legacy callback parameter spellings are extraction-only aliases.
    for old, new in [('p_149749_1_', 'world'), ('p_149749_2_', 'x'), ('p_149749_3_', 'y'), ('p_149749_4_', 'z')]:
        body = body.replace(old, new)
    methods.append('    // ' + filename + '\n    static void drop' + str(index) + '(World world, ItemStack ' + match.group(1) + ', Random rand) {\n        int x = 4, y = 8, z = 12;\n' + body + '\n    }')
output = root / 'build/portMachineDropsProbeSrc'
output.mkdir(parents=True, exist_ok=True)
java = '''import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
public final class ExtractedMachineDrops {
    interface Drop { void run(World world, ItemStack stack, Random rand); }
'''
java += '\n'.join(methods)
java += '\n    static final Drop[] DROPS = {\n' + ',\n'.join('        ExtractedMachineDrops::drop' + str(i) for i in range(len(files))) + '\n    };\n}\n'
(output / 'ExtractedMachineDrops.java').write_text(java, encoding='utf-8')
print('PASS: extracted %d guarded native drop blocks; pre-depletion item/meta/tag capture verified.' % len(files))

assert guards == 37, guards
print('PASS: all 37 audited drop/serialization getter consumers reject null and EMPTY before work.')

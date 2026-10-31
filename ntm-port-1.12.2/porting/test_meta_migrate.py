import re
from pathlib import Path

HELD_FILES = {
    'BlockNTSapling.java',
    'ExplosionChaos.java',
    'RBMKBase.java',
    'ItemRBMKLid.java',
    'TileEntityMachineOilWell.java',
    'EntityPinkCloudFX.java',
}

def transform_line(line):
    # Match receiver.setBlockMetadataWithNotify(...)
    # Note: receiver can be 'world', 'this.world', 'worldObj', etc.
    line = re.sub(
        r'([a-zA-Z0-9_.\[\]()]+?)\.setBlockMetadataWithNotify\(',
        r'com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(\1, ',
        line
    )
    return line

total_matches = 0
files_to_touch = []

for p in sorted(Path('src/main/java').rglob('*.java')):
    if p.name in HELD_FILES:
        continue
    content = p.read_text(encoding='utf-8', errors='replace')
    if '.setBlockMetadataWithNotify(' not in content:
        continue
    lines = content.splitlines()
    matches = 0
    for l_no, line in enumerate(lines, 1):
        if '.setBlockMetadataWithNotify(' in line:
            matches += 1
            t = transform_line(line)
            # print first match
            if matches == 1:
                print(f'{p.relative_to("src/main/java")}:{l_no}')
                print('  OLD:', line.strip())
                print('  NEW:', t.strip())
    total_matches += matches
    files_to_touch.append(p)

print(f'\nTotal non-held setBlockMetadataWithNotify calls: {total_matches} in {len(files_to_touch)} files')

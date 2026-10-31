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

META_RECEIVERS = {
    'this.world', 'world', 'p_149727_1_', 'worldObj', 'bullet.world',
    'p_150014_1_', 'p_149689_1_', 'this.worldObj'
}

SETBLOCK_RECEIVERS = {
    'event.getWorld()', 'world', 'event.getEntityLiving().world',
    'p_76484_1_', 'getWorld()', 'this.worldObj', 'this.world'
}

def transform_line(line):
    # Transform setBlockMetadataWithNotify
    for rec in sorted(META_RECEIVERS, key=len, reverse=True):
        pattern = re.escape(rec) + r'\.setBlockMetadataWithNotify\('
        replacement = f'com.hbm.world.WorldBlockState.setBlockMetadataWithNotify({rec}, '
        line = re.sub(pattern, replacement, line)

    # Transform setBlock
    for rec in sorted(SETBLOCK_RECEIVERS, key=len, reverse=True):
        pattern = re.escape(rec) + r'\.setBlock\('
        replacement = f'com.hbm.world.WorldBlockState.writeLegacyBlock({rec}, '
        line = re.sub(pattern, replacement, line)

    return line

def process_file(p):
    if p.name in HELD_FILES:
        return False
    data = p.read_bytes()
    crlf = b'\r\n' in data
    text = data.decode('utf-8', errors='replace')
    if '.setBlockMetadataWithNotify(' not in text and '.setBlock(' not in text:
        return False

    lines = text.split('\r\n' if crlf else '\n')
    new_lines = []
    changed = False
    for line in lines:
        if '.setBlockMetadataWithNotify(' in line or '.setBlock(' in line:
            new_line = transform_line(line)
            if new_line != line:
                changed = True
            new_lines.append(new_line)
        else:
            new_lines.append(line)

    if changed:
        sep = b'\r\n' if crlf else b'\n'
        out_bytes = sep.join(line.encode('utf-8') for line in new_lines)
        p.write_bytes(out_bytes)
        return True
    return False

if __name__ == '__main__':
    modified = []
    for p in sorted(Path('src/main/java').rglob('*.java')):
        if process_file(p):
            modified.append(p)
    print(f'Migrated {len(modified)} files.')

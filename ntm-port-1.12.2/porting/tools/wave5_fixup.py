"""Wave-5 fixup: correct wrong generate() insertions in IWorldGenerator
implementors, convert their signatures, fix EnumFacing offsets, and insert
the IInventory field contract where newly missing."""
import glob
import re

MARKER = 'public boolean generate(World world, Random rand, net.minecraft.util.math.BlockPos position)'

fixed_remove = 0
fixed_sig = 0
for path in glob.glob('src/main/java/**/*.java', recursive=True):
    with open(path, 'rb') as f:
        raw = f.read().decode('utf-8')
    if MARKER not in raw:
        continue
    if 'implements IWorldGenerator' not in raw and 'implements  IWorldGenerator' not in raw:
        continue
    lines = raw.split('\r\n')
    # drop the inserted delegation block (blank, @Override, marker, return, })
    out = []
    i = 0
    while i < len(lines):
        if MARKER in lines[i]:
            # remove preceding @Override (+ optional blank) and following return/}
            start = len(out) - 1
            while start >= 0 and out[start].strip() in ('@Override', ''):
                if out[start].strip() == '@Override':
                    out.pop(start)
                    break
                start -= 1
            else:
                pass
            # also drop one leading blank directly before @Override if present
            if out and out[-1].strip() == '':
                out.pop()
            i += 2  # skip marker + return line
            if i < len(lines) and lines[i].strip() == '}':
                i += 1
            fixed_remove += 1
        else:
            out.append(lines[i])
            i += 1
    raw2 = '\r\n'.join(out)
    # convert legacy generate signature
    raw3, n = re.subn(
        r'generate\((Random \w+, int \w+, int \w+, World \w+), IChunkProvider chunkGenerator, IChunkProvider (chunkProvider)\)',
        r'generate(\1, IChunkGenerator chunkGenerator, IChunkProvider \2)', raw2)
    if n:
        if 'import net.minecraft.world.gen.IChunkGenerator;' not in raw3:
            raw3 = raw3.replace('import net.minecraft.world.chunk.IChunkProvider;',
                                'import net.minecraft.world.chunk.IChunkProvider;\r\nimport net.minecraft.world.gen.IChunkGenerator;', 1)
        fixed_sig += n
    if raw3 != raw:
        with open(path, 'wb') as f:
            f.write(raw3.encode('utf-8'))
        print('fixed:', path)
print('blocks removed:', fixed_remove, 'signatures converted:', fixed_sig)

# EnumFacing offsets in canConnectNode bodies
for path in ['src/main/java/com/hbm/tileentity/TileEntityProxyCombo.java',
             'src/main/java/com/hbm/tileentity/bomb/TileEntityLaunchPadRocket.java']:
    with open(path, 'rb') as f:
        raw = f.read().decode('utf-8')
    # only inside canConnectNode: find method span and fix side.offset*
    m = re.search(r'canConnectNode\(net\.minecraft\.util\.EnumFacing side\)\s*\{(.*?)\n\t\}', raw, re.S)
    if m:
        span = m.group(0)
        new_span = span.replace('side.offsetX', 'side.getXOffset()').replace('side.offsetY', 'side.getYOffset()').replace('side.offsetZ', 'side.getZOffset()')
        raw = raw.replace(span, new_span)
        with open(path, 'wb') as f:
            f.write(raw.encode('utf-8'))
        print('offsets fixed:', path)

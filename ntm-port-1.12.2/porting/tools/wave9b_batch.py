"""Wave-9b (final): global renames and safe arg rewrites for vanished vanilla
APIs. No fluid canDisplace/displaceIfPossible rewrites (already converted in
most files; leftovers handled manually)."""
import glob
import re
import sys

BP = 'new net.minecraft.util.math.BlockPos'
RDB = 'com.hbm.world.WorldBlockState.read'

GLOBAL_TEXT = [
    ('.canLightningStrikeAt(', '.isRainingAt('),
    ('.getOffsetBoundingBox(', '.offset('),
    ('.func_150296_c()', '.getKeySet()'),
    ('.func_95999_t()', '.getCartItem()'),
    ('.getStackInSlotOnClosing(', '.removeStackFromSlot('),
    ('.theItemInWorldManager', '.interactionManager'),
    ('EnchantmentProtection.func_92092_a(', 'EnchantmentProtection.getBlastDamageReduction('),
    ('JsonToNBT.func_150315_a(', 'JsonToNBT.getTagFromJson('),
    ('.intersectsWith(', '.intersects('),
    ('this.isBlockContainer', 'this.hasTileEntity'),
    ('.getTextureName()', '.getRegistryName().toString()'),
    ('.getPlayerCoordinates().posX', '.getPosition().getX()'),
    ('.getPlayerCoordinates().posY', '.getPosition().getY()'),
    ('.getPlayerCoordinates().posZ', '.getPosition().getZ()'),
]

ARG_PATTERNS = [
    (re.compile(r'\.isRainingAt\(([^,()]+),\s*([^,()]+),\s*([^,()]+)\)'),
     '.isRainingAt(' + BP + '(\\1, \\2, \\3))'),
    (re.compile(r'\.dropBlockAsItem\((\w+),\s*([^,()]+),\s*([^,()]+),\s*([^,()]+),\s*([^()]+)\)'),
     '.dropBlockAsItem(\\1, ' + BP + '(\\2, \\3, \\4), ' + RDB + '(\\1, \\2, \\3, \\4))'),
    (re.compile(r'world\.getChunkFromBlockCoords\(([^,()]+),\s*([^,()]+)\)'),
     'world.getChunk(' + BP + '(\\1, 0, \\2))'),
    (re.compile(r'\.blockExists\(([^,()]+),\s*([^,()]+),\s*([^,()]+)\)'),
     '.isBlockLoaded(' + BP + '(\\1, \\2, \\3))'),
    (re.compile(r'\.getTileEntity\(([^,()]+),\s*([^,()]+),\s*([^,()]+)\)'),
     '.getTileEntity(' + BP + '(\\1, \\2, \\3))'),
    (re.compile(r'world\.checkChunksExist\(([^,()]+),\s*([^,()]+),\s*([^,()]+),\s*([^,()]+),\s*([^,()]+),\s*([^,()]+)\)'),
     'world.isAreaLoaded(' + BP + '(\\1, \\2, \\3), ' + BP + '(\\4, \\5, \\6))'),
    (re.compile(r'world\.playAuxSFXAtEntity\((\w+),\s*([^,()]+),\s*([^,()]+),\s*([^,()]+),\s*([^,()]+),\s*([^,()]+)\)'),
     'world.playEvent(\\1, \\2, ' + BP + '(\\3, \\4, \\5), \\6)'),
    (re.compile(r'new EntityAINearestAttackableTarget\(this, (\w+\.class), \d+, (true|false)\)'),
     'new EntityAINearestAttackableTarget(this, \\1, \\2)'),
    (re.compile(r'super\.getPlayerRelativeBlockHardness\((\w+),\s*(\w+),\s*([^,()]+),\s*([^,()]+),\s*([^,()]+)\)'),
     'super.getPlayerRelativeBlockHardness(\\1, ' + RDB + '(\\2, \\3, \\4, \\5), \\2, ' + BP + '(\\3, \\4, \\5))'),
    (re.compile(r'super\.onBlockPlacedBy\((\w+),\s*([^,()]+),\s*([^,()]+),\s*([^,()]+),\s*(\w+),\s*(\w+)\)'),
     'super.onBlockPlacedBy(\\1, ' + BP + '(\\2, \\3, \\4), ' + RDB + '(\\1, \\2, \\3, \\4), \\5, \\6)'),
]

DECLS = [
    (re.compile(r'randomDisplayTick\(World (\w+), Random (\w+), int (\w+), int (\w+), int (\w+)\)'),
     'randomDisplayTick(World \\1, Random \\2, net.minecraft.util.math.BlockPos pos)',
     'int \\3 = pos.getX(), \\4 = pos.getY(), \\5 = pos.getZ();'),
    (re.compile(r'onBlockExploded\(World (\w+), int (\w+), int (\w+), int (\w+)\)'),
     'onBlockExploded(World \\1, net.minecraft.util.math.BlockPos pos)',
     'int \\2 = pos.getX(), \\3 = pos.getY(), \\4 = pos.getZ();'),
]

DROP_OVERRIDE = ['isBeaconBase', 'getShadowSize', 'getRenderBlockPass', 'shouldRenderPass',
                 'func_145817_o', 'digBlock', 'onBlockDestroyedByExplosion', 'canCreatureSpawn',
                 'isLadder', 'removedByPlayer', 'canCommandSenderUseCommand', 'getBoundingBox',
                 'isVecInside', 'onBlockPlaced', 'generate', 'getMinecartType', 'canDisplace',
                 'displaceIfPossible', 'getItem']


def expand(template, m):
    s = template
    for g in range(1, (m.lastindex or 0) + 1):
        s = s.replace('\\%d' % g, m.group(g) or '')
    return s


def main():
    apply = '--apply' in sys.argv
    touched = 0
    for path in glob.glob('src/main/java/**/*.java', recursive=True):
        with open(path, 'rb') as f:
            text = f.read().decode('utf-8')
        orig = text
        for old, new in GLOBAL_TEXT:
            text = text.replace(old, new)
        for pat, rep in ARG_PATTERNS:
            text = pat.sub(lambda m_, r=rep: expand(r, m_), text)
        text = re.sub(r'\.setTextureName\([^()]*\)', '', text)
        nl = '\r\n' if '\r\n' in text else '\n'
        lines = text.split(nl)
        out = []
        pending_locals = None
        for i, l in enumerate(lines):
            if pending_locals is not None:
                if l.rstrip().endswith('{'):
                    out.append(l)
                    out.append(re.match(r'([\t ]*)', l).group(1) + '\t' + pending_locals)
                    pending_locals = None
                else:
                    out.append(l)
                    if l.strip():
                        pending_locals = None
                continue
            conv = False
            for pat, newdecl, locals_ in DECLS:
                m = pat.search(l)
                if not m:
                    continue
                l = pat.sub(lambda m_, r=newdecl: expand(r, m_), l, count=1)
                if locals_:
                    pending_locals = expand(locals_, m)
                out.append(l)
                conv = True
                break
            if conv:
                continue
            s = l.strip()
            if s == '@Override' and pending_locals is None:
                drop = False
                for j in range(i + 1, min(i + 4, len(lines))):
                    nxt = lines[j].strip()
                    if nxt.startswith('@') or not nxt:
                        continue
                    mm = re.search(r'(\w+)\s*\(', nxt)
                    if mm and mm.group(1) in DROP_OVERRIDE:
                        drop = True
                    break
                if drop:
                    continue
            out.append(l)
        text = nl.join(out)
        if text != orig:
            touched += 1
            if apply:
                with open(path, 'wb') as f:
                    f.write(text.encode('utf-8'))
    print('files touched: %d (%s)' % (touched, 'APPLY' if apply else 'PREVIEW'))


if __name__ == '__main__':
    main()

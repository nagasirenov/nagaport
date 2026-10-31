"""Wave-6: paren-balanced multi-line call rewriter for vanished vanilla APIs.

Finds `.method(` occurrences anywhere in each file, balances parentheses to
extract top-level args, and rewrites via per-family handlers. Only applied to
methods that do not exist in 1.12.2 and are not defined by NTM itself.
"""
import glob
import re
import sys

BP = 'new net.minecraft.util.math.BlockPos'
EF = 'net.minecraft.util.EnumFacing'
ITEM_STACK = 'new net.minecraft.item.ItemStack'


def split_args(s):
    args, depth, cur, q = [], 0, '', None
    i = 0
    while i < len(s):
        c = s[i]
        if q:
            cur += c
            if c == q and (i == 0 or s[i-1] != '\\'):
                q = None
        elif c in '"\'':
            cur += c
            q = c
        elif c in '([':
            depth += 1
            cur += c
        elif c in ')]':
            depth -= 1
            cur += c
        elif c == ',' and depth == 0:
            args.append(cur.strip())
            cur = ''
        else:
            cur += c
        i += 1
    if cur.strip():
        args.append(cur.strip())
    return args


def find_calls(text, method):
    """Yield (start, end, args) for each receiver.method(args) call."""
    pat = re.compile(r'\.' + method + r'\s*\(')
    pos = 0
    while True:
        m = pat.search(text, pos)
        if not m:
            return
        i = m.end() - 1
        depth = 0
        j = i
        while j < len(text):
            if text[j] == '(':
                depth += 1
            elif text[j] == ')':
                depth -= 1
                if depth == 0:
                    break
            j += 1
        yield m.start(), j + 1, text[i + 1:j]
        pos = j + 1


def rewrite(text, method, handler):
    out = text
    while True:
        for start, end, argstr in find_calls(out, method):
            args = split_args(argstr)
            rep = handler(args)
            if rep is None:
                continue
            out = out[:start] + '.' + rep + out[end:]
            break
        else:
            return out


FAMILIES = [
    ('getSavedLightValue',
     lambda a: 'getLightFor(%s, %s(%s, %s, %s))' % (a[0], BP, a[1], a[2], a[3]) if len(a) == 4 else None),
    ('getBlockLightValue',
     lambda a: 'getLightFromNeighborsFor(EnumSkyBlock.BLOCK, %s(%s, %s, %s))' % (BP, a[0], a[1], a[2]) if len(a) == 3 else None),
    ('getLightBrightnessForSkyBlocks',
     lambda a: 'getCombinedLight(%s(%s, %s, %s), %s)' % (BP, a[0], a[1], a[2], a[3]) if len(a) == 4 else None),
    ('canBlockSeeTheSky',
     lambda a: 'canSeeSky(%s(%s, %s, %s))' % (BP, a[0], a[1], a[2]) if len(a) == 3 else None),
    ('notifyBlocksOfNeighborChange',
     lambda a: 'notifyNeighborsOfStateChange(%s(%s, %s, %s), %s, true)' % (BP, a[0], a[1], a[2], a[3]) if len(a) == 4 else None),
    ('playAuxSFX',
     lambda a: 'playEvent(%s, %s(%s, %s, %s), %s)' % (a[0], BP, a[1], a[2], a[3], a[4]) if len(a) == 5 else None),
    ('func_150515_a',
     lambda a: 'setBlockAndNotifyAdequately(%s, %s(%s, %s, %s), %s.getDefaultState())' % (a[0], BP, a[1], a[2], a[3], a[4]) if len(a) == 5 else None),
    ('getIndirectPowerLevelTo',
     lambda a: 'getStrongPower(%s(%s, %s, %s), %s.byIndex(%s))' % (BP, a[0], a[1], a[2], EF, a[3]) if len(a) == 4 else None),
]


def rewrite_block_registry(text):
    """Block.blockRegistry.getObject(x) -> ForgeRegistries.BLOCKS.getValue(new ResourceLocation(x))."""
    out = text
    pat = re.compile(r'Block\.blockRegistry\.getObject\s*\(')
    while True:
        m = pat.search(out)
        if not m:
            return out
        i = m.end() - 1
        depth = 0
        j = i
        while j < len(out):
            if out[j] == '(':
                depth += 1
            elif out[j] == ')':
                depth -= 1
                if depth == 0:
                    break
            j += 1
        args = split_args(out[i + 1:j])
        if len(args) != 1:
            return out
        rep = ('net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.getValue('
               'new net.minecraft.util.ResourceLocation(%s))' % args[0])
        out = out[:m.start()] + rep + out[j + 1:]


def main():
    apply = '--apply' in sys.argv
    touched = 0
    for path in glob.glob('src/main/java/**/*.java', recursive=True):
        with open(path, 'rb') as f:
            text = f.read().decode('utf-8')
        orig = text
        for method, handler in FAMILIES:
            text = rewrite(text, method, handler)
        # textual families
        text = text.replace('TextureMap.locationItemsTexture', 'TextureMap.LOCATION_BLOCKS_TEXTURE')
        text = text.replace('TextureMap.locationBlocksTexture', 'TextureMap.LOCATION_BLOCKS_TEXTURE')
        text = rewrite_block_registry(text)
        # inventory.hasItem(x) -> hasItemStack(new ItemStack(x))
        text = rewrite_has_item(text)
        if text != orig:
            touched += 1
            if apply:
                with open(path, 'wb') as f:
                    f.write(text.encode('utf-8'))
            print('%s %s' % ('WROTE' if apply else 'would', path))
    print('files touched: %d (%s)' % (touched, 'APPLY' if apply else 'PREVIEW'))


def rewrite_has_item(text):
    out = text
    while True:
        for start, end, argstr in find_calls(out, 'hasItem'):
            prefix = out[max(0, start - 12):start]
            if 'inventory' not in prefix and 'Inventory' not in prefix:
                continue
            args = split_args(argstr)
            if len(args) != 1:
                continue
            rep = 'hasItemStack(%s(%s))' % (ITEM_STACK, args[0])
            out = out[:start] + '.' + rep + out[end:]
            break
        else:
            return out


if __name__ == '__main__':
    main()

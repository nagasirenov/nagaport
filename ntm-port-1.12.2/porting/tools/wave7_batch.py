"""Wave-7: entity sounds, riding, misc renames, and constructor arg fixes.

Mix of global identifier renames (vanilla-renamed APIs), paren-aware call
rewrites, and line deletions. Only vanished-vanilla APIs are touched globally.
"""
import glob
import re
import sys

RESOLVE = 'com.hbm.main.ModSoundEvents.resolve'


def split_args(s):
    args, depth, cur, q = [], 0, '', None
    for i, c in enumerate(s):
        if q:
            cur += c
            if c == q and s[i-1] != '\\':
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
    if cur.strip():
        args.append(cur.strip())
    return args


def rewrite_calls(text, method, handler, receiver_min=1):
    """Rewrite receiver.method(args); handler(recv, args) -> replacement or None."""
    out = text
    while True:
        pat = re.compile(r'([A-Za-z_]\w*(?:\.\w+)*)\.' + method + r'\s*\(')
        m = pat.search(out)
        if not m:
            return out
        recv = m.group(1)
        i = m.end() - 1
        depth, j = 0, m.end() - 1
        while j < len(out):
            if out[j] == '(':
                depth += 1
            elif out[j] == ')':
                depth -= 1
                if depth == 0:
                    break
            j += 1
        args = split_args(out[i + 1:j])
        rep = handler(recv, args)
        if rep is None:
            # skip this occurrence
            rest = out[m.end():]
            head = out[:m.end()]
            tail = rewrite_calls(rest, method, handler)
            return head + tail
        out = out[:m.start()] + rep + out[j + 1:]


def main():
    apply = '--apply' in sys.argv
    touched = 0
    for path in glob.glob('src/main/java/**/*.java', recursive=True):
        with open(path, 'rb') as f:
            text = f.read().decode('utf-8')
        orig = text

        # entity.playSound("name", vol, pitch) -> world.playSound(null, ...)
        text = rewrite_calls(text, 'playSound',
            lambda r, a: ('%s.world.playSound(null, %s.posX, %s.posY, %s.posZ, %s(%s),'
                          ' net.minecraft.util.SoundCategory.NEUTRAL, %s, %s)' % (r, r, r, r, RESOLVE, a[0], a[1], a[2]))
            if len(a) == 3 and a[0].startswith('"') else None)
        # world.playSound(x, y, z, "name", vol, pitch[, false]) -> playSound(null, ..., resolve, BLOCKS, ...)
        text = rewrite_calls(text, 'playSound',
            lambda r, a: ('%s.playSound(null, %s, %s, %s, %s(%s), net.minecraft.util.SoundCategory.BLOCKS, %s, %s)'
                          % (r, a[0], a[1], a[2], RESOLVE, a[3], a[4], a[5]))
            if len(a) in (6, 7) and a[3].startswith('"') else None)

        # mountEntity
        text = rewrite_calls(text, 'mountEntity',
            lambda r, a: '%s.dismountRidingEntity()' % r if len(a) == 1 and a[0] == 'null' else None)
        text = rewrite_calls(text, 'mountEntity',
            lambda r, a: '%s.startRiding(%s)' % (r, a[0]) if len(a) == 1 else None)

        # ItemStack.loadItemStackFromNBT(nbt) -> new ItemStack(nbt)
        text = rewrite_calls(text, 'loadItemStackFromNBT',
            lambda r, a: 'new net.minecraft.item.ItemStack(%s)' % a[0] if len(a) == 1 else None)
        # provider.loadChunk handled manually
        text = rewrite_calls(text, 'setEntityItemStack',
            lambda r, a: '%s.setItemStack(%s)' % (r, a[0]) if len(a) == 1 else None)
        # this.parseInt(sender, x) -> this.parseInt(x)
        text = rewrite_calls(text, 'parseInt',
            lambda r, a: '%s.parseInt(%s)' % (r, a[1]) if len(a) == 2 else None)
        # new ScaledResolution(mc, w, h) -> new ScaledResolution(mc)
        text = re.sub(r'new ScaledResolution\(\s*([^,()]+)\s*,\s*[^,()]+\.displayWidth\s*,\s*[^,()]+\.displayHeight\s*\)',
                      r'new ScaledResolution(\1)', text)
        # new EntityLightningBolt(w, x, y, z) -> add effectOnly arg
        text = re.sub(r'new EntityLightningBolt\(\s*([^,()]+)\s*,\s*([^,()]+)\s*,\s*([^,()]+)\s*,\s*([^,()]+)\s*\)',
                      r'new EntityLightningBolt(\1, \2, \3, \4, false)', text)
        # OreDictionary.getOres list decls
        text = re.sub(r'ArrayList<ItemStack> (\w+) = (OreDictionary\.getOres\()',
                      r'net.minecraft.util.NonNullList<ItemStack> \1 = \2', text)

        # global identifier renames
        text = re.sub(r'\bisUseableByPlayer\b', 'isUsableByPlayer', text)
        text = re.sub(r'\bGui\.icons\b', 'Gui.ICONS', text)

        # renderItemOverlayIntoGUI: drop TextureManager arg (paren-aware via regex with nested guard)
        text = re.sub(r'renderItemOverlayIntoGUI\((\w+(?:\.\w+)*),\s*(?:this\.mc\.getTextureManager\(\)|mc\.renderEngine|this\.mc\.renderEngine|Minecraft\.getMinecraft\(\)\.getTextureManager\(\)),',
                      r'renderItemOverlayIntoGUI(\1,', text)

        # icon contract stragglers
        lines = text.split('\r\n')
        kept = []
        for l in lines:
            s = l.strip()
            if re.match(r'^(this\.)?setBlockTextureName\([^)]*\);$', s):
                continue
            l = l.replace('this.getIconString()', 'this.getRegistryName().toString()')
            kept.append(l)
        text = '\r\n'.join(kept)

        if text != orig:
            touched += 1
            if apply:
                with open(path, 'wb') as f:
                    f.write(text.encode('utf-8'))
            print('%s %s' % ('WROTE' if apply else 'would', path))
    print('files touched: %d (%s)' % (touched, 'APPLY' if apply else 'PREVIEW'))


if __name__ == '__main__':
    main()

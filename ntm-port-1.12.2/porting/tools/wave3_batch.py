"""Wave-3 mechanical API converter for the NTM 1.12.2 port.

Applies a table of textual rewrites, only on lines flagged by the current
compile log. Lines that match no rule are left untouched.
"""
import re
import sys

LOG = 'porting/checkpoint161/compile.log'
ROOT = 'src/main/java/'

err_re = re.compile(r'^.*?build.sources.main.java.(.+?):(\d+): error: ')

def camel_upper(name):
    out = []
    for i, ch in enumerate(name):
        if ch.isupper() and i > 0:
            out.append('_')
        out.append(ch.upper())
    return ''.join(out)

ENCH_FIELDS = ('fortune', 'silkTouch', 'efficiency', 'unbreaking', 'protection', 'sharpness',
               'fireAspect', 'knockback', 'looting', 'power', 'punch', 'flame', 'infinity',
               'thorns', 'aquaAffinity', 'respiration', 'featherFalling', 'blastProtection',
               'projectileProtection', 'fireProtection', 'smite', 'baneOfArthropods', 'lure',
               'luckOfTheSea', 'sweeping', 'mending', 'frostWalker', 'depthStrider',
               'bindingCurse', 'vanishingCurse', 'strPsight')
ENCH_MAP = {f: camel_upper(f) for f in ENCH_FIELDS}

RULES = [
    # (pattern, replacement) applied in order, first match wins per rule
    (re.compile(r'ModSoundEvents\.resolve\((\w[\w.]*?)\.stepSound\.getStepResourcePath\(\)\)'),
     r'\1.getSoundType().getStepSound()'),
    (re.compile(r'com\.hbm\.main\.ModSoundEvents\.resolve\((\w[\w.]*?)\.stepSound\.getStepResourcePath\(\)\)'),
     r'\1.getSoundType().getStepSound()'),
    (re.compile(r'\.stepSound\.'), r'.getSoundType().'),
    (re.compile(r'EnumSkyBlock\.Sky\b'), 'EnumSkyBlock.SKY'),
    (re.compile(r'EnumSkyBlock\.Block\b'), 'EnumSkyBlock.BLOCK'),
    (re.compile(r'FurnaceRecipes\.smelting\(\)'), 'FurnaceRecipes.instance()'),
    (re.compile(r'\.hasNoTags\(\)'), '.isEmpty()'),
    (re.compile(r'\.getCurrentEquippedItem\(\)'), '.getHeldItemMainhand()'),
    (re.compile(r'\.getEntityToAttack\(\)'), '.getAttackTarget()'),
    (re.compile(r'\.getDataWatcher\(\)'), '.getDataManager()'),
    (re.compile(r'\.addCoord\('), '.expand('),
    (re.compile(r'\.canBlockSeeTheSky\(([^()]*)\)'), r'.canSeeSky(new net.minecraft.util.math.BlockPos(\1))'),
    (re.compile(r'\.getPotionID\(\)'), '.getPotion()'),
    (re.compile(r'Potion\.potionTypes\[([^\]]+)\]'), r'(\1)'),
    (re.compile(r'\.getServerHandler\(\)\.playerEntity\b'), '.getServerHandler().player'),
    (re.compile(r'BlockPistonBase\.determineOrientation\([^,]+,\s*[^,]+,\s*[^,]+,\s*[^,]+,\s*([^)]+)\)'),
     r'BlockPistonBase.getFacing(\1).getIndex()'),
    (re.compile(r'Block\.blockRegistry\.getObject\(([^()]+)\)'),
     r'net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(new net.minecraft.util.ResourceLocation(\1))'),
    (re.compile(r'(\w+)\.field_150939_a\b'), r'net.minecraft.init.Block.getBlockFromItem(\1)'),
    (re.compile(r'(\w[\w.]*)\.difficultySetting\b'), r'\1.getDifficulty()'),
    (re.compile(r'([A-Za-z_][\w.]*[Pp]otion\.[A-Za-z_]\w*)\.id\b'), r'\1'),
    (re.compile(r'(\w[\w.]*)\.setItemInUse\([^)]*\)'), r'\1.setActiveHand(net.minecraft.util.EnumHand.MAIN_HAND)'),
]

def ench_sub(line):
    def repl(m):
        field = m.group(1)
        return 'net.minecraft.init.Enchantments.' + ENCH_MAP.get(field, field.upper())
    return re.sub(r'Enchantment\.(' + '|'.join(ENCH_FIELDS) + r')\b', repl, line)

def delete_line_p(line):
    s = line.strip()
    if re.match(r'^(this\.)?setTextureName\([^)]*\);$', s):
        return True
    return False

def transform(line):
    if delete_line_p(line):
        return ''
    out = ench_sub(line)
    for pat, rep in RULES:
        out = pat.sub(rep, out)
    if out != line:
        return out
    return None

def main():
    apply = '--apply' in sys.argv
    errors = {}
    with open(LOG, encoding='utf-8', errors='replace') as f:
        for raw in f:
            m = err_re.match(raw.rstrip('\r\n'))
            if m:
                errors.setdefault(m.group(1).replace('\\', '/'), set()).add(int(m.group(2)))
    touched = 0
    for rel in sorted(errors):
        path = ROOT + rel
        with open(path, 'rb') as f:
            lines = f.read().decode('utf-8').split('\r\n')
        changed = []
        for ln in sorted(errors[rel]):
            i = ln - 1
            if i < len(lines):
                new = transform(lines[i])
                if new is not None:
                    lines[i] = new
                    changed.append(ln)
        if changed:
            touched += 1
            out = '\r\n'.join(lines)
            if not out.endswith('\r\n'):
                out += '\r\n'
            if apply:
                with open(path, 'wb') as f:
                    f.write(out.encode('utf-8'))
            print('%-70s %s (%d)' % (rel, 'WROTE' if apply else 'would-write', len(changed)))
    print('--- files touched: %d (%s)' % (touched, 'APPLY' if apply else 'PREVIEW'))

if __name__ == '__main__':
    main()

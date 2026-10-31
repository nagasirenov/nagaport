"""Wave-8: Block/Item/Entity method signature conversions driven by
@Override error lines from the current compile log."""
import re
import sys

LOG = 'porting/compile-baseline.log'
ROOT = 'src/main/java/'
BP = 'net.minecraft.util.math.BlockPos'
IBS = 'net.minecraft.block.state.IBlockState'

err_re = re.compile(r'^.*?build.sources.main.java.(.+?):(\d+): error: method does not override')

FAMILIES = [
    # (old decl regex, new decl template, adapter locals)
    (r'public void onBlockAdded\(World (\w+), int (\w+), int (\w+), int (\w+)\)',
     'public void onBlockAdded(World %s, %s pos, %s state)' % ('\\1', BP, IBS),
     'int \\2 = pos.getX(), \\3 = pos.getY(), \\4 = pos.getZ();'),
    (r'public boolean canBlockStay\(World (\w+), int (\w+), int (\w+), int (\w+)\)',
     'public boolean canBlockStay(World %s, %s pos)' % ('\\1', BP),
     'int \\2 = pos.getX(), \\3 = pos.getY(), \\4 = pos.getZ();'),
    (r'public AxisAlignedBB getCollisionBoundingBoxFromPool\(World (\w+), int (\w+), int (\w+), int (\w+)\)',
     'public AxisAlignedBB getCollisionBoundingBox(%s state, net.minecraft.world.IBlockAccess %s, %s pos)' % (IBS, '\\1', BP),
     'int \\2 = pos.getX(), \\3 = pos.getY(), \\4 = pos.getZ();'),
    (r'public Item getItemDropped\(int (\w+), Random (\w+), int (\w+)\)',
     'public net.minecraft.item.Item getItemDropped(%s state, Random \\2, int \\3)' % IBS,
     'int \\1 = state.getBlock().getMetaFromState(state);'),
    (r'public int damageDropped\(int (\w+)\)',
     'public int damageDropped(%s state)' % IBS,
     'int \\1 = state.getBlock().getMetaFromState(state);'),
    (r'public ItemStack createStackedBlock\(int (\w+)\)',
     'public net.minecraft.item.ItemStack createStackedBlock(%s state)' % IBS,
     'int \\1 = state.getBlock().getMetaFromState(state);'),
    (r'public void dropBlockAsItemWithChance\(World (\w+), int (\w+), int (\w+), int (\w+), int (\w+), float (\w+), int (\w+)\)',
     'public void dropBlockAsItemWithChance(World %s, %s pos, %s state, float \\6, int \\7)' % ('\\1', BP, IBS),
     'int \\2 = pos.getX(), \\3 = pos.getY(), \\4 = pos.getZ(); int \\5 = state.getBlock().getMetaFromState(state);'),
    (r'public Item getItem\(World (\w+), int (\w+), int (\w+), int (\w+)\)',
     'public net.minecraft.item.Item getItem(World %s, %s pos, %s state)' % ('\\1', BP, IBS),
     'int \\2 = pos.getX(), \\3 = pos.getY(), \\4 = pos.getZ();'),
    (r'public void getSubBlocks\(Item (\w+), CreativeTabs (\w+), List (\w+)\)',
     'public void getSubBlocks(net.minecraft.creativetab.CreativeTabs \\2, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> \\3)',
     None),
    (r'public ItemStack onEaten\(ItemStack (\w+), World (\w+), EntityPlayer (\w+)\)',
     'public net.minecraft.item.ItemStack onItemUseFinish(ItemStack \\1, World \\2, net.minecraft.entity.EntityLivingBase entity)',
     'EntityPlayer \\3 = (EntityPlayer) entity;'),
    (r'public List addTabCompletionOptions\(ICommandSender (\w+), String\[\] (\w+)\)',
     'public java.util.List<String> getTabCompletions(net.minecraft.server.MinecraftServer server, ICommandSender \\1, String[] \\2, %s pos)' % BP,
     None),
    (r'public int getBrightnessForRender\(float (\w+)\)',
     'public int getBrightnessForRender()', None),
    (r'public float getBrightness\(float (\w+)\)',
     'public float getBrightness()', None),
]

DROP_OVERRIDE = ['func_146077_cc', 'isAIEnabled', 'canAttackWithItem', 'getStillIcon', 'getFlowingIcon', 'getRenderType']


def main():
    apply = '--apply' in sys.argv
    byfile = {}
    with open(LOG, encoding='utf-8', errors='replace') as f:
        for raw in f:
            m = err_re.match(raw.rstrip('\r\n'))
            if m:
                byfile.setdefault(m.group(1).replace(chr(92), '/'), set()).add(int(m.group(2)))
    stats = {}
    for rel in sorted(byfile):
        path = ROOT + rel
        with open(path, 'rb') as f:
            lines = f.read().decode('utf-8').split('\r\n')
        edits = []
        for ln in sorted(byfile[rel]):
            i = ln - 1  # @Override line
            if i >= len(lines) or '@Override' not in lines[i]:
                continue
            # find decl within next 3 lines
            for j in range(i + 1, min(i + 4, len(lines))):
                decl = lines[j].strip()
                if decl.startswith('@') or not decl:
                    continue
                break
            else:
                continue
            matched = False
            for pat, newdecl, locals_ in FAMILIES:
                nm = re.search(pat, lines[j])
                if not nm:
                    continue
                lines[j] = re.sub(pat, newdecl, lines[j], count=1)
                indent = re.match(r'([\t ]*)', lines[j]).group(1) + '\t'
                if locals_:
                    body = re.sub(r'\\(\d)', lambda g: nm.group(int(g.group(1))), locals_)
                    # insert after opening brace: same line or next
                    if lines[j].rstrip().endswith('{'):
                        lines.insert(j + 1, indent + body)
                    else:
                        k = j + 1
                        while k < len(lines) and '{' not in lines[k]:
                            k += 1
                        lines.insert(k + 1, indent + body)
                stats[pat.split('(')[0].split()[-1]] = stats.get(pat.split('(')[0].split()[-1], 0) + 1
                matched = True
                break
            if not matched:
                name_m = re.search(r'(\w+)\s*\(', decl)
                fname = name_m.group(1) if name_m else '?'
                if fname in DROP_OVERRIDE:
                    # remove the @Override line
                    lines[i] = None
                    stats['drop:' + fname] = stats.get('drop:' + fname, 0) + 1
        if any(l is None for l in lines) or True:
            newlines = [l for l in lines if l is not None]
            # only write if changed
            with open(path, 'rb') as f:
                orig = f.read().decode('utf-8').split('\r\n')
            if newlines != orig:
                out = '\r\n'.join(newlines)
                if not out.endswith('\r\n'):
                    out += '\r\n'
                edits.append(rel)
                if apply:
                    with open(path, 'wb') as f:
                        f.write(out.encode('utf-8'))
    print('stats:', dict(sorted(stats.items(), key=lambda x: -x[1])))
    print('total conversions:', sum(stats.values()), '(apply=%s)' % apply)


if __name__ == '__main__':
    main()

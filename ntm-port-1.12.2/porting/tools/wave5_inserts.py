"""Wave-5: insert missing interface implementations reported as class-level errors.

Handles: IInventory.clear(), WorldGenerator.generate(World,Random,BlockPos)
delegations, and EntityMinecart.getType().
"""
import re
import sys

LOG = 'porting/compile-baseline.log'
ROOT = 'src/main/java/'

err_re = re.compile(
    r'^.*?build.sources.main.java.(.+?):(\d+): error: (\w+) is not abstract and does not override abstract method (\w+)\(([^)]*)\)')

CLEAR_BODY = (
    '\r\n\t\t@Override\r\n'
    '\t\tpublic void clear() {\r\n'
    '\t\t\tfor(int i = 0; i < this.getSizeInventory(); i++) {\r\n'
    '\t\t\t\tthis.setInventorySlotContents(i, null);\r\n'
    '\t\t\t}\r\n'
    '\t\t}\r\n'
)

GEN_BODY = (
    '\r\n\t\t@Override\r\n'
    '\t\tpublic boolean generate(World world, Random rand, net.minecraft.util.math.BlockPos position) {\r\n'
    '\t\t\treturn generate(world, rand, position.getX(), position.getY(), position.getZ());\r\n'
    '\t\t}\r\n'
)


def type_for(name):
    n = name.lower()
    if 'chest' in n: return 'CHEST'
    if 'furnace' in n: return 'FURNACE'
    if 'hopper' in n: return 'HOPPER'
    if 'tnt' in n: return 'TNT'
    if 'spawner' in n: return 'SPAWNER'
    if 'command' in n: return 'COMMAND_BLOCK'
    return 'RIDEABLE'


def find_brace(lines, start):
    for i in range(start, min(start + 6, len(lines))):
        if '{' in lines[i]:
            return i
    return None


def main():
    apply = '--apply' in sys.argv
    jobs = {}
    with open(LOG, encoding='utf-8', errors='replace') as f:
        for raw in f:
            m = err_re.match(raw.rstrip('\r\n'))
            if m:
                rel, ln, cls, method, args = m.groups()
                jobs.setdefault((rel.replace(chr(92), '/'), int(ln)), (cls, method, args))
    print('class-level jobs:', len(jobs))
    for (rel, ln), (cls, method, args) in sorted(jobs.items()):
        if method not in ('clear', 'generate', 'getType'):
            continue
        path = ROOT + rel
        with open(path, 'rb') as f:
            lines = f.read().decode('utf-8').split('\r\n')
        # the class decl is at the reported line (1-based); find its opening brace
        i = ln - 1
        brace = find_brace(lines, i)
        if brace is None:
            print('NO BRACE:', rel, ln)
            continue
        decl = lines[i]
        indent = re.match(r'([\t ]*)', decl).group(1) + '\t'
        if method == 'clear':
            body = CLEAR_BODY.replace('\t\t', indent)
        elif method == 'generate':
            body = GEN_BODY.replace('\t\t', indent)
        else:
            body = ('\r\n' + indent + '@Override\r\n' + indent +
                    'public net.minecraft.entity.item.EntityMinecart.Type getType() {\r\n' + indent +
                    '\treturn net.minecraft.entity.item.EntityMinecart.Type.%s;\r\n' % type_for(cls) +
                    indent + '}\r\n')
        lines.insert(brace + 1, body.rstrip('\r\n'))
        out = '\r\n'.join(lines)
        if not out.endswith('\r\n'):
            out += '\r\n'
        if apply:
            with open(path, 'wb') as f:
                f.write(out.encode('utf-8'))
        print('%s %s:%d -> %s (%s)' % ('WROTE' if apply else 'would', rel, ln, cls, method))
    print('done (%s)' % ('APPLY' if apply else 'PREVIEW'))


if __name__ == '__main__':
    main()

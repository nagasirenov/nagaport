"""Insert the IInventory field contract (getField/setField/getFieldCount) into
classes flagged by the current compile log."""
import re
import sys

LOG = 'porting/checkpoint162/compile.log'
ROOT = 'src/main/java/'
err_re = re.compile(
    r'^.*?build.sources.main.java.(.+?):(\d+): error: (\w+) is not abstract and does not override abstract method (\w+)\(')


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
                rel, ln, cls, method = m.groups()
                if method in ('getField', 'setField', 'getFieldCount', 'isEmpty'):
                    jobs[(rel.replace(chr(92), '/'), int(ln))] = (cls, method)
    print('jobs:', len(jobs))
    for (rel, ln), (cls, method) in sorted(jobs.items()):
        path = ROOT + rel
        with open(path, 'rb') as f:
            lines = f.read().decode('utf-8').split('\r\n')
        if any('public int getFieldCount()' in l for l in lines):
            print('already has:', rel)
            continue
        brace = find_brace(lines, ln - 1)
        if brace is None:
            print('NO BRACE:', rel, ln)
            continue
        indent = re.match(r'([\t ]*)', lines[ln - 1]).group(1) + '\t'
        block = [
            '',
            indent + '@Override public int getField(int id) { return 0; }',
            indent + '@Override public void setField(int id, int value) {}',
            indent + '@Override public int getFieldCount() { return 0; }',
            '',
            indent + '@Override',
            indent + 'public boolean isEmpty() {',
            indent + '\tfor(int i = 0; i < this.getSizeInventory(); i++) {',
            indent + '\t\tif(this.getStackInSlot(i) != null && !this.getStackInSlot(i).isEmpty()) return false;',
            indent + '\t}',
            indent + '\treturn true;',
            indent + '}',
        ]
        for k, text in enumerate(block):
            lines.insert(brace + 1 + k, text)
        out = '\r\n'.join(lines)
        if not out.endswith('\r\n'):
            out += '\r\n'
        if apply:
            with open(path, 'wb') as f:
                f.write(out.encode('utf-8'))
        print('%s %s:%d (%s)' % ('WROTE' if apply else 'would', rel, ln, method))


if __name__ == '__main__':
    main()

"""Surgical icon-contract converter for the NTM 1.12.2 port.

Only rewrites lines that the CP161 compile log flags as errors, so untouched
files and untouched lines keep compiling exactly as before.
"""
import re
import sys

LOG = 'porting/checkpoint161/compile.log'
ROOT = 'src/main/java/'

err_re = re.compile(r'^.*?build.sources.main.java.(.+?):(\d+): error: (.+)$')

LEGACY_SIG = [
    r'void registerIcons\(',
    r'void registerBlockIcons\(',
    r'IIcon getIconFromDamage',
    r'IIcon getIconFromSideForRenderPass',
    r'IIcon getIconFromDamageForRenderPass',
    r'IIcon getIcon\(',
    r'boolean requiresMultipleRenderPasses\(',
    r'int getRenderPasses\(',
    r'int getColorFromItemStack\(',
    r'String getIconName\(',
]
LEGACY_SIG_RE = [re.compile(p) for p in LEGACY_SIG]

super_reg_re = re.compile(r'super\.registerIcons\(\s*(\w+)\s*\)')
super_breg_re = re.compile(r'super\.registerBlockIcons\(\s*(\w+)\s*\)')
super_icon_call_re = re.compile(r'super\.getIcon(?:FromDamage|FromDamageForRenderPass|FromSideForRenderPass)?\s*\([^)]*\)')


def load_errors():
    errors = {}
    with open(LOG, encoding='utf-8', errors='replace') as f:
        for raw in f:
            m = err_re.match(raw.rstrip('\r\n'))
            if m:
                rel = m.group(1).replace('\\', '/')
                errors.setdefault(rel, set()).add(int(m.group(2)))
    return errors


def is_override_error(lines, idx):
    return 'does not override' in lines[idx + 1] if idx + 1 < len(lines) else False


def process(rel, err_lines):
    path = ROOT + rel
    with open(path, 'rb') as f:
        data = f.read()
    try:
        text = data.decode('utf-8')
    except UnicodeDecodeError:
        return None, 'skipped (non-utf8)'
    if '\r\n' not in text:
        return None, 'skipped (no CRLF)'
    lines = text.split('\r\n')
    changed = []
    need_item_field = False
    need_block_field = False

    # Pass 1: collect edits per line index
    for ln in sorted(err_lines):
        i = ln - 1
        if i >= len(lines):
            continue
        line = lines[i]
        nxt = '\n'.join(lines[i + 1:i + 4])
        if line.strip() == '@Override' and any(r.search(nxt) for r in LEGACY_SIG_RE):
            lines[i] = None  # delete this line
            changed.append((ln, 'drop @Override'))
            continue
        m = super_reg_re.search(line)
        if m:
            var = m.group(1)
            lines[i] = super_reg_re.sub(
                'this.itemIcon = %s.registerIcon(this.getRegistryName().toString());' % var, line)
            changed.append((ln, 'base itemIcon via %s' % var))
            continue
        m = super_breg_re.search(line)
        if m:
            var = m.group(1)
            lines[i] = super_breg_re.sub(
                'this.blockIcon = %s.registerIcon(this.getRegistryName().toString());' % var, line)
            changed.append((ln, 'base blockIcon via %s' % var))
            continue
        if super_icon_call_re.search(line):
            lines[i] = super_icon_call_re.sub('this.itemIcon', line)
            changed.append((ln, 'super icon call -> this.itemIcon'))
            continue
        if re.search(r'\bitemIcon\b', line):
            need_item_field = True
            changed.append((ln, 'itemIcon reference'))
            continue
        if re.search(r'\bblockIcon\b', line):
            need_block_field = True
            changed.append((ln, 'blockIcon reference'))
            continue

    if not changed:
        return None, []

    # Pass 2: field insertion if still referenced but not declared
    body = '\r\n'.join(l for l in lines if l is not None)
    final_lines = [l for l in lines if l is not None]
    if need_item_field and not re.search(r'IIcon\s+itemIcon\s*;', body):
        for j, l in enumerate(final_lines):
            if re.match(r'(public|abstract).*class\s+\w+.*\{', l.strip()) and l.rstrip().endswith('{'):
                indent = re.match(r'([\t ]*)', l).group(1) + '\t'
                final_lines.insert(j + 1, '')
                final_lines.insert(j + 2, indent + 'protected IIcon itemIcon;')
                changed.append((j + 2, 'insert itemIcon field'))
                break
        else:
            return None, 'no class brace found for field insert'
    if need_block_field and not re.search(r'IIcon\s+blockIcon\s*;', body):
        for j, l in enumerate(final_lines):
            if re.match(r'(public|abstract).*class\s+\w+.*\{', l.strip()) and l.rstrip().endswith('{'):
                indent = re.match(r'([\t ]*)', l).group(1) + '\t'
                final_lines.insert(j + 1, '')
                final_lines.insert(j + 2, indent + 'protected IIcon blockIcon;')
                changed.append((j + 2, 'insert blockIcon field'))
                break

    out = '\r\n'.join(final_lines)
    if not out.endswith('\r\n'):
        out += '\r\n'
    return (path, out), changed


def main():
    apply = '--apply' in sys.argv
    errors = load_errors()
    touched = 0
    for rel in sorted(errors):
        result, changed = process(rel, errors[rel])
        status = []
        if result:
            touched += 1
            if apply:
                with open(result[0], 'wb') as f:
                    f.write(result[1].encode('utf-8'))
                status.append('WROTE')
            else:
                status.append('would-write')
        print('%-70s %s (%d edits: %s)' % (rel, '/'.join(status) or 'noop', len(changed),
              '; '.join('%s:%s' % (c[0], c[1]) for c in changed[:6])))
    print('--- files touched: %d (%s mode)' % (touched, 'APPLY' if apply else 'PREVIEW'))


if __name__ == '__main__':
    main()

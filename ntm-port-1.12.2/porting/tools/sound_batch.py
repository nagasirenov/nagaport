"""Surgical playSound-family converter for the NTM 1.12.2 port.

Rewrites only lines flagged as errors by the current compile log.
  playSoundAtEntity(ent, name, vol, pitch)
      -> playSound(null, ent.posX, ent.posY, ent.posZ, resolve(name), PLAYERS, vol, pitch)
  playSoundEffect(x, y, z, name, vol, pitch[, delay])
      -> playSound(null, x, y, z, resolve(name), BLOCKS, vol, pitch)
"""
import re
import sys

LOG = 'porting/checkpoint161/compile.log'
ROOT = 'src/main/java/'

err_re = re.compile(r'^.*?build.sources.main.java.(.+?):(\d+): error: ')

at_entity_re = re.compile(
    r'\.playSoundAtEntity\(\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*\)\s*;')
effect_re = re.compile(
    r'\.playSoundEffect\(\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*\)\s*;')
effect_delay_re = re.compile(
    r'\.playSoundEffect\(\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*([^,]+?)\s*,\s*(?:true|false)\s*\)\s*;')


def load_errors():
    errors = {}
    with open(LOG, encoding='utf-8', errors='replace') as f:
        for raw in f:
            m = err_re.match(raw.rstrip('\r\n'))
            if m:
                errors.setdefault(m.group(1).replace('\\', '/'), set()).add(int(m.group(2)))
    return errors


def transform(line):
    m = at_entity_re.search(line)
    if m:
        ent, name, vol, pitch = m.groups()
        return at_entity_re.sub(
            '.playSound(null, %s.posX, %s.posY, %s.posZ, com.hbm.main.ModSoundEvents.resolve(%s),'
            ' net.minecraft.util.SoundCategory.PLAYERS, %s, %s);' % (ent, ent, ent, name, vol, pitch), line)
    m = effect_delay_re.search(line)
    if m:
        x, y, z, name, vol, pitch = m.groups()
        return effect_delay_re.sub(
            '.playSound(null, %s, %s, %s, com.hbm.main.ModSoundEvents.resolve(%s),'
            ' net.minecraft.util.SoundCategory.BLOCKS, %s, %s);' % (x, y, z, name, vol, pitch), line)
    m = effect_re.search(line)
    if m:
        x, y, z, name, vol, pitch = m.groups()
        return effect_re.sub(
            '.playSound(null, %s, %s, %s, com.hbm.main.ModSoundEvents.resolve(%s),'
            ' net.minecraft.util.SoundCategory.BLOCKS, %s, %s);' % (x, y, z, name, vol, pitch), line)
    return None


def main():
    apply = '--apply' in sys.argv
    errors = load_errors()
    touched = 0
    for rel in sorted(errors):
        path = ROOT + rel
        with open(path, 'rb') as f:
            text = f.read().decode('utf-8')
        lines = text.split('\r\n')
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
            print('%-70s %s (%d: %s)' % (rel, 'WROTE' if apply else 'would-write', len(changed), changed[:5]))
    print('--- files touched: %d (%s)' % (touched, 'APPLY' if apply else 'PREVIEW'))


if __name__ == '__main__':
    main()

# Offline validation of every blockstate model ref against the 1.12.2
# vanilla Variant$Deserializer doubling rule (ns + "block/" + path).
# A ref "hbm:X" resolves to "hbm:block/X"; that is served either by a real
# JSON at models/block/X.json or by a custom ICustomModelLoader whose route
# prefix (after the loader strips the doubled "block/") matches X.
import os, re, json

R = r'C:/Users/nagas/Desktop/port/ntm-port-1.12.2'
BS = R + '/src/main/resources/assets/hbm/blockstates'
MB = R + '/src/main/resources/assets/hbm/models/block'
SRC = R + '/src/main/java'

# Collect loader route prefixes from the source tree.
prefixes = set()
for root, _, files in os.walk(SRC):
    for fn in files:
        if not fn.endswith('.java'):
            continue
        text = open(os.path.join(root, fn), encoding='utf-8', errors='replace').read()
        for m in re.finditer(r'"(native_[a-z0-9_]+/?)', text):
            p = m.group(1)
            prefixes.add(p if p.endswith('/') else p + '/')

refs = {}          # ref -> [files]
missing = {}       # ref -> [files]
selfheal = 0
files = sorted(fn for fn in os.listdir(BS) if fn.endswith('.json'))
for fn in files:
    text = open(os.path.join(BS, fn), encoding='utf-8', errors='replace').read()
    for m in re.finditer(r'"model"\s*:\s*"([^"]+)"', text):
        ref = m.group(1)
        refs.setdefault(ref, []).append(fn)
        if ref.startswith('hbm:item/'):
            selfheal += 1
            continue
        if not ref.startswith('hbm:'):
            continue
        x = ref[len('hbm:'):]
        if (MB + '/' + x + '.json').replace('\\', '/'):
            if os.path.exists(MB + '/' + x + '.json'):
                continue
        if any(x.startswith(p) for p in prefixes):
            continue
        missing.setdefault(ref, []).append(fn)

print('blockstate files: %d, distinct refs: %d, self-healing item refs: %d' % (len(files), len(refs), selfheal))
print('loader prefixes found: %d' % len(prefixes))
if missing:
    print('MISSING TARGETS:')
    for ref in sorted(missing):
        print('  %s <- %s' % (ref, ', '.join(sorted(set(missing[ref])))))
else:
    print('all blockstate model refs resolve to a file or a loader prefix')

import pathlib
import re

root = pathlib.Path('src/main/java/com/hbm/inventory')
patterns = [
    (re.compile(rb'((?:this\.)?mc\.player\.inventory\.getItemStack\(\))\s*==\s*null'), rb'\1.isEmpty()'),
    (re.compile(rb'(player\.inventory\.getItemStack\(\))\s*==\s*null'), rb'\1.isEmpty()'),
    (re.compile(rb'((?:this\.)?mc\.player\.inventory\.getItemStack\(\))\s*!=\s*null'), rb'!\1.isEmpty()'),
    (re.compile(rb'(player\.inventory\.getItemStack\(\))\s*!=\s*null'), rb'!\1.isEmpty()'),
]

changed = []
for path in root.rglob('*.java'):
    data = path.read_bytes()
    new = data
    for pattern, replacement in patterns:
        new = pattern.sub(replacement, new)
    if new != data:
        path.write_bytes(new)
        changed.append(str(path))

for path in changed:
    print(path)
print('changed=%d' % len(changed))

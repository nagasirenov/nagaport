import pathlib
import re

root = pathlib.Path('src/main/java/com/hbm/inventory/container')
patterns = [
    (re.compile(rb'(ItemStack\s+\w+\s*=\s*)null;'), rb'\1ItemStack.EMPTY;'),
    (re.compile(rb'putStack\(\(ItemStack\)\s*null\)'), rb'putStack(ItemStack.EMPTY)'),
    (re.compile(rb'putStack\(null\)'), rb'putStack(ItemStack.EMPTY)'),
    (re.compile(rb'return null;'), rb'return ItemStack.EMPTY;'),
]

changed = []
for path in root.glob('*.java'):
    data = path.read_bytes()
    new = data
    for pattern, replacement in patterns:
        new = pattern.sub(replacement, new)
    if new != data:
        path.write_bytes(new)
        changed.append(path.name)

print('\n'.join(sorted(changed)))
print('changed=%d' % len(changed))

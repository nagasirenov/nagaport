import pathlib
import re

root = pathlib.Path('src/main/java/com/hbm/tileentity')
method_start = re.compile(r'public\s+ItemStack\s+(getStackInSlot|decrStackSize)\s*\([^)]*\)\s*\{')

changed = []

for path in root.rglob('*.java'):
    raw = path.read_bytes()
    text = raw.decode('utf-8')
    original = text

    # Normalize direct getStackInSlot slot returns.
    def replace_get(match):
        body_start = match.end()
        depth = 1
        i = body_start
        while i < len(text) and depth:
            if text[i] == '{':
                depth += 1
            elif text[i] == '}':
                depth -= 1
            i += 1
        body = text[body_start:i - 1]
        new_body = re.sub(
            r'return\s+(?:this\.)?slots\[([^\]]+)\];',
            r'ItemStack stack = slots[\1]; return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;',
            body
        )
        return text[match.start():body_start] + new_body + text[i - 1:]

    # Normalize null returns inside decrStackSize.
    def replace_decr(match):
        body_start = match.end()
        depth = 1
        i = body_start
        while i < len(text) and depth:
            if text[i] == '{':
                depth += 1
            elif text[i] == '}':
                depth -= 1
            i += 1
        body = text[body_start:i - 1]
        new_body = body.replace('return null;', 'return ItemStack.EMPTY;')
        return text[match.start():body_start] + new_body + text[i - 1:]

    # Re-scan until stable because callbacks use the original text offsets.
    for method in ('getStackInSlot', 'decrStackSize'):
        pattern = re.compile(r'public\s+ItemStack\s+' + method + r'\s*\([^)]*\)\s*\{')
        pos = 0
        while True:
            match = pattern.search(text, pos)
            if not match:
                break
            body_start = match.end()
            depth = 1
            i = body_start
            while i < len(text) and depth:
                if text[i] == '{':
                    depth += 1
                elif text[i] == '}':
                    depth -= 1
                i += 1
            body = text[body_start:i - 1]
            if method == 'getStackInSlot':
                new_body = re.sub(
                    r'return\s+(?:this\.)?slots\[([^\]]+)\];',
                    r'ItemStack stack = slots[\1]; return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack;',
                    body
                )
            else:
                new_body = body.replace('return null;', 'return ItemStack.EMPTY;')
            text = text[:body_start] + new_body + text[i - 1:]
            pos = body_start + len(new_body) + 1

    if text != original:
        path.write_bytes(text.encode('utf-8'))
        changed.append(str(path))

print('\n'.join(changed))
print('changed=%d' % len(changed))

import re
from pathlib import Path

def transform_line(line):
    # Postfix ++
    while True:
        m = re.search(r'([a-zA-Z0-9_.\[\]()]+?)\.stackSize\+\+', line)
        if not m:
            break
        rec = m.group(1)
        line = line[:m.start()] + f'{rec}.grow(1)' + line[m.end():]

    # Postfix --
    while True:
        m = re.search(r'([a-zA-Z0-9_.\[\]()]+?)\.stackSize--', line)
        if not m:
            break
        rec = m.group(1)
        line = line[:m.start()] + f'{rec}.shrink(1)' + line[m.end():]

    # Compound assignments: +=, -=, *=
    while True:
        m = re.search(r'([a-zA-Z0-9_.\[\]()]+?)\.stackSize\s*(\+=|-=|\*=)\s*([^;]+);', line)
        if not m:
            break
        rec = m.group(1)
        op = m.group(2)
        expr = m.group(3).strip()
        expr = re.sub(r'\.stackSize\b', '.getCount()', expr)
        if op == '+=':
            replacement = f'{rec}.grow({expr});'
        elif op == '-=':
            replacement = f'{rec}.shrink({expr});'
        elif op == '*=':
            replacement = f'{rec}.setCount({rec}.getCount() * ({expr}));'
        line = line[:m.start()] + replacement + line[m.end():]

    # Simple assignment: =
    while True:
        m = re.search(r'([a-zA-Z0-9_.\[\]()]+?)\.stackSize\s*(?<![=!<>])=(?!=)\s*([^;]+);', line)
        if not m:
            break
        rec = m.group(1)
        expr = m.group(2).strip()
        expr = re.sub(r'\.stackSize\b', '.getCount()', expr)
        replacement = f'{rec}.setCount({expr});'
        line = line[:m.start()] + replacement + line[m.end():]

    # Any remaining .stackSize are reads
    line = re.sub(r'\.stackSize\b', '.getCount()', line)
    return line

def process_file(p):
    if p.name == 'TileEntityFurnaceSpace.java':
        return False
    data = p.read_bytes()
    crlf = b'\r\n' in data
    text = data.decode('utf-8', errors='replace')
    if '.stackSize' not in text:
        return False
    
    # Split preserving line breaks
    lines = text.split('\r\n' if crlf else '\n')
    new_lines = []
    changed = False
    for line in lines:
        if '.stackSize' in line:
            new_line = transform_line(line)
            if new_line != line:
                changed = True
            new_lines.append(new_line)
        else:
            new_lines.append(line)
    
    if changed:
        sep = '\r\n' if crlf else '\n'
        out_text = sep.join(new_lines)
        p.write_bytes(out_text.encode('utf-8'))
        return True
    return False

if __name__ == '__main__':
    modified = []
    for p in sorted(Path('src/main/java').rglob('*.java')):
        if process_file(p):
            modified.append(p)
    print(f'Successfully migrated {len(modified)} files.')

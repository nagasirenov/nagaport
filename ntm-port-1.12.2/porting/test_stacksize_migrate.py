import re
from pathlib import Path

def extract_receiver(line, dot_idx):
    idx = dot_idx - 1
    depth_paren = 0
    depth_bracket = 0
    delims = set(';{},+-*/=!<>?&|:\t\n')
    while idx >= 0:
        c = line[idx]
        if c == ')' or c == ']':
            open_c = '(' if c == ')' else '['
            close_c = c
            depth = 1
            idx -= 1
            while idx >= 0 and depth > 0:
                if line[idx] == close_c: depth += 1
                elif line[idx] == open_c: depth -= 1
                idx -= 1
            continue
        elif c in delims:
            break
        elif c == ' ' and (idx == 0 or line[idx-1] in delims or line[idx-1] == ' '):
            break
        elif c == ' ' and depth_paren == 0 and depth_bracket == 0:
            break
        idx -= 1
    return line[idx+1:dot_idx].strip()

def transform_line(line):
    # Exclude TileEntityFurnaceSpace
    # First handle postfix ++
    # e.g. foo.stackSize++;
    # We must match receiver.stackSize++
    # Note: receiver can be indexed like slots[i].stackSize++
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
        # if expr has .stackSize, replace with .getCount()
        expr = re.sub(r'\.stackSize\b', '.getCount()', expr)
        if op == '+=':
            replacement = f'{rec}.grow({expr});'
        elif op == '-=':
            replacement = f'{rec}.shrink({expr});'
        elif op == '*=':
            replacement = f'{rec}.setCount({rec}.getCount() * ({expr}));'
        line = line[:m.start()] + replacement + line[m.end():]

    # Simple assignment: =
    # Must NOT match ==, <=, >=, !=
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

# Test on lines from all files
total_changed = 0
changed_files = 0
for p in sorted(Path('src/main/java').rglob('*.java')):
    if p.name == 'TileEntityFurnaceSpace.java':
        continue
    content = p.read_text(encoding='utf-8', errors='replace')
    if '.stackSize' not in content:
        continue
    lines = content.splitlines()
    new_lines = []
    diffs = []
    for l_no, line in enumerate(lines, 1):
        if '.stackSize' in line:
            t = transform_line(line)
            new_lines.append(t)
            diffs.append((l_no, line, t))
        else:
            new_lines.append(line)
    if diffs:
        changed_files += 1
        total_changed += len(diffs)
        print(f'=== {p.relative_to("src/main/java")} ({len(diffs)} diffs) ===')
        for l_no, old, new in diffs[:3]:
            print(f'  L{l_no} OLD: {old.strip()}')
            print(f'  L{l_no} NEW: {new.strip()}')
        if len(diffs) > 3:
            print(f'  ... and {len(diffs)-3} more')

print(f'\nTotal files to change: {changed_files}, lines changed: {total_changed}')

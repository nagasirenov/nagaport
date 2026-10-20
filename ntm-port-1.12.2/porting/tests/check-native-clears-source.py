#!/usr/bin/env python3
"""Exact source-only native clear audit. Does not claim compilation/gameplay coverage.

reverse_source composes before older baseline reversals. Whole-file hashes reject
unrelated edits; recorded offsets reverse only the reviewed literal arguments.
Default mode also rejects every remaining literal native clear throughout source.
--migration-only isolates this batch while separately owned calls are in flight.
"""
import hashlib
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MANIFEST = json.loads((ROOT / 'porting/tests/native-clear-source-manifest.json').read_text())
FILES = {entry['path']: entry for entry in MANIFEST['files']}

def require(ok, detail):
    if not ok:
        raise AssertionError(detail)

def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

def native_null_calls(text):
    # Mask comments and literals without changing offsets or line numbers.
    masked = re.sub(r'//[^\r\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',
                    lambda m: re.sub(r'[^\r\n]', ' ', m.group()), text)
    found = []
    for match in re.finditer(r'\b(putStack|setInventorySlotContents)\s*\(', masked):
        start = match.end(); depth = 1; args = []; arg_start = start
        for pos in range(start, len(masked)):
            char = masked[pos]
            if char in '([{': depth += 1
            elif char in ')]}':
                depth -= 1
                if depth == 0:
                    args.append((arg_start, pos)); break
            elif char == ',' and depth == 1:
                args.append((arg_start, pos)); arg_start = pos + 1
        else:
            raise AssertionError(('unbalanced native call', match.start()))
        index = 0 if match.group(1) == 'putStack' else 1
        if len(args) != index + 1: continue
        a, b = args[index]
        if masked[a:b].strip() == 'null':
            found.append((text[:match.start()].count('\n') + 1, text[match.start():pos+1]))
    return found

def reverse_source(relative, text):
    entry = FILES.get(relative)
    if entry is None: return text
    require(sha(text) == entry['after_sha256'], (relative, 'native-clear migrated whole-file drift'))
    offsets = []; delta = 0
    for edit in entry['edits']:
        offsets.append(edit['offset'] + delta)
        delta += len(edit['new']) - len(edit['old'])
    original = text
    for edit, offset in reversed(list(zip(entry['edits'], offsets))):
        require(original[offset:offset + len(edit['new'])] == edit['new'], (relative, 'exact clear slice drift'))
        original = original[:offset] + edit['old'] + original[offset + len(edit['new']):]
    require(sha(original) == entry['before_sha256'], (relative, 'native-clear baseline whole-file drift'))
    require(len(native_null_calls(original)) == len(entry['edits']), (relative, 'baseline call coverage'))
    return original

def main():
    require(len(FILES) == 17, '17 exact native-clear files')
    require(sum(len(e['edits']) for e in FILES.values()) == 19, '19 exact native-clear arguments')
    for relative, entry in FILES.items():
        text = (ROOT / relative).read_bytes().decode('utf-8')
        original = reverse_source(relative, text)
        require(not native_null_calls(text), (relative, 'unconverted clear'))
        for drift in (text + '\n', original, text.replace('ItemStack.EMPTY', 'null', 1)):
            try: reverse_source(relative, drift)
            except AssertionError: pass
            else: raise AssertionError((relative, 'drift not rejected'))
    require(native_null_calls('slot.putStack( /* absent */ null );\ninv.setInventorySlotContents(f(a,b), null);') ==
            [(1, 'putStack( /* absent */ null )'), (2, 'setInventorySlotContents(f(a,b), null)')], 'nested/comment parser')
    require(not native_null_calls('// slot.putStack(null);\nString x="putStack(null)";\nslots[0] = null;\nhelper(null);\ninv.setInventorySlotContents(i, x == null ? ItemStack.EMPTY : x);'), 'domain and comment exclusion')
    print('PASS: 17 exact whole-file reversals; 19 null-only argument edits; 51 drift rejections; lexical call audit fixtures.')
    if '--migration-only' not in sys.argv:
        remaining = [(p.relative_to(ROOT).as_posix(), line, call)
                     for p in sorted((ROOT / 'src/main/java').rglob('*.java'))
                     for line, call in native_null_calls(p.read_bytes().decode('utf-8'))]
        require(not remaining, ('remaining native literal-null clears', remaining))
        print('PASS: zero literal-null Slot/IInventory clears throughout src/main/java.')

if __name__ == '__main__':
    main()

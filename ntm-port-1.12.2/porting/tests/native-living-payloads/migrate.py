#!/usr/bin/env python3
"""Apply only checked compiler spans; reads, statement prefixes and matching closes."""
import argparse, base64, collections, hashlib, json
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
BASELINE = 'd5ef20a8f1cb65fbd06faa86a4901870c8bab3f2'
EXPECTED = {('LivingHurtEvent', 'READ_AMOUNT'): 7, ('LivingHurtEvent', 'READ_SOURCE'): 15,
            ('LivingHurtEvent', 'SET'): 10, ('LivingHurtEvent', 'MULTIPLY'): 7,
            ('LivingHurtEvent', 'SUBTRACT'): 3, ('LivingHurtEvent', 'CLOSE'): 20,
            ('LivingAttackEvent', 'READ_AMOUNT'): 4, ('LivingAttackEvent', 'READ_SOURCE'): 6}

def length(text):
    return len(text.encode('utf-16-le')) // 2

def parse_rows(text):
    rows = []
    for line in text.splitlines():
        path, start, end, new, kind, owner, receiver, method, old, group = line.split('\t')
        rows.append(dict(path=path, start=int(start), end=int(end),
                         new=base64.b64decode(new).decode(), old=base64.b64decode(old).decode(),
                         kind=kind, owner=owner, receiver=receiver, method=method, group=int(group)))
    return rows

def apply(raw, edits):
    data = raw.decode().encode('utf-16-le')
    previous = -1
    shift = 0
    for edit in sorted(edits, key=lambda e: (e['start'], e['end'])):
        assert edit['start'] >= previous, 'overlapping source edits'
        assert data[edit['start']*2:edit['end']*2].decode('utf-16-le') == edit['old'], edit
        previous = edit['end']
        edit['after_start'] = edit['start'] + shift
        edit['after_end'] = edit['after_start'] + length(edit['new'])
        shift += length(edit['new']) - (edit['end'] - edit['start'])
    for edit in sorted(edits, key=lambda e: (e['start'], e['end']), reverse=True):
        data = data[:edit['start']*2] + edit['new'].encode('utf-16-le') + data[edit['end']*2:]
    return data.decode('utf-16-le').encode()

def inventory(files):
    return collections.Counter((f['path'], e['kind'], e['owner'], e['receiver'], e['method']) for f in files for e in f['edits'])

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('attribution')
    args = parser.parse_args()
    rows = parse_rows(Path(args.attribution).read_text())
    actual = collections.Counter((e['owner'].split('.')[-1], e['kind']) for e in rows)
    assert actual == EXPECTED, (actual, EXPECTED)
    paths = sorted(set(e['path'] for e in rows))
    assert len(paths) == 14
    planned = []
    for path in paths:
        target = Path(path)
        assert target.is_relative_to(ROOT)
        edits = sorted(({k: v for k, v in e.items() if k != 'path'} for e in rows if e['path'] == path), key=lambda e: (e['start'], e['end']))
        raw = target.read_bytes()
        changed = apply(raw, edits)
        planned.append((target, changed, dict(path=str(target.relative_to(ROOT)), before=hashlib.sha256(raw).hexdigest(), after=hashlib.sha256(changed).hexdigest(), edits=edits)))
    manifest = HERE / 'source-manifest.json'
    if manifest.exists():
        assert inventory(json.loads(manifest.read_text())['files']) == inventory([x[2] for x in planned]), 'approved typed inventory changed'
    for target, changed, _ in planned:
        target.write_bytes(changed)
    manifest.write_text(json.dumps(dict(baseline=BASELINE, accesses=52, files=[x[2] for x in planned]), indent=2) + '\n')
    print('Applied 52 attributed payload accesses in 14 files: 20 statement stores, 32 reads; no other bytes changed')

if __name__ == '__main__':
    main()

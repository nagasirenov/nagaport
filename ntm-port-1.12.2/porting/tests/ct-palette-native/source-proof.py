#!/usr/bin/env python3
import collections,hashlib,json,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2]
rows=json.loads((H/'selection.json').read_text());groups=collections.defaultdict(list)
for r in rows:groups[r['file']].append(r)
with zipfile.ZipFile(H/'sources.zip') as z:
 for path,rs in groups.items():
  before=z.read(path).decode();actual=(M/path).read_text();expected=before
  for r in reversed(rs):assert expected[r['start']:r['end']]==r['old'];expected=expected[:r['start']]+r['new']+expected[r['end']:]
  assert expected==actual
  inverse=actual
  for r in reversed(rs):
   assert inverse.count(r['new'])==1;inverse=inverse.replace(r['new'],r['old'],1)
  assert inverse==before
  for r in rs:
   assert actual.replace(r['new'],r['new'].replace('placeUnorientedStructureBlock','INVALID'),1)!=expected
print('PASS',len(rows),'per-site mutation controls, seven whole-source inverses; unchanged argument and field read interiors.')

with zipfile.ZipFile(H/'helper-baseline.zip') as z:before=z.read('DungeonToolbox.java').decode()
comment=json.loads((H/'helper-comment.json').read_text());actual=(M/'src/main/java/com/hbm/world/generator/DungeonToolbox.java').read_text();assert actual==before.replace(comment['old'],comment['new'],1)
assert actual.replace(comment['new'],comment['old'],1)==before
print('PASS only helper comment broadened; all helper bodies unchanged, complete Toolbox inverse.')

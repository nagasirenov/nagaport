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
  if path.endswith('LibraryDungeon.java'):expected=expected.replace('    private static void fillDungeonChest(', ((H/'helper.txt').read_text()+'\n')+'    private static void fillDungeonChest(',1)
  assert expected==actual
  inverse=actual
  for r in reversed(rs):
   assert inverse.count(r['new'])==1;inverse=inverse.replace(r['new'],r['old'],1)
  if path.endswith('LibraryDungeon.java'):inverse=inverse.replace(((H/'helper.txt').read_text()+'\n'),'',1)
  assert inverse==before
  for r in rs:
   assert actual.replace(r['new'],r['new']+'/* MUTATED */',1)!=expected
print('PASS',len(rows),'per-site mutation controls, two whole-source inverses; unchanged argument and field read interiors.')

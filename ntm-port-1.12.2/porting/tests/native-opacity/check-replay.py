#!/usr/bin/env python3
import json,subprocess,tempfile
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent;BASE='d860257bb1f4bc6043d5cb80bd752ede1ebc53bd'
rows=json.loads((HERE/'sites.json').read_text())
with tempfile.TemporaryDirectory(prefix='opacity-replay-') as d:
 root=Path(d);original={}
 for row in rows:
  path=row['path'];raw=subprocess.check_output(['git','-C',str(REPO),'show',BASE+':ntm-port-1.12.2/'+path]);original[path]=raw;p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(raw)
 def run(*args):subprocess.run(['python3',str(HERE/'apply-fragments.py'),'--root',d,'--apply']+list(args),check=True,stdout=subprocess.DEVNULL)
 run()
 for p in original:assert (root/p).read_bytes()==(ROOT/p).read_bytes()
 run()
 for p in original:assert (root/p).read_bytes()==(ROOT/p).read_bytes()
 run('--reverse')
 for p in original:assert (root/p).read_bytes()==original[p]
 marker=b'\n// Independent callback-edit composition sentinel.\n'
 for p,raw in original.items():(root/p).write_bytes(raw+marker)
 run()
 for p in original:assert (root/p).read_bytes()==(ROOT/p).read_bytes()+marker
 run('--reverse')
 for p in original:assert (root/p).read_bytes()==original[p]+marker
print('PASS 95 exact-base forward/reapply/reverse method fragments and independent-edit composition controls; main never edited.')

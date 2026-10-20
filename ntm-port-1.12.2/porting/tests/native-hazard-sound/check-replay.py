#!/usr/bin/env python3
import json,subprocess,tempfile
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
BASE=subprocess.check_output(['git','-C',str(REPO),'rev-parse','c986d3f3'],text=True).strip()
paths=['src/main/java/com/hbm/blocks/ModBlocks.java']+list(json.loads((HERE/'additions.json').read_text()))
with tempfile.TemporaryDirectory(prefix='hazard-fragment-replay-') as d:
 root=Path(d);original={}
 for path in paths:
  raw=subprocess.check_output(['git','-C',str(REPO),'show',BASE+':ntm-port-1.12.2/'+path]);original[path]=raw
  p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(raw)
 def run(*extra):return subprocess.check_output(['python3',str(HERE/'apply-fragments.py'),'--root',d,'--apply']+list(extra),text=True)
 run()
 for path in paths:assert (root/path).read_bytes()==(ROOT/path).read_bytes(),path
 run()
 for path in paths:assert (root/path).read_bytes()==(ROOT/path).read_bytes(),path
 run('--reverse')
 for path in paths:assert (root/path).read_bytes()==original[path],path
 # Composition control: unrelated byte changes remain in place, never overwritten.
 marker=b'\n// Independent composition sentinel.\n'
 for path in paths:(root/path).write_bytes(original[path]+marker)
 run()
 for path in paths:assert (root/path).read_bytes()==(ROOT/path).read_bytes()+marker,path
 run('--reverse')
 for path in paths:assert (root/path).read_bytes()==original[path]+marker,path
print('PASS exact-base forward/reapply/reverse three-file replay and independent-edit composition controls; no main files edited.')

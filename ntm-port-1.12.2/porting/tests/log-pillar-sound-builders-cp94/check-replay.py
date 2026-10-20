#!/usr/bin/env python3
import gzip,importlib.util,subprocess,tempfile
import sys
sys.dont_write_bytecode=True
from pathlib import Path
HERE=Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('guard',HERE/'check-source.py');g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g)
old,new=g.inputs(True);assert g.validate(old,new)==3
patch=gzip.decompress((HERE/'production.patch.gz').read_bytes())
with tempfile.TemporaryDirectory(prefix='log-pillar-replay-') as d:
 t=Path(d)
 for path,data in old.items():
  p=t/'ntm-port-1.12.2'/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data)
 def apply(*args,ok=True):
  r=subprocess.run(['git','apply',*args,'-'],input=patch,cwd=t,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
  assert (r.returncode==0)==ok,r.stderr.decode()
 apply('--check');apply()
 for path,data in new.items():assert (t/'ntm-port-1.12.2'/path).read_bytes()==data
 apply('--check',ok=False);apply('--reverse','--check');apply('--reverse')
 for path,data in old.items():assert (t/'ntm-port-1.12.2'/path).read_bytes()==data
print('PASS production patch dry-run, forward byte-exact replay, duplicate rejection, reverse dry-run and byte-exact whole-file reversal; main worktree untouched.')

#!/usr/bin/env python3
import argparse,json,subprocess,sys
from pathlib import Path
HERE=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--java-home',required=True);p.add_argument('--classpath-file',required=True);a=p.parse_args();results=[]
for owner in ['BlockLogNT','BlockRotatablePillar']:
 for mutant in ['drop-assignment','wrong-argument','null-return']:
  command=[sys.executable,str(HERE/'run-native.py'),'--java-home',a.java_home,'--classpath-file',a.classpath_file,'--mutant',mutant,'--mutant-owner',owner]
  result=subprocess.run(command,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
  assert result.returncode!=0 and 'COMPILE_PASS:' in result.stdout and 'Caused by: java.lang.AssertionError' in result.stdout or (result.returncode!=0 and 'COMPILE_PASS:' in result.stdout and 'Caused by: java.lang.NullPointerException' in result.stdout),result.stdout
  reason=next(l.strip() for l in result.stdout.splitlines() if 'Caused by:' in l)
  results.append(dict(owner=owner,mutant=mutant,compiled=True,rejected_at_runtime=True,reason=reason))
  print('PASS runtime mutant rejected after successful compilation:',owner,mutant,reason,flush=True)
(HERE/'mutation-results.json').write_text(json.dumps(results,indent=2)+'\n')

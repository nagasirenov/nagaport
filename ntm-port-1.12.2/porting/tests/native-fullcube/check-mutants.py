#!/usr/bin/env python3
import subprocess,sys
from pathlib import Path
h=Path(__file__).resolve().parent
for mutant,expected in [('old-signature','Caused by: java.lang.AssertionError: null argument remains ignored'),('true-body','Caused by: java.lang.AssertionError: null argument remains ignored'),('state-dependent','Caused by: java.lang.NullPointerException')]:
 p=subprocess.run([sys.executable,str(h/'run-native.py')]+sys.argv[1:]+['--mutant',mutant],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
 assert p.returncode!=0 and expected in p.stdout and 'error:' not in p.stdout,(mutant,p.stdout)
 print('PASS',mutant,'compiled and runtime rejected:',expected)

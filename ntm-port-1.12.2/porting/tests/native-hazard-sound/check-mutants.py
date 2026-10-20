#!/usr/bin/env python3
import subprocess,sys
from pathlib import Path
here=Path(__file__).resolve().parent
for mutant,expected in [('drop-assignment','Caused by: java.lang.AssertionError: exact original sound argument identity'),('wrong-argument','Caused by: java.lang.AssertionError: exact original sound argument identity'),('null-return','Caused by: java.lang.NullPointerException')]:
 result=subprocess.run([sys.executable,str(here/'run-native.py')]+sys.argv[1:]+['--mutant',mutant],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
 assert result.returncode!=0 and expected in result.stdout,(mutant,result.stdout)
 assert 'error:' not in result.stdout,'A compiler error is not a runtime mutation kill'
 print('PASS',mutant,'rejected by',expected)

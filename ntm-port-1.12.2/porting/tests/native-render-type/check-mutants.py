import argparse,subprocess,sys
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--root',required=True);p.add_argument('--java-home',required=True);p.add_argument('--classpath-file',required=True);p.add_argument('--legacy-jar',required=True);a=p.parse_args();E=Path(__file__).resolve().parent
base=[sys.executable,str(E/'run-native.py')]
for k,v in vars(a).items():base+=['--'+k.replace('_','-'),v]
for mutant,expect in [('old-signature','method does not override'),('model','AssertionError: native state dispatch'),('state-dependent','AssertionError: native state dispatch')]:
 r=subprocess.run(base+['--mutant',mutant],stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True);assert r.returncode!=0 and expect in r.stdout,(mutant,r.stdout);print('PASS rejected '+mutant+': '+expect)

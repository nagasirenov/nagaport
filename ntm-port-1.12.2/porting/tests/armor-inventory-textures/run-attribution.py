#!/usr/bin/env python3
"""Analyze source symbols only, no class emission and no full-mod build."""
import argparse,subprocess,tempfile,tarfile,io
from pathlib import Path
import migrate
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);a=p.parse_args()
with tempfile.TemporaryDirectory(prefix='armor-texture-attribution-') as d:
 t=Path(d);archive=subprocess.check_output(['git','-C',str(REPO),'archive',migrate.BASE,'ntm-port-1.12.2/src/main/java'])
 with tarfile.open(fileobj=io.BytesIO(archive)) as z:z.extractall(t,filter='data')
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(a.java_home/'lib/tools.jar'),'-d',d,str(HERE/'ArmorTextureAttribution.java')],check=True)
 for name,root in [('before',t/'ntm-port-1.12.2'),('after',ROOT)]:
  with (HERE/(name+'.tsv')).open('w') as out,(HERE/(name+'.log')).open('w') as err:
   args=[str(a.java_home/'bin/java'),'-Xmx3g','-cp',d+':'+str(a.java_home/'lib/tools.jar'),'ArmorTextureAttribution',str(root),str(a.classpath_file),str(HERE/'sites.tsv'),str(HERE/(name+'-errors.tsv'))]
   if name=='after':args.extend([str(HERE/'owners.tsv'),str(HERE/'owners.txt')])
   subprocess.run(args,stdout=out,stderr=err,check=True)

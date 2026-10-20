#!/usr/bin/env python3
import argparse,subprocess,tempfile,tarfile,io
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);a=p.parse_args()
with tempfile.TemporaryDirectory(prefix='plain-cube-attribution-') as d:
 t=Path(d)
 archive=subprocess.check_output(['git','-C',str(REPO),'archive','a040cd6b46901a209b8e5db808b980dccc4981c5','ntm-port-1.12.2/src/main/java'])
 with tarfile.open(fileobj=io.BytesIO(archive)) as z:z.extractall(t,filter='data')
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(a.java_home/'lib/tools.jar'),'-d',d,str(HERE/'TextureAttribution.java')],check=True)
 for name,root in [('before',t/'ntm-port-1.12.2'),('after',ROOT)]:
  with (HERE/(name+'.tsv')).open('w') as out,(HERE/(name+'.log')).open('w') as err:
   subprocess.run([str(a.java_home/'bin/java'),'-Xmx3g','-cp',d+':'+str(a.java_home/'lib/tools.jar'),'TextureAttribution',str(root),str(a.classpath_file),str(HERE/'sites.tsv'),str(HERE/(name+'-errors.tsv'))],stdout=out,stderr=err,check=True)

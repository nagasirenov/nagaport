#!/usr/bin/env python3
import argparse,subprocess,tempfile,tarfile,io
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);a=p.parse_args()
with tempfile.TemporaryDirectory(prefix='fullcube-attribution-') as d:
 t=Path(d);archive=subprocess.check_output(['git','-C',str(REPO),'archive','91da806c9ad17b279ef5142d83d4fe60365d563f','ntm-port-1.12.2/src/main/java'])
 with tarfile.open(fileobj=io.BytesIO(archive)) as z:z.extractall(t,filter='data')
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(a.java_home/'lib/tools.jar'),'-d',d,str(HERE/'FullCubeAttribution.java')],check=True)
 for name,root in [('before',t/'ntm-port-1.12.2'),('after',ROOT)]:
  with (HERE/(name+'.log')).open('w') as out:
   subprocess.run([str(a.java_home/'bin/java'),'-Xmx2g','-cp',d+':'+str(a.java_home/'lib/tools.jar'),'FullCubeAttribution',str(root),str(a.classpath_file),str(HERE/name)],stdout=out,stderr=subprocess.STDOUT,check=True)

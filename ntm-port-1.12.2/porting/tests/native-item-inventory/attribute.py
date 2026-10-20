#!/usr/bin/env python3
"""Analyze complete source before/after without emitting mod bytecode or invoking Gradle."""
from pathlib import Path
import subprocess,tempfile,tarfile,io
import migrate
H=Path(__file__).resolve().parent;R=H.parents[2];J=Path('/workspace/shared/nagaport-toolchain/java8');CP=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt')
with tempfile.TemporaryDirectory(prefix='item-inventory-attribution-') as tmp:
 t=Path(tmp); archive=subprocess.check_output(['git','-C',str(R.parent),'archive',migrate.BASE,'ntm-port-1.12.2/src/main/java'])
 with tarfile.open(fileobj=io.BytesIO(archive)) as z:z.extractall(t,filter='data')
 subprocess.run([str(J/'bin/javac'),'-cp',str(J/'lib/tools.jar'),'-d',tmp,str(H/'InventoryScanner.java')],check=True)
 for name,root in [('before',t/'ntm-port-1.12.2'),('after',R)]:
  with (H/(name+'.tsv')).open('w') as out,(H/(name+'.log')).open('w') as err:
   subprocess.run([str(J/'bin/java'),'-Xmx3g','-cp',tmp+':'+str(J/'lib/tools.jar'),'InventoryScanner',str(root),str(CP),str(H/(name+'-errors.tsv'))],stdout=out,stderr=err,check=True)


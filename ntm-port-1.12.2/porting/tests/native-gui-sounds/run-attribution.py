#!/usr/bin/env python3
"""Reproduce the isolated CP63 before/after complete-source attribution without modifying sealed proof."""
import argparse,hashlib,json,shutil,subprocess,tempfile,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--repo',type=Path,required=True);p.add_argument('--classpath-file',default='/tmp/nagaport-ae2-native-classpath.txt');p.add_argument('--java-home',type=Path,default='/workspace/shared/nagaport-toolchain/java8');p.add_argument('--out',type=Path,required=True);a=p.parse_args();e=Path(__file__).resolve().parent;m=json.loads((e/'manifest.json').read_text());a.out.mkdir(parents=True,exist_ok=True);(a.out/'classes').mkdir(exist_ok=True);overlay=a.out/'overlay';overlay.mkdir(exist_ok=True);j=a.java_home/'bin';src=a.repo/'ntm-port-1.12.2/src/main/java';prefix='ntm-port-1.12.2/src/main/java/'
subprocess.run(['git','-C',str(a.repo),'archive','--format=zip','-o',str(a.out/'source.zip'),m['base'],prefix],check=True)
# Keep the isolated proof fixed even if integrated GUI files gain independent edits.
with zipfile.ZipFile(e/'sources.zip') as z:
 for name,info in m['files'].items():
  b=z.read(name);t=b.decode();marker=m['old_selector'];new=m['new_selector'];spans=[(int(x['select_start'])*2,int(x['select_end'])*2) for x in m['calls'] if x['path']==name];u=t.encode('utf-16-le')
  for start,end in sorted(spans,reverse=True):assert u[start:end].decode('utf-16-le')==marker;u=u[:start]+new.encode('utf-16-le')+u[end:]
  out=u.decode('utf-16-le').encode();assert hashlib.sha256(out).hexdigest()==info['after'];dest=overlay/name;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(out)
helper=src/'com/hbm/sound/GuiSound.java';assert hashlib.sha256(helper.read_bytes()).hexdigest()==m['fixed_sources']['src/main/java/com/hbm/sound/GuiSound.java'];dest=overlay/'com/hbm/sound/GuiSound.java';dest.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(helper,dest)
subprocess.run([str(j/'javac'),'-cp',str(a.java_home/'lib/tools.jar'),'-d',str(a.out/'classes'),str(e/'GuiSoundAttribution.java')],check=True)
for phase,o in [('before','-'),('after',str(overlay))]:
 with (a.out/(phase+'.tsv')).open('w') as out,(a.out/(phase+'.log')).open('w') as log:subprocess.run([str(j/'java'),'-Xmx4g','-cp',str(a.out/'classes')+':'+str(a.java_home/'lib/tools.jar'),'GuiSoundAttribution',str(a.out/'source.zip'),a.classpath_file,'-',str(a.out/(phase+'-diagnostics.tsv')),o],stdout=out,stderr=log,check=True)
print('Full isolated genuine-source records written to '+str(a.out))

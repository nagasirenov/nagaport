#!/usr/bin/env python3
import argparse,hashlib,json,subprocess,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--repo',type=Path,required=True);p.add_argument('--out',type=Path,required=True);p.add_argument('--classpath-file',default='/tmp/nagaport-ae2-native-classpath.txt');p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));a=p.parse_args();e=Path(__file__).resolve().parent;m=json.loads((e/'manifest.json').read_text());a.out.mkdir(parents=True,exist_ok=True);cl=a.out/'classes';cl.mkdir(exist_ok=True);overlay=a.out/'overlay';overlay.mkdir(exist_ok=True);j=a.java_home/'bin';sha=lambda b:hashlib.sha256(b).hexdigest()
subprocess.run(['git','-C',str(a.repo),'archive','--format=zip','-o',str(a.out/'source.zip'),m['base'],'ntm-port-1.12.2/src/main/java'],check=True)
with zipfile.ZipFile(e/'sources.zip') as z:
 for rel,info in m['files'].items():
  s=z.read(rel).decode().encode('utf-16-le')
  for c in sorted([x for x in m['calls'] if x['path']==rel],key=lambda x:int(x['start']),reverse=True):
   start,end=int(c['start'])*2,int(c['end'])*2;assert s[start:end].decode('utf-16-le')==c['full_invocation'];s=s[:start]+c['new_invocation'].encode('utf-16-le')+s[end:]
  b=s.decode('utf-16-le').encode();assert sha(b)==info['after'];dest=overlay/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(b)
 rel='com/hbm/main/ModSoundEvents.java';s=z.read(rel).decode();anchor='\n\t}\n\n\tprivate static void worldEffect(String path, SoundCategory category) {';s=s.replace(anchor,m['registry']['inserted_block']+anchor);assert sha(s.encode())==m['registry']['after'];dest=overlay/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(s)
subprocess.run([str(j/'javac'),'-cp',str(a.java_home/'lib/tools.jar'),'-d',str(cl),str(e/'WorldSoundWideningAttribution.java')],check=True)
for phase,o,sites in [('before','-','-'),('after',str(overlay),str(e/'sites.tsv'))]:
 with (a.out/(phase+'.tsv')).open('w') as out,(a.out/(phase+'.log')).open('w') as log:subprocess.run([str(j/'java'),'-Xmx4g','-cp',str(cl)+':'+str(a.java_home/'lib/tools.jar'),'WorldSoundWideningAttribution',str(a.out/'source.zip'),a.classpath_file,sites,str(a.out/(phase+'-diagnostics.tsv')),o],stdout=out,stderr=log,check=True)
print('Isolated full CP64 attribution replay written to '+str(a.out))

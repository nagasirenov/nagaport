#!/usr/bin/env python3
"""Original/CP86 complete Java source declaration audit, with exact retained evidence."""
from pathlib import Path
import hashlib,json,subprocess,tempfile
H=Path(__file__).resolve().parent;R=H.parents[3]
J=Path('/workspace/shared/nagaport-toolchain/java8');CP='/workspace/shared/nagaport-toolchain/native-classpath.txt'
BASE='fc2cd2dba34caba0dba74ed288f95107ab4dc979'
NAMES=[b'getEquipmentInSlot',b'getCurrentArmor',b'getItemStackFromSlot',b'armorItemInSlot',b'getCurrentItemOrArmor',b'getLastActiveItems',b'setCurrentItemOrArmor']
def census(prefix,temp):
 entries=subprocess.check_output(['git','ls-tree','-r',BASE,'--',prefix],cwd=R).decode().splitlines()
 entries=[(l.split()[2],l.split('\t',1)[1]) for l in entries if l.endswith('.java')]
 request=''.join(blob+'\n' for blob,path in entries).encode()
 result=subprocess.check_output(['git','cat-file','--batch'],input=request,cwd=R);pos=0;files=[];candidate_hashes={};all_hashes={}
 for blob,path in entries:
  end=result.index(b'\n',pos);size=int(result[pos:end].split()[-1]);b=result[end+1:end+1+size];pos=end+size+2
  all_hashes[path]=hashlib.sha256(b).hexdigest()
  if not any(n in b for n in NAMES) and b'\\u' not in b:continue
  dest=temp/path;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(b);files.append(dest);candidate_hashes[path]=all_hashes[path]
 inputs=temp/'inputs';inputs.write_text('\n'.join(map(str,files))+'\n')
 out=subprocess.run([str(J/'bin/java'),'-cp',str(temp/'classes')+':'+str(J/'lib/tools.jar'),'EquipmentReadScanner',str(inputs),CP,'--audit-only'],capture_output=True,text=True,check=True)
 assert 'Override declaration audit:' not in out.stderr,out.stderr
 return {'source_units':len(entries),'candidate_units':len(files),'candidate_hashes':candidate_hashes,'all_sources_sha256':hashlib.sha256(json.dumps(all_hashes,sort_keys=True).encode()).hexdigest(),'owned_hook_declarations':0}
with tempfile.TemporaryDirectory(prefix='equipment-source-audit-') as d:
 d=Path(d);(d/'classes').mkdir();subprocess.run([str(J/'bin/javac'),'-cp',str(J/'lib/tools.jar'),'-d',str(d/'classes'),str(H/'EquipmentReadScanner.java')],check=True)
 result={}
 for name,prefix in [('original','ntm-original-pristine/src/main/java/'),('cp86','ntm-port-1.12.2/src/main/java/')]:
  result[name]=census(prefix,d)
  print('PASS',name,result[name]['source_units'],'Java units screened;',result[name]['candidate_units'],'complete candidate/Unicode units parsed; zero owned read/dispatch hook declarations')
 target=H/'declaration-audit.json'
 if target.exists():assert json.loads(target.read_text())==result,'declaration evidence drift'
 else:target.write_text(json.dumps(result,indent=2)+'\n')

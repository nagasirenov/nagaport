from pathlib import Path
import subprocess,hashlib,json,difflib
root=Path('/workspace/shared/nagaport');module=root/'ntm-port-1.12.2';tests=module/'porting/tests';base='c2f60149e9a9fb836bade505fe566d40a54ef3cc'
def git(args):return subprocess.check_output(['git','-C',str(root)]+args)
paths=git(['diff','--name-only',base,'--','ntm-port-1.12.2/src/main/java']).decode().splitlines();m={'baseline':base,'files':{},'new_files':{},'deleted_files':{},'binary_files':{}}
for p in paths:
 exists=subprocess.run(['git','-C',str(root),'cat-file','-e',base+':'+p],stderr=subprocess.DEVNULL).returncode==0
 new=(root/p).read_bytes().decode()
 if not exists:
  m['new_files'][str(Path(p).relative_to('ntm-port-1.12.2'))]=hashlib.sha256(new.encode()).hexdigest();continue
 old=git(['show',base+':'+p]).decode();ed=[]
 ol=old.splitlines(keepends=True);nl=new.splitlines(keepends=True)
 oo=[0];no=[0]
 for line in ol:oo.append(oo[-1]+len(line))
 for line in nl:no.append(no[-1]+len(line))
 for tag,i,j,a,b in difflib.SequenceMatcher(None,ol,nl).get_opcodes():
  if tag!='equal':ed.append({'before_offset':oo[i],'after_offset':no[a],'old':old[oo[i]:oo[j]],'new':new[no[a]:no[b]]})
 m['files'][str(Path(p).relative_to('ntm-port-1.12.2'))]={'before_sha256':hashlib.sha256(old.encode()).hexdigest(),'after_sha256':hashlib.sha256(new.encode()).hexdigest(),'edits':ed}
raw=(json.dumps(m,indent=2,ensure_ascii=False)+'\n').encode();(tests/'native-shoot-sync-machine-source-manifest.json').write_bytes(raw)
original=git(['show',base+':ntm-port-1.12.2/porting/tests/check-native-equipment-destruction-name-source.py']).decode()
new=original.replace('native-equipment-destruction-name-source-manifest.json','native-shoot-sync-machine-source-manifest.json').replace("'b0cc59c48f61b822ce301f0a70eb4dcce75774247ad018ea8e2d9cb2018c15b6'",repr(hashlib.sha256(raw).hexdigest())).replace('fc2cd2dba34caba0dba74ed288f95107ab4dc979',base).replace('len(FILES)==78 and len(NEW_FILES)==2',f'len(FILES)=={len(m["files"])} and len(NEW_FILES)=={len(m["new_files"])}').replace('Native equipment/destruction/name source: 78',f'Native shoot/sync/machine source: {len(m["files"])}').replace('2 new,',f'{len(m["new_files"])} new,').replace('CP87 exact reversible equipment/destruction/name','CP88 exact reversible shoot/sync/machine')
(tests/'check-native-shoot-sync-machine-source.py').write_text(new)
wrapper=original.replace('from pathlib import Path\n','from pathlib import Path\nimport importlib.util\n_spec=importlib.util.spec_from_file_location("cp88_boundary",Path(__file__).with_name("check-native-shoot-sync-machine-source.py"))\n_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n').replace('def reverse_source(relative,text):\n','def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n').replace('    rejected=0\n','    _next.verify(root,verbose)\n    rejected=0\n').replace('assert forward==current','assert forward==_next.reverse_source(relative,current)')
(tests/'check-native-equipment-destruction-name-source.py').write_text(wrapper)
print('Prepared exact',len(m['files']),'existing and',len(m['new_files']),'new source boundaries against CP87')

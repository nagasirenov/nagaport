from pathlib import Path
import subprocess,hashlib,json,difflib
root=Path('/workspace/shared/nagaport');module=root/'ntm-port-1.12.2';tests=module/'porting/tests';base='a65d3acbf05634c3cedbce5eff43ad0bd15cefdb'
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
raw=(json.dumps(m,indent=2,ensure_ascii=False)+'\n').encode();(tests/'native-event-config-sync-source-manifest.json').write_bytes(raw)
original=git(['show',base+':ntm-port-1.12.2/porting/tests/check-native-shoot-sync-machine-source.py']).decode()
new=original.replace('native-shoot-sync-machine-source-manifest.json','native-event-config-sync-source-manifest.json').replace("'aaaa577f049a03f783703fec6e37a3849bb875ae2bfb21e5bb4c9dc39faa9284'",repr(hashlib.sha256(raw).hexdigest())).replace('c2f60149e9a9fb836bade505fe566d40a54ef3cc',base).replace('len(FILES)==34 and len(NEW_FILES)==1',f'len(FILES)=={len(m["files"])} and len(NEW_FILES)=={len(m["new_files"])}').replace('Native shoot/sync/machine source: 34',f'Native event/config/sync source: {len(m["files"])}').replace('1 new,',f'{len(m["new_files"])} new,').replace('CP88 exact reversible shoot/sync/machine','CP89 exact reversible event/config/sync')
(tests/'check-native-event-config-sync-source.py').write_text(new)
wrapper=original.replace('from pathlib import Path\n','from pathlib import Path\nimport importlib.util\n_spec=importlib.util.spec_from_file_location("cp89_boundary",Path(__file__).with_name("check-native-event-config-sync-source.py"))\n_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n').replace('def reverse_source(relative,text):\n','def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n').replace('    rejected=0\n','    _next.verify(root,verbose)\n    rejected=0\n').replace('assert forward==current','assert forward==_next.reverse_source(relative,current)')
(tests/'check-native-shoot-sync-machine-source.py').write_text(wrapper)
print('Prepared exact',len(m['files']),'existing and',len(m['new_files']),'new source boundaries against CP88')

from pathlib import Path
import subprocess,hashlib,json,difflib
root=Path('/workspace/shared/nagaport');module=root/'ntm-port-1.12.2';tests=module/'porting/tests';base='d5ef20a8f1cb65fbd06faa86a4901870c8bab3f2'
def git(args):return subprocess.check_output(['git','-C',str(root)]+args)
paths=git(['diff','--name-only',base,'--','ntm-port-1.12.2/src/main/java']).decode().splitlines();m={'baseline':base,'files':{},'new_files':{},'deleted_files':{},'binary_files':{}}
for p in paths:
 old=git(['show',base+':'+p]).decode();new=(root/p).read_bytes().decode();ed=[]
 ol=old.splitlines(keepends=True);nl=new.splitlines(keepends=True)
 oo=[0];no=[0]
 for line in ol:oo.append(oo[-1]+len(line))
 for line in nl:no.append(no[-1]+len(line))
 for tag,i,j,a,b in difflib.SequenceMatcher(None,ol,nl).get_opcodes():
  if tag!='equal':ed.append({'before_offset':oo[i],'after_offset':no[a],'old':old[oo[i]:oo[j]],'new':new[no[a]:no[b]]})
 m['files'][str(Path(p).relative_to('ntm-port-1.12.2'))]={'before_sha256':hashlib.sha256(old.encode()).hexdigest(),'after_sha256':hashlib.sha256(new.encode()).hexdigest(),'edits':ed}
raw=(json.dumps(m,indent=2,ensure_ascii=False)+'\n').encode();(tests/'native-payload-storage-naming-source-manifest.json').write_bytes(raw)
original=git(['show',base+':ntm-port-1.12.2/porting/tests/check-recovered-cp77-source.py']).decode()
new=original.replace('recovered-cp77-source-manifest.json','native-payload-storage-naming-source-manifest.json').replace("'2b30998ba3f1e0723ec97d9d8f4480bb3cb22a1951f4de5d52a3ab1d1e882a6e'",repr(hashlib.sha256(raw).hexdigest())).replace('eefb4c1f6f344e4fcb15efb7e6cb6a370419a570',base).replace('len(FILES)==37',f'len(FILES)=={len(paths)}').replace('Recovered CP77 source: 37',f'Native payload/storage/naming source: {len(paths)}').replace('CP36 exact reversible loop/item/block source boundary','CP78 exact reversible payload/storage/naming source boundary')
(tests/'check-native-payload-storage-naming-source.py').write_text(new)
wrapper=original.replace('from pathlib import Path\n','from pathlib import Path\nimport importlib.util\n_spec=importlib.util.spec_from_file_location("cp78_boundary",Path(__file__).with_name("check-native-payload-storage-naming-source.py"))\n_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n').replace('def reverse_source(relative,text):\n','def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n').replace('    rejected=0\n','    _next.verify(root,verbose)\n    rejected=0\n').replace('assert forward==current','assert forward==_next.reverse_source(relative,current)')
(tests/'check-recovered-cp77-source.py').write_text(wrapper)
print('Prepared exact',len(paths),'source inverses against sealed CP77')

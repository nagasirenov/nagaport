from pathlib import Path
import subprocess,hashlib,json,difflib
root=Path('/workspace/shared/nagaport');module=root/'ntm-port-1.12.2';tests=module/'porting/tests';base='a0dc7d14f453a1d6f01b0d5abdcd455358bf2714'
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
raw=(json.dumps(m,indent=2,ensure_ascii=False)+'\n').encode();(tests/'native-particles-furnace-comparator-source-manifest.json').write_bytes(raw)
original=git(['show',base+':ntm-port-1.12.2/porting/tests/check-native-depth-attribute-cure-projectile-source.py']).decode()
new=original.replace('native-depth-attribute-cure-projectile-source-manifest.json','native-particles-furnace-comparator-source-manifest.json').replace("'ec9d38f43541b2ecf03d80d4cd37a200106e7e0ab42e998e419b4df6920f3e09'",repr(hashlib.sha256(raw).hexdigest())).replace('77b595c32b46e2f9309bcf9924d654ddc29a3c95',base).replace('len(FILES)==31',f'len(FILES)=={len(paths)}').replace('Native depth/attribute/cure/projectile source: 31',f'Native particle/furnace/comparator source: {len(paths)}').replace('CP80 exact reversible depth/attribute/cure/projectile source boundary','CP81 exact reversible particle/furnace/comparator source boundary')
(tests/'check-native-particles-furnace-comparator-source.py').write_text(new)
wrapper=original.replace('from pathlib import Path\n','from pathlib import Path\nimport importlib.util\n_spec=importlib.util.spec_from_file_location("cp81_boundary",Path(__file__).with_name("check-native-particles-furnace-comparator-source.py"))\n_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n').replace('def reverse_source(relative,text):\n','def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n').replace('    rejected=0\n','    _next.verify(root,verbose)\n    rejected=0\n').replace('assert forward==current','assert forward==_next.reverse_source(relative,current)')
(tests/'check-native-depth-attribute-cure-projectile-source.py').write_text(wrapper)
print('Prepared exact',len(paths),'source inverses against sealed CP80')

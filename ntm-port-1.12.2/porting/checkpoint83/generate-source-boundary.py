from pathlib import Path
import subprocess,hashlib,json,difflib
root=Path('/workspace/shared/nagaport');module=root/'ntm-port-1.12.2';tests=module/'porting/tests';base='df18fc077648dd18e6d0c1055d82c11dc2ebdb78'
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
raw=(json.dumps(m,indent=2,ensure_ascii=False)+'\n').encode();(tests/'native-armor-owned-geometry-data-source-manifest.json').write_bytes(raw)
original=git(['show',base+':ntm-port-1.12.2/porting/tests/check-native-slot-world-plane-texture-source.py']).decode()
new=original.replace('native-slot-world-plane-texture-source-manifest.json','native-armor-owned-geometry-data-source-manifest.json').replace("'bf18dd31f1659b62f86ed2b8d0a2d18954c2f6f6058ae69c43c84d8409861cb2'",repr(hashlib.sha256(raw).hexdigest())).replace('242867906a53eb7da03c879f476ced61c1170e14',base).replace('len(FILES)==56',f'len(FILES)=={len(paths)}').replace('Native slot/world/plane/texture source: 56',f'Native armor/owned/geometry/data source: {len(paths)}').replace('CP82 exact reversible slot/world/plane/texture source boundary','CP83 exact reversible armor/owned/geometry/data source boundary')
(tests/'check-native-armor-owned-geometry-data-source.py').write_text(new)
wrapper=original.replace('from pathlib import Path\n','from pathlib import Path\nimport importlib.util\n_spec=importlib.util.spec_from_file_location("cp83_boundary",Path(__file__).with_name("check-native-armor-owned-geometry-data-source.py"))\n_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n').replace('def reverse_source(relative,text):\n','def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n').replace('    rejected=0\n','    _next.verify(root,verbose)\n    rejected=0\n').replace('assert forward==current','assert forward==_next.reverse_source(relative,current)')
(tests/'check-native-slot-world-plane-texture-source.py').write_text(wrapper)
print('Prepared exact',len(paths),'source inverses against sealed CP82')

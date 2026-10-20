from pathlib import Path
import subprocess,hashlib,json,difflib
root=Path('/workspace/shared/nagaport');module=root/'ntm-port-1.12.2';tests=module/'porting/tests';base='bdb4c897d5f554458d23c6efab036b056ee20de0'
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
raw=(json.dumps(m,indent=2,ensure_ascii=False)+'\n').encode();(tests/'native-state-schedule-render-bounds-source-manifest.json').write_bytes(raw)
original=git(['show',base+':ntm-port-1.12.2/porting/tests/check-native-armor-owned-geometry-data-source.py']).decode()
new=original.replace('native-armor-owned-geometry-data-source-manifest.json','native-state-schedule-render-bounds-source-manifest.json').replace("'b42485360c44e4a5b5b197147400f3d4397bc50e80d3610b029811bef6a51a7f'",repr(hashlib.sha256(raw).hexdigest())).replace('df18fc077648dd18e6d0c1055d82c11dc2ebdb78',base).replace('len(FILES)==18',f'len(FILES)=={len(paths)}').replace('Native armor/owned/geometry/data source: 18',f'Native state/schedule/render/bounds source: {len(paths)}').replace('CP83 exact reversible armor/owned/geometry/data source boundary','CP84 exact reversible state/schedule/render/bounds source boundary')
(tests/'check-native-state-schedule-render-bounds-source.py').write_text(new)
wrapper=original.replace('from pathlib import Path\n','from pathlib import Path\nimport importlib.util\n_spec=importlib.util.spec_from_file_location("cp84_boundary",Path(__file__).with_name("check-native-state-schedule-render-bounds-source.py"))\n_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n').replace('def reverse_source(relative,text):\n','def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n').replace('    rejected=0\n','    _next.verify(root,verbose)\n    rejected=0\n').replace('assert forward==current','assert forward==_next.reverse_source(relative,current)')
(tests/'check-native-armor-owned-geometry-data-source.py').write_text(wrapper)
print('Prepared exact',len(paths),'source inverses against sealed CP83')

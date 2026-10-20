from pathlib import Path
import subprocess,hashlib,json,difflib
root=Path('/workspace/shared/nagaport');module=root/'ntm-port-1.12.2';tests=module/'porting/tests';base='502f0b9dc3cd3e14622edd897fe3c753e309818d'
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
for rel in ['src/main/resources/assets/hbm/textures/models/chopperBomb.png','src/main/resources/assets/hbm/textures/models/chopperbomb.png']:
 before=git(['show',base+':ntm-port-1.12.2/'+rel]) if rel.endswith('chopperBomb.png') else None
 after=(module/rel).read_bytes() if (module/rel).exists() else None
 m['binary_files'][rel]={'before_sha256':hashlib.sha256(before).hexdigest() if before is not None else None,'after_sha256':hashlib.sha256(after).hexdigest() if after is not None else None}
assert m['binary_files']['src/main/resources/assets/hbm/textures/models/chopperBomb.png']['before_sha256']==m['binary_files']['src/main/resources/assets/hbm/textures/models/chopperbomb.png']['after_sha256']
raw=(json.dumps(m,indent=2,ensure_ascii=False)+'\n').encode();(tests/'native-owned-render-event-name-source-manifest.json').write_bytes(raw)
original=git(['show',base+':ntm-port-1.12.2/porting/tests/check-native-state-schedule-render-bounds-source.py']).decode()
new=original.replace('native-state-schedule-render-bounds-source-manifest.json','native-owned-render-event-name-source-manifest.json').replace("'2a34722af11724c6cf9a421cad581d9f2ab8d17f0ac6d8d7d523cc238f57ea57'",repr(hashlib.sha256(raw).hexdigest())).replace('bdb4c897d5f554458d23c6efab036b056ee20de0',base).replace('len(FILES)==38',f'len(FILES)=={len(paths)}').replace('Native state/schedule/render/bounds source: 38',f'Native owned/render/event/name source: {len(paths)}').replace('CP84 exact reversible state/schedule/render/bounds source boundary','CP85 exact reversible owned/render/event/name source boundary')
(tests/'check-native-owned-render-event-name-source.py').write_text(new)
wrapper=original.replace('from pathlib import Path\n','from pathlib import Path\nimport importlib.util\n_spec=importlib.util.spec_from_file_location("cp85_boundary",Path(__file__).with_name("check-native-owned-render-event-name-source.py"))\n_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n').replace('def reverse_source(relative,text):\n','def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n').replace('    rejected=0\n','    _next.verify(root,verbose)\n    rejected=0\n').replace('assert forward==current','assert forward==_next.reverse_source(relative,current)')
(tests/'check-native-state-schedule-render-bounds-source.py').write_text(wrapper)
print('Prepared exact',len(paths),'source inverses against sealed CP84')

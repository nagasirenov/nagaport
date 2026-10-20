from pathlib import Path
import subprocess,hashlib,json,difflib
root=Path('/workspace/shared/nagaport');module=root/'ntm-port-1.12.2';tests=module/'porting/tests';base='0be133c9fb9f07053e26f081371487e641c95214'
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
raw=(json.dumps(m,indent=2,ensure_ascii=False)+'\n').encode();(tests/'native-press-slots-colored-source-manifest.json').write_bytes(raw)
original=git(['show',base+':ntm-port-1.12.2/porting/tests/check-native-armor-textured-source.py']).decode()
new=original.replace('native-armor-textured-source-manifest.json','native-press-slots-colored-source-manifest.json').replace("'6bb5c12d42e2221262af291229708daefc80b48568665035ae498272672dc97a'",repr(hashlib.sha256(raw).hexdigest())).replace('42dea8e6e3241d0c4ce4ba9c5459fab2bdfdc99b',base).replace('len(FILES)==52 and len(NEW_FILES)==1',f'len(FILES)=={len(m["files"])} and len(NEW_FILES)=={len(m["new_files"])}').replace('Native armor/textured source: 52',f'Native press/slots/colored source: {len(m["files"])}').replace('1 new,',f'{len(m["new_files"])} new,').replace('CP90 exact reversible armor/textured','CP91 exact reversible press/slots/colored')
(tests/'check-native-press-slots-colored-source.py').write_text(new)
wrapper=original.replace('from pathlib import Path\n','from pathlib import Path\nimport importlib.util\n_spec=importlib.util.spec_from_file_location("cp91_boundary",Path(__file__).with_name("check-native-press-slots-colored-source.py"))\n_next=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_next)\n').replace('def reverse_source(relative,text):\n','def reverse_source(relative,text):\n    text=_next.reverse_source(relative,text)\n').replace('    rejected=0\n','    _next.verify(root,verbose)\n    rejected=0\n').replace('assert forward==current','assert forward==_next.reverse_source(relative,current)')
(tests/'check-native-armor-textured-source.py').write_text(wrapper)
print('Prepared exact',len(m['files']),'existing and',len(m['new_files']),'new source boundaries against CP90')

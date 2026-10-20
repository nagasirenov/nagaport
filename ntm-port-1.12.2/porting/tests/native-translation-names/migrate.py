import pathlib,json,hashlib,subprocess,collections
here=pathlib.Path(__file__).resolve().parent; root=here.parents[3]; base='0732d318302d807ce8579878a0cdde6b18bbba86'
H=lambda b:hashlib.sha256(b).hexdigest()
if not (here/'manifest.json').exists():
 groups=collections.defaultdict(list)
 for line in (here/'attribution.tsv').read_text().splitlines():
  status,path,start,end,old,new,*proof=line.split('\t')
  if status=='YES':groups[str(pathlib.Path(path).relative_to(root))].append(dict(start=int(start),end=int(end),old=old,new=new,proof=proof))
 manifest=[]
 for path,edits in sorted(groups.items()):
  before=subprocess.check_output(['git','show',base+':'+path],cwd=root).decode(); after=before
  for e in sorted(edits,key=lambda e:e['start'],reverse=True):
   assert after[e['start']:e['end']]==e['old'];after=after[:e['start']]+e['new']+after[e['end']:]
  (root/path).write_bytes(after.encode());manifest.append(dict(path=path,before=H(before.encode()),after=H(after.encode()),edits=edits))
 (here/'manifest.json').write_text(json.dumps(dict(baseline=base,files=manifest),indent=2)+'\n')
m=json.loads((here/'manifest.json').read_text());guards=0
for f in m['files']:
 before=subprocess.check_output(['git','show',base+':'+f['path']],cwd=root).decode();assert H(before.encode())==f['before']; expected=before
 for e in sorted(f['edits'],key=lambda e:e['start'],reverse=True):assert expected[e['start']:e['end']]==e['old'];expected=expected[:e['start']]+e['new']+expected[e['end']:]
 actual=(root/f['path']).read_bytes();assert actual==expected.encode();assert H(actual)==f['after'];recovered=expected;shift=0;positions=[]
 for e in sorted(f['edits'],key=lambda e:e['start']):positions.append((e,e['start']+shift));shift+=len(e['new'])-len(e['old'])
 for e,pos in reversed(positions):
  assert recovered[pos:pos+len(e['new'])]==e['new'];recovered=recovered[:pos]+e['old']+recovered[pos+len(e['new']):]
  mutant=actual[:pos]+b'X'+actual[pos+1:];assert mutant!=expected.encode();guards+=1
 assert recovered==before
print('PASS',sum(len(f['edits']) for f in m['files']),'identifier edits;',len(m['files']),'files; exact reversal;',guards,'single-token mutation guards')

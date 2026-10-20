from pathlib import Path
import subprocess,json,difflib,hashlib
r=Path('/workspace/shared/nagaport');m=r/'ntm-port-1.12.2';base='0732d318302d807ce8579878a0cdde6b18bbba86';h=m/'porting/tests/native-event-accessors';manifest=json.loads((h/'source-manifest.json').read_text());outputs={};receipt=[]
def sha(b):return hashlib.sha256(b).hexdigest()
for f in manifest['files']:
 old=subprocess.check_output(['git','show',base+':ntm-port-1.12.2/'+f['path']],cwd=r);assert sha(old)==f['before'];current=(m/f['path']).read_bytes();a=old.decode().encode('utf-16-le');b=current.decode().encode('utf-16-le')
 # Sequence alignment operates on UTF16 code units, matching scanner positions exactly.
 aa=[a[i:i+2] for i in range(0,len(a),2)];bb=[b[i:i+2] for i in range(0,len(b),2)];ops=difflib.SequenceMatcher(None,aa,bb,autojunk=False).get_opcodes();edits=[]
 for e in f['edits']:
  matches=[op for op in ops if op[0]=='equal' and op[1]<=e['start'] and e['end']<=op[2]];assert len(matches)==1,(f['path'],e)
  op=matches[0];start=op[3]+e['start']-op[1];end=start+e['end']-e['start'];assert b[start*2:end*2].decode('utf-16-le')==e['old'];edits.append(dict(e,start=start,end=end))
 out=b
 for e in sorted(edits,key=lambda x:x['start'],reverse=True):out=out[:e['start']*2]+e['new'].encode('utf-16-le')+out[e['end']*2:]
 inverse=[];delta=0
 for e in sorted(edits,key=lambda x:x['start']):
  n=len(e['new'].encode('utf-16-le'))//2;inverse.append((e['start']+delta,n,e['old']));delta+=n-e['end']+e['start']
 back=out
 for start,n,s in reversed(inverse):back=back[:start*2]+s.encode('utf-16-le')+back[(start+n)*2:]
 assert back==b;raw=out.decode('utf-16-le').encode();outputs[f['path']]=raw;receipt.append(dict(path=f['path'],before_sha256=sha(current),after_sha256=sha(raw),edits=edits))
assert sum(len(f['edits']) for f in receipt)==268
for p,b in outputs.items():(m/p).write_bytes(b)
(m/'porting/continuation-2026-10-19/checkpoint56-event-integration.json').write_text(json.dumps(receipt,indent=2)+'\n')
print('PASS merged268 exact immutable-provenance event tokens; all independent edits invert unchanged; isolatedmanifest unchanged.')

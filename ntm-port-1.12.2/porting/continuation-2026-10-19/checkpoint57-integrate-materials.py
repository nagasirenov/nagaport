from pathlib import Path
import subprocess,json,difflib,hashlib,bisect
r=Path('/workspace/shared/nagaport');m=r/'ntm-port-1.12.2';base='2fa015374be0e330d2541e5bfe9d4b96e380aa05';manifest=json.loads((m/'porting/tests/native-block-materials/manifest.json').read_text());outputs={};receipt=[]
def sha(b):return hashlib.sha256(b).hexdigest()
def offsets(lines):
 a=[0]
 for l in lines:a.append(a[-1]+len(l))
 return a
for name,f in manifest['files'].items():
 old=subprocess.check_output(['git','show',base+':ntm-port-1.12.2/'+name],cwd=r);assert sha(old)==f['before'];current=(m/name).read_bytes();a=old.decode();b=current.decode();al=a.splitlines(True);bl=b.splitlines(True);ao=offsets(al);bo=offsets(bl);ops=difflib.SequenceMatcher(None,al,bl,autojunk=False).get_opcodes();edits=[];ua=a.encode('utf-16-le')
 for e in f['edits']:
  lo=len(ua[:e['start']*2].decode('utf-16-le'));hi=len(ua[:e['end']*2].decode('utf-16-le'));assert a[lo:hi]==e['old'];line=bisect.bisect_right(ao,lo)-1;op=next(o for o in ops if o[1]<=line<o[2]);tag,i,j,k,l=op
  if tag=='equal':target=bo[k]+lo-ao[i]
  else:
   left=a[ao[i]:ao[j]];right=b[bo[k]:bo[l]];clo=lo-ao[i];chi=hi-ao[i];blocks=difflib.SequenceMatcher(None,left,right,autojunk=False).get_opcodes();matches=[o for o in blocks if o[0]=='equal' and o[1]<=clo and chi<=o[2]];assert len(matches)==1,(name,e);q=matches[0];target=bo[k]+q[3]+clo-q[1]
  assert b[target:target+len(e['old'])]==e['old'];edits.append(dict(start=target,end=target+len(e['old']),old=e['old'],new=e['new']))
 out=b
 for e in sorted(edits,key=lambda e:e['start'],reverse=True):out=out[:e['start']]+e['new']+out[e['end']:]
 inv=[];delta=0
 for e in sorted(edits,key=lambda e:e['start']):inv.append((e['start']+delta,len(e['new']),e['old']));delta+=len(e['new'])-len(e['old'])
 back=out
 for start,n,s in reversed(inv):back=back[:start]+s+back[start+n:]
 assert back==b;outputs[name]=out.encode();receipt.append(dict(path=name,before_sha256=sha(current),after_sha256=sha(out.encode()),codepoint_edits=edits))
assert sum(len(f['codepoint_edits']) for f in receipt)==590
for name,b in outputs.items():(m/name).write_bytes(b)
(m/'porting/continuation-2026-10-19/checkpoint57-material-integration.json').write_text(json.dumps(receipt,indent=2)+'\n')
print('PASS590 original-provenance tokens merged; exact inverses preserve all independent changes.')

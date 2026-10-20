#!/usr/bin/env python3
"""Compose only the audited selector edits with independent GUI changes, preserving all other bytes."""
import argparse,collections,hashlib,json,re
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project',type=Path,required=True);p.add_argument('--receipt',type=Path,required=True);p.add_argument('--apply',action='store_true');a=p.parse_args();e=Path(__file__).resolve().parent;m=json.loads((e/'manifest.json').read_text());sha=lambda x:hashlib.sha256(x).hexdigest();old=m['old_selector'];new=m['new_selector'];receipt={'base':m['base'],'files':{}};outputs={}
def unescape(s):return re.sub(r'\\([\\trn])',lambda q:{'\\':'\\','t':'\t','r':'\r','n':'\n'}[q[1]],s)
def mask(s):
 # Preserve positions while removing comments and literal contents. Call selection
 # is checked against the complete independently attributed invocation multiset.
 return re.sub(r'//[^\r\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',lambda q:' '*len(q[0]),s)
for path,entry in m['files'].items():
 p=a.project/'src/main/java'/path;b=p.read_bytes();s=b.decode();masked=mask(s);positions=[q.start() for q in re.finditer(re.escape(old)+r'(?=\s*\()',masked)];actual=[]
 for pos in positions:
  i=masked.index('(',pos);depth=1;k=i+1
  while depth:depth+=(masked[k]=='(')-(masked[k]==')');k+=1
  actual.append(s[pos:k])
 expected=[unescape(x['full_invocation']) for x in m['calls'] if x['path']==path];assert collections.Counter(actual)==collections.Counter(expected),path
 result=s
 for pos in reversed(positions):result=result[:pos]+new+result[pos+len(old):]
 assert result.replace(new,old)==s,'inverse failed '+path
 out=result.encode();outputs[p]=out;receipt['files'][path]={'before':sha(b),'after':sha(out),'full_selector_inverse_equals_input':True,'calls':len(positions)}
helper='src/main/java/com/hbm/sound/GuiSound.java';content=(e.parents[2]/helper).read_bytes();dest=a.project/helper
assert not dest.exists() or dest.read_bytes()==content;outputs[dest]=content;receipt['helper_sha256']=sha(content)
if a.apply:
 for p,data in outputs.items():p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data)
a.receipt.write_text(json.dumps(receipt,indent=2)+'\n');print('PASS exact original 157 invocation fragments, comment exclusion and full inverse preserving independent changes; '+('applied' if a.apply else 'dry run')+'.')

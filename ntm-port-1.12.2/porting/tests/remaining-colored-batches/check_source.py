#!/usr/bin/env python3
"""Exact full-source inverse and deterministic finite owned color-stream layer."""
import hashlib,importlib.util,json,pathlib,re
HERE=pathlib.Path(__file__).resolve().parent;ROOT=HERE.parents[2]
spec=importlib.util.spec_from_file_location('remaining_colored_transform',HERE/'transform.py');transform=importlib.util.module_from_spec(spec);spec.loader.exec_module(transform)
MANIFEST=json.loads((HERE/'manifest.json').read_text());FILES=MANIFEST['files']
def digest(s):return hashlib.sha256(s.encode()).hexdigest()
def reverse_source(relative,text):
 if relative not in FILES:return text
 r=FILES[relative];assert digest(text)==r['after_sha256'],('changed native source',relative)
 lines=text.splitlines(True)
 for e in reversed(r['edits']):
  i=e['new_start'];n=len(e['new']);assert lines[i:i+n]==e['new'];lines[i:i+n]=e['old']
 old=''.join(lines);assert digest(old)==r['before_sha256'];return old

def validate():
 scopes=batches=vertices=setters=mutations=0
 for relative,r in FILES.items():
  current=(ROOT/relative).read_text();old=reverse_source(relative,current);new,meta=transform.forward_source(relative,old);assert new==current and meta==r['scopes']
  for method,info in meta.items():
   scopes+=1;batches+=len(info['modes']);vertices+=len(info['vertices']);setters+=len(info['setters'])
   a,b=transform.method_span(old,method);ob=old[a:b];c,d=transform.method_span(current,method);nb=current[c:d];name=info['name']
   def surrounding(body):
    lines=[]
    for line,clean in zip(body.splitlines(),transform.scrub(body).splitlines()):
     if re.match(r'\s*(?:Tessellator '+name+r' =|BufferBuilder '+name+r'Buffer =|'+name+r'\.|'+name+r'Buffer\.|int '+name+r'Color)',clean):continue
     lines.append(line)
    return '\n'.join(lines)
   assert surrounding(ob)==surrounding(nb),(relative,method)
   # Attribute activation occurs at the first color setter before every vertex.
   # All selected locals and batches have already passed the finite nonescape
   # grammar and declaration replay; comments are deliberately untouched.
   ov=re.findall(name+r'\.addVertex(?:WithUV)?\(([^\n]*)\);',transform.scrub(ob))
   nv=re.findall(name+r'Buffer\.pos\(([^\n]*?)\)\.(?:tex\(([^\n]*)\)\.)?color\(',transform.scrub(nb))
   assert len(ov)==len(nv)==len(info['vertices'])
   for x,(pos,uv) in zip(ov,nv):assert transform.split_args(x)==transform.split_args(pos)+(transform.split_args(uv) if uv else [])
  method=next(iter(r['scopes']));a,b=transform.method_span(current,method);body=current[a:b]
  for x,y in [('.endVertex();',';'),('.color(','.tex('),('.pos(','.normal('),('DefaultVertexFormats.POSITION_','DefaultVertexFormats.NORMAL_'),('Tessellator.getInstance()','new Tessellator(1024)'),('.draw();','.getBuffer();')]:
   m=body.replace(x,y,1);assert m!=body
   try:reverse_source(relative,current[:a]+m+current[b:])
   except AssertionError:mutations+=1
   else:raise AssertionError('accepted mutation')
 assert (scopes,batches,vertices,setters,mutations)==(11,12,89,41,54),(scopes,batches,vertices,setters,mutations)
 assert reverse_source('unrelated.java','unchanged')=='unchanged'
 print('PASS 9 exact reversible full files, 11 nonescaping scopes, 12 color-stream sites, 89 vertices, 41 exact setter sites and 54 mutation rejections; unchanged complete loops, comments, GL, materials, RNG and animation.',flush=True)
if __name__=='__main__':validate()

#!/usr/bin/env python3
"""Exact reversible complete-file proof for a finite owned textured-batch layer."""
import hashlib,importlib.util,json,pathlib,re
HERE=pathlib.Path(__file__).resolve().parent;ROOT=HERE.parents[2]
spec=importlib.util.spec_from_file_location('remaining_textured_transform',HERE/'transform.py');transform=importlib.util.module_from_spec(spec);spec.loader.exec_module(transform)
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
 scopes=batches=vertices=resets=mutations=0
 for relative,r in FILES.items():
  s=(ROOT/relative).read_text();old=reverse_source(relative,s);new,meta=transform.forward_source(relative,old)
  assert new==s and meta==r['scopes'],relative
  for method,info in meta.items():
   scopes+=1;batches+=info['batches'];vertices+=len(info['vertices']);resets+=len(info['reset_setters'])
   a,b=transform.method_span(old,method);c,d=transform.method_span(s,method);ob=old[a:b];nb=s[c:d];name=info['name']
   starts=re.findall(re.escape(name)+r'\.startDrawingQuads\(\);(.*?)'+re.escape(name)+r'\.draw\(\);',ob,re.S)
   assert len(starts)==info['batches']
   for batch in starts:
    vertex_calls=re.findall(re.escape(name)+r'\.addVertexWithUV\(([^\n]*)\);',batch)
    assert len(vertex_calls)==4
    rest=re.sub(re.escape(name)+r'\.addVertexWithUV\([^\n]*\);','',batch)
    rest=re.sub(r'double qsize = size \* random.nextDouble\(\) \* 0\.(?:1|07);','',rest)
    assert not rest.strip(),(relative,method,rest)
   # Independent surrounding-source equality, preserving GL, matrix, textures,
   # animation, material/color, RNG, clock, predicates and complete loop structure.
   def surrounding(body):
    lines=[]
    for line in body.splitlines():
     if re.match(r'\s*(?:Tessellator '+name+r' =|BufferBuilder '+name+r'Buffer =|'+name+r'\.|'+name+r'Buffer\.|(?:float|int) '+name+r'Reset|// Legacy begin resets this attribute;)',line):continue
     lines.append(line)
    return '\n'.join(lines)
   assert surrounding(ob)==surrounding(nb),(relative,method)
  for x,y in [('POSITION_TEX','POSITION_COLOR'),('.endVertex();',';'),('.tex(','.color('),('.pos(','.normal('),('begin(7,','begin(4,'),('Tessellator.getInstance()','new Tessellator(1024)'),('.draw();','.getBuffer();')]:
   # Mutate a reviewed site, not a separate unchanged legacy method.
   method=next(iter(r['scopes']));a,b=transform.method_span(s,method);body=s[a:b];mut=body.replace(x,y,1);assert mut!=body
   try:reverse_source(relative,s[:a]+mut+s[b:])
   except AssertionError:mutations+=1
   else:raise AssertionError('accepted mutation')
 assert (scopes,batches,vertices,resets,mutations)==(10,29,116,5,49),(scopes,batches,vertices,resets,mutations)
 assert reverse_source('unrelated.java','unchanged')=='unchanged'
 print('PASS 7 exact reversible complete files, 10 nonescaping local scopes, 29 owned POSITION_TEX sites, 116 XYZUV expressions, 5 reset-setter argument evaluations and 49 mutation rejections; unchanged GL/matrix/material/animation/RNG/clock/control-flow surroundings.',flush=True)
if __name__=='__main__':validate()

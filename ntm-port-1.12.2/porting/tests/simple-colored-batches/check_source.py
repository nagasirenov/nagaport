#!/usr/bin/env python3
import hashlib,json,pathlib,re,importlib.util
HERE=pathlib.Path(__file__).resolve().parent
_spec=importlib.util.spec_from_file_location("colored_transform",HERE/"transform.py")
_transform=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_transform)
forward_source=_transform.forward_source
split_args=_transform.split_args
ROOT=HERE.parents[2]
MANIFEST=json.loads((HERE/'manifest.json').read_text())
FILES=MANIFEST['files']
def digest(s):return hashlib.sha256(s.encode()).hexdigest()
def reverse_source(relative,text):
 if relative not in FILES:return text
 record=FILES[relative];assert digest(text)==record['after_sha256'],('changed native source',relative)
 lines=text.splitlines(True)
 for edit in reversed(record['edits']):
  i=edit['new_start'];n=len(edit['new']);assert lines[i:i+n]==edit['new'];lines[i:i+n]=edit['old']
 old=''.join(lines);assert digest(old)==record['before_sha256'];return old

def validate():
 batches=vertices=mutations=0
 for relative,record in FILES.items():
  current=(ROOT/relative).read_text();old=reverse_source(relative,current);n=record['name']
  forward,metadata,name=forward_source(old);assert forward==current and metadata==record['batches'] and name==n
  # Full local-use grammar prevents escaped/borrowed or mixed-attribute batches.
  for line in old.splitlines():
   if re.search(r'\b'+n+r'\b',line):
    assert re.fullmatch(r'\s*(?:(?:final )?Tessellator '+n+r' = Tessellator.instance;|'+n+r'\.(?:startDrawingQuads|startDrawing|setColorOpaque_I|setColorRGBA_F|addVertex|draw)\(.*\);)\s*',line),(relative,line)
  before=re.findall(n+r'\.startDrawing(?:Quads)?\([^;]*;.*?'+n+r'\.draw\(\);',old,re.S)
  after=re.findall(n+r'Buffer.begin\([^;]*;.*?'+n+r'\.draw\(\);',current,re.S)
  assert len(before)==len(after)==old.count(n+'.draw();')==current.count(n+'.draw();')
  assert current.count('BufferBuilder '+n+'Buffer = '+n+'.getBuffer();')==old.count('Tessellator '+n+' = Tessellator.instance;')
  for a,b in zip(before,after):
   colors=re.findall(n+r'\.setColor(\w+)\(([^\n]+)\);',a);assert len(colors)==1
   assert a.index('.setColor')<a.index('.addVertex')
   av=re.findall(n+r'\.addVertex\(([^\n]+)\);',a);bv=re.findall(n+r'Buffer.pos\(([^\n]+)\)\.color\(',b)
   assert av==bv and av
   rest=re.sub(n+r'\.(?:startDrawingQuads|startDrawing|setColorOpaque_I|setColorRGBA_F|addVertex|draw)\([^\n]*\);','',a)
   if relative.endswith('/RenderRBMKFuelChannel.java'):
    assert re.sub(r'\s+','',rest)=='for(doublej=0;j<=offset;j+=0.25){}'
   else:assert not rest.strip(),(relative,rest)
   batches+=1;vertices+=len(av)
  replacements=[('POSITION_COLOR','POSITION_TEX'),('.endVertex();',';'),('.draw();','.getBuffer();'),('.color(','.tex('),('.pos(','.normal(')]
  if 'Math.max(0, Math.min(255,' in current:replacements += [('Math.max(0, Math.min(255,','Math.max(0, Math.min(254,'),('* 255.0F','* 256.0F')]
  else:replacements += [('>> 16','>> 15'),('& 255','& 127')]
  for a,b in replacements:
   mutated=current.replace(a,b,1);assert mutated!=current
   try:reverse_source(relative,mutated)
   except AssertionError:mutations+=1
   else:raise AssertionError('accepted mutation')
 assert batches==14 and vertices==62 and mutations==70
 assert reverse_source('unrelated.java','unchanged')=='unchanged'
 print('PASS 10 exact reversals, 14 constant-per-batch POSITION_COLOR sites, 62 vertex expressions, 70 mutation rejections',flush=True)
if __name__=='__main__':validate()

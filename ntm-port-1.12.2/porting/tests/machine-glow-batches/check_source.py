#!/usr/bin/env python3
"""Exact finite reversals plus local batch and callback closure, no git needed."""
import hashlib,json,pathlib,re,importlib.util
HERE=pathlib.Path(__file__).resolve().parent;ROOT=HERE.parents[2]
_spec=importlib.util.spec_from_file_location('machine_glow_transform',HERE/'transform.py')
_module=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_module)
forward_source=_module.forward_source
MANIFEST=json.loads((HERE/'manifest.json').read_text());FILES=MANIFEST['files']
def digest(text):return hashlib.sha256(text.encode()).hexdigest()
def reverse_source(relative,text):
 if relative not in FILES:return text
 r=FILES[relative];assert digest(text)==r['after_sha256'],('changed native source',relative)
 lines=text.splitlines(True)
 for e in reversed(r['edits']):
  i=e['new_start'];n=len(e['new']);assert lines[i:i+n]==e['new'];lines[i:i+n]=e['old']
 old=''.join(lines);assert digest(old)==r['before_sha256'];return old

def validate():
 closure=json.loads((HERE/'callback-closure.json').read_text());registry=(ROOT/closure['registry']).read_text()
 for binding in closure['bindings']:assert registry.count(binding)==1,binding
 vertices=mutations=colors=0
 for relative,r in FILES.items():
  s=(ROOT/relative).read_text();old=reverse_source(relative,s);new,meta=forward_source(old)
  assert new==s
  for key,value in meta.items():assert r[key]==value,(relative,key)
  # The entire local is owned here. It never escapes to helpers or borrowed batches.
  for line in old.splitlines():
   if 'tess' not in line or line.lstrip().startswith('//'):continue
   assert re.fullmatch(r'\s*(?:Tessellator tess = Tessellator.instance;|tess\.(?:startDrawingQuads|setColorRGBA_F|setBrightness|setNormal|addVertex|draw)\(.*\);)\s*',line),line
  assert old.count('tess.draw();')==s.count('tess.draw();')==1
  assert old.count('render(TileEntity ')==s.count('render(TileEntity ')==1
  assert 'int nativeDestroyStage, float nativeAlpha)' in s
  assert re.findall(r'tess.addVertex\(([^\n]+)\);',old)==re.findall(r'tessBuffer.pos\(([^\n]+)\).color\(',s)
  # No external GL/matrix, predicate, RNG/clock, binding, helper or vector code changed.
  def surrounding(t):
   t=re.sub(r'^import net.minecraft.client.renderer.(?:BufferBuilder|vertex.DefaultVertexFormats|vertex.VertexFormat);\n','',t,flags=re.M)
   return '\n'.join(line for line in t.splitlines() if not re.match(r'\s*(?:Tessellator tess =|BufferBuilder tessBuffer =|tess\.(?:startDrawingQuads|setColorRGBA_F|setBrightness|setNormal|addVertex|draw)\(|tessBuffer\.|int tessColor|int tessBrightness|float tessNormal)',line))
  assert surrounding(old)==surrounding(s),relative
  for a,b in [('.endVertex();',';'),('.draw();','.getBuffer();'),('.color(','.tex('),('.pos(','.normal('),('* 255.0F','* 256.0F'),('Math.min(255,','Math.min(254,'),('begin(7,','begin(4,')]+([('>>> 16','>>> 15'),('TEX_2S','TEX_2F'),('NORMAL_3B','COLOR_4UB')] if r['mixed'] else [('POSITION_COLOR','POSITION_TEX')]):
   m=s.replace(a,b,1);assert m!=s
   try:reverse_source(relative,m)
   except AssertionError:mutations+=1
   else:raise AssertionError('accepted mutation')
  vertices+=len(r['vertex_arguments']);colors+=len(r['color_setters'])
 assert vertices==56 and colors==18 and mutations==32,(vertices,colors,mutations)
 assert reverse_source('unrelated.java','unchanged')=='unchanged'
 print('PASS 4 exact reversible complete files, 4 owned quad sites, 56 vertex expressions, 18 color setters, 32 mutation rejections; unchanged enclosing callbacks, control flow, geometry, GL/matrices, random/clock and helper evaluation sites.',flush=True)
if __name__=='__main__':validate()

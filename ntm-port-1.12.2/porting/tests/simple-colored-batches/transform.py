"""Exact finite constant-per-batch color transform; test/evidence only."""
import re
import importlib.util,pathlib
_previous=pathlib.Path(__file__).resolve().parent.parent/'simple-textured-batches/check_source.py'
_spec=importlib.util.spec_from_file_location('textured_source',_previous)
_module=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_module)
split_args=_module.split_args
def forward_source(old):
 ns=set(re.findall(r'Tessellator\s+(\w+)\s*=\s*Tessellator.instance;',old));assert len(ns)==1;n=ns.pop()
 s=old.replace('import net.minecraft.client.renderer.Tessellator;','import net.minecraft.client.renderer.Tessellator;\nimport net.minecraft.client.renderer.BufferBuilder;\nimport net.minecraft.client.renderer.vertex.DefaultVertexFormats;')
 s=re.sub(r'(?m)^([\t ]*)((?:final )?)Tessellator '+n+r' = Tessellator.instance;',lambda x:x[1]+x[2]+'Tessellator '+n+' = Tessellator.getInstance();\n'+x[1]+'BufferBuilder '+n+'Buffer = '+n+'.getBuffer();',s)
 batches=[]
 def batch(x):
  b=x[0];index=len(batches);prefix=n+'Color'+str(index)
  colors=re.findall(n+r'\.setColor(\w+)\(([^\n]+)\);',b);assert len(colors)==1
  kind,raw=colors[0];args=split_args(raw)
  before=b[:b.index(n+'.setColor')];assert n+'.addVertex' not in before
  if kind=='Opaque_I':
   assert len(args)==1
   decl='int '+prefix+' = '+raw+';'
   channels=[ '('+prefix+' >> 16) & 255','('+prefix+' >> 8) & 255',prefix+' & 255','255']
  else:
   assert kind=='RGBA_F' and len(args)==4
   channels=[prefix+c for c in ['Red','Green','Blue','Alpha']]
   indent=re.search(r'(?m)^([\t ]*)'+n+r'\.setColor',b)[1]
   decl=('\n'+indent).join('int '+var+' = Math.max(0, Math.min(255, (int) (('+arg+') * 255.0F)));' for var,arg in zip(channels,args))
  b=b.replace(n+'.setColor'+kind+'('+raw+');',decl)
  b=re.sub(n+r'\.startDrawingQuads\(\);',n+'Buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);',b)
  b=re.sub(n+r'\.startDrawing\(([^\n]+)\);',lambda z:n+'Buffer.begin('+z[1]+', DefaultVertexFormats.POSITION_COLOR);',b)
  b=re.sub(n+r'\.addVertex\(([^\n]+)\);',lambda z:n+'Buffer.pos('+z[1]+').color('+', '.join(channels)+').endVertex();',b)
  batches.append({'kind':kind,'args':args,'vertices':x[0].count(n+'.addVertex(')})
  return b
 s=re.sub(n+r'\.startDrawing(?:Quads)?\([^;]*;.*?'+n+r'\.draw\(\);',batch,s,flags=re.S)
 return s,batches,n

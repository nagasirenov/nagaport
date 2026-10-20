"""Finite machine-glow conversion. Evidence generator, never production code."""
import importlib.util,pathlib,re
HERE=pathlib.Path(__file__).resolve().parent
_spec=importlib.util.spec_from_file_location('textured_source',HERE.parent/'simple-textured-batches/check_source.py')
_module=importlib.util.module_from_spec(_spec);_spec.loader.exec_module(_module)
split_args=_module.split_args
def forward_source(old):
 assert 'tess.setBrightness(' not in old and 'tess.setNormal(' not in old
 mixed=False
 assert old.count('Tessellator tess = Tessellator.instance;')==1
 s=old.replace('import net.minecraft.client.renderer.Tessellator;', 'import net.minecraft.client.renderer.Tessellator;\nimport net.minecraft.client.renderer.BufferBuilder;\nimport net.minecraft.client.renderer.vertex.DefaultVertexFormats;')
 active=False;color=None;setters=[];vertices=[];starts=draws=0;out=[]
 for line in s.splitlines(True):
  m=re.fullmatch(r'(\s*)Tessellator tess = Tessellator.instance;\n',line)
  if m:
   out.append(m[1]+'Tessellator tess = Tessellator.getInstance();\n'+m[1]+'BufferBuilder tessBuffer = tess.getBuffer();\n');continue
  m=re.fullmatch(r'([\t ]*)tess\.(\w+)\((.*)\);\n',line)
  if not m:out.append(line);continue
  indent,name,args=m.groups()
  if name=='startDrawingQuads':
   assert not active;active=True;starts+=1
   line=indent+'tessBuffer.begin(7, '+'DefaultVertexFormats.POSITION_COLOR'+');\n'
  elif name=='setColorRGBA_F':
   assert active
   a=split_args(args);assert len(a)==4
   color=['tessColor'+str(len(setters))+c for c in ['Red','Green','Blue','Alpha']]
   setters.append(a)
   line=''.join(indent+'int '+v+' = Math.max(0, Math.min(255, (int) (('+x+') * 255.0F)));\n' for v,x in zip(color,a))
  elif name=='addVertex':
   assert active and color
   suffix='.color('+', '.join(color)+')'
   line=indent+'tessBuffer.pos('+args+')'+suffix+'.endVertex();\n';vertices.append(args)
  elif name=='draw':
   assert active;active=False;draws+=1
  else:raise AssertionError(('unreviewed local call',line))
  out.append(line)
 assert not active and starts==draws==1
 return ''.join(out),{'mixed':mixed,'color_setters':setters,'vertex_arguments':vertices,'batches':starts}

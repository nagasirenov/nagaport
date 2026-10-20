"""Finite native owned colored-stream transformation; test tooling, not production."""
import importlib.util,pathlib,re
HERE=pathlib.Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('remaining_textured_transform',HERE.parent/'remaining-textured-batches/transform.py');prior=importlib.util.module_from_spec(spec);spec.loader.exec_module(prior)
split_args=prior.split_args;scrub=prior.scrub;method_span=prior.method_span
BASE='src/main/java/com/hbm/render/'
SCOPES={BASE+'tileentity/RenderForceField.java':['generateSphere','generateSphere2'],BASE+'tileentity/RenderMachineForceField.java':['generateSphere','generateSphere2'],BASE+'tileentity/RenderPumpjack.java':['render'],BASE+'entity/effect/RenderSpear.java':['renderFlash'],BASE+'entity/effect/RenderFlare.java':['doRender'],BASE+'entity/effect/RenderBlackHole.java':['renderJets'],BASE+'entity/mob/RenderBFA.java':['renderFlash'],BASE+'entity/projectile/RenderSiegeLaser.java':['renderDart'],BASE+'entity/effect/RenderCloudTom.java':['doRender']}
def forward_scope(body):
 clean=scrub(body);names=re.findall(r'Tessellator\s+(\w+) = Tessellator.instance;',clean);assert len(names)==1,names
 name=names[0];buffer=name+'Buffer';assert buffer not in body
 calls=re.findall(r'\b'+name+r'\.(\w+)\(',clean);assert len(re.findall(r'\b'+name+r'\b',clean))==len(calls)+1,'escaped local'
 assert set(calls)<={'startDrawing','startDrawingQuads','setColorRGBA_F','setColorOpaque_I','setColorRGBA_I','addVertex','addVertexWithUV','draw'}
 textured='addVertexWithUV' in calls;assert not(textured and 'addVertex' in calls)
 fmt='POSITION_TEX_COLOR' if textured else 'POSITION_COLOR';active=False;color=None;starts=[];draws=0;setters=[];vertices=[];out=[]
 for line,plain in zip(body.splitlines(True),clean.splitlines(True)):
  m=re.fullmatch(r'([\t ]*)Tessellator '+name+r' = Tessellator.instance;(\n?)',plain)
  if m:
   indent,eol=m.groups();out.append(indent+'Tessellator '+name+' = Tessellator.getInstance();\n'+indent+'BufferBuilder '+buffer+' = '+name+'.getBuffer();'+eol);continue
  m=re.fullmatch(r'([\t ]*)'+name+r'\.(\w+)\((.*)\);(\n?)',plain)
  if not m:out.append(line);continue
  indent,call,args,eol=m.groups()
  if call in ['startDrawing','startDrawingQuads']:
   assert not active;active=True;color=None;mode=args if call=='startDrawing' else '7';starts.append(mode)
   line=indent+buffer+'.begin('+mode+', DefaultVertexFormats.'+fmt+');'+eol
  elif call.startswith('setColor'):
   assert active;a=split_args(args);assert all(not re.search(r'\w\s*\(|\+\+|--|(?<![=!<>])=(?!=)',v) for v in a),a
   prefix=name+'Color'+str(len(setters));setters.append({'call':call,'arguments':a})
   if call=='setColorRGBA_F':
    assert len(a)==4;color=[prefix+c for c in ['Red','Green','Blue','Alpha']]
    line=''.join(indent+'int '+v+' = Math.max(0, Math.min(255, (int) (('+x+') * 255.0F)));\n' for v,x in zip(color,a))
   else:
    packed=prefix+'Packed';line=indent+'int '+packed+' = '+a[0]+';\n';color=['('+packed+' >> 16) & 255','('+packed+' >> 8) & 255',packed+' & 255','255']
    if call=='setColorRGBA_I':
     assert len(a)==2;alpha=prefix+'Alpha';line+=indent+'int '+alpha+' = Math.max(0, Math.min(255, '+a[1]+'));\n';color[-1]=alpha
    else:assert call=='setColorOpaque_I' and len(a)==1
  elif call in ['addVertex','addVertexWithUV']:
   assert active and color;a=split_args(args);assert len(a)==(5 if textured else 3)
   if textured:assert all(not re.search(r'\w\s*\(|\+\+|--|(?<![=!<>])=(?!=)',v) for v in a[3:])
   vertices.append(a)
   line=indent+buffer+'.pos('+', '.join(a[:3])+')'+('.tex('+', '.join(a[3:])+')' if textured else '')+'.color('+', '.join(color)+').endVertex();'+eol
  elif call=='draw':assert active;active=False;color=None;draws+=1
  else:raise AssertionError(line)
  out.append(line)
 assert not active and len(starts)==draws and draws>0
 return ''.join(out),{'name':name,'format':fmt,'modes':starts,'setters':setters,'vertices':vertices}
def forward_source(relative,old):
 s=old;meta={}
 for method in reversed(SCOPES[relative]):
  a,b=method_span(s,method);nb,record=forward_scope(s[a:b]);s=s[:a]+nb+s[b:];meta[method]=record
 for imp in ['net.minecraft.client.renderer.BufferBuilder','net.minecraft.client.renderer.vertex.DefaultVertexFormats']:assert 'import '+imp+';' not in s
 s=s.replace('import net.minecraft.client.renderer.Tessellator;','import net.minecraft.client.renderer.Tessellator;\nimport net.minecraft.client.renderer.BufferBuilder;\nimport net.minecraft.client.renderer.vertex.DefaultVertexFormats;')
 return s,meta

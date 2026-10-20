"""Finite complete-scope POSITION_TEX transform, test tooling only."""
import importlib.util,pathlib,re
HERE=pathlib.Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('prior_textured_source',HERE.parent/'simple-textured-batches/check_source.py')
prior=importlib.util.module_from_spec(spec);spec.loader.exec_module(prior)
split_args=prior.split_args
SCOPES={
 'src/main/java/com/hbm/render/tileentity/RenderCrucible.java':['render'],
 'src/main/java/com/hbm/render/tileentity/RenderStrandCaster.java':['render'],
 'src/main/java/com/hbm/inventory/gui/GUIScreenGuide.java':['drawImage'],
 'src/main/java/com/hbm/inventory/gui/GUIMachineRadarNT.java':['drawTexturedModalRectDouble'],
 'src/main/java/com/hbm/render/util/SmallBlockPronter.java':['renderSmolBlockAt'],
 'src/main/java/com/hbm/dim/SkyProviderCelestial.java':['renderCelestials','renderRings','renderDigamma','renderStation'],
 'src/main/java/com/hbm/dim/thatmo/SkyProviderThatmo.java':['renderSpecialEffects'],
}
INERT={
 'RenderCrucible':['tess.setNormal(0F, 1F, 0F);'],
 'RenderStrandCaster':['tess.setNormal(0F, 1F, 0F);','tess.setColorOpaque_F(r / 255F, g / 255F, b / 255F);'],
 'GUIScreenGuide':['tessellator.setBrightness(240);','tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, 1.0F);'],
}
def scrub(s):
 return re.sub(r'//[^\n]*|/\*.*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',lambda m:''.join('\n' if c=='\n' else ' ' for c in m[0]),s,flags=re.S)
def method_span(text,method):
 clean=scrub(text)
 matches=list(re.finditer(r'\b(?:public|protected|private)\s+(?:static\s+)?[\w<>\[\]]+\s+'+method+r'\([^;{]*\)\s*\{',clean))
 assert len(matches)==1,(method,len(matches))
 m=matches[0];start=m.start();depth=1;end=m.end()
 while depth:
  depth+=(clean[end]=='{')-(clean[end]=='}');end+=1
 return start,end

def forward_scope(body,stem):
 names=re.findall(r'Tessellator\s+(\w+) = Tessellator.instance;',body)
 assert len(names)==1,names
 name=names[0];buffer=name+'Buffer';assert buffer not in body
 disabled=INERT.get(stem,[])
 for statement in disabled:
  assert body.count(statement)==1 and body.index(statement)<body.index(name+'.startDrawingQuads();'),statement
 uses=re.findall(r'\b'+name+r'\b',scrub(body))
 calls=re.findall(r'\b'+name+r'\.(\w+)\(',scrub(body))
 assert len(uses)==len(calls)+1,(name,'escaped local')
 active=False;starts=draws=0;vertices=[];out=[]
 for line in body.splitlines(True):
  m=re.fullmatch(r'([\t ]*)Tessellator '+name+r' = Tessellator.instance;(\n?)',line)
  if m:
   indent,eol=m.groups();out.append(indent+'Tessellator '+name+' = Tessellator.getInstance();\n'+indent+'BufferBuilder '+buffer+' = '+name+'.getBuffer();'+eol);continue
  if line.strip() in disabled:
   indent=re.match(r'[\t ]*',line)[0]
   call,args=re.search(r'\.(\w+)\((.*)\);',line).groups();a=split_args(args)
   channels={'setNormal':['NormalX','NormalY','NormalZ'],'setColorOpaque_F':['Red','Green','Blue'],'setColorRGBA_F':['Red','Green','Blue','Alpha'],'setBrightness':['Brightness']}[call]
   typ='int' if call=='setBrightness' else 'float'
   out.append(indent+'// Legacy begin resets this attribute; retain its argument evaluation.\n')
   out.append(indent+typ+' '+', '.join(name+'Reset'+c+' = '+v for c,v in zip(channels,a))+';\n')
   continue
  m=re.fullmatch(r'([\t ]*)'+name+r'\.(\w+)\((.*)\);(\n?)',line)
  if m:
   indent,call,args,eol=m.groups()
   if call=='startDrawingQuads':
    assert not active;active=True;starts+=1
    line=indent+buffer+'.begin(7, DefaultVertexFormats.POSITION_TEX);'+eol
   elif call=='addVertexWithUV':
    assert active;a=split_args(args);assert len(a)==5
    # Reviewed UV arithmetic/local reads only; no invocation or side effects.
    assert all(not re.search(r'\w\s*\(|\+\+|--|(?<![=!<>])=(?!=)',x) for x in a[3:]),a
    line=indent+buffer+'.pos('+', '.join(a[:3])+').tex('+', '.join(a[3:])+').endVertex();'+eol
    vertices.append(a)
   elif call=='draw':assert active;active=False;draws+=1
   else:raise AssertionError(('unreviewed call',line))
  out.append(line)
 assert not active and starts==draws and starts>0
 return ''.join(out),{'name':name,'batches':starts,'vertices':vertices,'reset_setters':disabled}

def forward_source(relative,old):
 s=old;meta={};stem=pathlib.Path(relative).stem
 for method in reversed(SCOPES[relative]):
  a,b=method_span(s,method);new,info=forward_scope(s[a:b],stem);s=s[:a]+new+s[b:];meta[method]=info
 for imp in ['net.minecraft.client.renderer.BufferBuilder','net.minecraft.client.renderer.vertex.DefaultVertexFormats']:
  assert 'import '+imp+';' not in s
 s=s.replace('import net.minecraft.client.renderer.Tessellator;', 'import net.minecraft.client.renderer.Tessellator;\nimport net.minecraft.client.renderer.BufferBuilder;\nimport net.minecraft.client.renderer.vertex.DefaultVertexFormats;')
 return s,meta

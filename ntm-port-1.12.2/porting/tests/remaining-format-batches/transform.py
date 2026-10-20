"""Finite closed renderer scopes; native format derives from each complete batch."""
import importlib.util,pathlib,re
HERE=pathlib.Path(__file__).resolve().parent
spec=importlib.util.spec_from_file_location('format_prior',HERE.parent/'remaining-textured-batches/transform.py');prior=importlib.util.module_from_spec(spec);spec.loader.exec_module(prior)
split_args=prior.split_args;scrub=prior.scrub
SCOPES={
 'src/main/java/com/hbm/render/world/RenderNTMSkyboxImpact.java':['initializeDisplayLists','render','renderStars'],
 'src/main/java/com/hbm/dim/laythe/SkyProviderLaytheSunset.java':['renderSunset'],
 'src/main/java/com/hbm/inventory/gui/LoadingScreenRendererNT.java':['setLoadingProgress'],
 'src/main/java/com/hbm/render/util/GaugeUtil.java':['renderGauge','drawSmoothGauge:int colorOuter'],
 'src/main/java/com/hbm/render/tileentity/RenderBobble.java':['renderOrigin'],
 'src/main/java/com/hbm/render/entity/effect/RenderDeathBlast.java':['doRender'],
 'src/main/java/com/hbm/inventory/gui/GUIMachineRadarNT.java':['drawGuiContainerBackgroundLayer'],
}
def method_span(text,selector):
 method,_,filter=selector.partition(':');clean=scrub(text)
 matches=[m for m in re.finditer(r'\b(?:public|protected|private)\s+(?:static\s+)?[\w<>\[\]]+\s+'+method+r'\([^;{]*\)\s*\{',clean) if filter in m[0]]
 assert len(matches)==1,(selector,len(matches));m=matches[0];end=m.end();depth=1
 while depth:depth+=(clean[end]=='{')-(clean[end]=='}');end+=1
 return m.start(),end

def forward_scope(body):
 clean=scrub(body);decls=list(re.finditer(r'(?P<final>final\s+)?Tessellator (?P<name>\w+) = Tessellator.instance;',clean));names={m['name'] for m in decls};assert len(names)==1 and decls
 name=next(iter(names));buffer=name+'Buffer';assert buffer not in body
 calls=list(re.finditer(r'\b'+name+r'\.(?P<method>\w+)\((?P<args>[^;]*)\);',clean))
 assert len(re.findall(r'\b'+name+r'\b',clean))==len(decls)+len(calls),'escaped local'
 allowed={'startDrawing','startDrawingQuads','setColorOpaque_F','setColorRGBA_F','setColorOpaque_I','setColorRGBA_I','addVertex','addVertexWithUV','draw'}
 assert {m['method'] for m in calls}<=allowed
 batches=[];active=None
 for m in calls:
  call=m['method']
  if call.startswith('startDrawing'):assert active is None;active={'mode':m['args'] if call=='startDrawing' else '7','start':m.start(),'calls':[]}
  elif call=='draw':
   assert active is not None
   methods=[c['call'] for c in active['calls']];textured='addVertexWithUV' in methods;colored=any(c.startswith('setColor') for c in methods)
   assert not(textured and 'addVertex' in methods)
   active.update(end=m.end(),format='POSITION'+('_TEX' if textured else '')+('_COLOR' if colored else ''));batches.append(active);active=None
  elif active is not None:active['calls'].append({'call':call,'arguments':split_args(m['args'])})
  else:assert call=='setColorOpaque_F' and m['args']=='1F, 0F, 0F','unexpected pre-begin state'
 assert active is None and batches
 edits=[];setters=[];vertices=[];color=None;batchIndex=-1;reset=0
 for m in decls:
  final=m['final'] or '';indent=re.match(r'[\t ]*',body[body.rfind('\n',0,m.start())+1:m.start()])[0]
  edits.append((m.start(),m.end(),final+'Tessellator '+name+' = Tessellator.getInstance();\n'+indent+final+'BufferBuilder '+buffer+' = '+name+'.getBuffer();'))
 active=False
 for m in calls:
  call,args=m['method'],m['args'];aa=split_args(args);indent=re.match(r'[\t ]*',body[body.rfind('\n',0,m.start())+1:m.start()])[0];new=None
  if call.startswith('startDrawing'):
   active=True;color=None;batchIndex+=1;b=batches[batchIndex];new=buffer+'.begin('+b['mode']+', DefaultVertexFormats.'+b['format']+');'
  elif call.startswith('setColor'):
   assert all(not re.search(r'\w\s*\(|\+\+|--|(?<![=!<>])=(?!=)',a) for a in aa),aa
   if not active:
    reset+=1;new='// Legacy begin resets this attribute; retain its argument evaluation.\n'+indent+'float '+name+'ResetRed = '+aa[0]+', '+name+'ResetGreen = '+aa[1]+', '+name+'ResetBlue = '+aa[2]+';'
   else:
    prefix=name+'Color'+str(len(setters));setters.append({'call':call,'arguments':aa})
    if call in ['setColorRGBA_F','setColorOpaque_F']:
     values=aa+(['1.0F'] if call=='setColorOpaque_F' else []);assert len(values)==4;color=[prefix+c for c in ['Red','Green','Blue','Alpha']]
     new=('\n'+indent).join('int '+v+' = Math.max(0, Math.min(255, (int) (('+x+') * 255.0F)));' for v,x in zip(color,values))
    else:
     packed=prefix+'Packed';new='int '+packed+' = '+aa[0]+';';color=['('+packed+' >> 16) & 255','('+packed+' >> 8) & 255',packed+' & 255','255']
     if call=='setColorRGBA_I':
      assert len(aa)==2;alpha=prefix+'Alpha';new+='\n'+indent+'int '+alpha+' = Math.max(0, Math.min(255, '+aa[1]+'));';color[-1]=alpha
     else:assert call=='setColorOpaque_I' and len(aa)==1
  elif call.startswith('addVertex'):
   assert active and (color is not None)==('COLOR' in batches[batchIndex]['format']);vertices.append(aa)
   if call=='addVertexWithUV':assert len(aa)==5 and all(not re.search(r'\w\s*\(|\+\+|--|(?<![=!<>])=(?!=)',a) for a in aa[3:])
   else:assert len(aa)==3
   new=buffer+'.pos('+', '.join(aa[:3])+')'+('.tex('+', '.join(aa[3:])+')' if len(aa)==5 else '')+('.color('+', '.join(color)+')' if color else '')+'.endVertex();'
  elif call=='draw':active=False;color=None
  if new is not None:edits.append((m.start(),m.end(),new))
 s=body
 for a,b,new in sorted(edits,reverse=True):s=s[:a]+new+s[b:]
 for b in batches:del b['start'];del b['end']
 return s,{'name':name,'acquisitions':len(decls),'batches':batches,'setters':setters,'vertices':vertices,'reset_setters':reset}

def forward_source(relative,old):
 s=old;meta={}
 for method in reversed(SCOPES[relative]):
  a,b=method_span(s,method);nb,r=forward_scope(s[a:b]);s=s[:a]+nb+s[b:];meta[method]=r
 missing=[i for i in ['net.minecraft.client.renderer.BufferBuilder','net.minecraft.client.renderer.vertex.DefaultVertexFormats'] if 'import '+i+';' not in s]
 s=s.replace('import net.minecraft.client.renderer.Tessellator;','import net.minecraft.client.renderer.Tessellator;'+''.join('\nimport '+i+';' for i in missing))
 return s,meta

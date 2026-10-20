#!/usr/bin/env python3
"""Exact CP82 inverses and independent pristine constant-geometry provenance."""
import hashlib,json,re,subprocess,sys
from pathlib import Path
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];REPO=ROOT.parent
rows=json.loads((HERE/'sites.json').read_text())
def method(s,pattern):
 m=re.search(pattern,s);assert m,pattern
 i=m.end();d=1
 while d:
  if s[i]=='{':d+=1
  if s[i]=='}':d-=1
  i+=1
 return s[m.start():i]
def verify(s,r):
 assert hashlib.sha256(s).hexdigest()==r['after_sha256'],r['path']+' current source drift'
 t=s.decode()
 for c in reversed(r['changes']):
  assert t.count(c['after'])==1,(r['path'],c)
  t=t.replace(c['after'],c['before'],1)
 assert hashlib.sha256(t.encode()).hexdigest()==r['before_sha256'],r['path']+' exact inverse'
 ref=subprocess.check_output(['git','show','df18fc077648dd18e6d0c1055d82c11dc2ebdb78:ntm-port-1.12.2/'+r['path']],cwd=REPO)
 assert t.encode()==ref
 return t
for r in rows:
 current=(ROOT/r['path']).read_bytes();before=verify(current,r)
 pristine=(REPO/'ntm-original-pristine'/r['path']).read_text()
 name=Path(r['path']).stem
 ctor=method(before,r'public '+name+r'\([^)]*\)\s*\{')
 original=method(pristine,r'public '+name+r'\([^)]*\)\s*\{')
 # All geometry statements have original wording, arguments, and local float evaluation.
 geometry=lambda t:re.findall(r'(?:float f = [^;]+;|(?:this\.)?setBlockBounds\([^;]+;)',t)
 assert geometry(ctor)==geometry(original) and len(geometry(ctor)) in (1,2)
 assert before.count('setBlockBounds(')==1
 assert not re.search(r'(?:setBlockBoundsBasedOnState|setBlockBoundsForItemRender|collisionRayTrace|addCollisionBoxesToList|getSelectedBoundingBoxFromPool)\s*\(',before)
 for needle in ['getBoundingBox','return shape;',r['changes'][1]['after']]:
  mutated=current.replace(needle.encode(),(needle+' /* drift */').encode(),1)
  try:verify(mutated,r)
  except AssertionError:pass
  else:raise AssertionError('accepted mutation '+needle)
print('PASS 4 exact CP82 inverses; 4 pristine constructor geometry matches; 12 source mutants rejected')
if len(sys.argv)>1:
 out=Path(sys.argv[1]);out.mkdir(parents=True,exist_ok=True)
 for r in rows:
  s=(ROOT/r['path']).read_text();name=Path(r['path']).stem;pkg=re.search(r'package ([^;]+)',s)[1];base='BlockBush' if name=='BlockCrop' else 'Block'
  ctor=method(s,r'public '+name+r'\([^)]*\)\s*\{')
  shape=method(s,r'public net.minecraft.util.math.AxisAlignedBB getBoundingBox\([^)]*\)\s*\{')
  extra=''
  if name=='BlockMush':extra+='@Override '+method(s,r'public AxisAlignedBB getCollisionBoundingBox\([^)]*\)\s*\{')
  for m in ['isFullCube','isOpaqueCube']:
   if re.search(r'public boolean '+m,s):extra+='@Override '+method(s,r'public boolean '+m+r'\([^)]*\)\s*\{')
  fields='private final net.minecraft.util.math.AxisAlignedBB shape;'
  if name=='BlockCrop':fields+='protected Block soilBlock; private Predicate<CBT_Atmosphere> atmospherePredicate; public boolean canHydro;'
  projected='package '+pkg+'; import net.minecraft.block.*; import net.minecraft.block.material.*; import net.minecraft.util.math.*; import net.minecraft.world.*; import com.hbm.blocks.ModBlocks; import java.util.function.Predicate; import com.hbm.dim.trait.CBT_Atmosphere; public class '+name+' extends '+base+' {'+fields+ctor+'@Override '+shape+extra+'}'
  path=out/Path(r['path']).relative_to('src/main/java');path.parent.mkdir(parents=True,exist_ok=True);path.write_text(projected)

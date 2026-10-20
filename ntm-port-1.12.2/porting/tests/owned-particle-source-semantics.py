#!/usr/bin/env python3
"""Check retained custom updates and all migrated quad position/UV expressions."""
import importlib.util,re
from pathlib import Path
root=Path(__file__).resolve().parents[2]
spec=importlib.util.spec_from_file_location('owned',Path(__file__).with_name('check-native-owned-particles-source.py')); audit=importlib.util.module_from_spec(spec);spec.loader.exec_module(audit)
def body(s,marker):
 p=s.find(marker)
 if p<0:return None
 p=s.index('{',p); level=1; q=p+1
 while level:
  level += (s[q]=='{')-(s[q]=='}');q+=1
 return s[p:q]
def compact(s):return re.sub(r'\s+','',s)
def args(s,pos):
 depth=1; q=pos; start=pos; result=[]
 while depth:
  c=s[q]
  if c=='(':depth+=1
  elif c==')':depth-=1
  if (c==',' and depth==1) or depth==0:
   result.append(compact(s[start:q]));start=q+1
  q+=1
 return result,q
updates=vertices=0
for rel in audit.FILES:
 current=(root/rel).read_bytes().decode(); old=audit.reverse_source(rel,current)
 original=body(old,'void onUpdate()'); migrated=body(current,'void onUpdate()')
 if original is not None:
  expected=original.replace('setDead()', 'setExpired()').replace('worldObj.getHeightValue(x, z)', 'world.getHeight(new net.minecraft.util.math.BlockPos(x, 0, z)).getY()').replace('worldObj.spawnParticle("lava",','world.spawnParticle(net.minecraft.util.EnumParticleTypes.LAVA,')
  assert compact(expected)==compact(migrated),(rel,'custom update formula/order drift');updates+=1
 else: assert migrated is None
 original_args=[args(old,m.end())[0] for m in re.finditer(r'\.addVertexWithUV\(',old)]
 new_args=[]
 for m in re.finditer(r'\.pos\(',current):
  xyz,end=args(current,m.end())
  if current[end:end+5]=='.tex(':uv,_=args(current,end+5);new_args.append(xyz+uv)
 if original_args:
  assert original_args==new_args,(rel,'quad expression/order drift');vertices+=len(original_args)
print('PASS %d exact custom update bodies after explicit native API mapping; %d exact ordered position/UV expression tuples. Native superclass/GL behavior is separately scoped.'%(updates,vertices))

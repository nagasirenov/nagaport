#!/usr/bin/env python3
"""Read actual cached class files, classpath-first, to audit all external tile getter overrides."""
import struct,zipfile,pathlib,sys,json
class Reader:
 def __init__(self,b):self.b=b;self.i=0
 def read(self,n):v=self.b[self.i:self.i+n];self.i+=n;return v
 def u1(self):return self.read(1)[0]
 def u2(self):return struct.unpack('>H',self.read(2))[0]
 def u4(self):return struct.unpack('>I',self.read(4))[0]
def info(b):
 r=Reader(b);assert r.u4()==0xcafebabe;r.read(4);n=r.u2();cp=[None]*n;i=1
 while i<n:
  tag=r.u1()
  if tag==1:cp[i]=r.read(r.u2()).decode('utf8',errors='replace')
  elif tag==7:cp[i]=r.u2()
  elif tag in [3,4,9,10,11,12,17,18]:r.read(4)
  elif tag in [5,6]:r.read(8);i+=1
  elif tag in [8,16,19,20]:r.read(2)
  elif tag==15:r.read(3)
  else:raise AssertionError(tag)
  i+=1
 r.u2();this=r.u2();sup=r.u2();name=cp[cp[this]];parent=cp[cp[sup]] if sup else None
 r.read(r.u2()*2)
 def members():
  out=[]
  for _ in range(r.u2()):
   access=r.u2();nm=cp[r.u2()];desc=cp[r.u2()];out.append((nm,desc,access))
   for _ in range(r.u2()):r.u2();r.read(r.u4())
  return out
 members();return name,parent,members()
def audit(cp):
 classes={}
 for name in cp.split(':'):
  p=pathlib.Path(name)
  if p.suffix=='.jar' and p.exists():
   with zipfile.ZipFile(p) as z:
    for item in z.namelist():
     if item.endswith('.class') and not item.startswith('META-INF/versions/'):
      key,parent,methods=info(z.read(item));classes.setdefault(key,(parent,methods))
  elif p.is_dir():
   for f in p.rglob('*.class'):
    key,parent,methods=info(f.read_bytes());classes.setdefault(key,(parent,methods))
 base='net/minecraft/tileentity/TileEntity';descendants=[];overrides=[]
 for name,(parent,methods) in classes.items():
  if name==base:continue
  ancestry=set();cur=parent
  while cur and cur!=base and cur not in ancestry:ancestry.add(cur);cur=classes.get(cur,(None,[]))[0]
  if cur==base:
   descendants.append(name)
   if any(n in ('getWorld','getWorldObj') and d.startswith('()') for n,d,a in methods):overrides.append(name)
 assert not overrides,overrides
 print('PASS',len(descendants),'actual dependency TileEntity descendants; no getWorld/getWorldObj override among',len(classes),'class files')
 return descendants
if __name__=='__main__':audit(pathlib.Path(sys.argv[1]).read_text().strip())

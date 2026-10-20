import struct, zipfile, json, pathlib, io
class Class:
 def __init__(self,b):
  self.b=b; self.p=8; self.cp=[None]; n=self.u2(); i=1
  while i<n:
   t=self.u1()
   if t==1:
    l=self.u2(); self.cp.append(self.take(l).decode('utf8','replace'))
   elif t in (7,8,16,19,20): self.cp.append((t,self.u2()))
   elif t in (3,4): self.cp.append((t,self.take(4)))
   elif t in (5,6): self.cp.append((t,self.take(8))); self.cp.append(None); i+=1
   elif t in (9,10,11,12,18,17): self.cp.append((t,self.u2(),self.u2()))
   elif t==15: self.cp.append((t,self.u1(),self.u2()))
   else: raise Exception(t)
   i+=1
  self.access=self.u2(); self.name=self.cls(self.u2()); self.sup=self.cls(self.u2()); self.itf=[self.cls(self.u2()) for _ in range(self.u2())]; self.fields=self.members(); self.methods=self.members()
 def take(self,n): v=self.b[self.p:self.p+n]; self.p+=n; return v
 def u1(self): return self.take(1)[0]
 def u2(self): return int.from_bytes(self.take(2),'big')
 def u4(self): return int.from_bytes(self.take(4),'big')
 def cls(self,i): return self.cp[self.cp[i][1]] if i else None
 def members(self):
  r=[]
  for _ in range(self.u2()):
   a,n,d=self.u2(),self.u2(),self.u2(); attrs={}
   for _ in range(self.u2()):
    k=self.cp[self.u2()]; attrs[k]=self.take(self.u4()).hex()
   r.append({'access':a,'name':self.cp[n],'desc':self.cp[d],'attrs':attrs})
  return r

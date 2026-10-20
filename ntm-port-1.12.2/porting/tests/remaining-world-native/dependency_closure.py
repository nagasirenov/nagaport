#!/usr/bin/env python3
"""Frozen native dependency closure, classpath-first. Unknown external mods are not covered."""
import importlib.util,json,os,pathlib,sys,zipfile
h=pathlib.Path(__file__).resolve().parent;p=h.parent/'tile-world-reads/dependency_closure.py';s=importlib.util.spec_from_file_location('class_reader',p);reader=importlib.util.module_from_spec(s);s.loader.exec_module(reader)
def audit(cp):
 classes={}
 for entry in cp.split(os.pathsep):
  p=pathlib.Path(entry)
  if p.suffix=='.jar' and p.exists():
   with zipfile.ZipFile(p) as z:
    for item in z.namelist():
     if item.endswith('.class') and not item.startswith('META-INF/versions/'):
      name,parent,methods=reader.info(z.read(item));classes.setdefault(name,(parent,methods))
  elif p.is_dir():
   for f in p.rglob('*.class'):
    name,parent,methods=reader.info(f.read_bytes());classes.setdefault(name,(parent,methods))
 base='net/minecraft/world/chunk/Chunk';children=[]
 for name,(parent,methods) in classes.items():
  if name==base:continue
  visited=set();cur=parent
  while cur and cur!=base and cur not in visited:visited.add(cur);cur=classes.get(cur,(None,[]))[0]
  if cur==base:
   children.append(name);assert not any(n in ('getWorld','getWorldObj') and d.startswith('()') for n,d,a in methods),(name,methods)
 assert sorted(children)==['net/minecraft/world/chunk/EmptyChunk'],children
 print('PASS',len(classes),'native dependency class files; sole Chunk descendant EmptyChunk has no world getter override')
 return classes
if __name__=='__main__':audit(pathlib.Path(sys.argv[1]).read_text().strip())

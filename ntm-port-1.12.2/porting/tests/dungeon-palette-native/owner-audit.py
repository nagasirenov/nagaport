#!/usr/bin/env python3
"""Read-only textual owner audit against pinned CP56; not a Java attribution proof."""
import collections, json, pathlib, re, subprocess
ROOT=pathlib.Path(__file__).resolve().parents[3]
BASE='149469c7403653ca6576515b730bc033a1574cfe'
def read(p): return subprocess.check_output(['git','show',BASE+':./'+p],cwd=ROOT,text=True)
paths=subprocess.check_output(['git','ls-tree','-r','--name-only',BASE,'src/main/java'],cwd=ROOT,text=True).splitlines()
sources={p:read(p) for p in paths if p.endswith('.java')}
names=['Spaceship','Spaceship2','Ruin001','Ruin002','Barrel','DesertAtom001','DesertAtom002','DesertAtom003','Silo']
mod=sources['src/main/java/com/hbm/blocks/ModBlocks.java']
result={'baseline':BASE,'scope':'Textual source inventory; Java symbol attribution remains separate','classes':{},'subclasses':[],'qualified_palette_assignments':[]}
for name in names:
 p='src/main/java/com/hbm/world/dungeon/'+name+'.java'; s=sources[p]; fields={}
 for m in re.finditer(r'^[ \t]*Block\s+(Block\d+|sellafield)\s*=\s*ModBlocks\.(\w+)\s*;',s,re.M):
  field,owner=m.groups(); constructors=[]
  for op,os in sources.items():
   # ModBlocks declaration initialization or qualified assignment from any source.
   pattern=(r'(?<![\w.])'+owner+r'\s*=(?!=)' if op.endswith('/ModBlocks.java') else r'\bModBlocks\.'+owner+r'\s*=(?!=)')
   for om in re.finditer(pattern,os):
    line=os.count('\n',0,om.start())+1; constructors.append({'path':op,'line':line,'text':os.splitlines()[line-1].strip()})
  calls=collections.Counter(re.findall(r',\s*'+field+r'\s*,\s*([^,()]+)\s*,\s*([^,()]+)\s*\)',s))
  assignments=[s.count('\n',0,x.start())+1 for x in re.finditer(r'\b'+field+r'\s*=(?!=)',s)]
  fields[field]={'owner':'ModBlocks.'+owner,'declaration_line':s.count('\n',0,m.start())+1,'assignments_in_class':assignments,'placements':[{'metadata':k[0].strip(),'flags':k[1].strip(),'count':v} for k,v in calls.items()],'owner_assignments':constructors}
 result['classes'][name]={'path':p,'fields':fields}
for p,s in sources.items():
 for m in re.finditer(r'\bextends\s+('+'|'.join(names)+r')\b',s):result['subclasses'].append({'path':p,'line':s.count('\n',0,m.start())+1,'text':m.group()})
 for m in re.finditer(r'\.Block\d+\s*=(?!=)',s):result['qualified_palette_assignments'].append({'path':p,'line':s.count('\n',0,m.start())+1,'text':m.group()})
print(json.dumps(result,indent=2))

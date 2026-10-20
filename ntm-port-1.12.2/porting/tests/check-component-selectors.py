#!/usr/bin/env python3
"""Compare selector decision/RNG bodies with the untouched 1.7.10 reference.
Static source parity only; no claim that unported NTM block state mappings work.
"""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[2]
reference=root.parent/'ntm-original-pristine'
files=['world/gen/component/Component.java','world/gen/component/CivilianFeatures.java','world/gen/component/SiloComponent.java']
def body(s,start):
 i=s.index('{',start);j=i+1;d=1
 while d:d+=(s[j]=='{')-(s[j]=='}');j+=1
 return s[i+1:j-1]
def selectors(s):
 result={}
 for m in re.finditer(r'class (\w+) extends (?:StructureComponent\.)?BlockSelector\s*\{',s):
  c=body(s,m.start()); result[m[1]]=body(c,c.index('public void selectBlocks('))
 return result
def norm(s):
 s=re.sub(r'this\.blockstate = [^;]*;', '',s)
 s=s.replace('net.minecraft.block.Block selectedBlock;', '')
 s=s.replace('this.','').replace('field_151562_a','selectedBlock').replace('selectedBlockMetaData','selectedMetadata')
 s=re.sub(r'(?<!Mod)Blocks\.([A-Z_]+)',lambda m:'Blocks.'+m[1].lower(),s)
 return re.sub(r'\s+', '',s)
count=0
for f in files:
 old=selectors((reference/'src/main/java/com/hbm'/f).read_text());new=selectors((root/'src/main/java/com/hbm'/f).read_text())
 assert old.keys()==new.keys(),f
 for name in old:
  assert norm(old[name])==norm(new[name]),(f,name)
  count+=1
print('PASS: '+str(count)+' selector decision bodies match pristine after field/API renames; RNG branches and metadata accumulation preserved (static only).')

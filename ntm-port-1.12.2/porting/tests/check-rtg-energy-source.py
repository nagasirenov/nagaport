#!/usr/bin/env python3
"""Pin original RTG energy/decay methods to dff345a, allowing only native API spellings."""
from pathlib import Path
import re, subprocess
root=Path(__file__).resolve().parents[2]
def original(path):
 return subprocess.check_output(['git','show','dff345a:ntm-original-pristine/src/main/java/'+path],cwd=root,text=True)
def body(text,signature):
 start=text.index(signature);start=text.index('{',start);i=start+1;depth=1
 while depth:
  depth+=(text[i]=='{')-(text[i]=='}');i+=1
 return text[start:i]
def normalize(s):
 s=s.replace('stack.stackTagCompound = new NBTTagCompound();','stack.setTagCompound(new NBTTagCompound());').replace('stack.stackTagCompound','stack.getTagCompound()')
 s=s.replace('worldObj','world').replace('xCoord','pos.getX()').replace('yCoord','pos.getY()').replace('zCoord','pos.getZ()').replace('this.','')
 return re.sub(r'\s+','',s)
checks=0
for path,methods in {
 'com/hbm/tileentity/machine/TileEntityMachineRTG.java':['update','serialize','deserialize','getPowerScaled','getHeatScaled','provideExtraInfo'],
 'com/hbm/tileentity/machine/TileEntityMachineMiniRTG.java':['update','getOutput','getMaxPower','provideExtraInfo'],
 'com/hbm/items/machine/ItemRTGPellet.java':['handleDecay','decay','getLifespan','getScaledPower','setDecays','getDecayItem'],
 'com/hbm/util/RTGUtil.java':['getPower','hasHeat','updateRTGs','getLifespan']
}.items():
 old=original(path);new=(root/'src/main/java'/path).read_text()
 for method in methods:
  # Anchor declaration, not earlier call sites.
  signature=re.search(r'public (?:static )?[^\n{]+\b'+method+r'\(',new).group(0)
  oldsignature=signature.replace('update(', 'updateEntity(')
  assert normalize(body(old,oldsignature))==normalize(body(new,signature)),(path,method)
  checks+=1
print('PASS',checks,'complete original RTG/Mini/pellet/utility method bodies match dff345a after explicit native API-only normalization')

#!/usr/bin/env python3
"""Pinned exact metadata methods on real EntityFlying; explicitly not whole production owners."""
from pathlib import Path
import hashlib, itertools, json, os, re, subprocess, sys, tempfile
folder=Path(__file__).resolve().parent;root=folder.parents[3];manifest=json.loads((folder/'source-manifest.json').read_text());prefix='ntm-port-1.12.2/src/main/java/';slices={};units={}
def source(p):return (root/(prefix+p)).read_text()
def extract(s,pattern):
 m=re.search(pattern,s);assert m,pattern;i=s.index('{',m.end()-1);j=i+1;d=1
 while d:d+=(s[j]=='{')-(s[j]=='}');j+=1
 return s[m.start():j]
def keep(owner,label,s):slices[owner+'.'+label]={'source':s,'sha256':hashlib.sha256(s.encode()).hexdigest()};return s
def method(s,n):return extract(s,r'(?:public|protected) (?:static )?[\w<>]+ '+n+r'\([^)]*\)\s*\{')
imports='''import net.minecraft.entity.*;import net.minecraft.entity.monster.IMob;import net.minecraft.world.World;import net.minecraft.network.datasync.*;import net.minecraft.nbt.*;import com.hbm.entity.boss.INtmBossDisplay;import api.hbm.entity.*;'''
for e in manifest['entries']:
 s=(root/e['path']).read_text();name=Path(e['path']).stem;parts=[]
 declaration=keep(name,'actual-class-declaration',re.search(r'public class '+name+r'[^\n]+\{',s).group())
 for i,k in enumerate(re.findall(r'private static final DataParameter<\w+> [^;]+;',s)):parts.append(keep(name,'key-'+str(i),k))
 parts.append(keep(name,'constructor',extract(s,r'public '+name+r'\([^)]*\)\s*\{')))
 names=['entityInit','writeEntityToNBT','readEntityFromNBT']
 if name=='EntityHunterChopper':
  names+=['func_110182_bF','setIsDying','getIsDying']
  for field in ['public int attackCounter;','public boolean isDying = false;']:assert field in s;parts.append(keep(name,field,field))
  action=method(s,'updateEntityActionState');block=extract(action[action.index('\n\t\t\tif(!this.world.isRemote) {\n\t\t\t\tbyte b1'):],r'if\(!this.world.isRemote\)\s*\{')
  parts.append('public void updateActionMetadataBlockOnly() { '+keep(name,'updateEntityActionState-exact-inner-server-metadata-block',block)+' }')
 else:
  names+=['setBeam','getBeam','setWaypoint','getX','getY','getZ']
  for field in ['public int scanCooldown;','public int beamTimer;']:assert field in s;parts.append(keep(name,field,field))
 for n in names:parts.append(keep(name,n,method(s,n)))
 units['com/hbm/entity/mob/'+name+'.java']='package com.hbm.entity.mob;'+imports+declaration+'\n'+'\n'.join(parts)+'\n}'
for path in ['com/hbm/entity/boss/INtmBossDisplay.java','api/hbm/entity/IRadiationImmune.java','api/hbm/entity/ISuffocationImmune.java']:units[path]=keep(path,'whole-interface',source(path))
# Actual renderer and sound decisions, without GL or legacy sound superclass.
parts=[]
for name,owner in [('RenderUFO','EntityUFO'),('RenderBFA','EntityBFAngel')]:
 s=source('com/hbm/render/entity/mob/'+name+'.java');cond=keep(name,'beam-condition-expression',re.search(r'if\((ufo.getBeam\(\))\)',s)[1]);parts.append('static boolean beam('+owner+' ufo) { return '+cond+'; }')
for name in ['MovingSoundChopper','MovingSoundCrashing']:
 s=source('com/hbm/sound/'+name+'.java');cond=keep(name,'stop-condition-expression',re.search(r'if\((player instanceof EntityHunterChopper[^\n]+)\)',s)[1]);parts.append('static boolean stop'+name+'(Entity player) { return '+cond+'; }')
units['Consumers.java']='import com.hbm.entity.mob.*;import net.minecraft.entity.Entity;class Consumers {'+'\n'.join(parts)+'}'
if '--record-slices' in sys.argv:(folder/'slices.json').write_text(json.dumps(slices,indent=2)+'\n')
assert slices==json.loads((folder/'slices.json').read_text())
cp=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/workspace/shared/nagaport-toolchain/native-classpath.txt')).read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
with tempfile.TemporaryDirectory(prefix='native-flying-boss-') as d:
 t=Path(d);files=[]
 for name,s in units.items():p=t/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s);files.append(str(p))
 subprocess.run([str(java/'javac'),'-encoding','UTF-8','-cp',cp,'-d',d]+files+[str(folder/'Probe.java')],check=True)
 for permutation in itertools.permutations(['EntityUFO','EntityBFAngel','EntityHunterChopper']):subprocess.run([str(java/'java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+cp,'Probe',','.join(permutation)],cwd=d,check=True)
print('PASS',len(slices),'exact source slices; six fresh sibling-loading permutations; bounded metadata/constructor/NBT/consumer projections only')

#!/usr/bin/env python3
"""Complete original-derived constructors/metadata/NBT plus lookup prefix; no beam effects/game proof."""
from pathlib import Path
import os,re,json,hashlib,subprocess,tempfile,sys
folder=Path(__file__).resolve().parent;root=folder.parents[3];src=root/'ntm-port-1.12.2/src/main/java/com/hbm/entity/projectile';slices={}
def extract(s,pat):
 m=re.search(pat,s);assert m,pat
 i=s.index('{',m.end()-1);end=i+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[m.start():end]
def keep(n,label,s):slices[n+'.'+label]={'sha256':hashlib.sha256(s.encode()).hexdigest(),'source':s};return s
imports='import net.minecraft.entity.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.world.World;import net.minecraft.util.math.Vec3d;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.network.datasync.*;'
units={}
for name,parent in [('EntityBeamBase','Entity'),('EntityBeamVortex','EntityBeamBase')]:
 s=(src/(name+'.java')).read_text();parts=[]
 for args in [r'World world',r'World world, EntityPlayer player']:parts.append(keep(name,'ctor-'+args,extract(s,r'public '+name+r'\('+args+r'\)\s*\{')))
 if name=='EntityBeamBase':
  parts.append(keep(name,'key',re.search(r'protected static final DataParameter<String> [^;]+;',s).group()))
  parts.append(keep(name,'getOwnerDisplayName',extract(s,r'public String getOwnerDisplayName\(\)\s*\{')))
  for m in ['entityInit','readEntityFromNBT','writeEntityToNBT']:parts.append(keep(name,m,extract(s,r'protected void '+m+r'\([^)]*\)\s*\{')))
 else:
  full=extract(s,r'public void onUpdate\(\)\s*\{');prefix=full[full.index('{')+1:full.index('\n\t\tif(player != null)')];keep(name,'update-prefix',prefix)
  # Only the exact original prefix is executed. The post-lookup effects are not substituted.
  parts.append('public void lookupPrefix() {'+prefix+' LookupObservation.record(player);}')
 units[name]=imports+'\n'+('abstract ' if name=='EntityBeamBase' else '')+'class '+name+' extends '+parent+' {'+'\n'.join(parts)+'}'
renderer=(src.parents[1]/'render/entity/projectile/RenderVortexBeam.java').read_text();line=next(l.strip() for l in renderer.splitlines() if 'EntityPlayer player = ' in l);keep('RenderVortexBeam','owner-lookup',line);units['RendererLookup']=imports+' class RendererLookup {static EntityPlayer lookup(EntityBeamBase laser) {'+line+' return player;}}'
expected=folder/'slices.json'
if '--record-slices' in sys.argv:expected.write_text(json.dumps(slices,indent=2)+'\n')
assert slices==json.loads(expected.read_text())
cpfile=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt'));assert cpfile.is_file();cp=cpfile.read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
with tempfile.TemporaryDirectory(prefix='native-beam-owner-') as d:
 t=Path(d);files=[]
 for name,s in units.items():p=t/(name+'.java');p.write_text(s);files.append(str(p))
 files.append(str(folder/'Probe.java'));subprocess.run([str(java/'javac'),'-encoding','UTF-8','-cp',cp,'-d',d]+files,check=True)
 subprocess.run([str(java/'java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+cp,'Probe'],cwd=d,check=True)
print('PASS '+str(len(slices))+' exact beam source slices; no effects, damage, rendering, whole-owner or game claim')

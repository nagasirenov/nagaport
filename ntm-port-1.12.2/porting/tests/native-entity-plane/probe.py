#!/usr/bin/env python3
"""Bounded exact source methods, genuine native metadata/Forge APIs; no game/world-startup claim."""
from pathlib import Path
import hashlib,json,os,re,subprocess,sys,tempfile
folder=Path(__file__).resolve().parent;root=folder.parents[3];manifest=json.loads((folder/'source-manifest.json').read_text());src=root/'ntm-port-1.12.2/src/main/java/com/hbm';slices={}
def baseline(rel):return subprocess.check_output(['git','-C',str(root),'show',manifest['base']+':ntm-port-1.12.2/src/main/java/com/hbm/'+rel]).decode()
def extract(s,pat):
 m=re.search(pat,s);assert m,pat
 i=s.index('{',m.end()-1);end=i+1;depth=1
 while depth:depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[m.start():end]
def keep(n,label,s):slices[n+'.'+label]={'sha256':hashlib.sha256(s.encode()).hexdigest(),'source':s};return s
imports='''package com.hbm.entity.logic;
import java.util.*;import com.google.common.collect.ImmutableSet;
import com.hbm.main.MainRegistry;import com.hbm.config.GeneralConfig;import com.hbm.sound.AudioWrapper;import com.hbm.util.EnumUtil;
import net.minecraft.entity.*;import net.minecraft.world.World;import net.minecraft.util.math.*;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.network.datasync.*;
import net.minecraftforge.common.ForgeChunkManager;import net.minecraftforge.common.ForgeChunkManager.Ticket;import net.minecraftforge.common.ForgeChunkManager.Type;
'''
units={}
for name,parent in [('EntityPlaneBase','Entity'),('EntityBomber','EntityPlaneBase'),('EntityC130','EntityPlaneBase')]:
 s=(src/'entity/logic'/(name+'.java')).read_text();parts=[]
 parts.append(keep(name,'constructor',extract(s,r'public '+name+r'\(World world\)\s*\{')))
 fields={'EntityPlaneBase':[r'private Ticket loaderTicket;',r'private List<ChunkPos> loadedChunks = [^;]+;',r'public float health = getMaxHealth\(\);',r'public int timer = getLifetime\(\);'], 'EntityBomber':[r'int bombStart = 75;',r'int bombStop = 125;',r'int bombRate = 3;',r'int type = 0;',r'protected AudioWrapper audio;'],'EntityC130':[r'protected AudioWrapper audio;',r'public C130PayloadType payload = C130PayloadType.SUPPLIES;']}[name]
 for f in fields:parts.append(keep(name,f,re.search(f,s).group()))
 if name!='EntityC130':parts.append(keep(name,'key',re.search(r'(?:protected|private) static final DataParameter<[^>]+> [^;]+;',s).group()))
 methods={'EntityPlaneBase':['getMaxHealth','getLifetime','entityInit','init','rotation','loadNeighboringChunks','clearChunkLoader','setDead','readEntityFromNBT','writeEntityToNBT'],'EntityBomber':['entityInit','getBomberStyle','fac','statFacCarpet','statFacNapalm','statFacChlorine','statFacOrange','statFacABomb','statFacStinger','statFacBoxcar','statFacPC','statFacCV','statFacCarpetJet','readEntityFromNBT','writeEntityToNBT'],'EntityC130':['readEntityFromNBT','writeEntityToNBT','fac']}[name]
 for m in methods:parts.append(keep(name,m,extract(s,r'(?:public|protected) (?:static )?[\w<>]+ '+m+r'\([^)]*\)\s*\{')))
 if name=='EntityPlaneBase':
  full=extract(s,r'public void onUpdate\(\)\s*\{');prefix=full[full.index('{')+1:full.index('\n\n\t\tif(this.world.isRemote)')];parts.append('public void synchronizeHealthPrefix() {'+keep(name,'exact-health-update-prefix',prefix)+'\n}')
 else:
  full=extract(s,r'public void onUpdate\(\)\s*\{');block=extract(full,r'if\(this.world.isRemote\)\s*\{');parts.append('public void audioConsumerBlock() {'+keep(name,'exact-audio-consumer-block',block)+'\n}')
 if name=='EntityC130':parts.append(keep(name,'payload-enum',extract(s,r'public static enum C130PayloadType\s*\{')))
 units['com/hbm/entity/logic/'+name+'.java']=imports+'\npublic '+('abstract ' if name=='EntityPlaneBase' else '')+'class '+name+' extends '+parent+(' implements IChunkLoader' if name=='EntityPlaneBase' else '')+' {\n'+'\n'.join(parts)+'\n}'
for path in ['entity/logic/IChunkLoader.java','util/EnumUtil.java']:units['com/hbm/'+path]=keep(path,'full-baseline-helper',baseline(path))
r=(src/'render/entity/item/RenderBomber.java').read_text();line=next(l.strip() for l in r.splitlines() if 'int i = ' in l);units['com/hbm/render/entity/item/RendererLookup.java']='package com.hbm.render.entity.item;import net.minecraft.entity.Entity;import com.hbm.entity.logic.EntityBomber;public class RendererLookup {public static int style(Entity entity) {'+keep('RenderBomber','style-lookup',line)+'return i;}}'
if '--record-slices' in sys.argv:(folder/'slices.json').write_text(json.dumps(slices,indent=2)+'\n')
assert slices==json.loads((folder/'slices.json').read_text())
cpfile=Path(os.environ.get('NAGAPORT_NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt'));assert cpfile.is_file();cp=cpfile.read_text().strip();java=Path(os.environ.get('JAVA_HOME','/workspace/shared/nagaport-toolchain/java8'))/'bin'
with tempfile.TemporaryDirectory(prefix='native-plane-data-') as d:
 t=Path(d);files=[]
 for name,s in units.items():p=t/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s);files.append(str(p))
 files += [str(p) for p in (folder/'fixtures').rglob('*.java')]+[str(folder/'Probe.java')]
 subprocess.run([str(java/'javac'),'-encoding','UTF-8','-cp',cp,'-d',d]+files,check=True)
 for order in ['bomber-first','c130-first']:subprocess.run([str(java/'java'),'-Djava.awt.headless=true','-cp',d+os.pathsep+cp,'com.hbm.entity.logic.Probe',order],cwd=d,check=True)
print('PASS '+str(len(slices))+' exact source slices; no flight/damage/payload/GL or whole-owner/game proof')

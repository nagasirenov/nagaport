#!/usr/bin/env python3
"""Offline bytecode proof of original instance state, native global state and inherited dispatch."""
from pathlib import Path
import hashlib,json,os,re,subprocess,tempfile,zipfile
H=Path(__file__).resolve().parent
J=Path(os.environ.get('JAVA8_HOME','/workspace/shared/nagaport-toolchain/java8'))
CP=Path(os.environ.get('NATIVE_CP_FILE','/workspace/shared/nagaport-toolchain/native-classpath.txt')).read_text().strip()
OLD=Path(os.environ.get('LEGACY_CLIENT_JAR','/tmp/nagaport-legacy-client.jar'))
SRG=Path(os.environ.get('LEGACY_SRG_ZIP','/workspace/shared/nagaport-toolchain/gradle-user-home/caches/modules-2/files-2.1/de.oceanlabs.mcp/mcp/1.7.10/10b95bb342f55d681025bf2d698c893d9c4b4201/mcp-1.7.10-srg.zip'))
NATIVE=next(Path(x) for x in CP.split(os.pathsep) if Path(x).is_file() and zipfile.is_zipfile(x) and 'net/minecraft/entity/Entity.class' in zipfile.ZipFile(x).namelist())
def sha(b):return hashlib.sha256(b).hexdigest()
def inspect():
 lines=zipfile.ZipFile(SRG).read('joined.srg').decode().splitlines()
 native_classes=['net.minecraft.entity.Entity','net.minecraft.entity.EntityLivingBase','net.minecraft.entity.EntityLiving','net.minecraft.entity.EntityCreature','net.minecraft.entity.monster.EntityMob','net.minecraft.util.math.AxisAlignedBB']
 legacy_names=['net/minecraft/entity/Entity','net/minecraft/entity/EntityLivingBase','net/minecraft/entity/EntityLiving','net/minecraft/entity/EntityCreature','net/minecraft/entity/monster/EntityMob','net/minecraft/util/AxisAlignedBB']
 mapping=[next(s for s in lines if s.startswith('CL: ') and s.endswith(' '+n)) for n in legacy_names]
 mapping += ['FD: sa/j net/minecraft/entity/Entity/field_70155_l','MD: sa/a (D)Z net/minecraft/entity/Entity/func_70112_a (D)Z']
 assert all(x in lines for x in mapping)
 result={'inputs':{'legacy_sha256':sha(OLD.read_bytes()),'native_sha256':sha(NATIVE.read_bytes()),'srg_sha256':sha(SRG.read_bytes())},'mapping_lines':mapping,'classes':{}}
 texts={}
 for kind,jar,classes in [('legacy',OLD,[x.split()[1] for x in mapping[:6]]),('native',NATIVE,native_classes)]:
  with zipfile.ZipFile(jar) as z:
   for name in classes:
    b=z.read(name.replace('.','/')+'.class');key=kind+':'+name
    out=subprocess.check_output([str(J/'bin/javap'),'-p','-c','-s','-classpath',str(jar),name],text=True)
    result['classes'][key]={'sha256':sha(b),'javap_sha256':sha(out.encode())};texts[key]=out
 legacy=texts['legacy:sa'];native=texts['native:net.minecraft.entity.Entity']
 assert '\n  public double j;' in legacy and '\n  private static double renderDistanceWeight;' in native
 def body(txt,header):
  pos=txt.index('\n  '+header);end=txt.find('\n  ',pos+3)
  # javap continuation indentation is >=4; select the next member, not descriptor/code lines.
  m=re.search(r'\n  \S',txt[pos+3:]);return txt[pos:pos+3+m.start()] if m else txt[pos:]
 b=body(legacy,'public boolean a(double);')
 assert re.search(r'getfield\s+#[0-9]+\s+// Field j:D',b)
 assert b.count('dmul')==3 and 'isNaN' not in b
 result['legacy_range_bytecode']=b
 b=body(native,'public boolean isInRangeToRenderDist(double);')
 assert 'Double.isNaN' in b and re.search(r'getstatic\s+#[0-9]+\s+// Field renderDistanceWeight:D',b)
 result['native_range_bytecode']=b
 for h,op in [('public static double getRenderDistanceWeight();','getstatic'),('public static void setRenderDistanceWeight(double);','putstatic')]:
  b=body(native,h);assert op in b and 'Field renderDistanceWeight:D' in b;result[h]=b
 for key,txt in texts.items():
  if key.startswith('legacy:') and key not in ('legacy:sa','legacy:azt'):assert 'public boolean a(double);' not in txt,key
  if key.startswith('native:') and key not in ('native:net.minecraft.entity.Entity','native:net.minecraft.util.math.AxisAlignedBB'):assert 'isInRangeToRenderDist(double)' not in txt,key
 result['original_constructor_initializes_weight_one']=bool(re.search(r'dconst_1\s*\d+: putfield\s+#[0-9]+\s+// Field j:D',legacy))
 assert result['original_constructor_initializes_weight_one']
 return result
if __name__=='__main__':
 result=inspect();expected=json.loads((H/'engine-manifest.json').read_text());assert result==expected,'binary/mapping/bytecode drift'
 with tempfile.TemporaryDirectory(prefix='range-dispatch-') as td:
  d=Path(td)
  subprocess.run([str(J/'bin/javac'),'-cp',CP,'-d',str(d),str(H/'EngineDispatchScan.java')],check=True)
  for kind,jar in [('legacy',OLD),('native',NATIVE)]:
   actual=subprocess.check_output([str(J/'bin/java'),'-cp',str(d)+os.pathsep+CP,'EngineDispatchScan',kind,str(jar)],text=True)
   assert actual==(H/(kind+'-dispatch.tsv')).read_text(),kind+' full-engine field/read/caller drift'
 print('PASS full original 1,803-class and native 4,876-class field/caller census; only original Entity and unrelated ItemFrame read instance weight. Native Render/RenderGlobal -> virtual 3D range -> virtual range override.')
 print('PASS 12 pinned engine classes, 8 exact SRG lines, original per-instance default1, native static load/store, exact range arithmetic and four intermediate superclass non-overrides in both versions.')

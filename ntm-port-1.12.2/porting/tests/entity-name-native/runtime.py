#!/usr/bin/env python3
from pathlib import Path
import subprocess,os,tempfile,re
H=Path(__file__).resolve().parent;ROOT=H.parents[3];J=Path(os.environ.get('JAVA8_HOME','/workspace/shared/nagaport-toolchain/java8'));CP=Path(os.environ.get('NATIVE_CP_FILE','/tmp/nagaport-ae2-native-classpath.txt')).read_text().strip();OLD=os.environ.get('LEGACY_CLIENT_JAR','/tmp/nagaport-legacy-client.jar')
def run(args):
 p=subprocess.run(list(map(str,args)),stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
 assert p.returncode==0,p.stdout
 return p.stdout
s=(ROOT/'ntm-port-1.12.2/src/main/java/com/hbm/entity/mob/EntityDummy.java').read_text();dummy=re.search(r'@Override public String getName\(\) \{.*?\}',s,re.S).group()
s=(ROOT/'ntm-port-1.12.2/src/main/java/com/hbm/entity/projectile/EntityThrowableNT.java').read_text();body=re.search(r'if\(\(this.throwerName == null.*?nbt.setString\("ownerName".*?;',s,re.S).group()
s=(ROOT/'ntm-port-1.12.2/src/main/java/com/hbm/util/CompatExternal.java').read_text();target=re.search(r'entity.getName\(\).equals\("Target Practice"\)',s).group()
assert 'this.thrower instanceof EntityPlayer' in body
p=(H/'NativeNameTest.java.in').read_text().replace('@DUMMY_METHOD@',dummy).replace('@OWNER_BODY@',body).replace('@TARGET_EXPRESSION@',target)
with tempfile.TemporaryDirectory(prefix='native-entity-name-') as t:
 d=Path(t);f=d/'NativeNameTest.java';f.write_text(p)
 run([J/'bin/javac','-source','8','-target','8','-cp',CP,'-d',d,f,H/'LegacyPlayerNameTest.java'])
 logs=[run([J/'bin/java','-cp',str(d)+':'+OLD+':'+CP,'nameproof.LegacyPlayerNameTest']),run([J/'bin/java','-cp',str(d)+':'+CP,'nameproof.NativeNameTest'])]
 bytecode=run([J/'bin/javap','-p','-c','-classpath',str(d)+':'+CP,'nameproof.NativeNameTest$OwnerProjection','nameproof.NativeNameTest','nameproof.NativeNameTest$DummyProjection'])
 assert 'EntityLivingBase.getName:()Ljava/lang/String;' in bytecode and 'Entity.getName:()Ljava/lang/String;' in bytecode
 (H/'runtime.log').write_text(''.join(logs));print(''.join(logs))

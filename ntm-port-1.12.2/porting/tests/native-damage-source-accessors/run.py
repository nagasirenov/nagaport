#!/usr/bin/env python3
"""Reproducible bounded proof. No downloads, platform shims, or full-game claim."""
import argparse,hashlib,json,os,subprocess,tempfile
from pathlib import Path
from audit_bytecode import audit
from check_source import check
from check_generated_dummy import DUMMIES,verify_generated_dummy

p=argparse.ArgumentParser();p.add_argument('--module',type=Path);p.add_argument('--original',type=Path,default=Path('/workspace/shared/nagaport/ntm-original-pristine'));p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/tmp/nagaport-ae2-native-classpath.txt'));p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));p.add_argument('--legacy-forge',type=Path,default=Path('/tmp/nagaport-forge1614-userdev.jar'));p.add_argument('--legacy-srg',type=Path,default=Path('/tmp/nagaport1710-srg.zip'));p.add_argument('--manifest',type=Path);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args()
here=Path(__file__).resolve().parent;root=(a.module or here.parents[2]).resolve();manifest=json.loads((a.manifest or here/'source-manifest.json').read_text());cp=a.classpath_file.read_text().strip();java=a.java_home/'bin/java';javac=a.java_home/'bin/javac';mapping=Path('/workspace/shared/nagaport-toolchain/gradle-user-home/caches/minecraft/de/oceanlabs/mcp/mcp_stable/39');
def run(args,**kwargs):return subprocess.run(list(map(str,args)),check=True,text=True,**kwargs)
expected=json.loads((here/'dependency-sha256.json').read_text())
for path,digest in expected.items():
 if path in DUMMIES:verify_generated_dummy(path)
 else:assert hashlib.sha256(Path(path).read_bytes()).hexdigest()==digest,('dependency changed',path)
print('PASS two generated dummy JARs exactly match pinned ForgeGradle generator entry/payload; all real dependency hashes remain strict',flush=True)
print('PASS pinned dependency and original mapping hashes',flush=True)
check(root,manifest,a.original)
audit(a.java_home,cp,a.legacy_client,a.legacy_forge,a.legacy_srg,mapping/'methods.csv',mapping/'1.12.2/srgs/srg-mcp.srg',here/'bytecode-evidence.txt')
with tempfile.TemporaryDirectory(prefix='damage-native-') as tmp:
 t=Path(tmp);files=[here/'NativeDamageSourceTest.java',here/'LegacyDamageSourceTest.java',here/'DamageSourceScanner.java',root/'src/main/java/com/hbm/items/weapon/sedna/DamageSourceSednaNoAttacker.java',root/'src/main/java/com/hbm/items/weapon/sedna/DamageSourceSednaWithAttacker.java']
 run([javac,'-source','8','-target','8','-encoding','UTF-8','-cp',cp+os.pathsep+str(a.java_home/'lib/tools.jar'),'-d',t,*files])
 run([java,'-Djava.awt.headless=true','-cp',str(t)+os.pathsep+str(a.legacy_client)+os.pathsep+cp,'damageproof.LegacyDamageSourceTest'],cwd=t)
 run([java,'-Djava.awt.headless=true','-cp',str(t)+os.pathsep+cp,'NativeDamageSourceTest',a.classpath_file],cwd=t)
 scanner=[java,'-Xmx3g','-cp',str(t)+os.pathsep+str(a.java_home/'lib/tools.jar'),'DamageSourceScanner',t/'sources',a.classpath_file]
 cases=[('legacy true','Object f(net.minecraft.util.DamageSource s){return s.getEntity();}',1),('legacy immediate','Object f(net.minecraft.util.EntityDamageSourceIndirect s){return s.getSourceOfDamage();}',1),('native','Object f(net.minecraft.util.DamageSource s){return s.getTrueSource();}',1),('unknown','Object f(Unknown s){return s.getEntity();}',0),('unrelated','static class E {Object getEntity(){return null;}} Object f(E s){return s.getEntity();}',0),('custom override','static class D extends net.minecraft.util.DamageSource {D(){super("x");}public net.minecraft.entity.Entity getEntity(){return null;}}',1),('comments and UTF16','/* \U0001f680 getEntity() */ Object f(net.minecraft.util.DamageSource s){return s.getEntity();}',1),('receiver evaluation','net.minecraft.util.DamageSource s(){return null;} Object f(){return s().getSourceOfDamage();}',1)]
 for name,body,count in cases:
  fixture=t/'Fixture.java';text='class Fixture {'+body+'}';fixture.write_text(text);(t/'sources').write_text(str(fixture)+'\n');result=run(scanner,capture_output=True);lines=result.stdout.splitlines();assert len(lines)==count,(name,lines)
  for line in lines:
   fields=line.split('\t');assert text.encode('utf-16-le')[int(fields[2])*2:int(fields[3])*2].decode('utf-16-le')==fields[4],(name,line)
 print('PASS 8 real javac attribution fixtures including unknown/unrelated exclusions, custom overrides and UTF-16 spans')
 if a.verify_attribution:
  (t/'sources').write_text('\n'.join(map(str,sorted((root/'src/main/java').rglob('*.java'))))+'\n');result=run(scanner,capture_output=True);(here/'attribution-current.tsv').write_text(result.stdout);(here/'attribution-current.diagnostics.txt').write_text(result.stderr);print(result.stderr.strip());check(root,manifest,a.original,here/'attribution-current.tsv')
print('PASS bounded DamageSource accessor migration; full module still unfinished, no gameplay or complete caller lifecycle certified')

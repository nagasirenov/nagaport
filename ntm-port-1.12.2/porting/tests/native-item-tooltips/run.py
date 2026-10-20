#!/usr/bin/env python3
"""Compile unchanged complete callback bodies against real Minecraft; no platform substitutes."""
import argparse,hashlib,json,pathlib,subprocess,zipfile
p=argparse.ArgumentParser();p.add_argument('--project',type=pathlib.Path,required=True);p.add_argument('--out',type=pathlib.Path,required=True);p.add_argument('--java-home',default='/workspace/shared/nagaport-toolchain/java8');p.add_argument('--classpath-file',default='/tmp/nagaport-ae2-native-classpath.txt');p.add_argument('--legacy',default='/tmp/nagaport-legacy-client.jar');p.add_argument('--unsigned-legacy',default='/tmp/attribute-legacy-unsigned.jar');a=p.parse_args();e=pathlib.Path(__file__).resolve().parent;m=json.loads((e/'manifest.json').read_text());a.out.mkdir(parents=True,exist_ok=True);cp=pathlib.Path(a.classpath_file).read_text().strip();j=pathlib.Path(a.java_home)/'bin';h=lambda b:hashlib.sha256(b).hexdigest()
with zipfile.ZipFile(a.legacy) as original,zipfile.ZipFile(a.unsigned_legacy) as unsigned:
 names=[n for n in original.namelist() if not n.startswith('META-INF/')];assert set(names)=={n for n in unsigned.namelist() if not n.startswith('META-INF/')};assert all(original.read(n)==unsigned.read(n) for n in names)
selected={'ItemGlyphBlock','ItemMissileShuttle','ItemTemFlakes','ItemStarmetal','ItemReactorSensor','ItemTeleLink','ItemDetonator','ItemWiring'}
source='import java.util.*;import net.minecraft.item.*;import net.minecraft.util.text.TextFormatting;\npublic class RepresentativeItemTooltips {\n';count=0
for path,v in m['files'].items():
 name=v['owner'].split('.')[-1]
 if name not in selected:continue
 actual=(a.project/path).read_bytes();assert h(actual)==v['after_sha256'];assert v['body'] in actual.decode();params=v['original_parameters'];source+='public static class '+name+' extends Item { @Override public void '+v['new']+' '+v['body']+'\n}\n';count+=1
assert count==8;source+='}\n';f=a.out/'RepresentativeItemTooltips.java';f.write_bytes(source.encode());probe=e/'ItemTooltipProbe.java';inputs={str(q):h(q.read_bytes()) for q in (f,probe,pathlib.Path(a.legacy),pathlib.Path(a.unsigned_legacy))};(a.out/'input-hashes.json').write_text(json.dumps({'inputs':inputs,'original_entries_verified':len(names)},indent=2)+'\n')
def run(out,src,success):
 out.mkdir(exist_ok=True);subprocess.run([str(j/'javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'empty'),'-cp',cp,'-d',str(out),str(probe),str(src)],check=True)
 r=subprocess.run([str(j/'java'),'-Xmx400m','-cp',str(out)+':'+a.unsigned_legacy+':'+cp,'ItemTooltipProbe'],cwd=out,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True);(out/'runtime.log').write_text(r.stdout);assert (r.returncode==0)==success,r.stdout
 if not success:assert 'ItemMissileShuttle meta=' in r.stdout,r.stdout
 return r.stdout
print(run(a.out,f,True));mut=a.out/'text-order-mutant';mut.mkdir(exist_ok=True);mf=mut/f.name;bad=source.replace('list.add("Tonite, on bo\'\'om gear:");','list.add("WRONG ORDER/TEXT");');assert bad!=source;mf.write_bytes(bad.encode());run(mut,mf,False);print('PASS real-native compiled text mutant rejected')

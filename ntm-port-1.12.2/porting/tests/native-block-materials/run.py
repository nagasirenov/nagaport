#!/usr/bin/env python3
import argparse,hashlib,importlib.util,json,os,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args();H=pathlib.Path(__file__).resolve().parent;M=H.parents[2]
spec=importlib.util.spec_from_file_location('guard',H/'check_source.py');g=importlib.util.module_from_spec(spec);spec.loader.exec_module(g)
def call(cmd,**kw):return subprocess.check_output(list(map(str,cmd)),text=True,**kw)
expected=json.loads((H/'dependency-hashes.json').read_text());actual={x:hashlib.sha256(pathlib.Path(x).read_bytes()).hexdigest() for x in a.classpath.split(os.pathsep) if pathlib.Path(x).is_file()};assert actual==expected,'Dependency artifact drift requires re-audit'
assert hashlib.sha256(pathlib.Path(a.legacy_client).read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
with tempfile.TemporaryDirectory(prefix='material-native-') as td:
 d=pathlib.Path(td);old=d/'old';old.mkdir();cp=a.classpath+os.pathsep+str(a.java_home/'lib/tools.jar');java=a.java_home/'bin/java';javac=a.java_home/'bin/javac'
 print(call([javac,'-cp',cp,'-d',d]+list(H.glob('*NativeTest.java'))+[H/'ExtractedNukeMaterial.java',H/'MaterialAccessScanner.java',H/'MaterialOverrideScan.java',H/'DefaultStateAudit.java']))
 print(call([javac,'-cp',a.legacy_client+os.pathsep+cp,'-d',old,H/'ExtractedOldNukeMaterial.java']))
 print(call([java,'-cp',str(d)+os.pathsep+cp,'MaterialNativeTest',a.legacy_client,H/'mapping.tsv']))
 print(call([java,'-cp',str(d)+os.pathsep+cp,'BlockMaterialNativeTest',a.legacy_client,H/'mapping.tsv',old],cwd=d))
 cpfile=d/'cp.txt';cpfile.write_text(a.classpath)
 actual=call([java,'-cp',str(d)+os.pathsep+cp,'MaterialOverrideScan',cpfile]);assert actual==(H/'dependency-material-methods.tsv').read_text(),actual
 print('PASS all actual classpath JAR material override inventories match')
 result=subprocess.run([str(java),'-cp',str(d)+os.pathsep+cp,'DefaultStateAudit',str(cpfile)],text=True,stdout=subprocess.PIPE,stderr=subprocess.PIPE,check=True)
 assert result.stdout==(H/'dependency-default-state-origins.tsv').read_text()
 assert result.stderr==(H/'dependency-container-origins.tsv').read_text()
 assert all(row.split('\t')[2]=='THIS' and not any(x in row for x in ['NULL','OP','PARAM','LOOP']) for row in result.stdout.splitlines())
 print('PASS 110 actual dependency default-state setter origins and 128 container constructor origins')
 print(call(['python3',H/'check_state_owners.py']))

 cases=[('import net.minecraft.block.material.Material; class T {Object x=Material.water;}',1),('import net.minecraft.block.material.Material; class T {Object Material; Object x=Material.water;}',0),('import net.minecraft.block.material.Material; class T {void f(Object Material){Object x=Material.water;}}',0),('import net.minecraft.block.material.Material; class T {class Material {} Object x=Material.water;}',0),('class T {Object x=net.minecraft.block.material.Material.water;}',1),('class T {Object net; Object x=net.minecraft.block.material.Material.water;}',0),('import net.minecraft.block.material.*; class T {Object x=Material.water;}',0),('import net.minecraft.block.material.Material; class T {String x="Material.water"; /*Material.water*/}',0),('import net.minecraft.block.material.Material; class T {Object x=Material.wood;}',0),('import net.minecraft.block.Block; class T {Object f(Block b){return b.getMaterial();}}',1),('import net.minecraft.block.state.IBlockState; class T {Object f(IBlockState b){return b.getMaterial();}}',0),('class T {Object f(Object b){return b.getMaterial();}}',0),('import net.minecraft.block.Block; class T {Block f(){return null;} Object x=f().getMaterial();}',1),('import net.minecraft.block.Block; class T {Object f(Block b){return b.getMaterial(null);}}',0),('import net.minecraft.block.material.Material; class T {String s="😀"; Object x=Material.water;}',1),('import net.minecraft.block.material.Material; class T extends P {Object x=Material.water;} class P{Object Material;}',0)]
 for i,(source,n) in enumerate(cases):
  f=d/'Fixture.java';f.write_text(source);inp=d/'input.txt';inp.write_text(str(f)+'\n');out=call([java,'-cp',str(d)+os.pathsep+cp,'MaterialAccessScanner',H/'mapping.tsv',inp,cpfile],stderr=subprocess.PIPE);assert len(out.splitlines())==n,(i,out)
 f.write_text('import net.minecraft.block.*;import net.minecraft.block.material.*;import net.minecraft.block.state.*;class T extends Block{T(){super(Material.AIR);}public Material getMaterial(IBlockState s){return Material.WATER;}Object f(Block b){return b.getMaterial();}}');assert not call([java,'-cp',str(d)+os.pathsep+cp,'MaterialAccessScanner',H/'mapping.tsv',inp,cpfile],stderr=subprocess.PIPE)
 print('PASS 17 adversarial javac fixtures including source stateful material override rejection')
 print(call(['python3',H/'check_source.py']))
 # Enforce extracted method remains the complete actual production method, not a rewritten predicate.
 s=(M/'src/main/java/com/hbm/explosion/ExplosionNukeRayParallelized.java').read_text();start=s.index('private static float getNukeResistance(Block b)');end=s.index('\n\t}',start)+3;body=s[start:end].replace('private static','public static');assert body in (H/'ExtractedNukeMaterial.java').read_text()
 if a.verify_attribution:
  root=d/'original';paths=[]
  for src in sorted((M/'src/main/java').rglob('*.java')):
   if src.name in ['Silo.java','DungeonToolbox.java']:continue
   rel=src.relative_to(M).as_posix();raw=src.read_bytes()
   if rel in g.data['files']:raw=g.reverse(raw,g.data['files'][rel])
   dest=root/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(raw);paths.append(dest)
  inp.write_text('\n'.join(map(str,paths))+'\n');out=call([java,'-Xmx3g','-cp',str(d)+os.pathsep+cp,'MaterialAccessScanner',H/'mapping.tsv',inp,cpfile]);normalized=[]
  for row in out.splitlines():
   r=row.split('\t');r[0]=pathlib.Path(r[0]).relative_to(root).as_posix();normalized.append('\t'.join(r))
  assert sorted(normalized)==sorted((H/'attribution.tsv').read_text().splitlines())
  print('PASS complete original-source re-attribution of all 590 selected accesses')

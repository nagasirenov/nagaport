#!/usr/bin/env python3
import argparse,pathlib,tempfile,subprocess,os,json,hashlib
import check_source as guard
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args();h=pathlib.Path(__file__).resolve().parent;root=h.parents[2]
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()==json.loads((h/'mapping-provenance.json').read_text())['legacy_sha256']
with tempfile.TemporaryDirectory(prefix='native-math-proof-') as td:
 d=pathlib.Path(td);cp=a.classpath+os.pathsep+str(a.java_home/'lib/tools.jar');java=str(a.java_home/'bin/java');javac=str(a.java_home/'bin/javac')
 subprocess.run([javac,'-cp',cp,'-d',str(d),str(h/'MathCallScanner.java'),str(h/'MathOracle.java')],check=True)
 subprocess.run([java,'-cp',str(d)+os.pathsep+cp,'MathOracle',str(h/'map.tsv'),str(a.legacy_client)],check=True,cwd=d)
 cases=[('import net.minecraft.util.math.MathHelper; class T {int x=MathHelper.floor_double(1.0);}',1),('import net.minecraft.util.math.MathHelper; class T {int x=MathHelper.floor_double(16777217);}',1),('import net.minecraft.util.math.MathHelper; class T {Object MathHelper; int x=MathHelper.floor_double(1.0);}',0),('import net.minecraft.util.math.MathHelper; class T {int f(Object MathHelper){return MathHelper.floor_double(1.0);}}',0),('class T {class MathHelper{} int x=MathHelper.floor_double(1.0);}',0),('class T {int x=net.minecraft.util.math.MathHelper.floor_double(1.0);}',1),('class T {Object net;int x=net.minecraft.util.math.MathHelper.floor_double(1.0);}',0),('import static net.minecraft.util.math.MathHelper.floor_double; class T {int x=floor_double(1.0);}',0),('import net.minecraft.util.math.MathHelper; class T {String s="MathHelper.floor_double(1.0)";/* MathHelper.floor_double(1.0) */}',0),('import net.minecraft.util.math.MathHelper; class T {int x=MathHelper.floor_double(unknown());}',0),('import net.minecraft.util.math.MathHelper; class T {String s="😀";int x=MathHelper.floor_double(1.0);}',1),('import net.minecraft.util.math.MathHelper; class T extends Parent {int x=MathHelper.floor_double(1.0);}',0),('import net.minecraft.util.math.MathHelper; class T {int x=MathHelper.floor(1.0);}',0),('import net.minecraft.util.math.MathHelper; class T {int x=MathHelper.fl\\u006for_double(1.0);}',0)]
 paths=[]
 for i,(s,n) in enumerate(cases):
  f=d/('T'+str(i)+'.java');f.write_text(s.replace('class T ','class T'+str(i)+' '));paths.append(f)
 parent=d/'Parent.java';parent.write_text('class Parent {Object MathHelper;}');inp=d/'inputs';inp.write_text('\n'.join(map(str,paths+[parent])));cpfile=d/'cp';cpfile.write_text(a.classpath)
 out=subprocess.check_output([java,'-cp',str(d)+os.pathsep+cp,'MathCallScanner',str(h/'map.tsv'),str(inp),str(cpfile)],text=True,stderr=subprocess.PIPE)
 for i,(_,n) in enumerate(cases):assert len([r for r in out.splitlines() if r.split('\t')[0]==str(paths[i])])==n,(i,out)
 print('PASS',len(cases),'actual javac owner/primitive/shadow/comment/Unicode controls')
 subprocess.run(['python3',str(h/'check_source.py')],check=True)
 # Compile exact complete production methods copied from BobMathUtil, without collaborators or stubs.
 source=(root/'src/main/java/com/hbm/util/BobMathUtil.java').read_text();methods=[]
 for name in ['safeClamp','remap01_clamp']:
  start=source.index('\tpublic static ',source.index('public class'));start=source.rfind('\tpublic static ',0,source.index(' '+name+'('));brace=source.index('{',start);end=brace+1;depth=1
  while depth:
   if source[end]=='{':depth+=1
   if source[end]=='}':depth-=1
   end+=1
  methods.append(source[start:end])
 prod=d/'ProductionMathMethods.java';prod.write_text('import net.minecraft.util.math.MathHelper; public class ProductionMathMethods {\n'+'\n'.join(methods)+'\n}')
 subprocess.run([javac,'-cp',a.classpath,'-d',str(d),str(prod)],check=True)
 bytecode=subprocess.check_output([str(a.java_home/'bin/javap'),'-c','-classpath',str(d),'ProductionMathMethods'],text=True);assert 'clamp:(DDD)D' in bytecode and 'clamp:(FFF)F' in bytecode
 print('PASS exact complete production safeClamp/remap01_clamp methods compile; bytecode selects original DDD/FFF descriptors')
 if a.verify_attribution:
  sources=d/'original';paths=[]
  for src in sorted((root/'src/main/java').rglob('*.java')):
   rel=src.relative_to(root).as_posix();raw=src.read_bytes()
   if rel in guard.data['files']:raw=guard.reverse(raw,guard.data['files'][rel])
   dest=sources/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(raw);paths.append(dest)
  inp.write_text('\n'.join(map(str,paths)));out=subprocess.check_output([java,'-Xmx3g','-cp',str(d)+os.pathsep+cp,'MathCallScanner',str(h/'map.tsv'),str(inp),str(cpfile)],text=True)
  normalized=[]
  for row in out.splitlines():
   r=row.split('\t');r[0]=pathlib.Path(r[0]).relative_to(sources).as_posix();normalized.append('\t'.join(r))
  assert sorted(normalized)==sorted((h/'attributed-calls.tsv').read_text().splitlines()),'attribution drift'
  print('PASS all584 original class receivers and primitive argument types re-attributed')

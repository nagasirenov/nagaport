#!/usr/bin/env python3
import argparse,hashlib,json,os,pathlib,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args();h=pathlib.Path(__file__).resolve().parent
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()==json.loads((h/'constant-map.json').read_text())['original_client_sha256']
with tempfile.TemporaryDirectory(prefix='native-constant-proof-') as td:
 d=pathlib.Path(td);cp=a.classpath+os.pathsep+str(a.java_home/'lib/tools.jar')
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',cp,'-d',str(d),str(h/'ConstantAccessScanner.java'),str(h/'VanillaConstantsNativeTest.java')],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',str(d)+os.pathsep+cp,'VanillaConstantsNativeTest',str(h/'constant-map.tsv'),str(a.legacy_client)],check=True,cwd=d)
 cases=[('import net.minecraft.init.Blocks; class T {Object x=Blocks.air; String s="Blocks.air"; /* Blocks.air */}',1),('import net.minecraft.init.Items; class T {Object x=Items.apple;}',1),('import net.minecraft.init.Blocks; class T {Object Blocks; Object x=Blocks.air;}',0),('import net.minecraft.init.Blocks; class T {void f(Object Blocks){Object x=Blocks.air;}}',0),('import net.minecraft.init.Blocks; class T {class Blocks{} Object x=Blocks.air;}',0),('import other.Blocks; class T {Object x=Blocks.air;}',0),('class T {Object x=net.minecraft.init.Blocks.air;}',1),('class T {Object net; Object x=net.minecraft.init.Blocks.air;}',0),('import static net.minecraft.init.Blocks.air; class T {Object x=air;}',0),('import net.minecraft.init.Blocks; class T {Object x=Blocks.AIR;}',0),('import net.minecraft.init.Blocks; class T {Object x=Other.Blocks.air;}',0),('import net.minecraft.init.Blocks; class T {Object x=Blocks.\\u0061ir;}',0),('import net.minecraft.init.*; class T {Object x=Blocks.air;}',0),('import net.minecraft.init.Blocks; class T {String s="😀";Object x=Blocks.air;}',1),('import net.minecraft.init.Blocks; class T extends Parent {Object x=Blocks.air;}',0)]
 paths=[]
 for i,(source,n) in enumerate(cases):
  f=d/('T'+str(i)+'.java');f.write_text(source.replace('class T ','class T'+str(i)+' '));paths.append(f)
 parent=d/'Parent.java';parent.write_text('class Parent {Object Blocks;}');inp=d/'inputs.txt';inp.write_text('\n'.join(map(str,paths+[parent]))+'\n');classpath=d/'classpath.txt';classpath.write_text(a.classpath)
 out=subprocess.check_output([str(a.java_home/'bin/java'),'-cp',str(d)+os.pathsep+cp,'ConstantAccessScanner',str(h/'constant-map.tsv'),str(inp),str(classpath)],text=True,stderr=subprocess.PIPE)
 rows=[s.split('\t') for s in out.splitlines()]
 for i,(source,n) in enumerate(cases):
  selected=[r for r in rows if r[0]==str(paths[i])];assert len(selected)==n,(i,selected)
  for r in selected:
   raw=paths[i].read_text().encode('utf-16-le');start,end=map(int,r[1:3]);assert raw[start*2:end*2].decode('utf-16-le')==r[4]
 print('PASS',len(cases),'actual attributed ownership/comment/string/shadow/inherited-field/import/Unicode fixtures.')
 subprocess.run(['python3',str(h/'check_source.py')],check=True)

 if a.verify_attribution:
  import importlib.util
  spec=importlib.util.spec_from_file_location('source_guard',h/'check_source.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard)
  module=h.parents[2];selected=[];root=d/'attribution-sources'
  for source in (module/'src/main/java').rglob('*.java'):
   rel=source.relative_to(module).as_posix()
   if '/items/' in rel or '/world/dungeon/' in rel or source.name in ['ModBlocks.java','BlockBase.java','ClientProxy.java','LegacyIconRegister.java','LegacyIconRegistrationHandler.java']:continue
   raw=source.read_bytes()
   if rel in guard.data['files']:raw=guard.reverse(raw,guard.data['files'][rel])
   dest=root/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(raw);selected.append(dest)
  inputs=d/'all-source-inputs.txt';inputs.write_text('\n'.join(map(str,sorted(selected)))+'\n')
  actual=subprocess.check_output([str(a.java_home/'bin/java'),'-Xmx3g','-cp',str(d)+os.pathsep+cp,'ConstantAccessScanner',str(h/'constant-map.tsv'),str(inputs),str(classpath)],text=True)
  normalized=[]
  for row in actual.splitlines():
   fields=row.split('\t');fields[0]=pathlib.Path(fields[0]).relative_to(root).as_posix();normalized.append('\t'.join(fields))
  assert sorted(normalized)==sorted((h/'attributed-accesses.tsv').read_text().splitlines()),'Attributed selection drift'
  print('PASS all3073 original-expression owners re-attributed to actual native class symbols; unfinished-source diagnostics are not compile success.')

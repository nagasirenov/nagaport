#!/usr/bin/env python3
import argparse,hashlib,json,os,pathlib,re,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--java-home',type=pathlib.Path,required=True);p.add_argument('--classpath-file',type=pathlib.Path,required=True);p.add_argument('--legacy-client',type=pathlib.Path,required=True);p.add_argument('--verify-attribution',action='store_true');a=p.parse_args()
h=pathlib.Path(__file__).resolve().parent;root=h.parents[2];cp=a.classpath_file.read_text().strip();tools=str(a.java_home/'lib/tools.jar');java=str(a.java_home/'bin/java');javac=str(a.java_home/'bin/javac')
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
subprocess.run(['python3',str(h/'check_source.py')],check=True)
with tempfile.TemporaryDirectory(prefix='native-count-proof-') as td:
 d=pathlib.Path(td)
 # Extract complete production method unchanged, with explicitly bounded mod-owned collaborator fixture.
 source=(root/'src/main/java/com/hbm/tileentity/machine/storage/TileEntityMassStorage.java').read_text();start=source.index('\tpublic ItemStack quickExtract()');end=source.index('\n\t// Note:',start);method=source[start:end]
 assert method.count('result.stackSize = amount')==1
 # Deferred candidate only: explicitly apply the proposed setter in this test fixture, NOT production.
 method=method.replace('result.stackSize = amount','result.setCount(amount)')
 fixture=d/'ExtractedMassStorage.java';fixture.write_text('import net.minecraft.item.ItemStack; public class ExtractedMassStorage { boolean output; int stack, dirty; ItemStack[] slots=new ItemStack[3]; ItemStack getType(){return slots[1];} int getStockpile(){return stack;} void markDirty(){dirty++;}\n'+method+'\n}')
 subprocess.run([javac,'-cp',cp+os.pathsep+tools,'-d',str(d),str(h/'CountAssignmentScanner.java'),str(h/'NativeCountTest.java'),str(fixture)],check=True)
 subprocess.run([java,'-cp',str(d)+os.pathsep+cp,'NativeCountTest',str(a.legacy_client)],check=True,cwd=d)
 bytecode=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',cp,'net.minecraft.item.ItemStack'],text=True)
 setter=bytecode.split('public void setCount(int);')[1].split('public void grow')[0];assert 'putfield' in setter and 'Field stackSize:I' in setter and 'Method updateEmptyState:()V' in setter
 getter=bytecode.split('public int getCount();')[1].split('public void setCount')[0];assert 'Field isEmpty:Z' in getter and 'Field stackSize:I' in getter
 print('PASS actual native bytecode: setter writes raw count then updates empty state; getter normalizes EMPTY.')
 cases=[('void f(ItemStack s){s.stackSize=2;}',1),('void f(ItemStack s){s.stackSize=rhs();} int rhs(){return 2;}',1),('int f(ItemStack s){return s.stackSize=2;}',0),('void f(ItemStack s){s.stackSize+=2;}',0),('void f(ItemStack s){s.stackSize++;}',0),('int f(ItemStack s){return s.stackSize;}',0),('void f(ItemStack s){int x=(s.stackSize=2);}',0),('void f(ItemStack s){String t="s.stackSize = 1"; /* s.stackSize = 1 */}',0),('static class Fake {int stackSize;} void f(Fake s){s.stackSize=2;}',0),('void f(Unknown s){s.stackSize=2;}',0),('void f(ItemStack s){s.stackSize /* retain */ =2;}',0),('void f(ItemStack s){s.\\u0073tackSize=2;}',0),('void f(ItemStack[] a,int i){a[i++].stackSize=2;}',1),('void f(){ItemStack.stackSize=2;}',0),('void f(ItemStack s){String t="😀";s.stackSize=2;}',1)]
 paths=[]
 for i,(body,n) in enumerate(cases):
  f=d/('Case'+str(i)+'.java');f.write_text('import net.minecraft.item.ItemStack; class Case'+str(i)+' {'+body+'}');paths.append(f)
 inputs=d/'inputs.txt';inputs.write_text('\n'.join(map(str,paths))+'\n')
 def scan(inp):return subprocess.check_output([java,'-Xmx3g','-cp',str(d)+os.pathsep+tools,'CountAssignmentScanner',str(inp),str(a.classpath_file)],text=True)
 rows=[s.split('\t') for s in scan(inputs).splitlines()]
 for f,(_,n) in zip(paths,cases):assert sum(r[0]==str(f) for r in rows)==n,(f,n,rows)
 for row in rows:
  f,s,e,rs,re_,vs,ve,*_=row;raw=pathlib.Path(f).read_text().encode('utf-16-le');assert raw[int(s)*2:int(e)*2].decode('utf-16-le').endswith('2') or 'rhs()' in raw[int(s)*2:int(e)*2].decode('utf-16-le')
 print('PASS',len(cases),'actual native-owner attribution, used-value/compound/unary/shadow/comment/unicode/error/static-access negative controls.')
 if a.verify_attribution:
  import importlib.util
  spec=importlib.util.spec_from_file_location('guard',h/'check_source.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard)
  original=d/'original';allpaths=[]
  for f in (root/'src/main/java').rglob('*.java'):
   rel=f.relative_to(root).as_posix();raw=f.read_bytes()
   if rel in guard.data['files']:raw=guard.reverse(raw,guard.data['files'][rel])
   dest=original/rel;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(raw);allpaths.append(dest)
  inputs.write_text('\n'.join(map(str,sorted(allpaths)))+'\n');actual=scan(inputs);normalized=['\t'.join([str(pathlib.Path(r[0]).relative_to(original))]+r[1:]) for r in map(lambda s:s.split('\t'),actual.splitlines())];selected=(h/'attributed-accesses.tsv').read_text().splitlines();assert set(selected)<=set(normalized);assert set(normalized)==set((h/'all-attributed-candidates.tsv').read_text().splitlines())
  print('PASS all',len(selected),'selected original writes re-attributed among',len(normalized),'candidates using all production sources; unfinished compile errors expected.')

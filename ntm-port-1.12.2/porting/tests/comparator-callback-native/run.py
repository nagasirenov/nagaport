#!/usr/bin/env python3
"""Exact complete callback bodies in bounded owner/domain fixtures, real native dispatch."""
import argparse,hashlib,json,re,subprocess,tempfile,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];S=M/'src/main/java'
def sha(b):return hashlib.sha256(b).hexdigest()
def method(s,name):
 m=re.search(r'public (?:boolean|int) '+name+r'\([^)]*\)\s*\{',s);assert m,name
 i=m.end();depth=1
 while depth:
  depth+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[m.start():i]
def main():
 p=argparse.ArgumentParser();p.add_argument('--allow-unrelated',action='store_true');p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);a=p.parse_args();cp=a.classpath_file.read_text().strip();ops=json.loads((H/'selection.json').read_text())['operations'];paths=sorted(set(o['path'] for o in ops));receipts=[]
 with zipfile.ZipFile(H/'baseline.zip') as z, tempfile.TemporaryDirectory(prefix='comparator-native-') as td:
  t=Path(td);generated=[];inst=[]
  for index,path in enumerate(paths):
   before=z.read(path).decode();after=(S/path).read_text();inverse=after
   for o in reversed([o for o in ops if o['path']==path]):assert inverse.count(o['new'])==1;inverse=inverse.replace(o['new'],o['old'])
   assert inverse==before or a.allow_unrelated,('outside-scope edit',path)
   for name in ['hasComparatorInputOverride','getComparatorInputOverride']:assert method(inverse,name)==method(before,name),('callback body changed',path,name)
   oldvalue=method(before,'getComparatorInputOverride');param=re.search(r'int (\w+)\)\s*\{',oldvalue).group(1);assert not re.search(r'\b'+param+r'\b',oldvalue[oldvalue.index('{')+1:])
   for prefix,source in [('Native',after),('Legacy',before)]:
    methods='\n'.join(('@Override ' if prefix=='Native' else '')+method(source,n) for n in ['hasComparatorInputOverride','getComparatorInputOverride']);generated.append('static class '+prefix+str(index)+' extends Owner {'+methods+'}')
   inst.append('test(new Native'+str(index)+'(),new Legacy'+str(index)+'(),"'+Path(path).stem+'");')
   receipts.append(dict(path=path,baseline_sha256=sha(before.encode()),patched_sha256=sha(after.encode()),exact_inverse=(inverse==before),unused_old_side=param))
  java=(H/'Test.java.in').read_text().replace('OWNERS','\n'.join(generated)).replace('RUN_CASES','\n'.join(inst));(t/'Test.java').write_text(java);classes=t/'classes';classes.mkdir()
  cmd=[str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',cp,'-d',str(classes),str(t/'Test.java'),str(S/'com/hbm/world/WorldBlockState.java')];subprocess.run(cmd,check=True)
  run=[str(a.java_home/'bin/java'),'-Xmx400m','-cp',str(classes)+':'+cp,'Test'];r=subprocess.run(run,cwd=t,text=True,capture_output=True);(H/'native-test.log').write_text(r.stdout+r.stderr);print(r.stdout+r.stderr);assert r.returncode==0
  api=subprocess.check_output([str(a.java_home/'bin/javap'),'-c','-p','-classpath',cp,'net.minecraft.block.Block','net.minecraft.block.state.BlockStateContainer$StateImplementation','net.minecraft.block.BlockRedstoneComparator'],text=True);(H/'native-api.log').write_text(api)
  assert 'Block.hasComparatorInputOverride:(Lnet/minecraft/block/state/IBlockState;)Z' in api and 'Block.getComparatorInputOverride:(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I' in api
  original=java
  for o in ops: original=original.replace(o['new'],o['old'])
  (t/'Test.java').write_text(original);r=subprocess.run(cmd,text=True,capture_output=True);assert r.returncode and r.stderr.count('method does not override or implement a method from a supertype')==18,r.stderr
  (H/'compiler-attribution.log').write_text(r.stderr)
  print('PASS native compiler rejects all 18 original callback overrides')
  for old,new,label in [('return true;','return false;','presence disabled'),('int x = blockPos.getX();','int x = blockPos.getZ();','swapped x coordinate'),('return battery.getComparatorPower();','return 0;','battery value lost')]:
   assert old in java;(t/'Test.java').write_text(java.replace(old,new,1));subprocess.run(cmd,check=True,capture_output=True);r=subprocess.run(run,cwd=t,capture_output=True,text=True);assert r.returncode and 'AssertionError' in r.stderr,(label,r.stdout,r.stderr);print('PASS mutant rejected: '+label)
  (H/'source-proof.json').write_text(json.dumps({'owners':receipts,'native_api_sha256':sha(api.encode()),'classpath_file_sha256':sha(a.classpath_file.read_bytes())},indent=2)+'\n')
if __name__=='__main__':main()

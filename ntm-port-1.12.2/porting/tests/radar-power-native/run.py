#!/usr/bin/env python3
import argparse,json,re,subprocess,tempfile,zipfile,hashlib
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];S=M/'src/main/java'
def method(s,name):
 m=re.search(r'public (?:boolean|int) '+name+r'\([^)]*\)\s*\{',s);assert m,name
 i=m.end();depth=1
 while depth:depth+=(s[i]=='{')-(s[i]=='}');i+=1
 return s[m.start():i]
def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--allow-unrelated',action='store_true');a=p.parse_args();cp=a.classpath_file.read_text().strip();d=json.loads((H/'selection.json').read_text());receipts=[];shells=[]
 with zipfile.ZipFile(H/'baseline.zip') as z,tempfile.TemporaryDirectory(prefix='radar-power-') as td:
  t=Path(td)
  for name in ['MachineRadar','MachineRadarLarge']:
   path='com/hbm/blocks/machine/'+name+'.java';before=z.read(path).decode();after=(S/path).read_text();inverse=after
   for op in reversed([o for o in d['operations'] if o['path']==path]):assert inverse.count(op['new'])==1;inverse=inverse.replace(op['new'],op['old'])
   assert inverse==before or a.allow_unrelated
   for m in ['canProvidePower','isProvidingWeakPower','isProvidingStrongPower']:assert method(inverse,m)==method(before,m)
   receipts.append({'path':path,'exact_file_inverse':inverse==before,'before_sha256':hashlib.sha256(before.encode()).hexdigest(),'after_sha256':hashlib.sha256(after.encode()).hexdigest()})
   native='\n'.join('@Override '+method(after,m) for m in ['canProvidePower','getWeakPower','getStrongPower'])
   old='\n'.join(method(before,m) for m in ['canProvidePower','isProvidingWeakPower','isProvidingStrongPower']).replace('IBlockAccess world','LegacyAccess world')
   shells+=['static class Native'+name+' extends Owner {'+native+'}','static class Legacy'+name+' {'+old+'}']
  tile=(S/'com/hbm/tileentity/machine/TileEntityMachineRadarNT.java').read_text()
  java=(H/'Test.java.in').read_text().replace('OWNER_METHODS','\n'.join(shells)).replace('EXACT_TILE_POWER',method(tile,'getRedPower'));(t/'Test.java').write_text(java);classes=t/'classes';classes.mkdir();jc=[str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',cp,'-d',str(classes),str(t/'Test.java'),str(S/'com/hbm/lib/ForgeDirection.java'),str(S/'com/hbm/world/WorldBlockState.java')];subprocess.run(jc,check=True);cmd=[str(a.java_home/'bin/java'),'-Xmx400m','-cp',str(classes)+':'+cp,'Test'];r=subprocess.run(cmd,cwd=t,text=True,capture_output=True);print(r.stdout+r.stderr);(H/'native-test.log').write_text(r.stdout+r.stderr);assert r.returncode==0
  original=java
  for op in reversed(d['operations']):original=original.replace(op['new'],op['old'])
  (t/'Test.java').write_text(original);r=subprocess.run(jc,text=True,capture_output=True);assert r.returncode and r.stderr.count('error:')==9 and r.stderr.count('method does not override or implement a method from a supertype')==6,r.stderr
  (H/'compiler-attribution.log').write_text(r.stderr);print('PASS compiler rejects six old overrides and three old scalar IBlockAccess calls')
  for old,new,label in [('int m = side.getIndex();','int m = side.getOpposite().getIndex();','opposite facing'),('if(meta >= 6)','if(meta > 6)','metadata boundary'),('return getWeakPower(state, world, blockPos, side);','return 0;','strong forwarding'),('int x = blockPos.getX();','int x = blockPos.getZ();','coordinate swap')]:
   assert old in java;(t/'Test.java').write_text(java.replace(old,new,1));subprocess.run(jc,check=True,capture_output=True);r=subprocess.run(cmd,cwd=t,capture_output=True,text=True);assert r.returncode and 'AssertionError' in r.stderr,(label,r.stdout,r.stderr);print('PASS mutant rejected: '+label)
  (H/'source-proof.json').write_text(json.dumps(receipts,indent=2)+'\n')
if __name__=='__main__':main()

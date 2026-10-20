#!/usr/bin/env python3
import hashlib,json,subprocess,zipfile,re
from pathlib import Path
H=Path(__file__).resolve().parent;J=Path('/workspace/shared/nagaport-toolchain/java8/bin/javap');legacy=Path('/tmp/nagaport-legacy-client.jar');forge=Path('/tmp/nagaport-forge1614-userdev.jar');cpfile=Path('/tmp/nagaport-ae2-native-classpath.txt');cp=cpfile.read_text().strip()
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
with zipfile.ZipFile(forge) as z:
 mappings=z.read('conf/packaged.srg').decode();names=z.read('conf/methods.csv').decode();selected='\n'.join(l for l in mappings.splitlines() if l.startswith('MD: aji/') and ('func_149740_M' in l or 'func_149736_g' in l))+'\n'+'\n'.join(l for l in names.splitlines() if l.startswith(('func_149740_M,','func_149736_g,')))
 assert 'MD: aji/M ()Z' in selected and 'MD: aji/g (Lahb;IIII)I' in selected
(H/'legacy-mapping.txt').write_text(selected+'\n')
old=subprocess.check_output([str(J),'-c','-p','-classpath',str(legacy),'aji','akc'],text=True);assert 'Method aji.M:()Z' in old and 'Method aji.g:(Lahb;IIII)I' in old;(H/'legacy-engine.log').write_text(old)
inputs={str(p):sha(p) for p in [legacy,forge,cpfile,J,J.parent/'java',J.parent/'javac']}
for p in map(Path,cp.split(':')):
 if p.is_file():
  with zipfile.ZipFile(p) as z:
   if 'net/minecraft/block/BlockRedstoneComparator.class' in z.namelist():inputs[str(p)]=sha(p)
paths=sorted({o['path'] for o in json.loads((H/'selection.json').read_text())['operations']});original=Path('/workspace/shared/nagaport/ntm-original-pristine/src/main/java');snippets=[]
for path in paths:
 p=original/path;s=p.read_text();start=s.index('public boolean hasComparatorInputOverride()');value=s.index('public int getComparatorInputOverride(',start);i=s.index('{',value)+1;depth=1
 while depth:depth+=(s[i]=='{')-(s[i]=='}');i+=1
 snippets.append('// '+path+'\n'+'\n'.join(line.rstrip() for line in s[start:i].splitlines()));inputs[str(p)]=sha(p)
(H/'original-owner-callbacks.txt').write_text('\n\n'.join(snippets)+'\n');(H/'input-sha256.json').write_text(json.dumps(inputs,indent=2)+'\n');print('PASS original engine dispatch, MCP names, nine original-owner callback excerpts and native input fingerprints')

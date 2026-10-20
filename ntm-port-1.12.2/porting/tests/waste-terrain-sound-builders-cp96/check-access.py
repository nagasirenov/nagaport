#!/usr/bin/env python3
import argparse,hashlib,json,re,subprocess,tempfile,zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);a=p.parse_args();cp=a.classpath_file.read_text().strip()
block=subprocess.check_output([str(a.java_home/'bin/javap'),'-classpath',cp,'-p','-c','net.minecraft.block.Block'],text=True)
proof=[]
for name in ['setSoundType','setTranslationKey','setHardness','setResistance','setCreativeTab','setLightLevel']:
 visibility='protected' if name=='setSoundType' else 'public'
 m=re.search(r'  '+visibility+r' net.minecraft.block.Block '+name+r'\([^)]*\);\n    Code:\n((?: +\d+: .+\n)+)',block);assert m,name
 instructions=re.findall(r'\d+: ([^\n]+)',m.group(1));assert instructions[-2:]==['aload_0','areturn'],name
 assert instructions.count('areturn')==1 and not any('invoke' in x for x in instructions),name
 if name=='setSoundType':assert instructions==['aload_0','aload_1','putfield      #376                // Field blockSoundType:Lnet/minecraft/block/SoundType;','aload_0','areturn']
 proof.append(m.group(0))
(HERE/'native-setter-bytecode.txt').write_text('\n'.join(proof))
with tempfile.TemporaryDirectory(prefix='waste-terrain-access-') as d:
 t=Path(d)
 for name,expression,expected in [('ProtectedSetter','new Block(Material.ROCK).setSoundType(SoundType.WOOD)','setSoundType(SoundType) has protected access in Block'),('NoForeignBuilder','new Block(Material.ROCK).withSoundType(SoundType.WOOD)','cannot find symbol')]:
  path=t/(name+'.java');path.write_text('import net.minecraft.block.*; import net.minecraft.block.material.Material; class '+name+' { Object value = '+expression+'; }')
  r=subprocess.run([str(a.java_home/'bin/javac'),'-cp',cp,'-sourcepath',str(t/'empty'),'-d',d,str(path)],text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
  assert r.returncode!=0 and expected in r.stdout,r.stdout
  print('PASS native compiler negative control:',name,flush=True)
native=next(Path(x) for x in cp.split(':') if 'forgeBin-1.12.2-' in x and x.endswith('.jar'))
with zipfile.ZipFile(native) as z:native_class=z.read('net/minecraft/block/Block.class')
assert hashlib.sha256(native.read_bytes()).hexdigest()=='69e6736b75384d9218846614fd47fd61c66c0f7e8899341ef98d1886b835d775', 'Native jar differs from sealed CP94 proof'
assert hashlib.sha256(native_class).hexdigest()=='ea913e592dd98ad91b61e779e4ee07889ded2585a5fdea7eb72a1d81bf2d451d', 'Native Block differs from sealed CP94 proof'
(HERE/'access-evidence.json').write_text(json.dumps(dict(native_jar=str(native),native_jar_sha256=hashlib.sha256(native.read_bytes()).hexdigest(),native_block_class_sha256=hashlib.sha256(native_class).hexdigest(),native_visibility='protected',native_methods_verified=['setSoundType','setTranslationKey','setHardness','setResistance','setCreativeTab','setLightLevel'],changes='No access transformer, native/cache/dependency edits, native-class replacement or reflective production setter.'),indent=2)+'\n')
print('PASS genuine native protected sound setter and identical-receiver setter bytecode. Source guard independently proves exact native Block parent and absence of owner overrides.')

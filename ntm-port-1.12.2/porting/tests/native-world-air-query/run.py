from pathlib import Path
import subprocess,tempfile,zipfile,io,hashlib,json
H=Path(__file__).resolve().parent;J=Path('/workspace/shared/nagaport-toolchain/java8');CP=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt').read_text().strip();F=Path('/tmp/nagaport-forge1614-userdev.jar');O=Path('/tmp/nagaport-legacy-client.jar')
assert hashlib.sha256(F.read_bytes()).hexdigest()=='6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91'
assert hashlib.sha256(O.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
for name,e in json.loads((H/'native-class-hashes.json').read_text()).items():
 with zipfile.ZipFile(e['jar'])as z:assert hashlib.sha256(z.read(name)).hexdigest()==e['sha256'],name
with zipfile.ZipFile(F)as f,zipfile.ZipFile(io.BytesIO(f.read('forgepatches.zip')))as z:
 text=z.read('net/minecraft/world/World.java.patch').decode();needle='''+        Block block = this.func_147439_a(p_147437_1_, p_147437_2_, p_147437_3_);
+        return block.isAir(this, p_147437_1_, p_147437_2_, p_147437_3_);''';assert needle in text
 (H/'original-world-air-patch.txt').write_text(needle+'\n')
with tempfile.TemporaryDirectory(prefix='native-world-air-')as t:
 d=Path(t);subprocess.run([str(J/'bin/javac'),'-cp',CP,'-d',str(d),str(H/'NativeWorldAirTest.java')],check=True);subprocess.run([str(J/'bin/java'),'-cp',str(d)+':'+CP,'NativeWorldAirTest'],check=True,cwd=d)
code=subprocess.check_output([str(J/'bin/javap'),'-classpath',CP,'-c','net.minecraft.world.World'],text=True);start=code.index('  public boolean isAirBlock(');body=code[start:code.index('\n  public ',start+2)];assert body.count('// Method getBlockState:')==2 and 'Block.isAir:'in body;(H/'native-world-air.javap').write_text(body+'\n')
print('PASS official Forge1614 original one-read hook and native genuine two-state-read method recorded; platform difference explicit, not hidden parity')

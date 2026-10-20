#!/usr/bin/env python3
import argparse, hashlib, json, re, subprocess, tempfile, zipfile
from pathlib import Path
HERE = Path(__file__).resolve().parent
p = argparse.ArgumentParser()
p.add_argument('--java-home', type=Path, required=True)
p.add_argument('--classpath-file', type=Path, required=True)
p.add_argument('--forge-userdev-jar', type=Path, required=True)
a = p.parse_args()
cp = a.classpath_file.read_text().strip()
block = subprocess.check_output([str(a.java_home/'bin/javap'), '-classpath', cp, '-p', '-c', 'net.minecraft.block.Block'], text=True)
m = re.search(r'  protected net.minecraft.block.Block setSoundType\(net.minecraft.block.SoundType\);\n    Code:\n((?:       .+\n)+)', block)
assert m, 'Native sound setter must remain protected'
assert re.findall(r'\d+: ([^\n]+)', m.group(1))[:5] == ['aload_0', 'aload_1', 'putfield      #376                // Field blockSoundType:Lnet/minecraft/block/SoundType;', 'aload_0', 'areturn']
(HERE/'native-setter-bytecode.txt').write_text(m.group(0))
for parent in ['BlockFalling', 'BlockContainer']:
    members = subprocess.check_output([str(a.java_home/'bin/javap'), '-classpath', cp, '-p', 'net.minecraft.block.' + parent], text=True)
    assert ' extends net.minecraft.block.Block ' in members
    for name in ['setSoundType', 'setTranslationKey', 'setHardness', 'setResistance', 'setCreativeTab', 'withSoundType']:
        assert not re.search(r'\b' + name + r'\(', members), (parent, name)
with tempfile.TemporaryDirectory(prefix='crate-sound-access-') as d:
    t = Path(d)
    for name, expression, expected in [('ProtectedSetter', 'new Block(Material.ROCK).setSoundType(SoundType.WOOD)', 'setSoundType(SoundType) has protected access in Block'), ('NoForeignBuilder', 'new Block(Material.ROCK).withSoundType(SoundType.WOOD)', 'cannot find symbol')]:
        path = t/(name+'.java')
        path.write_text('import net.minecraft.block.*; import net.minecraft.block.material.Material; class ' + name + ' { Object value = ' + expression + '; }')
        result = subprocess.run([str(a.java_home/'bin/javac'), '-cp', cp, '-sourcepath', str(t/'empty'), '-d', d, str(path)], text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
        assert result.returncode != 0 and expected in result.stdout, result.stdout
        print('PASS compile-time domain negative control:', name, flush=True)
assert hashlib.sha256(a.forge_userdev_jar.read_bytes()).hexdigest() == '6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91'
with zipfile.ZipFile(a.forge_userdev_jar) as z:
    member = 'src/main/resources/forge_at.cfg'; data = z.read(member)
    entry = 'public net.minecraft.block.Block func_149672_a(Lnet.minecraft.block.Block$SoundType;)Lnet.minecraft.block.Block; #setStepSound'
    assert data.decode().splitlines()[34] == entry
native = next(Path(x) for x in cp.split(':') if 'forgeBin-1.12.2-' in x and x.endswith('.jar'))
with zipfile.ZipFile(native) as z:
    native_class = z.read('net/minecraft/block/Block.class')
(HERE/'access-evidence.json').write_text(json.dumps(dict(native_jar=str(native), native_jar_sha256=hashlib.sha256(native.read_bytes()).hexdigest(), native_block_class_sha256=hashlib.sha256(native_class).hexdigest(), native_visibility='protected', original_forge_jar_sha256=hashlib.sha256(a.forge_userdev_jar.read_bytes()).hexdigest(), original_access_resource=member, original_access_resource_sha256=hashlib.sha256(data).hexdigest(), original_access_line=35, original_access_entry=entry, changes='No dependency, cache, access transformer, native visibility or runtime access alteration.'), indent=2)+'\n')
print('PASS unchanged native protected setter and native superclass bindings; authentic original Forge1614 public setter access confirmed without transformation.')

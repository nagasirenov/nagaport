#!/usr/bin/env python3
"""Bounded actual-engine attribute oracle; no dependencies downloaded and no mod build."""
from pathlib import Path
import argparse, hashlib, io, json, os, subprocess, tempfile, zipfile
HERE = Path(__file__).resolve().parent
p = argparse.ArgumentParser()
p.add_argument('--java-home', type=Path, default=Path('/workspace/shared/nagaport-toolchain/java8'))
p.add_argument('--classpath-file', type=Path, default=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt'))
p.add_argument('--legacy-client', type=Path, default=Path('/tmp/nagaport-legacy-client.jar'))
p.add_argument('--original-mod', type=Path, default=Path('/workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar'))
p.add_argument('--forge-userdev', type=Path, default=Path('/tmp/nagaport-forge1614-userdev.jar'))
a = p.parse_args()
cp = a.classpath_file.read_text().strip()
java, javac = (str(a.java_home / 'bin' / name) for name in ('java', 'javac'))
assert hashlib.sha256(a.original_mod.read_bytes()).hexdigest() == '3f5629077628b5956ff7ff4e254c25ef0e1bc51f5b6668fde77badd5f784f67b'
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest() == 'a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
assert hashlib.sha256(a.forge_userdev.read_bytes()).hexdigest() == '6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91'
logs = []
def run(command, cwd=None):
    result = subprocess.run(list(map(str, command)), cwd=cwd, capture_output=True, text=True)
    logs.append(result.stdout + result.stderr)
    if result.returncode:
        (HERE / 'runtime-oracle.log').write_text(''.join(logs))
        raise RuntimeError(result.stdout + result.stderr)
    return result.stdout
with zipfile.ZipFile(a.forge_userdev) as archive:
    with zipfile.ZipFile(io.BytesIO(archive.read('forgepatches.zip'))) as patches:
        item = patches.read('net/minecraft/item/Item.java.patch').decode()
        stack = patches.read('net/minecraft/item/ItemStack.java.patch').decode()
        armor = patches.read('net/minecraft/item/ItemArmor.java.patch').decode()
        living = patches.read('net/minecraft/entity/EntityLivingBase.java.patch').decode()
        assert '+    public Multimap getAttributeModifiers(ItemStack stack)' in item
        assert '+        return this.func_111205_h();' in item
        assert '+            object = this.func_77973_b().getAttributeModifiers(this);' in stack
        assert 'func_111205_h' not in armor and 'getAttributeModifiers' not in armor
        assert 'func_111283_C' not in living and 'getAttributeModifiers' not in living
    with zipfile.ZipFile(io.BytesIO(archive.read('fmlpatches.zip'))) as patches:
        for name in ('Item', 'ItemStack', 'ItemArmor', 'EntityLivingBase'):
            suffix = 'net/minecraft/' + ('entity/' if name == 'EntityLivingBase' else 'item/') + name + '.java.patch'
            if suffix in patches.namelist():
                patch = patches.read(suffix).decode()
                assert 'func_111205_h' not in patch and 'func_111283_C' not in patch and 'getAttributeModifiers' not in patch
logs.append('PASS Forge1614 actual Item/ItemStack virtual bridge patches and absence of armor hook replacements; FML has no competing attribute hook patch\n')
with tempfile.TemporaryDirectory(prefix='native-armor-attribute-engine-') as directory:
    temp = Path(directory)
    unsigned = temp / 'legacy-unsigned.jar'
    with zipfile.ZipFile(a.legacy_client) as source, zipfile.ZipFile(unsigned, 'w') as target:
        for entry in source.infolist():
            if entry.filename.upper().startswith('META-INF/') and entry.filename.upper().endswith(('.SF', '.RSA', '.DSA')):
                continue
            target.writestr(entry, source.read(entry.filename))
    run([javac, '-source', '8', '-target', '8', '-cp', cp, '-d', temp, HERE / 'NativeArmorAttributeEngineTest.java', HERE / 'OriginalArmorPayloadOracle.java'])
    print(run([java, '-cp', str(temp) + os.pathsep + cp, 'NativeArmorAttributeEngineTest', unsigned], cwd=temp), end='')
    print(run([java, '-cp', str(temp) + os.pathsep + cp, 'OriginalArmorPayloadOracle', unsigned, a.original_mod, a.forge_userdev, HERE / 'original-payloads.json'], cwd=temp), end='')
(HERE / 'runtime-oracle.log').write_text(''.join(logs))
(HERE / 'runtime-inputs.json').write_text(json.dumps({
    'original_payload_oracle_sha256': hashlib.sha256((HERE / 'OriginalArmorPayloadOracle.java').read_bytes()).hexdigest(),
    'test_sha256': hashlib.sha256((HERE / 'NativeArmorAttributeEngineTest.java').read_bytes()).hexdigest(),
    'original_mod_sha256': hashlib.sha256(a.original_mod.read_bytes()).hexdigest(),
    'original_sha256': hashlib.sha256(a.legacy_client.read_bytes()).hexdigest(),
    'native_engine_sha256': hashlib.sha256(Path(next(x for x in cp.split(':') if '/forgeBin-' in x)).read_bytes()).hexdigest(),
    'scope': 'Complete original armor classfiles with symbols-only remapping: 12 maps/16 payload tuples; real original Item/ItemArmor maps; native item bridge, ItemStack NBT, ItemArmor baseline, actual AttributeMap contamination/preservation; Forge patch and equipment-loop bytecode evidence, not full mod/gameplay'
}, indent=2) + '\n')

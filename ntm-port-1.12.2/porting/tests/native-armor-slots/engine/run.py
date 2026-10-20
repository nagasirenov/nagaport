#!/usr/bin/env python3
"""Bounded original/native armor engine oracle, using only existing toolchain artifacts."""
from pathlib import Path
import argparse, hashlib, io, json, os, subprocess, tempfile, zipfile
HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[3]
p = argparse.ArgumentParser()
p.add_argument('--java-home', type=Path, default=Path('/workspace/shared/nagaport-toolchain/java8'))
p.add_argument('--classpath-file', type=Path, default=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt'))
p.add_argument('--legacy-client', type=Path, default=Path('/tmp/nagaport-legacy-client.jar'))
p.add_argument('--forge-userdev', type=Path, default=Path('/tmp/nagaport-forge1614-userdev.jar'))
a = p.parse_args()
cp = a.classpath_file.read_text().strip()
java, javac = (str(a.java_home / 'bin' / n) for n in ('java', 'javac'))
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest() == 'a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
assert hashlib.sha256(a.forge_userdev.read_bytes()).hexdigest() == '6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91'
with zipfile.ZipFile(a.forge_userdev) as userdev:
    for patch_name in ('forgepatches.zip', 'fmlpatches.zip'):
        with zipfile.ZipFile(io.BytesIO(userdev.read(patch_name))) as patches:
            for owner in ('net/minecraft/entity/EntityLiving.java.patch', 'net/minecraft/entity/monster/EntityZombie.java.patch'):
                if owner in patches.namelist():
                    text = patches.read(owner).decode()
                    assert 'func_70062_b' not in text and 'func_71124_b' not in text, (patch_name, owner)
print('PASS Forge1614/FML do not patch concrete mob equipment setter/getter contracts')
helper = ROOT / 'src/main/java/com/hbm/util/ArmorSlotOrder.java'
assert helper.is_file(), helper
log = []
def run(command, cwd=None):
    result = subprocess.run(list(map(str, command)), cwd=cwd, capture_output=True, text=True)
    log.append(result.stdout + result.stderr)
    if result.returncode:
        (HERE / 'runtime-oracle.log').write_text(''.join(log))
        raise RuntimeError(result.stdout + result.stderr)
    return result.stdout
with tempfile.TemporaryDirectory(prefix='native-armor-engine-') as td:
    temp = Path(td)
    unsigned = temp / 'original-unsigned.jar'
    with zipfile.ZipFile(a.legacy_client) as source, zipfile.ZipFile(unsigned, 'w') as target:
        for entry in source.infolist():
            if entry.filename.upper().startswith('META-INF/') and entry.filename.upper().endswith(('.SF', '.RSA', '.DSA')):
                continue
            target.writestr(entry, source.read(entry.filename))
    original = temp / 'original'; original.mkdir()
    (original / 'OriginalArmorTracking.java').write_text('''public class OriginalArmorTracking extends yq {
 public int calls, slot; public add stack;
 public OriginalArmorTracking() { super((ahb) null); }
 @Override public void c(int slot, add stack) { calls++; this.slot=slot; this.stack=stack; super.c(slot,stack); }
}\n''')
    run([javac, '-source', '8', '-target', '8', '-cp', a.legacy_client, '-d', original, original / 'OriginalArmorTracking.java'])
    run([javac, '-source', '8', '-target', '8', '-cp', cp, '-d', temp, helper, HERE / 'NativeArmorEngineTest.java'])
    print(run([java, '-cp', str(temp) + os.pathsep + cp, 'NativeArmorEngineTest', unsigned, original], cwd=temp), end='')
    # Mutation controls must fail during real runtime, not merely be rejected by string guards.
    source = helper.read_text()
    mutations = {
        'wrong_head_slot': source.replace('case 0: return EntityEquipmentSlot.HEAD;', 'case 0: return EntityEquipmentSlot.FEET;'),
        'inventory_index_confusion': source.replace('case HEAD: return 0;', 'case HEAD: return 3;'),
        'accept_hand_slot': source.replace('case HEAD: return 0;', 'case MAINHAND: return 0;\n        case HEAD: return 0;'),
    }
    expected_failures = {
        'wrong_head_slot': 'production helper expected slot 0',
        'inventory_index_confusion': 'production helper round trip 0',
        'accept_hand_slot': 'accepted hand as armor',
    }
    for name, body in mutations.items():
        assert body != source, name
        mutant = temp / name; mutant.mkdir()
        java_file = mutant / 'ArmorSlotOrder.java'; java_file.write_text(body)
        run([javac, '-source', '8', '-target', '8', '-cp', cp, '-d', mutant, java_file])
        result = subprocess.run([java, '-cp', str(mutant) + os.pathsep + str(temp) + os.pathsep + cp, 'NativeArmorEngineTest', str(unsigned), str(original)], cwd=temp, capture_output=True, text=True)
        assert result.returncode != 0 and ('AssertionError: ' + expected_failures[name]) in result.stderr, (name, result.stdout, result.stderr)
        log.append('PASS killed mutation ' + name + '\n' + result.stderr)
        print('PASS killed mutation ' + name)
(HERE / 'runtime-oracle.log').write_text(''.join(log))
(HERE / 'runtime-inputs.json').write_text(json.dumps({'helper_sha256': hashlib.sha256(helper.read_bytes()).hexdigest(), 'original_sha256': hashlib.sha256(a.legacy_client.read_bytes()).hexdigest(), 'scope': '20 builtin constructor pairs, actual mob setters, virtual dispatch, production helper, 3 mutation controls'}, indent=2) + '\n')

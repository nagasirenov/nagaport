#!/usr/bin/env python3
"""Offline Java 8 proof against pinned, genuine Forge classes; never runs Gradle."""
import argparse, hashlib, json, pathlib, re, subprocess, tempfile, zipfile
from importlib.machinery import SourceFileLoader
HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parents[3]
OWNERS = ['CommonConfig', 'BombConfig', 'WeaponConfig']
PREFIX = 'ntm-port-1.12.2/src/main/java/com/hbm/config/'

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--java-home', type=pathlib.Path, required=True)
    ap.add_argument('--classpath-file', type=pathlib.Path, required=True)
    ap.add_argument('--legacy-userdev', type=pathlib.Path, required=True)
    ap.add_argument('--report', type=pathlib.Path)
    ap.add_argument('--mutants', action='store_true')
    args = ap.parse_args()
    guard = SourceFileLoader('source_proof', str(HERE / 'source-proof.py')).load_module()
    guard.main()
    cp = args.classpath_file.read_text().strip()
    javac, java, javap = [str(args.java_home / 'bin' / n) for n in ['javac', 'java', 'javap']]
    report = {'base': 'a65d3acbf05634c3cedbce5eff43ad0bd15cefdb', 'production_owners': OWNERS,
              'legacy_userdev_sha256': hashlib.sha256(args.legacy_userdev.read_bytes()).hexdigest()}
    with tempfile.TemporaryDirectory(prefix='property-comment-native-') as temporary:
        temp = pathlib.Path(temporary)
        with zipfile.ZipFile(args.legacy_userdev) as archive:
            legacy_jar = temp / 'legacy-binaries.jar'; legacy_jar.write_bytes(archive.read('binaries.jar'))
        native_sources = {n: (ROOT / (PREFIX + n + '.java')).read_bytes() for n in OWNERS}
        with zipfile.ZipFile(HERE / 'baseline.zip') as archive:
            legacy_sources = {n: archive.read(PREFIX + n + '.java') for n in OWNERS}
        fixture = temp / 'fixture/com/hbm/main/MainRegistry.java'; fixture.parent.mkdir(parents=True)
        fixture.write_text('package com.hbm.main; public final class MainRegistry { public static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger("property-comment-test"); }\n')
        oracle = temp / 'fixture/com/hbm/testing/NativeFieldOracle.java'; oracle.parent.mkdir(parents=True)
        oracle.write_text('package com.hbm.testing; import net.minecraftforge.common.config.Property; public final class NativeFieldOracle { public static void assign(Property property, String value) { try { java.lang.reflect.Field field = Property.class.getDeclaredField("comment"); field.setAccessible(true); field.set(property, value); } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); } } }\n')
        def compile_sources(label, sources, classpath, should_pass=True):
            src = temp / label / 'src'; classes = temp / label / 'classes'; classes.mkdir(parents=True)
            files = []
            for name, content in sources.items():
                path = src / 'com/hbm/config' / (name + '.java'); path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(content); files.append(str(path))
            result = subprocess.run([javac, '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-cp', classpath,
                '-sourcepath', str(src), '-d', str(classes), str(fixture), str(oracle), str(HERE / 'PropertyCommentTest.java')] + files,
                stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
            if should_pass and result.returncode: raise AssertionError(label + ' compile failed:\n' + result.stdout)
            return classes, result
        def run(label, sources, classpath, mode, expected_pass=True):
            classes, _ = compile_sources(label, sources, classpath)
            output = temp / label / 'output'
            proc = subprocess.run([java, '-cp', str(classes) + ':' + classpath, 'PropertyCommentTest', mode, str(output)],
                cwd=temp, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
            if expected_pass:
                if proc.returncode: raise AssertionError(label + ' run failed:\n' + proc.stdout)
                print(proc.stdout.strip())
            else:
                assert proc.returncode and 'AssertionError' in proc.stdout, label + ': mutant escaped or failed for wrong reason\n' + proc.stdout
                print('PASS runtime mutant rejected: ' + label)
            return output, classes
        native_output, native_classes = run('native', native_sources, cp, 'native')
        legacy_output, _ = run('legacy', legacy_sources, str(legacy_jar) + ':' + cp, 'legacy')
        oracle_sources = {name: re.sub(rb'(\w+)\.setComment\(([^\r\n]*)\);', rb'com.hbm.testing.NativeFieldOracle.assign(\1, \2);', content) for name, content in native_sources.items()}
        oracle_output, _ = run('native-field-oracle', oracle_sources, cp, 'native')
        files = sorted(p.name for p in native_output.iterdir())
        assert files == sorted(p.name for p in legacy_output.iterdir()) == sorted(p.name for p in oracle_output.iterdir())
        hashes = {}; engine_differences = {}
        for name in files:
            native, legacy = (native_output / name).read_bytes(), (legacy_output / name).read_bytes()
            assert native == (oracle_output / name).read_bytes(), name + ': native setter differs from direct field assignment'
            if name == 'snapshots.txt':
                native_lines, legacy_lines = native.decode().splitlines(), legacy.decode().splitlines()
                assert len(native_lines) == len(legacy_lines)
                phase = ''
                for native_line, legacy_line in zip(native_lines, legacy_lines):
                    if ' changed=' in legacy_line: phase = legacy_line
                    if native_line == legacy_line: continue
                    assert ' post-save changed=' in phase, 'unexpected cross-version delta outside save: ' + legacy_line
                    assert legacy_line.endswith('|true') and native_line == legacy_line[:-4] + 'false', 'unexpected engine difference: ' + legacy_line
                    engine_differences[phase] = engine_differences.get(phase, 0) + 1
                assert sum(engine_differences.values()) == 198
            else:
                assert native == legacy, name + ': cross-version saved bytes differ'
            hashes[name] = hashlib.sha256(native).hexdigest()
        print('PASS: native setter and native direct-field oracle match all 21 saved files and every snapshot byte')
        print('PASS: legacy 1614 and native 2768 match all 21 saved files and value/default/comment/bounds/pre-save/reload snapshots')
        print('LIMIT: 198 post-save Property changed flags differ only because native save clears them; legacy save does not')
        report['matching_native_artifact_sha256'] = hashes
        report['inherited_engine_post_save_flag_differences'] = engine_differences
        _, negative = compile_sources('legacy-on-native', legacy_sources, cp, should_pass=False)
        assert negative.returncode and negative.stdout.count('comment has private access in Property') == 27, negative.stdout
        print('PASS: native compiler rejects exactly 27 legacy private Property.comment writes')
        property_code = subprocess.check_output([javap, '-classpath', cp, '-private', '-c', 'net.minecraftforge.common.config.Property'], text=True)
        setter = re.search(r'public void setComment\(java.lang.String\);\s+Code:\s+(.*?)\n\n', property_code, re.S).group(1)
        operations = [x.strip().split('//')[0].strip() for x in setter.splitlines()]
        assert len(operations) == 4 and operations[0] == '0: aload_0' and operations[1] == '1: aload_1' and operations[2].startswith('2: putfield') and operations[3] == '5: return', setter
        assert 'Field comment:Ljava/lang/String;' in setter
        report['native_setter_bytecode'] = setter
        print('PASS: genuine native setter bytecode is exactly aload_0, aload_1, putfield comment, return')
        owner_code = subprocess.check_output([javap, '-classpath', str(native_classes) + ':' + cp, '-c'] + ['com.hbm.config.' + n for n in OWNERS], text=True)
        assert owner_code.count('Property.setComment:') == 27
        print('PASS: complete migrated owners emit exactly 27 real Property.setComment invocations')
        report['runtime_mutants'] = []
        if args.mutants:
            for label, owner, old, new in [
                ('omit-helper-comment', 'CommonConfig', b'prop.setComment(comment);', b';'),
                ('corrupt-literal-comment', 'BombConfig', b'propGadget.setComment("Radius of the Gadget");', b'propGadget.setComment("Changed text");'),
                ('alter-helper-return', 'CommonConfig', b'return prop.getInt();', b'return 0;'),
                ('alter-radius-default', 'BombConfig', b'"3.00_gadgetRadius", 150', b'"3.00_gadgetRadius", 151')]:
                mutated = dict(native_sources); assert old in mutated[owner]
                mutated[owner] = mutated[owner].replace(old, new, 1)
                run(label, mutated, cp, 'native', expected_pass=False)
                report['runtime_mutants'].append(label)
        # Record native class provenance without copying copyrighted engine artifacts.
        provenance = {}
        for entry in cp.split(':'):
            path = pathlib.Path(entry)
            if path.is_file() and path.suffix == '.jar':
                with zipfile.ZipFile(path) as archive:
                    for name in ['net/minecraftforge/common/config/Property.class', 'net/minecraftforge/common/config/Configuration.class', 'net/minecraftforge/common/config/ConfigCategory.class']:
                        if name in archive.namelist():
                            assert name not in provenance, 'duplicate engine class: ' + name
                            provenance[name] = {'jar': str(path), 'sha256': hashlib.sha256(archive.read(name)).hexdigest()}
        assert len(provenance) == 3
        report['native_classes'] = provenance
    if args.report: args.report.write_text(json.dumps(report, indent=2) + '\n')

if __name__ == '__main__': main()

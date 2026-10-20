#!/usr/bin/env python3
"""Actual native CPU model/loading/buffer classes, all current model resources and caller audit."""
import argparse
import os
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java'

def clean(source):
    return re.sub(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/',
                  lambda m: ' ' if m.group().startswith(('//', '/*')) else m.group(), source)

def require(value, message):
    if not value:
        raise AssertionError(message)

def inventory():
    entries = []
    counts = {'HFR': 0, 'PLAIN': 0}
    for path in SRC.rglob('*.java'):
        source = clean(path.read_text())
        # The eight non-inline plain loaders use a same-file constant ResourceLocation.
        constants = dict(re.findall(r'\bResourceLocation\s+(\w+)\s*=\s*new\s+ResourceLocation\([^;]*?"(models/[^"\n]+\.obj)"\s*\)', source))
        for line in source.splitlines():
            if 'new HFRWavefrontObject(' not in line and 'new ObjMeshModel(' not in line:
                continue
            kind = 'HFR' if 'new HFRWavefrontObject(' in line else 'PLAIN'
            counts[kind] += 1
            paths = re.findall(r'"(models/[^"\n]+\.obj)"', line)
            if not paths:
                match = re.search(r'new ObjMeshModel\((\w+)\)', line)
                require(match and match.group(1) in constants, 'Unresolved eager model location: ' + str(path) + ': ' + line)
                paths = [constants[match.group(1)]]
            require(len(paths) == 1, 'Ambiguous model location: ' + line)
            entries.append((kind, paths[0], ', true)' in line))
    require(counts == {'HFR': 393, 'PLAIN': 146}, 'Model inventory changed: ' + str(counts))
    return sorted(set(entries)), counts

def missing_uv_callers():
    # All five HFR assets with missing UV groups: four lead with such a group; radgen does not.
    names = {'radgen', 'lantern', 'siege_ufo', 'charger', 'lance'}
    references = []
    for path in SRC.rglob('*.java'):
        source = clean(path.read_text())
        require(not re.search(r'import\s+static\s+com\.hbm\.main\.ResourceManager\.', source),
                'Re-audit missing-UV ownership after static ResourceManager import: ' + str(path))
        for line in source.splitlines():
            for name in names:
                if not re.search(r'\bResourceManager\.' + name + r'\b', line):
                    continue
                references.append((str(path), line.strip()))
                part_call = re.search(r'\bResourceManager\.' + name + r'\.renderPart\("[^"]+"\)', line)
                glint = (name == 'siege_ufo' and 'RenderMiscEffects.renderClassicGlint(' in line
                         and 'ResourceManager.siege_ufo, "UFO",' in line)
                require(part_call or glint, 'New whole-model/alias use of missing-UV HFR model requires review: ' + str(path) + ': ' + line)
    require(len(references) == 26, 'Missing-UV caller set changed: ' + str(len(references)))
    glint = clean((SRC / 'com/hbm/render/util/RenderMiscEffects.java').read_text())
    require(re.search(r'if\s*\(\s*"all"\.equals\(part\)\s*\)\s*model\.renderAll\(\);\s*else\s*model\.renderPart\(part\);', glint),
            'Glint part selection changed; re-audit literal UFO route')
    return references

def main():
    p = argparse.ArgumentParser()
    p.add_argument('--classpath')
    p.add_argument('--java-home', type=Path)
    p.add_argument('--audit-only', action='store_true')
    a = p.parse_args()
    entries, counts = inventory()
    references = missing_uv_callers()
    print('PASS source audit: ' + str(counts) + ', ' + str(len(entries)) + ' distinct policy/resource combinations; 25 direct missing-UV part calls plus one literal UFO glint route, no source aliases/static imports.', flush=True)
    print('LIMIT: this caller audit cannot establish external mod or changed-resource-pack behavior.', flush=True)
    if a.audit_only:
        return
    require(a.classpath and a.java_home, '--classpath and --java-home required for native tests')
    hbm = SRC / 'com/hbm'
    files = sorted((hbm / 'render/mesh').glob('*.java')) + [
        hbm / 'lib/RefStrings.java', hbm / 'render/loader/HFRWavefrontObject.java',
        hbm / 'render/loader/HFRWavefrontObjectVBO.java', hbm / 'render/loader/HFRModelReloader.java',
        hbm / 'render/loader/ModelRendererObj.java', ROOT / 'porting/tests/HfrCpuMinecraftTest.java']
    with tempfile.TemporaryDirectory(prefix='hfr-cpu-native-') as tmp:
        manifest = Path(tmp) / 'models.tsv'
        manifest.write_text(''.join(kind + '\t' + asset + '\t' + str(mixed).lower() + '\n' for kind, asset, mixed in entries))
        subprocess.run([str(a.java_home / 'bin/javac'), '-source', '8', '-target', '8', '-encoding', 'UTF-8',
                        '-cp', a.classpath, '-d', tmp] + list(map(str, files)), check=True)
        subprocess.run([str(a.java_home / 'bin/java'), '-Xmx1G', '-cp', tmp + os.pathsep + a.classpath,
                        'HfrCpuMinecraftTest', str(ROOT / 'src/main/resources/assets/hbm'), str(manifest)], cwd=tmp, check=True)

if __name__ == '__main__':
    main()

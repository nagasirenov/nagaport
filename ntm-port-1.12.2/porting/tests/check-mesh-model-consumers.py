#!/usr/bin/env python3
"""Portable checkpoint-7 reverse-source audit for the typed mesh consumer boundary.

This validates actual complete consumer source files, not extracted Java fixtures.
It neither compiles those consumers nor proves renderer/backend runtime behavior.
The manifest contains baseline hashes, so recipient checkouts need no local Git
objects, pristine sibling directory, Minecraft binaries, or network access.
"""

import argparse
import hashlib
import json
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile


ROOT = Path(__file__).resolve().parents[2]
MANIFEST = ROOT / 'porting/tests/mesh-model-consumer-manifest.json'
NON_CODE = re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\r\n]*|/\*[\s\S]*?\*/')
OLD_TYPES = {'C': 'IModelCustom', 'N': 'IModelCustomNamed'}


def require(condition, message):
    if not condition:
        raise AssertionError(message)


def digest(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()


def code_view(text):
    """Hide comments/literals without changing character offsets or line breaks."""
    return NON_CODE.sub(lambda match: re.sub(r'[^\r\n]', ' ', match.group()), text)


def type_matches(text, pattern):
    view = code_view(text)
    imports = list(re.finditer(r'(?m)^\s*import\s+[^;]+;', view))
    return [match for match in re.finditer(pattern, view)
            if not any(start.start() <= match.start() < start.end() for start in imports)]


def apply_spans(text, changes):
    for start, end, replacement in sorted(changes, reverse=True):
        text = text[:start] + replacement + text[end:]
    return text


def count_code(text, pattern):
    return len(re.findall(pattern, code_view(text)))


def invariant_counts(text):
    return {
        'hfr_constructions': count_code(text, r'\bnew\s+HFRWavefrontObject\s*\('),
        'as_vbo_calls': count_code(text, r'\.\s*asVBO\s*\('),
        'no_smooth_calls': count_code(text, r'\.\s*noSmooth\s*\('),
    }


def reverse_consumer(text, entry):
    require(digest(text) == entry['migrated_sha256'], entry['path'] + ': migrated source hash changed')
    for change in reversed(entry.get('whitespace_normalizations', [])):
        require(text.count(change['new']) == 1, entry['path'] + ': normalized indentation boundary changed')
        text = text.replace(change['new'], change['old'], 1)
    matches = type_matches(text, r'\bMeshModel\b')
    sequence = entry['original_type_sequence']
    require(len(matches) == len(sequence), entry['path'] + ': type occurrence count changed')
    require(all(kind in OLD_TYPES for kind in sequence), entry['path'] + ': invalid original type marker')
    restored = apply_spans(text, [(match.start(), match.end(), OLD_TYPES[kind])
                                 for match, kind in zip(matches, sequence)])
    calls = list(re.finditer(r'\bnew ObjMeshModel(?=\s*\()', code_view(restored)))
    require(len(calls) == entry['plain_factory_calls'], entry['path'] + ': eager plain constructor count changed')
    restored = apply_spans(restored, [(match.start(), match.end(), 'AdvancedModelLoader.loadModel')
                                    for match in calls])
    for change in reversed(entry['imports']):
        require(restored.count(change['new']) == 1, entry['path'] + ': import boundary changed')
        restored = restored.replace(change['new'], change['old'], 1)
    require(digest(restored) == entry['baseline_sha256'],
            entry['path'] + ': source changed outside the recorded type/import/factory/indentation substitutions')
    require(invariant_counts(text) == entry['invariants'], entry['path'] + ': concrete HFR fluent calls changed')
    require(invariant_counts(restored) == entry['invariants'], entry['path'] + ': baseline invariant mismatch')
    return restored


def self_test(manifest):
    sample = r'''// MeshModel new ObjMeshModel()
String text = "MeshModel \" // IModelCustom";
char quote = '"'; /* IModelCustom */
MeshModel live;
'''
    require(len(type_matches(sample, r'\bMeshModel\b')) == 1, 'Java lexical exclusion self-test failed')
    entry = next(item for item in manifest['files'] if item['path'].endswith('/ResourceManager.java'))
    source = (ROOT / entry['path']).read_bytes().decode('utf-8')
    mutants = {
        'asset path': source.replace('models/machines/stirling.obj', 'models/machines/sawmill.obj', 1),
        'noSmooth chain': source.replace('.noSmooth().asVBO()', '.asVBO()', 1),
        'eager factory': source.replace('new ObjMeshModel(', 'deferredLoad(', 1),
        'named field finality': source.replace('public static MeshModel pheo_fire_door',
                                               'public static final MeshModel pheo_fire_door', 1),
        'concrete VBO type': source.replace('final HFRWavefrontObjectVBO rbmk_element_rods_vbo',
                                            'final MeshModel rbmk_element_rods_vbo', 1),
    }
    for name, mutant in mutants.items():
        require(mutant != source, 'Self-test mutation did not apply: ' + name)
        changed = dict(entry, migrated_sha256=digest(mutant))
        try:
            reverse_consumer(mutant, changed)
        except AssertionError:
            pass
        else:
            raise AssertionError('Reverse-source audit accepted mutation: ' + name)
    with tempfile.TemporaryDirectory(prefix='mesh-consumer-portable-') as temporary:
        target = Path(temporary)
        paths = [item['path'] for item in manifest['files']] + [
            'porting/tests/check-mesh-model-consumers.py',
            'porting/tests/mesh-model-consumer-manifest.json']
        for relative in paths:
            destination = target / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(ROOT / relative, destination)
        unrelated = target / 'unrelated-working-directory'
        unrelated.mkdir()
        require(not (target / '.git').exists(), 'Portability self-test unexpectedly has Git metadata')
        subprocess.run([sys.executable, str(target / paths[-2])], cwd=str(unrelated), check=True)
    print('PASS: no-Git-copy portability, Java lexical exclusion, and five negative reverse-audit cases.')


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--self-test', action='store_true', help='Also exercise portability and deliberate invalid source mutations')
    args = parser.parse_args(argv)
    manifest = json.loads(MANIFEST.read_text())
    require(manifest['format'] == 1, 'Unsupported consumer manifest format')
    paths = [entry['path'] for entry in manifest['files']]
    require(paths == sorted(set(paths)), 'Consumer manifest paths must be unique and sorted')
    require(len(paths) == 31, 'Expected all 31 non-loader consumer files')
    normalized_paths = [entry['path'] for entry in manifest['files'] if entry.get('whitespace_normalizations')]
    require(normalized_paths == ['src/main/java/com/hbm/render/entity/effect/' + name + '.java'
                                 for name in ['RenderCloudFleija', 'RenderCloudRainbow', 'RenderCloudSolinium']],
            'Only the three known Cloud loader-line indentation prefixes may be normalized')
    for entry in manifest['files']:
        changes = entry.get('whitespace_normalizations', [])
        if changes:
            require(changes == [{
                'old': '    \tblastModel = new ObjMeshModel(objTesterModelRL);\n',
                'new': '\t\tblastModel = new ObjMeshModel(objTesterModelRL);\n'}],
                entry['path'] + ': whitespace delta must be the exact recorded loader-line prefix')
    totals = {'plain_factory_calls': 0, 'ordinary_type_uses': 0, 'named_type_uses': 0,
              'hfr_constructions': 0, 'as_vbo_calls': 0, 'no_smooth_calls': 0}
    restored_resources = None
    for entry in manifest['files']:
        require(entry['path'].startswith('src/main/java/com/hbm/'), 'Unexpected consumer path')
        require('/render/loader/' not in entry['path'] and '/render/mesh/' not in entry['path'],
                'Consumer audit must not claim backend/loader ownership')
        text = (ROOT / entry['path']).read_bytes().decode('utf-8')
        old = reverse_consumer(text, entry)
        require(not re.search(r'\b(?:IModelCustom|IModelCustomNamed|AdvancedModelLoader)\b', code_view(text)),
                entry['path'] + ': live legacy type/factory reference remains')
        require(text.count('import com.hbm.render.mesh.MeshModel;') == 1,
                entry['path'] + ': missing or duplicate domain import')
        require(text.count('import com.hbm.render.mesh.ObjMeshModel;') == (entry['plain_factory_calls'] > 0),
                entry['path'] + ': missing or unused plain-model import')
        totals['plain_factory_calls'] += entry['plain_factory_calls']
        totals['ordinary_type_uses'] += entry['original_type_sequence'].count('C')
        totals['named_type_uses'] += entry['original_type_sequence'].count('N')
        for key, value in entry['invariants'].items():
            totals[key] += value
        if entry['path'].endswith('/ResourceManager.java'):
            restored_resources = old
    require(totals == manifest['totals'], 'Consumer totals differ from the checkpoint-7 manifest')
    require(totals == {'plain_factory_calls': 146, 'ordinary_type_uses': 499, 'named_type_uses': 17,
                       'hfr_constructions': 393, 'as_vbo_calls': 293, 'no_smooth_calls': 77},
            'Expected checkpoint-7 consumer inventory changed')
    require(sum(entry['plain_factory_calls'] > 0 for entry in manifest['files']) == 12,
            'Expected twelve plain-factory consumer files')
    resources = (ROOT / 'src/main/java/com/hbm/main/ResourceManager.java').read_text()
    require(restored_resources is not None, 'ResourceManager is missing from the manifest')
    old_named = re.findall(r'public static IModelCustomNamed\s+(\w+)\s*=', code_view(restored_resources))
    require(old_named == manifest['resource_manager']['named_door_fields'], 'Original named-door field inventory changed')
    for name in old_named:
        require(count_code(resources, r'public static MeshModel\s+' + re.escape(name) + r'\s*=') == 1,
                name + ': named door must remain an eager non-final field in its original position')
    vbo_pattern = r'public static final HFRWavefrontObjectVBO\s+(\w+)\s*='
    require(re.findall(vbo_pattern, code_view(resources)) == manifest['resource_manager']['concrete_vbo_fields'],
            'The eight concrete VBO field types/order must remain unchanged')
    legacy = re.compile(r'\b(?:IModelCustom|IModelCustomNamed|AdvancedModelLoader)\b')
    for path in (ROOT / 'src/main/java').rglob('*.java'):
        relative = path.relative_to(ROOT).as_posix()
        if '/render/loader/' in relative or '/render/mesh/' in relative or path.name in ('ObjUtil.java', 'RenderMirror.java'):
            continue
        require(not legacy.search(code_view(path.read_text())), 'Unmigrated external consumer: ' + relative)
    print('PASS: all 31 complete consumer files reverse exactly to their recorded checkpoint-7 SHA-256.')
    print('PASS: 499 ordinary + 17 named type uses migrated; 146 eager plain loads in 12 files retained.')
    print('PASS: 393 HFR constructions, 293 asVBO calls, 77 noSmooth calls, 12 named doors and 8 concrete VBO fields retained.')
    print('PASS: exactly three inherited Cloud loader-line indentation prefixes normalized and reversed explicitly.')
    print('BASELINE: ' + manifest['base_commit'] + ' (informational; no Git object required)')
    print('LIMIT: exact source-delta parity, not full consumer compilation, GL execution, or gameplay acceptance.')
    if args.self_test:
        self_test(manifest)


if __name__ == '__main__':
    main()

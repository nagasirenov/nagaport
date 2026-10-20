#!/usr/bin/env python3
"""Read-only retirement audit. No Java sources, native inputs, or source guards are rewritten."""
import argparse
import hashlib
import json
import re
import sys
import tempfile
import zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent
MANIFEST_SHA256 = 'c0c554b75e8947fc047e04e94529e3293fa9629d46eb057de419cf95b22d7960'
NAMES = ('RenderBlocksFixed', 'RenderBlocksNT', 'RenderBlocksWrapper', 'IconUtil')
SYMBOL = re.compile('|'.join(NAMES))
TOKENS = re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\r\n]*|/\*.*?\*/', re.S)


def sha(data):
    return hashlib.sha256(data).hexdigest()


def java_without_comments(text):
    # Keep literals: a reflective class-name string is a live reference, not trivia.
    text = re.sub(r'\\u+([0-9a-fA-F]{4})', lambda m: chr(int(m[1], 16)), text)
    return TOKENS.sub(lambda m: '' if m[0].startswith(('//', '/*')) else m[0], text)


def require_no_reference(relative, data):
    assert not SYMBOL.search(relative), (relative, 'retired class named by runtime resource/source path')
    if relative.endswith('.java'):
        hits = SYMBOL.findall(java_without_comments(data.decode('utf-8')))
    else:
        hits = [name for name in NAMES if name.encode() in data or
                name.encode('utf-16-le') in data or name.encode('utf-16-be') in data]
    assert not hits, (relative, 'retired runtime reference', hits)


def require_absent(root, relative):
    assert not (root / relative).exists(), (relative, 'retired implementation reintroduced, including empty tombstones')


def self_test():
    rejected = 0
    for name in NAMES:
        samples = [f'import com.hbm.render.util.{name};', f'new {name}();',
                   f'Class.forName("com.hbm.render.util.{name}");',
                   f'String c = "{name}";', ('\\u%04x' % ord(name[0])) + name[1:]]
        for sample in samples:
            try:
                require_no_reference('fixture.java', sample.encode())
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError(('missed reference fixture', sample))
        for encoding in ('utf-8', 'utf-16-le', 'utf-16-be'):
            try:
                require_no_reference('META-INF/services/fixture', name.encode(encoding))
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError(('missed resource fixture', name, encoding))
        try:
            require_no_reference('META-INF/services/' + name, b'')
        except AssertionError:
            rejected += 1
        else:
            raise AssertionError(('missed service filename fixture', name))
        require_no_reference('fixture.java', f'// {name}\n/** {name} */ class Unrelated {{}}'.encode())
    with tempfile.TemporaryDirectory(prefix='render-retirement-audit-') as temp:
        root = Path(temp)
        require_absent(root, 'Empty.java')
        (root / 'Empty.java').write_bytes(b'')
        try:
            require_absent(root, 'Empty.java')
        except AssertionError:
            rejected += 1
        else:
            raise AssertionError('empty tombstone accepted')
    return rejected


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--module', type=Path, default=HERE.parents[2])
    parser.add_argument('--pristine', type=Path)
    args = parser.parse_args()
    root = args.module.resolve()
    pristine = args.pristine or root.parent / 'ntm-original-pristine'
    raw = (HERE / 'source-manifest.json').read_bytes()
    assert sha(raw) == MANIFEST_SHA256, 'retirement specification changed'
    manifest = json.loads(raw)
    assert len(manifest['deleted_files']) == 4
    for relative, entry in manifest['deleted_files'].items():
        require_absent(root, relative)
        assert sha((pristine / relative).read_bytes()) == entry['pristine_sha256'], (relative, 'pristine implementation changed')
    for relative, expected in manifest['pristine_consumer_sha256'].items():
        assert sha((pristine / relative).read_bytes()) == expected, (relative, 'pristine consumer changed')
    for relative, expected in manifest['native_successor_sha256'].items():
        assert sha((root / relative).read_bytes()) == expected, (relative, 'native replacement changed; review its proof before refreshing this audit')
    original_resource_count = 0
    for path in sorted((pristine / 'src/main/resources').rglob('*')):
        if path.is_file():
            require_no_reference(path.relative_to(pristine).as_posix(), path.read_bytes())
            original_resource_count += 1
    original_consumers = {name: [] for name in NAMES}
    for path in sorted((pristine / 'src/main/java').rglob('*.java')):
        relative = path.relative_to(pristine).as_posix()
        if relative in manifest['deleted_files']:
            continue
        text = java_without_comments(path.read_bytes().decode('utf-8'))
        for name in NAMES:
            if name in text:
                original_consumers[name].append(relative)
    assert original_consumers == manifest['pristine_consumers'], 'pristine consumer census changed'
    comments = {}
    scanned = 0
    for path in sorted((root / 'src/main').rglob('*')):
        if not path.is_file():
            continue
        relative = path.relative_to(root).as_posix()
        data = path.read_bytes()
        require_no_reference(relative, data)
        if path.suffix == '.java':
            count = len(SYMBOL.findall(data.decode('utf-8')))
            if count:
                comments[relative] = count
        scanned += 1
    assert comments == manifest['comment_only_references'], 'documentation-only reference census changed'
    for relative in manifest['runtime_config_files']:
        require_no_reference(relative, (root / relative).read_bytes())
    archive = root / manifest['original_binary_archive']['path']
    assert sha(archive.read_bytes()) == manifest['original_binary_archive']['sha256'], 'pristine binary archive changed'
    with zipfile.ZipFile(archive) as source:
        for entry in manifest['deleted_files'].values():
            assert entry['original_binary_entry'] in source.namelist(), entry['original_binary_entry']
    print('PASS four retired implementations absent, including empty tombstones; four pristine sources and original binary archive unchanged.')
    print('PASS pristine consumers: Fixed=0, Wrapper=0, IconUtil=0, NT=8; %d pristine resources have no named references; 27 exact native successor sources retained.' % original_resource_count)
    print('PASS %d runtime source/resource files and three build/config files: no live identifier, literal, binary-name or service/config references; two explanatory comments only.' % scanned)
    print('PASS %d negative reference/tombstone fixtures rejected.' % self_test())
    print('SOURCE/REACHABILITY AUDIT ONLY: existing family proofs are cited in README; no new full compile, original/native light-solver, gameplay or external-mod compatibility claim.')


if __name__ == '__main__':
    assert not sys.flags.optimize, 'Run without -O'
    main()

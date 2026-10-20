#!/usr/bin/env python3
"""Native HFR VBO compilation, unchanged-GL lifecycle audit and independent raw-asset oracle.

The manifest is an immutable checkpoint-7 inventory, not a copied Forge API or test backend.
Normal verification is portable and needs no git object. --check-baseline-revision additionally
re-derives the inventory from a locally available checkpoint for provenance verification.
No GL context is created and no GPU execution claim is made.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / 'src/main/java'
MANIFEST = Path(__file__).with_name('vbo-mesh-baseline.json')
VBO = 'com/hbm/render/loader/HFRWavefrontObjectVBO.java'


def require(value, label):
    if not value:
        raise AssertionError(label)


def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()


def strip_comments(source):
    # Preserve string/character literals, including comment-looking characters in strings.
    return re.sub(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/',
                  lambda m: ' ' if m.group().startswith(('//', '/*')) else m.group(), source)


def compact(source):
    return re.sub(r'\s+', '', strip_comments(source))


def matching(source, start, left='(', right=')'):
    depth = 0
    quoted = None
    escape = False
    for i in range(start, len(source)):
        c = source[i]
        if quoted:
            if escape:
                escape = False
            elif c == '\\':
                escape = True
            elif c == quoted:
                quoted = None
        elif c in ('"', "'"):
            quoted = c
        elif c == left:
            depth += 1
        elif c == right:
            depth -= 1
            if depth == 0:
                return i
    raise AssertionError('Unclosed source expression')


def inventory(sources):
    calls = []
    for path, raw in sources.items():
        source = strip_comments(raw)
        found = 0
        for match in re.finditer(r'new\s+HFRWavefrontObject\s*\(', source):
            end = matching(source, source.index('(', match.start())) + 1
            tail = end
            methods = []
            while True:
                method = re.match(r'\s*\.\s*(\w+)\s*\(', source[tail:])
                if not method:
                    break
                methods.append(method.group(1))
                tail = matching(source, tail + method.end() - 1) + 1
            if 'asVBO' not in methods:
                continue
            paths = re.findall(r'"(models/[^"\n]+\.(?:obj|hmf))"', source[match.start():end])
            require(len(paths) == 1, 'Unresolved VBO resource expression in ' + path)
            calls.append({'source': path, 'asset': paths[0]})
            found += 1
        require(found == len(re.findall(r'\.\s*asVBO\s*\(', source)),
                'VBO call not traced to an explicit HFR constructor: ' + path)
    return sorted(calls, key=lambda item: (item['source'], item['asset']))


def method(source, name):
    match = re.search(r'(?:public|private)\s+[\w<>]+\s+' + re.escape(name) + r'\s*\(', source)
    require(match is not None, 'Method missing: ' + name)
    start = source.index('{', match.end())
    return source[match.start():matching(source, start, '{', '}') + 1]


def gl_upload(source):
    start = source.index('data.vertexHandle = GL15.glGenBuffers();')
    end = source.index('groups.add(data);', start) + len('groups.add(data);')
    return compact(source[start:end])


def all_sources(revision=None):
    if revision:
        prefix = 'ntm-port-1.12.2/src/main/java/'
        files = subprocess.check_output(['git', 'ls-tree', '-r', '--name-only', revision, '--', prefix],
                                        cwd=ROOT.parent, text=True).splitlines()
        return {path[len(prefix):]: subprocess.check_output(['git', 'show', revision + ':' + path],
                 cwd=ROOT.parent, text=True) for path in files if path.endswith('.java')}
    return {str(file.relative_to(SRC)): file.read_text() for file in SRC.rglob('*.java')}


def audit(manifest, sources):
    calls = inventory(sources)
    require(calls == manifest['calls'], 'Current VBO constructor/path inventory differs from checkpoint 7')
    require(len(calls) == 293 and len({item['asset'] for item in calls}) == 293,
            'Expected 293 distinct VBO resources')
    for asset, expected in manifest['assets'].items():
        # These source paths are ASCII; this is ResourceLocation's Locale.ROOT path
        # normalization, not a fallback filesystem casing search. Preserve manifest keys.
        require(asset.isascii(), 'Native-path audit requires ASCII resource path: ' + asset)
        data = (ROOT / 'src/main/resources/assets/hbm' / asset.lower()).read_bytes()
        require(hashlib.sha256(data).hexdigest() == expected, 'VBO asset changed: ' + asset)
    source = sources[VBO]
    require('implements MeshModel' in source, 'VBO does not implement native mesh contract')
    require(not re.search(r'\b(?:IModelCustomNamed|S_GroupObject|S_Face|Vertex|TextureCoordinate)\b', source),
            'Legacy VBO geometry dependency remains')
    require(sha(gl_upload(source)) == manifest['upload_hash'], 'VBO allocation/upload sequence changed')
    for name, expected in manifest['method_hashes'].items():
        body = method(source, name)
        if name == 'destroy':
            body = body.replace('mesh = ObjMesh.empty();', '')
        require(sha(compact(body)) == expected, 'Original VBO control/GL sequence changed: ' + name)
    normalized = compact(source)
    for expected in ['staticintVERTEX_SIZE=3;', 'staticintUV_SIZE=3;',
                     'ObjMeshloadedMesh=obj.getMesh();', 'for(ObjMesh.Partpart:loadedMesh.parts)',
                     'ObjMeshVboPayloadpayload=ObjMeshVboPayload.pack(part);',
                     'data.vertices=payload.vertices;', 'FloatBuffervertexData=payload.positions;',
                     'FloatBufferuvData=payload.uvs;', 'FloatBuffernormalData=payload.normals;',
                     'groups.add(data);}mesh=loadedMesh;', 'groups.clear();mesh=ObjMesh.empty();',
                     'publicObjMeshgetMesh(){returnmesh;}']:
        require(expected in normalized, 'Production payload/snapshot boundary changed: ' + expected)
    print('PASS VBO source audit: 293 unchanged assets/calls; actual GL15 allocation, upload, pointer, draw, '
          'delete ordering and exact source-order/duplicate selection methods retained.', flush=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--classpath')
    parser.add_argument('--java-home', type=Path)
    parser.add_argument('--audit-only', action='store_true')
    parser.add_argument('--check-baseline-revision')
    args = parser.parse_args()
    manifest = json.loads(MANIFEST.read_text())
    if args.check_baseline_revision:
        sources = all_sources(args.check_baseline_revision)
        require(inventory(sources) == manifest['calls'], 'Baseline constructor inventory differs')
        require(sha(sources[VBO]) == manifest['vbo_sha256'], 'Baseline VBO differs')
        print('PASS VBO checkpoint inventory independently re-derived from ' + args.check_baseline_revision,
              flush=True)
    audit(manifest, all_sources())
    if args.audit_only:
        return
    require(args.classpath and args.java_home, '--classpath and --java-home required for native compile')
    files = sorted((SRC / 'com/hbm/render/mesh').glob('*.java')) + [
        SRC / 'com/hbm/render/loader/HFRWavefrontObject.java', SRC / VBO,
        SRC / 'com/hbm/lib/RefStrings.java', Path(__file__).with_name('VboMeshNativeTest.java')]
    with tempfile.TemporaryDirectory(prefix='vbo-native-probe-') as tmp:
        asset_list = Path(tmp) / 'assets.txt'
        asset_list.write_text('\n'.join(sorted(manifest['assets'])) + '\n')
        subprocess.run([str(args.java_home / 'bin/javac'), '-source', '8', '-target', '8', '-encoding', 'UTF-8',
                        '-cp', args.classpath, '-d', tmp] + list(map(str, files)), check=True)
        subprocess.run([str(args.java_home / 'bin/java'), '-cp', tmp + os.pathsep + args.classpath,
                        'VboMeshNativeTest', str(ROOT / 'src/main/resources/assets/hbm'), str(asset_list)],
                       cwd=tmp, check=True)


if __name__ == '__main__':
    main()

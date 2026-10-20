#!/usr/bin/env python3
"""Bounded genuine-native proof. No downloads, Gradle, or whole-mod code emission."""
import argparse
import hashlib
import io
import json
import os
from pathlib import Path
import re
import struct
import subprocess
import tempfile
import zipfile
import audit

parser = argparse.ArgumentParser()
parser.add_argument('--java-home', type=Path, default=Path('/workspace/shared/nagaport-toolchain/java8'))
parser.add_argument('--classpath-file', type=Path, default=Path('/workspace/shared/nagaport-toolchain/native-classpath.txt'))
parser.add_argument('--legacy-client', type=Path, default=Path('/tmp/nagaport-legacy-client.jar'))
parser.add_argument('--forge-userdev', type=Path, default=Path('/tmp/nagaport-forge1614-userdev.jar'))
parser.add_argument('--attribute', action='store_true')
parser.add_argument('--mutants', action='store_true')
args = parser.parse_args()
HERE, ROOT = audit.HERE, audit.ROOT
PRIOR = HERE.parent / 'world-update-notification-native'
manifest = audit.verify()
cp = args.classpath_file.read_text().strip()
java, javac, javap = [args.java_home / 'bin' / name for name in ['java', 'javac', 'javap']]
tools = args.java_home / 'lib/tools.jar'


def run(command, **kwargs):
    return subprocess.run(list(map(str, command)), check=True, **kwargs)


def relocate(path, name):
    # Relocate only this test's own name to avoid the signed original default package.
    # This never modifies or reimplements an original Minecraft method.
    raw = path.read_bytes()
    count = struct.unpack('>H', raw[8:10])[0]
    out, index, position = bytearray(raw[:10]), 1, 10
    while index < count:
        tag = raw[position]; out.append(tag); position += 1
        if tag == 1:
            size = struct.unpack('>H', raw[position:position + 2])[0]; position += 2
            value = raw[position:position + size].replace(name.encode(), ('proof/' + name).encode()); position += size
            out += struct.pack('>H', len(value)) + value
        else:
            size = {3:4,4:4,5:8,6:8,7:2,8:2,9:4,10:4,11:4,12:4,15:3,16:2,18:4}[tag]
            out += raw[position:position + size]; position += size
            if tag in (5, 6): index += 1
        index += 1
    out += raw[position:]
    destination = path.parent / 'proof' / path.name
    destination.parent.mkdir(exist_ok=True); destination.write_bytes(out); path.unlink()


pins = json.loads((PRIOR / 'dependency-hashes.json').read_text())
for path in [args.legacy_client, args.forge_userdev, next(Path(p) for p in cp.split(os.pathsep) if Path(p).name.startswith('forgeBin-'))]:
    assert audit.digest(path.read_bytes()) == next(v for k, v in pins.items() if Path(k).name == path.name), path
with zipfile.ZipFile(args.forge_userdev) as archive:
    srg = archive.read('conf/packaged.srg').decode()
    assert 'MD: mt/t ()Lmq; net/minecraft/world/WorldServer/func_73040_p ()Lnet/minecraft/server/management/PlayerManager;' in srg
    assert 'MD: mq/a (III)V net/minecraft/server/management/PlayerManager/func_151250_a (III)V' in srg
    with zipfile.ZipFile(io.BytesIO(archive.read('forgepatches.zip'))) as patches:
        assert patches.read('net/minecraft/server/management/PlayerManager.java.patch') == (PRIOR / 'forgepatches.zip.server_management_PlayerManager.java.patch').read_bytes()
print('PASS: existing original/native SHA-256 pins, actual getter/map original mappings and complete Forge player-manager patch')

# Retain exact original/native bytecode for ownership, no-create lookup and int constructors.
boundary = []
for classpath, owner, selectors in [
    (str(args.legacy_client), 'mt', ['public mq t();']),
    (str(args.legacy_client), 'mq', ['private mr a(int, int, boolean);', 'public void a(int, int, int);']),
    (str(args.legacy_client), 'mr', ['public void a(int, int, int);']),
    (cp, 'net.minecraft.world.WorldServer', ['public net.minecraft.server.management.PlayerChunkMap getPlayerChunkMap();']),
    (cp, 'net.minecraft.server.management.PlayerChunkMap', ['public net.minecraft.server.management.PlayerChunkMapEntry getEntry(int, int);', 'public void markBlockForUpdate(net.minecraft.util.math.BlockPos);']),
    (cp, 'net.minecraft.server.management.PlayerChunkMapEntry', ['public void blockChanged(int, int, int);', 'public void update();']),
    (cp, 'net.minecraft.util.math.BlockPos', ['public net.minecraft.util.math.BlockPos(int, int, int);']),
    (cp, 'net.minecraft.util.math.Vec3i', ['public net.minecraft.util.math.Vec3i(int, int, int);'])
]:
    text = run([javap, '-p', '-c', '-classpath', classpath, owner], stdout=subprocess.PIPE, universal_newlines=True).stdout
    for selector in selectors:
        match = re.search(r'^  ' + re.escape(selector) + r'\n.*?(?=^  \S|^\})', text, re.M | re.S)
        assert match, (owner, selector)
        boundary.append(owner + '\n' + match.group())
assert (HERE / 'boundary-bytecode.txt').read_text() == '\n'.join(boundary).rstrip() + '\n'
print('PASS: retained exact getter, queue, existing-entry lookup, native update and int-constructor bytecode')

with tempfile.TemporaryDirectory(prefix='player-chunk-update-') as temporary:
    tmp = Path(temporary); output = tmp / 'classes'; output.mkdir()
    runtime = str(output) + os.pathsep + cp
    # Seven production expressions copied byte-for-byte into a bounded TileEntity projection.
    expressions = [edit['new'] for record in manifest['files'].values() for edit in record['edits']]
    projection = 'import net.minecraft.world.*; import net.minecraft.tileentity.TileEntity;\nclass SelectedPlayerCalls extends TileEntity {\n void probePosition(net.minecraft.util.math.BlockPos value){this.pos=value;}\n'
    for index, expression in enumerate(expressions):
        projection += ' void call' + str(index) + '(' + ('World' if index == 0 else 'WorldServer') + ' world,int x,int y,int z,TileEntity pylon,TileEntity pipeline){' + expression + ';}\n'
    projection += ' void apply(int index,WorldServer world,int x,int y,int z,TileEntity pylon,TileEntity pipeline){switch(index){'
    for index in range(7): projection += 'case ' + str(index) + ':call' + str(index) + '(world,x,y,z,pylon,pipeline);break;'
    projection += 'default:throw new AssertionError(index);}}}\n'
    selected = tmp / 'SelectedPlayerCalls.java'; selected.write_text(projection)
    # Existing fixture dependencies are unchanged real production helper/state owners.
    text = (ROOT / next(iter(manifest['files']))).read_text()
    methods = '\n'.join(re.search(r'    @Override (?:protected|public) [^\n]+ ' + name + r'\([^\n]+', text).group() for name in ['createBlockState', 'getStateFromMeta', 'getMetaFromState'])
    owner = tmp / 'SelectedOwnerState.java'
    owner.write_text('import com.hbm.blocks.MultipassContext; abstract class SelectedOwnerState extends net.minecraft.block.BlockContainer { SelectedOwnerState(){super(net.minecraft.block.material.Material.IRON);}' + methods + '}')
    branch = tmp / 'SelectedBranch.java'
    branch.write_text('class SelectedBranch { static void apply(net.minecraft.world.World world,int x,int y,int z,NotificationTest.TestTile tile){tile.paint=null;com.hbm.world.TileEntityBlockUpdate.notifyAt(world,x,y,z);tile.markDirty();}}')
    dependencies = [ROOT / 'src/main/java/com/hbm/world/TileEntityBlockUpdate.java', ROOT / 'src/main/java/com/hbm/world/WorldBlockState.java', ROOT / 'src/main/java/com/hbm/blocks/MultipassContext.java', PRIOR / 'NotificationTest.java', owner, branch]
    run([javac, '-encoding', 'UTF-8', '-cp', cp, '-d', output] + dependencies + [selected, HERE / 'PlayerChunkUpdateTest.java'])
    run([java, '-cp', runtime, 'PlayerChunkUpdateTest'], cwd=tmp)
    name = 'OriginalPlayerChunkTest'
    run([javac, '-cp', str(args.legacy_client) + os.pathsep + cp, '-d', output, HERE / (name + '.java')])
    for path in output.glob(name + '*.class'): relocate(path, name)
    run([java, '-cp', runtime + os.pathsep + str(args.legacy_client), 'proof.' + name], cwd=tmp)
    run([javac, '-cp', tools, '-d', output, HERE / 'PlayerChunkScanner.java'])
    inputs, classpath = tmp / 'inputs', tmp / 'classpath'
    classpath.write_text(cp)
    def scan(paths, mode, log):
        inputs.write_text('\n'.join(map(str, paths)) + '\n')
        result = run([java, '-Xmx3g', '-cp', str(output) + os.pathsep + str(tools), 'PlayerChunkScanner', inputs, classpath, mode], stdout=subprocess.PIPE, stderr=subprocess.PIPE, universal_newlines=True)
        (HERE / log).write_text(result.stdout + result.stderr)
        return result.stdout
    control = tmp / 'Controls.java'; control.write_bytes((HERE / 'controls.java.in').read_bytes())
    assert len(scan([control], 'old', 'old-controls.log').splitlines()) == 2
    native_controls = control.read_text().replace('.getPlayerManager()', '.getPlayerChunkMap()')
    native_controls = re.sub(r'\.markBlockForUpdate\(([^;]+)\);', r'.markBlockForUpdate(new net.minecraft.util.math.BlockPos(\1));', native_controls)
    control.write_text(native_controls)
    assert len(scan([control], 'native', 'native-controls.log').splitlines()) == 2
    print('PASS: two positives and eight foreign/subtype/boxed/wrong-arity/unresolved controls in both attribution modes')
    if args.attribute:
        paths = sorted((ROOT / 'src/main/java').rglob('*.java'))
        actual_rows = scan(paths, 'native', 'native-attribution.log')
        actual = {(str(Path(line.split('\t')[0]).relative_to(ROOT)), int(line.split('\t')[1])) for line in actual_rows.splitlines()}
        expected = set()
        for path, record in manifest['files'].items():
            prior_delta = 0
            for edit in record['edits']:
                prefix = (ROOT / path).read_bytes().decode()[:edit['start'] + prior_delta]
                expected.add((path, len(prefix.encode('utf-16-le')) // 2))
                prior_delta += len(edit['new']) - len(edit['old'])
        assert actual == expected, (actual, expected)
        original = tmp / 'original'
        for path in paths:
            relative = str(path.relative_to(ROOT)); destination = original / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(audit.original_bytes(relative, path.read_bytes()) if relative in manifest['files'] else path.read_bytes())
        rows = scan(sorted(original.rglob('*.java')), 'old', 'original-attribution.log')
        actual = {(str(Path(line.split('\t')[0]).relative_to(original)), int(line.split('\t')[1])) for line in rows.splitlines()}
        expected = {(path, len((original / path).read_bytes().decode()[:edit['start']].encode('utf-16-le')) // 2) for path, record in manifest['files'].items() for edit in record['edits']}
        assert actual == expected, (actual, expected)
        log = (ROOT / 'porting/checkpoint88/full-compile.log').read_text()
        for path, record in manifest['files'].items():
            for edit in record['edits']:
                diagnostic = '/build/sources/main/java/' + path.split('src/main/java/')[1] + ':' + str(edit['line']) + ': error: cannot find symbol\n'
                assert diagnostic in log, (path, edit['line'])
        print('PASS: all seven original typed server/int positions match CP88 missing-method diagnostics; all seven new getter/map/int constructor owners resolve')
    if args.mutants:
        mutations = [
            projection.replace(expressions[0], expressions[0].replace('BlockPos(x, y, z)', 'BlockPos(z, y, x)')),
            projection.replace(expressions[1], expressions[1].replace('this.pos.getY()', 'this.pos.getX()')),
            projection.replace(expressions[4], expressions[4].replace('pylon.getPos().getX(), pylon.getPos().getY(), pylon.getPos().getZ()', 'this.pos.getX(), this.pos.getY(), this.pos.getZ()')),
            projection.replace(expressions[0] + ';', expressions[0] + ';' + expressions[0] + ';'),
            projection.replace(expressions[0] + ';', 'world.getBlockState(new net.minecraft.util.math.BlockPos(x,y,z));' + expressions[0] + ';'),
        ]
        for index, expression in enumerate(expressions):
            line = next(line for line in projection.splitlines() if line.startswith(' void call' + str(index) + '('))
            mutations.append(projection.replace(line, line.replace(expression + ';', ';')))
        for index, mutation in enumerate(mutations):
            assert mutation != projection
            selected.write_text(mutation)
            run([javac, '-cp', runtime, '-d', output, selected])
            result = subprocess.run([str(java), '-cp', runtime, 'PlayerChunkUpdateTest'], cwd=tmp, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
            assert result.returncode != 0, ('surviving runtime mutation', index)
        print('PASS: twelve coordinate/peer/duplicate/state-read/no-op extracted-expression runtime mutations rejected')
        for path, record in manifest['files'].items():
            original = (ROOT / path).read_bytes()
            try:
                delta = 0
                for edit in record['edits']:
                    source = original.decode(); start = edit['start'] + delta
                    assert source[start:start + len(edit['new'])] == edit['new']
                    (ROOT / path).write_bytes((source[:start] + source[start + len(edit['new']):]).encode())
                    delta += len(edit['new']) - len(edit['old'])
                    try:
                        audit.verify(); raise RuntimeError('source deletion survived: ' + path)
                    except AssertionError: pass
            finally: (ROOT / path).write_bytes(original)
        print('PASS: seven individual production deletion mutants rejected; all source restored')
print('PASS: bounded native proof complete; no full enclosing owner build, launched game, wire transport or save/reload acceptance claimed')

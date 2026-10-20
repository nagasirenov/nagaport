#!/usr/bin/env python3
"""Static exact-change guard for this bounded migration, not a runtime test.
Uses checkpoint a1b1930 when available; otherwise verifies pinned reviewed source hashes.
An explicit first positional argument selects a git checkpoint (must exist).
"""
from pathlib import Path
import subprocess
import sys
root = Path(__file__).resolve().parents[2]
import hashlib
try:
    repo = Path(subprocess.check_output(['git', 'rev-parse', '--show-toplevel'], cwd=root, text=True, stderr=subprocess.DEVNULL).strip())
except subprocess.CalledProcessError:
    repo = None
ref = sys.argv[1] if len(sys.argv) > 1 else 'a1b1930'
paths = '''commands/CommandStations.java
dim/SolarSystemWorldSavedData.java
explosion/ExplosionNukeRayBatched.java
explosion/ExplosionNukeRayParallelized.java
handler/ImpactWorldHandler.java
handler/pollution/PollutionHandler.java
items/ItemVOTVdrive.java
tileentity/machine/TileEntityMachineStardar.java
tileentity/network/RequestNetwork.java
tileentity/network/TileEntityRequestNetwork.java
util/SubChunkKey.java
world/WorldUtil.java
world/feature/OreLayer3D.java'''.splitlines()
# Captured only after complete-source API-only parity passed against a1b1930.
# Newline-normalized fingerprints, not embedded copies of baseline source.
reviewed_hashes = {'commands/CommandStations.java': 'ec5fccc8197f19514072fd24366fb860965b6953d66c02e3e88803f6d0844785',
 'dim/SolarSystemWorldSavedData.java': 'fa99d76c36210846cd121afbcc2feca97197ee7d0e57f2c7de3dfaab08df8151',
 'explosion/ExplosionNukeRayBatched.java': 'af34d6212cbcfe4cdc3a6d7fa0c0787d220f1eb80679968f5a50899b83067aea',
 'explosion/ExplosionNukeRayParallelized.java': '204a95173aa27be8589ce5bac0e199028ed2c75d852a0a772f533043ebe9d8f0',
 'handler/ImpactWorldHandler.java': '445117dc5c741fb7dca92a4afa1d8d191b5583c141cda987484e150a78e46c53',
 'handler/pollution/PollutionHandler.java': 'bc8b2252d8c5bcf02a41ef5a7e12bbd24109b79dfbf4673a5241daa4ace60c14',
 'items/ItemVOTVdrive.java': 'ca40490b9cd8f18f62644018ad9e35a9ff80e0bab6a0ce62b0d04f5349e7635c',
 'tileentity/machine/TileEntityMachineStardar.java': 'b1051cd02b79dd75f9ba279b80ef66b48138266d884830770a7847d973fa6bca',
 'tileentity/network/RequestNetwork.java': 'd4911e6a58d9814b00d407c7ba0a30e41d8f8073397a255a78c3938a4642f598',
 'tileentity/network/TileEntityRequestNetwork.java': 'f06e8171740b5950028083be9b2f07a9b907b484741e98a286bd9cf55df6892a',
 'util/SubChunkKey.java': '39d7d28169101451eb5c11e9d975b913bdeab0d96ffe8e1a2f58f6a105da91ab',
 'world/WorldUtil.java': '4564d4782b87d014bc3426ba153997d265b25b41135b65e92048b2db4b376914',
 'world/feature/OreLayer3D.java': 'bdad8cc1f7fc17c29f109a821ea6a8d01231ea4679e0939e7b638c63369ab61c'}
has_reference = repo is not None and subprocess.run(
    ['git', 'cat-file', '-e', ref + '^{commit}'], cwd=repo,
    stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode == 0
if not has_reference:
    if len(sys.argv) > 1:
        raise SystemExit('Explicit git reference is unavailable: ' + ref)
    for relative in paths:
        source = (root / 'src/main/java/com/hbm' / relative).read_text()
        assert hashlib.sha256(source.encode()).hexdigest() == reviewed_hashes[relative], relative + ': differs from reviewed migration snapshot'
    print('PASS: 13 newline-normalized source fingerprints match the reviewed API-only migration snapshot. No fresh baseline comparison: checkpoint unavailable.')
    raise SystemExit(0)
for relative in paths:
    path = root / 'src/main/java/com/hbm' / relative
    old = subprocess.check_output(['git', 'show', ref + ':' + str(path.relative_to(repo))], cwd=repo).decode()
    expected = old.replace('import net.minecraft.world.ChunkCoordIntPair;', 'import net.minecraft.util.math.ChunkPos;')
    expected = expected.replace('ChunkCoordIntPair', 'ChunkPos').replace('getChunkPos()', 'getPos()')
    expected = expected.replace('.chunkXPos', '.x').replace('.chunkZPos', '.z').replace('ChunkPos.chunkXZ2Int', 'ChunkPos.asLong')
    if path.name == 'SubChunkKey.java':
        for a, b in [('this.x', 'this.chunkXPos'), ('this.z', 'this.chunkZPos'), ('k.x', 'k.chunkXPos'), ('k.z', 'k.chunkZPos')]:
            expected = expected.replace(a, b)
    if path.name == 'ImpactWorldHandler.java':
        expected = expected.replace('coord.getCenterXPos() - 8', 'coord.getXStart()').replace('coord.getCenterZPosition() - 8', 'coord.getZStart()')
    if path.name == 'WorldUtil.java':
        expected = expected.replace('import net.minecraft.util.math.ChunkPos;\n', '', 1)
    assert path.read_text() == expected.replace('\r\n', '\n'), relative + ': change outside verified native API substitutions'
print('PASS: 13 complete consumer sources match checkpoint except enumerated coordinate API changes; NBT keys, shifts/scales, RNG calls, ordering, station-ID formatting and algorithms unchanged. Static only.')

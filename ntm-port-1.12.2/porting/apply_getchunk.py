from pathlib import Path

files = [
    'src/main/java/com/hbm/commands/CommandDebugChunkLoad.java',
    'src/main/java/com/hbm/dim/CelestialBody.java',
    'src/main/java/com/hbm/entity/mob/glyphid/EntityGlyphidScout.java',
    'src/main/java/com/hbm/explosion/ExplosionNukeRayParallelized.java',
    'src/main/java/com/hbm/handler/radiation/ChunkRadiationHandlerNT.java',
    'src/main/java/com/hbm/handler/radiation/ChunkRadiationHandlerPRISM.java',
    'src/main/java/com/hbm/items/armor/ItemModLens.java',
    'src/main/java/com/hbm/main/ModEventHandlerImpact.java',
    'src/main/java/com/hbm/packet/toclient/BiomeSyncPacket.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityMachineRadarNT.java',
    'src/main/java/com/hbm/world/WorldUtil.java',
]

for fpath in files:
    p = Path(fpath)
    text = p.read_bytes().decode('utf-8')
    orig = text
    text = text.replace('getChunkFromChunkCoords', 'getChunk')
    if text != orig:
        crlf = text.replace('\r\n', '\n').replace('\n', '\r\n')
        p.write_bytes(crlf.encode('utf-8'))
        print(f'Updated getChunk in {fpath}')

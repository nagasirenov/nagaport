from pathlib import Path

files = [
    'src/main/java/com/hbm/dim/duna/biome/BiomeGenDunaHills.java',
    'src/main/java/com/hbm/dim/eve/ChunkProviderEve.java',
    'src/main/java/com/hbm/dim/mapgen/MapGenPlateau.java',
    'src/main/java/com/hbm/items/special/ItemBedrockOreBase.java',
    'src/main/java/com/hbm/world/feature/BiomeCave.java',
    'src/main/java/com/hbm/world/feature/OilSpot.java',
    'src/main/java/com/hbm/world/feature/OreCave.java',
    'src/main/java/com/hbm/world/feature/OreLayer.java',
    'src/main/java/com/hbm/world/feature/OreLayer3D.java',
    'src/main/java/com/hbm/world/feature/SchistStratum.java',
]

for fpath in files:
    p = Path(fpath)
    text = p.read_bytes().decode('utf-8')
    orig = text
    text = text.replace('func_151601_a', 'getValue')
    if 'ItemBedrockOreBase.java' in fpath:
        text = text.replace('MathHelper.clamp_double', 'MathHelper.clamp')
    if text != orig:
        crlf = text.replace('\r\n', '\n').replace('\n', '\r\n')
        p.write_bytes(crlf.encode('utf-8'))
        print(f'Updated func_151601_a in {fpath}')

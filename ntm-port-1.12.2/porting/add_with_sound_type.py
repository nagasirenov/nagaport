from pathlib import Path
import re

classes = [
    'BlockAmmoCrate',
    'BlockAshes',
    'BlockAstroturf',
    'BlockCrop',
    'BlockDirt',
    'BlockDummyable',
    'BlockFF',
    'BlockFallout',
    'BlockFurnaceSpace',
    'BlockGenericStairs',
    'BlockGeysierDCM',
    'BlockGeysir',
    'BlockGlyphid',
    'BlockGlyphidSpawner',
    'BlockJungleCrate',
    'BlockKelp',
    'BlockLayering',
    'BlockMassStorage',
    'BlockMush',
    'BlockMushHuge',
    'BlockNTMTrapdoor',
    'BlockPinkSlab',
    'BlockPlushie',
    'BlockRubberFarm',
    'BlockRubberLeaves',
    'BlockSellafield',
    'BlockSellafieldSlaked',
    'BlockTint',
    'BlockVinylVine',
    'BlockWaterPlant',
    'DecoBlock',
    'DemonLamp',
    'FluidPipeAnchor',
    'MeltedFlesh',
    'RubberGrass',
    'TritiumLamp',
    'WarController'
]

for c in sorted(classes):
    files = list(Path('src/main/java').rglob(f'{c}.java'))
    if not files:
        print(f'ERROR: File for {c} not found!')
        continue
    p = files[0]
    text = p.read_bytes().decode('utf-8')
    if 'withSoundType(' in text:
        print(f'{c} already has withSoundType')
        continue

    # Find the class declaration
    pattern = re.compile(r'(public\s+(?:abstract\s+)?class\s+' + c + r'\b[^{]*\{)', re.M)
    m = pattern.search(text)
    if not m:
        print(f'ERROR: Could not match class decl for {c}')
        continue

    if c == 'BlockDummyable':
        method = '\r\n\tpublic <T extends BlockDummyable> T withSoundType(net.minecraft.block.SoundType sound) {\r\n\t\tsuper.setSoundType(sound);\r\n\t\treturn (T) this;\r\n\t}\r\n'
    elif c == 'BlockGeysir':
        method = '\r\n\tpublic <T extends BlockGeysir> T withSoundType(net.minecraft.block.SoundType sound) {\r\n\t\tsuper.setSoundType(sound);\r\n\t\treturn (T) this;\r\n\t}\r\n'
    elif c == 'BlockDirt':
        method = '\r\n\tpublic <T extends BlockDirt> T withSoundType(net.minecraft.block.SoundType sound) {\r\n\t\tsuper.setSoundType(sound);\r\n\t\treturn (T) this;\r\n\t}\r\n'
    else:
        method = f'\r\n\tpublic {c} withSoundType(net.minecraft.block.SoundType sound) {{\r\n\t\tsuper.setSoundType(sound);\r\n\t\treturn this;\r\n\t}}\r\n'

    idx = m.end()
    text = text[:idx] + method + text[idx:]
    crlf_text = text.replace('\r\n', '\n').replace('\n', '\r\n')
    p.write_bytes(crlf_text.encode('utf-8'))
    print(f'Added withSoundType to {c}')

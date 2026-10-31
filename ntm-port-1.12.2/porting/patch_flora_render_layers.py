import os

base = r'C:\Users\nagas\Desktop\port\ntm-port-1.12.2'

targets = [
    r'src\main\java\com\hbm\blocks\generic\BlockNTMFlower.java',
    r'src\main\java\com\hbm\blocks\generic\BlockDeadPlant.java',
    r'src\main\java\com\hbm\blocks\generic\BlockTallPlant.java',
    r'src\main\java\com\hbm\blocks\generic\BlockRubberPlant.java',
    r'src\main\java\com\hbm\blocks\BlockWaterPlant.java',
    r'src\main\java\com\hbm\blocks\BlockCrop.java',
    r'src\main\java\com\hbm\blocks\BlockNTSapling.java',
    r'src\main\java\com\hbm\blocks\generic\BlockMush.java',
    r'src\main\java\com\hbm\blocks\generic\BlockReeds.java',
    r'src\main\java\com\hbm\blocks\generic\BlockGrate.java',
    r'src\main\java\com\hbm\blocks\generic\BarbedWire.java',
    r'src\main\java\com\hbm\blocks\generic\BlockChain.java',
    r'src\main\java\com\hbm\blocks\generic\BlockNTMLadder.java',
    r'src\main\java\com\hbm\blocks\generic\BlockNTMGlass.java',
    r'src\main\java\com\hbm\blocks\generic\BlockNTMGlassPane.java',
    r'src\main\java\com\hbm\blocks\generic\BlockNTMGlassCT.java',
    r'src\main\java\com\hbm\blocks\machine\BlockCMGlass.java',
    r'src\main\java\com\hbm\blocks\generic\BlockRubberCacti.java',
]

for rel in targets:
    fp = os.path.join(base, rel)
    with open(fp, 'rb') as f:
        c = f.read()
    
    if b'getBlockLayer()' in c:
        c = c.replace(b'getBlockLayer()', b'getRenderLayer()')
        with open(fp, 'wb') as f:
            f.write(c)
        print('Updated getBlockLayer -> getRenderLayer in:', rel)

print('All classes updated.')

from pathlib import Path

files = [
    'src/main/java/com/hbm/blocks/BlockRubberFarm.java',
    'src/main/java/com/hbm/blocks/bomb/Balefire.java',
    'src/main/java/com/hbm/blocks/generic/BlockPlasma.java',
    'src/main/java/com/hbm/dim/WorldProviderCelestial.java',
    'src/main/java/com/hbm/entity/mob/EntityCreeperNuclear.java',
    'src/main/java/com/hbm/entity/mob/EntityCreeperTainted.java',
    'src/main/java/com/hbm/main/ModEventHandler.java',
    'src/main/java/com/hbm/tileentity/machine/rbmk/RBMKDials.java',
    'src/main/java/com/hbm/util/GameRuleHelper.java',
]

for fpath in files:
    p = Path(fpath)
    text = p.read_bytes().decode('utf-8')
    orig = text
    text = text.replace('getGameRuleBooleanValue', 'getBoolean')
    text = text.replace('getGameRuleStringValue', 'getString')
    if 'GameRuleHelper.java' in fpath:
        text = text.replace('MathHelper.clamp_double', 'MathHelper.clamp')
        text = text.replace('MathHelper.clamp_int', 'MathHelper.clamp')
    if text != orig:
        crlf = text.replace('\r\n', '\n').replace('\n', '\r\n')
        p.write_bytes(crlf.encode('utf-8'))
        print(f'Updated GameRules in {fpath}')

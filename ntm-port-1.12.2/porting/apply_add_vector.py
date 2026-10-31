from pathlib import Path
import re

files = [
    'src/main/java/com/hbm/dim/SolarSystem.java',
    'src/main/java/com/hbm/entity/mob/ai/EntityAIBreaking.java',
    'src/main/java/com/hbm/entity/train/EntityRailCarBase.java',
    'src/main/java/com/hbm/handler/ability/IToolAreaAbility.java',
    'src/main/java/com/hbm/handler/radiation/ChunkRadiationHandlerNT.java',
    'src/main/java/com/hbm/items/tool/ItemBoatRubber.java',
    'src/main/java/com/hbm/lib/Library.java',
    'src/main/java/com/hbm/render/entity/item/RenderMinecartTest.java',
    'src/main/java/com/hbm/render/entity/item/RenderNeoCart.java',
    'src/main/java/com/hbm/render/entity/mob/RenderSiegeCraft.java',
    'src/main/java/com/hbm/tileentity/machine/TileEntityMachineDrain.java',
    'src/main/java/com/hbm/tileentity/network/TileEntityPipelineBase.java',
    'src/main/java/com/hbm/tileentity/network/TileEntityPylonBase.java',
    'src/main/java/com/hbm/tileentity/turret/TileEntityTurretArty.java',
    'src/main/java/com/hbm/util/EntityDamageUtil.java',
]

for fpath in files:
    p = Path(fpath)
    text = p.read_bytes().decode('utf-8')
    orig = text
    text = text.replace('.addVector(', '.add(')
    if text != orig:
        crlf = text.replace('\r\n', '\n').replace('\n', '\r\n')
        p.write_bytes(crlf.encode('utf-8'))
        print(f'Updated addVector in {fpath}')

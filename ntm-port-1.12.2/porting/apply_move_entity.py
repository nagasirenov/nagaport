from pathlib import Path
import re

files = [
    'src/main/java/com/hbm/entity/effect/EntityMist.java',
    'src/main/java/com/hbm/entity/item/EntityBoatRubber.java',
    'src/main/java/com/hbm/entity/item/EntityDroneBase.java',
    'src/main/java/com/hbm/entity/item/EntityFireworks.java',
    'src/main/java/com/hbm/entity/item/EntityMinecartDeobf.java',
    'src/main/java/com/hbm/entity/item/EntityMinecartModBase.java',
    'src/main/java/com/hbm/entity/item/EntityTNTPrimedBase.java',
    'src/main/java/com/hbm/entity/mob/botprime/EntityBurrowingNT.java',
    'src/main/java/com/hbm/entity/mob/EntityBurrowingBase.java',
    'src/main/java/com/hbm/entity/mob/EntityFish.java',
    'src/main/java/com/hbm/entity/mob/EntityPlasticBag.java',
    'src/main/java/com/hbm/entity/mob/glyphid/EntityGlyphidBrenda.java',
    'src/main/java/com/hbm/entity/particle/EntityModFX.java',
    'src/main/java/com/hbm/entity/projectile/EntityDebrisBase.java',
    'src/main/java/com/hbm/entity/projectile/EntityMeteor.java',
    'src/main/java/com/hbm/entity/projectile/EntityRBMKDebris.java',
    'src/main/java/com/hbm/entity/projectile/EntityZirnoxDebris.java',
    'src/main/java/com/hbm/entity/train/EntityRailCarBase.java',
]

for fpath in files:
    p = Path(fpath)
    text = p.read_bytes().decode('utf-8')
    orig = text

    if fpath == 'src/main/java/com/hbm/entity/effect/EntityMist.java':
        text = text.replace('@Override public void moveEntity(double x, double y, double z) { }',
                            '@Override public void move(net.minecraft.entity.MoverType type, double x, double y, double z) { }')
    elif fpath == 'src/main/java/com/hbm/entity/projectile/EntityDebrisBase.java':
        text = text.replace('public void moveEntity(double moX, double moY, double moZ) {',
                            '@Override public void move(net.minecraft.entity.MoverType type, double moX, double moY, double moZ) {')
        text = text.replace('super.moveEntity(moX, moY, moZ);',
                            'super.move(type, moX, moY, moZ);')
    else:
        # replace caller sites: .moveEntity(...) or moveEntity(...)
        # e.g. this.moveEntity(x, y, z) -> this.move(net.minecraft.entity.MoverType.SELF, x, y, z)
        # e.g. moveEntity(x, y, z) -> move(net.minecraft.entity.MoverType.SELF, x, y, z)
        # e.g. glyphid.moveEntity(x, y, z) -> glyphid.move(net.minecraft.entity.MoverType.SELF, x, y, z)
        text = re.sub(r'\bmoveEntity\(', 'move(net.minecraft.entity.MoverType.SELF, ', text)

    if text != orig:
        crlf = text.replace('\r\n', '\n').replace('\n', '\r\n')
        p.write_bytes(crlf.encode('utf-8'))
        print(f'Updated moveEntity in {fpath}')

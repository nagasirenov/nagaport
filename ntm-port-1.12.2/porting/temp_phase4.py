from pathlib import Path

# 1. ItemBoatRubber.java
p = Path('src/main/java/com/hbm/items/tool/ItemBoatRubber.java')
raw = p.read_bytes()
assert b'axisalignedbb.isVecInside(pos)' in raw
raw = raw.replace(b'axisalignedbb.isVecInside(pos)', b'axisalignedbb.contains(pos)')
p.write_bytes(raw)
print('ItemBoatRubber updated.')

# 2. EntityDamageUtil.java
p = Path('src/main/java/com/hbm/util/EntityDamageUtil.java')
raw = p.read_bytes()
assert b'axisalignedbb.isVecInside(pos)' in raw
raw = raw.replace(b'axisalignedbb.isVecInside(pos)', b'axisalignedbb.contains(pos)')
p.write_bytes(raw)
print('EntityDamageUtil updated.')

# 3. EntityBullet.java
p = Path('src/main/java/com/hbm/entity/projectile/EntityBullet.java')
raw = p.read_bytes()
old_code = (
    b'\t\tBlock block = com.hbm.world.WorldBlockState.read(this.world, this.tileX, this.tileY, this.tileZ).getBlock();\r\n'
    b'\r\n'
    b'\t\tif (block.getDefaultState().getMaterial() != Material.AIR) {\r\n'
    b'\t\t\tblock.setBlockBoundsBasedOnState(this.world, this.tileX, this.tileY,\r\n'
    b'\t\t\t\t\tthis.tileZ);\r\n'
    b'\t\t\tAxisAlignedBB axisalignedbb = block.getCollisionBoundingBoxFromPool(this.world, this.tileX,\r\n'
    b'\t\t\t\t\tthis.tileY, this.tileZ);\r\n'
    b'\r\n'
    b'\t\t\tif (axisalignedbb != null\r\n'
    b'\t\t\t\t\t&& axisalignedbb.isVecInside(new Vec3d(this.posX, this.posY, this.posZ))\r\n'
    b'\t\t\t\t\t&& !this.getIsCritical()) {\r\n'
    b'\t\t\t\tthis.inGround = true;\r\n'
    b'\t\t\t}'
)
new_code = (
    b'\t\tnet.minecraft.util.math.BlockPos hitPos = new net.minecraft.util.math.BlockPos(this.tileX, this.tileY, this.tileZ);\r\n'
    b'\t\tnet.minecraft.block.state.IBlockState state = this.world.getBlockState(hitPos);\r\n'
    b'\r\n'
    b'\t\tif (state.getMaterial() != Material.AIR) {\r\n'
    b'\t\t\tAxisAlignedBB axisalignedbb = state.getCollisionBoundingBox(this.world, hitPos);\r\n'
    b'\r\n'
    b'\t\t\tif (axisalignedbb != null\r\n'
    b'\t\t\t\t\t&& axisalignedbb.offset(hitPos).contains(new Vec3d(this.posX, this.posY, this.posZ))\r\n'
    b'\t\t\t\t\t&& !this.getIsCritical()) {\r\n'
    b'\t\t\t\tthis.inGround = true;\r\n'
    b'\t\t\t}'
)
assert old_code in raw, 'EntityBullet old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('EntityBullet updated.')

# 4. EntityRocket.java
p = Path('src/main/java/com/hbm/entity/projectile/EntityRocket.java')
raw = p.read_bytes()
old_code = (
    b'        Block block = com.hbm.world.WorldBlockState.read(this.world, this.field_145791_d, this.field_145792_e, this.field_145789_f).getBlock();\r\n'
    b'\r\n'
    b'        if (block.getDefaultState().getMaterial() != Material.AIR)\r\n'
    b'        {\r\n'
    b'            block.setBlockBoundsBasedOnState(this.world, this.field_145791_d, this.field_145792_e, this.field_145789_f);\r\n'
    b'            AxisAlignedBB axisalignedbb = block.getCollisionBoundingBoxFromPool(this.world, this.field_145791_d, this.field_145792_e, this.field_145789_f);\r\n'
    b'\r\n'
    b'            if (axisalignedbb != null && axisalignedbb.isVecInside(new Vec3d(this.posX, this.posY, this.posZ)))\r\n'
    b'            {\r\n'
    b'                this.inGround = true;\r\n'
    b'            }\r\n'
    b'        }'
)
new_code = (
    b'        net.minecraft.util.math.BlockPos hitPos = new net.minecraft.util.math.BlockPos(this.field_145791_d, this.field_145792_e, this.field_145789_f);\r\n'
    b'        net.minecraft.block.state.IBlockState state = this.world.getBlockState(hitPos);\r\n'
    b'\r\n'
    b'        if (state.getMaterial() != Material.AIR)\r\n'
    b'        {\r\n'
    b'            AxisAlignedBB axisalignedbb = state.getCollisionBoundingBox(this.world, hitPos);\r\n'
    b'\r\n'
    b'            if (axisalignedbb != null && axisalignedbb.offset(hitPos).contains(new Vec3d(this.posX, this.posY, this.posZ)))\r\n'
    b'            {\r\n'
    b'                this.inGround = true;\r\n'
    b'            }\r\n'
    b'        }'
)
assert old_code in raw, 'EntityRocket old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('EntityRocket updated.')

# 5. EntitySchrab.java
p = Path('src/main/java/com/hbm/entity/projectile/EntitySchrab.java')
raw = p.read_bytes()
assert old_code in raw, 'EntitySchrab old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('EntitySchrab updated.')

# 6. EntityExplosiveBeam.java
p = Path('src/main/java/com/hbm/entity/projectile/EntityExplosiveBeam.java')
raw = p.read_bytes()
old_code = (
    b'        Block block = com.hbm.world.WorldBlockState.read(this.world, this.field_145791_d, this.field_145792_e, this.field_145789_f).getBlock();\r\n'
    b'\r\n'
    b'        if (block.getDefaultState().getMaterial() != Material.AIR)\r\n'
    b'        {\r\n'
    b'            block.setBlockBoundsBasedOnState(this.world, this.field_145791_d, this.field_145792_e, this.field_145789_f); \r\n'
    b'    \t\tthis.setDead();\r\n'
    b'    \t\texplode();\r\n'
    b'        }'
)
new_code = (
    b'        net.minecraft.util.math.BlockPos hitPos = new net.minecraft.util.math.BlockPos(this.field_145791_d, this.field_145792_e, this.field_145789_f);\r\n'
    b'        net.minecraft.block.state.IBlockState state = this.world.getBlockState(hitPos);\r\n'
    b'\r\n'
    b'        if (state.getMaterial() != Material.AIR)\r\n'
    b'        {\r\n'
    b'    \t\tthis.setDead();\r\n'
    b'    \t\texplode();\r\n'
    b'        }'
)
assert old_code in raw, 'EntityExplosiveBeam old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('EntityExplosiveBeam updated.')

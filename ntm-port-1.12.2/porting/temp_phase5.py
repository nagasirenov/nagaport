from pathlib import Path

# 1. Machine maxY removals
for fname in ['MachineCentrifuge.java', 'MachineCrucible.java', 'MachineGasCent.java', 'MachineGasFlare.java', 'MachineSawmill.java']:
    p = list(Path('src/main/java').rglob(fname))[0]
    raw = p.read_bytes()
    assert b'this.maxY = 0.999D;' in raw, f'maxY not found in {fname}'
    raw = raw.replace(b'\t\tthis.maxY = 0.999D; //item bounce prevention\r\n', b'')
    p.write_bytes(raw)
    print(f'{fname} updated.')

# 2. y + this.maxY replacements
p = list(Path('src/main/java').rglob('MachineConveyorPress.java'))[0]
raw = p.read_bytes()
assert b'y + this.maxY' in raw
raw = raw.replace(b'y + this.maxY', b'y + 1.0')
p.write_bytes(raw)
print('MachineConveyorPress updated.')

p = list(Path('src/main/java').rglob('MachineStrandCaster.java'))[0]
raw = p.read_bytes()
assert b'y + this.maxY' in raw
raw = raw.replace(b'y + this.maxY', b'y + 1.0')
p.write_bytes(raw)
print('MachineStrandCaster updated.')

p = list(Path('src/main/java').rglob('VolcanicBlock.java'))[0]
raw = p.read_bytes()
assert b'dy = (double) y + this.maxY;' in raw
raw = raw.replace(b'dy = (double) y + this.maxY;', b'dy = (double) y + 1.0;')
p.write_bytes(raw)
print('VolcanicBlock updated.')

# 3. BlockRubberLeaves.java
p = list(Path('src/main/java').rglob('BlockRubberLeaves.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t@Override\r\n'
    b'\t@SideOnly(Side.CLIENT)\r\n'
    b'\tpublic boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {\r\n'
    b'\t\treturn side == 0 && this.minY > 0.0D ? true : (side == 1 && this.maxY < 1.0D ? true : (side == 2 && this.minZ > 0.0D ? true : (side == 3 && this.maxZ < 1.0D ? true : (side == 4 && this.minX > 0.0D ? true : (side == 5 && this.maxX < 1.0D ? true : !com.hbm.world.WorldBlockState.read(world, x, y, z).isOpaqueCube())))));\r\n'
    b'\t}\r\n'
)
assert old_code in raw, 'BlockRubberLeaves old code not found'
raw = raw.replace(old_code, b'')
p.write_bytes(raw)
print('BlockRubberLeaves updated.')

# 4. BlockGasClorine.java
p = list(Path('src/main/java').rglob('BlockGasClorine.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t\treturn p_149646_5_ == 0 && this.minY > 0.0D ? true : (p_149646_5_ == 1 && this.maxY < 1.0D ? true : (p_149646_5_ == 2 && this.minZ > 0.0D ? true : (p_149646_5_ == 3 && this.maxZ < 1.0D ? true : (p_149646_5_ == 4 && this.minX > 0.0D ? true : (p_149646_5_ == 5 && this.maxX < 1.0D ? true : !com.hbm.world.WorldBlockState.read(p_149646_1_, p_149646_2_, p_149646_3_, p_149646_4_).isOpaqueCube())))));'
)
new_code = (
    b'\t\treturn !com.hbm.world.WorldBlockState.read(p_149646_1_, p_149646_2_, p_149646_3_, p_149646_4_).isOpaqueCube();'
)
assert old_code in raw, 'BlockGasClorine old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('BlockGasClorine updated.')

# 5. BlockBobble.java
p = list(Path('src/main/java').rglob('BlockBobble.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {\r\n'
    b'\t\tfloat f = 0.0625F;\r\n'
    b'\t\tthis.setBlockBounds(5.5F * f, 0.0F, 5.5F * f, 1.0F - 5.5F * f, 0.625F, 1.0F - 5.5F * f);\r\n'
    b'\t}\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'\t\tthis.setBlockBoundsBasedOnState(world, x, y, z);\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\tprivate static final AxisAlignedBB BOUNDS = new AxisAlignedBB(5.5F * 0.0625F, 0.0F, 5.5F * 0.0625F, 1.0F - 5.5F * 0.0625F, 0.625F, 1.0F - 5.5F * 0.0625F);\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\treturn BOUNDS;\r\n'
    b'\t}'
)
assert old_code in raw, 'BlockBobble old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('BlockBobble updated.')

# 6. BlockSnowglobe.java
p = list(Path('src/main/java').rglob('BlockSnowglobe.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {\r\n'
    b'\t\tfloat f = 0.0625F;\r\n'
    b'\t\tthis.setBlockBounds(4F * f, 0.0F, 4F * f, 1.0F - 4F * f, 0.3125F, 1.0F - 4F * f);\r\n'
    b'\t}\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'\t\tthis.setBlockBoundsBasedOnState(world, x, y, z);\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\tprivate static final AxisAlignedBB BOUNDS = new AxisAlignedBB(4F * 0.0625F, 0.0F, 4F * 0.0625F, 1.0F - 4F * 0.0625F, 0.3125F, 1.0F - 4F * 0.0625F);\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\treturn BOUNDS;\r\n'
    b'\t}'
)
assert old_code in raw, 'BlockSnowglobe old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('BlockSnowglobe updated.')

# 7. BlockLoot.java
p = list(Path('src/main/java').rglob('BlockLoot.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {\r\n'
    b'\t\tthis.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);\r\n'
    b'\t}\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'\t\tthis.setBlockBoundsBasedOnState(world, x, y, z);\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\tprivate static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\treturn BOUNDS;\r\n'
    b'\t}'
)
assert old_code in raw, 'BlockLoot old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('BlockLoot updated.')

# 8. BombMulti.java
p = list(Path('src/main/java').rglob('BombMulti.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_) {\r\n'
    b'\t\tfloat f = 0.0625F;\r\n'
    b'\t\tthis.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 8 * f, 1.0F);\r\n'
    b'\t}\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'\t\tfloat f = 0.0625F;\r\n'
    b'\t\tthis.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 8 * f, 1.0F);\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\tprivate static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 8 * 0.0625F, 1.0F);\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\treturn BOUNDS;\r\n'
    b'\t}'
)
assert old_code in raw, 'BombMulti old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('BombMulti updated.')

# 9. CompactLauncher.java
p = list(Path('src/main/java').rglob('CompactLauncher.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_) {\r\n'
    b'\t\tthis.setBlockBounds(0, 1, 0, 1, 1, 1);\r\n'
    b'\t}\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'\r\n'
    b'\t\tthis.setBlockBounds(0, 1, 0, 1, 1, 1);\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\tprivate static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0, 1, 0, 1, 1, 1);\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\treturn BOUNDS;\r\n'
    b'\t}'
)
assert old_code in raw, 'CompactLauncher old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('CompactLauncher updated.')

# 10. DecoBlockAlt.java
p = list(Path('src/main/java').rglob('DecoBlockAlt.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_) {\r\n'
    b'\t\tfloat f = 0.0625F;\r\n'
    b'\t\tthis.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 8 * f, 1.0F);\r\n'
    b'\t}\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'\t\tfloat f = 0.0625F;\r\n'
    b'\t\tthis.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 8 * f, 1.0F);\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\tprivate static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 8 * 0.0625F, 1.0F);\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\treturn BOUNDS;\r\n'
    b'\t}'
)
assert old_code in raw, 'DecoBlockAlt old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('DecoBlockAlt updated.')

# 11. MachineSatDock.java
p = list(Path('src/main/java').rglob('MachineSatDock.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_)\r\n'
    b'    {\r\n'
    b'        float f = 0.0625F;\r\n'
    b'        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 12*f, 1.0F);\r\n'
    b'    }\r\n'
    b'\t\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'        float f = 0.0625F;\r\n'
    b'        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 12*f, 1.0F);\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\tprivate static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 12 * 0.0625F, 1.0F);\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\treturn BOUNDS;\r\n'
    b'\t}'
)
assert old_code in raw, 'MachineSatDock old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('MachineSatDock updated.')

# 12. MachineGasDock.java
p = list(Path('src/main/java').rglob('MachineGasDock.java'))[0]
raw = p.read_bytes()
if b'import net.minecraft.block.state.IBlockState;' not in raw:
    raw = raw.replace(b'import net.minecraft.block.BlockContainer;', b'import net.minecraft.block.BlockContainer;\r\nimport net.minecraft.block.state.IBlockState;')
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_) {\r\n'
    b'\t\tfloat f = 0.0625F;\r\n'
    b'\t\tthis.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 12*f, 1.0F);\r\n'
    b'\t}\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'\t\tfloat f = 0.0625F;\r\n'
    b'\t\tthis.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 12*f, 1.0F);\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\tprivate static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 12 * 0.0625F, 1.0F);\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\treturn BOUNDS;\r\n'
    b'\t}'
)
assert old_code in raw, 'MachineGasDock old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('MachineGasDock updated.')

# 13. StorageDrum.java
p = list(Path('src/main/java').rglob('StorageDrum.java'))[0]
raw = p.read_bytes()
if b'import net.minecraft.util.math.BlockPos;' not in raw:
    raw = raw.replace(b'import net.minecraft.block.material.Material;', b'import net.minecraft.block.material.Material;\r\nimport net.minecraft.util.math.BlockPos;')
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_) {\r\n'
    b'\t\tfloat f = 0.0625F;\r\n'
    b'\t\tthis.setBlockBounds(2 * f, 0.0F, 2 * f, 14 * f, 1.0F, 14 * f);\r\n'
    b'\t}\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'\t\tfloat f = 0.0625F;\r\n'
    b'\t\tthis.setBlockBounds(2 * f, 0.0F, 2 * f, 14 * f, 1.0F, 14 * f);\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\tprivate static final AxisAlignedBB BOUNDS = new AxisAlignedBB(2 * 0.0625F, 0.0F, 2 * 0.0625F, 14 * 0.0625F, 1.0F, 14 * 0.0625F);\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\treturn BOUNDS;\r\n'
    b'\t}'
)
assert old_code in raw, 'StorageDrum old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('StorageDrum updated.')

# 14. TurretBaseNT.java
p = list(Path('src/main/java').rglob('TurretBaseNT.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_) {\r\n'
    b'\t\tthis.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);\r\n'
    b'\t}\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'\t\tthis.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\tprivate static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\treturn BOUNDS;\r\n'
    b'\t}'
)
assert old_code in raw, 'TurretBaseNT old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('TurretBaseNT updated.')

# 15. BlockDynamicSlag.java
p = list(Path('src/main/java').rglob('BlockDynamicSlag.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {\r\n'
    b'\t\tTileEntitySlag tile = (TileEntitySlag) world.getTileEntity(x, y, z);\r\n'
    b'\t\tif(tile != null) {\r\n'
    b'\t\t\tthis.setBlockBounds(0F, 0F, 0F, 1F, (float) tile.amount / (float) TileEntitySlag.maxAmount, 1F);\r\n'
    b'\t\t}\r\n'
    b'\t}\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'\t\tTileEntitySlag tile = (TileEntitySlag) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));\r\n'
    b'\t\tif(tile != null) {\r\n'
    b'\t\t\tthis.setBlockBounds(0F, 0F, 0F, 1F, (float) tile.amount / (float) TileEntitySlag.maxAmount, 1F);\r\n'
    b'\t\t}\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\tTileEntitySlag tile = (TileEntitySlag) world.getTileEntity(pos);\r\n'
    b'\t\tif(tile != null) {\r\n'
    b'\t\t\treturn new AxisAlignedBB(0F, 0F, 0F, 1F, (float) tile.amount / (float) TileEntitySlag.maxAmount, 1F);\r\n'
    b'\t\t}\r\n'
    b'\t\treturn FULL_BLOCK_AABB;\r\n'
    b'\t}'
)
assert old_code in raw, 'BlockDynamicSlag old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('BlockDynamicSlag updated.')

# 16. Landmine.java
p = list(Path('src/main/java').rglob('Landmine.java'))[0]
raw = p.read_bytes()
old_code = (
    b'\t@Override\r\n'
    b'\tpublic void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {\r\n'
    b'\t\tfloat f = 0.0625F;\r\n'
    b'\t\tif(this == ModBlocks.mine_ap) this.setBlockBounds(5 * f, 0.0F, 5 * f, 11 * f, 1 * f, 11 * f);\r\n'
    b'\t\tif(this == ModBlocks.mine_he) this.setBlockBounds(4 * f, 0.0F, 4 * f, 12 * f, 2 * f, 12 * f);\r\n'
    b'\t\tif(this == ModBlocks.mine_shrap) this.setBlockBounds(5 * f, 0.0F, 5 * f, 11 * f, 1 * f, 11 * f);\r\n'
    b'\t\tif(this == ModBlocks.mine_fat) this.setBlockBounds(5 * f, 0.0F, 4 * f, 11 * f, 6 * f, 12 * f);}\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {\r\n'
    b'\t\tsetBlockBoundsBasedOnState(world, x, y, z);\r\n'
    b'\t\treturn new AxisAlignedBB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);\r\n'
    b'\t}'
)
new_code = (
    b'\tprivate static final AxisAlignedBB BOUNDS_AP = new AxisAlignedBB(5 * 0.0625F, 0.0F, 5 * 0.0625F, 11 * 0.0625F, 1 * 0.0625F, 11 * 0.0625F);\r\n'
    b'\tprivate static final AxisAlignedBB BOUNDS_HE = new AxisAlignedBB(4 * 0.0625F, 0.0F, 4 * 0.0625F, 12 * 0.0625F, 2 * 0.0625F, 12 * 0.0625F);\r\n'
    b'\tprivate static final AxisAlignedBB BOUNDS_SHRAP = new AxisAlignedBB(5 * 0.0625F, 0.0F, 5 * 0.0625F, 11 * 0.0625F, 1 * 0.0625F, 11 * 0.0625F);\r\n'
    b'\tprivate static final AxisAlignedBB BOUNDS_FAT = new AxisAlignedBB(5 * 0.0625F, 0.0F, 4 * 0.0625F, 11 * 0.0625F, 6 * 0.0625F, 12 * 0.0625F);\r\n'
    b'\r\n'
    b'\t@Override\r\n'
    b'\tpublic AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {\r\n'
    b'\t\tif(this == ModBlocks.mine_ap) return BOUNDS_AP;\r\n'
    b'\t\tif(this == ModBlocks.mine_he) return BOUNDS_HE;\r\n'
    b'\t\tif(this == ModBlocks.mine_shrap) return BOUNDS_SHRAP;\r\n'
    b'\t\tif(this == ModBlocks.mine_fat) return BOUNDS_FAT;\r\n'
    b'\t\treturn FULL_BLOCK_AABB;\r\n'
    b'\t}'
)
assert old_code in raw, 'Landmine old code not found'
raw = raw.replace(old_code, new_code)
p.write_bytes(raw)
print('Landmine updated.')

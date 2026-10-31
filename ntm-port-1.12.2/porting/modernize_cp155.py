import os

def update_schrabidic():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\blocks\fluid\SchrabidicBlock.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    # Imports
    text = text.replace("import net.minecraft.world.World;",
                        """import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;""")

    # Remove @Override on getIcon and registerBlockIcons
    text = text.replace("""\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic IIcon getIcon(int side, int meta) {""",
                        """\t@SideOnly(Side.CLIENT)
\tpublic IIcon getIcon(int side, int meta) {""")

    text = text.replace("""\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic void registerBlockIcons(IIconRegister register) {""",
                        """\t@SideOnly(Side.CLIENT)
\tpublic void registerBlockIcons(IIconRegister register) {""")

    # canDisplace & displaceIfPossible
    old_displace = """\t@Override
\tpublic boolean canDisplace(IBlockAccess world, int x, int y, int z) {

\t\tif (com.hbm.world.WorldBlockState.read(world, x, y, z).getMaterial().isLiquid()) {
\t\t\treturn false;
\t\t}
\t\treturn super.canDisplace(world, x, y, z);
\t}

\t@Override
\tpublic boolean displaceIfPossible(World world, int x, int y, int z) {

\t\tif (com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock().getDefaultState().getMaterial().isLiquid()) {
\t\t\treturn false;
\t\t}
\t\treturn super.displaceIfPossible(world, x, y, z);
\t}"""

    new_displace = """\t@Override
\tpublic boolean canDisplace(IBlockAccess world, BlockPos pos) {
\t\tif (world.getBlockState(pos).getMaterial().isLiquid()) {
\t\t\treturn false;
\t\t}
\t\treturn super.canDisplace(world, pos);
\t}

\tpublic boolean canDisplace(IBlockAccess world, int x, int y, int z) {
\t\treturn canDisplace(world, new BlockPos(x, y, z));
\t}

\t@Override
\tpublic boolean displaceIfPossible(World world, BlockPos pos) {
\t\tif (world.getBlockState(pos).getMaterial().isLiquid()) {
\t\t\treturn false;
\t\t}
\t\treturn super.displaceIfPossible(world, pos);
\t}

\tpublic boolean displaceIfPossible(World world, int x, int y, int z) {
\t\treturn displaceIfPossible(world, new BlockPos(x, y, z));
\t}"""
    assert old_displace in text, "schrabidic old_displace"
    text = text.replace(old_displace, new_displace, 1)

    # playSound calls
    text = text.replace('world.playSound((double)x,(double)y,(double)z,"random.fizz", 0.2F, 1F,false);',
                        'world.playSound(null, (double)x, (double)y, (double)z, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.2F, 1.0F);')

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("SchrabidicBlock updated")


def update_volcanic():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\blocks\fluid\VolcanicBlock.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    text = text.replace("import net.minecraft.world.World;",
                        """import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;""")

    # Remove @Override on getIcon and registerBlockIcons
    text = text.replace("""\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic IIcon getIcon(int side, int meta) {""",
                        """\t@SideOnly(Side.CLIENT)
\tpublic IIcon getIcon(int side, int meta) {""")

    text = text.replace("""\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic void registerBlockIcons(IIconRegister register) {""",
                        """\t@SideOnly(Side.CLIENT)
\tpublic void registerBlockIcons(IIconRegister register) {""")

    # isSourceBlock
    text = text.replace("!this.isSourceBlock(world, x, y, z)",
                        "!this.isSourceBlock(world, new BlockPos(x, y, z))")

    # canDisplace & displaceIfPossible
    old_displace = """\t@Override
\tpublic boolean canDisplace(IBlockAccess world, int x, int y, int z) {
\t\tBlock b = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();

\t\tif(Blocks.FIRE.getFlammability(b) > 0)
\t\t\treturn true;

\t\tif(b.isReplaceable(world, new net.minecraft.util.math.BlockPos(x, y, z)))
\t\t\treturn true;

\t\treturn super.canDisplace(world, x, y, z);
\t}

\t@Override
\tpublic boolean displaceIfPossible(World world, int x, int y, int z) {
\t\treturn super.displaceIfPossible(world, x, y, z) || canDisplace(world, x, y, z);
\t}"""

    new_displace = """\t@Override
\tpublic boolean canDisplace(IBlockAccess world, BlockPos pos) {
\t\tBlock b = world.getBlockState(pos).getBlock();

\t\tif(Blocks.FIRE.getFlammability(b) > 0)
\t\t\treturn true;

\t\tif(b.isReplaceable(world, pos))
\t\t\treturn true;

\t\treturn super.canDisplace(world, pos);
\t}

\tpublic boolean canDisplace(IBlockAccess world, int x, int y, int z) {
\t\treturn canDisplace(world, new BlockPos(x, y, z));
\t}

\t@Override
\tpublic boolean displaceIfPossible(World world, BlockPos pos) {
\t\treturn super.displaceIfPossible(world, pos) || canDisplace(world, pos);
\t}

\tpublic boolean displaceIfPossible(World world, int x, int y, int z) {
\t\treturn displaceIfPossible(world, new BlockPos(x, y, z));
\t}"""
    assert old_displace in text, "volcanic old_displace"
    text = text.replace(old_displace, new_displace, 1)

    # Remove @Override on getRenderBlockPass
    text = text.replace("""\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic int getRenderBlockPass() {""",
                        """\t@SideOnly(Side.CLIENT)
\tpublic int getRenderBlockPass() {""")

    # isOpaqueCube & playSound
    text = text.replace("!com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().isOpaqueCube()",
                        "!com.hbm.world.WorldBlockState.read(world, x, y + 1, z).isOpaqueCube()")

    text = text.replace('world.playSound(dx, dy, dz, "liquid.lavapop", 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);',
                        'world.playSound(null, dx, dy, dz, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F);')

    text = text.replace('world.playSound((double) x, (double) y, (double) z, "liquid.lava", 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);',
                        'world.playSound(null, (double) x, (double) y, (double) z, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F);')

    text = text.replace("World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)",
                        "world.getBlockState(new BlockPos(x, y - 1, z)).isTopSolid()")

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("VolcanicBlock updated")


def update_balefire():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\blocks\bomb\Balefire.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    text = text.replace("icon = register.registerIcon(this.getTextureName());",
                        'icon = register.registerIcon(RefStrings.MODID + ":balefire");')

    text = text.replace("World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)",
                        "world.getBlockState(new net.minecraft.util.math.BlockPos(x, y - 1, z)).isTopSolid()")

    text = text.replace("this.tryCatchFire(world, x + 1, y, z, 500, rand, meta, WEST);",
                        "this.tryCatchFire(world, x + 1, y, z, 500, rand, meta, ForgeDirection.WEST);")
    text = text.replace("this.tryCatchFire(world, x - 1, y, z, 500, rand, meta, EAST);",
                        "this.tryCatchFire(world, x - 1, y, z, 500, rand, meta, ForgeDirection.EAST);")
    text = text.replace("this.tryCatchFire(world, x, y - 1, z, 300, rand, meta, UP);",
                        "this.tryCatchFire(world, x, y - 1, z, 300, rand, meta, ForgeDirection.UP);")
    text = text.replace("this.tryCatchFire(world, x, y + 1, z, 300, rand, meta, DOWN);",
                        "this.tryCatchFire(world, x, y + 1, z, 300, rand, meta, ForgeDirection.DOWN);")
    text = text.replace("this.tryCatchFire(world, x, y, z - 1, 500, rand, meta, SOUTH);",
                        "this.tryCatchFire(world, x, y, z - 1, 500, rand, meta, ForgeDirection.SOUTH);")
    text = text.replace("this.tryCatchFire(world, x, y, z + 1, 500, rand, meta, NORTH);",
                        "this.tryCatchFire(world, x, y, z + 1, 500, rand, meta, ForgeDirection.NORTH);")

    text = text.replace("spread = this.getChanceToEncourageFire(world, x + 1, y, z, spread, WEST);",
                        "spread = this.getChanceToEncourageFire(world, x + 1, y, z, spread, ForgeDirection.WEST);")
    text = text.replace("spread = this.getChanceToEncourageFire(world, x - 1, y, z, spread, EAST);",
                        "spread = this.getChanceToEncourageFire(world, x - 1, y, z, spread, ForgeDirection.EAST);")
    text = text.replace("spread = this.getChanceToEncourageFire(world, x, y - 1, z, spread, UP);",
                        "spread = this.getChanceToEncourageFire(world, x, y - 1, z, spread, ForgeDirection.UP);")
    text = text.replace("spread = this.getChanceToEncourageFire(world, x, y + 1, z, spread, DOWN);",
                        "spread = this.getChanceToEncourageFire(world, x, y + 1, z, spread, ForgeDirection.DOWN);")
    text = text.replace("spread = this.getChanceToEncourageFire(world, x, y, z - 1, spread, SOUTH);",
                        "spread = this.getChanceToEncourageFire(world, x, y, z - 1, spread, ForgeDirection.SOUTH);")
    text = text.replace("spread = this.getChanceToEncourageFire(world, x, y, z + 1, spread, NORTH);",
                        "spread = this.getChanceToEncourageFire(world, x, y, z + 1, spread, ForgeDirection.NORTH);")

    text = text.replace("world.difficultySetting.getDifficultyId()",
                        "world.getDifficulty().getDifficultyId()")

    text = text.replace("HbmPotion.radiation.id",
                        "HbmPotion.radiation")

    text = text.replace("""\t@Override
\tpublic int getRenderType() {""",
                        """\tpublic int getRenderType() {""")

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("Balefire updated")


def update_waste_earth():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\blocks\generic\WasteEarth.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    # Imports
    text = text.replace("import net.minecraft.world.World;",
                        """import com.hbm.lib.ForgeDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;""")

    # blockIcon field
    text = text.replace("private IIcon iconBottom;",
                        "private IIcon iconBottom;\n\tprotected IIcon blockIcon;")

    # Remove @Override on registerBlockIcons and getIcon
    text = text.replace("""\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic void registerBlockIcons(IIconRegister iconRegister) {""",
                        """\t@SideOnly(Side.CLIENT)
\tpublic void registerBlockIcons(IIconRegister iconRegister) {""")

    text = text.replace("""\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic IIcon getIcon(int side, int meta) {""",
                        """\t@SideOnly(Side.CLIENT)
\tpublic IIcon getIcon(int side, int meta) {""")

    # Potion radiation
    text = text.replace("HbmPotion.radiation.id", "HbmPotion.radiation")

    # isOpaqueCube
    text = text.replace("!b1.isOpaqueCube()", "!b1.getDefaultState().isOpaqueCube()")

    # blockExists
    text = text.replace("!world.blockExists(x + i, y + j, z + k)",
                        "!world.isBlockLoaded(new BlockPos(x + i, y + j, z + k))")

    # canLightningStrikeAt
    text = text.replace("!world.canLightningStrikeAt(x, y, z)",
                        "!world.canLightningStrike(new BlockPos(x, y, z))")

    # getBlockLightValue & getBlockLightOpacity
    old_light = "world.getBlockLightValue(x, y + 1, z) < 4 && world.getBlockLightOpacity(x, y + 1, z) > 2"
    new_light = "world.getLight(new BlockPos(x, y + 1, z)) < 4 && world.getBlockState(new BlockPos(x, y + 1, z)).getLightOpacity(world, new BlockPos(x, y + 1, z)) > 2"
    assert old_light in text, "waste_earth old_light"
    text = text.replace(old_light, new_light, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("WasteEarth updated")


def update_chunk_rad_handler():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\handler\radiation\ChunkRadiationHandlerNT.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    text = text.replace("import net.minecraft.world.World;",
                        """import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;""")

    text = text.replace("if(!world.blockExists(x, y, z)) {",
                        "if(!world.isBlockLoaded(new BlockPos(x, y, z))) {")

    text = text.replace("rebuildChunkPockets(world.getChunkFromBlockCoords(x, z), y >> 4);",
                        "rebuildChunkPockets(world.getChunk(new BlockPos(x, 0, z)), y >> 4);")

    text = text.replace("st = new ChunkRadiationStorage(worldRadData, world.getChunkFromBlockCoords(x, z));",
                        "st = new ChunkRadiationStorage(worldRadData, world.getChunk(new BlockPos(x, 0, z)));")

    text = text.replace("p.parent.parent.chunk.isModified = true;",
                        "p.parent.parent.chunk.markDirty();")

    text = text.replace("if(!p.parent.parent.chunk.getWorld().blockExists(nPos.getX(), nPos.getY(), nPos.getZ()) || nPos.getY() < 0 || nPos.getY() > 255)",
                        "if(!p.parent.parent.chunk.getWorld().isBlockLoaded(nPos) || nPos.getY() < 0 || nPos.getY() > 255)")

    text = text.replace("rebuildChunkPockets(p.parent.parent.chunk.getWorld().getChunkFromBlockCoords(nPos.getX(), nPos.getZ()), nPos.getY() >> 4);",
                        "rebuildChunkPockets(p.parent.parent.chunk.getWorld().getChunk(nPos), nPos.getY() >> 4);")

    text = text.replace("Block block = blocks.getBlockByExtId(x, y, z); // !!!",
                        "Block block = blocks.get(x, y, z).getBlock();")

    text = text.replace("Block block = chunk.getBlockByExtId(pos.getX(), pos.getY(), pos.getZ()); // !!!",
                        "Block block = chunk.get(pos.getX(), pos.getY(), pos.getZ()).getBlock();")

    text = text.replace("provider.loadChunk((pos.getX() + 16) >> 4, (pos.getZ() + 16) >> 4); // !!!",
                        "provider.provideChunk((pos.getX() + 16) >> 4, (pos.getZ() + 16) >> 4);")

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("ChunkRadiationHandlerNT updated")


def update_block_vacuum():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\blocks\gas\BlockVacuum.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    text = text.replace("import net.minecraft.world.World;",
                        """import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;""")

    # getCollisionBoundingBoxFromPool
    old_box = """\t@Override
\tpublic AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
\t\treturn null;
\t}"""
    new_box = """\t@Override
\tpublic AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
\t\treturn NULL_AABB;
\t}"""
    assert old_box in text, "vacuum old_box"
    text = text.replace(old_box, new_box, 1)

    # shouldSideBeRendered
    old_side = """\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
\t\treturn false;
\t}"""
    new_side = """\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
\t\treturn false;
\t}"""
    assert old_side in text, "vacuum old_side"
    text = text.replace(old_side, new_side, 1)

    # canCollideCheck
    old_check = """\t@Override
\tpublic boolean canCollideCheck(int p_149678_1_, boolean p_149678_2_) {
\t\treturn false;
\t}"""
    new_check = """\t@Override
\tpublic boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
\t\treturn false;
\t}"""
    assert old_check in text, "vacuum old_check"
    text = text.replace(old_check, new_check, 1)

    # onBlockAdded
    old_added = """\t@Override
\tpublic void onBlockAdded(World world, int x, int y, int z) {
\t\tif(!world.isRemote) world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y, z), this, 1);
\t}"""
    new_added = """\t@Override
\tpublic void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
\t\tif(!worldIn.isRemote) worldIn.scheduleUpdate(pos, this, 1);
\t}"""
    assert old_added in text, "vacuum old_added"
    text = text.replace(old_added, new_added, 1)

    # isAirBlock
    old_leaky = """\tprivate boolean isAirBlock(Block b) {

\t\tif(b == this)
\t\t\treturn false;

\t\treturn b.getDefaultState().getMaterial() == Material.AIR ||
\t\t\t\tb.getBlockBoundsMinX() > 0 || b.getBlockBoundsMinY() > 0 || b.getBlockBoundsMinZ() > 0 ||
\t\t\t\tb.getBlockBoundsMaxX() < 1 || b.getBlockBoundsMaxY() < 1 || b.getBlockBoundsMaxZ() < 1;
\t}"""
    new_leaky = """\tprivate boolean isAirBlock(Block b) {

\t\tif(b == this)
\t\t\treturn false;

\t\treturn b.getDefaultState().getMaterial() == Material.AIR || !b.getDefaultState().isFullCube();
\t}"""
    assert old_leaky in text, "vacuum old_leaky"
    text = text.replace(old_leaky, new_leaky, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("BlockVacuum updated")


def update_block_mush():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\blocks\generic\BlockMush.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    # Imports
    text = text.replace("import net.minecraft.world.World;",
                        """import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;""")

    # canBlockStay: remove @Override
    text = text.replace("""\t\t@Override
\tpublic boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {""",
                        """\tpublic boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {""")

    # IGrowable 1.12 overrides
    old_growable = """\t/**
\t * General grow condition (\"has space?\")
\t */
\t@Override
\tpublic boolean func_149851_a(World world, int x, int y, int z, boolean b) {
\t\treturn canBlockStay(world, x, y, z);
\t}

\t/**
\t * Grow chance (40%)
\t */
\t@Override
\tpublic boolean func_149852_a(World p_149852_1_, Random p_149852_2_, int p_149852_3_, int p_149852_4_, int p_149852_5_) {
\t\treturn p_149852_2_.nextFloat() < 0.4D;
\t}

\t/**
\t * On successful bonemeal grow
\t */
\t@Override
\tpublic void func_149853_b(World world, Random rand, int x, int y, int z) {
\t\tthis.growHuge(world, x, y, z, rand);
\t}"""

    new_growable = """\t@Override
\tpublic boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
\t\treturn canBlockStay(worldIn, pos.getX(), pos.getY(), pos.getZ());
\t}

\t@Override
\tpublic boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
\t\treturn rand.nextFloat() < 0.4D;
\t}

\t@Override
\tpublic void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
\t\tthis.growHuge(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
\t}

\tpublic boolean func_149851_a(World world, int x, int y, int z, boolean b) {
\t\treturn canBlockStay(world, x, y, z);
\t}

\tpublic boolean func_149852_a(World p_149852_1_, Random p_149852_2_, int p_149852_3_, int p_149852_4_, int p_149852_5_) {
\t\treturn p_149852_2_.nextFloat() < 0.4D;
\t}

\tpublic void func_149853_b(World world, Random rand, int x, int y, int z) {
\t\tthis.growHuge(world, x, y, z, rand);
\t}"""
    assert old_growable in text, "mush old_growable"
    text = text.replace(old_growable, new_growable, 1)

    # getRenderType: remove @Override
    text = text.replace("""\t@Override
\tpublic int getRenderType() {""",
                        """\tpublic int getRenderType() {""")

    # dropBlockAsItem
    text = text.replace("this.dropBlockAsItem(world, x, y, z, com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ), 0);",
                        "BlockPos dpos = new BlockPos(x, y, z); this.dropBlockAsItem(world, dpos, world.getBlockState(dpos), 0);")

    # IPlantable methods
    old_plantable = """\t@Override
\tpublic EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
\t\treturn EnumPlantType.Cave;
\t}

\t@Override
\tpublic Block getPlant(IBlockAccess world, int x, int y, int z) {
\t\treturn this;
\t}

\t@Override
\tpublic int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
\t\treturn com.hbm.world.WorldBlockState.readMetadata(world, x, y, z);
\t}"""

    new_plantable = """\t@Override
\tpublic EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
\t\treturn EnumPlantType.Cave;
\t}

\t@Override
\tpublic IBlockState getPlant(IBlockAccess world, BlockPos pos) {
\t\treturn world.getBlockState(pos);
\t}

\tpublic EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z) {
\t\treturn EnumPlantType.Cave;
\t}

\tpublic Block getPlant(IBlockAccess world, int x, int y, int z) {
\t\treturn this;
\t}

\tpublic int getPlantMetadata(IBlockAccess world, int x, int y, int z) {
\t\treturn com.hbm.world.WorldBlockState.readMetadata(world, x, y, z);
\t}"""
    assert old_plantable in text, "mush old_plantable"
    text = text.replace(old_plantable, new_plantable, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("BlockMush updated")


def update_debris_base():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\projectile\EntityDebrisBase.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    start_marker = "\t@Override\n\tpublic void move(net.minecraft.entity.MoverType type, double moX, double moY, double moZ) {\n"
    end_marker = "\t\tthis.world.profiler.endSection();\n\t}\n"
    idx1 = text.find(start_marker)
    assert idx1 != -1, "debris start_marker"
    idx2 = text.find(end_marker, idx1)
    assert idx2 != -1, "debris end_marker"

    new_move = """\t@Override
\tpublic void move(net.minecraft.entity.MoverType type, double moX, double moY, double moZ) {
\t\tif(this.isInWeb) {
\t\t\tthis.isInWeb = false;
\t\t}

\t\tdouble initMoX = moX;
\t\tdouble initMoZ = moZ;

\t\tsuper.move(type, moX, moY, moZ);

\t\tif(initMoX != this.motionX && this.collidedHorizontally) {
\t\t\tthis.motionX *= -0.75D;
\t\t}
\t\tif(initMoZ != this.motionZ && this.collidedHorizontally) {
\t\t\tthis.motionZ *= -0.75D;
\t\t}
\t}\n"""

    text = text[:idx1] + new_move + text[idx2 + len(end_marker):]

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntityDebrisBase updated")


def update_entity_bullet():
    path = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\entity\projectile\EntityBullet.java"
    with open(path, "rb") as f:
        text = f.read().decode("utf-8").replace("\r\n", "\n")

    # Imports
    text = text.replace("import net.minecraft.world.World;",
                        """import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;""")

    # setPositionAndRotationDirect
    old_pos_rot = """\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_,
\t		float p_70056_8_, int p_70056_9_) {
\t\tthis.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
\t\tthis.setRotation(p_70056_7_, p_70056_8_);
\t}"""

    new_pos_rot = """\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic void setPositionAndRotationDirect(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_,
\t\t\tfloat p_70056_8_, int p_70056_9_, boolean teleport) {
\t\tthis.setPosition(p_70056_1_, p_70056_3_, p_70056_5_);
\t\tthis.setRotation(p_70056_7_, p_70056_8_);
\t}"""
    assert old_pos_rot in text, "bullet old_pos_rot"
    text = text.replace(old_pos_rot, new_pos_rot, 1)

    # glass playSound
    old_sound = """this.world.playSound(this.tileX, this.tileY, this.tileZ, "dig.glass",
						1.0F, 1.0F, true);"""
    new_sound = """this.world.playSound(null, new BlockPos(this.tileX, this.tileY, this.tileZ), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);"""
    assert old_sound in text, "bullet old_sound"
    text = text.replace(old_sound, new_sound, 1)

    # BoundingBox expand
    old_expand = "this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D)"
    new_expand = "this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D)"
    assert old_expand in text, "bullet old_expand"
    text = text.replace(old_expand, new_expand, 1)

    # EnchantmentHelper
    text = text.replace("EnchantmentHelper.func_151384_a(entitylivingbase, this.shootingEntity);",
                        "EnchantmentHelper.applyThornEnchantments(entitylivingbase, this.shootingEntity);")
    text = text.replace("EnchantmentHelper.func_151385_b((EntityLivingBase) this.shootingEntity,\n\t\t\t\t\t\t\t\t\t\t\t\t\tentitylivingbase);",
                        "EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) this.shootingEntity, entitylivingbase);")

    # onEntityCollidedWithBlock -> onEntityCollision
    old_collision = "this.field_145790_g.onEntityCollidedWithBlock(this.world, this.tileX,\n\t\t\t\t\t\t\t\tthis.tileY, this.tileZ, this);"
    new_collision = """BlockPos bpos = new BlockPos(this.tileX, this.tileY, this.tileZ);
\t\t\t\t\t\t\t\tthis.field_145790_g.onEntityCollision(this.world, bpos, this.world.getBlockState(bpos), this);"""
    assert old_collision in text, "bullet old_collision"
    text = text.replace(old_collision, new_collision, 1)

    # func_145775_I() -> doBlockCollisions()
    text = text.replace("this.func_145775_I();", "this.doBlockCollisions();")

    # getShadowSize: remove @Override
    text = text.replace("""\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic float getShadowSize() {""",
                        """\t@SideOnly(Side.CLIENT)
\tpublic float getShadowSize() {""")

    # canAttackWithItem -> canBeAttackedWithItem
    old_attack = """\t@Override
\tpublic boolean canAttackWithItem() {
\t\treturn false;
\t}"""
    new_attack = """\t@Override
\tpublic boolean canBeAttackedWithItem() {
\t\treturn false;
\t}"""
    assert old_attack in text, "bullet old_attack"
    text = text.replace(old_attack, new_attack, 1)

    # Brightness overrides
    old_bright = """\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic int getBrightnessForRender(float p_70070_1_) {
\t\tif(this.getIsCritical() || this.getIsChopper())
\t\t\treturn 15728880;
\t\telse
\t\t\treturn super.getBrightnessForRender(p_70070_1_);
\t}

\t@Override
\tpublic float getBrightness(float p_70013_1_) {
\t\tif(this.getIsCritical() || this.getIsChopper())
\t\t\treturn 1.0F;
\t\telse
\t\t\treturn super.getBrightness(p_70013_1_);
\t}"""

    new_bright = """\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic int getBrightnessForRender() {
\t\tif(this.getIsCritical() || this.getIsChopper())
\t\t\treturn 15728880;
\t\telse
\t\t\treturn super.getBrightnessForRender();
\t}

\t@Override
\tpublic float getBrightness() {
\t\tif(this.getIsCritical() || this.getIsChopper())
\t\t\treturn 1.0F;
\t\telse
\t\t\treturn super.getBrightness();
\t}"""
    assert old_bright in text, "bullet old_bright"
    text = text.replace(old_bright, new_bright, 1)

    with open(path, "wb") as f:
        f.write(text.replace("\n", "\r\n").encode("utf-8"))
    print("EntityBullet updated")


if __name__ == "__main__":
    for fn in [
        update_schrabidic,
        update_volcanic,
        update_balefire,
        update_waste_earth,
        update_chunk_rad_handler,
        update_block_vacuum,
        update_block_mush,
        update_debris_base,
        update_entity_bullet,
    ]:
        try:
            fn()
        except Exception as e:
            print(f"Skipping {fn.__name__}: {e}")
    print("FINISHED CP155 MODERNIZATION")

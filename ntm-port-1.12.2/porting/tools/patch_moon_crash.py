import sys

def patch_file(path, old_sub, new_sub):
    with open(path, 'rb') as f:
        content = f.read()
    crlf = b'\r\n' in content
    old_b = old_sub.replace('\r\n', '\n').replace('\n', '\r\n' if crlf else '\n').encode('utf-8')
    new_b = new_sub.replace('\r\n', '\n').replace('\n', '\r\n' if crlf else '\n').encode('utf-8')
    if old_b not in content:
        print(f'ERROR: snippet not found in {path}')
        sys.exit(1)
    new_content = content.replace(old_b, new_b, 1)
    with open(path, 'wb') as f:
        f.write(new_content)
    print(f'Successfully patched {path}')

# 1. TileEntityDoorGeneric ranges and states
old_ranges = "\t\t\t\t\tfloat time = getDoorType().getDoorRangeOpenTime(openTicks, i);"
new_ranges = "\t\t\t\t\tfloat time = type.getDoorRangeOpenTime(openTicks, i);"

old_states = """\t\t\tif(state == STATE_OPENING && openTicks == getDoorType().timeToOpen()) {
\t\t\t\tstate = STATE_OPEN;
\t\t\t\t((IBlockSealable)blockType).updateSealedState(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
\t\t\t}
\t\t\tif(state == STATE_CLOSING && openTicks == 0) {
\t\t\t\tstate = STATE_CLOSED;
\t\t\t\t((IBlockSealable)blockType).updateSealedState(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
\t\t\t}"""

new_states = """\t\t\tif(state == STATE_OPENING && openTicks == type.timeToOpen()) {
\t\t\t\tstate = STATE_OPEN;
\t\t\t\tif(blockType instanceof IBlockSealable)
\t\t\t\t\t((IBlockSealable)blockType).updateSealedState(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
\t\t\t}
\t\t\tif(state == STATE_CLOSING && openTicks == 0) {
\t\t\t\tstate = STATE_CLOSED;
\t\t\t\tif(blockType instanceof IBlockSealable)
\t\t\t\t\t((IBlockSealable)blockType).updateSealedState(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
\t\t\t}"""

old_toggle1 = """\tpublic boolean tryToggle(EntityPlayer player) {

\t\tif(this.isLocked() && player == null) return false;"""
new_toggle1 = """\tpublic boolean tryToggle(EntityPlayer player) {
\t\tif(getDoorType() == null) return false;

\t\tif(this.isLocked() && player == null) return false;"""

old_toggle2 = """\tpublic boolean tryToggle(int passcode) {
\t\tif(this.isLocked() && passcode != this.lock) return false;"""
new_toggle2 = """\tpublic boolean tryToggle(int passcode) {
\t\tif(getDoorType() == null) return false;
\t\tif(this.isLocked() && passcode != this.lock) return false;"""

old_handle = """\t@SideOnly(Side.CLIENT)
\tpublic void handleNewState(byte state) {

\t\tif(this.state != state) {
\t\t\tDoorDecl doorType = getDoorType();"""
new_handle = """\t@SideOnly(Side.CLIENT)
\tpublic void handleNewState(byte state) {

\t\tif(this.state != state) {
\t\t\tDoorDecl doorType = getDoorType();
\t\t\tif(doorType == null) return;"""

old_skin = """\tpublic boolean cycleSkinIndex() {
\t\tif(!getDoorType().hasSkins()) return false;
\t\tthis.skinIndex++;
\t\tthis.skinIndex %= getDoorType().getSkinCount();"""
new_skin = """\tpublic boolean cycleSkinIndex() {
\t\tDoorDecl type = getDoorType();
\t\tif(type == null || !type.hasSkins()) return false;
\t\tthis.skinIndex++;
\t\tthis.skinIndex %= type.getSkinCount();"""

old_write = """\t\tif(getDoorType().hasSkins())
\t\t\ttag.setByte("skin", skinIndex);"""
new_write = """\t\tif(getDoorType() != null && getDoorType().hasSkins())
\t\t\ttag.setByte("skin", skinIndex);"""

patch_file('src/main/java/com/hbm/tileentity/TileEntityDoorGeneric.java', old_ranges, new_ranges)
patch_file('src/main/java/com/hbm/tileentity/TileEntityDoorGeneric.java', old_ranges, new_ranges)
patch_file('src/main/java/com/hbm/tileentity/TileEntityDoorGeneric.java', old_states, new_states)
patch_file('src/main/java/com/hbm/tileentity/TileEntityDoorGeneric.java', old_toggle1, new_toggle1)
patch_file('src/main/java/com/hbm/tileentity/TileEntityDoorGeneric.java', old_toggle2, new_toggle2)
patch_file('src/main/java/com/hbm/tileentity/TileEntityDoorGeneric.java', old_handle, new_handle)
patch_file('src/main/java/com/hbm/tileentity/TileEntityDoorGeneric.java', old_skin, new_skin)
patch_file('src/main/java/com/hbm/tileentity/TileEntityDoorGeneric.java', old_write, new_write)

# 2. BlockWandLoot.java
old_loot_update = """\t\t@Override
\t\tpublic void update() {
\t\t\tif(!this.world.isRemote) {
\t\t\t\tif(triggerReplace) {
\t\t\t\t\t// On the first tick of this TE, replace with intended block and fill with loot
\t\t\t\t\treplace();
\t\t\t\t} else {
\t\t\t\t\tnetworkPackNT(15);
\t\t\t\t}
\t\t\t}
\t\t}"""

new_loot_update = """\t\t@Override
\t\tpublic void update() {
\t\t\tif(this.isInvalid()) return;
\t\t\tif(!(this.world.getBlockState(this.pos).getBlock() instanceof BlockWandLoot)) {
\t\t\t\tMainRegistry.logger.warn("Somehow the block at: " + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ() + " isn't a loot block, cleaning up orphan TE!");
\t\t\t\tthis.invalidate();
\t\t\t\tif(this.hasWorld()) {
\t\t\t\t\tthis.world.removeTileEntity(this.pos);
\t\t\t\t}
\t\t\t\treturn;
\t\t\t}
\t\t\tif(!this.world.isRemote) {
\t\t\t\tif(triggerReplace) {
\t\t\t\t\t// On the first tick of this TE, replace with intended block and fill with loot
\t\t\t\t\treplace();
\t\t\t\t} else {
\t\t\t\t\tnetworkPackNT(15);
\t\t\t\t}
\t\t\t}
\t\t}"""

old_loot_replace = """\t\tprivate void replace() {
\t\t\tif(!(this.world.getBlockState(this.pos).getBlock() instanceof BlockWandLoot)) {
\t\t\t\tMainRegistry.logger.warn("Somehow the block at: " + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ() + " isn't a loot block but we're doing a TE update as if it is, cancelling!");
\t\t\t\treturn;
\t\t\t}"""

new_loot_replace = """\t\tprivate void replace() {
\t\t\tif(this.isInvalid()) return;
\t\t\tif(!(this.world.getBlockState(this.pos).getBlock() instanceof BlockWandLoot)) {
\t\t\t\tMainRegistry.logger.warn("Somehow the block at: " + this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ() + " isn't a loot block, cleaning up orphan TE!");
\t\t\t\tthis.invalidate();
\t\t\t\tif(this.hasWorld()) {
\t\t\t\t\tthis.world.removeTileEntity(this.pos);
\t\t\t\t}
\t\t\t\treturn;
\t\t\t}"""

patch_file('src/main/java/com/hbm/blocks/generic/BlockWandLoot.java', old_loot_update, new_loot_update)
patch_file('src/main/java/com/hbm/blocks/generic/BlockWandLoot.java', old_loot_replace, new_loot_replace)

# 3. BlockWandJigsaw.java
old_jigsaw_update = """\t\t@Override
\t\tpublic void update() {
\t\t\tif(!this.world.isRemote) {
\t\t\t\tnetworkPackNT(15);
\t\t\t}
\t\t}"""

new_jigsaw_update = """\t\t@Override
\t\tpublic void update() {
\t\t\tif(this.isInvalid()) return;
\t\t\tif(!(this.world.getBlockState(this.pos).getBlock() instanceof BlockWandJigsaw)) {
\t\t\t\tthis.invalidate();
\t\t\t\tif(this.hasWorld()) {
\t\t\t\t\tthis.world.removeTileEntity(this.pos);
\t\t\t\t}
\t\t\t\treturn;
\t\t\t}
\t\t\tif(!this.world.isRemote) {
\t\t\t\tnetworkPackNT(15);
\t\t\t}
\t\t}"""

patch_file('src/main/java/com/hbm/blocks/generic/BlockWandJigsaw.java', old_jigsaw_update, new_jigsaw_update)

# 4. BlockWandTandem.java
old_tandem_update = """\t\t@Override
\t\tpublic void update() {
\t\t\tif(!this.world.isRemote) {
\t\t\t\ttryGenerate();
\t\t\t\tnetworkPackNT(15);
\t\t\t}
\t\t}"""

new_tandem_update = """\t\t@Override
\t\tpublic void update() {
\t\t\tif(this.isInvalid()) return;
\t\t\tif(!(this.world.getBlockState(this.pos).getBlock() instanceof BlockWandTandem)) {
\t\t\t\tthis.invalidate();
\t\t\t\tif(this.hasWorld()) {
\t\t\t\t\tthis.world.removeTileEntity(this.pos);
\t\t\t\t}
\t\t\t\treturn;
\t\t\t}
\t\t\tif(!this.world.isRemote) {
\t\t\t\ttryGenerate();
\t\t\t\tnetworkPackNT(15);
\t\t\t}
\t\t}"""

patch_file('src/main/java/com/hbm/blocks/generic/BlockWandTandem.java', old_tandem_update, new_tandem_update)

# 5. TileEntityDummy.java
old_dummy_update = """    @Override
\tpublic void update() {
    	if(!this.world.isRemote) {
    		if(!(com.hbm.world.WorldBlockState.read(this.world, targetX, targetY, targetZ).getBlock() instanceof IMultiblock)) {
    			com.hbm.world.WorldBlockDestruction.destroy(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), false);
    		}
    	}
    }"""

new_dummy_update = """    @Override
\tpublic void update() {
    	if(this.isInvalid()) return;
    	if(!this.world.isRemote) {
    		if(com.hbm.world.WorldBlockState.isAir(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ())) {
    			this.invalidate();
    			this.world.removeTileEntity(this.pos);
    			return;
    		}
    		if(!(com.hbm.world.WorldBlockState.read(this.world, targetX, targetY, targetZ).getBlock() instanceof IMultiblock)) {
    			com.hbm.world.WorldBlockDestruction.destroy(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), false);
    		}
    	}
    }"""

patch_file('src/main/java/com/hbm/tileentity/machine/TileEntityDummy.java', old_dummy_update, new_dummy_update)

# 6. NBTStructure.java
old_nbt_wipe = """\t\t\t\t\t\tif(wipeExisting) { net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = rx; int nativePlacementY = by + y; int nativePlacementZ = rz; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }"""

new_nbt_wipe = """\t\t\t\t\t\tif(wipeExisting) {
\t\t\t\t\t\t\tnet.minecraft.util.math.BlockPos clearPos = new net.minecraft.util.math.BlockPos(rx, by + y, rz);
\t\t\t\t\t\t\tworld.setBlockState(clearPos, Blocks.AIR.getDefaultState(), 2);
\t\t\t\t\t\t\tworld.removeTileEntity(clearPos);
\t\t\t\t\t\t}"""

old_nbt_te1 = """\t\t\t\t\tif(state.nbt != null) {
\t\t\t\t\t\tTileEntity te = buildTileEntity(world, block, worldItemPalette, state.nbt, coordBaseMode, null);
\t\t\t\t\t\tworld.setTileEntity(new net.minecraft.util.math.BlockPos(rx, ry, rz), te);
\t\t\t\t\t}"""

new_nbt_te1 = """\t\t\t\t\tif(state.nbt != null && block != Blocks.AIR && block.hasTileEntity(block.getStateFromMeta(meta))) {
\t\t\t\t\t\tTileEntity te = buildTileEntity(world, block, worldItemPalette, state.nbt, coordBaseMode, null);
\t\t\t\t\t\tif(te != null) {
\t\t\t\t\t\t\tworld.setTileEntity(new net.minecraft.util.math.BlockPos(rx, ry, rz), te);
\t\t\t\t\t\t}
\t\t\t\t\t}"""

old_nbt_te2 = """\t\t\t\t\tif(state.nbt != null) {
\t\t\t\t\t\tTileEntity te = buildTileEntity(world, block, worldItemPalette, state.nbt, coordBaseMode, structureName);
\t\t\t\t\t\tworld.setTileEntity(new net.minecraft.util.math.BlockPos(rx, ry, rz), te);
\t\t\t\t\t}"""

new_nbt_te2 = """\t\t\t\t\tif(state.nbt != null && block != Blocks.AIR && block.hasTileEntity(block.getStateFromMeta(meta))) {
\t\t\t\t\t\tTileEntity te = buildTileEntity(world, block, worldItemPalette, state.nbt, coordBaseMode, structureName);
\t\t\t\t\t\tif(te != null) {
\t\t\t\t\t\t\tworld.setTileEntity(new net.minecraft.util.math.BlockPos(rx, ry, rz), te);
\t\t\t\t\t\t}
\t\t\t\t\t}"""

patch_file('src/main/java/com/hbm/world/gen/nbt/NBTStructure.java', old_nbt_wipe, new_nbt_wipe)
patch_file('src/main/java/com/hbm/world/gen/nbt/NBTStructure.java', old_nbt_te1, new_nbt_te1)
patch_file('src/main/java/com/hbm/world/gen/nbt/NBTStructure.java', old_nbt_te2, new_nbt_te2)

print('ALL PATCHES APPLIED SUCCESSFULLY!')

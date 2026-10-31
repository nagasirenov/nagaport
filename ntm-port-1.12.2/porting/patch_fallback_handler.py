path = r'C:\Users\nagas\Desktop\port\ntm-port-1.12.2\src\main\java\com\hbm\main\NativeStateFallbackHandler.java'

with open(path, 'rb') as f:
    c = f.read()

# 1. Update BlockBaseAdapter
t1 = b'this.cross = legacyRenderType(block) == 1;'
r1 = b'this.cross = legacyRenderType(block) == 1 || isPlantBlock(block);'
assert t1 in c, 't1 not found'
c = c.replace(t1, r1)

# 2. Update DeclaredIconAdapter
t2 = b'private static final class DeclaredIconAdapter implements FallbackBlock {\r\n\t\tprivate final Block block;\r\n\t\tprivate final java.lang.reflect.Method lookup;\r\n\r\n\t\tDeclaredIconAdapter(Block block, java.lang.reflect.Method lookup) {\r\n\t\t\tthis.block = block;\r\n\t\t\tthis.lookup = lookup;\r\n\t\t}\r\n\r\n\t\t@Override public boolean isCross() { return false; }'
r2 = b'private static final class DeclaredIconAdapter implements FallbackBlock {\r\n\t\tprivate final Block block;\r\n\t\tprivate final java.lang.reflect.Method lookup;\r\n\r\n\t\tDeclaredIconAdapter(Block block, java.lang.reflect.Method lookup) {\r\n\t\t\tthis.block = block;\r\n\t\t\tthis.lookup = lookup;\r\n\t\t}\r\n\r\n\t\t@Override public boolean isCross() { return isPlantBlock(block); }'
assert t2 in c, 't2 not found'
c = c.replace(t2, r2)

# 3. Update BoundAdapter
t3 = b'private static final class BoundAdapter implements FallbackBlock {\r\n\t\tprivate final Block block;\r\n\r\n\t\tBoundAdapter(Block block) {\r\n\t\t\tthis.block = block;\r\n\t\t}\r\n\r\n\t\t@Override public boolean isCross() { return false; }'
r3 = b'private static final class BoundAdapter implements FallbackBlock {\r\n\t\tprivate final Block block;\r\n\r\n\t\tBoundAdapter(Block block) {\r\n\t\t\tthis.block = block;\r\n\t\t}\r\n\r\n\t\t@Override public boolean isCross() { return isPlantBlock(block); }'
assert t3 in c, 't3 not found'
c = c.replace(t3, r3)

# 4. Add isPlantBlock helper before legacyRenderType
t4 = b'\tprivate static int legacyRenderType(Block block) {'
r4 = b'''\tprivate static boolean isPlantBlock(Block block) {\r
\t\treturn block instanceof net.minecraftforge.common.IPlantable\r
\t\t\t\t|| block instanceof net.minecraft.block.IGrowable\r
\t\t\t\t|| block instanceof com.hbm.blocks.generic.BlockNTMFlower\r
\t\t\t\t|| block instanceof com.hbm.blocks.generic.BlockDeadPlant\r
\t\t\t\t|| block instanceof com.hbm.blocks.generic.BlockTallPlant\r
\t\t\t\t|| block instanceof com.hbm.blocks.generic.BlockRubberPlant\r
\t\t\t\t|| block instanceof com.hbm.blocks.BlockWaterPlant\r
\t\t\t\t|| block instanceof com.hbm.blocks.BlockCrop\r
\t\t\t\t|| block instanceof com.hbm.blocks.BlockNTSapling\r
\t\t\t\t|| block.getDefaultState().getMaterial() == net.minecraft.block.material.Material.PLANTS\r
\t\t\t\t|| block.getDefaultState().getMaterial() == net.minecraft.block.material.Material.VINE;\r
\t}\r
\r
\tprivate static int legacyRenderType(Block block) {'''
assert t4 in c, 't4 not found'
c = c.replace(t4, r4)

# 5. Update getQuads to handle cross in inventory
t5 = b'''\t\t\tif(adapter.isCross() && !inventory) {\r
\t\t\t\tif(side != null) return Collections.emptyList();\r
\t\t\t\tList<BakedQuad> cached = CROSS_CACHE.get(this);\r
\t\t\t\tif(cached != null) return cached;\r
\t\t\t\tList<BakedQuad> quads = crossQuads(adapter.icon(EnumFacing.NORTH, resolved));\r
\t\t\t\tCROSS_CACHE.put(this, quads);\r
\t\t\t\treturn quads;\r
\t\t\t}'''

r5 = b'''\t\t\tif(adapter.isCross()) {\r
\t\t\t\tif(side != null) return Collections.emptyList();\r
\t\t\t\tList<BakedQuad> cached = CROSS_CACHE.get(this);\r
\t\t\t\tif(cached != null) return cached;\r
\t\t\t\tList<BakedQuad> quads = inventory\r
\t\t\t\t\t\t? makeFlatItemQuads(resolve(adapter.icon(EnumFacing.NORTH, resolved)))\r
\t\t\t\t\t\t: crossQuads(adapter.icon(EnumFacing.NORTH, resolved));\r
\t\t\t\tCROSS_CACHE.put(this, quads);\r
\t\t\t\treturn quads;\r
\t\t\t}'''
assert t5 in c, 't5 not found'
c = c.replace(t5, r5)

# 6. Add makeFlatItemQuads
t6 = b'\t\tprivate static List<BakedQuad> crossQuads(IIcon icon) {'
r6 = b'''\t\tprivate static List<BakedQuad> makeFlatItemQuads(TextureAtlasSprite sprite) {\r
\t\t\tif(sprite == null) return Collections.emptyList();\r
\t\t\tBlockPartFace face = new BlockPartFace(null, 0, sprite.getIconName(),\r
\t\t\t\t\tnew BlockFaceUV(new float[] {0.0F, 0.0F, 16.0F, 16.0F}, 0));\r
\t\t\tList<BakedQuad> quads = new ArrayList<>(2);\r
\t\t\tquads.add(FACE_BAKERY.makeBakedQuad(new Vector3f(0.0F, 0.0F, 7.5F),\r
\t\t\t\t\tnew Vector3f(16.0F, 16.0F, 8.5F), face, sprite, EnumFacing.NORTH,\r
\t\t\t\t\tModelRotation.X0_Y0, null, true, false));\r
\t\t\tquads.add(FACE_BAKERY.makeBakedQuad(new Vector3f(0.0F, 0.0F, 7.5F),\r
\t\t\t\t\tnew Vector3f(16.0F, 16.0F, 8.5F), face, sprite, EnumFacing.SOUTH,\r
\t\t\t\t\tModelRotation.X0_Y0, null, true, false));\r
\t\t\treturn Collections.unmodifiableList(quads);\r
\t\t}\r
\r
\t\tprivate static List<BakedQuad> crossQuads(IIcon icon) {'''
assert t6 in c, 't6 not found'
c = c.replace(t6, r6)

with open(path, 'wb') as f:
    f.write(c)

print('Successfully patched NativeStateFallbackHandler.java')

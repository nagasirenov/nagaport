#!/usr/bin/env python3
# CP167 wave 11: does-not-override family - @Override removals + 1.12 signature conversions.
# Op kinds:
#   rm   (rel, line, next_frag)              - drop the @Override line at `line` if next line contains next_frag
#   decl (rel, line, old, new, adapter|None) - rewrite decl line, insert adapter line right after it
#   rep  (rel, line, old, new)               - plain substring replace in window (wave10 semantics)
# Usage: python wave11_batch.py [--apply]
import sys, os, re

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')

RM = [
 ("com/hbm/blocks/BlockVinylVine.java", 40, "canBlockStay(net.minecraft.world.World worldIn"),
 ("com/hbm/blocks/BlockVinylVine.java", 51, "public int getBlockColor()"),
 ("com/hbm/blocks/BlockVinylVine.java", 60, "public int getRenderColor(int"),
 ("com/hbm/blocks/BlockVinylVine.java", 65, "public int colorMultiplier(IBlockAccess"),
  ("com/hbm/blocks/BlockWaterPlant.java", 50, "public int quantityDropped(int meta"),
 ("com/hbm/blocks/generic/BlockRubberCacti.java", 74, "public BlockRenderLayer getBlockLayer()"),
 ("com/hbm/blocks/generic/BlockRotatablePillar.java", 43, "protected IIcon getSideIcon(int"),
 ("com/hbm/tileentity/network/TileEntityFluidCounterValve.java", 48, "public boolean shouldRefresh(Block oldBlock"),
 ("com/hbm/tileentity/network/TileEntityFluidValve.java", 24, "public boolean shouldRefresh(Block oldBlock"),
 ("com/hbm/entity/mob/EntityParasiteMaggot.java", 30, "protected Entity findPlayerToAttack()"),
 ("com/hbm/items/special/ItemModRecord.java", 89, "public ResourceLocation getRecordResource(String"),
 ("com/hbm/items/tool/ItemCraftingDegradation.java", 18, "public boolean doesContainerItemLeaveCraftingGrid(ItemStack"),
 ("com/hbm/wiaj/FauxWorld.java", 25, "protected int func_152379_p()"),
 ("com/hbm/entity/mob/EntityDummy.java", 18, "public boolean interact(EntityPlayer"),
 ("com/hbm/render/entity/effect/RenderQuasar.java", 51, "protected void setColorFromIteration(Tessellator"),
 ("com/hbm/blocks/generic/BlockDoorGeneric.java", 150, "public boolean getBlocksMovement(IBlockAccess"),
]

DECL = [
 ("com/hbm/blocks/generic/BlockOreBasalt.java", 80,
  "public void dropBlockAsItemWithChance(World world, int x, int y, int z, int meta, float chance, int fortune) {",
  "public void dropBlockAsItemWithChance(World world, net.minecraft.block.state.IBlockState state, net.minecraft.util.math.BlockPos pos, float chance, int fortune) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ(); int meta = com.hbm.world.WorldBlockState.metadata(state);"),
 ("com/hbm/blocks/bomb/BlockChargeBase.java", 100,
  "public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {",
  "public boolean canPlaceBlockOnSide(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing facing) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ(); int side = facing.getIndex();"),
 ("com/hbm/blocks/bomb/DigammaFlame.java", 55,
  "public int getRenderType() {",
  "public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {",
  None),
 ("com/hbm/blocks/generic/BlockDeadPlant.java", 81,
  "public int getRenderType() {",
  "public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {",
  None),
 ("com/hbm/blocks/generic/BlockRubberPlant.java", 78,
  "public int getRenderType() {",
  "public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {",
  None),
 ("com/hbm/blocks/gas/BlockGasBase.java", 67,
  "public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {",
  "public boolean shouldSideBeRendered(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing side) {",
  None),
 ("com/hbm/blocks/gas/BlockGasBase.java", 72,
  "public boolean canCollideCheck(int p_149678_1_, boolean p_149678_2_) {",
  "public boolean canCollideCheck(net.minecraft.block.state.IBlockState state, boolean p_149678_2_) {",
  None),
 ("com/hbm/blocks/gas/BlockGasBase.java", 82,
  "public void onBlockAdded(World world, int x, int y, int z) {",
  "public void onBlockAdded(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/bomb/CompactLauncher.java", 78,
  "public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {",
  "public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, EntityLivingBase player, ItemStack itemStack) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/generic/BlockBedrockOreTE.java", 82,
  "public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {",
  "public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, EntityLivingBase entity, ItemStack stack) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/machine/MachineChungus.java", 211,
  "public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {",
  "public void onBlockExploded(World world, net.minecraft.util.math.BlockPos pos, Explosion explosion) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/machine/MachineCondenserPowered.java", 121,
  "public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {",
  "public void onBlockExploded(World world, net.minecraft.util.math.BlockPos pos, Explosion explosion) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/machine/MachineOilWell.java", 119,
  "public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {",
  "public void onBlockExploded(World world, net.minecraft.util.math.BlockPos pos, Explosion explosion) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/generic/BlockNTMFlower.java", 195,
  "public boolean func_149851_a(World world, int x, int y, int z, boolean b) {",
  "public boolean canGrow(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, boolean b) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/generic/BlockNTMFlower.java", 218,
  "public boolean func_149852_a(World world, Random rand, int x, int y, int z) {",
  "public boolean canUseBonemeal(World world, Random rand, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/generic/BlockNTMFlower.java", 231,
  "public void func_149853_b(World world, Random rand, int x, int y, int z) {",
  "public void grow(World world, Random rand, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/generic/BlockNTMGlassPane.java", 36,
  "public boolean canPaneConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {",
  "public boolean canPaneConnectTo(net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing dir) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/generic/BlockPinkSlab.java", 30,
  "protected ItemStack createStackedBlock(int i) {",
  "protected ItemStack createStackedBlock(net.minecraft.block.state.IBlockState state) {",
  "\t\tint i = com.hbm.world.WorldBlockState.metadata(state);"),
 ("com/hbm/blocks/generic/BlockPorous.java", 18,
  "protected ItemStack createStackedBlock(int meta) {",
  "protected ItemStack createStackedBlock(net.minecraft.block.state.IBlockState state) {",
  None),
 ("com/hbm/blocks/generic/BlockPorous.java", 24,
  "public boolean isReplaceableOreGen(World world, int x, int y, int z, Block target) {",
  "public boolean isReplaceableOreGen(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, com.google.common.base.Predicate<net.minecraft.block.state.IBlockState> target) {",
  None),
 ("com/hbm/blocks/generic/ReinforcedLamp.java", 80,
  "protected ItemStack createStackedBlock(int e) {",
  "protected ItemStack createStackedBlock(net.minecraft.block.state.IBlockState state) {",
  None),
 ("com/hbm/blocks/generic/TritiumLamp.java", 106,
  "protected ItemStack createStackedBlock(int e) {",
  "protected ItemStack createStackedBlock(net.minecraft.block.state.IBlockState state) {",
  None),
 ("com/hbm/blocks/machine/BlockDecon.java", 48,
  "public void randomDisplayTick(World p_149734_1_, int x, int y, int z, Random rand) { }",
  "public void randomDisplayTick(net.minecraft.block.state.IBlockState state, World world, net.minecraft.util.math.BlockPos pos, Random rand) { }",
  None),
 ("com/hbm/blocks/machine/MachineDiesel.java", 41,
  "public void randomDisplayTick(World world, int x, int y, int z, Random rand) {",
  "public void randomDisplayTick(net.minecraft.block.state.IBlockState state, World world, net.minecraft.util.math.BlockPos pos, Random rand) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/machine/MachineForceField.java", 68,
  "public void randomDisplayTick(World world, int x, int y, int z, Random rand) {",
  "public void randomDisplayTick(net.minecraft.block.state.IBlockState state, World world, net.minecraft.util.math.BlockPos pos, Random rand) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/blocks/generic/BlockSellafield.java", 86,
  "public void getSubBlocks(Item item, CreativeTabs tabs, List list) {",
  "public void getSubBlocks(net.minecraft.creativetab.CreativeTabs tabs, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {",
  None),
 ("com/hbm/blocks/generic/BlockSellafieldOre.java", 89,
  "public int getExpDrop(IBlockAccess world, int meta, int fortune) {",
  "public int getExpDrop(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, int fortune) {",
  "\t\tint meta = com.hbm.world.WorldBlockState.metadata(state);"),
 ("com/hbm/blocks/generic/BlockStalagmite.java", 81,
  "public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {",
  "public net.minecraft.util.math.AxisAlignedBB getBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess source, net.minecraft.util.math.BlockPos pos) {",
  None),
 ("com/hbm/commands/CommandLocate.java", 121,
  "public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {",
  "public List<String> getTabCompletions(net.minecraft.server.MinecraftServer server, ICommandSender sender, String[] args, net.minecraft.util.math.BlockPos targetPos) {",
  None),
 ("com/hbm/blocks/BlockWaterPlant.java", 51,
  "public int quantityDropped(int meta, int fortune, java.util.Random random) {",
  "public int quantityDropped(net.minecraft.block.state.IBlockState state, int fortune, java.util.Random random) {",
  None),
 ("com/hbm/entity/effect/EntityNukeTorex.java", 64,
  "@Override @SideOnly(Side.CLIENT) public int getBrightnessForRender(float interp) { return 15728880; }",
  "@Override @SideOnly(Side.CLIENT) public int getBrightnessForRender() { return 15728880; }",
  None),
 ("com/hbm/entity/effect/EntityNukeTorex.java", 65,
  "@Override public float getBrightness(float interp) { return 1.0F; }",
  "@Override public float getBrightness() { return 1.0F; }",
  None),
 ("com/hbm/entity/projectile/EntityMeteor.java", 180,
  "@Override @SideOnly(Side.CLIENT) public int getBrightnessForRender(float f) { return 15728880; }",
  "@Override @SideOnly(Side.CLIENT) public int getBrightnessForRender() { return 15728880; }",
  None),
 ("com/hbm/entity/projectile/EntityMeteor.java", 181,
  "@Override public float getBrightness(float f) { return 1.0F; }",
  "@Override public float getBrightness() { return 1.0F; }",
  None),
 ("com/hbm/entity/item/EntityItemWaste.java", 23,
  "public boolean isEntityInvulnerable() {",
  "public boolean isEntityInvulnerable(net.minecraft.util.DamageSource source) {",
  None),
 ("com/hbm/entity/mob/EntityGhost.java", 59,
  "public boolean isEntityInvulnerable() {",
  "public boolean isEntityInvulnerable(net.minecraft.util.DamageSource source) {",
  None),
 ("com/hbm/entity/mob/EntityQuackos.java", 180,
  "public boolean allowLeashing() {",
  "public boolean canBeLeashedTo(EntityPlayer player) {",
  None),
 ("com/hbm/entity/mob/EntityWarBehemoth.java", 96,
  "protected String getHurtSound()",
  "protected net.minecraft.util.SoundEvent getHurtSound(net.minecraft.util.DamageSource src)",
  None),
 ("com/hbm/entity/mob/siege/EntitySiegeUFO.java", 178,
  "public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {",
  "public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, IEntityLivingData data) {",
  None),
 ("com/hbm/entity/projectile/EntityExplosiveBeam.java", 190,
  "public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_)",
  "public void setPositionAndRotationDirect(double p_70056_1_, double p_70056_3_, double p_70056_5_, float p_70056_7_, float p_70056_8_, int p_70056_9_, boolean teleport)",
  None),
 ("com/hbm/blocks/generic/BlockDoorGeneric.java", 173,
  "public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {",
  "public net.minecraft.util.math.AxisAlignedBB getBoundingBox(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/inventory/gui/GUICombustionEngine.java", 101,
  "protected void mouseMovedOrUp(int x, int y, int i) {",
  "protected void mouseReleased(int x, int y, int i) {",
  None),
 ("com/hbm/inventory/gui/GUIMachineStardar.java", 430,
  "protected void mouseMovedOrUp(int x, int y, int which) {",
  "protected void mouseReleased(int x, int y, int which) {",
  None),
 ("com/hbm/items/tool/ItemDefuser.java", 27,
  "public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity) {",
  "public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, net.minecraft.util.EnumHand hand) {",
  None),
 ("com/hbm/items/weapon/sedna/impl/ItemGunDrill.java", 24,
  "public int getHarvestLevel(ItemStack stack, String toolClass) {",
  "public int getHarvestLevel(ItemStack stack, String toolClass, net.minecraft.entity.player.EntityPlayer player, net.minecraft.block.state.IBlockState state) {",
  None),
 ("com/hbm/items/weapon/sedna/impl/ItemGunDrill.java", 29,
  "public boolean canHarvestBlock(Block par1Block, ItemStack itemStack) {",
  "public boolean canHarvestBlock(net.minecraft.block.state.IBlockState state, ItemStack itemStack) {",
  None),
 ("com/hbm/world/gen/MapGenNTMFeatures.java", 50,
  "public String func_143025_a() {",
  "public String getStructureName() {",
  None),
]

REP = [
 ("com/hbm/blocks/bomb/DigammaFlame.java", 56, "return 1;", "return net.minecraft.util.EnumBlockRenderType.MODEL;"),
 ("com/hbm/blocks/generic/BlockDeadPlant.java", 82, "return 1;", "return net.minecraft.util.EnumBlockRenderType.MODEL;"),
 ("com/hbm/blocks/generic/BlockRubberPlant.java", 79, "return 1;", "return net.minecraft.util.EnumBlockRenderType.MODEL;"),
 ("com/hbm/blocks/generic/BlockNTMGlassPane.java", 38, "return super.canPaneConnectTo(world, x, y, z, dir) || b instanceof BlockNTMGlass;", "return super.canPaneConnectTo(world, pos, dir) || b instanceof BlockNTMGlass;"),
 ("com/hbm/blocks/generic/BlockPorous.java", 25, "return target == this || target == Blocks.STONE;", "return state.getBlock() == this || state.getBlock() == Blocks.STONE;"),
 ("com/hbm/blocks/generic/BlockSellafield.java", 88, "list.add(new ItemStack(item, 1, i));", "list.add(new ItemStack(Item.getItemFromBlock(this), 1, i));"),
 ("com/hbm/entity/mob/EntityWarBehemoth.java", 98, 'return "hbm:entity.cybercrab";', 'return com.hbm.main.ModSoundEvents.resolve("hbm:entity.cybercrab");'),
 ("com/hbm/entity/mob/EntityParasiteMaggot.java", 32, "return this.world.getClosestVulnerablePlayerToEntity(this, 16);", "return this.world.getNearestAttackablePlayer(this, 16, 16);"),
 ("com/hbm/entity/mob/EntityFBI.java", 128, "this.setAttackTarget(this.world.getClosestVulnerablePlayerToEntity(this, 128.0D));", "this.setAttackTarget(this.world.getNearestAttackablePlayer(this, 128.0D, 128.0D));"),
 ("com/hbm/entity/mob/EntityDummy.java", 26, "return super.interact(player);", "return super.processInitialInteract(player, net.minecraft.util.EnumHand.MAIN_HAND);"),
 ("com/hbm/entity/mob/EntityQuackos.java", 99, "if(super.interact(player)) {", "if(super.processInitialInteract(player, net.minecraft.util.EnumHand.MAIN_HAND)) {"),
 ("com/hbm/inventory/gui/GUICombustionEngine.java", 102, "super.mouseMovedOrUp(x, y, i);", "super.mouseReleased(x, y, i);"),
 ("com/hbm/inventory/gui/GUIMachineStardar.java", 431, "super.mouseMovedOrUp(x, y, which);", "super.mouseReleased(x, y, which);"),
]

INSERT = [
 # (rel, anchor_frag, lines_to_insert_after_anchor)  -- anchor must be unique in file
 ("com/hbm/crafting/handlers/TestCraftingHandler.java", None, None),  # placeholder removed
]

def load(path):
    raw = open(path, 'rb').read()
    text = raw.decode('utf-8')
    parts = re.split(r'(\r\n|\r|\n)', text)
    return parts[0::2], parts[1::2] + ['']

def save(path, contents, terms):
    out = ''.join(c + t for c, t in zip(contents, terms))
    open(path, 'wb').write(out.encode('utf-8'))

def find_line(contents, lineno, frag):
    a, b = max(0, lineno - 5), min(len(contents), lineno + 4)
    hits = [i for i in range(a, b) if frag in contents[i]]
    return hits[0] if len(hits) == 1 else None

def main():
    apply = '--apply' in sys.argv
    fails = []
    done = 0
    for rel, lineno, frag in RM:
        path = os.path.join(ROOT, 'src', 'main', 'java', rel.replace('/', os.sep))
        try:
            contents, terms = load(path)
        except OSError as e:
            fails.append((rel, lineno, 'MISSING', str(e))); continue
        i = find_line(contents, lineno, frag)
        if i is None:
            fails.append((rel, lineno, 'RM-MISS', frag[:60])); continue
        j = i - 1
        while j >= 0 and contents[j].strip().startswith('@SideOnly'):
            j -= 1
        if j < 0 or not contents[j].strip().startswith('@Override'):
            fails.append((rel, lineno, 'RM-MISS', frag[:60])); continue
        del contents[j]; del terms[j]
        if apply: save(path, contents, terms)
        done += 1
    for rel, lineno, old, new, adapter in DECL:
        path = os.path.join(ROOT, 'src', 'main', 'java', rel.replace('/', os.sep))
        try:
            contents, terms = load(path)
        except OSError as e:
            fails.append((rel, lineno, 'MISSING', str(e))); continue
        i = find_line(contents, lineno, old)
        if i is None:
            fails.append((rel, lineno, 'DECL-MISS', old[:60])); continue
        contents[i] = contents[i].replace(old, new)
        if adapter:
            indent = contents[i][:len(contents[i]) - len(contents[i].lstrip())]
            contents.insert(i + 1, adapter if adapter.startswith('\t') else indent + adapter)
            terms.insert(i + 1, terms[i])
        if apply: save(path, contents, terms)
        done += 1
    for rel, lineno, old, new in REP:
        path = os.path.join(ROOT, 'src', 'main', 'java', rel.replace('/', os.sep))
        try:
            contents, terms = load(path)
        except OSError as e:
            fails.append((rel, lineno, 'MISSING', str(e))); continue
        i = find_line(contents, lineno, old)
        if i is None:
            fails.append((rel, lineno, 'REP-MISS', old[:60])); continue
        contents[i] = contents[i].replace(old, new)
        if apply: save(path, contents, terms)
        done += 1
    total = len(RM) + len(DECL) + len(REP)
    for f in fails:
        print('FAIL %-72s L%-5d %s | %s' % f)
    print('total ops: %d, ok: %d, failed: %d, mode: %s' % (total, done, len(fails), 'APPLY' if apply else 'PREVIEW'))
    if fails:
        sys.exit(1)

if __name__ == '__main__':
    main()

# Wave 14 Final Batch: fix all 70 remaining diagnostics
import os
import sys
import re

BASE_DIR = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2"
SRC_DIR = os.path.join(BASE_DIR, "src", "main", "java")

# List of operations: (rel_path, target_snippet, replacement_snippet)
OPS = [
    # 1. BlockOreBasalt.java: swap pos and state
    (
        "com/hbm/blocks/generic/BlockOreBasalt.java",
        "\t@Override\n\tpublic void dropBlockAsItemWithChance(World world, net.minecraft.block.state.IBlockState state, net.minecraft.util.math.BlockPos pos, float chance, int fortune) {",
        "\t@Override\n\tpublic void dropBlockAsItemWithChance(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, float chance, int fortune) {"
    ),
    # 2. BlockNTMSand.java: FallingBlock constructor
    (
        "com/hbm/blocks/generic/BlockNTMSand.java",
        "EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, x + 0.5, y + 0.5, z + 0.5, this, com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ));",
        "EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, x + 0.5, y + 0.5, z + 0.5, com.hbm.world.WorldBlockState.read(world, x, y, z));"
    ),
    # 3. IToolHarvestAbility.java: getDrops with BlockPos
    (
        "com/hbm/handler/ability/IToolHarvestAbility.java",
        "List<ItemStack> drops = block.getDrops(world, x, y, z, com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ), 0);",
        "List<ItemStack> drops = block.getDrops(world, new net.minecraft.util.math.BlockPos(x, y, z), com.hbm.world.WorldBlockState.read(world, x, y, z), 0);"
    ),
    # 4. EntityMissileBaseNT.java: tracker.lastYaw reflection
    (
        "com/hbm/entity/missile/EntityMissileBaseNT.java",
        "if(tracker != null) tracker.lastYaw += 100; //coax the tracker into sending smother updates",
        "if(tracker != null) { int cur = net.minecraftforge.fml.relauncher.ReflectionHelper.getPrivateValue(net.minecraft.entity.EntityTrackerEntry.class, tracker, \"field_73127_g\", \"encodedRotationYaw\"); net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.entity.EntityTrackerEntry.class, tracker, cur + 100, \"field_73127_g\", \"encodedRotationYaw\"); } //coax the tracker into sending smoother updates"
    ),
    # 5. BlockAstroturf.java: icon
    (
        "com/hbm/blocks/BlockAstroturf.java",
        "return side == 1 ? this.iconTop : (side == 0 ? Blocks.DIRT.getBlockTextureFromSide(side) : this.blockIcon);",
        "return side == 1 ? this.iconTop : this.blockIcon;"
    ),
    # 6. BlockRemap.java: getIcon
    (
        "com/hbm/blocks/BlockRemap.java",
        "\t@SideOnly(Side.CLIENT)\n\tpublic IIcon getIcon(int meta, int side) {\n\t\treturn this.remapBlock.getIcon(meta, side);\n\t}",
        "\t@SideOnly(Side.CLIENT)\n\tpublic IIcon getIcon(int meta, int side) {\n\t\tif(this.remapBlock instanceof BlockBase) return ((BlockBase) this.remapBlock).getIcon(side, meta);\n\t\treturn null;\n\t}"
    ),
    # 7. BlockRubberFarm.java: getIcon
    (
        "com/hbm/blocks/BlockRubberFarm.java",
        "return ModBlocks.rubber_silt.getBlockTextureFromSide(side);",
        "return ((BlockBase) ModBlocks.rubber_silt).getIcon(side, 0);"
    ),
    # 8. BlockWaterPlant.java: canPlaceBlockAt
    (
        "com/hbm/blocks/BlockWaterPlant.java",
        "return super.canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z)) && this.canBlockStay(world, x, y, z) && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().getDefaultState().getMaterial().isLiquid();",
        "return super.canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x, y, z)) && com.hbm.world.WorldBlockState.read(world, x, y + 1, z).getBlock().getDefaultState().getMaterial().isLiquid();"
    ),
    # 9. BlockTaint.java: EntityFallingBlock ctor
    (
        "com/hbm/blocks/bomb/BlockTaint.java",
        "EntityFallingBlock falling = new EntityFallingBlock(world, x + i + 0.5, y + j + 0.5, z + k + 0.5, this, targetMeta);",
        "EntityFallingBlock falling = new EntityFallingBlock(world, x + i + 0.5, y + j + 0.5, z + k + 0.5, this.getStateFromMeta(targetMeta));"
    ),
    # 10. Landmine.java: onBlockDestroyedByPlayer
    (
        "com/hbm/blocks/bomb/Landmine.java",
        "super.onBlockDestroyedByPlayer(world, x, y, z, meta);",
        "super.onBlockDestroyedByPlayer(world, pos, state);"
    ),
    # 11. BlockDeadPlant.java: dropBlockAsItem and remove @Override on canBlockStay
    (
        "com/hbm/blocks/generic/BlockDeadPlant.java",
        "this.dropBlockAsItem(world, x, y, z, com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ), 0);",
        "this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), com.hbm.world.WorldBlockState.read(world, x, y, z), 0);"
    ),
    (
        "com/hbm/blocks/generic/BlockDeadPlant.java",
        "\t\t@Override\n\tpublic boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {",
        "\tpublic boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {"
    ),
    # 12. BlockLogNT.java: declare icon arrays
    (
        "com/hbm/blocks/generic/BlockLogNT.java",
        "\tprivate String sideIcon;\n\tprivate String topIcon;",
        "\tprotected IIcon[] field_150167_a;\n\tprotected IIcon[] field_150166_b;\n\tprivate String sideIcon;\n\tprivate String topIcon;"
    ),
    # 13. BlockTallPlant.java: remove @Override on canBlockStay
    (
        "com/hbm/blocks/generic/BlockTallPlant.java",
        "\t\t@Override\n\tpublic boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {",
        "\tpublic boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {"
    ),
    # 14. BlockPinkSlab.java: getSilkTouchDrop
    (
        "com/hbm/blocks/generic/BlockPinkSlab.java",
        "protected ItemStack createStackedBlock(net.minecraft.block.state.IBlockState state) {",
        "protected ItemStack getSilkTouchDrop(net.minecraft.block.state.IBlockState state) {"
    ),
    # 15. BlockPorous.java: getSilkTouchDrop
    (
        "com/hbm/blocks/generic/BlockPorous.java",
        "protected ItemStack createStackedBlock(net.minecraft.block.state.IBlockState state) {",
        "protected ItemStack getSilkTouchDrop(net.minecraft.block.state.IBlockState state) {"
    ),
    # 16. BlockRotatablePillar.java: declare field_150164_N
    (
        "com/hbm/blocks/generic/BlockRotatablePillar.java",
        "\t@SideOnly(Side.CLIENT)\n\tprotected IIcon iconSide;",
        "\t@SideOnly(Side.CLIENT)\n\tprotected IIcon iconSide;\n\t@SideOnly(Side.CLIENT)\n\tprotected IIcon field_150164_N;"
    ),
    # 17. BlockRubberPlant.java: dropBlockAsItem and remove @Override on canBlockStay
    (
        "com/hbm/blocks/generic/BlockRubberPlant.java",
        "this.dropBlockAsItem(world, x, y, z, com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ), 0);",
        "this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), com.hbm.world.WorldBlockState.read(world, x, y, z), 0);"
    ),
    (
        "com/hbm/blocks/generic/BlockRubberPlant.java",
        "\t\t@Override\n\tpublic boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {",
        "\tpublic boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {"
    ),
    # 18. BlockSellafieldSlaked.java: canCreatureSpawn
    (
        "com/hbm/blocks/generic/BlockSellafieldSlaked.java",
        "\tpublic boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {\n\t\treturn this == ModBlocks.sellafield_bedrock ? false : super.canCreatureSpawn(type, world, x, y, z);\n\t}",
        "\t@Override\n\tpublic boolean canCreatureSpawn(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.entity.EntityLiving.SpawnPlacementType type) {\n\t\treturn this == ModBlocks.sellafield_bedrock ? false : super.canCreatureSpawn(state, world, pos, type);\n\t}"
    ),
    # 19. BlockTallPlantWater.java: remove @Override on canBlockStay
    (
        "com/hbm/blocks/generic/BlockTallPlantWater.java",
        "\t\t@Override\n\tpublic boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {",
        "\tpublic boolean canBlockStay(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {"
    ),
    # 20. ReinforcedLamp.java: getSilkTouchDrop
    (
        "com/hbm/blocks/generic/ReinforcedLamp.java",
        "protected ItemStack createStackedBlock(net.minecraft.block.state.IBlockState state) {",
        "protected ItemStack getSilkTouchDrop(net.minecraft.block.state.IBlockState state) {"
    ),
    # 21. TritiumLamp.java: getSilkTouchDrop
    (
        "com/hbm/blocks/generic/TritiumLamp.java",
        "protected ItemStack createStackedBlock(net.minecraft.block.state.IBlockState state) {",
        "protected ItemStack getSilkTouchDrop(net.minecraft.block.state.IBlockState state) {"
    ),
    # 22. BlockCMGlass.java: shouldSideBeRendered
    (
        "com/hbm/blocks/machine/BlockCMGlass.java",
        "\t@SideOnly(Side.CLIENT)\n\tpublic boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {\n\t\tBlock block = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock();\n\n\t\treturn block == this ? false : super.shouldSideBeRendered(world, x, y, z, side);\n\t}",
        "\t@Override\n\t@SideOnly(Side.CLIENT)\n\tpublic boolean shouldSideBeRendered(net.minecraft.block.state.IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing side) {\n\t\tnet.minecraft.block.state.IBlockState neighbor = world.getBlockState(pos.offset(side));\n\t\treturn neighbor.getBlock() == this ? false : super.shouldSideBeRendered(state, world, pos, side);\n\t}"
    ),
    # 23. BlockFurnaceSpace.java: setTileEntity
    (
        "com/hbm/blocks/machine/BlockFurnaceSpace.java",
        "world.setTileEntity(x, y, z, tileentity);",
        "world.setTileEntity(new net.minecraft.util.math.BlockPos(x, y, z), tileentity);"
    ),
    # 24. BlockOrbitalStation.java: removedByPlayer
    (
        "com/hbm/blocks/machine/BlockOrbitalStation.java",
        "\tpublic boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {\n\t\tif(this == ModBlocks.orbital_station) return false; // block removal of main port\n\t\treturn super.removedByPlayer(world, player, x, y, z, willHarvest);\n\t}",
        "\t@Override\n\tpublic boolean removedByPlayer(net.minecraft.block.state.IBlockState state, World world, net.minecraft.util.math.BlockPos pos, EntityPlayer player, boolean willHarvest) {\n\t\tif(this == ModBlocks.orbital_station) return false; // block removal of main port\n\t\treturn super.removedByPlayer(state, world, pos, player, willHarvest);\n\t}"
    ),
    # 25. MachineOilWell.java: onExplosionDestroy
    (
        "com/hbm/blocks/machine/MachineOilWell.java",
        "onBlockDestroyedByExplosion(world, new net.minecraft.util.math.BlockPos(x, y, z), explosion);",
        "onExplosionDestroy(world, new net.minecraft.util.math.BlockPos(x, y, z), explosion);"
    ),
    # 26. MachineTrollDisturber.java: onBlockAdded
    (
        "com/hbm/blocks/machine/MachineTrollDisturber.java",
        "super.onBlockAdded(world, new net.minecraft.util.math.BlockPos(x, y, z));",
        "super.onBlockAdded(world, nativeTickPos, nativeTickState);"
    ),
    # 27. BlockOpenComputersCablePaintable.java: controlsConnectivity
    (
        "com/hbm/blocks/network/BlockOpenComputersCablePaintable.java",
        "\t\tpublic int getColor() {\n\t\t\treturn color.getColor();\n\t\t}",
        "\t\tpublic int getColor() {\n\t\t\treturn color.getColor();\n\t\t}\n\n\t\t@Optional.Method(modid = \"OpenComputers\")\n\t\tpublic boolean controlsConnectivity() {\n\t\t\treturn true;\n\t\t}"
    ),
    # 28. ConnectorRedWireSuper.java: remove @Override on onBlockPlaced
    (
        "com/hbm/blocks/network/ConnectorRedWireSuper.java",
        "\t@Override public int onBlockPlaced",
        "\tpublic int onBlockPlaced"
    ),
    # 29. MachineBatteryREDD.java: dropBlockAsItem with fortune
    (
        "com/hbm/blocks/network/MachineBatteryREDD.java",
        "this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), com.hbm.world.WorldBlockState.read(world, x, y, z));",
        "this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), com.hbm.world.WorldBlockState.read(world, x, y, z), 0);"
    ),
    # 30. BiomeDecoratorCelestial.java: LAKE_LAVA
    (
        "com/hbm/dim/BiomeDecoratorCelestial.java",
        "boolean doGen = TerrainGen.decorate(world, random, chunkPos, net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.LAKE);",
        "boolean doGen = TerrainGen.decorate(world, random, chunkPos, LAKE_LAVA);"
    ),
    # 31. WorldGenRubberPlant.java: getTopSolidOrLiquidBlock
    (
        "com/hbm/dim/WorldGenRubberPlant.java",
        "com.hbm.world.WorldBlockState.writeLegacyBlock(world, px, world.getTopSolidOrLiquidBlock(px, pz), pz, ModBlocks.spike_cacti, EnumBushType.CACT.ordinal(), 2);",
        "com.hbm.world.WorldBlockState.writeLegacyBlock(world, px, world.getTopSolidOrLiquidBlock(new net.minecraft.util.math.BlockPos(px, 0, pz)).getY(), pz, ModBlocks.spike_cacti, EnumBushType.CACT.ordinal(), 2);"
    ),
    # 32. BiomeGenBaseDres.java: public createBiomeDecorator
    (
        "com/hbm/dim/dres/biome/BiomeGenBaseDres.java",
        "protected BiomeDecorator createBiomeDecorator()",
        "public BiomeDecorator createBiomeDecorator()"
    ),
    # 33. WorldChunkManagerCelestial.java: remove @Override on getRainfall
    (
        "com/hbm/dim/WorldChunkManagerCelestial.java",
        "\t@Override\n\tpublic int[] getRainfall",
        "\tpublic int[] getRainfall"
    ),
    # 34. BiomeGenBaseDuna.java: public createBiomeDecorator
    (
        "com/hbm/dim/duna/biome/BiomeGenBaseDuna.java",
        "protected BiomeDecorator createBiomeDecorator()",
        "public BiomeDecorator createBiomeDecorator()"
    ),
    # 35. BiomeGenBaseEve.java: public createBiomeDecorator
    (
        "com/hbm/dim/eve/biome/BiomeGenBaseEve.java",
        "protected BiomeDecorator createBiomeDecorator()",
        "public BiomeDecorator createBiomeDecorator()"
    ),
    # 36. BiomeGenIke.java: public createBiomeDecorator
    (
        "com/hbm/dim/Ike/BiomeGenIke.java",
        "protected BiomeDecorator createBiomeDecorator()",
        "public BiomeDecorator createBiomeDecorator()"
    ),
    # 37. BiomeGenBaseLaythe.java: public createBiomeDecorator
    (
        "com/hbm/dim/laythe/biome/BiomeGenBaseLaythe.java",
        "protected BiomeDecorator createBiomeDecorator()",
        "public BiomeDecorator createBiomeDecorator()"
    ),
    # 38. BiomeGenBaseMinmus.java: public createBiomeDecorator
    (
        "com/hbm/dim/minmus/biome/BiomeGenBaseMinmus.java",
        "protected BiomeDecorator createBiomeDecorator()",
        "public BiomeDecorator createBiomeDecorator()"
    ),
    # 39. BiomeGenBaseMoho.java: public createBiomeDecorator
    (
        "com/hbm/dim/moho/biome/BiomeGenBaseMoho.java",
        "protected BiomeDecorator createBiomeDecorator()",
        "public BiomeDecorator createBiomeDecorator()"
    ),
    # 40. BiomeGenMoon.java: drop generateLakes
    (
        "com/hbm/dim/moon/BiomeGenMoon.java",
        "\t\tthis.decorator.generateLakes = false;\n",
        ""
    ),
    # 41. BiomeGenBaseTekto.java: public createBiomeDecorator
    (
        "com/hbm/dim/tekto/biome/BiomeGenBaseTekto.java",
        "protected BiomeDecorator createBiomeDecorator()",
        "public BiomeDecorator createBiomeDecorator()"
    ),
    # 42. BiomeGenThatmo.java: drop generateLakes
    (
        "com/hbm/dim/thatmo/BiomeGenThatmo.java",
        "\t\tthis.decorator.generateLakes = false;\n",
        ""
    ),
    # 43. EntityAIWanderConditional.java: getIdleTime
    (
        "com/hbm/entity/mob/ai/EntityAIWanderConditional.java",
        "if(this.creature.getGrowingAge() >= 100) {",
        "if(this.creature.getIdleTime() >= 100) {"
    ),
    # 44. EntityWormBaseNT.java: predicate T-bounds and EntityWormBaseNT.class
    (
        "com/hbm/entity/mob/botprime/EntityWormBaseNT.java",
        "public static final Predicate<EntityLiving> wormSelector = new Predicate<EntityLiving>() {\n\n\t\t@Override\n\t\tpublic boolean apply(EntityLiving target) {\n\t\t\treturn target instanceof EntityWormBaseNT;\n\t\t}\n\t};",
        "public static final Predicate<Entity> wormSelector = new Predicate<Entity>() {\n\n\t\t@Override\n\t\tpublic boolean apply(Entity target) {\n\t\t\treturn target instanceof EntityWormBaseNT;\n\t\t}\n\t};"
    ),
    (
        "com/hbm/entity/mob/botprime/EntityWormBaseNT.java",
        "findEntityToFollow(this.world.getEntitiesWithinAABB(EntityLiving.class, this.getEntityBoundingBox().grow(this.rangeForParts, this.rangeForParts, this.rangeForParts), EntityWormBaseNT.wormSelector));",
        "findEntityToFollow(this.world.getEntitiesWithinAABB(EntityWormBaseNT.class, this.getEntityBoundingBox().grow(this.rangeForParts, this.rangeForParts, this.rangeForParts), EntityWormBaseNT.wormSelector));"
    ),
    # 45. EntityBOTPrimeBase.java: getDeathSound
    (
        "com/hbm/entity/mob/botprime/EntityBOTPrimeBase.java",
        "\tprotected String getDeathSound() {\n\t\treturn \"hbm:entity.bombDet\";\n\t}",
        "\t@Override\n\tprotected net.minecraft.util.SoundEvent getDeathSound() {\n\t\treturn com.hbm.main.ModSoundEvents.resolve(\"hbm:entity.bombDet\");\n\t}"
    ),
    # 46. EntityCreeperNuclear.java: fuseTime
    (
        "com/hbm/entity/mob/EntityCreeperNuclear.java",
        "this.fuseTime = 75;",
        "net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.entity.monster.EntityCreeper.class, this, 75, \"fuseTime\", \"field_82225_f\");"
    ),
    # 47. EntityCreeperPhosgene.java: fuseTime
    (
        "com/hbm/entity/mob/EntityCreeperPhosgene.java",
        "this.fuseTime = 20; //ehehehehehe",
        "net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.entity.monster.EntityCreeper.class, this, 20, \"fuseTime\", \"field_82225_f\"); //ehehehehehe"
    ),
    # 48. EntityDuck.java: getDeathSound
    (
        "com/hbm/entity/mob/EntityDuck.java",
        "\tprotected String getDeathSound() {\n\t\treturn \"hbm:entity.ducc\";\n\t}",
        "\t@Override\n\tprotected net.minecraft.util.SoundEvent getDeathSound() {\n\t\treturn com.hbm.main.ModSoundEvents.resolve(\"hbm:entity.ducc\");\n\t}"
    ),
    # 49. EntityQuackos.java: getDeathSound
    (
        "com/hbm/entity/mob/EntityQuackos.java",
        "\tprotected String getDeathSound() {\n\t\treturn \"hbm:entity.megaquacc\";\n\t}",
        "\t@Override\n\tprotected net.minecraft.util.SoundEvent getDeathSound() {\n\t\treturn com.hbm.main.ModSoundEvents.resolve(\"hbm:entity.megaquacc\");\n\t}"
    ),
    # 50. EntityGlyphidDigger.java: getExplosionResistance
    (
        "com/hbm/entity/mob/glyphid/EntityGlyphidDigger.java",
        "float k = b.getExplosionResistance(this, this.world, x1, y1, z1, posX, posY, posZ);",
        "float k = b.getExplosionResistance(this.world, new net.minecraft.util.math.BlockPos(x1, y1, z1), this, null);"
    ),
    # 51. BlockMutatorBulkie.java: isNormalCube
    (
        "com/hbm/explosion/vanillant/standard/BlockMutatorBulkie.java",
        "if(!block.isNormalCube()) return;",
        "if(!block.isNormalCube(block.getStateFromMeta(meta))) return;"
    ),
    # 52. BossSpawnHandler.java: stats
    (
        "com/hbm/handler/BossSpawnHandler.java",
        "\t\t\t\t\tint id = Item.getIdFromItem(Item.getItemFromBlock(ModBlocks.machine_crystallizer));\n\n\t\t\t\t\tStatBase statCraft = StatList.objectCraftStats[id];\n\t\t\t\t\tStatBase statPlace = StatList.objectUseStats[id];\n\n\t\t\t\t\tif(!(player instanceof EntityPlayerMP)) return;\n\t\t\t\t\tEntityPlayerMP playerMP = (EntityPlayerMP) player;\n\n\t\t\t\t\tboolean acidizerStat = !GeneralConfig.enableStatReRegistering || (statCraft != null && playerMP.func_147099_x().writeStat(statCraft) > 0)|| (statPlace != null && playerMP.func_147099_x().writeStat(statPlace) > 0);",
        "\t\t\t\t\tItem item = Item.getItemFromBlock(ModBlocks.machine_crystallizer);\n\n\t\t\t\t\tStatBase statCraft = StatList.getCraftStats(item);\n\t\t\t\t\tStatBase statPlace = StatList.getObjectUseStats(item);\n\n\t\t\t\t\tif(!(player instanceof EntityPlayerMP)) return;\n\t\t\t\t\tEntityPlayerMP playerMP = (EntityPlayerMP) player;\n\n\t\t\t\t\tboolean acidizerStat = !GeneralConfig.enableStatReRegistering || (statCraft != null && playerMP.getStatFile().readStat(statCraft) > 0)|| (statPlace != null && playerMP.getStatFile().readStat(statPlace) > 0);"
    ),
    # 53. ChunkRadiationHandler3D.java: markDirty
    (
        "com/hbm/handler/radiation/ChunkRadiationHandler3D.java",
        "world.getChunk(new net.minecraft.util.math.BlockPos(x, 0, z)).isModified = true;",
        "world.getChunk(new net.minecraft.util.math.BlockPos(x, 0, z)).markDirty();"
    ),
    # 54. ContainerArmorTable.java: getSlotTexture
    (
        "com/hbm/inventory/container/ContainerArmorTable.java",
        "\t\t\t\t@SideOnly(Side.CLIENT)\n\t\t\t\tpublic IIcon getBackgroundIconIndex() {\n\t\t\t\t\treturn ItemArmor.func_94602_b(k);\n\t\t\t\t}",
        "\t\t\t\t@SideOnly(Side.CLIENT)\n\t\t\t\tpublic String getSlotTexture() {\n\t\t\t\t\treturn ItemArmor.EMPTY_SLOT_NAMES[k];\n\t\t\t\t}"
    ),
    # 55. GUIPump.java: partialTicks in drawButton
    (
        "com/hbm/inventory/gui/GUIPump.java",
        "\t\tbuttonPressure.drawButton(mc, mouseX, mouseY);\n\n\t\tdrawString(fontRenderer, \"Priority:\", this.width / 2 + 50, 80, 0xA0A0A0);\n\t\tbuttonPriority.drawButton(mc, mouseX, mouseY);",
        "\t\tbuttonPressure.drawButton(mc, mouseX, mouseY, partialTicks);\n\n\t\tdrawString(fontRenderer, \"Priority:\", this.width / 2 + 50, 80, 0xA0A0A0);\n\t\tbuttonPriority.drawButton(mc, mouseX, mouseY, partialTicks);"
    ),
    # 56. ItemModHeavyBoots.java: swingProgress
    (
        "com/hbm/items/armor/ItemModHeavyBoots.java",
        "modelbiped.onGround = ((net.minecraft.client.model.ModelBiped) renderer.getMainModel()).onGround;",
        "modelbiped.swingProgress = ((net.minecraft.client.model.ModelBiped) renderer.getMainModel()).swingProgress;"
    ),
    # 57. ItemEnergy.java: onItemUseFinish
    (
        "com/hbm/items/food/ItemEnergy.java",
        "return super.onEaten(stack, world, player);",
        "return super.onItemUseFinish(stack, world, entity);"
    ),
    # 58. ItemLemon.java: onItemUseFinish
    (
        "com/hbm/items/food/ItemLemon.java",
        "    public ItemStack onEaten(ItemStack stack, World worldObj, EntityPlayer player)\n    {\n        ItemStack sta = super.onEaten(stack, worldObj, player);",
        "    @Override\n    public ItemStack onItemUseFinish(ItemStack stack, World worldObj, net.minecraft.entity.EntityLivingBase entityLiving)\n    {\n        ItemStack sta = super.onItemUseFinish(stack, worldObj, entityLiving);"
    ),
    # 59. EntityWarBehemoth.java: add newRotationYaw
    (
        "com/hbm/entity/mob/EntityWarBehemoth.java",
        "    public double headTargetYaw;",
        "    public double headTargetYaw;\n    public double newRotationYaw;"
    ),
]

def normalize_newlines(text):
    return text.replace("\r\n", "\n").replace("\r", "\n")

def apply_op(content, target, replacement, is_crlf):
    norm_content = normalize_newlines(content)
    norm_target = normalize_newlines(target)
    norm_replacement = normalize_newlines(replacement)

    if norm_target not in norm_content:
        return None, f"Target not found: {repr(norm_target[:60])}"

    count = norm_content.count(norm_target)
    if count > 1:
        return None, f"Target found {count} times (not unique): {repr(norm_target[:60])}"

    new_norm = norm_content.replace(norm_target, norm_replacement, 1)
    if is_crlf:
        new_content = new_norm.replace("\n", "\r\n")
    else:
        new_content = new_norm
    return new_content, "OK"

def main():
    apply_mode = "--apply" in sys.argv
    print(f"Running Wave 14 ({'APPLY' if apply_mode else 'PREVIEW'}). Operations: {len(OPS)}")

    # Group operations by file
    from collections import defaultdict
    ops_by_file = defaultdict(list)
    for op in OPS:
        ops_by_file[op[0]].append((op[1], op[2]))

    success_files = 0
    total_files = len(ops_by_file)

    for rel_path, file_ops in ops_by_file.items():
        full_path = os.path.join(SRC_DIR, rel_path)
        if not os.path.exists(full_path):
            print(f"ERROR: File not found: {rel_path}")
            continue

        with open(full_path, "rb") as f:
            raw = f.read()

        is_crlf = b"\r\n" in raw
        text = raw.decode("utf-8", errors="replace")

        current_text = text
        all_ok = True
        for target, replacement in file_ops:
            new_text, status = apply_op(current_text, target, replacement, is_crlf)
            if status != "OK":
                print(f"FAIL in {rel_path}: {status}")
                all_ok = False
                break
            current_text = new_text

        if all_ok:
            if apply_mode:
                with open(full_path, "wb") as f:
                    f.write(current_text.encode("utf-8"))
                print(f"APPLIED: {rel_path} ({len(file_ops)} ops)")
            else:
                print(f"VERIFIED: {rel_path} ({len(file_ops)} ops)")
            success_files += 1
        else:
            print(f"SKIPPED (errors): {rel_path}")

    print(f"\nResult: {success_files}/{total_files} files successful.")

if __name__ == "__main__":
    main()

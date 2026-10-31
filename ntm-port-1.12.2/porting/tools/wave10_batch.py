#!/usr/bin/env python3
# CP167 wave 10: line-driven CFS renames + BlockPos adapters for flagged lines only.
# Usage: python wave10_batch.py            (preview)
#        python wave10_batch.py --apply
import sys, os, re

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')

# (relpath, line, old, new)  -- old must occur on exactly one line within [line-4, line+4];
# ALL occurrences of old on that line are replaced.
OPS = [
 # --- BlockFalling.func_149831_e -> canFallThrough(IBlockState)
 ("com/hbm/blocks/generic/BlockNTMSand.java", 44, "BlockFalling.func_149831_e(world, x, y - 1, z)", "BlockFalling.canFallThrough(com.hbm.world.WorldBlockState.read(world, x, y - 1, z))"),
 ("com/hbm/blocks/generic/BlockNTMSand.java", 55, "BlockFalling.func_149831_e(world, x, y - 1, z)", "BlockFalling.canFallThrough(com.hbm.world.WorldBlockState.read(world, x, y - 1, z))"),
 ("com/hbm/blocks/bomb/BlockTaint.java", 86, "BlockFalling.func_149831_e(world, x + i, y + j - 1, z + k)", "BlockFalling.canFallThrough(com.hbm.world.WorldBlockState.read(world, x + i, y + j - 1, z + k))"),
 # --- EntityItem.setItemStack -> setItem
 ("com/hbm/entity/mob/ai/EntityAIEatBread.java", 65, ".setItemStack(stack)", ".setItem(stack)"),
 ("com/hbm/items/special/ItemDemonCore.java", 20, ".setItemStack(new ItemStack(ModItems.demon_core_closed))", ".setItem(new ItemStack(ModItems.demon_core_closed))"),
 ("com/hbm/items/special/ItemRag.java", 26, ".setItemStack(new ItemStack(ModItems.rag_damp, stack.getCount()))", ".setItem(new ItemStack(ModItems.rag_damp, stack.getCount()))"),
 # --- Chunk.isModified -> markDirty()
 ("com/hbm/handler/radiation/ChunkRadiationHandlerSimple.java", 55, "world.getChunk(new net.minecraft.util.math.BlockPos(x, 0, z)).isModified = true;", "world.getChunk(new net.minecraft.util.math.BlockPos(x, 0, z)).markDirty();"),
 ("com/hbm/packet/toclient/BiomeSyncPacket.java", 104, "chunk.isModified = true;", "chunk.markDirty();"),
 # --- IInventory legacy name methods
 ("com/hbm/inventory/gui/GUICartDestroyer.java", 30, "hasCustomInventoryName() ? this.cart.getInventoryName() : I18n.format(this.cart.getInventoryName())", "hasCustomName() ? this.cart.getName() : I18n.format(this.cart.getName())"),
 ("com/hbm/inventory/gui/GUIMachineStrandCaster.java", 47, "hasCustomInventoryName() ? this.caster.getInventoryName() : I18n.format(this.caster.getInventoryName())", "hasCustomName() ? this.caster.getName() : I18n.format(this.caster.getName())"),
 # --- ItemFood legacy SRGs
 ("com/hbm/inventory/recipes/LiquefactionRecipes.java", 100, "food.func_150905_g(stack)", "food.getHealAmount(stack)"),
 ("com/hbm/inventory/recipes/LiquefactionRecipes.java", 100, "food.func_150906_h(stack)", "food.getSaturationModifier(stack)"),
 # --- ItemDye SRGs
 ("com/hbm/blocks/bomb/BlockFireworks.java", 86, "ItemDye.field_150922_c", "ItemDye.DYE_COLORS"),
 ("com/hbm/blocks/generic/BlockEmitter.java", 71, "ItemDye.field_150922_c", "ItemDye.DYE_COLORS"),
 # --- World collision queries
 ("com/hbm/entity/effect/EntityMist.java", 323, "getCollidingBoundingBoxes", "getCollisionBoxes"),
 ("com/hbm/entity/effect/EntityMist.java", 323, "isAnyLiquid", "containsAnyLiquid"),
 ("com/hbm/entity/mob/EntityUFOBase.java", 131, "getCollidingBoundingBoxes", "getCollisionBoxes"),
 ("com/hbm/items/tool/ItemBoatRubber.java", 90, "getCollidingBoundingBoxes", "getCollisionBoxes"),
 ("com/hbm/particle/psys/engine/PSysFXMoving.java", 59, "getCollidingBoundingBoxes", "getCollisionBoxes"),
 # --- PathNavigate legacy mutators -> NodeProcessor
 ("com/hbm/entity/mob/ai/EntityAISwimmingConditional.java", 22, "living.getNavigator().setCanSwim(true)", "living.getNavigator().getNodeProcessor().setCanSwim(true)"),
 ("com/hbm/entity/mob/EntityFish.java", 20, "this.getNavigator().setCanSwim(true)", "this.getNavigator().getNodeProcessor().setCanSwim(true)"),
 ("com/hbm/entity/mob/EntityBlockSpider.java", 24, "this.getNavigator().setAvoidsWater(true)", "this.getNavigator().getNodeProcessor().setCanSwim(false)"),
 ("com/hbm/entity/mob/EntityWarBehemoth.java", 47, "this.getNavigator().setAvoidsWater(true)", "this.getNavigator().getNodeProcessor().setCanSwim(false)"),
 ("com/hbm/entity/mob/ai/EntityAIFireGun.java", 64, "clearPathEntity()", "clearPath()"),
 ("com/hbm/entity/mob/EntityFBI.java", 47, "this.getNavigator().setBreakDoors(true)", "this.getNavigator().getNodeProcessor().setCanEnterDoors(true)"),
 # --- AI target/age renames
 ("com/hbm/entity/mob/ai/EntityAIPanicConditional.java", 31, "this.creature.getAITarget()", "this.creature.getRevengeTarget()"),
 ("com/hbm/entity/mob/ai/EntityAIStartFlying.java", 21, "this.living.getAITarget()", "this.living.getRevengeTarget()"),
 ("com/hbm/entity/mob/ai/EntityAIWanderConditional.java", 31, "this.creature.getAge()", "this.creature.getGrowingAge()"),
 # --- Entity body/portal/movement hooks
 ("com/hbm/entity/mob/botprime/EntityBurrowingNT.java", 34, "!handleLavaMovement()", "!isInLava()"),
 ("com/hbm/entity/mob/botprime/EntityBurrowingNT.java", 42, "moveFlying(strafe, forward, 0.02F)", "moveRelative(strafe, 0.0F, forward, 0.02F)"),
 ("com/hbm/entity/mob/ai/EntityAISwimmingConditional.java", 27, "this.living.handleLavaMovement()", "this.living.isInLava()"),
 ("com/hbm/entity/mob/botprime/EntityWormBaseNT.java", 91, "this.setBeenAttacked()", "this.velocityChanged = true;"),
 ("com/hbm/entity/projectile/EntityThrowableNT.java", 231, "this.setInPortal()", "this.inPortal = true"),
 ("com/hbm/entity/projectile/EntityBulletBeamBase.java", 306, "this.setInPortal()", "this.inPortal = true"),
 ("com/hbm/entity/projectile/EntityBulletBeamBase.java", 331, "@Override @SideOnly", "@SideOnly"),
 ("com/hbm/entity/projectile/EntityBullet.java", 547, "EnchantmentHelper.func_151385_b", "EnchantmentHelper.applyArthropodEnchantments"),
 ("com/hbm/entity/projectile/EntityExplosiveBeam.java", 354, "this.func_145775_I()", "this.doBlockCollisions()"),
 # --- Potions family
 ("com/hbm/util/ContaminationUtil.java", 125, "HbmPotion.stability.id", "HbmPotion.stability"),
 ("com/hbm/items/armor/ArmorEuphemium.java", 39, "Potion.regeneration.id", "net.minecraft.init.MobEffects.REGENERATION"),
 ("com/hbm/items/armor/ArmorEuphemium.java", 40, "Potion.resistance.id", "net.minecraft.init.MobEffects.RESISTANCE"),
 ("com/hbm/items/armor/ArmorEuphemium.java", 41, "Potion.fireResistance.id", "net.minecraft.init.MobEffects.FIRE_RESISTANCE"),
 ("com/hbm/items/armor/ArmorEuphemium.java", 42, "Potion.field_76443_y.id", "net.minecraft.init.MobEffects.SATURATION"),
 ("com/hbm/items/armor/ArmorNCRPA.java", 69, "Potion.nightVision.id", "net.minecraft.init.MobEffects.NIGHT_VISION"),
 ("com/hbm/items/special/ItemStarterKit.java", 412, "Potion.invisibility.id", "net.minecraft.init.MobEffects.INVISIBILITY"),
 ("com/hbm/items/weapon/sedna/factory/Orchestras.java", 1089, "Potion.damageBoost.id", "net.minecraft.init.MobEffects.STRENGTH"),
 ("com/hbm/items/special/ItemSimpleConsumable.java", 102, "new PotionEffect(effect.id, duration, level)", "new PotionEffect(effect, duration, level)"),
 ("com/hbm/items/special/ItemSimpleConsumable.java", 105, "new PotionEffect(effect.id, d, level)", "new PotionEffect(effect, d, level)"),
 ("com/hbm/potion/HbmPotion.java", 59, "super(id, isBad, color);", "super(isBad, color);"),
 ("com/hbm/potion/HbmPotion.java", 182, "new PotionEffect(moveSlowdown.id,5,10)", "new PotionEffect(net.minecraft.init.MobEffects.SLOWNESS,5,10)"),
 ("com/hbm/world/gen/util/LogicBlockInteractions.java", 44, "new PotionEffect(HbmPotion.radaway.getId(), 3 * 60 * 20, 4)", "new PotionEffect(HbmPotion.radaway, 3 * 60 * 20, 4)"),
 ("com/hbm/world/gen/util/LogicBlockInteractions.java", 45, "new PotionEffect(HbmPotion.radx.getId(), 3 * 60 * 20, 4)", "new PotionEffect(HbmPotion.radx, 3 * 60 * 20, 4)"),
 # --- Creative tabs / items
 ("com/hbm/blocks/generic/BlockConcreteColored.java", 24, "CreativeTabs.tabBlock", "CreativeTabs.BUILDING_BLOCKS"),
 ("com/hbm/items/tool/ItemBalefireMatch.java", 16, "CreativeTabs.tabTools", "CreativeTabs.TOOLS"),
 ("com/hbm/items/tool/ItemBoatRubber.java", 23, "CreativeTabs.tabTransport", "CreativeTabs.TRANSPORTATION"),
 ("com/hbm/items/tool/ItemModDoor.java", 19, "CreativeTabs.tabRedstone", "CreativeTabs.REDSTONE"),
 ("com/hbm/items/special/ItemSiegeCoin.java", 39, "this.itemIcon = Items.stick.getIconFromDamage(0);", "this.itemIcon = com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(net.minecraft.init.Items.STICK, 0));"),
 ("com/hbm/items/ItemRemap.java", 27, "return this.remapItem.getIconFromDamage(this.remapMeta);", "return com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(this.remapItem, this.remapMeta));"),
 ("com/hbm/util/ColorUtil.java", 24, "String iconName = stack.getItem().getIconFromDamage(stack.getItemDamage()).getIconName();", "String iconName = net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(stack.getItem(), stack.getItemDamage()).getIconName();"),
 # --- Chunk/world fields
 ("com/hbm/dim/CelestialBody.java", 721, "return chunk.heightMap;", "return chunk.getHeightMap();"),
 ("com/hbm/entity/effect/EntityCloudTom.java", 55, "this.world.lastLightningBolt = 2;", "this.world.setLastLightningBolt(2);"),
 ("com/hbm/entity/effect/EntityNukeTorex.java", 82, "if(ticksExisted < 100) this.world.lastLightningBolt = 2;", "if(ticksExisted < 100) this.world.setLastLightningBolt(2);"),
 # --- EnumCreatureType constants
 ("com/hbm/handler/EntityEffectHandler.java", 866, "EnumCreatureType.waterCreature", "EnumCreatureType.WATER_CREATURE"),
 ("com/hbm/tileentity/bomb/TileEntityLandmine.java", 49, "EnumCreatureType.waterCreature", "EnumCreatureType.WATER_CREATURE"),
 ("com/hbm/tileentity/bomb/TileEntityLandmine.java", 50, "EnumCreatureType.ambient", "EnumCreatureType.AMBIENT"),
 # --- Player misc
 ("com/hbm/items/tool/ItemSurveyScanner.java", 95, "player.travelToDimension(1)", "player.changeDimension(1)"),
 ("com/hbm/saveddata/satellites/SatelliteResonator.java", 21, "((EntityPlayerMP)player).playerNetServerHandler.setPlayerLocation", "((EntityPlayerMP)player).connection.setPlayerLocation"),
 # --- AABB copy()
 ("com/hbm/entity/effect/EntityMist.java", 105, "this.getEntityBoundingBox().copy()", "this.getEntityBoundingBox()"),
 ("com/hbm/entity/mob/EntityUFOBase.java", 126, "this.getEntityBoundingBox().copy()", "this.getEntityBoundingBox()"),
 ("com/hbm/entity/mob/EntityUFOBase.java", 129, "axisalignedbb.offset(d4, d5, d6);", "axisalignedbb = axisalignedbb.offset(d4, d5, d6);"),
 # --- Missile invulnerable call
 ("com/hbm/entity/missile/EntityMissileBaseNT.java", 263, "if(this.isEntityInvulnerable()) {", "if(this.isEntityInvulnerable(source)) {"),
 # --- Tracker hack via ReflectionHelper
 ("com/hbm/entity/projectile/EntityBulletBaseMK4.java", 157, "entry.lastYaw = MathHelper.floor(this.rotationYaw * 256.0F / 360.0F) + 10;", 'net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.entity.EntityTrackerEntry.class, entry, MathHelper.floor(this.rotationYaw * 256.0F / 360.0F) + 10, "field_73127_g", "encodedRotationYaw");'),
 # --- DataWatcher -> DataParameter
 ("com/hbm/render/entity/mob/RenderGlyphid.java", 36, "getWatchableObjectByte(EntityGlyphid.DW_SUBTYPE)", "get(EntityGlyphid.SUBTYPE)"),
 ("com/hbm/render/entity/mob/RenderGlyphid.java", 77, "getWatchableObjectByte(EntityGlyphid.DW_ARMOR)", "get(EntityGlyphid.ARMOR)"),
 ("com/hbm/render/entity/mob/RenderGlyphidNuclear.java", 107, "getWatchableObjectByte(17)", "get(EntityGlyphid.ARMOR)"),
 ("com/hbm/render/entity/mob/RenderBehemoth.java", 58, "getWatchableObjectInt(19)", "get(EntityWarBehemoth.TARGET)"),
 ("com/hbm/items/armor/ItemModDefuser.java", 51, "creeper.getDataManager().updateObject(18, new Byte((byte) 0));", 'net.minecraft.network.datasync.DataParameter<Boolean> ignited = net.minecraftforge.fml.relauncher.ReflectionHelper.getPrivateValue(net.minecraft.entity.monster.EntityCreeper.class, null, "field_184715_c", "IGNITED"); creeper.getDataManager().set(ignited, Boolean.FALSE);'),
 ("com/hbm/tileentity/machine/TileEntityTesla.java", 119, "((EntityCreeper)e).getDataManager().updateObject(17, Byte.valueOf((byte)1));", 'net.minecraft.network.datasync.DataParameter<Boolean> powered = net.minecraftforge.fml.relauncher.ReflectionHelper.getPrivateValue(net.minecraft.entity.monster.EntityCreeper.class, null, "field_184714_b", "POWERED"); ((EntityCreeper)e).getDataManager().set(powered, Boolean.TRUE);'),
 # --- EntityCoin AABB bounds
 ("com/hbm/entity/projectile/EntityCoin.java", 23, "this.getEntityBoundingBox().setBounds(x - f, y - this.yOffset + this.ySize, z - f, x + f, y - this.yOffset + this.ySize + this.height, z + f);", "this.setEntityBoundingBox(new net.minecraft.util.math.AxisAlignedBB(x - f, y, z - f, x + f, y + this.height, z + f));"),
 # --- EntityFish AIWander (EntityWaterMob no longer EntityCreature)
 ("com/hbm/entity/mob/EntityFish.java", 24, "this.tasks.addTask(2, new EntityAIWander(this, swimSpeed));", ""),
 # --- GUI textfield fields
 ("com/hbm/inventory/gui/GUITurretBase.java", 112, "this.field.xPosition", "this.field.x"),
 ("com/hbm/inventory/gui/GUITurretBase.java", 112, "this.field.yPosition", "this.field.y"),
 # --- Drill block harvest checks
 ("com/hbm/items/weapon/sedna/factory/XFactoryDrill.java", 109, "!block.canHarvestBlock(player, meta)", "!block.canHarvestBlock(world, new net.minecraft.util.math.BlockPos(x, y, z), player)"),
 ("com/hbm/items/weapon/sedna/factory/XFactoryDrill.java", 109, "block.getPlayerRelativeBlockHardness(player, world, x, y, z)", "block.getPlayerRelativeBlockHardness(com.hbm.world.WorldBlockState.read(world, x, y, z), player, world, new net.minecraft.util.math.BlockPos(x, y, z))"),
 # --- drawOutlinedBoundingBox -> drawSelectionBoundingBox
 ("com/hbm/items/weapon/sedna/factory/XFactoryDrill.java", 174, "RenderGlobal.drawOutlinedBoundingBox(aabb.expand(exp, exp, exp).offset(mop.getBlockPos().getX() - dX, mop.getBlockPos().getY() - dY, mop.getBlockPos().getZ() - dZ), aoe > 0 ? -1 : 0x800000);", "int outlineColor = aoe > 0 ? -1 : 0x800000; RenderGlobal.drawSelectionBoundingBox(aabb.expand(exp, exp, exp).offset(mop.getBlockPos().getX() - dX, mop.getBlockPos().getY() - dY, mop.getBlockPos().getZ() - dZ), (outlineColor >> 16 & 255) / 255F, (outlineColor >> 8 & 255) / 255F, (outlineColor & 255) / 255F, 1.0F);"),
 ("com/hbm/items/weapon/sedna/factory/XFactoryDrill.java", 178, "RenderGlobal.drawOutlinedBoundingBox(aabb.expand(exp, exp, exp).offset(mop.getBlockPos().getX() - dX, mop.getBlockPos().getY() - dY, mop.getBlockPos().getZ() - dZ), 0x800000);", "int outlineColor = 0x800000; RenderGlobal.drawSelectionBoundingBox(aabb.expand(exp, exp, exp).offset(mop.getBlockPos().getX() - dX, mop.getBlockPos().getY() - dY, mop.getBlockPos().getZ() - dZ), (outlineColor >> 16 & 255) / 255F, (outlineColor >> 8 & 255) / 255F, (outlineColor & 255) / 255F, 1.0F);"),
 ("com/hbm/blocks/machine/MachineCrucible.java", 157, "event.getContext().drawOutlinedBoundingBox(aabb.expand(exp, exp, exp).offset(x - dX + 0.5, y - dY, z - dZ + 0.5), -1);", "event.getContext().drawSelectionBoundingBox(aabb.expand(exp, exp, exp).offset(x - dX + 0.5, y - dY, z - dZ + 0.5), 1.0F, 1.0F, 1.0F, 1.0F);"),
 # --- stack.getIconIndex -> wrapSprite
 ("com/hbm/render/tileentity/RenderBobble.java", 525, "IIcon icon = stack.getIconIndex();", "IIcon icon = com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(stack.getItem(), stack.getItemDamage()));"),
 ("com/hbm/render/tileentity/RenderCatalyticReformer.java", 78, "IIcon icon = stack.getIconIndex();", "IIcon icon = com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(stack.getItem(), stack.getItemDamage()));"),
 ("com/hbm/render/tileentity/RenderPlushie.java", 133, "IIcon icon = stack.getIconIndex();", "IIcon icon = com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(stack.getItem(), stack.getItemDamage()));"),
 # --- Model SRG fields / ctors
 ("com/hbm/render/model/ModelMoonCow.java", 28, "this.field_78145_g", "this.childYOffset"),
 ("com/hbm/render/model/ModelMoonCow.java", 28, "this.field_78151_h", "this.childZOffset"),
 ("com/hbm/render/model/ModelSkeletonNT.java", 13, "super(scale, 0.0F, 64, 32)", "super(scale, false)"),
 # --- tooltip flag / NBT list / dimension id
 ("com/hbm/inventory/gui/GUIScreenTemplateFolder.java", 99, "stack.getTooltip(MainRegistry.proxy.me(), true)", "stack.getTooltip(MainRegistry.proxy.me(), net.minecraft.client.util.ITooltipFlag.TooltipFlags.ADVANCED)"),
 ("com/hbm/saveddata/AnnihilatorSavedData.java", 158, "NBTTagCompound compound = (NBTTagCompound) nbt.tagList.get(i);", "NBTTagCompound compound = nbt.getCompoundTagAt(i);"),
 ("com/hbm/inventory/gui/GUIMachineStardar.java", 420, "star.getWorld().provider.dimensionId", "star.getWorld().provider.getDimension()"),
 # --- EntityTippedArrow
 ("com/hbm/tileentity/deco/TileEntityTrappedBrick.java", 59, "EntityArrow arrow = new EntityArrow(this.world);", "EntityArrow arrow = new net.minecraft.entity.projectile.EntityTippedArrow(this.world);"),
 ("com/hbm/tileentity/deco/TileEntityTrappedBrick.java", 66, "EntityArrow farrow = new EntityArrow(this.world);", "EntityArrow farrow = new net.minecraft.entity.projectile.EntityTippedArrow(this.world);"),
 # --- MapGenStructureIO SRGs / skull rotation / isVecInside
 ("com/hbm/lib/HbmWorld.java", 59, "MapGenStructureIO.func_143031_a", "MapGenStructureIO.registerStructureComponent"),
 ("com/hbm/world/gen/MapGenNTMFeatures.java", 183, "MapGenStructureIO.func_143036_a", "MapGenStructureIO.getStructureComponentName"),
 ("com/hbm/world/feature/GlyphidHive.java", 101, "skull.func_145903_a(rand.nextInt(16))", "skull.setSkullRotation(rand.nextInt(16))"),
 ("com/hbm/world/gen/component/SiloComponent.java", 1240, "box.isVecInside(posX, posY, posZ)", "box.isVecInside(new net.minecraft.util.math.BlockPos(posX, posY, posZ))"),
 ("com/hbm/world/gen/component/SiloComponent.java", 1382, "box.isVecInside(posX, posY, posZ)", "box.isVecInside(new net.minecraft.util.math.BlockPos(posX, posY, posZ))"),
 # --- biome ids
 ("com/hbm/world/ModBiomes.java", 226, "ForgeRegistries.BIOMES.getID(biome)", "net.minecraft.world.biome.Biome.getIdForBiome(biome)"),
 ("com/hbm/world/ModBiomes.java", 300, "ForgeRegistries.BIOMES.getID(orbitBiome)", "net.minecraft.world.biome.Biome.getIdForBiome(orbitBiome)"),
 ("com/hbm/world/ModBiomes.java", 338, "ForgeRegistries.BIOMES.getID(biomes[i])", "net.minecraft.world.biome.Biome.getIdForBiome(biomes[i])"),
 # --- PinkCloudFX metadata/setBlock
 ("com/hbm/entity/particle/EntityPinkCloudFX.java", 79, "int meta = this.world.getBlockMetadata((int) posX, (int) posY, (int) posZ);", "int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, (int) posX, (int) posY, (int) posZ));"),
 ("com/hbm/entity/particle/EntityPinkCloudFX.java", 80, "this.world.setBlock((int) posX, (int) posY, (int) posZ, ModBlocks.broadcaster_pc, meta, 2);", "this.world.setBlockState(new net.minecraft.util.math.BlockPos((int) posX, (int) posY, (int) posZ), ModBlocks.broadcaster_pc.getStateFromMeta(meta), 2);"),
 # --- oil well replaceable + setBlock
 ("com/hbm/tileentity/machine/oil/TileEntityMachineOilWell.java", 68, "read(this.world, this.pos.getX() + j, this.pos.getY() + 10, this.pos.getZ() + j).getBlock().isReplaceable(this.world, this.pos.getX() + j, this.pos.getY() + 7, this.pos.getZ() + k)", "read(this.world, this.pos.getX() + j, this.pos.getY() + 7, this.pos.getZ() + k).getMaterial().isReplaceable()"),
 ("com/hbm/tileentity/machine/oil/TileEntityMachineOilWell.java", 69, "this.world.setBlock(this.pos.getX() + k, this.pos.getY() + 10, this.pos.getZ() + k, ModBlocks.gas_radon_dense);", "this.world.setBlockState(new net.minecraft.util.math.BlockPos(this.pos.getX() + k, this.pos.getY() + 10, this.pos.getZ() + k), ModBlocks.gas_radon_dense.getDefaultState());"),
 ("com/hbm/tileentity/machine/oil/TileEntityMachineOilWell.java", 78, "read(this.world, this.pos.getX() + j, this.pos.getY() + 10, this.pos.getZ() + j).getBlock().isReplaceable(this.world, this.pos.getX() + j, this.pos.getY() + 7, this.pos.getZ() + k)", "read(this.world, this.pos.getX() + j, this.pos.getY() + 7, this.pos.getZ() + k).getMaterial().isReplaceable()"),
 ("com/hbm/tileentity/machine/oil/TileEntityMachineOilWell.java", 79, "this.world.setBlock(this.pos.getX() + k, this.pos.getY() + 10, this.pos.getZ() + k, ModBlocks.gas_asbestos);", "this.world.setBlockState(new net.minecraft.util.math.BlockPos(this.pos.getX() + k, this.pos.getY() + 10, this.pos.getZ() + k), ModBlocks.gas_asbestos.getDefaultState());"),
 # --- IGrowable/IPlantable
 ("com/hbm/tileentity/machine/TileEntityMachineAutosaw.java", 261, "((IGrowable) b).func_149851_a(world, x, y, z, world.isRemote)", "((IGrowable) b).canGrow(world, new net.minecraft.util.math.BlockPos(x, y, z), com.hbm.world.WorldBlockState.read(world, x, y, z), world.isRemote)"),
 ("com/hbm/tileentity/machine/TileEntityMachineAutosaw.java", 284, "com.hbm.world.WorldBlockState.read(this.world, x, y, z).getBlock().isNormalCube(this.world, x, y, z)", "com.hbm.world.WorldBlockState.read(this.world, x, y, z).isNormalCube()"),
 ("com/hbm/tileentity/machine/TileEntityMachineAutosaw.java", 311, "replacementBlock = seed.getPlant(this.world, x, y, z);", "replacementBlock = seed.getPlant(this.world, new net.minecraft.util.math.BlockPos(x, y, z)).getBlock();"),
 ("com/hbm/tileentity/machine/TileEntityMachineAutosaw.java", 312, "replacementMeta = seed.getPlantMetadata(this.world, x, y, z);", "replacementMeta = com.hbm.world.WorldBlockState.metadata(seed.getPlant(this.world, new net.minecraft.util.math.BlockPos(x, y, z)));"),
 # --- HeRf facing / turret vec
 ("com/hbm/tileentity/network/TileEntityConverterHeRf.java", 54, "receiver.receiveEnergy(dir.getOpposite(), maxAvailable, false)", "receiver.receiveEnergy(net.minecraft.util.EnumFacing.byIndex(dir.getOpposite().ordinal()), maxAvailable, false)"),
 ("com/hbm/tileentity/turret/TileEntityTurretHoward.java", 165, "hOff = hOff.scale(-1);", "hOff = hOff.multiply(-1);"),
 # --- Enchantment.effectId
 ("com/hbm/util/EnchantmentUtil.java", 31, "== enchantment.effectId)", "== net.minecraft.enchantment.Enchantment.getEnchantmentID(enchantment))"),
 ("com/hbm/util/EnchantmentUtil.java", 46, "== enchantment.effectId)", "== net.minecraft.enchantment.Enchantment.getEnchantmentID(enchantment))"),
 # --- GameRules value type
 ("com/hbm/util/GameRuleHelper.java", 30, "rules.addGameRule(s, String.valueOf(def));", "rules.addGameRule(s, String.valueOf(def), net.minecraft.world.GameRules.ValueType.ANY_VALUE);"),
 ("com/hbm/util/GameRuleHelper.java", 45, "rules.addGameRule(s, String.valueOf(def));", "rules.addGameRule(s, String.valueOf(def), net.minecraft.world.GameRules.ValueType.ANY_VALUE);"),
 # --- equipment slot mapping
 ("com/hbm/util/MobUtil.java", 251, "entity.setCurrentItemOrArmor(slot, stack);", "entity.setItemStackToSlot(slot == 0 ? net.minecraft.inventory.EntityEquipmentSlot.MAINHAND : net.minecraft.inventory.EntityEquipmentSlot.values()[slot + 1], stack);"),
 # --- World.scheduledUpdatesAreImmediate / BlockFurnace statics via ReflectionHelper
 ("com/hbm/blocks/gas/BlockGasBase.java", 110, "world.scheduledUpdatesAreImmediate = false;", 'net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.world.World.class, world, false, "field_72999_e", "scheduledUpdatesAreImmediate");'),
 ("com/hbm/blocks/machine/BlockFurnaceSpace.java", 25, "BlockFurnace.field_149934_M = true;", 'net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.block.BlockFurnace.class, null, true, "field_149934_M", "keepInventory");'),
 ("com/hbm/blocks/machine/BlockFurnaceSpace.java", 33, "BlockFurnace.field_149934_M = false;", 'net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.block.BlockFurnace.class, null, false, "field_149934_M", "keepInventory");'),
 # --- skeleton type / landmine ground checks / emitter solid side
 ("com/hbm/handler/ability/IWeaponAbility.java", 287, "if(((EntitySkeleton) living).getSkeletonType() == 0) {", "if(living instanceof net.minecraft.entity.monster.EntitySkeleton) {"),
 ("com/hbm/blocks/bomb/Landmine.java", 96, "!World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)", "!com.hbm.world.WorldBlockState.read(world, x, y - 1, z).isTopSolid()"),
 ("com/hbm/blocks/bomb/Landmine.java", 96, "!BlockFence.func_149825_a(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock())", "!com.hbm.world.WorldBlockState.read(world, x, y - 1, z).isOpaqueCube()"),
 ("com/hbm/blocks/generic/BlockEmitter.java", 135, "if(b.isBlockSolid(this.world, x, y, z, dir.ordinal())) {", "if(b.isSideSolid(com.hbm.world.WorldBlockState.read(this.world, x, y, z), this.world, new net.minecraft.util.math.BlockPos(x, y, z), net.minecraft.util.EnumFacing.byIndex(dir.ordinal()))) {"),
 # --- XFactory12ga dir -> side offsets
 ("com/hbm/items/weapon/sedna/factory/XFactory12ga.java", 134, "dir.offsetX, dir.offsetY, dir.offsetZ", "side.getXOffset(), side.getYOffset(), side.getZOffset()"),
 # --- canPlaceTorchOnTop / canFallThrough-adjacent ground checks
 ("com/hbm/dim/duna/WorldGeneratorDuna.java", 81, "com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock().canPlaceTorchOnTop(world, x, y - 1, z)", "com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock().canPlaceTorchOnTop(com.hbm.world.WorldBlockState.read(world, x, y - 1, z), world, new net.minecraft.util.math.BlockPos(x, y - 1, z))"),
 ("com/hbm/world/dungeon/ArcticVault.java", 79, "com.hbm.world.WorldBlockState.read(world, x, iy - 1, z).getBlock().canPlaceTorchOnTop(world, x, iy - 1, z)", "com.hbm.world.WorldBlockState.read(world, x, iy - 1, z).getBlock().canPlaceTorchOnTop(com.hbm.world.WorldBlockState.read(world, x, iy - 1, z), world, new net.minecraft.util.math.BlockPos(x, iy - 1, z))"),
 # --- chunk provider roundtrip
 ("com/hbm/saveddata/satellites/SatelliteHorizons.java", 49, "provider.world.getChunk(x >> 4, z >> 4);", "world.getChunk(x >> 4, z >> 4);"),
 # --- fluid block legacy coords
 ("com/hbm/blocks/fluid/CoriumBlock.java", 100, "this.isSourceBlock(world, x, y, z)", "this.isSourceBlock(world, new net.minecraft.util.math.BlockPos(x, y, z))"),
 ("com/hbm/blocks/fluid/AcidBlock.java", 58, "return super.canDisplace(world, x, y, z);", "return super.canDisplace(world, new net.minecraft.util.math.BlockPos(x, y, z));"),
 ("com/hbm/blocks/fluid/AcidBlock.java", 66, "return super.displaceIfPossible(world, x, y, z);", "return super.displaceIfPossible(world, new net.minecraft.util.math.BlockPos(x, y, z));"),
 ("com/hbm/blocks/fluid/MudBlock.java", 61, "return super.canDisplace(world, x, y, z);", "return super.canDisplace(world, new net.minecraft.util.math.BlockPos(x, y, z));"),
 ("com/hbm/blocks/fluid/MudBlock.java", 69, "return super.displaceIfPossible(world, x, y, z);", "return super.displaceIfPossible(world, new net.minecraft.util.math.BlockPos(x, y, z));"),
 ("com/hbm/blocks/fluid/ToxicBlock.java", 61, "return super.canDisplace(world, x, y, z);", "return super.canDisplace(world, new net.minecraft.util.math.BlockPos(x, y, z));"),
 ("com/hbm/blocks/fluid/ToxicBlock.java", 69, "return super.displaceIfPossible(world, x, y, z);", "return super.displaceIfPossible(world, new net.minecraft.util.math.BlockPos(x, y, z));"),
 # --- oil well explosion hook call / troll disturber super
 ("com/hbm/blocks/machine/MachineOilWell.java", 127, "onBlockDestroyedByExplosion(world, x, y, z, explosion);", "onBlockDestroyedByExplosion(world, new net.minecraft.util.math.BlockPos(x, y, z), explosion);"),
 ("com/hbm/blocks/machine/MachineTrollDisturber.java", 41, "super.onBlockAdded(world, x, y, z);", "super.onBlockAdded(world, new net.minecraft.util.math.BlockPos(x, y, z));"),
 # --- light opacity / renderAsNormalBlock
 ("com/hbm/blocks/machine/Floodlight.java", 246, "if(block.getLightOpacity(this.world, iX, iY, iZ) < 127) continue;", "if(com.hbm.world.WorldBlockState.read(this.world, iX, iY, iZ).getLightOpacity() < 127) continue;"),
 ("com/hbm/blocks/network/BlockCablePaintable.java", 95, "return paint.renderAsNormalBlock() && paint != that;", "return paint.getDefaultState().isOpaqueCube() && paint != that;"),
 ("com/hbm/blocks/network/BlockOpenComputersCablePaintable.java", 87, "if(block.renderAsNormalBlock() && block != this) {", "if(block.getDefaultState().isOpaqueCube() && block != this) {"),
 # --- vanillant processors
 ("com/hbm/explosion/vanillant/standard/BlockProcessorStandard.java", 62, "block.dropBlockAsItemWithChance(world, blockX, blockY, blockZ, com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, blockX, blockY, blockZ) ), dropChance, dropFortune);", "block.dropBlockAsItemWithChance(world, com.hbm.world.WorldBlockState.read(world, blockX, blockY, blockZ), new net.minecraft.util.math.BlockPos(blockX, blockY, blockZ), dropChance, dropFortune);"),
 ("com/hbm/explosion/vanillant/standard/BlockProcessorStandard.java", 65, "block.onBlockExploded(world, blockX, blockY, blockZ, explosion.compat);", "block.onBlockExploded(world, new net.minecraft.util.math.BlockPos(blockX, blockY, blockZ), explosion.compat);"),
 ("com/hbm/explosion/vanillant/standard/CustomDamageHandlerDyson.java", 21, "DamageSource.setExplosionSource(explosion.compat)", "DamageSource.causeExplosionDamage(explosion.compat)"),
 ("com/hbm/explosion/vanillant/ExplosionVNT.java", 64, "this.compat = new Explosion(world, exploder, x, y, z, size) {", "this.compat = new Explosion(world, exploder, x, y, z, size, false, true) {"),
 ("com/hbm/explosion/vanillant/ExplosionVNT.java", 67, "public Map func_77277_b() {", "public Map getPlayerKnockbackMap() {"),
 ("com/hbm/explosion/vanillant/ExplosionVNT.java", 75, "this.compat.exploder = this.exploder;", ""),
 ("com/hbm/explosion/vanillant/ExplosionVNT.java", 76, "this.compat.explosionSize = this.size;", ""),
 # --- vanillant func_145774_a clause removals (hook removed in 1.12; vanilla always returned true)
 ("com/hbm/explosion/vanillant/standard/BlockAllocatorBulkie.java", 75, "if(explosion.exploder == null || explosion.exploder.func_145774_a(explosion.compat, world, blockX, blockY, blockZ, block, explosion.size)) {", "if(explosion.exploder == null) {"),
 ("com/hbm/explosion/vanillant/standard/BlockAllocatorGlyphidDig.java", 75, "if(explosion.exploder == null || explosion.exploder.func_145774_a(explosion.compat, world, blockX, blockY, blockZ, block, explosion.size)) {", "if(explosion.exploder == null) {"),
 ("com/hbm/explosion/vanillant/standard/BlockAllocatorStandard.java", 65, "if(powerRemaining > 0.0F && (explosion.exploder == null || explosion.exploder.func_145774_a(explosion.compat, world, blockX, blockY, blockZ, block, powerRemaining))) {", "if(powerRemaining > 0.0F && explosion.exploder == null) {"),
 ("com/hbm/explosion/vanillant/standard/BlockAllocatorWater.java", 61, "(explosion.getExplosivePlacedBy() == null || explosion.getExplosivePlacedBy().func_145774_a(explosion.compat, world, blockX, blockY, blockZ, block, powerRemaining)) &&", ""),
 # --- EBS access
 ("com/hbm/util/SubChunkSnapshot.java", 62, "Block block = ebs.getBlockByExtId(lx, ly, lz);", "Block block = ebs.get(lx, ly, lz).getBlock();"),
 # --- AtmosphereBlob bounding box
 ("com/hbm/handler/atmosphere/AtmosphereBlob.java", 105, "bb = block.getCollisionBoundingBoxFromPool(null, x, y, z);", "bb = block.getBoundingBox(com.hbm.world.WorldBlockState.read(world, x, y, z), null, new net.minecraft.util.math.BlockPos(x, y, z));"),
 # --- falling blocks
 ("com/hbm/blocks/generic/BlockUberConcrete.java", 79, "new EntityFallingBlock(world, x + 0.5 + dir.offsetX, y + 0.5, z + 0.5 + dir.offsetZ, ModBlocks.concrete_super_broken);", "new EntityFallingBlock(world, x + 0.5 + dir.offsetX, y + 0.5, z + 0.5 + dir.offsetZ, ModBlocks.concrete_super_broken.getDefaultState());"),
 ("com/hbm/blocks/generic/BlockUberConcrete.java", 80, "debris.field_145812_b = 2;", "debris.fallTime = 2;"),
 ("com/hbm/blocks/generic/BlockUberConcrete.java", 81, "debris.field_145813_c = false;", "debris.shouldDropItem = false;"),
 ("com/hbm/blocks/generic/BlockUberConcrete.java", 82, "debris.func_145806_a(true);", "debris.setHurtEntities(true);"),
 ("com/hbm/blocks/generic/WasteLeaves.java", 53, "new EntityFallingBlock(world, x + 0.5, y + 0.5, z + 0.5, ModBlocks.leaves_layer);", "new EntityFallingBlock(world, x + 0.5, y + 0.5, z + 0.5, ModBlocks.leaves_layer.getDefaultState());"),
 ("com/hbm/blocks/generic/WasteLeaves.java", 54, "leaves.field_145812_b = 2;", "leaves.fallTime = 2;"),
 ("com/hbm/blocks/generic/WasteLeaves.java", 55, "leaves.field_145813_c = false;", "leaves.shouldDropItem = false;"),
 # --- black hole rubble state
 ("com/hbm/entity/effect/EntityBlackHole.java", 112, "Block b = ((EntityFallingBlock)e).func_145805_f();", "net.minecraft.block.state.IBlockState fstate = ((EntityFallingBlock)e).getBlock(); Block b = fstate.getBlock();"),
 ("com/hbm/entity/effect/EntityBlackHole.java", 113, "int meta = ((EntityFallingBlock)e).field_145814_a;", "int meta = com.hbm.world.WorldBlockState.metadata(fstate);"),
 # --- trapdoor legacy helpers
 ("com/hbm/blocks/generic/BlockNTMTrapdoor.java", 28, "if (!func_150118_d(meta))", "if ((meta & 4) == 0)"),
 ("com/hbm/blocks/generic/BlockNTMTrapdoor.java", 31, "Block blockBelow = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();", "net.minecraft.block.state.IBlockState belowState = com.hbm.world.WorldBlockState.read(world, x, y - 1, z);"),
 ("com/hbm/blocks/generic/BlockNTMTrapdoor.java", 32, "return blockBelow != null && blockBelow.isLadder(world, x, y - 1, z, entity);", "return belowState != null && belowState.getBlock().isLadder(belowState, world, new net.minecraft.util.math.BlockPos(x, y - 1, z), entity);"),
 ("com/hbm/blocks/generic/BlockNTMTrapdoor.java", 56, "return super.getCollisionBoundingBoxFromPool(world, x, y, z);", "return super.getBoundingBox(com.hbm.world.WorldBlockState.read(world, x, y, z), world, new net.minecraft.util.math.BlockPos(x, y, z));"),
 # --- commands
 ("com/hbm/commands/CommandDebugChunkLoad.java", 55, "IChunkLoader loader = serv.currentChunkLoader;", "IChunkLoader loader = serv.chunkLoader;"),
 ("com/hbm/commands/CommandDebugChunkLoad.java", 66, "if(chunk.isChunkLoaded) {", "if(chunk.isLoaded()) {"),
 ("com/hbm/commands/CommandLocate.java", 58, "throw new PlayerNotFoundException();", 'throw new PlayerNotFoundException(new net.minecraft.util.text.TextComponentTranslation("commands.generic.player.notFound"));'),
 ("com/hbm/commands/CommandRadiation.java", 47, "this.parseDoubleBounded(sender, args[1], 0D, 100_000D)", "parseDouble(args[1], 0D, 100_000D)"),
 ("com/hbm/commands/CommandSatellites.java", 66, "freq = parseInt(sender, args[1]);", "freq = parseInt(args[1]);"),
 ("com/hbm/commands/CommandSatellites.java", 80, "int freq = parseInt(sender, args[1]);", "int freq = parseInt(args[1]);"),
 ("com/hbm/commands/CommandSatellites.java", 123, "getListOfStringsFromIterableMatchingLastWord(args,", "getListOfStringsMatchingLastWord(args,"),
 ("com/hbm/commands/CommandReloadConfig.java", 101, "getListOfStringsFromIterableMatchingLastWord(args,", "getListOfStringsMatchingLastWord(args,"),
 ("com/hbm/config/FalloutConfigJSON.java", 313, "net.minecraft.init.Block.getBlockById(meta.getX())", "net.minecraft.block.Block.getBlockById(meta.getX())"),
 ("com/hbm/main/CraftingManager.java", 1149, "new java.util.ArrayList<>(net.minecraft.item.crafting.CraftingManager.REGISTRY.getValuesCollection())", "new java.util.ArrayList<>(com.google.common.collect.Lists.newArrayList(net.minecraft.item.crafting.CraftingManager.REGISTRY.iterator()))"),
 ("com/hbm/main/MainRegistry.java", 343, "GameRegistry.registerTileEntityWithAlternatives(e.getKey(), e.getValue()[0], e.getValue());", "for(String alt : e.getValue()) GameRegistry.registerTileEntity(e.getKey(), alt);"),
 # --- bucket item names
 ("com/hbm/inventory/FluidContainerRegistry.java", 37, "ModItems.BUCKET_mud", "ModItems.bucket_mud"),
 ("com/hbm/inventory/FluidContainerRegistry.java", 38, "ModItems.BUCKET_schrabidic_acid", "ModItems.bucket_schrabidic_acid"),
 ("com/hbm/inventory/FluidContainerRegistry.java", 39, "ModItems.BUCKET_sulfuric_acid", "ModItems.bucket_sulfuric_acid"),
 ("com/hbm/inventory/FluidContainerRegistry.java", 40, "ModItems.BUCKET_mercury", "ModItems.bucket_mercury"),
 ("com/hbm/inventory/FluidContainerRegistry.java", 41, "ModItems.BUCKET_bromine", "ModItems.bucket_bromine"),
 ("com/hbm/inventory/FluidContainerRegistry.java", 42, "ModItems.BUCKET_ccl", "ModItems.bucket_ccl"),
 # --- RenderItem-based icon rendering
 ("com/hbm/inventory/gui/GUIScreenBobmazon.java", 272, "gui.itemRender.renderItemAndEffectIntoGUI(gui.fontRenderer, gui.mc.getTextureManager(), com.hbm.advancement.NtmAdvancements.icon(requirement.achievement), x + 1, y + 1);", "gui.itemRender.renderItemAndEffectIntoGUI(com.hbm.advancement.NtmAdvancements.icon(requirement.achievement), x + 1, y + 1);"),
 ("com/hbm/qmaw/GuiQMAW.java", 280, "itemRender.renderItemAndEffectIntoGUI(this.fontRenderer, mc.renderEngine, this.icon, guiLeft + x, guiTop + y);", "itemRender.renderItemAndEffectIntoGUI(this.icon, guiLeft + x, guiTop + y);"),
 # --- ItemRenderer hand anim
 ("com/hbm/packet/toclient/HbmAnimationPacket.java", 117, "Minecraft.getMinecraft().entityRenderer.itemRenderer.resetEquippedProgress();", "Minecraft.getMinecraft().entityRenderer.itemRenderer.resetEquippedProgress(net.minecraft.util.EnumHand.MAIN_HAND);"),
 ("com/hbm/packet/toclient/HbmAnimationPacket.java", 118, "Minecraft.getMinecraft().entityRenderer.itemRenderer.itemToRender = stack;", 'net.minecraftforge.fml.relauncher.ReflectionHelper.setPrivateValue(net.minecraft.client.renderer.ItemRenderer.class, Minecraft.getMinecraft().entityRenderer.itemRenderer, stack, "field_187467_d", "itemStackMainHand");'),
 # --- particles destroy effects
 ("com/hbm/packet/toclient/ParticleBurstPacket.java", 56, "Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(m.x, m.y, m.z, Block.getBlockById(m.block), m.meta);", "Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(new net.minecraft.util.math.BlockPos(m.x, m.y, m.z), net.minecraft.block.Block.getBlockById(m.block).getStateFromMeta(m.meta));"),
 # --- siren RL accessors
 ("com/hbm/packet/toclient/TESirenPacket.java", 87, "String path = loc.getResourceDomain() + \":\" + loc.getResourcePath();", "String path = loc.getNamespace() + \":\" + loc.getPath();"),
 # --- QMAW resource pack internals
 ("com/hbm/qmaw/QMAWLoader.java", 86, "Minecraft.getMinecraft().mcDataDir.getAbsolutePath()", "Minecraft.getMinecraft().gameDir.getAbsolutePath()"),
 ("com/hbm/qmaw/QMAWLoader.java", 101, "dissectZip(((FileResourcePack) pack).resourcePackFile);", 'dissectZip(net.minecraftforge.fml.relauncher.ReflectionHelper.getPrivateValue(net.minecraft.client.resources.AbstractResourcePack.class, (net.minecraft.client.resources.FileResourcePack) pack, "field_110597_b", "resourcePackFile"));'),
 ("com/hbm/qmaw/QMAWLoader.java", 105, "dissectFolder(((FolderResourcePack) pack).resourcePackFile);", 'dissectFolder(net.minecraftforge.fml.relauncher.ReflectionHelper.getPrivateValue(net.minecraft.client.resources.AbstractResourcePack.class, (net.minecraft.client.resources.FolderResourcePack) pack, "field_110597_b", "resourcePackFile"));'),
 # --- ClientProxy renderers / sound
 ("com/hbm/main/ClientProxy.java", 863, "new RenderMinecart()", "new RenderMinecart(net.minecraft.client.Minecraft.getMinecraft().getRenderManager())"),
 ("com/hbm/main/ClientProxy.java", 1985, "ParticleSplash fx = new ParticleSplash(man, world, x, y, z);", "com.hbm.particle.ParticleSplash fx = new com.hbm.particle.ParticleSplash(man, world, x, y, z);"),
 ("com/hbm/main/ClientProxy.java", 2313, "new PositionedSoundRecord(new ResourceLocation(sound), volume, pitch, (float) x, (float) y, (float) z)", "new PositionedSoundRecord(new ResourceLocation(sound), net.minecraft.util.SoundCategory.MASTER, pitch, volume, false, 0, net.minecraft.client.audio.ISound.AttenuationType.LINEAR, (float) x, (float) y, (float) z)"),
 # --- celestial decorators / providers / chunks
 ("com/hbm/dim/BiomeDecoratorCelestial.java", 54, "this.generateOres();", "this.generateOres(world, random);"),
 ("com/hbm/dim/BiomeDecoratorCelestial.java", 56, "TerrainGen.decorate(world, random, chunkPos, LAKE)", "TerrainGen.decorate(world, random, chunkPos, net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate.EventType.LAKE)"),
 ("com/hbm/world/feature/OreCave.java", 124, "int cX = event.getChunkX();", "int cX = event.getPos().getX();"),
 ("com/hbm/world/feature/OreCave.java", 125, "int cZ = event.getChunkZ();", "int cZ = event.getPos().getZ();"),
 ("com/hbm/world/feature/OreLayer3D.java", 82, "int cx = event.getChunkX();", "int cx = event.getPos().getX();"),
 ("com/hbm/world/feature/OreLayer3D.java", 83, "int cz = event.getChunkZ();", "int cz = event.getPos().getZ();"),
 ("com/hbm/dim/BiomeGenBaseCelestial.java", 75, "metadata[y] = (byte)Block.getMetaFromState(state);", "metadata[y] = (byte)state.getBlock().getMetaFromState(state);"),
 ("com/hbm/dim/eve/WorldProviderEve.java", 63, 'MainRegistry.proxy.me().playSound("hbm:misc.rumble", 10F, 1F);', 'MainRegistry.proxy.me().playSound(com.hbm.main.ModSoundEvents.resolve("hbm:misc.rumble"), 10F, 1F);'),
 ("com/hbm/dim/laythe/WorldProviderLaythe.java", 62, "if(event.block.getMaterial() == Material.WATER) {", "if(event.getState().getMaterial() == Material.WATER) {"),
 ("com/hbm/dim/WorldProviderCelestial.java", 507, "@Override", ""),
 ("com/hbm/dim/WorldProviderCelestial.java", 510, "return super.drawClouds(partialTicks);", "return this.getCloudColor(partialTicks);"),
 ("com/hbm/world/WorldProviderTom.java", 161, "Vec3d clouds = super.drawClouds(partialTicks);", "Vec3d clouds = this.getCloudColor(partialTicks);"),
 ("com/hbm/lib/HbmWorldGen.java", 105, "Biomes.FLOWER_FOREST", "Biomes.MUTATED_FOREST"),
 ("com/hbm/dim/trait/CBT_War.java", 81, "World targetWorld = MinecraftServer.getServer().worldServerForDimension(projectile.getTarget());", "World targetWorld = net.minecraftforge.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(projectile.getTarget());"),
 ("com/hbm/dim/trait/CBT_Lights.java", 26, "lights += block.getLightValue();", "lights += block.getLightValue(block.getDefaultState());"),
 ("com/hbm/dim/trait/CBT_Lights.java", 30, "lights -= block.getLightValue();", "lights -= block.getLightValue(block.getDefaultState());"),
 # --- rail block hooks
 ("com/hbm/blocks/machine/RailHighspeed.java", 31, "public float getRailMaxSpeed(World world, EntityMinecart cart, int y, int x, int z) {", "public float getRailMaxSpeed(World world, EntityMinecart cart, net.minecraft.util.math.BlockPos pos) {"),
 ("com/hbm/blocks/machine/RailBooster.java", 15, "public void onMinecartPass(World world, EntityMinecart cart, int y, int x, int z) {", "public void onMinecartPass(World world, EntityMinecart cart, net.minecraft.util.math.BlockPos pos) {"),
 # --- crafting handler
 ("com/hbm/crafting/handlers/TestCraftingHandler.java", 42, "public int getRecipeSize() {", "public boolean canFit(int width, int height) {"),
 ("com/hbm/crafting/handlers/TestCraftingHandler.java", 43, "return 1;", "return width >= 1 && height >= 1;"),
 # --- gui list entry
 ("com/hbm/inventory/gui/element/GuiFileList.java", 78, "public void drawEntry(int id, int x, int y, int width, int height, Tessellator tess, int mouseX, int mouseY, boolean isVisible) {", "public void drawEntry(int id, int x, int y, int width, int height, int mouseX, int mouseY, boolean isVisible, float partialTicks) {"),
 # --- slot legacy hook
 ("com/hbm/inventory/SlotDeprecated.java", 23, "@Override", ""),
 # --- misc render/entity
 ("com/hbm/render/entity/effect/RenderQuasar.java", 57, "tess.setColorRGBA_F(r, g, b, alpha);", "tess.getBuffer().color(r, g, b, alpha);"),
 ("com/hbm/render/entity/mob/RenderDummy.java", 15, "super(new ModelBiped(0.0F), 0.5F, 1.0F);", "super(net.minecraft.client.Minecraft.getMinecraft().getRenderManager(), new ModelBiped(0.0F), 0.5F, 1.0F);"),
 ("com/hbm/render/entity/mob/RenderGhost.java", 18, "super(new ModelBiped(0.0F), 0.5F, 1.0F);", "super(net.minecraft.client.Minecraft.getMinecraft().getRenderManager(), new ModelBiped(0.0F), 0.5F, 1.0F);"),
 ("com/hbm/render/entity/mob/RenderSifterEel.java", 17, "super(p_i1252_1_, p_i1252_2_);", "super(net.minecraft.client.Minecraft.getMinecraft().getRenderManager(), p_i1252_1_, p_i1252_2_);"),
 ("com/hbm/render/item/weapon/sedna/ItemRenderFatMan.java", 158, "mc.entityRenderer.disableLightmap(interp);", "mc.entityRenderer.disableLightmap();"),
 ("com/hbm/render/item/weapon/sedna/ItemRenderFatMan.java", 204, "mc.entityRenderer.enableLightmap(interp);", "mc.entityRenderer.enableLightmap();"),
 ("com/hbm/render/tileentity/RenderOrrery.java", 49, "Tessellator.instance.disableColor();", "Tessellator.getInstance().getBuffer().noColor();"),
 ("com/hbm/render/world/RenderNTMSkyboxChainloader.java", 57, "rg.renderSky(partialTicks);", "rg.renderSky(partialTicks, 0);"),
 ("com/hbm/render/world/RenderNTMSkyboxImpact.java", 253, "double d0 = mc.player.getPosition(partialTicks).y - world.getHorizon();", "double d0 = (mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * (double) partialTicks) - world.getHorizon();"),
 # --- minecart event / renderer calls
 ("com/hbm/entity/cart/EntityMinecartDestroyer.java", 50, "new net.minecraftforge.event.entity.minecart.MinecartInteractEvent(this, player)", "new net.minecraftforge.event.entity.minecart.MinecartInteractEvent(this, player, net.minecraft.util.EnumHand.MAIN_HAND)"),
 ("com/hbm/render/entity/item/RenderNeoCart.java", 40, "cart.func_70489_a(interpX, interpY, interpZ)", "cart.getPos(interpX, interpY, interpZ)"),
 ("com/hbm/render/entity/item/RenderNeoCart.java", 44, "cart.func_70495_a(interpX, interpY, interpZ, mult)", "cart.getPosOffset(interpX, interpY, interpZ, mult)"),
 ("com/hbm/render/entity/item/RenderNeoCart.java", 45, "cart.func_70495_a(interpX, interpY, interpZ, -mult)", "cart.getPosOffset(interpX, interpY, interpZ, -mult)"),
 ("com/hbm/render/entity/item/RenderNeoCart.java", 109, "public void bindEntityTexture(Entity entity) {", "protected boolean bindEntityTexture(Entity entity) {"),
 ("com/hbm/render/entity/item/RenderNeoCart.java", 110, "this.bindTexture(this.getEntityTexture(entity));", "this.bindTexture(this.getEntityTexture(entity)); return true;"),
 # --- record / jukebox
 ("com/hbm/items/special/ItemModRecord.java", 29, "super(string);", "super(string, com.hbm.main.ModSoundEvents.resolve(string));"),
 ("com/hbm/items/special/ItemModRecord.java", 50, "((BlockJukebox) Blocks.JUKEBOX).func_149926_b(p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_, p_77648_1_);", "net.minecraft.tileentity.TileEntity jukeTe = p_77648_3_.getTileEntity(new net.minecraft.util.math.BlockPos(p_77648_4_, p_77648_5_, p_77648_6_)); if(jukeTe instanceof net.minecraft.block.BlockJukebox.TileEntityJukebox) ((net.minecraft.block.BlockJukebox.TileEntityJukebox) jukeTe).setRecord(p_77648_1_);"),
 ("com/hbm/items/special/ItemModRecord.java", 51, "p_77648_3_.playAuxSFXAtEntity((EntityPlayer) null, 1005, p_77648_4_, p_77648_5_, p_77648_6_, Item.getIdFromItem(this));", "p_77648_3_.playEvent((EntityPlayer) null, 1005, new net.minecraft.util.math.BlockPos(p_77648_4_, p_77648_5_, p_77648_6_), Item.getIdFromItem(this));"),
 ("com/hbm/items/tool/ItemModDoor.java", 43, "player.canPlayerEdit(x, y + 1, z, side, stack)", "player.canPlayerEdit(new net.minecraft.util.math.BlockPos(x, y + 1, z), net.minecraft.util.EnumFacing.byIndex(side), stack)"),
 # --- BlockTaint map color
 ("com/hbm/blocks/bomb/BlockTaint.java", 58, "@Override public MapColor getMapColor(int meta) { return MapColor.grayColor; }", "@Override public MapColor getMapColor(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) { return MapColor.GRAY; }"),
 # --- stairs ctor / graphite sound / redstone queries
 ("com/hbm/blocks/generic/BlockGenericStairs.java", 22, "super(block, meta);", "super(block.getStateFromMeta(meta));"),
 ("com/hbm/blocks/machine/pile/BlockGraphite.java", 33, "com.hbm.main.ModSoundEvents.resolve(this.getSoundType().func_150496_b())", "this.getSoundType().getBreakSound()"),
 ("com/hbm/blocks/machine/PistonInserter.java", 69, "if(world.getIndirectPowerOutput(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.ordinal()))", "if(world.getStrongPower(new net.minecraft.util.math.BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ), net.minecraft.util.EnumFacing.byIndex(dir.ordinal())) > 0)"),
 ("com/hbm/blocks/machine/ReactorZirnox.java", 121, "world.getBlockPowerInput(sx, sy, sz) > 0", "world.getStrongPower(new net.minecraft.util.math.BlockPos(sx, sy, sz)) > 0"),
 ("com/hbm/blocks/MaterialGas.java", 9, "super(MapColor.airColor)", "super(MapColor.AIR)"),
 # --- concrete colored dye names / map colors
 ("com/hbm/blocks/generic/BlockConcreteColored.java", 56, "ItemDye.field_150921_b[func_150031_c(i)]", "net.minecraft.item.EnumDyeColor.byDyeDamage(func_150031_c(i)).getDyeColorName()"),
 ("com/hbm/blocks/generic/BlockConcreteColored.java", 61, "return MapColor.getMapColorForBlockColored(p_149728_1_);", "return new net.minecraft.block.material.MapColor[] {net.minecraft.block.material.MapColor.SNOW, net.minecraft.block.material.MapColor.ADOBE, net.minecraft.block.material.MapColor.MAGENTA, net.minecraft.block.material.MapColor.LIGHT_BLUE, net.minecraft.block.material.MapColor.YELLOW, net.minecraft.block.material.MapColor.LIME, net.minecraft.block.material.MapColor.PINK, net.minecraft.block.material.MapColor.GRAY, net.minecraft.block.material.MapColor.SILVER, net.minecraft.block.material.MapColor.CYAN, net.minecraft.block.material.MapColor.PURPLE, net.minecraft.block.material.MapColor.BLUE, net.minecraft.block.material.MapColor.BROWN, net.minecraft.block.material.MapColor.GREEN, net.minecraft.block.material.MapColor.RED, net.minecraft.block.material.MapColor.BLACK}[p_149728_1_ % 16];"),
 # --- armor light handling
 ("com/hbm/items/armor/ArmorNo9.java", 134, "int occupancy = world.getBlockLightOpacity(x, y, z);", "int occupancy = com.hbm.world.WorldBlockState.read(world, x, y, z).getLightOpacity();"),
 ("com/hbm/items/armor/ArmorNo9.java", 139, "world.setLightValue(EnumSkyBlock.BLOCK, x, y, z, newLight);", "world.setLightFor(EnumSkyBlock.BLOCK, new net.minecraft.util.math.BlockPos(x, y, z), newLight);"),
 ("com/hbm/items/armor/ArmorNo9.java", 161, "world.updateLightByType(EnumSkyBlock.BLOCK, pos.getX(), pos.getY(), pos.getZ());", "world.checkLight(new net.minecraft.util.math.BlockPos(pos.getX(), pos.getY(), pos.getZ()));"),
 # --- celestial chunk provider noise event
 ("com/hbm/dim/ChunkProviderCelestial.java", 102, "NoiseGenerator[] noiseGens = { firstOrder, secondOrder, thirdOrder, perlin, heightOrder };", 'net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld ctx = new net.minecraftforge.event.terraingen.InitNoiseGensEvent.ContextOverworld(firstOrder, secondOrder, thirdOrder, perlin, heightOrder, heightOrder, heightOrder);'),
 ("com/hbm/dim/ChunkProviderCelestial.java", 103, "noiseGens = TerrainGen.getModdedNoiseGenerators(world, this.rand, noiseGens);", "ctx = TerrainGen.getModdedNoiseGenerators(world, this.rand, ctx);"),
 ("com/hbm/dim/ChunkProviderCelestial.java", 104, "this.firstOrder = (NoiseGeneratorOctaves) noiseGens[0];", "this.firstOrder = ctx.getLPerlin1();"),
 ("com/hbm/dim/ChunkProviderCelestial.java", 105, "this.secondOrder = (NoiseGeneratorOctaves) noiseGens[1];", "this.secondOrder = ctx.getLPerlin2();"),
 ("com/hbm/dim/ChunkProviderCelestial.java", 106, "this.thirdOrder = (NoiseGeneratorOctaves) noiseGens[2];", "this.thirdOrder = ctx.getPerlin();"),
 ("com/hbm/dim/ChunkProviderCelestial.java", 107, "this.perlin = (NoiseGeneratorPerlin) noiseGens[3];", "this.perlin = ctx.getHeight();"),
 ("com/hbm/dim/ChunkProviderCelestial.java", 108, "this.heightOrder = (NoiseGeneratorOctaves) noiseGens[4];", "this.heightOrder = ctx.getScale();"),
]

def process(op, apply):
    rel, lineno, old, new = op
    path = os.path.join(ROOT, 'src', 'main', 'java', rel.replace('/', os.sep))
    try:
        with open(path, 'rb') as f:
            raw = f.read()
    except OSError as e:
        return (rel, lineno, 'MISSING FILE', str(e))
    text = raw.decode('utf-8')
    # javac numbers lines by \n; some files carry bare-LF lines from earlier
    # checkpoints, so split keeping each line's own terminator and rejoin verbatim.
    parts = re.split(r'(\r\n|\r|\n)', text)
    contents = parts[0::2]
    terms = parts[1::2] + ['']
    a, b = max(0, lineno - 5), min(len(contents), lineno + 4)
    hits = [i for i in range(a, b) if old in contents[i]]
    if len(hits) != 1:
        return (rel, lineno, 'HITS=%d' % len(hits), old[:70])
    i = hits[0]
    contents[i] = contents[i].replace(old, new)
    if apply:
        out = ''.join(c + t for c, t in zip(contents, terms))
        with open(path, 'wb') as f:
            f.write(out.encode('utf-8'))
    return (rel, lineno, 'OK', (contents[i].strip() or '<empty>')[:110])

def main():
    apply = '--apply' in sys.argv
    fails = 0
    for op in OPS:
        rel, lineno, status, info = process(op, apply)
        if status != 'OK':
            fails += 1
            print('FAIL %-70s L%-5d %s | %s' % (rel, lineno, status, info))
    print('total ops: %d, ok: %d, failed: %d, mode: %s' % (len(OPS), len(OPS) - fails, fails, 'APPLY' if apply else 'PREVIEW'))
    if fails:
        sys.exit(1)

if __name__ == '__main__':
    main()

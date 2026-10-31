#!/usr/bin/env python3
# CP167 wave 12: remaining contextual fixes - decl renames, API removals, InventoryUtil flip,
# fluid ctors, render pass cleanup, hooks and contract inserts.
# Op kinds: rm(rel,line,frag) | decl(rel,line,old,new,adapter|None) | rep(rel,line,old,new)
#           ins(rel,anchor,offset,lines) | multi(rel,old_block,new_block)
import sys, os, re

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
NNL = "net.minecraft.util.NonNullList<net.minecraft.item.ItemStack>"

RM = [
 ("com/hbm/entity/train/EntityRailCarRidable.java", 188, "public void updateRiderPosition()"),
 ("com/hbm/entity/train/EntityRailCarRidable.java", 257, "public void updateRiderPosition()"),
 ("com/hbm/entity/item/EntityMinecartModBase.java", 1162, "public Block func_145820_n()"),
]

DECL = [
 ("com/hbm/entity/train/EntityRailCarBase.java", 507,
  "@Override @SideOnly(Side.CLIENT) public void setPositionAndRotation2(double posX, double posY, double posZ, float yaw, float pitch, int turnProg) {",
  "@Override @SideOnly(Side.CLIENT) public void setPositionAndRotationDirect(double posX, double posY, double posZ, float yaw, float pitch, int turnProg, boolean teleport) {", None),
 ("com/hbm/entity/train/EntityRailCarRidable.java", 250,
  "@Override @SideOnly(Side.CLIENT) public void setPositionAndRotation2(double posX, double posY, double posZ, float yaw, float pitch, int turnProg) {",
  "@Override @SideOnly(Side.CLIENT) public void setPositionAndRotationDirect(double posX, double posY, double posZ, float yaw, float pitch, int turnProg, boolean teleport) {", None),
 ("com/hbm/entity/mob/botprime/EntityWormBaseNT.java", 107,
  "protected void updateEntityActionState() {", "protected void updateEntityActionStateNT() {", None),
 ("com/hbm/entity/mob/EntityBurrowingSwingingBase.java", 87,
  "protected void updateEntityActionState() {", "protected void updateEntityActionStateNT() {", None),
 ("com/hbm/entity/mob/EntityUFOBase.java", 44,
  "protected void updateEntityActionState() {", "protected void updateEntityActionStateNT() {", None),
 ("com/hbm/entity/mob/EntityFBIDrone.java", 18,
  "protected void updateEntityActionState() {", "protected void updateEntityActionStateNT() {", None),
 ("com/hbm/entity/mob/siege/EntitySiegeUFO.java", 88,
  "protected void updateEntityActionState() {", "protected void updateEntityActionStateNT() {", None),
 # InventoryUtil: flip ItemStack[] signatures to NonNullList
 ("com/hbm/util/InventoryUtil.java", 37,
  "public static ItemStack tryAddItemToInventory(ItemStack[] inv, int start, int end, ItemStack stack) {",
  "public static ItemStack tryAddItemToInventory(" + NNL + " inv, int start, int end, ItemStack stack) {", None),
 ("com/hbm/util/InventoryUtil.java", 52,
  "public static ItemStack tryAddItemToInventory(ItemStack[] inv, ItemStack stack) {",
  "public static ItemStack tryAddItemToInventory(" + NNL + " inv, ItemStack stack) {", None),
 ("com/hbm/util/InventoryUtil.java", 64,
  "public static ItemStack tryAddItemToExistingStack(ItemStack[] inv, int start, int end, ItemStack stack) {",
  "public static ItemStack tryAddItemToExistingStack(" + NNL + " inv, int start, int end, ItemStack stack) {", None),
 ("com/hbm/util/InventoryUtil.java", 96,
  "public static boolean tryAddItemToNewSlot(ItemStack[] inv, int start, int end, ItemStack stack) {",
  "public static boolean tryAddItemToNewSlot(" + NNL + " inv, int start, int end, ItemStack stack) {", None),
 ("com/hbm/util/InventoryUtil.java", 177,
  "public static boolean tryConsumeAStack(ItemStack[] inv, int start, int end, AStack stack) {",
  "public static boolean tryConsumeAStack(" + NNL + " inv, int start, int end, AStack stack) {", None),
 ("com/hbm/util/InventoryUtil.java", 590,
  "public static int countAStackMatches(ItemStack[] inventory, AStack stack, boolean ignoreSize) {",
  "public static int countAStackMatches(" + NNL + " inventory, AStack stack, boolean ignoreSize) {", None),
 ("com/hbm/util/InventoryUtil.java", 611,
  "public static boolean doesInventoryHaveAStack(ItemStack[] inventory, AStack stack, boolean shouldRemove, boolean ignoreSize) {",
  "public static boolean doesInventoryHaveAStack(" + NNL + " inventory, AStack stack, boolean shouldRemove, boolean ignoreSize) {", None),
 ("com/hbm/blocks/generic/BlockPinkSlab.java", 21, "super(bool, mat);", "super(mat);", None),
 ("com/hbm/blocks/generic/BlockPinkSlab.java", 36,
  "public String func_150002_b(int p_150002_1_) {", "public String getTranslationKey(int p_150002_1_) {", None),
 ("com/hbm/blocks/generic/BlockNTMGlassPane.java", 28,
  "super(name, rimTextureName, material, false);", "super(material, false);", None),
 ("com/hbm/blocks/bomb/Landmine.java", 105,
  "public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {",
  "public void onBlockDestroyedByPlayer(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, EntityPlayer player) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/items/tool/ItemSettingsTool.java", 81,
  "public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fX, float fY, float fZ) {",
  "public net.minecraft.util.EnumActionResult onItemUseFirst(EntityPlayer player, World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing side, float fX, float fY, float fZ, net.minecraft.util.EnumHand hand) {",
  "\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();"),
 ("com/hbm/entity/effect/EntityFireLingering.java", 112,
  "@Override public void writeToNBT(NBTTagCompound nbt) { }",
  "@Override public NBTTagCompound writeToNBT(NBTTagCompound nbt) { return nbt; }", None),
 ("com/hbm/blocks/fluid/RadFluid.java", 11,
  'super("rad_lava_fluid");',
  'super("rad_lava_fluid", new net.minecraft.util.ResourceLocation("hbm", "blocks/rad_lava_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/rad_lava_flowing"));', None),
 ("com/hbm/blocks/fluid/SchrabidicFluid.java", 11,
  'super("schrabidic_fluid");',
  'super("schrabidic_fluid", new net.minecraft.util.ResourceLocation("hbm", "blocks/schrabidic_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/schrabidic_flowing"));', None),
 ("com/hbm/blocks/fluid/VolcanicFluid.java", 11,
  'super("volcanic_lava_fluid");',
  'super("volcanic_lava_fluid", new net.minecraft.util.ResourceLocation("hbm", "blocks/volcanic_lava_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/volcanic_lava_flowing"));', None),
 ("com/hbm/blocks/fluid/GenericFluid.java", 11,
  "super(name);",
  'super(name, new net.minecraft.util.ResourceLocation("hbm", "blocks/" + name + "_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/" + name + "_flowing"));', None),
]

REP = [
 ("com/hbm/entity/mob/botprime/EntityWormBaseNT.java", 50, "public static final Predicate<Entity> wormSelector = new Predicate<Entity>() {", "public static final Predicate<EntityLiving> wormSelector = new Predicate<EntityLiving>() {"),
 ("com/hbm/entity/mob/botprime/EntityWormBaseNT.java", 53, "public boolean apply(Entity target) {", "public boolean apply(EntityLiving target) {"),
 ("com/hbm/entity/mob/botprime/EntityWormBaseNT.java", 91, "this.velocityChanged = true;;", "this.velocityChanged = true;"),
 ("com/hbm/entity/mob/botprime/EntityWormBaseNT.java", 146, "this.entityAge = 0;", ""),
 ("com/hbm/entity/mob/EntityBurrowingSwingingBase.java", 80, "this.updateEntityActionState();", "this.updateEntityActionStateNT();"),
 ("com/hbm/entity/mob/EntityBurrowingSwingingBase.java", 121, "this.entityAge = 0;", ""),
 ("com/hbm/entity/mob/EntityBurrowingSwingingBase.java", 109, "canAttackClass(target.getClass())", "canAttackClass((Class<? extends net.minecraft.entity.EntityLivingBase>) target.getClass())"),
 ("com/hbm/entity/mob/EntityUFOBase.java", 88, "canAttackClass(entity.getClass())", "canAttackClass((Class<? extends net.minecraft.entity.EntityLivingBase>) entity.getClass())"),
 ("com/hbm/entity/mob/EntityFBIDrone.java", 19, "super.updateEntityActionState();", "super.updateEntityActionStateNT();"),
 ("com/hbm/entity/mob/siege/EntitySiegeUFO.java", 107, "super.updateEntityActionState();", "super.updateEntityActionStateNT();"),
 ("com/hbm/entity/mob/ai/EntityMoonWalkHelper.java", 18, "if(this.update) {", "if(this.isUpdating()) {"),
 ("com/hbm/entity/mob/ai/EntityMoonWalkHelper.java", 19, "this.update = false;", "this.action = net.minecraft.entity.ai.EntityMoveHelper.Action.WAIT;"),
 ("com/hbm/entity/train/EntityRailCarBase.java", 483,
  "@Override public boolean interactFirst(EntityPlayer player) { if(train != null) return train.interactFirst(player); return super.interactFirst(player); }",
  "@Override public boolean processInitialInteract(EntityPlayer player, net.minecraft.util.EnumHand hand) { if(train != null) return train.interactFirst(player); return super.processInitialInteract(player, hand); }"),
 ("com/hbm/util/InventoryUtil.java", 53, "return tryAddItemToInventory(inv, 0, inv.length - 1, stack);", "return tryAddItemToInventory(inv, 0, inv.size() - 1, stack);"),
 ("com/hbm/util/InventoryUtil.java", 71, "if(doesStackDataMatch(inv[i], stack)) {", "if(doesStackDataMatch(inv.get(i), stack)) {"),
 ("com/hbm/util/InventoryUtil.java", 73, "int transfer = Math.min(stack.getCount(), inv[i].getMaxStackSize() - inv[i].getCount());", "int transfer = Math.min(stack.getCount(), inv.get(i).getMaxStackSize() - inv.get(i).getCount());"),
 ("com/hbm/util/InventoryUtil.java", 76, "inv[i].setCount(inv[i].getCount() + transfer);", "inv.get(i).setCount(inv.get(i).getCount() + transfer);"),
 ("com/hbm/util/InventoryUtil.java", 103, "if(inv[i] == null) {", "if(inv.get(i).isEmpty()) {"),
 ("com/hbm/util/InventoryUtil.java", 104, "inv[i] = stack;", "inv.set(i, stack);"),
 ("com/hbm/util/InventoryUtil.java", 185, "ItemStack in = inv[i];", "ItemStack in = inv.get(i);"),
 ("com/hbm/util/InventoryUtil.java", 194, "inv[i] = null;", "inv.set(i, net.minecraft.item.ItemStack.EMPTY);"),
 ("com/hbm/util/InventoryUtil.java", 232, "ItemStack[] original = player.inventory.mainInventory;", "java.util.List<ItemStack> original = player.inventory.mainInventory;"),
 ("com/hbm/util/InventoryUtil.java", 233, "ItemStack[] inventory = new ItemStack[original.length];", "ItemStack[] inventory = new ItemStack[original.size()];"),
 ("com/hbm/util/InventoryUtil.java", 234, "boolean[] modified = new boolean[original.length];", "boolean[] modified = new boolean[original.size()];"),
 ("com/hbm/util/InventoryUtil.java", 243, "for(int i = 0; i < original.length; i++) {", "for(int i = 0; i < original.size(); i++) {"),
 ("com/hbm/util/InventoryUtil.java", 244, "if(original[i] != null) {", "if(original.get(i) != null) {"),
 ("com/hbm/util/InventoryUtil.java", 245, "inventory[i] = original[i].copy();", "inventory[i] = original.get(i).copy();"),
 ("com/hbm/items/tool/ItemSettingsTool.java", 83, "if(schrodinger == null) return false;", "if(schrodinger == null) return net.minecraft.util.EnumActionResult.PASS;"),
 ("com/hbm/items/tool/ItemSettingsTool.java", 122, "return !world.isRemote;", "return !world.isRemote ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;"),
 ("com/hbm/blocks/generic/BlockOutgas.java", 133,
  "super.dropBlockAsItemWithChance(world, x, y, z, meta, chance, fortune);",
  "super.dropBlockAsItemWithChance(world, new net.minecraft.util.math.BlockPos(x, y, z), state, chance, fortune);"),
 ("com/hbm/tileentity/machine/TileEntityCyberCrab.java", 23,
  "this.world.getClosestPlayer(this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, 25)",
  "this.world.getClosestPlayer(this.pos.getX() + 0.5, this.pos.getY() + 1, this.pos.getZ() + 0.5, 25D, false)"),
 ("com/hbm/tileentity/machine/TileEntityMachineExcavator.java", 498,
  'Method createStackedBlock = ReflectionHelper.findMethod(Block.class, b, new String[] {"createStackedBlock", "func_149644_j"}, int.class);',
  'Method createStackedBlock = ReflectionHelper.findMethod(Block.class, "createStackedBlock", "func_149644_j", net.minecraft.block.state.IBlockState.class);'),
 ("com/hbm/tileentity/machine/TileEntityMachineExcavator.java", 499,
  "ItemStack result = (ItemStack) createStackedBlock.invoke(b, com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, x, y, z) ));",
  "ItemStack result = (ItemStack) createStackedBlock.invoke(b, com.hbm.world.WorldBlockState.read(this.world, x, y, z));"),
 ("com/hbm/entity/mob/ai/EntityAIBreaking.java", 126,
  "entityDigger.world.destroyBlockInWorldPartially(entityDigger.getEntityId(), markedLoc[0], markedLoc[1], markedLoc[2], (int)(str * 10F));", ""),
 ("com/hbm/tileentity/machine/rbmk/TileEntityRBMKKeyPad.java", 119,
  "TileEntityRBMKKeyPad.this.world.playSound(null, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5,",
  "TileEntityRBMKKeyPad.this.world.playSound(null, TileEntityRBMKKeyPad.this.pos.getX() + 0.5, TileEntityRBMKKeyPad.this.pos.getY() + 0.5, TileEntityRBMKKeyPad.this.pos.getZ() + 0.5,"),
 ("com/hbm/blocks/generic/LogicBlock.java", 46,
  "return logicBlock.disguise.getIcon(side, logicBlock.disguiseMeta);",
  "return ((com.hbm.blocks.BlockBase) logicBlock.disguise).getIcon(side, logicBlock.disguiseMeta);"),
 ("com/hbm/blocks/generic/LogicBlock.java", 131,
  'nbt.setString("disguise", GameRegistry.findUniqueIdentifierFor(disguise).toString());',
  'nbt.setString("disguise", disguise.getRegistryName().toString());'),
 ("com/hbm/handler/CompatHandler.java", 210,
  "disk.item = Items.registerFloppy(s, disk.color, disk.fs);",
  "disk.item = li.cil.oc.api.Items.registerFloppy(s, net.minecraft.item.EnumDyeColor.byMetadata(disk.color & 15), disk.fs, false);"),
 ("com/hbm/inventory/gui/GUIScreenWikiRender.java", 74,
  "saveScreenshot(Minecraft.getMinecraft().mcDataDir,", "saveScreenshot(Minecraft.getMinecraft().gameDir,"),
 ("com/hbm/inventory/gui/GUIScreenWikiRender.java", 136, "TextureUtil.func_147953_a(pixelValues, width, height);", ""),
 ("com/hbm/render/tileentity/RenderLoot.java", 196,
  "for(int i = 0; i < stack.getItem().getRenderPasses(stack.getItemDamage()); i++) {", "for(int i = 0; i < 1; i++) {"),
 ("com/hbm/render/tileentity/RenderLoot.java", 198,
  "IIcon icon = stack.getItem().getIcon(stack, i);",
  "IIcon icon = com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(stack.getItem(), stack.getItemDamage()));"),
 ("com/hbm/render/tileentity/RenderLoot.java", 204,
  "int k1 = stack.getItem().getColorFromItemStack(stack, i);",
  "int k1 = net.minecraft.client.Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, i);"),
 ("com/hbm/world/gen/NTMWorldGenerator.java", 275,
  "nbtGen.generateStructures(event.getWorld(), rand, event.chunkProvider, event.getChunkX(), event.getChunkZ());",
  "nbtGen.generateStructures(event.getWorld(), rand, event.getWorld().getChunkProvider(), event.getChunkX(), event.getChunkZ());"),
 ("com/hbm/render/entity/mob/RenderBehemoth.java", 30, "this.setRenderPassModel(this.mainModel);", ""),
 ("com/hbm/render/entity/mob/RenderGlyphid.java", 23, "this.setRenderPassModel(this.mainModel);", ""),
 ("com/hbm/render/entity/mob/RenderCreeperUniversal.java", 17, "super();", "super(net.minecraft.client.Minecraft.getMinecraft().getRenderManager());"),
 ("com/hbm/render/entity/mob/RenderCreeperUniversal.java", 65, "this.setRenderPassModel(this.creeperModel);", ""),
 ("com/hbm/render/entity/mob/RenderRADBeast.java", 29, "this.blazeModel = ((ModelBlaze) this.mainModel).func_78104_a();", ""),
 ("com/hbm/render/entity/mob/RenderRADBeast.java", 33, "int i = ((ModelBlaze) this.mainModel).func_78104_a();", ""),
 ("com/hbm/render/entity/mob/RenderRADBeast.java", 35, "if(i != this.blazeModel) {", ""),
 ("com/hbm/render/entity/mob/RenderRADBeast.java", 36, "this.blazeModel = i;", ""),
 ("com/hbm/render/entity/mob/RenderRADBeast.java", 37, "this.mainModel = new ModelBlaze();", ""),
 ("com/hbm/render/entity/mob/RenderRADBeast.java", 38, "}", ""),
 ("com/hbm/render/entity/mob/RenderRADBeast.java", 99, "this.setRenderPassModel(modelM65);", ""),
 ("com/hbm/render/entity/mob/RenderRADBeast.java", 103, "return super.shouldRenderPass(p_77032_1_, p_77032_2_, p_77032_3_);", "return 0;"),
 ("com/hbm/inventory/gui/GuiScreenRadioTelex.java", 175, "tess.startDrawing(GL11.GL_LINES);",
  "net.minecraft.client.renderer.BufferBuilder buf = tess.getBuffer(); buf.begin(GL11.GL_LINES, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);"),
 ("com/hbm/inventory/gui/GuiScreenRadioTelex.java", 176, "tess.setColorOpaque_I(0x00ff00);", "buf.color(0, 255, 0, 255);"),
 ("com/hbm/inventory/gui/GuiScreenRadioTelex.java", 179, "tess.addVertex(guiLeft + 199 + i, guiTop + 93.5 + offset, this.zLevel + 10);", "buf.pos(guiLeft + 199 + i, guiTop + 93.5 + offset, this.zLevel + 10).endVertex();"),
 ("com/hbm/inventory/gui/GuiScreenRadioTelex.java", 182, "tess.addVertex(guiLeft + 199 + i + 1, guiTop + 93.5 + offset, this.zLevel + 10);", "buf.pos(guiLeft + 199 + i + 1, guiTop + 93.5 + offset, this.zLevel + 10).endVertex();"),
 ("com/hbm/items/tool/ItemPipette.java", 184, "return pass == 1 ? this.overlayIcon : getIconFromDamageForRenderPass(stack.getItemDamage(), pass);", "return pass == 1 ? this.overlayIcon : this.itemIcon;"),
 ("com/hbm/blocks/fluid/GenericFluid.java", 21, "return this.block.getIcon(0, 0);",
  "return com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(net.minecraft.item.Item.getItemFromBlock(this.block), 0));"),
 ("com/hbm/blocks/fluid/GenericFluid.java", 26, "return this.block.getIcon(1, 0);",
  "return com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(net.minecraft.item.Item.getItemFromBlock(this.block), 1));"),
 ("com/hbm/blocks/generic/BlockNTMGlassPane.java", 31, "this.opaque = true;", ""),
]

INS = [
 # (rel, anchor_frag, lines_after_offset, lines_to_insert)
 ("com/hbm/render/model/ModelJetPack.java", "public class ModelJetPack extends ModelBiped {", 0,
  ["\tprotected int heldItemRight = 0;", "\tprotected boolean aimedBow = false;", ""]),
 ("com/hbm/entity/mob/botprime/EntityWormBaseNT.java", "protected void updateEntityActionStateNT() {", 0,
  ["\t@Override", "\tpublic void onUpdate() {", "\t\tsuper.onUpdate();", "\t\tif(!this.world.isRemote) this.updateEntityActionStateNT();", "\t}", ""]),
 ("com/hbm/entity/mob/EntityUFOBase.java", "protected void updateEntityActionStateNT() {", 0,
  ["\t@Override", "\tpublic void onUpdate() {", "\t\tsuper.onUpdate();", "\t\tif(!this.world.isRemote) this.updateEntityActionStateNT();", "\t}", ""]),
 ("com/hbm/entity/mob/EntityMaskMan.java", "public class EntityMaskMan extends EntityMob implements INtmBossDisplay, IRadiationImmune, ISuffocationImmune {", 0,
  ["\tprivate float prevHealth;", ""]),
 ("com/hbm/entity/mob/EntityMaskMan.java", "this.world.createExplosion(this, posX, posY + 4, posZ, 2.5F, true);", 1,
  ["\t\tthis.prevHealth = this.getHealth();"]),
 ("com/hbm/items/tool/ItemPipette.java", "@SideOnly(Side.CLIENT) protected IIcon emptyIcon;", 0,
  ["\t@SideOnly(Side.CLIENT) protected IIcon itemIcon;"]),
]

MULTI = [
 ("com/hbm/blocks/machine/RailHighspeed.java",
  "\t\treturn 1.0f;\n\t}\n}",
  "\t\treturn 1.0f;\n\t}\n\n\t@Override\n\tpublic net.minecraft.block.properties.IProperty<net.minecraft.block.BlockRailBase.EnumRailDirection> getShapeProperty() { return net.minecraft.block.BlockRail.SHAPE; }\n}"),
 ("com/hbm/entity/mob/EntityFBI.java",
  "\t\treturn super.onInitialSpawn(difficulty, data);\n\t}",
  "\t\treturn super.onInitialSpawn(difficulty, data);\n\t}\n\n\t@Override\n\tpublic void setSwingingArms(boolean swingingArms) { }"),
 ("com/hbm/world/gen/MapGenNTMFeatures.java",
  "\t\treturn \"NTMFeatures\";\n\t}",
  "\t\treturn \"NTMFeatures\";\n\t}\n\n\t@Override\n\tpublic net.minecraft.util.math.BlockPos getNearestStructurePos(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos position, boolean findUnexplored) { return null; }"),
 ("com/hbm/wiaj/FauxWorld.java",
  "\tprotected IChunkProvider createChunkProvider() {\n\t\treturn null;\n\t}",
  "\tprotected IChunkProvider createChunkProvider() {\n\t\treturn null;\n\t}\n\n\t@Override\n\tpublic boolean isChunkLoaded(int x, int z, boolean allowEmpty) {\n\t\treturn false;\n\t}"),
 ("com/hbm/wiaj/FauxWorld.java",
  "\t\tsuper(new SaveHandlerMP(), \"FauxWorld\", p_i45368_3_, p_i45368_4_, p_i45368_5_);",
  "\t\tsuper(new SaveHandlerMP(), new net.minecraft.world.storage.WorldInfo(p_i45368_4_, \"FauxWorld\"), p_i45368_3_, p_i45368_5_, true);"),
 ("com/hbm/blocks/generic/BlockPinkSlab.java",
  "\t@Override\n\tpublic String getTranslationKey(int p_150002_1_) {\n\t\treturn null;\n\t}\n\n}",
  "\t@Override\n\tpublic String getTranslationKey(int p_150002_1_) {\n\t\treturn null;\n\t}\n\n\t@Override\n\tpublic boolean isDouble() { return false; }\n\n\t@Override\n\tpublic Comparable<?> getTypeForItem(net.minecraft.item.ItemStack stack) { return stack.getMetadata(); }\n\n\tpublic BlockPinkSlab setDouble(boolean dbl) { return this; }\n}"),
 ("com/hbm/dim/WorldChunkManagerCelestial.java",
  "\t@Override\n\tpublic float[] getRainfall(float[] values, int x, int z, int width, int length) {\n\t\tIntCache.resetIntCache();\n\n\t\tif(values == null || values.length < width * length) {\n\t\t\tvalues = new float[width * length];\n\t\t}\n\n\t\tint[] biomeIds = biomeDetailLayer.getInts(x, z, width, length);\n\n\t\tfor(int i = 0; i < width * length; ++i) {\n\t\t\tfloat rainfall = Biome.getBiome(biomeIds[i]).getRainfall();\n\n\t\t\tif(rainfall > 1.0F) rainfall = 1.0F;\n\n\t\t\tvalues[i] = rainfall;\n\t\t}\n\n\t\treturn values;\n\t}",
  "\t@Override\n\tpublic int[] getRainfall(int[] values, int x, int z, int width, int length) {\n\t\tIntCache.resetIntCache();\n\n\t\tif(values == null || values.length < width * length) {\n\t\t\tvalues = new int[width * length];\n\t\t}\n\n\t\tint[] biomeIds = biomeDetailLayer.getInts(x, z, width, length);\n\n\t\tfor(int i = 0; i < width * length; ++i) {\n\t\t\tfloat rainfall = Biome.getBiome(biomeIds[i]).getRainfall();\n\n\t\t\tif(rainfall > 1.0F) rainfall = 1.0F;\n\n\t\t\tvalues[i] = (int) (rainfall * 65536.0F);\n\t\t}\n\n\t\treturn values;\n\t}"),
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
    a, b = max(0, lineno - 6), min(len(contents), lineno + 5)
    hits = [i for i in range(a, b) if frag in contents[i]]
    return hits[0] if len(hits) == 1 else None

def main():
    apply = '--apply' in sys.argv
    fails = []
    done = 0
    for rel, lineno, frag in RM:
        path = os.path.join(ROOT, 'src', 'main', 'java', rel.replace('/', os.sep))
        contents, terms = load(path)
        i = find_line(contents, lineno, frag)
        if i is None:
            fails.append((rel, lineno, 'RM-MISS', frag[:60])); continue
        j = i - 1
        while j >= 0 and contents[j].strip().startswith('@SideOnly'):
            j -= 1
        if j < 0 or not contents[j].strip().startswith('@Override'):
            fails.append((rel, lineno, 'RM-MISS-OVR', frag[:60])); continue
        del contents[j]; del terms[j]
        if apply: save(path, contents, terms)
        done += 1
    for rel, lineno, old, new, adapter in DECL:
        path = os.path.join(ROOT, 'src', 'main', 'java', rel.replace('/', os.sep))
        contents, terms = load(path)
        i = find_line(contents, lineno, old)
        if i is None:
            fails.append((rel, lineno, 'DECL-MISS', old[:60])); continue
        contents[i] = contents[i].replace(old, new)
        if adapter:
            contents.insert(i + 1, adapter)
            terms.insert(i + 1, terms[i])
        if apply: save(path, contents, terms)
        done += 1
    for rel, lineno, old, new in REP:
        path = os.path.join(ROOT, 'src', 'main', 'java', rel.replace('/', os.sep))
        if not os.path.exists(path):
            fails.append((rel, lineno, 'NOFILE', old[:60])); continue
        contents, terms = load(path)
        i = find_line(contents, lineno, old)
        if i is None:
            fails.append((rel, lineno, 'REP-MISS', old[:60])); continue
        contents[i] = contents[i].replace(old, new)
        if apply: save(path, contents, terms)
        done += 1
    for rel, anchor, offset, lines in INS:
        path = os.path.join(ROOT, 'src', 'main', 'java', rel.replace('/', os.sep))
        contents, terms = load(path)
        hits = [i for i, c in enumerate(contents) if anchor in c]
        if len(hits) != 1:
            fails.append((rel, 0, 'INS-HITS=%d' % len(hits), anchor[:60])); continue
        i = hits[0] + 1 + offset
        for k, ln in enumerate(lines):
            contents.insert(i + k, ln)
            terms.insert(i + k, terms[i - 1])
        if apply: save(path, contents, terms)
        done += 1
    for rel, old_block, new_block in MULTI:
        path = os.path.join(ROOT, 'src', 'main', 'java', rel.replace('/', os.sep))
        raw = open(path, 'rb').read().decode('utf-8')
        nl = '\r\n' if '\r\n' in raw else '\n'
        old_t = old_block.replace('\n', nl)
        new_t = new_block.replace('\n', nl)
        if raw.count(old_t) != 1:
            fails.append((rel, 0, 'MULTI-HITS=%d' % raw.count(old_t), old_block[:60])); continue
        if apply:
            open(path, 'wb').write(raw.replace(old_t, new_t).encode('utf-8'))
        done += 1
    total = len(RM) + len(DECL) + len(REP) + len(INS) + len(MULTI)
    for f in fails:
        print('FAIL %-72s L%-5d %-14s | %s' % f)
    print('total ops: %d, ok: %d, failed: %d, mode: %s' % (total, done, len(fails), 'APPLY' if apply else 'PREVIEW'))
    if fails:
        sys.exit(1)

if __name__ == '__main__':
    main()

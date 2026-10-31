import os, sys

def patch_file(path, old_text, new_text):
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    if old_text not in content:
        raise ValueError(f"Target text not found in {path}")
    content = content.replace(old_text, new_text, 1)
    with open(path, 'w', encoding='utf-8', newline='') as f:
        f.write(content)
    print(f"Patched {path}")

# 1. TextureAtlasSpriteMutatable.java
path_tasm = "src/main/java/com/hbm/render/icon/TextureAtlasSpriteMutatable.java"
old_tasm = """\t\tint domain = path.indexOf(':');
\t\tif(domain >= 0) source = new ResourceLocation(path.substring(0, domain), path.substring(domain + 1));
\t\telse source = new ResourceLocation(location.getNamespace(), path);"""

new_tasm = """\t\tint domain = path.indexOf(':');
\t\tString domainStr = (domain >= 0) ? path.substring(0, domain) : location.getNamespace();
\t\tif(domainStr == null || domainStr.isEmpty() || "minecraft".equals(domainStr)) {
\t\t\tdomainStr = com.hbm.lib.RefStrings.MODID;
\t\t}
\t\tString subPath = (domain >= 0) ? path.substring(domain + 1) : path;
\t\tsource = new ResourceLocation(domainStr, subPath);"""

patch_file(path_tasm, old_tasm, new_tasm)

old_tasm_load = """\t\ttry(IResource resource = manager.getResource(sourceTexture)) {
\t\t\tloadSprite(PngSizeInfo.makeFromResource(resource), false);
\t\t\tloadSpriteFrames(resource, mipmap);
\t\t\tmutateFrames();"""

new_tasm_load = """\t\ttry(IResource resource = manager.getResource(sourceTexture)) {
\t\t\tloadSprite(PngSizeInfo.makeFromResource(resource), false);
\t\t\tint effectiveMipmap = Math.max(1, Minecraft.getMinecraft().gameSettings.mipmapLevels + 1);
\t\t\tloadSpriteFrames(resource, effectiveMipmap);
\t\t\tmutateFrames();"""

patch_file(path_tasm, old_tasm_load, new_tasm_load)

# 2. ItemWatzPellet.java
path_watz = "src/main/java/com/hbm/items/machine/ItemWatzPellet.java"
old_watz = """\t\tfor(int i = 0; i < EnumWatzType.values().length; i++) {
\t\t\tEnumWatzType type = EnumWatzType.values()[i];
\t\t\tString placeholderName = RefStrings.MODID + ":watz_pellet-" + type.name() + getRegistryName().getPath();"""

new_watz = """\t\tfor(int i = 0; i < EnumWatzType.values().length; i++) {
\t\t\tEnumWatzType type = EnumWatzType.values()[i];
\t\t\tString placeholderName = RefStrings.MODID + ":watz_pellet-" + type.name().toLowerCase(java.util.Locale.ROOT) + "-" + (this == ModItems.watz_pellet_depleted ? "depleted" : "active");"""

patch_file(path_watz, old_watz, new_watz)

# 3. RouteFallbackModelRepair.java
path_rfmr = "src/main/java/com/hbm/render/item/RouteFallbackModelRepair.java"
old_rfmr_repair = """        java.lang.reflect.Method byMeta = declaredCallback(item, "getIconFromDamage", int.class);
        java.lang.reflect.Method byPass = declaredCallback(item, "getIcon", ItemStack.class, int.class);
        if(byMeta != null || byPass != null) {
            IBakedModel callbackModel = callbackModel(item, byMeta, byPass);
            if(callbackModel != null) return callbackModel;
        }"""

new_rfmr_repair = """        java.lang.reflect.Method byMeta = declaredCallback(item, "getIconFromDamage", int.class);
        java.lang.reflect.Method byPass = declaredCallback(item, "getIcon", ItemStack.class, int.class);
        java.lang.reflect.Method byDamagePass = declaredCallback(item, "getIconFromDamageForRenderPass", int.class, int.class);
        if(byMeta != null || byPass != null || byDamagePass != null) {
            IBakedModel callbackModel = callbackModel(item, byMeta, byPass, byDamagePass);
            if(callbackModel != null) return callbackModel;
        }"""

patch_file(path_rfmr, old_rfmr_repair, new_rfmr_repair)

old_rfmr_cb = """    private static IBakedModel callbackModel(Item item,
            java.lang.reflect.Method byMeta, java.lang.reflect.Method byPass) {
        // Real stacks (from getSubItems where available) preserve metadata and
        // NBT; molds carry IDs beyond the 4-bit block-metadata range.
        List<ItemStack> resolvedStacks = new ArrayList<>();
        List<List<TextureAtlasSprite>> layersList = new ArrayList<>();
        for(ItemStack stack : subItems(item)) {
            List<TextureAtlasSprite> layers = layersFor(item, byMeta, byPass, stack);"""

new_rfmr_cb = """    private static IBakedModel callbackModel(Item item,
            java.lang.reflect.Method byMeta, java.lang.reflect.Method byPass, java.lang.reflect.Method byDamagePass) {
        // Real stacks (from getSubItems where available) preserve metadata and
        // NBT; molds carry IDs beyond the 4-bit block-metadata range.
        List<ItemStack> resolvedStacks = new ArrayList<>();
        List<List<TextureAtlasSprite>> layersList = new ArrayList<>();
        for(ItemStack stack : subItems(item)) {
            List<TextureAtlasSprite> layers = layersFor(item, byMeta, byPass, byDamagePass, stack);"""

patch_file(path_rfmr, old_rfmr_cb, new_rfmr_cb)

old_rfmr_layers = """    private static List<TextureAtlasSprite> layersFor(Item item,
            java.lang.reflect.Method byMeta, java.lang.reflect.Method byPass, ItemStack stack) {
        try {
            List<TextureAtlasSprite> layers = new ArrayList<>();
            if(byPass != null) {
                for(int pass = 0; pass < 2; pass++) {
                    TextureAtlasSprite sprite = NativeItemParticles.spriteOf(
                            (com.hbm.render.icon.IIcon) byPass.invoke(item, stack, pass));
                    if(sprite == null) break;
                    layers.add(sprite);
                }
            }
            if(layers.isEmpty() && byMeta != null) {
                TextureAtlasSprite sprite = NativeItemParticles.spriteOf(
                        (com.hbm.render.icon.IIcon) byMeta.invoke(item, stack.getMetadata()));
                if(sprite != null) layers.add(sprite);
            }
            return layers.isEmpty() ? null : layers;
        } catch(ReflectiveOperationException | RuntimeException failure) {
            return null;
        }
    }"""

new_rfmr_layers = """    private static List<TextureAtlasSprite> layersFor(Item item,
            java.lang.reflect.Method byMeta, java.lang.reflect.Method byPass, java.lang.reflect.Method byDamagePass, ItemStack stack) {
        try {
            List<TextureAtlasSprite> layers = new ArrayList<>();
            if(byDamagePass != null) {
                java.lang.reflect.Method getPasses = declaredCallback(item, "getRenderPasses", int.class);
                int passes = 2;
                if(getPasses != null) {
                    try {
                        passes = (int) getPasses.invoke(item, stack.getMetadata());
                    } catch(Exception ignored) {}
                }
                for(int pass = 0; pass < passes; pass++) {
                    TextureAtlasSprite sprite = NativeItemParticles.spriteOf(
                            (com.hbm.render.icon.IIcon) byDamagePass.invoke(item, stack.getMetadata(), pass));
                    if(sprite != null) layers.add(sprite);
                }
            } else if(byPass != null) {
                for(int pass = 0; pass < 2; pass++) {
                    TextureAtlasSprite sprite = NativeItemParticles.spriteOf(
                            (com.hbm.render.icon.IIcon) byPass.invoke(item, stack, pass));
                    if(sprite == null) break;
                    layers.add(sprite);
                }
            }
            if(layers.isEmpty() && byMeta != null) {
                TextureAtlasSprite sprite = NativeItemParticles.spriteOf(
                        (com.hbm.render.icon.IIcon) byMeta.invoke(item, stack.getMetadata()));
                if(sprite != null) layers.add(sprite);
            }
            return layers.isEmpty() ? null : layers;
        } catch(ReflectiveOperationException | RuntimeException failure) {
            return null;
        }
    }"""

patch_file(path_rfmr, old_rfmr_layers, new_rfmr_layers)

# 4. BarbedWireObjModelHandler.java
path_bw = "src/main/java/com/hbm/main/BarbedWireObjModelHandler.java"
old_bw = """\t\t\t// Inventory icons use metadata zero; all current BarbedWire metadata values
\t\t\t// resolve to this identity's same icon, as in the legacy block callback.
\t\t\tString texture = RefStrings.MODID + ":blocks/" + name.getPath() + "_model";"""

new_bw = """\t\t\t// Inventory icons use metadata zero; all current BarbedWire metadata values
\t\t\t// resolve to this identity's same icon, as in the legacy block callback.
\t\t\tString pathName = name.getPath();
\t\t\tif(pathName.startsWith("tile.")) pathName = pathName.substring(5);
\t\t\tString texture = RefStrings.MODID + ":blocks/" + pathName + "_model";"""

patch_file(path_bw, old_bw, new_bw)

# 5. DroneDock.java
path_dd = "src/main/java/com/hbm/blocks/network/DroneDock.java"
old_dd = """\t@SideOnly(Side.CLIENT)
\tpublic void registerBlockIcons(IIconRegister reg) {
\t\t// The original dock/crate blocks registered side, top and bottom faces.
\t\tString base = com.hbm.blocks.LegacyBlockTextures.texture(this);
\t\tif(base == null) base = this.getRegistryName().toString();
\t\tthis.iconSide = reg.registerIcon(base + "_side");
\t\tthis.iconTop = reg.registerIcon(base + "_top");
\t\tthis.iconBottom = reg.registerIcon(base + "_bottom");
\t}

\t/** Legacy side icon lookup; null until the atlas pass has run. */
\t@SideOnly(Side.CLIENT)
\tpublic IIcon getLegacyIcon(int side) {
\t\tif(side == 0) return this.iconBottom;
\t\tif(side == 1) return this.iconTop;
\t\treturn this.iconSide;
\t}"""

new_dd = """\t@SideOnly(Side.CLIENT)
\tpublic void registerBlockIcons(IIconRegister reg) {
\t\t// The original dock/crate blocks registered side, top and bottom faces.
\t\tString base = this.getRegistryName().toString();
\t\tif(base.startsWith("hbm:tile.")) base = "hbm:" + base.substring(9);
\t\tthis.iconSide = reg.registerIcon(base + "_side");
\t\tthis.iconTop = reg.registerIcon(base + "_top");
\t\tthis.iconBottom = reg.registerIcon(base + "_bottom");
\t}

\t/** Legacy side icon lookup; null until the atlas pass has run. */
\t@SideOnly(Side.CLIENT)
\tpublic IIcon getLegacyIcon(int side) {
\t\tif(side == 0) return this.iconBottom;
\t\tif(side == 1) return this.iconTop;
\t\treturn this.iconSide;
\t}

\t@SideOnly(Side.CLIENT)
\tpublic IIcon getIcon(int side, int meta) {
\t\treturn getLegacyIcon(side);
\t}"""

patch_file(path_dd, old_dd, new_dd)

# 6. ModBlocks.java
path_mb = "src/main/java/com/hbm/blocks/ModBlocks.java"
old_mb_drone = """\t\tdrone_dock = com.hbm.blocks.LegacyBlockTextures.bind(new DroneDock().setTranslationKey("drone_dock").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab), RefStrings.MODID + ":drone_dock");
\t\tdrone_crate_provider = com.hbm.blocks.LegacyBlockTextures.bind(new DroneDock().setTranslationKey("drone_crate_provider").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab), RefStrings.MODID + ":drone_crate_provider");
\t\tdrone_crate_requester = com.hbm.blocks.LegacyBlockTextures.bind(new DroneDock().setTranslationKey("drone_crate_requester").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab), RefStrings.MODID + ":drone_crate_requester");"""

new_mb_drone = """\t\tdrone_dock = new DroneDock().setTranslationKey("drone_dock").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
\t\tdrone_crate_provider = new DroneDock().setTranslationKey("drone_crate_provider").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);
\t\tdrone_crate_requester = new DroneDock().setTranslationKey("drone_crate_requester").setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);"""

patch_file(path_mb, old_mb_drone, new_mb_drone)

old_mb_flesh = """\t\tflesh_block = new MeltedFlesh(Material.SNOW).withSoundType(SoundType.GROUND).setTranslationKey("flesh_block").setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0);
\t\tcom.hbm.blocks.LegacyBlockTextures.bind(flesh_block, RefStrings.MODID + ":meltedFlesh");
\t\tcharred_flesh_block = new MeltedFlesh(Material.SNOW).withSoundType(SoundType.GROUND).setTranslationKey("charred_flesh_block").setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0);
\t\tcom.hbm.blocks.LegacyBlockTextures.bind(charred_flesh_block, RefStrings.MODID + ":charredFlesh");
\t\tcarbonized_flesh_block = new MeltedFlesh(Material.SNOW).withSoundType(SoundType.GROUND).setTranslationKey("carbonized_flesh_block").setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0);
\t\tcom.hbm.blocks.LegacyBlockTextures.bind(carbonized_flesh_block, RefStrings.MODID + ":carbonizedFlesh");"""

new_mb_flesh = """\t\tflesh_block = new MeltedFlesh(Material.SNOW).withSoundType(SoundType.GROUND).setTranslationKey("flesh_block").setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0);
\t\tcharred_flesh_block = new MeltedFlesh(Material.SNOW).withSoundType(SoundType.GROUND).setTranslationKey("charred_flesh_block").setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0);
\t\tcarbonized_flesh_block = new MeltedFlesh(Material.SNOW).withSoundType(SoundType.GROUND).setTranslationKey("carbonized_flesh_block").setCreativeTab(MainRegistry.blockTab).setHardness(0.1F).setLightOpacity(0);"""

patch_file(path_mb, old_mb_flesh, new_mb_flesh)

# 7. BlockNTMGlassPane.java
path_pane = "src/main/java/com/hbm/blocks/generic/BlockNTMGlassPane.java"
old_pane = """\t@Override
\t@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
\tpublic net.minecraft.util.BlockRenderLayer getRenderLayer() {
\t\treturn net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED;
\t}"""

new_pane = """\t@Override
\t@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
\tpublic net.minecraft.util.BlockRenderLayer getRenderLayer() {
\t\treturn renderLayer == 1 ? net.minecraft.util.BlockRenderLayer.TRANSLUCENT : net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED;
\t}"""

patch_file(path_pane, old_pane, new_pane)

# 8. WasteLeaves.java
path_wl = "src/main/java/com/hbm/blocks/generic/WasteLeaves.java"
old_wl = """\t@SideOnly(Side.CLIENT)
\tpublic void registerBlockIcons(IIconRegister iconRegister) {
\t\tthis.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":waste_leaves");
\t}"""

new_wl = """\t@SideOnly(Side.CLIENT)
\tpublic void registerBlockIcons(IIconRegister iconRegister) {
\t\tthis.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":waste_leaves");
\t}

\t@SideOnly(Side.CLIENT)
\tpublic IIcon getIcon(int side, int meta) {
\t\treturn this.blockIcon;
\t}

\t@Override
\t@SideOnly(Side.CLIENT)
\tpublic net.minecraft.util.BlockRenderLayer getRenderLayer() {
\t\treturn net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED;
\t}"""

patch_file(path_wl, old_wl, new_wl)

print("All code patches applied successfully!")

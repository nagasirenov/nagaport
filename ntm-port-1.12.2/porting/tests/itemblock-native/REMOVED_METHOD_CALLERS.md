# Removed legacy ItemBlockBase callbacks: caller audit

Audited all Java source occurrences of getIconFromDamage/getColorFromItemStack (raw inventory in removed-method-references.json). No direct ItemBlockBase-typed call or ItemBlockBase descendant super-call to these methods was found. That does NOT mean all arbitrary Item callers are safe.

## Exact unresolved consumers that can receive an ItemBlock

- com/hbm/items/BrokenItem.java:35: arbitrary registry item selected by NBT itemID calls getIconFromDamage(meta). Needs native model/texture composition retaining its two passes and stored metadata; do not substitute a fixed icon.
- com/hbm/items/ItemRemap.java:28: remapItem is declared Item, so target may be an ItemBlock. Needs native model delegation preserving remapMeta. This class is NOT ItemBlockRemap; the latter's gameplay conversion is tested here.
- com/hbm/util/ColorUtil.java:24: arbitrary stack.getItem() calls getIconFromDamage then constructs textures/items path. Needs a defined native textured-model sampling policy; a model particle sprite alone is not proof of original average-image semantics.
- com/hbm/render/entity/effect/MultiCloudRenderer.java:79: constructor accepts arbitrary Item[]; chosen item calls getIconFromDamage with configured metadata. Needs native sprite/model route retaining age selection and metadata.
- com/hbm/render/tileentity/RenderLoot.java:204: arbitrary inventory stack calls getColorFromItemStack(stack, legacy render pass). Needs native baked-quad tint-index dispatch through ItemColors plus its existing render-pass/geometry semantics. Legacy pass is not automatically a native tint index.

These are already legacy API consumers against native Item and remain full-mod compilation/rendering gates. No compatibility method or replacement Forge API was added to hide them.

## Reflective consumer

Actual reachability proves no regression for this handler's registered ItemBlockBase routes:

1. LegacyBaseModelHandler.java:62–65 selects only eligible BlockBase objects. Lines72–76 reject every non-BlockBase ItemBlockBase, then record that actual block in itemModels. The only other route, lines77–78, is ItemEnumMulti, not an ItemBlockBase.
2. Color registration lines135–144 iterates only itemModels. Thus every registered ItemBlockBase color callback has a BlockBase argument; the null/white branch cannot be a non-BlockBase ItemBlockBase route.
3. Before removal, lines162–163 already explicitly rejected the root-declared getColorFromItemStack when block instanceof BlockBase. After removal lookup returns null. Both cases execute the identical getLegacyRenderColor(block, stack.getMetadata()) at167. None of the five transitive production descendants overrides the removed icon/color callbacks; arbitrary future subclass overrides would still be discovered by reflection.
4. Item models call LegacyBaseBakedModel.forBlockItem at handler115. That factory's lines69–71 read block.getIcon(1, metadata) directly, never ItemBlockBase.getIconFromDamage. Its textured block geometry remains unchanged.
5. BlockNTMFlower and BlockTallPlant are actually BlockBase descendants via BlockEnumMulti→BlockMulti, but both return legacy getRenderType()==1 (flower135–136, tall119–120). hasCustomLegacyRenderType excludes them at handler205–208 and232–236. They were not registered by this handler before this patch. BlockVinylVine and BlockRubberLeaves extend native BlockVine/BlockLeaves and fail the BlockBase guard outright. Their missing broader color paths are previously blocked owner gates, not a new fallback regression.

No handler adaptation or new shim is needed. check-legacy-route.py checks these actual source guards and the complete transitive descendant set; this finite source proof is explicitly separate from runtime mesh tests.

Additional transitive descendant discovered: ItemBlockColoredConcrete extends ItemBlockBlastInfo; ModBlocks.java:3065 registers concrete_colored with it. Its legacy getUnlocalizedName(ItemStack), ItemDye.field_150923_a and BlockConcreteColored.func_150032_b remain a naming migration gate. It has no removed icon/color callback override. Four earlier descendants referred only to direct children; the total transitive count is five.

## Other matching calls are not ItemBlockBase paths

LegacyBaseBakedModel.forEnumItem accepts ItemEnumMulti, not ItemBlockBase; forBlockItem reads the block icon directly. ItemSiegeCoin and ItemRenderAberrator explicitly access vanilla stick/sword. ItemAutogen, ItemBedrockOreNew, ItemMineralOre, ItemMarshmallow, ItemVOTVdrive, ItemScraps, ItemWatzPellet and ItemAmmoArty invoke their own unrelated item-family callbacks. RenderGenericGrenade uses its grenade item's callback. Their legacy APIs remain their own migration gates; removal from ItemBlockBase does not migrate them.

## Native engine replacement proved here

Scaffold and fence use actual registered production ItemBlockBase with existing native model registry/loaders and mesher. Native engine rendering does not invoke getIconFromDamage. Root tint tests register native ItemColors and invoke native BlockColors with metadata and tint index; white inherited owners return the native -1 no-tint sentinel. This is selected-owner evidence only, not blanket replacement for every consumer listed above.

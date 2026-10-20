# BlockMulti native-state source closure audit

Read-only production-source audit of CP83 at `/workspace/shared/nagaport/ntm-port-1.12.2`, compared with `/workspace/shared/nagaport/ntm-original-pristine`. This report accompanies the bounded CP84 `BlockMulti` metadata-state change. No full build, game run, network access, or production edit was performed for this audit. Paths below are relative to `src/main/java/com/hbm/` unless otherwise stated.

## Actual scope and counts

- One shared state owner changes: abstract `BlockMulti`, not `BlockBase`.
- Transitive closure: **33 classes including BlockMulti, 32 descendants**. Four immediate descendants are `BlockEnumMulti`, `BlockToolConversion`, `BlockDecoCRT`, and `BlockDecoToaster`.
- **25 descendant classes inherit the new codecs/container**. Of those, **24 classes are instantiated**, in **29 constructor sites / registered block instances**. `BlockToolConversionPillar` has no constructor site.
- **Six pre-existing complete native state owners** protect **seven descendant classes / seven registered instances**: `BlockDecoCRT`, `BlockDecoToaster`, `BlockDecoModel` (also inherited by `BlockDecoContainer`), `BlockNTMFlower`, `BlockRubberCacti`, and `BlockWoodStructure`. All six own `createBlockState`, `getStateFromMeta`, and `getMetaFromState`; there are **no partial overrides** in this closure.
- Thus **36 constructor sites / registered instances total**, all in `blocks/ModBlocks.java`. No anonymous `BlockMulti` construction was found. Slab classes with “Multi” in their names and other independent `IBlockMulti` implementations are not in this inheritance closure.

## Complete class closure

“I” means inherits the new BlockMulti state owner. “O” means owns an existing complete native container/codec set. “D” means inherits an existing owner. Counts are construction sites, all registered once in ModBlocks.

| Class | Direct parent | State route | Instances and fields |
|---|---|---|---|
| BlockMulti | BlockBase | New abstract root | 0 |
| BlockEnumMulti | BlockMulti | I | 2: vinyl_tile, laythe_coral_block |
| BlockToolConversion | BlockMulti | I | 1: watz_end |
| BlockDecoCRT | BlockMulti | O | 1: deco_crt |
| BlockDecoToaster | BlockMulti | O | 1: deco_toaster |
| BlockAbsorber | BlockEnumMulti | I | 1: rad_absorber |
| BlockBiomeStone | BlockEnumMulti | I | 1: stone_biome |
| BlockCM | BlockEnumMulti | I | 4: cm_block, cm_sheet, cm_engine, cm_circuit |
| BlockCap | BlockEnumMulti | I | 1: block_cap |
| BlockCoke | BlockEnumMulti | I | 1: block_coke |
| BlockConcreteColoredExt | BlockEnumMulti | I | 1: concrete_colored_ext |
| BlockCrashedBomb | BlockEnumMulti | I | 1: crashed_balefire |
| BlockDeadPlant | BlockEnumMulti | I | 1: plant_dead |
| BlockDecoModel | BlockEnumMulti | O | 1: deco_computer |
| BlockICFLaserComponent | BlockEnumMulti | I | 1: icf_laser_component |
| BlockLightstone | BlockEnumMulti | I | 1: lightstone |
| BlockMeteorOre | BlockEnumMulti | I | 1: ore_meteor |
| BlockNTMFlower | BlockEnumMulti | O | 1: plant_flower |
| BlockNTMSand | BlockEnumMulti | I | 1: sand_mix |
| BlockOreBasalt | BlockEnumMulti | I | 1: ore_basalt |
| BlockResourceStone | BlockEnumMulti | I | 1: stone_resource |
| BlockRubberCacti | BlockEnumMulti | O | 1: spike_cacti |
| BlockRubberPlant | BlockEnumMulti | I | 1: rubber_plant |
| BlockStalagmite | BlockEnumMulti | I | 2: stalagmite, stalactite |
| BlockTallPlant | BlockEnumMulti | I | 1: plant_tall |
| BlockTallPlantWater | BlockEnumMulti | I | 1: plant_tall_laythe |
| BlockWoodStructure | BlockEnumMulti | O, extended state | 1: wood_structure |
| BlockCMGlass | BlockCM | I | 1: cm_tank |
| BlockCMPort | BlockCM | I | 1: cm_port |
| BlockDecoContainer | BlockDecoModel | D | 1: filing_cabinet |
| BlockFusionComponent | BlockToolConversion | I | 1: fusion_component |
| BlockICFComponent | BlockToolConversion | I | 1: icf_component |
| BlockToolConversionPillar | BlockToolConversion | I | 0 |

Owner references: `blocks/generic/BlockDecoCRT.java:23-30`, `BlockDecoToaster.java:20-24`, `BlockDecoModel.java:25-28`, `BlockNTMFlower.java:36-56`, `BlockRubberCacti.java:30-61`, and `BlockWoodStructure.java:21-37`.

Constructor references in `blocks/ModBlocks.java`: 1488, 1531-1534, 1537, 1640, 1662, 1694, 1729, 1754, 1788, 1850-1853, 1900, 1918-1920, 1922, 1931, 2013, 2264-2269, 2293, 2305, 2307, 2312, 2620, and 2704-2705. These expand to 36 sites. Matching registrations occur at 2838, 2865, 2879-2882, 2953, 2991, 3049, 3068, 3090, 3130, 3177-3180, 3224-3226, 3231-3233, 3314, 3358, 3431, 3826, 3839-3844, 3869, 3881, 3886, and 3889. `ModBlocks.java:4053-4058` routes ordinary registrations to `ItemBlockBase`; the two `ItemBlockBlastInfo` registrations also descend from ItemBlockBase.

## Metadata and construction invariants

1. Preserve the full legacy **0..15 metadata nibble**, not `rectify(meta)` or an enum-length clamp, in native encoding/decoding. Pristine `blocks/BlockMulti.java:22-24` rectifies **item drop damage only**; pristine `blocks/IBlockMulti.java` computes `Math.abs(meta % getSubCount())`. That is not the world-state storage contract.
2. `BlockTallPlant` has four item variants but stores lower halves 0..3 and upper halves 8..11. `BlockTallPlantWater` has **one** item variant but stores lower=0 and upper=8. Pristine and port sources both test `meta > 7`, match lower metadata using `meta - 8`, and place `stack.getItemDamage() + 8`. Port references: `blocks/generic/BlockTallPlant.java:99-100,164-168,197-199`; `BlockTallPlantWater.java:64-65,135-139,173-175`. These bits also control growth, cut detection, and drops. A subtype-range state property would destroy valid upper halves.
3. No currently constructed closure instance has `getSubCount() > 16`; the maximum is **8** (resource stone, extended concrete, NTM flower). All 29 newly affected instances together expose **122 creative item variants**. The seven existing-owner instances expose counts 4, 3, 1, 2, 8, 3, 3. `BlockEnumMulti` remains generic and its public enum constructor could accept larger enums in future, but no current registration does so. `BlockToolConversion` derives its count from `names.length + 1`: watz=2, fusion=4, ICF=5; the uninstantiated pillar is not evidence of a larger runtime count.
4. Do not consult `getSubCount()` during the Block superclass constructor: `BlockEnumMulti.theEnum` is assigned only after `super(mat)` (`blocks/BlockEnumMulti.java:19-23`), and tool-conversion names are assigned later. A constant 0..15 property avoids this initialization-order hazard.
5. Do not unconditionally assign the shared property in a `BlockMulti` constructor. Native `Block` dispatches `createBlockState()` virtually during construction, so descendants may supply `TYPE`, `VARIANT`, or an extended container instead. The CP84 implementation's lack of a new constructor property assignment preserves those owners. In particular, RubberCacti's property is an enum and WoodStructure carries unlisted connection state; neither should be flattened.
6. Preserve the existing native `damageDropped(IBlockState)` adapter's virtual call to `damageDropped(int)` so subclass drop semantics remain in charge. `ItemBlockBase.java:34-40` passes subtype metadata through, and the local Forge Block bytecode confirms native `getStateForPlacement(..., int meta, ...)` delegates to `getStateFromMeta(meta)`. Thus placement begins using the inherited codec without a new per-block placement override.

## Model routes and limits

`main/LegacyBaseModelHandler.java:62-76` selects eligible BlockBase cubes and uses `IBlockMulti.getSubCount()` for ItemBlockBase inventory variants. `:86-95` registers `inventory` / `inventory_N`; `:102-107` replaces already-present world model-registry entries by block registry name. `render/model/LegacyBaseBakedModel.java:89-113` reads `block.getMetaFromState(state)` and caches/bakes quads by metadata, so eligible cubes can now supply their actual native state metadata to icon selection. This patch does not change that handler or its resource-routing strategy.

By static inspection, **20 of the 29 newly affected instances** satisfy this handler's eligibility conditions, contributing **93 valid inventory subtype routes**: ore_meteor, stone_resource, ore_basalt, block_coke, block_cap, concrete_colored_ext, lightstone, vinyl_tile, sand_mix, laythe_coral_block, cm_block, cm_sheet, cm_engine, cm_circuit, cm_port, fusion_component, icf_component, icf_laser_component, watz_end, and rad_absorber.

The other **nine** are deliberately excluded from that generic model route: stone_biome (contextual icon), cm_tank (not full cube), crashed_balefire (invisible/TESR), plant_dead, rubber_plant, stalagmite, stalactite, plant_tall, and plant_tall_laythe (special legacy renderer/non-full-cube). See `LegacyBaseModelHandler.java:200-239`, `BlockBiomeStone.java:43`, and the respective native `isFullCube` / legacy `getRenderType` declarations. Existing explicit state owners remain excluded or handled by their existing dedicated routes. `main/PlantBlockModelHandler` handles only reeds, coral, and hanging vine; it is not a general remedy for the excluded tall/dead/rubber plants.

No new blockstate JSON or custom state mapper is provided here. The shared property changes the vanilla state-key space for inherited blocks to 16 variants; whether each requested world model key is present and replaced correctly must be established by a client/model-bake test, not by this source audit. Sampled inherited blockstate JSON files (including tile.cm_block, tile.vinyl_tile, tile.stone_resource, tile.plant_tall, tile.cm_tank, and tile.rad_absorber) are absent from source resources, as before this change. Do not claim the nine excluded routes have been migrated or that all models render correctly merely because metadata round-trips.

## Tile entity and lifecycle boundaries

There are exactly **three ITileEntityProvider descendants**: `blocks/machine/BlockCMPort.java:11-19`, `blocks/bomb/BlockCrashedBomb.java:31-43`, and `blocks/generic/BlockDecoContainer.java:27-47`. Only the first two inherit the new state codecs; DecoContainer remains under DecoModel's existing owner. Their tile factories currently ignore their metadata argument.

Read-only `javap` inspection of the existing local Forge jar (`.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`) confirms that native Block initializes `isTileProvider` using `this instanceof ITileEntityProvider`; `hasTileEntity(IBlockState)` returns that flag; and `createTileEntity(World,IBlockState)` calls `createNewTileEntity(world,getMetaFromState(state))`. Therefore no new tile-provider flag or shared factory bridge is required just to add native metadata. This is API/bytecode evidence, not a runtime guarantee of every TE lifecycle, persistence, renderer, or activation path.

Legacy callback migration is explicitly **not** included. Concrete remaining declarations include:

- `BlockCMPort.java:23,28`: old-coordinate onBlockAdded and breakBlock. `BlockCrashedBomb.java:52`: old-coordinate activation. DecoContainer's existing native activation/break methods are separate and unchanged.
- `BlockAbsorber.java:78` and `BlockNTMSand.java:22,27`: old-coordinate scheduling/neighbor callbacks. NTMSand's instant-fall branch (`:44-54`) also uses the original metadata-less setBlock; the shared codec does not repair that existing subtype-loss path.
- `BlockDeadPlant.java:30,39,57`, `BlockRubberPlant.java:27,36,54`, `BlockStalagmite.java:58,67,74`: old-coordinate placement checks, neighbor hooks, and collision callbacks.
- `BlockTallPlant.java:104,124,133,179,227,254,264,297,345` and `BlockTallPlantWater.java:69,89,94,103,150,186,192,202,212,231`: remaining old collision/placement/neighbor/harvest, legacy IGrowable/IPlantable, and drops callbacks. Their onBlockPlacedBy and updateTick entries already use native signatures, but contain remaining compatibility calls; preserving +8 state bits does not constitute complete plant behavior migration.
- `BlockCap.java:50`, `BlockOreBasalt.java:56,62,74,80`, `BlockResourceStone.java:23,33`, and `BlockStalagmite.java:41,79`: legacy drop or walking callbacks. `BlockCMGlass.java:26` still has the old-coordinate side-render callback. `BlockBiomeStone.java:43` retains the contextual legacy icon callback.
- Existing owner NTMFlower also retains legacy grow/plant/drop/collision methods (`BlockNTMFlower.java:82,120,166,189,202,275,289`); existing owner protection does not imply its whole lifecycle is natively migrated.

## Audit conclusion

The bounded `BlockMulti` ownership change is materially safer than changing BlockBase: it restores a raw metadata nibble to **29 registered blocks** without disturbing **seven already-owned native state instances**, while preserving genuine non-subtype bits and polymorphic drop behavior. The closure is fully enumerated above. Codec/constructor checks, runtime state IDs, client models, and world/TE lifecycle tests are distinct evidence categories; this source audit establishes the closure and hazards only and must not be reported as full gameplay migration.

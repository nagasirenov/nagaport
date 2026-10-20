# Native BlockMulti property: unlocked legacy-write audit

## Result

The property supplies a missing state-representation prerequisite for **30 direct-target remaining compiler sites**: **26 World.setBlock calls and four Chunk.func_150807_a calls**, spanning **10 registry fields / nine target classes**. It does **not itself rewrite any call or remove these diagnostics**.

There are separately **eight additional source-supported, non-direct candidates**: one simple alias, three `this` placements, and four context-owned tall-plant metadata mutations. Thus **38** is a useful bounded initial candidate count, not a claim that all 38 can be converted identically. Seven further mixed/dynamic write sinks demonstrably serve affected targets but also serve other blocks; those remain a separate audit tier, not added to the direct-target count.

## Evidence and counting method

Inputs: CP83 `porting/checkpoint83/full-compile.log` and actual current `src/main/java` under `/workspace/shared/nagaport/ntm-port-1.12.2`. The log SHA-256 is `aa4631a861e183656374852c954a82067b20014d75b36221da3ffd16e6bfbcce`.

Parsed method diagnostics for `setBlock`, `setBlockMetadataWithNotify`, and `func_150807_a`, then checked every reported source line byte-for-byte after whitespace trimming against current source. This verified **1,172 remaining write diagnostics**: 901 setBlock, 267 metadata-with-notify, four chunk writes. Direct classification uses the actual fourth target argument, not merely a ModBlocks reference anywhere on the line. The candidate rows below are distinct file/line calls, not loop iterations or estimated runtime placements. These are read-only audit results; no compile/build, production edit, or API rewrite was performed.

The eligible target set is the **29 registered instances newly inheriting BlockMulti state**, not every IBlockMulti block and not every BlockBase subclass. Existing owners (`plant_flower`, `spike_cacti`, `wood_structure`, `deco_computer`, `filing_cabinet`, `deco_crt`, and `deco_toaster`) are excluded as targets. A caller file such as BlockNTMFlower is still included when it writes a newly unlocked *different* target.

## Direct ModBlocks target sites

Paths are relative to `src/main/java/com/hbm/`.

| Target registry field | Remaining calls | Source sites |
|---|---:|---|
| crashed_balefire | 2 | `world/dungeon/Ruin001.java:317`; `world/feature/Dud.java:64` |
| ore_basalt | 5 | `blocks/fluid/VolcanicBlock.java:102,103,104,105,106` |
| plant_dead | 9 | `blocks/generic/BlockTallPlant.java:215`; `blocks/generic/BlockNTMFlower.java:209`; `world/feature/OilSpot.java:48,50,56,58,60,62,119` |
| plant_tall | 4 | `blocks/generic/BlockNTMFlower.java:215,216,226,227` |
| rad_absorber | 4 | `main/ModEventHandler.java:1617,1620,1623,1626` |
| rubber_plant | 2 | `dim/WorldGenRubberPlant.java:45,85` |
| sand_mix | 1 | `items/weapon/sedna/factory/XFactoryTool.java:171` |
| stalactite | 1 | `world/feature/OreCave.java:196` |
| stalagmite | 1 | `world/feature/OreCave.java:199` |
| stone_biome | 1 | `world/feature/BiomeCave.java:114` |

The three plant-related groups `plant_dead`, `plant_tall`, and `rubber_plant` account for 15 direct calls; the two tall/dead groups alone account for 13. The two stalagmite registry fields share one target class. `rad_absorber` accounts for all four chunk calls; every other table row is a six-argument World.setBlock.

## Non-direct candidates: keep separate

| Category | Count | Source and ownership evidence |
|---|---:|---|
| Simple source-owned alias | 1 | `world/dungeon/DesertAtom001.java:424`: target `Block4`; its sole source assignment at :25 is `ModBlocks.crashed_balefire`. No reassignment appears in the class. |
| This-target placements | 3 | `blocks/generic/BlockNTMSand.java:56`, `BlockTallPlant.java:198`, `BlockTallPlantWater.java:175`. The latter two place item metadata +8; NTMSand is the original metadata-less fallback placement. |
| Context-owned metadata updates | 4 | `blocks/generic/BlockTallPlant.java:274,275,280,281`, in its growth branch. Current and adjacent plant halves are intended targets, but the operation means mutate the existing block's metadata, not replace it unconditionally with `this`. Preserve target-block identity and the original pairing behavior. |

These eight plus the 30 direct calls yield 38 source-supported initial candidates. Seven additional **mixed/dynamic sinks** are relevant but not exclusively unlocked:

- `blocks/generic/BlockToolConversion.java:98` (one): `result.value.block` / `.meta`. Current built-in recipes at :154-157 target watz_end, fusion_component, and icf_component, all newly owned. The public mutable conversion map is open-ended; this is one sink, not four calls or a guarantee about every future target.
- `blocks/fluid/VolcanicBlock.java:54` (one): `getReaction` can return ore_basalt for diamond ore (:65), but also vanilla stone/fire and waste_log/waste_planks. A mention of ore_basalt in the metadata conditional is not a direct target.
- `world/feature/Meteorite.java:706` (one): generic helper target `b`; its ore-generation inputs include ore_meteor via :691-694, but the helper also places other meteor materials.
- `world/feature/OreCave.java:176,185,189` (three): `ore.block` / `ore.meta`. MainRegistry.java:565-597 includes ten stone_resource registrations and additional basalt, tumor, and laythe_silt registrations. The new property addresses resource-stone states, not the full target universe of those three sinks.
- `tileentity/machine/TileEntityWatz.java:540` (one): generic broken-column helper target `b`; :516-523 supplies watz_end, while earlier calls also use other reactor blocks. Eight source-level watz_end helper invocations are not eight compiler diagnostics; the failing sink is one.

A broader expansion therefore encounters **45 inspected diagnostic sites** (30 direct + 8 non-direct + 7 mixed), but the last seven must not be described as fully authorized/semantically closed by this property alone. This bounded survey is not whole-program points-to analysis; other data-driven paths can exist without naming their eventual target in the writer file.

## Important limits and hazards

- The 30 direct calls need the same original coordinate limits, evaluation/RNG order, flags, return behavior, and target semantics preserved as any other native-world-write migration. State support is necessary, not sufficient. Do not promise a 30- or 38-error reduction before call sites are actually migrated and freshly compiled.
- Four absorber calls operate on a Chunk in `main/ModEventHandler.java:1615-1626`. Their x/z loop variables are chunk-local. A native Chunk/setBlockState conversion needs an explicit local/world coordinate and lifecycle audit; replacing them with generic World writes and assumed flags is not equivalent.
- Tall plants require upper-half +8 metadata; enum-length normalization would corrupt them. `world/dungeon/Ruin001.java:317` deliberately supplies crashed_balefire metadata **5**, outside that block's four creative subtypes but inside the nibble. Preserve it rather than “correcting” it. The codec's raw-nibble design matters beyond current creative subcounts.
- NTMSand's `this` fallback already discarded subtype metadata in the original metadata-less setBlock call. Converting this API should preserve original default-meta behavior, not silently fix it without separate scope.
- Some sources containing affected-field references are false positives. BedrockOre's `depthRock` write, OilSpot's other terrain/falling-ground writes, Silo's block aliases, and SiloComponent's launchpad helper are not automatically affected merely because the same files also mention resource stone, plants, biome stone, or concrete. They were not included.
- Old structure helper callers mentioning vinyl_tile, concrete_colored_ext, or crashed_balefire are not additional matched write-method compiler sites in this log. Already-native state/primer calls likewise do not count as remaining compiler errors, even though the new codec may make their stored metadata meaningful.
- Already-specialized target writes, unrelated air/gas replacements inside affected classes, and non-write diagnostics (placement checks, material checks, rendering, etc.) are excluded. The audit does not establish native lifecycle/model correctness of the target families.

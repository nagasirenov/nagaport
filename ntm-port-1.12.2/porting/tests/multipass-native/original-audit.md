# Original multipass family audit and independent material oracle

## Reproduce

```sh
python3 porting/tests/multipass-native/original-material-oracle.py \
  --original-root "$ORIGINAL_1710_SOURCE" \
  --java-home "$JAVA8_HOME" \
  --classpath "$NATIVE_MINECRAFT_CLASSPATH" \
  --legacy-client "$ORIGINAL_1710_CLIENT_JAR" \
  --legacy-srg "$ORIGINAL_1710_SRG_ZIP" \
  --forge-patches "$ORIGINAL_1710_FORGE1614_PATCHES_ZIP" \
  --output /tmp/multipass-original-materials.tsv \
  --damage-evidence /tmp/multipass-original-damage-bytecode.txt \
  --compare-native
```

`original-manifest.json` pins pristine original sources, the official 1.7.10 client, official SRG archive, Forge1614 patch archive, all71 original owner construction/registration statements, and all100 required HBM PNGs. `original-owners.tsv` is the readable owner/item-registration table. The program verifies every pinned value before execution and, with `--compare-native`, verifies all100 port PNGs remain byte-identical. `--freeze` exists only for initial capture and refuses to overwrite a pinned manifest.

The original-source extractor takes complete registerBlockIcons, getIcon, colorMultiplier, shouldSideBeRendered, getPasses, and shouldRenderItemMulti methods from the twelve direct implementers plus BlockSellafieldSlaked. It does not read native source to determine expected materials. API names map to the real obfuscated old Block, TileEntity, IBlockAccess, IIcon and IIconRegister classes. Old inherited icon routing, registration, white color and neighbor-opacity culling execute real official class bytes. The narrow NTM-only tile collaborators hold paint, shape, directions and color; they are explicitly not gameplay/network implementations. The audited Body texture table and fixed fixture fluid/color data replace unavailable mod-wide bootstrapping.

The old JAR signs the default package. The probe makes a temporary class-byte-identical copy excluding META-INF signature metadata so generated default-package classes can reference real obfuscated APIs; it does not rewrite any class bytes. No fake Minecraft/Forge classes, broad old-Forge facade, production edits, download or full Gradle build are involved. The Forge archive is pinned and audited, not silently presented as an executed Forge installation.

## Results and scope

`original-proof.log` records the actual run. The original oracle tests all12 families, metadata0–15, all6 sides, every pass, missing/unpainted/painted tiles, opaque/exposed neighbors, four positions including negative/large coordinates and all4 Sellafield variants. A separate truth table covers every49 pneumatic insertion/ejection pair including UNKNOWN and identical directions. Rows record original material, RGB, pass, side and actual original neighbor-culling result. The native comparator checks MultipassMaterials and MultipassContext directly against those rows. The latest run passes213,438 original assertions across74,880 rows and374,694 native assertions, including3,840 camouflage dispatches. The source audit also compares all71 native loader owner class/family/material-alias tuples against the original registrations.

Camouflage is a dispatch check: the original chooses `paintBlock.getIcon(side, paintMeta)` with metadata13 and a side-specific icon. Native camouflage must select the captured IBlockState identity. Actual native paint model sprite resolution, owner context capture, render geometry, world-light solver, resource reload, item drawing and destruction are **not established by this selector test**. Missing-tile and legal state inputs are tested; malformed negative metadata/shape and wrong-type OC tiles are not given invented repaired semantics.

## Owner and resource registration

There are71 original owners:44 direct BlockOre,3 BlockOreOutgas,5 BlockOreFluid,4 BlockCluster,5 BlockSellafieldOre, and one each of the other10 direct families. All56 BlockOre-family owners inherit its two-pass inventory policy. `deco_titanium` is a BlockOre despite its name. `ore_uranium_scorched`, basalt-specific ores, Nether ores and other non-BlockOre classes are not accidentally included by prefix matching.

The owner discovery walks actual Java inheritance, then reads actual ModBlocks constructor assignments. It agrees with all71 native loader entries independently. `deco_titanium` and `red_cable_paintable` use the original default vanilla ItemBlock registration overload. Other entries use ItemBlockBase or the explicitly recorded ItemBlockLore class. The private `register(Block)` helper selects ItemBlockBase, not vanilla ItemBlock. OC construction and registration are conditional on OpenComputers being loaded. `ore_bedrock` has no creative tab but still has an ItemBlock registration.

The11 Body base textures, in enum order, are:

1. ORBIT: minecraft:stone
2. KERBIN: minecraft:stone
3. MUN: hbm:moon_rock
4. MINMUS: hbm:minmus_stone
5. DUNA: hbm:duna_rock
6. MOHO: hbm:moho_stone
7. DRES: hbm:dresbase
8. EVE: hbm:eve_stone_2
9. IKE: hbm:ike_stone
10. LAYTHE: minecraft:stone
11. TEKTO: hbm:basalt

Metadata11–15 wraps modulo11. The5 Sellafield owner overlays are ore_overlay_diamond, ore_overlay_emerald, ore_overlay_uranium_scorched, ore_overlay_schrabidium and ore_overlay_radgem. Base Sellafield materials are sellafield_slaked and suffixes_1,_2,_3, chosen by the original overflow-sensitive Java coordinate hash in world; inventory always uses variant0.

## Twelve family semantics

| Family | World passes/materials | Tint | Item behavior |
|---|---|---|---|
| BlockCablePaintable |2: red_cable_base or painted side/meta; red_cable_overlay |White |One base-only pass |
| BlockOpenComputersCablePaintable |3: oc_cable_base or painted side/meta; oc_cable_overlay; oc_cable_color |Only pass2 tile color; no tile white |One base-only pass |
| FluidDuctPaintable |2: fluid_duct_paintable or painted side/meta; unpainted fluid_duct_paintable_color or painted fluid_duct_paintable_overlay |Only unpainted tile pass1 fluid color |One base-only pass |
| FluidDuctPaintableBlockExhaust |2: unpainted exhaust base twice; painted side/meta then fluid_duct_paintable_overlay |White |One base-only pass |
| PneumoTubePaintableBlock |2: base or painted side/meta; overlay_in on ejection, overlay_out on insertion, ordinary overlay elsewhere; no tile base twice |White |One base-only pass |
| BlockCableGauge |2: deco_red_copper; cable_gauge only side=metadata, base elsewhere |White |One base-only pass |
| FluidDuctGauge |2: deco_steel; pipe_gauge side=metadata, fluid_duct_paintable_overlay elsewhere |White |One base-only pass |
| CraneRouter |7: crane_in all sides; crane_router_overlay on side=pass-1 only |Pass1–6 ff0000,ff8000,ffff00,00ff00,0080ff,8000ff |One base-only pass |
| BlockFissure |2: minecraft:bedrock; molten_overlay |White |Both passes |
| BlockOre and subclasses |2: Body base by metadata%11; registered ore texture |White |Both passes |
| BlockBedrockOreTE |2: minecraft:bedrock; ore_random_(shape%10+1); missing tile bedrock twice |Only tile overlay pass1 color |One bedrock-only pass |
| BlockSellafieldOre |2: hashed Sellafield slaked variant; owner ore overlay |Pass0 grayscale Color.HSBtoRGB(0,0,1-meta/15f); pass1 white |Both passes, no world tint, variant0 |

Pneumatic ejection wins when insertion and ejection are equal. Paint uses side/meta texture, not paint's world icon or biome colorMultiplier. Fluid paint disables fluid-color tint and selects the different structural overlay. Missing paint tile does not behave like a painted tile. The OC original color getter casts without instanceof, so a wrong-type non-null tile can throw; the oracle does not pretend this is a valid missing-tile case.

## Geometry, UV, culling and inventory audit

The world renderer forces bounds0,0,0–1,1,1 and invokes original renderStandardBlock once per pass. The six faces therefore use standard original cube coordinates/UV orientation, normal and winding. The renderer has no per-layer epsilon, translated overlay or custom UV rotation; coincident repeated opaque bases are real old behavior and must not be silently optimized away. Original default neighbor-opacity culling applies on all owners except CraneRouter. Router pass0 always returns true, even behind solid neighbors; its overlays render one specific side per pass, also independent of neighbor solidity. A native implementation that places router quads only in ordinary neighbor-culled face buckets will differ.

Inventory calls setBlockBoundsForItemRender, copies bounds into RenderBlocks, applies +90° Y rotation and translation(-0.5,-0.5,-0.5), then loops its opted-in passes. Each pass emits6 faces in Y-,Y+,Z-,Z+,X-,X+ order with an appropriate normal per face and a single draw at the end. It never invokes colorMultiplier and never sets GL color. A white native inventory selector is therefore the ordinary default, not a proof of inherited GL-current-color equivalence. Item passes do not reset the original mutable currentPass afterward; world does reset it to0. The replacement's immutable per-call context intentionally removes that cross-call state hazard.

## Lighting and destruction: acceptance gate remains open

`original-renderer-bytecode.txt` records independent javap evidence from the pinned old client. Original renderStandardBlock reads colorMultiplier, optionally applies anaglyph, and chooses full/partial AO only when global AO is enabled and block light emission is0; otherwise it chooses flat rendering. `ore_volcano` is explicitly light level1 and `ore_glowstone` is4/15 in ModBlocks. Pass or material alone does not decide original AO. Any native blanket AO policy needs owner/light-aware validation.

Original standard AO rendering clears its block-tint flag when overrideBlockTexture is present. The six original renderFace methods each replace their incoming material with that override, retaining original pass count and side selection. However, **this AO flag difference is not by itself evidence of an emitted damage-color regression**. The actual original RenderGlobal damage path disables vertex color before it invokes the renderer.

`original-damage-bytecode.txt` provides the exact independent evidence. In original RenderGlobal `bma.a(bmh,yz,float)`, bytecode offset128 calls startDrawingQuads, offset145 calls disableColor, and offset319 invokes RenderBlocks with the crack override. StartDrawing resets hasColor=false and colorDisabled=false. DisableColor sets colorDisabled=true. The RGBA setter immediately returns when colorDisabled, so ordinary tint setters cannot enable hasColor. AddVertex writes packed color only when hasColor; draw binds/enables GL_COLOR_ARRAY only when hasColor. RenderGlobal instead sets constant GL color(1,1,1,0.5) for the damage pass. Lightmap/brightness data remain independently enabled and must not be confused with vertex color.

The executable `original-damage-no-color.java.in` verifies this contract using unmodified official Tessellator class bytes, with no GL draw: all seven color setter entrypoints are ignored, the raw color slot remains untouched, positions/UV/brightness still emit, and an ordinary-color positive control writes color correctly. It passes72 assertions. The pinned Forge1614 RenderGlobal patch introduces the EntityLivingBase overload but preserves the noColor sequence. The Tessellator patch changes buffer allocation/growth and draw batching but preserves the relevant color-disabled and hasColor gates. The probe checks these bytecode and patch properties on every run.

Accordingly, native baked tint metadata or retained raw quad color is immaterial to the ordinary damage pass **if** the actual native RenderGlobal/dispatcher noColor pipeline likewise suppresses emitted color. The separately owned native dispatcher test must establish that end-to-end condition; a direct retextured-quad draw without noColor is not the faithful world-damage contract. Neither this audit nor the original AO flag alone justifies a special tint callback or claims a rendered tint mismatch. Multiplicity, pass-side selection, crack UVs and surviving brightness/light data are separate obligations.

This audit does not waive CP37. Full original-vs-native light-solver/brightness and GPU visual parity, inherited item GL color, client screenshots and complete owner/network gameplay remain explicit open validation gates. The narrower ordinary damage-color issue can be closed by matched original and native noColor pipeline evidence without claiming those broader gates closed. Selector parity and PNG identity must not be described as full visual parity.

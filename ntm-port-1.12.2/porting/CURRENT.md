# Current continuation: 2026-10-24

## Verified status

Latest integrated working snapshot: **CP168 (CLEAN BUILD)**. A fresh Windows offline clean compile exits 0; compileJava exits 0 with **0 compiler diagnostics (BUILD SUCCESSFUL)**. The normalized CP167 -> CP168 diagnostic comparison records **70 removed diagnostics and zero added** (multi-session total CP160 -> CP168: 1,886 removed, 100% compilation completion). All **4,076 production Java sources** match the generated compiler inputs byte-for-byte (`diff -r`: 0 mismatches), and `git diff --check` reports zero whitespace or line ending issues across all modified files.

CP168 completed the compiler port through targeted batches and fixes:
1. `wave14_final.py`: Resolved 62 issues across 59 files (celestial biomes `createBiomeDecorator`, dropped obsolete lake gen, entity `getDeathSound` SoundEvents, `fuseTime` and `lastYaw` reflection, `newRotationYaw` field, `wormSelector` predicate bounds, `super.onItemUseFinish` in food items, block drops/lifecycle adapters, and removed obsolete `@Override`s).
2. `wave15_gui_ioexception.py`: Added `throws java.io.IOException` to `mouseClicked`, `keyTyped`, `actionPerformed`, and `handleMouseInput` across 97 GUI classes. Wrapped `LoadingScreenRendererNT` loading handler in try-catch rethrowing RuntimeException.
3. Added `throws CommandException` across 6 command classes.
4. Removed redundant `try ... catch (IOException)` around `PacketBuffer.writeCompoundTag` across 4 packet classes.
5. Resolved JVM 64KB method bytecode limits (`code too large`) by cleanly splitting schematic generator methods in `DesertAtom002.java`, `DesertAtom003.java`, `Ruin002.java`, and `Silo.java`.

These results verify that all 4,076 Java source files of NTM compile cleanly under Forge 1.12.2 (`stable_39` mappings). Runtime, asset loading, and gameplay behavior remain unverified. Keep the user-supplied `appliedenergistics2-rv6-stable-7.jar` intact and untracked. All changes remain uncommitted locally on branch `main` at base commit `b9eb458d`. Do not push.

## Verification sequence

- Offline clean and compile commands:
  `./gradlew.bat --offline clean --console=plain`
  `./gradlew.bat --offline -I porting/compile-diagnostics-expanded.init.gradle compileJava --console=plain`
- Logs and exit codes saved in `porting/checkpoint168/`:
  - `clean.log`, `clean.exit` (code 0)
  - `compile.log`, `compile.exit` (code 0, BUILD SUCCESSFUL, 0 errors)
## Post-CP168 Runtime In-Game Testing & Fixes (2026-10-24)

Following the 0-diagnostic build, the reobfuscated JAR was tested in a live Minecraft 1.12.2 Forge client (ElyPrismLauncher). The following runtime issues were identified and fixed:
1. `BlockStalagmite.java`:
   - `getBoundingBox` previously returned `null`, causing `NullPointerException: Tesselating block model` in `Block.shouldSideBeRendered` whenever stalactites/stalagmites rendered in chunk view.
   - Fixed by returning `FULL_BLOCK_AABB` from `getBoundingBox` and `NULL_AABB` from `getCollisionBoundingBox`.
2. `ModSoundEvents.java`:
   - Legacy 1.7.10 sound strings (such as `"random.fizz"`, `"random.explode"`, `"fire.ignite"`) called via `ModSoundEvents.resolve` threw `IllegalArgumentException: Unregistered sound event`.
   - Added `resolveVanilla` mapping table converting legacy sound strings directly to vanilla `net.minecraft.init.SoundEvents`.
   - Replaced exception throw with safe dynamic `new SoundEvent(location)` fallback.
3. `ModEventHandler.java`:
   - Added null/empty checks on `ItemStack` instances before invoking `ItemStack.areItemStacksEqual` in `onLivingUpdate`.
4. HazardSystem.java & OreDictionary / Nullable ItemStack Adapters (OreDictionary.getOreIDs crash):
   - Mobs ticking in the world (e.g. minecraft:pig in HazardSystem.updateLivingInventory) triggered java.lang.IllegalArgumentException: Stack can not be invalid! at
et.minecraftforge.oredict.OreDictionary.getOreIDs(OreDictionary.java:504) via HazardSystem.isItemBlacklisted.
   - In 1.12.2, OreDictionary.getOreIDs strictly throws IllegalArgumentException if stack.isEmpty().
   - LivingEquipmentRead, PlayerInventoryRead, and NativeHeldItem only checked reference identity stack == ItemStack.EMPTY ? null : stack. Non-canonical empty stacks (count <= 0 or empty item) were returned as non-null, causing 1.7.10 null checks to pass on invalid/empty stacks.
   - Updated LivingEquipmentRead, PlayerInventoryRead, and NativeHeldItem to check (stack == null || stack.isEmpty()) ? null : stack.
   - Guarded HazardSystem.isItemBlacklisted, getHazardsFromStack, pplyHazards, updatePlayerInventory, and updateLivingInventory against null/empty stacks.
   - Audited and guarded all remaining OreDictionary.getOreIDs callers across the codebase (BlockOpenComputersCablePaintable, GUIScreenSatInterface, MachineRecipes, TileEntityMachineOilWell, TileEntityMachinePumpjack, InventoryUtil, ItemStackUtil, RecipesCommon.ComparableStack).
   - Cleaned up empty stack sentinels across item handlers (ItemInventory, ItemInventoryTransfer, ItemBlockStorageCrate, ItemPancake, ItemAmmoContainer, ItemConveyorWand, ItemFusionCore, IMagazine, ModEventHandler, TileEntityDiFurnace).

## Post-CP168 NBT Numeric-ID Fix & 96-Site Audit (2026-10-25)

Crash: `ClassCastException: ItemAir cannot be cast to ItemCustomMissilePart` in `ItemCustomMissile.getStruct:96` (creative inventory). Root cause: `hbm:item.missile_custom` persisted numeric item IDs in NBT (unstable across sessions), and a missing `stability` key read as 0 → `Items.AIR` → direct cast. Full report: `porting/nbt-id-audit/REPORT.md`.

| File | Change |
|---|---|
| ItemCustomMissile | registry-name persistence (`key + "_name"`) via `writePartToNBT`; public `readPart(ItemStack,String)`/`readPart(int)` with instanceof validation (no casts); `getStruct` returns null when warhead/fuselage/thruster invalid; `addInformation` null-safe |
| TileEntityCompactLauncher, TileEntityLaunchTable | `launchTo`: direct casts removed, single `struct = getStruct(slots[0])` with null-guard, chip via `readPart`, fins from struct |
| MissileStruct | `readFromByteBuffer`: instanceof validation via `readPart(int)` (packet desync protection) |
| EntityMissileCustom | 5 DataWatcher read sites guarded (`onUpdate`/`spawnContrail`/`onMissileImpact`/`getUnlocalizedName`/`getBlipLevel`) |
| EntityGrenadeBouncyGeneric, EntityGrenadeImpactGeneric | `getGrenade()` instanceof filter + existing fallback |
| BlockBedrockOreTE | TE NBT writes `0name` (registry name), reads name-first with legacy `0id` fallback; air also falls back to powder_iron |
| EntityRequestDrone | program `comp` entries read `name`/`id` from the per-program compound (fixes pre-existing root-NBT bug that loaded all comp programs as air), invalid entries skipped, writes `name` |

Audit of all 96 `Item.getItemById(`/`Block.getBlockById(` sites: 26 fixed, 37 session-safe (packets/DataWatcher sync, stable within a session), 24 persistent-NBT risks deferred (BlockRebar, BlockICF/BlockPWR, 5 paintable TE `paintblock` keys, EntityFallingBlockNT, SILEX, wands, NBTStructure — see report), 9 harmless (incl. dead-code StorageManifest:48 — sole usage commented out in TileEntityCraneBoxer).

Verification: compileJava 0 errors (no new diagnostics), reobfJar OK, CRLF clean on all 9 touched .java; reobf javap spot-check shows dev names for port methods and SRG names for vanilla calls (`func_111206_d` = getByNameOrId). JAR SHA-256 `e83ef7fe583f879062950e3abc6e5447dce6e7257481e26e971267a26be70fe0` installed into the test instance with the game closed.

## Post-CP168 Legacy IIcon Contract Audit & Systematic Fix (2026-10-25)

Crash: `AbstractMethodError: LegacyTextureAtlasSprite.getIconName()` in `NativeBlockMaterial.texture:28` via `RenderRubble.doRender` (entity_rubble). Same reobf landmine closed earlier for `WrappedIcon`: a class extending vanilla `TextureAtlasSprite` while implementing the port `IIcon` interface loses dev-named contract methods to SRG renaming in the reobf jar. The earlier fix had only converted the static `wrapSprite` path; the instance path `LegacyIconRegister.wrap(...)` — which serves every `registerIcon` callback and `registerGeneratedSprite` (including TextureAtlasSpriteMutatable and MaterialTextureAtlasSprite) — still handed out the broken class. Full report: `porting/icon-contract-audit/REPORT.md`.

Audit results: the port `IIcon` is the complete 1.7.10 nine-method contract (321 call sites: getMinU ×64, getMaxU ×62, getMinV ×63, getMaxV ×61, getInterpolatedU/V ×14 each, getIconName ×23, getIconWidth/Height ×10 each). Of the 5 implementors only `LegacyTextureAtlasSprite` had a vanilla superclass; of the 5 `TextureAtlasSprite` subclasses none of the others implement `IIcon`. The three IIcon→sprite resolvers (NativeStateFallbackHandler:412, NativeItemParticles:180, LegacyBaseBakedModel:139) check `instanceof WrappedIcon` first, so inheritance keeps them correct.

| File | Change |
|---|---|
| WrappedIcon | non-final now; holds all 9 dev-named contract methods; documented as the mandatory vanilla-superclass-free base |
| LegacyTextureAtlasSprite | `extends TextureAtlasSprite implements IIcon` (9 overriding methods) → `extends WrappedIcon`; identity, `getSprite()` and register dedup unchanged |
| LegacyIconRegister | javadoc updated; `wrap()` now produces reobf-safe wrappers on all paths |

Verification: compileJava/reobfJar 0 errors, CRLF clean; reobf javap confirms `WrappedIcon` declares the full contract with dev names, `LegacyTextureAtlasSprite` has no vanilla superclass, and `NativeBlockMaterial` resolves `InterfaceMethod IIcon.getIconName`. JAR SHA-256 `6650d3c87778388c8a6de79ad543b22b07f78ba2b514ac80df39fb5a14f73e75` installed with the game closed.

## Post-CP168 Explosion Zone-Entity Cast Guards (2026-10-25)

Server crash: `ClassCastException: EntityBlackHole cannot be cast to EntityLivingBase` in `EntityProcessorStandard.process:73` — an exploding `ItemDrop` (ItemDrop.onEntityItemUpdate → ExplosionVNT.explode) pulled a black-hole effect entity into the zone query. `getEntitiesWithinAABBExcludingEntity` returns arbitrary entities; vanilla 1.12 guards the knockback enchantment reduction with `instanceof EntityLivingBase` (fallback = raw knockback), but the port's rename of `func_92092_a` reintroduced bare casts. Audit of all `getBlastDamageReduction` call sites:

| Site | Status |
|---|---|
| `EntityProcessorStandard.java:73` (crash) | fixed: vanilla ternary guard, fallback raw knockback |
| `EntityProcessorCross.java:115` (same pattern) | fixed: same guard |
| `TestEventTester.java:498` (dev block, same pattern) | fixed: same guard |
| `ExplosionNT.java:181` | already guarded (reference pattern) |
| `EntityProcessorCrossSmooth.attackEntity`, `CustomDamageHandlerAmat.handleAttack`, `ExplosionThermo.freezer/setEntitiesOnFire` | already guarded |

Non-living entities still take `attackEntityFrom` and raw knockback as in vanilla; only the blast-protection enchantment reduction is living-only. compileJava/reobfJar 0 errors, CRLF clean. JAR SHA-256 `bd77b635de0f9649767e052c4471645286c8e71b2ce58d0573a27e86d2aa5643` installed with the game closed.

## Model-Routing Systemic Fixes (2026-10-25, user-verified analysis, JAR NOT installed)

Baseline after bd77b635: bake validation `unresolved=1005` (DefaultVariantQuiet=803, ItemRouteFallback=202→65 after repair). Root causes (user-confirmed, incl. decompiled Forge 2768):

1. **Inventory registration was coupled to the world-state reroute** in `NativeStateFallbackHandler`: `BlockContainer.getRenderType(state)` is `INVISIBLE` in this MC, so `InvisibleWorldModelHandler` (also LOWEST, earlier) had already rerouted all container-block states to empty world models → `touched=false` → `continue` skipped the inventory registration below. Crates, machine batteries ×5, cable/fluid switches+valves, drone docks, shredder, teleporter, autocrafter, turbine, mass_storage, safe, custom_machine, wand_structure etc. got no inventory model at all. **Fix:** inventory branch now runs whenever an adapter exists and the item is route-free, independent of `touched`; keeps per-face legacy icons.
2. **`TextureAtlasSpriteMutatable.load()` returned `true`** — in Forge 2768 this exits BEFORE the sprite is added to the stitcher, so "loaded" generated sprites (ItemAutogen colors, dynamic slag ×117) never got atlas coordinates. **Fix:** returns `false` (frames go to the ordinary stitcher) and strips the legacy `tile.`/`item.` prefix from the source name, so slag's `hbm:tile.slag-*` sprites read the real `textures/blocks/slag.png`.
3. **Particle verification used `getTextureExtry`** (registered map) — a registered-but-never-stitched sprite passed as "resolved". **Fix:** `NativeItemParticles` now requires the post-stitch `getAtlasSprite(name)` to return that exact sprite (`stitchedSprite`), applied to bind/declared/icon paths; new public `spriteOf(IIcon)` helper.
4. **Declared textures were stitched only for "eligible" items** — items with inherited icon/color callbacks (waste_* ×16, soyuz, fluid_icon, broken_item, gun_pa_ranged...) had PNGs but no stitched sprite, so the repair's declared-texture source failed. **Fix:** `NativeItemTextureModelHandler.stitch` now stitches every declared texture whose concrete PNG exists (base-prefix declarations like upgrade_ejector/upgrade_stack, whose art is `_1.._3`, are skipped with a debug note); `eligible()` still gates model-route ownership only.
5. **Repair was single-sprite and metadata-blind.** `RouteFallbackModelRepair` now resolves per item: native sources → declared texture → legacy callbacks (`getIconFromDamage(int)` per-meta, `getIcon(ItemStack,pass)` two-layer, e.g. minecart base+overlay) → registry-name icon; bakes multi-layer generated models with a `MetaSelectingModel` (metadata-dispatching overrides via `handleItemState`). Priority LOW, ahead of the LOWEST validation, so repaired keys count as replaced.
6. **Lost base-texture declaration for the minecart**: `ItemModMinecart.registerIcons` derives `cart.<base>`/`cart_overlay.<type>` names from `NativeItemTextureRegistry.texture(this)`, which was null. Declared `hbm:cart` in ModItems (cart.vanilla/cart_overlay.* PNGs exist).
7. **Validation reporting split** in `BakeOwnedModelLoader`: DefaultVariantQuiet unresolved keys aggregate into a `quiet-by-design=` count (owning a mesh route doesn't prove the default key unreachable, so the number stays visible, but no per-item ERROR spam — those 803 were mostly items whose models are installed later at load-complete via TEISR or that live on other route keys); ItemRouteFallback keys keep per-item ERRORs as the real defect signal.

Verification: compileJava BUILD SUCCESSFUL (0 errors), CRLF clean on all 7 touched files. Runtime/visual verification deliberately not performed (user runs it): expected movement — the ~39 block-item keys gain per-face icon models, waste/upgrade/mold/soyuz/mineral/cart families repair from real stitched art, slag generated sprites actually stitch, `unresolved owners={ItemRouteFallback=...}` becomes the honest small remainder (invisible helpers like beams/geysir/`_undef` and genuinely absent art stay unresolved by design). `modelm65mono` not investigated this pass.

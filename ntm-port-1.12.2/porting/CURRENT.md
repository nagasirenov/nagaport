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

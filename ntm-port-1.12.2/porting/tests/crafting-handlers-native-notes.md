# Native crafting follow-up after revision 2

Baseline: isolated r2 commit d8f01842eecc744338eab5dd53c431fdbc013a76,
whose production patch was integrated by the coordinator as CP58. This is an
incremental patch only; no main worktree changes or Gradle invocation.

## Production scope

Seven producer statements: MKU, five storage upgrades, and empty Zirnox rods.
The first six remain in their exact CraftingManager positions, executed by the
already-integrated recipe event callback after native Item registration. The
RodRecipes.register call remains in its original CraftingManager position.
No initialization moves are added. Stable explicit custom recipe names use
`hbm:crafting/special/mku` and `hbm:crafting/container_upgrade/<name>`;
existing names are rejected before registration, never overridden. Rod uses
r2's canonical definition identity. No recipe is dropped or auto-unlocked.

ContainerUpgradeCraftingHandler remains native ShapedOreRecipe, adding its
required nullable group constructor argument and adapting inventory EMPTY.
Its original result tag assignment is intentionally an ALIAS, not a copy:
`result.setTagCompound(source.getTagCompound())`. Original safe/crate slot-loss
and storage-format comments remain unchanged. MKU's exact seeded shuffle,
six ingredients, three nullable internal array slots, cache, count/NBT-ignoring
and metadata-sensitive matching, result and book code are source-conserved.
Only the native registry base, inventory EMPTY checks, canFit and dynamic recipe
classification are added. Internal recipe-array nulls are not replaced.
ShapedOreRecipeExt's sole constructor gets its native group argument; it has no
current production callers and receives compile coverage, not gameplay claims.

## Evidence

`check-crafting-handlers-source.py` reverses only the listed transformations and
compares complete non-import owner bodies to frozen baseline hashes, covering
RodRecipes, CraftingManager, ContainerUpgradeCraftingHandler, MKUCraftingHandler
and ShapedOreRecipeExt. The earlier 269+auto source proof still passes, with six
explicit custom registration reversals now additionally recognized.

`probe-crafting-handlers.py` extracts all seven actual producer statements,
including exact shapes, quantities, metadata and ingredients, directly from
production source. It compiles complete real handlers and registry bridge against
pinned Forge2768. The native GameData Block/Item and recipe event dispatchers are
used, with real native ore dictionary/ingredients/ItemStacks/World/registries.
No Forge/Minecraft doubles. NTM block subclasses and item catalog are bounded
identity collaborators only: storage world logic, actual item behaviors and book
rendering are NOT executed. The ItemCircuit enum is extracted exactly; ore
material names, prefixes, group-name behavior and fromOne ordinal behavior are
checked against source. Creative-tab assignment on identity fixtures permits
native wildcard Ingredient enumeration; it is not a production change.

70 assertions pass: each storage upgrade's actual shape, output item/count/tier,
NBT reference identity and later mutation, fresh result stack, absent tag,
missing source rejection; both mass-storage exact input tiers; rod exact seven
slots/four outputs, live late ore alternative and missing ingot; MKU seeded
array/cache across 0, 1, -1, Long.MAX_VALUE and back to 0, exact metadata and
empty slots, ignored count/NBT, null world and fresh output; duplicate custom
identity guard retains registry object. Seed generation is the actual complete
owner with source conservation, not an independently copied shuffle oracle.

Run:

    python3 porting/tests/check-crafting-registration-source.py
    python3 porting/tests/probe-crafting-handlers.py --java-home "$JAVA8" --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"

## Audited next boundaries, NOT completed by this patch

RodRecipes.registerInit is called in init; CustomMachineConfigJSON.initialize
and CompatHandler.init are in postInit. They already funnel ordinary recipes
through r2's native auto helper. Pinned Forge2768 Loader.java lines 744-753
dispatch init and postInit before GameData.freezeData: initial late registrations
are not inherently outside the writable native registry window. Runtime config
reload/crumple registration after freeze is a separate unresolved lifecycle gate.

The only remaining active direct GameRegistry.addRecipe calls under com/hbm are
RBMKFuelCraftingHandler, CargoShellCraftingHandler and ScrapsCraftingHandler.
Cargo references removed doesContainerItemLeaveCraftingGrid and requires an
actual item/container-contract audit, not deletion of that condition. Legacy
RecipeSorter calls/global precedence remain unresolved. No invented overlap or
priority wrapper is introduced. The earlier original-vs-native SlotCrafting
remaining-item placement limitation remains; inherited native remaining-item
payload is not a claim that old placement behavior is complete. None of this
is a full mod launch, full compile or full-game acceptance test.

# Native crafting registration candidate: acceptance gates remain open

Revision 2 supersedes the initial CP55 candidate (which copied vanilla outputs and used global ordinals).

Baseline: CP55 `2fa015374be0e330d2541e5bfe9d4b96e380aa05`. Isolated worktree only.
This is NOT a completed or gameplay-accepted crafting port. Do not integrate the
common registration path as accepted gameplay while the precedence gate below
remains unresolved. It is an executable production candidate plus exact evidence,
not a fake Forge API, removed family, excluded source set, or game-launch proof.

## Production scope

- Genuine `IRecipe` / `IForgeRegistryEntry.Impl` NTM domain recipes; native shaped
  dimensions (`IShapedRecipe`) exposed to consumers. No Minecraft/Forge doubles.
- 269 direct Mineral registration sites migrated:184 vanilla-shaped,2 ore-shaped,
  23 vanilla-shapeless,60 ore-shapeless. Helpers/loops expand to more runtime
  recipes. The inverse-transform check proves every non-import character of the
  original CP55 Mineral source is retained after reversing just registrations.
  Counts, metadata, output and ingredient expressions, misspelled ore keys,
  conditionals and declaration order are unchanged.
- Four shared `CraftingManager.addRecipeAuto/addShapelessAuto` terminal branches
  migrated without changing their original ore-detection logic. These cover main
  crafting and callers in Mineral/Rod/Tool/Armor/Weapon/Consumable/Powder, plus
  late CustomMachineConfigJSON and CompatHandler consumers.
- Identity (revision2): `hbm:crafting/<site>/<canonical SHA-256>/<duplicate occurrence>`.
  Definition encoding includes shaped/ore flags, original ordered pattern/keys,
  ore names, item/block registry names, output/input metadata and full integer
  counts, and native serialized stack NBT/capabilities. Compound keys are sorted
  recursively; lists and array values remain ordered. Types and lengths are
  encoded explicitly. No runtime stack trace or global auto ordinal is used.
  Different earlier conditional definitions do not shift identities. Identical
  duplicates remain separate with a duplicate-only suffix. Digest collisions
  compare full canonical bytes and fail rather than silently coalescing recipes.
  IDs snapshot the definition at registration; later aliased-output mutations do
  not rename the recipe. No unlock calls/new player progression behavior.
- Actual native `ForgeRegistries.RECIPES.register`, not an emulated GameRegistry.
  Existing native lock/validation rules remain in force.

## Exact lifecycle movement and dependency boundary

Only two existing MainRegistry calls move their EXECUTION:

1. `CraftingManager.mainRegistry()` becomes a scheduled method reference at its
   same source position. It now executes at Register<IRecipe>, constructing
   outputs and inputs after all actual Item registry events.
2. `OreDictManager.registerOres()` likewise becomes the second scheduled method
   reference and executes after crafting. Its material-fragment recipe generation
   through MineralRecipes helpers therefore retains the original internal order.

`OreDictManager` construction, OreRegisterEvent listener subscription, and
`registerGroups()` remain preInit in their original positions. This is necessary
so other mods' intervening ore events still receive NTM aliases/group handling.
Group initialization itself only installs prefix rules; it does not populate the
ore tables. Crafting entries retain live native ore lists, so NTM ore additions
following recipe construction remain visible. No queue stores prematurely built
ItemStacks. SmeltingRecipes is nested inside mainRegistry and moves with it;
this avoids its numerous pre-registration `Item.getItemFromBlock` calls too.

Pinned Forge2768 source proves: Loader distributes PREINITIALIZATION, fires
non-recipe registry events (GameData orders Block then Item), then initializeMods
calls CraftingHelper.loadRecipes(false), whose recipe-only event precedes the
INITIALIZATION dispatch. Native probe executes those exact GameData filtered
registry dispatchers under the real EventSubscriptionTransformer. It does not
pretend to launch Loader or all NTM producers. The probe compiles the COMPLETE
production item/block bridge and the three new domain classes unchanged.

Remaining preInit statements stay put: SiegeTier.registerTiers,
HazardRegistry.registerItems/registerTrafos, XWeaponModManager.init,
SolarSystem.init, tool repair/material assignments, GUI handler, TileMappings,
MachineDynConfig.initialize, launchable tables, tile registrations, loot and
entity mappings, chunkloading callback, dispensers and microblock IMC.
The directly inspected implementations of SiegeTier/XWeaponModManager/
SolarSystem/DispenserBehaviorHandler/MicroBlocksCompatHandler do not read
CraftingManager or OreDictionary recipe results. Launchable tables key ModItems;
MachineDynConfig directly reads/writes machine config, not crafting tables.
Its reflective machine constructors/config callbacks are not a completed
transitive-initialization audit. External mods' preInit consumption of NTM ore
entries is not proven preserved after native lifecycle relocation.

Independent pre-existing early ItemBlock consumers remain, NOT fixed here:
HazardRegistry bobblehead ItemStacks; MainRegistry's schrabidium-block hammer
repair stack; MicroBlocksCompatHandler's block IMC ItemStacks. This is not a
whole-preInit readiness claim. Do not expand this patch into unrelated init moves
without auditing those owners.

Later recipes: RodRecipes.registerInit in init, CustomMachineConfigJSON.initialize
and CompatHandler.init in postInit, and CraftingManager.crumple at player ticks100
and200. The normal helper uses the real registry at those phases; native freeze
is not bypassed. Runtime crumple still references removed old recipe-list APIs
and is NOT ported. Dynamic config reload/registry replacement needs separate
acceptance. The native recipe event itself is treated as one startup event;
repeat firing deliberately fails rather than silently duplicating recipes.

## Original matching source and native evidence

Behavior reference: supplied original NTM producer tables and Forge1614 userdev
`src/main/java/net/minecraftforge/oredict/{ShapedOreRecipe,ShapelessOreRecipe,
OreDictionary,RecipeSorter}.java`. New domain implementation is independently
written; no Forge source file is embedded or rebranded as a platform class.
Original shapeless ore matching greedily removes the first matching ingredient;
native2768 backtracks/uses RecipeItemHelper, which broadens matches for overlapping
alternatives. Original bare Block shapeless input constructs metadata0, whereas
native CraftingHelper converts Block to wildcard. Both differences have explicit
positive and negative production assertions. Original shaped Block inputs are
wildcard, retained here. Item/damage matching ignores ingredient count and NBT;
output aliasing is family-specific: original vanilla afh/afi retain the caller's
output reference; original Forge1614 ore constructors copy it. Production now
retains the reference for non-ore and deep-copies for ore. All families expose
the stored reference through getRecipeOutput and return a fresh current-output
copy at craft time. Ore lists stay live.

Native probe passes56 assertions in each of two fresh JVMs covering real item/recipe event ordering,
preInit empty-output irreversibility, lazy construction, IDs, normal/ore,
metadata/wildcard/NBT, mirrors, shapeless order, remaining container identity,
with no global precedence claim. The final result intentionally states full
producer registration remains blocked. Actual MineralRecipes/MainRegistry are
source-audited, not runtime-launched by this probe. No full Gradle/game run.

## Precedence gate: global equivalence remains unproven

Forge1614 RecipeSorter orders shaped before shapeless, descending input size,
then class priority. Forge2768 uses registry order. The new registration path
retains declaration order, not that old global sorting. No production override
is installed, and no unknown-mod recipes are rewritten. Existing custom
GameRegistry.addRecipe and RecipeSorter compilation errors remain visible.
This is an unresolved acceptance gate, not permission to change gameplay.

Correction to an intermediate investigation: the original vinyl-paper recipe
contains ONE row "LL", i.e. two inputs, not a2x2 pattern. A temporary2x2 fixture
showed only a synthetic conflict and did NOT establish an original conflict.
It and an unintegrated wrapper candidate were removed. There is no claim that
the original vinyl-paper recipe conflicts with the vanilla workbench. Remaining
original recipe overlaps require an exact original producer/ingredient audit.
The final probe generates the vinyl-paper test factory directly from its
byte-conserved real producer expression, substituting ONLY the isolated NTM
block identity. It proves the real one-row pattern matches two planks, rejects
four, and does not overlap the genuine loaded native crafting_table entry.
All other tests are explicitly generic native/production API boundary fixtures,
not evidence that an arbitrary matching example occurs in original recipes.
The source checker also byte-conserves the entire CraftingManager body after
reversing only its four terminal helper changes, preventing pattern inventions.

Native API design facts, not implemented: ForgeRegistry same-name replacement
retains numeric ID, requires active owner and rejects locked writes. Namespace or
class alone does not prove vanilla ownership. Its public makeSnapshot().overrides
reveals prior override owners but full provenance needs additional checks. Do not
use those facts as authorization for global rewriting or manufactured conflicts.

## Remaining-item gate: distinguish payload from destination

Forge1614 `forgepatches.zip/net/minecraft/inventory/SlotCrafting.java.patch`
changes container handling to hasContainerItem(stack)/getContainerItem(stack)
and destroys over-damaged results. It then uses doesContainerItemLeaveCraftingGrid
(default true) to prefer player inventory. Forge2768 ForgeHooks preserves the
container payload and over-damage check, but native SlotCrafting placement is not
that old preference. Thus milk -> bucket payload is proven, inventory-vs-grid
placement parity is NOT. NTM ItemCraftingDegradation and ItemFluidIDMulti explicitly
returned false (stay in grid), which is consistent with native placement, but their
full current item class migration and mutation behavior are not validated here.
Do not label getRemainingItems alone a complete original container oracle.

## Reproduce

Run `python3 porting/tests/check-crafting-registration-source.py` from the project.
Run `python3 porting/tests/probe-crafting-registration-boundary.py --java-home <JDK8>
--classpath <actual native classpath> --forge-sources <Forge2768 sources.zip>`.
The probe downloads nothing, uses a temporary output directory, and logs source
hashes. See continuation evidence log. Coordinator owns full compilation.

## Revision2 output-identity oracle and stable-ID regressions

`OriginalCraftingOutputAliasTest` uses reflection solely in the test harness to
execute the genuine signed, obfuscated1.7 client classes `afh` and `afi`. It does
not repackage the jar, replace platform classes, or copy their constructors. It
checks output object identity, later count/NBT mutations, and isolated craft-result
copies. The runner hashes the old client and authentic Forge1614 ore source files;
both ore constructors explicitly use `output = result.func_77946_l()`.
Production native tests check all four families' reference/copy distinction and
mutations to both caller output and returned craft results.

The runner invokes the native test twice in fresh JVMs, once inserting an earlier
distinct conditional recipe in the SAME site bucket. It compares actual registry
ResourceLocations and requires exact equality. Additional tests preserve identical
duplicates, canonicalize recursively reordered compound keys (including Java
HashMap hash-collision keys), preserve array/list payloads, and distinguish output
counts, metadata, ore names, recipe shape and NBT content.

Reviewer-observed limitation retained: the domain shaped matcher uses actual grid
dimensions, whereas old shaped matching scans a fixed3x3 grid. Empty-edge patterns
on a2x2 grid can differ; no current migrated producer was found requiring that
edge case. This remains documented rather than silently generalized.

Revised runner additionally requires `--legacy-client <signed1.7 client.jar>` and
`--legacy-forge-userdev <Forge1614 userdev.jar>`. All earlier registration,
container-placement, custom-handler and lifecycle acceptance limits still apply.

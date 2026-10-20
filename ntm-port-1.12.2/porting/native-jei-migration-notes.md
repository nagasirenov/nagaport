# Native JEI root implementation status

2026-10-19. Production source implemented against actual cached JEI 4.12.0.216 and official NEI 2.4.3.245. Parent owns common builds, checkpoint, source-history guards, and final staging.

## Implemented

- Removed all legacy TemplateRecipeHandler/PositionedStack/RecipeTransferRect/arecipes usage from the complete 77-file recipe handler directory and registry/config integration. No compatibility shim or source exclusion.
- Native IRecipeCategory/MachineRecipeView IRecipeWrapper with input/output alternative lists and direct JEI slot initialization. Decorations remain visible/clickable but do not enter recipe indexing. Domain views retain original source recipe/context identity.
- Native IRecipeRegistryPlugin queries current recipe maps/ordered lists and preserves per-category lookup semantics rather than flattening them into default indexing. Distinct heat thresholds, NBT checks, fluid matching, secret filtering, input/output asymmetries and occurrence duplicates retained.
- All 72 original built-in category registrations remain in original registry order, with the original RTG condition. 72 unique UIDs; Shredder uses its actual original lookup/navigation key shredding instead of its erroneous old catalyst ID ntmRadiolysis. All custom configurations get distinct ntm_ + unlocalizedName categories.
- Native catalysts replace inert GTNH-only handler/catalyst IMC. Obsolete proxy/MainRegistry initialization hook removed in exact narrow sections.
- Machine GUI click rectangles translated from legacy NEI coordinates by +5,+11; recipe-local coordinates unchanged. Source parity test covers every original declared area, including cross-product multiple GUI/multiple rectangle cases.
- Anvil native transfer handler only selects the exact AnvilConstructionRecipe through GUIAnvil.focusRecipe. Layout-to-source identity preserves duplicate-output distinctions; doTransfer=false never selects; no packet crafting, item movement or recipe-content changes. Original GUI is tracked via GuiOpenEvent and verified by container identity.
- NEI item hiding remains via IConfigureNEI. Same visibility policy is applied to JEI's ingredient blacklist. NEI optional GUI modid guards use actual official modid nei.
- Custom machine presentation height is 84 pixels so the original pollution label at y75 remains visible instead of clipping at standard 65px.

## Explicit open visual/interaction validation gates

- Official NEI 1.12 removed IHighlightHandler and the world-highlight HUD. The old plushie callback duplicated tile-derived variant identity; the block already has a native-signature getPickBlock but its own old tile lookup still needs the separate native-world port. Keeping pick-block variant identity does not restore the removed HUD. No new HUD dependency added. User approved omission of this specific world-hover variant icon HUD on2026-10-19 12:08:21UTC. Plushie blocks, variants and native pick-block remain required; no broader feature omission authorized.
- Native JEI controls pagination from category dimensions, not old recipiesPerPage overrides. All original recipe content/layout is ported, but exact legacy page counts require in-game review.
- JEI item alternatives cycle at its native ~1s period. Some old handlers used 48 ticks (~2.4s), or Shredder decorations 24 ticks. Animated progress bars preserve original nominal duration/direction/meaning via JEI animated drawables, but wall-clock timing and pixel rounding differ from legacy tick-driven animation. No broad legacy timer emulation was introduced; do not claim pixel/timing identity.
- Recipe-arrow left/right clicks are restored. Hovered-arrow R/U keyboard behavior has no direct IRecipeWrapper callback in this JEI API; keyboard interaction remains an explicit validation/integration gate, not silently claimed complete.
- Recipe-arrow tooltips are restored through native localized bounds; see final proof below.
- NEI ghost-slot drag/drop remains with corrected optional modid. JEI inventory-panel ghost dragging still requires its separate native IGhostIngredientHandler bridge if that surface is part of full UI acceptance; do not claim that route is already implemented.
- Full GUI rendering, custom configuration lifecycle, dedicated-server optional-classloading, anvil screen return and in-game recipe interaction remain blocked behind broader mod compile/launch work.

## Production files outside recipe directories

- main/NEIRegistry.java: native type with all original registrations.
- main/NEIConfig.java: retained shared hidden-item policy; removed obsolete recipe/HUD registration calls.
- handler/imc/IMCHandlerNHNEI.java: real native catalyst registration, no empty legacy IMCSender placeholder.
- main/MainRegistry.java: removed only obsolete GTNH Loader/version block.
- main/ClientProxy.java: removed only handleNHNEICompat override; renderer registration untouched by this worker.
- main/ServerProxy.java: removed only empty legacy handleNHNEICompat declaration.
- inventory/gui/GuiInfoContainer.java: six Optional modid strings updated to nei; changed guard lines normalized for whitespace checker.
- build.gradle explicit JEI pin is parent-owned, not changed by this worker.

## Final production freeze and rerun (12:05 UTC)

- /tmp/nagaport-jei-production-freeze.sha256 records 90 current production inputs, including all recipe files and shared integration files. sha256sum -c passed after all final tests. Shared ClientProxy/MainRegistry hashes include other parent-approved work; parent must verify its final aggregate manifest.
- Core final: 58 assertions; nine selected production handlers: 272 assertions. Exact selected class list: NEIUniversalHandler, FuelPoolHandler, AnnihilatorHandler, NEIGenericRecipeHandler, AssemblyMachineRecipeHandler, PrecAssRecipeHandler, ChemicalPlantRecipeHandler, PUREXRecipeHandler, FusionRecipeHandler.
- CustomMachineHandler complete production class: 78 executable assertions, plus 13 separate source-only annotation contracts. Config routing/identity/fluid lookup/display caps/chance NBT/84px height pass.
- AnvilRecipeHandler + AnvilOverlayHandler complete production classes: 159 assertions. All four geometries, chance NBT copies, duplicate-output exact source selection, layout reuse, dry-run inertness, container identity and GuiOpenEvent target lifecycle pass. Bytecode call allowlist verifies no craft/packet/inventory operations in the selection handler. Actual production GUI and container are explicitly isolated NTM collaborators, not claimed compiled/gameplay tested.
- SatelliteHandler complete production class: 407 assertions with actual weighted loot classes and extracted unchanged production tooltip helper; all/output/input lookup, NBT, weighted probabilities and output grouping pass. Separate source/overflow parity 540 remains source-only.
- Source registration inventory: 300 assertions, 72 original built-ins in original order, unique UIDs, RTG condition, all dynamic configurations and exact translated machine-GUI rectangles.
- Full final rerun logs: /tmp/jei-core-final-proof.log, /tmp/jei-handlers-final-proof.log, /tmp/jei-custom-final-proof.log, /tmp/jei-anvil-final-proof.log, /tmp/jei-satellite-final-proof.log.
- Navigation tooltips are now finished, not pending: native localized jei.tooltip.show.all.recipes, tested from pinned locale. Shared isNavigationArea bounds drive both tooltip and left/right clicks. All 18 specialist overrides converted, including false bounds for Anvil and Satellite (no invented hotspot).
- ToolingHandler and ConstructionHandler catalyst overrides were corrected to native getMachines; original mappings retained.
- Satellite null-padding concern: upstream legacy PositionedStack.generatePermutations removes nulls, so native compact alternatives match that implementation rather than blank pages. Evidence pinned GTNH NEI commit7163630cb16b005957a435b71c79f61e5871ad80, source SHA256 aca1f50bcb36cac8bf3509c28bd8f073f6b37a2572f43c9e41cc161da1584a7d. Exact original ChickenBones1.0.3.74 artifact is not independently verified; do not overstate this evidence.
- No new production edits planned. Parent can run final fresh common compile and reconcile exact source-history guards.

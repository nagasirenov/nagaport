# String-tooltip contract review

## Conclusion

The 12 legacy `drawCreativeTabHoveringText(String,int,int)` calls are suitable for direct migration to native `drawHoveringText(String,int,int)`. No additional String-wrapper semantic incompatibility was found beyond the already accepted native List-tooltip layout, hooks, and depth-field behavior in `/tmp/tooltip-native-path-review.md`. A separate pre-existing ArmorTable empty-cursor guard defect is identified below and should remain outside this rename batch.

Reviewed source at CP66 HEAD `c986d3f35683a7b81fe0e2caf928f6a47a7a6b5f`, supplied old/native bytecode, exact 1.7.10 SRG and Forge1614 patch archives, and `porting/tests/native-string-tooltips/before-attribution.tsv`. This review did not edit main source, download, install, push, or execute rendering.

## Exact wrapper and receiver contract

- `joined.srg` inside `/tmp/nagaport1710-srg.zip` maps `bdw` to `GuiScreen`; `bdw.a(Ljava/lang/String;II)V` to `GuiScreen.func_146279_a`; and `bdw.a(Ljava/util/List;II)V` to `GuiScreen.func_146283_a`.
- Old `/tmp/string-tooltip-old-screen.javap`, String method offsets 0–17, builds `Arrays.asList(new String[]{text})`, then virtually invokes the three-argument List method with the same x/y. Native `/tmp/string-tooltip-native-screen.javap` does the same with `drawHoveringText`. Neither wrapper splits lines, strips formatting, filters null/empty text, chooses an item font, changes coordinates, or directly touches GL/depth fields. Both pass a fixed-size, element-replaceable one-element list. The native method is public instead of protected; this does not obstruct these callers.
- Forge1614's `net/minecraft/client/gui/GuiScreen.java.patch` leaves the String method unchanged. It delegates the List method to the font-aware overload with the screen font. Native likewise delegates String → List → font-aware overload, then native GuiUtils. Thus the String wrapper does not add a distinct rendering contract beyond the reviewed List path.
- Attribution records exactly 12 selected calls in 10 `drawScreen(int,int,float)` methods, all arguments `[java.lang.String, int, int]`, all GuiScreen-subtype receivers. Eleven use explicit `this`; only GUICombustionEngine uses an implicit receiver. Nine classes inherit `GuiInfoContainer → GuiContainer → GuiScreen`; GUIScreenRecipeSelector directly inherits GuiScreen. Every selected member closure contains only native GuiScreen tooltip overloads. Source inspection found no relevant legacy/native tooltip override and no source subclass of these ten concrete callers.

## Bounded call-state audit

Seven machine calls are localized yellow “set recipe” fallbacks: PUREX, PrecAss, AssemblyMachine, AssemblyFactory, ChemicalPlant, ChemicalFactory, and FusionTorus. Existing recipe-presence/map tests and mouse bounds select these branches. ArmorTable uses a localized, slot-dependent purple/yellow label behind cursor/slot tests. RecipeSelector has three independent formatted literal labels with normal mouse coordinates. CombustionEngine builds a numeric `mB/t` string and clamps both coordinates; its lock flag permits the tooltip outside the ordinary hover rectangle. All twelve arguments are String concatenations and therefore non-null results even if a localization helper returns null.

None of the ten calling drawScreen methods directly writes screen or item-renderer depth fields. The six relevant machine background methods balance their own `zLevel=300` with `zLevel=0`; RecipeSelector's item-render helper balances item-render depth 100→0. GuiInfoContainer's drawing helpers separately reset their own depth writes. No selected source caller was found relying on the old tooltip's forced depth-zero cleanup. This does not establish safety against external overrides, event handlers, or exceptional control flow.

## Recommended no-render caller coverage

Every complete original drawScreen method performs unrelated rendering before the selected call. Do not claim a no-GL run of any complete original method.

Use exact complete if-statement subtrees from these two methods, with explicitly documented fragment-only scope:

1. `GUICombustionEngine.drawScreen(int,int,float)`, lines 51–53: unique implicit receiver, numeric text, mouse-lock OR gate, and both clamped coordinates. Test unlocked interior and edge inclusion/exclusion (left inclusive, right exclusive, top exclusive, bottom inclusive); locked far-outside inputs should exercise both clamp endpoints.
2. `GUIScreenRecipeSelector.drawScreen(int,int,float)`, lines 151–161: all three independent tooltip if-statements, their exact bounds, yellow/italic payloads, and unchanged mouse coordinates. Exercise each hit rectangle, excluded boundaries, and misses.

Keep native TextFormatting, MathHelper and GuiScreen. Run genuine String→List→font-aware dispatch and intercept the font-aware overload for capture, or use the genuine Forge Pre path with cancellation before rendering. A deliberate capture hook is not a fabricated native API. Preserve the source subtrees exactly except the intended method-name migration and record their origin/hashes. These four subtrees cover meaningful String-specific payload/coordinate/gate variation, not localized recipe branches, ArmorTable's slot/cursor gate, whole screens, GL state, pixels, or full-mod compilation. Those other gates remain source-audited only.

## Separate actionable defect: ArmorTable empty cursor

`src/main/java/com/hbm/inventory/gui/GUIArmorTable.java:38` gates the tooltip at line 58 on `this.mc.player.inventory.getItemStack() == null`. Under ordinary native 1.12.2 empty-cursor state this is false, so the tooltip branch is skipped even after the method rename.

Verified actual native bytecode with the existing Forge jar at `/workspace/shared/nagaport/ntm-port-1.12.2/.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`, using its `InventoryPlayer` class. Evidence is `/tmp/string-tooltip-native-inventory-player.javap`:

- Constructor offsets 70–74 initialize private `itemStack` to `ItemStack.EMPTY`.
- `getItemStack()` offsets 0–4 return that field directly.
- `setItemStack(ItemStack)` offsets 0–5 store the supplied reference directly, with no null normalization. Therefore do not claim null is impossible; legacy callers could still inject it.

A separate narrowly scoped fix should use native emptiness semantics at that exact guard and test an empty cursor versus a held stack, while deciding whether a null compatibility guard is needed for remaining legacy setters. Do not bundle this into the String-tooltip rename or imply that this audit fixes ArmorTable reachability.

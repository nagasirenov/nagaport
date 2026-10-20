# Scoped native anvil category and selection probe

Run from the repository root with a Java 8 JDK and the actual pinned dependency classpath:

```sh
python3 porting/tests/jei-anvil-native-test.py --java-home "$JAVA_HOME" --classpath "$NATIVE_CLASSPATH"
```

The runner resolves `java`/`javac` or their Windows `.exe` names and uses the platform classpath separator. It compiles the complete production `AnvilRecipeHandler`, `AnvilOverlayHandler`, `MachineRecipeCategory`, `MachineRecipeView`, `NativeRecipeLookup`, `RecipeItemAlternatives`, and `RefStrings` sources, without source slicing or production changes. It reuses the existing native core test's real JEI ingredient-registry setup.

Verified on 2026-10-19 using Java 8, Minecraft 1.12.2 / Forge 14.23.5.2768, JEI 4.12.0.216 and NEI 2.4.3.245: **159 assertions passed**.

## Runtime coverage

- The complete anvil category and transfer class compile and execute against native dependency classes. No Minecraft, Forge, NEI or JEI class is replaced by a fixture.
- Native JEI transfer helper/internal-error instances are used, including dispatch through the real `IRecipeTransferHandler` interface.
- Both `maxTransfer` values with `doTransfer=false` leave GUI focus untouched. Executing transfer passes the exact displayed `AnvilConstructionRecipe` instance.
- Distinct same-output recipes stay distinct in output queries and transfer. A reused recipe layout resolves the newly displayed source rather than retaining a stale selection.
- Missing target, unknown/null layout, wrong/null container and GUI closure reject transfer. An adversarial NTM container fixture deliberately compares equal to another container, proving the guard is reference identity rather than `equals`.
- Native Forge `GuiOpenEvent` objects exercise anvil opening, a non-anvil screen, returning to the original anvil, replacing the anvil GUI and closing. A non-null intermediary screen retains the target; returning does not clear/reapply the selection callback; closing clears it.
- All four input/output layout shapes, including wrapped rows, retain their original coordinates. Tier alternatives remain decoration rather than indexed ingredients. The twelve catalyst entries and native JEI background dimensions are checked.
- Native NEI wildcard matching and duplicate-input suppression remain intact. Changing the source registry changes the next query. Chance-tooltip requests decorate copies rather than mutating authoritative recipe stacks.
- Real Minecraft inventory ItemStack identity, count and NBT remain unchanged. Fail-on-action NTM container methods catch transfer/click attempts. An ASM audit of the actual compiled overlay class restricts every method invocation and external field write: its only selection side effect is `GUIAnvil.focusRecipe`, with no craft, packet-send or inventory-action call.

## Explicit proof limits

The temporary fixtures replace only NTM-owned `ModBlocks`, `NTMAnvil`, `RecipesCommon`, `AnvilRecipes`, `ItemStackUtil`, `ContainerAnvil` and `GUIAnvil` collaborators. Recipe fixture fields mirror the source's `List<AStack>`, `List<AnvilOutput>`, `tierLower`, output `ItemStack` and float chance shapes. The GUI fixture extends the real `GuiContainer` and records callbacks; it is **not** a production gameplay GUI. Interface proxies observe layout calls against real JEI interfaces, without copying their APIs.

This is not proof that the complete `GUIAnvil`, `ContainerAnvil`, all recipe registration, rendering, Forge event-bus dispatch or an in-game JEI round trip works. The non-anvil screen is an anonymous native `GuiScreen` subclass, not a running JEI recipes screen. Production GUI/container sources still contain legacy API calls and require their own migration/whole-class validation.

Separate, explicitly source-only guards check that the actual `GUIAnvil.initGui` does not reset selected recipe/paging and that its `focusRecipe`/recipe-reset path has no packet/inventory call. Thus the callback target survives the tested native event sequence, while production GUI selection persistence after a full client reopen remains an in-game/whole-GUI test obligation. The tooltip fixture observes the requested percentage; it does not validate NTM's full tooltip helper.

# Dynamic crafting handlers: incremental native port

Baseline: isolated seven-producer follow-up commit
2c6dc45f (full commit recorded in dynamic-crafting-source-manifest.json).
No main worktree edits or full Gradle build. Production changes are only
RBMKFuelCraftingHandler, CargoShellCraftingHandler, ScrapsCraftingHandler and
three existing registration statements in CraftingManager.

All three handlers are real native IRecipe/IForgeRegistryEntry instances,
registered at their original producer positions through the already tested
recipe event lifecycle, with explicit special/rbmk_fuel, special/cargo_shell,
special/scraps identities. No precedence wrapper, interception, new outputs,
recipe-book unlocking or producer relocation. Original getRecipeSize remains
as a non-interface method. Native canFit uses required occupied slot counts;
actual matches still executes the complete original grid algorithm.

## Preserved behavior

RBMK: exactly one occupied slot; ItemRBMKRod with a nonnull pellet; both heats
strictly below 50. Eight pellets, original ceil/clamp enrichment metadata and
poison offset at >=0.5. The original matches/result asymmetry at enrichment
>0.99 is retained: matches may return true while result is empty. Actual
ItemRBMKRod NBT accessors can initialize the input's default NBT while matching;
that original mutation is retained. Input metadata/count do not affect these
conditions. Native remaining-item payload uses the item's configured empty
fuel hull; this is NOT an original SlotCrafting placement parity claim.

Scraps: exactly one scraps stack, material amount >=2; output is two scraps
with integer-truncated half amount, original material metadata, and rebuilt
NBT. Original liquid/extra tags are not transferred. Default untagged amount is
one ingot (72). The original malformed material-ID null dereference is retained
and explicitly tested, not silently repaired under the port.

Cargo: exactly two occupied slots and exactly one untagged ammo_arty metadata8
shell. Payload is copied at count1 and serialized into the copied count1 shell's
cargo compound. Original display output ammo_shell versus actual ammo_arty is
retained. A tagged ammo_arty may itself be cargo beside one clean shell.

Native inventory EMPTY and result EMPTY replace only legacy absence checks/
null results. Internal nullable algorithm variables remain nullable. No recipe
template zero-count stacks are normalized or rewritten.

## Removed container predicate: bounded equivalence proof

Original Cargo ORed hasContainerItem(stack) with !doesContainerItemLeaveCraftingGrid(stack).
Only the second term is removed; the native stack-sensitive first term remains.
Pristine NTM has exactly two leave-grid overrides: ItemCraftingDegradation
(false, stack-sensitive container true) and ItemFluidIDMulti (false, no-arg
container true). ItemTooling, ItemToolingWeapon and ItemDefuser inherit those
methods unchanged; no other NTM subclass overrides them. Both Forge1614 and
Forge2768 stack-sensitive container predicates delegate virtually to the no-arg
method. Thus the removed term never independently rejected an original NTM item.
Original vanilla Item returns true from leave-grid and no vanilla Item subclass
overrides it, so vanilla also has no independent rejection. Durable source and
bytecode audit is retained separately in cargo-container-contract-audit.txt.

Unknown external legacy mods could return false for both predicates. Forge2768
has no equivalent leave-grid item query, so those external opt-outs cannot be
claimed preserved. A specific integration would need separate native design.
No invented instanceof exclusion, fake method or constant replacement is used.

## Executable evidence and limits

check-dynamic-crafting-source.py inverts only reviewed native API substitutions,
compares all three complete handler bodies and the full CraftingManager body
against the frozen prior baseline, and asserts exactly three registration moves.
Earlier Mineral/auto and seven-producer source checks remain executable.

probe-dynamic-crafting.py compiles the complete three production handlers and
real native registry helper against pinned Forge2768, extracts the exact three
producer statements, and executes them through real GameData recipe events.
NTM heavy item/catalog collaborators are bounded fixtures: RBMK getter/setter/
default-NBT methods, Scraps getMats/create overloads, MaterialStack, quantity q,
and the two container predicates are extracted byte-exact from production
source with hashes logged. Their whole item/rendering/physics owners are NOT
compiled or claimed ported. Material registry and item identities are fixtures.
No Minecraft/Forge platform classes are mocked. All ItemStack/NBT, grid, native
IRecipe matching/results, container dispatch and registry operations are real.

99 assertions pass, covering threshold boundaries, original exceptional and
asymmetric cases, metadata/count/NBT behavior, independent serialized cargo,
real water-bucket exclusion, and both original NTM container dispatch forms.
The predicate fixtures intentionally do not implement the removed leave-grid
API: its independent redundancy is proven by source/bytecode audit.

Run with Java8 and the established pinned native classpath:

    python3 porting/tests/check-dynamic-crafting-source.py
    python3 porting/tests/probe-dynamic-crafting.py --java-home "$JAVA8" --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"

Remaining gates: heavy item owners and complete mod compilation, global original
RecipeSorter precedence, original remaining-item placement, and runtime config
registration after registry freeze. These local tests do not establish a full
mod launch, world session, all third-party compatibility or completed crafting.

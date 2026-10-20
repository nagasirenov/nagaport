# Thermal furnace family: original algorithms, shared native boundary

Frozen production ownership is six TileEntityFurnace/ContainerFurnace files for
Brick, Iron and Steel. The companion GUI family owns their three GUI files and
has its own exact guard, native test and notes. This is a scalable family batch,
not three machine rewrites. No balance, refill, heat, progress, ash, callback or
achievement correction is included. Original/pristine sources are untouched.

## Transform and dependency scope

`thermal-furnace-family-migration.py` has exactly six whitelisted files. Its
manifest stores checkpoint18 baseline/current full-source SHA256, every exact
replacement at its before/after offset, and per-file edit counts. Application
preflights all six exact baseline hashes before writing any file. Default audit
reconstructs every complete baseline, reapplies the batch and rejects 18 drift
mutations. There is no global null conversion. Exported `reverse_source` is
available for the parent's historical source-guard composition.

The common boundary changes are FurnaceRecipes.instance and native EMPTY recipe
absence; nullable internal inventory versus native empty stack checks; native
count mutations; inherited World/BlockPos access; AABB constructors; typed native
particles; and the native fire ambient sound/category. Containers return/clear
native EMPTY without changing slot ranges, direction or routing. Brick still has
no onSlotChange call; Iron and Steel retain theirs. None acquires onTake.

Complete unchanged ModuleBurnTime, FuelHandler (real Forge IFuelHandler),
IConfigurableMachine, IHeatSource, ItemStackUtil, UpgradeManagerNT and SlotUpgrade
compile in the family probe. Shared RecipesCommon and BufferUtil migrations are
owned/tested separately and compile as complete production classes in this probe.
Their exact compiler-input SHA256 values are in the final log. No classpath fake
Minecraft/Forge/NEI type, production shim or source exclusion was introduced.

## Depletion boundary and original callback audit

Native ItemStack hides its item at zero count. Both fuel consumers capture only
the callback's Item receiver before shrink, then invoke getContainerItem with the
same original stack object, already at count zero, at the original point in the
algorithm. A positive copy is never substituted for an arbitrary callback.
Native EMPTY callback results become null only at the nullable backing-array
boundary. Tests assert callback identity, observed zero count, one invocation,
no premature invocation and actual vanilla lava-to-bucket residue.

Brick's ash lookup remains after decrement. An identity copy is captured before
depletion solely for the count-independent ore classifier. The old classifier
is verbatim in the probe; ItemStackUtil is complete production code. Pinned
Forge1614 getOreIDs reads item registry identity and metadata, not count or NBT;
see `recipe-key-original-ore-evidence.log`. Native Forge refuses empty stacks,
so passing the depleted native stack would misclassify last wood/coal fuel.
The copy preserves item/meta/NBT while classification and the wood/coal/misc
processing order remain at their original post-decrement position.

The original tree has exactly two NTM getContainerItem(ItemStack) overrides:
ItemCraftingDegradation mutates/returns its argument; ItemFluidIDMulti copies its
argument. Neither original registered item is a vanilla fuel or appears in the
original FuelHandler fuel list. Ordinary reachable fuels use vanilla/Item
container behavior, including the tested lava bucket. This does not claim that
an arbitrary external mod callback sees a legacy item identity through native
zero-count getItem: its actual argument is necessarily the native depleted stack.
A callback inspecting count does see the original zero, not a substituted one.
No actual original reachable fuel requires a callback behavior compromise.

## Grouped native execution

From project root:

    python3 porting/tests/thermal-furnace-family-migration.py
    python3 porting/tests/probe-thermal-furnace-family.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"

The probe compiles 30 byte-identical complete production classes with real native
Minecraft/Forge libraries. Test-only NTM boundaries are explicitly listed in the
runner. World is a controlled subclass of actual native World, never a fake
World API. Mod item/block registry fields, atmosphere, pollution, block toggle,
packet dispatch, GUI and output achievement boundaries are controlled. Only
InventoryUtil.mergeItemStack, DictFrame.fromOne(Item,Enum), the original ash
classifier and BufPacket transport body are verbatim extracted methods/bodies.
The complete InventoryUtil, Firebox, OreDictManager and BufPacket handler are NOT
claimed to compile. Output SlotSmelting/SlotCraftingOutput remain explicit NTM
collaborators, not an achievements migration.

492 assertions execute the full production thermal tiles and containers:

- Brick speed1/2/4 and200 progress, burned-fuel decay while blocked/no-air,
  threshold2000, single wood/coal/misc/lava ash/residues, one ash per type per
  fuel rather than an added while loop, blocked/full ash, wood-first ash order,
  last fuel and input, and the original stale-progress outer-guard anomaly.
- Iron160/134/107/80 durations, original lignite/coal/coke/solid/rocket/balefire
  multipliers through the complete real fuel cache, first valid fuel slot,
  loading while blocked but burning only successful ticks with air, lava residue
  and mutated zero-count callback. Cache keys remain count/NBT independent and
  distinguish coal versus charcoal metadata.
- Steel33333 threshold,40000 process completion, one computed burn reused across
  three sequential lanes, same-reference lastItems aliases and positive final
  removal alias,25 ore/50 log/tar bonus, saturation loss, item-change resets,
  ceil(diff*0.05), cooling/equal-source/maxheat behavior and full source drain
  before cap. Completion outside canSmelt still occurs, including incompatible
  overfull output: deliberately not repaired.
- Native absent/EMPTY/zero input, missing and multi-count recipes, capacity/full
  outputs, all six sided directions, native container full/partial conservation,
  original plain-Slot routing precedence and original callback omissions.
- Exact NBT field names/transient omissions, packet field/array length order,
  inherited muffled prefix and complete BufferUtil arrays, server-only mutation,
  client particle branches, sound dispatch and native network coordinates.

Logs are `thermal-furnace-family-source.log` and
`thermal-furnace-family-native-FINAL.log` in continuation-2026-10-19.
The earlier native.log records the first successful grouped run, not the final
helper snapshot. GUI coverage and its separate6,735 assertions are described in
thermal-gui-notes.md.

## Limits

No actual block/TESR integration, GL client, dedicated server, live world save,
full-mod compile success, runnable mod JAR or playable-machine claim. Iron/Steel
blocks still inherit the unported663-line BlockDummyable root. Brick block toggle
is a controlled boundary here. No full Gradle run, source exclusions, commit or
push was performed by this worker; the parent owns the common clean build and
checkpoint packaging after source freeze. The native sound mapping is compiled
and dispatched under controlled World, not acoustically validated.

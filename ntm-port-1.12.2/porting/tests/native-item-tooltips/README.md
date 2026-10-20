# Native terminal item-tooltip callbacks

## Sealed scope

Exact base: `112cf44057e5cd1d1173561e66dbcca97edc9bdb` (CP71).
Production changes exactly 91 declaration lines in 91 files:

`addInformation(ItemStack, EntityPlayer, List, boolean)` becomes
`addInformation(ItemStack, World, List<String>, ITooltipFlag)`.

The native types are fully qualified to avoid unrelated import churn. All entire
method bodies, annotations, line endings, text ordering, localization, numeric
expressions, NBT reads, inventory access and unrelated methods are byte-identical.
There are no adapters, new production classes, source exclusions, dependencies,
global-player lookups, hazard/radiation formula edits, or modified resources.

The selected classes directly extend native Minecraft Item (79), ItemFood (5),
ItemBlock (3), ItemSword (3), or ItemRecord (1). Full-source javac attribution
shows Item ancestry, no source descendants, one addInformation declaration per
selected owner, no resolved owner-specific direct consumers, and zero references
to either the old player parameter or old advanced boolean in each body. The
selection deliberately rejects custom-parent/domain families even when they look
locally harmless. All 98 excluded legacy Item declarations and reasons are in
the manifest. This is a finite safe subset, not all legacy item callbacks.

## Contract and flag timing

The supplied 1.7.10 client and joined SRG mapping identify Item.func_77624_a as
`adb.a(add,yz,List,boolean)`. Its actual bytecode is an empty return. Native Item's
`addInformation(ItemStack,World,List<String>,ITooltipFlag)` is likewise empty.
Actual original ItemStack passes its caller's player and boolean; native
ItemStack passes null if its player is null, otherwise player.world, and forwards
the same flag object. Saved javap methods and class/archive hashes substantiate
these statements (`engine-proof.py` regenerates them).

Because both changed inputs were unused, the port does not read world, synthesize
a player, evaluate `flag.isAdvanced()`, or introduce a cached boolean. There is
therefore no new flag evaluation timing or null-flag failure inside these
callbacks. Raw List versus List<String> has the same JVM erasure. Existing
callback branches and operations on the stack/list remain in their original
order. Tests verify zero flag evaluations with counting native flags and null
flags. Native vanilla's surrounding tooltip behavior is not claimed identical
to 1.7.10; only these callback bodies and their proper native dispatch are covered.

## Direct-call limitation, explicitly retained

The complete recorded call audit includes unresolved calls; it does not pretend
that all source calls type-check. ArmorUtil.addGasMaskTooltip still invokes
`filter.getItem().addInformation(filter, player, lore, ext)` and the legacy
ForgeEventFactory tooltip event form. This dynamic Item receiver remains an
independent compile blocker and may eventually reach migrated items. It is not
an owner-specific consumer and was already unresolved on CP71. The surrounding
IGasMask/player/domain bridge is deliberately untouched; this patch does not
claim that gas-mask tooltips are now functional. Other unresolved super calls,
existing ItemBlockBase player bridges, block ITooltipProvider methods, and the
player-dependent Sedna family are likewise outside this scope.

## Evidence and outcomes

- `check.py` proves 91 exact Git-base whole-file inverses and forward round trips,
  identical complete bodies, owner ancestry/descendant/overload closure, all
  source direct-call targets/text unchanged, and 273 rejected signature/body/
  flag-evaluation mutations.
- Full-source native attribution: **13,966 → 13,875 errors**. Exactly 91
  `method.does.not.override.superclass` diagnostics disappear, all belonging to
  selected files; zero new diagnostics. Both runs remain failed/incomplete mod
  attribution, not successful compilation. Current full snapshots are stored
  compressed with deterministic gzip, not copied historical build logs.
- Real original and real native Item base methods are executed, including null
  stack/player/world/flag inputs where applicable and both original booleans.
- Eight complete unchanged production callback bodies are extracted, checked
  against whole-file hashes and compiled in genuine native Item subclasses.
  **864 cases / 3,461 assertions pass**, covering metadata 0..17, null/present
  NBT with signed/extreme coordinates, formatting, literal line order,
  null/normal/advanced flags, and unchanged stack count/metadata/NBT presence.
  Examples include all 16 glyph labels, three food variants, italic starmetal
  variants, detector linked/unlinked branches, and the seven-line shuttle lore.
- Actual native ItemStack.getTooltip dispatch is exercised for normal and
  advanced flags using the complete shuttle callback and a test item registered
  through the genuine Forge registry API in the isolated process.
- A separately compiled text-corruption mutant fails the actual native tooltip
  assertion. No replacement Minecraft/Forge classes or platform placeholders are
  defined. The existing unsigned legacy test archive is reused only after
  checking all 2,844 non-META-INF entries against the supplied original.

These are complete callback-body tests, not complete production item-class,
localization-resource, rendering, client startup or gameplay tests. Other
callbacks are source-audited, not individually executed. The headless harness
registers its own test item, not a production mod item. Runtime output includes
Forge's expected namespace warning for that isolated test registration.

## Reproduce

From the repository root:

```sh
P=ntm-port-1.12.2
E=$P/porting/tests/native-item-tooltips
python3 "$E/check.py" --project "$P" --reattribute
python3 "$E/run.py" --project "$P" --out /tmp/native-item-tooltips-recheck
python3 "$E/engine-proof.py" --out /tmp/native-item-tooltips-engine-recheck
```

Defaults use the provided Java8 toolchain, native classpath, original client,
SRG archive and existing unsigned original archive. Flags support alternate
paths. No downloads, installations or full build are needed. Whole-file and
full-attribution snapshots are intentionally CP71-specific; composed checkpoints
must reconcile overlapping source edits before applying these exact checks.
Preserved CRLF files require `git -c core.whitespace=cr-at-eol diff --check`.

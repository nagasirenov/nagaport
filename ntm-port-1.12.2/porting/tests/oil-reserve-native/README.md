# Oil reserve lifecycle native boundary

Exact production patch base: d3e012b1ee72c0170cb8e4ff89bfabadb25ac553 (CP75).
Only BlockOreFluid is changed. Its scalar drain API remains; the old neighbor
callback is replaced by native neighborChanged. Flags 3, first-write then
second-write order, original chance formula and RNG draw order are retained.
The neighbor callback deliberately rereads live-world metadata, not supplied
state, including during synchronous reentrant notifications. Original world
bounds are tested before converting a target metadata state. The legacy
World bytecode bounds excerpt and pinned client hash are retained separately.

## Reproduce offline

From the module root (use the module itself for SUPPORT_MODULE in a full tree):

    python3 porting/tests/oil-reserve-native/run.py \
      --support-module /path/to/SUPPORT_MODULE \
      --original-root /path/to/ntm-original-pristine \
      --java-home /path/to/java8 \
      --classpath-file /path/to/native-probe-classpath.txt

The classpath is the existing Forge 1.12.2 native deobfuscated development
classpath, one colon-separated line, with existing cached dependencies.
No downloads or dependency changes are needed. The inherited ore-callback
probe must exist under SUPPORT_MODULE; it provides complete production
BlockOre, BlockCluster, interfaces, MultipassContext and explicit fixture
construction. This sparse worktree intentionally obtains unchanged support
sources from the full module. provenance.json records actual source and jar
hashes, not machine-specific paths as replay requirements.

run.py first executes source-proof.py. That pins the pristine original owner
hash, reverses only the specified native boundaries, then compares the entire
original owner ignoring whitespace and line comments. It audits the exact five
production bindings, all four nonnull target constructors as actual BlockOre,
and absence of BlockOreFluid descendants. Individual unchanged formula bodies
are additionally compared byte-for-byte. callers.log records class references;
existing extraction machines and unrelated legacy callbacks are not migrated
or certified by this patch.

## Native evidence and limits

verified.log: 1,951 runtime checks, plus the source gates. The test compiles the
whole production BlockOreFluid and actual full BlockOre parent, not a fake
16-state substitute. Actual native World.setBlockState, Chunk and synchronous
neighbor notifications execute. Real registry/state codecs cover all 16 values.
The test compares original ordered bodies, with only original platform calls
adapted, against native writes, RNG tails, final states and stale supplied-state
callbacks. It includes three reserve types, multiple vertical chains, raw int
metadata, negative/zero/NaN/infinite multipliers, out-of-domain coordinates,
null-world ordering and the null-target bedrock early returns.

The four actual nonnull bindings are ore_oil -> ore_oil_empty, ore_gas ->
ore_gas_empty, ore_brine -> ore_brine_empty and ore_tekto -> ore_tekto_empty.
ore_bedrock_oil has null target. Registry constructors are source-audited;
the runtime uses representative actual BlockOre instances per reserve type,
not full ModBlocks initialization. Production config and fluid registry are
explicit test value/identity fixtures. The complete Body enum is extracted;
unexercised astronomical collaborators fail closed. World uses preloaded real
chunks; lighting calculation is bypassed. No original/native universal engine,
arbitrary foreign target block, debug-world, full registry/startup, full compile,
asset/render, extraction-machine or launched-game parity is claimed. The
original oracle runs on native World; it is not a second 1.7.10 engine.

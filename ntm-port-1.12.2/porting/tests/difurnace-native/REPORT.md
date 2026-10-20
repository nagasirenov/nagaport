# DiFurnace native quantity lifecycle, CP80

Production base: a0dc7d14f453a1d6f01b0d5abdcd455358bf2714.
Production scope: TileEntityDiFurnace only. Six inaccessible stackSize uses are
replaced; no shared storage, other machine, recipe registry, formula or balance
changes. The prior last-fuel Item capture and callback alias correction stays.

## Explicit furnace boundary

The user approved treating zero/negative furnace stacks as native empty. Fuel
power, recipe inputs, recipe output acceptance and vacant output selection now
check native isEmpty. Existing nullable array storage remains nullable; native
EMPTY and noncanonical empty stacks are not valid fuel/input/results. An empty
output slot can receive a new recipe result. These admission checks use native emptiness, not a global normalization helper. The user separately approved rejecting malformed native-empty fuel, inputs and recipe outputs in this furnace. Dynamic-nonempty repaired aliases remain admitted and retain raw quantities through the private reader below; they are not covered by the empty-state approval.

Valid positive inputs/results use the private raw-quantity fallback described in
../difurnace-raw-reader/REPORT.md; grow/shrink retain native raw integer arithmetic. In particular MAX_INT result plus existing1 still passes the old
signed-overflow capacity expression and produces raw MIN_INT in the SAME output
object. It is not clamped, promoted to long, or discarded at mutation time. On
later checks that now-empty stack follows the explicitly approved empty policy.

Fuel and progress formulas, thresholds, residual extension progress, the original repeated recipe
lookups, output copying before consumption, sequential input decrement, dirty
ordering, pollution call formula, smoke calls and packet methods remain intact.
Actual MachineBase decrStackSize remains untouched: consuming a last positive
unit clears its slot without decrementing the returned/externally held alias.
The test explicitly preserves this original behavior rather than repairing it.

## Native execution and boundaries

run.py compiles the COMPLETE production DiFurnace and COMPLETE production
MachineBase, LoadedBase, WorldBlockState and their listed small support classes
against genuine cached Forge/Minecraft dependencies. runtime.log prints every
complete production input SHA256 and every controlled NTM source/hash. No fake
Minecraft/Forge classes or installed production substitutes are used.

Controlled NTM surroundings are registry field holders, GUI/container
constructors, block-state callback observations, atmosphere fixtures, pollution
parent operations, smoke tanks, fluid interfaces and packet dispatch. The
polluting parent is controlled, so this does NOT test actual smoke-fluid routing
or pollution accumulation. MachineBase/LoadedBase ARE real complete production
classes, so inventory decrement/setter/NBT and native packet serialization paths
are exercised. BufPacket body is extracted from production; its handler is not
included. Full gameplay, GUI/container operation and complete mod compilation
are NOT claimed.

BlastFurnace recipe registry fixtures and type expansion are controlled. Actual
getOutput traversal/copy, doStacksMatch, and ComparableStack.matchesRecipe are
extracted verbatim. This tests direct item matching in the real lookup algorithm,
count-ignored matching and copied result semantics. It
does not test full registry initialization, JSON/IMC, ore-dictionary expansion,
or all custom AStack subclasses. None of those production sources is edited.

PASS504 checks per MCP/SRG field layout execute actual complete DiFurnace methods: 400-tick cycles,
extension speed/residual2, capacity/mismatch, energy/no-power, input/output
aliases, last units, coal/blaze/lava power and containers, callback argument,
oxygen, refuel limits, nonpositive boundary cases, signed overflow, pollution
call amounts, client no-op, positive NBT, network bytes and scale formulas.
Two shared-input/output-input alias cases verify the original sequential
mutation order. Seven deliberately wrong production-class copies are separately
compiled only in the temporary probe and rejected by tests: changed energy,
replaced output merge, lost last-fuel receiver, strict capacity, doubled input
cost, stale-cache capacity substitution and invalid second-result acceptance.
Three source-drift controls also fail closed.

## Reproduction and historical guards

    python3 porting/tests/difurnace-native/check.py
    python3 porting/tests/difurnace-native/run.py

Uses /workspace/shared/nagaport-toolchain/java8 and cached classpath file
/tmp/nagaport-ae2-native-classpath.txt. No downloads. check.reverse_source
restores exact CP80 whole source; compose this BEFORE the older fuel receiver
source guard when checking historical snapshots. The older fuel block probe
itself expects pre-migration stackSize syntax and is superseded for current
DiFurnace by this stronger complete-class test, not evidence of a runtime
regression. Source manifest includes complete before/after hashes and edits.

The second recipe lookup now explicitly rejects null/native-empty before output
or input mutation. See the raw-reader report for exact timing and genuine SRG
field-layout proof. Historical scope-audit CONTRACT.md describes the rejected
pre-fallback proposal; its repaired-cache capacity change no longer applies.

# Native coordinate consumer migration

Bounded continuation of checkpoint9 `8240cad6ab08a3c3feaed397e8631640fdd8f5d3`.
Eight existing consumers plus the real, narrowly scoped `SolarMirrorPosition` key.
No old API namespace shim, source exclusion, stub, or unrelated command/render/boss
migration is introduced. This is not a complete mod build or gameplay acceptance.

## Ownership and behavior

- `EntityWormBaseNT` and `EntityBurrowingSwingingBase` each own a new native
  `BlockPos.MutableBlockPos`. `EntityBOTPrimeHead` uses the inherited field.
  In-place `setPos(int,int,int)` preserves aliases; getters preserve integer
  arithmetic, NBT keys and existing spawn floor conversion. No mutable spawn key
  is used in a hash collection.
- `CommandSatellites` reads `sender.getPosition()` for completion. Official old
  player `f_()` and native `Entity.getPosition()` both floor X/Z and floor Y+0.5.
  The old execution-path `(int)player.posX/posZ` truncation remains untouched.
  The completion target argument is not a substitute for sender position.
- `RenderSolarPanel` had only an unused import.
- Client mirror registration now constructs immutable `SolarMirrorPosition`
  objects containing full-range native `BlockPos` triples. No packed coordinate
  truncation, mutable registration, or new persistence format is introduced.
- Boiler primary/secondary remain persistent `HashSet`s. The exact clear,
  addAll, clear rollover and producer order remain unchanged. Renderer loop,
  increment-before-limit-break, integer differences and integer squared-distance
  arithmetic remain unchanged. No float/double distance helper is substituted.

## Why a domain key is necessary

Actual old binary `r.hashCode()` executes `((x+z) << (8+y)) << 16`, including
Java integer overflow and masked shift counts. Native `BlockPos` inherits
`(y+z*31)*31+x`. Six registrations `(i,64,i*3)` produce old X iteration
`[0,1,2,3,4,5]` but native BlockPos iteration `[0,4,1,5,2,3]` on pinned JDK8.
That changes which mirrors survive the rendering beam limit.

The domain key preserves old hash and same-triple equality without extending
BlockPos, which would violate native equality/hash symmetry. It intentionally
has no Comparable implementation. The official old class implements raw
Comparable; JDK8 HashMap does not recognize that as a self-typed ordering for
tree bins. Identity-based tie-breaking makes arbitrary collision-heavy iteration
nondeterministic across allocations, even with the original class. Tests therefore
assert exact small/non-tree-bin fixtures and membership/lifecycle invariants for
large collision sets, not universal cross-allocation render-order equality.

## Evidence and portable tests

- `check-native-coordinates.py` uses the manifest's ordered exact replacement
  offsets and complete reconstructed checkpoint9 file SHA-256 hashes. It needs
  neither git objects nor a pristine sibling. Exported `reverse_source(rel,text)`
  is a fail-closed bridge for earlier audits. It returns unrelated paths unchanged.
  The eight files are deliberately exact snapshots; an unrelated edit to one of
  them requires a new reviewed manifest, not silent re-baselining.
- `NativeCoordinateMigrationTest.java` compiles the actual production key and uses
  real native BlockPos/MutableBlockPos/NBT/ICommandSender APIs. It covers signed
  extremes, shifts/overflow, duplicates, alias restoration, snapshots, queue
  rollover/stale clearing, beam limits and collision-heavy membership.
- Ordinary recipient runs use the deterministic legacy instruction-sequence
  expression. Development additionally compared the production key directly to
  official Minecraft 1.7.10 client binary class `r` via isolated reflection.
  SHA-256 of that development oracle jar:
  `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
  The jar is not distributed and is not needed for recipient tests. Optional
  `-PlegacyMinecraftClientJar=<path>` or `--legacy-client <path>` enables it.
- Legacy player position bytecode (`mw.f_()`) and native Entity.getPosition()
  were inspected with pinned JDK8 javap; both use X/Z floor and Y+0.5 floor.
  The native command test checks the API/rounding contract with a test sender,
  not a live player or the full unported CommandSatellites class.

Dedicated Gradle invocation (Windows PowerShell, with normal project JDK8 setup):

    .\gradlew.bat --offline -I porting/native-coordinate-probe.init.gradle checkPortNativeCoordinateSource testPortNativeCoordinates -PportPython=python

`portPython` is one executable path, not a shell command with arguments. Select
an installed Python 3 executable. Linux development used the pinned Gradle4.10.3
and JDK8 runner; Windows execution remains untested.

Portable standalone alternative (supply actual target compile classpath, using
Windows semicolons or Linux colons as appropriate):

    python porting/tests/probe-native-coordinates.py --java-home JDK8_PATH --classpath TARGET_CLASSPATH

This runner uses pathlib, subprocess argument arrays, platform executable suffixes,
`os.pathsep`, and a temporary working directory. It does not require Bash. The
Gradle init adds diagnostic tasks only; it does not alter compileJava/sourceSets.

## Explicit remaining full-consumer blockers

The complete consumers are still part of the normal mod build and still blocked
by independent APIs (the worm selector is already migrated to Guava Predicate):
IBossDisplayData and old spawning APIs in BOTPrimeHead; old command overrides,
parsers/tab helper, held-item access and stackSize; old TESR/Tessellator and
IItemRenderer; legacy tile/world coordinate/light/sky calls and AABB factory.
This checkpoint does not replace or hide these blockers. Full compiler evidence
is owned by the common freeze build, not the dedicated key/API probe.

## Final fixture correction and proof counts

Removed a tautological cast assertion from the synthetic sender fixture. The
fixture checks native BlockPos rounding and ICommandSender API compatibility,
not actual Entity.getPosition execution or the full command. Actual player
rounding evidence is the separately inspected official/native bytecode.
The earlier Gradle log records the pre-correction run (60,800); final direct
source compilation/execution records 60,793 checks without the optional binary
and 70,044 with it. No additional Gradle run followed this test-only correction.

The Windows command above does not need or download the legacy client binary.
Only users who already possess the exact official 1.7.10 client and want the
extra oracle should append `-PlegacyMinecraftClientJar=C:\path\to\client.jar`.
The source manifest and normal expression-oracle tests remain standalone.

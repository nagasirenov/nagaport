# Vehicle/missile scalar data tranche

Exact baseline36387bc1c10fa9c36fbb27f5e9d44b45d474eb18 (CP62). Independent of sealed BYTE and Vortex batches. Six production files,35 exact watcher operations,seven actual static per-owner DataParameter keys.

## Owners/defaults and external closure

- EntityBoatRubber directly extends Entity. Slot17 Integer0 becomes TIME_SINCE_HIT/VARINT; slot18 Integer1 becomes FORWARD_DIRECTION/VARINT; slot19 Float+0 becomes DAMAGE_TAKEN/FLOAT. All setters/getters preserve exact arbitrary values; no vanilla EntityBoat inheritance or helper replaces its original custom movement.
- EntityMinerRocket directly extends Entity. Slot16 Integer0 becomes FLIGHT_MODE; slot17 Integer0 becomes SATELLITE_FREQUENCY, both VARINT. Existing unqualified versus explicit-this receiver forms are preserved. New typed getFlightMode/getSatelliteFrequency/setSatelliteFrequency methods replace all three external SatDock numeric consumers. Dock launch sets the exact frequency; unloading still requires mode==1 AND timer==50, and its chip mismatch logic still compares the same raw int. No launch/spawn or item logic changed.
- EntitySoyuz directly extends Entity. Slot8 Integer0 becomes SKIN/VARINT. RenderSoyuz reads the existing typed getSkin via a cast justified by its sole EntitySoyuz renderer registration. No resource, GL or skin indexing behavior is changed.
- EntityCombatDropPod extends native EntityThrowable. Slot20 Integer0 becomes COLOR/VARINT. Existing setColor still writes BOTH its ordinary local color field and synchronized key; existing getColor reads the key. Other entity payload state stays ordinary NBT, not a newly synchronized compound.

Each key is private static final and allocated with its own declaring class, with no shared key objects. Existing entityInit registration has no side asymmetry and occurs after vanilla base initialization. EntityThrowable has no extra custom entityInit registration. No descendants of these four classes exist. consumer-closure.json records every production file referring to these entity types; none retains a watcher call. audit.py independently verifies the SatDock typed constructor/iteration declarations and Soyuz registration domain. No DataWatcher type aliases exist.

## Persistence and initialization are preserved, not repaired

Boat's empty read/write entity NBT remains empty: a fresh load retains direction1, damage0 and hitTime0; reading into a live entity does not reset its transient state. MinerRocket persists mode/sat/timer exactly, with absent keys zero. All flight motion, docking timing, bounds and explosions remain byte-for-byte unchanged apart from the typed synchronization access.

Soyuz's entire original readEntityFromNBT and writeEntityToNBT, including ItemStack payload loops and legacy APIs, are UNCHANGED. This tranche neither converts nor mocks its payload serialization. Probe tests only exact leading scalar statements before the payload loops, with explicitly renamed readScalarPrefix/writeScalarPrefix fixture methods. It does not claim whole Soyuz NBT compiles. Calling the fixture's actual full-NBT method names deliberately throws, so there is no silent no-op payload substitute.

CombatDropPod's actual complete NBT methods are tested using an ordinary compound payload. They retain the original lack of EntityThrowable super calls. The local color field used by writeEntityToNBT is NOT updated when client metadata changes the manager key. This existing asymmetry is explicitly tested: a packet changes getColor while later client save still writes the previous local color. No new behavior is introduced to “fix” it. The setter and setPayload continue to update both values.

Production constructors and initialization side effects are not edited. Minimal test-only constructors just invoke the native parent, exercising actual virtual entityInit; unrelated production constructor geometry/world effects are outside this evidence and protected by inverse source comparison.

## Native evidence

PASS92 focused assertions covering unique vanilla/custom allocations, per-owner defaults and full snapshots, native SPacketEntityMetadata byte serialization/read/application, signed integer and negative-zero setter values, equal-value dirty behavior, empty-delta handling, binary CompressedStreamTools NBT for all tested save slices, exact dock accessors/unload predicate and Soyuz renderer read.

The existing native integer/float wire-edge characterization from earlier batches is reused rather than redundantly rerunning a large protocol suite per owner. Empty default deltas contain a null entry list; the test applies them only when non-null, matching the actual client handler's guard (included javap excerpt). This corrected an initial harness null-entry assumption, not production behavior. Initial generic type inference in one fixture assertion was also corrected; final production-method probe passes.

source-manifest.json records original/baseline/after hashes, exact full statements and source lines, declaring/receiver owners, serializer/default and source method. audit.py compares exact original watcher INVOCATIONS, rather than requiring surrounding already-ported world/block text to match. It independently verifies current ranges/owners and reconstructs every production byte from CP62 after reversing only declared migrations/imports/accessors. Thus untouched constructor effects, payload loops, NBT, movement, spawn and balance logic are all conserved.

No ItemStack/BlockPos/optional serializer migration, fake platform classes or production exclusions. This is not full entity/mod compilation, dedicated-server/client startup, launch simulation, renderer GL acceptance or full Soyuz save acceptance. Many unrelated legacy APIs still block full classes; coordinator handles integration compile.

Run from project root:
python3 porting/tests/native-entity-vehicle/audit.py /workspace/shared/nagaport/ntm-original-pristine
JAVA_HOME=/workspace/shared/nagaport-toolchain/java8 python3 porting/tests/native-entity-vehicle/probe.py

Classpath defaults to /tmp/nagaport-ae2-native-classpath.txt; override NAGAPORT_NATIVE_CP_FILE. No downloads, new dependencies or full Gradle build.

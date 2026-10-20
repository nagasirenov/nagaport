# Native rocket metadata: bounded migration and assessment

Baseline: checkpoint 10, commit `8098f2258b5880cf948009c5e9d766f6b2f536ba`.
The two modified existing production files are identical at checkpoint 11 (`24dab0cd`).
This slice removes RocketStruct's final DataWatcher declaration dependency and migrates
its sole EntityRideableRocket caller, including that class's nested dummy. It does not
claim to migrate the rocket's entire inheritance tree or make the mod runnable.

## Production boundary

`RocketMetadata` is a complete, small production native-key group, not a numeric old-API
shim. EntityRideableRocket allocates every key with its own declaring class. The helper
receives capsule/count and two five-element lists of native integer DataParameters;
registration and reads/writes use real EntityDataManager. There are no hard-coded wire
IDs, assumed contiguous IDs, custom serializers, registry lookups or rendering imports.
The helper defensively copies the key lists. Its new constructor requires exactly five
keys in each list; this validates the new key-group API, not RocketStruct runtime stage
counts. Native EntityDataManager remains responsible for duplicate key registration.

Outer metadata is STATE VARINT (default AWAITING ordinal 0), DRIVE ITEM_STACK (default
EMPTY), TIMER VARINT (default 0), plus twelve rocket VARINT values (all default 0).
Rocket values are capsule ID, count, then five packed pairs. Dummy PARENT_ID is a separate
VARINT key allocated for EntityRideableRocketDummy, initially 0. Existing inherited key
numbers are not copied or reserved. Proper allocation along the inheritance chain will
coexist with later superclass migrations.

Null drive input becomes ItemStack.EMPTY at the native boundary. getTarget and
getDestination turn an empty native stack back into null before existing ItemVOTVdrive
calls, retaining their null absence branches and destinationOverride short circuits.
The setter does not add a copy: existing same-reference mutation/dirty behavior remains.
Empty native stacks represent absence, rather than the old possibility of a non-null
zero-count legacy stack. Native stack serialization necessarily uses 1.12.2 semantics.

## Behavior deliberately preserved

- Writer updates capsule, then count, then first/second of each live stage. Shrinking
  does not clear unused trailing pairs. No stage count or packed count clamping.
- Six stages still fail only after capsule/count and all first five pairs were written.
  The precise failure changes from old missing-watcher access (normally null failure)
  to native key-list IndexOutOfBoundsException. No silent success, early prevalidation,
  rollback or new runtime-stage rejection was introduced. An excessive read similarly
  fails on the first unavailable pair. Negative stage count still results in zero loop
  iterations. These are unsupported-input behaviors, not repaired data.
- Packing remains `fuselageId << 16 | finsId` and
  `thrusterId << 16 | fuselageCount << 8 | thrusterCount`, without input masks.
  Upper fields are decoded with signed `>> 16`; fins and counts use unsigned low masks.
  Oversized/negative counts can contaminate adjacent fields. Zero packed counts decode
  to zero, unlike new RocketStage's counts of one. getStack still divides before clamping
  its result and can throw for zero thruster count.
- Capsule lookup fallback remains metadata-read-only, using ModItems.rp_capsule_20.
  satFreq and extraIssues remain excluded from metadata.
- setState resets watched timer and local stateTimer, in that order after ordinal update.
  Network metadata application does not invoke that setter. Invalid ordinals still fail
  by direct values-array indexing.
- RocketStruct NBT writes freq but never reads it; loaded satFreq stays zero. NBT counts
  alone retain their original minimum-one behavior; extraIssues is not persisted.
- ByteBuf counts remain signed bytes; ByteBuf carries extraIssues but not satFreq.
- Entity NBT reads timer then state, resetting the loaded timer to zero; navDrive load
  does not immediately synchronize DRIVE. Entity satFreq remains separate from struct
  satFreq. All original NBT and unrelated behavior are unchanged, not opportunistically
  repaired.

## Test evidence and exact limits

`probe-rocket-metadata.py` compiles the entire production RocketMetadata unchanged
against the actual pinned native target. It generates explicitly extracted caller
fixtures from the current entity: key declarations, RocketState, state/timer/drive
methods, initialization, and the dummy parent key/registration. A native fixture parent
adds its own BYTE key to test inherited allocation; it is not EntityMissileBaseNT.
Initialization only replaces the audited one-line RocketStruct setup delegate with the
actual complete RocketMetadata.register call. Dummy constructors/update/interaction are
not represented as complete production execution.

Native tests construct real Minecraft Entity and World classes, then use
EntityDataManager and SPacketEntityMetadata writePacketData/readPacketData with
PacketBuffer and apply decoded entries to a receiving native manager. They check full
and dirty snapshots, unique keys with native/inherited entries, signed integer extremes,
all five pairs, zero defaults, same-value dirtiness, stale slots after shrink/regrowth,
partial writes before six-stage failure, state timer resets, stack count/damage/NBT,
serializer copies, alias mutation behavior, and dummy IDs. Both logical sides run.

Packing tests extract actual arithmetic expressions, replacing domain lookup results
with integer inputs. They are expression fixtures, not complete RocketStage execution
or MissilePart registry/fallback tests. Raw stage-loop tests explicitly reproduce the
source-audited writer order; they do not claim execution of RocketStruct itself.

Exact whole-file reverse-source SHA-256 checks cover both existing modified classes,
including imports, comments and ordering, against checkpoint10/11. A separate whole
class-body comparison with the pristine original permits only enumerated prior symbol
renames and this metadata mapping. Thus untouched NBT anomalies and other behavior are
source-verified, not claimed as executed NBT tests. The helper is production-complete;
RocketStruct, EntityRideableRocket and its dummy are not compiled as complete classes.
No fake domain/Minecraft/Forge implementations, Unsafe allocation, Gradle source
exclusions, client startup, GL execution, actual chunk tickets, live tracking, gameplay
or runnable-mod claim is made.

## Remaining inheritance and initialization blockers

EntityThrowableNT still uses old BYTE watcher 2 (default 0; int-to-byte setter and
signed-byte getter). EntityThrowableInterp adds no watcher. EntityMissileBaseNT still
registers BYTE watcher 3 (default 5; no other direct watcher uses found) and requests a
ForgeChunkManager ticket from MainRegistry.instance during entityInit. These unchanged
superclass dependencies prevent honest reuse of the complete-EMP headless-entity claim.
EntityRideableRocket and dependencies also retain unrelated removed APIs: worldObj,
legacy mounting/interaction/movement and ItemStack fields/load APIs. RocketStruct still
uses stackSize in addStage. Those remain separate migration work.

MissilePart class initialization itself only constructs its parts map. Merely importing
ResourceManager or referencing MissilePart types does not initialize ResourceManager.
However, registerAllParts accesses ResourceManager's eager model fields and constructs
RenderDropPod. registerAllPartsServer avoids those render resources but still touches
extensive ModItems state. Narrow registerPart overloads avoid ResourceManager, yet require
ItemCustomMissilePart whose constructor touches MainRegistry.missileTab. Native part IDs
come from Item's registry and the map is keyed by item.hashCode; getPart(id <= 0) returns
null. Avoid claiming these domain initialization paths were covered by integer tests.

## Reproduction, portability and historical audit bridge

Use the prepared target Java 8/classpath directly:

    python porting/tests/probe-rocket-metadata.py --java-home <jdk8> --classpath <target-classpath>

Or parent-controlled Gradle init task:

    gradlew -I porting/rocket-metadata-probe.init.gradle testPortRocketMetadata -PportPython=python

Source-only:

    python porting/tests/probe-rocket-metadata.py --source-only
    python porting/tests/check-rocket-metadata-source.py

Python uses pathlib, UTF-8, tempfile, subprocess argument arrays, os.pathsep and platform
Java executable suffixes. No local absolute toolchain paths are committed. CRLF input is
normalized for the exact baseline manifest, so checkout line endings do not affect it.
The pristine sibling is required by the broader original-body comparison, as with the
EMP harness; the standalone exact reverse audit needs no git/pristine checkout.

`check-rocket-metadata-source.py` exports fail-closed `reverse_source(rel, text)` for
historical audit composition. It validates exact replacement spans and complete original
SHA-256, returning only the audit view. Compose it before the historical dataWatcher and
world-receiver audits so the dummy's unchanged worldObj receiver line is seen in its
original form. It never changes source or regenerates historical hashes.

# Maskman casual approach: complete native AI slice

Based on coordinate checkpoint `8098f2258b5880cf948009c5e9d766f6b2f536ba`.
Only production `EntityAIMaskmanCasualApproach` is changed. No caller, other AI,
PathFinderUtils, source set, compatibility shim or original/pristine source is changed.

## Native API changes and preserved behavior

- `PathEntity` becomes `Path`; `continueExecuting` becomes the actual native
  `shouldContinueExecuting` override. The five AI lifecycle methods have override
  annotations so an accidental old callback name cannot silently compile.
- The class's own `World worldObj` remains. Its owner value comes from
  `owner.getEntityWorld()`, rather than the removed inherited owner field.
- Native bounding-box getter, `PathPoint.x/y/z`, and `clearPath()` replace old APIs.
- Home checking passes explicit `MathHelper.floor` results for all three coordinates
  into a native `BlockPos` and `isWithinHomeDistanceFromPosition`.
- Constructors, class filter, mutex, counters, expression evaluation order, RNG
  calls, approach formula and the exact disabled attack comment are preserved.
  The pre-existing `Vec3d` migration is retained without formula changes.
- Deliberately preserve legacy anomalies: cached positive timer can return true
  after a null path; the old failed-path penalty contributes to this tick's timer
  before the penalty is changed; approach Y adds the normalized Y component without
  multiplying by range. No new attack behavior or null-target update guard is added.

Native target signatures were inspected with pinned Java 8 `javap`. In particular,
`PathNavigate.getPathToXYZ` is final and converts to `BlockPos`, then invokes
`getPathToPos`; fixtures intercept the latter instead of inventing an overridable
XYZ method. Native home distance excludes the exact radius boundary.

## Evidence and limits

`MaskmanApproachMinecraftTest.java` compiles alongside the **entire production
class**, against the actual target compile classpath. It uses native World and
EntityCreature subclasses, native Path/PathPoint, a PathNavigateGround subclass,
actual look-helper updates, native path state/noPath/clearPath and native home
checking. The World is allocated without save/network startup, following the
existing target-AI fixture pattern. Controlled navigator and EntitySenses overrides
supply outcomes; they do not emulate or validate actual path-search or visibility
engines. No fake Minecraft/Forge classes are supplied.

The final direct Linux run passes **128 assertions** covering:

- target null/dead/type/subclass gates; initial path/timer behavior and cached result;
- path start/reset and native active/finished/null continuation;
- long-memory home checks, exact radius, and negative floors on all three axes;
- visibility/memory/timer/movement/random short-circuits and strict thresholds;
- exact RNG call order and seeded state after initial search, refresh and approach;
- null/empty/near/exactly-one-away endpoint penalties, old-penalty ordering and a
  second refresh consuming then clearing an accumulated penalty;
- squared-distance thresholds at 256/1024, failed movement and bounding-box minY;
- zero/near-zero/near/10/20/far/vertical/mixed-sign approach vectors;
- native look-helper activity, attack-counter saturation and no attack invocation.

The independent scalar approach expectation uses the target Vec3d bytecode's
float-rounded square root, rather than calling the production method or Vec3d.
The suite is bounded; it does not perform redundant randomized sweeps.

`probe-maskman-approach.py` additionally reconstructs the complete pre-slice class
and compares its SHA-256, then verifies the forward mapping roundtrip. This guards
all original source and comments, not a selection of method fragments. It needs
neither a git checkout nor a pristine sibling to run. Its exported
`reverse_source(rel, text)` is a fail-closed audit-only bridge for prior migration
audits and leaves unrelated paths unchanged. It never supplies compilation code.

Logs:

- `porting/continuation-2026-10-19/maskman-approach-native-checks.log`
- `porting/continuation-2026-10-19/maskman-approach-target-api.log`

The warnings are the test's isolated-world Unsafe allocation and the preserved
production raw Class API. This is **Linux-tested only**, not Windows execution,
full-mod compilation, loaded-world navigation, gameplay or a distributable mod.
The coordinated full build remains separate from this isolated class probe.

## Portable commands

Windows PowerShell, using the project's normal JDK 8 setup:

    .\gradlew.bat --offline -I porting/maskman-approach-probe.init.gradle testPortMaskmanApproach -PportPython=python

`portPython` is a Python 3 executable path, not a shell command with arguments.
The init script adds only the isolated task and does not alter production source
sets. The worker did not invoke Gradle; the parent owns coordinated Gradle runs.

Standalone (supply the actual target compile classpath for the current platform):

    python porting/tests/probe-maskman-approach.py --java-home JDK8_PATH --classpath TARGET_CLASSPATH

Source-only:

    python porting/tests/probe-maskman-approach.py --source-only

The runner uses pathlib, subprocess argument arrays, platform executable suffixes,
os.pathsep and a temporary output directory. No Bash or pinned Linux path is
required in the shipped runner. Windows classpaths use semicolons; Linux uses colons.

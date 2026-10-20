# Native Entity bounding-box reads, bounded CP54 candidate

Base CP52 e0d4369. Exactly 184 compiler-attributed reads in 57 production files
change native Entity.boundingBox to getEntityBoundingBox(). No write is migrated.
Native Entity's getter and setter bytecode are exactly field load/store; the
original SRG field sa.C / field_70121_D is public final, unlike native private
mutable boundingBox. Existing getBoundingBox/getCollisionBox domain callbacks are
not renamed or removed. StructureBoundingBox and particle-owned fields remain.

The production declaration tree and all 67 resolved dependency JARs contain no
Entity subclass accessor overrides. The scanner rejects field shadows, unknown
receivers, assignment/compound/write expressions and accessor overrides, including
base-typed receivers that can reach a declared overriding subclass. A deliberate
runtime overriding subclass proves why a field-to-virtual-call rewrite is NOT
universally equivalent. Unknown external mod subclasses are outside this evidence;
this migration uses the supported native accessor contract, not reflective access
or a claim that arbitrary overriding mods have unchanged behavior.

Reproduce from the module root:

    python3 porting/tests/entity-bounds-native/run.py \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath-file /tmp/nagaport-ae2-native-classpath.txt \
      --legacy-client /tmp/nagaport-legacy-client.jar \
      --legacy-srg /tmp/nagaport1710-srg.zip --verify-attribution

Use your local equivalent paths; no downloaded dependencies or platform stubs.
Results: 57 exact inverses and 171 mutation negative controls; 26 real javac
ownership/read-only/shadow/Unicode/override fixtures; 17 actual native/original
field, exact native accessor bytecode, identity/evaluation/exception, extracted
production callback and override-negative controls; 5 actual original binary
read/callback/alias/null/receiver-exception oracle checks. Complete source
re-attribution independently reproduces the finite 184 edits. Compilation-unit
attribution errors are expected in unfinished unrelated source and are explicitly
reported, not suppressed or counted as a successful whole-source compilation.

The production callbacks exercised are exact current EntityBoatRubber
getCollisionBox/getBoundingBox bodies, compiled in a genuine EntityItem subclass
fixture. This proves these extracted bodies, not the complete boat lifecycle.
Original oracle executes real original Entity/AxisAlignedBB classes. A test-only
container omits signature metadata only, leaving every original class byte intact.
Unsafe/reflection are test-only, never production migration substitutes.

No AABB mutation, arithmetic, grow/expand/copy semantics, complete entity class
compilation or game/world behavior is certified by this bounded read migration.

## Fragment merge after overlapping unrelated edits

Do not copy whole production files from the worktree. Copy this test directory,
re-run EntityBoundsScanner against the destination's current Java source tree,
then pass the absolute-path TSV output to migrate.py. It requires the same exact
file/receiver/replacement multiset as the approved manifest and applies only newly
attributed UTF16 token spans. It re-records before/after hashes and offsets against
the merged tree so the inverse remains exact without undoing other workers' edits.
The scanner must run before migration; a second migration intentionally fails.

    J=/workspace/shared/nagaport-toolchain/java8
    mkdir -p /tmp/entity-bounds-merge
    "$J/bin/javac" -cp "$J/lib/tools.jar" -d /tmp/entity-bounds-merge \
      porting/tests/entity-bounds-native/EntityBoundsScanner.java
    find "$PWD/src/main/java" -name '*.java' | sort > /tmp/entity-bounds-merge/sources
    "$J/bin/java" -Xmx3g -cp "/tmp/entity-bounds-merge:$J/lib/tools.jar" \
      EntityBoundsScanner /tmp/entity-bounds-merge/sources \
      /tmp/nagaport-ae2-native-classpath.txt > /tmp/entity-bounds-merge/accesses.tsv
    python3 porting/tests/entity-bounds-native/migrate.py /tmp/entity-bounds-merge/accesses.tsv

Re-run the proof above after integration. If another migration changes the owned
receiver inventory, stop and review rather than broadening or bypassing the gate.

Run python3 porting/tests/entity-bounds-native/test_merge.py for the additional 57-file unrelated-edit preservation and changed-inventory rejection checks. Original CRLF is deliberately retained; git diff --check flags two existing CRLF-terminated changed lines, not introduced trailing spaces.

Integrated on CP53 5663b44d12fe9cad50d7d1a10496e55d32eabc9d: fresh native javac attribution reproduced the same184 receiver/file replacements. Before hashes were independently verified against CP53 git blobs; only owned field tokens changed. Original CP52 candidate proof remains the provenance, not the current source baseline.

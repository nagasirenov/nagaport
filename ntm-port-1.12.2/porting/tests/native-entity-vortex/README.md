# Inherited BlackHole/Vortex native synchronization

Exact baseline: c22f7048a82784fe0e8246d38aef102c43f1df70. Independently applicable after CP61; no overlap with the previous CP60 scalar batch subsequently included in CP62.

## Finite production scope

EntityBlackHole plus EntityVortex, EntityRagingVortex and EntityQuasar; RenderBlackHole and RenderQuasar. Six files, 16 original watcher operations replaced. One protected static final DataParameter<Float>, BLACK_HOLE_SIZE, allocated exactly once by EntityDataManager.createKey(EntityBlackHole.class, DataSerializers.FLOAT). Original slot16/default0.5F registers through base entityInit on every descendant. No descendant shadow key, separate registration, fake numeric adapter or platform class. Both server/client initialization paths are identical. Vanilla Entity has six slots; this base key is allocated at native index6. Wire protocol compatibility with legacy clients is not intended.

The existing size-taking constructors set this shared entry without clamps. New public getBlackHoleSize returns its native value. Both renderer numeric consumers now cast their Entity receiver to EntityBlackHole and call that getter. Existing registration routes prove this domain: BlackHole/Vortex/RagingVortex use RenderBlackHole, Quasar uses RenderQuasar. All derive from the actual base. No rendering geometry, timing, resource, GL or registration code changes.

consumer-closure.json records exact old float-slot16 getter locations, registration source lines, and zero declared DataWatcher-type aliases or nonnumeric float slot arguments throughout the production Java tree. audit.py independently checks descendant closure and the global literal float-slot16 consumers. All such consumers are in these six files and disappear after migration; unrelated integer/byte slot16 owners are deliberately untouched.

## Preserved gameplay and NBT

All gravity/attraction, damage, block interaction and size-change formulas are byte-for-byte conserved by inverse audit. Vortex subtracts shrinkRate (default0.0025F), retains negative size, then dies when size<=0. RagingVortex's original timer anomaly (`if(timer <= 20) timer -= 20`) is retained, not “fixed”; exact pre-base-update prefix tests verify -19/-38/-57 progression and pulse math. The random explosion branch is source-preserved but not executed by the headless prefix test. No gameplay limits or new mechanics.

EntityBlackHole explicitly persists size as Float and breaksBlocks as Boolean under unchanged tags. Missing tags remain size0/false, distinct from fresh registration0.5/fieldtrue. Vortex invokes actual base NBT and persists shrinkRate; RagingVortex invokes base NBT and persists vortexTimer; Quasar inherits base NBT directly. Native data manager does not persist automatically. noBreak still changes the ordinary unsynchronized breaksBlocks field only.

Tests use actual CompressedStreamTools binary NBT write/read in addition to exact production NBT methods. Binary NBT uses vanilla float handling, so NaN values are compared canonically after binary reload; metadata packet testing separately confirms raw IEEE NaN payload preservation. Integer/Float immutable snapshots are safe here. Unrelated ItemStack use in attraction gameplay is not a synchronized key and is not altered.

## Evidence and limitations

source-manifest.json records pristine/CP61/after hashes, class/superclass, receiver/static type, true declaring owner, original slot/default/type, full before/after statements with independently checked lines and enclosing methods. audit.py inversely reconstructs every production file byte-for-byte from CP61 after removing only the new imports/key/getter and reversing exact call changes. Original synchronization statements match ntm-original-pristine, including both renderer reads.

probe.py extracts exact actual base/descendant synchronization methods, constructors and NBT into a real Entity-based hierarchy. It does not replace the custom parent with a fake stand-in. The heavyweight base onUpdate is omitted from the bounded harness, never called or substituted; the Vortex/Raging prefix ends exactly before super.onUpdate and is explicitly renamed advanceSizeBeforeBaseUpdate in the test fixture. Both production renderer-read statements are compiled and executed separately, not the GL renderer. No production source exclusions or fake platform classes are introduced.

Two independent JVM runs (base-first and descendant-first initialization) each PASS409 assertions, 818 total. Coverage: actual SPacketEntityMetadata serialization/read/application, default/full/delta, dirty/equality/reset and force-dirty characterization, unique inherited vanilla key allocation, floating edge values and NaN/signed-zero semantics, immutable snapshots, actual size constructors, explicit binary NBT, typed renderer expressions, and bounded shrink/pulse prefixes. Registration starts at0.5F even in descendants and no mutable serializer types are included.

This does NOT prove full entity/renderer/mod compilation, world simulation, GPU output, client/server game startup or explosion behavior. Existing old world/block APIs, AABB, brightness, renderer constructor/GL APIs and unrelated spawn calls remain. No spawn statements or boss behavior were edited. Full compilation belongs to coordinator integration.

Reproduce from project root:
python3 porting/tests/native-entity-vortex/audit.py /workspace/shared/nagaport/ntm-original-pristine
JAVA_HOME=/workspace/shared/nagaport-toolchain/java8 python3 porting/tests/native-entity-vortex/probe.py

Classpath defaults to /tmp/nagaport-ae2-native-classpath.txt (override NAGAPORT_NATIVE_CP_FILE). No Gradle, downloads or new dependencies.

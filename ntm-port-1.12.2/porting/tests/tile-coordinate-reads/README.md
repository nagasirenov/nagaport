# Native TileEntity coordinate reads, CP55 bounded root

Baseline CP54 `0732d318`. Exactly 943 compiler-attributed reads in 136 production files: 321 X, 301 Y, 321 Z. Explicit receivers keep the receiver expression once; bare inherited fields use `this.getPos()`. Only field-name UTF16 spans change. Comments, strings, imports, local coordinates, private domain `pos` fields, all original line endings and every unrelated byte remain unchanged. No getter/world changes, writes, compound assignments, increments, formulas or balance changes.

The initially requested Vec3d field root does not exist: the separate vector audit finds zero such native-owner reads across all 4,048 CP53 sources. No vector edits were made. The coordinate diagnostic names belonged to TileEntity. This corrected tile scope was explicitly approved before implementation.

## Ownership and exclusions

Actual Java8 javac attribution resolves each receiver's native TileEntity ancestry and the native public zero-argument `getPos(): BlockPos` declaration. Any legacy-coordinate field in the ancestry, interface shadow, source override, static access, error/type-variable receiver or unresolved lexical binding is excluded. Bare names require their nearest lexical class to be a TileEntity descendant; nested outer-instance fields are not guessed. Assignments/compounds/increments, including parenthesized left-hand sides, are excluded. Source closure includes 454 native TileEntity descendants with no `getPos` override. The dependency class-file audit reads the actual cached classpath, including AE2 and OpenComputers, and rejects any external descendant override. Tests additionally execute reflection checks on 27 native tile types. New third-party overrides introduced after this frozen dependency/source closure are not covered and must be reviewed.

Private domain `pos` fields in RBMK classes deliberately remain unchanged. The inherited getter returns the native base field rather than accidentally binding to the hidden domain field. No direct replacement with `this.pos` is used in this batch. `getWorldObj` was deliberately left untouched because its dispatch family has different override considerations.

## Evidence

Run from this directory (paths may be elsewhere):

    python3 run.py --java-home /path/to/java8 --classpath-file /path/to/native-classpath.txt --legacy-client /path/to/original-client.jar --verify-attribution
    python3 dependency_closure.py /path/to/native-classpath.txt
    python3 check_source.py

The runner checks the original client SHA256. Retained original `aor` and native TileEntity bytecode shows public legacy coordinate fields versus native BlockPos storage, native getPos returning the exact base field, constructor origin, setPos immutable-copy behavior and actual NBT serialization.

7,380 runtime assertions use the real old client classes and real mapped Forge/native classes. Coverage includes initial origin, negative/extreme integer coordinates, native immutable-position identity, mutable-position detachment, private domain shadow preservation, actual old/native NBT write/read, invalidate/validate/unload lifecycle, native mutable world identity, one receiver evaluation, left-to-right evaluation and null short-circuiting. Two complete current production method bodies from TileEntityFusionKlystron are extracted unchanged and compiled/executed: getConPos (all metadata directions, signed overflow, directions) and hasPermission (distance boundary, NaN, infinity), the latter compared with actual original Entity distance bytecode. Five complete supporting production types are compiled, not platform stubs.

20 compiler fixtures include positives and adversarial ownership/write/shadow/interface/nested/static/Unicode controls. The full 943-call source set is re-attributed from its exact inverse. The source guard verifies all 136 exact inverses and rejects 943 token mutations plus 136 whole-file drifts. It deliberately fails if another migration changes selected files, until the parent composes exact inverses. Historical manifests must not be regenerated.

These are bounded API/read and extracted-method proofs, not compilation or execution of the 136 complete consumers, network topology, complete fusion/RBMK gameplay, client/server startup or saved-world integration. No Gradle task, dependency download, platform stub, source exclusion or push was used. Existing mixed indentation warnings are byte-preserved rather than opportunistically reformatted.

## Integration

Only production token changes and this evidence are owned. The parent owns overlapping CP55 AABB/held-stack edits, the integrated source guard and full compile. Apply the production patch when possible; otherwise compose exact token edits against CP54, preserving all independent fragments. `check_source.reverse_source(relative,text,manifest)` provides the isolated exact inverse; parent receipt/inverse must first remove unrelated newer changes. Never overwrite a whole overlapping file with this worktree copy.

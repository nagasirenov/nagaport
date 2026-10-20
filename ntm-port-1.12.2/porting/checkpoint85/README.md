# Checkpoint 85: registered writes, renderer lifecycle, immutable events and bounded names

Baseline CP84 502f0b9dc3cd3e14622edd897fe3c753e309818d. Fresh full offline compilation **FAILS with 11,940 diagnostics**, 126 fewer than CP84. All 4,065 generated compiler inputs match the frozen source hashes. Changes cover 65 Java files and one byte-identical case-only texture rename. No runnable JAR, complete client/server startup, full machinery or gameplay acceptance is claimed.

## Integrated production and meaningful checks

### 72 registered state writes

32 callers now use the existing native WorldBlockState.write boundary. Forty-two four-argument statements preserve metadata 0/flags 3; thirty six-argument statements preserve their exact metadata expressions. Flags remain 3 at 65 sites and 2 at seven. The scope includes 24 newly supported direct BlockMulti targets, one proven crashed-bomb alias, one sand self-placement and 46 exact registered metadata-zero owners. Original receiver/coordinate/block/metadata/flag evaluation order, bounds, null-receiver timing, branching and ignored results are preserved.

38,495 assertions execute actual original/native World/Chunk storage, update/neighbor observations and original/native falling-block on-added schedule requests. Complete Generic/NoDrop/CT/BlockEnumMulti representatives and native BlockFalling are exercised; 16 verbatim registry construction projections do not imply complete registry startup. Five conversion-bytecode audits, 15 generated-template checks, eight directly compiled original evaluation cases and typed/assignment/codec/capture/call-deletion controls pass. The final attribution used the pristine native classpath; an earlier fixture-contaminated attribution was not used for the sealed result.

State/storage proof is not whole owner gameplay. Sand falling, plant growth/support/paired halves, asbestos interactions and CrashedBomb tile/radiation/explosion lifecycle remain unverified and retain their original source. Native observer/debug-world/lighting/snapshot behavior remains ordinary platform integration. Null pre-initialization/custom registry replacement is not certified. Raw metadata outside the canonical nibble domain is not silently masked. Two mutable OreCave metadata writes and four distinct Chunk absorber writes remain excluded. See tests/registered-state-writes.

### Native renderer manager and typed override family

All 19 direct Render constructor roots, required Quasar inheritance closure, three chicken constructors and two generic erasure clashes are migrated. Twenty-four live entity/renderer registrations keep their exact original pairing and order; unregistered RenderBalls/RenderQuackos are not added. Factories use their callback manager during native RenderManager construction, not a stale/unassigned Minecraft global. Chicken subclasses restore caller model identity and shadow radius after the native fixed-default constructor; Quacc's original 25× transform is preserved without boss gameplay/HUD changes.

232 native checks include real manager instances, original model/mesh initialization, seven complete renderer owners, GPU primitives, typed chicken bridge phases, ownership and three rejecting mutants. Sixteen other owners have explicitly bounded constructor/field projections and unchanged draw-body source proofs, not complete rendering acceptance. Three pre-existing unused/fallback texture literals remain documented unresolved; no substitute assets were invented. See tests/entity-renderers-native.

The live chopper texture is renamed from assets/hbm/textures/models/chopperBomb.png to chopperbomb.png. Bytes and SHA-256 are identical. Actual native FileResourcePack tests demonstrate the original mixed-case ZIP lookup failure and corrected lowercase PNG lookup, followed by the actual textured draw. Both original code references are retained. Verify the Git/JAR spelling on Windows; if a patch application leaves old casing, follow the evidence README's verified-unused temporary two-step rename, never overwrite another file. This is not a blanket resource rewrite.

### 27 immutable render-event accessor reads

Six files use genuine getters for RenderPlayerEvent renderer/partialRenderTick, RenderHandEvent partialTicks and RenderItemInFrameEvent item. All formulas, casts, receiver evaluation, GL calls and cancellation behavior remain unchanged. 916 assertions execute actual native event constructors and selected reads, float bit/reference/null preservation, frame-item snapshot identity and exception order. Four immutable original Forge fields and exact native direct-field getter bytecode are checked. All 27 reversed private-field reads fail compilation; a wrong-value getter mutant, ten scope fixtures and 27 distinct source-position mutations are rejected.

These tests do not certify full handlers or actual deprecated SetArmorModel event posting. Test-only renderer identity allocation and EGL initialization are not a game/render scene. Unknown external virtual getter overrides are explicitly outside the field/getter equivalence claim. No production reflection, event wrapper or ItemStack.EMPTY normalization is introduced. The older recovery README is marked superseded by actual CP77 revalidation, not rewritten as historical success. See tests/native-render-event-extension.

### Three proven name-dispatch changes

Only the guarded player projectile-owner name, the player-registered targeting predicate and EntityDummy's unchanged health-status override migrate. Sixteen actual original-player and 194 native assertions, 41 bytecode checks, exact inverses and 12 drift controls pass. Thirty-two broader calls and both minecart declarations remain untouched: native generic Entity naming honors a custom-name channel that original Entity lacked. EntityRailCarCargo's existing inventory/entity-name dispatch collision is recorded, not silently fixed. Whole owner gameplay remains unverified. See tests/entity-name-native.

## Combined verification and continuation

The source guard proves 65 exact CP84 inverses, 195 rejected mutations and two pinned resource-path states before the historical guard chain. Individual source attribution reductions are 72 + 24 + 27 + 3 = 126; the actual full compile independently confirms this result. Fresh terminal log, input manifest and diagnostics accompany this checkpoint. No source exclusions, gameplay stubs, invented metadata default or unrelated dependency change is used.

From ntm-port-1.12.2:

    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
    python3 porting/tests/check-thermal-family-source.py

The first build was interrupted by an outdated execution-permission premise, before any compiler result. Its log is retained. The already-granted official bootstrap permissions were reconciled, the missing process confirmed, and the same offline command restarted. The terminal run is the count authority. CURRENT.md supersedes historical pending-bootstrap statements without removing old evidence. ModMaven/synthetic lookup restrictions and separate unapproved potion policy remain unchanged.

CP84 was durably preserved before this work. Published main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570, no new push. The cumulative archive replaces earlier recovery patches and reproduces an exact tree from that baseline; use a clean backed-up checkout and git apply --check, stopping on conflicts or CRLF refusal. Windows execution remains untested. Full inventory, energy, radiation, machine, save, server/client and many compiler families remain unfinished. Continue from actual latest diagnostics, not historical small declaration counts or earlier unexecuted plans.

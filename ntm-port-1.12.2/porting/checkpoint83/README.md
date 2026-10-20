# Checkpoint 83: owned states, armor icons, geometry and native entity data

Baseline: CP82 df18fc077648dd18e6d0c1055d82c11dc2ebdb78. This integrated snapshot changes 18 production Java files. Fresh full offline compilation completed with **12,169 diagnostics**, versus 12,359 at CP82 (190 fewer). All 4,065 generated inputs are byte-identical to source. The historical source guard passes, including 18 exact CP82 inverses and 54 drift controls. Whole-mod compilation still FAILS: no runnable JAR, client/server startup or game readiness claim.

## Production and actual checks

### Armor inventory texture declarations

105 attributed armor declarations and the ArmorFSB constructor-time default use the existing native texture registry. Material, armor slot, statistics, gameplay, worn textures and worn models are untouched. The independent suite passes 3,077 native checks and 1,576 actual original-bytecode checks, with 29-class ownership closure and 98 pristine PNGs. Complete armor gameplay classes do not compile/run in this harness: genuine native ItemArmor receiver contracts and exact constructor-tail/declaration projections are explicitly bounded. Twenty-one naming-error chains, beta and no9 remain excluded from this finite migration.

### Correction to the earlier plain-item model provider

The old native provider registered metadata0 only for non-subtyped items. Actual native ItemModelMesher preserves nonzero metadata when maxDamage is zero, so these stacks got a missing model even though original default Item icons ignored metadata. Eleven already-migrated ItemDrop declarations are confirmed affected; the defect is not ItemDrop-specific.

For already-eligible NTM-owned explicit default-icon declarations without a custom mesh/model route, the handler now always installs the existing fixed-location native mesh definition. It does not mutate item metadata/counts, change vanilla or foreign item routes, replace unknown custom ownership, or overwrite a resource-pack model. Native real-mesher metadata matrix, original Item bytecode, negative CP82 control, late registration, custom ownership, wrappers and reload checks pass. The preexisting foundation suite also passes. See tests/plain-item-metadata-native for exact cases and the strict combined armor dependency receipt. This is a real regression correction, not diagnostic-count optimization; it does not prove in-game visuals.

### Owned block states and writes

57 original writes in BlockKeyhole, BlockRedBrickKeyhole and Antenna use existing native-state mutation after explicit original-order captures and bounds/receiver checks. Exact assignments resolve to RedBrick and the two audited steel-pole owners. RedBrick now has a genuine 16-state metadata property, preserving side==metadata icon selection and metadata6–15 all-base behavior. Its placement preserves the original corner/height orientation formula rather than native piston thresholds. Original no-drop behavior and icon assets remain unchanged. Full icon-render integration remains separately unfinished; declaring the native state does not claim a completed visual route.

Complete native owner/storage proofs compare actual supplied original mod owner classfiles and original World/Chunk behavior, with pinned class/JAR provenance. Source/order/mutation controls and exact typed attribution pass. Native observer, debug-world and lighting behavior remains ordinary 1.12 platform integration, not a claim of identical 1.7 engine internals. The 36,416 passing assertions and 67 mutation controls cover initialized, nonnull owned targets. An explicit original-null probe shows that an in-domain null target can mutate chunk bookkeeping before throwing, whereas native conversion throws before those effects. Pre-initialization/custom-null public fields, unknown external replacements and broader custom block semantics are therefore outside this equivalence claim; normal registered owners remain covered. See tests/world-owned-write-native for exact scope and null/assignment boundaries.

### Static geometry

BlockCrop, BlockMush, BlockFallout and MeltedFlesh retain their constructor float expressions in immutable native AABBs. Native selection/collision callbacks preserve plant null collision. 15,666 original/native assertions cover real World/Chunk collision enumeration and ray traversal; five behavioral mutants and twelve source controls fail as intended. Direct geometry comparisons are bit-exact; native World DDA point comparisons disclose a bounded ULP tolerance. No descendants were found in the complete 4,065-source census. Callback-only mutable-bound owners were excluded, not flattened. Rendering remains untouched and independently unported.

### Projectile/utility data keys

Seven files replace 16 original accesses with typed BYTE/VARINT/FLOAT keys and typed external consumers. 3,646 native assertions pass in each opposite loading order (7,292 total), alongside exact file inverses, pristine-call comparison and 30-file consumer closure. Chemical retains its omitted-super initialization; FallingNuke's byte remains orientation including NukeCustom/renderer consumers; NukeBoy uses metadata-only Torex scale assignment without changing simulation dimensions. Complete FallingNuke compiles; other owners use clearly bounded exact-source execution. Unrelated physics/behavior errors remain unported.

## Reproduce and continue

From ntm-port-1.12.2:

    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
    python3 porting/tests/check-thermal-family-source.py

Exact full-compile.log, diagnostics.json, source-inputs.json and source-guards.log accompany this snapshot. Family READMEs contain bounded runtime commands, hashes and limitations. No source exclusions, dependency downloads, platform stubs, metadata normalization or network bypass were introduced. The model fix uses the prior strict route-ownership checks unchanged.

CP82 was durably preserved before this work (recovery Library version10). Remote main is still published CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570; no new push. The cumulative archive replaces earlier recovery patches and includes local history. Use a fresh exact-baseline checkout, back up, run git apply --check first, and stop on conflicts/CRLF refusal. No force/reset/automatic normalization. Windows instructions are supplied; Windows execution is untested.

The separate potion registry candidate remains unapproved and isolated; legacy config/JSON/save migration policy is unresolved. Sound/AT changes remain parked. Future state work must audit BlockBase/BlockMulti descendants before adding a shared metadata contract. Native inventory-empty/quantity, machine/energy network, radiation, client/server/save and other compiler families remain unfinished. Existing user-approved visual gates remain narrow; no blanket rendering or mechanics simplification is implied.

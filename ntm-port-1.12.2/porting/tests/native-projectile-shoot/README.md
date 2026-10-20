# Native owned projectile shoot callback

Baseline: CP87 `c2f60149e9a9fb836bade505fe566d40a54ef3cc`.

## Finite production change

Exactly 33 identifier substitutions in 16 files: seven declarations and 26 locally resolved calls rename `setThrowableHeading(DDDFF)` to native `shoot(DDDFF)`. No callback body, argument, expression order, constructor, field, annotation, helper, superclass, or other byte changes. No legacy forwarding alias or compatibility layer is introduced.

The roots are `EntityThrowableNT`, `EntityGrenadeBouncyBase`, `EntityBullet`, `EntityChopperMine`, `EntityRocket`, `EntityExplosiveBeam`, and `EntitySchrab`. Javac attribution finds 90 classes in their complete local inheritance closure: 65 top-level classes and 25 nested missile classes. All seven roots directly extend real native Entity. There are no pre-existing owned `shoot` declarations, and no callback overrides below these roots. `headingForceMult` remains virtual; its two existing overrides in BulletBaseNT and BulletBaseMK4 still return 1D. Derived MK4CL and CombineBallNT inherit those overrides unchanged.

Bodies are materially different and intentionally retained:

- ThrowableNT uses 0.0075D through three virtual helper calls, interleaved with Gaussian draws; it updates yaw, pitch, and previous rotations and resets ticksInGround.
- Bullet, Rocket and Schrab retain their three Gaussian/Boolean alternating draws and the original 0.007499999832361937D scale.
- ExplosiveBeam retains the same signed RNG pattern with its distinct 0.002499999832361937D scale.
- BouncyBase retains Gaussian-only draws, yaw/previous-yaw updates only, and does not reset pitch or ticksInGround.
- ChopperMine's callback was already an empty method in the pristine source. Its deliberate no-op, including no RNG use, is preserved. This patch does not add a stub.

All seven complete baseline callbacks exactly match the pristine 1.7.10 callbacks after only the already-established MathHelper.sqrt_double-to-sqrt symbol mapping. That helper mapping is checked against actual old/native bytecode, both of which call Math.sqrt and cast to float.

## Exclusions and engine contract

GlyphidBrawler's unrelated same-name local method and caller are unchanged. Bullet's separate `setThrowableHeading2` declaration and call are unchanged.

SiegeCraft's sole `EntitySiegeLaser` call is also deliberately unchanged. That receiver derives from native EntityThrowable, not an owned root. Although the native callback name maps correctly, actual 1.7.10 and 1.12.2 engine implementations differ in rotation: the former uses Math.atan2 and division by float-pi converted to double; the latter uses MathHelper.atan2 and multiplication by a different degree constant. Migrating that call would require a separate behavior decision. No workaround or broad compatibility method is added here.

`engine-contract.javap.txt` records the real old `zh.c(DDDFF)`, native IProjectile.shoot descriptor, real engine dispenser invokeinterface instructions, both sqrt helpers, and the two EntityThrowable bodies. The original SRG mapping links zh.c to IProjectile.func_70186_c. Both complete Forge1614 patch archives are inspected and hash-pinned: neither patches IProjectile/EntityThrowable nor references func_70186_c. This is not a vanilla-only baseline assumption.

## Source and typed evidence

`source-manifest.json` contains before/after/pristine whole-file hashes and every UTF-16-positioned edit with attributed owner and receiver type. `production.patch` and `source-inverse.patch` are exact forward/reverse files. `check-source.py` reverses the full files and checks their original bytes, verifies the 90-class hierarchy, original bodies, caller/owner identity before and after, all exclusions, no overload/callback collisions, and the whole current-tree legacy-name census. Four deliberately altered source variants must fail.

`before-attribution.jsonl` and `after-attribution.jsonl` cover all 4,068 production source units, not a hand-selected caller list. Parsing succeeds and javac analysis resolves the callback declarations/calls, but unrelated incomplete-port diagnostics remain. This is explicitly analysis-only; it does not generate or certify complete NTM classes. `--attribute` reruns the final full-tree analysis and checks exact equality with the frozen result.

## Executed native boundary

`run.py` compiles and executes all seven complete production callbacks plus the three complete helper declarations against the actual installed native Entity, IProjectile and MathHelper classes. It builds an explicitly bounded 90-node inheritance model using the actual source superclass relationships. Synthetic test constructors and a small Entity-derived field boundary are used; complete source owners, entity metadata, actual constructors and their side effects are not compiled or executed.

The baseline comparison uses the complete pre-edit methods in separate test classes, without pretending the old method implements native IProjectile. The native side is always called through the real IProjectile interface. A negative compile separately proves that all seven exact old callback declarations fail specifically for missing native shoot(DDDFF).

41,494 runtime assertions cover:

- All 90 modeled classes resolve to their expected owned callback through genuine native interface dispatch, with no lingering old-name bridge.
- Seven seeds and thirteen vectors/options, two sequential calls, exact floating-point motion/rotation/reset results, RNG traces and post-call random state including Gaussian caching.
- Zero/signed-zero vectors, zero or negative speed/inaccuracy, NaN, infinity, subnormal and overflowing inputs; no new normalization/fallback.
- Independent scripted Gaussian/Boolean golden calculations distinguishing the seven bodies and two inherited custom multipliers.
- Once-only virtual callback dispatch, exactly three still-virtual helper invocations, and original Gaussian/helper interleaving.
- Six intentionally wrong variants are rejected: missing Boolean RNG, altered ThrowableNT multiplier, lost custom helper override, swapped rotation arguments, lost reset, and an extra Gaussian draw.

No native Minecraft/Forge class is stubbed or replaced. The model's omitted Entity lifecycle/NBT methods are explicit test boundaries. This establishes callback ABI and exact selected behavior, not successful whole-owner or whole-mod compilation, missile operation, flight/collision/payload effects, game startup, networking, or save/restart parity.

## Reproduce offline

From the worktree root:

    python3 ntm-port-1.12.2/porting/tests/native-projectile-shoot/check-source.py
    python3 ntm-port-1.12.2/porting/tests/native-projectile-shoot/run.py --attribute

Defaults use the authorized Java8 and native-classpath.txt in `/workspace/shared/nagaport-toolchain`, plus the installed `/tmp/nagaport-legacy-client.jar`, `/tmp/nagaport-forge1614-userdev.jar` and `/tmp/nagaport1710-srg.zip`. Each can be overridden by an explicit argument. The runner does not download, alter caches, run Gradle, or build the full mod.

The default whole-file guard describes this isolated family exactly and intentionally rejects concurrent unrelated edits. `run.py --composed-source` first requires the pinned CP88 aggregate source guard, then retains all sixteen exact family hashes, caller/inheritance checks and callback/helper slices. For explicitly untouched callback exclusions only, it reverses independently verified sibling edits before comparing to CP87 (SiegeCraft has separately migrated metadata). Actual runtime methods are extracted from current production, not substituted historical code. Isolated offset-based attribution still requires the isolated family tree. This mode never skips source validation.

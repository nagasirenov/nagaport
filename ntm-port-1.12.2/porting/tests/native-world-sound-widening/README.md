# Native coordinate sound: finite integer-coordinate extension

Exact incremental baseline: `641d2641bf196265e34e3f6968dba795f1dd061b`, the detached prior worker result consisting of CP59 `e72ec3d2ae8db6653ebaad529a574ee32c99260b` plus the already delivered dormant entity-transport production/evidence pair. This patch does not repeat that pair or alter its production.

## Activated scope

16 genuine `World.playSoundEffect` invocations across14 files become calls to the unchanged `WorldSoundEffects.play` bridge. Fourteen have `(int,int,int,String,float,float)` source arguments, two `(double,int,double,String,float,float)`. Java widens the same coordinate values to double in both original and native signatures. Volume and pitch already are float; no casts, expression changes, coordinate offsets, new event hook or entity-position inference is introduced.

The six names are already in the proven45-name registry/category allowlist: player.vomit, turret.jeremy_fire, turret.jeremy_reload, weapon.flamethrowerShoot, weapon.fstbmbPing and weapon.missileTakeOff, all under hbm. Full javac attribution inspected all243 remaining coordinate invocations. These16 are every remaining direct-literal existing-allowlist coordinate call, not an arbitrary sample. Unknown names, legacy vanilla aliases, dynamic names and the separately parked entity overload remain untouched.

Production is only16 exact invocation replacements. Existing helper, registry, resource-pack category resolver/bridge and dormant entity transport are hash-frozen. This retains the previously documented coordinate contract: live listeners, original raw callback values, exact built-in server packet pitch behavior, silent client callback and no new Forge entity event. It does not claim universal audible parity or make earlier custom-listener/API boundaries disappear.

## Proof

Independent BEFORE and AFTER JavacTask parse/analyze covers4,058 genuine baseline production units. Complete original receiver/argument/invocation ranges drive the rewrite, including UTF-16 source offsets; no regex receiver selector is used. All16 destinations bind to `com.hbm.sound.WorldSoundEffects.play(World,double,double,double,String,float,float)`, with the full original receiver as argument0 and all six original expressions/types unchanged. All14 complete source-file inverses equal baseline bytes. Exactly16 missing-method errors disappear (17,474→17,458); every other path/line/code/message diagnostic multiset is unchanged. This is scoped attribution, not a successful whole-mod compile.

The runtime probe invokes the actual cached1.7.10 World/WorldClient coordinate callback through its actual listener interface and the unchanged native bridge. Constructor/startup bypass uses Unsafe, not fake engine classes. It compares numeric bits, all six sound identities under the existing native normalization boundary, int-coordinate extremes, float volume/pitch, nested/member receiver evaluation, null receiver timing and exception order. Long/float coordinate and int/long volume cases are explicitly robustness-only, not extra migrated scope. Javap verifies identical pre-call instruction sequences and the expected three/one i2d instructions for the two migrated shapes.

Native callback, registry/category, packet and full-receiver regression probes are reused unchanged. No game, socket delivery or audible-audio claim is made.

## Historical96-call proof composition

The original96 AST evidence and call metadata remain immutable. Only EntityEffectHandler overlaps: its two new int-coordinate calls require explicit before/after whole-file hash composition, while its two prior sound invocations remain byte-identical. Two additional pins were already stale at the prior worker baseline due to identified CP59 non-sound changes: BlockCrashedBomb placement and TileEntityMachineMiningLaser item spawning. The existing manifest now records exact committed diffs and old/new inverse hashes for those two, not unexplained hash regeneration. The prior manifest is preserved in attribution/existing-world-sound-manifest.json.

The composed historical guard passes96 inverses and71 controls on this isolated baseline. It is NOT asserted to pass integrated CP61: later legitimate non-sound edits changed other whole files, which the coordinator's combined historical guard handles. This new16-call guard is independent and scoped to its14 files and frozen sound infrastructure. At delivery preparation, all14 selected pre-change source files matched integrated main exactly; that observation is not a blanket claim about all prior historical pins.

## Reproduction

From repository root:

    python3 ntm-port-1.12.2/porting/tests/native-world-sound-widening/check-source.py

Runtime and attribution runners accept explicit project/classpath/Java8 inputs and a fresh output directory. Generated logs/classes never overwrite preserved evidence. Their exact invocation and final counts are in their own notes. Large diagnostic files may be deterministic gzip with raw hashes retained. All patch generation uses native Git, and the final delivery receipt compares every intended path and byte after applying the actual patches to a separate sparse baseline.

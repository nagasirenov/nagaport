# Native particle migration assessment

Read-only assessment, 2026-10-19. Baseline checkpoint8 `1e5e9b634570632edb63f2e5ec590f449c855e23`; production compile remains blocked (1489 diagnostics in `continuation-2026-10-19/hfr-root-clean-checks.log`). No particle implementation or full-parity acceptance is recorded here.

## Evidence and provenance

- Current production sources: `src/main/java/com/hbm/particle/`, `main/ClientProxy.java`, `main/ModEventHandlerClient.java`, and `render/icon/`. Supplied original remains read-only at `../ntm-original-pristine`; see `continuation-2026-10-19/RESUME.md` for original-tree provenance and patch-chain identity caveats.
- Actual pinned target inspected with Java8 javap: `.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`, MCP stable_39. Particle, ParticleManager and IParticleFactory declarations and bytecode were inspected, not assumed from another version.
- Existing official 1.7.10 client oracle `/tmp/nagaport-legacy-client.jar`, SHA256 `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`; existing `/tmp/nagaport-mc-1.7.10.json` identifies the official client SHA1 `e80d9b3bf5085002218d4be59e668bac718abbc6`. Inspected bkm (EntityFX), sa (Entity), bkn (EffectRenderer), identified through their signatures and bytecode. This is vanilla binary evidence, not proof of every possible legacy Forge transformation.
- Temporary disassemblies: `/tmp/native-particle.txt`, `/tmp/native-particlemanager.txt`, `/tmp/legacy-particle.txt`, `/tmp/legacy-entity.txt`, `/tmp/legacy-particlemanager.txt`. They are scratch evidence, not portable dependencies. No download, Gradle run or production edit was performed for this assessment.

## Native contract

Particle no longer extends Entity. Constructors are protected `(World,double,double,double)` and public `(World,double,double,double,double,double,double)`. Callback is `renderParticle(BufferBuilder,Entity,float,float,float,float,float,float)` (camera entity, partial tick, five billboard rotations). Native operations include setExpired/isAlive, move, setPosition, get/setBoundingBox, setParticleTexture(TextureAtlasSprite), setMaxAge. World, motion, positions, age/color/scale and collision flags are protected. Entity identity, yaw/pitch, setVelocity, yOffset/ySize and Entity collision bookkeeping are absent.

Existing direct construction plus `ParticleManager.addEffect(Particle)` is supported; introducing IParticleFactory is unnecessary. Its native signature, if needed, is `createParticle(int,World,double,double,double,double,double,double,int...)`.

Native ordinary layers0–2 receive an already-open QUADS batch with `PARTICLE_POSITION_TEX_COLOR_LMAP`: append position, UV, color and lightmap on every vertex, without nested begin/draw. There is no normal channel. Layer1 uses the block atlas. Layer3 receives an unopened buffer and must retain real owned drawing and GL state behavior. The old manager supplied each particle's brightness before its render callback; native custom callbacks must explicitly emit it.

## Preservable mod behavior versus engine differences

Preservable: original update ordering, lifetime boundaries, color/scale formulas, velocity formulas, UV selection, billboard geometry, fixed brightness values, custom texture selection, primitive modes, blend/depth/fog state and actual draw ownership. Preserve quirks rather than silently correcting formulas. Rotating particles use degrees and explicit Rodrigues geometry, not a direct substitution of native particleAngle.

Engine differences requiring explicit treatment, not a global EntityFX rename:

1. Original FX constructor sets yOffset=height/2 and centers its Y bounding box. Native Particle uses minY=posY. Original initializes lastTickPos; native initializes prevPos. First-frame interpolation and collision placement differ.
2. Base tick ordering/constants match structurally: previous positions, postincrement age/expiry, gravity0.04, movement, drag0.9800000190734863 and grounded X/Z drag0.699999988079071. This does not establish movement parity.
3. Legacy Entity movement zeros clipped Y velocity as well as X/Z and includes Entity collision/block side effects. Native Particle.move zeros clipped X/Z only and calls world collision queries with a null Entity. Legacy noClip returns after box translation/position recovery; native performs its own final flag handling. Collision-enabled classes need a real behavioral design and oracle, not field renames.
4. Original ordinary-layer manager disabled depth writes regardless of alpha. Native default shouldDisableDepth is false; original atlas behavior needs an explicit true override, not alpha-based guessing.
5. Original vanilla manager inserts immediately and caps each layer at4000, removing oldest. Native addEffect queues; updateEffects ticks existing particles first, then drains additions, and caps each layer/depth bucket at16384. This changes first-tick/render admission, ordering and pressure behavior. Native Particle subclassing alone does not preserve the old global policy.
6. Old getEntityId affects UV flips, spin direction and seeded render RNG. An arbitrary particle-only counter would change the original shared Entity allocation sequence and must not be called exact identity parity.

**Decision boundary:** current full-original-parity authorization does not silently approve native engine admission/timing differences. Before implementation, resolve whether exact old global policy is required and obtain explicit approval before accepting a changed behavior. This assessment does not propose a broad compatibility manager shim or approve a deviation.

## Bounded candidate, conditional on that boundary

Complete ParticleGlow + ParticleDigammaSmoke, real sprite registration owner, and narrowly bounded existing client hooks. Both are noClip and fixed-brightness, avoiding identity-dependent geometry and collision/world-light queries.

- Glow: inherited billboard; explicit requested velocity; age1; lifetime50–99; alpha0.8; original sine scale expression unchanged; brightness15728880. Verify inherited native billboard behavior against original rather than assuming equivalence.
- DigammaSmoke: custom atlas quad; separate private age and public maxAge; alpha calculated before increment; expiry only on equality; velocity damping0.99 before movement; brightness240. Its original custom scale is not the inherited billboard's scale factor.
- Extract seven existing particle sprite registrations into a small complete native production owner hooked from the real TextureStitchEvent.Pre callback. Preserve exact resource names and existing legacy consumers' adapter relationships. Native sprites must be actual TextureMap-registered objects, not fake ModEventHandlerClient fields.
- Glow factory in ClientProxy can keep constructor/addEffect flow. radialDigamma directly writes motion fields that become protected: use a purposeful setter or typed constructor and preserve caller rotation/RNG/order. Do not broaden this into global motion-field substitutions.
- Large ClientProxy/ModEventHandlerClient remain independently blocked. A source-delta audit of those hook sites is not complete compilation of those classes.

Proposed owned production scope: two particle classes, one sprite-owner class, exact stitch callback and exact flare/radialDigamma branches. No overlap with TileEntity world-field migration. No code changes authorized by this assessment itself.

## Required checks and untouched blockers

Compile complete particle and sprite-owner classes against actual target types, with no fake mod-handler or Minecraft stubs. Check actual TextureMap registration identity/resource names and repeat registration/reload behavior. Check actual BufferBuilder corner/UV/color/lightmap bytes at fractional ticks, both brightness values, neighboring vertices, and absence of nested begin/draw. Check constructors, first-frame state, all age boundaries, original formulas and real no-collision movement; explicitly distinguish bbox invariants from rendered-position equivalence. Check actual native manager queue insertion, depth bucket and expiry removal; report its policy differences rather than laundering them through fixtures. Audit exact existing hook/factory deltas against frozen original source.

Untouched: Splash/DeadLeaf ID-dependent UV; BlackPowderSmoke/ExplosionSmall/Flamethrower ID-dependent spin; RocketFlame/ExSmoke/Contrail ID-seeded rendering; collision-dependent BlackPowderSpark and other grounded effects; SpentCasing bespoke bounce/AABB/model code; Giblet vanilla dust creation; vanilla EntitySmokeFX/EntityBlockDustFX/EntityCritFX replacements; layer3 model/text/line/custom-texture paths; protected external motion/age accesses and reflection sites. ParticleNT's existing incomplete definition behavior must not be silently invented.

RBMKFlame exemplifies a separate layer3 slice: additive blend, fog toggling, animated UV, camera transform and real owned draws. DebugLine is another separate own-GL_LINES slice. Neither proves atlas lifecycle, and neither justifies broad automation.

Headless complete-class/data/lifecycle checks do not establish full mod compilation, GPU visual equivalence, real texture upload/reload, client rendering or game acceptance. No runnable mod or particle parity claim is made. The next implementation should stop on unresolved semantics rather than adding no-ops, placeholders, CE/EE replacements or generic compatibility scaffolding.

## CP22 follow-on preparation, 2026-10-19 12:49 UTC

Read-only production assessment and independent native design probe completed while CP22 remains frozen/uncommitted. No particle production or integration code was changed. Native manager queue/cap/first-frame policy is **pending user approval**; no permission to activate this migration is inferred from the original-port goal.

Recommended bounded choice: native ParticleManager queued admission and 16,384-per-layer/depth-bucket retention, plus native previous-position initialization at the spawn position. Additions become visible after the next queue drain and are first ticked on the following manager update; depending on the original spawn/update phase, visible onset or chained-effect movement can shift by one client tick. Dense effects can retain more particles and cost more CPU/GPU. This does not itself change machine/energy/recipe logic. Do not bundle native collision or identity substitution into this choice. Original colliding effects depend on centered boxes, clipped-Y handling and Entity block-contact/context behavior; debris/foam web expiry, smoke-plume ceiling response, casing bounce/plink and grounded-ash smoke require their own treatment. Entity block callbacks can invoke mod client behavior, so blanket removal is not established as purely cosmetic.

The complete source closure is 39 direct EntityFX classes (including EntityFXRotating), 10 descendants, and ParticleGasFlame/EntitySmokeFX: 50 classes, not 39 independent replacements. Seventeen use Entity ID for spin, UV flips or seeded geometry. Further exact hazards: ParticleRocketFlame's fluent setMaxAge(int) conflicts with native void setMaxAge(int) and writes its separate private maxAge; ParticleDebris's WorldInAJar world shadows native Particle.world; legacy/native random-velocity constructors differ in float rounding. Existing seven sprite fields remain IIcon adapters. Their native sprites must be real TextureMap entries; existing untouched consumer relationships must stay valid.

Pending implementation scope: complete ParticleGlow and ParticleDigammaSmoke, a native ParticleSprites owner, the precise ModEventHandlerClient stitch hook, and the precise ClientProxy radialDigamma velocity boundary. Retain flare construction/addEffect and original update formulas. Glow needs explicit original EntityFX billboard corner arithmetic, rather than inheriting the slightly different native Vec3d-based evaluation. The next noncollision/non-ID atlas batch is ParticleDust plus ParticleCoolingTower, with explicit original world-brightness sampling. Their selected constructor velocity output is overwritten (Dust) or position-only (CoolingTower). Debug/Text are later layer3 candidates, not proof of owned GL/font rendering.

Independent scratch probe: /tmp/nagaport-particle-design-probe/ParticleDesignProbe.java; output /tmp/nagaport-particle-design-probe/result.log. It compiled against the actual pinned API and passed 55,032 checks: real TextureMap registration identity and new-map refresh, actual native no-collision movement and first-frame state, actual ParticleManager admission/expiry/depth-bucket/cap behavior, and real BufferBuilder adjacent geometry/RGBA/lightmap bytes. Its proposed explicit vertex emitter preserves the original float evaluation; this is design feasibility, not compiled mod implementation acceptance. Native inherited geometry differed exactly in 1,993 of 12,000 sampled position components (maximum difference 3.8146973E-6). Preserve old evaluation in production rather than declaring those formulas identical. BufferBuilder's SHORT lightmap encoding writes the second argument first; native call lightmap(sky, block) correctly produces block/sky bytes.

The probe uses a real WorldClient allocated without its unrelated bootstrap constructor solely to supply the actual manager's Profiler. It does not replace Minecraft APIs, run GL upload, render the full client, or prove collision/full-mod parity. Seven-resource atlas registration was tested, not actual sprite image upload/reload. No dependency installation or network access occurred.

Reproduction on this prepared cloud (temporary paths are not portable dependencies):

```sh
CP="$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"
J=/workspace/shared/nagaport-toolchain/java8/bin
"$J/javac" -source 8 -target 8 -cp "$CP" -d /tmp/nagaport-particle-design-probe /tmp/nagaport-particle-design-probe/ParticleDesignProbe.java
"$J/java" -cp "/tmp/nagaport-particle-design-probe:$CP" ParticleDesignProbe
```

SHA256 provenance:
- Actual native jar `.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`: `8c75037188f87bc62939eefa03c2480551509034f069728de012c09276f8f49e`.
- Scratch probe source: `179d5ac472b4f722e34daa2d63733f74631cc0f7b39476f0c5a4f918d5af4299`.
- Final scratch result: `9366b6b51143f92b7b93512def326807e1b6a3c84bc2abcef8a75e570474f94d`.

## Approved implementation frozen for CP23, 2026-10-19 12:58 UTC

The later user instruction supersedes the strict cosmetic-parity/pending-policy portions above: the user normally disables particles and asked to make this subsystem as simple as possible. The parent authorized native 1.12 queue/caps/initial position and simpler particle visuals, without changing machine/energy/recipe/radiation mechanics or discarding gameplay-affecting collision callbacks. Tiny billboard rounding and visual RNG equivalence are therefore not implementation gates. No global renderer simplification is authorized by this particle choice.

Implemented complete native ParticleGlow, ParticleDigammaSmoke, ParticleDust and ParticleCoolingTower. All retain original no-collision intent, age boundaries, velocity/drag, scale/color/alpha formulas, and original settings-dependent factory branches. Glow uses the actual native inherited billboard. The other three retain their original larger custom scale through the small native ParticleQuadRenderer, which appends explicit position/UV/RGBA/lightmap attributes to the manager-owned buffer. All explicitly retain the original no-depth-write atlas grouping. Dust/CoolingTower use native environmental-light sampling and native bounding boxes; this is an accepted particle-visual difference, not claimed exact 1.7 brightness sampling. Their boxes never perform collision queries.

New ParticleSprites registers actual native TextureAtlasSprite entries; the precise stitch hook refreshes them. The existing legacy consumers remain supported by their existing wrappers referencing the same registered sprites. Native resource keys are `hbm:blocks/particle/{particle_base,dead_leaf,particlenote2,particlenote1,particle_splash,particle_ashes,yelflare}`: the original files live under `textures/blocks/particle/`. The old unprefixed keys would miss several textures or select the wrong rectangular note image, so all seven native and existing-consumer registration keys were corrected without altering PNG files. The precise ClientProxy radialDigamma and tower velocity accesses use typed setters; the unrelated extension-renderer registration edit belongs to the separate block batch and was not overwritten.

`porting/tests/probe-native-atlas-particles.py` compiles all six complete production particle/support classes against the actual pinned jar, then runs `NativeAtlasParticlesTest.java`. The native tests cover sprite identity/re-registration/new-map refresh; actual FolderResourcePack lookup, native PngSizeInfo reading and ordinary TextureAtlasSprite aspect validation for all seven original PNGs; pixel decode/dimension agreement; full production update/lifetime/configuration behavior; real BufferBuilder geometry/UV/color/lightmap emission; and actual ParticleManager queue/admission/expiry/depth grouping/16,384-cap behavior. The LightWorld fixture uses the real World API with a controlled light source and bypasses unrelated world bootstrap. Separate narrow source checks protect the stitch/resource routes and unchanged tower settings/lifetime gates; these do not establish complete ClientProxy or ModEventHandlerClient compilation.

No GL upload, mip processing, actual render frame, full-client or gameplay acceptance is claimed. A preliminary attempt to extend the PNG check through native loadSpriteFrames required TextureUtil's GL initialization and failed in the headless invocation; the final deliberately headless check stops at native PNG/aspect validation plus real pixel decode. Broader collision-dependent particles, Entity-ID-dependent effects, and GasFlame/Debug/Text/DebugLine remain untouched in this checkpoint. Next economic batch: GasFlame native smoke subclass and no-collision/stationary debug/text effects, with their real owned render contracts, separately from colliding effects.

Reproduction:

```sh
python3 porting/tests/probe-native-atlas-particles.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"
```

Final frozen run: **PASS 318** complete-production native assertions plus source integration/settings/sprite-route checks. Log: `porting/continuation-2026-10-19/native-atlas-particles.log`, SHA256 `775a28765d48d34fc3a973fe2ea49cf7055066efb6f7d222550e40a538b73e72`. `git diff --check` passes for this owned slice. No broader particle production files were edited after this freeze.

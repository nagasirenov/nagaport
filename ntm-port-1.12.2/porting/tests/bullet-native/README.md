# Native bullet renderer root

Frozen source baseline: `641ba083c276c6ac75afa6a8f996cdd86f8b16d4` (CP46).
This is a bounded rendering/synchronization migration, not a finished projectile-mechanics port or a successful whole-mod build.

## Production scope

Complete native classes:
- `com.hbm.render.entity.projectile.RenderBullet`: typed `Render<EntityBulletBaseNT>`, supplied `RenderManager`, native BufferBuilder triangles/quads and explicit per-vertex color/normal, native player/world fields, native frame-item rendering for the blade.
- `com.hbm.render.util.BeamPronter`: all four public overloads, both enums and public Random are retained. LINE uses POSITION_COLOR; SOLID intentionally uses POSITION and current GL color. It must not become a byte-packed color array: existing callers pass alpha 256, which GL clamps to 1.
- `com.hbm.render.util.RenderSparks`: original line draws, widths, seeded randomness and colors. The earlier immutable-vector rewrite had changed the rounding of `dir.x *= length * rand.nextFloat()` by promoting the first multiplication to double. Parentheses now retain the original float product on all three axes.

Exact supporting fragments, not complete owner migrations:
- `EntityBulletBaseNT`: three typed parameters, getters, three registrations, nine constructor writes and one lazy config-ID read replace watchers 16/17/18.
- `EntityThrowableNT`: inherited stuck-face BYTE declaration, registration, setter and getter replace watcher 2. Initial zero and signed-byte narrowing/widening are retained, including the original distinction between zero and -1.
- `ClientProxy`: only `new RenderBullet()` becomes the supplied-manager factory `RenderBullet::new`. Scope verification checks this exact fragment, not the shared file's whole hash; adjacent migrations can coexist.

No new runtime facade, replaced game API, disabled style, asset substitution, source exclusion or broad entity rewrite. Model/texture files are unchanged.

## Native and original evidence

`run.py` compiles all three complete changed rendering classes and 60 complete unchanged production collaborators against genuine Minecraft 1.12.2/Forge 2768/LWJGL classes. The frozen scope contains 63 complete compiled production sources, 10 supporting source inputs, five pristine original source references, and 17 unchanged original model/texture assets. The native vanilla obsidian PNG is also verified byte-identical to the original client JAR.

The original RenderBullet, BeamPronter and RenderSparks source bodies are compiled as test oracles. Only platform endpoints are adapted: native entity/state access, constructor/API names, immutable vector updates, and a test-only named adapter for the genuine original Tessellator. That adapter reflects into the unchanged, SHA-256-pinned 1.7.10 `bmh` class for every begin/color/normal/vertex/draw call. It is not a reimplementation. No original game classes or production bytecode are rewritten.

The original blade method is deliberately not claimed as a full old RenderItem execution oracle. Its outer transforms, clock spin, ordinary ENTITY_FRAME route, fresh count-one stack/zero hover/age, nested arguments, native generated model, and actual blade PNG are checked separately. The reused complete native item helper's existing ownership, full-draw and model suites run again (26/111/192/41 checks). The registry holder maps the blade identity to a controlled native item; complete ModItems initialization is outside this test.

All implemented and default/fallback bullet branches execute:
- Rifle/pistol/flechette/buckshot/rocket/grenade/APDS meshes, original materials, transforms and group selectors.
- Normal/Folly alternatives with tau/emplacer cube model. The actual original ModelRenderer/ModelBox is separately constructed from the original client JAR and compared with the real native ModelBullet: all 24 source-order position/UV vertices and nonempty rendered framebuffers match.
- Every bolt palette, unknown/default palette, positive/negative entity-ID seeds, eight triangles and four gradient tail quads.
- Both ORB materials/scales/spark routes and unknown trail. Both invocations must fit the same actual 100 ms source seed window; no production clock was replaced. Counts can vary slightly with that seed.
- Meteor molten/vanilla-obsidian binding, original current-texture behavior for an unrecognized trail.
- Leadburster group order and stuck/not-stuck rotation gate.
- WAR beam and direct shared BeamPronter tests across RANDOM/SPIRAL, LINE/SOLID, depth modes and alpha 0/.25/1/256.
- Tau white/orange trails, initial-position sentinel, node translation including zero-alpha suffix, normals, early-life alpha above 1 and the original integer alpha clamp, late-life empty batches, interpolation and player-relative matrix reset. Empty batches can have different declared unused attributes; no vertex, image or state is lost.

A native Tessellator subclass captures actual BufferBuilder bytes before delegating every draw to the unchanged real uploader/GL path. The original adapter likewise captures genuine original packed vertex data before invoking the actual original draw. Test output separates original-Tessellator vertices from unchanged shared-native-mesh vertices. Shared mesh captures are not mislabeled as a new old-OBJ-loader comparison. Existing OBJ/HFR migration limitations remain separate.

Both paths use actual native resource managers, real repository OBJ/PNG assets, real native item models and a surfaceless Mesa OpenGL framebuffer. Tests compare primitive counts/order, meaningful attributes, model matrices, material/current color, relevant GL state, final state and pixels. Shader mods, a launched client and other graphics drivers are not certified.

## GL-state compatibility

The original helpers intentionally leave some state changed: a solid beam leaves culling off; sparks leave line width 2; leadburster leaves rescale-normal on. These behaviors are retained rather than silently normalized.

Simply replacing raw GL with cached GlStateManager calls introduced two audit-detected regressions in mixed callers: RenderFEL's raw cull enable and RenderVol2's raw alpha-blend selection could make the helper skip required native state changes. Every migrated state write therefore retains its unconditional original raw GL effect and also synchronizes the native cache. Tests deliberately diverge actual/cached cull and blend values between beam calls and verify the next actual draw remains uncullled and additive. This is not a broad GL shim or a migration of all callers. Whole-class failure/exception recovery and unrelated raw-GL callers remain outside the proof.

## Synchronization boundary

Tests execute the exact typed declarations/register/getter/setter fragments on real native Entity/DataManager instances. Native metadata encode/decode, incremental dirty entries, signed-byte boundaries, VARINT extremes and noncolliding inherited IDs are exercised. Native Forge spawn bytecode is audited: `FMLMessage.EntitySpawnMessage.toBytes` calls `getDataManager().writeEntries`, and `EntitySpawnHandler.spawnEntity` applies `setEntryValues` before adding the entity to the client world. No new custom spawn payload is required.

NBT behavior is intentionally unchanged: `writeToNBTOptional` returns false; `readEntityFromNBT` calls its superclass then kills the bullet. No new persistence is invented. Existing invalid-configuration constructor dereferences, default config-0 failure without spawn population, and lazy-once config resolution are not silently fixed.

The complete EntityBulletBaseNT/EntityThrowableNT/EntityThrowableInterp mechanics are still legacy and do NOT compile in this isolated probe. Their storage/constructor context is a declared NTM fixture; only the copied, hash-guarded sync/lifecycle fragments execute. Native Entity, DataManager, NBT, PacketBuffer, renderer, item model, mesh, uploader and GL implementations are genuine. Camera interpolation uses a real native player object allocated without its network/world lifecycle. No launched spawn/network, hit/damage/trajectory, save/reload or server acceptance is claimed.

The fixture list in scope.json is exhaustive. Additional machine/registry fixtures come from the unchanged machine-helper regression harness. Original source/hash and exact inverse-mapping guards are source evidence, not behavior tests.

## Reproduce

From `ntm-port-1.12.2`, using existing dependencies only:

```sh
python3 porting/tests/bullet-native/check-mapping.py --source-root .
python3 porting/tests/bullet-native/run.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" \
  --legacy-client /tmp/nagaport-legacy-client.jar \
  --original-root ../ntm-original-pristine
```

The original client JAR must have SHA-256 `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`; it is not redistributed. `--write-scope` is an explicit maintainer option for a separately reviewed scope change, never used by the normal verification path.

No full Gradle build is performed here; the coordinator owns combined full compilation. No policy choice about plants, boss HUDs or lighting elsewhere is implied or waived.

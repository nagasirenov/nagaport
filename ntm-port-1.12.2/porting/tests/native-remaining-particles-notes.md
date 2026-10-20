# Remaining native particle family

## Scope and result

The finite production edit is 22 files: 18 particle classes, SkeletonCreator, ExplosionCreator, IParticleCreator, and CasingEjector. The exact paths and frozen SHA-256 values are in `native-remaining-particles-scope.json`. ClientProxy integration and ModSoundEvents/resource registrations are parent-owned, not part of this manifest's production edit list.

All selected classes compile whole against genuine Forge 14.23.5.2768 / Minecraft 1.12.2 under Java 8. The runner uses the genuine LaunchClassLoader and Forge EventSubscriptionTransformer, actual native worlds/chunks/collision APIs, ParticleManager, BufferBuilder/Tessellator, registry/EventBus, texture/resource manager and Mesa OpenGL context. It neither declares fake platform classes nor edits/excludes production method bodies.

Run:

```
python3 porting/tests/probe-native-remaining-particles.py \
  --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)" \
  --java-home /workspace/shared/nagaport-toolchain/java8
```

The final terminal log is `porting/continuation-2026-10-19/native-remaining-particles-FINAL.log`. Earlier 1,755/1,818 runs preceded additional creator/packet/category checks and are not the final evidence.

## Native boundaries and retained behavior

- Removed EntityFX inheritance becomes real Particle inheritance. Owned layer-3 draws use real formats and actual original OBJ/VBO/texture resources. Atlas callbacks append to the manager-owned batch without taking its begin/draw ownership. Rocket/foam transparent particles explicitly use the native no-depth bucket.
- Original lifetimes, motion/drag, atmospheric thresholds, spawn counts, bone layouts, smoke settings and casing bounce factors remain. SkeletonCreator retains the original entity-vanish call, and native block-dust factories replace removed blood/slime dust constructors. Skeleton landing uses the genuine vanilla hurt event with HOSTILE category, preserving its volume setting.
- Casing collision uses actual native immutable AABBs and world collision boxes, not an entity emulator. Its floor contact, rebound, sound threshold and original pitch/volume are exercised. Other representative collisions cover foam expiration, plume upward restoration and spark rebound.
- Exact original packet key/type construction remains, including ExplosionCreator's unusual byte-cast stored in a short. The native dimension getter supplies a genuine TargetPoint. The packet transport itself is an explicit fixture boundary, not a multiplayer-delivery claim.
- Original four casing names use owned native sound events; registered third-party events work. Unknown names remain silent with a bounded warning rather than throwing or inventing events. A sounds.json-only external custom event without a registered native SoundEvent is an explicit compatibility limitation. Explosion near/far boundaries, radius, original delay, volume/pitch and category are checked at the actual PositionedSoundRecord / SoundHandler callback boundary. Parent-owned sound-resource tests separately verify real directory/JAR resolution; this test does not claim audible audio-device output.

## Approved cosmetic adaptations

The user's particle simplification approval covers native pending queues, native initial interpolation and native per-layer cap (16,384 in this Forge jar). These are exercised directly, including first-drain timing and expiration. Native particle bounding-box anchors replace Entity yOffset bookkeeping; entity fall/web callbacks are not emulated. Purely visual per-particle variation no longer allocates Entity IDs. Local mesh orientation fields are only explicit visual state on mesh particles. Typed motion/collision/offset methods replace external access to protected native fields. RocketFlame.setMaxAge now has native void return type; no in-repo caller relied on the old fluent result. No exact visual/RNG micro-parity or whole-game acceptance is asserted.

## Explicit NTM collaborators

The runner names every controlled collaborator: ClientProxy's vanish endpoint (captured ID only); the narrow ResourceManager field holder referencing real production OBJ/HFR/VBO implementations and original resources; BobMathUtil's two used method bodies; empty MainRegistry for unused imports; abstract EntityDummy/EntityUndeadSoldier class-name boundaries; and captured AuxParticlePacketNT/PacketThreading transport. All tested particle/creator/ejector classes, WorldInAJar, mesh/loader implementations, and production ModSoundEvents are complete actual sources. Platform client setup uses native constructors, not substitute Minecraft/Forge classes.

The selected tests prove native execution of the finite family, both actual manager render phases, original-resource pixels, balanced matrices/following native GL state, and bounded lifecycle/interaction behavior. They do not prove the entire mod, every ClientProxy factory mode, full live multiplayer, or particle-disabled gameplay acceptance.

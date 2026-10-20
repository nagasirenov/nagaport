# Native moving atlas particles, 2026-10-19

## Production scope

Eleven complete classes now use native `Particle` and `BufferBuilder`: the existing mod-owned `EntityFXRotating` geometry base, BlackPowderSmoke, ExplosionSmall, Flamethrower, Ashes, DuoDec, DuoDec2, DeadLeaf, Splash, ExSmoke and BlackPowderSpark. `EntityFXRotating` retains its historical name but does not import or emulate the removed platform EntityFX API. It owns only degree-based billboard spin, a visual seed and a motion setter.

New `ParticleAtlasVertex` appends actual native 28-byte particle vertices, clamps RGBA as the old tessellator did and supplies lightmap values explicitly. Atlas ownership stays with the existing `ParticleSprites` stitch registration. No assets, gameplay blocks, machinery, energy or radiation behavior are changed. BlackPowderCreator now uses the narrow native motion setter; AshesCreator captures its three original spawn expressions in original RNG order and uses the real native ParticleFlame factory. Vanishing an entity, companion-flame creation and particle admission order remain.

## Preserved behavior and approved differences

- Ten complete original onUpdate bodies compare byte-for-byte against the supplied pristine original after reversing only expiry/movement API names, visual-ID and spin-field names, and the smoke enum. Original age boundaries, gravity/drag order and constants, drift speed caps, hues/scales/alpha, rotations and quad expressions remain.
- Native ParticleManager admission/queue/caps, initial previous position, native bottom-anchored boxes, null-Entity collision queries and clipped-Y behavior are used under the user's simpler-visuals permission. The real native mover is not represented as legacy Entity movement parity. No Entity adapter, fake old API, global physics shim, source exclusion or stub is introduced in production.
- Original noClip smoke/explosion effects remain non-colliding. Other scoped effects still collide with real world boxes. Leaves/notes remain after landing; splashes expire; ashes randomize their landing orientation and still emit their original periodic smoke, including on the expiry tick.
- Spin/UV/cloud/ash variation uses a stable per-particle identity hash instead of consuming shared Entity IDs. This changes purely visual random selection. It is not an exact original Entity-ID sequence guarantee.
- Legacy atlas depth writes remain disabled. Original rotated math remains in degrees, not native angle radians. Fullbright and block-only flame brightness are distinct and retained. Original extra bloom-color/negative final-frame-alpha values clamp rather than wrapping in the byte buffer.

## Moving contact audit

Inspected all 27 declared NTM `onEntityCollidedWithBlock` methods and the rubber-farm `onFallenUpon` method in production, comparing applicable branches to client-only nonliving, non-item particles. No reachable authoritative gameplay mutation was identified for this family:

- Gas/radiation/digamma/armor/potion handlers require EntityLivingBase, EntityPlayer, an exact creeper class or TeslaCrab. Those types exclude old particles.
- Flammable-gas combustion, TNT arrow ignition, conveyor item conversion and rubber-farm conversion are server-gated; particles in these effect factories exist only on the client.
- Reachable vine/taint/fluid slowdowns, web flags, fire flags and sulfuric-acid fizz affect only the visual particle/contact sound. Those old Entity-only cosmetic side effects are deliberately not synthesized by native particles. Scoped particles have no damage override that forwards damage into gameplay. This does not remove any contact handler from actual blocks/entities, nor does it justify dropping callbacks in unrelated entities or third-party code.

The changed collision box and no-context query can visually alter how these particles meet custom blocks. No arbitrary third-party block parity is claimed.

## Native evidence

Run from the module root with Java 8 and the real resolved native classpath:

    python3 porting/tests/probe-native-moving-atlas-particles.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"
    python3 porting/tests/moving-atlas-source-semantics.py

The source check defaults to the sibling supplied original; use `--reference /path/to/ntm-original-pristine` elsewhere. The native runner accepts a platform-native classpath and selects java.exe/javac.exe when present. Windows execution remains untested.

Result: 750 native assertions PASS. All eleven complete production particle classes and the actual vertex/sprite/mutable-vector helpers compile against the pinned native jars. Tests cover constructors, noClip intent, original custom ticks, expiry, native floor/wall clipping, splash/leaf outcomes, ash landing/smoke including expiry, UV flips, rotated geometry, exact ordinary RGBA/lightmap bytes, native flame creation, final-frame clamping and actual manager queue/removal. The controlled World fixture uses real World methods and native AxisAlignedBB collision; it is not a running world.

FlameCreator is a deliberately bounded dependency fixture containing only its five exact mutable META integer declarations, extracted from production. No Minecraft or Forge classes are fabricated. This does not compile FlameCreator networking or its packet helper chain. The two changed creator files are source-audited, not claimed as complete compiled helpers; their IParticleCreator/ClientProxy/packet dependencies remain outside this slice. The real native flame-factory and motion-setter contracts used by them are exercised.

Evidence logs: `continuation-2026-10-19/moving-atlas-particles-native.log` and `moving-atlas-source-semantics.log`. Initial test-authoring failures were an ambiguous wildcard import and reversed expected Rodrigues direction, both fixed in the tests; no production fallback was needed. No GL render, full mod build, game launch or save/gameplay acceptance is claimed. Common clean compilation and ClientProxy's five ExSmoke call-site adaptations belong to the checkpoint integrator.

## Frozen production hashes

62ba2ee2ba35229a49cb0f418d04717134bd38eb586410119c1a5dbd0c5a080e  src/main/java/com/hbm/particle/EntityFXRotating.java
bd811def9a77af9394f53d4f4cb5160973445ae6b2c045fc784f979fbf1a61d4  src/main/java/com/hbm/particle/ParticleBlackPowderSmoke.java
af213b08ac128c419352e0fdb4621c2838d31ed0bd0cbf1fcc84dc7167cf5411  src/main/java/com/hbm/particle/ParticleExplosionSmall.java
31db0ce8b85eaee3b6b007a0ab8564159113b52ad07ebcd8ab304d71947de19b  src/main/java/com/hbm/particle/ParticleFlamethrower.java
d8d0b65b975325b2ff959fcd5c7be62c80039dccec464454ca9aaa63d2607d16  src/main/java/com/hbm/particle/ParticleAshes.java
9549289a2e4ce7e8df0ee014d6cd76b7c9ad2a5d3f58fb843bd6b235cd554323  src/main/java/com/hbm/particle/ParticleDuoDec.java
9535f5ddf6142ce019ab4d3b127db0df9e10ab4de1e17a911be20cb5a5d90d80  src/main/java/com/hbm/particle/ParticleDuoDec2.java
09ce950d0d1d3bb3ad21f549371ba5565960bf71675e348f166082e118a7f2cf  src/main/java/com/hbm/particle/ParticleDeadLeaf.java
cb5971bc6e182382b87122ef7d6ed886fe604afea45d56fcc358aacec16530cd  src/main/java/com/hbm/particle/ParticleSplash.java
67f8d3a55da82c074e1b5215fb99da57dccc466c9a90f7406ad2c6ff53f59603  src/main/java/com/hbm/particle/ParticleExSmoke.java
8455170e15ea373f5e6f4e50017c6e93d786086ab851ce307b4b769ddc64251b  src/main/java/com/hbm/particle/ParticleBlackPowderSpark.java
e48019900168f60fc371ed8cdd22f473dd77550fb73ddf823ee5c061dded76af  src/main/java/com/hbm/particle/ParticleAtlasVertex.java
78df0f43e3e06a0d59e7b5ced0b0dbd52551c6fcb967c205a946ffcd28292181  src/main/java/com/hbm/particle/helper/AshesCreator.java
2d24fd33d1f26b8694ee2a6b262119a10e30b08fab64944e716d21b7013cfe9d  src/main/java/com/hbm/particle/helper/BlackPowderCreator.java

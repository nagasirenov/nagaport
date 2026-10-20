# Native stationary particle batch, 2026-10-19, after CP26

Production scope: `ParticleAmatFlash`, `ParticleHadron`, `ParticleMukeWave`, `ParticlePlasmaBlast`, `ParticleRBMKFlame`, and `ParticleRBMKSteam`. They now directly extend native `Particle`, use the native owned-render callback and actual `BufferBuilder` formats. No proxy, manifests, platform stubs, exclusions, shared Entity compatibility layer, or gameplay block code is changed by this batch.

## Bounded semantics

- Original constructors and all current call sites supply no motion or gravity; no caller sets velocity. These are stationary flashes, rings and jets. Their original post-increment lifetime, position, caller scale/lifetime setters, fade/expansion formulas, UV animation and geometry remain. Plasma orientation is local particle data, not an invented Entity field. Amat retains its 100 seeded fans.
- Explicit stationary updates replace the inherited old Entity movement/contact path. Inspection of the 27 mod block contact implementations found no authoritative gameplay mutation applicable to these client-only, nonliving, non-item particles: living/player/item actions exclude them, and combustion/damage/item conversion is server-gated. Old fire/web flags and sulfuric-acid `random.fizz` on these particles are cosmetic side effects deliberately omitted under the approved simpler-particle policy. This conclusion does not authorize removing contact callbacks from moving/colliding particles or block code.
- Native manager queue/caps and spawn-position initialization follow the approved policy. Camera-aware render callbacks replace hard-coded local-player camera reads where applicable. Owned renderers use cached `GlStateManager` APIs and restore depth writes, ordinary blending and culling instead of leaking additive/depth state into subsequent effects.
- Native vertex RGBA is explicitly clamped, as old Tessellator did. Without this, negative last-frame fade values wrap to an almost opaque byte. This is tested. The original fade formulas themselves remain unchanged.

## Reproduce

From the repository root:

```sh
python3 porting/tests/probe-native-stationary-particles.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"
```

The probe compiles all six complete production classes plus actual shared `ParticleVertexFormats` and `RefStrings`, without source projection or fake platform classes. It checks 644 assertions covering native admission, stationary lifetime/expiry, first position, caller settings, five actual quad builders, the actual five-vertex fan builder, RGBA/UV/normal/lightmap packing, final-frame clamping and actual native resource resolution/PNG header plus pixel decoding. The controlled `World` fixture rejects collision/chunk access. It lives only in the isolated test.

Result: `porting/continuation-2026-10-19/stationary-particles-native-FINAL.log`.

Native Forge jar SHA-256: `8c75037188f87bc62939eefa03c2480551509034f069728de012c09276f8f49e`.

This is not GL rendering, full-mod compilation, or in-game acceptance. Integration compilation remains the checkpoint owner's responsibility. No particle buffer/GL pixel parity claim is made.

## Frozen production hashes

```text
a6e7f413ae3b62b2a80fc11416975d2cefb4de5148be21578ab301033ff921f8  src/main/java/com/hbm/particle/ParticleAmatFlash.java
861989e9021e3f6abe2849c5c756d4a521dffb96789fc977940c4144bea85c56  src/main/java/com/hbm/particle/ParticleHadron.java
bcee60a00f052283ee0d9338833ffa0b1029665a9e54fd90cfc22894fbebb14f  src/main/java/com/hbm/particle/ParticleMukeWave.java
c0b93282ef5dc81e5d90a065f773b0cbe0d7519fe849f4e8171653a1e21c849d  src/main/java/com/hbm/particle/ParticlePlasmaBlast.java
50895f35791b0f66d37ee91e9f32be994b0636191d0777a2c0cadff0d8598878  src/main/java/com/hbm/particle/ParticleRBMKFlame.java
a8ec8fd6647139b1c4fedfdc47442db7b6ce617e3e0d22e44894b52eb6bc6873  src/main/java/com/hbm/particle/ParticleRBMKSteam.java

```

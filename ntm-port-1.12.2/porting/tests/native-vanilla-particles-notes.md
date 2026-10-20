# Native vanilla particle caller checkpoint

## Finite scope

Production changes are `NativeVanillaParticles.java`, `ExplosionSmallCreator.java`, and ClientProxy's particle imports/effectNT callback region. The helper and creator compile whole against genuine Forge 14.23.5.2768 / Minecraft 1.12.2 under Java 8. Fifteen complete production ClientProxy branches are extracted verbatim and compiled/executed; every native-helper call in effectNT must belong to this selection. No claim is made that all of ClientProxy or the whole mod compiles. Registration methods were not edited by this worker.

`native-vanilla-particles-scope.json` freezes both whole production files, the entire effectNT callback and each selected branch. The full ClientProxy hash is a provenance snapshot only, so independently owned registration changes do not invalidate the bounded check. `check-native-vanilla-particles-source.py` verifies the frozen scope and searches active repository Java sources for removed vanilla particle imports. The only other old-name hit outside the original caller was a comment in TestEventTester; the active ExplosionSmallCreator caller is included here.

## Native implementation

The focused helper uses real vanilla particle subclasses, not an EntityFX adapter. Updates, rendering, bounding boxes, lifetimes and manager admission belong to native classes. Its interface exposes only packet-requested motion and collision decisions. Constructors never enqueue; actual caller branches own exactly one addEffect per produced particle. Native block dust calls init(), required to establish its source block position before the real native renderer reads brightness. Native public setMaxAge replaces private-field reflection. No old particle reflection remains.

Original packet names, branch counts, particle-setting gates, explicit age overrides, custom motion, jetpack ray traces and side checks, block/meta selections and small explosion packet composition are retained. The generic vanilla packet resolves native enum names and translates optional legacy block/item suffix parameters into native argument arrays; unknown/malformed visual names remain silent. Small-explosion near/far threshold, range, volume, pitch, delay and BLOCKS category retain their original values. Its two native sound constants/resources are parent-owned.

Approved visual simplifications: native cloud size replaces the old reflected colored-cloud scale; smoke scale uses the native constructor (including its native size/lifetime coupling when no explicit age override exists), and all variants use native initial interpolation, random visual variation, queue timing and per-layer caps. No exact visual or RNG micro-parity is asserted.

## Evidence

Run:

    python3 porting/tests/probe-native-vanilla-particles.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8

Final output: `porting/continuation-2026-10-19/native-vanilla-particles-FINAL.log`.

The runner reuses the existing native test infrastructure and compiles its complete required production sources. The 15 exact branches cover waterSplash, cloudFX2, fireworks, vanillaburst, vanillaExt, vanilla, jetpack, bnuuy, jetpack_bj, jetpack_dns, sweat, vomit, radiation, schrabfog and fluidfill. Actual ParticleManager admission, no duplicate identities, both real GL render phases, floor collision versus noclip, expiration, the native 16,384 cap, counts/settings and packet motion are tested. Real rendering exposed and regression-tests the required block-dust initialization.

Tests use genuine Minecraft/Forge classes and a genuine Mesa OpenGL context. Explicit boundaries are the existing native test world's preloaded chunks, NTM resource field holder, two actual math collaborator bodies, unused entity class names, vanish endpoint, and captured NTM packet transport. Small sound dispatch is captured at the actual native SoundHandler/PositionedSoundRecord boundary, not an audio-device claim. The generic named-particle branch is verified at native World's enum/argument callback boundary; its world-renderer dispatch is not a live-game claim. Parent-owned sound resource validation separately covers directory/JAR lookup.

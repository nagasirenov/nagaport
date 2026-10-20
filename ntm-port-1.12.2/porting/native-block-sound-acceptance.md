# Bounded native block sounds

## Scope and API

The complete production `ModSoundType`, `ModSoundTypes` and `ModSoundEvents` classes are native 1.12.2 implementations. No source exclusion, replacement Minecraft/Forge class, fabricated fallback sound or general legacy sound shim is introduced.

`ModSoundType` directly extends `net.minecraft.block.SoundType`. All original factories and string constructors remain; existing-type factory parameters now use native `SoundType`, and all five native getters return `SoundEvent`. Native hit and fall getters intentionally dispatch through `getStepSound`, preserving the original STEP subtype. The original PLACE/BREAK/STEP envelope state, default PLACE state, volume/pitch callback behavior, nullable envelope reset, per-query random draws and explicit/default random constructors remain. The pipe and morkite envelopes still share `MainRegistry.instance.rand`, retain base volume/pitch 0.85, subtract 0.15 for BREAK only, and add `nextFloat() * 0.2` without changing operation order.

The original SLICER/BLOCK_OPENC/BLOCK_CLOSEC registrations remain. Exactly three events are added from the original keys `hbm:step.metalBlock`, `hbm:block.pipePlaced`, `hbm:step.morkite`; native `ResourceLocation` canonicalizes the first two to lowercase. The class owns each event object before the Forge sound-registration event, allowing preInit block construction to retain the exact subsequently registered object. String factories resolve those owned events or an already registered native event. Unknown strings fail explicitly rather than silently returning a fallback or constructing an event which never enters the native registry. Future generic `mod(name)` calls must have the corresponding `hbm:dig.name` and `hbm:step.name` events registered.

The constructor binding in `FluidDuctStandard` uses `setSoundType(ModSoundTypes.pipe)` and is owned/tested by the fluid-block work, not this sound implementation.

## Independent original behavior evidence

The cached original 1.7.10 client jar SHA256 is `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`. `LegacyBlockSoundOracle` executes its unmodified `aji`/`ajm` classes in a separate URLClassLoader. The original stone (`aji.i`) and metal (`aji.j`) types both use `dig.stone` for place/break and `step.stone` for steps; their only distinction is base pitch 1.0 versus 1.5. The original custom pipe factory overrides that pitch with 0.85. Consequently pipe retains native STONE step samples instead of adopting the different METAL event identity. Original `bje` hit and `sv` landing bytecode also calls `ajm.e()` (the step resource) before volume and pitch, confirming the hit/fall STEP mapping.

## Exact resource repair

Native `ResourceLocation` lowercases OGG paths. Eight case-only asset renames are recorded with pristine SHA256 values in `porting/tests/native-block-sound-resource-renames.json`; temporary intermediate names make these portable on Windows without case-only duplicate entries. Every audio byte remains identical to pristine. Only the eight file references in the three relevant sounds.json entries gain an explicit `hbm:` namespace, retaining the original case in JSON, sample ordering, weights, gain, pitch, category and streaming metadata. Event keys retain their original spelling and normalize when native SoundHandler constructs ResourceLocation keys.

The namespace fix is necessary independently of case normalization: native SoundListSerializer creates unqualified file references in the `minecraft` namespace, and SoundHandler does not later rebase them to the event namespace. The test invokes actual `SoundHandler.getSoundMap`, `loadSoundResource` and its downstream `validateSoundResource` against a real `SimpleReloadableResourceManager`, both a native directory pack and generated jar pack. All eight correctly namespaced files are retained in their event accessors. A negative unqualified `block/pipePlaced` probe really requests `minecraft:sounds/block/pipeplaced.ogg`, is rejected, and yields MISSING_SOUND. Those two expected warning lines in the test log are negative-control results, not missing production assets.

The actual native `LegacyV2Adapter` is also exercised: it repairs old language-file country-code casing only and returns the original ResourceLocation object for sounds.json and OGG requests. It cannot supply missing sound namespaces or mixed-case audio-file compatibility.

## Verification and limits

`porting/tests/block-sound-native-probe.py` compiles all three complete production sound classes and production `RefStrings` against cached genuine Minecraft/Forge 1.12.2. Its only NTM fixture is MainRegistry's instance and Random field. The existing genuine Forge EventSubscriptionTransformer launcher is reused. The real event bus registers exactly six owned events; tests verify identity before and after registration, native block sound assignment, every factory and five getters, 1,000-action deterministic volume/pitch traces, shared pipe/morkite RNG ordering, native resource parsing/loading, byte identity and exact original sample ordering. The final log is `porting/native-block-sound-test.log`: 9,317 native assertions and 12 optional original-bytecode oracle assertions pass.

Reproduction:

    python3 porting/tests/block-sound-native-probe.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)" --legacy-jar /tmp/nagaport-legacy-client.jar

The optional cached legacy jar can be omitted. Gradle entrypoint `porting/tests/block-sound-probe.init.gradle` provides `testPortBlockSounds` without changing source sets or compileJava.

No audio device/backend is started. These results establish native event/resource resolution and original sound-selection/envelope behavior, not audible playback acceptance, full game launch, dedicated-server startup, whole-mod compilation or pack-wide sound migration. Unrelated sounds.json entries and assets remain outside scope.

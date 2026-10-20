# Foundry insertion sound: native registration and resource lookup

The casting-block insertion callback retains original event `hbm:item.upgradePlug`, category PLAYERS, volume 1 and pitch 1. `ModSoundEvents` now owns and registers its exact native SoundEvent object; no nullable lookup or fallback is used.

Native ResourceLocation lowercases the sample path. The original `sounds/tool/upgradePlug.ogg` is renamed to `sounds/tool/upgradeplug.ogg`, byte-identically. The sounds.json sample gains only the `hbm:` namespace; leaving it unqualified would cause the native SoundHandler to search Minecraft's namespace. The original event key, category, stream flag and sample are unchanged.

`foundry-sound-renames.json` pins the original audio SHA256. The extended existing `block-sound-native-probe.py` verifies all fourteen historical/current audio renames against the pristine original, checks exact stored spelling (including on case-insensitive filesystems), and runs genuine native sound registry events, SoundHandler parsing/validation and folder/JAR lookups. Terminal result: 9501 native assertions and 12 original-client oracle assertions. See continuation-2026-10-19/foundry-sound-native-FINAL.log.

Reproduce with JDK8 and the cached native classpath:

```
python3 porting/tests/block-sound-native-probe.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8 --legacy-jar /tmp/nagaport-legacy-client.jar
```

This is resource and callback acceptance, not audible hardware playback or a launched game. The case-only rename must use the checkpoint helper's verified safe apply path on Windows; never overwrite local edits or normalize the repository to force a patch.

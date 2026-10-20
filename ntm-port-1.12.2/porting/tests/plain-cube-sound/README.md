# Nine plain-cube sound-builder construction chains

Separate baseline: `a040cd6b46901a209b8e5db808b980dccc4981c5`, a local immutable freeze of CP59 plus the previously delivered plain-cube r2 complete patches. This increment does not include unrelated main/CP60 edits and never edited main.

## Production scope

Exactly nine ModBlocks statements, plus one public NTM-owned `withSoundType(SoundType)` builder in each of BlockOutgas and BlockStepEffect. Each builder calls the genuine protected native Block.setSoundType and returns the same receiver. No fake/public Forge API, generic legacy setter, superclass substitution or gameplay callback migration is introduced. The prior 27 texture bindings, PlainCubeTextures, PlainCubeModelHandler, PlainCubeModel and all prior evidence remain byte-identical.

Earlier setTranslationKey/setCreativeTab calls return Block. Instead of moving the sound assignment to the front of each chain, the new statement casts the COMPLETE preceding receiver to its proven exact freshly constructed NTM owner at the original setter position, then calls withSoundType. Original argument and assignment order remain intact. Existing noFortune remains at its original position.

The cast cannot introduce a reachable ClassCastException at these selected sites: javac attributes the innermost new expression to the exact named, non-anonymous BlockOutgas/BlockStepEffect; every preceding native Block setter returns the same receiver, and the unchanged noFortune returns its own BlockStepEffect. Neither owner overrides the native setters. The actual original setters and the native constructor slices verify identity at runtime. This is not an assertion that the cast is safe on arbitrary Block expressions or unknown subclasses.

These nine statements had ERROR-typed complete texture receivers because setStepSound no longer exists. Before/after attribution retains that honest failure. After the sound correction, all nine final complete texture receivers and helper arguments are genuine native Block; no source/type errors are suppressed.

## Proof

- Independent Java8 full-source attribution over 4,059 units before and after, including complete sound receivers, sound arguments, actual preceding setter executable owners/return types, builder owner/return types and final texture arguments. The local diagnostic counts are 17,448 before and 17,439 after, not a complete-build or unique-defect claim. Errors elsewhere remain. Counts fall by nine because the earlier setter error masked downstream texture errors.
- check-source.py exactly inverts all nine full statements and both owner additions to the immutable baseline, preserving every unrelated byte and all historical r2 files. It proves fresh-owner closure and retains the exact existing SoundType.CLOTH/GLASS/SAND/WOOD arguments and registration statements. All nine items use standard ItemBlock. Frozen production/test hashes are enforced.
- 44,135 native assertions execute all nine exact constructor chains, both actual builder bodies, real native sound fields/world accessors, exact return identity (including null sound values), name/tab/sound/hardness sequencing, native model registration, raw-damage-independent Forge mesher lookup, native atlas/GL/reload/material/UV/winding, and existing native lighting/culling controls. No new camera/AO behavior is introduced.
- Original 1.7.10 Block bytecode checks original setStepSound field-reference/return/null behavior, preceding name/tab setter return identity, the four existing sound categories and native volume/pitch. Original six-face rendering and raw metadata icon/color tests remain active.
- The prior 27-site native regression separately still passes 70,597 assertions (`previous27-native.log`). Its old source guard is preserved, not silently rebased; this increment's inverse check accounts for the intended new changes.

The native tests extract exact production constructors, noFortune and withSoundType into explicit NTM-only owner fixtures because unrelated gameplay callbacks still do not compile. All Minecraft/Forge classes are genuine. Standard ItemBlock is the actual selected item type for all nine. No complete gameplay-owner compile, launched client/server, audio waveform/sample identity, or original-vs-native camera/AO pixel parity is claimed. The already-present native SoundType constants are not changed or remapped; existing approval gates remain unchanged.

## Reproduce

From this module, using cached inputs only:

    python3 porting/tests/plain-cube-sound/run-attribution.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt
    python3 porting/tests/plain-cube-sound/check-source.py
    python3 porting/tests/plain-cube-sound/run-native.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --resources /workspace/shared/nagaport/ntm-port-1.12.2/src/main/resources --legacy-jar /tmp/nagaport-legacy-client.jar

The coordinator owns the combined build and integration manifest. The package is an increment from the frozen prior r2 source, not a replacement for the prior r2 patch.

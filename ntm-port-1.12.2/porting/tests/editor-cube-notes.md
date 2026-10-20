# Complete editor-cube owner migration

Finite production ownership: `BlockWandJigsaw`, `BlockWandTandem`, `BlockWandLogic`, and `BlockWandLoot`, including all nested tile and GUI classes. Shared interface, state snapshot, model, handler, registry initialization and legacy-root retirement belong to the surrounding side-rotation closure, not this worker.

## Native changes

- Native `IBlockState`/`BlockPos` placement and activation, full 0–15 persisted metadata, `MODEL` render type, immutable atlas `ResourceLocation` catalogs. Source material remains IRON and inherited sound remains STONE.
- Original face selection and rotation formulas are unchanged. These four world selectors intentionally match their inventory selectors.
- Native `GuiTextField` IDs, `fontRenderer`, `GuiButton.drawButton` partial ticks, checked `IOException` callbacks, tile positions and item tag access. Original GUI text, layout numbers, save order, invalid-integer handling, pause distinction and reference-equality string behavior are retained.
- Native registry names, world block states, tile access, native math names and fake-player `world` access replace removed APIs. Existing NBT keys and wire order remain unchanged.
- Each of the four actual nested tile classes has a block-identity `shouldRefresh` override. Native chunk lifecycle retains its populated data across orientation changes and removes it on block replacement.
- Jigsaw/Tandem preserve the old piston formula rather than use the changed native centered/eye-height helper. Legacy `posY + 1.82D - yOffset` is `posY + 1.82D` in native feet-relative coordinates, with strict float proximity around the integer corner and the same strict vertical thresholds/yaw mapping. The old source formula is also mirrored (inverted facing) by the repository's `BlockPlasticExplosive`; external vanilla source reference: https://www.javatips.net/api/WildAnimalsPlus-1.7.10-master/build/tmp/recompSrc/net/minecraft/block/BlockPistonBase.java

## Proof

`editor-cube-source-test.py` compares 54 complete behavior/GUI method bodies against pristine source after explicit native API normalization, including every serializer/deserializer, NBT read/write, tick/update, generation/replace body, controls, settings, GUI save and GUI event body. It also compares all original string literals after only the atlas path conversion and checks finite legacy API residue. This is source preservation evidence, not runtime proof.

`editor-cube-native-probe.py` compiles the complete production four owner files, including every nested body, with real Forge 1.12.2/Minecraft and JDK 8. No owner method is copied, extracted or substituted. Actual supporting `BufferUtil`, `LegacyWeightedLoot`, `WeightedRandomChestContent`, `ForgeDirection`, `JigsawPiece`, `JigsawPool`, interface/state classes and relevant owner interfaces also compile whole.

The native runtime test passes **2,439 assertions** covering:

- All metadata states on both logical sides; native World/Chunk identity retention with populated actual tile fields, plus invalidation/removal on replacement.
- Exact byte-level wire order, Unicode payloads, native NBT round trips, invalid loot block fallback, original permission/tick guard/range behavior.
- Main/off-hand native activation, sneak guards, air selector, structure-wand pass-through, paper copy/paste exclusions and preserved armed state; native placement yaw and strict legacy piston boundaries.
- Logic and loot tool bounds, setting copy behavior, generation dispatch ordering/coordinates, debug behavior, original peculiar logic fallback, actual weighted loot into native chest inventory, and decorative loot dispatch.
- Actual GUI close/save bodies, native GUI objects, packet coordinates, invalid integer fallback and original string identity quirk. GUI text fields are allocated without GL and populated reflectively only to exercise actual save handlers.

## Explicit bounds

The probe names and prints every external collaborator. `TileEntityLoadedBase` is a native-TileEntity-based bounded sync/base-NBT observer, not proof of its transport implementation. Registries/helpers, structure engine build/connection lookup, loot/action catalogs, `MainRegistry`, packet construction/transport and external destination tile types are bounded NTM collaborators. The packet boundary uses the actual `NetworkHandler` type and `sendToServer(IMessage)` signature, not `SimpleNetworkWrapper`. Actual native World and Chunk implementation execute the lifecycle, while chunk availability, saving, generation and light work are bounded by a test World subclass.

This proves neither full-mod compilation/startup nor live GL rendering, GUI opening or network delivery. Server `FakePlayerFactory` execution is not exercised; its complete owner code compiles and the source preservation comparison covers the unchanged policy/ordering. Shared legacy `ICopiable`, `ModBlocks`, packet and generation engines remain outside this finite closure.

## Commands

    python3 porting/tests/editor-cube-source-test.py
    python3 porting/tests/editor-cube-native-probe.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)"

Frozen source/test hashes are in `editor-cube-source-manifest.json`; recorded outputs are `porting/editor-cube-source-test.log` and `porting/editor-cube-native-test.log`.

The source guard defaults to the pinned portable side-rotation pristine fixture archive via `side_rotation_fixtures.py`; `--original /path/to/pristine` remains available for an independent cross-check. Both paths produced byte-identical source-proof output. No local Git object is required.

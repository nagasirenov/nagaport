# Concrete metadata-zero native evidence

This is the owner/state prerequisite for the sibling `concrete-placement-native`
source migration. It makes **no production changes** and deliberately adds no
metadata property to `BlockGeneric` or its widely shared `BlockBase` parent.
The final native run passes **24,754 assertions** with Java 8 and genuine
Minecraft 1.12.2 / Forge 14.23.5.2768 classes. See `native-test.log`.

## Reproduce

From `ntm-port-1.12.2`:

```sh
python porting/tests/generic-metadata-native/run-native.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath-file /tmp/nagaport-ae2-native-classpath.txt \
  --legacy-jar /tmp/nagaport-legacy-client.jar \
  --original-assets-jar /workspace/shared/nagaport-original-pathfinder/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar
```

The optional original HBM JAR enables direct archived bytecode and resource
comparisons. Without it, current assets must still match the frozen manifest,
and the pinned vanilla 1.7.10 bytecode oracle still runs. No downloads, Gradle,
source-selection changes, altered platform APIs, Unsafe, or fake ModBlocks class
are used. Mesa surfaceless EGL supplies the real offscreen OpenGL context.

Normal execution **compares**, rather than regenerates, `source-manifest.json`.
`--record-source-evidence` is an explicit initial capture option, not a repair
for changed evidence. The complete Generic/Base files must also match pinned
CP54 commit `0732d318302d807ce8579878a0cdde6b18bbba86` byte-for-byte, using
the SHA256-pinned `source-baseline.zip`. Normal runs need no Git history or
unpublished commit objects. The four
constructor statements and RNG method must match that commit; the RNG method
also matches its `ntm-original-pristine` source. The probe verifies exactly one
assignment to each selected static field. Item/model source-only route hashes
are frozen separately from compiled owners.

## Real owners and explicit boundaries

- Thirteen complete production Java files compile, including actual
  `BlockGeneric`, `BlockBase`, `LegacyBaseBakedModel`, the icon adapter and full
  texture-stitch handler. No owner is shortened to make it compile.
- `ConcreteConstructionSlice` contains the four **verbatim construction
  statements** for ordinary, mossy, cracked and broken concrete. It is not a
  replacement `ModBlocks` class or a full-table compilation claim.
- `ConcreteRandomSlice` retains the original static Random field and full
  `Library.getRandomConcrete()` method. Its only production-body change is the
  explicitly named fixture qualifier `ModBlocks.` to `ConcreteConstructionSlice.`.
  Seed/nextLong test methods expose deterministic independent verification.
- The sole NTM static boundary is MainRegistry logger, Random and creative-tab
  identity. The tab is a genuine native tab fixture, not the complete mod tab.
- Genuine Forge registries receive the four actual production block objects.
  A genuine native standard `ItemBlock` is registered only as an explicit
  mechanics boundary for drops/pick. Production still uses `ItemBlockBlastInfo`.
  Its exact registration statements are frozen but not executed here.
- Source-only proof establishes the actual ItemBlockBlastInfo constructor
  forwards to ItemBlockBase; non-IBlockMulti metadata delegates to native
  ItemBlock; neither class overrides placement. Generic/Base are not
  IBlockMulti. This does **not** claim that the full production tooltip and
  persistent-data dependency graph compiled or ran.

## What is established

1. All four exact constructors retain name, registry identity, texture,
   hardness, resistance and creative-tab reference. Native resistance getters
   match the actual archived vanilla setter/getter scaling, not a guessed value.
2. Every actual block has one property-free default state. Metadata **0**
   decodes/encodes losslessly, with real native global state-ID and registry
   identity roundtrips. All sixteen input values are tested: inputs **1–15
   collapse to zero**. This is a documented limitation, never a nonzero parity
   result or authority to migrate nonzero placements.
3. Actual World.setBlockState and Chunk storage place/read 64 states. Genuine
   AnvilChunkLoader write/read methods plus CompressedStreamTools compression
   roundtrip the section data into a fresh actual Chunk/World. This tests native
   chunk persistence codecs, not a handwritten state serializer. Region-file
   filesystem IO, save-version migration and full server saves are not run.
4. Actual block drops and native pick return one corresponding registered item
   with damage zero. Pinned original vanilla bytecode independently confirms
   quantity one and damageDropped zero for all sixteen metadata values; original
   getDamageValue bytecode reads world metadata and calls damageDropped.
5. Real placement, same-state updates, replacement and removal create no tile
   entities or tile NBT. No nonexistent tile-refresh behavior is invented.
   Lighting propagation, generation, entity ticking and networking are outside
   the preloaded real-chunk fixture.
6. The archived HBM Generic/Base class declarations confirm no original
   metadata/drop/pick/tile overrides. The complete original HBM RNG instruction
   stream equals the generated slice after normalizing only fixture owners and
   debug-label representation. An independent java.util.Random oracle checks
   20,000 choices, every nextInt(20) branch and RNG post-state for 200 seeds.
7. Native DefaultStateMapper yields exactly one `#normal` key per block.
   Actual LegacyBaseBakedModel and FaceBakery yield all six correctly oriented,
   sprite-backed unit-cube faces and six inventory faces. Full
   LegacyBaseModelHandler registry/bake integration is source-audited only.
8. A real Forge TextureStitchEvent invokes the complete production callback.
   Actual PNG decoding, mip creation, stitching, GL upload and pixel readback
   preserve every concrete texel, on two reloads. Fresh icon wrappers are
   required; native TextureAtlasSprite reuse is allowed. All four PNGs are
   byte-identical to the archived original HBM JAR.

## Remaining limits

Full ModBlocks/Library compilation, production ItemBlockBlastInfo/tooltip runtime,
whole-client model-event startup, dedicated server, multiplayer, world generation,
lighting and live gameplay remain **NOT_RUN**. Nonzero BlockGeneric metadata
preservation is specifically **not implemented**. This evidence only supports
metadata-zero construction/placement, together with the sibling selection proof.

The import-safe Python API exposes `PRODUCTION`, `generate_slices(Path)`,
`ConcreteConstructionSlice.initialize()/blocks()` and
`ConcreteRandomSlice.getRandomConcrete()/seed(long)/nextLong()` to that proof.

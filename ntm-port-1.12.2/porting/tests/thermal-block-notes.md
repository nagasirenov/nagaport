# Thermal block and native baked model parity

Verified on 2026-10-19 with native Minecraft 1.12.2 / Forge 14.23.5.2768, Java 8:

- 9,006 complete-block lifecycle assertions passed.
- 411,490 native model/render assertions passed.
- Source guard passed: ten pristine source provenance hashes, 24 unchanged assets (23 PNGs and the original OBJ), and reviewed complete production source/model-state hashes.

## Run

Source only, no Java or sibling checkout required:

```sh
python3 porting/tests/thermal-block-source-test.py
```

Using a target compile classpath resolved from the configured Forge project:

```sh
python3 porting/tests/thermal-block-native-probe.py \
  --java-home /path/to/java8 \
  --classpath "$TARGET_COMPILE_CLASSPATH"
```

Existing local toolchain invocation used for the passing run:

```sh
python3 porting/tests/thermal-block-native-probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)"
```

Portable isolated Gradle task (does not alter source sets or compileJava):

```sh
gradle --offline -I porting/tests/thermal-block-probe.init.gradle testPortThermalBlocks
```

The Gradle entry point resolves the project's compile classpath. The reported native run used the direct Python invocation, not a claim that the entire mod compiles.

## What runs

The probe compiles the complete five production blocks, native base class, both complete production baked models, their lighting/transformation helpers, production custom-model handler, and production OBJ parser/mesh classes. There are no generated net.minecraft or net.minecraftforge classes and no extracted block/model method stand-ins.

Controlled NTM collaborators are listed literally in the Python harness: ModBlocks identities, MainRegistry GUI owner, inventory tile classes, IBlockSealable's one method, TileEntityProxyCombo capability flags, Compat's native coordinate lookup, and ItemStackUtil's brick spill invocation counter. Four exact production shouldRefresh methods are copied into the controlled tile collaborators and their SHA256 hashes are printed. Their algorithms are not rewritten by the test.

Lifecycle coverage uses actual World.setBlockState and Chunk tile creation/removal, plus a bounded native World subclass recording emitted drops and particles. It covers:

- Every metadata 0..15, four active/off identities, original material, bounds and render shape.
- Original asymmetric drop/pick rules and registry-configured light values. The extension retains full collision/selection bounds while all six native face shapes and side-support checks remain non-solid, with no torch support.
- All sixteen neighbor masks, yaw orientations, custom names, native tile alias preservation through metadata edits and active swaps.
- Seeded item split/drop positions, velocities, metadata and NBT conservation, including final depleted stacks.
- Exact particle types, order, positions, RNG consumption, and di-furnace covered chimney position.
- Derived covered state without changing persisted metadata; extension proxy inventory/fluid flags and core lookup.
- Client/server/sneaking/missing-tile activation branches that do not open a GUI.

Model coverage is independent of production texture selector methods. The Java oracle transcribes the pinned pristine selector expressions and parses the original OBJ text independently, computing source triangle normals instead of comparing production geometry against itself. It covers:

- Every family, metadata, active, world/item and covered combination; six distinct culled cube faces and no duplicated general item faces.
- Face sprites, native quad vertex positions, outward winding, original cube UV orientation with native 0.1% inset, normals, tint/diffuse flags, and particle selectors.
- All twenty extension source triangles and exact group sprite selection, original V flip, no added UV inset, repeated third vertices, world-only shade byte truncation, and item cross-product normals.
- Real Forge VertexLighterFlat and vanilla BlockModelRenderer.renderModelFlat feeding real BufferBuilder output. All eighty vertices retain original precomputed shade and sample block-position light, not deliberately different neighbor light.
- Native default state mapper and ModelBlockDefinition JSON parsing, all 160 cube world variants plus extension, actual ModelLoaderRegistry custom-loader selection and IModel.bake, exact atlas dependency sets, and 144 real ItemModelMesher metadata registrations.
- Both folder and ZIP resource packs, exact-case PNG presence/decoding, actual native resource reload. A controlled temporary resource pack changes one OBJ vertex, verifies the new geometry after reload, then restores and verifies the original, without modifying production assets.

## Boundaries

Atlas sprite rectangles are controlled; CPU-side native ModelLoader initialization runs, but atlas GL upload and full game client startup do not. An Unsafe-created native Minecraft holder supplies the real resource manager without launching a window. The inventory tiles and listed NTM collaborators are controlled, so this is not integrated thermal tile ticking, networking, actual GUI opening, world persistence or chunk reload, or whole-mod compilation. Brick spill behavior proves its existing helper is invoked, not that helper's separate implementation. No screenshot, visual playtest, or live graphical acceptance is claimed.

The independent tests caught two real implementation issues before acceptance: DOWN cube V orientation and Forge's native block/ model-location prefix. Production was corrected; no geometry oracle was relaxed to pass those failures.

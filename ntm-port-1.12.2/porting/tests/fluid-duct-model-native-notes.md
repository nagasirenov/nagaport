# Native standard fluid duct model parity probe

Run from the repository root with the already cached native Forge 1.12.2 classpath and JDK 8:

```sh
python3 porting/tests/fluid-duct-model-native-probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)" \
  --original-root /workspace/shared/nagaport/ntm-original-pristine
```

No downloads, installs, network calls, Minecraft stubs, or legacy Forge facades are used.

When the original client is already cached, add `--legacy-client /tmp/nagaport-legacy-client.jar` to independently execute the actual original Tessellator integer/float color setters. The optional test pins Mojang's original 1.7.10 client SHA-256 `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce` (manifest SHA-1 `e80d9b3bf5085002218d4be59e668bac718abbc6`). It runs in a separate subprocess and classloader, respecting the original signed jar; that jar is never placed on the native model probe's classpath. The test verifies integer and float saturation, negative/overflow channels and the actual pipe shader edge.

Successful output is retained in `porting/native-fluid-duct-model-test.log`.

## Independent original oracle

The harness pins the original `RenderTestPipe`, `ObjUtil`, `FluidDuctStandard`, `IBlockMulti`, `Fluids`, OBJ, and all six texture hashes. The native assets must match the original bytes exactly.

Original world selector expressions execute unchanged in the oracle. They cover the zero-connection six arms, single-connection opposite-axis pair, reversed Z-arm names, and eight conditional corner groups. Original inventory render calls determine the four horizontal base groups followed by four overlay groups. World ordering remains each group's base followed immediately by its overlay.

The original `ObjUtil` shadow/tint expressions execute unchanged. A small recording adapter preserves the legacy Tessellator's channel clamping after float-to-integer conversion. Expected RGB multiplies each original byte by shade before truncation; it is not reconstructed by multiplying an already-truncated grayscale color.

An independent parser reads the pinned original OBJ rather than production mesh output. Expected vertices retain source face/corner order, V inversion, float-before-double face normal arithmetic, triangle vertex duplication, atlas UVs without contraction, signed-byte item normals, original 180-degree Y rotation and 1.25 inventory scale, and centered native block coordinates.

## Native coverage

- Complete production renderer, baked model, custom model loader, connection/color properties, quad factory, block-position-lighting helper, camera transforms, and OBJ model/parser classes.
- All 64 masks and eight RGB colors, including black, white, very low RGB bytes, unequal channels, source NONE color, fallback magenta, and an integer with high alpha bits, across all three skins.
- Every quad's ordered positions, UVs, layer sprite, packed color, item normal, native format, tint index and diffuse flag.
- Separate inventory output, immutable cached variants, RNG-independent routing, general-bucket-only geometry, absent-state/property fallback, bounded cache eviction and regeneration without mutating retained lists.
- Real vanilla flat block rendering and Forge `VertexLighterFlat`, with deliberately different neighbor light, proving the original block-position light and no second directional shade or tint.
- Actual client-only model-registry subscription, all sixteen persisted metadata state routes, correct metadata modulo-three skin mapping, both declared atlas textures, all normalized loader prefixes, all item registrations and real `ItemModelMesher` routing.
- Exact-case PNG decoding and OBJ access from both a resource directory and a generated resource jar.
- A reload changes original OBJ vertex coordinates and verifies fresh model/mesh output, unchanged retained lists, and restored original geometry after the original pack returns.

## Controlled boundaries

The NTM `FluidDuctStandard` state container and `Fluids.NONE` collaborators are deliberately controlled. The ItemBlock fixture preserves the production owner's subtype flag and metadata behavior. The unchanged production `Library.canConnect` method is extracted solely to compile the shared `ConnectionState` helper; energy connection behavior is not the subject of this probe. Complete production Base/Standard behavior is independently covered by the separate fluid block probe.

All Minecraft and Forge behavior is native. Headless setup creates a minimal Minecraft resource-manager holder and controlled atlas rectangles; it does not claim a full-mod build or live graphical acceptance.

The initial run exposed a production overflow defect, fixed before the successful final run: diagonal pipe faces can produce original brightness above one. Wrapping RGB through `& 255` turned those faces almost black instead of retaining legacy Tessellator saturation. The oracle retains the required clamp and cannot accept that wraparound.

The affected original asset contains eight over-one-shade faces (four in `pX` and four in `nX`), with shade approximately 1.015432 and a pre-clamp white channel of 258. The original modern cable asset has no face above shade one. The color adapter clamp is also directly corroborated by the cached original Tessellator bytecode: float setters multiply by 255 then convert to integer; the four-channel setter clamps to 0..255 before packing.

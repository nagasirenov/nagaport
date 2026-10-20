# Native cable parity probe

Run from the repository root with an existing JDK 8 and native Forge 1.12.2 classpath:

```sh
python3 porting/tests/cable-native-probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)" \
  --original-root /workspace/shared/nagaport/ntm-original-pristine
```

The original-source location defaults to the sibling `ntm-original-pristine` checkout. No downloads, installs, network calls, legacy Minecraft facades, or Forge stubs are used. The test fails if any pinned original renderer/block/OBJ hash changes or if the production cable OBJ no longer exactly matches the original asset.

## Independent original oracle

- Classic inventory and world geometry execute the original renderer's branch, shading, UV and vertex expressions through a recording adapter. Only the old Tessellator/icon plumbing is replaced. Every original conditional world branch is exercised under all 64 masks. The distinct 22-quad inventory geometry is not approximated by a world mask.
- Modern cable group selection executes the original selector expressions. A separate small OBJ parser reads the pinned original file rather than the production `ObjMesh` output. It preserves source order, loader V inversion, float-before-double face-normal math and triangle-to-quad vertex duplication.
- Expected inventory positions apply the original 1.25 scale, modern 180-degree Y rotation and native centered-coordinate conversion. Expected normals preserve original signed-byte truncation; expected world colors preserve original float-to-byte truncation.

## Native coverage

The probe compiles complete production `BlockCable`, connection state, both cable renderers, baked model, quad factory, block-position lighting helper and model registration/loader classes against the real Forge/Minecraft jar.

It checks:

- All 64 modern and classic connection masks, including exclusive `CX`/`CY`/`CZ` through-segments and the original reversed Z-arm mesh names.
- Every quad's ordered XYZ, uncontracted atlas UV, original shading, item packed normal, vertex format, tint and diffuse flag. General-bucket-only routing, cached lists and list immutability are exercised.
- Real vanilla flat rendering and Forge `VertexLighterFlat`, with intentionally differently lit neighbors, to prove original block-position light and prevent double shading.
- All 16 metadata values across both blocks, all 64 masks, accepting block connectors, tile-only connectors and block-to-tile fallback, opposite-side arguments, height guards, merged selection/collision bounds and non-solid support semantics.
- Real native `World`/`Chunk` tile creation, six-neighbor resolution, next-render neighbor removal and world-offset collision callbacks.
- Actual model-registry event subscription, metadata-ignoring state mapping, exact packaged blockstate variants, model paths including the native `models/block/` loader route, both item identities at every metadata, and actual `ItemModelMesher` routing.
- Exact-case sprite existence and PNG decoding from both a resource directory and a generated resource jar. Resource reload changes an original OBJ vertex coordinate, proves a fresh model/mesh, then restores the original asset.

## Controlled boundaries

The NTM block registry fields and cable tile collaborator are narrowly controlled. The real production `Library.canConnect` method is extracted unchanged into an otherwise minimal NTM fixture because unrelated methods in `Library` still use unported APIs. The energy connector interfaces and direction enum are complete production classes. All Minecraft and Forge behavior comes from native classes.

Headless loader setup uses a minimal `Minecraft` holder and controlled atlas rectangles, avoiding a live OpenGL context. This is CPU-native source/behavior evidence, not a claim that a complete mod builds or that a graphical client has been visually accepted.

The first run exposed two production defects that were corrected before acceptance: native Forge rounded diagonal item normal bytes instead of preserving the original Tessellator truncation; and model ownership initially used unprefixed registry names instead of the actual `tile.red_cable` and `tile.red_cable_classic` identities.

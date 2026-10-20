# SolarMirror native closure proof

Run from any directory, supplying a complete port source/resources module, the pristine original tree, JDK 8, and the pinned native Forge 1.12.2 development classpath:

```sh
python3 porting/tests/solar-mirror-native/run.py \
  --module /path/to/port-module \
  --java-home /path/to/jdk8 \
  --classpath "$(cat /path/to/native-classpath.txt)" \
  --original /path/to/ntm-original-pristine
```

There are no dependencies on another test directory, build output from this proof, or machine-specific paths in the runner. Compilation and reference source adaptation happen in a fresh temporary directory. `--original` is required; source/resource SHA-256 pins verify that the executed reference is the reviewed original and that the port retains the original OBJ/atlas texture. `manifest.sha256` verifies this evidence bundle.

## Boundary

The runner compiles thirteen complete production sources: SolarMirror, SolarMirrorModelState, TileEntitySolarMirror, SolarMirrorPosition, RenderMirror, SolarMirrorGeometry, SolarMirrorBakedModel, SolarMirrorModelHandler, RefStrings, ObjMesh, ObjMeshParser, AtlasObjGeometry and BlockPositionLightQuad. Production owner classes are not copied, patched, replaced, truncated, or shadowed by fixtures. The compiler source path is restricted to exactly two explicitly named outside-owner collaborators:

- TileEntityTickingBase: the native TileEntity/ITickable base, dirty marker and network call recorder. No real network dispatch is claimed.
- TileEntitySolarBoiler: a native TileEntity destination carrying the heat integer and source-position set. No full boiler behavior is claimed.

Minecraft/Forge classes come from the supplied native classpath. Bytecode disassembly rejects the known legacy coordinate, CPW, compatibility, IIcon and ISBRH references in the compiled production closure. The test bootstraps actual native Forge registries, event bus, state mapper, custom model routes, ItemLayerModel and native quad/vertex APIs without starting a game or drawing through OpenGL. Unsafe is used only to allocate a test Minecraft singleton for its resource manager, not to replace any owner method.

## Independent original renderer oracle

The runner reads and compiles the entire actual pristine RenderMirror.java, changing only its package and import declarations into the isolated `solaroracle` test namespace. Every method body, including the full world callback and printMirror, is unchanged. It also extracts and compiles all three pristine ObjUtil.renderPartWithIcon overloads verbatim. No production render helper is used to calculate expected geometry.

OriginalMirrorOracle.java supplies explicit reference-only old API adapters, a capturing tessellator, an independent OBJ reader, and the original scalar Vec3 operation semantics. The parser preserves source group order, the loader V flip, float coordinate subtraction, and float-square-root normal normalization. Minecraft's native MathHelper lookup table supplies the historical sin/cos values. The oracle is an adapted pristine-source execution proof, not a run of the whole old game, old renderer binary or old loader. The originals of both renderer/helper and loader/face sources are hash pinned for provenance.

## Covered outcomes

- 1,058 rendering cases and 43,132 complete native vertices, with bit-exact positions, uncontracted atlas UVs, packed color and original source part order.
- Real original asset plus asymmetric triangle/quad fixtures, cross-face quad streams, duplicate groups (last exact name wins), missing groups and case-sensitive names.
- Static and elevated targets in all yaw quadrants, direct vertical targets, deterministic randomized targets, positive and negative integer-product overflow (including collapsed point quads from NaN lookup), X-then-Y rotation about y=1, original float pitched UV arithmetic versus double named-part UV arithmetic, and unrotated pitched normal shading.
- Native BLOCK format, no extra tint/diffuse/AO, block-position lighting pipeline normals, absent/wrong tile invisibility, immutable extended-state primitive capture, absolute-Y branch selection even when relative dy overflows, and culled-face empty lists.
- Complete actual owner factory/shape state and tile sunlight gates, below/equal target-height distinction, boiler heat destination offset, sync cadence/range, target dirty/sync mutation, original wire order, native NBT return and keys, render bounds/distance, client beam collection and redraw.
- Actual model registry event routing, original blockstate JSON route, all 16 flat item metadata routes, actual native generated ItemLayerModel baking, and resource reload invalidation/reparse.
- Unrelated IDs/namespaces and malformed loader paths stay excluded. Separate fresh JVMs prove that a subclass or unrelated block occupying the exact owner registry ID is not captured.

The final full run passed 415,098 assertions across the main and two negative-owner JVMs. `last-result.json` records the result and production-source hashes for that run. The runner retests current supplied production sources rather than pinning their implementation. This is bounded CPU/native lifecycle evidence, not live-game visual validation, a full mod build, real packet delivery, or the full solar boiler dependency closure.

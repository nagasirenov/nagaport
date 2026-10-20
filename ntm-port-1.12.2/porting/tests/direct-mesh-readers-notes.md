# Direct OBJ geometry readers: bounded migration

## Production scope

Only these two production files change:

- `src/main/java/com/hbm/render/util/ObjUtil.java`
- `src/main/java/com/hbm/render/block/RenderMirror.java`

The old `S_GroupObject`, `S_Face`, Forge `Vertex`, and Forge
`TextureCoordinate` reads become reads of the existing mod-owned `ObjMesh`
types through `HFRWavefrontObject.getMesh().parts`. Faces supply `normal` and
an ordered `corners` list; each corner supplies `position` and `uv`. UV `u/v`
becomes `x/y`, with the same float field types. This adds no compatibility
geometry classes, fake Minecraft/Forge APIs, renderer no-ops, source exclusions,
or changes to callers, public signatures, or the `Tessellator` argument.

The existing HFR native-mesh migration supplies `getMesh()` separately. This
reader change alone is not a working native block renderer.

## Preserved behavior

The audit reverses every allowed substitution and requires the entire resulting
source to match the pinned baseline byte-for-byte, including imports, comments,
method signatures, overload forwarding, and legacy body calls. In particular:

- Whole-model traversal keeps source part, face, and corner order.
- Selected-part and mirror selection remain last-match and case-sensitive,
  with an immediate return on a missing part. They do not use the native
  renderer's case-insensitive, all-matches selection helper.
- Face normals, not per-corner supplied normals, feed the existing normal and
  brightness calculations. Normal rotation still omits roll where it did before.
- Existing formulas, rotation order, casts, tint state, minimum brightness,
  mirror pivot offset, and mirror aim calculations are unchanged.
- Whole-model and mirror UV expressions retain `* 16`; selected-part UV
  expressions retain `* 16D`. These preserve float versus double evaluation.
- Whole-model rendering and `RenderMirror` still duplicate corner index 2 even
  for a quad. Selected-part rendering duplicates only triangle corner index 2.
  The differing guards and unusual five-vertex quad output are intentional
  original behavior, not a newly corrected triangulation policy.

## Verification

Run the portable source audit from any working directory:

```sh
python3 /path/to/ntm-port-1.12.2/porting/tests/audit-direct-mesh-readers.py
```

The manifest pins baseline commit
`a60fe7806f90257b5dbfbe9a62f46b887946bf93` and SHA-256 fingerprints of both
baseline and migrated readers. The audit reads that commit from the local Git
repository, verifies both fingerprints, checks exact forward and reverse
substitutions, and rejects seven deliberate semantic mutations covering formula,
rotation order, case matching, duplication guard, UV precision, tint, and null
return. It does not rewrite source files, mutate Git state, or invoke Gradle.

To additionally compile the actual production `ObjMesh` and `ObjMeshParser`
with the dedicated CPU-only geometry fixture using an existing JDK:

```sh
python3 /path/to/ntm-port-1.12.2/porting/tests/audit-direct-mesh-readers.py \
  --java-home /path/to/existing/jdk
```

The fixture builds in a temporary directory removed on exit. No Minecraft,
Forge, GL, substitute APIs, downloaded software, or persistent build output is
involved. Source fingerprints for both geometry classes and the fixture are
printed. Its 18 checks exercise actual parser geometry with duplicate names,
case differences, triangles, quads, empty/missing parts, face versus supplied
corner normals, unbounded UVs, and float-versus-double UV evaluation. The
fixture's traversal forms mirror the audited readers; it does **not** execute
the two production renderer classes. The source audit, not this fixture, is the
evidence that their other source logic remains unchanged.

Verified on 2026-10-19 with the existing Java 8 toolchain at
`/workspace/shared/nagaport-toolchain/java8`: both exact reverse-source checks,
all seven mutation rejections, and all 18 CPU geometry checks passed.
`git diff --check` passed for the two production readers. No shared Gradle,
full-class compilation, full build, or game rendering was attempted.

## Explicitly remaining unported code

The native mesh data migration deliberately leaves these old renderer/body APIs
visible and unresolved; it must not be reported as a native renderer compile:

- `ObjUtil`: `Tessellator.setNormal`, `setColorOpaque_F`, `setColorOpaque`, and
  `addVertexWithUV` calls remain, as do the original `Tessellator` arguments.
- `RenderMirror`: the old `ISimpleBlockRenderingHandler` and `RenderBlocks`
  integration, `Tessellator.instance`, brightness/color/normal/translation/vertex
  calls, and renderer override-texture access remain.
- `RenderMirror`: old block icon and mixed-brightness calls, coordinate-based
  world metadata/tile lookup, tile `xCoord/yCoord/zCoord` access, and legacy
  render-ID plumbing remain.

The existing caller graph, icon pipeline, tint/lighting behavior at runtime,
texture atlas integration, HFR resource reload/display-list lifecycle, and
Minecraft block-render registration are outside this test's execution scope.

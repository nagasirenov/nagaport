# Native HFR/HMF/VBO continuation assessment

The HMF slice below was completed in checkpoint 7 (`a60fe7806f90257b5dbfbe9a62f46b887946bf93`). The later coordinated shared-root implementation is described in `hfr-cpu-migration-notes.md`; the isolated HorsePronter recommendation below is historical, not the current plan.

Assessed 2026-10-19 against checkpoint 6 (`f15a65995a9fcd20a8b817d190f540dd0fa0e577`) before the bounded Tom HMF migration below. Read-only assessment, except this document and the separately authorized HMF implementation. No network/source replacement, CE base, source exclusion, old Forge API emulation or no-op VBO conversion.

## Completed checkpoint 7 HMF boundary (historical recommendation)

Complete the real HMF dependency boundary now: replace both `ResourceManager.tom_main` and `tom_flame` with eager native mesh snapshots, migrate the two actual drawing sites in `TomPronter`, retain `Clock` and `HmfController`, remove the obsolete loader registration and retire exactly `HmfModelLoader`, `HbmModelObject`, `HbmFace`, `HbmGroupObject` after their last callers disappear. This removes real production dependencies on `IModelCustom`, `IModelCustomLoader`, legacy `Vertex` and `TextureCoordinate`, rather than reproducing their obsolete API.

This is preferable to attempting the whole HFR root in the same change. The supplied Tom meshes are each one fully attributed triangle group, so current `ObjMeshRenderer.drawParts` has the same one-batch boundary as their old `renderAll`. This is a bounded equivalence for these actual resources, not a universal `renderAll` implementation. General multi-group resource replacements remain outside this acceptance claim. The shared mesh core should remain unchanged in this slice.

The two assets, unchanged:

| Asset | Geometry | Source group | Original shading | SHA-256 |
|---|---|---|---|---|
| `models/weapons/tom_main.obj` | 106 triangles, 318 corners | `Cube_Cube.001` | Plain Forge flat, inset 0.0005F | `13f91982a16bbabd39fa94c8f9da7578c9b39b805d351c5be3f1af2aa3b9775f` |
| `models/weapons/tom_flame.hmf` | 60 triangles, 180 corners | `Cube.001_Cube.003` | HMF flat, inset 0.0005F, animated V | `ac7ce0b81645307777c4ede34bb72c8dc33535235b89e6a2b8940c2f6fbbffdd` |

Both contain UVs and vertex normals on every corner. `tom_main` is **not HFR smooth shading**: it was loaded through plain `AdvancedModelLoader`. `tom_flame` has source V range -0.434961 to 1.485286. Preserve raw UV wrapping, V flip and the per-face centerward inset; do not atlas-clamp it.

## Exact pre-migration dependency inventory

Counts below exclude comments while preserving Java string literals. They count syntactic source occurrences, not javac diagnostics or distinct runtime instances.

- `IModelCustom`: 541 token mentions in 34 files, including 34 active imports. `ResourceManager` accounts for 451 mentions: one import and 450 final fields. The old diagnostic count is not this inventory.
- `IModelCustomNamed`: 23 token mentions in six files: its own declaration, HFR CPU/VBO implementations, `ResourceManager`, `DoorDecl`, `RenderDoorGeneric`. The twelve named ResourceManager fields are non-final and distinct from the 450 above.
- HFR construction: 393 calls in eleven files. `ResourceManager` 378; `RenderPlushie` six; one each in `ParticleSkeleton`, `RenderDemonLamp`, `RenderSnowglobe`, `RenderFloodlight`, `HorsePronter`, `RenderWormHead`, `RenderSiegeTunneler`, `RenderWormBody`, `RenderCoin`.
- `asVBO`: 293 calls in six files. `ResourceManager` 283; `RenderPlushie` six; one each in `ParticleSkeleton`, `RenderSnowglobe`, `HorsePronter`, `RenderCoin`.
- `noSmooth`: 77 calls, all in `ResourceManager`; 26 also call `asVBO`, 51 do not.
- `AdvancedModelLoader.loadModel`: 149 active calls in thirteen files, including 137 in `ResourceManager`. One live HMF asset (`tom_flame`) and 148 OBJ calls. The commented `sphere_uv_anim` is not a caller.
- Direct legacy `Vertex` and `TextureCoordinate` imports: seven files each, the same set: HFR loader, VBO loader, `S_Face`, HMF loader/model and face, `ObjUtil`, `RenderMirror` (specifically `HbmModelObject` and `HbmFace`, not `HmfModelLoader`).
- Outside the loader package, direct HFR group geometry access exists only in `ObjUtil` (two loops) and `RenderMirror` (one loop). There are 186 HFR casts in 39 files.
- External `ObjUtil.renderWithIcon` calls: 44 in 22 files; `renderPartWithIcon`: 143 in twenty files. Those sets overlap.
- External direct calls to HFR/HMF `tessellateAll`, `tessellatePart`, `tessellateOnly`, `tessellateAllExcept`: zero. Shared batches nevertheless matter inside `renderAll` and inside caller-owned block/ObjUtil emission.
- `new ModelRendererObj`: 182 sites in 24 files. This is a substantial consumer of typed model handles, not a reason to recreate `IModelCustom` under a Forge package.

The HMF removal audit before editing found only one caller chain: `ClientProxy` registration -> `HmfModelLoader` -> `HbmModelObject` -> `HbmGroupObject`/`HbmFace`. `ResourceManager.tom_flame` is the sole active `.hmf` load; `TomPronter` is its sole geometry consumer. `RenderBullet` separately uses `tom_flame_tex`, which remains untouched. `tom_main` also has only the Tom geometry consumer.

## Contracts that must precede broad HFR conversion

### Smoothing is captured at parse time, not draw time

`HFRWavefrontObject` initializes `smoothing=true`; its constructor loads and parses faces before returning. Each `parseFace` constructs `new S_Face(this.smoothing)`. The chained `noSmooth()` changes only the loader's future-parse flag. Existing faces remain smooth until a subsequent reload creates new faces. `S_Face` sets smooth per-corner normals **only in its textured branch**. Untextured smooth faces do not use their supplied `vn`; within a shared batch they retain previous current normal/UV state where those attributes were already enabled. Flat faces set the first-three-corner face normal regardless of UV presence.

Crucially, target `SimpleReloadableResourceManager.registerReloadListener` immediately invokes the new listener. The actual mapped bytecode was inspected, not inferred. `ClientProxy.registerRenderInfo` registers HFR reload after tile/item/entity/block renderer registration and `Jars.initJars`. Thus models already initialized can be reparsed with `smoothing=false` after `noSmooth()` before the first visible draw; later-initialized models wait until the next reload. Preserve the initialization/registration sequence. Neither globally smoothing every `noSmooth` model nor eagerly flattening its initial parse reproduces this behavior.

### Part order, duplicates, primitive mode and batches

- HFR/HMF `renderAll` starts one Tessellator batch, using the **last current group's** draw mode, then visits all source groups.
- Their `renderPart`, `renderOnly` and `renderAllExcept` start individual group batches. Source group order is outermost; requested names are inner, case-insensitive. Duplicate requested names repeat emission. Duplicate source names remain separate groups.
- VBO selection uses the same ordered, case-insensitive nested loops, without combining group buffers. `getPartNames` retains duplicate names.
- `ObjUtil.renderPartWithIcon` is different: it selects the **last case-sensitive** matching group. Do not replace it with `selectParts` without preserving this difference.
- Existing native `append` has an explicit triangle-to-quad duplication policy intended for icon rendering. It is not the raw one-corner-per-source-corner emission of general legacy `renderAll`. General shared rendering needs an explicit batch topology policy; do not silently triangulate a quad, pad every triangle, or split batches and reset current attributes.
- The original allows different homogeneous primitive sizes in different groups, and `renderAll` still uses its last group's mode. Preserve or explicitly scope this oddity; a broad rewrite must not silently reinterpret it.

### Native block geometry is a separate coordinated boundary

`ObjUtil` and `RenderMirror` own atlas UV interpolation, normal-derived color, world brightness, transforms and a caller-owned Tessellator batch. The current native mesh append intentionally refuses color/lightmap formats. It cannot replace those utilities as-is.

Preserve their actual distinctions: all-part versus selected-part brightness formulas; float `t.u * 16` versus double `t.u * 16D`; selected-part tint state; source rotation order X(roll), Z(pitch), Y(yaw), with normals not rotated through roll; and the different triangle-duplication guards. `RenderMirror` uses its own aiming transform and lighting. A loader-only type substitution would leave these actual rendering contracts unresolved.

### Real VBO representation and lifetime

All 293 active VBO calls resolve to 293 distinct existing assets. Every face is a triangle and every corner has a source normal. Six contain corners with no UV: `machines/liquefactor.obj`, `machines/solidifier.obj`, `machines/chemical_factory.obj`, `weapons/detonator_laser.obj`, `armor/AJR.obj`, `armor/no9.obj`. None of these 293 assets has duplicate case-insensitive source group names or nonzero UV W. Tests still need synthetic duplicate groups and nonzero W to preserve the loader contract.

`HFRWavefrontObjectVBO` uploads three **float3** arrays per group: position, UV including W, and source vertex normal. Missing UV becomes `(0,0,0)`, not the CPU path's current-attribute carry. It ignores captured smoothing and always uses per-corner source normals. There is no 0.0005F UV inset. It allocates/uploads eagerly, enables vertex/texture/normal client arrays, draws triangles, disables them, and unbinds the array buffer. `destroy` deletes every group's three handles and clears the group list. The global insertion-ordered model set and VBO->CPU map keep owners for reload.

Do not feed `DefaultVertexFormats.POSITION_TEX_NORMAL` bytes to a replacement VBO and call this equivalent: that format has two UV floats and signed-byte normals. Retain native float3 UV/normal precision using explicit native format/buffers, or demonstrate an exact equivalent before changing representation.

The actual target `VertexBuffer` API offers constructor(VertexFormat), bindBuffer, bufferData(ByteBuffer), drawArrays(int), unbindBuffer and deleteGlBuffers. Constructor allocates immediately via `OpenGlHelper.glGenBuffers`. bufferData uploads static data and computes count as `byteBuffer.limit()/vertexFormat.getSize()`; supply a correctly flipped, zero-position buffer. deleteGlBuffers guards IDs below zero and marks deletion with -1. It does not set vertex/UV/normal pointers for the caller. A real implementation can retain three float buffers via native OpenGlHelper, or use a verified float3/float3/float3 interleaved format plus explicit pointer offsets. It must not replace `asVBO` with a CPU alias or a no-op.

Old HFR reload is two ordered phases: destroy/reparse every CPU model, then destroy/reupload every VBO. It uses Minecraft's manager rather than the listener argument. Opening-resource IO failure is swallowed after CPU destruction, so that model remains empty; parse errors become unchecked ModelFormatException and can abort before the VBO phase. These are actual failure semantics, unlike checkpoint 6 `ReloadableObjMesh`'s atomic retain-last-good explicit reload. Do not casually claim the latter is a drop-in reload owner. A broader migration must document its failure decision, close resources, maintain source ownership and perform GL operations on the render thread. No lifecycle change is made in the Tom slice: neither old Tom model subscribed to HFR reload, and neither new snapshot is registered.

## Superseded isolated HFR/VBO recommendation (historical assessment)

Use `HorsePronter` as the next isolated real VBO consumer: one model, four source call sites, fourteen source groups and 264 triangles, no ResourceManager geometry dependence. Preserve all existing matrices, pose/visibility logic and textures. Implement a concrete native mesh/VBO owner with source snapshot, ordered part selection, real eager upload/delete and resource reload registration at the existing client lifecycle. Add the native listener alongside the existing HFR listener for the unmigrated models; do not redirect all 293 VBO sites prematurely. Test source normal floats/UV W, missing UV zero fill, repeated part selection and upload payloads headlessly; validate actual GL allocation, draw, deletion and reload in a client before claiming GPU acceptance.

`RenderCoin` is an alternate smaller one-group VBO slice (356 triangles), but its old Render superclass constructor/registration also needs a native RenderManager factory. `HorsePronter` avoids that unrelated renderer lifecycle expansion and exercises actual multi-part selection.

For the eventual root removal, evolve a mod-owned model/mesh contract only when all concrete backends and consumers exist: native CPU mesh snapshot, ordered draw selections, optional real VBO ownership and explicit reload lifetime. Remove unused legacy `getType`/loader discovery rather than cloning old Forge interfaces. Keep the plain Forge flat, HFR parse-captured smooth/flat, and HMF timed policies distinct. Coordinate the 34 typed consumer files, twelve named door fields, MissilePart/ModelRendererObj/RenderMiscEffects and the ObjUtil/mirror native block-emission boundary. Declaration-error reduction is not acceptance.

## Exact timing and source-preservation acceptance for Tom

- `Clock.time_ms` is a cached long; `ModEventHandlerClient.onRenderWorldLastEvent` calls `Clock.update()` at source line 1356 in the inspected snapshot. It is not a newly sampled wall clock in HMF rendering.
- Old `HbmModelObject.tessellateAll` assigns `float currentTime = Clock.get_ms()` once per call, then passes the float to every face. `HbmFace` converts that float to double before `% modoloMod`, divides by `quotientMod`, then casts the offset to float.
- Tom makes twenty separate flame calls. Preserve one cached-clock read and long-to-float conversion inside each iteration. Do not move it outside the loop or perform long/double modulo before the float conversion.
- Preserve separate `float rot = -System.currentTimeMillis() / 10 % 360;` exactly: unary minus, integer division and remainder happen as long arithmetic before float assignment.
- Preserve `Random(0)`, twenty `nextInt(90)` calls, every push/pop/scale/rotate and texture/GL flag call, and controller `setMod(50000D,2500D)`/`resetMod()` positions. Do not add a new try/finally behavioral rewrite in this bounded migration.
- ResourceManager fields remain eager, in their original positions, with main before flame. No lazy conversion, source texture move or reload subscription.

## Verification commands and evidence

The target jar inspected is `.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`, SHA-256 `8c75037188f87bc62939eefa03c2480551509034f069728de012c09276f8f49e` in this environment.

```sh
J=/workspace/shared/nagaport-toolchain/java8/bin/javap
C='.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar'
"$J" -cp "$C" -c net.minecraft.client.resources.SimpleReloadableResourceManager
"$J" -cp "$C" -c -private net.minecraft.client.renderer.vertex.VertexBuffer
"$J" -cp "$C" net.minecraft.client.renderer.vertex.VertexFormatElement \
  net.minecraft.client.renderer.vertex.VertexFormat net.minecraft.client.renderer.OpenGlHelper
rg -n 'registerReloadListener|registerModelHandler|HmfModelLoader' src/main/java/com/hbm/main/ClientProxy.java
rg -n 'Clock.update|Clock.get_ms|System.currentTimeMillis|setMod|resetMod' \
  src/main/java/com/hbm/{main/ModEventHandlerClient.java,util/Clock.java,render/util/TomPronter.java,render/loader/HmfController.java}
rg -n '\b(HmfModelLoader|HbmModelObject|HbmFace|HbmGroupObject)\b' src/main/java
python3 porting/tests/probe-hmf-mesh.py --audit-only
/workspace/shared/nagaport-toolchain/run-gradle.sh --offline \
  --init-script porting/hmf-mesh-probe.init.gradle testPortHmfMesh --console=plain
```

The old deleted loader sources remain in the untouched supplied `../ntm-original-pristine` tree and checkpoint 6 git history. To reproduce their current-port-before-removal source inspection:

```sh
git show f15a65995a9fcd20a8b817d190f540dd0fa0e577:ntm-port-1.12.2/src/main/java/com/hbm/render/loader/HbmModelObject.java
git show f15a65995a9fcd20a8b817d190f540dd0fa0e577:ntm-port-1.12.2/src/main/java/com/hbm/render/loader/HbmFace.java
```

`probe-hmf-mesh.py` proves an exact reverse-delta hash for the original TomPronter and ResourceManager snapshot, unchanged Clock/controller/assets, and zero remaining references to the four retired classes. This is intentionally a checkpoint-specific source audit; future unrelated ResourceManager edits require re-basing that audit, not claiming an evergreen whole-file hash.

The native task compiles the complete production mesh classes plus actual Clock/HmfController and checks both real assets' emitted BufferBuilder bytes, winding, flat normals, UV flip/inset/wrap, cached-clock float loss and eager snapshots. Its only reused old test code is the independent two-resource byte oracle, not the previous complete 102270-assertion suite. TomPronter/ResourceManager/ClientProxy whole-class native compilation and gameplay remain a separate whole-mod task. No fake ResourceManager class or extracted body execution is represented as that proof. GPU upload/delete, visual alpha/blend/lighting and reload acceptance need a real GL context/client; they are not claimed by these headless tests.

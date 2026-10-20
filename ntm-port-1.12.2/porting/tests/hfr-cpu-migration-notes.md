# Native CPU model and HFR lifecycle migration

Baseline: checkpoint 7 `a60fe7806f90257b5dbfbe9a62f46b887946bf93`. This is the coordinated production root migration, not the earlier isolated HorsePronter recommendation. No old Forge package/interface, placeholder model, no-op VBO, source exclusion, or substitute Minecraft class is introduced.

## Implemented production boundary

- `MeshModel` is the real mod-owned domain contract: immutable source snapshot `getMesh`, ordered `getPartNames`, and the four actually used draw verbs. It intentionally has no obsolete loader discovery, `getType`, or Tessellator API.
- `ObjMeshModel` eagerly loads the actual native resource and uses original plain-Forge flat normals plus 0.0005F UV inset. It does not subscribe to reload. The constructor accepting `IResourceManager` uses the same actual loader as the client constructor, not a parallel test implementation.
- `HFRWavefrontObject` owns native geometry and retains its constructors, future-parse `noSmooth`, `mixedMode`, real `asVBO`, insertion-ordered CPU owner set and VBO-owner map. The smoothing policy is captured after each successful parse. `noSmooth()` does not alter already loaded faces.
- `HFRModelReloader` preserves two phases: destroy/reparse all CPU owners, then delete/reupload every VBO in insertion order. Its actual callback still obtains Minecraft's resource manager, not the callback argument. The explicit native-manager phase method lets the complete production lifecycle execute without a client bootstrap.
- `ModelRendererObj` now consumes `MeshModel`; its matrix/body behavior is unchanged. The coordinated typed-consumer and direct-reader workers have separate source audits.
- `S_Face`, `S_GroupObject`, and `IModelCustomNamed` are retired after no active callers remain. Their originals remain in the pinned source and checkpoint history.
- `ObjMeshVboPayload` and `HFRWavefrontObjectVBO` retain the real GL15 three-float3 backend; see `vbo-mesh-migration-notes.md`. Native packed-byte immediate normals are not substituted for VBO float normals.

## Draw and attribute contracts

`renderAll` is one source-order raw-corner batch with the last source group's primitive mode. No per-group reset, triangulation, or triangle padding is introduced. A trailing empty group retains the old invalid mode -1 at the Java boundary; no bundled OBJ has that topology, and no successful GL behavior is claimed for it. Selected-part drawing uses a separate batch per source group, source-order outer iteration, case-insensitive name comparisons, and repeated requested-name multiplicity. Empty groups and duplicate source names are retained.

The existing caller-owned `append` retains its explicit triangle-to-quad padding convention; new `appendRaw` emits exactly the source corners. Both preserve a supplied cursor across calls. HFR smooth normals are assigned only inside the textured branch, matching original S_Face. A wholly untextured smooth batch is POSITION-only even if its source has normals. Once UVs/normals are enabled in a batch, missing attributes carry the previously emitted values across faces and groups. UV V-flip, unbounded/wrapping coordinates, plain inset and per-corner normal byte conversion are retained.

### Explicitly approved undefined-prefix failure

The user approved a clear error, after being told an unhandled error can crash the client, for the narrow case where a batch emits vertices before a UV/normal attribute is first enabled later in that same batch. Official 1.7.10 Tessellator bytecode confirms those prefix slots were not initialized; they could contain stale values from unrelated previous global draws. See `legacy-tessellator-attribute-evidence.md` for official artifact provenance, SHA-1, exact inspection commands and limitations. It is not valid to assert that zero initialization is equivalent.

The native renderer preflights this case before `BufferBuilder.begin` or GL drawing. Errors identify the resource and affected source group. Selected-part drawing validates **all** selected parts before an earlier valid part can draw. Entirely disabled attributes and defined current-attribute carry remain valid. The existing lower-level caller-owned append API can use an explicitly initialized cursor.

This is a documented compatibility limit for external calls and resource-pack replacements, not universal behavioral parity. The in-repository alias audit identifies 25 direct named-part calls plus one literal `"UFO"` glint route for the five missing-UV HFR models, with no whole-model/array/ModelRendererObj alias routes. Four whole meshes could trigger this limit if called differently: lantern, siege_ufo, charger, lance. Radgen begins with a textured group and carries into its untextured Light group correctly. A source audit cannot establish arbitrary external-mod behavior.

## Loading and reload details

Actual target `SimpleReloadableResourceManager.registerReloadListener` immediately invokes the newly registered listener. Existing registration order is unchanged: models already constructed may therefore be reparsed with smoothing=false after `noSmooth()` before first visible drawing; later models wait for a subsequent reload. No eager flattening or new registration is added.

A resource-open IOException still leaves the destroyed CPU owner empty and continues the CPU loop. An unchecked parse error still aborts before later CPU owners/VBO reuploads. Immutable parsing publishes only a complete snapshot: malformed input does not expose the old mutable loader's partially accumulated arrays. Direct repeated `loadObjModel` calls replace the snapshot rather than reproduce the old append-to-internal-index-arrays oddity. Actual production callers use fresh constructors or destroy-before-reload. These malformed/manual-call differences are explicit boundaries, not claimed exact external compatibility.

Mixed triangle/quad parts remain available as native geometry to the actual direct readers, but HFR draw entry points still reject mixed-mode owners before GL. The sole current mixed asset is rbmk/rbmk_element.obj. Pure quads and cross-group primitive modes retain their source corner count and final mode.

## Actual native verification

`porting/hfr-cpu-probe.init.gradle` adds `testPortHfrCpu`. It compiles the complete production mesh package, HFR CPU/VBO/reloader and ModelRendererObj against the actual mapped Minecraft/Forge and LWJGL classes. `HfrCpuMinecraftTest` executes actual BufferBuilder bytes and native resource APIs. No fake ResourceManager production class, extracted model body, or GL-success stub is used.

`probe-hfr-cpu.py` inventories 393 HFR and 146 plain eager factories, resolving all eight same-file ResourceLocation variables, yielding 531 distinct policy/resource combinations. Every existing resource is checked against an independent text/index oracle for source order, corner positions, flat/smooth normals, UV flip/inset/wrap/carry, native normal bytes and exact primitive corner count. The resource fixture opens the exact normalized `ResourceLocation.getPath()`; it does not provide a case-insensitive alias.

The baseline `models/BombGenericLarge.obj` is absent from the original resources and belongs to the unregistered RenderBombMultiLarge class. Its actual native eager-loading IOException is asserted and reported separately, not replaced with invented geometry or counted as accepted mesh bytes. Parent-owned byte-identical lowercase renames fix ten referenced OBJ paths and the actual EMP texture; other legacy resource casing remains future work.

Synthetic native tests cover captured noSmooth timing, CPU reload insertion order, eager nonreload lifetime, opening/parse failures, mixed-mode rejection, ordered/duplicate selectors, raw shared batches, current-attribute carry, POSITION-only untextured smooth parts, last-group mode including -1, and approved failure before mutation of an already-active caller buffer. A selection with a valid first group and invalid later group must fail without touching GL.

Run through the parent-owned common toolchain:

```sh
python3 porting/tests/probe-hfr-cpu.py --audit-only
/workspace/shared/nagaport-toolchain/run-gradle.sh --offline \
  --init-script porting/hfr-cpu-probe.init.gradle testPortHfrCpu --console=plain
```

Isolated native result: PASS, 13,626,604 assertions across 530 existing policy/resource combinations, plus the explicit expected missing-resource failure. The independent position oracle includes the original/native double translation addition at zero, which canonicalizes a source negative-zero position to positive zero at emission. This result is not the parent common Gradle/full-mod result.

Headless tests do not establish live GL upload/delete/reload, visual alpha/blend/lighting, full-mod compilation, or gameplay. ObjUtil and RenderMirror geometry-read source audits do not compile their entire still-legacy block rendering bodies; old Tessellator, RenderBlocks, icon, brightness and lighting calls remain explicit unresolved production blockers. See `direct-mesh-readers-notes.md` and `mesh-model-consumer-notes.md`.

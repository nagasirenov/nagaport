# Typed native mesh consumer boundary

Baseline: checkpoint 7, `a60fe7806f90257b5dbfbe9a62f46b887946bf93`.

## Implemented scope

Exactly 31 production consumer files now use the mod-owned
`com.hbm.render.mesh.MeshModel` contract instead of the removed Forge
`IModelCustom` or mod `IModelCustomNamed` handles. The explicit file list and
before/after SHA-256 values are in `mesh-model-consumer-manifest.json`.

The change is limited to imports, non-import type occurrences, replacing
`AdvancedModelLoader.loadModel(resource)` with `new ObjMeshModel(resource)`,
and explicitly normalizing three inherited loader-line indentation prefixes.
It does not rename rendering calls or alter animation, matrices, shaders,
textures, lighting, body arithmetic, field order, or constructor ownership.

| Source inventory | Baseline | Migration |
|---|---:|---|
| Consumer files | 31 | Same 31 files |
| Ordinary type occurrences, excluding imports | 499 | `MeshModel` |
| Named type occurrences, excluding imports | 17 | `MeshModel` |
| Ordinary old type imports | 29 | Native domain imports |
| Named old type imports | 3 | Native domain imports; the redundant ResourceManager import is removed |
| Plain factory call sites | 146 in 12 files | 146 eager `ObjMeshModel` constructions |
| ResourceManager plain factory sites | 135 | Same field positions and resource expressions |
| HFR constructor sites | 393 in 11 files | Unchanged |
| `asVBO()` sites | 293 in 6 files | Unchanged |
| `noSmooth()` sites | 77 in ResourceManager | Unchanged |
| Named ResourceManager door fields | 12 | Still non-final, eager, and in original order |
| Concrete ResourceManager VBO fields | 8 | Still `HFRWavefrontObjectVBO` |

The 11 plain loads outside ResourceManager comprise three static fields
(`RenderBobble`, `RenderDeathBlast`, `RenderBalls`) and eight existing renderer
constructor sites. None becomes lazy, moves to a shared cache, or changes its
static/per-instance lifetime. The existing two Tom `ReloadableObjMesh` fields
and all concrete HFR construction/fluent expressions are untouched. Plain flat
models are not substituted for HFR smooth or VBO models.

`DoorDecl` return types and `RenderDoorGeneric`'s named model handle use the same
domain contract. The existing ordered part-name iteration and all door
animation/rendering code stay byte-for-byte identical. `MissilePart`,
`RenderMiscEffects`, and the other typed field/argument/return sites are also
included, not merely ResourceManager declarations.

## Coordinated boundaries, not owned by this change

`render/loader/*`, `render/mesh/*`, `ObjUtil.java`, and `RenderMirror.java` are
outside this manifest. The coordinated backend work supplies the actual
`MeshModel` implementations, eager plain flat loading, CPU emission, HFR
smoothing/reload lifetime, and real VBO allocation/upload/draw/delete behavior.
`ModelRendererObj` is loader-owned and is not one of these 31 consumers.

The old interfaces and loader classes are not re-created under a different
Forge package. No fake Minecraft/Forge API, production stub, source exclusion,
CPU alias for `asVBO()`, or no-op rendering fallback is introduced here.

## Reproduce the portable audit

From the project root:

```sh
python3 porting/tests/check-mesh-model-consumers.py
# Includes independent-directory/no-Git reproduction and five negative mutations:
python3 porting/tests/check-mesh-model-consumers.py --self-test
```

Optional Gradle entry point, using the existing Java 8 / ForgeGradle environment:

```sh
gradle --offline --init-script porting/mesh-model-consumer-probe.init.gradle \
  testPortMeshModelConsumers --console=plain
```

The Gradle task runs the same Python source audit. It does not compile a reduced
consumer set and does not change the production source set or dependencies.

For every complete source file, the audit checks the migrated SHA-256, reverses
the three explicitly recorded indentation normalizations and import changes,
restores each old type in lexical source order,
reverses the eager plain constructor syntax, and matches the recorded
checkpoint-7 SHA-256. Comments and Java string/character literals are excluded
from live token substitution. The `C` and `N` markers in each manifest entry
mean ordinary and named old types respectively. This distinguishes the twelve
named ResourceManager fields from its ordinary model fields without retaining
an old API in production.

The baseline commit is informational at audit runtime. No Git object, sibling
original tree, network, Minecraft jar, or developer-specific absolute path is
needed. The audit was also run successfully in a temporary directory containing
only its script, manifest, and the 31 consumer files, from an unrelated working
directory and without `.git`.

Additional direct checks passed for Java comment/literal exclusion and five
negative source mutations: a resource path change, removed `noSmooth()` call,
deferred factory replacement, named-door finality change, and concrete VBO type
replacement. The negative checks deliberately recomputed the migrated hash;
reverse-source reconstruction still rejected the mutations.

## Acceptance limits and snapshot history

This is **source-delta parity, not complete consumer compilation**. It proves
the original paths, eager creation sites, ordering, fluent calls, and unrelated
consumer source are retained, not that all 31 complete classes now compile or
that the replacement backend has identical runtime behavior. Whole-mod native
compilation, backend native probes, resource reload, GL allocation/draw/delete,
visual rendering, and client/gameplay acceptance require their own evidence.
No shared Gradle build was run as part of this consumer-only implementation.

`probe-hmf-mesh.py` now explicitly composes the current consumer boundary with
its historical HMF source audit. When the native MeshModel import is present,
it first independently validates all 31 complete consumers, then reconstructs
the exact checkpoint-7 ResourceManager with `reverse_consumer`, then applies
its existing HMF-only reverse delta to the unchanged checkpoint-6 hash. It
prints that composition and its source-only limit. This is not a replacement
hash or blanket relaxation. Without the native marker, the historical hash
still rejects unexplained current changes.

```sh
python3 porting/tests/probe-hmf-mesh.py --audit-only
```

The composed audit also passed in a standalone no-Git temporary copy containing
the relevant sources, scripts, manifest, and two Tom assets. Negative checks
confirmed that changing an unrelated typed consumer fails before the historical
ResourceManager checks, and removing the native marker cannot bypass the
unchanged historical ResourceManager hash. These checks do not execute the
native Java HMF tests; that is a separate Gradle/native test run.

Only the changed loader-line prefixes of `RenderCloudFleija`,
`RenderCloudRainbow`, and `RenderCloudSolinium` were normalized from four spaces
plus one tab to two tabs. Their exact before/after lines are in the manifest,
the audit permits only those three known deltas, and every original file hash
still validates after reversal. All other source whitespace is preserved.
`git diff --check` passes for the 31 owned production files.

This audit is a bounded snapshot. Future unrelated consumer edits require an
explicitly reviewed new baseline or an additional documented reverse delta,
not merely replacing hashes and describing the old proof as evergreen.

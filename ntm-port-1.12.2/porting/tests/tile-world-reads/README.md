# Native tile world reads (CP56 isolated base)

Exact baseline: `149469c7403653ca6576515b730bc033a1574cfe`.
165 compiler-attributed zero-argument `TileEntity.getWorldObj()` calls in 70 files become genuine `getWorld()` calls. Only method-name UTF16 spans change. No world writes, fake getters, new production classes, reflection, source exclusions, gameplay changes, downloads or Gradle invocation. Silo, DungeonToolbox and crafting registration are untouched.

## Dispatch and scope

Javac attributes native receiver ancestry, excludes unresolved/type-variable/non-tile/static receivers, nonzero arity, method shadows, interface methods and resolved unrelated lexical bindings. Bare calls require the nearest lexical class to be a native tile descendant and a nonstatic context. Post-attribution requires the native TileEntity method (or the specifically audited AirPump override). Strings/comments and unresolved RendererObjTester calls remain unchanged.

All 454 current source TileEntity descendants are checked for both getter declarations. The sole source override is TileEntityAirPump.getWorld, whose body is exactly `return this.world;`. AST field attribution verifies that world belongs to native TileEntity, not a hidden source field. Any other source override fails the audit, including overrides on descendants whose call sites are statically typed TileEntity. The original source override returns `worldObj`; retained hashes and the AirPump → MachineBase → LoadedBase → TileEntity ancestry audit show no intervening World declaration. The original getter-declaration inventory has only AirPump. The cached class-file closure covers 75 external tile descendants among 48,243 classes and rejects getWorld/getWorldObj overrides. Unknown external mods remain outside this frozen closure.

Actual original `aor.w(): ahb` and native `TileEntity.getWorld(): World` bytecode are each `aload_0; getfield [world]; areturn`. Original SRG maps aor.w to func_145831_w and aor.b to field_145850_b. Virtual dispatch is not assumed away: the audited AirPump override returns exactly the same native base field; a descendant with a hidden world field is exercised without changing the inherited override's field binding.

## Reproduce

From this directory:

    python3 run.py --java-home /path/to/java8 --classpath-file /path/to/native-classpath.txt --legacy-client /path/to/original-client.jar --verify-attribution
    python3 dependency_closure.py /path/to/native-classpath.txt
    python3 check_source.py

The runner validates the original client hash, 20 compiler ownership/arity/override/static/shadow/Unicode fixtures, all 165 original attribution positions and all 165 native destinations. The source guard checks 70 exact byte inverses and rejects 165 token mutations plus 70 unrelated whole-file drifts. Source-only checks are not runtime proof.

Native/old runtime checks use actual engine TileEntity and World objects for constructor null, setter replacement, retained null and world identity, invalidate/validate, native unload, position mutation independence, once-only receivers and null failure. Worlds are allocated without constructors only as identity objects; no world simulation or lifecycle construction is claimed. The exact current AirPump getter body executes. The complete current FluidType.onFluidRelease(TileEntity, FluidTank, int) forwarding method is extracted unchanged and executed with real native TileEntity/world/position getters and a recording domain sink plus a FluidTank identity token. The FluidTank and downstream overload are explicitly test seams, not execution of complete FluidType, tank spill logic, particles or fluid physics. Null/negative/extreme arguments and null-before-dispatch are tested. No complete energy topology, neutron behavior, render, machine gameplay, saved-world import or launched client/server claim.

## Integration

Separate production/evidence patches are supplied. Preserve independently changed argument expressions and overlapping file edits: apply the contextual production patch or replay only manifest tokens against their original source provenance. Never copy whole production files from this isolated tree. The family guard intentionally detects unrelated later edits; compose exact inverse receipts instead of rewriting its immutable baseline hashes.

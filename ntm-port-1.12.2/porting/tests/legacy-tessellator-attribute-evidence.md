# Legacy late-attribute activation: verified bytecode evidence

This records read-only inspection, not a rendered image or an executed legacy client.

## Official source artifact

- Mojang version metadata: https://piston-meta.mojang.com/v1/packages/ed5d8789ed29872ea2ef1c348302b0c55e3f3468/1.7.10.json
- Official client artifact: https://launcher.mojang.com/v1/objects/e80d9b3bf5085002218d4be59e668bac718abbc6/client.jar
- SHA-1 from metadata and independently checked downloaded bytes: `e80d9b3bf5085002218d4be59e668bac718abbc6`.
- The client jar is inspection-only, not a port dependency or distributed in this checkpoint.
- Current temporary copy: `/tmp/nagaport-legacy-client.jar`; full javap output: `/tmp/nagaport-legacy-tessellator-javap.txt`. These temporary paths are not required for the ordinary port build.

Reproduce with JDK8:

```sh
javap -private -c -classpath /path/to/official-1.7.10-client.jar bmh
```

`bmh` is the original client Tessellator: a singleton, raw int array, attribute flags, vertex/UV/color/normal methods and GL array draw path.

## Observed writes

- Private reset method `d()` sets vertex count `g`, raw index `p` and added count `q` to zero and calls ByteBuffer.clear(). It does not clear raw int array `f`.
- Start-draw method `a(int)` invokes that reset, sets primitive mode and clears attribute-enable flags. It does not clear the underlying raw array.
- UV setter `a(double,double)` sets flag `m` and the current UV values.
- Vertex method `a(double,double,double)` always writes position slots 0/1/2. It writes UV slots 3/4 only when `m` is already set. It writes packed normal slot 6 only when normal flag `o` is already set. Color and brightness have analogous conditional writes.
- Therefore, if a later face enables UVs/normals in a single shared batch, earlier vertices' corresponding raw-array slots retain whatever was previously stored there. Fresh array slots may be zero, but that is not a reliable value after unrelated earlier draws.

This is not ordinary current-attribute carry after an attribute has been enabled. That defined carry must be preserved by the native mesh emitter. The ambiguous case is a prefix emitted before an attribute is first enabled, followed by activation in the same draw batch.

## Reachability and policy boundary

The bundled HFR lantern, siege_ufo, charger and lance assets have this possible whole-model topology, but the audited in-repository callers draw their groups separately. The separate untextured smooth group must remain POSITION-only; it must not gain a fabricated normal array merely because source vn values exist. Public whole-model calls and resource-pack replacements cannot be ruled out by an internal source audit.

Exact old stale-slot values depend on prior unrelated uses of the global legacy tessellator and cannot be derived from just a mesh snapshot or a native BufferBuilder. No deterministic zero-value parity or exact external compatibility is asserted here. The late-activation policy must be explicitly recorded in the CPU migration notes; this evidence alone does not authorize changing that contract.

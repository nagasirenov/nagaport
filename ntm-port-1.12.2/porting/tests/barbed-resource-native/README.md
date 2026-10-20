# Barbed-wire native resource routes

Baseline: `3875114d4e1f0e9b0ceb67c2d84840e66ba1b20c` (134 placements already separate).

This bounded correction preserves all six intentional `hbm:tile.barbed_wire*` registry IDs. It adds an explicit original atlas texture declaration in the sole factory, rather than deriving a texture from registry identity. Original texture logical names map to `hbm:blocks/barbed_wire*_model`. No texture/OBJ bytes change.

Three inherited defects were exercised, not concealed by source pins:
- Registry-derived texture requested nonexistent `textures/blocks/tile.barbed_wire*_model.png`.
- Existing state resources without the `tile.` prefix did not match actual state IDs, and referenced an absent model.
- Passing `models/blocks/barbed_wire.obj` to native ModelLoaderRegistry doubled `models/`.

Additionally, actual Forge state JSON parsing unconditionally prepends `block/`, so directly referencing `blocks/barbed_wire.obj` asks for nonexistent `models/block/blocks/barbed_wire.obj`. The exact owned custom loader accepts ONLY `hbm:models/block/barbed_wire` and forwards to native OBJ loading at `hbm:blocks/barbed_wire.obj`. It has no cache, no namespace-wide interception, and no geometry implementation. Six new state JSONs match real registry IDs, advertise all six orientations and inventory, and retexture native default/white materials. Old unmatched state assets remain unchanged; they are not registry aliases.

The existing model-bake handler, mesh transforms, normals, UVs, shading, orientation and inventory algorithms remain byte-for-byte identical from onModelBake onward. The only handler edits are correcting the OBJ request and registering this precise route. Owner gameplay callbacks and state/drop/save behavior remain unchanged, proved by exact source inverse and preceding placement evidence. That earlier evidence intentionally records the then-broken texture and is historical, not a current rendering certification.

## Reproduction and result

`python porting/tests/barbed-resource-native/run.py`

Uses existing Java8 and native classpath file. Compiles complete actual BarbedWire, handler and icon adapters, extracted actual factory, and explicit bounded NTM collaborators from preceding owner proof. No fake Minecraft/Forge classes or modified platform bytecode. Reuses the existing offscreen Mesa EGL context and actual Minecraft constructor from MaterialTextureAtlasSpriteNativeTest. Reflection installs native client resource/atlas/model manager fields without starting the full game. Factory instances are registered through actual Forge registries. No full-mod compile or lifecycle claim.

87,339 checks: actual SimpleReloadableResourceManager directory and JAR reloads; actual Forge VariantLoader/owned loader/OBJLoader parse and bake all 42 world/inventory routes; native TextureMap mip2 loading, stitch and real GL upload with base-level pixel readback; actual handler publications and ItemModelMesher metadata0..5 dispatch. Changed original OBJ vertices and PNG pixels reload through the SAME resource manager and alter baked geometry/uploaded data; restoring pack restores exact geometry/pixels. Unknown loader requests are rejected. Atlas mip2 is exercised but this probe reads back level0 only.

Four runtime negative controls: `run.py texture`, `obj-route`, `loader-route`, `state-route`. Each is expected to exit nonzero. Logs show real missing sprite or native model/variant failure. They alter only temp sources/resources, not production. `source-proof.py` verifies changed and unchanged hashes, exact owner/factory inverse, untouched bake/transform code, constructor closure and original assets. Complete owner and model handler compile unsplit under Java8, thus no hidden method splitting/size exclusion.

Limits: isolated genuine resource/model/atlas integration, not full game startup, screenshots, legacy pixel parity, destruction overlay or lighting acceptance. CP37 visual gate remains open. No change to pending rendering policy. Acid and other damage variants are not newly behavior-tested; gameplay unchanged. Global mod pre-stitch subscribers are not invoked: exact owner icon callback is called through the actual native adapter, followed by native atlas load. Existing ClientProxy event subscription is unchanged.

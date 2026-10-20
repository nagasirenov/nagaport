# Native EMP effect and two plain-Forge renderer consumers

## Scope and parity

Complete production classes `EntityEMPBlast`, `RenderEMPBlast`, and `RenderPlasticBag` use the actual pinned 1.12.2 Minecraft/Forge APIs. The four existing mesh classes and `RefStrings` are compiled alongside them without substitutes. This batch does not migrate the plastic bag entity, other model consumers, resource reload subscriptions, or VBO lifecycle.

`EntityEMPBlast` replaces DataWatcher integer slot 16 with a class-owned native `DataParameter<Integer>` using `DataSerializers.VARINT`. The native allocator chooses its ID after inherited entity entries; the old numeric ID is not a 1.12 wire contract. The value remains a full signed integer. The default constructor still exposes public `maxAge=100` while its synchronized lifetime is zero. The lifetime constructor still leaves `ignoreFrustumCheck` false, unlike the default constructor. `setMaxAge` changes only synchronized data, not the public field, animation, or liveness. Update retains the exact increment, comparison, reset, native death, then scale-increment order, without adding `super.onUpdate()`. Both native brightness callbacks now have no argument, with original constants. NBT keeps exactly `age` and `scale` as signed shorts, including narrowing/fractional/nonfinite cast behavior, and deliberately does not persist synchronized lifetime.

Both renderer draw bodies preserve the supplied pristine transforms, texture binding, animation interpolation, and call order. They use `LEGACY_FLAT`, including first-three-vertex flat normals and the original 0.0005F centerward UV inset, not HFR smoothing. `models/ring.obj` (requested by the unchanged `models/Ring.obj` constructor string) is 256 triangles and `mobs/plasticbag.obj` is 26 triangles; each has one fully attributed part, so existing `drawParts` batching does not introduce cross-group attribute changes for these resources.

Original GL state leaks are retained, not silently repaired: EMP leaves lighting and face culling disabled; plastic bag leaves face culling disabled. Neither restores its bound texture. These are source-parity observations, not executed GL acceptance. EMP retains X/Z scaling with unit Y, float position translation, and no partial-tick scale interpolation. Plastic bag retains double position translation, its original yaw/pitch interpolation and offsets, and zero shadow opacity.

## Resource lifetime and registration

In native startup, mod PreInit runs before `Minecraft.renderManager` is assigned. Passing `Minecraft.getMinecraft().getRenderManager()` to a renderer constructed at the old registration point would capture null in native `Render`'s final manager field. Simply changing to a lazy factory would instead move model resource sampling later.

The EMP registration therefore calls `RenderEMPBlast.loadRingModel()` in its original position, then captures that resource snapshot in a native factory whose supplied manager constructs `RenderEMPBlast(manager, empRing)`. The ordinary `RenderEMPBlast(RenderManager)` constructor remains eager and loads one fresh mesh per renderer. The registration preloads one snapshot for its one renderer, as the original direct construction did. Its texture `ResourceLocation` is created when the factory constructs the renderer; that pure value construction does not read texture data. No reload listener is added.

Plastic bag retains its original eager static singleton mesh. Calling `RenderPlasticBag.getModel()` at its original registration position triggers the same class/resource initialization before its native `RenderPlasticBag::new` factory is registered. Renderer construction uses that singleton, not repeated loads. The parent integrates the ClientProxy statements; this worker does not change unrelated registration sites.

## Evidence and reproduction

`testPortEmpEffect` compiles the full production classes above, all four mesh core classes, and `RefStrings` against `compileJava.classpath`, not extracted production implementations. It also compiles the exact EMP registration statements extracted from ClientProxy and a native generic plastic bag factory/registration signature; it does not compile the whole ClientProxy or unported plastic bag entity.

The isolated probe passed 2,026 entity assertions and 8,760 mesh/resource assertions:

- Real native entity construction and bounding boxes in both server/client-flagged fixtures; a real `World` constructor provides dimension/remote state, while unexpected chunk requests fail explicitly. There are no replacement Minecraft/Forge types and no unsafe allocation.
- Default and explicit lifetimes, negative/zero/positive bounds, integer age overflow, float increment edge cases, original frustum difference, constant brightness, no vanilla-super tick bookkeeping or physics, lifetime changes taking effect on the next tick, and observation of age reset before native `setDead`, with scale increment afterward.
- Actual `EntityDataManager` dirty/equal-value behavior, unique inherited data IDs, actual `SPacketEntityMetadata` full and incremental serialization through native `PacketBuffer`, complete frame consumption, signed values through int bounds, and applying decoded metadata to a real second entity without changing age/scale.
- Actual protected NBT callbacks, native compressed NBT roundtrips, both tag types, absent/wrong-typed tags, caller-owned fields preserved, signed-short narrowing and nonfinite casts. The real complete `Entity.writeToNBT/readFromNBT` lifecycle is also exercised, preserving position and the missing-maxAge anomaly. This is a bounded fixture, not a saved-world acceptance test.
- Independent raw-resource vertex oracle verifies both assets' part count, face count, full corner attributes, source positions/winding, exact float UV bytes after V flip/inset, and packed flat normals in actual `BufferBuilder` output. Native vertex padding is not treated as geometry. No GL context is created.
- Eight resource assertions construct actual native `ResourceLocation` values from the unchanged `models/Ring.obj` and `textures/models/EMPBlast.png` strings, verify namespace and lowercase path, require files at paths resolved through `getNamespace()`/`getPath()`, and compare their complete bytes against frozen original SHA-256 values from `resource-case-migration.json`. Both geometry oracles now also resolve through those native accessors from the assets root. This proves canonical filesystem lookup and byte preservation, not resource-manager loading or texture sampling.
- The source audit compares constructors, update/NBT bodies, renderer transforms/texture/state order, and selected helper/registration statements against the supplied pristine files, allowing only the documented API substitutions.

Run from the project root with JDK 8 and the configured pinned toolchain:

```sh
/workspace/shared/nagaport-toolchain/run-gradle.sh --offline \
  --init-script porting/emp-effect-probe.init.gradle \
  testPortEmpEffect --console=plain
```

This worker ran only the isolated Python/javac probe with the actual resolved compile classpath; coordinated Gradle/whole-mod compilation is the parent's responsibility. Initial test development exposed a null-world test fixture in full native NBT loading (`setGlowing` needs `world.isRemote`); replacing that fixture with a real bounded World constructor resolved it without production changes. A test-only shadowed-world constructor qualification was corrected before the final pass.

## Provenance and limitations

Supplied pristine source SHA256:

- EntityEMPBlast.java: `0d3b1e9d2835cf580f0ab9d74afdb17ad4c823f4450f84642492a05e1dbaaaa6`
- RenderEMPBlast.java: `4810d803cee7dc955e0754db48ef0599ba1eb04e46fbac52605027caab278bf7`
- RenderPlasticBag.java: `fc055fe4ecf0cadeb12335daad647df12e416361179f5e6a9289595467a1d395`

Byte-preserved asset SHA256 (case-only renamed Ring/EMP texture paths recorded in `resource-case-migration.json`):

- models/ring.obj: `8cd07021419e853cfa2d32ec8e4d00223304124133d5c15efcc43470d6b65783`
- models/mobs/plasticbag.obj: `2c19b849e733f626448a0d75aab20eaae182b2dc487174dc9767e722dd92c747`

- textures/models/empblast.png: `fd679b8d79dce0982091c068b628f17222fb803c83082ea14a1d27b5aaa350e8`

See `obj-mesh-migration-notes.md` for the pinned official legacy flat-normal/inset provenance and core limitations. No client startup/rendering, GL state execution, texture sampling, GPU/VBO work, dedicated-server startup, live tracker/network lifecycle, gameplay, or save-file acceptance has been performed. The complete mod still has unrelated compilation blockers; these bounded probes do not produce a runnable mod.

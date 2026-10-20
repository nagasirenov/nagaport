# Native side-rotation model proof

Result: **PASS 584,751 assertions, 2,584 complete layouts, 62,016 ordered native vertices**. No production change was needed by this proof.

## Replay

From the repository root:

```sh
python3 porting/tests/side-rotation-model-native-probe.py \
  --java-home /workspace/shared/nagaport-toolchain/java8 \
  --classpath "$(cat /workspace/shared/nagaport-tick-preparation/classpath)" \
  --legacy-client /tmp/nagaport-legacy-client.jar
```

The default pristine input is the repository's pinned `fixtures/side-rotation-pristine.zip`, via `side_rotation_fixtures.py`. `--pristine /path/to/original` optionally verifies against the original checkout. `--generate-only` validates inputs and prints the generated independent oracle SHA without compiling/running. Both fixture and pristine-checkout paths generated exactly the same oracle SHA256: `e67566516da748929e334322d514136068ffafd207516123efb48e146d8b1228`.

The original 1.7.10 client must already be cached locally; the probe never downloads it. Its required SHA256 is `a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.

## Coverage

- One compilation contains 39 complete real production sources, including the six crane block classes, actual crane base tile, all four full editor blocks and nested tile/GUI classes, real snapshot/interface, native renderer/model/handler, and unchanged cuboid helper. All input production hashes are printed in the proof log. Explicit unrelated NTM collaborators are composed from the audited owner probes' AST literals, using current editor supersets for shared contracts. There are no owner substitutes and no replacement Minecraft/Forge classes.
- Independent pristine source extraction supplies all original texture and rotation selectors, interface rotation helpers, renderer UV assignments, inventory yaw, and original registry names. The original cached RenderBlocks face bytecode executes against each selected sprite, rather than comparing two implementations of the new geometry. Its private field readers are mapped independently of the production source-side array. Native canonical face order is compared to cyclically equivalent original winding with exact XYZ/UV float bits and normals.
- All nine owners and all 16 metadata values exercise both item and world rendering. Five cranes additionally cover all seven outputOverride values and missing tiles. Logic/Loot raw rotations outside 1..3 exercise original default UV behavior. Inventory preserves no UV rotation assignments, original +90Y geometry/normals, and the source-vertex UV pairing. Snapshots retain raw metadata, independently verified original selectors, defensive copies, and immunity to subsequent live tile mutation.
- Actual native Forge VertexLighterSmoothAo and vanilla BlockModelRenderer smooth paths run for all nine owners. Exact position/color/light outputs match native FaceBakery reference corners under asymmetric neighbor light and AO; smooth differs from flat. Air, asymmetric stone, fully opaque stone, and glass neighborhoods exercise actual owner face culling, including zero output when fully occluded. Open fullbright surroundings verify one directional diffuse application.
- The actual ForgeHooksClient damage-model path receives listed state and obtains the production extended snapshot itself. A crane output override survives all six native face buckets and crack-sprite retexturing, with exact native BakedQuadRetextured position/UV bytes.
- The genuine Forge event transformer and event bus dispatch ModelRegistryEvent to the actual annotated production handler. Missing deferred ItemBlocks fail explicitly. A bounded fixture then registers the real ItemBlocks and installs the engine's block-to-item identities before the event, proving the handler uses those exact item delegates. This does not claim automatic mod registry lifecycle execution.
- Both native FolderResourcePack and FileResourcePack resolve the nine actual JSON files, all 144 world and 144 item routes, and all 87 original exact-case atlas PNGs, which decode through ImageIO. ModelLoaderRegistry loads actual descriptors. Actual ItemModelMesher and BlockModelShapes return the registered baked objects for every metadata state. Every crane override and missing-tile layout is also tested through each resource route.
- Actual native model-cache reload creates new descriptors and fresh-atlas baked objects for all nine owners. Previously retained models keep immutable geometry and original atlas objects. All accepted canonical path prefixes and representative malformed/unknown-owner routes are checked.

## Boundaries

This is headless CPU proof, not live graphics or full-mod acceptance. Atlas rectangles and the Minecraft resource holder are controlled fixtures. External NTM registries, MachineBase/concrete crane engines, structure execution, networking transport, and unrelated UI catalogues are explicit collaborators from the existing owner probes. The actual crane base and every owner/model/handler body are complete. Tile orientation metadata is supplied through the native TileEntity metadata cache and the production NBT reader; full owner behavior and chunk-packet synchronization are independently covered by the owner proof. No GL frame comparison, actual atlas stitching, automatic subscriber discovery/full loading lifecycle, or legacy/native pixel-light equivalence is claimed.

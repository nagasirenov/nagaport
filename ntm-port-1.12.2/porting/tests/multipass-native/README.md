# Native multipass family: bounded frozen handoff

Base: CP49 `caf22cf027f33f9d0d0be366454c2f54fdba7915`. This batch is separate from CP50 and can be integrated after it using the exact shared fragments. No original/pristine file, texture, mechanic balance, network algorithm, tile lifecycle, build exclusion, broad old-Forge facade, or platform substitute is introduced. No push or full Gradle build was run.

## Scope

- Complete native `IBlockMultiPass`, immutable `MultipassContext`, `MultipassMaterials`, `RenderBlockMultipass`, and `MultipassModelHandler`.
- All12 original direct implementing owners have native metadata/extended-state/model/layer hooks. Contexts capture paint state, OC/fluid colors, tube directions, bedrock shape/color, or Sellafield coordinate variation without retaining world/tile references.
- Complete `BlockFissure` compiles against actual native Minecraft/Forge. Its original upward replacement/refill/sender behavior is exercised using explicit NTM fluid-network collaborator fixtures. The actual network implementation is not certified by this test.
- The other11 owners are deliberately bounded render-hook extractions, not whole-owner compilation claims. Their old mechanics APIs remain separate port work. The3 inherited ore subclasses likewise retain their original mechanics.
- 71 exact original block variants include44 direct BlockOre,3 Outgas,5 Fluid,4 Cluster,5 Sellafield and10 other families. All16 metadata item routes and16 listed world states are mapped, without enabling OC when the original conditional registration omits it.
- 129 exact shared fragments update71 constructor lines,57 actual legacy registration statements and remove the obsolete ClientProxy handler. Fourteen already-native registration-helper calls need no change. Default vanilla ItemBlock exceptions (deco_titanium, red_cable_paintable) are preserved; ItemBlockBase/ItemBlockLore selections are not replaced.
- ModBlocks and ClientProxy are byte-preserved outside those exact fragments. Ore/Sellafield `withSoundType` is a narrow public native setter used before generic Block-returning chaining; no protected native method is called from ModBlocks.

## Native dependency

Uses the frozen CP50 `com/hbm/render/util/NativeBlockMaterial.java`, SHA256 `948cdeae5bb39ac0919e1f8fcf2fd415be1bf38a889a74442339f4a15b387a01` from block-entity commit `cfaefbbd25b5c15d80bfbdab958cfa7acf0edd88`. That file is not duplicated in this patch. Only its pure `faceSprite` unique-geometric-material resolver is consumed; rubble's standalone-resource policy is not used.

Existing native ConveyorCuboidGeometry and CT inventory +90-degree face geometry are reused unchanged. Their hashes and all compiler inputs are logged in native-proof.log.

## Proofs

Run using Java8 and the resolved native Minecraft/Forge classpath:

```sh
python3 porting/tests/multipass-native/source-audit.py
python3 porting/tests/multipass-native/probe.py \
  --java-home "$JAVA8_HOME" --classpath "$NATIVE_CLASSPATH" \
  --legacy-client "$LEGACY_CLIENT_JAR"
```

Before CP50 integration, `probe.py --material-source /path/to/frozen/NativeBlockMaterial.java` supplies the exact shared dependency. After integrating unrelated shared edits, use `source-audit.py --integrated-shared`; this verifies every owned fragment while allowing other workers' changes. The default isolated audit verifies whole shared files byte-for-byte against the pinned CP49 snapshots plus only this fragment list.

The original selector/oracle command is in original-audit.md. No production source is replaced by a test fixture: complete native classes and explicitly extracted owner hook bodies are identified individually in the logs. Minecraft/Forge always use actual artifacts. Other mod-wide initialization/network/gameplay boundaries are narrow NTM fixtures and are not an integration claim.

Results:

- Source invariance:156 nonrender methods/constructors and9 complete nested tile bodies unchanged from pinned CP49, apart from the explicitly asserted native Fissure updateTick signature/world-call mapping.
- Independent source-original material oracle:74,880 rows,213,438 original assertions and374,694 native-selector assertions. Includes all12 families, allmetadata, every49 tube-direction pair, missing/unpainted/painted tiles, four Sellafield variants,71 exact registry/material aliases and100 byte-identical PNGs.
- Complete native model/registry and bounded owners:1,712,721 assertions,30,720 original RenderBlocks/Tessellator bytecode XYZ/UV/winding vertices. Covers actual state mapper/model registry/loader/bake/reload,1136 item routes, metadata-specific world/particle routes, concurrency, native Forge/vanilla AO/flat, culling, pass multiplicity and original item exceptions.
- Actual BlockRendererDispatcher.renderBlockDamage -> ForgeHooksClient.getDamageModel -> native renderers under RenderGlobal's exact noColor contract:384,000 emitted vertices are white across12 families ×16 metadata ×10 damage stages ×Forge/vanilla AO/flat. Independent original Tessellator noColor test passes72 assertions. Native BakedQuadRetextured retains tint metadata, but the actual standard damage path discards it, so this is not a rendered crack-tint regression. No replacement damage callback or dummy tile was added.

## Explicit unresolved gates, not accepted substitutions

1. Full original-versus-native light-solver equality, including all corner lightmap/AO and optional vanilla fallback, remains unproved. Native light response tests are not a waiver of CP37.
2. Full GPU inventory/chunk/destruction drawing, shader packs, and arbitrary original incoming inventory GL-current-color carry are unproved. The baked inventory model assumes ordinary white input; this is documented, not silently accepted as equality for arbitrary GL state.
3. Foreign camouflage resolves only a unique side material. Existing NTM BlockBase retains its original side/meta icon provider; this native multipass family explicitly selects its own known pass-zero material when used as paint. A foreign absent/multi-material/built-in model throws a descriptive unresolved-material exception instead of choosing its particle sprite, hiding geometry, changing allowedPaint, or inventing a material. Such foreign compatibility remains a gate requiring an explicit source-specific rule, not an approved rendering failure or fallback.
4. Whole larger-owner mechanics/packet-render invalidation, complete mod build, actual client/server/game/save and Windows acceptance are not claimed by this bounded stage.

No pending CP37 decision is resolved or overridden by this patch. The original renderer's mutable static currentPass is retired completely from live production references.

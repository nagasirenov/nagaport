# Native rebar owner

## Production scope

`src/main/java/com/hbm/blocks/generic/BlockRebar.java` is compiled in full against the actual cached Forge 14.23.5.2768 / Minecraft 1.12.2 jars. The block, its nested production `TileEntityRebar` and `RebarNode`, both render-last branches and all callbacks are compiler inputs. No RenderBlocks shim, source exclusion, generated production replacement or Minecraft/Forge fixture is used.

The parent universal-model migration owns `ISBRHUniversal`, `UniversalBlockState`, `UniversalCuboidGeometry`, model registration and the shared event invocation. Rebar supplies the original twelve inventory cuboids, twelve world cuboids or three simple world cuboids. The world longitudinal limits remain -0.001 and 1.001; inventory limits remain zero and one. The shared native geometry preserves the original inventory-versus-world NORTH/EAST UV difference, interval clamping and inventory rotation. There is no baked concrete fill; that remains a separate render-last draw.

Native metadata states retain all sixteen original values. A scoped `TileEntityRebar.shouldRefresh` override preserves the original same-block metadata tile lifetime. Native Forge's default mod-tile behavior otherwise discards the tile on a changed state. Actual World/Chunk transitions across all sixteen states verify retention. Collision remains a full block although the block is nonopaque/nonfull for rendering and attachments.

The server tile retains every original ordering and arithmetic decision: changed-progress dirty marking, 1,000-unit solidification before all further work, chosen concrete metadata or default rebar concrete, 60-tick fluid resubscription, node retrieval/recreation, radius-100 incremental buffer publication, lowest-Y selection, 50-unit per-link rate, integer averaging, invalid/missing link behavior, unsaved previous progress, original NBT overlay behavior and unload/invalidation distinction. No fluid distribution, recipe or balance change was made.

## Render-last paths

Concrete uses a native six-face `SimpleBakedModel`, the original concrete-liquid atlas resource and native BlockModelRenderer with ambient occlusion, original unbounded progress/1000 height, camera-relative translation and original smooth/flat shade ownership. The selection outline retains all 24 ordered vertices, opaque white, original packed light 240, adjacent-hit selection, 0.125/0.875 inset, integer volume arithmetic and tooltip behavior.

The outline uses native BLOCK vertex format because UV1's genuine native format indexing requires a preceding UV0 entry. UV0 is unused while texturing is disabled. `BufferBuilder.lightmap(0, 240)` produces the original packed value 240; the native API's SHORT storage reverses the argument order. Buffer bytes explicitly verify this rather than assuming names. No custom old renderer API is retained.

## Reproduction

From the project root:

    python3 porting/tests/native-rebar-source-parity.py
    python3 porting/tests/native-rebar-probe.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8

The classpath and Java paths are local examples; the runner accepts a supplied platform classpath and JDK8. The optional offscreen draw portion uses existing Mesa EGL and cached LWJGL native libraries, without downloading or installing anything. Windows/other graphics providers have not been tested.

`native-rebar-source-parity.py` compares fourteen complete original methods and the original neighbor body after enumerated native API mappings; fifteen ordered world cuboids, twelve inventory cuboids, all 24 selection vertices, selection-limit admission, camera interpolation and original volume/count arithmetic are checked directly against the included pristine source. It does not need an unpublished Git commit. The original source SHA256 is `0ce00dd4a37e0aaf3fbaa39524b4b455463babd9573bfa2fa9f1e597cf81c7bc`.

`native-rebar-probe.py` imports its explicit collaborator/source spec from `native-rebar-fixtures.py`. It compiles complete actual owner and shared native production classes. The Java template is `native-rebar-test.java.in`. The transfer oracle is the complete pristine method with only its World/BlockPos lookup adapted, not a hand-rewritten algorithm. The successful bounded run records 15,441 assertions, including 1,600 six-link original/native transfer comparisons, real native block/tile/chunk/tick/NBT behavior and actual BufferBuilder bytes. Evidence is `native-rebar-proof.log`.

The offscreen portion constructs actual Minecraft, EntityRenderer, WorldClient, NetHandlerPlayClient and EntityPlayerSP objects, stitches both original rebar/concrete textures, and invokes the complete production `renderRebar` method for concrete and selection paths. It uses the existing genuine EGL helper and actual GL draw calls, not Unsafe-allocated client holders. No network connection is initiated by the disconnected NetHandler fixture. It is not a launched world or a screenshot/visual comparison.

## Explicit boundaries and remaining work

Test fixtures supply only external NTM collaborators: the broad registries/configuration, solidification recipe predicate, fluid type/tank and receiver service, pipe type, utility facade, packet transport and HUD sink. The original minimum helper's long arithmetic is reproduced exactly in the fixture. Actual `TileEntityLoadedBase`, `IBufPacketReceiver`, `GenNode`, `NodeNet`, `UniNodespace`, `RebarNetwork`, `RebarNetworkProvider` and domain coordinate classes compile unchanged and their relevant real node lifetime paths are exercised. Existing independent foundry evidence covers the shared dispatcher loop; this rebar test does not claim a newly exercised dedicated-server dispatcher loop.

The actual external `IFluidReceiverMK2` still contains a legacy `provider.dimensionId` use. The actual shared `BufPacket.Handler` still has legacy client World field/coordinate calls. `ItemRebarPlacer` and the rest of `ModEventHandlerClient` remain outside this owner migration. Their fixtures do not make those collaborators native or prove the complete rebar gameplay flow. The existing `tileentity_rebar` TileMappings entry remains unchanged. The parent updates only the existing render-last invocation to native `.world` and `event.getPartialTicks()` and reserves both original atlas textures.

No full-mod compile success, game startup, save migration, complete fluid-network transport, live visuals or dedicated-server acceptance is claimed here.

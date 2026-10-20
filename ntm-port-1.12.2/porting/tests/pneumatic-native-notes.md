# Native exposed pneumatic tube: finite owner/model checkpoint

## Result and scope

Six complete production sources replace the exposed `RenderPneumoTube` family and its owner boundary. The complete owner compiles against real cached Minecraft/Forge 1.12.2 APIs. Native block activation, tool dispatch, selection/collision, extended blockstate, neighbor queries, model registry event, state mapper, item metadata routes, atlas model bake and registry reload paths are present. No old Forge renderer facade, legacy package shim, generated replacement production method or source exclusion is introduced.

This is **model + owner boundary**, not a functional pneumatic network acceptance. The test explicitly supplies NTM-domain collaborators for the existing tile, fluid traits/library, tool recipe key, localization and registry instance. It does NOT compile, replace or claim the pneumatic transport tile/system, GUI/container or paintable variant. Minecraft and Forge classes are real. The headless registry test uses Unsafe only to allocate a Minecraft holder with a real resource manager, not to replace engine APIs.

## Original mechanics audited, deliberately untouched

`TileEntityPneumoTube` owns 15 filter slots, `ModulePatternMatcher`, insertion/ejection directions with ordinal-6 UNKNOWN, whitelist/redstone flags, send/receive ordering, pressure fluid tank, transfer counter, sound throttling, packets, GUI and persistent state. It extends `TileEntityMachineBase`, implements `IGUIProvider`, `IFluidStandardReceiverMK2` and `IControlReceiverFilter`, and creates/destroys a `PneumaticNode` in `UniNodespace` through `PneumaticNetworkProvider`.

Original tank capacity is 4,000 with initial pressure 1. Pressure 0..5 gives ranges 0,10,25,100,250,1000. It subscribes on non-insertion/ejection sides every 10 ticks, attempts sending every staggered 5 ticks with at least 50 fluid, and consumes 50 only on successful transfer. Endpoint receivers refresh every 10 ticks. `PneumaticNetwork` preserves inventory slot access, sided extraction/insertion, filtering, max transfer mass 64, first/last/random source ordering and round-robin/random destination ordering, receiver expiration and range checks. Its closure includes `TileEntityMachineAutocrafter` for the one-item hard cap plus `NodeNet`, `GenNode`, `UniNodespace`, `BobMathUtil`, `ItemStackUtil`, `Tuple`, fluid systems, control networking and GUI/container dependencies. These are real dependencies, not removed or claimed as completed here.

The paintable block has its own full-cube two-pass renderer plus paint persistence/copying. It is not an exposed-tube material variant and is intentionally outside this checkpoint. No new Forge item-handler/fluid-handler capability or new mechanics are invented.

## Production integration

Copy these six relative paths from this isolated tree into the target source tree:

- src/main/java/com/hbm/blocks/network/PneumoTube.java
- src/main/java/com/hbm/blocks/network/PneumaticModelState.java
- src/main/java/com/hbm/render/block/RenderPneumoTube.java
- src/main/java/com/hbm/render/block/nativeblock/PneumaticCuboidGeometry.java
- src/main/java/com/hbm/render/block/nativeblock/PneumaticBakedModel.java
- src/main/java/com/hbm/main/PneumaticModelHandler.java

Shared edits for the coordinator only:

1. Remove `RenderingRegistry.registerBlockHandler(new RenderPneumoTube());` from ClientProxy.
2. Replace only the exposed owner declaration in ModBlocks with:

    pneumatic_tube = new PneumoTube().setTranslationKey("pneumatic_tube").setSoundType(ModSoundTypes.pipe).setHardness(0.1F).setResistance(10.0F).setCreativeTab(MainRegistry.machineTab);

3. Keep `register(pneumatic_tube)`, the existing tile registration and the paintable declaration unchanged. Existing five original PNGs remain byte-for-byte unchanged. No blockstate JSON is required: the client-only event subscriber targets only exact PneumoTube.class with hbm:tile.pneumatic_tube identity (foreign, subclass and unspecified owners are untouched), and installs the world mapper and sixteen item metadata routes directly.

The model uses the existing complete `ThermalModelTransforms` class. The owner uses existing `ForgeDirection`, `IToolable`, `ITooltipProvider`, `IItemFluidIdentifier` and current real mechanical collaborators. No existing shared helper was edited by this worker.

## Preserved geometry and owner details

Original 5/16..11/16 core, all six arms, three optimized straight axes, straight UV rotations, original inventory Z-axis tube/end textures, insertion/ejection/air collars (4/16..12/16), neck lengths and materials are retained. The world N/E faces retain RenderBlocksNT's global mirror correction, rather than RenderBlocks' partial-bound reversal. Renderer state is immutable per block; no mutable active icon/render-side state leaks between block instances or chunk workers. A synchronized 128-entry per-model cache avoids an unbounded cross-world cache, and each reload bake receives fresh sprites.

Tube and fluid neighbors still use the original owner predicates. Compressor air connections exclude insertion/ejection directions and tube neighbors. Missing-tile render behavior is retained separately from selection/collision behavior. Selection includes configured collar directions; actual entity collision includes only center plus tube/fluid-connected narrow arms, exactly as the original (not the collar volumes). Screwdriver cycling skips tubes, selects an IInventory, skips the opposite configured port, allows UNKNOWN, updates the native world on the server, and acknowledges on the client without mutation. Existing activation gas validation/GUI branch logic remains intact; GUI and transport runtime acceptance are not claimed.

## Portable verification

Copy the four test artifacts together: `probe-native-pneumatic.py`, `PneumaticNativeTest.java`, `pneumatic-oracle.java.in`, `pneumatic-source-manifest.json`. They work from the isolated tree or after integration into the normal repo, compiling an explicit selected source list. They do not use historical staging source, previously compiled classes, machine-specific test code, network downloads or a hidden dependency sourcepath.

Run with your local paths:

    python3 porting/tests/probe-native-pneumatic.py --base /path/to/current/ntm-port-1.12.2 --pristine /path/to/ntm-original-pristine --java-home /path/to/jdk8 --classpath "<cached native 1.12.2 dependency classpath>"

The manifest pins pristine source and five asset hashes and records complete selected production hashes. The script rejects changed pristine/asset pins. Original renderer bodies are read verbatim into a test-only descriptor recorder, not made into a fake engine library. Tests compare face topology, bounds and materials for 2,752 adjacency/port cases, plus inventory. Independent analytic tests cover all three straight-axis UV branches and asymmetric N/E world UVs. Real native World subclass tests call complete owner collision and screwdriver methods; native IBlockAccess tests exercise all 64 tube neighbor masks into extended-state/model paths, air exclusion and selection. Native registry tests invoke the production subscribed method, verify all sixteen item mappings, resolve the production world state mapper through ModelLoaderRegistry, and exercise cache clearing/reloading.

Current passing result: 139,742 assertions, 2,752 pristine descriptor cases. Compiler emits expected headless Unsafe/deprecation/unchecked warnings; Forge warns about the hbm registry prefix because the standalone test has no active hbm ModContainer. Neither warning is a swallowed failure.

No GL rendering, live chunk lighting/atlas upload, full gameplay/server save, pressure/transfer behavior, paintable rendering or whole-mod acceptance is asserted. Those remain integration/runtime work outside this finite checkpoint.

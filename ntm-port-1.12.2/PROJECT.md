# Project: NTM 1.12.2 Multi-Agent Parallel Porting & Verification (Sessions 2–6)

## Architecture
HBM's Nuclear Tech Mod (NTM) Forge 1.12.2 port.
The baseline JAR `build/libs/HBM-NTM-1.12.2-port.0.1.0.jar` (SHA-256 `C37C7DECE7AAF3646E7788724CE28223E1BFC3B01DCEC490C0E77E65A63F2C62`) is verified clean compile with 0 diagnostics.
To avoid build/workspace contention and file locks under Windows, the work is decomposed into four parallel isolated worktrees for Sessions 2, 3, 4, 5, followed by Session 6 cross-session integration and release gate qualification:

- **Session 2 (Worktree: `worktrees/session2`)**: Container inventories (`ContainerNT`, `ContainerBase`), slot boundaries (`ItemStack.EMPTY`), target containers (`TileEntityCrateBase`, RBMK, drone crates, ammo bags, minecarts), metadata codecs (`MachineBattery`, `MachineBoiler`, `BlockCustomMachine`, `MachineFan`, `CMAnchor`, `FluidPump`, `BlockMassStorage`), network blocks (`CableSwitch`, `CableDetector`, `FluidValve`, `FluidSwitch`), and legacy numeric ID persistence (`BlockRebar`, `BlockICF`, `BlockPWR`, `BlockBedrockOreTE`, `ItemCustomMissile`).
- **Session 3 (Worktree: `worktrees/session3`)**: Client asset declarations (`ammo_debug`, `gun_pa_ranged`), parent paths (`bf`/`eu` logs), case sensitivity corrections (`modelm65mono.png`, `modelbroadcaster.png`), dynamic slag sprites, model routing, high-ID molds (>15), Soyuz skins, upgrade variants, minecart anvil resilience, and F3+T reload stability.
- **Session 4 (Worktree: `worktrees/session4`)**: Dedicated server runtime (`runServer` in `run/`, port 25565), packet threading (`ScheduledMessageHandler`), buffer lifecycle (`BufPacket`/`EntityBufPacket` copy & release), TE client sync (`networkPackNT`, `getUpdateTag`, `getUpdatePacket`), and `@SideOnly` leak prevention (`SkeletonCreator.java`).
- **Session 5 (Worktree: `worktrees/session5`)**: Entity lifecycle (`ModEntityList`, `EntityRegistry`), spawn eggs, environmental & boss spawners (`BlockGlyphidSpawner`, `DungeonSpawner`, `BlockBallsSpawner`), 11 celestial dimensions (`PlanetGen.init()`), cross-dimension rocket travel (`CelestialTeleporter`), and space worldgen isolation (`NTMWorldGenerator` dimension 0 guard).
- **Session 6 (Project Root Integration)**: Cross-session branch merge, resolution of residual blockers, recipe/sound audits, final release compilation (`compileJava`, `reobfJar`), `git diff --check`, ZIP integrity, SHA-256 calculation, and release matrix in `porting/CURRENT.md`.

---

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | `ContainerNT` Click Pipeline | 7-mode click dispatcher (`QUICK_CRAFT`, `PICKUP`, `SWAP`, `CLONE`, `THROW`, `PICKUP_ALL`) | M1 (Session 2) | spec_miner_2 |
| 2 | `ContainerBase` Merge Validator | Slot-validity checking and stack-limit capping `mergeItemStack` | M1 (Session 2) | spec_miner_2 |
| 3 | `TileEntityCrateBase` Storage | Nullable backing array with `ItemStack.EMPTY` adapter & empty-slot omission | M1 (Session 2) | spec_miner_2 |
| 4 | `ContainerCrateBase` Item Lock | Prevents moving or swapping open crate in inventory | M1 (Session 2) | spec_miner_2 |
| 5 | `TileEntityRBMKSlottedBase` Storage | Component inventory logic for RBMK fuel, control, and utility rods | M1 (Session 2) | spec_miner_2 |
| 6 | `ContainerRBMKRod` Thermal Lock | Prevents manual rod handling when reactor > 50°C | M1 (Session 2) | spec_miner_2 |
| 7 | `TileEntityDroneCrate` Logistics | Automated drone item and fluid loading/unloading interface | M1 (Session 2) | spec_miner_2 |
| 8 | `ContainerDroneCrate` Transfer Routing | Dual shift-click routing for fluid ID vs cargo items | M1 (Session 2) | spec_miner_2 |
| 9 | `ContainerDroneRequester` Filter | Phantom filter slots with mode cycling and zero-cost pattern assignment | M1 (Session 2) | spec_miner_2 |
| 10 | Bag Inventory Self-Lock | Prevents nesting open bags in `ContainerAmmoBag`/`CasingBag`/`PlasticBag` | M1 (Session 2) | spec_miner_2 |
| 11 | `EntityMinecartContainerBase` Storage | Custom minecart container supporting variable capacity | M1 (Session 2) | spec_miner_2 |
| 12 | `MachineBattery` Metadata Codec | 6-way `FACING` 0..5 blockstate codec preserving legacy orientations | M1 (Session 2) | spec_miner_2 |
| 13 | `MachineBoiler` Metadata Codec | 6-way `FACING` 0..5 blockstate codec preserving legacy orientations | M1 (Session 2) | spec_miner_2 |
| 14 | `BlockCustomMachine` Codec & Config | `FACING` 0..5 codec and damage-based configuration initialization | M1 (Session 2) | spec_miner_2 |
| 15 | `MachineFan` 6-Way Codec | True 6-direction orientation codec (including UP/DOWN) with blowing vector | M1 (Session 2) | spec_miner_2 |
| 16 | `BlockCMAnchor` Metadata Codec | `FACING` 0..5 codec for multiblock construction anchors | M1 (Session 2) | spec_miner_2 |
| 17 | `FluidPump` Metadata Codec | `FACING` 0..5 codec for fluid transfer pump blocks | M1 (Session 2) | spec_miner_2 |
| 18 | `BlockMassStorage` Packed Codec | Packed 0..15 codec combining 4 material subtypes and 4 horizontal facings | M1 (Session 2) | spec_miner_2 |
| 19 | `CableSwitch` & `CableDetector` State | Power network switching blocks using `ACTIVE` property | M1 (Session 2) | spec_miner_2 |
| 20 | `FluidValve` & `FluidSwitch` State | Fluid duct switching blocks using `OPEN` property | M1 (Session 2) | spec_miner_2 |
| 21 | `PersistentRegistryRef` Schema v2 | Registry-name string persistence with raw legacy numeric tag preservation | M1 (Session 2) | spec_miner_2 |
| 22 | `BlockRebar` Persistent Concrete Ref | Concrete rebar TE persisting target block via registry name | M1 (Session 2) | spec_miner_2 |
| 23 | `BlockICF` / `BlockPWR` Multiblock Ref | ICF and PWR decorative block and core coordinate persistence | M1 (Session 2) | spec_miner_2 |
| 24 | `BlockBedrockOreTE` Resource Ref | Bedrock ore deposit persisting target ore resource via `0name` string | M1 (Session 2) | spec_miner_2 |
| 25 | `ItemCustomMissile` Part Schema | Modular missile item persisting parts by name with instanceof validation | M1 (Session 2) | spec_miner_2 |
| 26 | `ContainerClicksMinecraftTest` Harness | Headless test harness verifying container click, shift, merge, and drag | M1 (Session 2) | spec_miner_2 |
| 27 | `InventoryBoundaryMinecraftTest` Harness | Headless test harness verifying TE inventory boundaries and slot limits | M1 (Session 2) | spec_miner_2 |
| 28 | `ItemRebarPlacer` Shift-Click Fix | Replace `return null;` with `return ItemStack.EMPTY;` in ContainerRebar:246 | M1 (Session 2) | spec_miner_2 |
| 29 | `ContainerDroneCrate` Slot Clear Fix | Ensure slot clearing executes on shift-click before returning | M1 (Session 2) | spec_miner_2 |
| 30 | `ammo_debug` Texture Declaration | Maps `ammo_debug` to `hbm:ammo_45` in `NativeItemTextureRegistry` | M2 (Session 3) | spec_miner_3 |
| 31 | `gun_pa_ranged` Texture Declaration | Maps `gun_pa_ranged` to `hbm:gun_pa_ranged` in `NativeItemTextureRegistry` | M2 (Session 3) | spec_miner_3 |
| 32 | Invisible Beam & Geyser Classification | 7 non-item world blocks categorized as quiet-by-design placeholders | M2 (Session 3) | spec_miner_3 |
| 33 | `tile.icf_block` Full Model Pipeline | Complete blockstate, block model, and item model for ICF Laser | M2 (Session 3) | spec_miner_3 |
| 34 | Log, Furnace & Sapling Model Parents | Normalized 10 item models to valid `hbm:block/...` parent paths | M2 (Session 3) | spec_miner_3 |
| 35 | Case-Sensitive Model Textures Fix | Fix `ModelM65Mono.png` and `ModelBroadcaster.png` mixed-case references | M2 (Session 3) | spec_miner_3 |
| 36 | Dynamic Slag Atlas Sprite Generation | Base path normalization to `textures/blocks` and stitcher pass-through | M2 (Session 3) | spec_miner_3 |
| 37 | High-ID Mold Models (>15) | Extended damage variant model selection up to ID 28+ | M2 (Session 3) | spec_miner_3 |
| 38 | Soyuz Multi-Skin Model Routing | Callback-priority variant dispatch for 3 distinct rocket skins | M2 (Session 3) | spec_miner_3 |
| 39 | Upgrade Tier Model Routing | Suffix-based (`_1.._N`) variant dispatch for machine upgrade tiers | M2 (Session 3) | spec_miner_3 |
| 40 | Minecart Multi-Layer & Anvil Resilience | Multi-pass model ignoring `display`, `RepairCost`, and stack size | M2 (Session 3) | spec_miner_3 |
| 41 | F3+T Reload Pipeline Stability | Cache invalidation and re-bake coordination across texture reload | M2 (Session 3) | spec_miner_3 |
| 42 | `ScheduledMessageHandler` Dispatch | Universal wrapper scheduling inbound Netty messages onto game threads | M3 (Session 4) | spec_miner_2 |
| 43 | `ClientPacketScheduler` Bridge | Client-only reflective bridge dispatching tasks to `Minecraft.addScheduledTask` | M3 (Session 4) | spec_miner_2 |
| 44 | `BufPacket` Payload Copy & Release | ByteBuf payload duplication (`copy()`) and guaranteed release in `finally` | M3 (Session 4) | spec_miner_2 |
| 45 | `EntityBufPacket` Copy & Release | Entity ByteBuf payload duplication and guaranteed release in `finally` | M3 (Session 4) | spec_miner_2 |
| 46 | `networkPackNT` Throttled Sync | High-frequency TE client synchronization with 1 Hz heartbeat deduping | M3 (Session 4) | spec_miner_2 |
| 47 | `TileEntityDiFurnace` State Sync | Direct ByteBuf serialization of progress, fuel, and IO configurations | M3 (Session 4) | spec_miner_2 |
| 48 | Dedicated Server Execution | Server running in `run/` with `eula=true` verifying tick loop stability | M3 (Session 4) | spec_miner_2 |
| 49 | `SkeletonCreator` `@SideOnly` Fix | Resolve client-only `EntityPlayerSP` reference in dedicated server classpath | M3 (Session 4) | spec_miner_2 |
| 50 | Native Entity Registration & Tracking | Forge entity registry mapping with tracking ranges and update frequencies | M4 (Session 5) | spec_miner_3 |
| 51 | Vanilla Spawn Egg Binding | Spawn egg creation using vanilla `Items.SPAWN_EGG` and `EntityTag` | M4 (Session 5) | spec_miner_3 |
| 52 | Glyphid Environmental Spawner | Dynamic swarm spawner evaluating soot pollution and mob limits | M4 (Session 5) | spec_miner_3 |
| 53 | Aberrator Dungeon Spawner | Phased spawner summoning soldier waves and rewarding artifacts | M4 (Session 5) | spec_miner_3 |
| 54 | Boss Summon Spawner | Mechanical key interaction summoning `EntityBOTPrimeHead` boss | M4 (Session 5) | spec_miner_3 |
| 55 | Entity Persistent Registry References | Schema v2 `<key>_name` NBT persistence for missiles, grenades, falling blocks | M4 (Session 5) | spec_miner_3 |
| 56 | Entity Client Renderers | Complete registration of render factories in `RenderingRegistry` | M4 (Session 5) | spec_miner_3 |
| 57 | 11 Celestial Dimension Providers | Registration of Moon, Duna, Eve, Orbit, etc. via `DimensionManager` | M4 (Session 5) | spec_miner_3 |
| 58 | Dimension 0 Provider Guard | No-op check preventing duplicate registration crash on dimension 0 | M4 (Session 5) | spec_miner_3 |
| 59 | Planetary Traits & Physical Metrics | Comprehensive celestial body data (gravity, atmosphere, radiation, orbit) | M4 (Session 5) | spec_miner_3 |
| 60 | Cross-Dimension Rocket Teleportation | Server-tick queued player + riding rocket transfer to other dimensions | M4 (Session 5) | spec_miner_3 |
| 61 | Persistent Solar System Saved Data | WorldSavedData stored in dimension 0 surviving celestial unloads | M4 (Session 5) | spec_miner_3 |
| 62 | Satellite Orbital Tracking | Body-associated satellite data with orbital dimension resolution | M4 (Session 5) | spec_miner_3 |
| 63 | Space Worldgen Dimension Filtering | `NTMWorldGenerator` isolation strictly to dimension 0 | M4 (Session 5) | spec_miner_3 |
| 64 | Dedicated Space Worldgen | Celestial ore and terrain generation for Moon, Duna, Eve, etc. | M4 (Session 5) | spec_miner_3 |
| 65 | Dimension Unload Cache Eviction | Event handlers clearing per-world radiation and atmosphere caches | M4 (Session 5) | spec_miner_3 |
| 66 | Cross-Session Branch Integration | Merge verified branches from Sessions 2, 3, 4, 5 into release branch | M5 (Session 6) | explorer_1 |
| 67 | Residual Polish & Release Compilation | CompileJava, reobfJar, ZIP integrity, and SHA-256 calculation | M5 (Session 6) | explorer_1 |
| 68 | Release Gate Status Matrix | Final documentation of all areas in `porting/CURRENT.md` | M5 (Session 6) | explorer_1 |

*(All 68 features assigned to Milestones M1–M5. Zero features unassigned.)*

---

## Milestones

| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Session 2: Runtime Verification of Inventories, Metadata & Persistence | Features 1–29: `ContainerNT`/`ContainerBase` rules, target container verification, machine metadata codecs, network blocks, legacy numeric ID persistence, and inventory defect fixes in `ItemRebarPlacer` & `ContainerDroneCrate`. | None (runs in `worktrees/session2`) | DONE |
| M2 | Session 3: Client Models, Textures & F3+T | Features 30–41: Texture declarations, `tile.icf_block`, log parent paths, case-sensitivity fixes in `ArmorGasMask`/`ItemModGasmask`/`RenderDecoBlock`, dynamic slag sprites, model routing, high-ID molds, Soyuz skins, upgrade variants, minecarts, and F3+T reload. | None (runs in `worktrees/session3`) | DONE |
| M3 | Session 4: Dedicated Server, Packets & TE Client Sync | Features 42–49: Dedicated server runtime, `ScheduledMessageHandler`, Netty buffer lifecycle (`copy`/`release`), TE sync (`networkPackNT`), and `@SideOnly` fix in `SkeletonCreator.java`. | None (runs in `worktrees/session4`) | DONE |
| M4 | Session 5: Entities, Dimensions & Space Worldgen | Features 50–65: Entity registration/spawners/persistence, 11 celestial dimensions, cross-dimension rocket teleportation, space worldgen filtering (`NTMWorldGenerator` dim 0 isolation), and dimension unload cache eviction. | None (runs in `worktrees/session5`) | DONE |
| M5 | Session 6: Integration, Residual Polish & Final Release Gate | Features 66–68: Cross-session integration, resolution of residual blockers, full compilation, reobfJar, ZIP integrity, final SHA-256, and release matrix in `porting/CURRENT.md`. | M1, M2, M3, M4 | DONE |

---

## Interface Contracts

### Session 2 (Inventories & Persistence) ↔ Session 6 (Integration)
- File boundaries:
  - `src/main/java/com/hbm/items/tool/ItemRebarPlacer.java`
  - `src/main/java/com/hbm/inventory/container/ContainerDroneCrate.java`
  - `porting/tests/` (unit and headless tests)
- Contract:
  - `ContainerRebar.transferStackInSlot` returns `ItemStack.EMPTY` (never `null`).
  - `ContainerDroneCrate.transferStackInSlot` clears slot to `ItemStack.EMPTY` and invokes `onSlotChanged()` before returning.
  - No modification to registry names (`hbm:tile.*`, `hbm:item.*`). Strict CRLF.

### Session 3 (Client Models & Textures) ↔ Session 6 (Integration)
- File boundaries:
  - `src/main/java/com/hbm/items/armor/ArmorGasMask.java`
  - `src/main/java/com/hbm/items/armor/ItemModGasmask.java`
  - `src/main/java/com/hbm/render/tileentity/RenderDecoBlock.java`
- Contract:
  - ResourceLocations must reference lowercase `"hbm:textures/models/modelm65mono.png"` and `"hbm:textures/models/modelbroadcaster.png"`.
  - No dummy fallback assets. Strict CRLF.

### Session 4 (Dedicated Server & Packets) ↔ Session 6 (Integration)
- File boundaries:
  - `src/main/java/com/hbm/particle/helper/SkeletonCreator.java`
- Contract:
  - `SkeletonCreator.java` must not import or reference `EntityPlayerSP` / `EntityOtherPlayerMP` on the server classpath. Mark client-only methods with `@SideOnly(Side.CLIENT)` or use string literals.
  - Dedicated server port 25565 runs exclusively in Session 4.

### Session 5 (Entities & Dimensions) ↔ Session 6 (Integration)
- File boundaries:
  - Entity registration in `ModEntityList.java`, dimension registration in `PlanetGen.java`.
- Contract:
  - Zero modification to dimension IDs or entity registry names.
  - `NTMWorldGenerator` strictly restricts structure generation to dimension 0.

---

## Code Layout
- Root project: `C:\Users\nagas\Desktop\port\ntm-port-1.12.2`
- Isolated Worktree directories:
  - Session 2: `C:\Users\nagas\Desktop\port\worktrees\session2\ntm-port-1.12.2`
  - Session 3: `C:\Users\nagas\Desktop\port\worktrees\session3\ntm-port-1.12.2`
  - Session 4: `C:\Users\nagas\Desktop\port\worktrees\session4\ntm-port-1.12.2`
  - Session 5: `C:\Users\nagas\Desktop\port\worktrees\session5\ntm-port-1.12.2`
- Required local dependencies:
  - `.local-dependencies/maven/appeng/appliedenergistics2/rv6-stable-7/appliedenergistics2-rv6-stable-7.jar`
- Baseline verified build artifact:
  - `build/libs/HBM-NTM-1.12.2-port.0.1.0.jar` (SHA-256 `C37C7DECE7AAF3646E7788724CE28223E1BFC3B01DCEC490C0E77E65A63F2C62`)
- Dedicated server runtime:
  - `run/` with `eula.txt` (`eula=true`) and `server.properties`

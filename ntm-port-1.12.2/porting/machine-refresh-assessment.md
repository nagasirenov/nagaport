# Shared tile refresh assessment (read-only, 2026-10-19)

## Decision

**Do not add an unconditional block-identity-only shouldRefresh to TileEntityLoadedBase or TileEntityMachineBase.** It fixes facing-only state updates but breaks existing same-block transitions that intentionally replace the tile role (core/proxy/no tile). The safe current boundary remains explicit opt-in per audited tile/block family. ElectricFurnace's override is an example, not evidence that every machine shares its invariant.

Documentation-only; no production files changed, builds/tests run, or implementation proposed as complete. This report comes from source/hierarchy/callsite scans, representative lifecycle inspection, and pinned native bytecode inspection. Automated hierarchy coverage does not mean every transition has been gameplay-tested.

## Historical distinction matters

Original build declares Forge `1.7.10-10.13.4.1614-1.7.10` (`ntm-original-pristine/build.gradle:40`). The exact local official Forge1614 userdev TileEntity patch's shouldRefresh uses `!isVanilla || oldBlock != newBlock`; modded tiles refresh whenever that hook is reached. Its separate metadata setter path updates metadata/cache rather than taking the block-replacement path. Thus “old metadata-only facing retained a tile” is not equivalent to “old modded shouldRefresh compared block identity.”

Immutable local evidence verified from the official Forge1614 userdev artifact:
- Userdev JAR SHA-256: `6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91`.
- Container entry `forgepatches.zip` (283644 bytes), SHA-256: `5cb7f1cd1c4faf0c435548c610c3322c15ae3524cbb4926f272ec057dce83bd4`. Its bytes exactly match the separately extracted archive.
- Nested entry `net/minecraft/tileentity/TileEntity.java.patch` (4561 bytes), SHA-256: `b0860fb3143667a109dc908fa905700adce088b4748ef7c2cac152f7f6a6f54e`.
- Nested entry `net/minecraft/world/chunk/Chunk.java.patch` (13438 bytes), SHA-256: `08e9af58a4d9a8d5063b8c376ae5040722e19bad23cb40afc0a54a2314f07553`.
The exact TileEntity patch confirms the modded always-refresh predicate; the exact Chunk patch confirms the block-replacement hooks and separate metadata path. This supersedes the initial branch-only provenance caveat. Original mod source equivalence and native1.7 runtime execution are separate questions; neither is claimed here.

Pinned target `.gradle/minecraft/forgeBin-1.12.2-14.23.5.2768-PROJECT(ntm-port-1.12.2).jar`, javap TileEntity.shouldRefresh: vanilla uses block identity, modded uses state identity (`oldState != newState`). State-based mutation now unifies old metadata-only updates with block-state replacement. A refresh override cannot recover which old operation the caller intended from state identity alone.

## Hierarchy and override audit

Paths below are relative to `ntm-port-1.12.2/src/main/java/`.

Whole-source declaration scan (including nested classes, excluding comments): 433 declared classes reach the native TileEntity root through locally declared ancestors. One additional class, `com/hbm/tileentity/machine/TileEntityFurnaceSpace.java:9`, extends external native TileEntityFurnace and must be treated separately. Counts are static scan observations, not compiled registration counts.

- `com/hbm/tileentity/TileEntityLoadedBase.java:13`: 79 direct and336 transitive descendants.
- `.../TileEntityMachineBase.java:27`: 125 direct and182 transitive descendants, already inside LoadedBase's set.
- `.../TileEntityTickingBase.java:5`: two direct/transitive descendants, also inside LoadedBase.
- `.../TileEntityInventoryBase.java:16`: one descendant, SoyuzCapsule, separate native root.
- Many direct native roots are outside these shared roots: bomb inventories, RTG furnace, multiblock/structural tiles, decorations, nested block tiles, third-party `cofh/api/energy/TileEnergyHandler`, and WIAJ dummy connector. A shared LoadedBase change cannot be described as whole-mod coverage.

All shouldRefresh declarations found:
1. `com/hbm/tileentity/network/TileEntityFluidValve.java:26`: obsolete1.7 signature; explicit block-identity policy.
2. `.../TileEntityFluidCounterValve.java:50`: same obsolete signature/policy.
3. `com/hbm/tileentity/machine/TileEntityMachineElectricFurnace.java:55`: native signature, block-identity policy with facing preservation comment.

The supplied pristine tree contains only the first two. Migrating their signatures is the strongest unambiguous next scope, but their adjacent old metadata cache/world APIs remain unresolved; do not claim complete valve compilation on signature changes alone.

## Concrete exceptions blocking shared-root migration

### Metadata encodes tile role, not just facing

`com/hbm/blocks/BlockDummyable.java:267` sets the same block with core metadata on placement. `makeExtra:322–336` and `removeExtra:339–352` deliberately call same-block setBlock while the metadata-only call is commented out. Extras are proxies; removing an extra must not retain that proxy as a ghost.

Representative factories:
- `blocks/machine/MachineChemicalPlant.java:20`: meta>=12 core, >=6 configured TileEntityProxyCombo, otherwise null.
- `blocks/generic/BlockAirScrubber.java:29`: same three-role split.
- `blocks/machine/rbmk/RBMKRod.java:37`: core versus inventory proxy versus null.
- `blocks/machine/rbmk/RBMKStorage.java:14`: core versus proxy.
- `blocks/machine/fusion/MachineFusionTorus.java:79`: core versus configured proxy versus null.
- `blocks/generic/BlockLanternBehemoth.java:28`: core tile versus null.

`TileEntityProxyCombo.java:37` extends ProxyBase -> LoadedBase. Core machines usually extend MachineBase -> LoadedBase. Therefore both suggested roots include demonstrably incompatible state roles. Same-block identity alone is not sufficient. Nor is “hasTileEntity remains true,” because both states can have different tile classes/configurations. Do not instantiate factory tiles to compare their classes: constructors/configuration may have effects and class equality misses differently configured proxies.

### Active/off variants intentionally restore tile identity

Existing save tile -> keepInventory -> replace block -> metadata restore -> validate -> reinsert patterns:
- `blocks/machine/MachineElectricFurnace.java:159–179` (native).
- `MachineDiFurnace.java:111–128`.
- `MachineDiFurnaceRTG.java:135–153`.
- `MachineBrickFurnace.java:116–134`.
- `MachineRtgFurnace.java:151–170` (its tile is a direct native root).
- `BlockFurnaceSpace.java:15–33` (external vanilla-derived tile).

Different block identities must still invalidate/replace normally before these methods restore the captured tile. Do not generalize into keeping every off/on tile indefinitely, delete keepInventory, or change validate/dirty/scheduler behavior in a refresh-only patch. BlockOrbitalStationLauncher.validate is a rocket validation false-positive, not tile reinsertion.

### Network switch/valve metadata is behavior-preserving state

`blocks/network/FluidValve.java:59–66`, `FluidSwitch.java:57–70`, `CableSwitch.java:51–59`, `CableDetector.java:51–64` mutate metadata then call updateState on a previously captured tile. Native refresh-on-property-change can invalidate that object before updateState. The valve overrides already encode preservation intent. `tileentity/network/TileEntityPipeBaseNT.java:86` destroys nodes on invalidate, so a wrong refresh is observably more than an allocation. These families merit explicit coordinated tests, not removal of invalidation.

### Other structural migrations need their own boundaries

`tileentity/machine/TileEntityICFStruct.java:38` promotes via setBlock with core metadata; `blocks/BlockDummyable.java:147–149` legacy monoblock promotion instead uses metadata-only update. `blocks/machine/BlockICF.java:75–96` and `BlockPWR.java:82–103` explicitly remove structure tiles and restore their recorded block on break. Do not merge these distinct replacement mechanisms into a universal rule.

## Smallest safe reusable design and tests

1. Keep default root refresh unchanged for now. Port the two explicit valve signatures without changing their policy; test with native IBlockState. Keep furnace override until a genuinely identical audited family policy exists.
2. If extracting reuse, make it an **opt-in semantic policy** for blocks whose every same-block state has the same persistent tile role. Do not move policy to all LoadedBase/MachineBase descendants or reparent all direct roots mechanically.
3. A later shared root could delegate to an explicit block tile-role policy with conservative default refresh. Require role-transition tests for core/proxy/none and configured proxies before opting dummyable blocks in. Inventing this API now is larger than a native-signature migration.
4. InventoryBase has only SoyuzCapsule (`blocks/machine/SoyuzCapsule.java:27` always creates same tile), so it is narrower, but no observed need alone justifies changing its default; audit its actual state mutation before adopting opt-in.

Required native tests, not just boolean predicates:
- Native Chunk/World.setBlockState, loaded tile/scheduler maps, identity and inventory/name preservation after facing/property changes; no duplicate ticking/invalidation.
- Changed block -> air/unrelated tile invalidates/removes exactly once, no ghost tile or stale network entry.
- Valve open/close retains exact tile/counter/type; closes node through updateState rather than destructive invalidate; invalidation still destroys node on break.
- Dummyable plain->extra->plain, proxy->core, core->proxy; expected null/class/configuration after each state transition and no old tile remaining in ticking collections.
- Off/on block replacements still preserve captured inventory exactly once via existing restoration path; no drops during toggle, one drop on break, dirty/save reload unchanged.
- Both client and server paths, chunk reload, native TileEntityFurnace-derived subclass behavior; no accidental removal of subclass-specific override.

A source manifest should list affected descendants and explicit policy exceptions and fail closed if new descendants/overrides appear. A focused test of a root must not be presented as approval of every descendant's block-state semantics.

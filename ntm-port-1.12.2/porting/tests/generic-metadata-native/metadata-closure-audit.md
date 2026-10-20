# Generic metadata and random-concrete closure audit

Audit scope: CP54 source tree, read-only production inspection. No downloads,
full Gradle build, production edits, or gameplay/runtime claims. The requested
placement slice needs only metadata zero; adding a new metadata property is not
necessary and would broaden the semantic change.

## Safe bounded target

`src/main/java/com/hbm/lib/Library.java:307-312` defines one public static
`Block getRandomConcrete()` method. Its four returns are exactly:

- `ModBlocks.brick_concrete_broken`
- `ModBlocks.brick_concrete_cracked`
- `ModBlocks.brick_concrete_mossy`
- `ModBlocks.brick_concrete`

Each field has exactly one assignment in the Java source tree, at
`src/main/java/com/hbm/blocks/ModBlocks.java:1735-1738`. Each right-hand side
constructs an exact `new BlockGeneric(Material.ROCK)`, not a subclass or anonymous
class. The static helper has no virtual dispatch. This is a source-closure proof,
not a guarantee against arbitrary external/reflection-based field mutation.

`BlockGeneric.java:7-11` only delegates construction to `BlockBase`.
`BlockBase.java:20,29-35` directly extends native `net.minecraft.block.Block`
and delegates both constructors. Neither class defines `createBlockState`,
`getStateFromMeta`, `getMetaFromState`, or changes its default state.

The 2,512 initially eligible helper call sites all supply metadata 0 and flags 3.
The final implementation selects 1,595: 917 in Silo.generate_r0 are deferred
after actual javac rejected their expanded selected statements as code too large.
The following table describes candidates, not the final edited count:

| Dungeon file | Calls |
| --- | ---: |
| Barrel.java | 101 |
| DesertAtom001.java | 182 |
| DesertAtom002.java | 564 |
| DesertAtom003.java | 325 |
| Silo.java | 1,340 |

Consequently these sites can use native `setBlockState(BlockPos,
block.getStateFromMeta(0), 3)` without any new property or metadata conversions.
Preserving the legacy coordinate guard and expression evaluation order is a
separate placement-conversion requirement.

## Native behavior verified from the existing Forge binary

Read with Java 8 `javap -c -p`, without executing or changing the dependency:

- Artifact: Forge 1.12.2-14.23.5.2768 `forgeBin` already present under the
  CP53 compile snapshot's `.gradle/minecraft` directory.
- SHA-256: `8c75037188f87bc62939eefa03c2480551509034f069728de012c09276f8f49e`.
- `Block.createBlockState()` constructs a `BlockStateContainer` with an empty
  `IProperty[]`.
- Native construction calls `createBlockState()` virtually, then sets the
  default state to that container's base state.
- `Block.getStateFromMeta(int)` returns the default state.
- `Block.getMetaFromState(IBlockState)` returns 0 for an empty property set;
  it throws if properties exist and no conversion override handles them.
- `Block.damageDropped(IBlockState)` returns 0. `getPickBlock(...)` delegates
  to `getItem(...)`, which constructs the block item with `damageDropped(state)`.
- Native tile-provider detection uses `instanceof ITileEntityProvider`.

Thus the four concrete identities already possess a real native metadata-zero
state, with native zero-damage drops/picks and no tile refresh path. This does
not establish nonzero metadata preservation for generic blocks.

## BlockGeneric closure, if later nonzero work is needed

Exactly four direct descendants and no indirect descendants were found:

- `generic/BlockGenericDrop.java`: only constructor and legacy
  `getItemDropped(int, Random, int)`; no state overrides or tile provider.
  That old drop signature is not the native state-based callback. A future
  state patch must not silently claim to repair this independent legacy issue.
- `generic/BlockDragonProof.java`: only constructor and legacy entity-destruction
  callback; no state overrides or tile provider.
- `machine/BlockGenericPWR.java`: constructor and tooltip only.
- `machine/rbmk/RBMKLoader.java`: constructor, fluid-connectivity test, tooltip;
  no tile provider despite its machine-related name.

The registry contains 71 exact `BlockGeneric` constructions, plus 1 GenericDrop,
1 DragonProof, 6 GenericPWR, and 1 RBMKLoader: 80 constructed identities in this
five-class closure. A native property owned by BlockGeneric would be far more
bounded than a BlockBase change, but is unnecessary for this metadata-zero slice.

## Why not add metadata to BlockBase

Source inheritance closure contains BlockBase plus 79 descendants. Ten classes
define the complete native state-container/conversion trio: BlockChargeBase,
BlockDecoCRT, BlockDecoModel, BlockDecoToaster, BlockNTMFlower, BlockRubberCacti,
BlockWoodStructure, RadioTorchBase, RedBarrel, and YellowBarrel.

One partial override is incompatible with naive inherited metadata conversion:
`blocks/bomb/DetCord.java:31-32` supplies an `ExtendedBlockState` with **zero
listed properties**, retaining only unlisted `ConnectionState.CONNECTIONS`.
It inherits conversions through BlockDetonatable and BlockBase. Adding inherited
`withProperty(META, ...)` / `getValue(META)` would fail for its state container.

Also, unconditionally setting a new base META property in a BlockBase constructor
would operate on the subclass-created container because native construction
dispatches `createBlockState()` virtually. Existing containers use different
properties, including BlockChargeBase.ORIENTATION, BlockRubberCacti.TYPE, and
BlockNTMFlower.VARIANT. Their later constructor defaults do not make an earlier
base-constructor property assignment safe. Existing complete trios/defaults
must stay isolated from a generic-base metadata change.

The broader closure also includes BlockMulti's native damage bridge
(`BlockMulti.java:21-26`) and tile-provider branches such as BlockContainerBase,
BlockCMPort, and the graphite machine family. Their legacy subtype/drop/refresh
semantics are outside this concrete-only closure. There is only one exact
`new BlockBase(...)` registry construction, press_preheater at ModBlocks.java:2519;
the concrete identities are exact BlockGeneric instances instead.

## Rendering and item boundaries

`main/LegacyBaseModelHandler.java:64-76,100-120,200-213` accepts eligible full-cube
BlockBase instances, registers eligible ItemBlockBase forms, and substitutes
legacy atlas models for discovered state model keys. The four concrete items
are ItemBlockBlastInfo registrations at `ModBlocks.java:3076-3079`, and
`ItemBlockBlastInfo` extends ItemBlockBase.

`render/model/LegacyBaseBakedModel.java:87-98` obtains metadata through the
block's native conversion and caches quads by metadata. The existing empty
state converts to 0, and `BlockBase.getIcon(int,int)` returns its single icon.
`items/block/ItemBlockBase.java:34-40` only passes item metadata through for
IBlockMulti; exact BlockGeneric is not IBlockMulti. No item/model/default-state
change is required for the audited metadata-zero placements.

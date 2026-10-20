# Registered-state write source-closure audit

## Scope and evidence level

This audit describes the selection in `manifest.json`, based on
`502f0b9dc3cd3e14622edd897fe3c753e309818d`: **72 statements in 32 caller files**,
covering **32 ModBlocks fields and 12 concrete owner classes**. There are 42
four-argument calls and 30 six-argument calls; 65 retain flags 3 and seven retain
flags 2. Every selected original call is an expression statement whose boolean
result is ignored. The manifest, not a class-wide or name-pattern rule, defines
the selected call sites.

The authorized proof boundary is the selected write expression, its evaluated
arguments, exact source-owned block identity, metadata domain, genuine native
state conversion, and native storage operation. Bounded test projections may
isolate that boundary while retaining the real production codec and genuine
Minecraft/Forge classes. Such projections are **not complete compiled versions
of their containing production owners** and do not establish native dispatch or
behavior of omitted callbacks. This document does not claim whole-ModBlocks
initialization, complete enclosing-method compilation, a full build, a game
launch, or complete gameplay equivalence.

`manifest.json` retains the exact constructor assignments and original/replacement
statements. `baseline.zip` and the source regeneration/inverse audit provide the
intended check that bytes outside those statements remain unchanged. Source
observations below are not substitutes for the executable attribution, range,
identity, conversion, storage, and mutation checks in the surrounding bundle.

## Selected owner and field inventory

Counts below are selected statements, not all occurrences of these blocks.

| Actual constructor owner | Selected fields and counts | Metadata domain |
| --- | --- | --- |
| `BlockGeneric` | `rubber_silt` 4; `crystal_hardened` 3; `brick_jungle` 2; `block_foam`, `brick_concrete_mossy`, `brick_jungle_cracked`, `concrete`, `ore_nether_schrabidium`, `ore_tektite_osmiridium`, `reinforced_stone`, `stone_gneiss`, `tektite` 1 each | Exactly 0; 18 calls |
| `BlockNoDrop` | `barricade` 3; `oil_pipe` 1 | Exactly 0; 4 calls |
| `BlockStepEffect` | `waste_planks` 8; `frozen_planks`, `waste_trinitite`, `waste_trinitite_red` 2 each; `block_meteor_cobble`, `brick_jungle_mystic`, `brick_jungle_ooze`, `frozen_dirt`, `ore_gneiss_schrabidium` 1 each | Exactly 0; 19 calls |
| Native `net.minecraft.block.BlockFalling` | `concrete_super_broken` 2 | Exactly 0 |
| `BlockDecoCT` | `deco_steel` 3 | Exactly 0; two flags-3 writes and one flags-2 write |
| `BlockOreBasalt` | `ore_basalt` 5 | Literal 0, 1, 2, 4, 3 in the original branch order |
| `BlockDeadPlant` | `plant_dead` 9 | Eight enum-ordinal writes in 0..4; one stored-metadata write in 0..15 |
| `BlockTallPlant` | `plant_tall` 4 | WEED 0, WEED upper half 8, CD2 1, CD2 upper half 9 |
| `BlockRubberPlant` | `rubber_plant` 2 | GRASS/FERN/SHRUB ordinals 0..2 |
| `BlockNTMSand` | `sand_mix` 1; exact `this` write 1 | BORON ordinal 0; implicit metadata 0 |
| `BlockBiomeStone` | `stone_biome` 1 | DESERT/WOODLAND ordinals 0..1 |
| `BlockCrashedBomb` | `crashed_balefire` 2; its `DesertAtom001.Block4` alias 1 | Literal 5; random 0..3; alias literal 2 |

## Constructor and alias closure

The 32 exact assignments are recorded in the manifest. Each constructs the
stated concrete class, rather than a subclass selected by a factory. Casts in
the builder chains change only the static type. The reviewed native Block
builders return the receiver. NTM-owned `BlockBase.setBlockName`,
`setBlockTextureName`, and public `setSoundType` also return that receiver.
`BlockStepEffect.noFortune` changes only its fortune option;
`BlockStepEffect.withSoundType` delegates to the protected native sound setter
and returns `this`. `BlockDecoCT.noFortune` likewise returns `this`.

`PlainCubeTextures.bind` is an explicitly allowlisted, identity-keyed texture
sidecar. It rejects null/unreviewed concrete owners, records the supplied texture,
and returns the supplied block object. It does not replace the block, its
superclass, state codec, or gameplay callbacks. This is the outer binding for
the selected StepEffect and NoDrop fields and `concrete_super_broken`.

The current source has one assignment for each selected ModBlocks field and no
declaration initializer for those fields. The exact-assignment guards must
continue rejecting additional assignments, changed constructor owners, and
unreviewed builders. Public-field replacement by addons or reflection is outside
this closed source contract; the fields are not claimed to be immutable Java
constants. Source subclasses of BlockGeneric and vanilla BlockFalling exist,
but are not the objects created by these exact assignments.

Two non-direct field expressions require additional closure:

- `DesertAtom001.Block4` is the instance field initialized at
  `DesertAtom001.java:25` from `ModBlocks.crashed_balefire`. The selected statement
  reads `Block4`, not the registry field again. There is no other source
  assignment to this alias. The replacement preserves the captured alias,
  including its initialization timing. The normal-lifecycle identity claim
  assumes the generator instance was created after ModBlocks initialization;
  a pre-initialization null snapshot is not converted into a later field lookup.
- The implicit-zero write in `BlockNTMSand.fall` captures `this`, not
  `ModBlocks.sand_mix`. Current source contains one BlockNTMSand construction
  (`ModBlocks.java:1900`) and no source subclass. This bounds the ordinary
  registered receiver; externally constructed or subclassed owners are not
  certified by that source fact. The replacement retains virtual conversion on
  the actual captured receiver and does not substitute a different object.

Registry construction/registration can still have unrelated porting problems.
Pinning a source assignment is not a claim that the complete registry compiles
or initializes successfully.

## Genuine native codec closure

### Metadata-zero owners

`BlockGeneric -> BlockBase -> Block`, `BlockNoDrop -> Block`,
`BlockStepEffect -> Block`, and native `BlockFalling -> Block` do not override
`createBlockState`, `getStateFromMeta`, or `getMetaFromState` in these chains.
The genuine native Block codec returns `getDefaultState()` from
`getStateFromMeta`, and returns 0 for states with an empty property collection.
The selected input domain is exactly zero, not arbitrary legacy metadata.
Calling that inherited codec therefore preserves the selected stored value
without inventing a generic metadata adapter.

### Explicit CT nibble

`BlockDecoCT` owns an ExtendedBlockState with `CTContext.METADATA` covering 0..15
and a separate unlisted connected-texture property. Its constructor sets
metadata 0; conversion applies `metadata & 15`, and reverse conversion reads the
listed property. The three selected writes use 0. Connectivity and extended
render context are not consulted by this conversion.

### Inherited BlockMulti nibble

`BlockOreBasalt`, `BlockDeadPlant`, `BlockTallPlant`, `BlockRubberPlant`,
`BlockNTMSand`, `BlockBiomeStone`, and `BlockCrashedBomb` inherit through
`BlockEnumMulti -> BlockMulti -> BlockBase -> Block`. BlockEnumMulti does not
override the state codec. The real production BlockMulti owns
`PropertyInteger("metadata", 0, 15)`, constructs a native BlockStateContainer,
converts with `getDefaultState().withProperty(METADATA, meta & 15)`, and reads
the same property for reverse conversion.

The enum and item/drop rectification functions are not state conversion.
In particular, the crashed-bomb write of literal 5 remains 5 even though
EnumDudType has four constants. Upper-half plant values 8 and 9 remain upper
halves. They are not clamped to a subtype count.

The selected dynamic domains are independently bounded by their source:

- `WorldGenRubberPlant`: each selected local starts with `rand.nextInt(3)` and
  its exhaustive switch maps to the three EnumRubberPlantType ordinals.
- `BiomeCave.handleBiome`: `type` is the EnumBiomeType DESERT or WOODLAND;
  its callers reject null before invoking the method.
- `Dud`: `rand.nextInt(EnumDudType.values().length)` yields 0..3. The random
  call remains in the original argument evaluation position.
- `OilSpot.generateCrack`: the selected `meta` is read from the existing world
  state, and the branch requires `ground == ModBlocks.rubber_plant`. Its actual
  state owner uses the BlockMulti nibble codec, so this is a stored value 0..15,
  not an unrestricted item-damage or arbitrary configuration integer.
- Selected tall-plant expressions use the fixed WEED/CD2 ordinals and add 8;
  they are not the unrelated arbitrary-stack-damage expression.

Admission of canonical stored values does not prove that arbitrary raw metadata
is safe to mask at a write boundary. Raw 16, negative values, or larger item
damage are not silently admitted by this selection.

## Evaluation, nulls, and coordinate domain

Each replacement captures the original receiver, X, Y, Z, block expression,
metadata, and flags once, left to right, before checking the receiver. This
retains argument evaluation and exceptions even when coordinates are outside
the old domain. Four-argument calls supply the original implicit metadata 0
and flags 3; six-argument expressions and flags are not normalized.

The receiver null check precedes domain rejection. Conversion is delayed until
after the original domain check: `-30000000 <= X,Z < 30000000` and
`0 <= Y < 256`. Out-of-domain calls do not dereference a potentially null block
or call its conversion. In-domain writes use the existing
`WorldBlockState.write(World,int,int,int,IBlockState,int)` boundary. Its repeated
domain check is intentional; it is not a new Block/metadata compatibility API.

Normal in-domain identity equivalence assumes initialized, nonnull registered
objects and the alias lifecycle stated above. Matching a null-target exception
type alone does not establish identical original chunk-allocation or cache
failure side effects; pre-initialization/null-target writes are not claimed as
equivalent successful lifecycle writes.

## Callback and projection limits

Only the manifested statements are changed. This does not erase the following
existing source behavior or certify unfinished native callback migrations:

- Generic, NoDrop, and DecoCT have no custom write-time onBlockAdded/removal
  mechanics. NoDrop's null item drop and DecoCT's connectivity/fortune behavior
  remain outside the mutation expression.
- StepEffect has no onBlockAdded, breakBlock, tile provider, or state-codec
  override. Walking, drops, particles, and its molten-meteor player-destruction
  behavior remain unchanged. Those legacy callback gaps must not be described
  as fixed by these writes.
- `concrete_super_broken` is the genuine native BlockFalling owner. Original
  vanilla BlockFalling (`akx` in the archived original client) and native
  BlockFalling both schedule the same owner at the callback coordinates using
  tickRate 2 in their added/neighbor callbacks. A plain-Block projection cannot
  prove this lifecycle behavior. Falling entity physics and scheduled-tick
  execution remain outside a codec/storage-only result.
- BlockNTMSand is **not** plain BlockFalling. Its existing legacy onBlockAdded
  and neighbor callback schedule tickRate 2; its falling algorithm and entity
  construction still contain unrelated legacy calls. A projection using the
  real inherited BlockMulti codec proves its selected state values, not
  production dispatch of these callbacks or complete falling behavior.
- Dead, tall, and rubber plants have support/neighbor/drop behavior; TallPlant
  additionally has growth, paired-half, harvest, and random-tick mechanics.
  Native storage of the selected metadata is not certification of those
  interactions, including notifications between adjacent plant halves.
- OreBasalt's asbestos walking/drop effects remain; its native neighborChanged
  callback is explicitly empty. The enum has item initialization prerequisites.
  A state-codec projection does not establish full ore/item lifecycle startup.
- BlockCrashedBomb implements the genuine `ITileEntityProvider` and
  `createNewTileEntity` returns a real `TileEntityCrashedBomb`. The tile has
  radiation/ticking behavior. No tile-free projection may be represented as
  proof of tile creation, refresh, replacement, persistence, radiation, or
  activation/explosion mechanics. Those dependencies remain in production
  source and require their own evidence wherever such behavior is claimed.

The bounded source changes leave custom callbacks in place; that fact is
different from proving they already compile or dispatch correctly on 1.12.2.
Where tests omit such dependencies, their result must remain explicitly limited
to argument capture, codec, and storage. Arbitrary old-block removal effects,
neighbor behavior, observers, lighting, Forge snapshots, server networking,
client rendering, save/reload, and full containing-method behavior are not
established by this source audit.

## Explicit exclusions

- **OreCave:** the `ore.block`/`ore.meta` pairs are carried in a MetaBlock object
  accepted through configurable constructors. `meta` is not established as a
  canonical stored nibble merely because common registrations use small values.
  The owner and mutable metadata require an independent closure proof; the
  writes in `OreCave.java:176,185,189` are not selected.
- **Chunk absorber replacement:** `ModEventHandler.onChunkLoad` writes through
  `Chunk.func_150807_a` with chunk-local X/Z in 0..15 and Y in 0..254. This is not
  the selected World-coordinate API. The new BlockAbsorber has an added callback
  scheduling tickRate 10, and its recurring radiation updates also schedule
  themselves. Replacing these calls with World writes without proving local to
  world coordinates and original/native scheduling would change the boundary.
  The four absorber replacement calls remain excluded.
- **Tall plant from arbitrary stack damage:** the still-existing
  `BlockTallPlant.onBlockPlacedBy` call writes `stack.getItemDamage() + 8`.
  Arbitrary item damage and integer arithmetic are not bounded by the four
  fixed enum/upper-half writes in this manifest. This call remains excluded.
- **BlockHazard/block_corium:** its real added callback initializes radiation
  and schedules updates. It was not admitted as a harmless metadata-zero owner.
- **Other falling registrations and other aliases:** this manifest selects only
  `concrete_super_broken` from native BlockFalling. It does not migrate all
  fourteen initially discovered falling calls, other unresolved construction
  chains, arbitrary block variables, or every identity-compatible alias.

No full build was run for this audit. Its only repository deliverable is this
Markdown file; it makes no changes to production code or other evidence.

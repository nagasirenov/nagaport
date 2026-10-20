# Dungeon palette owner audit (CP56)

This is source inventory, not compiler symbol attribution. `owner-audit.py` reads the pinned CP56 tree, never production working files, and produces `owner-audit.json` with every palette initializer, owner construction, literal metadata/flag placement count, generator subclass, and qualified palette write. It changes no production.

## First safe migration slice

Ruin001 declares package-private, nonstatic, nonfinal `Block` instance fields Block1/2/3 initialized from ModBlocks.brick_concrete, brick_concrete_cracked, brick_concrete_broken. They have 211/7/14 placements. Ruin002 declares the same kind of Block1 field initialized from brick_concrete, with 199 placements. These 431 calls all use metadata 0 and flag 3. No explicit constructors, field reassignment, local/parameter shadow, anonymous generator subclass, or source subclass occurs. The fields are accessible to package peers, so preserve actual field reads rather than substitute static ModBlocks references or assume Java final semantics.

The three ModBlocks construction statements at lines 1735/1737/1738 each instantiate exact BlockGeneric(Material.ROCK), not a subclass. BlockGeneric extends BlockBase, which extends native Block; neither overrides state container or metadata conversion. Metadata 0 is the native default-state equivalence, not a generic promise about other metadata.

Existing `generic-metadata-native` contains real complete owner compilation, exact constructors, metadata-zero World/Chunk storage and persistence, model/drop evidence, and source manifests for all three concrete owners. `concrete-placement-native` separately establishes left-to-right argument capture, original pre-chunk coordinate range checks, null timing, and native placement conversion for the same owner set. Reuse those boundaries explicitly; do not claim complete generator or full ModBlocks compilation.

## Additional candidates, requiring matching owner proof

- reinforced_sand, reinforced_brick, reinforced_light, brick_light and ore_nether_plutonium are exact BlockGeneric constructors. Existing generic evidence covers the owner class, but not these exact construction/assets/registration identities. Extend that evidence before asserting full identity coverage.
- deco_tungsten, deco_red_copper, deco_steel and deco_lead are BlockDecoCT. reinforced_glass is BlockNTMGlassCT. These have native metadata properties preserving 0..15; see `ct-native` and native block-owner evidence. Geometry-only CT evidence is not itself complete owner lifecycle proof.
- Ruin002 Block5 is steel_wall / BlockSteelDecoration, with four metadata-5 calls, not metadata zero. It has native 0..15 state machinery and a separate `steel-native` proof.
- Do not infer readiness for remaining fields just because another field in the generator is native: many map to distinct tile/machine/fluid/ore owners.

Repository-wide textual scans found no generator subclasses and no qualified `.BlockN =` writes. These are closed-source-tree observations, not guarantees against reflection, external code, or a future source change. Java attribution should enforce exact field owner and no shadowing for the migrated use sites.

## Working-tree closure verification

`python3 porting/tests/dungeon-palette-native/check-owner-closure.py` passes on the migrated tree. It rejects nine in-memory source mutations (field write, external qualified write, local shadow, parameter shadow, named subclass, anonymous subclass, qualified static owner write, unqualified static owner write, and changed field modifiers). It intentionally rejects any external `.Block1/.Block2/.Block3` access rather than trying to guess receiver types. This conservative lexical guard complements, and does not replace, actual Java attribution. The script matches the original three exact construction statements against pinned CP56.

The executable closure check no longer requires Git history: complete original ModBlocks.java is preserved in `owner-sources.zip`; the archive SHA-256 is pinned in the checker and `owner-source-manifest.json`, which also records CP56 and the source digest. The broad exploratory owner-audit.py remains a Git-baseline inventory utility, not a required verification step.

Native owner proof was rerun successfully in the isolated worktree: `owner-native-rerun.log` records 24,754 assertions, complete exact owner constructions, real World/Chunk/Anvil compressed persistence, drop/pick, original bytecode RNG oracle, and two actual atlas/GL reloads. Only the four required tracked concrete PNG assets were restored from CP56 into this isolated checkout; no sparse-checkout setting changed. The existing generic-metadata-native source manifest compared unchanged and was not regenerated.

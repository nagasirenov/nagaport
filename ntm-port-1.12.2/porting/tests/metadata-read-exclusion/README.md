# Metadata read exclusion audit

Current continuation note (CP86, 2026-10-20): the BlockLayering recommendation below is historical and has been completed. See ../layering-native/README.md and the current BlockLayering.java codec/lifecycle. Do not repeat that migration. Other listed domains still require fresh source evidence before selection.

Exact inspected base: CP72 e626aa7ad59f440bdbf6c98c9fee8c5740accec6.
No production changes, no new runtime tests, no successful build claim.
The JSON pins current source bytes and the previous increment's 26 attributed
World metadata reads in 17 files. Those positions/counts are historical evidence,
not a fresh CP72 compiler attribution or exhaustive current census.

## Findings

- `WorldBlockState.read(World,int,int,int)` already implements the original
  world-coordinate bounds and receiver-null check. `metadata(IBlockState)`
  already uses the state owner once. A missing general helper is NOT the blocker.
- The largest historical group is minecarts: seven reads in two classes. General
  rails permit custom owners. The activator/golden-rail cases retain a cached
  block identity across virtual calls (`useRail`, `func_145821_a`, rail-position
  hooks). That identity alone does not prove the owner of the later live read.
  Substituting cached state or metadata would also change evaluation semantics.
- Projectile hits, rubble/fallout, missiles, particles and block-impact effects
  can read arbitrary custom blocks. Native metadata cannot preserve an arbitrary
  original nibble until those blocks implement an appropriate state mapping.
- `BlockLayering extends Block` has no state factory or metadata codec, despite
  masks, layer-dependent bounds, quantity and replacement predicates. Four real
  registrations are foam_layer, sand_boron_layer, leaves_layer and oil_spill.
  XFactoryTool's two layer increments require this root, not merely read syntax.
  Their intervening replaceability/placement callbacks must also be audited.
- `BlockVolcano extends BlockContainer` has no state codec. FalloutRain copies
  volcano_core metadata into volcano_rad_core. Both source and destination
  mappings must be established before migration.
- VolcanicBlock inherits Forge BlockFluidClassic; its volcanic-lava zero check
  needs an explicit original/native fluid-level codec comparison, not a guessed
  interpretation based on class name. Water-material checks are not sufficient
  to identify a vanilla water owner.
- BlockDummyable DOES have the 0–15 native property and an existing private
  single-read helper. Six tempting descendant reads (two door, two plasma
  heater, one radar, one crane console) are declared IBlockAccess, not World.
  Hydroponic also reads neighbors. The old World proof does not cover them.
- WorldInAJar is a real IBlockAccess implementor with separately stored raw
  `short` metadata; its getBlockState decodes via the block's getStateFromMeta.
  Re-encoding can lose values for unconverted owners. A universal IBlockAccess
  adapter would conceal this problem. Callback-owned state can be used only
  after proving the particular callback's original live-read semantics.

## Recommended coherent next root

Port BlockLayering as a complete native state/bounds/lifecycle family for all
four registrations. Audit original valid and high-bit metadata, preserve the
original `meta & 7` consumers and raw comparisons deliberately, and test native
placement, layer addition, support removal, replacement, drops and chunk save
roundtrips. Then revisit its exact XFactoryTool readers/writers. This is a
bounded real shared root with existing already-migrated readers that currently
collapse metadata, unlike a broad shim or a six-call interface microbatch.

The broader arbitrary-block consumers require incremental owner codec closure;
there is no single safe global metadata fix. Existing generic-metadata-native
proof explicitly demonstrates nonzero values collapsing to zero, not parity.

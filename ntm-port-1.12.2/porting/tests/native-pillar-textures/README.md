# Native pillar owner/model lifecycle

Base: sealed CP96 `f44d3ffc5ad3bfa0afcfd93d035e51bdecde2de4`.
Production is frozen at patch SHA-256
`4d885d15dc471f76f1519a4981eae4124b55a951ee213acb270eeb3ad33717bc`.
Only three production files change: `blocks/ModBlocks.java`,
`blocks/machine/BlockPillar.java`, and new `main/PillarModelHandler.java`.
No main-worktree edit, dependency operation, network access, push or full-mod
build was performed by this worker.

## Exact finite owner family

The eight original direct BlockPillar instances are gneiss_chiseled, basalt,
block_smore, brick_forgotten, meteor_battery, fusion_heater, watz_element and
watz_cooler. The three PWR instances are pwr_fuel, pwr_control and pwr_channel.
PWR's entire additional behavior is its unchanged tooltip interface/delegate;
it does not override rendering or texture selection. `sites.json` records all
11 verbatim construction and registry statements, original item classes and
texture identifiers.

The owner still extends native Block, not BlockBase or a replacement class.
Constructors, the texture setter's original String references/order, top/alt
precedence, return identity and getIcon face expression are retained. Removed
native fields are owned here with their original protected visibility. The
callback still registers alternate, top and side in that order. BOTH DOWN and
UP use top; the alternate is registered but never used for a face. Empty-alt
hbm:code fallback is original behavior, not a newly invented default texture.
Original block-relative paths are translated to native `blocks/` atlas paths.
Only the 11 existing receiver chains receive a cast; no registry/item class,
construction value, original side effect or gameplay callback is rewritten.

## Native world state, independently of item metadata

The initial visual candidate inherited native Block's metadata collapse to
zero. That candidate was not frozen. The final owner represents all 16 values
with an integer property and masks arbitrary inputs to the original nibble.
Default state and ordinary item placement/drop metadata remain zero. All 16
world states route to the same original metadata-independent art.

`metadata-original-symbol.txt` and `metadata-original-metadata-bytecode.txt`
retain existing evidence identifying original Chunk metadata updates.
`original-apz-bytecode.txt` shows that path delegates to storage's metadata
nibble array. `original-apv-bytecode.txt` shows both halves read/write with
`& 15`. `original-block-field-visibility.txt` shows the old protected String
and icon fields. These are disassemblies of the cached unmodified original
client, not invented engine behavior.

PWR assembly logic tests block identity. Excluded BlockForgottenLock reads
inherited world metadata before generation, so discarding metadata at this
shared owner would not be safe. The final native test verifies every selected
owner's 0..15 state conversions, raw-int masking, actual ExtendedBlockStorage
state retention, native named-state NBT write/read and unchanged drop metadata.
This is not an old-world import or full chunk-save migration certification.

## Native geometry, items, atlas and reload

A client-only native model loader explicitly admits only exact BlockPillar and
BlockPillarPWR classes. It uses genuine vanilla cube geometry with distinct cap
and lateral textures and the already-tested PlainCubeModel UV convention. The
native model supplies culling, normals, untinted color, AO and display matrices;
there are no placeholder sprites or fabricated geometry. Alternate texture
registration is also retained as a model dependency.

Registration occurs after actual Block and Item registry events. Missing
ItemBlocks fail explicitly, never registering AIR. Existing ItemBlock,
ItemBlockBase and ItemBlockLore identities remain unchanged. A constant native
mesh definition covers all raw item damage, not only 0..15. No baked model or
sprite is cached by the new handler; native model-cache invalidation and atlas
reload refresh both models and legacy icon wrappers.

The five other current descendants, BlockWriting, BlockForgottenLock, DetMiner,
BlockCMFlux and BlockCMHeat, are not automatically admitted to this renderer.
All five source files are unchanged. None refers directly to the texture/icon
fields or overrides texture/state methods, and their exact constructors still
call super(mat, top). Protected member visibility is additionally compiled and
exercised from a separate-package descendant. The complete CMFlux and CMHeat
owners also compile and exercise inherited callbacks. The other three contain
unported gameplay APIs; their whole bodies are not claimed native-compiled or
rendering-complete. See `descendant-audit.json`.

## Completed evidence

- `native.log`: PASS **18,631** assertions. All 11 exact construction/registry
  statements, full pillar/PWR owners, true Forge events/model loading/baking,
  all 22 original 16x16 PNGs through actual mipmap/stitch/Mesa GL upload and pixel
  readback, old RenderBlocks XYZ/UV/winding oracle, native perspective/culling,
  arbitrary raw item metadata, all 16 world metadata values and two reloads.
- The second reload places an unchanged original basalt PNG in a temporary
  higher-priority resource pack at one cap path, verifies the selected bytes,
  then discards the temporary directory. No production resource is modified.
- `source.log`: exact reverse patch reconstructs the entire CP96 ModBlocks and
  BlockPillar byte-for-byte and removes only the new handler. All untouched
  descendants/items/model/registry helpers are hash-guarded.
- `resource-audit.json`: all 22 selected PNGs equal pristine original bytes;
  none has an animation companion. No copied/default/replaced production art.
- `completed-run-receipt.json` and `runtime-inputs.json` pin the completed run.
  After interruption, source, test and log hashes were verified; the successful
  native run was retained rather than repeated.
- `preceding-audit-provenance.json` links and hashes the prior read-only
  texture-dispatch audit. Its 373 count is historical CP95 evidence. BlockBase
  and BlockPillar hashes still match; CP96 changed the full ModBlocks hash, but
  all 11 selected statements remain identical. No all-source parse was rerun.

## Honest boundary

The runner compiles 18 complete production owners/helpers, with explicit exact
constructor/getMetadata slices for ItemBlockBase and a constructor slice for
ItemBlockLore. Unrelated item tooltip/name/rarity bodies are unchanged and
source-guarded, not claimed compiled. MainRegistry is an explicit bootstrap/
localization boundary fixture. No Minecraft/Forge/LWJGL class is replaced,
patched or mocked. Native setup uses actual constructors and EGL. The reused
old RenderBlocks oracle uses Unsafe only to allocate the original 1.7 renderer
without launching its client; that is not native production behavior.

This is bounded headless native model/atlas/state proof, NOT a launched game,
full-mod successful compile, whole-client resource-pack lifecycle, original
versus native held/GUI lighting-pixel parity, full gameplay port or save-world
migration claim. The parent owns common compilation/checkpoint integration.
No build configuration or source exclusion was introduced.

## Reproduce

From the module root, finite source verification:

    python3 porting/tests/native-pillar-textures/check-source.py

The completed native test can be reproduced with existing cached inputs:

    python3 porting/tests/native-pillar-textures/run.py \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath-file /workspace/shared/nagaport/ntm-port-1.12.2/porting/continuation-2026-10-19/native-probe-classpath.txt \
      --resources /workspace/shared/nagaport/ntm-port-1.12.2/src/main/resources \
      --legacy-jar /workspace/shared/nagaport-toolchain/archives/minecraft-1.7.10-client.jar

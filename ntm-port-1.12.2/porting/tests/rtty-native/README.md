# Native RTTY radio-torch family

Frozen from `641ba083c276c6ac75afa6a8f996cdd86f8b16d4` in an isolated export.
`production-files.txt` and `production.sha256` enumerate the 30 complete-scope
source/resource files. This includes unchanged family descendants for a complete
handoff. No production source exclusions, legacy Forge platform shims, gameplay
simplification, balancing edits, full Gradle invocation, downloads, or main edits.

## Integration

1. Copy exactly the files listed by `production-files.txt`, preserving their paths
   relative to `ntm-port-1.12.2`.
2. Apply `registry-fragments.patch` from the repository root. It only removes three
   obsolete `setBlockTextureName` tails in ModBlocks, whose original texture names
   now belong to the native owners, and replaces the one RenderRTTY registration.
   Original mixed line endings are preserved; `git apply --check` passes against
   the pinned export. The model handler registers via the native client event bus.
3. Copy this proof directory. Existing `porting/tests/probe-machine-core.py` and
   `ForgeEventNativeLauncher.java` are reused, not changed.
4. Check the production hashes from the module root:

       sha256sum -c porting/tests/rtty-native/production.sha256

Do not copy whole ModBlocks or ClientProxy files. The unregistered original
programmer remains unregistered and continues returning no tile. Six originally
registered torch owners have exact native world/item routes.

## Native implementation

- Nine full block owners share native BlockBase, ITileEntityProvider, all16
  metadata state, placement, original attachment predicates, directional AABBs,
  neighbor-loss drops, weak power, GUI entry points and container tile events.
  The Forge-patched native Block removes the tile on break; this is verified via
  genuine World/Chunk mutation, not an invented lifecycle adapter.
- A Boolean unlisted property captures the sign of volatile lastState from either
  the base tile or the separately inherited logic tile. Old snapshots remain
  immutable after tile mutations. Cached baked models never retain worlds/tiles.
- Base and logic tiles publish initial native update tags and change-only packet
  render invalidation. Render tags do not erase separately synchronized settings.
  Sender assigns state before native notification so immediate packet readers see
  the new value. Original channel, polling, mapping, timing, protocol and NBT
  semantics remain unchanged. See `tile-notes.md` for exact lifecycle changes.
- The seven tile classes, both actual native base classes, RTTYSystem, actual
  IControlReceiverFilter and matcher compile together with the owners/model.
  The filter's only changes are two original dirty calls mapped to markChunkDirty.
- Native RenderRTTY preserves the exact original orientation sequence for every
  metadata value, centered original OBJ vertices, UVs and continuous triangle-to-
  quad stream. Existing AtlasObjGeometry is reused without modifying that helper.
  The RTTY wrapper restores shadow=false white instead of inheriting its shading.
- World geometry is untinted, unculled, non-AO and uses original own-position
  packed light. Items use real ItemLayerModel with the original flat off icon.
  Loader ownership is limited to the six exact original names and classes.
- Breaking uses RadioTorchBreakingRenderer over the unchanged native
  NetworkBreakingRenderer(false), not the standard crack-retexture path. Exact
  six-block/six-tile predicates enable custom progress and suppress the standard
  damage route; foreign classes/names, programmer and tile subclasses are not
  claimed. Both base/logic dynamic states remain visible in the damage pass.
  Ordinary tile renders are no-ops. Damage output retains original atlas icons,
  UVs and own-position light, and drops vertex COLOR exactly as the original
  damage dispatcher did. Eight correction paths are listed in
  `breaking-correction-files.txt`.

## Portable proofs

From the module root, with a genuine cached 1.12.2 Minecraft/Forge classpath:

    python3 porting/tests/rtty-native/probe-owner-model.py --java-home /path/to/jdk8 --classpath "$NATIVE_CLASSPATH" --pristine /path/to/ntm-original-pristine --legacy-client /path/to/original-1.7.10-client.jar
    python3 porting/tests/rtty-native/tile-probe.py --java-home /path/to/jdk8 --classpath "$NATIVE_CLASSPATH" --pristine /path/to/ntm-original-pristine
    python3 porting/tests/rtty-native/audit-breaking-routing.py --java-home /path/to/jdk8 --classpath "$NATIVE_CLASSPATH" --legacy-client /path/to/original-1.7.10-client.jar

The original client SHA256 is pinned to
`a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce`.
Original owner/renderer/OBJ/texture sources and tile protocols are independently
pinned in the two original manifests. Original OBJ and texture assets are unchanged.

Current results:

- **285,026 owner/model assertions**. Complete production owners and tiles/bases
  compile together. Native World/Chunk all16 metadata and lifecycle, all6 attachment
  directions/support loss, weak-power callbacks, inherited tile events, immutable
  snapshots, six families x16 metadata x2 states, exact ordered original XYZ/UV/
  material/color, actual original 1.7.10 Vec3 bytecode rotation, native model-loader
  routes/JSON/item registrations, malformed route rejection and cache immutability.
- Both genuine Forge VertexLighterFlat and vanilla BlockModelRenderer flat fallback
  preserve white and own-position packed light on all6 valid orientations.
- **491 tile assertions**. Actual radio tiles, RTTY queue, both bases, filter,
  matcher, BufferUtil; six-side native weak-power and vanilla redstone dispatch;
  comparator priority; polling timestamp edges; condition precedence; queue
  isolation/summing/delayed visibility; NBT/buffer parity; packet/tag invalidation;
  and exact filter dirty calls. See `tile.log` and `tile-notes.md`.
- **Actual native breaking route**. The real ModelRegistry handler binds the six
  exact tile renderers. The genuine native dispatcher executes six owners x16
  metadata x2 states x2 AO modes x3 damage stages. The only captured boundary is
  GPU upload. Ordered position, original-icon UV, packed light and colorless
  output are tested; ordinary/invalid/unowned passes do not reach upload.
- **Routing bytecode evidence**. `breaking-routing-bytecode.txt` verifies the
  pinned original disableColor/blend ordering, native RenderGlobal's custom
  progress branch and ordinary damage-skip branch, and the exact genuine native
  dispatcher overload executed in the proof. The earlier standard retexture
  regression is corrected rather than accepted as a visual gap.

## Remaining acceptance boundaries

The original RenderRTTY ignores RenderBlocks.overrideBlockTexture. The corrected
breaking-only TESR now preserves that behavior and the original colorless damage
contract. Ordinary world light and white shading, and damage geometry/material/
light routing, are directly tested rather than presumed unresolved. GPU draw and
live visual acceptance remain separate from the CPU/bytecode evidence.

No live GL rasterization, whole-mod startup, production network threading/transport,
optional-mod integration, or complete GUI operation was run. Test collaborators
are bounded external NTM atmosphere, compatibility, explosion, ore lookup, GUI,
main singleton and packet dispatch components. Unrelated CableDiode/CranePartitioner
class-token branches and DiodeBakedModel are bounded collaborators when compiling
the shared breaking renderer; the full radio breaking path is actual production. No Minecraft/Forge classes are
stubbed. The native client singleton is allocated headlessly to exercise actual
model registry/resource classes without starting a graphical client.

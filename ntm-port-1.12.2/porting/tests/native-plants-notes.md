# Native aquatic plants: implementation and bounded acceptance

Production owner/model work is frozen, but **reeds and phosphor vine acceptance remains gated on nondefault vanilla-light-pipeline parity**. Do not classify this whole family as unconditionally original-parity complete.

## Owned registrations and original behaviors

- hbm:tile.plant_reeds: native no-collision plant with stick drop, water-only placement, no-drop removal on water support loss. Original depth scan stops before Y=0 and honors RENDER_REEDS. Top/middle/bottom atlas choices, 0.9-width crossed geometry, exact 1.7 backside UVs and 0.75 RGB shade retained.
- hbm:tile.laythe_coral: native 0–15 stored metadata, five creative variants and metadata-modulo-five material selection, calcium/zero-meta drop, above-liquid placement. All six attachment orientations and NORTH/SOUTH/WEST/EAST/UP/DOWN priority retained. The original Block.isBlockSolid uses material solidity, NOT Forge side-solid support: fences, glass and slabs must still count. Selection stays a full unit block and collision stays absent.
- hbm:tile.vine_phosphor: no collision, original UP-face support query above, no-drop support loss, shear/silk behavior and one own-block shear drop retained. Entity velocity halves on all three axes and fall distance resets. The original odd current-position ground and air queries remain unchanged. Base and separate glow crossed quads retain independent materials, winding and UVs. Glow is exactly packed light 240 (block 15, sky 0), not conventional full sky+block brightness.
- hbm:tile.algae_film: separate algae notes/probe cover native facing, tile/overlay compatibility, original 92-triangle OBJ and four rotations plus inventory. Metadata outside 2–5 renders the original zero rotation.

The plant model handler uses exact registry identities AND exact original owner classes, preserving unowned addon registrations and replacement classes. It validates owned ItemBlocks, registers all 16 item metadata routes and uses native flat ItemLayerModel inventory paths. Native blockstate JSON routes all listed states. Particle material follows coral variant. All original PNG/animation assets remain byte-identical.

## Native light pipeline and explicit gate

PlantGeometry stores original shade/light in native quads and uses a scoped, delegating IBlockAccess light view for VertexLighterFlat. This prevents Forge's max(world, emitted) merge from changing per-depth reed light or injecting sky light into the vine glow. The lighter's world is restored after every quad, including exception paths. No tile entity, global renderer override, package shim, compatibility fake, source exclusion or visual simplification was added.

With the normal Forge light pipeline enabled, actual native VertexLighterFlat → VertexBufferConsumer → BufferBuilder proves exact position, RGB and packed light. With ForgeModContainer.forgeLightPipelineEnabled=false, native BlockModelRenderer.renderModelFlat overwrites every quad's baked light with one owner-position sample. Coral is exact on that path, but multi-depth reeds and sky-zero vine glow cannot be represented exactly by a single ordinary baked model under that fallback. Tests explicitly prove the mismatch. No global setting is silently changed, no tile-entity fallback is introduced, and acceptance of that compatibility gap is pending parent/user decision. This is a real bounded limitation, not claimed as passing parity.

## Evidence and reproducibility

Run from the module:

    python3 porting/tests/probe-native-plants.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8
    python3 porting/tests/check-native-plants-source.py
    python3 porting/tests/algae-film-native-probe.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8

The plant probe compiles 13 complete production classes against actual Minecraft/Forge 1.12.2. External fixtures are ONLY the ClientConfig boolean wrapper and calcium ModItems registry identity. All block/model/state/resource/registry/lighter/buffer classes are actual native classes. Test World supplies deterministic neighbor states/light; it is not a production compatibility class. The client singleton is allocated without a game loop for native ModelLoaderRegistry and ItemModelMesher registration checks.

Oracle checks execute pristine coral/vine source method bodies with a recording vertex sink. Reeds execute the actual pinned Minecraft 1.7.10 client blm.a(rf,DDD,F) method bytecode, remapping only renderer/icon/vertex-recorder references in a test-only generated class. No original client classes enter production. --legacy-client can point to the same pinned legacy jar; default /tmp/nagaport-legacy-client.jar. Manifest verifies source/texture/jar digests before compilation. Bytecode audit evidence also records original Block.isBlockSolid material behavior.

Final plant proof: 151,930 assertions plus 7 replacement-registration assertions. It covers 64 attachment masks ×16 metadata, all orientations, exact ordered XYZ/UV, 30 water depths, config override and Y=1/Y=0 edges, native callbacks, real rendered RGB/packed light, 48 item metadata routes, actual native loader/mesher dispatch, folder and ZIP texture resources and reload. Original animation metadata is pinned and unchanged; a live timed animation/GL frame is not claimed. Algae proof: 13,527 assertions and 1,840 exact original vertices.

No full-project build, launched game, screenshot visual acceptance, or nondefault reed/vine fallback success is claimed. Parent integration converts the four ModBlocks construction chains to native names/materials/sounds and removes four obsolete ClientProxy ISBRH registrations; handlers are automatic client-only subscribers. Parent-owned shared files are not part of this worker's production manifest.

# Algae film native rendering slice

The complete production BlockAlgaeFilm, RenderAlgaeFilm and AlgaeFilmModelHandler compile against actual Minecraft/Forge 1.12.2, with no net.minecraft/net.minecraftforge stubs. NTM tile/fluid, localization and blink collaborators are controlled: this is not a proof of their full gameplay implementations.

The client-only automatic model subscriber registers a custom model loader and all 16 item metadata routes. Four facing blockstates use the original 2/3/4/5 metadata and exact yaw. Immutable quads use the already audited AtlasObjGeometry helper: original OBJ face order, atlas UVs, triangle repetition, integer-clamped ObjUtil shade, inventory signed normals, and own-block lighting (no doubled directional diffuse/AO). Native block display transforms provide original centered inventory placement. No GL, RenderBlocks, legacy render-ID or compatibility rendering callback remains in this owner/slice.

Probe: `python porting/tests/algae-film-native-probe.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"`

Result: 13,527 assertions; 1,840 exact vertices; 92 pristine original triangles; four world rotations, inventory, actual Forge and vanilla flat lighting, native ModelLoaderRegistry dispatch, all 16 ItemModelMesher routes, directory and jar resources, and mesh invalidation/reload. Original sources/assets and final slice files are SHA-256 pinned in adjacent manifests. Existing shared geometry/model helpers are SHA-listed in the run log.

Integration owned by parent: remove `RenderingRegistry.registerBlockHandler(new RenderAlgaeFilm())` from ClientProxy; change the ModBlocks algae definition to Material.IRON and setTranslationKey without setBlockTextureName. No manual handler event registration is required. Existing registry naming (`tile.algae_film`) is retained.

Boundaries: CPU/headless semantic proof, not live graphical acceptance. No full-project clean-build or complete algae tile/fluid gameplay claim.

Registration is restricted to the exact hbm:tile.algae_film registry identity and exact BlockAlgaeFilm class, and requires its owning ItemBlock. Tests reject addon namespace/path, subclasses, and same-name replacement classes. Invalid metadata 0, 1, and 6 through 15 normalizes to NORTH (zero yaw), matching the original renderer rather than EnumFacing modulo wrapping.

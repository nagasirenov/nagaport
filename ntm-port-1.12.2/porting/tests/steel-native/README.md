# Steel wall/corner native proof

Run with Python 3, a JDK 8, the unchanged native Minecraft/Forge 1.12.2 dependency classpath and the locally cached original Minecraft 1.7.10 client JAR:

    python run.py --module /path/to/module --java-home /path/to/jdk8 --classpath "NATIVE_CLASSPATH" --legacy-client /path/to/1.7.10-client.jar

Optionally add `--original /path/to/pristine-ntm` to verify the original checkout against the bundled immutable source archive. Without it, the runner uses the bundled byte-identical original files. The native classpath and legacy client path are explicit inputs; no download, compiled project output, absolute workspace dependency, hidden sibling task, or module working directory is required. Native dependencies must include the normal Forge/Minecraft deobfuscated JAR and libraries. The original Minecraft JAR is hash-checked, not redistributed. Its actual RenderBlocks face methods execute in an isolated loader. The original project licenses accompany the original source archive.

## Complete production boundary

The runner compiles the actual focused `BlockSteelDecoration`, `RenderSteelWall`, `RenderSteelCorner`, `SteelDecorationBakedModel`, `SteelDecorationModelHandler`, and unchanged tool/structure interfaces, native cuboid helpers, transform helper and namespace constants. There is exactly one controlled external NTM collaborator: `RecipesCommon.ComparableStack`, required by the unrelated tool-to-recipe map initializer. No Minecraft/Forge class, migrated owner, geometry producer, model handler, or structure/tool callback is stubbed or extracted. The reference oracle's call recorders are deliberately separate from production and are not substitutes for migrated classes.

## Evidence

- Complete actual World/Chunk placement and screwdriver updates, metadata states, item drops, absence of tile entity, collision pieces/order/filtering and stateful invalid-orientation bounds.
- Both owners × all 16 metadata × item/world × five preceding wall-bound histories. Exact XYZ, UV and winding compare to executed original RenderBlocks and extracted pristine RenderBlocksNT faces, including partial-box north/east UV distinctions, wall item centering and corner's two omitted item faces.
- Native Forge and vanilla flat/smooth render paths with asymmetric light/AO; real damage retexturing. No custom approximation or global shader change.
- Actual ModelRegistryEvent invocation, deferred ItemBlock identity, ModelLoaderRegistry, blockstate JSON mapping, all 32 item and 32 world routes, actual ItemModelMesher/BlockModelShapes, directory/JAR PNG lookup and cache/atlas reload.
- Unknown namespace/identity/kind/class and unknown model routes remain unclaimed. Fresh-JVM wrong-kind and replacement-owner negative tests.
- Bytecode checks reject legacy world/render API references in the migrated family.

The runner temporarily registers its actual model event handler only while posting ModelRegistryEvent, then unregisters it. This avoids headless non-transformed Forge's shared Event listener-list limitation; it does not bypass or replace the production handler. Native Minecraft holder/atlas rectangles and the normally automatic ItemBlock registry flush are explicit headless fixtures.

This is bounded CPU/native evidence, not a launched client, full-project successful build, complete mod lifecycle, or live graphical/gameplay acceptance. See `oracle-notes.md` for source/engine mapping and the stateful legacy invalid-metadata contract.

# Native scaffold scope and evidence

Complete production BlockScaffold and RenderScaffoldBlock now use native 1.12 block states, placement callbacks, collision bounds and baked geometry. ScaffoldBlockModelHandler installs a real custom model loader at ModelRegistryEvent and registers the exact tile.steel_scaffold ItemBlock only. Foreign aliases/subclasses are skipped. Sixteen blockstate routes and sixteen inventory metadata routes cover four original materials and four world orientations. The original inventory transform intentionally does not use the world orientation.

AtlasObjGeometry, AtlasObjBakedModel and BlockPositionLightQuad are existing unchanged production dependencies, not substitutes. They retain the original whole-OBJ continuous vertex stream, Z-then-Y float lookup-table rotations, original whole-model shading, uncontracted UV mapping, byte normals and own-block lighting. SOLID remains the original pass-zero rendering behavior. No assets are changed.

Run:

    python3 porting/tests/scaffold-native-probe.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"

The runner verifies frozen production/original SHA256 and unchanged model/texture bytes, compiles all listed full production Java files against actual Forge 1.12.2, and invokes the subscribed event, real model loader and native ItemModelMesher. Its oracle extracts transform branches, bounds, placement decisions and shader directly from pinned pristine original source, paired with the independently parsed original OBJ vertex stream. No Minecraft/Forge classes, NTM owner blocks or production helpers are stubbed. The native World fixture captures placement writes; it does not emulate renderer APIs. Unsafe allocates only a headless Minecraft/ModelManager shell for resource access and item-model meshing.

Terminal result: 338,907 assertions and 56,832 native vertices. Directory and jar paths cover all sixteen world and item routes, exact vertices/UV/colors/normals, all four materials, original item camera transform, actual Forge and vanilla flat rendering at every orientation, item model resolution, immutable models, changed/missing/restored resources, owner-alias/subclass exclusion, metadata/drop behavior, selection and collision bounds, creative variants and six placement faces times four colors times twelve representative yaws. Real atlas texture PNGs decode. Resource-reload mutations are controlled test inputs only, never production changes.

Parent integration is separate: remove obsolete RenderScaffoldBlock handler registration from ClientProxy and convert the steel_scaffold ModBlocks initializer to native naming/registry APIs, preserving original hardness 5, resistance 15 and creative tab. The dedicated client-only subscriber needs no extra install call. This work does not migrate the still-separate RenderSteelWall/RenderSteelCorner shared DecoBlock owner or CranePartitioner conveyor/inventory engine.

This is a bounded CPU/headless native API and parity proof, not full-mod compilation, graphical acceptance, full-game launch, or proof of unrelated block item registration/tooltips.

# Native cargo inventory, containers and display boundary

Pinned starting revision: 641ba083c276c6ac75afa6a8f996cdd86f8b16d4 (checkpoint 46).

Run `python3 porting/tests/train-cargo-native/run.py --java-home PATH --classpath NATIVE_CLASSPATH` from the module. Uses installed Java 8, the actual Forge 1.12.2/MCP stable39 dependencies and Linux Mesa/EGL for the renderer probe. No dependencies are fetched by the probe. Windows execution is untested.

Production scope is the complete EntityRailCarCargo and RenderTrainCargoTramTrailer classes, plus the complete nested inventory container callbacks in TrainCargoTram and TrainCargoTramTrailer. The remaining outer train physics, electric power, GUI and rail network classes have NOT been certified or migrated by this batch.

- 173 actual native inventory/NBT/EntityDataManager packet serialization, server tick, name and distance assertions.
- 7,758 complete renderer assertions: all occupied-slot counts, original nine cargo layouts, interpolation, yaw wrap, real native cube item framebuffers, and exception/state cleanup.
- 95 complete verbatim nested container assertions: native EMPTY contracts, slot layout, shift-click ordering, battery-only routing, full destination refusals, NBT and item-count conservation.

Fixture boundaries are explicit: the train physics superclass is a small recording Entity subclass; nested containers compile verbatim inside declared outer train fixtures; the train OBJ mesh is a recording ResourceManager boundary. Cargo crate draws use actual native item rendering. These are native harness checks, not a loaded Minecraft world, complete train integration or visual sign-off for the train mesh. Existing native machine-renderer fixture setup is reused, not its unrelated test suites.

The inventory keeps the original internal null-slot layout and raw occupied-slot counting, including the original live-zero-stack presence quirk. Native public inventory methods return EMPTY. Server synchronization occurs at the original tick/load boundaries. Original slot limits, reference aliasing on insertion, byte slot NBT format, last duplicate-slot overwrite, lack of custom inventory-name persistence, reverse shift-click order and battery destination are retained; no new energy/recipe behavior is introduced. The NBT reader necessarily uses native empty-stack validation. `getInventoryName` remains an NTM domain hook for the two existing GUIs, not an imitation of old Forge.

The renderer uses the existing ENTITY_FRAME item context, not the dropped-item count/bob path. Original position lists are pinned in original-layouts.json, generated from the pristine scoped original source. All production source inputs are printed with SHA256 in proof.log.

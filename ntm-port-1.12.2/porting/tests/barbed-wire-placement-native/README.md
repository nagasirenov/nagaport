# Barbed-wire placement-only batch

Exact baseline 621a2656fe88635d67ea54e5836ec35aeaf57d16, after the stone-brick batch. 134 direct ModBlocks.barbed_wire calls: 56 DesertAtom002, 78 DesertAtom003, all metadata0/flags3. Only the two generator sources change. The existing DungeonToolbox helper remains byte-identical and reads the actual mutable static field at the original argument position. No wire owner, factory, registry identity, model, texture or state asset changes are included.

IMPORTANT inherited resource blocker: the intentional registry identity is hbm:tile.barbed_wire, consistent with original getUnlocalizedName registration. The existing owner derives the icon request hbm:blocks/tile.barbed_wire_model, but the actual original texture is hbm:blocks/barbed_wire_model. Existing state assets are named barbed_wire*.json, not tile.barbed_wire*.json. Both mismatches apply to the six wire identities. The native test reproduces the actual icon request through the real owner callback; tracked-resource checks confirm the missing requested names. It does NOT certify atlas/OBJ/model runtime, and registry identities must not be stripped or renamed as a shortcut. Parent authorized this explicit placement-only boundary; minimal route correction is a separate assessment. No dedicated prior barbed-wire runtime proof was found in tracked evidence, so none is claimed reused.

Actual complete BarbedWire and its interfaces compile against native APIs. The exact createBarbedWire factory is extracted unchanged, retaining material, translation key, registry name, hardness5, resistance10 and native creative tab. Bounded NTM collaborators supply the six identity slots and otherwise unexercised acid/death/potion dependencies; acid suit calls fail closed. The test scope is the ordinary barbed_wire identity, not the complete other five variants. No Minecraft/Forge classes are substituted.

Actual native default orientation0 and all six existing orientation states round-trip through global state IDs, real World/Chunk placement, compressed Anvil save/load and removal. Native Block.getDrops and World.destroyBlock/EntityItem paths preserve item identity/count and drop damage0. Actual placed-by yaw quadrants retain orientations5/3/4/2; ordinary-wire collision retains web, motion scaling, cactus damage2, non-full/nonopaque and null collision box. A native Entity subclass records damage/web callbacks; no complete entity AI/suit gameplay is claimed. Three runtime mutations reject wrong default orientation, drop damage and collision shape. Unsupported metadata outside0..5 is not migrated or normalized by this batch.

All 4,050 tracked Java sources establish the sole exact mutable owner assignment, exact factory and no BarbedWire descendants. Three closure mutations and eleven Java8 attribution controls pass. Source inverses cover every selected expression and preserve all remaining generator code. Sequence tests execute the actual original World bounds prefix and compare ordered native/original events at nine origins, null receiver, mutable field substitution, effectful arguments, exception timing and RNG continuation. The extra stone identity is only a non-caching control, not an expansion of production owner scope. Complete selected sequences compile unsplit to Code lengths1615 and2316 bytes. These are not full-generator compilation or full-game claims. Lighting calculations are bypassed in the real-World test fixture and all existing visual gates stay open.

Reproduce:

    python3 porting/tests/barbed-wire-placement-native/source-proof.py
    python3 porting/tests/barbed-wire-placement-native/check-closure.py
    python3 porting/tests/barbed-wire-placement-native/ownership.py --java-home /path/to/java8 --classpath-file /path/to/classpath.txt
    python3 porting/tests/barbed-wire-placement-native/migrate.py --java-home /path/to/java8 --classpath-file /path/to/classpath.txt
    python3 porting/tests/barbed-wire-placement-native/prove.py --java-home /path/to/java8 --classpath-file /path/to/classpath.txt --legacy-client /path/to/original-client.jar --mutants

Use merge.py --module <module> first for integration dry-run, then --apply --receipt <json>. Exact unchanged placement lines and inverse receipts preserve independent main edits. Do not use migrate.py --apply against a later integrated tree.

The optional36 brick_red calls were excluded: their BlockRedBrick owner still has obsolete drop/placement callbacks and icon fields. They require a separate genuine owner migration, not inherited vanilla behavior.

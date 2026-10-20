# Titanium dungeon palette, after native BlockOre callbacks

Baseline 0542355f96bcc0100f9b6c5ab43be87d157e90b4 (the separate ore-callback-native production/evidence must precede this batch).

278 placements only: DesertAtom002.Block15 (32), DesertAtom003.Block15 (246). Both are actual package-private mutable Block fields initialized from ModBlocks.deco_titanium. Whole-source closure covers all 4,050 tracked Java sources, sole exact constructor assignment, external writes, subclass/anonymous exposure, shadowing and declaration modifiers. Java8 attribution establishes actual World receiver, declaring field owner, int arguments, standalone statement and literal metadata0/flags3. Fourteen ownership controls and nine closure mutants pass. This does not pretend fields are immutable: every replacement evaluates the actual field at its original argument position.

Production only changes the two generators. Existing DungeonToolbox helper and placeRandomConcrete are byte-identical. No BlockOre or resource changes occur in this batch. Exact source inverses and per-site controls preserve every other generator fragment. merge.py refuses changed placement lines and can integrate into the coordinator's independently edited snapshots with an exact inverse receipt.

The proof compiles complete real BlockOre/Cluster and DungeonToolbox, reusing the preceding ore callback probe. Actual titanium constructor/default16 states/noFortune/material/hardness/resistance, native global state IDs, actual World/Chunk placement, absence of tile lifecycle, compressed native Anvil persistence and removal are checked. Original-expression and native ordered streams cover all 278 selected expressions, nine origins including overflow/bounds, null receiver, and 22 mutable-field identity substitutions from the actual ore owner test. The broader substitutions test non-caching and dispatch, not permission to migrate those other ore families. Effectful receiver/arguments, RNG continuation, exception order and bounds-before-conversion use the previous evaluation proof with titanium identity. The actual old World placement bounds prefix is executed. Callback drop and placement proof reruns in the same native process.

The Library fixture used only to compile complete DungeonToolbox has the six actual direction fields and an unexercised concrete selector that throws. No full Library or ModBlocks initialization is claimed. Owner proof details and fixture boundaries are in ore-callback-native/README.md. Existing model/resource and multipass/CT visual gates are inherited unchanged, without new visual acceptance.

Unsplit selected-expression methods compile to Code lengths 982, 4825, 2887 bytes. These are the full selected sequences, not subdivided artificial chunks; they are not claims that the full still-unported generators compile or execute. The earlier structure-palette evidence contains the actual direct-expansion Java method-size failure justifying the shared fixed-meta0/flags3 helper. No generic legacy placement emulator was introduced.

Reproduce:

    python3 porting/tests/titanium-palette-native/source-proof.py
    python3 porting/tests/titanium-palette-native/check-owner-closure.py
    python3 porting/tests/titanium-palette-native/ownership.py --java-home /path/to/java8 --classpath-file /path/to/classpath.txt
    python3 porting/tests/titanium-palette-native/migrate.py --java-home /path/to/java8 --classpath-file /path/to/classpath.txt
    python3 porting/tests/titanium-palette-native/prove.py --java-home /path/to/java8 --classpath-file /path/to/classpath.txt --legacy-client /path/to/original-client.jar

Do not run migrate.py --apply on integrated later work. For composed integration, use merge.py --module <module> --apply --receipt <json>; dry-run first.

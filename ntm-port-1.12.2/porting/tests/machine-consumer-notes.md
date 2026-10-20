# Consumer EMPTY/native transfer closure

17 production files covered by `machine-consumer-source-manifest.json`. The fail-closed checker checks complete exact UTF-8 byte source hashes (including CRLF), reverses every recorded hunk, and verifies the pre-edit snapshot hash. The manifest preserves earlier concurrent callback edits present before this task. It is not a proof that the original legacy implementation was correct.

The native test compiles verbatim extracted current production methods against real Minecraft/Forge: InventoryUtil's IInventory insertion overloads, Slot merge, unchanged data matcher; PneumaticNetwork's transfer, accessible slots, receiver comparator and receiver registration; SatDock setter/removal. Supporting ItemStackUtil copy/compatibility, BobMathUtil minimum/order helpers, NodeNet bad-link check, Tube identifier and ILoadedTile declaration are also extracted unchanged. Complete production TileEntityInventoryBase, SlotPattern, ForgeDirection, Tuple and Spaghetti are compiled. Tube filter/registry context and the autocrafter hard-cap class identity are explicitly controlled domain fixtures, not fake Minecraft types. This is NOT full machine/network-class or gameplay acceptance.

43 checks passed using Java 8 and the local Forge 1.12.2 classpath, without Gradle: null and EMPTY vacancies, insertion full/partial remainders, alias ownership, Slot merge and denial, network full/partial transfer, source debit, destination receipt, native outbound EMPTY clear, denied insertion/extraction, SatDock clamp/removal identity, SlotPattern copying/count/clears, and unchanged EMPTY sentinel. A deliberate unexpected-source mutation was rejected by the source checker.

Run portably:

    python porting/tests/check-machine-consumer-source.py
    python porting/tests/machine-consumer-native-test.py --java-home /path/to/jdk8 --classpath "<real Forge/Minecraft classpath>"

Optional isolated task definitions are in `machine-consumer-probe.init.gradle` (not executed here). They do not alter compileJava or sourceSets.

Native count API edits are bounded to coherent transfer methods; domain insertion overloads, nullable arrays/remainders, magazine bag reads, recipe logic and existing count anomalies remain unchanged. Pneumatic native position/clamp access and SlotPattern's native isEnabled callback were required to compile the tested code. Other legacy APIs remain elsewhere in these source files. Autocrafter's nullable template-array values are adapted only when forwarded to native InventoryCrafting. SatDock's getter remains a legacy nullable getter; only its removal callback and setter ingress are changed, as requested.

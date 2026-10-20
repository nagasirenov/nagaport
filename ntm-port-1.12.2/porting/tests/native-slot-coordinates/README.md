# CP91 native Slot coordinate fields

Baseline CP90 0be133c9fb9f07053e26f081371487e641c95214. 54 compiler-attributed tokens in thirteen complete source files migrate xDisplayPosition/yDisplayPosition to genuine public mutable Slot.xPos/yPos. Two unqualified SlotLayer writes use this.yPos to avoid local capture; every receiver, argument, arithmetic expression and control-flow byte outside those tokens remains unchanged. No GUI rendering, container inventory logic or validation policy is rewritten.

The scanner accepts only proven Slot descendants with no legacy or destination field shadows, rejects static/type receivers, unknown/generic types, nested implicit capture and local/parameter shadowing. It handles Java UTF-16 offsets, including source with non-BMP characters. Sixteen typed positive/negative controls and independent whole-tree re-attribution pass. The exact manifest plus baseline.zip reconstruct thirteen full files and reject all54 omitted-token mutants.

23,411 actual old/native runtime checks compare real Slot constructor/mutable fields and the complete original mod SlotLayer bytecode against complete migrated SlotLayer. The original class is relinked only at constant-pool symbolic names to the original obfuscated vanilla engine; all post-pool class data and method instructions are byte-identical. Original Forge1614/FML patches do not alter these coordinate fields. Signed original client entries are removed only from a temporary class-identical container to permit this test linkage. Original proprietary class files are not redistributed.

The test covers extreme signed coordinates/layer values, all layer selections, unchanged visible/validity decisions, 9999 hidden y sentinel, restoration of original y after external y mutation, preservation of external x mutation, native field evaluation/null behavior and three rejected executable SlotLayer mutants. Null inventory is intentional: none of the tested coordinate/visibility methods accesses inventory. No claim of full GUI linkage, inventory content behavior, render pixels or launched client/server is made. Other GUI APIs remain unfinished.

Reproduce from ntm-port-1.12.2 with existing cached Java8/native/original artifacts:

    python3 porting/tests/native-slot-coordinates/check_source.py
    python3 porting/tests/native-slot-coordinates/attribute.py
    python3 porting/tests/native-slot-coordinates/run.py

The full native project build is performed separately by the coordinator on the integrated frozen checkpoint. Attribution diagnostics from unfinished sources are not a successful compilation.

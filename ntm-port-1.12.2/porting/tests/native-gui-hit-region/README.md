# Native machine GUI hit-region method

Base CP91 f5ce86cd607379cc9810896cf91d1fee86ce6331. Five remaining method-name-only calls in four GUI owners become actual protected GuiContainer.isPointInRegion. All six original int arguments, receivers, short-circuit stack checks and surrounding bytes remain unchanged. These are the five API errors exposed by CP91 slot-coordinate resolution, not new input behavior.

Actual whole-tree compiler attribution resolves all five calls to the exact native GuiContainer owner, six int arguments and boolean result. One already-native call in GuiInfoContainer is recorded but untouched. Attribution still reports unfinished unrelated errors, not project compile success.

42 actual original/native hit-region instructions are identical after only official field-name linkage, constant-pool index and javap whitespace normalization. All arithmetic/branches/offsets remain exact. 165,157 real original/native method calls cover origins, one-pixel inclusive/exclusive boundaries, negative dimensions and signed overflow. Test-only Unsafe allocation bypasses unrelated GUI initialization; protected hit-testing itself executes genuine engine code. No fake GUI method or rewritten formula is used. This is not complete GUI-owner compilation, rendering, input-loop or game acceptance.

Four complete-file inverses and eight unrelated/omitted-edit controls pass. The prior Slot-coordinate source guard composes only this hash-pinned exact inverse and keeps all original protected hashes; no arbitrary repinning or skipped source checks.

From the module:

    python3 porting/tests/native-gui-hit-region/check_source.py
    python3 porting/tests/native-gui-hit-region/attribute.py
    python3 porting/tests/native-gui-hit-region/instructions.py
    python3 porting/tests/native-gui-hit-region/run.py
    python3 porting/tests/native-slot-coordinates/check_source.py

Only existing authentic cached original/native toolchain inputs are used. No download, dependency regeneration, whole-project build or push is performed by these bounded checks.

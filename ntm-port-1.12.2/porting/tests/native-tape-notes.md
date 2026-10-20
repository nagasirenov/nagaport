# Native tape recorder and steel poles

CP37 family, baseline CP36 e146f1df78ba6ce7ebf3198217a67d132e1e7dff. Complete DecoTapeRecorder, DecoSteelPoles, RenderTapeBlock and native loader replace the obsolete block callback. AtlasObjGeometry and all supporting model/light/parser helpers remain unchanged and hash-pinned. Two OBJ and two PNG assets are byte-identical to the included original tree.

The original 16 metadata values round-trip. Placement retains yaw rounding, direction sequence 2/5/3/4 and notify flag2. Both preserve full collision/selection bounds despite nonopaque/nonfull rendering, generic zero-damage drops and the tape's deliberately null tile factory. Original structure metadata transformation remains its unchanged actual interface implementation. No audio/playback functionality was invented for the decorative recorder.

Geometry proof independently computes original whole-OBJ continuous GL_QUADS stream and exact rotations/positions for every metadata and world/item context. The steel pole world rotation offset is deliberately absent from inventory. Shared original UV/shading/normal/light machinery is unchanged and pinned, with prior native atlas/scaffold coverage, not falsely counted as a newly repeated full renderer proof. Actual native loader, atlas material resource lookup and bake are exercised from folder and ZIP. Native registry/model event invocation adds only exact-owned item routes, verifies default state mapper metadata variants, and preserves foreign aliases and same-name replacement class models. No Minecraft/Forge or unrelated NTM sources are substituted in the twelve complete compiled production classes. The small recording World isolates placement rather than claiming real world startup.

Run from module:
python3 porting/tests/probe-native-tape.py --java-home /path/to/jdk8 --classpath "native classpath"
python3 porting/tests/check-native-tape-source.py

Source scope pins originals and unchanged assets; parent complete reversible boundary additionally covers ClientProxy and ModBlocks integration. The native model event is invoked against actual registries and ModelLoader, not a preconstructed mock map. No full client, world saves or Windows runtime claim. Initial harness-only compiler qualification/native-library setup issues were corrected before final evidence; production geometry did not change during those fixes.

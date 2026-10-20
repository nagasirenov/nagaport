# Native OBJ checkpoint 6

Five production classes: four mod-owned mesh/parser/native BufferBuilder classes and ModelWormHead. No old Forge interface emulation. Only the worm head consumer has moved; existing HFR/HMF/VBO consumers remain unchanged.

## Evidence

One fresh clean production compile: 2102 errors, exit 1 (previous 2105). All sources remain included. Gradle testPortObjMesh PASS: 102270 assertions using actual BufferBuilder bytes; complete four new production classes plus ModelWormHead and RefStrings compiled against actual mapped target Minecraft/Forge.

Six actual resources: bot_prime_head 680 faces, stirling 932, firebox 348, core_receiver 372, fluidtank 1392, tom_flame 60. Probe covers packed normals, winding, raw wrapping UV/V flip, UV inset, missing MTL ignored, source-order/case-insensitive duplicate selection, HMF timing and explicit reload failure. A precision regression caught float-rounded sqrt normalization; corrected to original semantics. Independent read-only review found no blocking defect for the single-part fully attributed head.

Official legacy reference: MinecraftForge/MinecraftForge commit 9274e4fe435cb415099a8216c1b42235f185443e, Face.java SHA256 afd6fc808708765039ff83b0873a81cbc2f95d7411ef87f7db818f2ed5c88ce6, WavefrontObject.java SHA256 9d55d712b66254eda54b69b077a2bf041d9da6e0d5847091f98776325afb95dd. Plain Forge head uses flat first-three-vertex normals and 0.0005F centerward UV inset, not HFR smooth shading. Eager sampling, no automatic reload subscription and original matrix transforms retained.

## Limits and continuation

No ModelWormHead initialization or drawing in a client, no GL/VBO upload/delete test, no dedicated server or game-save verification. The complete mod still fails compilation. drawParts batches each part independently, not a universal replacement for legacy renderAll cross-group current attributes. Only the single fully attributed head is migrated. HFR missing-UV smooth-normal carry, noSmooth timing, VBO per-vertex normals and real reload/GPU lifecycle need coordinated preservation before broader migration. Twenty-two assets contain v//vn corners; do not silently treat them as universally supported consumer conversions. The parser preserves existing restricted syntax rather than broadening accepted files.

Next: inspect coherent additional consumers; preserve texture/tint/matrix/animation ownership, explicitly plan attribute-state and VBO lifecycle. No placeholders or source exclusions.

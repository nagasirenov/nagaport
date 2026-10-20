# Checkpoint 90: native armor registration, slot boundaries and owned textured batches

Baseline CP89 42dea8e6e3241d0c4ce4ba9c5459fab2bdfdc99b. Production is frozen: 52 existing Java files plus the new NTM-domain ArmorSlotOrder helper. Fresh full offline compilation **FAILS with 10,831 diagnostics**, down **320** from 11,151. All **4,070 generated Java inputs** byte-match frozen production. No playable JAR, launched game or whole-mod armor/combat acceptance is claimed.

## Armor materials and repairs

31 actual EnumHelper material registrations use the native API, preserving names, durability factors, enchantability, creation order and distinct same-named enum instances. Reduction arrays are translated from original head-first to native feet-first order. 30 repair assignments retain the exact Item reference, assignment phase and reassignment capability through Forge's public repairMaterial field, with wildcard metadata to preserve original item-only matching for ordinary positive stacks. The unused Aus3 registration remains; Euphemium still has no repair assignment.

The new native texture-name parameter is leather because every original custom-material constructor used renderIndex 0 and that is the proven vanilla fallback when the NTM worn-texture hook returns null. Custom enum identity is not vanilla LEATHER, so this does not enable leather overlays. Generic native equip feedback and zero toughness supply required new platform parameters, not a claim of old/new combat or audio equivalence. Invalid or cached-empty repair eligibility retains the documented native engine boundary; the furnace-only approval is not used as global permission. See armor-material-registration/repair-engine-boundaries.tsv. 3,056 actual native/original assertions and twelve mutation controls pass; all 138 original material constructor consumers and 28 ancestor/concrete owners were audited.

## Native armor slots and callbacks

Eight constructor roots map the original NTM head0/chest1/legs2/feet3 domain only at the native super-call. All 152 construction arguments and 24 intermediate forwards remain unchanged. 39 genuine native item hooks preserve original model arrays, textures, cached models, validity and null fallback. 51 native armorType consumers map back to original NTM ordinals; the remaining Dummy call uses its actual native equipment slot and concrete EntityLiving virtual dispatch. Integer ISpecialArmor slots remain feet-first in both versions and are deliberately not converted.

Armor-table physical indices use mainInventory.size()+3-i rather than the old total-size expression, which would select native offhand first. Real InventoryPlayer and Slot tests cover all four rows and offhand exclusion. The helper is a bounded domain mapping, not old Forge emulation. In-repository constructors and callbacks are exhaustively attributed; out-of-domain external calls are not certified.

35,432 bounded production assertions, 393 genuine original/native engine assertions, nine executable mutation controls and 126 full-file drift controls pass. Complete small owners and actual model classes are distinguished from constructor/callback projections and explicit dependency fixtures in native-armor-slots/README.md. Real native hook dispatch is tested, not full worn-model GL or gameplay. Three no-argument armor attribute overrides and nine old attribute super-calls remain a separate root; this checkpoint does not claim those protections/speed toggles function end-to-end.

## Remaining owned textured batches

Seven owners contain ten complete nonescaping local scopes, 29 quad sites and 116 XYZUV expressions migrated to actual native BufferBuilder POSITION_TEX. Geometry, UV, winding, material/GL/matrix state, texture/shader calls, animation, RNG, timing and all surrounding control flow remain. Five setters preceding old startDrawing were actually reset by that old call; their argument evaluation is retained in place, not converted into guessed vertex attributes. Actual original bytecode confirms the reset behavior.

522 paired streams, 2,088 vertices, 1,044 actual old/native Mesa-driver draws and 49 source controls pass. This certifies the extracted owned batches, not complete owner linkage, full framebuffer appearance or game rendering. FurnaceSteel's UV1-only stream and borrowed/mixed batches remain separate, not silently filled with dummy UV/normal values.

## Known press legacy-upgrade regression, correction pending separately

The read-only metadata assessment is included without production changes. 104 real original/native assertions reproduce an existing CP43 legacy-press-upgrade lifecycle defect: metadata-only promotion originally retains its tile, but native setBlockState can invalidate and recreate a default mod tile. The complete production BlockDummyable callback chain was executed with explicit payload-bearing tile fixtures for ordinary rotations. Actual complete Press/EPress owners remain uncompiled, so this is a demonstrated platform/callback defect supported by their default refresh implementation, not an observed user-save failure or complete machine test.

CP43 makeExtra/removeExtra and MultiblockHandlerXR.fillSpace originally used full block writes, not metadata-only writes; this finding does not refute those full-write mappings. RBMK metadata-only conversion is withheld because it has the same retention hazard. A scoped press correction is being developed independently for the next checkpoint; no broad shouldRefresh override, NBT snapshot copying or private cache mutation is included here. Read rbmk-metadata-assessment/README.md and the qualification added to dummyable-native/README.md for CP43.

## Reproduction and recovery

From ntm-port-1.12.2:

    python3 porting/tests/check-thermal-family-source.py
    python3 porting/tests/armor-material-registration/audit.py
    python3 porting/tests/native-armor-slots/check_source.py
    python3 porting/tests/remaining-textured-batches/check_source.py
    /workspace/shared/nagaport-toolchain/run-gradle.sh --offline -I /workspace/shared/nagaport-toolchain/restore-official-repositories.init.gradle -I porting/compile-diagnostics-expanded.init.gradle clean compileJava --console=plain
    python3 porting/checkpoint90/verify-build.py

Family READMEs include the actual engine/projection runtime commands and exact limitations. The aggregate guard reverses all 52 complete modified files, pins the new helper and rejects 159 source drift mutants while composing earlier historical guards. Family composition only reverses an exact pinned sibling transformation; no source hashes are arbitrarily repinned or skipped.

The first offline build poll was denied using the superseded pending-Mojang-approval premise. The partial log was preserved; process absence and missing exit receipt were checked. Applicable later user grants were reconciled, the exact denied poll returned unknown process, and only the same offline build command was restarted. No alternate network route, dependency regeneration or source exclusion occurred.

Published main remains CP75 eefb4c1f6f344e4fcb15efb7e6cb6a370419a570. CP89 is durably preserved in Library version17. This cumulative archive targets the exact published baseline and replaces earlier recovery archives, not a patch chain. Stop on dirty conflicts or CRLF refusal; never reset, force or normalize user files. Windows execution is untested. Potion config/save policy and other explicit historical gaps remain separate. Full build, startup, machine/energy/radiation integration, networking and saves remain unfinished. No new push is authorized.

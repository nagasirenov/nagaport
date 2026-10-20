# CP89 native Forge event payload extension

Baseline: `a65d3acbf05634c3cedbce5eff43ad0bd15cefdb` (CP88).

Production changes are exactly **165 attributed read-token substitutions and 10 actual native setter expressions in 21 existing Java files**. Forty-five owner/field mappings are used; `rules.tsv` verifies 46 mappings including unused PopulateChunkEvent.rand. No new production class, dependency, compatibility façade, reflection, registration, cancellation, condition, callback, or surrounding event logic was added or removed. Two-phase source inverses reconstruct the actual CP88 git blobs byte-for-byte, preserving CRLF and every other byte. All 175 per-edit reversal mutants are rejected. The same guard passes unchanged on the coordinator's frozen CP89 source.

The typed scanner checks exact native declaring owner, field type/privacy/mutability and genuine getter return type, native setter parameter/return types, instance receivers, inherited shadows, and reachable source getter/setter overrides. Reads exclude writes, compound lvalues and unary updates. Setter rewrites only accept standalone assignments to a stable local/parameter receiver, assignable RHS types, and the observed float multiply assignment. Effectful receivers, nested assignments, receiver reassignment in the RHS, unsupported compounds, unresolved values, and overrides are rejected. The float multiply retains Java's narrowing cast, old-value-before-RHS order and null/exception timing. No getter substitutes for a write.

## Bounded evidence

Run from anywhere:

    python3 porting/tests/native-event-extension-cp89/run.py --verify-attribution

Defaults use the restored Java 8 toolchain, `/workspace/shared/nagaport-toolchain/native-classpath.txt`, and `/tmp/nagaport-forge1614-userdev.jar`. `--module`, `--java-home`, `--classpath-file`, and `--forge1614` can override paths. Source-only replay:

    python3 porting/tests/native-event-extension-cp89/check_source.py --module MODULE

No Gradle build, installation, download or dependency regeneration is performed.

- 722 checks execute genuine native event payload getters/setters, constructors, selected migrated reads and writes, reference/null identity, mutable list aliases, primitive values, float narrowing, and receiver/RHS/null/exception order. Exact ASM getter/setter bytecode is verified. All 67 resolved dependency JARs are inspected for matching descendant overrides.
- ItemTooltipEvent's native getEntityPlayer override is explicitly allowed only after proving its bytecode is a direct invokespecial call to PlayerEvent.getEntityPlayer. No arbitrary override is accepted. External runtime mods outside the inspected closure could override virtual accessors and are not certified.
- All 165 old read expressions fail against genuine native API with private-access errors; all 165 exact migrated receiver/getter expressions compile and execute. Twenty-two isolated javac fixtures reject unrelated/unknown receivers, shadows, overrides, invalid write shapes and invalid RHS types.
- All ten setter expressions are extracted verbatim from the production manifest and compiled/executed against actual Forge events. The gravity fixture uses the verified actual production constant 1.6F and float inputs, not an invented game formula.
- The **entire current production BucketHandler.java** compiles unchanged against genuine native classes. Its real @SubscribeEvent method is registered on the actual Forge EventBus under Forge's genuine EventSubscriptionTransformer. Eighteen assertions cover source-bucket success, exact hit position, read/removal order, real resulting stack, preserved inputs, unknown-block prior result/payload, flowing-water rejection, already-canceled suppression, and unregister control. An explicitly named test World supplies in-memory state, not a fake Minecraft/Forge API. FillBucketEvent's actual default filled payload is null, which the test preserves rather than assuming ItemStack.EMPTY.
- Original public fields are checked in authentic Forge1614 source and embedded binaries. LivingDropsEvent's old ArrayList field widens to native List: the sole changed caller iterates it, and the test verifies the live list alias. BiomeGenBase/Biome and MovingObjectPosition/RayTraceResult are real versioned native types, not fabricated aliases.
- The isolated javac attribution diagnostic count is **11,430 -> 11,265 after reads -> 11,255 after setters**, exactly 175 fewer. This is analysis of an unfinished module, not compilation success. Final attribution validates all 165 changed getter tokens at their exact transformed UTF-16 positions; nine other getters comprise eight pre-existing sites and the newly required compound-read getter.

## Explicit exclusions

AnvilRepairEvent is deliberately untouched. Forge1614 ForgeHooks.onAnvilRepair constructs `new AnvilRepairEvent(player, left, right, output)` despite the old constructor's `(player, output, left, right)` parameter order. Thus the production handler explicitly compensates for old left=ingredient, right=result, output=input. Native deprecated getLeft/getRight/getOutput have their own misleading mappings. Mechanical field-to-getter renaming would change gameplay, and native empty-stack policy requires a separately proven hook/handler migration.

No conversion of block/position contracts is claimed: BlockEvent's legacy block/x/y/z, village original/replacement state, DecorateBiomeEvent block-origin coordinates, Populate chunk-access methods, biome topBlock IBlockState writes, FillBucket target subfields, UseHoe coordinates, ItemTooltip advanced flags, command execution signatures, item pickup contracts and ArrowLoose construction/hand/ammo behavior remain separate work. Existing native event declarations, control flow and ItemStack null comparisons are unchanged. This sweep does not repair those surrounding incompatibilities, render fog in a GL context, execute world generation or anvil repair, or certify full client/server/gameplay parity.

Runtime proof uses test-only Unsafe/reflection to prepare real native payload objects without launching a full server. Constructor tests use actual native event constructors. Selected-read tests seed actual native event instances, and the bucket test supplies explicit world state. These are bounded native payload/callback proofs, not a substitute for whole-module compilation and live game acceptance.

# CP87 candidate audit: constructor-owned block sound setters

Base: CP86 `fc2cd2dba34caba0dba74ed288f95107ab4dc979`.

## Result

**The authorized direct-migration subset is empty. No production edit is proposed.**

- Parse every tracked production Java unit, ignoring comments/strings by using the JDK compiler AST. All 113 surviving `setStepSound` invocations belong to `ModBlocks`; none is an own-constructor `this`/implicit-receiver call.
- Match every AST site by exact file and line to the sealed CP86 compiler diagnostics. Those existing diagnostics attribute 87 arguments as `SoundType` and 26 as `ModSoundType`. Receiver locations are native `Block` for 107 calls and the six PA subclasses for the other six.
- The six direct PA receiver classes extend `BlockDummyable`, which extends native `BlockContainer`; neither these owners nor `BlockDummyable` overrides the setter. Calling their protected native setter from unrelated `ModBlocks` is not legal either.
- The current native 1.12.2 `Block.setSoundType(SoundType)` is protected. Its bytecode stores the exact argument reference and returns `this`. Untransformed vanilla 1.7.10 `aji.a(ajm)` is protected with the same five-instruction behavior, but that is not the complete original Forge visibility: Forge 1614 explicitly makes `setStepSound` public through its own access transformer. This explains why the original external registry calls were legal. The current target artifact does not expose the native setter publicly, so an external textual rename alone would produce an access failure.
- `BlockCrop` and `BlockGraphiteDrilledBase` already use legal constructor-owned native calls. The earlier proof is `porting/tests/native-block-sound-setter/README.md`; this audit does not claim that old fixture as a new complete-owner test.

## Sound semantics retained

No `ModSoundTypes` or `ModSoundType` definitions change. Existing `legacyMetal` retains stone place/break/step events, volume 1.0, pitch 1.5, rather than changing to modern `SoundType.METAL`. Existing grate/pipe/mork custom event selection and random pitch envelopes remain untouched. Their native constructor maps legacy hit/fall to step events. The exhaustive byte comparison in this audit also preserves all setter arguments, evaluation order, builder chains and surrounding production code.

Registry sound migration requires a separately designed NTM-owned constructor or legally accessible owner-builder initialization path preserving receiver, argument and chain semantics. Existing source-owned `withSoundType` builders in `porting/tests/native-hazard-sound` and `porting/tests/plain-cube-sound` are valid precedents; such a future design is not inherently forbidden. It is excluded from this direct-rename batch. No production wrapper, reflection, new access transformer, global mutation, event substitution, or dependency change is introduced.

## Original Forge access evidence

The authentic cached `forge-1.7.10-10.13.4.1614-1.7.10-userdev.jar`, SHA-256 `6ed364c0defdb481a41ef2d99c02d382f16fc42a6ea0ea1c2b849848d8466e91`, contains this exact line at `src/main/resources/forge_at.cfg:35`:

`public net.minecraft.block.Block func_149672_a(Lnet.minecraft.block.Block$SoundType;)Lnet.minecraft.block.Block; #setStepSound`

`forge-original-access.json` records the archive and complete resource hashes, member path, line number and exact text. The script reads this existing archive without transformation or cache regeneration. This is authoritative original Forge access configuration plus vanilla bytecode evidence, not a newly launched original runtime.

## Reproduce

Run `python3 porting/tests/owned-block-sound-audit-cp87/run.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt --legacy-jar /tmp/nagaport-legacy-client.jar --forge-userdev-jar /tmp/nagaport-forge1614-userdev.jar` from the project or use the script's absolute path.

`inventory.tsv` is the fresh parse-only AST inventory. `audit.json` adds the sealed diagnostic type evidence and its SHA-256. `setter-bytecode.txt` is freshly inspected native/untransformed-original bytecode. `proof.log` records the run. The script byte-compares every tracked production source to the CP86 blob, an exact empty-patch inverse.

Scope: this is a negative candidate-selection audit, not a new full build, new compiler attribution pass, fixture-projection runtime result, complete-owner runtime test, or claimed diagnostic reduction. No full build or network access was used.

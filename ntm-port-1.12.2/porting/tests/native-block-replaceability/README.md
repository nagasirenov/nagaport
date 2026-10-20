# Native contextual replaceability, bounded CP90 continuation

Baseline: `0be133c9fb9f07053e26f081371487e641c95214` (CP90). Production is exactly 36 parameter-list edits in 28 files: 30 call sites and six NTM override declarations. No helper/shim, formula change, receiver rewrite, method-body rewrite, source exclusion, download, dependency regeneration, or whole-project build occurs in this task.

## Exact finite rule

- A selected call `receiver.isReplaceable(world, x, y, z)` becomes `receiver.isReplaceable(world, new net.minecraft.util.math.BlockPos(x, y, z))`. Receiver and all four original argument expressions remain in exactly the same evaluation order, once each. Existing repeated receiver-coordinate reads are not collapsed. Native virtual dispatch remains on the original receiver. BlockPos stores the original ints, without range clipping or packed-coordinate truncation.
- Six genuine NTM owners migrate `(IBlockAccess world, int x, int y, int z)` to `(IBlockAccess world, BlockPos pos)`. Their bodies remain byte-for-byte unchanged. CoriumBlock, CoriumFinite and BlockVacuum return false; MeltedFlesh, BlockGasBase and BlockFallout return true.
- ASM scanning every class of the original NTM JAR confirms all six old descriptors are `(IBlockAccess;III)Z`, not `(World;III)Z`, and their complete instructions are exactly ICONST_0/1, IRETURN. Full current-source parsing finds seven replaceability declarations: those six and the already-native BlockLayering. No omitted NTM override/overload remains.
- The exact same 16 owner/descendant types inherit the six methods in the original artifact and current source. The ten descendants are gas types inheriting BlockGasBase. No descendant defines a competing override. `owner-descendants.json` pins the closure. BlockLayering has separate existing native semantics and is not edited here.

## Same-target caller audit and honest exclusion

`manifest.json` records each original receiver, four arguments, source line, origin, and intervening source for all selected callers. Nineteen read the target immediately with WorldBlockState.read(...).getBlock(); eleven use a Block local from the exact same world and coordinates. No selected call tests a prospective block instead of the current target. Intervening checks are identity/scalar tests, immutable position coordinate getters, material/bounds reads, a metadata read, or BlockFire's flammability lookup. There is no intervening NTM world mutation. BlockVacuum.isAirBlock only tests identity, material and bounds. Coordinates and world expressions have no intervening mutation. Repeated getters/expressions remain repeated, not optimized away.

The native base method is NOT identical to Forge 1.7.10: it reads world.getBlockState(pos).getMaterial().isReplaceable(), while the old Forge hook read the receiver's material. Native base execution therefore adds a state read. This ordinary native engine contract is retained only for audited same-target calls; arbitrary foreign callbacks, concurrent mutation or future source/dependency changes are not certified. Native custom/foreign virtual overrides retain their native meaning; this task does not replace them with a guessed material predicate.

Both TileEntityMachineOilWell calls at original lines 68 and 78 are intentionally untouched. Their receiver is read at `(posX + j, posY + 10, posZ + j)`, query context is `(posX + j, posY + 7, posZ + k)`, and write coordinates differ again. Calling the native base there would change the original mod behavior. Neither silently correcting the coordinates nor bypassing native dispatch is authorized by this migration. The entire excluded file is hash-pinned. The executable stone-receiver/air-target counterexample demonstrates why these two calls remain compiler failures.

Air, placement and hardness families are NOT part of this patch. BlockMultiSlab hardness delegation and other unresolved owner boundaries remain future work.

## Executed evidence

- 28 byte-exact before/after inverses and round trips, 36 exact edit ranges, 84 rejected source-drift mutations. CRLF is preserved, including changed lines that default git whitespace checks flag solely because of CRLF.
- Complete syntax-only source inventories and original/dependency ASM inventories. Parsing is not successful project attribution/compilation.
- Real Java 8 partial-source attribution identifies exactly net.minecraft.block.Block as receiver at all 30 selected calls before and after. Actual current OilDrillBase, MachineBase and LoadedBase ancestry sources, actual compiled WorldBlockState/ForgeDirection helpers and the original NTM artifact as a type/ancestry dependency resolve the scope. No synthetic receiver stand-in is used for this attribution. The 538 before / 504 after diagnostics are unrelated incomplete-source errors, not a passing build or a projected full-build count.
- Scoped native javac succeeds for genuine current BlockLayering, WorldBlockState, WorldAirRemoval and ForgeDirection, six complete method projections, and the test harness. Three explicitly test-only NTM type/registry fixtures stand in for ModBlocks, ZirnoxDestroyed and RBMKDebris solely to link actual BlockLayering; they are not production replacements or successful whole-owner compilation claims.
- 11,080 runtime assertions, including 5,392 actual native registry states using the actual Block base method; constant vanilla overrides; contextual snow and double-plant overrides; full actual BlockLayering nibble range 0..15 and its three special identities; virtual inherited dispatch; receiver/world/XYZ single evaluation, null receiver timing, argument exceptions and short-circuit behavior; raw integer extremes and excluded mismatch counterexample.
- Each of the six original NTM method bodies executes as an exact ASM-copied bytecode method projection against its complete native-source method projection, with poison/null IBlockAccess contexts and boundary coordinates. The six containing NTM classes and their full lifecycle are NOT compiled or executed. The projected inherited tests validate the method contract, while the 16 genuine class relationships are separately inventoried.

The actual native dependency inventory includes AE2 BlockCableBus and BlockPaint overrides. They remain normal native virtual dispatch; this test does not claim live AE2 cable/paint gameplay execution. Forge1614's original base hook is hash-pinned and checked directly in the supplied userdev archive. No proprietary binary is added to evidence.

## Reproduce offline

Run from any directory:

    python3 porting/tests/native-block-replaceability/run.py \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath-file /workspace/shared/nagaport-toolchain/native-classpath.txt \
      --original-jar /workspace/shared/nagaport/mods1710/HBM-NTM-.1.0.27_X5645_H261.jar \
      --forge1614 /tmp/nagaport-forge1614-userdev.jar \
      --verify-attribution

Paths in artifact-hashes.json pin actual retained artifacts, not downloads. The runner uses a temporary directory and no project source mutation. The parent must perform the combined full build; this evidence does not establish mod compilation, a playable JAR, startup, networking, integrated fluid/gas machines, radiation, or gameplay correctness.

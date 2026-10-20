# Twelve remaining block inventory spill callbacks

Exact base: a4739afa41966b75e2012bccd7df919e51d5c3a0 (CP73). Separate sparse worktree; no dependency, build-setting, resource, registry or remote changes.

## Production scope

BombMulti, NukeBoy, NukeCustom, NukeFleija, NukeGadget, NukeMan, NukeMike, NukeN2, NukePrototype, NukeSolinium, NukeTsar and MachineShredder. Each directly extends native BlockContainer with no named/anonymous source descendants. The full breakBlock callback becomes native (World, BlockPos, IBlockState), using old state owner for the comparator and the real native superclass removal. Original metadata is used only in the replaced superclass call, so no fabricated metadata conversion is needed.

Item, metadata and NBT references are captured before shrink; last-chunk construction therefore retains original identity, then native EntityItem.getItem receives a deep tag copy. Source stack object and source tag references remain in the slot; no slot-clearing behavior is invented. All original offsets, chunk ranges, float/Gaussian expression order, source decrement, constructor arguments, spawn ordering, keepInventory guards, comparator and super timing remain. In particular the original nonnull predicate stays: a nonnull zero/negative stack still consumes its three offset draws but emits nothing. Reusing ItemStackUtil.spillItems directly would lose those draws because that helper skips isEmpty stacks.

These getters target an immediately constructed EntityItem carrying a positive real ItemStack. The original getter's absent-watcher fresh-stone fallback is unreachable here; neither EMPTY nor explicit stored null is normalized. No universal getter shim, reflection, Unsafe or artificial count revival is introduced.

## Evidence and fixture boundaries

Recorded PASS: 26,640 native assertions plus five rejected executable mutants. The complete twelve production callback slices run through genuine World.setBlockState -> genuine Chunk -> virtual Block.breakBlock, actual EntityItem construction/data manager/getItem, native ItemStack/NBT and actual BlockContainer super removal. The world fixture intercepts spawn collection, lighting and comparator observations; real chunks/tile invalidation execute. Named production tile types are projected to a functional inventory fixture, and class-qualified RNG references are renamed with the callback owner. This is not whole-owner/tile compilation or end-to-end gameplay. No no-op superclass supplies proof.

The independent scalar reference implements original chunk/offset/motion expressions and separately tracks remaining count, including original zero-stack draw behavior. Source audit verifies each CP73 callback equals the pristine original callback after reversing only the already-existing BlockPos tile lookup. selection.json records exact old/new fragments and whole-file inverse hashes; changes outside the callback fail the guard. All twelve direct parents and no descendants were inspected across the full source tree.

Coverage: counts -1, 0, 1, 10, 31, 64, 130; three seeds; successful/rejected spawn; tagged/untagged stacks; nested deep copies; unchanged source aliases; total quantities; item and metadata including last depletion; complete coordinates/motions and RNG tail; keepInventory; direct/native engine dispatch; client Chunk behavior; null/EMPTY slots; missing/wrong tiles; exceptions after source decrement before comparator/removal. Runtime mutants reject late item lookup, shared NBT, isEmpty guard insertion, omitted superclass removal and duplicate spawns.

## Deliberate exclusions

ItemInventory.checkNBT and ItemToolBox.moveRows overflow owners remain unchanged. All other EntityItem read families remain unchanged. Existing manual six-argument breakBlock consumers in PWR/ICF/FEL and BlockDummyableBeam remain separate unfinished families. In particular PWRController's dynamically read Block receiver is not claimed closed by this callback work; its original six-argument call is already incompatible with native Block. No claim is made that the whole mod compiles, that arbitrary external legacy direct callers work, or that full block registration/state/GUI/explosion behavior is ported.

## Reproduce offline

From module root:

    python3 porting/tests/remaining-spill-native/run.py --java-home <jdk8> --classpath-file <cached-native-classpath> --mutants
    python3 porting/tests/remaining-spill-native/audit_source.py --original-source <pristine-original>/src/main/java --full-source <complete-port>/src/main/java

No downloads. Native classpath is existing Forge 2768 / MCP stable39. native-test.log records the run; original-scope-audit.json records the source audit. Parent owns independent full-build/whole-source attribution; no full-build claim is made here.

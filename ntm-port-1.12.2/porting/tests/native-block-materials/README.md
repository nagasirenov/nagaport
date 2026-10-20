# Native material constants and bounded Block material reads

Base: CP55 `2fa015374be0e330d2541e5bfe9d4b96e380aa05`. Production changes are 334 javac-owner-attributed Material constant identifiers and 256 no-argument calls on receivers attributed exactly as genuine `net.minecraft.block.Block`, in 255 files. Silo.java and DungeonToolbox.java are excluded. No fake platform APIs, global compatibility class, reflection in production, downloads, or full Gradle run.

## Exact native expression

Original `aji.o():awt` / `Block.getMaterial()` is a virtual direct load of the block's material field. Native `Block.getMaterial(IBlockState)` likewise directly loads material. Selected calls use `receiver.getDefaultState().getMaterial()`. The receiver is evaluated once and a null receiver still fails before a material value is produced. `getDefaultState()` is final/direct load; native state material access dispatches to its owning block. All current state containers and default-state setter origins are audited for ownership below. Source audit covers all 84 createBlockState methods, all 57 setDefaultState calls and all nine forwarding helpers. Dependency ASM dataflow covers all 110 setter calls and 128 state/container constructor calls, including all five slab branch joins. The owners are this, the already-owned default state, the owner forwarded by the standard constructor, the builder owner field, or the existing extended state owner. Actual native registry blocks and every valid native registry state additionally execute ownership/material checks. No null-state convention is introduced.

The scope is the pinned source and 67 actual available dependency JARs. ASM scans every class in those JARs: the only native Material-returning getMaterial declarations are Block, IBlockProperties, and StateImplementation. Source javac parsing finds no getMaterial declarations; the scanner refuses ALL block-call edits if any source declaration appears. The legacy client has the Block getter plus unrelated awt.o() (Material mobility setter), not another block getter. Forge1614 has no Material patch and does not modify the Block getter declaration/body. `artifact-hashes.json`, bytecode excerpts, SRG identities and dependency inventory pin this evidence.

This is not a universal guarantee for arbitrary future/external mods that add stateful material overrides. Such changes require re-audit; the expression must not be blindly reused. Exact Block-subclass receiver types, unresolved receivers, existing IBlockState.getMaterial(), and unrelated getMaterial APIs are not selected. Existing unfinished MaterialGas overrides and other prior porting defects are NOT fixed or certified by this accessor migration.

## Constants

`mapping.tsv` records original source name, native field, old obfuscated field and stable SRG identity. The real old and native objects are checked for static-final identity, eight gameplay/material predicates, push mobility, and both map palette index and RGB. All pairwise singleton equalities are checked. Material is not a block registry: no material is replaced with a visually similar registry block or guessed replacement.

WOOD/GROUND/CLOTH stay excluded under the existing visual policy gate. The oracle also discovered and excludes WEB/CARPET (index 3 RGB 10987431 -> 13092807) and SPONGE (index 3 -> 18, RGB 10987431 -> 15066419). No excluded constant identifier is renamed. Comments, strings, OBJModel.Material, ArmorMaterial, static-import bare identifiers, wildcard imports and shadows are not accepted by the scanner.

## Executed evidence

- 1,148 actual old-client/native Material checks: static-final identities, eight predicates, mobility, map index/RGB and pairwise singleton identity.
- 5,855 actual old/native Block and registry-state checks for all 28 approved materials: returned object identity, one receiver evaluation, null failure, and complete extracted NTM ExplosionNukeRayParallelized.getNukeResistance. Actual sandstone, obsidian and stone branches execute as well. This is a complete extracted method, not whole explosion/gameplay execution. Old wrapper changes only access visibility and required obfuscated API spellings; an explicit `(sa)null` resolves old obfuscated overload collisions.
- 17 real-javac ownership/shadow/import/Unicode/state-receiver/side-effecting-receiver fixtures, including rejection when a source stateful getMaterial override appears.
- Exact UTF-16-position inverse and SHA256 before/after for every selected file; comments, line endings and all unrelated source bytes remain unchanged.
- `--verify-attribution` reconstructs originals in temporary storage and re-runs attribution over all source inputs except the two reserved files. 23,598 incomplete-source attribution diagnostics are explicitly NOT successful full compilation.

Run with existing Java 8 and classpath:

    python3 porting/tests/native-block-materials/run.py --java-home JAVA8 --classpath NATIVE_CLASSPATH --legacy-client ORIGINAL_1710_CLIENT_JAR --verify-attribution

Full mod build, block lifecycle, world loading, networking, chemistry/radiation gameplay and live client/server acceptance remain unverified. Constants/accessor evidence does not certify all containing methods or every implementation's behavior between Minecraft versions. Existing checkpoint source/in-game gates remain in force.

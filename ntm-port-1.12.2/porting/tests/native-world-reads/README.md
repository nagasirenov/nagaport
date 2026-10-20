# Bounded native World state reads (CP54 candidate)

Base: CP52 `e0d436953a26ba3e7cb58effa31adc48282eb501`. No main-worktree edits.

## Production scope

1,030 calls across 323 existing production files: 804 `World.getBlock(int,int,int)` and 226 `World.getBlockMetadata(int,int,int)` calls. The compiler attributes the receiver to exactly native `net.minecraft.world.World`, a variable element, with three resolved int expressions. IBlockAccess, ChunkCache, subclasses/custom declared receiver types, unresolved arguments and non-int arguments remain untouched. This intentionally does not exhaust all newly exposed World fields from other CP53 work.

The only new production class, `com.hbm.world.WorldBlockState`, is a narrow native IBlockState read boundary. It rejects coordinates outside original X/Z [-30,000,000,30,000,000) and Y [0,256), returning genuine AIR state. It checks null after caller argument evaluation and before bounds. In-bounds reads use real native World.getBlockState. Block reads use that state's getBlock; metadata uses that state's own block.getMetaFromState, once per read. No World replacement, legacy methods injected into Minecraft, platform stubs, source exclusions or downloads.

The original ahb.a/getBlock and ahb.e/getBlockMetadata bytecode is included in boundary-bytecode.txt. Native World.getBlockState only checks height, so blindly replacing reads with BlockPos would alter X/Z semantics.

## Reproduce

From this directory:

    python3 probe.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --original-client /tmp/nagaport-legacy-client.jar --attribute --mutants

The Java home, genuine native dependency classpath and original client are arguments, not downloaded or fabricated by the test. Original client SHA256: a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce.

Tests execute real native WorldServer/ChunkProviderServer/Chunk/ExtendedBlockStorage, and actual original World/Chunk/ExtendedBlockStorage bytecode through an isolated loader. Only the original chunk-provider interface is a recording seam; no World or Chunk implementations are substituted for those tests. Unsafe bypasses unrelated bootstrap constructors. Direct original evaluation tests invoke actual original methods; only the generated test class's binary name is moved to a named package to avoid mixed signer certificates in the default package, leaving every original client byte unchanged.

Five complete actual production method bodies are extracted at execution time: FusionTorusStruct.gbr/gmr, NTSapling.getDamageValue, TallPlant.getDamageValue and Crop.func_149851_a. Their native runtime tests include relative coordinates, integer overflow, identity, owner metadata, remainder and growth predicates. Extraction is explicit, not a claim that their entire unported enclosing classes compile.

Negative attribution controls cover exact ownership, custom/subclass/interface receivers, arity, unresolved arithmetic and numeric types. Runtime mutants change lower/upper X bound, upper Y bound, null check, coordinate ordering and metadata. All must fail. Full-source attribution compares exact file/UTF16 invocation positions before and after, not just source regex counts. It reports expected unfinished-port diagnostics; it is not a successful full build.

## Fragment integration

    python3 merge.py --target /path/to/ntm-port-1.12.2
    python3 merge.py --target /path/to/ntm-port-1.12.2 --apply

All files are preflighted before any write. Only exact receiver/call prefixes and balanced closing delimiters change; every argument interior is retained from the target. Unique baseline left-context anchors resolve new identical calls introduced by concurrent world-field migration; otherwise exact full invocation/count/ordinal, then exact prefix/count/ordinal is required. Counts or ambiguous changes fail closed. New helper/evidence files may only be added or byte-identical. Do not cherry-pick complete overlapping production files over other workers' changes. Re-run native attribution on the integrated snapshot. For the runtime suite against integrated sources, add `--integrated` and omit `--attribute`; this checks per-file read counts and executes the same real production method tests without asserting unrelated baseline whole-file hashes.

`audit.py` proves byte-exact baseline reconstruction and target regeneration for every existing production file. Existing indentation/trailing whitespace is intentionally retained, including historical diff-check warnings.

## Explicit limits

Metadata is genuine native state-owner metadata. Arbitrary raw legacy metadata nibbles which cannot be represented by a native blockstate are not synthesized, and broad custom-block metadata migration is not certified. The original raw metadata behavior is characterized separately. Original crash-report wrapping and message identity are not recreated; receiver/argument exception timing and native lookup semantics are preserved. No whole affected-class, complete generator, gameplay, world-save, rendering or launched-game parity claim. This batch does not change the outstanding CP37 lighting gate or boss HUD decision.
